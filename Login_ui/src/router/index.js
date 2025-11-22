// src/router/index.js

import { createRouter, createWebHistory } from 'vue-router'
import request from '../api/request'
import AuthView from '../views/AuthView.vue'
import MainView from '../views/MainView.vue'
import AdminView from '../views/AdminView.vue'
import { useUserStore } from '@/stores/user' // 导入 Pinia Store

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: AuthView },
  { path: '/register', name: 'Register', component: AuthView },
  { path: '/forgot-password', name: 'ForgotPassword', component: AuthView },
  { path: '/main', name: 'Main', component: MainView, meta: { requiresAuth: true } },
  { path: '/admin', name: 'Admin', component: AdminView, meta: { requiresAuth: true, requiresAdmin: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')

  // 关键：获取 Store 实例
  const userStore = useUserStore()

  if (to.meta.requiresAuth) {
    // 1. 访问需要登录的页面
    if (!token) {
      // 没有 token，重定向到登录页
      return next('/login')
    }

    try {
      // 验证 token 有效性，并获取用户数据
      const res = await request.get('/auth/me')

      // 🚀 核心修改：验证成功，保存用户数据
      userStore.setUserData(res.data)

      // 检查是否需要管理员权限
      if (to.meta.requiresAdmin && !res.data.isAdmin) {
        // 如果需要管理员权限但当前用户不是管理员，重定向到主页
        return next('/main')
      }

      next()  // 验证成功，继续
    } catch (err) {
      // token 无效或过期
      console.error('Token 验证失败:', err)
      localStorage.removeItem('token')
      userStore.clearUserData() // 清除过期数据
      next('/login')
    }
  } else if ((to.path === '/login' || to.path === '/register' || to.path === '/forgot-password') && token) {
    // 2. 已登录用户尝试访问登录/注册页面
    next('/main')
  } else {
    // 3. 访问其他公共页面
    next()
  }
})

export default router