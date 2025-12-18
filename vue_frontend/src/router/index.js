import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import UserView from '../views/UserView.vue'

// 检查是否已登录的函数
const isAuthenticated = () => {
  const accessToken = localStorage.getItem('auth_token')
  return accessToken !== null && accessToken !== ''
}

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterView
  },
  {
    path: '/user',
    name: 'User',
    component: UserView,
    meta: { requiresAuth: true } // 添加路由元信息，表示需要认证
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/HomeView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL || '/'),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // 检查是否已登录
    if (!isAuthenticated()) {
      // 未登录，重定向到登录页面
      next({
        path: '/login',
        query: { redirect: to.fullPath } // 保存想要访问的页面，登录后可以跳转回该页面
      })
    } else {
      // 已登录，继续访问
      next()
    }
  } else {
    // 不需要认证的页面，直接访问
    next()
  }
})

export default router
