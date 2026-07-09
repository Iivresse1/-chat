import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import * as userApi from '@/api/user'

export interface UserInfo {
  id: number
  nickName: string
  avatar: string
  sign: string
  gender: number | null
  email: string
  phone: string
  birthday: string
  onlineStatus: string
  status: number
}

export const useUserStore = defineStore('user', () => {
  const user = ref<UserInfo | null>(null)
  const token = ref(localStorage.getItem('token') || '')

  const isLoggedIn = computed(() => !!token.value)
  const userId = computed(() => user.value?.id || 0)
  const avatar = computed(() => user.value?.avatar || '')
  const nickName = computed(() => user.value?.nickName || '用户')

  async function login(email: string, password: string) {
    const res = await userApi.login(email, password)
    token.value = res.data as string
    localStorage.setItem('token', res.data as string)
    return res
  }

  async function register(email: string, nickname: string, password: string, checkcode: string) {
    return userApi.register(email, nickname, password, checkcode)
  }

  async function fetchProfile() {
    const res = await userApi.getProfile()
    user.value = res.data as UserInfo
    return res
  }

  async function setStatus(status: string) {
    await userApi.updateStatus(status)
    if (user.value) user.value.onlineStatus = status
  }

  async function updateProfileData(data: Record<string, any>) {
    await userApi.updateProfile(data)
    await fetchProfile()
  }

  async function uploadAvatarFile(file: File) {
    const res = await userApi.uploadAvatar(file)
    await fetchProfile()
    return res
  }

  function logout() {
    user.value = null
    token.value = ''
    localStorage.removeItem('token')
  }

  return {
    user, token, isLoggedIn, userId, avatar, nickName,
    login, register, fetchProfile, setStatus, updateProfileData, uploadAvatarFile, logout
  }
})
