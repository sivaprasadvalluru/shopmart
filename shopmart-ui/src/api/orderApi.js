import axiosInstance from './axiosInstance'

export function createOrder() {
  return axiosInstance.post('/orders').then((res) => res.data)
}

export function getOrders() {
  return axiosInstance.get('/orders').then((res) => res.data)
}

export function getOrder(id) {
  return axiosInstance.get(`/orders/${id}`).then((res) => res.data)
}
