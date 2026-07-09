<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { UserFilled, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({ email: '', password: '' })
const loading = ref(false)

async function doLogin() {
  if (!form.email || !form.password) {
    ElMessage.warning('请输入邮箱和密码')
    return
  }
  loading.value = true
  try {
    await userStore.login(form.email, form.password)
    ElMessage.success('登录成功')
    router.push('/chat')
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <h1 class="logo">αchat</h1>
        <p class="subtitle">实时聊天，始终连接</p>
      </div>
      <el-form @submit.prevent="doLogin" label-position="top">
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" :prefix-icon="UserFilled" size="large" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="doLogin" class="submit-btn" native-type="submit">
          登 录
        </el-button>
      </el-form>
      <div class="auth-footer">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f0fe 0%, #f0e8fe 100%);
}
.auth-card {
  width: 400px;
  padding: 48px 40px 36px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.08);
}
.auth-header { text-align: center; margin-bottom: 36px; }
.logo { font-size: 32px; font-weight: 700; color: #1677ff; letter-spacing: 2px; }
.subtitle { color: #999; font-size: 13px; margin-top: 6px; }
.submit-btn { width: 100%; margin-top: 8px; height: 44px; font-size: 15px; }
.auth-footer { text-align: center; margin-top: 20px; font-size: 13px; color: #999; }
.auth-footer a { color: #1677ff; text-decoration: none; }
</style>
