import axiosInstance from './axiosInstance'

export function getCart() {
  return axiosInstance.get('/cart').then((res) => res.data)
}

export function addCartItem(productId, quantity = 1) {
  return axiosInstance.post('/cart/items', { productId, quantity }).then((res) => res.data)
}

export function removeCartItem(id) {
  return axiosInstance.delete(`/cart/items/${id}`).then((res) => res.data)
}
