<template>
  <div class="settings-page" :style="{ backgroundColor: isDark ? '#1f1f1f' : '#ffffff' }">
    <a-tabs 
      v-model:activeKey="activeTab" 
      tab-position="left"
      :style="getTabsStyle()"
    >
      <a-tab-pane key="general" tab="通用设置">
        <a-card title="基本配置" :style="getCardStyle()">
          <template #title>
            <span :style="{ color: isDark ? '#ffffff' : '#333' }">基本配置</span>
          </template>
          <a-form layout="vertical">
            <a-form-item label="系统名称">
              <template #label>
                <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">系统名称</span>
              </template>
              <a-input v-model:value="settings.systemName" :style="getInputStyle()" />
            </a-form-item>
            <a-form-item label="默认语言">
              <template #label>
                <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">默认语言</span>
              </template>
              <a-select v-model:value="settings.language" :style="getSelectStyle()">
                <a-select-option value="zh-CN">中文</a-select-option>
                <a-select-option value="en-US">English</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="时区">
              <template #label>
                <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">时区</span>
              </template>
              <a-select v-model:value="settings.timezone" :style="getSelectStyle()">
                <a-select-option value="Asia/Shanghai">Asia/Shanghai</a-select-option>
                <a-select-option value="UTC">UTC</a-select-option>
              </a-select>
            </a-form-item>
          </a-form>
        </a-card>
      </a-tab-pane>
      
      <a-tab-pane key="graph" tab="图谱配置">
        <a-card title="知识图谱设置" :style="getCardStyle()">
          <template #title>
            <span :style="{ color: isDark ? '#ffffff' : '#333' }">知识图谱设置</span>
          </template>
          <a-form layout="vertical">
            <a-form-item label="默认图谱">
              <template #label>
                <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">默认图谱</span>
              </template>
              <a-select v-model:value="settings.defaultGraph" :style="getSelectStyle()">
                <a-select-option value="main">主图谱</a-select-option>
                <a-select-option value="test">测试图谱</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="节点显示限制">
              <template #label>
                <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">节点显示限制</span>
              </template>
              <a-input-number 
                v-model:value="settings.nodeLimit" 
                :min="100" 
                :max="10000" 
                :style="getInputStyle()"
              />
            </a-form-item>
            <a-form-item label="关系显示限制">
              <template #label>
                <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">关系显示限制</span>
              </template>
              <a-input-number 
                v-model:value="settings.relationLimit" 
                :min="100" 
                :max="10000" 
                :style="getInputStyle()"
              />
            </a-form-item>
          </a-form>
        </a-card>
      </a-tab-pane>
      
      <a-tab-pane key="api" tab="API配置">
        <a-card title="接口设置" :style="getCardStyle()">
          <template #title>
            <span :style="{ color: isDark ? '#ffffff' : '#333' }">接口设置</span>
          </template>
          <a-form layout="vertical">
            <a-form-item label="API基础地址">
              <template #label>
                <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">API基础地址</span>
              </template>
              <a-input v-model:value="settings.apiBaseUrl" :style="getInputStyle()" />
            </a-form-item>
            <a-form-item label="请求超时时间（秒）">
              <template #label>
                <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">请求超时时间（秒）</span>
              </template>
              <a-input-number 
                v-model:value="settings.requestTimeout" 
                :min="10" 
                :max="300" 
                :style="getInputStyle()"
              />
            </a-form-item>
            <a-form-item label="批量操作大小">
              <template #label>
                <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">批量操作大小</span>
              </template>
              <a-input-number 
                v-model:value="settings.batchSize" 
                :min="10" 
                :max="1000" 
                :style="getInputStyle()"
              />
            </a-form-item>
          </a-form>
        </a-card>
      </a-tab-pane>
      
      <a-tab-pane key="security" tab="安全设置">
        <a-card title="安全配置" :style="getCardStyle()">
          <template #title>
            <span :style="{ color: isDark ? '#ffffff' : '#333' }">安全配置</span>
          </template>
          <a-form layout="vertical">
            <a-form-item label="会话超时时间（分钟）">
              <template #label>
                <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">会话超时时间（分钟）</span>
              </template>
              <a-input-number 
                v-model:value="settings.sessionTimeout" 
                :min="5" 
                :max="1440" 
                :style="getInputStyle()"
              />
            </a-form-item>
            <a-form-item label="密码策略">
              <template #label>
                <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">密码策略</span>
              </template>
              <a-checkbox-group v-model:value="settings.passwordPolicy" :style="getCheckboxStyle()">
                <div style="display: flex; flex-direction: column; gap: 8px;">
                  <a-checkbox value="minLength" :style="{ color: isDark ? '#ffffff' : '#333' }">
                    最小长度8位
                  </a-checkbox>
                  <a-checkbox value="requireNumber" :style="{ color: isDark ? '#ffffff' : '#333' }">
                    包含数字
                  </a-checkbox>
                  <a-checkbox value="requireSymbol" :style="{ color: isDark ? '#ffffff' : '#333' }">
                    包含特殊字符
                  </a-checkbox>
                  <a-checkbox value="requireUppercase" :style="{ color: isDark ? '#ffffff' : '#333' }">
                    包含大写字母
                  </a-checkbox>
                </div>
              </a-checkbox-group>
            </a-form-item>
          </a-form>
        </a-card>
      </a-tab-pane>
    </a-tabs>
    
    <div class="settings-actions" :style="getActionsStyle()">
      <a-space>
        <a-button @click="resetSettings" :style="getButtonStyle()">
          重置
        </a-button>
        <a-button 
          type="primary" 
          @click="saveSettings" 
          :loading="saving"
          :style="getPrimaryButtonStyle()"
        >
          保存设置
        </a-button>
      </a-space>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { message } from 'ant-design-vue'

