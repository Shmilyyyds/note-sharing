import service from './request'

// =========================================================
//                     私信 / 会话相关 API
// =========================================================

/**
 * 获取用户的会话列表
 * [后端] GET /api/v1/conversation/list?userId={userId}
 */
export const fetchConversationList = (userId) => {
  const id = Number(userId)
  if (!id || Number.isNaN(id)) {
    return Promise.reject(new Error('无效的用户ID'))
  }
  return service.get('/conversation/list', {
    params: { userId: id }
  }).then(res => res.data.data)
}

/**
 * 获取单个会话详情（包含消息列表）
 * [后端] GET /api/v1/conversation/detail?conversationId={conversationId}
 */
export const fetchConversationDetail = (conversationId) => {
  if (!conversationId) {
    return Promise.reject(new Error('会话ID不能为空'))
  }
  return service.get('/conversation/detail', {
    params: { conversationId }
  }).then(res => res.data.data)
}

/**
 * 获取用户所有会话的未读统计（按会话分组）
 * [后端] GET /api/v1/conversation/unread/all?userId={userId}
 * 返回 Map<Object, Object>，这里前端统一转成普通对象
 */
export const fetchAllUnreadCounts = (userId) => {
  const id = Number(userId)
  if (!id || Number.isNaN(id)) {
    return Promise.reject(new Error('无效的用户ID'))
  }
  return service.get('/conversation/unread/all', {
    params: { userId: id }
  }).then(res => res.data.data || {})
}

/**
 * 获取某个会话的未读数
 * [后端] GET /api/v1/conversation/unread?userId={userId}&conversationId={conversationId}
 */
export const fetchUnreadCount = (userId, conversationId) => {
  const id = Number(userId)
  if (!id || Number.isNaN(id) || !conversationId) {
    return Promise.reject(new Error('参数不合法'))
  }
  return service.get('/conversation/unread', {
    params: { userId: id, conversationId }
  }).then(res => res.data.data ?? 0)
}

/**
 * 获取用户总未读数
 * [后端] GET /api/v1/conversation/unread/total?userId={userId}
 */
export const fetchUnreadTotal = (userId) => {
  const id = Number(userId)
  if (!id || Number.isNaN(id)) {
    return Promise.reject(new Error('无效的用户ID'))
  }
  return service.get('/conversation/unread/total', {
    params: { userId: id }
  }).then(res => res.data.data ?? 0)
}

/**
 * 将会话标记为已读
 * [后端] POST /api/v1/conversation/read?userId={userId}&conversationId={conversationId}
 */
export const markConversationAsRead = (userId, conversationId) => {
  const id = Number(userId)
  if (!id || Number.isNaN(id) || !conversationId) {
    return Promise.reject(new Error('参数不合法'))
  }
  return service.post('/conversation/read', null, {
    params: { userId: id, conversationId }
  }).then(res => res.data)
}

