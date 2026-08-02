import axios from 'axios'

const BASE_URL = 'http://localhost:8080/api'

const axiosInstance = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Attach the JWT (if any) to every outgoing request.
axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('shopmart_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default axiosInstance
