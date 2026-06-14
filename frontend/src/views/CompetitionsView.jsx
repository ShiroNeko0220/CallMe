import { useState, useEffect } from 'react'
import { Trophy, Calendar, MapPin, Trash2, RefreshCw } from 'lucide-react'
import { api } from '../api'
import { Card, Btn, Input, Alert, Spinner, ConfirmModal } from '../components/Card'

const dateMin7 = () => {
  const d = new Date()
  d.setDate(d.getDate() + 8)
  return d.toISOString().split('T')[0]
}

const selectCls = "w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300 bg-white"

export default function CompetitionsView({ role }) {
  const [competitions,     setCompetitions]     = useState([])
  const [enseignants,      setEnseignants]      = useState([])
  const [membres,          setMembres]          = useState([])
  const [loading,          setLoading]          = useState(true)
  const [loadingResultats, setLoadingResultats] = useState(false)
  const [selected,         setSelected]         = useState(null)
  const [resultats,        setResultats]        = useState([])
  const [alert,            setAlert]            = useState(null)
  const [showForm,         setShowForm]         = useState(false)
  const [showResForm,      setShowResForm]      = useState(false)
  const [confirm,          setConfirm]          = useState(null)
  const [form, setForm] = useState({
    titre: '', niveauCible: 1, date: '', heureDebut: '',
    duree: 90, lieu: '', enseignantId: '',
  })
  const [resForm, setResForm] = useState({ eleveId: '', enseignantId: '', note: 5 })

  useEffect(() => {
    charger()
    api.utilisateurs.lister(role).then(r => {
      const tous = r.data
      setEnseignants(tous.filter(u => u.role === 'ENSEIGNANT'))
      setMembres(tous)
    }).catch(() => {})
  }, [])

  const nomMembre = (id) => {
    const m = membres.find(m => m.id === Number(id))
    return m ? `${m.prenom} ${m.nom}` : `Membre #${id}`
  }
  const nomEnseignant = (id) => {
    const e = enseignants.find(e => e.id === Number(id))
    return e ? `${e.prenom} ${e.nom}` : `Enseignant #${id}`
  }

  const charger = async () => {
    setLoading(true)
    try {
      const res = await api.competitions.lister()
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
    if (!form.titre.trim()) return setAlert({ type: 'error', message: 'Le titre de la compétition est obligatoire.' })
    if (!form.date) return setAlert({ type: 'error', message: 'La date est obligatoire (au moins 7 jours à l\'avance).' })
    if (!form.enseignantId) return setAlert({ type: 'error', message: 'Veuillez indiquer le numéro de l\'enseignant responsable.' })
    try {
      await api.competitions.creer(form, role)
      setAlert({ type: 'success', message: 'Compétition créée avec succès !' })
      setShowForm(false)
      charger()
    } catch (e) {
      const msg = e.response?.data?.error
      setAlert({ type: 'error', message: msg || 'Impossible de créer la compétition. Vérifiez que l\'enseignant est apte pour le niveau choisi.' })
    }
  }

  const ajouterResultat = async () => {
    if (!resForm.eleveId) return setAlert({ type: 'error', message: 'Veuillez indiquer le numéro du membre.' })
    if (!resForm.enseignantId) return setAlert({ type: 'error', message: 'Veuillez indiquer le numéro de l\'enseignant.' })
    try {
      await api.competitions.ajouterResultat(selected.id, {
        competitionId: selected.id,
        eleveId: Number(resForm.eleveId),
        enseignantId: Number(resForm.enseignantId),
        note: Number(resForm.note),
      }, role)
      setAlert({ type: 'success', message: 'Résultat enregistré !' })
      setShowResForm(false)
      voirResultats(selected)
    } catch (e) {
      const status = e.response?.status
      const msg = e.response?.data?.error
      if (status === 409) {
        setAlert({ type: 'error', message: 'Un résultat existe déjà pour ce membre dans cette compétition.' })
      } else if (status === 403) {
        setAlert({ type: 'error', message: msg || 'Cet enseignant n\'est pas habilité pour le niveau de cette compétition.' })
      } else if (status === 400) {
        setAlert({ type: 'error', message: msg || 'Le membre ne correspond pas au niveau de cette compétition.' })
      } else {
        setAlert({ type: 'error', message: msg || 'Impossible d\'enregistrer le résultat. Veuillez réessayer.' })
      }
    }
  }

  const supprimer = (id) => {
    setConfirm({ message: 'Supprimer cette compétition définitivement ?', onConfirm: async () => {
      setConfirm(null)
      try {
        await api.competitions.supprimer(id, role)
        setAlert({ type: 'success', message: 'Compétition supprimée.' })
        setSelected(null)
        charger()
      } catch (e) {
        setAlert({ type: 'error', message: 'Impossible de supprimer cette compétition. Veuillez réessayer.' })
      }
    }})
  }

  const f = (field, val) => setForm(p => ({ ...p, [field]: val }))
  const rf = (field, val) => setResForm(p => ({ ...p, [field]: val }))
  const posInt = (val) => { const n = parseInt(val); return isNaN(n) || n < 1 ? '' : n }

  return (
    <div>
      <ConfirmModal
        message={confirm?.message}
        onConfirm={confirm?.onConfirm}
        onCancel={() => setConfirm(null)}
      />

      <div className="flex items-center justify-between mb-5">
        <h1 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <Trophy size={20} className="text-blue-600" /> Compétitions
        </h1>
        {['ENSEIGNANT', 'PRESIDENT'].includes(role) && (
          <Btn onClick={() => setShowForm(!showForm)}>+ Nouvelle compétition</Btn>
        )}
      </div>

      <Alert {...alert} onClose={() => setAlert(null)} />

      {showForm && ['ENSEIGNANT', 'PRESIDENT'].includes(role) && (
        <Card title="Créer une compétition">
          <p className="text-xs text-gray-400 mb-3"><span className="text-red-500">*</span> Champ obligatoire</p>
          <div className="grid grid-cols-2 gap-x-4">
            <Input label="Titre" required value={form.titre} onChange={e => f('titre', e.target.value)} placeholder="ex. Championnat régional" />
            <Input label="Date (au moins 7 jours à l'avance)" required value={form.date} onChange={e => f('date', e.target.value)} type="date" min={dateMin7()} />
            <Input label="Heure de début" required value={form.heureDebut} onChange={e => f('heureDebut', e.target.value)} type="time" />
            <Input label="Durée en minutes" required value={form.duree}
              onChange={e => { const n = parseInt(e.target.value); f('duree', isNaN(n) || n < 1 ? 1 : n) }}
              type="number" min="1" />
            <Input label="Lieu" optional value={form.lieu} onChange={e => f('lieu', e.target.value)} placeholder="ex. Palais des sports" />
            <div className="mb-3">
              <label className="block text-sm text-gray-600 mb-1">Enseignant <span className="text-red-500">*</span></label>
              <select value={form.enseignantId} onChange={e => f('enseignantId', Number(e.target.value))} className={selectCls}>
                <option value="">-- Choisir un enseignant --</option>
                {enseignants.map(e => (
                  <option key={e.id} value={e.id}>{e.prenom} {e.nom} - Niv. {e.niveauExpertise}</option>
                ))}
              </select>
            </div>
            <div className="mb-3">
              <label className="block text-sm text-gray-600 mb-1">Niveau cible <span className="text-red-500">*</span></label>
              <select value={form.niveauCible} onChange={e => f('niveauCible', Number(e.target.value))}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-300">
                {[1, 2, 3, 4, 5].map(n => <option key={n} value={n}>Niveau {n}</option>)}
              </select>
            </div>
          </div>
          <div className="flex gap-2 mt-2">
            <Btn onClick={creer}>Créer</Btn>
            <Btn variant="outline" onClick={() => setShowForm(false)}>Annuler</Btn>
          </div>
        </Card>
      )}

      <div className="grid grid-cols-2 gap-4">
        <Card title={`${competitions.length} compétitions`} action={<button onClick={charger} className="p-1.5 rounded bg-blue-50 hover:bg-blue-100 text-blue-500 hover:text-blue-700 cursor-pointer" title="Actualiser"><RefreshCw size={15} /></button>}>
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
                  <span className="flex items-center gap-1"><MapPin size={12} className="text-blue-500" />{c.lieu}</span>
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
          title={selected ? `Résultats - ${selected.titre}` : 'Sélectionnez une compétition'}
          action={selected && ['ENSEIGNANT', 'PRESIDENT'].includes(role) && (
            <Btn size="sm" onClick={() => setShowResForm(!showResForm)}>+ Résultat</Btn>
          )}
        >
          {showResForm && (
            <div className="mb-4 p-3 bg-gray-50 rounded-lg">
              <p className="text-xs text-gray-400 mb-2"><span className="text-red-500">*</span> Champ obligatoire</p>
              <Input label="Numéro du membre" required value={resForm.eleveId}
                onChange={e => rf('eleveId', posInt(e.target.value))}
                type="number" min="1" placeholder="ex. 5" />
              <div className="mb-3">
                <label className="block text-sm text-gray-600 mb-1">Enseignant <span className="text-red-500">*</span></label>
                <select value={resForm.enseignantId} onChange={e => rf('enseignantId', Number(e.target.value))} className={selectCls}>
                  <option value="">-- Choisir un enseignant --</option>
                  {enseignants.map(e => (
                    <option key={e.id} value={e.id}>{e.prenom} {e.nom} - Niv. {e.niveauExpertise}</option>
                  ))}
                </select>
              </div>
              <div className="mb-3">
                <label className="block text-sm text-gray-600 mb-1">Note (0 à 10) <span className="text-red-500">*</span></label>
                <input type="range" min="0" max="10" step="0.5" value={resForm.note}
                  onChange={e => rf('note', e.target.value)} className="w-full accent-blue-600" />
                <span className="text-sm font-medium text-blue-600">{resForm.note} / 10</span>
              </div>
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
                  <th className="pb-2">Enseignant</th>
                </tr>
              </thead>
              <tbody>
                {resultats.map((r, i) => (
                  <tr key={i} className="border-b border-gray-50">
                    <td className="py-2">{nomMembre(r.eleveId)}</td>
                    <td className="py-2 font-bold text-blue-600">{r.note} / 10</td>
                    <td className="py-2 text-gray-400 text-xs">{nomEnseignant(r.enseignantId)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : selected ? (
            <p className="text-gray-400 text-sm">Aucun résultat enregistré pour cette compétition.</p>
          ) : (
            <p className="text-gray-300 text-sm text-center py-6">Cliquez sur une compétition pour voir ses résultats</p>
          )}
        </Card>
      </div>
    </div>
  )
}
