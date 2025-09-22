<template>
  <div class="auth-layout" :style="{ backgroundColor: themeStyles.contentBg }">
    <!-- 认证内容 -->
    <div class="auth-content">
      <router-view />
    </div>

    <!-- 版权信息 -->
    <div class="auth-footer" :style="{ color: themeStyles.textSecondary }">
      <p>NeoKG Admin ©2025 Created by NeoKG Team</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { isDarkMode, initTheme } from '@/stores/theme'

// 主题样式
const themeStyles = computed(() => {
  if (isDarkMode.value) {
    return {
      contentBg: '#0f1419',
      textSecondary: '#999999'
    }
  } else {
    return {
      contentBg: '#f0f2f5',
      textSecondary: '#666666'
    }
  }
})

onMounted(() => {
  initTheme()
})
</script>

<style scoped>
.auth-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  background-image: 
    radial-gradient(circle at 20% 80%, rgba(24, 144, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(82, 196, 26, 0.1) 0%, transparent 50%);
}

.auth-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.auth-footer {
  text-align: center;
  padding: 20px;
  font-size: 14px;
}

@media (max-width: 480px) {
  .auth-content {
    padding: 20px 16px;
  }
}
</style>