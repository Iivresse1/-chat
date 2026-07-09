<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getFriendList, getPendingRequests, handleFriendRequest,
  searchUsers, sendFriendRequest, deleteFriend
} from '@/api/friend'
import { UserFilled } from '@element-plus/icons-vue'

const router = useRouter()
const chatStore = useChatStore()

const friends = ref<any[]>([])
const pendingRequests = ref<any[]>([])
const searchKeyword = ref('')
const searchResults = ref<any[]>([])
const expanded = ref<Record<string, boolean>>({})

const categories = ['朋友', '家人', '同学']

const categoryMap = computed(() => {
  const map: Record<string, any[]> = {}
  for (const cat of categories) map[cat] = []
  for (const f of friends.value) {
    const cat = f.category || '朋友'
    if (!map[cat]) map[cat] = []
    map[cat].push(f)
  }
  return map
})

function toggleExpand(key: string) {
  expanded.value[key] = !expanded.value[key]
}

async function loadData() {
  try {
    const [listRes, pendRes] = await Promise.all([
      getFriendList(), getPendingRequests()
    ])
    friends.value = (listRes.data as any[]) || []
    pendingRequests.value = (pendRes.data as any[]) || []
  } catch { /* backend not available */ }
}

onMounted(loadData)

async function handleSearch() {
  const kw = searchKeyword.value.trim()
  if (!kw) { searchResults.value = []; return }
  try {
    const res = await searchUsers(kw)
    searchResults.value = (res.data as any[]) || []
  } catch { /* ignore */ }
}

async function handleAdd(user: any) {
  try {
    await sendFriendRequest(user.id)
    ElMessage.success('好友请求已发送')
    searchKeyword.value = ''
    searchResults.value = []
  } catch { /* handled */ }
}

async function handleRequest(reqId: number, accept: boolean) {
  try {
    await handleFriendRequest(reqId, accept)
    ElMessage.success(accept ? '已通过' : '已拒绝')
    loadData()
  } catch { /* handled */ }
}

