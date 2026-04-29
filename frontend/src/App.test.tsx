import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import App from './App'
import { api } from './api/client'

vi.mock('./api/client', () => ({
  api: {
    getCategories: vi.fn(),
    getProducts: vi.fn(),
    getProduct: vi.fn(),
    getCart: vi.fn(),
    addToCart: vi.fn(),
    updateCartItem: vi.fn(),
    removeCartItem: vi.fn(),
    checkout: vi.fn(),
  },
}))

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const mockApi = api as unknown as Record<string, ReturnType<typeof vi.fn<any[], any>>>

beforeEach(() => {
  mockApi.getCategories.mockResolvedValue([
    { id: 1, name: 'Electronics', description: 'Devices' },
  ])
  mockApi.getProducts.mockResolvedValue([
    { id: 1, name: 'Test Headphones', description: 'Great', price: 99.99, imageUrl: '', category: { id: 1, name: 'Electronics', description: '' } },
  ])
  mockApi.getCart.mockResolvedValue({ sessionId: 'x', items: [], total: 0 })
})

describe('App routing', () => {
  it('renders product list on /', async () => {
    render(
      <MemoryRouter initialEntries={['/']}>
        <App />
      </MemoryRouter>
    )
    expect(await screen.findByText('Test Headphones')).toBeInTheDocument()
  })

  it('renders cart page on /cart', async () => {
    render(
      <MemoryRouter initialEntries={['/cart']}>
        <App />
      </MemoryRouter>
    )
    expect(await screen.findByText('Your Cart')).toBeInTheDocument()
  })

  it('hides Place Order button when cart is empty', async () => {
    render(
      <MemoryRouter initialEntries={['/cart']}>
        <App />
      </MemoryRouter>
    )
    expect(await screen.findByText('Your Cart')).toBeInTheDocument()
    expect(screen.queryByText('Place Order')).not.toBeInTheDocument()
  })

  it('shows Place Order button when cart has items', async () => {
    mockApi.getCart.mockResolvedValue({
      sessionId: 'x',
      items: [{ productId: 1, productName: 'Headphones', price: 99.99, quantity: 1, imageUrl: '' }],
      total: 99.99,
    })
    render(
      <MemoryRouter initialEntries={['/cart']}>
        <App />
      </MemoryRouter>
    )
    expect(await screen.findByText('Place Order')).toBeInTheDocument()
  })
})
