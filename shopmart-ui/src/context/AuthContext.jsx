import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import * as authApi from '../api/authApi'

const AuthContext = createContext(null)

const TOKEN_KEY = 'shopmart_token'
const EMAIL_KEY = 'shopmart_email'
const ROLE_KEY = 'shopmart_role'

function readStoredAuth() {
  const token = localStorage.getItem(TOKEN_KEY)
  const email = localStorage.getItem(EMAIL_KEY)
  const role = localStorage.getItem(ROLE_KEY)
  if (!token) {
    return { token: null, email: null, role: null }
  }
  return { token, email, role }
}

function persistAuth({ token, email, role }) {
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(EMAIL_KEY, email)
  localStorage.setItem(ROLE_KEY, role)
}

function clearStoredAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(EMAIL_KEY)
  localStorage.removeItem(ROLE_KEY)
}

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(readStoredAuth)

  const login = useCallback(async (email, password) => {
    const data = await authApi.login(email, password)
    persistAuth(data)
    setAuth({ token: data.token, email: data.email, role: data.role })
    return data
  }, [])

  const register = useCallback(async (email, password) => {
    const data = await authApi.register(email, password)
    persistAuth(data)
    setAuth({ token: data.token, email: data.email, role: data.role })
    return data
  }, [])

  const logout = useCallback(() => {
    clearStoredAuth()
    setAuth({ token: null, email: null, role: null })
  }, [])

  const value = useMemo(
    () => ({
      token: auth.token,
      email: auth.email,
      role: auth.role,
      user: auth.token ? { email: auth.email, role: auth.role } : null,
      isAuthenticated: Boolean(auth.token),
      login,
      register,
      logout,
    }),
    [auth, login, register, logout]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return ctx
}

export default AuthContext
