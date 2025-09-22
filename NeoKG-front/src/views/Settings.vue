<template>
  <div class="settings-page" :style="{ backgroundColor: themeStyles.contentBg }">
    <!-- 页面标题 -->
    <div class="page-header" :style="{ 
      borderBottom: `1px solid ${themeStyles.borderColor}`,
      backgroundColor: themeStyles.cardBg 
    }">
      <h2 :style="{ color: themeStyles.textColor, margin: 0 }">设置</h2>
      <p :style="{ color: themeStyles.textSecondary, margin: '8px 0 0 0' }">
        修改您的密码
      </p>
    </div>

    <!-- 设置内容 -->
    <div class="settings-content">
      <div class="settings-panel" :style="{ 
        backgroundColor: themeStyles.cardBg,
        borderColor: themeStyles.borderColor
      }">
        <!-- 修改密码 -->
        <div class="panel-content">
          <div class="panel-header">
            <h3 :style="{ color: themeStyles.textColor }">修改密码</h3>
            <p :style="{ color: themeStyles.textSecondary }">请输入当前密码和新密码</p>
          </div>

          <a-form 
            ref="passwordFormRef" 
            :model="passwordForm" 
            layout="vertical"
            :style="{ marginTop: '32px' }"
            @finish="changePassword"
          >
            <a-form-item 
              label="当前密码" 
              name="currentPassword"
              :rules="[{ required: true, message: '请输入当前密码', trigger: 'blur' }]"
            >
              <template #label>
                <span :style="{ color: themeStyles.textColor }">当前密码</span>
              </template>
              <a-input-password 
                v-model:value="passwordForm.currentPassword"
                size="large"
                placeholder="请输入当前密码"
                :style="getInputStyle()"
              />
            </a-form-item>
            
            <a-form-item 
              label="新密码" 
              name="newPassword"
              :rules="
                [
                  { required: true, message: '请输入新密码', trigger: 'blur' },
                  { min: 6, message: '密码长度至少6位', trigger: 'blur' }
                ]"
            >
              <template #label>
                <span :style="{ color: themeStyles.textColor }">新密码</span>
              </template>
              <a-input-password 
                v-model:value="passwordForm.newPassword"
                size="large"
                placeholder="请输入新密码"
                :style="getInputStyle()"
              />
            </a-form-item>
            
            <a-form-item 
              label="确认新密码" 
              name="confirmPassword"
              :rules="
                [
                  { required: true, message: '请确认新密码', trigger: 'blur' },
                  { validator: validateConfirmPassword, trigger: 'blur' }
                ]"
            >
              <template #label>
                <span :style="{ color: themeStyles.textColor }">确认新密码</span>
              </template>
              <a-input-password 
                v-model:value="passwordForm.confirmPassword"
                size="large"
                placeholder="请再次输入新密码"
                :style="getInputStyle()"
              />
            </a-form-item>

            <div class="form-actions">
              <a-space>
                <a-button @click="resetForm" :style="getButtonStyle()">
                  重置
                </a-button>
                <a-button 
                  type="primary" 
                  html-type="submit"
                  :loading="passwordLoading"
                  size="large"
                >
                  更新密码
                </a-button>
              </a-space>
            </div>
          </a-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import { isDarkMode } from '@/stores/theme'

// 主题样式
const themeStyles = computed(() => {
  if (isDarkMode.value) {
    return {
      contentBg: '#1f1f1f',
      cardBg: '#262626',
      textColor: '#ffffff',
      textSecondary: '#999999',
      borderColor: '#434343',
      inputBg: '#1f1f1f'
    }
  } else {
    return {
      contentBg: '#f5f5f5',
      cardBg: '#ffffff',
      textColor: '#000000',
      textSecondary: '#666666',
      borderColor: '#f0f0f0',
      inputBg: '#ffffff'
    }
  }
})

// 样式函数
const getInputStyle = () => ({
  backgroundColor: themeStyles.value.inputBg,
  borderColor: themeStyles.value.borderColor,
  color: themeStyles.value.textColor
})

const getButtonStyle = () => ({
  backgroundColor: themeStyles.value.cardBg,
  borderColor: themeStyles.value.borderColor,
  color: themeStyles.value.textColor
})

// 密码表单
const passwordFormRef = ref()
const passwordLoading = ref(false)
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 确认密码验证
const validateConfirmPassword = (rule: any, value: string) => {
  if (value !== passwordForm.value.newPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

// 修改密码
const changePassword = async () => {
  passwordLoading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    message.success('密码修改成功')
    
    // 清空表单
    passwordForm.value = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    message.error('密码修改失败，请重试')
  } finally {
    passwordLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  passwordForm.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordFormRef.value?.clearValidate()
  message.info('表单已重置')
}
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  padding: 0;
}

.page-header {
  padding: 24px 32px;
  border-bottom: 1px solid;
  margin-bottom: 24px;
}

.settings-content {
  padding: 0 32px 32px;
  display: flex;
  justify-content: center;
}

.settings-panel {
  border: 1px solid;
  border-radius: 8px;
  width: 100%;
  max-width: 600px;
}

.panel-content {
  padding: 32px;
}

.panel-header {
  margin-bottom: 0;
}

.panel-header h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
}

.panel-header p {
  margin: 0;
  font-size: 14px;
}

.form-actions {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid v-bind('themeStyles.borderColor');
  text-align: right;
}

/* 深度样式覆盖 */
:deep(.ant-input) {
  background: v-bind('themeStyles.inputBg') !important;
  border-color: v-bind('themeStyles.borderColor') !important;
  color: v-bind('themeStyles.textColor') !important;
}

:deep(.ant-input:focus) {
  border-color: #1890ff !important;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2) !important;
}

:deep(.ant-input::placeholder) {
  color: v-bind('themeStyles.textSecondary') !important;
}

:deep(.ant-btn:not(.ant-btn-primary)) {
  background: v-bind('themeStyles.cardBg') !important;
  border-color: v-bind('themeStyles.borderColor') !important;
  color: v-bind('themeStyles.textColor') !important;
}

:deep(.ant-btn:not(.ant-btn-primary):hover) {
  border-color: #1890ff !important;
  color: #1890ff !important;
}

:deep(.ant-form-item-label > label) {
  color: v-bind('themeStyles.textColor') !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .settings-content {
    padding: 0 16px 16px;
  }
  
  .page-header {
    padding: 16px 24px;
  }
  
  .panel-content {
    padding: 20px;
  }
}
</style>