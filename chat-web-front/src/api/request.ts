import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

request.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && body.code && body.code !== 200) {
      ElMessage.error(body.msg || '请求失败')
      return Promise.reject(new Error(body.msg || '请求失败'))
    }
    return body
  },
  (err) => {
    if (err.response) {
      const status = err.response.status
      if (status === 401) {
        localStorage.removeItem('token')
        router.push('/login')
        ElMessage.error('请先登录')
      } else {
        ElMessage.error(err.response.data?.msg || '请求失败')
      }
    } else {
      ElMessage.error('网络异常')
    }
    return Promise.reject(err)
  }
)

export default request
