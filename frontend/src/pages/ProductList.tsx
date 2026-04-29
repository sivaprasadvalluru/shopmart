import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, Category, Product } from '../api/client'

export default function ProductList() {
  const [categories, setCategories] = useState<Category[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [selectedCategoryId, setSelectedCategoryId] = useState<number | undefined>(undefined)
  const [searchInput, setSearchInput] = useState('')
  const [searchQuery, setSearchQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    api.getCategories().then(setCategories).catch(() => {})
  }, [])

  // Debounce: only fire the API call 300 ms after the user stops typing
  useEffect(() => {
    const timer = setTimeout(() => setSearchQuery(searchInput), 300)
    return () => clearTimeout(timer)
  }, [searchInput])

  useEffect(() => {
    setLoading(true)
    api.getProducts(selectedCategoryId, searchQuery)
      .then(setProducts)
      .catch(() => setError('Failed to load products.'))
      .finally(() => setLoading(false))
  }, [selectedCategoryId, searchQuery])

  const handleAddToCart = async (e: React.MouseEvent, productId: number) => {
    e.stopPropagation()
    await api.addToCart(productId, 1)
  }

  if (error) return <p className="error-msg">{error}</p>

  return (
    <div className="container">
      <div className="page-header">
        <h1>Products</h1>
        <input
          className="search-box"
          type="search"
          placeholder="Search products…"
          value={searchInput}
          onChange={e => setSearchInput(e.target.value)}
          aria-label="Search products"
        />
      </div>

      <div className="filter-bar">
        <button
          className={`filter-btn${selectedCategoryId == null ? ' active' : ''}`}
          onClick={() => setSelectedCategoryId(undefined)}
        >
          All
        </button>
        {categories.map(c => (
          <button
            key={c.id}
            className={`filter-btn${selectedCategoryId === c.id ? ' active' : ''}`}
            onClick={() => setSelectedCategoryId(c.id)}
          >
            {c.name}
          </button>
        ))}
      </div>

      {loading ? (
        <p className="loading">Loading…</p>
      ) : products.length === 0 ? (
        <p className="error-msg" style={{ color: '#6b7280' }}>No products found.</p>
      ) : (
        <div className="product-grid">
          {products.map(p => (
            <div key={p.id} className="product-card" onClick={() => navigate(`/products/${p.id}`)}>
              <img src={p.imageUrl} alt={p.name} loading="lazy" />
              <div className="card-body">
                <p className="card-category">{p.category.name}</p>
                <p className="card-title">{p.name}</p>
                <p className="card-price">${p.price.toFixed(2)}</p>
                <button className="btn-primary" onClick={e => handleAddToCart(e, p.id)}>
                  Add to Cart
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
