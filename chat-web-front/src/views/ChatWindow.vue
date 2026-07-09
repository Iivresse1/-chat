<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat'
import { useUserStore } from '@/stores/user'
import { UserFilled, ArrowLeft, Picture, Paperclip } from '@element-plus/icons-vue'
import type { ChatMessage, Conversation } from '@/stores/chat'

const props = defineProps({
  chatType: { type: String as () => 'private' | 'group', required: true },
  chatId: { type: Number, required: true },
  chatName: { type: String, default: '' }
})

const router = useRouter()
const chatStore = useChatStore()
const userStore = useUserStore()

const inputText = ref('')
const msgListRef = ref<HTMLElement | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)
const imageInput = ref<HTMLInputElement | null>(null)
const loading = ref(false)

const conv = computed(() => chatStore.getConversation(props.chatType, props.chatId))
const messages = computed(() => chatStore.getMessages(props.chatType, props.chatId))

const title = computed(() => props.chatName || conv.value?.name || (props.chatType === 'private' ? '私聊' : '群聊'))

const onlineStatus = computed(() => {
  if (props.chatType === 'group') return ''
  return conv.value?.onlineStatus || 'offline'
})

const statusText = computed(() => {
  if (props.chatType === 'group') return ''
  const s = onlineStatus.value
  if (s === 'online') return '在线'
  if (s === 'busy') return '忙碌'
  if (s === 'away') return '离开'
  return '离线'
})

function scrollToBottom() {
  nextTick(() => {
    if (msgListRef.value) {
      msgListRef.value.scrollTop = msgListRef.value.scrollHeight
    }
  })
}

async function loadHistory() {
  loading.value = true
  try {
    await chatStore.loadHistory(props.chatType, props.chatId)
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text) return
  inputText.value = ''
  try {
    if (props.chatType === 'private') {
      await chatStore.sendPrivateMessage(props.chatId, text)
    } else {
      await chatStore.sendGroupMessage(props.chatId, text)
    }
    scrollToBottom()
  } catch { /* handled in store */ }
}

async function sendImage(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const content = `[图片] ${file.name}`
  try {
    if (props.chatType === 'private') {
      await chatStore.sendPrivateMessage(props.chatId, content)
    } else {
      await chatStore.sendGroupMessage(props.chatId, content)
    }
    scrollToBottom()
  } catch { /* ignore */ }
  (e.target as HTMLInputElement).value = ''
}

