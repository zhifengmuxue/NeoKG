<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  UserOutlined,
  SearchOutlined,
  DashboardOutlined,
  UploadOutlined,
  SettingOutlined,
  BulbOutlined,
  LogoutOutlined,
  RightOutlined,
  LeftOutlined
} from '@ant-design/icons-vue'
import { isDarkMode, toggleDarkMode, initTheme } from './stores/theme'

const router = useRouter()
const route = useRoute()

const collapsed = ref(true) // 默认收起状态

// 根据当前路由设置选中的菜单项
const selectedKeys = computed(() => {
  const routeMap: Record<string, string[]> = {
    '/query': ['1'],
    '/dashboard': ['2'],
    '/upload': ['3'],
    '/settings': ['settings']
  }
  return routeMap[route.path] || ['2']
})

// 计算主题相关的样式
const themeStyles = computed(() => {
  if (isDarkMode.value) {
    return {
      // 深色主题样式
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
      // 浅色主题样式
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

const toggleCollapsed = (): void => {
  collapsed.value = !collapsed.value
}

const handleMenuClick = (key: string): void => {
  if (key === 'theme') {
    console.log('点击主题切换按钮，当前状态:', isDarkMode.value)
    toggleDarkMode()
    console.log('切换后状态:', isDarkMode.value)
    return
  }
  
  const routeMap: Record<string, string> = {
    '1': '/query',
    '2': '/dashboard',
    '3': '/upload',
    'settings': '/settings'
  }
  if (routeMap[key]) {
    router.push(routeMap[key])
  }
}

onMounted(() => {
  initTheme()
  console.log('App.vue mounted, 主题状态:', isDarkMode.value)
})
</script>

<template>
  <a-layout :style="{ minHeight: '100vh', background: themeStyles.layoutBg }">
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
            @click="({ key }) => handleMenuClick(key as string)"
          >
            <a-menu-item key="1" class="menu-item">
              <search-outlined />
              <span>Query</span>
            </a-menu-item>
            <a-menu-item key="2" class="menu-item">
              <dashboard-outlined />
              <span>看板</span>
            </a-menu-item>
            <a-menu-item key="3" class="menu-item">
              <upload-outlined />
              <span>上传文件</span>
            </a-menu-item>
          </a-menu>
        </div>

        <!-- 底部区域 -->
        <div class="bottom-section">
          <!-- 分隔线 -->
          <a-divider :style="{ margin: '16px 12px', borderColor: themeStyles.borderColor }" />

          <!-- 底部功能菜单 -->
          <div class="bottom-menu">
            <a-menu
              :theme="themeStyles.menuTheme"
              mode="inline"
              :inline-collapsed="collapsed"
              style="border-right: none;"
              @click="({ key }) => handleMenuClick(key as string)"
            >
              <a-menu-item key="settings" class="menu-item">
                <setting-outlined />
                <span>设置</span>
              </a-menu-item>
              <a-menu-item key="theme" class="menu-item">
                <bulb-outlined />
                <span>{{ isDarkMode ? '浅色主题' : '深色主题' }}</span>
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
        </div>
      </a-layout-header>

      <!-- 内容区域 -->
      <a-layout-content 
        :style="{
          marginTop: '64px',
          padding: '24px 16px',
          background: themeStyles.contentBg,
          minHeight: 'calc(100vh - 64px - 70px)'
        }"
      >
        <div :style="{ background: themeStyles.contentBg, padding: 0, borderRadius: 0, boxShadow: 'none' }">
          <router-view />
        </div>
      </a-layout-content>

      <!-- 底部 -->
      <a-layout-footer 
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
/* 样式部分保持不变 */
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
</style>
