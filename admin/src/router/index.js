import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'songs',
        name: 'Songs',
        component: () => import('../views/Songs.vue'),
        meta: { title: '曲谱管理' }
      },
      {
        path: 'videos',
        name: 'Videos',
        component: () => import('../views/Videos.vue'),
        meta: { title: '视频管理' }
      },
      {
        path: 'banners',
        name: 'Banners',
        component: () => import('../views/Banners.vue'),
        meta: { title: 'Banner管理' }
      },
      {
        path: 'knowledge',
        name: 'Knowledge',
        component: () => import('../views/Knowledge.vue'),
        meta: { title: '知识管理' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/Users.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'song-requests',
        name: 'SongRequests',
        component: () => import('../views/SongRequestManage.vue'),
        meta: { title: '求谱管理' }
      },
      {
        path: 'feedbacks',
        name: 'Feedbacks',
        component: () => import('../views/FeedbackManage.vue'),
        meta: { title: '反馈管理' }
      },
      {
        path: 'comments',
        name: 'Comments',
        component: () => import('../views/Comments.vue'),
        meta: { title: '评论管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router