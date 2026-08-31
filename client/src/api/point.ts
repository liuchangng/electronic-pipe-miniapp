/**
 * 积分相关 API
 */
import { get, post } from '@/utils/request'

// 获取积分信息
export const getPointInfo = () => {
  return get('/api/point/info')
}

// 每日签到
export const checkIn = () => {
  return post('/api/point/checkin')
}

// 获取积分流水
export const getPointLogs = (page = 1, size = 20) => {
  return get(`/api/point/logs?page=${page}&size=${size}`)
}