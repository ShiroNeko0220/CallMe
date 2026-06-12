import { useState, useEffect } from 'react'
import { Users, Star } from 'lucide-react'
import { api } from '../api'
import { Card, BadgeTag, Btn, Input, Alert, Spinner } from '../components/Card'

const ROLE_COLOR = { MEMBRE: 'blue', ENSEIGNANT: 'green', SECRETAIRE: 'amber', PRESIDENT: 'purple' }

function NiveauStars({ niveau }) {
  return (
    <span className="flex gap-0.5">
      {Array.from({ length: 5 }).map((_, i) => (
        <Star key={i} size={12}
          className={i < niveau ? 'text-blue-500 fill-blue-500' : 'text-gray-200 fill-gray-200'} />
      ))}
    </span>
  )
}

export default function MembresView({ role }) {
  const [membres,  setMembres]  = useState([])
  const [loading,  setLoading]  = useState(false)
  const [alert,    setAlert]    = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({
    nom: '', prenom: '', email: '',
    idConnexion: { login: '', mdp: '' },
    adresse: { ville: '', pays: 'France' },
  })

  useEffect(() => { charger() }, [])

  const charger = async () => {
    setLoading(true)
    try {
      const res = await api.utilisateurs.lister()
      setMembres(res.data)
    } catch (e) {
      setAlert({ type: 'error', message: 'Impossible de charger la liste des membres. Veuillez réessayer.' })
    } finally {
      setLoading(false)
    }
  }

  const creer = async () => {
    try {
      await api.utilisateurs.creer(form)
      setAlert({ type: 'success', message: 'Membre créé avec succès !' })
      setShowForm(false)
      setForm({ nom: '', prenom: '', email: '', idConnexion: { login: '', mdp: '' }, adresse: { ville: '', pays: 'France' } })
      charger()
    } catch (e) {
      const msg = e.response?.data?.error
      setAlert({ type: 'error', message: msg || 'Impossible de créer le membre. Vérifiez que le login et l\'email ne sont pas déjà utilisés.' })
    }
  }

  const supprimer = async (id) => {
    if (!confirm('Supprimer ce membre définitivement ?')) return
    try {
      await api.utilisateurs.supprimer(id, role)
      setAlert({ type: 'success', message: 'Membre supprimé.' })
      charger()
    } catch (e) {
      setAlert({ type: 'error', message: 'Impossible de supprimer ce membre. Veuillez réessayer.' })
    }
  }

  const f = (field, val) => setForm(p => ({ ...p, [field]: val }))
  const fCnx = (field, val) => setForm(p => ({ ...p, idConnexion: { ...p.idConnexion, [field]: val } }))
  const fAdr = (field, val) => setForm(p => ({ ...p, adresse: { ...p.adresse, [field]: val } }))

  return (
    <div>
      <div className="flex items-center justify-between mb-5">
        <h1 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <Users size={20} className="text-blue-600" /> Membres du club
        </h1>
        {['SECRETAIRE', 'PRESIDENT'].includes(role) && (
          <Btn onClick={() => setShowForm(!showForm)}>+ Nouveau membre</Btn>
        )}
      </div>

      <Alert {...alert} onClose={() => setAlert(null)} />

      {showForm && ['SECRETAIRE', 'PRESIDENT'].includes(role) && (
        <Card title="Créer un nouveau membre">
          <p className="text-xs text-amber-600 mb-3">Note : le rôle et le niveau seront MEMBRE/1 par défaut (modifiable ensuite).</p>
          <div className="grid grid-cols-2 gap-x-4">
            <Input label="Nom"    value={form.nom}    onChange={e => f('nom', e.target.value)}    placeholder="Dupont" />
            <Input label="Prénom" value={form.prenom} onChange={e => f('prenom', e.target.value)} placeholder="Alice" />
            <Input label="Email"  value={form.email}  onChange={e => f('email', e.target.value)}  placeholder="alice@club.fr" type="email" />
            <Input label="Login"  value={form.idConnexion.login} onChange={e => fCnx('login', e.target.value)} placeholder="alice" />
            <Input label="Mot de passe" value={form.idConnexion.mdp} onChange={e => fCnx('mdp', e.target.value)} type="password" placeholder="••••••" />
            <Input label="Ville" value={form.adresse.ville} onChange={e => fAdr('ville', e.target.value)} placeholder="Toulouse" />
            <Input label="Pays"  value={form.adresse.pays}  onChange={e => fAdr('pays', e.target.value)}  placeholder="France" />
          </div>
          <div className="flex gap-2 mt-2">
            <Btn onClick={creer}>Créer</Btn>
            <Btn variant="outline" onClick={() => setShowForm(false)}>Annuler</Btn>
          </div>
        </Card>
      )}

      <Card title={`${membres.length} membre(s)`} action={<Btn variant="outline" onClick={charger}>Actualiser</Btn>}>
        {loading ? <Spinner /> : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-100 text-gray-500 text-left">
                  <th className="pb-2 pr-4">ID</th>
                  <th className="pb-2 pr-4">Nom</th>
                  <th className="pb-2 pr-4">Login</th>
                  <th className="pb-2 pr-4">Email</th>
                  <th className="pb-2 pr-4">Niveau</th>
                  <th className="pb-2 pr-4">Rôle</th>
                  <th className="pb-2 pr-4">Ville</th>
                  {role === 'PRESIDENT' && <th className="pb-2">Action</th>}
                </tr>
              </thead>
              <tbody>
                {membres.map(m => (
                  <tr key={m.id} className="border-b border-gray-50 hover:bg-gray-50">
                    <td className="py-2 pr-4 text-gray-400">{m.id}</td>
                    <td className="py-2 pr-4 font-medium">{m.prenom} {m.nom}</td>
                    <td className="py-2 pr-4 text-gray-500 font-mono text-xs">{m.login}</td>
                    <td className="py-2 pr-4 text-gray-500">{m.email}</td>
                    <td className="py-2 pr-4"><NiveauStars niveau={m.niveauExpertise} /></td>
                    <td className="py-2 pr-4">
                      <BadgeTag label={m.role} color={ROLE_COLOR[m.role] || 'gray'} />
                    </td>
                    <td className="py-2 pr-4 text-gray-500">{m.ville || '—'}</td>
                    {role === 'PRESIDENT' && (
                      <td className="py-2">
                        <Btn variant="danger" onClick={() => supprimer(m.id)}>Supprimer</Btn>
                      </td>
                    )}
                  </tr>
                ))}
                {membres.length === 0 && (
                  <tr><td colSpan={8} className="py-4 text-center text-gray-400">Aucun membre.</td></tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  )
}
