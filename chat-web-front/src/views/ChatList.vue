<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat'
import { useUserStore } from '@/stores/user'
import { UserFilled, ChatLineSquare } from '@element-plus/icons-vue'

const router = useRouter()
const chatStore = useChatStore()

const conversations = computed(() => chatStore.conversations)

function openChat(conv: typeof conversations.value[0]) {
  if (conv.type === 'private') {
    router.push({ name: 'private-chat', params: { userId: conv.targetId }, query: { name: conv.name } })
  } else {
    router.push({ name: 'group-chat', params: { groupId: conv.targetId }, query: { name: conv.name } })
  }
}

function formatTime(iso: string): string {
  if (!iso) return ''
  const d = new Date(iso)
  const now = new Date()
  const isToday = d.toDateString() === now.toDateString()
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  const isYesterday = d.toDateString() === yesterday.toDateString()

  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  if (isToday) return `${hh}:${mm}`
  if (isYesterday) return '昨天'
  return `${d.getMonth() + 1}/${d.getDate()}`
}

function truncate(text: string, max: number): string {
  if (!text) return ''
  return text.length > max ? text.slice(0, max) + '...' : text
}

function statusColor(s: string): string {
  if (s === 'online') return '#4ade80'
  if (s === 'busy') return '#ef5350'
  if (s === 'away') return '#ffb74d'
  return '#888'
}

function lastMsgPreview(conv: typeof conversations.value[0]): string {
  const msgs = chatStore.messages[conv.id]
  if (!msgs || msgs.length === 0) return conv.lastMessage || ''
  const last = msgs[msgs.length - 1]
  if (last.msgType === 'text' || last.msgType === 1) {
    return truncate(last.content, 30)
  }
  if (last.fileName) return `[文件] ${last.fileName}`
  return '[非文本消息]'
}
</script>

<template>
  <div class="chat-list-page">
    <!-- Header -->
    <div class="list-header">
      <span class="header-title">消息</span>
    </div>

    <!-- Conversation list -->
    <div class="conv-list" v-if="conversations.length > 0">
      <div
        v-for="conv in conversations"
        :key="conv.id"
        :class="['conv-item', { active: chatStore.activeConversationId === conv.id }]"
        @click="openChat(conv)"
      >
        <div class="conv-avatar">
          <el-avatar :size="44" :icon="UserFilled" />
          <span v-if="conv.type === 'private'" class="online-dot" :style="{ background: statusColor(conv.onlineStatus) }" />
        </div>
        <div class="conv-body">
          <div class="conv-top">
            <span class="conv-name">{{ conv.name }}</span>
            <span class="conv-time">{{ formatTime(conv.lastTime) }}</span>
          </div>
          <div class="conv-bottom">
            <span class="conv-preview">{{ lastMsgPreview(conv) }}</span>
            <span v-if="conv.unreadCount > 0" class="unread-badge">{{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else class="empty-state">
      <el-icon :size="60" color="#ccc"><ChatLineSquare /></el-icon>
      <p class="empty-text">暂无会话</p>
      <p class="empty-hint">去好友列表发起聊天吧</p>
    </div>
  </div>
</template>

<style scoped>
.chat-list-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.list-header {
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

/* Conversation list */
.conv-list {
  flex: 1;
  overflow-y: auto;
}
.conv-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 18px;
  cursor: pointer;
  transition: background 0.1s;
}
.conv-item:hover {
  background: #f5f5f5;
}
.conv-item.active {
  background: #e8f0fe;
}

.conv-avatar {
  position: relative;
  flex-shrink: 0;
}
.online-dot {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid #fff;
  background: #888;
}

.conv-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.conv-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.conv-name {
  font-size: 14px;
  font-weight: 500;
  color: #222;
}
.conv-time {
  font-size: 11px;
  color: #999;
  flex-shrink: 0;
}
.conv-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.conv-preview {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
}
.unread-badge {
  background: #ff4d4f;
  color: #fff;
  font-size: 10px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  border-radius: 9px;
  padding: 0 5px;
  flex-shrink: 0;
}

/* Empty */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.empty-text {
  font-size: 15px;
  color: #999;
  margin-top: 12px;
}
.empty-hint {
  font-size: 12px;
  color: #ccc;
}
</style>
