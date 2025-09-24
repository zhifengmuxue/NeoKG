import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn, initUserState } from '@/stores/user'
import AuthLayout from '@/layouts/AuthLayout.vue'
import Dashboard from '../views/Dashboard.vue'
import Upload from '../views/Upload.vue'
import Query from '../views/Query.vue'
import Keywords from '../views/Keywords.vue'
import Documents from '../views/Documents.vue'
import AiChat from '../views/AiChat.vue'
import Settings from '../views/Settings.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'

const routes = [
  {
    path: '/',
    redirect: (to: any) => {
      // 初始化用户状态
      initUserState()
      // 根据登录状态决定重定向目标
      return isLoggedIn.value ? '/dashboard' : '/auth/login'
    }
  },
  // 认证路由组 - 使用独立布局
  {
    path: '/auth',
    component: AuthLayout,
    children: [
      {
        path: '',
        redirect: '/auth/login'
      },
      {
        path: 'login',
        name: 'Login',
        component: Login,
        meta: { requiresGuest: true, title: '登录' }
      },
      {
        path: 'register',
        name: 'Register',
        component: Register,
        meta: { requiresGuest: true, title: '注册' }
      }
    ]
  },
  // 主应用路由组 - 需要认证
  {
    path: '/ai-chat',
    name: 'AiChat',
    component: AiChat,
    meta: { requiresAuth: true, title: 'AI问答' }
  },
  {
    path: '/query',
    name: 'Query',
    component: Query,
    meta: { requiresAuth: true, title: '可视化图谱' }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    meta: { requiresAuth: true, title: '数据面板' }
  },
  {
    path: '/upload',
    name: 'Upload',
    component: Upload,
    meta: { requiresAuth: true, title: '上传文件' }
  },
  {
    path: '/keywords',
    name: 'Keywords',
    component: Keywords,
    meta: { requiresAuth: true, title: '关键词管理' }
  },
  {
    path: '/documents',
    name: 'Documents',
    component: Documents,
    meta: { requiresAuth: true, title: '文档管理' }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: Settings,
    meta: { requiresAuth: true, title: '设置' }
  },
  // 404 页面
  {
    path: '/:pathMatch(.*)*',
    redirect: '/auth/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 增加详细日志
router.beforeEach(async (to, from, next) => {
  console.log('=== 路由守卫开始 ===')
  console.log('目标路径:', to.path)
  console.log('目标路由名称:', to.name)
  console.log('来源路径:', from.path)
  
  // 在路由守卫中初始化用户状态
  initUserState()
  
  console.log('用户登录状态:', isLoggedIn.value)
  console.log('目标路由 meta:', to.meta)
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - NeoKG`
  } else {
    document.title = 'NeoKG - 知识图谱管理平台'
  }
  
  // 如果用户未登录且访问需要认证的页面
  if (to.meta.requiresAuth && !isLoggedIn.value) {
    console.log('❌ 用户未登录，需要重定向到登录页面')
    console.log('正在执行 next("/auth/login")')
    next('/auth/login')
    console.log('next("/auth/login") 已执行')
    return
  }
  
  // 如果用户已登录但访问登录/注册页面
  if (to.meta.requiresGuest && isLoggedIn.value) {
    console.log('✅ 用户已登录，重定向到仪表板')
    next('/dashboard')
    return
  }
  
  console.log('✅ 允许访问:', to.path)
  console.log('=== 路由守卫结束 ===')
  next()
})

export default router
