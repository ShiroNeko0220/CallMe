import { useState, useEffect } from 'react'
import { CreditCard, Trash2, RefreshCw } from 'lucide-react'
import { api } from '../api'
import { Card, BadgeTag, Btn, Input, Alert, Spinner, ConfirmModal } from '../components/Card'

const STATUT_COLOR = { ASSOCIE: 'green', DISPONIBLE: 'blue', PERDU: 'red', DESACTIVE: 'gray' }
const selectCls = "w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300 bg-white"

export default function BadgesView({ role }) {
  const [badges,    setBadges]    = useState([])
  const [membres,   setMembres]   = useState([])
  const [loading,   setLoading]   = useState(true)
  const [alert,     setAlert]     = useState(null)
  const [confirm,   setConfirm]   = useState(null)
  const [assocForm, setAssocForm] = useState({ idBadge: '', idPorteur: '' })

  useEffect(() => {
    charger()
    api.utilisateurs.lister(role).then(r => setMembres(r.data)).catch(() => {})
  }, [])

  const charger = async () => {
    setLoading(true)
    try {
      const res = await api.badges.lister()
      setBadges(res.data)
    } catch (e) {
      setAlert({ type: 'error', message: 'Impossible de charger les badges. Veuillez réessayer.' })
    } finally {
      setLoading(false)
    }
  }

  const creerBadge = async () => {
    try {
      await api.badges.creer(role)
      setAlert({ type: 'success', message: 'Badge créé. Il est maintenant disponible pour être associé à un membre.' })
      charger()
    } catch (e) {
      setAlert({ type: 'error', message: 'Impossible de créer le badge. Veuillez réessayer.' })
    }
  }

  const associer = async () => {
    if (!assocForm.idBadge) return setAlert({ type: 'error', message: 'Veuillez saisir le numéro du badge.' })
    if (!assocForm.idPorteur) return setAlert({ type: 'error', message: 'Veuillez saisir le numéro du membre.' })
    try {
      await api.badges.associer(assocForm.idBadge, assocForm.idPorteur, role)
      setAlert({ type: 'success', message: `Badge #${assocForm.idBadge} associé au membre #${assocForm.idPorteur}.` })
      setAssocForm({ idBadge: '', idPorteur: '' })
      charger()
    } catch (e) {
      const status = e.response?.status
      const msg = e.response?.data?.error
      if (status === 404) {
        setAlert({ type: 'error', message: 'Badge ou membre introuvable. Vérifiez les numéros saisis.' })
      } else if (status === 409) {
        setAlert({ type: 'error', message: msg || 'Conflit : badge déjà associé ou membre possède déjà un badge.' })
      } else {
        setAlert({ type: 'error', message: msg || 'Impossible d\'associer le badge. Veuillez réessayer.' })
      }
    }
  }

  const supprimer = (idBadge) => {
    setConfirm({ message: `Supprimer définitivement le badge #${idBadge} ?`, onConfirm: async () => {
      setConfirm(null)
      try {
        await api.badges.supprimer(idBadge, role)
        setAlert({ type: 'success', message: `Badge #${idBadge} supprimé.` })
        charger()
      } catch (e) {
        setAlert({ type: 'error', message: 'Impossible de supprimer ce badge. Veuillez réessayer.' })
      }
    }})
  }

  const dissocier = (idBadge) => {
    setConfirm({ message: `Dissocier le badge #${idBadge} de son porteur ?`, onConfirm: async () => {
      setConfirm(null)
      try {
        await api.badges.dissocier(idBadge, role)
        setAlert({ type: 'success', message: 'Badge dissocié. Il est de nouveau disponible.' })
        charger()
      } catch (e) {
        setAlert({ type: 'error', message: 'Impossible de dissocier ce badge. Veuillez réessayer.' })
      }
    }})
  }

  const posInt = (val) => { const n = parseInt(val); return isNaN(n) || n < 1 ? '' : n }
  const disponibles = badges.filter(b => b.statut === 'DISPONIBLE')
  const associes    = badges.filter(b => b.statut === 'ASSOCIE')

  return (
    <div>
      <ConfirmModal
        message={confirm?.message}
        onConfirm={confirm?.onConfirm}
        onCancel={() => setConfirm(null)}
      />

      <div className="flex items-center justify-between mb-5">
        <h1 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <CreditCard size={20} className="text-blue-600" /> Badges NFC
        </h1>
        {['SECRETAIRE', 'PRESIDENT'].includes(role) && (
          <Btn onClick={creerBadge}>+ Créer un badge</Btn>
        )}
      </div>

      <Alert {...alert} onClose={() => setAlert(null)} />

      <div className="grid grid-cols-3 gap-4 mb-5">
        {[
          { label: 'Total',       value: badges.length,      color: 'bg-gray-50 border-gray-200' },
          { label: 'Associés',    value: associes.length,    color: 'bg-green-50 border-green-200' },
          { label: 'Disponibles', value: disponibles.length, color: 'bg-blue-50 border-blue-200' },
        ].map(s => (
          <div key={s.label} className={`border rounded-xl p-4 ${s.color}`}>
            <div className="text-2xl font-bold text-gray-800">{s.value}</div>
            <div className="text-sm text-gray-500">{s.label}</div>
          </div>
        ))}
      </div>

      {['SECRETAIRE', 'PRESIDENT'].includes(role) && (
        <Card title="Associer un badge à un membre">
          <p className="text-xs text-gray-400 mb-3">
            Un badge nouvellement créé est <strong>disponible</strong> (sans porteur). Sélectionnez-en un et indiquez le membre à qui l'attribuer.
          </p>
          <div className="grid grid-cols-2 gap-x-4">
            <Input label="Numéro du badge" value={assocForm.idBadge}
              onChange={e => setAssocForm(p => ({ ...p, idBadge: posInt(e.target.value) }))}
              type="number" min="1" placeholder="ex. 1" />
            <div className="mb-3">
              <label className="block text-sm text-gray-600 mb-1">Membre</label>
              <select value={assocForm.idPorteur}
                onChange={e => setAssocForm(p => ({ ...p, idPorteur: e.target.value }))}
                className={selectCls}>
                <option value="">-- Choisir un membre --</option>
                {membres.map(m => (
                  <option key={m.id} value={m.id}>{m.prenom} {m.nom} - {m.role}</option>
                ))}
              </select>
            </div>
          </div>
          <Btn onClick={associer} variant="success">Associer</Btn>
        </Card>
      )}

      <Card title="Liste des badges" action={<button onClick={charger} className="p-1.5 rounded bg-blue-50 hover:bg-blue-100 text-blue-500 hover:text-blue-700 cursor-pointer" title="Actualiser"><RefreshCw size={15} /></button>}>
        {loading ? <Spinner /> : (
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-gray-500 border-b">
                <th className="pb-2 pr-4">N°</th>
                <th className="pb-2 pr-4">Statut</th>
                <th className="pb-2 pr-4">Porteur</th>
                <th className="pb-2 pr-4">Créé le</th>
                <th className="pb-2 pr-4">Associé le</th>
                {['SECRETAIRE', 'PRESIDENT'].includes(role) && <th className="pb-2">Actions</th>}
              </tr>
            </thead>
            <tbody>
              {badges.map(b => (
                <tr key={b.idBadge} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="py-2 pr-4 text-gray-400 font-mono">#{b.idBadge}</td>
                  <td className="py-2 pr-4">
                    <BadgeTag label={b.statut} color={STATUT_COLOR[b.statut] || 'gray'} />
                  </td>
                  <td className="py-2 pr-4">
                    {b.idPorteur
                      ? (() => { const m = membres.find(m => m.id === b.idPorteur); return m ? `${m.prenom} ${m.nom}` : `Membre #${b.idPorteur}` })()
                      : <span className="text-gray-400">Non attribué</span>}
                  </td>
                  <td className="py-2 pr-4 text-gray-400 text-xs">
                    {b.dateCreation ? new Date(b.dateCreation).toLocaleDateString('fr-FR') : '-'}
                  </td>
                  <td className="py-2 pr-4 text-gray-400 text-xs">
                    {b.dateAssociation ? new Date(b.dateAssociation).toLocaleDateString('fr-FR') : '-'}
                  </td>
                  {['SECRETAIRE', 'PRESIDENT'].includes(role) && (
                    <td className="py-2">
                      <div className="flex items-center gap-2">
                        {b.statut === 'ASSOCIE' && (
                          <Btn size="sm" variant="outline" onClick={() => dissocier(b.idBadge)}>Dissocier</Btn>
                        )}
                        {b.statut !== 'ASSOCIE' && (
                          <Btn size="sm" variant="danger" onClick={() => supprimer(b.idBadge)}>Supprimer</Btn>
                        )}
                      </div>
                    </td>
                  )}
                </tr>
              ))}
              {badges.length === 0 && (
                <tr><td colSpan={6} className="py-4 text-center text-gray-400">Aucun badge.</td></tr>
              )}
            </tbody>
          </table>
        )}
      </Card>
    </div>
  )
}
