export function Spinner() {
  return (
    <div className="flex justify-center items-center py-10">
      <div className="w-10 h-10 border-4 border-blue-100 border-t-blue-500 rounded-full animate-spin" />
    </div>
  )
}

export function ConfirmModal({ message, onConfirm, onCancel }) {
  if (!message) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="bg-white rounded-xl shadow-xl p-6 max-w-sm w-full mx-4">
        <p className="text-gray-800 font-medium mb-6 text-center">{message}</p>
        <div className="flex gap-3 justify-center">
          <button
            onClick={onConfirm}
            className="px-5 py-2 bg-red-600 hover:bg-red-700 text-white text-sm font-medium rounded-lg transition-colors cursor-pointer"
          >
            Oui
          </button>
          <button
            onClick={onCancel}
            className="px-5 py-2 border border-gray-300 hover:bg-gray-50 text-gray-700 text-sm font-medium rounded-lg transition-colors cursor-pointer"
          >
            Non
          </button>
        </div>
      </div>
    </div>
  )
}

export function Card({ title, children, action }) {
  return (
    <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-5 mb-5">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-base font-semibold text-gray-800">{title}</h2>
        {action && <div>{action}</div>}
      </div>
      {children}
    </div>
  )
}

export function BadgeTag({ label, color = 'gray' }) {
  const colors = {
    gray:   'bg-gray-100 text-gray-700',
    blue:   'bg-blue-100 text-blue-700',
    green:  'bg-green-100 text-green-700',
    amber:  'bg-amber-100 text-amber-700',
    purple: 'bg-purple-100 text-purple-700',
    red:    'bg-red-100 text-red-700',
  }
  return (
    <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${colors[color] || colors.gray}`}>
      {label}
    </span>
  )
}

export function Btn({ children, onClick, variant = 'primary', size = 'sm', disabled }) {
  const variants = {
    primary: 'bg-indigo-600 hover:bg-indigo-700 text-white',
    danger:  'bg-red-500 hover:bg-red-600 text-white',
    outline: 'border border-gray-300 hover:bg-gray-50 text-gray-700',
    success: 'bg-green-600 hover:bg-green-700 text-white',
  }
  const sizes = { sm: 'px-3 py-1.5 text-sm', md: 'px-4 py-2 text-sm' }
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      className={`rounded-lg font-medium transition-colors disabled:opacity-50 cursor-pointer ${variants[variant] || variants.primary} ${sizes[size] || sizes.sm}`}
    >
      {children}
    </button>
  )
}

export function Input({ label, required, optional, ...props }) {
  return (
    <div className="mb-3">
      {label && (
        <label className="block text-sm text-gray-600 mb-1">
          {label}
          {required && <span className="text-red-500 ml-0.5">*</span>}
          {optional && <span className="text-gray-400 ml-1 font-normal">(optionnel)</span>}
        </label>
      )}
      <input
        {...props}
        className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300"
      />
    </div>
  )
}

export function Alert({ type = 'error', message, onClose }) {
  if (!message) return null
  const colors = {
    error:   'bg-red-50 border-red-200 text-red-700',
    success: 'bg-green-50 border-green-200 text-green-700',
    info:    'bg-blue-50 border-blue-200 text-blue-700',
  }
  return (
    <div className={`border rounded-lg px-4 py-3 text-sm flex items-center justify-between mb-4 ${colors[type] || colors.error}`}>
      <span>{message}</span>
      {onClose && <button onClick={onClose} className="ml-3 font-bold cursor-pointer">×</button>}
    </div>
  )
}
