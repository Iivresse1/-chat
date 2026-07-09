import request from './request'

export const getPrivateHistory = (withUserId: number, page = 1, size = 50) =>
  request.get('/private-msg/history', { params: { withUserId, page, size } })

export const sendPrivateMsg = (toUserId: number, msgType: string, content: string, fileUrl?: string, fileName?: string, fileSize?: number) =>
  request.post('/private-msg/send', { toUserId, msgType, content, fileUrl, fileName, fileSize })

export const markRead = (fromUserId: number) =>
  request.post('/private-msg/mark-read', { fromUserId })

export const unreadCount = (fromUserId: number) =>
  request.get('/private-msg/unread-count', { params: { fromUserId } })

export const getGroupHistory = (groupId: number, page = 1, size = 50) =>
  request.get('/group-msg/history', { params: { groupId, page, size } })

export const sendGroupMsg = (groupId: number, msgType: number, content: string, fileUrl?: string, fileName?: string, fileSize?: number) =>
  request.post('/group-msg/send', { groupId, msgType, content, fileUrl, fileName, fileSize })