async function sendFile(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const content = `[文件] ${file.name}`
  try {
    if (props.chatType === 'private') {
      await chatStore.sendPrivateMessage(props.chatId, content)
    } else {
      await chatStore.sendGroupMessage(props.chatId, content)
    }
    scrollToBottom()
  } catch { /* ignore */ }
  (e.target as HTMLInputElement).value = ''
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

function goBack() {
  router.push({ name: 'chat' })
}

function isMine(msg: ChatMessage): boolean {
  return msg.fromUserId === userStore.userId
}

function statusIcon(msg: ChatMessage): string {
  if (msg.msgStatus === 'sending') return '⏳'
  if (msg.msgStatus === 'sent') return '✓'
  if (msg.msgStatus === 'delivered') return '✓✓'
  if (msg.msgStatus === 'read') return '✓✓'
  return ''
}

function statusClass(msg: ChatMessage): string {
  if (msg.msgStatus === 'read') return 'status-read'
  if (msg.msgStatus === 'delivered') return 'status-delivered'
  return 'status-sent'
}

function formatTime(iso: string): string {
  if (!iso) return ''
  const d = new Date(iso)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function formatFullTime(iso: string): string {
  if (!iso) return ''
  const d = new Date(iso)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function isImageMsg(msg: ChatMessage): boolean {
  return msg.content.startsWith('[图片]') || msg.msgType === 'image' || msg.msgType === 2
}

function isFileMsg(msg: ChatMessage): boolean {
  return msg.content.startsWith('[文件]') || msg.msgType === 'file' || msg.msgType === 5
}

// Lifecycle
onMounted(() => {
  chatStore.openConversation(props.chatType, props.chatId)
  loadHistory()
})

onBeforeUnmount(() => {
  chatStore.closeConversation()
})

// Watch for real-time message updates to auto-scroll
watch(() => messages.value.length, () => {
  scrollToBottom()
})
</script>

<template>
  <div class="chat-window">
    <!-- Top bar -->
    <div class="chat-topbar">
      <div class="top-left">
        <el-button :icon="ArrowLeft" text @click="goBack" class="back-btn" />
        <el-avatar :size="34" :icon="UserFilled" />
        <div class="top-info">
          <span class="top-name">{{ title }}</span>
          <span v-if="chatType === 'private'" class="top-status" :class="onlineStatus">
            {{ statusText }}
          </span>
        </div>
      </div>
      <div class="top-actions">
        <span v-if="chatType === 'private'" class="status-dot-sm" :class="onlineStatus" />
      </div>
    </div>

    <!-- Message list -->
    <div ref="msgListRef" class="msg-list">
      <div v-if="loading" class="loading-hint">加载中...</div>

      <div v-for="m in messages" :key="m.id" :class="['msg-row', isMine(m) ? 'msg-mine' : 'msg-other']">
        <!-- Avatar (other) -->
        <el-avatar v-if="!isMine(m)" :size="32" :icon="UserFilled" class="msg-avatar" />

        <div class="msg-content">
          <!-- Sender name in group -->
          <div v-if="chatType === 'group' && !isMine(m)" class="msg-sender">用户{{ m.fromUserId }}</div>

          <div v-if="isImageMsg(m)" class="msg-bubble image-bubble">
            📷 {{ m.content.replace('[图片] ', '') }}
          </div>
          <div v-else-if="isFileMsg(m)" class="msg-bubble file-bubble">
            📎 {{ m.content.replace('[文件] ', '') }}
          </div>
          <div v-else :class="['msg-bubble', isMine(m) ? 'bubble-mine' : 'bubble-other']">
            <span class="msg-text">{{ m.content }}</span>
          </div>

          <div :class="['msg-meta', isMine(m) ? 'meta-mine' : 'meta-other']">
            <span class="msg-time">{{ formatTime(m.msgSendTime) }}</span>
            <span v-if="isMine(m)" :class="['msg-status', statusClass(m)]" :title="formatFullTime(m.msgSendTime)">
              {{ statusIcon(m) }}
            </span>
          </div>
        </div>

        <!-- Avatar (me) -->
        <el-avatar v-if="isMine(m)" :size="32" :src="userStore.avatar || ''" :icon="UserFilled" class="msg-avatar" />
      </div>

      <div v-if="!loading && messages.length === 0" class="empty-hint">
        <p>暂无消息，发送一条开始聊天吧</p>
      </div>
    </div>

    <!-- Toolbar -->
    <div class="chat-toolbar">
      <button class="tool-btn" title="图片" @click="imageInput?.click()">🖼</button>
      <button class="tool-btn" title="文件" @click="fileInput?.click()">📎</button>
      <input ref="imageInput" type="file" accept="image/*" style="display:none" @change="sendImage" />
      <input ref="fileInput" type="file" style="display:none" @change="sendFile" />
    </div>

    <!-- Input area -->
    <div class="chat-input-row">
      <textarea
        v-model="inputText"
        class="chat-input"
        placeholder="输入消息... (Enter发送, Shift+Enter换行)"
        rows="3"
        @keydown="handleKeydown"
      />
      <button class="send-btn" @click="sendMessage" :disabled="!inputText.trim()">发送</button>
    </div>
  </div>
</template>

<style scoped>
.chat-window {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
}

/* Top bar */
.chat-topbar {
  height: 52px;
  min-height: 52px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}
.top-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.back-btn {
  display: none;
}
@media (max-width: 600px) {
  .back-btn { display: inline-flex; }
}
.top-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
}
.top-name {
  font-size: 15px;
  font-weight: 600;
  color: #222;
}
.top-status {
  font-size: 11px;
  color: #999;
}
.top-status.online { color: #4ade80; }
.top-status.busy   { color: #ef5350; }
.top-status.away   { color: #ffb74d; }

.status-dot-sm {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: #888;
}
.status-dot-sm.online { background: #4ade80; }
.status-dot-sm.busy   { background: #ef5350; }
.status-dot-sm.away   { background: #ffb74d; }

/* Message list */
.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}
.msg-mine {
  justify-content: flex-end;
}
.msg-other {
  justify-content: flex-start;
}

.msg-avatar {
  flex-shrink: 0;
  margin-top: 4px;
}

.msg-content {
  max-width: 65%;
  display: flex;
  flex-direction: column;
}

.msg-sender {
  font-size: 11px;
  color: #999;
  margin-bottom: 2px;
  padding: 0 4px;
}

.msg-bubble {
  padding: 8px 14px;
  font-size: 14px;
  line-height: 1.55;
  word-break: break-word;
  border-radius: 10px;
}
.bubble-mine {
  background: #95ec69;
  border-bottom-right-radius: 3px;
  color: #222;
}
.bubble-other {
  background: #fff;
  border-bottom-left-radius: 3px;
  color: #222;
}
.image-bubble,
.file-bubble {
  background: #fff;
  border-radius: 10px;
  font-size: 13px;
}

.msg-text {
  white-space: pre-wrap;
}

.msg-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 2px;
  padding: 0 4px;
}
.meta-mine { justify-content: flex-end; }
.meta-other { justify-content: flex-start; }

.msg-time {
  font-size: 10px;
  color: #aaa;
}

.msg-status {
  font-size: 10px;
}
.status-sent { color: #aaa; }
.status-delivered { color: #888; }
.status-read { color: #1677ff; }

.loading-hint {
  text-align: center;
  color: #aaa;
  padding: 20px;
  font-size: 13px;
}
.empty-hint {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.empty-hint p {
  color: #bbb;
  font-size: 14px;
}

/* Toolbar */
.chat-toolbar {
  display: flex;
  gap: 2px;
  padding: 4px 14px;
  background: #fff;
  border-top: 1px solid #eee;
}
.tool-btn {
  width: 30px; height: 30px;
  border: none;
  border-radius: 6px;
  background: transparent;
  font-size: 15px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;
}
.tool-btn:hover { background: #e8e8e8; }

/* Input */
.chat-input-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 6px 14px 10px;
  background: #fff;
  border-top: 1px solid #eee;
}
.chat-input {
  flex: 1;
  border: none;
  outline: none;
  resize: none;
  font-size: 14px;
  padding: 8px 12px;
  border-radius: 8px;
  background: #f0f2f5;
  font-family: inherit;
  line-height: 1.5;
  min-height: 40px;
}
.chat-input:focus {
  background: #fff;
  box-shadow: 0 0 0 1px #1677ff;
}
.send-btn {
  padding: 8px 22px;
  border: none;
  border-radius: 8px;
  background: #1677ff;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.15s;
}
.send-btn:hover { background: #4096ff; }
.send-btn:disabled {
  background: #bbb;
  cursor: not-allowed;
}
</style>