async function handleDeleteFriend(friend: any) {
  try {
    await ElMessageBox.confirm(
      `确定删除好友 ${friend.friendNickName || friend.friendRemark || ''}？`,
      '删除好友',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteFriend(friend.friendId)
    ElMessage.success('已删除')
    chatStore.removeConversation(friend.friendId)
    loadData()
  } catch {
    if ((arguments[0] as any) !== 'cancel') ElMessage.error('删除失败')
  }
}

function openChat(friend: any) {
  chatStore.addConversationFromFriend(friend)
  const name = friend.friendNickName || friend.friendRemark || `用户${friend.friendId}`
  router.push({
    name: 'private-chat',
    params: { userId: friend.friendId },
    query: { name }
  })
}
</script>

<template>
  <div class="contacts-page">
    <!-- Header -->
    <div class="contacts-header">
      <span class="header-title">好友</span>
    </div>

    <!-- Search -->
    <div class="search-section">
      <div class="search-row">
        <input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索用户 按昵称/邮箱"
          @input="handleSearch"
        />
      </div>
      <div v-if="searchKeyword && searchResults.length" class="search-dropdown">
        <div v-for="u in searchResults" :key="u.id" class="search-item">
          <el-avatar :size="30" :icon="UserFilled" />
          <div class="search-info">
            <span class="search-name">{{ u.nickName }}</span>
            <span class="search-email">{{ u.email }}</span>
          </div>
          <button class="add-btn" @click="handleAdd(u)">添加</button>
        </div>
      </div>
    </div>

    <!-- Contact list -->
    <div class="contact-list">
      <!-- Pending requests -->
      <div v-if="pendingRequests.length" class="group">
        <div class="group-title" @click="toggleExpand('new_friends')">
          <span class="arrow" :class="{ open: expanded['new_friends'] }">▶</span>
          <span>新朋友</span>
          <span class="badge">{{ pendingRequests.length }}</span>
        </div>
        <div v-if="expanded['new_friends']">
          <div v-for="r in pendingRequests" :key="r.id" class="req-row">
            <el-avatar :size="32" :icon="UserFilled" />
            <div class="req-info">
              <span class="req-name">{{ r.senderNickName || '用户' }}</span>
              <span class="req-msg" v-if="r.message">{{ r.message }}</span>
            </div>
            <div class="req-btns">
              <button class="btn-accept" @click="handleRequest(r.id, true)">通过</button>
              <button class="btn-reject" @click="handleRequest(r.id, false)">拒绝</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Friend categories -->
      <div v-for="cat in categories" :key="cat" class="group">
        <div class="group-title" @click="toggleExpand(cat)">
          <span class="arrow" :class="{ open: expanded[cat] }">▶</span>
          <span>{{ cat }}</span>
          <span class="count">{{ (categoryMap[cat] || []).length }}</span>
        </div>
        <template v-if="expanded[cat] && categoryMap[cat]">
          <div
            v-for="f in categoryMap[cat]"
            :key="f.id"
            class="friend-row"
            @click="openChat(f)"
            @contextmenu.prevent="handleDeleteFriend(f)"
          >
            <el-avatar :size="36" :icon="UserFilled" />
            <div class="friend-info">
              <span class="friend-name">{{ f.friendNickName || f.friendRemark || '好友' }}</span>
              <span class="friend-sign" v-if="f.friendSign">{{ f.friendSign }}</span>
            </div>
          </div>
        </template>
      </div>

      <!-- Empty state -->
      <div v-if="friends.length === 0 && pendingRequests.length === 0" class="empty-state">
        <p>还没有好友，去搜索添加吧</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.contacts-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.contacts-header {
  height: 52px;
  min-height: 52px;
  display: flex;
  align-items: center;
  padding: 0 18px;
  border-bottom: 1px solid #eee;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #222;
}

/* Search */
.search-section {
  position: relative;
  padding: 10px 14px;
  border-bottom: 1px solid #f0f0f0;
}
.search-row {
  display: flex;
  gap: 8px;
}
.search-input {
  flex: 1;
  padding: 7px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 12px;
  outline: none;
  background: #f5f5f5;
}
.search-input:focus {
  border-color: #1677ff;
  background: #fff;
}
.search-dropdown {
  position: absolute;
  top: 44px;
  left: 14px;
  right: 14px;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
  z-index: 10;
  max-height: 260px;
  overflow-y: auto;
}
.search-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
}
.search-item:hover { background: #f5f5f5; }
.search-info { flex: 1; display: flex; flex-direction: column; gap: 1px; min-width: 0; }
.search-name { font-size: 13px; color: #222; }
.search-email { font-size: 11px; color: #999; }
.add-btn {
  padding: 4px 12px;
  border: 1px solid #1677ff;
  border-radius: 4px;
  background: transparent;
  color: #1677ff;
  font-size: 11px;
  cursor: pointer;
  white-space: nowrap;
}
.add-btn:hover { background: #1677ff; color: #fff; }

/* Contact list */
.contact-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.group {
  border-bottom: 1px solid #f5f5f5;
}
.group-title {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  cursor: pointer;
  font-size: 13px;
  color: #555;
  user-select: none;
}
.group-title:hover { background: #f5f5f5; }
.arrow {
  font-size: 9px;
  color: #999;
  display: inline-block;
  transition: transform 0.2s;
}
.arrow.open { transform: rotate(90deg); }
.badge {
  background: #ff4d4f;
  color: #fff;
  border-radius: 10px;
  padding: 0 6px;
  font-size: 10px;
  margin-left: auto;
}
.count {
  color: #aaa;
  font-size: 11px;
  margin-left: auto;
}

/* Friend rows */
.friend-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 14px 8px 44px;
  cursor: pointer;
}
.friend-row:hover { background: #f0f7ff; }
.friend-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.friend-name {
  font-size: 13px;
  color: #222;
}
.friend-sign {
  font-size: 11px;
  color: #aaa;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Request rows */
.req-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 14px 8px 44px;
}
.req-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.req-name { font-size: 13px; color: #222; }
.req-msg { font-size: 11px; color: #999; }
.req-btns { display: flex; gap: 6px; }
.btn-accept, .btn-reject {
  padding: 4px 12px;
  border-radius: 4px;
  border: none;
  font-size: 11px;
  cursor: pointer;
}
.btn-accept { background: #1677ff; color: #fff; }
.btn-reject { background: #f0f0f0; color: #666; }

/* Empty */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #bbb;
  font-size: 14px;
}
</style>
