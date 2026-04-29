import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'

export default function Navbar() {
  const [itemCount, setItemCount] = useState(0)

  useEffect(() => {
    let cancelled = false
    const load = () =>
      api.getCart().then(c => {
        if (!cancelled) setItemCount(c.items.reduce((s, i) => s + i.quantity, 0))
      }).catch(() => {})

    load()
    const id = setInterval(load, 5000)
    return () => { cancelled = true; clearInterval(id) }
  }, [])

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">ShopMart</Link>
      <div className="navbar-links">
        <Link to="/">Products</Link>
        <Link to="/cart" className="cart-badge">
          Cart {itemCount > 0 && <span className="badge">{itemCount}</span>}
        </Link>
      </div>
    </nav>
  )
}
