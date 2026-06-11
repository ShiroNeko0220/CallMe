import { useState } from 'react'
import { Users, BookOpen, Trophy, CreditCard, CheckSquare, BarChart2, LogOut } from 'lucide-react'
import LoginPage        from './views/LoginPage'
import MembresView      from './views/MembresView'
import CoursView        from './views/CoursView'
import CompetitionsView from './views/CompetitionsView'
import BadgesView       from './views/BadgesView'
import PresencesView    from './views/PresencesView'
import StatistiquesView from './views/StatistiquesView'

const ROLE_COLORS = {
  MEMBRE:     'bg-blue-100 text-blue-800',
  ENSEIGNANT: 'bg-green-100 text-green-800',
  SECRETAIRE: 'bg-amber-100 text-amber-800',
  PRESIDENT:  'bg-purple-100 text-purple-800',
}

const NAV = [
  { key: 'membres',      label: 'Membres',      Icon: Users },
  { key: 'cours',        label: 'Cours',         Icon: BookOpen },
  { key: 'competitions', label: 'Compétitions',  Icon: Trophy },
  { key: 'badges',       label: 'Badges',        Icon: CreditCard },
  { key: 'presences',    label: 'Présences',     Icon: CheckSquare },
  { key: 'statistiques', label: 'Statistiques',  Icon: BarChart2 },
]

export default function App() {
  const [user,    setUser]    = useState(null)
  const [section, setSection] = useState('membres')

  if (!user) {
    return <LoginPage onLogin={setUser} />
  }

  const role = user.role

  return (
    <div className="min-h-screen bg-gray-50">

      <header className="bg-white border-b border-gray-200 px-6 py-3 flex items-center justify-between shadow-sm">
        <div className="flex items-center gap-3">
          <img src="/logo.svg" alt="Oduru" className="h-9 w-9" />
          <div>
            <span className="text-2xl font-bold text-blue-600">Oduru</span>
            <span className="text-sm text-gray-400 ml-2">Club de danse rythmique</span>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <span className="text-sm text-gray-700 font-medium">
            {user.prenom} {user.nom}
          </span>
          <span className={`text-xs font-semibold px-3 py-1 rounded-full ${ROLE_COLORS[role] ?? 'bg-gray-100 text-gray-600'}`}>
            {role}
          </span>
          <button
            onClick={() => { setUser(null); setSection('membres') }}
            className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-red-600 transition-colors cursor-pointer"
          >
            <LogOut size={15} />
            Déconnexion
          </button>
        </div>
      </header>

      <div className="flex">
        <nav className="w-52 min-h-screen bg-white border-r border-gray-200 pt-4 shrink-0">
          {NAV.map(({ key, label, Icon }) => (
            <button
              key={key}
              onClick={() => setSection(key)}
              className={`w-full text-left px-5 py-3 text-sm transition-colors cursor-pointer flex items-center gap-2.5 ${
                section === key
                  ? 'bg-blue-50 text-blue-700 font-medium border-r-2 border-blue-500'
                  : 'text-gray-600 hover:bg-gray-50'
              }`}
            >
              <Icon size={16} className={section === key ? 'text-blue-600' : 'text-gray-400'} />
              {label}
            </button>
          ))}

          <div className="mt-6 mx-3 p-3 bg-gray-50 rounded-lg text-xs text-gray-500">
            <div className="font-medium mb-1">Accès selon rôle :</div>
            <div>MEMBRE → lecture</div>
            <div>ENSEIGNANT → cours, compéts</div>
            <div>SECRETAIRE → badges, membres</div>
            <div>PRESIDENT → tout</div>
          </div>
        </nav>

        <main className="flex-1 p-6 min-w-0">
          {section === 'membres'      && <MembresView      role={role} />}
          {section === 'cours'        && <CoursView        role={role} />}
          {section === 'competitions' && <CompetitionsView role={role} />}
          {section === 'badges'       && <BadgesView       role={role} />}
          {section === 'presences'    && <PresencesView    role={role} />}
          {section === 'statistiques' && <StatistiquesView role={role} />}
        </main>
      </div>
    </div>
  )
}
