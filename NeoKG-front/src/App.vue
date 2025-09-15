<script setup>
import { ref, computed } from 'vue'
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

const router = useRouter()
const route = useRoute()

const collapsed = ref(true) // 默认收起状态
const darkTheme = ref(false)

// 根据当前路由设置选中的菜单项
const selectedKeys = computed(() => {
  const routeMap = {
    '/query': ['1'],
    '/dashboard': ['2'],
    '/upload': ['3'],
    '/settings': ['settings']
  }
  return routeMap[route.path] || ['2']
})

const toggleCollapsed = () => {
  collapsed.value = !collapsed.value
}

const toggleTheme = () => {
  darkTheme.value = !darkTheme.value
}

const handleSignOut = () => {
  console.log('用户登出')
}

const handleMenuClick = (key) => {
  const routeMap = {
    '1': '/query',
    '2': '/dashboard',
    '3': '/upload',
    'settings': '/settings'
  }
  if (routeMap[key]) {
    router.push(routeMap[key])
  }
}

// 面包屑导航
const breadcrumbs = computed(() => {
  const breadcrumbMap = {
    '/dashboard': [{ title: '首页' }, { title: '仪表板' }],
    '/query': [{ title: '首页' }, { title: 'Query' }],
    '/upload': [{ title: '首页' }, { title: '上传文件' }],
    '/settings': [{ title: '首页' }, { title: '设置' }]
  }
  return breadcrumbMap[route.path] || [{ title: '首页' }]
})
</script>

<template>
  <a-layout style="min-height: 100vh; background: #fff;">
    <!-- 侧边栏 -->
    <a-layout-sider 
      v-model:collapsed="collapsed" 
      :trigger="null" 
      collapsible
      :width="240"
      :collapsed-width="80"
      style="background: #fff; border-right: 1px solid #f0f0f0; position: fixed; left: 0; top: 0; bottom: 0; height: 100vh; z-index: 1000;"
    >
      <div class="sider-content">
        <!-- Logo区域 -->
        <div class="logo">
          <h2 v-if="!collapsed" style="color: #1890ff; text-align: center; margin: 16px 0; font-weight: bold;">
            NeoKG
          </h2>
          <h2 v-else style="color: #1890ff; text-align: center; margin: 16px 0; font-weight: bold;">
            NK
          </h2>
        </div>

        <!-- 主要功能菜单 -->
        <div class="main-menu">
          <a-menu
            :selectedKeys="selectedKeys"
            theme="light"
            mode="inline"
            :inline-collapsed="collapsed"
            style="border-right: none;"
            @click="({ key }) => handleMenuClick(key)"
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
          <a-divider style="margin: 16px 12px;" />

          <!-- 底部功能菜单 -->
          <div class="bottom-menu">
            <a-menu
              theme="light"
              mode="inline"
              :inline-collapsed="collapsed"
              style="border-right: none;"
              @click="({ key }) => handleMenuClick(key)"
            >
              <a-menu-item key="settings" class="menu-item">
                <setting-outlined />
                <span>设置</span>
              </a-menu-item>
              <a-menu-item key="theme" class="menu-item" @click="toggleTheme">
                <bulb-outlined />
                <span>{{ darkTheme ? 'Light Theme' : 'Dark Theme' }}</span>
              </a-menu-item>
              <a-menu-item key="logout" class="menu-item" @click="handleSignOut">
                <logout-outlined />
                <span>Sign Out</span>
              </a-menu-item>
            </a-menu>
          </div>

          <!-- 展开/收起按钮 - 与菜单项完全一致的样式 -->
          <div class="collapse-menu-item menu-item" @click="toggleCollapsed">
            <right-outlined v-if="collapsed" />
            <left-outlined v-else />
          </div>
        </div>
      </div>
    </a-layout-sider>

    <!-- 主体内容区域 -->
    <a-layout :style="{ marginLeft: collapsed ? '80px' : '240px', transition: 'margin-left 0.2s' }">
      <!-- 头部 - 固定位置 -->
      <a-layout-header 
        style="
          background: #fff; 
          padding: 0; 
          box-shadow: 0 1px 4px rgba(0,21,41,.08);
          position: fixed;
          top: 0;
          right: 0;
          z-index: 999;
        "
        :style="{ 
          left: collapsed ? '80px' : '240px',
          width: collapsed ? 'calc(100% - 80px)' : 'calc(100% - 240px)',
          transition: 'left 0.2s, width 0.2s'
        }"
      >
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 0 24px; height: 64px;">
          <div style="color: #666; font-size: 16px; font-weight: 500;">
            欢迎使用 NeoKG 管理平台
          </div>
          
          <div style="display: flex; align-items: center;">
            <a-badge :count="5" style="margin-right: 20px;">
              <a-button type="text" shape="circle">
                <template #icon><user-outlined /></template>
              </a-button>
            </a-badge>
            <a-dropdown>
              <a-button type="text">
                <user-outlined />
                管理员
              </a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item key="profile">个人资料</a-menu-item>
                  <a-menu-item key="settings">设置</a-menu-item>
                  <a-menu-divider />
                  <a-menu-item key="logout">退出登录</a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
        </div>
      </a-layout-header>

      <!-- 内容区域 -->
      <a-layout-content 
        style="
          margin-top: 64px; 
          padding: 24px 16px; 
          background: #f5f5f5; 
          min-height: calc(100vh - 64px - 70px);
        "
      >
        <!-- 面包屑 -->
        <a-breadcrumb style="margin-bottom: 16px; padding: 0 8px;">
          <a-breadcrumb-item v-for="item in breadcrumbs" :key="item.title">
            {{ item.title }}
          </a-breadcrumb-item>
        </a-breadcrumb>

        <!-- 路由视图容器 -->
        <div style="background: #fff; padding: 24px; border-radius: 6px; box-shadow: 0 1px 3px rgba(0,0,0,0.1);">
          <router-view />
        </div>
      </a-layout-content>

      <!-- 底部 -->
      <a-layout-footer 
        style="
          text-align: center; 
          background: #fff; 
          border-top: 1px solid #f0f0f0;
          padding: 24px 16px;
        "
      >
        NeoKG Admin ©2024 Created by Your Team
      </a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<style scoped>
