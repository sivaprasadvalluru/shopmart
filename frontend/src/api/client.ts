const BASE_URL = 'http://localhost:8081/api'

function getSessionId(): string {
  let id = localStorage.getItem('shopmart-session-id')
  if (!id) {
    id = crypto.randomUUID()
    localStorage.setItem('shopmart-session-id', id)
  }
  return id
}

const sessionId = getSessionId()

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'X-Session-Id': sessionId,
      ...(options.headers ?? {}),
    },
  })
  if (!res.ok) throw new Error(`HTTP ${res.status} ${res.statusText}`)
  return res.json() as Promise<T>
}

export interface Category {
  id: number
  name: string
  description: string
}

export interface Product {
  id: number
  name: string
  description: string
  price: number
  imageUrl: string
  category: Category
}

export interface CartItem {
  productId: number
  productName: string
  price: number
  quantity: number
  imageUrl: string
}

export interface Cart {
  sessionId: string
  items: CartItem[]
  total: number
}

export const api = {
  getCategories: () => request<Category[]>('/categories'),

  getProducts: (categoryId?: number) =>
    request<Product[]>(categoryId != null ? `/products?categoryId=${categoryId}` : '/products'),

  getProduct: (id: number) => request<Product>(`/products/${id}`),

  getCart: () => request<Cart>('/cart'),

  addToCart: (productId: number, quantity: number) =>
    request<Cart>('/cart/items', {
      method: 'POST',
      body: JSON.stringify({ productId, quantity }),
    }),

  updateCartItem: (productId: number, quantity: number) =>
    request<Cart>(`/cart/items/${productId}`, {
      method: 'PUT',
      body: JSON.stringify({ quantity }),
    }),

  removeCartItem: (productId: number) =>
    request<Cart>(`/cart/items/${productId}`, { method: 'DELETE' }),
}
