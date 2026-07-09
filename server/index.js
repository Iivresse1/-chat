import { createServer } from 'node:http'
import { Server } from 'socket.io'
import axios from 'axios'
import { v4 as uuidv4 } from 'uuid'

const PORT = 3000
const API_BASE = 'http://localhost:1000/chat'

const httpServer = createServer()
const io = new Server(httpServer, {
  cors: {
    origin: ['http://localhost:5173', 'http://127.0.0.1:5173'],
    methods: ['GET', 'POST']
  },
  pingTimeout: 60000,
  pingInterval: 25000
})

// ============ In-memory stores ============
const onlineUsers = new Map()       // userId -> Set<socketId>
const offlineMessages = new Map()   // userId -> ChatMessage[]
const socketToUser = new Map()      // socketId -> { userId, token }

// ============ Helpers ============
function apiHeaders(token) {
  return { headers: { Authorization: token, 'Content-Type': 'application/json' } }
}

function storeOfflineMessage(userId, msg) {
  if (!offlineMessages.has(userId)) {
    offlineMessages.set(userId, [])
  }
  offlineMessages.get(userId).push(msg)
  // Cap at 500 messages per user
  if (offlineMessages.get(userId).length > 500) {
    offlineMessages.get(userId).shift()
  }
}

function drainOfflineMessages(userId) {
  const msgs = offlineMessages.get(userId) || []
  offlineMessages.delete(userId)
  return msgs
}

