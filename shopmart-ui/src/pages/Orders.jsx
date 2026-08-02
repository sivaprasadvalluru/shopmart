import { useEffect, useState } from 'react'
import * as orderApi from '../api/orderApi'
import OrderRow from '../components/OrderRow'
import '../styles/Orders.css'

export default function Orders() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    orderApi
      .getOrders()
      .then((data) => {
        if (!cancelled) setOrders(data || [])
      })
      .catch((err) => {
        if (!cancelled) setError(err.response?.data?.message || 'Failed to load orders.')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  if (loading) {
    return (
      <div className="orders-page">
        <h1>Order History</h1>
        <p>Loading orders...</p>
      </div>
    )
  }

  return (
    <div className="orders-page">
      <h1>Order History</h1>
      {error && <p className="form-error">{error}</p>}
      {orders.length === 0 ? (
        <p>You have no orders yet.</p>
      ) : (
        <table className="orders-table">
          <thead>
            <tr>
              <th>Order</th>
              <th>Status</th>
              <th>Total</th>
              <th>Placed</th>
              <th>Items</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => (
              <OrderRow key={order.id} order={order} />
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
