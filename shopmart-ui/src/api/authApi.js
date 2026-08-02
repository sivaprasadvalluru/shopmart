import axiosInstance from './axiosInstance'

export function login(email, password) {
  return axiosInstance.post('/auth/login', { email, password }).then((res) => res.data)
}

export function register(email, password) {
  return axiosInstance.post('/auth/register', { email, password }).then((res) => res.data)
}
