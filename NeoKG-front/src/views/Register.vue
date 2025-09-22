<template>
  <div class="register-card" :style="{ 
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

    <!-- 注册表单 -->
    <div class="form-section">
      <a-form
        ref="formRef"
        :model="registerForm"
        :rules="rules"
        layout="vertical"
        @finish="handleRegister"
      >
        <a-form-item label="用户名" name="username">
          <a-input 
            v-model:value="registerForm.username"
            size="large"
            placeholder="请输入用户名"
            :style="inputStyle"
          >
            <template #prefix>
              <user-outlined :style="{ color: themeStyles.textSecondary }" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item label="昵称" name="nickname">
          <a-input 
            v-model:value="registerForm.nickname"
            size="large"
            placeholder="请输入昵称（可选）"
            :style="inputStyle"
          >
            <template #prefix>
              <smile-outlined :style="{ color: themeStyles.textSecondary }" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item label="密码" name="password">
          <a-input-password 
            v-model:value="registerForm.password"
            size="large"
            placeholder="请输入密码"
            :style="inputStyle"
          >
            <template #prefix>
              <lock-outlined :style="{ color: themeStyles.textSecondary }" />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item label="确认密码" name="confirmPassword">
          <a-input-password 
            v-model:value="registerForm.confirmPassword"
            size="large"
            placeholder="请再次输入密码"
            :style="inputStyle"
          >
            <template #prefix>
              <lock-outlined :style="{ color: themeStyles.textSecondary }" />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item>
          <a-checkbox v-model:checked="registerForm.agree">
            我已阅读并同意
            <a href="#" :style="{ color: themeStyles.primaryColor }">用户协议</a>
            和
            <a href="#" :style="{ color: themeStyles.primaryColor }">隐私政策</a>
          </a-checkbox>
        </a-form-item>

        <a-form-item>
          <a-button 
            type="primary" 
            size="large" 
            block 
            :loading="loading"
            html-type="submit"
            class="register-button"
          >
            注册
          </a-button>
        </a-form-item>

        <div class="login-link">
          <span :style="{ color: themeStyles.textSecondary }">已有账号？</span>
          <a @click="goToLogin" :style="{ color: themeStyles.primaryColor }">
            立即登录
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
  LockOutlined,
  SmileOutlined
} from '@ant-design/icons-vue'
import { isDarkMode } from '@/stores/theme'
import { authService, type RegisterRequest } from '@/services/auth'

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
const registerForm = ref({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  agree: false
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  nickname: [
    { max: 20, message: '昵称不能超过20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string) => {
        if (value !== registerForm.value.password) {
          return Promise.reject('两次输入的密码不一致')
        }
        return Promise.resolve()
      },
      trigger: 'blur'
    }
  ]
}

// 注册处理
const handleRegister = async () => {
  if (!registerForm.value.agree) {
    message.error('请先同意用户协议和隐私政策')
    return
  }

  try {
    loading.value = true
    
    const registerData: RegisterRequest = {
      username: registerForm.value.username,
      email: '', // 不再需要邮箱
      nickname: registerForm.value.nickname,
      password: registerForm.value.password,
      confirmPassword: registerForm.value.confirmPassword,
      rememberMe: false
    }
    
    const result = await authService.register(registerData)
    
    if (result.code === 'SUCCESS') {
      message.success('注册成功！请使用您的账号登录')
      router.push('/auth/login')
    } else {
      message.error(result.message || '注册失败')
    }
  } catch (error: any) {
    console.error('注册失败:', error)
    if (error.response) {
      const errorMsg = error.response.data?.message || error.response.statusText || '服务器错误'
      message.error(errorMsg)
    } else if (error.message?.includes('Network Error')) {
      message.error('无法连接到服务器，请检查网络连接')
    } else {
      message.error(error.message || '注册失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

// 跳转到登录页面
const goToLogin = () => {
  router.push('/auth/login')
}
</script>

<style scoped>
.register-card {
  width: 400px;
  min-height: auto;
  max-height: 90vh;
  padding: 32px;
  border-radius: 12px;
  border: 1px solid;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  overflow: visible;
}

.logo-section {
  text-align: center;
  margin-bottom: 24px;
}

.logo h1 {
  font-size: 28px;
  font-weight: bold;
  margin: 0;
  margin-bottom: 6px;
}

.logo p {
  margin: 0;
  font-size: 14px;
}

.form-section {
  width: 100%;
}

.register-button {
  height: 44px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
}

.login-link {
  text-align: center;
  margin-top: 20px;
}

.login-link a {
  text-decoration: none;
  margin-left: 4px;
  cursor: pointer;
}

.login-link a:hover {
  text-decoration: underline;
}

/* 减少表单项间距 */
:deep(.ant-form-item) {
  margin-bottom: 16px !important;
}

:deep(.ant-form-item:last-child) {
  margin-bottom: 0 !important;
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
  .register-card {
    width: 320px;
    padding: 24px;
  }
}
</style>