// ============ Socket.io ============
io.on('connection', (socket) => {
  const { token, userId } = socket.handshake.auth
  if (!token || !userId) {
    socket.emit('error', { message: '认证失败: 缺少 token 或 userId' })
    socket.disconnect(true)
    return
  }

  const uid = Number(userId)
  socketToUser.set(socket.id, { userId: uid, token })

  // Track online
  if (!onlineUsers.has(uid)) {
    onlineUsers.set(uid, new Set())
  }
  onlineUsers.get(uid).add(socket.id)

  console.log(`[connect] user=${uid} socket=${socket.id} (online: ${onlineUsers.size} users)`)

  // Broadcast online status to all
  io.emit('user-status-change', { userId: uid, status: 'online' })

  // ========== Offline message retrieval ==========
  socket.on('fetch-offline', () => {
    const msgs = drainOfflineMessages(uid)
    if (msgs.length > 0) {
      console.log(`[offline] delivering ${msgs.length} messages to user=${uid}`)
      socket.emit('offline-messages', msgs)
    }
  })

  // ========== Private message ==========
  socket.on('private-message', async (data) => {
    const { toUserId, content, msgType, fileUrl, fileName, fileSize, tempId } = data
    const msgId = uuidv4()
    const now = new Date().toISOString()

    const msg = {
      id: tempId || msgId,
      messageId: msgId,
      fromUserId: uid,
      toUserId,
      content: content || '',
      msgType: msgType || 'text',
      msgStatus: 'sent',
      msgSendTime: now,
      fileUrl: fileUrl || null,
      fileName: fileName || null,
      fileSize: fileSize || null
    }

    // Persist via REST API (fire and forget)
    try {
      const res = await axios.post(`${API_BASE}/private-msg/send`, {
        toUserId, msgType: msgType || 'text', content,
        fileUrl, fileName, fileSize
      }, apiHeaders(token))
      // Use the persisted message ID if available
      if (res.data?.data?.id) {
        msg.id = res.data.data.id
      }
    } catch (e) {
      console.error(`[private-msg] API persist failed:`, e.message)
    }

    // Confirm sent back to sender
    socket.emit('message-status', {
      messageId: tempId || msgId,
      status: 'sent',
      conversationId: `p_${toUserId}`,
      persistedId: msg.id
    })

    // Deliver to recipient if online
    const recipientSockets = onlineUsers.get(Number(toUserId))
    if (recipientSockets && recipientSockets.size > 0) {
      for (const sid of recipientSockets) {
        io.to(sid).emit('private-message', msg)
      }
      // Mark as delivered
      socket.emit('message-status', {
        messageId: tempId || msgId,
        status: 'delivered',
        conversationId: `p_${toUserId}`
      })
    } else {
      // Offline - store for later
      storeOfflineMessage(Number(toUserId), msg)
    }
  })

  // ========== Group message ==========
  socket.on('group-message', async (data) => {
    const { groupId, content, msgType, fileUrl, fileName, fileSize, tempId } = data
    const msgId = uuidv4()
    const now = new Date().toISOString()

    const msg = {
      id: tempId || msgId,
      messageId: msgId,
      fromUserId: uid,
      groupId,
      content: content || '',
      msgType: msgType || 1,
      msgStatus: 'sent',
      msgSendTime: now,
      fileUrl: fileUrl || null,
      fileName: fileName || null,
      fileSize: fileSize || null
    }

    // Persist via REST API
    try {
      const res = await axios.post(`${API_BASE}/group-msg/send`, {
        groupId, msgType: msgType || 1, content,
        fileUrl, fileName, fileSize
      }, apiHeaders(token))
      if (res.data?.data?.id) {
        msg.id = res.data.data.id
      }
    } catch (e) {
      console.error(`[group-msg] API persist failed:`, e.message)
    }

    // Confirm sent
    socket.emit('message-status', {
      messageId: tempId || msgId,
      status: 'sent',
      conversationId: `g_${groupId}`,
      persistedId: msg.id
    })

    // Get group members from API
    try {
      const res = await axios.get(`${API_BASE}/group/members`, {
        params: { groupId },
        ...apiHeaders(token)
      })
      const members = res.data?.data || []
      for (const member of members) {
        const memberId = member.userId || member.id
        if (memberId === uid) continue // skip sender

        const memberSockets = onlineUsers.get(Number(memberId))
        if (memberSockets && memberSockets.size > 0) {
          for (const sid of memberSockets) {
            io.to(sid).emit('group-message', msg)
          }
        } else {
          storeOfflineMessage(Number(memberId), msg)
        }
      }
    } catch (e) {
      // Fallback: broadcast to all connections
      // (in production, track group membership in-memory)
      console.error(`[group-msg] member fetch failed, broadcasting:`, e.message)
      socket.broadcast.emit('group-message', msg)
    }
  })

  // ========== Read receipt ==========
  socket.on('mark-read', (data) => {
    const { fromUserId } = data
    // Notify the sender that messages were read
    const senderSockets = onlineUsers.get(Number(fromUserId))
    if (senderSockets) {
      for (const sid of senderSockets) {
        io.to(sid).emit('message-status', {
          fromUserId: uid,
          status: 'read',
          conversationId: `p_${uid}`
        })
      }
    }

    // Also call REST API
    axios.post(`${API_BASE}/private-msg/mark-read`, {
      fromUserId
    }, apiHeaders(token)).catch(() => {})
  })

  // ========== Status change ==========
  socket.on('status-change', (data) => {
    const { status } = data
    io.emit('user-status-change', { userId: uid, status })
  })

  // ========== Typing indicator (optional) ==========
  socket.on('typing-start', (data) => {
    const { toUserId, groupId } = data
    if (toUserId) {
      const target = onlineUsers.get(Number(toUserId))
      if (target) {
        for (const sid of target) {
          io.to(sid).emit('typing-start', { userId: uid })
        }
      }
    }
  })

  socket.on('typing-stop', (data) => {
    const { toUserId, groupId } = data
    if (toUserId) {
      const target = onlineUsers.get(Number(toUserId))
      if (target) {
        for (const sid of target) {
          io.to(sid).emit('typing-stop', { userId: uid })
        }
      }
    }
  })

  // ========== Disconnect ==========
  socket.on('disconnect', () => {
    const user = socketToUser.get(socket.id)
    if (user) {
      const sockets = onlineUsers.get(user.userId)
      if (sockets) {
        sockets.delete(socket.id)
        if (sockets.size === 0) {
          onlineUsers.delete(user.userId)
          // Broadcast offline after a short delay (in case of reconnection)
          setTimeout(() => {
            if (!onlineUsers.has(user.userId)) {
              io.emit('user-status-change', { userId: user.userId, status: 'offline' })
            }
          }, 5000)
        }
      }
      console.log(`[disconnect] user=${user.userId} socket=${socket.id} (online: ${onlineUsers.size} users)`)
    }
    socketToUser.delete(socket.id)
  })
})

httpServer.listen(PORT, () => {
  console.log(`αchat Socket.io server running on http://localhost:${PORT}`)
  console.log(`API proxy target: ${API_BASE}`)
})
