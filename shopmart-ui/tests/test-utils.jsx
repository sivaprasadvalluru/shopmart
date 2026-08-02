import { MemoryRouter } from 'react-router-dom'
import { AuthProvider } from '../src/context/AuthContext.jsx'

export function setStoredAuth({ token, email, role } = {}) {
  if (token) {
    localStorage.setItem('shopmart_token', token)
    localStorage.setItem('shopmart_email', email || 'user@example.com')
    localStorage.setItem('shopmart_role', role || 'CUSTOMER')
  } else {
    localStorage.removeItem('shopmart_token')
    localStorage.removeItem('shopmart_email')
    localStorage.removeItem('shopmart_role')
  }
}

export function AppProviders({ children, initialEntries = ['/'] }) {
  return (
    <MemoryRouter initialEntries={initialEntries}>
      <AuthProvider>{children}</AuthProvider>
    </MemoryRouter>
  )
}