/* 侧边栏样式 */
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
  border-bottom: 1px solid #f0f0f0;
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
  background: #fff;
}

.bottom-menu {
  padding-bottom: 10px;
}

/* 统一菜单项样式 */
.menu-item {
  margin: 4px 8px !important;
  border-radius: 6px !important;
  height: 40px !important;
  line-height: 40px !important;
}

.menu-item:hover {
  background-color: #f5f5f5 !important;
}

.menu-item.ant-menu-item-selected {
  background-color: #e6f7ff !important;
  color: #1890ff !important;
}

.menu-item.ant-menu-item-selected::after {
  display: none;
}

/* 展开收起按钮样式 - 完全模拟菜单项 */
.collapse-menu-item {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  cursor: pointer;
  transition: all 0.3s;
  background: transparent !important;
  border: none !important;
  color: rgba(0, 0, 0, 0.85);
  padding: 0 24px;
  margin-bottom: 16px !important;
  font-size: 14px;
}

.collapse-menu-item:hover {
  background-color: #f5f5f5 !important;
  color: #1890ff;
}

/* 全局样式重置 */
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

/* 头部固定时的滚动条处理 */
:deep(.ant-layout-content) {
  overflow-y: auto;
}

/* 确保菜单项图标对齐 */
:deep(.ant-menu-item) {
  padding-left: 24px !important;
}

:deep(.ant-menu-inline-collapsed .ant-menu-item) {
  padding-left: 32px !important;
  text-align: left !important;
}
</style>
