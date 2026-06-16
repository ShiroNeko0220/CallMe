import { useState, useEffect } from 'react'
import { Trophy, Calendar, MapPin, RefreshCw } from 'lucide-react'
import { api } from '../api'
import { Card, Btn, Input, Alert, Spinner } from '../components/Card'

const dateMin7 = () => {
  const d = new Date()
  d.setDate(d.getDate() + 8)
  return d.toISOString().split('T')[0]
}

const selectCls = "w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-300 bg-white"
const heure = (v) => v ? String(v).slice(0, 5) : ''

export default function CompetitionsView({ role, currentUser }) {
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
  const [editCompetitionId, setEditCompetitionId] = useState(null)
  const [editForm,          setEditForm]          = useState(null)
  const [editResultatId,    setEditResultatId]    = useState(null)
  const [editResForm,       setEditResForm]       = useState(null)

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

  useEffect(() => {
    charger()
    if (['ENSEIGNANT', 'PRESIDENT'].includes(role)) {
      api.utilisateurs.lister(role)
          .then(r => {
            setEnseignants(r.data.filter(u => u.role === 'ENSEIGNANT'))
            setMembres(r.data.filter(u => u.role === 'MEMBRE'))
          })
          .catch(() => {})
    }
  }, [role, currentUser?.id, currentUser?.niveauExpertise])

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
    setEditResultatId(null)
    setEditResForm(null)
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

  const peutGererCompetition = (competition = selected) => (
      competition && (
          role === 'PRESIDENT' ||
          (role === 'ENSEIGNANT' && Number(competition.enseignantId) === Number(currentUser?.id))
      )
  )

  const validerCompetition = (payload) => {
    if (!payload.titre?.trim()) return 'Le titre de la compétition est obligatoire.'
    if (!payload.date) return 'La date est obligatoire (au moins 7 jours à l’avance).'
    if (!payload.heureDebut) return 'L’heure de début est obligatoire.'
    if (!payload.duree || payload.duree < 45) return 'La durée doit être au moins de 45 minutes.'
    if (!payload.enseignantId) return 'Veuillez indiquer l’enseignant responsable.'
    if (role === 'ENSEIGNANT' && payload.niveauCible > niveauMaxEnseignant) {
      return 'Un enseignant ne peut créer/modifier qu’une compétition de son niveau ou d’un niveau inférieur.'
    }
    return null
  }

  const creer = async () => {
    const payload = {
      ...form,
      niveauCible: Number(form.niveauCible),
      duree: Number(form.duree),
      enseignantId: role === 'ENSEIGNANT' ? Number(currentUser?.id) : Number(form.enseignantId),
    }

    const erreur = validerCompetition(payload)
    if (erreur) return setAlert({ type: 'error', message: erreur })

    try {
      await api.competitions.creer(payload, role, currentUser?.id)
      setAlert({ type: 'success', message: 'Compétition créée avec succès !' })
      setShowForm(false)
      setForm({ titre: '', niveauCible: 1, date: '', heureDebut: '', duree: 90, lieu: '', enseignantId: role === 'ENSEIGNANT' ? currentUser?.id : '' })
      charger()
    } catch (e) {
      const msg = e.response?.data?.error
      setAlert({ type: 'error', message: msg || 'Impossible de créer la compétition. Vérifiez que l’enseignant est apte pour le niveau choisi.' })
    }
  }

  const ouvrirEditionCompetition = (c) => {
    setShowForm(false)
    setEditCompetitionId(c.id)
    setEditForm({
      titre: c.titre || '',
      niveauCible: c.niveauCible || 1,
      date: c.date || '',
      heureDebut: heure(c.heureDebut),
      duree: c.duree || 90,
      lieu: c.lieu || '',
      enseignantId: c.enseignantId || '',
    })
  }

  const modifierCompetition = async () => {
    const payload = {
      ...editForm,
      niveauCible: Number(editForm.niveauCible),
      duree: Number(editForm.duree),
      enseignantId: role === 'ENSEIGNANT' ? Number(currentUser?.id) : Number(editForm.enseignantId),
    }

    const erreur = validerCompetition(payload)
    if (erreur) return setAlert({ type: 'error', message: erreur })

    try {
      const res = await api.competitions.modifier(editCompetitionId, payload, role, currentUser?.id)
      setAlert({ type: 'success', message: 'Compétition modifiée.' })
      setEditCompetitionId(null)
      setEditForm(null)
      charger()
      if (selected?.id === editCompetitionId) setSelected(res.data)
    } catch (e) {
      const msg = e.response?.data?.error
      setAlert({ type: 'error', message: msg || 'Impossible de modifier cette compétition.' })
    }
  }

  const ajouterResultat = async () => {
    if (!selected) return
    if (!resForm.eleveId) return setAlert({ type: 'error', message: 'Veuillez choisir un membre.' })
    if (resForm.note === '' || resForm.note === null) return setAlert({ type: 'error', message: 'La note est obligatoire.' })

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
        setAlert({ type: 'error', message: msg || 'Impossible d’enregistrer le résultat. Veuillez réessayer.' })
      }
    }
  }

  const ouvrirEditionResultat = (r) => {
    setShowResForm(false)
    setEditResultatId(r.id)
    setEditResForm({ eleveId: r.eleveId, note: r.note })
  }

  const modifierResultat = async () => {
    if (!editResultatId) return
    if (!editResForm.eleveId) return setAlert({ type: 'error', message: 'Veuillez choisir un membre.' })
    if (editResForm.note === '' || editResForm.note === null) return setAlert({ type: 'error', message: 'La note est obligatoire.' })

    try {
      await api.competitions.modifierResultat(editResultatId, {
        eleveId: Number(editResForm.eleveId),
        note: Number(editResForm.note),
      }, role, currentUser?.id)
      setAlert({ type: 'success', message: 'Résultat modifié.' })
      setEditResultatId(null)
      setEditResForm(null)
      voirResultats(selected)
    } catch (e) {
      setAlert({ type: 'error', message: e.response?.data?.error || 'Impossible de modifier le résultat.' })
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
      setAlert({ type: 'error', message: e.response?.data?.error || 'Impossible de supprimer cette compétition. Veuillez réessayer.' })
    }
  }

  const f = (field, val) => setForm(p => ({ ...p, [field]: val }))
  const ef = (field, val) => setEditForm(p => ({ ...p, [field]: val }))
  const rf = (field, val) => setResForm(p => ({ ...p, [field]: val }))
  const erf = (field, val) => setEditResForm(p => ({ ...p, [field]: val }))

  const membresResultat = selected
      ? membres.filter(m => Number(m.niveauExpertise) === Number(selected.niveauCible))
      : membres

  const nomMembre = (id) => {
    const m = membres.find(m => Number(m.id) === Number(id))
    return m ? `${m.prenom} ${m.nom}` : `Membre #${id}`
  }

  const formulaireCompetition = (data, setField, onSubmit, titre, submitLabel, onCancel) => (
      <Card title={titre}>
        <p className="text-xs text-gray-400 mb-3"><span className="text-red-500">*</span> Champ obligatoire</p>
        <div className="grid grid-cols-2 gap-x-4">
          <Input label="Titre" required value={data.titre} onChange={e => setField('titre', e.target.value)} placeholder="ex. Championnat régional" />
          <Input label="Date (au moins 7 jours à l'avance)" required value={data.date} onChange={e => setField('date', e.target.value)} type="date" min={dateMin7()} />
          <Input label="Heure de début" required value={data.heureDebut} onChange={e => setField('heureDebut', e.target.value)} type="time" />
          <Input label="Durée en minutes" required value={data.duree} onChange={e => { const n = parseInt(e.target.value); setField('duree', isNaN(n) || n < 45 ? 45 : n) }} type="number" min="45" />
          <Input label="Lieu" optional value={data.lieu} onChange={e => setField('lieu', e.target.value)} placeholder="ex. Palais des sports" />
          {role !== 'ENSEIGNANT' ? (
              <div className="mb-3">
                <label className="block text-sm text-gray-600 mb-1">Enseignant responsable <span className="text-red-500">*</span></label>
                <select value={data.enseignantId} onChange={e => setField('enseignantId', Number(e.target.value))} className={selectCls}>
                  <option value="">-- Choisir un enseignant --</option>
                  {enseignants.map(e => <option key={e.id} value={e.id}>{e.prenom} {e.nom} - Niv. {e.niveauExpertise}</option>)}
                </select>
              </div>
          ) : (
              <div className="mb-3">
                <label className="block text-sm text-gray-600 mb-1">Enseignant responsable</label>
                <div className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm bg-gray-50 text-gray-600">
                  {currentUser?.prenom} {currentUser?.nom} — ID #{currentUser?.id}
                </div>
              </div>
          )}
          <div className="mb-3">
            <label className="block text-sm text-gray-600 mb-1">Niveau cible <span className="text-red-500">*</span></label>
            <select value={data.niveauCible} onChange={e => setField('niveauCible', Number(e.target.value))} className={selectCls}>
              {niveauxCreation.map(n => <option key={n} value={n}>Niveau {n}</option>)}
            </select>
            {role === 'ENSEIGNANT' && <p className="text-xs text-gray-400 mt-1">Vous pouvez gérer une compétition de votre niveau ou d’un niveau inférieur.</p>}
          </div>
        </div>
        <div className="flex gap-2 mt-2">
          <Btn onClick={onSubmit}>{submitLabel}</Btn>
          <Btn variant="outline" onClick={onCancel}>Annuler</Btn>
        </div>
      </Card>
  )

  const formulaireResultat = (data, setField, onSubmit, submitLabel, onCancel) => (
      <div className="mb-4 p-3 bg-gray-50 rounded-lg">
        <p className="text-xs text-gray-400 mb-3"><span className="text-red-500">*</span> Champ obligatoire</p>
        <div className="mb-3">
          <label className="block text-sm text-gray-600 mb-1">Membre <span className="text-red-500">*</span></label>
          <select value={data.eleveId} onChange={e => setField('eleveId', e.target.value)} className={selectCls}>
            <option value="">-- Choisir un membre --</option>
            {membresResultat.map(m => <option key={m.id} value={m.id}>{m.prenom} {m.nom} - Niv. {m.niveauExpertise}</option>)}
          </select>
        </div>
        <Input label="Note (0 à 10)" required value={data.note} onChange={e => setField('note', e.target.value)} type="number" min="0" max="10" step="0.1" />
        <div className="flex gap-2">
          <Btn size="sm" onClick={onSubmit}>{submitLabel}</Btn>
          <Btn size="sm" variant="outline" onClick={onCancel}>Annuler</Btn>
        </div>
      </div>
  )

  return (
      <div>
        <div className="flex items-center justify-between mb-5">
          <h1 className="text-xl font-bold text-gray-800 flex items-center gap-2">
            <Trophy size={20} className="text-blue-600" /> Compétitions
          </h1>
          {['ENSEIGNANT', 'PRESIDENT'].includes(role) && (
              <Btn onClick={() => { setEditCompetitionId(null); setEditForm(null); setShowForm(!showForm) }}>+ Nouvelle compétition</Btn>
          )}
        </div>

        <Alert {...alert} onClose={() => setAlert(null)} />

        {showForm && formulaireCompetition(form, f, creer, 'Créer une compétition', 'Créer', () => setShowForm(false))}
        {editCompetitionId && editForm && formulaireCompetition(editForm, ef, modifierCompetition, `Modifier la compétition`, 'Enregistrer', () => { setEditCompetitionId(null); setEditForm(null) })}

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
                    <div className="text-xs text-gray-500 mt-1 flex items-center gap-3 flex-wrap">
                      <span className="flex items-center gap-1"><Calendar size={12} className="text-blue-500" />{c.date}</span>
                      <span>{heure(c.heureDebut)}</span>
                      <span>Niv. {c.niveauCible}</span>
                      <span className="flex items-center gap-1"><MapPin size={12} className="text-blue-500" />{c.lieu || '-'}</span>
                    </div>
                    <div className="flex gap-3 mt-1">
                      {peutGererCompetition(c) && (
                          <button
                              onClick={e => { e.stopPropagation(); ouvrirEditionCompetition(c) }}
                              className="text-xs text-blue-600 hover:text-blue-800 cursor-pointer"
                          >Modifier</button>
                      )}
                      {role === 'PRESIDENT' && (
                          <button
                              onClick={e => { e.stopPropagation(); supprimer(c.id) }}
                              className="text-xs text-red-500 hover:text-red-700 cursor-pointer"
                          >Supprimer</button>
                      )}
                    </div>
                  </div>
              ))}
              {competitions.length === 0 && <p className="text-gray-400 text-sm">Aucune compétition.</p>}
            </div>}
          </Card>

          <Card
              title={selected ? `Résultats — ${selected.titre}` : 'Sélectionnez une compétition'}
              action={selected && peutGererCompetition(selected) && (
                  <Btn size="sm" onClick={() => { setEditResultatId(null); setEditResForm(null); setShowResForm(!showResForm) }}>+ Résultat</Btn>
              )}
          >
            {showResForm && formulaireResultat(resForm, rf, ajouterResultat, 'Enregistrer', () => setShowResForm(false))}
            {editResultatId && editResForm && formulaireResultat(editResForm, erf, modifierResultat, 'Modifier', () => { setEditResultatId(null); setEditResForm(null) })}

            {loadingResultats ? <Spinner /> : resultats.length > 0 ? (
                <table className="w-full text-sm">
                  <thead>
                  <tr className="text-left text-gray-500 border-b">
                    <th className="pb-2">Membre</th>
                    <th className="pb-2">Note</th>
                    {selected && peutGererCompetition(selected) && <th className="pb-2 text-right">Action</th>}
                  </tr>
                  </thead>
                  <tbody>
                  {resultats.map((r, i) => (
                      <tr key={r.id || i} className="border-b border-gray-50">
                        <td className="py-2">{nomMembre(r.eleveId)}</td>
                        <td className="py-2 font-bold text-blue-600">{r.note} / 10</td>
                        {selected && peutGererCompetition(selected) && (
                            <td className="py-2 text-right">
                              <button onClick={() => ouvrirEditionResultat(r)} className="text-xs text-blue-600 hover:text-blue-800 cursor-pointer">Modifier</button>
                            </td>
                        )}
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
