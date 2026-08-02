import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import Orders from '../src/pages/Orders.jsx'
import { AppProviders, setStoredAuth } from './test-utils.jsx'
import * as orderApi from '../src/api/orderApi.js'

vi.mock('../src/api/orderApi.js')

const sampleOrders = [
  {
    id: 5,
    status: 'DELIVERED',
    totalAmount: 42.5,
    createdAt: '2026-07-01T10:00:00Z',
    items: [{ productId: 1, productName: 'Widget', quantity: 2, priceAtPurchase: 21.25 }],
  },
]

describe('Orders page', () => {
  beforeEach(() => {
    localStorage.clear()
    setStoredAuth({ token: 'tok' })
    vi.clearAllMocks()
  })

  it('loads and renders order history', async () => {
    orderApi.getOrders.mockResolvedValue(sampleOrders)
    render(
      <AppProviders>
        <Orders />
      </AppProviders>
    )
    expect(await screen.findByText('#5')).toBeInTheDocument()
    expect(screen.getByText('DELIVERED')).toBeInTheDocument()
    expect(screen.getByText('$42.50')).toBeInTheDocument()
  })

  it('shows an empty state when there are no orders', async () => {
    orderApi.getOrders.mockResolvedValue([])
    render(
      <AppProviders>
        <Orders />
      </AppProviders>
    )
    expect(await screen.findByText('You have no orders yet.')).toBeInTheDocument()
  })

  it('shows an error message when the request fails', async () => {
    orderApi.getOrders.mockRejectedValue({
      response: { data: { message: 'Failed to load orders.' } },
    })
    render(
      <AppProviders>
        <Orders />
      </AppProviders>
    )
    expect(await screen.findByText('Failed to load orders.')).toBeInTheDocument()
  })
})
