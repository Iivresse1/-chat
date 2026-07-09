<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import { UserFilled, ChatDotRound, User, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const chatStore = useChatStore()

const showProfile = ref(false)
const showStatus = ref(false)
const showProfileDialog = ref(false)
const collapsed = ref(false)

const statusOptions = ['在线', '忙碌', '离开', '隐身']
const statusMap: Record<string, string> = { '在线': 'online', '忙碌': 'busy', '离开': 'away', '隐身': 'invisible' }
const statusLabels: Record<string, string> = { online: '在线', busy: '忙碌', away: '离开', invisible: '隐身' }

const currentStatus = ref('在线')
const avatarInput = ref<HTMLInputElement | null>(null)

const profileForm = ref({
  nickName: '',
  sign: '',
  gender: null as number | null,
  birthYear: '',
  birthMonth: '',
  birthDay: ''
})

const currentYear = new Date().getFullYear()

const navTabs = [
  { path: '/chat', label: '消息', icon: ChatDotRound },
  { path: '/contacts', label: '好友', icon: User }
]

const isActive = (path: string) => {
  if (path === '/chat') return route.path.startsWith('/chat') || route.path === '/chat'
  return route.path.startsWith(path)
}

function goTab(path: string) {
  router.push(path)
}

async function init() {
  try {
    await userStore.fetchProfile()
    const u = userStore.user
    if (u?.onlineStatus && statusLabels[u.onlineStatus]) {
      currentStatus.value = statusLabels[u.onlineStatus]
    }
    // connect socket after profile loaded
    chatStore.connectSocket()
    chatStore.loadFromStorage()
  } catch {
    // ignore
  }
}

async function selectStatus(s: string) {
  currentStatus.value = s
  showStatus.value = false
  try {
    await userStore.setStatus(statusMap[s])
    chatStore.socket?.emit('status-change', { status: statusMap[s] })
  } catch { /* ignore */ }
}

function openProfileDialog() {
  showProfile.value = false
  const u = userStore.user
  if (u) {
    profileForm.value = {
      nickName: u.nickName || '',
      sign: u.sign || '',
      gender: u.gender ?? null,
      birthYear: '',
      birthMonth: '',
      birthDay: ''
    }
    if (u.birthday && /^\d{4}-\d{2}-\d{2}$/.test(u.birthday)) {
      const [y, m, d] = u.birthday.split('-')
      profileForm.value.birthYear = y
      profileForm.value.birthMonth = m
      profileForm.value.birthDay = d
    }
  }
  showProfileDialog.value = true
}

async function saveProfile() {
  const data: Record<string, any> = {
    nickName: profileForm.value.nickName,
    sign: profileForm.value.sign,
    gender: profileForm.value.gender
  }
  const { birthYear, birthMonth, birthDay } = profileForm.value
  if (birthYear && birthMonth && birthDay) {
    data.birthday = `${birthYear}-${String(birthMonth).padStart(2, '0')}-${String(birthDay).padStart(2, '0')}`
  }
  try {
    await userStore.updateProfileData(data)
    ElMessage.success('个人资料已更新')
    showProfileDialog.value = false
  } catch {
    ElMessage.error('保存失败')
  }
}

async function onAvatarFileChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  try {
    await userStore.uploadAvatarFile(file)
    ElMessage.success('头像已更新')
  } catch {
    ElMessage.error('头像上传失败')
  }
}

function handleLogout() {
  chatStore.disconnectSocket()
  userStore.logout()
  router.push('/login')
}

function statusDotClass(s: string) {
  return {
    'status-dot': true,
    online: s === '在线',
    busy: s === '忙碌',
    away: s === '离开',
    invisible: s === '隐身'
  }
}

// Close popovers on outside click
function onDocClick(e: MouseEvent) {
  const target = e.target as HTMLElement
  if (!target.closest('.sidebar-avatar')) {
    showProfile.value = false
    showStatus.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', onDocClick)
  init()

  const mq = window.matchMedia('(max-width: 860px)')
  collapsed.value = mq.matches
  mq.addEventListener('change', (e) => { collapsed.value = e.matches })
})

const searchKeyword = ref('')
</script>

