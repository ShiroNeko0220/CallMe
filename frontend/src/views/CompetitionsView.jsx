import { useState, useEffect } from 'react'
import { Trophy, Calendar, MapPin, RefreshCw } from 'lucide-react'
import { api } from '../api'
import { Card, Btn, Input, Alert, Spinner } from '../components/Card'

const dateMin7 = () => {
  const d = new Date()
  d.setDate(d.getDate() + 8)
  return d.toISOString().split('T')[0]
}

export default function CompetitionsView({ role, currentUser }) {
  const [competitions,     setCompetitions]     = useState([])
  const [loading,          setLoading]          = useState(true)
  const [loadingResultats, setLoadingResultats] = useState(false)
  const [selected,         setSelected]         = useState(null)
  const [resultats,        setResultats]        = useState([])
  const [alert,            setAlert]            = useState(null)
  const [showForm,         setShowForm]         = useState(false)
  const [showResForm,      setShowResForm]      = useState(false)

  const niveauMaxEnseignant = Number(currentUser?.niveauExpertise || 1)
  const niveauxCreation = role === 'ENSEIGNANT'
      ? Array.from({ length: niveauMaxEnseignant }, (_, i) => i + 1)
      : [1, 2, 3, 4, 5]

  const [form, setForm] = useState({
    titre: '', niveauCible: 1, date: '', heureDebut: '', duree: 90, lieu: '', enseignantId: '',
  })
  const [resForm, setResForm] = useState({ eleveId: '', note: 5 })

  useEffect(() => {
    if (role === 'ENSEIGNANT' && currentUser?.id) {
      setForm(p => ({
        ...p,
        enseignantId: currentUser.id,
        niveauCible: Math.min(Number(p.niveauCible || 1), niveauMaxEnseignant),
      }))
    }
  }, [role, currentUser?.id, currentUser?.niveauExpertise])

  useEffect(() => { charger() }, [role, currentUser?.id, currentUser?.niveauExpertise])

  const charger = async () => {
    setLoading(true)
    try {
      let res
      if (role === 'MEMBRE' && currentUser?.id) {
        res = await api.competitions.listerPourEleve(currentUser.id)
      } else if (role === 'ENSEIGNANT' && currentUser?.id) {
        res = await api.competitions.listerParEnseignant(currentUser.id)
      } else {
        res = await api.competitions.lister()
      }
      setCompetitions(res.data)
    } catch (e) {
      setAlert({ type: 'error', message: 'Impossible de charger les compétitions. Veuillez réessayer.' })
    } finally {
      setLoading(false)
    }
  }

  const voirResultats = async (c) => {
    setSelected(c)
    setShowResForm(false)
    setLoadingResultats(true)
    try {
      const res = await api.competitions.listerResultats(c.id)
      setResultats(res.data)
    } catch {
      setResultats([])
    } finally {
      setLoadingResultats(false)
    }
  }

  const creer = async () => {
    const payload = {
      ...form,
      niveauCible: Number(form.niveauCible),
      duree: Number(form.duree),
      enseignantId: role === 'ENSEIGNANT' ? Number(currentUser?.id) : Number(form.enseignantId),
    }

    if (!payload.titre.trim()) return setAlert({ type: 'error', message: 'Le titre de la compétition est obligatoire.' })
    if (!payload.date) return setAlert({ type: 'error', message: 'La date est obligatoire (au moins 7 jours à l\'avance).' })
    if (!payload.enseignantId) return setAlert({ type: 'error', message: 'Veuillez indiquer l\'enseignant responsable.' })
    if (role === 'ENSEIGNANT' && payload.niveauCible > niveauMaxEnseignant) {
      return setAlert({ type: 'error', message: 'Un enseignant ne peut créer qu’une compétition de son niveau ou d’un niveau inférieur.' })
    }

    try {
      await api.competitions.creer(payload, role, currentUser?.id)
      setAlert({ type: 'success', message: 'Compétition créée avec succès !' })
      setShowForm(false)
      setForm({ titre: '', niveauCible: role === 'ENSEIGNANT' ? 1 : 1, date: '', heureDebut: '', duree: 90, lieu: '', enseignantId: role === 'ENSEIGNANT' ? currentUser?.id : '' })
      charger()
    } catch (e) {
      const msg = e.response?.data?.error
      setAlert({ type: 'error', message: msg || 'Impossible de créer la compétition. Vérifiez que l\'enseignant est apte pour le niveau choisi.' })
    }
  }

  const ajouterResultat = async () => {
    if (!selected) return
    if (!resForm.eleveId) return setAlert({ type: 'error', message: 'Veuillez indiquer le numéro du membre.' })

    try {
      await api.competitions.ajouterResultat(selected.id, {
        eleveId: Number(resForm.eleveId),
        note: Number(resForm.note),
      }, role, currentUser?.id)
      setAlert({ type: 'success', message: 'Résultat enregistré !' })
      setShowResForm(false)
      setResForm({ eleveId: '', note: 5 })
      voirResultats(selected)
    } catch (e) {
      const status = e.response?.status
      const msg = e.response?.data?.error
      if (status === 409) {
        setAlert({ type: 'error', message: 'Un résultat existe déjà pour ce membre dans cette compétition.' })
      } else if (status === 403) {
        setAlert({ type: 'error', message: msg || 'Vous n’êtes pas responsable de cette compétition.' })
      } else if (status === 400) {
        setAlert({ type: 'error', message: msg || 'Le membre ne correspond pas au niveau de cette compétition.' })
      } else {
        setAlert({ type: 'error', message: msg || 'Impossible d\'enregistrer le résultat. Veuillez réessayer.' })
      }
    }
  }

  const supprimer = async (id) => {
    if (!confirm('Supprimer cette compétition définitivement ?')) return
    try {
      await api.competitions.supprimer(id, role)
      setAlert({ type: 'success', message: 'Compétition supprimée.' })
      setSelected(null)
      charger()
    } catch (e) {
      setAlert({ type: 'error', message: 'Impossible de supprimer cette compétition. Veuillez réessayer.' })
    }
  }

  const f = (field, val) => setForm(p => ({ ...p, [field]: val }))
  const rf = (field, val) => setResForm(p => ({ ...p, [field]: val }))

  return (
      <div>
        <div className="flex items-center justify-between mb-5">
          <h1 className="text-xl font-bold text-gray-800 flex items-center gap-2">
            <Trophy size={20} className="text-blue-600" /> Compétitions
          </h1>
          {['ENSEIGNANT', 'PRESIDENT'].includes(role) && (
              <Btn onClick={() => setShowForm(!showForm)}>+ Nouvelle compétition</Btn>
          )}
        </div>

        <Alert {...alert} onClose={() => setAlert(null)} />

        {showForm && (
            <Card title="Créer une compétition">
              <div className="grid grid-cols-2 gap-x-4">
                <Input label="Titre" value={form.titre} onChange={e => f('titre', e.target.value)} placeholder="ex. Championnat régional" />
                <Input label="Date (au moins 7 jours à l'avance)" value={form.date} onChange={e => f('date', e.target.value)} type="date" min={dateMin7()} />
                <Input label="Heure de début" value={form.heureDebut} onChange={e => f('heureDebut', e.target.value)} type="time" />
                <Input label="Durée en minutes" value={form.duree} onChange={e => f('duree', Number(e.target.value))} type="number" min="45" />
                <Input label="Lieu" value={form.lieu} onChange={e => f('lieu', e.target.value)} placeholder="ex. Palais des sports" />
                {role !== 'ENSEIGNANT' && (
                    <Input label="Numéro de l'enseignant" value={form.enseignantId} onChange={e => f('enseignantId', Number(e.target.value))} type="number" min="1" placeholder="ex. 3" />
                )}
                {role === 'ENSEIGNANT' && (
                    <div className="mb-3">
                      <label className="block text-sm text-gray-600 mb-1">Enseignant responsable</label>
                      <div className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm bg-gray-50 text-gray-600">
                        {currentUser?.prenom} {currentUser?.nom} — ID #{currentUser?.id}
                      </div>
                    </div>
                )}
                <div className="mb-3">
                  <label className="block text-sm text-gray-600 mb-1">Niveau cible</label>
                  <select value={form.niveauCible} onChange={e => f('niveauCible', Number(e.target.value))}
                          className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-300">
                    {niveauxCreation.map(n => <option key={n} value={n}>Niveau {n}</option>)}
                  </select>
                  {role === 'ENSEIGNANT' && <p className="text-xs text-gray-400 mt-1">Vous pouvez créer une compétition de votre niveau ou d’un niveau inférieur.</p>}
                </div>
              </div>
              <div className="flex gap-2 mt-2">
                <Btn onClick={creer}>Créer</Btn>
                <Btn variant="outline" onClick={() => setShowForm(false)}>Annuler</Btn>
              </div>
            </Card>
        )}

        <div className="grid grid-cols-2 gap-4">
          <Card title={`${competitions.length} compétition(s) affichée(s)`} action={<button onClick={charger} className="p-1.5 rounded bg-blue-50 hover:bg-blue-100 text-blue-500 hover:text-blue-700 cursor-pointer" title="Actualiser"><RefreshCw size={15} /></button>}>
            {loading ? <Spinner /> : <div className="space-y-2">
              {competitions.map(c => (
                  <div key={c.id}
                       onClick={() => voirResultats(c)}
                       className={`p-3 rounded-lg cursor-pointer transition-colors ${
                           selected?.id === c.id ? 'bg-blue-50 border border-blue-200' : 'bg-gray-50 hover:bg-gray-100'
                       }`}
                  >
                    <div className="font-medium text-sm">{c.titre}</div>
                    <div className="text-xs text-gray-500 mt-1 flex items-center gap-3">
                      <span className="flex items-center gap-1"><Calendar size={12} className="text-blue-500" />{c.date}</span>
                      <span>Niv. {c.niveauCible}</span>
                      <span className="flex items-center gap-1"><MapPin size={12} className="text-blue-500" />{c.lieu || '-'}</span>
                    </div>
                    {role === 'PRESIDENT' && (
                        <button
                            onClick={e => { e.stopPropagation(); supprimer(c.id) }}
                            className="text-xs text-red-500 hover:text-red-700 mt-1 cursor-pointer"
                        >Supprimer</button>
                    )}
                  </div>
              ))}
              {competitions.length === 0 && <p className="text-gray-400 text-sm">Aucune compétition.</p>}
            </div>}
          </Card>

          <Card
              title={selected ? `Résultats — ${selected.titre}` : 'Sélectionnez une compétition'}
              action={selected && role === 'ENSEIGNANT' && Number(selected.enseignantId) === Number(currentUser?.id) && (
                  <Btn size="sm" onClick={() => setShowResForm(!showResForm)}>+ Résultat</Btn>
              )}
          >
            {showResForm && (
                <div className="mb-4 p-3 bg-gray-50 rounded-lg">
                  <Input label="Numéro du membre" value={resForm.eleveId} onChange={e => rf('eleveId', e.target.value)} type="number" min="1" placeholder="ex. 5" />
                  <Input label="Note (0 à 10)" value={resForm.note} onChange={e => rf('note', e.target.value)} type="number" min="0" max="10" step="0.1" />
                  <div className="flex gap-2">
                    <Btn size="sm" onClick={ajouterResultat}>Enregistrer</Btn>
                    <Btn size="sm" variant="outline" onClick={() => setShowResForm(false)}>Annuler</Btn>
                  </div>
                </div>
            )}

            {loadingResultats ? <Spinner /> : resultats.length > 0 ? (
                <table className="w-full text-sm">
                  <thead>
                  <tr className="text-left text-gray-500 border-b">
                    <th className="pb-2">Membre</th>
                    <th className="pb-2">Note</th>
                  </tr>
                  </thead>
                  <tbody>
                  {resultats.map((r, i) => (
                      <tr key={i} className="border-b border-gray-50">
                        <td className="py-2">Membre #{r.eleveId}</td>
                        <td className="py-2 font-bold text-blue-600">{r.note} / 10</td>
                      </tr>
                  ))}
                  </tbody>
                </table>
            ) : selected ? (
                <p className="text-gray-400 text-sm">Aucun résultat enregistré pour cette compétition.</p>
            ) : (
                <p className="text-gray-300 text-sm text-center py-6">Cliquez sur une compétition pour voir ses résultats</p>
            ) }
          </Card>
        </div>
      </div>
  )
}
