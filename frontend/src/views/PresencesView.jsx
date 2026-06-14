import { useState, useEffect } from 'react'
import { CheckSquare, Smartphone, BookOpen } from 'lucide-react'
import { api } from '../api'
import { Card, Btn, Input, Alert, Spinner } from '../components/Card'

export default function PresencesView({ role, user }) {
  const [presences,   setPresences]   = useState([])
  const [mesCours,    setMesCours]    = useState([])
  const [loadingMes,  setLoadingMes]  = useState(false)
  const [alert,       setAlert]       = useState(null)
  const [badgeForm,   setBadgeForm]   = useState({ idBadge: '', idCours: '' })
  const [filtreType,  setFiltreType]  = useState('cours')
  const [filtreId,    setFiltreId]    = useState('')

  useEffect(() => {
    if (role === 'MEMBRE' && user?.id) chargerMesCours()
  }, [])

  const chargerMesCours = async () => {
    setLoadingMes(true)
    try {
      const res = await api.presences.listerParEleve(user.id)
      // Enrichir avec le détail du cours
      const enrichies = await Promise.all(
        res.data.map(async p => {
          try {
            const c = await api.cours.consulter(p.idCours)
            return { ...p, cours: c.data }
          } catch {
            return { ...p, cours: null }
          }
        })
      )
      setMesCours(enrichies)
    } catch {
      // silencieux : section facultative
    } finally {
      setLoadingMes(false)
    }
  }

  const charger = async () => {
    if (!filtreId) return setAlert({ type: 'info', message: 'Veuillez entrer un numéro pour rechercher.' })
    try {
      const res = filtreType === 'eleve'
        ? await api.presences.listerParEleve(filtreId)
        : await api.presences.listerParCours(filtreId, role)
      setPresences(res.data)
      if (res.data.length === 0) setAlert({ type: 'info', message: 'Aucune présence trouvée pour ce critère.' })
    } catch (e) {
      const status = e.response?.status
      if (status === 404) {
        setAlert({ type: 'error', message: `Aucun ${filtreType === 'eleve' ? 'membre' : 'cours'} trouvé avec ce numéro.` })
      } else {
        setAlert({ type: 'error', message: 'Impossible de récupérer les présences. Veuillez réessayer.' })
      }
    }
  }

  const enregistrer = async () => {
    if (!badgeForm.idBadge) return setAlert({ type: 'error', message: 'Veuillez saisir le numéro du badge.' })
    if (!badgeForm.idCours) return setAlert({ type: 'error', message: 'Veuillez saisir le numéro du cours.' })
    try {
      await api.presences.enregistrer(Number(badgeForm.idBadge), Number(badgeForm.idCours))
      setAlert({ type: 'success', message: `Présence enregistrée : badge #${badgeForm.idBadge} pour le cours #${badgeForm.idCours}.` })
      setBadgeForm({ idBadge: '', idCours: '' })
      if (role === 'MEMBRE' && user?.id) chargerMesCours()
      if (filtreId) charger()
    } catch (e) {
      const status = e.response?.status
      if (status === 409) {
        setAlert({ type: 'error', message: 'Ce badge a déjà été scanné pour ce cours.' })
      } else if (status === 404) {
        setAlert({ type: 'error', message: 'Badge ou cours introuvable. Vérifiez les numéros saisis.' })
      } else if (status === 403) {
        setAlert({ type: 'error', message: 'Ce badge n\'est pas attribué à un membre ou n\'est pas actif.' })
      } else {
        setAlert({ type: 'error', message: 'Impossible d\'enregistrer la présence. Veuillez réessayer.' })
      }
    }
  }

  return (
    <div>
      <h1 className="text-xl font-bold text-gray-800 mb-5 flex items-center gap-2">
        <CheckSquare size={20} className="text-blue-600" /> Présences & Badgeage
      </h1>

      <Alert {...alert} onClose={() => setAlert(null)} />

      <Card title={<span className="flex items-center gap-2"><Smartphone size={15} className="text-blue-600" /> Simuler un scan de badge (boîtier NFC)</span>}>
        <p className="text-sm text-gray-500 mb-4">
          Reproduit ce que fait le boîtier physique (accessible à tous).
        </p>
        <div className="flex items-end gap-3">
          <div className="flex-1">
            <Input label="Numéro de badge" value={badgeForm.idBadge}
              onChange={e => setBadgeForm(p => ({ ...p, idBadge: e.target.value }))}
              type="number" min="1" placeholder="ex. 1" />
          </div>
          <div className="flex-1">
            <Input label="Numéro du cours" value={badgeForm.idCours}
              onChange={e => setBadgeForm(p => ({ ...p, idCours: e.target.value }))}
              type="number" min="1" placeholder="ex. 1" />
          </div>
          <div className="mb-3">
            <Btn variant="success" onClick={enregistrer}>Scanner</Btn>
          </div>
        </div>
      </Card>

      {role === 'MEMBRE' && (
        <Card title={<span className="flex items-center gap-2"><BookOpen size={15} className="text-blue-600" /> Mes cours suivis</span>}
          action={<Btn variant="outline" onClick={chargerMesCours}>Actualiser</Btn>}>
          {loadingMes ? <Spinner /> : mesCours.length === 0 ? (
            <p className="text-gray-400 text-sm text-center py-4">Aucun cours enregistré pour votre compte.</p>
          ) : (
            <div className="space-y-2">
              {mesCours.map(p => (
                <div key={p.idPresence} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div>
                    <div className="font-medium text-gray-800">
                      {p.cours ? p.cours.titre : `Cours #${p.idCours}`}
                    </div>
                    <div className="text-xs text-gray-400 mt-0.5">
                      {p.cours && (<>
                        {p.cours.date} · {p.cours.heureDebut} · {p.cours.lieu} · Niveau {p.cours.niveauCible}
                      </>)}
                    </div>
                  </div>
                  <span className="text-xs text-green-600 bg-green-50 border border-green-200 rounded-full px-2 py-0.5">
                    Présent ✓
                  </span>
                </div>
              ))}
            </div>
          )}
        </Card>
      )}

      {['ENSEIGNANT', 'SECRETAIRE', 'PRESIDENT'].includes(role) && (
        <Card title="Consulter les présences">
          <div className="flex items-center gap-3 mb-4 flex-wrap">
            {['cours', 'eleve'].map(t => (
              <button key={t} onClick={() => { setFiltreType(t); setFiltreId(''); setPresences([]) }}
                className={`text-sm px-3 py-1 rounded-full border transition-colors cursor-pointer ${
                  filtreType === t
                    ? 'bg-blue-600 text-white border-blue-600'
                    : 'border-gray-300 hover:bg-gray-50 text-gray-600'
                }`}
              >
                {t === 'eleve' ? 'Par membre' : 'Par cours'}
              </button>
            ))}
            <Input
              value={filtreId}
              onChange={e => setFiltreId(e.target.value)}
              placeholder={filtreType === 'eleve' ? 'Numéro du membre' : 'Numéro du cours'}
              type="number"
              min="1"
            />
            <Btn size="sm" onClick={charger}>Chercher</Btn>
          </div>
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-gray-500 border-b">
                <th className="pb-2 pr-4">N°</th>
                <th className="pb-2 pr-4">Badge</th>
                <th className="pb-2 pr-4">Porteur</th>
                <th className="pb-2 pr-4">Cours</th>
                <th className="pb-2">Date de scan</th>
              </tr>
            </thead>
            <tbody>
              {presences.map(p => (
                <tr key={p.idPresence} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="py-2 pr-4 text-gray-400">{p.idPresence}</td>
                  <td className="py-2 pr-4">#{p.idBadge}</td>
                  <td className="py-2 pr-4">Membre #{p.idPorteur}</td>
                  <td className="py-2 pr-4">Cours #{p.idCours}</td>
                  <td className="py-2 text-gray-400 text-xs">
                    {p.dateBadgeage ? new Date(p.dateBadgeage).toLocaleString('fr-FR') : '—'}
                  </td>
                </tr>
              ))}
              {presences.length === 0 && (
                <tr><td colSpan={5} className="py-4 text-center text-gray-400">
                  Entrez un numéro et cliquez Chercher.
                </td></tr>
              )}
            </tbody>
          </table>
        </Card>
      )}
    </div>
  )
}
