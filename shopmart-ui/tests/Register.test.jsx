import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Register from '../src/pages/Register.jsx'
import { AppProviders } from './test-utils.jsx'
import * as authApi from '../src/api/authApi.js'

vi.mock('../src/api/authApi.js')

describe('Register page', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('calls authApi.register with form values on submit', async () => {
    authApi.register.mockResolvedValue({ token: 'tok', email: 'new@user.com', role: 'CUSTOMER' })
    render(
      <AppProviders>
        <Register />
      </AppProviders>
    )
    await userEvent.type(screen.getByLabelText('Email'), 'new@user.com')
    await userEvent.type(screen.getByLabelText('Password'), 'password1')
    await userEvent.click(screen.getByRole('button', { name: 'Register' }))

    expect(authApi.register).toHaveBeenCalledWith('new@user.com', 'password1')
  })

  it('shows field-level validation errors when present', async () => {
    authApi.register.mockRejectedValue({
      response: {
        data: {
          message: 'Validation failed',
          fieldErrors: { email: 'Email is invalid', password: 'Password too short' },
        },
      },
    })
    render(
      <AppProviders>
        <Register />
      </AppProviders>
    )
    await userEvent.type(screen.getByLabelText('Email'), 'bad@example.com')
    await userEvent.type(screen.getByLabelText('Password'), '123')
    await userEvent.click(screen.getByRole('button', { name: 'Register' }))

    expect(await screen.findByText('Email is invalid')).toBeInTheDocument()
    expect(screen.getByText('Password too short')).toBeInTheDocument()
  })

  it('shows top-level message when no fieldErrors present', async () => {
    authApi.register.mockRejectedValue({
      response: { data: { message: 'Email already registered' } },
    })
    render(
      <AppProviders>
        <Register />
      </AppProviders>
    )
    await userEvent.type(screen.getByLabelText('Email'), 'dup@user.com')
    await userEvent.type(screen.getByLabelText('Password'), 'password1')
    await userEvent.click(screen.getByRole('button', { name: 'Register' }))

    expect(await screen.findByText('Email already registered')).toBeInTheDocument()
  })
})