<template>
  <div class="main-root">
    <!-- Sidebar -->
    <aside class="sidebar" :class="{ collapsed }">
      <div class="sidebar-avatar">
        <el-avatar
          :size="collapsed ? 36 : 44"
          :src="userStore.user?.avatar || ''"
          :icon="UserFilled"
          @click.stop="showProfile = !showProfile"
          style="cursor: pointer; flex-shrink: 0;"
        />
        <div v-if="!collapsed" class="avatar-info">
          <span class="nick">{{ userStore.nickName }}</span>
          <div class="status-box" @click.stop="showStatus = !showStatus">
            <span :class="statusDotClass(currentStatus)" />
            <span class="status-text">{{ currentStatus }}</span>
            <div v-if="showStatus" class="dropdown">
              <div v-for="s in statusOptions" :key="s"
                   :class="['dropdown-item', { active: currentStatus === s }]"
                   @click="selectStatus(s)">{{ s }}</div>
            </div>
          </div>
        </div>

        <div v-if="showProfile" class="dropdown" style="top: 68px; left: 16px;">
          <div class="dropdown-item" @click="openProfileDialog">个人资料</div>
          <div class="dropdown-divider" />
          <div class="dropdown-item logout" @click="handleLogout">退出登录</div>
        </div>
      </div>

      <nav class="sidebar-nav">
        <div v-for="t in navTabs" :key="t.path"
             :class="['nav-item', { active: isActive(t.path) }]"
             @click="goTab(t.path)">
          <el-icon :size="20"><component :is="t.icon" /></el-icon>
          <span v-if="!collapsed" class="nav-label">{{ t.label }}</span>
        </div>
      </nav>

      <!-- Online indicator -->
      <div class="sidebar-footer">
        <div class="connection-indicator" :class="{ connected: chatStore.connected }">
          <span class="conn-dot" />
          <span v-if="!collapsed" class="conn-text">{{ chatStore.connected ? '已连接' : '未连接' }}</span>
        </div>
      </div>
    </aside>

    <!-- Content -->
    <main class="content">
      <RouterView />
    </main>

    <!-- Profile Dialog -->
    <el-dialog v-model="showProfileDialog" title="编辑个人资料" width="440px" :close-on-click-modal="false">
      <div class="profile-dialog">
        <div class="avatar-section" @click="avatarInput?.click()">
          <el-avatar :size="80" :src="userStore.user?.avatar || ''" :icon="UserFilled" style="cursor: pointer;" />
          <span class="avatar-hint">点击更换头像</span>
          <input ref="avatarInput" type="file" accept="image/*" style="display:none" @change="onAvatarFileChange" />
        </div>
        <el-form :model="profileForm" label-width="70px">
          <el-form-item label="昵称">
            <el-input v-model="profileForm.nickName" maxlength="16" />
          </el-form-item>
          <el-form-item label="个性签名">
            <el-input v-model="profileForm.sign" maxlength="100" />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="profileForm.gender">
              <el-radio :value="null">保密</el-radio>
              <el-radio :value="0">男</el-radio>
              <el-radio :value="1">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="生日">
            <div style="display: flex; gap: 8px;">
              <el-select v-model="profileForm.birthYear" placeholder="年" clearable style="flex:1;">
                <el-option v-for="y in 80" :key="y" :label="currentYear - y + 1" :value="currentYear - y + 1" />
              </el-select>
              <el-select v-model="profileForm.birthMonth" placeholder="月" clearable style="flex:1;">
                <el-option v-for="m in 12" :key="m" :label="m" :value="m" />
              </el-select>
              <el-select v-model="profileForm.birthDay" placeholder="日" clearable style="flex:1;">
                <el-option v-for="d in 31" :key="d" :label="d" :value="d" />
              </el-select>
            </div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showProfileDialog = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.main-root {
  height: 100vh;
  display: flex;
  overflow: hidden;
}

/* Sidebar */
.sidebar {
  width: 260px;
  min-width: 260px;
  background: #2b2d31;
  display: flex;
  flex-direction: column;
  transition: width 0.25s, min-width 0.25s;
  user-select: none;
}
.sidebar.collapsed {
  width: 60px;
  min-width: 60px;
}

.sidebar-avatar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 14px;
  position: relative;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
.avatar-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
  min-width: 0;
}
.nick {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255,255,255,0.9);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.status-box {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 2px 8px;
  border: 1px solid rgba(255,255,255,0.12);
  border-radius: 10px;
  cursor: pointer;
  width: fit-content;
  position: relative;
  font-size: 11px;
  color: rgba(255,255,255,0.65);
}
.status-box:hover {
  border-color: rgba(255,255,255,0.3);
}
.status-dot {
  width: 7px; height: 7px;
  border-radius: 50%;
  background: #666;
  flex-shrink: 0;
}
.status-dot.online { background: #4ade80; box-shadow: 0 0 5px rgba(74,222,128,0.4); }
.status-dot.busy   { background: #ef5350; }
.status-dot.away   { background: #ffb74d; }
.status-dot.invisible { background: #777; }

.dropdown {
  position: absolute;
  background: #1e1f22;
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 8px;
  min-width: 100px;
  padding: 4px 0;
  z-index: 50;
  box-shadow: 0 4px 20px rgba(0,0,0,0.4);
}
.dropdown-item {
  padding: 8px 16px;
  font-size: 12px;
  color: rgba(255,255,255,0.75);
  cursor: pointer;
  white-space: nowrap;
}
.dropdown-item:hover { background: rgba(255,255,255,0.06); }
.dropdown-item.active { color: #4ade80; }
.dropdown-item.logout { color: #ef5350; }
.dropdown-divider {
  height: 1px;
  background: rgba(255,255,255,0.06);
  margin: 3px 0;
}

/* Nav */
.sidebar-nav {
  flex: 1;
  padding: 10px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  color: rgba(255,255,255,0.5);
  cursor: pointer;
  transition: all 0.15s;
  font-size: 14px;
}
.nav-item:hover {
  background: rgba(255,255,255,0.05);
  color: rgba(255,255,255,0.75);
}
.nav-item.active {
  background: rgba(255,255,255,0.1);
  color: #fff;
}
.nav-label { white-space: nowrap; }

/* Footer */
.sidebar-footer {
  padding: 12px 14px;
  border-top: 1px solid rgba(255,255,255,0.06);
}
.connection-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: rgba(255,255,255,0.4);
}
.conn-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: #666;
}
.connection-indicator.connected .conn-dot {
  background: #4ade80;
}
.conn-text { white-space: nowrap; }

/* Content */
.content {
  flex: 1;
  background: #f5f5f5;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* Profile Dialog */
.profile-dialog {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.avatar-hint {
  font-size: 12px;
  color: #999;
}
</style>