const activeTab = ref('general')
const saving = ref(false)

// 改进的主题检测
const isDark = ref(false)

const detectTheme = () => {
  const bodyBg = getComputedStyle(document.body).backgroundColor
  const htmlBg = getComputedStyle(document.documentElement).backgroundColor
  
  const isDarkTheme = bodyBg === 'rgb(20, 20, 20)' || 
                     htmlBg === 'rgb(20, 20, 20)' ||
                     bodyBg === '#141414' ||
                     htmlBg === '#141414'
  
  isDark.value = isDarkTheme
}

// 监听主题变化
watch(isDark, () => {
  console.log('Settings theme changed:', isDark.value)
})

// 样式计算函数
const getTabsStyle = () => ({
  backgroundColor: isDark.value ? '#262626' : '#ffffff',
  transition: 'all 0.3s ease'
})

const getCardStyle = () => ({
  backgroundColor: isDark.value ? '#262626' : '#ffffff',
  border: `1px solid ${isDark.value ? '#434343' : '#f0f0f0'}`,
  transition: 'all 0.3s ease'
})

const getInputStyle = () => ({
  backgroundColor: isDark.value ? '#1f1f1f' : '#ffffff',
  borderColor: isDark.value ? '#434343' : '#d9d9d9',
  color: isDark.value ? '#ffffff' : '#333'
})

const getSelectStyle = () => ({
  backgroundColor: isDark.value ? '#1f1f1f' : '#ffffff',
  borderColor: isDark.value ? '#434343' : '#d9d9d9',
  color: isDark.value ? '#ffffff' : '#333'
})

const getCheckboxStyle = () => ({
  color: isDark.value ? '#ffffff' : '#333'
})

const getActionsStyle = () => ({
  borderTop: `1px solid ${isDark.value ? '#434343' : '#f0f0f0'}`,
  backgroundColor: isDark.value ? '#1f1f1f' : '#ffffff',
  transition: 'all 0.3s ease'
})

const getButtonStyle = () => ({
  backgroundColor: isDark.value ? '#262626' : '#ffffff',
  borderColor: isDark.value ? '#434343' : '#d9d9d9',
  color: isDark.value ? '#ffffff' : '#333'
})

const getPrimaryButtonStyle = () => ({
  backgroundColor: '#1890ff',
  borderColor: '#1890ff',
  color: '#ffffff'
})

const settings = ref({
  systemName: 'NeoKG管理平台',
  language: 'zh-CN',
  timezone: 'Asia/Shanghai',
  defaultGraph: 'main',
  nodeLimit: 1000,
  relationLimit: 1000,
  apiBaseUrl: '/api/v1',
  requestTimeout: 30,
  batchSize: 100,
  sessionTimeout: 120,
  passwordPolicy: ['minLength', 'requireNumber']
})

const saveSettings = async () => {
  saving.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    message.success('设置保存成功')
  } catch (error) {
    message.error('设置保存失败')
  } finally {
    saving.value = false
  }
}

const resetSettings = () => {
  settings.value = {
    systemName: 'NeoKG管理平台',
    language: 'zh-CN',
    timezone: 'Asia/Shanghai',
    defaultGraph: 'main',
    nodeLimit: 1000,
    relationLimit: 1000,
    apiBaseUrl: '/api/v1',
    requestTimeout: 30,
    batchSize: 100,
    sessionTimeout: 120,
    passwordPolicy: ['minLength', 'requireNumber']
  }
  message.info('设置已重置')
}

// 页面挂载时检测主题，并监听变化
import { onMounted } from 'vue'

onMounted(() => {
  detectTheme()
  
  const observer = new MutationObserver(() => {
    detectTheme()
  })
  
  observer.observe(document.body, {
    attributes: true,
    attributeFilter: ['style']
  })
  
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['style']
  })
})
</script>

<style scoped>
.settings-page {
  padding: 0;
  transition: background-color 0.3s ease;
}

.settings-actions {
  margin-top: 24px;
  padding-top: 24px;
  text-align: right;
  transition: all 0.3s ease;
}

/* 深色主题下的 Ant Design 组件样式覆盖 */
:deep(.ant-tabs-tab) {
  color: v-bind('isDark ? "#b3b3b3" : "#666"') !important;
  transition: color 0.3s ease !important;
}

:deep(.ant-tabs-tab-active) {
  color: v-bind('isDark ? "#1890ff" : "#1890ff"') !important;
}

