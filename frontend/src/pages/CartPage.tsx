import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, Cart } from '../api/client'

export default function CartPage() {
  const [cart, setCart] = useState<Cart | null>(null)
  const [loading, setLoading] = useState(true)
  const [ordered, setOrdered] = useState(false)
  const [checkoutError, setCheckoutError] = useState('')

  useEffect(() => {
    api.getCart()
      .then(setCart)
      .finally(() => setLoading(false))
  }, [])

  const update = (fn: () => Promise<Cart>) =>
    fn().then(setCart).catch(console.error)

  const changeQty = (productId: number, delta: number, currentQty: number) => {
    const next = currentQty + delta
    if (next <= 0) {
      update(() => api.removeCartItem(productId))
    } else {
      update(() => api.updateCartItem(productId, next))
    }
  }

  const handleCheckout = async () => {
    setCheckoutError('')
    try {
      await api.checkout()
      setOrdered(true)
    } catch {
      setCheckoutError('Cannot place an order with an empty cart.')
    }
  }

  if (loading) return <p className="loading">Loading cart…</p>

  if (ordered) {
    return (
      <div className="cart-page">
        <div className="empty-cart">
          <p>Thank you! Your order has been placed.</p>
          <Link to="/"><button className="btn-primary">Continue Shopping</button></Link>
        </div>
      </div>
    )
  }

  return (
    <div className="cart-page">
      <h1>Your Cart</h1>

      {cart && cart.items.length === 0 ? (
        <div className="empty-cart">
          <p>Your cart is empty.</p>
          <Link to="/"><button className="btn-primary">Shop Now</button></Link>
        </div>
      ) : (
        cart?.items.map(item => (
          <div key={item.productId} className="cart-item">
            <img src={item.imageUrl} alt={item.productName} />
            <div className="cart-item-info">
              <h3>{item.productName}</h3>
              <p className="cart-item-price">${item.price.toFixed(2)} each</p>
            </div>
            <div className="qty-controls">
              <button onClick={() => changeQty(item.productId, -1, item.quantity)}>−</button>
              <span className="qty-num">{item.quantity}</span>
              <button onClick={() => changeQty(item.productId, 1, item.quantity)}>+</button>
            </div>
            <button className="btn-danger" onClick={() => update(() => api.removeCartItem(item.productId))}>
              Remove
            </button>
          </div>
        ))
      )}

      {cart && cart.items.length > 0 && (
        <div className="cart-summary">
          <div className="total-row">
            <span>Total</span>
            <span>${cart.total.toFixed(2)}</span>
          </div>
          {checkoutError && <p className="error-msg" style={{ marginBottom: '12px' }}>{checkoutError}</p>}
          <button
            className="btn-primary"
            style={{ width: '100%' }}
            onClick={handleCheckout}
            disabled={cart.items.length === 0}
          >
            Place Order
          </button>
        </div>
      )}
    </div>
  )
}
