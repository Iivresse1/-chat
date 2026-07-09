import request from './request'

export const login = (email: string, password: string) =>
  request.post('/user/login', { email, password })

export const register = (email: string, nickname: string, password: string, checkcode: string) =>
  request.post('/user/register', { email, nickname, password, checkcode })

export const getProfile = () => request.get('/user/profile')

export const updateProfile = (data: Record<string, any>) => request.put('/user/profile', data)

export const updateStatus = (status: string) => request.put('/user/status', { status })

export const uploadAvatar = (file: File) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/user/avatar/upload', fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const searchUsers = (keyword: string) => request.get('/user/search', { params: { keyword } })

export const sendVerifyCode = (email: string) =>
  request.post('/verify-code/send', { email })
