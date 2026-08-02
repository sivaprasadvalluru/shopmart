import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import * as cartApi from '../api/cartApi'
import * as orderApi from '../api/orderApi'
import CartLine from '../components/CartLine'
import '../styles/Cart.css'

export default function Cart() {
  const navigate = useNavigate()
  const [cart, setCart] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [placingOrder, setPlacingOrder] = useState(false)

  const loadCart = useCallback(() => {
    setLoading(true)
    setError('')
    return cartApi
      .getCart()
      .then((data) => setCart(data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load cart.'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    loadCart()
  }, [loadCart])

  async function handleRemove(itemId) {
    setError('')
    try {
      await cartApi.removeCartItem(itemId)
      await loadCart()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to remove item.')
    }
  }

  async function handlePlaceOrder() {
    setError('')
    setPlacingOrder(true)
    try {
      await orderApi.createOrder()
      navigate('/orders')
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to place order.')
    } finally {
      setPlacingOrder(false)
    }
  }

  if (loading) {
    return (
      <div className="cart-page">
        <h1>Your Cart</h1>
        <p>Loading cart...</p>
      </div>
    )
  }

  const items = cart?.items || []

  return (
    <div className="cart-page">
      <h1>Your Cart</h1>
      {error && <p className="form-error">{error}</p>}

      {items.length === 0 ? (
        <p>Your cart is empty.</p>
      ) : (
        <>
          <table className="cart-table">
            <thead>
              <tr>
                <th>Product</th>
                <th>Unit Price</th>
                <th>Quantity</th>
                <th>Line Total</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <CartLine key={item.id} item={item} onRemove={handleRemove} />
              ))}
            </tbody>
          </table>

          <div className="cart-summary">
            <div>
              <span>Subtotal</span>
              <span>${Number(cart.subtotal).toFixed(2)}</span>
            </div>
            <div>
              <span>Discount</span>
              <span>${Number(cart.discount).toFixed(2)}</span>
            </div>
            <div className="cart-summary-total">
              <span>Grand Total</span>
              <span>${Number(cart.grandTotal).toFixed(2)}</span>
            </div>
          </div>

          <button
            type="button"
            className="btn btn-primary"
            onClick={handlePlaceOrder}
            disabled={placingOrder}
          >
            {placingOrder ? 'Placing order...' : 'Place order'}
          </button>
        </>
      )}
    </div>
  )
}
