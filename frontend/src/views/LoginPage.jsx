import { useState } from 'react'
import { api } from '../api'

export default function LoginPage({ onLogin }) {
  const [form,  setForm]  = useState({ login: '', mdp: '' })
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

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
                placeholder="Id membre"
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-300"
              />
            </div>

            <div>
              <label className="block text-sm text-gray-600 mb-1">Mot de passe</label>
              <input
                type="password"
                value={form.mdp}
                onChange={e => setForm(p => ({ ...p, mdp: e.target.value }))}
                required
                placeholder="••••••••"
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-300"
              />
            </div>

            {error && (
              <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
                {error}
              </p>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 hover:bg-blue-700 disabled:opacity-60 text-white font-medium py-2 rounded-lg text-sm transition-colors cursor-pointer"
            >
              {loading ? 'Connexion…' : 'Se connecter'}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}
