import { useState, useEffect } from 'react'
import { Users, Star, Eye, EyeOff, RefreshCw, X } from 'lucide-react'
import { api } from '../api'
import { Card, BadgeTag, Btn, Input, Alert, Spinner, ConfirmModal } from '../components/Card'

const ROLE_COLOR = { MEMBRE: 'blue', ENSEIGNANT: 'green', SECRETAIRE: 'amber', PRESIDENT: 'purple' }
const selectCls = "w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300 bg-white"

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

export default function MembresView({ role, currentUser, onUserUpdated }) {
  const [membres, setMembres] = useState([])
  const [loading, setLoading] = useState(true)
  const [alert, setAlert] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [confirm, setConfirm] = useState(null)
  const [showPwd, setShowPwd] = useState(false)
  const [editingMember, setEditingMember] = useState(null)
  const [editForm, setEditForm] = useState({})
  const [form, setForm] = useState({
    nom: '', prenom: '', email: '',
    idConnexion: { login: '', mdp: '' },
    adresse: { ville: '', pays: 'France' },
  })

  const admin = role === 'SECRETAIRE' || role === 'PRESIDENT'

  useEffect(() => { charger() }, [role])

  const charger = async () => {
    setLoading(true)
    try {
      const res = await api.utilisateurs.lister(role)
      setMembres(res.data)
    } catch (e) {
      setAlert({ type: 'error', message: 'Impossible de charger la liste des membres.' })
    } finally {
      setLoading(false)
    }
  }

  const creer = async () => {
    try {
      await api.utilisateurs.creer(form, role, currentUser?.id)
      setAlert({ type: 'success', message: 'Membre créé avec succès.' })
      setShowForm(false)
      setForm({ nom: '', prenom: '', email: '', idConnexion: { login: '', mdp: '' }, adresse: { ville: '', pays: 'France' } })
      charger()
    } catch (e) {
      const msg = e.response?.data?.error
      setAlert({ type: 'error', message: msg || 'Impossible de créer le membre. Vérifiez le login et l’email.' })
    }
  }

  const ouvrirEditionAdmin = (membre) => {
    setEditingMember(membre)
    setEditForm({
      nom: membre.nom || '',
      prenom: membre.prenom || '',
      email: membre.email || '',
      ville: membre.ville || '',
      pays: membre.pays || 'France',
      niveauExpertise: membre.niveauExpertise || 1,
      role: membre.role || 'MEMBRE',
    })
  }

  const fermerEdition = () => {
    setEditingMember(null)
    setEditForm({})
  }

  const modifierAdmin = async () => {
    if (!editingMember) return
    try {
      const payload = {
        nom: editForm.nom,
        prenom: editForm.prenom,
        email: editForm.email,
        ville: editForm.ville,
        pays: editForm.pays,
        niveauExpertise: Number(editForm.niveauExpertise),
        role: editForm.role,
      }
      const res = await api.utilisateurs.modifierAdmin(editingMember.id, payload, role, currentUser?.id)
      if (Number(editingMember.id) === Number(currentUser?.id)) onUserUpdated?.(res.data)
      setAlert({ type: 'success', message: 'Membre modifié avec succès.' })
      fermerEdition()
      charger()
    } catch (e) {
      const msg = e.response?.data?.error
      setAlert({ type: 'error', message: msg || 'Impossible de modifier ce membre.' })
    }
  }

  const supprimer = (id) => {
    setConfirm({ message: 'Supprimer ce membre définitivement ?', onConfirm: async () => {
        setConfirm(null)
        try {
          await api.utilisateurs.supprimer(id, role)
          setAlert({ type: 'success', message: 'Membre supprimé.' })
          charger()
        } catch (e) {
          setAlert({ type: 'error', message: 'Impossible de supprimer ce membre.' })
        }
      }})
  }

  const f = (field, val) => setForm(p => ({ ...p, [field]: val }))
  const fCnx = (field, val) => setForm(p => ({ ...p, idConnexion: { ...p.idConnexion, [field]: val } }))
  const fAdr = (field, val) => setForm(p => ({ ...p, adresse: { ...p.adresse, [field]: val } }))
  const fEdit = (field, val) => setEditForm(p => ({ ...p, [field]: val }))

  return (
      <div>
        <ConfirmModal message={confirm?.message} onConfirm={confirm?.onConfirm} onCancel={() => setConfirm(null)} />

        <div className="flex items-center justify-between mb-5">
          <h1 className="text-xl font-bold text-gray-800 flex items-center gap-2">
            <Users size={20} className="text-blue-600" /> Membres du club
          </h1>
          {admin && <Btn onClick={() => setShowForm(!showForm)}>+ Nouveau membre</Btn>}
        </div>

        <Alert {...alert} onClose={() => setAlert(null)} />

        {showForm && admin && (
            <Card title="Créer un nouveau membre">
              <p className="text-xs text-amber-600 mb-1">Le rôle et le niveau seront MEMBRE / niveau 1 par défaut. Ils peuvent être changés ensuite avec Éditer.</p>
              <p className="text-xs text-gray-400 mb-3"><span className="text-red-500">*</span> Champ obligatoire</p>
              <div className="grid grid-cols-2 gap-x-4">
                <Input label="Nom" required value={form.nom} onChange={e => f('nom', e.target.value)} placeholder="Dupont" />
                <Input label="Prénom" required value={form.prenom} onChange={e => f('prenom', e.target.value)} placeholder="Alice" />
                <Input label="Email" required value={form.email} onChange={e => f('email', e.target.value)} placeholder="alice@club.fr" type="email" />
                <Input label="Login" required value={form.idConnexion.login} onChange={e => fCnx('login', e.target.value)} placeholder="alice" />
                <div className="mb-3">
                  <label className="block text-sm text-gray-600 mb-1">Mot de passe <span className="text-red-500">*</span></label>
                  <div className="relative">
                    <input type={showPwd ? 'text' : 'password'} value={form.idConnexion.mdp} onChange={e => fCnx('mdp', e.target.value)} placeholder="••••••" className="w-full border border-gray-300 rounded-lg px-3 py-2 pr-9 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300" />
                    <button type="button" onClick={() => setShowPwd(v => !v)} className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 cursor-pointer">
                      {showPwd ? <Eye size={16} /> : <EyeOff size={16} />}
                    </button>
                  </div>
                </div>
                <Input label="Ville" optional value={form.adresse.ville} onChange={e => fAdr('ville', e.target.value)} placeholder="Toulouse" />
                <Input label="Pays" optional value={form.adresse.pays} onChange={e => fAdr('pays', e.target.value)} placeholder="France" />
              </div>
              <div className="flex gap-2 mt-2">
                <Btn onClick={creer}>Créer</Btn>
                <Btn variant="outline" onClick={() => setShowForm(false)}>Annuler</Btn>
              </div>
            </Card>
        )}

        <Card title={`${membres.length} membre(s)`} action={<button onClick={charger} className="p-1.5 rounded bg-blue-50 hover:bg-blue-100 text-blue-500 hover:text-blue-700 cursor-pointer" title="Actualiser"><RefreshCw size={15} /></button>}>
          {loading ? <Spinner /> : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                  <tr className="border-b border-gray-100 text-gray-500 text-left">
                    <th className="pb-2 pr-4">ID</th>
                    <th className="pb-2 pr-4">Nom</th>
                    <th className="pb-2 pr-4">Email</th>
                    <th className="pb-2 pr-4">Niveau</th>
                    <th className="pb-2 pr-4">Rôle</th>
                    <th className="pb-2 pr-4">Ville</th>
                    {admin && <th className="pb-2">Action</th>}
                  </tr>
                  </thead>
                  <tbody>
                  {membres.map(m => (
                      <tr key={m.id} className="border-b border-gray-50 hover:bg-gray-50">
                        <td className="py-2 pr-4 text-gray-400">{m.id}</td>
                        <td className="py-2 pr-4 font-medium">{m.prenom} {m.nom}</td>
                        <td className="py-2 pr-4 text-gray-500">{m.email}</td>
                        <td className="py-2 pr-4"><NiveauStars niveau={m.niveauExpertise} /></td>
                        <td className="py-2 pr-4"><BadgeTag label={m.role} color={ROLE_COLOR[m.role] || 'gray'} /></td>
                        <td className="py-2 pr-4 text-gray-500">{m.ville || '-'}</td>
                        {admin && (
                            <td className="py-2">
                              <div className="flex gap-2">
                                <Btn variant="outline" onClick={() => ouvrirEditionAdmin(m)}><span className="inline-flex items-center gap-1">Éditer</span></Btn>
                                {role === 'PRESIDENT' && <Btn variant="danger" onClick={() => supprimer(m.id)}>Supprimer</Btn>}
                              </div>
                            </td>
                        )}
                      </tr>
                  ))}
                  {membres.length === 0 && <tr><td colSpan={admin ? 7 : 6} className="py-4 text-center text-gray-400">Aucun membre.</td></tr>}
                  </tbody>
                </table>
              </div>
          )}
        </Card>

        {editingMember && admin && (
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
              <div className="bg-white rounded-2xl shadow-xl border border-gray-200 p-6 w-full max-w-2xl">
                <div className="flex items-center justify-between mb-4">
                  <h2 className="text-lg font-semibold text-gray-800">Éditer {editingMember.prenom} {editingMember.nom}</h2>
                  <button onClick={fermerEdition} className="text-gray-400 hover:text-gray-600 cursor-pointer"><X size={20} /></button>
                </div>
                <div className="grid grid-cols-2 gap-x-4">
                  <Input label="Nom" value={editForm.nom} onChange={e => fEdit('nom', e.target.value)} />
                  <Input label="Prénom" value={editForm.prenom} onChange={e => fEdit('prenom', e.target.value)} />
                  <Input label="Email" type="email" value={editForm.email} onChange={e => fEdit('email', e.target.value)} />
                  <Input label="Ville" value={editForm.ville} onChange={e => fEdit('ville', e.target.value)} />
                  <Input label="Pays" value={editForm.pays} onChange={e => fEdit('pays', e.target.value)} />
                  <div className="mb-3">
                    <label className="block text-sm text-gray-600 mb-1">Niveau d'expertise</label>
                    <select value={editForm.niveauExpertise} onChange={e => fEdit('niveauExpertise', Number(e.target.value))} className={selectCls}>
                      {[1, 2, 3, 4, 5].map(n => <option key={n} value={n}>Niveau {n}</option>)}
                    </select>
                  </div>
                  <div className="mb-3">
                    <label className="block text-sm text-gray-600 mb-1">Rôle</label>
                    <select value={editForm.role} onChange={e => fEdit('role', e.target.value)} className={selectCls}>
                      <option value="MEMBRE">MEMBRE</option>
                      <option value="ENSEIGNANT">ENSEIGNANT</option>
                      <option value="SECRETAIRE">SECRETAIRE</option>
                      <option value="PRESIDENT">PRESIDENT</option>
                    </select>
                  </div>
                </div>
                <div className="flex justify-end gap-2 mt-3">
                  <Btn variant="outline" onClick={fermerEdition}>Annuler</Btn>
                  <Btn onClick={modifierAdmin}>Enregistrer</Btn>
                </div>
              </div>
            </div>
        )}
      </div>
  )
}
