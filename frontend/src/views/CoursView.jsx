import { useState, useEffect } from 'react'
import { BookOpen, Calendar, Clock, Timer, MapPin } from 'lucide-react'
import { api } from '../api'
import { Card, Btn, Input, Alert } from '../components/Card'

const dateMin7 = () => {
  const d = new Date()
  d.setDate(d.getDate() + 8)
  return d.toISOString().split('T')[0]
}

export default function CoursView({ role }) {
  const [cours,        setCours]        = useState([])
  const [alert,        setAlert]        = useState(null)
  const [showForm,     setShowForm]     = useState(false)
  const [filtreNiveau, setFiltreNiveau] = useState('')
  const [form, setForm] = useState({
    titre: '', date: '', heureDebut: '', duree: 60,
    lieu: '', niveauCible: 1, enseignantId: '',
  })

  useEffect(() => { charger() }, [])

  const charger = async (niveau = filtreNiveau) => {
    try {
      const res = niveau
        ? await api.cours.listerParNiveau(niveau)
        : await api.cours.lister()
      setCours(res.data)
    } catch (e) {
      setAlert({ type: 'error', message: 'Impossible de charger les cours. Veuillez réessayer.' })
    }
  }

  const creer = async () => {
    if (!['SECRETAIRE', 'PRESIDENT'].includes(role)) {
      return setAlert({ type: 'error', message: 'Seuls la secrétaire et le président peuvent créer un cours.' })
    }
    if (!form.titre.trim()) return setAlert({ type: 'error', message: 'Le titre du cours est obligatoire.' })
    if (!form.date) return setAlert({ type: 'error', message: 'La date du cours est obligatoire (au moins 7 jours à l\'avance).' })
    if (!form.enseignantId) return setAlert({ type: 'error', message: 'Veuillez indiquer le numéro de l\'enseignant responsable.' })
    try {
      await api.cours.creer(form, role)
      setAlert({ type: 'success', message: 'Le cours a bien été créé !' })
      setShowForm(false)
      charger()
    } catch (e) {
      const msg = e.response?.data?.error
      setAlert({ type: 'error', message: msg || 'Impossible de créer le cours. Vérifiez que l\'enseignant est inscrit et apte pour le niveau choisi.' })
    }
  }

  const supprimer = async (id) => {
    if (role !== 'PRESIDENT') return setAlert({ type: 'error', message: 'Seul le président peut supprimer un cours.' })
    if (!confirm('Supprimer ce cours définitivement ?')) return
    try {
      await api.cours.supprimer(id, role)
      setAlert({ type: 'success', message: 'Cours supprimé.' })
      charger()
    } catch (e) {
      setAlert({ type: 'error', message: 'Impossible de supprimer ce cours. Veuillez réessayer.' })
    }
  }

  const appliquerFiltre = (n) => {
    setFiltreNiveau(n)
    charger(n)
  }

  const f = (field, val) => setForm(p => ({ ...p, [field]: val }))

  return (
    <div>
      <div className="flex items-center justify-between mb-5">
        <h1 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <BookOpen size={20} className="text-blue-600" /> Cours
        </h1>
        {['SECRETAIRE', 'PRESIDENT'].includes(role) && (
          <Btn onClick={() => setShowForm(!showForm)}>+ Nouveau cours</Btn>
        )}
      </div>

      <Alert {...alert} onClose={() => setAlert(null)} />

      <div className="flex items-center gap-2 mb-4 flex-wrap">
        <span className="text-sm text-gray-500">Filtrer :</span>
        {['', 1, 2, 3, 4, 5].map(n => (
          <button key={String(n)}
            onClick={() => appliquerFiltre(n)}
            className={`text-sm px-3 py-1 rounded-full border transition-colors cursor-pointer ${
              filtreNiveau === n
                ? 'bg-blue-600 text-white border-blue-600'
                : 'border-gray-300 hover:bg-gray-50 text-gray-600'
            }`}
          >
            {n === '' ? 'Tous' : `Niv. ${n}`}
          </button>
        ))}
      </div>

      {showForm && (
        <Card title="Créer un cours">
          <div className="grid grid-cols-2 gap-x-4">
            <Input label="Titre" value={form.titre} onChange={e => f('titre', e.target.value)} placeholder="ex. Salsa débutant" />
            <Input label="Date (au moins 7 jours à l'avance)" value={form.date} onChange={e => f('date', e.target.value)} type="date" min={dateMin7()} />
            <Input label="Heure de début" value={form.heureDebut} onChange={e => f('heureDebut', e.target.value)} type="time" />
            <Input label="Durée en minutes (45 min minimum)" value={form.duree} onChange={e => f('duree', Number(e.target.value))} type="number" min="45" />
            <Input label="Lieu" value={form.lieu} onChange={e => f('lieu', e.target.value)} placeholder="ex. Salle A" />
            <Input label="Numéro de l'enseignant" value={form.enseignantId} onChange={e => f('enseignantId', Number(e.target.value))} type="number" min="1" placeholder="ex. 3" />
            <div className="mb-3">
              <label className="block text-sm text-gray-600 mb-1">Niveau cible</label>
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

      <Card title={`${cours.length} cours`} action={<Btn variant="outline" onClick={() => charger()}>Actualiser</Btn>}>
        <div className="space-y-3">
          {cours.map(c => (
            <div key={c.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
              <div>
                <div className="font-medium text-gray-800">{c.titre}</div>
                <div className="text-sm text-gray-500 mt-0.5 flex items-center gap-3">
                  <span className="flex items-center gap-1"><Calendar size={13} className="text-blue-500" />{c.date}</span>
                  <span className="flex items-center gap-1"><Clock size={13} className="text-blue-500" />{c.heureDebut}</span>
                  <span className="flex items-center gap-1"><Timer size={13} className="text-blue-500" />{c.duree} min</span>
                  <span className="flex items-center gap-1"><MapPin size={13} className="text-blue-500" />{c.lieu}</span>
                </div>
                <div className="text-xs text-gray-400 mt-0.5">
                  Niveau {c.niveauCible} · Enseignant #{c.enseignantId}
                </div>
              </div>
              {role === 'PRESIDENT' && (
                <Btn variant="danger" onClick={() => supprimer(c.id)}>Supprimer</Btn>
              )}
            </div>
          ))}
          {cours.length === 0 && <p className="text-gray-400 text-sm text-center py-4">Aucun cours trouvé.</p>}
        </div>
      </Card>
    </div>
  )
}