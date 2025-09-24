<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  UserOutlined,
  SearchOutlined,
  DashboardOutlined,
  UploadOutlined,
  BulbOutlined,
  LogoutOutlined,
  RightOutlined,
  LeftOutlined,
  TagsOutlined,
  FileTextOutlined,
  RobotOutlined
} from '@ant-design/icons-vue'
import { isDarkMode, toggleDarkMode, initTheme } from './stores/theme'
import { message } from 'ant-design-vue'

// 直接导入而不是使用动态导入
import { 
  isLoggedIn, 
  userInfo, 
  userDisplayName, 
  userAvatar, 
  userAvatarSeed, 
  logout, 
  initUserState, 
  updateAvatarSeed 
} from './stores/user'

import DicebearAvatar from './components/DicebearAvatar.vue'

// 认证服务导入
let authService = { logout: () => Promise.resolve() }
try {
  import('./services/auth').then(auth => {
    authService = auth.authService
  })
} catch (error) {
  console.error('认证服务导入失败:', error)
}

const router = useRouter()
const route = useRoute()

const collapsed = ref(true)

// 判断是否显示主应用布局（不在认证页面时显示）
const showMainLayout = computed(() => {
  return !route.path.startsWith('/auth')
})

// 根据当前路由设置选中的菜单项
const selectedKeys = computed(() => {
  const routeMap: Record<string, string[]> = {
    '/ai-chat': ['1'],
    '/query': ['2'],
    '/dashboard': ['3'],
    '/upload': ['4'],
    '/keywords': ['5'],
    '/documents': ['6']
  }
  return routeMap[route.path] || ['3']
})

// 计算主题相关的样式
const themeStyles = computed(() => {
  if (isDarkMode.value) {
    return {
      layoutBg: '#141414',
      siderBg: '#001529',
      headerBg: '#001529',
      contentBg: '#1f1f1f',
      footerBg: '#001529',
      textColor: '#ffffff',
      borderColor: '#303030',
      menuTheme: 'dark' as const
    }
  } else {
    return {
      layoutBg: '#ffffff',
      siderBg: '#ffffff',
      headerBg: '#ffffff',
      contentBg: '#ffffff',
      footerBg: '#ffffff',
      textColor: '#666',
      borderColor: '#f0f0f0',
      menuTheme: 'light' as const
    }
  }
})

// 判断是否是全屏页面
const isFullHeightPage = computed(() => {
  return route.path === '/query' || route.path === '/ai-chat'
})

const toggleCollapsed = (): void => {
  collapsed.value = !collapsed.value
}

const handleMenuClick = (key: string): void => {
  if (key === 'theme') {
    toggleDarkMode()
    return
  }
  
  if (key === 'logout') {
    handleLogout()
    return
  }
  
  const routeMap: Record<string, string> = {
    '1': '/ai-chat',
    '2': '/query',
    '3': '/dashboard',
    '4': '/upload',
    '5': '/keywords',
    '6': '/documents'
  }
  if (routeMap[key]) {
    router.push(routeMap[key])
  }
}

// 处理头像点击 - 更换头像
const handleAvatarClick = (newSeed: string) => {
  updateAvatarSeed(newSeed)
  message.success('头像已更换！')
}

// 登出处理
const handleLogout = async () => {
  try {
    await authService.logout()
  } catch (error) {
    console.error('登出接口调用失败:', error)
  } finally {
    logout()
    message.success('已退出登录')
    router.push('/auth/login')
  }
}

onMounted(() => {
  initTheme()
  initUserState()
})
</script>

