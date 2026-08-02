import { Link } from 'react-router-dom'

export default function OrderRow({ order }) {
  const itemCount = order.items ? order.items.length : 0
  return (
    <tr className="order-row">
      <td>
        <Link to={`/orders/${order.id}`}>#{order.id}</Link>
      </td>
      <td>
        <span className={`order-status status-${order.status?.toLowerCase()}`}>{order.status}</span>
      </td>
      <td>${Number(order.totalAmount).toFixed(2)}</td>
      <td>{new Date(order.createdAt).toLocaleString()}</td>
      <td>{itemCount}</td>
    </tr>
  )
}
