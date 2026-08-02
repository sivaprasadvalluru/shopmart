import { describe, it, expect, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Navbar from '../src/components/Navbar.jsx'
import { AppProviders, setStoredAuth } from './test-utils.jsx'

describe('Navbar', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('shows brand name and nav links', () => {
    render(
      <AppProviders>
        <Navbar />
      </AppProviders>
    )
    expect(screen.getByText('ShopMart')).toBeInTheDocument()
    expect(screen.getByText('Products')).toBeInTheDocument()
    expect(screen.getByText('Cart')).toBeInTheDocument()
    expect(screen.getByText('Orders')).toBeInTheDocument()
  })

  it('shows Login/Register links when logged out', () => {
    render(
      <AppProviders>
        <Navbar />
      </AppProviders>
    )
    expect(screen.getByText('Login')).toBeInTheDocument()
    expect(screen.getByText('Register')).toBeInTheDocument()
  })

  it('shows user email and Logout button when logged in', () => {
    setStoredAuth({ token: 'abc123', email: 'jane@example.com', role: 'CUSTOMER' })
    render(
      <AppProviders>
        <Navbar />
      </AppProviders>
    )
    expect(screen.getByText('jane@example.com')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Logout' })).toBeInTheDocument()
    expect(screen.queryByText('Login')).not.toBeInTheDocument()
  })

  it('clears auth and navigates to /login on logout click', async () => {
    setStoredAuth({ token: 'abc123', email: 'jane@example.com', role: 'CUSTOMER' })
    render(
      <AppProviders>
        <Navbar />
      </AppProviders>
    )
    await userEvent.click(screen.getByRole('button', { name: 'Logout' }))
    expect(localStorage.getItem('shopmart_token')).toBeNull()
    expect(screen.getByText('Login')).toBeInTheDocument()
  })
})
