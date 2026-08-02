import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import '../styles/Products.css'

export default function ProductCard({ product, onAddToCart }) {
  const { isAuthenticated } = useAuth()
  const [quantity, setQuantity] = useState(1)
  const [status, setStatus] = useState('idle') // idle | adding | added | error

  const outOfStock = product.stockQuantity <= 0

  async function handleAddToCart() {
    setStatus('adding')
    try {
      await onAddToCart(product.id, quantity)
      setStatus('added')
      setTimeout(() => setStatus('idle'), 1500)
    } catch (err) {
      setStatus('error')
    }
  }

  return (
    <div className="product-card">
      <div className="product-card-body">
        <h3 className="product-name">{product.name}</h3>
        <p className="product-description">{product.description}</p>
        <div className="product-meta">
          <span className="product-category">{product.category}</span>
          <span className={outOfStock ? 'product-stock out' : 'product-stock'}>
            {outOfStock ? 'Out of stock' : `${product.stockQuantity} in stock`}
          </span>
        </div>
        <div className="product-price">${Number(product.price).toFixed(2)}</div>
      </div>
      <div className="product-card-footer">
        {isAuthenticated ? (
          <>
            <input
              type="number"
              min="1"
              max={product.stockQuantity || 1}
              value={quantity}
              onChange={(e) => setQuantity(Math.max(1, Number(e.target.value) || 1))}
              className="qty-input"
              aria-label={`Quantity for ${product.name}`}
              disabled={outOfStock}
            />
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleAddToCart}
              disabled={outOfStock || status === 'adding'}
            >
              {status === 'added' ? 'Added!' : 'Add to cart'}
            </button>
          </>
        ) : (
          <Link to="/login" className="btn btn-secondary">
            Login to add to cart
          </Link>
        )}
      </div>
      {status === 'error' && <p className="form-error">Could not add to cart. Try again.</p>}
    </div>
  )
}
