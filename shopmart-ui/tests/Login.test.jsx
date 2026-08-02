import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Login from '../src/pages/Login.jsx'
import { AppProviders } from './test-utils.jsx'
import * as authApi from '../src/api/authApi.js'

vi.mock('../src/api/authApi.js')

describe('Login page', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('renders email and password fields', () => {
    render(
      <AppProviders>
        <Login />
      </AppProviders>
    )
    expect(screen.getByLabelText('Email')).toBeInTheDocument()
    expect(screen.getByLabelText('Password')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Login' })).toBeInTheDocument()
  })

  it('calls authApi.login with form values on submit', async () => {
    authApi.login.mockResolvedValue({ token: 'tok', email: 'a@b.com', role: 'CUSTOMER' })
    render(
      <AppProviders>
        <Login />
      </AppProviders>
    )
    await userEvent.type(screen.getByLabelText('Email'), 'a@b.com')
    await userEvent.type(screen.getByLabelText('Password'), 'password1')
    await userEvent.click(screen.getByRole('button', { name: 'Login' }))

    expect(authApi.login).toHaveBeenCalledWith('a@b.com', 'password1')
  })

  it('shows the API error message on failed login', async () => {
    authApi.login.mockRejectedValue({
      response: { data: { message: 'Invalid credentials' } },
    })
    render(
      <AppProviders>
        <Login />
      </AppProviders>
    )
    await userEvent.type(screen.getByLabelText('Email'), 'a@b.com')
    await userEvent.type(screen.getByLabelText('Password'), 'wrongpass')
    await userEvent.click(screen.getByRole('button', { name: 'Login' }))

    expect(await screen.findByText('Invalid credentials')).toBeInTheDocument()
  })
})
