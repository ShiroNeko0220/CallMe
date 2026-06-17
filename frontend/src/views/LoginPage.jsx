import { useState } from 'react'
import { Eye, EyeOff, X, UserPlus, CheckCircle } from 'lucide-react'
import { api } from '../api'
import { Input, Btn, Alert } from '../components/Card'

const initialSignup = {
  nom: '', prenom: '', email: '',
  idConnexion: { login: '', mdp: '' },
  adresse: { ville: '', pays: 'France' },
}

export default function LoginPage({ onLogin }) {
  const [form, setForm] = useState({ login: '', mdp: '' })
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)
  const [showPwd, setShowPwd] = useState(false)
  const [showSignup, setShowSignup] = useState(false)
  const [signup, setSignup] = useState(initialSignup)
  const [signupError, setSignupError] = useState(null)
  const [signupLoading, setSignupLoading] = useState(false)
  const [showSignupPwd, setShowSignupPwd] = useState(false)
  const [showSuccessModal, setShowSuccessModal] = useState(false)
  const [createdLogin, setCreatedLogin] = useState('')

  const submit = async (e) => {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const res = await api.utilisateurs.login(form)
      onLogin(res.data)
    } catch (err) {
      setError('Identifiant ou mot de passe incorrect. Veuillez réessayer.')
    } finally {
      setLoading(false)
    }
  }

  const fSignup = (field, val) => setSignup(p => ({ ...p, [field]: val }))
  const fSignupCnx = (field, val) => setSignup(p => ({ ...p, idConnexion: { ...p.idConnexion, [field]: val } }))
  const fSignupAdr = (field, val) => setSignup(p => ({ ...p, adresse: { ...p.adresse, [field]: val } }))

  const creerCompte = async (e) => {
    e.preventDefault()
    setSignupError(null)
    setSignupLoading(true)
    try {
      await api.utilisateurs.creer(signup)
      setCreatedLogin(signup.idConnexion.login)
      setForm({ login: signup.idConnexion.login, mdp: '' })
      setSignup(initialSignup)
      setShowSignup(false)
      setShowSuccessModal(true)
    } catch (err) {
      const msg = err.response?.data?.error
      setSignupError(msg || "Impossible de créer le compte. Vérifiez le login et l'email.")
    } finally {
      setSignupLoading(false)
    }
  }

  return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="w-full max-w-sm">
          <div className="text-center mb-8">
            <div className="flex items-center justify-center gap-3 mb-1">
              <img src="/logo.svg" alt="Oduru" className="h-12 w-12" />
              <span className="text-3xl font-bold text-blue-600">Oduru</span>
            </div>
            <p className="text-gray-400 text-sm">Club de danse</p>
          </div>

          <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-8">
            <h2 className="text-lg font-semibold text-gray-800 mb-6">Connexion</h2>

            <form onSubmit={submit} className="space-y-4">
              <div>
                <label className="block text-sm text-gray-600 mb-1">Identifiant</label>
                <input
                    type="text"
                    value={form.login}
                    onChange={e => setForm(p => ({ ...p, login: e.target.value }))}
                    required
                    autoFocus
                    placeholder="Identifiant"
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-300"
                />
              </div>

              <div>
                <label className="block text-sm text-gray-600 mb-1">Mot de passe</label>
                <div className="relative">
                  <input
                      type={showPwd ? 'text' : 'password'}
                      value={form.mdp}
                      onChange={e => setForm(p => ({ ...p, mdp: e.target.value }))}
                      required
                      placeholder="••••••••"
                      className="w-full border border-gray-300 rounded-lg px-3 py-2 pr-9 text-sm focus:outline-none focus:ring-2 focus:ring-blue-300"
                  />
                  <button
                      type="button"
                      onClick={() => setShowPwd(v => !v)}
                      className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 cursor-pointer"
                  >
                    {showPwd ? <Eye size={16} /> : <EyeOff size={16} />}
                  </button>
                </div>
              </div>

              {error && <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">{error}</p>}

              <button
                  type="submit"
                  disabled={loading}
                  className="w-full bg-blue-600 hover:bg-blue-700 disabled:opacity-60 text-white font-medium py-2 rounded-lg text-sm transition-colors cursor-pointer"
              >
                {loading ? 'Connexion...' : 'Se connecter'}
              </button>
            </form>

            <div className="mt-4 flex justify-center">
              <button
                  type="button"
                  onClick={() => { setShowSignup(true); setSignupError(null) }}
                  className="inline-flex items-center gap-1.5 text-sm text-gray-500 border border-gray-300 hover:bg-gray-50 hover:text-gray-700 px-3 py-1.5 rounded-lg transition-colors cursor-pointer"
              >
                <UserPlus size={14} />
                Nouveau compte
              </button>
            </div>
          </div>
        </div>

        {showSignup && (
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
              <div className="bg-white rounded-2xl shadow-xl border border-gray-200 p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <h2 className="text-lg font-semibold text-gray-800">Créer un compte membre</h2>
                    <p className="text-xs text-gray-500 mt-1">Le compte est créé avec le rôle MEMBRE et le niveau 1 par défaut.</p>
                  </div>
                  <button onClick={() => setShowSignup(false)} className="text-gray-400 hover:text-gray-600 cursor-pointer"><X size={20} /></button>
                </div>

                <form onSubmit={creerCompte}>
                  <div className="grid grid-cols-2 gap-x-4">
                    <Input label="Nom" required value={signup.nom} onChange={e => fSignup('nom', e.target.value)} placeholder="Dupont" />
                    <Input label="Prénom" required value={signup.prenom} onChange={e => fSignup('prenom', e.target.value)} placeholder="Robert" />
                    <Input label="Email" required type="email" value={signup.email} onChange={e => fSignup('email', e.target.value)} placeholder="exemple@exemple
                    .fr" />
                    <Input label="Login" required value={signup.idConnexion.login} onChange={e => fSignupCnx('login', e.target.value)} placeholder="robert" />
                    <div className="mb-3">
                      <label className="block text-sm text-gray-600 mb-1">Mot de passe <span className="text-red-500">*</span></label>
                      <div className="relative">
                        <input
                            type={showSignupPwd ? 'text' : 'password'}
                            required
                            value={signup.idConnexion.mdp}
                            onChange={e => fSignupCnx('mdp', e.target.value)}
                            placeholder="••••••••"
                            className="w-full border border-gray-300 rounded-lg px-3 py-2 pr-9 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300"
                        />
                        <button type="button" onClick={() => setShowSignupPwd(v => !v)} className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 cursor-pointer">
                          {showSignupPwd ? <Eye size={16} /> : <EyeOff size={16} />}
                        </button>
                      </div>
                    </div>
                    <Input label="Ville" optional value={signup.adresse.ville} onChange={e => fSignupAdr('ville', e.target.value)} placeholder="Toulouse" />
                    <Input label="Pays" optional value={signup.adresse.pays} onChange={e => fSignupAdr('pays', e.target.value)} placeholder="France" />
                  </div>

                  <Alert type="error" message={signupError} onClose={() => setSignupError(null)} />

                  <div className="flex justify-end gap-2 mt-2">
                    <Btn type="button" variant="outline" onClick={() => setShowSignup(false)}>Annuler</Btn>
                    <Btn type="submit" disabled={signupLoading}>{signupLoading ? 'Création...' : 'Créer le compte'}</Btn>
                  </div>
                </form>
              </div>
            </div>
        )}

        {showSuccessModal && (
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
              <div className="bg-white rounded-2xl shadow-xl border border-gray-200 p-8 w-full max-w-sm text-center">
                <div className="flex justify-center mb-4">
                  <CheckCircle size={48} className="text-green-500" />
                </div>
                <h2 className="text-lg font-semibold text-gray-800 mb-2">Compte créé !</h2>
                <p className="text-sm text-gray-500 mb-1">
                  Votre compte <span className="font-medium text-gray-700">"{createdLogin}"</span> a été créé avec succès.
                </p>
                <p className="text-sm text-gray-500 mb-6">
                  Vous pouvez maintenant vous connecter avec vos identifiants.
                </p>
                <button
                    onClick={() => setShowSuccessModal(false)}
                    className="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 rounded-lg text-sm transition-colors cursor-pointer"
                >
                  Me connecter
                </button>
              </div>
            </div>
        )}
      </div>
  )
}
