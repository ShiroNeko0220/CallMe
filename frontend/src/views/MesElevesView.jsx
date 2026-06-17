import { useState, useEffect } from 'react'
import { Users, Star, RefreshCw } from 'lucide-react'
import { api } from '../api'
import { Card, Spinner, Alert, BadgeTag } from '../components/Card'

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

export default function MesElevesView({ role, currentUser }) {
  const [eleves, setEleves] = useState([])
  const [loading, setLoading] = useState(true)
  const [alert, setAlert] = useState(null)

  useEffect(() => { charger() }, [currentUser?.id])

  const charger = async () => {
    if (!currentUser?.id) return
    setLoading(true)
    try {
      const [coursRes, competRes, membresRes] = await Promise.all([
        api.cours.listerParEnseignant(currentUser.id),
        api.competitions.listerParEnseignant(currentUser.id),
        api.utilisateurs.lister(role),
      ])

      const eleveIds = new Set()

      await Promise.all(coursRes.data.map(async c => {
        try {
          const p = await api.presences.listerParCours(c.id, role)
          p.data.forEach(pr => { if (pr.idPorteur) eleveIds.add(pr.idPorteur) })
        } catch {}
      }))

      await Promise.all(competRes.data.map(async c => {
        try {
          const r = await api.competitions.listerResultats(c.id)
          r.data.forEach(res => { if (res.eleveId) eleveIds.add(res.eleveId) })
        } catch {}
      }))

      const tous = membresRes.data
      setEleves(tous.filter(m => eleveIds.has(m.id)))
    } catch (e) {
      setAlert({ type: 'error', message: 'Impossible de charger vos élèves.' })
    } finally {
      setLoading(false)
    }
  }

  return (
      <div>
        <div className="flex items-center justify-between mb-5">
          <h1 className="text-xl font-bold text-gray-800 flex items-center gap-2">
            <Users size={20} className="text-blue-600" /> Mes élèves
          </h1>
        </div>

        <Alert {...alert} onClose={() => setAlert(null)} />

        <Card
            title={loading ? 'Chargement...' : `${eleves.length} élève(s) dans vos cours et compétitions`}
            action={<button onClick={charger} className="p-1.5 rounded bg-blue-50 hover:bg-blue-100 text-blue-500 hover:text-blue-700 cursor-pointer" title="Actualiser"><RefreshCw size={15} /></button>}
        >
          {loading ? <Spinner /> : eleves.length === 0 ? (
              <p className="text-gray-400 text-sm text-center py-4">Aucun élève trouvé dans vos cours ou compétitions.</p>
          ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                  <tr className="border-b border-gray-100 text-gray-500 text-left">
                    <th className="pb-2 pr-4">Nom</th>
                    <th className="pb-2 pr-4">Email</th>
                    <th className="pb-2 pr-4">Niveau</th>
                    <th className="pb-2">Role</th>
                  </tr>
                  </thead>
                  <tbody>
                  {eleves.map(m => (
                      <tr key={m.id} className="border-b border-gray-50 hover:bg-gray-50">
                        <td className="py-2 pr-4 font-medium">{m.prenom} {m.nom}</td>
                        <td className="py-2 pr-4 text-gray-500">{m.email}</td>
                        <td className="py-2 pr-4"><NiveauStars niveau={m.niveauExpertise} /></td>
                        <td className="py-2"><BadgeTag label={m.role} color={ROLE_COLOR[m.role] || 'gray'} /></td>
                      </tr>
                  ))}
                  </tbody>
                </table>
              </div>
          )}
        </Card>
      </div>
  )
}
