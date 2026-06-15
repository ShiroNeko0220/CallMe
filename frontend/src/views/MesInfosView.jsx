import { useState } from 'react'
import { UserCircle, Edit3, Save, X } from 'lucide-react'
import { api } from '../api'
import { Card, Btn, Input, Alert, BadgeTag } from '../components/Card'

const ROLE_COLOR = { MEMBRE: 'blue', ENSEIGNANT: 'green', SECRETAIRE: 'amber', PRESIDENT: 'purple' }

export default function MesInfosView({ role, currentUser, onUserUpdated }) {
    const [editing, setEditing] = useState(false)
    const [alert, setAlert] = useState(null)
    const [form, setForm] = useState({
        nom: currentUser?.nom || '',
        prenom: currentUser?.prenom || '',
        email: currentUser?.email || '',
        ville: currentUser?.ville || '',
        pays: currentUser?.pays || 'France',
    })

    const f = (field, value) => setForm(prev => ({ ...prev, [field]: value }))

    const resetForm = () => {
        setForm({
            nom: currentUser?.nom || '',
            prenom: currentUser?.prenom || '',
            email: currentUser?.email || '',
            ville: currentUser?.ville || '',
            pays: currentUser?.pays || 'France',
        })
        setEditing(false)
    }

    const sauvegarder = async () => {
        try {
            const payload = {
                nom: form.nom,
                prenom: form.prenom,
                email: form.email,
                ville: form.ville,
                pays: form.pays,
            }
            const res = await api.utilisateurs.modifier(currentUser.id, payload, role, currentUser.id)
            onUserUpdated?.(res.data)
            setAlert({ type: 'success', message: 'Vos informations ont été mises à jour.' })
            setEditing(false)
        } catch (e) {
            const msg = e.response?.data?.error
            setAlert({ type: 'error', message: msg || 'Impossible de modifier vos informations.' })
        }
    }

    return (
        <div>
            <div className="flex items-center justify-between mb-5">
                <h1 className="text-xl font-bold text-gray-800 flex items-center gap-2">
                    <UserCircle size={20} className="text-blue-600" /> Mon profil
                </h1>
                {!editing ? (
                    <Btn onClick={() => setEditing(true)}><span className="inline-flex items-center gap-1"><Edit3 size={14} /> Éditer</span></Btn>
                ) : (
                    <div className="flex gap-2">
                        <Btn onClick={sauvegarder}><span className="inline-flex items-center gap-1"><Save size={14} /> Enregistrer</span></Btn>
                        <Btn variant="outline" onClick={resetForm}><span className="inline-flex items-center gap-1"><X size={14} /> Annuler</span></Btn>
                    </div>
                )}
            </div>

            <Alert {...alert} onClose={() => setAlert(null)} />

            <Card title="Informations personnelles">
                {!editing ? (
                    <div className="grid grid-cols-2 gap-x-8 gap-y-4 text-sm">
                        <Info label="Nom" value={currentUser?.nom} />
                        <Info label="Prénom" value={currentUser?.prenom} />
                        <Info label="Email" value={currentUser?.email} />
                        <Info label="Ville" value={currentUser?.ville || '-'} />
                        <Info label="Pays" value={currentUser?.pays || '-'} />
                        <div>
                            <div className="text-gray-400 text-xs uppercase tracking-wide mb-1">Rôle</div>
                            <BadgeTag label={currentUser?.role} color={ROLE_COLOR[currentUser?.role] || 'gray'} />
                        </div>
                        <Info label="Niveau d'expertise" value={`Niveau ${currentUser?.niveauExpertise || '-'}`} />
                    </div>
                ) : (
                    <div>
                        <p className="text-xs text-gray-500 mb-3">
                            Vous pouvez modifier vos informations personnelles. Le rôle et le niveau d'expertise restent gérés par le secrétariat.
                        </p>
                        <div className="grid grid-cols-2 gap-x-4">
                            <Input label="Nom" value={form.nom} onChange={e => f('nom', e.target.value)} />
                            <Input label="Prénom" value={form.prenom} onChange={e => f('prenom', e.target.value)} />
                            <Input label="Email" type="email" value={form.email} onChange={e => f('email', e.target.value)} />
                            <Input label="Ville" value={form.ville} onChange={e => f('ville', e.target.value)} />
                            <Input label="Pays" value={form.pays} onChange={e => f('pays', e.target.value)} />
                        </div>
                    </div>
                )}
            </Card>
        </div>
    )
}

function Info({ label, value }) {
    return (
        <div>
            <div className="text-gray-400 text-xs uppercase tracking-wide mb-1">{label}</div>
            <div className="text-gray-800 font-medium">{value || '-'}</div>
        </div>
    )
}
