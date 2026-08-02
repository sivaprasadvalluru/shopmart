import { useEffect, useState } from 'react'
import * as productApi from '../api/productApi'
import * as cartApi from '../api/cartApi'
import ProductCard from '../components/ProductCard'
import '../styles/Products.css'

const CATEGORIES = ['All', 'Electronics', 'Clothing', 'Books']
const PAGE_SIZE = 10

export default function Products() {
  const [products, setProducts] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [category, setCategory] = useState('All')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError('')
    productApi
      .getProducts({ page, size: PAGE_SIZE, category: category === 'All' ? '' : category })
      .then((data) => {
        if (cancelled) return
        setProducts(data.content || [])
        setTotalPages(data.totalPages ?? 0)
      })
      .catch((err) => {
        if (cancelled) return
        setError(err.response?.data?.message || 'Failed to load products.')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [page, category])

  function handleCategoryChange(nextCategory) {
    setCategory(nextCategory)
    setPage(0)
  }

  async function handleAddToCart(productId, quantity) {
    setNotice('')
    try {
      await cartApi.addCartItem(productId, quantity)
      setNotice('Item added to cart.')
      setTimeout(() => setNotice(''), 2000)
    } catch (err) {
      throw err
    }
  }

  return (
    <div className="products-page">
      <div className="products-header">
        <h1>Products</h1>
        <div className="category-filter">
          {CATEGORIES.map((c) => (
            <button
              key={c}
              type="button"
              className={c === category ? 'chip chip-active' : 'chip'}
              onClick={() => handleCategoryChange(c)}
            >
              {c}
            </button>
          ))}
        </div>
      </div>

      {notice && <p className="notice">{notice}</p>}
      {error && <p className="form-error">{error}</p>}

      {loading ? (
        <p>Loading products...</p>
      ) : products.length === 0 ? (
        <p>No products found.</p>
      ) : (
        <div className="product-grid">
          {products.map((product) => (
            <ProductCard key={product.id} product={product} onAddToCart={handleAddToCart} />
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="pagination">
          <button
            type="button"
            className="btn btn-secondary"
            disabled={page <= 0}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
          >
            Previous
          </button>
          <span>
            Page {page + 1} of {totalPages}
          </span>
          <button
            type="button"
            className="btn btn-secondary"
            disabled={page >= totalPages - 1}
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
          >
            Next
          </button>
        </div>
      )}
    </div>
  )
}
