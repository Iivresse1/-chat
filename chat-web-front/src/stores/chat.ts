import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { io, Socket } from 'socket.io-client'
import { useUserStore } from './user'
import * as chatApi from '@/api/chat'
import { ElMessage } from 'element-plus'

export interface ChatMessage {
  id: number | string
  fromUserId: number
  toUserId?: number
  groupId?: number
  content: string
  msgType: string | number
  msgStatus: 'sending' | 'sent' | 'delivered' | 'read'
  msgSendTime: string
  fileUrl?: string
  fileName?: string
  fileSize?: number
}

export interface Conversation {
  id: string           // key: "p_{userId}" or "g_{groupId}"
  type: 'private' | 'group'
  targetId: number
  name: string
  avatar: string
  lastMessage: string
  lastTime: string
  unreadCount: number
  onlineStatus: string  // only for private
}

const LS_PREFIX = 'achat_'

export const useChatStore = defineStore('chat', () => {
  const conversations = ref<Conversation[]>([])
  const messages = ref<Record<string, ChatMessage[]>>({})
  const activeConversationId = ref<string | null>(null)
  const socket = ref<Socket | null>(null)
  const connected = ref(false)

  const userStore = useUserStore()

  // ========== helpers ==========
  function convKey(type: string, id: number): string {
    return `${type[0]}_${id}`
  }

  function loadFromStorage() {
    const uid = userStore.userId
    if (!uid) return
    try {
      const raw = localStorage.getItem(`${LS_PREFIX}conv_${uid}`)
      if (raw) conversations.value = JSON.parse(raw)
      const rawMsg = localStorage.getItem(`${LS_PREFIX}msg_${uid}`)
      if (rawMsg) messages.value = JSON.parse(rawMsg)
    } catch { /* ignore */ }
  }

  function saveToStorage() {
    const uid = userStore.userId
    if (!uid) return
    localStorage.setItem(`${LS_PREFIX}conv_${uid}`, JSON.stringify(conversations.value))
    localStorage.setItem(`${LS_PREFIX}msg_${uid}`, JSON.stringify(messages.value))
  }

  // ========== socket ==========
  function connectSocket() {
    if (socket.value?.connected) return
    const s = io('http://localhost:1001', {
      query: { token: userStore.token, userId: userStore.userId },
      transports: ['websocket', 'polling'],
      reconnection: true,
      reconnectionDelay: 1000,
      reconnectionAttempts: Infinity
    })
    socket.value = s

    s.on('connect', () => {
      connected.value = true
      // fetch offline messages
      s.emit('fetch-offline')
    })

    s.on('disconnect', () => { connected.value = false })

    s.on('private-message', (msg: ChatMessage) => {
      handleIncomingMessage('private', msg.fromUserId, msg)
    })

    s.on('group-message', (msg: ChatMessage) => {
      handleIncomingMessage('group', msg.groupId!, msg)
    })

    s.on('message-status', (data: { messageId: string, status: string, conversationId: string, persistedId?: string, fromUserId?: number }) => {
      updateMessageStatus(data.conversationId, data.messageId, data.status as any, data.persistedId)
      // Handle read receipt from other user
      if (data.status === 'read' && data.fromUserId) {
        const cid = convKey('private', data.fromUserId)
        const msgs = messages.value[cid]
        if (msgs) {
          for (const m of msgs) {
            if (m.fromUserId === userStore.userId && m.msgStatus !== 'read') {
              m.msgStatus = 'read'
            }
          }
          saveToStorage()
        }
      }
    })

    s.on('user-status-change', (data: { userId: number, status: string }) => {
      const cid = convKey('private', data.userId)
      const conv = conversations.value.find(c => c.id === cid)
      if (conv) {
        conv.onlineStatus = data.status
        saveToStorage()
      }
    })

    s.on('offline-messages', (msgs: ChatMessage[]) => {
      for (const msg of msgs) {
        const cType = msg.groupId ? 'group' : 'private'
        const tId = msg.groupId || msg.fromUserId
        handleIncomingMessage(cType, tId!, msg, false)
      }
    })

    s.on('error', (err: any) => {
      ElMessage.error('连接异常: ' + (err.message || 'unknown'))
    })
  }

  function disconnectSocket() {
    socket.value?.disconnect()
    socket.value = null
    connected.value = false
  }

  // ========== message handling ==========
  function handleIncomingMessage(type: 'private' | 'group', targetId: number, msg: ChatMessage, incrementUnread = true) {
    const cid = convKey(type, targetId)
    if (!messages.value[cid]) messages.value[cid] = []
    // avoid duplicate
    if (!messages.value[cid].find(m => m.id === msg.id)) {
      messages.value[cid].push(msg)
    }

    let conv = conversations.value.find(c => c.id === cid)
    if (!conv) {
      conv = {
        id: cid,
        type,
        targetId,
        name: type === 'private' ? `用户${targetId}` : `群${targetId}`,
        avatar: '',
        lastMessage: msg.msgType === 'text' || msg.msgType === 1 ? msg.content : '[非文本消息]',
        lastTime: msg.msgSendTime,
        unreadCount: 0,
        onlineStatus: 'offline'
      }
      conversations.value.unshift(conv)
    } else {
      conv.lastMessage = msg.msgType === 'text' || msg.msgType === 1 ? msg.content : '[非文本消息]'
      conv.lastTime = msg.msgSendTime
    }

    if (incrementUnread && cid !== activeConversationId.value) {
      conv.unreadCount++
    }

    // sort by lastTime desc
    conversations.value.sort((a, b) => new Date(b.lastTime).getTime() - new Date(a.lastTime).getTime())
    saveToStorage()
  }

  function updateMessageStatus(cid: string, msgId: string | number, status: 'sent' | 'delivered' | 'read', persistedId?: string) {
    const msgs = messages.value[cid]
    if (!msgs) return
    const msg = msgs.find(m => m.id === msgId)
    if (msg) {
      msg.msgStatus = status
      // Replace temp ID with real DB ID
      if (persistedId && String(msg.id).startsWith('temp_')) {
        msg.id = persistedId
      }
      saveToStorage()
    }
  }

  // ========== sending ==========
  async function sendPrivateMessage(toUserId: number, content: string): Promise<void> {
    const tempId = 'temp_' + Date.now() + '_' + Math.random().toString(36).slice(2)
    const cid = convKey('private', toUserId)
    if (!messages.value[cid]) messages.value[cid] = []

    const msg: ChatMessage = {
      id: tempId,
      fromUserId: userStore.userId,
      toUserId,
      content,
      msgType: 'text',
      msgStatus: 'sending',
      msgSendTime: new Date().toISOString()
    }
    messages.value[cid].push(msg)
    updateConversationAfterSend('private', toUserId, content)
    saveToStorage()

    try {
      // Send via Socket.io for real-time
      if (socket.value?.connected) {
        socket.value.emit('private-message', {
          toUserId,
          content,
          msgType: 'text',
          tempId
        })
      }
      // Also POST to REST API for persistence
      await chatApi.sendPrivateMsg(toUserId, 'text', content)
      // Socket.io server will send back the persisted message
    } catch (e) {
      msg.msgStatus = 'sent' // fallback
      saveToStorage()
    }
  }

  async function sendGroupMessage(groupId: number, content: string): Promise<void> {
    const tempId = 'temp_' + Date.now() + '_' + Math.random().toString(36).slice(2)
    const cid = convKey('group', groupId)
    if (!messages.value[cid]) messages.value[cid] = []

    const msg: ChatMessage = {
      id: tempId,
      fromUserId: userStore.userId,
      groupId,
      content,
      msgType: 1 as any,
      msgStatus: 'sending',
      msgSendTime: new Date().toISOString()
    }
    messages.value[cid].push(msg)
    updateConversationAfterSend('group', groupId, content)
    saveToStorage()

    try {
      if (socket.value?.connected) {
        socket.value.emit('group-message', {
          groupId,
          content,
          msgType: 1,
          tempId
        })
      }
      await chatApi.sendGroupMsg(groupId, 1, content)
    } catch (e) {
      msg.msgStatus = 'sent'
      saveToStorage()
    }
  }

  function updateConversationAfterSend(type: 'private' | 'group', targetId: number, content: string) {
    const cid = convKey(type, targetId)
    let conv = conversations.value.find(c => c.id === cid)
    if (!conv) {
      conv = {
        id: cid, type, targetId,
        name: type === 'private' ? `用户${targetId}` : `群${targetId}`,
        avatar: '', lastMessage: content,
        lastTime: new Date().toISOString(),
        unreadCount: 0, onlineStatus: 'offline'
      }
      conversations.value.unshift(conv)
    } else {
      conv.lastMessage = content
      conv.lastTime = new Date().toISOString()
    }
    conversations.value.sort((a, b) => new Date(b.lastTime).getTime() - new Date(a.lastTime).getTime())
    saveToStorage()
  }

  // ========== history loading ==========
  async function loadHistory(type: 'private' | 'group', targetId: number) {
    const cid = convKey(type, targetId)
    try {
      const res = type === 'private'
        ? await chatApi.getPrivateHistory(targetId)
        : await chatApi.getGroupHistory(targetId)
      const list = (res.data as any)?.records || (res.data as any[]) || []
      const msgs: ChatMessage[] = (Array.isArray(list) ? list : []).map((m: any) => ({
        id: m.id,
        fromUserId: m.fromUserId || m.fromUserId,
        toUserId: m.toUserId,
        groupId: m.groupId,
        content: m.content || '',
        msgType: m.msgType || 1,
        msgStatus: m.msgStatus || 'sent',
        msgSendTime: m.msgSendTime || m.createTime || '',
        fileUrl: m.fileUrl,
        fileName: m.fileName,
        fileSize: m.fileSize
      }))
      messages.value[cid] = msgs
      saveToStorage()
    } catch (e) {
      // fallback to local
      if (!messages.value[cid]) messages.value[cid] = []
    }
  }

  // ========== unread management ==========
  function openConversation(type: 'private' | 'group', targetId: number) {
    const cid = convKey(type, targetId)
    activeConversationId.value = cid
    const conv = conversations.value.find(c => c.id === cid)
    if (conv) {
      conv.unreadCount = 0
      saveToStorage()
    }
    // send read receipt for private
    if (type === 'private' && socket.value?.connected) {
      socket.value.emit('mark-read', { fromUserId: targetId })
      chatApi.markRead(targetId).catch(() => {})
    }
  }

  function closeConversation() {
    activeConversationId.value = null
  }

  function getMessages(type: 'private' | 'group', targetId: number): ChatMessage[] {
    const cid = convKey(type, targetId)
    return messages.value[cid] || []
  }

  function getConversation(type: 'private' | 'group', targetId: number): Conversation | undefined {
    const cid = convKey(type, targetId)
    return conversations.value.find(c => c.id === cid)
  }

  // ========== conversation management ==========
  function addConversationFromFriend(friend: any) {
    const cid = convKey('private', friend.friendId)
    if (conversations.value.find(c => c.id === cid)) return
    conversations.value.push({
      id: cid,
      type: 'private',
      targetId: friend.friendId,
      name: friend.friendNickName || friend.friendRemark || `用户${friend.friendId}`,
      avatar: '',
      lastMessage: '',
      lastTime: '',
      unreadCount: 0,
      onlineStatus: 'offline'
    })
    saveToStorage()
  }

  function removeConversation(friendId: number) {
    const cid = convKey('private', friendId)
    conversations.value = conversations.value.filter(c => c.id !== cid)
    delete messages.value[cid]
    saveToStorage()
  }

  function updateConversationInfo(type: 'private' | 'group', targetId: number, info: Partial<Conversation>) {
    const cid = convKey(type, targetId)
    const conv = conversations.value.find(c => c.id === cid)
    if (conv) {
      Object.assign(conv, info)
      saveToStorage()
    }
  }

  return {
    conversations, messages, activeConversationId, socket, connected,
    connectSocket, disconnectSocket, loadFromStorage, saveToStorage,
    sendPrivateMessage, sendGroupMessage,
    loadHistory, openConversation, closeConversation,
    getMessages, getConversation,
    addConversationFromFriend, removeConversation, updateConversationInfo,
    convKey
  }
})
