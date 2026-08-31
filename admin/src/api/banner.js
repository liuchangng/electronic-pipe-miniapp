import request from '../utils/request'

// 获取Banner列表
export const getBannerList = (params) => request.get('/api/admin/banner/list', { params })

// 新增Banner
export const addBanner = (data) => request.post('/api/admin/banner/add', data)

// 更新Banner
export const updateBanner = (data) => request.post('/api/admin/banner/update', data)

// 删除Banner
export const deleteBanner = (id) => request.post(`/api/admin/banner/delete/${id}`)