import axiosInstance from './axiosInstance'

export function getProducts({ page = 0, size = 10, category = '' } = {}) {
  const params = { page, size }
  if (category) {
    params.category = category
  }
  return axiosInstance.get('/products', { params }).then((res) => res.data)
}

export function getProduct(id) {
  return axiosInstance.get(`/products/${id}`).then((res) => res.data)
}
