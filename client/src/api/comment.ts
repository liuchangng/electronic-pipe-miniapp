/**
 * 评论相关 API
 */
import { get, post } from '@/utils/request'

// 添加评论
export const addComment = (data: { videoId: number; content: string; parentId?: number }) => {
  return post('/api/comment/add', data)
}

// 获取视频评论列表
export const getCommentList = (videoId: number) => {
  return get(`/api/comment/list?videoId=${videoId}`)
}

// 获取视频评论数量
export const getCommentCount = (videoId: number) => {
  return get(`/api/comment/count?videoId=${videoId}`)
}

// 删除自己的评论
export const deleteComment = (commentId: number) => {
  return post('/api/comment/delete', { commentId })
}