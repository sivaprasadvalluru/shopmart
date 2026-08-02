import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import '../styles/Auth.css'

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setFieldErrors(null)
    setSubmitting(true)
    try {
      await register(email, password)
      navigate('/products', { replace: true })
    } catch (err) {
      const data = err.response?.data
      if (data?.fieldErrors) {
        setFieldErrors(data.fieldErrors)
      } else {
        setError(data?.message || 'Registration failed. Please try again.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-form" onSubmit={handleSubmit}>
        <h1>Register</h1>
        {error && <p className="form-error">{error}</p>}
        <label htmlFor="email">Email</label>
        <input
          id="email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        {fieldErrors?.email && <p className="field-error">{fieldErrors.email}</p>}
        <label htmlFor="password">Password</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        {fieldErrors?.password && <p className="field-error">{fieldErrors.password}</p>}
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Registering...' : 'Register'}
        </button>
        <p className="auth-switch">
          Already have an account? <Link to="/login">Login</Link>
        </p>
      </form>
    </div>
  )
}