<template>
  <!-- 认证页面：使用 AuthLayout -->
  <router-view v-if="!showMainLayout" />
  
  <!-- 主应用：使用原来的布局 -->
  <a-layout v-else :style="{ minHeight: '100vh', background: themeStyles.layoutBg }">
    <!-- 侧边栏 -->
    <a-layout-sider 
      v-model:collapsed="collapsed" 
      :trigger="null" 
      collapsible
      :width="240"
      :collapsed-width="80"
      :style="{
        background: themeStyles.siderBg,
        borderRight: `1px solid ${themeStyles.borderColor}`,
        position: 'fixed',
        left: 0,
        top: 0,
        bottom: 0,
        height: '100vh',
        zIndex: 1000
      }"
    >
      <div class="sider-content">
        <!-- Logo区域 -->
        <div class="logo" :style="{ borderBottom: `1px solid ${themeStyles.borderColor}` }">
          <h2 v-if="!collapsed" :style="{ color: isDarkMode ? '#ffffff' : '#1890ff', textAlign: 'center', margin: '16px 0', fontWeight: 'bold' }">
            NeoKG
          </h2>
          <h2 v-else :style="{ color: isDarkMode ? '#ffffff' : '#1890ff', textAlign: 'center', margin: '16px 0', fontWeight: 'bold' }">
            NK
          </h2>
        </div>

        <!-- 主要功能菜单 -->
        <div class="main-menu">
          <a-menu
            :selectedKeys="selectedKeys"
            :theme="themeStyles.menuTheme"
            mode="inline"
            :inline-collapsed="collapsed"
            style="border-right: none;"
            @click="(e: { key: string }) => handleMenuClick(e.key)"
          >
            <!-- AI问答 -->
            <a-menu-item key="1" class="menu-item">
              <robot-outlined />
              <span>AI问答</span>
            </a-menu-item>
            <!-- 可视化图谱 -->
            <a-menu-item key="2" class="menu-item">
              <search-outlined />
              <span>可视化图谱</span>
            </a-menu-item>
            <!-- 数据面板 -->
            <a-menu-item key="3" class="menu-item">
              <dashboard-outlined />
              <span>数据面板</span>
            </a-menu-item>
            <!-- 上传文件 -->
            <a-menu-item key="4" class="menu-item">
              <upload-outlined />
              <span>上传文件</span>
            </a-menu-item>
            <!-- 关键词管理 -->
            <a-menu-item key="5" class="menu-item">
              <tags-outlined />
              <span>关键词管理</span>
            </a-menu-item>
            <!-- 文档管理 -->
            <a-menu-item key="6" class="menu-item">
              <file-text-outlined />
              <span>文档管理</span>
            </a-menu-item>
          </a-menu>
        </div>

        <!-- 底部区域 -->
        <div class="bottom-section">
          <a-divider :style="{ margin: '16px 12px', borderColor: themeStyles.borderColor }" />

          <!-- 底部功能菜单 -->
          <div class="bottom-menu">
            <a-menu
              :theme="themeStyles.menuTheme"
              mode="inline"
              :inline-collapsed="collapsed"
              style="border-right: none;"
              @click="(e: { key: string }) => handleMenuClick(e.key)"
            >
              <a-menu-item key="theme" class="menu-item">
                <bulb-outlined />
                <span>{{ isDarkMode ? '浅色主题' : '深色主题' }}</span>
              </a-menu-item>
              <!-- 登出按钮 -->
              <a-menu-item key="logout" class="menu-item logout-item">
                <logout-outlined />
                <span>退出登录</span>
              </a-menu-item>
            </a-menu>
          </div>

          <!-- 展开/收起按钮 -->
          <div 
            class="collapse-menu-item menu-item" 
            @click="toggleCollapsed"
            :style="{ color: isDarkMode ? '#ffffff' : 'rgba(0, 0, 0, 0.85)' }"
          >
            <right-outlined v-if="collapsed" />
            <left-outlined v-else />
          </div>
        </div>
      </div>
    </a-layout-sider>

    <!-- 主体内容区域 -->
    <a-layout :style="{ marginLeft: collapsed ? '80px' : '240px', transition: 'margin-left 0.2s' }">
      <!-- 头部 -->
      <a-layout-header 
        :style="{
          background: themeStyles.headerBg,
          padding: 0,
          boxShadow: isDarkMode ? '0 1px 4px rgba(0,0,0,.3)' : '0 1px 4px rgba(0,21,41,.08)',
          position: 'fixed',
          top: 0,
          right: 0,
          zIndex: 999,
          left: collapsed ? '80px' : '240px',
          width: collapsed ? 'calc(100% - 80px)' : 'calc(100% - 240px)',
          transition: 'left 0.2s, width 0.2s'
        }"
      >
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 0 24px; height: 64px;">
          <div :style="{ color: themeStyles.textColor, fontSize: '16px', fontWeight: '500' }">
            欢迎使用 NeoKG 管理平台
          </div>
          
          <!-- 用户信息 -->
          <div class="user-info" :style="{ display: 'flex', alignItems: 'center', gap: '12px' }">
            <span :style="{ color: themeStyles.textColor, fontSize: '14px' }">
              {{ userDisplayName }}
            </span>
            
            <!-- 使用 Dicebear 头像组件 -->
            <a-tooltip title="点击更换头像" placement="bottomRight">
              <dicebear-avatar
                v-if="userAvatarSeed"
                :seed="userAvatarSeed"
                :size="36"
                :avatar-style="'adventurer'"
                :clickable="true"
                shadow="0 2px 8px rgba(0, 0, 0, 0.15)"
                @click="handleAvatarClick"
              />
              <!-- 兜底头像 -->
              <a-avatar 
                v-else
                :size="36" 
                :src="userAvatar" 
                :style="{ backgroundColor: '#1890ff' }"
              >
                <template #icon>
                  <user-outlined />
                </template>
              </a-avatar>
            </a-tooltip>
          </div>
        </div>
      </a-layout-header>

      <!-- 内容区域 -->
      <a-layout-content 
        :style="{
          marginTop: '64px',
          padding: isFullHeightPage ? 0 : '24px 16px',
          background: themeStyles.contentBg,
          minHeight: isFullHeightPage ? 'calc(100vh - 64px)' : 'calc(100vh - 64px - 70px)',
          height: isFullHeightPage ? 'calc(100vh - 64px)' : 'auto',
          overflow: isFullHeightPage ? 'hidden' : 'visible'
        }"
      >
        <div :style="{ 
          background: themeStyles.contentBg, 
          padding: 0, 
          borderRadius: 0, 
          boxShadow: 'none',
          height: isFullHeightPage ? '100%' : 'auto'
        }">
          <router-view />
        </div>
      </a-layout-content>

      <!-- 底部 -->
      <a-layout-footer 
        v-if="!isFullHeightPage"
        :style="{
          textAlign: 'center',
          background: themeStyles.footerBg,
          borderTop: `1px solid ${themeStyles.borderColor}`,
          padding: '24px 16px',
          color: themeStyles.textColor
        }"
      >
        NeoKG Admin ©2025 Created by NeoKG Team
      </a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<style scoped>
