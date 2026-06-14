import { useState, useEffect } from 'react'
import { BarChart2, Users, BookOpen, Trophy, TrendingUp, CheckCircle, XCircle } from 'lucide-react'
import { api } from '../api'
import { Card, Alert, BadgeTag, Spinner } from '../components/Card'

function KpiCard({ label, value, color = 'blue' }) {
  const colors = {
    blue:  'bg-blue-50 text-blue-700',
    green: 'bg-green-50 text-green-700',
  }
  return (
    <div className={`${colors[color].split(' ')[0]} rounded-xl p-4 flex-1`}>
      <div className={`text-3xl font-bold ${colors[color].split(' ')[1]}`}>{value}</div>
      <div className="text-sm text-gray-600 mt-1">{label}</div>
    </div>
  )
}

function BarChart({ data }) {
  const max = Math.max(...data.map(d => d.value), 1)
  return (
    <div className="flex items-end gap-3 h-28 mt-2">
      {data.map(({ label, value }) => (
        <div key={label} className="flex flex-col items-center gap-1 flex-1">
          <span className="text-xs font-bold text-gray-700">{value}</span>
          <div className="w-full bg-gray-100 rounded-t-md flex items-end" style={{ height: '72px' }}>
            <div className="w-full bg-blue-500 rounded-t-md transition-all duration-500"
              style={{ height: `${Math.max((value / max) * 72, value > 0 ? 4 : 0)}px` }} />
          </div>
          <span className="text-xs text-gray-500">Niv.{label}</span>
        </div>
      ))}
    </div>
  )
}

const selectCls = "w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300 bg-white"

