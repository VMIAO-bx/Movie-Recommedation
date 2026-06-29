import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  // 开发环境默认 localhost，生产环境通过 Netlify 环境变量指向 Railway
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000
})

// 请求拦截器：自动添加 token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：统一处理响应
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      // 401/403 → 清除过期 token，静默处理（由业务层 catch 决定是否提示）
      if (res.code === 401 || res.code === 403) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        // 不在拦截器弹窗，让业务层按需处理
        return Promise.reject(new Error(res.message || '未登录'))
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    // HTTP 错误（网络问题等）
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      ElMessage.warning('登录已过期，请重新登录')
      return Promise.reject(error)
    }
    const msg = error.response?.data?.message || error.message || '网络错误'
    if (error.code !== 'ERR_CANCELED') {
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

export default request
