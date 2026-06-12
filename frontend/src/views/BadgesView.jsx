import { useState, useEffect } from 'react'
import { CreditCard } from 'lucide-react'
import { api } from '../api'
import { Card, BadgeTag, Btn, Input, Alert } from '../components/Card'

const STATUT_COLOR = { ASSOCIE: 'green', DISPONIBLE: 'blue', PERDU: 'red', DESACTIVE: 'gray' }

export default function BadgesView({ role }) {
  const [badges,    setBadges]    = useState([])
  const [alert,     setAlert]     = useState(null)
  const [assocForm, setAssocForm] = useState({ idBadge: '', idPorteur: '' })

  useEffect(() => { charger() }, [])

  const charger = async () => {
    try {
      const res = await api.badges.lister()
      setBadges(res.data)
    } catch (e) {
      setAlert({ type: 'error', message: 'Impossible de charger les badges. Veuillez réessayer.' })
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
        setAlert({ type: 'error', message: 'Ce badge est déjà associé à un membre. Dissociez-le d\'abord.' })
      } else {
        setAlert({ type: 'error', message: msg || 'Impossible d\'associer le badge. Veuillez réessayer.' })
      }
    }
  }

  const dissocier = async (idBadge) => {
    try {
      await api.badges.dissocier(idBadge, role)
      setAlert({ type: 'success', message: 'Badge dissocié. Il est de nouveau disponible.' })
      charger()
    } catch (e) {
      setAlert({ type: 'error', message: 'Impossible de dissocier ce badge. Veuillez réessayer.' })
    }
  }

  const disponibles = badges.filter(b => b.statut === 'DISPONIBLE')
  const associes    = badges.filter(b => b.statut === 'ASSOCIE')

  return (
    <div>
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
          <div className="flex items-end gap-3">
            <div className="flex-1">
              <Input label="Numéro du badge" value={assocForm.idBadge}
                onChange={e => setAssocForm(p => ({ ...p, idBadge: e.target.value }))}
                type="number" min="1" placeholder="ex. 1" />
            </div>
            <div className="flex-1">
              <Input label="Numéro du membre" value={assocForm.idPorteur}
                onChange={e => setAssocForm(p => ({ ...p, idPorteur: e.target.value }))}
                type="number" min="1" placeholder="ex. 5" />
            </div>
            <div className="mb-3">
              <Btn onClick={associer} variant="success">Associer</Btn>
            </div>
          </div>
        </Card>
      )}

      <Card title="Liste des badges" action={<Btn variant="outline" onClick={charger}>Actualiser</Btn>}>
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
                  {b.idPorteur ? `Membre #${b.idPorteur}` : <span className="text-gray-400">Non attribué</span>}
                </td>
                <td className="py-2 pr-4 text-gray-400 text-xs">
                  {b.dateCreation ? new Date(b.dateCreation).toLocaleDateString('fr-FR') : '—'}
                </td>
                <td className="py-2 pr-4 text-gray-400 text-xs">
                  {b.dateAssociation ? new Date(b.dateAssociation).toLocaleDateString('fr-FR') : '—'}
                </td>
                {['SECRETAIRE', 'PRESIDENT'].includes(role) && (
                  <td className="py-2">
                    {b.statut === 'ASSOCIE' && (
                      <Btn size="sm" variant="outline" onClick={() => dissocier(b.idBadge)}>Dissocier</Btn>
                    )}
                  </td>
                )}
              </tr>
            ))}
            {badges.length === 0 && (
              <tr><td colSpan={6} className="py-4 text-center text-gray-400">Aucun badge.</td></tr>
            )}
          </tbody>
        </table>
      </Card>
    </div>
  )
}