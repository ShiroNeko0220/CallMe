import { useState } from 'react'
import { Users, BookOpen, Trophy, CreditCard, CheckSquare, BarChart2, LogOut, UserCircle } from 'lucide-react'
import LoginPage        from './views/LoginPage'
import MembresView      from './views/MembresView'
import CoursView        from './views/CoursView'
import CompetitionsView from './views/CompetitionsView'
import BadgesView       from './views/BadgesView'
import PresencesView    from './views/PresencesView'
import StatistiquesView from './views/StatistiquesView'

const NAV = [
  { key: 'membres',      label: 'Membres',      Icon: Users,       roles: ['ENSEIGNANT', 'SECRETAIRE', 'PRESIDENT'] },
  { key: 'cours',        label: 'Cours',         Icon: BookOpen,    roles: ['MEMBRE', 'ENSEIGNANT', 'SECRETAIRE', 'PRESIDENT'] },
  { key: 'competitions', label: 'Compétitions',  Icon: Trophy,      roles: ['MEMBRE', 'ENSEIGNANT', 'SECRETAIRE', 'PRESIDENT'] },
  { key: 'badges',       label: 'Badges',        Icon: CreditCard,  roles: ['SECRETAIRE', 'PRESIDENT'] },
  { key: 'presences',    label: 'Présences',     Icon: CheckSquare, roles: ['MEMBRE', 'ENSEIGNANT', 'SECRETAIRE', 'PRESIDENT'] },
  { key: 'statistiques', label: 'Statistiques',  Icon: BarChart2,   roles: ['PRESIDENT'] },
]

export default function App() {
  const [user,    setUser]    = useState(() => {
    try { return JSON.parse(localStorage.getItem('callme_user')) } catch { return null }
  })
  const [section, setSection] = useState('membres')

  const login  = (u) => { localStorage.setItem('callme_user', JSON.stringify(u)); setUser(u) }
  const logout = () => { localStorage.removeItem('callme_user'); setUser(null); setSection('membres') }

  if (!user) return <LoginPage onLogin={login} />

  const role        = user.role
  const visibleNav  = NAV.filter(({ roles }) => roles.includes(role))
  const activeSection = visibleNav.find(n => n.key === section) ? section : visibleNav[0]?.key

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">

      <header className="bg-white border-b border-gray-200 px-6 py-3 flex items-center justify-between shadow-sm">
        <div className="flex items-center gap-3">
          <img src="/logo.svg" alt="Oduru" className="h-9 w-9" />
          <div>
            <span className="text-2xl font-bold text-blue-600">Oduru</span>
            <span className="text-sm text-gray-400 ml-2">Club de danse</span>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2 text-sm text-gray-700">
            <UserCircle size={20} className="text-gray-400" />
            <span className="font-medium">{user.prenom} {user.nom}</span>
          </div>
          <button
            onClick={logout}
            className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-red-600 transition-colors cursor-pointer"
          >
            <LogOut size={15} />
            Déconnexion
          </button>
        </div>
      </header>

      <div className="flex flex-1">
        <nav className="w-52 bg-white border-r border-gray-200 pt-4 shrink-0">
          {visibleNav.map(({ key, label, Icon }) => (
            <button
              key={key}
              onClick={() => setSection(key)}
              className={`w-full text-left px-5 py-3 text-sm transition-colors cursor-pointer flex items-center gap-2.5 ${
                activeSection === key
                  ? 'bg-blue-50 text-blue-700 font-medium border-r-2 border-blue-500'
                  : 'text-gray-600 hover:bg-gray-50'
              }`}
            >
              <Icon size={16} className={activeSection === key ? 'text-blue-600' : 'text-gray-400'} />
              {label}
            </button>
          ))}
        </nav>

        <main className="flex-1 p-6 min-w-0">
          {activeSection === 'membres'      && <MembresView      role={role} />}
          {activeSection === 'cours'        && <CoursView        role={role} />}
          {activeSection === 'competitions' && <CompetitionsView role={role} />}
          {activeSection === 'badges'       && <BadgesView       role={role} />}
          {activeSection === 'presences'    && <PresencesView    role={role} user={user} />}
          {activeSection === 'statistiques' && <StatistiquesView role={role} />}
        </main>
      </div>

      <footer className="bg-white border-t border-gray-200 px-6 py-3 text-center text-xs text-gray-400">
        Oduru v1.0.0 · MIAGE Toulouse · 2026
      </footer>

    </div>
  )
}