export default function StatistiquesView({ role }) {
  const [alert, setAlert] = useState(null)

  // Données de référence chargées au démarrage
  const [membres, setMembres] = useState([])
  const [cours,   setCours]   = useState([])

  // Bloc 1 - KPIs globaux
  const [globalCours,    setGlobalCours]    = useState(null)
  const [loadingGlobal,  setLoadingGlobal]  = useState(true)

  // Bloc 2 - Nb compétitions par niveau
  const [niveauxData,    setNiveauxData]    = useState(null)
  const [loadingNiveaux, setLoadingNiveaux] = useState(true)

  // Bloc 3 - Présences à un cours
  const [coursId,        setCoursId]        = useState('')
  const [elevesPresents, setElevesPresents] = useState(null)
  const [loadingCours,   setLoadingCours]   = useState(false)

  // Bloc 4 - Parcours d'un membre
  const [eleveId,      setEleveId]      = useState('')
  const [periodeDebut, setPeriodeDebut] = useState('')
  const [periodeFin,   setPeriodeFin]   = useState('')
  const [coursEleve,   setCoursEleve]   = useState(null)
  const [competEleve,  setCompetEleve]  = useState(null)
  const [loadingEleve, setLoadingEleve] = useState(false)

  const err = (e) => setAlert({ type: 'error', message: e.response?.data?.error || 'Une erreur est survenue.' })
  const nomMembre = (id) => {
    const m = membres.find(m => m.id === id)
    return m ? `${m.prenom} ${m.nom}` : `Membre #${id}`
  }

  useEffect(() => {
    // Charger référentiels
    api.utilisateurs.lister(role).then(r => setMembres(r.data)).catch(() => {})
    api.cours.lister().then(r => setCours(r.data)).catch(() => {})

    // Bloc 1
    const loadGlobal = async () => {
      try {
        const res = await api.statistiques.globalCours(role)
        setGlobalCours(res.data)
      } catch (e) { err(e) } finally { setLoadingGlobal(false) }
    }
    // Bloc 2
    const loadNiveaux = async () => {
      try {
        const results = await Promise.all([1,2,3,4,5].map(n => api.statistiques.nbCompetitionsNiveau(n, role)))
        setNiveauxData(results.map((r, i) => ({ label: String(i + 1), value: Number(r.data) })))
      } catch (e) { err(e) } finally { setLoadingNiveaux(false) }
    }
    loadGlobal()
    loadNiveaux()
  }, [])

  const chargerElevesParCours = async () => {
    if (!coursId) return
    setLoadingCours(true)
    setElevesPresents(null)
    try {
      // presences/cours retourne idBadge + idPorteur
      const res = await api.presences.listerParCours(coursId, role)
      setElevesPresents(res.data)
    } catch (e) { err(e) } finally { setLoadingCours(false) }
  }

  const chargerCoursEleve = async () => {
    if (!eleveId) return
    setLoadingEleve(true)
    setCoursEleve(null)
    setCompetEleve(null)
    try {
      const [c, k] = await Promise.all([
        api.statistiques.coursParEleve(eleveId, role, periodeDebut || null, periodeFin || null),
        api.statistiques.competitionsParEleve(eleveId, role, periodeDebut || null, periodeFin || null),
      ])
      setCoursEleve(c.data)
      const enriched = await Promise.all(k.data.map(async r => {
        try {
          const comp = await api.competitions.consulter(r.competitionId)
          return { ...r, titre: comp.data.titre, niveauCible: comp.data.niveauCible }
        } catch {
          return { ...r, titre: r.competitionId, niveauCible: null }
        }
      }))
      setCompetEleve(enriched)
    } catch (e) { err(e) } finally { setLoadingEleve(false) }
  }

  const nbPresents = coursEleve ? coursEleve.filter(c => c.present).length : 0
  const nbAbsents  = coursEleve ? coursEleve.filter(c => !c.present).length : 0
  const totalCompet = niveauxData ? niveauxData.reduce((s, d) => s + d.value, 0) : 0
  const membreSelectionne = membres.find(m => m.id === Number(eleveId))

  return (
    <div>
      <h1 className="text-xl font-bold text-gray-800 mb-5 flex items-center gap-2">
        <BarChart2 size={20} className="text-blue-600" /> Statistiques du club
      </h1>
      <Alert {...alert} onClose={() => setAlert(null)} />

      {/* ── Blocs 1 & 2 ─────────────────────────────────────────────── */}
      <div className="grid grid-cols-2 gap-4 mb-4">
        <Card title={<span className="flex items-center gap-2"><BookOpen size={15} className="text-blue-500" /> Activité des cours</span>}>
          {loadingGlobal ? <Spinner /> : globalCours ? (
            <div className="flex gap-3 mt-1">
              <KpiCard label="Cours organisés" value={globalCours.nombreCours} color="blue" />
              <KpiCard label="Moy. élèves présents / cours" value={Number(globalCours.nombreMoyenElevesPresents).toFixed(1)} color="green" />
            </div>
          ) : <p className="text-gray-400 text-sm">Aucune donnée.</p>}
        </Card>

        <Card title={<span className="flex items-center gap-2"><Trophy size={15} className="text-blue-500" /> Nb compétitions par niveau</span>}>
          {loadingNiveaux ? <Spinner /> : niveauxData ? (
            <>
              <p className="text-xs text-gray-400 mb-1">{totalCompet} compétition(s) au total</p>
              <BarChart data={niveauxData} />
            </>
          ) : <p className="text-gray-400 text-sm">Aucune donnée.</p>}
        </Card>
      </div>

      {/* ── Bloc 3 : Élèves présents à un cours ──────────────────────── */}
      <Card title={<span className="flex items-center gap-2"><Users size={15} className="text-blue-500" /> Élèves présents à un cours</span>}>
        <div className="flex gap-2 mb-3">
          <select value={coursId} onChange={e => { setCoursId(e.target.value); setElevesPresents(null) }}
            className={`flex-1 ${selectCls}`}>
            <option value="">-- Sélectionner un cours --</option>
            {cours.map(c => (
              <option key={c.id} value={c.id}>
                {c.titre} - {c.date} · Niv.{c.niveauCible}
              </option>
            ))}
          </select>
          <button onClick={chargerElevesParCours} disabled={!coursId || loadingCours}
            className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm rounded-lg disabled:opacity-50 cursor-pointer">
            Afficher
          </button>
        </div>

        {loadingCours ? <Spinner /> : elevesPresents !== null && (
          elevesPresents.length === 0
            ? <p className="text-gray-400 text-sm">Aucune présence enregistrée pour ce cours.</p>
            : <>
                <p className="text-sm font-semibold text-gray-700 mb-2">{elevesPresents.length} élève(s) présent(s)</p>
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-left text-gray-400 text-xs border-b">
                      <th className="pb-1 pr-4">Membre</th>
                      <th className="pb-1 pr-4">Badge</th>
                      <th className="pb-1">Date de scan</th>
                    </tr>
                  </thead>
                  <tbody>
                    {elevesPresents.map((p, i) => (
                      <tr key={i} className="border-b border-gray-50 hover:bg-gray-50">
                        <td className="py-1.5 pr-4 font-medium">{nomMembre(p.idPorteur)}</td>
                        <td className="py-1.5 pr-4 text-gray-500 font-mono text-xs">#{p.idBadge}</td>
                        <td className="py-1.5 text-gray-400 text-xs">
                          {p.dateBadgeage ? new Date(p.dateBadgeage).toLocaleString('fr-FR') : '-'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </>
        )}
      </Card>

      {/* ── Bloc 4 : Parcours d'un membre ────────────────────────────── */}
      <Card title={<span className="flex items-center gap-2"><TrendingUp size={15} className="text-blue-500" /> Parcours d'un membre</span>}>
        <div className="flex gap-2 mb-2">
          <select value={eleveId} onChange={e => { setEleveId(e.target.value); setCoursEleve(null); setCompetEleve(null) }}
            className={`flex-1 ${selectCls}`}>
            <option value="">-- Sélectionner un membre --</option>
            {membres.map(m => (
              <option key={m.id} value={m.id}>
                {m.prenom} {m.nom} - {m.role} · Niv.{m.niveauExpertise}
              </option>
            ))}
          </select>
          <button onClick={chargerCoursEleve} disabled={!eleveId || loadingEleve}
            className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm rounded-lg disabled:opacity-50 cursor-pointer">
            Afficher
          </button>
        </div>

        <div className="flex gap-2 mb-3">
          <div className="flex-1">
            <label className="block text-xs text-gray-400 mb-1">Période - du <span className="italic">(optionnel)</span></label>
            <input type="date" value={periodeDebut} onChange={e => setPeriodeDebut(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300" />
          </div>
          <div className="flex-1">
            <label className="block text-xs text-gray-400 mb-1">au</label>
            <input type="date" value={periodeFin} onChange={e => setPeriodeFin(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300" />
          </div>
        </div>

        {loadingEleve ? <Spinner /> : (coursEleve !== null || competEleve !== null) && (
          <>
            {membreSelectionne && (
              <p className="text-sm font-semibold text-gray-700 mb-3">
                {membreSelectionne.prenom} {membreSelectionne.nom} - {membreSelectionne.role} · Niveau {membreSelectionne.niveauExpertise}
              </p>
            )}
            <div className="grid grid-cols-2 gap-4">

              {/* Cours */}
              <div>
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm font-semibold text-gray-700 flex items-center gap-1">
                    <BookOpen size={13} className="text-blue-500" /> Cours ({coursEleve?.length ?? 0})
                  </span>
                  {coursEleve?.length > 0 && (
                    <div className="flex gap-2 text-xs">
                      <span className="text-green-600 font-medium">{nbPresents} présent(s)</span>
                      <span className="text-red-400">{nbAbsents} absent(s)</span>
                    </div>
                  )}
                </div>
                {coursEleve?.length > 0 && (
                  <div className="flex gap-0.5 h-1.5 rounded-full overflow-hidden mb-2">
                    <div className="bg-green-400 transition-all" style={{ width: `${(nbPresents / coursEleve.length) * 100}%` }} />
                    <div className="bg-red-300 flex-1" />
                  </div>
                )}
                {coursEleve?.length === 0
                  ? <p className="text-gray-400 text-xs">Aucun cours sur cette période.</p>
                  : <div className="space-y-1 max-h-52 overflow-y-auto">
                      {coursEleve?.map((c, i) => (
                        <div key={i} className={`flex items-start justify-between p-2 rounded-lg text-xs ${c.present ? 'bg-green-50' : 'bg-red-50'}`}>
                          <div>
                            <div className="font-medium text-gray-800">{c.titre}</div>
                            <div className="text-gray-400 mt-0.5">{c.date} · Niv.{c.niveauCible}</div>
                          </div>
                          <span className={`ml-2 flex items-center gap-0.5 font-semibold shrink-0 ${c.present ? 'text-green-600' : 'text-red-400'}`}>
                            {c.present ? <><CheckCircle size={12} /> Présent</> : <><XCircle size={12} /> Absent</>}
                          </span>
                        </div>
                      ))}
                    </div>
                }
              </div>

              {/* Compétitions */}
              <div>
                <span className="text-sm font-semibold text-gray-700 flex items-center gap-1 mb-2">
                  <Trophy size={13} className="text-blue-500" /> Compétitions ({competEleve?.length ?? 0})
                </span>
                {competEleve?.length === 0
                  ? <p className="text-gray-400 text-xs">Aucune compétition sur cette période.</p>
                  : <div className="space-y-1 max-h-52 overflow-y-auto">
                      {competEleve?.map((r, i) => (
                        <div key={i} className="flex items-start justify-between p-2 bg-blue-50 rounded-lg text-xs">
                          <div>
                            <div className="font-medium text-gray-800">{r.titre}</div>
                            <div className="text-gray-400 mt-0.5">
                              {r.niveauCible && `Niv.${r.niveauCible} · `}
                              {r.competitionDate ? new Date(r.competitionDate).toLocaleDateString('fr-FR') : '-'}
                            </div>
                          </div>
                          <span className="ml-2 font-bold text-blue-700 shrink-0">
                            {r.note != null ? `${Number(r.note).toFixed(1)} / 10` : 'N/A'}
                          </span>
                        </div>
                      ))}
                    </div>
                }
              </div>

            </div>
          </>
        )}
      </Card>
    </div>
  )
}
