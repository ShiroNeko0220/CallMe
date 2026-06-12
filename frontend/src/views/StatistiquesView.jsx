import { useState } from 'react'
import { BarChart2, Lock, Users, BookOpen, Trophy, TrendingUp } from 'lucide-react'
import { api } from '../api'
import { Card, Btn, Input, Alert, BadgeTag, Spinner } from '../components/Card'

export default function StatistiquesView({ role }) {
  const [alert, setAlert] = useState(null)

  const [globalCours,      setGlobalCours]      = useState(null)
  const [loadingGlobal,    setLoadingGlobal]    = useState(false)
  const [eleveId,          setEleveId]          = useState('')
  const [periodeDebut,     setPeriodeDebut]     = useState('')
  const [periodeFin,       setPeriodeFin]       = useState('')
  const [coursEleve,       setCoursEleve]       = useState(null)
  const [competEleve,      setCompetEleve]      = useState(null)
  const [loadingEleve,     setLoadingEleve]     = useState(false)
  const [coursId,          setCoursId]          = useState('')
  const [elevesPresents,   setElevesPresents]   = useState(null)
  const [loadingCours,     setLoadingCours]     = useState(false)
  const [niveau,           setNiveau]           = useState('')
  const [nbCompet,         setNbCompet]         = useState(null)
  const [loadingNiveau,    setLoadingNiveau]    = useState(false)

  if (!['PRESIDENT'].includes(role)) {
    return (
      <div>
        <h1 className="text-xl font-bold text-gray-800 mb-5 flex items-center gap-2">
          <BarChart2 size={20} className="text-blue-600" /> Statistiques
        </h1>
        <div className="bg-amber-50 border border-amber-200 rounded-xl p-8 text-center">
          <div className="flex justify-center mb-3">
            <Lock size={36} className="text-amber-500" />
          </div>
          <p className="text-amber-700 font-semibold">Accès réservé au Président</p>
          <p className="text-amber-600 text-sm mt-2">Cette section est accessible uniquement depuis le compte Président.</p>
        </div>
      </div>
    )
  }

  const err = (e) => setAlert({ type: 'error', message: e.response?.data?.error || 'Une erreur est survenue.' })

  const chargerGlobalCours = async () => {
    setLoadingGlobal(true)
    try {
      const res = await api.statistiques.globalCours(role)
      setGlobalCours(res.data)
    } catch (e) { err(e) } finally { setLoadingGlobal(false) }
  }

  const chargerCoursEleve = async () => {
    if (!eleveId) return setAlert({ type: 'error', message: 'Entrez un numéro de membre.' })
    setLoadingEleve(true)
    try {
      const [c, k] = await Promise.all([
        api.statistiques.coursParEleve(eleveId, role, periodeDebut || null, periodeFin || null),
        api.statistiques.competitionsParEleve(eleveId, role, periodeDebut || null, periodeFin || null),
      ])
      setCoursEleve(c.data)
      setCompetEleve(k.data)
    } catch (e) { err(e) } finally { setLoadingEleve(false) }
  }

  const chargerElevesParCours = async () => {
    if (!coursId) return setAlert({ type: 'error', message: 'Entrez un numéro de cours.' })
    setLoadingCours(true)
    try {
      const res = await api.statistiques.elevesParCours(coursId, role)
      setElevesPresents(res.data)
    } catch (e) { err(e) } finally { setLoadingCours(false) }
  }

  const chargerNbCompetNiveau = async () => {
    if (!niveau) return setAlert({ type: 'error', message: 'Entrez un niveau (1 à 5).' })
    setLoadingNiveau(true)
    try {
      const res = await api.statistiques.nbCompetitionsNiveau(niveau, role)
      setNbCompet(res.data)
    } catch (e) { err(e) } finally { setLoadingNiveau(false) }
  }

  const nbPresents  = coursEleve ? coursEleve.filter(c => c.present).length : 0
  const nbAbsents   = coursEleve ? coursEleve.filter(c => !c.present).length : 0

  return (
    <div>
      <h1 className="text-xl font-bold text-gray-800 mb-5 flex items-center gap-2">
        <BarChart2 size={20} className="text-blue-600" /> Statistiques du club
      </h1>
      <Alert {...alert} onClose={() => setAlert(null)} />

      <div className="grid grid-cols-2 gap-4">

        {/* Bloc 1 : vue globale des cours */}
        <Card title={<span className="flex items-center gap-2"><BookOpen size={15} className="text-blue-500" /> Activité des cours</span>}>
          <Btn size="sm" onClick={chargerGlobalCours} disabled={loadingGlobal}>
            {loadingGlobal ? 'Chargement…' : 'Charger'}
          </Btn>
          {loadingGlobal ? <Spinner /> : globalCours && (
            <div className="mt-3 space-y-2 text-sm">
              <div className="flex justify-between p-2 bg-blue-50 rounded">
                <span className="text-gray-500">Nombre de cours</span>
                <span className="font-bold text-blue-700">{globalCours.nombreCours}</span>
              </div>
              <div className="flex justify-between p-2 bg-green-50 rounded">
                <span className="text-gray-500">Moy. élèves présents par cours</span>
                <span className="font-bold text-green-700">
                  {Number(globalCours.nombreMoyenElevesPresents).toFixed(1)}
                </span>
              </div>
            </div>
          )}
        </Card>

        {/* Bloc 2 : compétitions par niveau */}

        <Card title={<span className="flex items-center gap-2"><Trophy size={15} className="text-blue-500" /> Compétitions par niveau</span>}>
          <div className="flex gap-2 mb-3">
            <Input value={niveau}
              onChange={e => { const v = Number(e.target.value); setNiveau(v >= 1 && v <= 5 ? v : '') }}
              placeholder="Niveau (1 à 5)" type="number" min="1" max="5" />
            <Btn size="sm" onClick={chargerNbCompetNiveau} disabled={loadingNiveau}>
              {loadingNiveau ? '…' : 'Chercher'}
            </Btn>
          </div>
          {loadingNiveau ? <Spinner /> : nbCompet !== null && (
            <div className="flex justify-between p-2 bg-blue-50 rounded text-sm">
              <span className="text-gray-500">Compétitions niveau {niveau}</span>
              <span className="font-bold text-blue-700">{nbCompet}</span>
            </div>
          )}
        </Card>

        {/* Bloc 3 : présences à un cours donné */}
        <Card title={<span className="flex items-center gap-2"><Users size={15} className="text-blue-500" /> Élèves présents à un cours</span>}>
          <div className="flex gap-2 mb-3">
            <Input value={coursId}
              onChange={e => { const v = Number(e.target.value); setCoursId(v >= 1 ? v : '') }}
              placeholder="Numéro du cours" type="number" min="1" />
            <Btn size="sm" onClick={chargerElevesParCours} disabled={loadingCours}>
              {loadingCours ? '…' : 'Chercher'}
            </Btn>
          </div>
          {loadingCours ? <Spinner /> : elevesPresents !== null && (
            <>
              <p className="text-xs text-gray-500 mb-2">{elevesPresents.length} présence(s) enregistrée(s)</p>
              <div className="space-y-1 max-h-56 overflow-y-auto">
                {elevesPresents.length === 0
                  ? <p className="text-gray-400 text-sm">Aucune présence enregistrée pour ce cours.</p>
                  : elevesPresents.map((p, i) => (
                    <div key={i} className="flex justify-between p-1.5 bg-blue-50 rounded text-xs">
                      <span className="font-medium">Badge #{p.numBadge ?? p.idBadge}</span>
                      <span className="text-gray-500">
                        {p.dateBadgeage ? new Date(p.dateBadgeage).toLocaleString('fr-FR') : '—'}
                      </span>
                    </div>
                  ))
                }
              </div>
            </>
          )}
        </Card>

        {/* Bloc 4 : parcours d'un membre */}
        <Card title={<span className="flex items-center gap-2"><TrendingUp size={15} className="text-blue-500" /> Parcours d'un membre</span>}>
          <div className="flex gap-2 mb-2">
            <Input value={eleveId}
              onChange={e => { const v = Number(e.target.value); setEleveId(v >= 1 ? v : '') }}
              placeholder="Numéro de membre" type="number" min="1" />
            <Btn size="sm" onClick={chargerCoursEleve} disabled={loadingEleve}>
              {loadingEleve ? '…' : 'Chercher'}
            </Btn>
          </div>
          <div className="flex gap-2 mb-3">
            <div className="flex-1">
              <Input label="Du" value={periodeDebut} onChange={e => setPeriodeDebut(e.target.value)} type="date" />
            </div>
            <div className="flex-1">
              <Input label="Au" value={periodeFin} onChange={e => setPeriodeFin(e.target.value)} type="date" />
            </div>
          </div>
          <p className="text-xs text-gray-400 mb-3">Laissez les dates vides pour voir tout l'historique.</p>

          {loadingEleve ? <Spinner /> : (
            <>
              {coursEleve !== null && (
                <div className="mb-4">
                  <div className="flex items-center justify-between mb-1">
                    <p className="text-xs font-semibold text-gray-600">Cours ({coursEleve.length})</p>
                    <div className="flex gap-2 text-xs">
                      <span className="text-green-700 font-medium">{nbPresents} présent(s)</span>
                      <span className="text-red-500">{nbAbsents} absent(s)</span>
                    </div>
                  </div>
                  <div className="space-y-1 max-h-40 overflow-y-auto">
                    {coursEleve.length === 0
                      ? <p className="text-gray-400 text-xs">Aucun cours sur cette période.</p>
                      : coursEleve.map((c, i) => (
                        <div key={i} className={`flex justify-between p-1.5 rounded text-xs ${c.present ? 'bg-green-50' : 'bg-red-50'}`}>
                          <span>{c.titre}</span>
                          <BadgeTag label={c.present ? 'Présent' : 'Absent'} color={c.present ? 'green' : 'red'} />
                        </div>
                      ))
                    }
                  </div>
                </div>
              )}
              {competEleve !== null && (
                <div>
                  <p className="text-xs font-semibold text-gray-600 mb-1">Compétitions ({competEleve.length})</p>
                  <div className="space-y-1 max-h-32 overflow-y-auto">
                    {competEleve.length === 0
                      ? <p className="text-gray-400 text-xs">Aucune compétition sur cette période.</p>
                      : competEleve.map((r, i) => (
                        <div key={i} className="flex justify-between p-1.5 bg-blue-50 rounded text-xs">
                          <span className="text-gray-600">
                            {r.competitionDate ?? `Compétition #${r.competitionId}`}
                          </span>
                          <span className="font-medium text-blue-700">
                            {r.note != null ? `${Number(r.note).toFixed(1)} / 10` : 'Sans note'}
                          </span>
                        </div>
                      ))
                    }
                  </div>
                </div>
              )}
            </>
          )}
        </Card>

      </div>
    </div>
  )
}