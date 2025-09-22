<template>
  <div class="login-card" :style="{ 
    backgroundColor: themeStyles.cardBg,
    borderColor: themeStyles.borderColor
  }">
    <!-- Logo 区域 -->
    <div class="logo-section">
      <div class="logo">
        <h1 :style="{ color: themeStyles.primaryColor }">NeoKG</h1>
        <p :style="{ color: themeStyles.textSecondary }">知识图谱管理平台</p>
      </div>
    </div>

    <!-- 登录表单 -->
    <div class="form-section">
      
      <a-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        layout="vertical"
        @finish="handleLogin"
      >
        <a-form-item label="用户名" name="username">
          <a-input 
            v-model:value="loginForm.username"
            size="large"
            placeholder="请输入用户名"
            :style="inputStyle"
          >
            <template #prefix>
              <user-outlined :style="{ color: themeStyles.textSecondary }" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item label="密码" name="password">
          <a-input-password 
            v-model:value="loginForm.password"
            size="large"
            placeholder="请输入密码"
            :style="inputStyle"
          >
            <template #prefix>
              <lock-outlined :style="{ color: themeStyles.textSecondary }" />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item>
          <div class="form-options">
            <a-checkbox v-model:checked="loginForm.remember">
              记住我
            </a-checkbox>
            <a href="#" :style="{ color: themeStyles.primaryColor }">
              忘记密码？
            </a>
          </div>
        </a-form-item>

        <a-form-item>
          <a-button 
            type="primary" 
            size="large" 
            block 
            :loading="loading"
            html-type="submit"
            class="login-button"
          >
            登录
          </a-button>
        </a-form-item>

        <div class="register-link">
          <span :style="{ color: themeStyles.textSecondary }">还没有账号？</span>
          <a @click="goToRegister" :style="{ color: themeStyles.primaryColor }">
            立即注册
          </a>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  UserOutlined,
  LockOutlined
} from '@ant-design/icons-vue'
import { isDarkMode } from '@/stores/theme'
import { login, type UserInfo } from '@/stores/user'
import { authService, type LoginRequest } from '@/services/auth'

const router = useRouter()

// 主题样式
const themeStyles = computed(() => {
  if (isDarkMode.value) {
    return {
      cardBg: '#1f1f1f',
      textColor: '#ffffff',
      textSecondary: '#999999',
      borderColor: '#434343',
      primaryColor: '#1890ff',
      inputBg: '#262626'
    }
  } else {
    return {
      cardBg: '#ffffff',
      textColor: '#000000',
      textSecondary: '#666666',
      borderColor: '#d9d9d9',
      primaryColor: '#1890ff',
      inputBg: '#ffffff'
    }
  }
})

const inputStyle = computed(() => ({
  backgroundColor: themeStyles.value.inputBg,
  borderColor: themeStyles.value.borderColor,
  color: themeStyles.value.textColor
}))

// 表单数据
const formRef = ref()
const loading = ref(false)
const loginForm = ref({
  username: '',
  password: '',
  remember: false
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ]
}

// 登录处理
const handleLogin = async () => {
  try {
    loading.value = true
    
    const loginData: LoginRequest = {
      username: loginForm.value.username,
      password: loginForm.value.password,
      rememberMe: loginForm.value.remember
    }
    
    console.log('发送登录请求:', loginData) // 调试用
    
    const result = await authService.login(loginData)
    
    console.log('登录响应:', result) // 调试用
    
    if (result.code === 'SUCCESS') {
      let userInfo: UserInfo
      let token: string
      
      // 处理后端返回的数据结构
      if (result.data && typeof result.data === 'object' && result.data.user && result.data.token) {
        // 标准格式：包含user对象和token
        userInfo = {
          id: result.data.user.id || '',
          username: result.data.user.username,
          email: result.data.user.email || '',
          nickname: result.data.user.nickname,
          avatar: result.data.user.avatar
        }
        token = result.data.token
      } else if (typeof result.data === 'string') {
        // 只返回token字符串的格式
        token = result.data
        userInfo = {
          id: '',
          username: loginForm.value.username,
          email: '',
          nickname: loginForm.value.username,
          avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${loginForm.value.username}`
        }
      } else {
        // 其他未知格式，使用基本信息
        token = result.data?.token || result.data || 'unknown-token'
        userInfo = {
          id: '',
          username: loginForm.value.username,
          email: '',
          nickname: loginForm.value.username,
          avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${loginForm.value.username}`
        }
      }
      
      console.log('准备登录用户:', userInfo, '令牌:', token) // 调试用
      
      // 执行登录
      login(userInfo, token)
      
      console.log('登录状态更新完成') // 调试用
      
      message.success('登录成功')
      
      // 跳转到主页面
      console.log('准备跳转到dashboard') // 调试用
      await router.push('/dashboard')
      
    } else {
      console.log('登录失败，code不是SUCCESS:', result.code) // 调试用
      message.error(result.message || '登录失败')
    }
  } catch (error: any) {
    console.error('登录错误详情:', error) // 详细错误日志
    
    // 显示具体的错误信息
    if (error && typeof error === 'object') {
      if (error.response) {
        // 后端返回的错误
        const errorMsg = error.response.data?.message || error.response.statusText || '服务器错误'
        console.error('后端错误:', errorMsg)
        message.error(errorMsg)
      } else if (error.message) {
        // 网络错误或其他错误
        console.error('网络或其他错误:', error.message)
        if (error.message.includes('Network Error') || error.code === 'ERR_NETWORK') {
          message.error('无法连接到服务器，请检查网络连接')
        } else {
          message.error(error.message)
        }
      } else if (typeof error === 'string') {
        console.error('字符串错误:', error)
        message.error(error)
      } else {
        console.error('未知错误格式:', error)
        message.error('登录失败，请重试')
      }
    } else {
      console.error('非对象错误:', error)
      message.error('登录失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/auth/register')
}
</script>

<style scoped>
.login-card {
  width: 400px;
  padding: 48px 40px;
  border-radius: 12px;
  border: 1px solid;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.logo-section {
  text-align: center;
  margin-bottom: 32px;
}

.logo h1 {
  font-size: 32px;
  font-weight: bold;
  margin: 0;
  margin-bottom: 8px;
}

.logo p {
  margin: 0;
  font-size: 14px;
}

.form-section {
  width: 100%;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.login-button {
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
}

.register-link {
  text-align: center;
  margin-top: 24px;
}

.register-link a {
  text-decoration: none;
  margin-left: 4px;
  cursor: pointer;
}

.register-link a:hover {
  text-decoration: underline;
}

/* 深度样式覆盖 */
:deep(.ant-form-item-label > label) {
  color: v-bind('themeStyles.textColor') !important;
  font-weight: 500;
}

:deep(.ant-input) {
  background: v-bind('themeStyles.inputBg') !important;
  border-color: v-bind('themeStyles.borderColor') !important;
  color: v-bind('themeStyles.textColor') !important;
}

:deep(.ant-input:focus) {
  border-color: v-bind('themeStyles.primaryColor') !important;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2) !important;
}

:deep(.ant-input::placeholder) {
  color: v-bind('themeStyles.textSecondary') !important;
}

:deep(.ant-checkbox-wrapper) {
  color: v-bind('themeStyles.textColor') !important;
}

@media (max-width: 480px) {
  .login-card {
    width: 320px;
    padding: 32px 24px;
  }
}
</style>