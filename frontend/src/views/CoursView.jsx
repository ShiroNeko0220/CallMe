import { useState, useEffect } from 'react'
import { BookOpen, Calendar, Clock, Timer, MapPin, RefreshCw } from 'lucide-react'
import { api } from '../api'
import { Card, Btn, Input, Alert, Spinner, ConfirmModal } from '../components/Card'

const dateMin8 = () => {
  const d = new Date()
  d.setDate(d.getDate() + 8)
  return d.toISOString().split('T')[0]
}

const selectCls = "w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300 bg-white"
const heure = (v) => v ? String(v).slice(0, 5) : ''

export default function CoursView({ role, currentUser }) {
  const [cours, setCours] = useState([])
  const [enseignants, setEnseignants] = useState([])
  const [loading, setLoading] = useState(true)
  const [alert, setAlert] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [filtreNiveau, setFiltreNiveau] = useState('')
  const [confirm, setConfirm] = useState(null)
  const [editId, setEditId] = useState(null)
  const [editForm, setEditForm] = useState(null)
  const [form, setForm] = useState({
    titre: '', date: '', heureDebut: '', duree: 60,
    lieu: '', niveauCible: 1, enseignantId: '',
  })

  const admin = role === 'SECRETAIRE' || role === 'PRESIDENT'
  const enseignant = role === 'ENSEIGNANT'
  const peutCreer = admin || enseignant

  useEffect(() => {
    charger()
    if (admin || enseignant) {
      api.utilisateurs.lister(role)
          .then(r => setEnseignants(r.data.filter(u => u.role === 'ENSEIGNANT')))
          .catch(() => {})
    }
  }, [role, currentUser?.id, currentUser?.niveauExpertise])

  useEffect(() => {
    if (enseignant && currentUser) {
      setForm(p => ({ ...p, enseignantId: currentUser.id, niveauCible: Math.min(p.niveauCible || 1, currentUser.niveauExpertise || 1) }))
    }
  }, [enseignant, currentUser?.id, currentUser?.niveauExpertise])

  const charger = async (niveau = filtreNiveau) => {
    setLoading(true)
    try {
      let res
      if (role === 'MEMBRE') {
        res = await api.cours.listerParNiveau(currentUser.niveauExpertise)
      } else if (role === 'ENSEIGNANT') {
        res = await api.cours.listerParEnseignant(currentUser.id)
      } else if (niveau) {
        res = await api.cours.listerParNiveau(niveau)
      } else {
        res = await api.cours.lister()
      }
      setCours(res.data)
    } catch (e) {
      setAlert({ type: 'error', message: 'Impossible de charger les cours.' })
    } finally {
      setLoading(false)
    }
  }

  const validerPayload = (payload) => {
    if (!payload.titre?.trim()) return 'Le titre du cours est obligatoire.'
    if (!payload.date) return 'La date du cours est obligatoire.'
    if (!payload.heureDebut) return 'L’heure de début est obligatoire.'
    if (!payload.duree || payload.duree < 45) return 'La durée doit être au moins de 45 minutes.'
    if (!payload.enseignantId) return 'Veuillez choisir un enseignant.'
    if (enseignant && payload.niveauCible > currentUser.niveauExpertise) {
      return `Vous êtes apte jusqu’au niveau ${currentUser.niveauExpertise}. Vous ne pouvez pas gérer un cours de niveau ${payload.niveauCible}.`
    }
    return null
  }

  const creer = async () => {
    const payload = {
      ...form,
      niveauCible: Number(form.niveauCible),
      duree: Number(form.duree),
      enseignantId: enseignant ? currentUser.id : Number(form.enseignantId),
    }

    const erreur = validerPayload(payload)
    if (erreur) return setAlert({ type: 'error', message: erreur })

    try {
      await api.cours.creer(payload, role, currentUser?.id)
      setAlert({ type: 'success', message: 'Le cours a bien été créé.' })
      setShowForm(false)
      setForm({ titre: '', date: '', heureDebut: '', duree: 60, lieu: '', niveauCible: 1, enseignantId: enseignant ? currentUser.id : '' })
      charger()
    } catch (e) {
      const msg = e.response?.data?.error
      setAlert({ type: 'error', message: msg || 'Impossible de créer le cours. Vérifiez que l’enseignant est apte pour le niveau choisi.' })
    }
  }

  const ouvrirEdition = (c) => {
    setShowForm(false)
    setEditId(c.id)
    setEditForm({
      titre: c.titre || '',
      date: c.date || '',
      heureDebut: heure(c.heureDebut),
      duree: c.duree || 60,
      lieu: c.lieu || '',
      niveauCible: c.niveauCible || 1,
      enseignantId: c.enseignantId || '',
    })
  }

  const modifier = async () => {
    const payload = {
      ...editForm,
      niveauCible: Number(editForm.niveauCible),
      duree: Number(editForm.duree),
      enseignantId: enseignant ? currentUser.id : Number(editForm.enseignantId),
    }

    const erreur = validerPayload(payload)
    if (erreur) return setAlert({ type: 'error', message: erreur })

    try {
      await api.cours.modifier(editId, payload, role, currentUser?.id)
      setAlert({ type: 'success', message: 'Cours modifié.' })
      setEditId(null)
      setEditForm(null)
      charger()
    } catch (e) {
      const msg = e.response?.data?.error
      setAlert({ type: 'error', message: msg || 'Impossible de modifier ce cours.' })
    }
  }

  const supprimer = (id) => {
    setConfirm({ message: 'Supprimer ce cours définitivement ?', onConfirm: async () => {
        setConfirm(null)
        try {
          await api.cours.supprimer(id, role)
          setAlert({ type: 'success', message: 'Cours supprimé.' })
          charger()
        } catch (e) {
          setAlert({ type: 'error', message: e.response?.data?.error || 'Impossible de supprimer ce cours.' })
        }
      }})
  }

  const appliquerFiltre = (n) => {
    setFiltreNiveau(n)
    charger(n)
  }

  const f = (field, val) => setForm(p => ({ ...p, [field]: val }))
  const ef = (field, val) => setEditForm(p => ({ ...p, [field]: val }))
  const niveauxPossibles = enseignant
      ? Array.from({ length: currentUser?.niveauExpertise || 1 }, (_, i) => i + 1)
      : [1, 2, 3, 4, 5]

  const titreCarte = role === 'MEMBRE'
      ? `Mes cours de niveau ${currentUser?.niveauExpertise}`
      : role === 'ENSEIGNANT'
          ? 'Mes cours'
          : `${cours.length} cours`

  const peutModifierCours = (c) => admin || (enseignant && Number(c.enseignantId) === Number(currentUser?.id))

  const formulaireCours = (data, setField, onSubmit, titre, submitLabel, onCancel) => (
      <Card title={titre}>
        {enseignant && <p className="text-xs text-blue-700 bg-blue-50 border border-blue-100 rounded-lg px-3 py-2 mb-3">Vous pouvez gérer uniquement vos propres cours et uniquement jusqu’au niveau {currentUser?.niveauExpertise}.</p>}
        <p className="text-xs text-gray-400 mb-3"><span className="text-red-500">*</span> Champ obligatoire</p>
        <div className="grid grid-cols-2 gap-x-4">
          <Input label="Titre" required value={data.titre} onChange={e => setField('titre', e.target.value)} placeholder="ex. Salsa débutant" />
          <Input label="Date (au moins 7 jours à l'avance)" required value={data.date} onChange={e => setField('date', e.target.value)} type="date" min={dateMin8()} />
          <Input label="Heure de début" required value={data.heureDebut} onChange={e => setField('heureDebut', e.target.value)} type="time" />
          <Input label="Durée en minutes (45 min minimum)" required value={data.duree} onChange={e => { const n = parseInt(e.target.value); setField('duree', isNaN(n) || n < 45 ? 45 : n) }} type="number" min="45" />
          <Input label="Lieu" optional value={data.lieu} onChange={e => setField('lieu', e.target.value)} placeholder="ex. Salle A" />
          {admin ? (
              <div className="mb-3">
                <label className="block text-sm text-gray-600 mb-1">Enseignant <span className="text-red-500">*</span></label>
                <select value={data.enseignantId} onChange={e => setField('enseignantId', Number(e.target.value))} className={selectCls}>
                  <option value="">-- Choisir un enseignant --</option>
                  {enseignants.map(e => <option key={e.id} value={e.id}>{e.prenom} {e.nom} - Niv. {e.niveauExpertise}</option>)}
                </select>
              </div>
          ) : (
              <div className="mb-3">
                <label className="block text-sm text-gray-600 mb-1">Enseignant</label>
                <input disabled value={`${currentUser?.prenom || ''} ${currentUser?.nom || ''}`} className="w-full border border-gray-200 bg-gray-50 rounded-lg px-3 py-2 text-sm text-gray-500" />
              </div>
          )}
          <div className="mb-3">
            <label className="block text-sm text-gray-600 mb-1">Niveau cible <span className="text-red-500">*</span></label>
            <select value={data.niveauCible} onChange={e => setField('niveauCible', Number(e.target.value))} className={selectCls}>
              {niveauxPossibles.map(n => <option key={n} value={n}>Niveau {n}</option>)}
            </select>
          </div>
        </div>
        <div className="flex gap-2 mt-2">
          <Btn onClick={onSubmit}>{submitLabel}</Btn>
          <Btn variant="outline" onClick={onCancel}>Annuler</Btn>
        </div>
      </Card>
  )

  return (
      <div>
        <ConfirmModal message={confirm?.message} onConfirm={confirm?.onConfirm} onCancel={() => setConfirm(null)} />

        <div className="flex items-center justify-between mb-5">
          <h1 className="text-xl font-bold text-gray-800 flex items-center gap-2">
            <BookOpen size={20} className="text-blue-600" /> Cours
          </h1>
          {peutCreer && <Btn onClick={() => { setEditId(null); setEditForm(null); setShowForm(!showForm) }}>+ Nouveau cours</Btn>}
        </div>

        <Alert {...alert} onClose={() => setAlert(null)} />

        {admin && (
            <div className="flex items-center gap-2 mb-4 flex-wrap">
              <span className="text-sm text-gray-500">Filtrer :</span>
              {['', 1, 2, 3, 4, 5].map(n => (
                  <button key={String(n)} onClick={() => appliquerFiltre(n)} className={`text-sm px-3 py-1 rounded-full border transition-colors cursor-pointer ${filtreNiveau === n ? 'bg-blue-600 text-white border-blue-600' : 'border-gray-300 hover:bg-gray-50 text-gray-600'}`}>
                    {n === '' ? 'Tous' : `Niv. ${n}`}
                  </button>
              ))}
            </div>
        )}

        {showForm && peutCreer && formulaireCours(form, f, creer, 'Créer un cours', 'Créer', () => setShowForm(false))}
        {editId && editForm && formulaireCours(editForm, ef, modifier, `Modifier le cours #${editId}`, 'Enregistrer', () => { setEditId(null); setEditForm(null) })}

        <Card title={titreCarte} action={<button onClick={() => charger()} className="p-1.5 rounded bg-blue-50 hover:bg-blue-100 text-blue-500 hover:text-blue-700 cursor-pointer" title="Actualiser"><RefreshCw size={15} /></button>}>
          {loading ? <Spinner /> : <div className="space-y-3">
            {cours.map(c => (
                <div key={c.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div>
                    <div className="font-medium text-gray-800">{c.titre}</div>
                    <div className="text-sm text-gray-500 mt-0.5 flex items-center gap-3 flex-wrap">
                      <span className="flex items-center gap-1"><Calendar size={13} className="text-blue-500" />{c.date}</span>
                      <span className="flex items-center gap-1"><Clock size={13} className="text-blue-500" />{heure(c.heureDebut)}</span>
                      <span className="flex items-center gap-1"><Timer size={13} className="text-blue-500" />{c.duree} min</span>
                      <span className="flex items-center gap-1"><MapPin size={13} className="text-blue-500" />{c.lieu}</span>
                    </div>
                    <div className="text-xs text-gray-400 mt-0.5">Niveau {c.niveauCible} · Enseignant #{c.enseignantId}</div>
                  </div>
                  <div className="flex gap-2">
                    {peutModifierCours(c) && <Btn variant="outline" onClick={() => ouvrirEdition(c)}>Modifier</Btn>}
                    {role === 'PRESIDENT' && <Btn variant="danger" onClick={() => supprimer(c.id)}>Supprimer</Btn>}
                  </div>
                </div>
            ))}
            {cours.length === 0 && <p className="text-gray-400 text-sm text-center py-4">Aucun cours trouvé.</p>}
          </div>}
        </Card>
      </div>
  )
}
