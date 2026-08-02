import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Products from '../src/pages/Products.jsx'
import { AppProviders, setStoredAuth } from './test-utils.jsx'
import * as productApi from '../src/api/productApi.js'
import * as cartApi from '../src/api/cartApi.js'

vi.mock('../src/api/productApi.js')
vi.mock('../src/api/cartApi.js')

const samplePage = {
  content: [
    {
      id: 1,
      name: 'Widget',
      description: 'A fine widget',
      price: 9.99,
      stockQuantity: 5,
      category: 'Electronics',
      active: true,
    },
  ],
  totalElements: 1,
  totalPages: 1,
  number: 0,
  size: 10,
}

describe('Products page', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
    productApi.getProducts.mockResolvedValue(samplePage)
  })

  it('fetches and renders products', async () => {
    render(
      <AppProviders>
        <Products />
      </AppProviders>
    )
    expect(await screen.findByText('Widget')).toBeInTheDocument()
    expect(productApi.getProducts).toHaveBeenCalledWith({ page: 0, size: 10, category: '' })
  })

  it('refetches with category param when filter changes', async () => {
    render(
      <AppProviders>
        <Products />
      </AppProviders>
    )
    await screen.findByText('Widget')
    await userEvent.click(screen.getByRole('button', { name: 'Electronics' }))

    await waitFor(() => {
      expect(productApi.getProducts).toHaveBeenCalledWith({
        page: 0,
        size: 10,
        category: 'Electronics',
      })
    })
  })

  it('shows a login link instead of add-to-cart when logged out', async () => {
    render(
      <AppProviders>
        <Products />
      </AppProviders>
    )
    await screen.findByText('Widget')
    expect(screen.getByText('Login to add to cart')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Add to cart' })).not.toBeInTheDocument()
  })

  it('calls cartApi.addCartItem when logged-in user adds to cart', async () => {
    setStoredAuth({ token: 'tok' })
    cartApi.addCartItem.mockResolvedValue({
      id: 1,
      productId: 1,
      productName: 'Widget',
      unitPrice: 9.99,
      quantity: 1,
      lineTotal: 9.99,
    })
    render(
      <AppProviders>
        <Products />
      </AppProviders>
    )
    await screen.findByText('Widget')
    await userEvent.click(screen.getByRole('button', { name: 'Add to cart' }))

    expect(cartApi.addCartItem).toHaveBeenCalledWith(1, 1)
  })
})
