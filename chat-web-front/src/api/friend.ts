import request from './request'

export const getFriendList = () => request.get('/friendship/list')

export const getFriendCategories = () => request.get('/friendship/categories')

export const getGroupsView = () => request.get('/friendship/groups-view')

export const searchUsers = (keyword: string) => request.get('/user/search', { params: { keyword } })

export const sendFriendRequest = (receiveUserId: number) =>
  request.post('/friend-request/send', { receiveUserId })

export const getPendingRequests = () => request.get('/friend-request/pending')

export const handleFriendRequest = (requestId: number, accept: boolean) =>
  request.post('/friend-request/handle', { requestId, status: accept ? 'accepted' : 'rejected' })

export const deleteFriend = (friendId: number) => request.delete(`/friendship/delete/${friendId}`)
