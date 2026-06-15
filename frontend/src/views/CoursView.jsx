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

export default function CoursView({ role, currentUser }) {
  const [cours, setCours] = useState([])
  const [enseignants, setEnseignants] = useState([])
  const [loading, setLoading] = useState(true)
  const [alert, setAlert] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [filtreNiveau, setFiltreNiveau] = useState('')
  const [confirm, setConfirm] = useState(null)
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

  const creer = async () => {
    const payload = {
      ...form,
      niveauCible: Number(form.niveauCible),
      duree: Number(form.duree),
      enseignantId: enseignant ? currentUser.id : Number(form.enseignantId),
    }

    if (!payload.titre.trim()) return setAlert({ type: 'error', message: 'Le titre du cours est obligatoire.' })
    if (!payload.date) return setAlert({ type: 'error', message: 'La date du cours est obligatoire.' })
    if (!payload.enseignantId) return setAlert({ type: 'error', message: 'Veuillez choisir un enseignant.' })
    if (enseignant && payload.niveauCible > currentUser.niveauExpertise) {
      return setAlert({ type: 'error', message: `Vous êtes apte jusqu’au niveau ${currentUser.niveauExpertise}. Vous ne pouvez pas créer un cours de niveau ${payload.niveauCible}.` })
    }

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

  const supprimer = (id) => {
    setConfirm({ message: 'Supprimer ce cours définitivement ?', onConfirm: async () => {
        setConfirm(null)
        try {
          await api.cours.supprimer(id, role)
          setAlert({ type: 'success', message: 'Cours supprimé.' })
          charger()
        } catch (e) {
          setAlert({ type: 'error', message: 'Impossible de supprimer ce cours.' })
        }
      }})
  }

  const appliquerFiltre = (n) => {
    setFiltreNiveau(n)
    charger(n)
  }

  const f = (field, val) => setForm(p => ({ ...p, [field]: val }))
  const niveauxPossibles = enseignant
      ? Array.from({ length: currentUser?.niveauExpertise || 1 }, (_, i) => i + 1)
      : [1, 2, 3, 4, 5]

  const titreCarte = role === 'MEMBRE'
      ? `Mes cours de niveau ${currentUser?.niveauExpertise}`
      : role === 'ENSEIGNANT'
          ? 'Mes cours'
          : `${cours.length} cours`

  return (
      <div>
        <ConfirmModal message={confirm?.message} onConfirm={confirm?.onConfirm} onCancel={() => setConfirm(null)} />

        <div className="flex items-center justify-between mb-5">
          <h1 className="text-xl font-bold text-gray-800 flex items-center gap-2">
            <BookOpen size={20} className="text-blue-600" /> Cours
          </h1>
          {peutCreer && <Btn onClick={() => setShowForm(!showForm)}>+ Nouveau cours</Btn>}
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

        {showForm && peutCreer && (
            <Card title="Créer un cours">
              {enseignant && <p className="text-xs text-blue-700 bg-blue-50 border border-blue-100 rounded-lg px-3 py-2 mb-3">Vous créez un cours en tant qu’enseignant connecté. Vous pouvez créer uniquement des cours de niveau ≤ {currentUser?.niveauExpertise}.</p>}
              <p className="text-xs text-gray-400 mb-3"><span className="text-red-500">*</span> Champ obligatoire</p>
              <div className="grid grid-cols-2 gap-x-4">
                <Input label="Titre" required value={form.titre} onChange={e => f('titre', e.target.value)} placeholder="ex. Salsa débutant" />
                <Input label="Date (au moins 7 jours à l'avance)" required value={form.date} onChange={e => f('date', e.target.value)} type="date" min={dateMin8()} />
                <Input label="Heure de début" required value={form.heureDebut} onChange={e => f('heureDebut', e.target.value)} type="time" />
                <Input label="Durée en minutes (45 min minimum)" required value={form.duree} onChange={e => { const n = parseInt(e.target.value); f('duree', isNaN(n) || n < 45 ? 45 : n) }} type="number" min="45" />
                <Input label="Lieu" optional value={form.lieu} onChange={e => f('lieu', e.target.value)} placeholder="ex. Salle A" />
                {admin ? (
                    <div className="mb-3">
                      <label className="block text-sm text-gray-600 mb-1">Enseignant <span className="text-red-500">*</span></label>
                      <select value={form.enseignantId} onChange={e => f('enseignantId', Number(e.target.value))} className={selectCls}>
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
                  <label className="block text-sm text-gray-600 mb-1">Niveau cible</label>
                  <select value={form.niveauCible} onChange={e => f('niveauCible', Number(e.target.value))} className={selectCls}>
                    {niveauxPossibles.map(n => <option key={n} value={n}>Niveau {n}</option>)}
                  </select>
                </div>
              </div>
              <div className="flex gap-2 mt-2">
                <Btn onClick={creer}>Créer</Btn>
                <Btn variant="outline" onClick={() => setShowForm(false)}>Annuler</Btn>
              </div>
            </Card>
        )}

        <Card title={titreCarte} action={<button onClick={() => charger()} className="p-1.5 rounded bg-blue-50 hover:bg-blue-100 text-blue-500 hover:text-blue-700 cursor-pointer" title="Actualiser"><RefreshCw size={15} /></button>}>
          {loading ? <Spinner /> : <div className="space-y-3">
            {cours.map(c => (
                <div key={c.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div>
                    <div className="font-medium text-gray-800">{c.titre}</div>
                    <div className="text-sm text-gray-500 mt-0.5 flex items-center gap-3 flex-wrap">
                      <span className="flex items-center gap-1"><Calendar size={13} className="text-blue-500" />{c.date}</span>
                      <span className="flex items-center gap-1"><Clock size={13} className="text-blue-500" />{c.heureDebut}</span>
                      <span className="flex items-center gap-1"><Timer size={13} className="text-blue-500" />{c.duree} min</span>
                      <span className="flex items-center gap-1"><MapPin size={13} className="text-blue-500" />{c.lieu}</span>
                    </div>
                    <div className="text-xs text-gray-400 mt-0.5">Niveau {c.niveauCible} · Enseignant #{c.enseignantId}</div>
                  </div>
                  {role === 'PRESIDENT' && <Btn variant="danger" onClick={() => supprimer(c.id)}>Supprimer</Btn>}
                </div>
            ))}
            {cours.length === 0 && <p className="text-gray-400 text-sm text-center py-4">Aucun cours trouvé.</p>}
          </div>}
        </Card>
      </div>
  )
}
