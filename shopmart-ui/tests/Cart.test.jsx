import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Cart from '../src/pages/Cart.jsx'
import { AppProviders, setStoredAuth } from './test-utils.jsx'
import * as cartApi from '../src/api/cartApi.js'
import * as orderApi from '../src/api/orderApi.js'

vi.mock('../src/api/cartApi.js')
vi.mock('../src/api/orderApi.js')

const sampleCart = {
  items: [
    {
      id: 11,
      productId: 1,
      productName: 'Widget',
      unitPrice: 10,
      quantity: 2,
      lineTotal: 20,
    },
  ],
  subtotal: 20,
  discount: 0,
  grandTotal: 20,
}

describe('Cart page', () => {
  beforeEach(() => {
    localStorage.clear()
    setStoredAuth({ token: 'tok' })
    vi.clearAllMocks()
    cartApi.getCart.mockResolvedValue(sampleCart)
  })

  it('loads and renders cart line items and summary', async () => {
    render(
      <AppProviders>
        <Cart />
      </AppProviders>
    )
    expect(await screen.findByText('Widget')).toBeInTheDocument()
    // subtotal, line total, and grand total are all $20.00 in this fixture
    expect(screen.getAllByText('$20.00')).toHaveLength(3)
    expect(screen.getByText('$10.00')).toBeInTheDocument()
  })

  it('removes a line item when Remove is clicked', async () => {
    cartApi.removeCartItem.mockResolvedValue(undefined)
    render(
      <AppProviders>
        <Cart />
      </AppProviders>
    )
    await screen.findByText('Widget')
    await userEvent.click(screen.getByRole('button', { name: 'Remove' }))

    await waitFor(() => {
      expect(cartApi.removeCartItem).toHaveBeenCalledWith(11)
    })
    expect(cartApi.getCart).toHaveBeenCalledTimes(2)
  })

  it('places an order and navigates on success', async () => {
    orderApi.createOrder.mockResolvedValue({
      id: 99,
      status: 'PENDING',
      totalAmount: 20,
      createdAt: new Date().toISOString(),
      items: [],
    })
    render(
      <AppProviders>
        <Cart />
      </AppProviders>
    )
    await screen.findByText('Widget')
    await userEvent.click(screen.getByRole('button', { name: 'Place order' }))

    await waitFor(() => {
      expect(orderApi.createOrder).toHaveBeenCalled()
    })
  })
})
