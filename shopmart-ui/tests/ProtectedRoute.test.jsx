import { describe, it, expect, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { Route, Routes } from 'react-router-dom'
import ProtectedRoute from '../src/components/ProtectedRoute.jsx'
import { AppProviders, setStoredAuth } from './test-utils.jsx'

function Secret() {
  return <div>Secret content</div>
}

function LoginStub() {
  return <div>Login page</div>
}

function renderProtected(initialEntries) {
  return render(
    <AppProviders initialEntries={initialEntries}>
      <Routes>
        <Route path="/login" element={<LoginStub />} />
        <Route
          path="/secret"
          element={
            <ProtectedRoute>
              <Secret />
            </ProtectedRoute>
          }
        />
      </Routes>
    </AppProviders>
  )
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('redirects to /login when there is no auth token', () => {
    renderProtected(['/secret'])
    expect(screen.getByText('Login page')).toBeInTheDocument()
    expect(screen.queryByText('Secret content')).not.toBeInTheDocument()
  })

  it('renders children when authenticated', () => {
    setStoredAuth({ token: 'abc123' })
    renderProtected(['/secret'])
    expect(screen.getByText('Secret content')).toBeInTheDocument()
  })
})