:deep(.ant-tabs-tab:hover) {
  color: v-bind('isDark ? "#ffffff" : "#333"') !important;
}

:deep(.ant-tabs-content-holder) {
  background-color: v-bind('isDark ? "#1f1f1f" : "#ffffff"') !important;
  transition: background-color 0.3s ease !important;
}

:deep(.ant-tabs-tabpane) {
  background-color: v-bind('isDark ? "#1f1f1f" : "#ffffff"') !important;
}

:deep(.ant-tabs-nav) {
  background-color: v-bind('isDark ? "#262626" : "#ffffff"') !important;
  border-right-color: v-bind('isDark ? "#434343" : "#f0f0f0"') !important;
}

:deep(.ant-tabs-nav::before) {
  border-bottom-color: v-bind('isDark ? "#434343" : "#f0f0f0"') !important;
}

:deep(.ant-tabs-ink-bar) {
  background-color: #1890ff !important;
}

:deep(.ant-card) {
  background-color: v-bind('isDark ? "#262626" : "#ffffff"') !important;
  border-color: v-bind('isDark ? "#434343" : "#f0f0f0"') !important;
  transition: all 0.3s ease !important;
}

:deep(.ant-card-head) {
  background-color: v-bind('isDark ? "#262626" : "#ffffff"') !important;
  border-bottom-color: v-bind('isDark ? "#434343" : "#f0f0f0"') !important;
}

:deep(.ant-card-body) {
  background-color: v-bind('isDark ? "#262626" : "#ffffff"') !important;
}

:deep(.ant-input) {
  background-color: v-bind('isDark ? "#1f1f1f" : "#ffffff"') !important;
  border-color: v-bind('isDark ? "#434343" : "#d9d9d9"') !important;
  color: v-bind('isDark ? "#ffffff" : "#333"') !important;
  transition: all 0.3s ease !important;
}

:deep(.ant-input:focus) {
  border-color: #1890ff !important;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2) !important;
}

:deep(.ant-input-number) {
  background-color: v-bind('isDark ? "#1f1f1f" : "#ffffff"') !important;
  border-color: v-bind('isDark ? "#434343" : "#d9d9d9"') !important;
  color: v-bind('isDark ? "#ffffff" : "#333"') !important;
}

:deep(.ant-input-number-input) {
  background-color: v-bind('isDark ? "#1f1f1f" : "#ffffff"') !important;
  color: v-bind('isDark ? "#ffffff" : "#333"') !important;
}

:deep(.ant-select-selector) {
  background-color: v-bind('isDark ? "#1f1f1f" : "#ffffff"') !important;
  border-color: v-bind('isDark ? "#434343" : "#d9d9d9"') !important;
  color: v-bind('isDark ? "#ffffff" : "#333"') !important;
}

:deep(.ant-select-selection-item) {
  color: v-bind('isDark ? "#ffffff" : "#333"') !important;
}

:deep(.ant-select-arrow) {
  color: v-bind('isDark ? "#ffffff" : "#333"') !important;
}

:deep(.ant-checkbox-wrapper) {
  color: v-bind('isDark ? "#ffffff" : "#333"') !important;
  transition: color 0.3s ease !important;
}

:deep(.ant-checkbox-inner) {
  background-color: v-bind('isDark ? "#1f1f1f" : "#ffffff"') !important;
  border-color: v-bind('isDark ? "#434343" : "#d9d9d9"') !important;
}

:deep(.ant-checkbox-checked .ant-checkbox-inner) {
  background-color: #1890ff !important;
  border-color: #1890ff !important;
}

:deep(.ant-btn) {
  transition: all 0.3s ease !important;
}

:deep(.ant-btn:not(.ant-btn-primary)) {
  background-color: v-bind('isDark ? "#262626" : "#ffffff"') !important;
  border-color: v-bind('isDark ? "#434343" : "#d9d9d9"') !important;
  color: v-bind('isDark ? "#ffffff" : "#333"') !important;
}

:deep(.ant-btn:not(.ant-btn-primary):hover) {
  background-color: v-bind('isDark ? "#434343" : "#f5f5f5"') !important;
  border-color: v-bind('isDark ? "#666" : "#1890ff"') !important;
  color: v-bind('isDark ? "#ffffff" : "#1890ff"') !important;
}

/* 下拉菜单样式 */
:deep(.ant-select-dropdown) {
  background-color: v-bind('isDark ? "#262626" : "#ffffff"') !important;
  border-color: v-bind('isDark ? "#434343" : "#d9d9d9"') !important;
}

:deep(.ant-select-item) {
  color: v-bind('isDark ? "#ffffff" : "#333"') !important;
}

:deep(.ant-select-item:hover) {
  background-color: v-bind('isDark ? "#434343" : "#f5f5f5"') !important;
}

:deep(.ant-select-item-option-selected) {
  background-color: v-bind('isDark ? "#1890ff" : "#e6f7ff"') !important;
  color: v-bind('isDark ? "#ffffff" : "#1890ff"') !important;
}
</style>