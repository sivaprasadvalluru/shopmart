import { useEffect, useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { api, Product } from '../api/client'

export default function ProductDetail() {
  const { id } = useParams<{ id: string }>()
  const [product, setProduct] = useState<Product | null>(null)
  const [qty, setQty] = useState(1)
  const [loading, setLoading] = useState(true)
  const [added, setAdded] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    if (!id) return
    api.getProduct(Number(id))
      .then(setProduct)
      .catch(() => navigate('/'))
      .finally(() => setLoading(false))
  }, [id, navigate])

  const handleAddToCart = async () => {
    if (!product) return
    await api.addToCart(product.id, qty)
    setAdded(true)
    setTimeout(() => setAdded(false), 2000)
  }

  if (loading) return <p className="loading">Loading…</p>
  if (!product) return null

  return (
    <div className="detail-page">
      <div className="detail-image">
        <img src={product.imageUrl} alt={product.name} />
      </div>
      <div className="detail-info">
        <Link to="/" className="back-link">← Back to products</Link>
        <p className="detail-category">{product.category.name}</p>
        <h1>{product.name}</h1>
        {product.shortDescription && (
          <p className="detail-short-description">{product.shortDescription}</p>
        )}
        <p className="detail-price">${product.price.toFixed(2)}</p>
        <p className="detail-description">{product.description}</p>
        <div className="qty-row">
          <label htmlFor="qty">Qty:</label>
          <input
            id="qty"
            type="number"
            min={1}
            value={qty}
            onChange={e => setQty(Number(e.target.value))}
          />
        </div>
        <button className="btn-primary" onClick={handleAddToCart}>
          {added ? 'Added!' : 'Add to Cart'}
        </button>
        <button className="btn-outline" onClick={() => navigate('/cart')}>
          View Cart
        </button>
      </div>
    </div>
  )
}
