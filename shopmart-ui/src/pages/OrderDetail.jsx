import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import * as orderApi from '../api/orderApi'
import '../styles/Orders.css'

export default function OrderDetail() {
  const { id } = useParams()
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError('')
    orderApi
      .getOrder(id)
      .then((data) => {
        if (!cancelled) setOrder(data)
      })
      .catch((err) => {
        if (cancelled) return
        if (err.response?.status === 404) {
          setError('Order not found.')
        } else if (err.response?.status === 403) {
          setError('You do not have access to this order.')
        } else {
          setError(err.response?.data?.message || 'Failed to load order.')
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [id])

  return (
    <div className="orders-page">
      <p>
        <Link to="/orders">&larr; Back to orders</Link>
      </p>
      <h1>Order #{id}</h1>
      {loading && <p>Loading order...</p>}
      {error && <p className="form-error">{error}</p>}
      {order && (
        <>
          <div className="order-detail-meta">
            <div>
              <span>Status</span>
              <span className={`order-status status-${order.status?.toLowerCase()}`}>
                {order.status}
              </span>
            </div>
            <div>
              <span>Total</span>
              <span>${Number(order.totalAmount).toFixed(2)}</span>
            </div>
            <div>
              <span>Placed</span>
              <span>{new Date(order.createdAt).toLocaleString()}</span>
            </div>
          </div>

          <table className="orders-table">
            <thead>
              <tr>
                <th>Product</th>
                <th>Quantity</th>
                <th>Price at Purchase</th>
              </tr>
            </thead>
            <tbody>
              {(order.items || []).map((item) => (
                <tr key={item.productId}>
                  <td>{item.productName}</td>
                  <td>{item.quantity}</td>
                  <td>${Number(item.priceAtPurchase).toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </div>
  )
}
