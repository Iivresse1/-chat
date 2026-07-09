<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { sendVerifyCode } from '@/api/user'
import { ElMessage } from 'element-plus'
import { UserFilled, Lock, Message } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  email: '',
  nickname: '',
  password: '',
  checkcode: ''
})

const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

async function doSendCode() {
  if (!form.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    ElMessage.warning('邮箱格式不正确')
    return
  }

  sending.value = true
  try {
    await sendVerifyCode(form.email)
    ElMessage.success('验证码已发送至邮箱')
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer!)
        timer = null
      }
    }, 1000)
  } catch {
    // handled by interceptor
  } finally {
    sending.value = false
  }
}

async function doRegister() {
  if (!form.email || !form.nickname || !form.password) {
    ElMessage.warning('请填写所有必填项')
    return
  }
  if (!form.checkcode) {
    ElMessage.warning('请输入验证码')
    return
  }
  if (form.nickname.length < 2 || form.nickname.length > 16) {
    ElMessage.warning('昵称长度2-16个字符')
    return
  }
  if (form.password.length < 6 || form.password.length > 16) {
    ElMessage.warning('密码长度6-16位')
    return
  }
  loading.value = true
  try {
    await userStore.register(form.email, form.nickname, form.password, form.checkcode)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

// Cleanup timer on unmount
import { onBeforeUnmount } from 'vue'
onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <h1 class="logo">αchat</h1>
        <p class="subtitle">创建你的账号</p>
      </div>
      <el-form @submit.prevent="doRegister" label-position="top">
        <el-form-item label="邮箱">
          <div class="email-row">
            <el-input
              v-model="form.email"
              placeholder="请输入邮箱"
              :prefix-icon="Message"
              size="large"
              class="email-input"
            />
            <el-button
              size="large"
              :loading="sending"
              :disabled="countdown > 0"
              @click="doSendCode"
              class="code-btn"
            >
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="验证码">
          <el-input
            v-model="form.checkcode"
            placeholder="请输入邮箱收到的6位验证码"
            :prefix-icon="Message"
            size="large"
            maxlength="6"
          />
        </el-form-item>

        <el-form-item label="昵称">
          <el-input
            v-model="form.nickname"
            placeholder="2-16个字符"
            :prefix-icon="UserFilled"
            size="large"
            maxlength="16"
          />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="6-16位，需包含字母和数字"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          :loading="loading"
          @click="doRegister"
          class="submit-btn"
          native-type="submit"
        >
          注 册
        </el-button>
      </el-form>
      <div class="auth-footer">
        已有账号？<router-link to="/login">返回登录</router-link>
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
  width: 420px;
  padding: 40px 40px 32px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.08);
}
.auth-header { text-align: center; margin-bottom: 28px; }
.logo { font-size: 32px; font-weight: 700; color: #1677ff; letter-spacing: 2px; }
.subtitle { color: #999; font-size: 13px; margin-top: 6px; }

.email-row {
  display: flex;
  gap: 8px;
}
.email-input {
  flex: 1;
}
.code-btn {
  flex-shrink: 0;
  min-width: 110px;
  font-size: 13px;
}

.submit-btn { width: 100%; margin-top: 4px; height: 44px; font-size: 15px; }
.auth-footer { text-align: center; margin-top: 18px; font-size: 13px; color: #999; }
.auth-footer a { color: #1677ff; text-decoration: none; }
</style>
