import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import App from './App.vue'

// 路由配置
const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('./views/Dashboard.vue')
  },
  {
    path: '/query',
    name: 'Query',
    component: () => import('./views/Query.vue')
  },
  {
    path: '/upload',
    name: 'Upload',
    component: () => import('./views/Upload.vue')
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('./views/Settings.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const app = createApp(App)
app.use(Antd)
app.use(router)
app.mount('#app')