.sider-content {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.main-menu {
  flex: 1;
  padding-top: 20px;
  overflow-y: auto;
  overflow-x: hidden;
}

.bottom-section {
  flex-shrink: 0;
}

.bottom-menu {
  padding-bottom: 10px;
}

.menu-item {
  margin: 4px 8px !important;
  border-radius: 6px !important;
  height: 40px !important;
  line-height: 40px !important;
}

.logout-item {
  color: #ff4d4f !important;
}

.collapse-menu-item {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  cursor: pointer;
  transition: all 0.3s;
  background: transparent !important;
  border: none !important;
  padding: 0 24px;
  margin-bottom: 16px !important;
  font-size: 14px;
}

.collapse-menu-item:hover {
  opacity: 0.7;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

:deep(.ant-layout-sider-children) {
  height: 100vh;
  overflow: hidden;
}

:deep(.ant-menu-inline) {
  border-right: none !important;
}

:deep(.ant-menu-item::after) {
  display: none !important;
}

:deep(.ant-layout-content) {
  overflow-y: auto;
}

:deep(.ant-menu-item) {
  padding-left: 24px !important;
}

:deep(.ant-menu-inline-collapsed .ant-menu-item) {
  padding-left: 32px !important;
  text-align: left !important;
}

:deep(.ant-card) {
  box-shadow: none !important;
  border: none !important;
}

:deep(.ant-table-wrapper) {
  box-shadow: none !important;
}

:deep(.ant-form) {
  background: transparent !important;
}

:deep(.logout-item) {
  color: #ff4d4f !important;
}

:deep(.logout-item:hover) {
  background-color: rgba(255, 77, 79, 0.1) !important;
}
</style>
