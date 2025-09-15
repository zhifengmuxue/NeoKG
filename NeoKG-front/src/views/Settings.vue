<template>
  <div class="settings-page">
    <a-tabs v-model:activeKey="activeTab" tab-position="left">
      <a-tab-pane key="general" tab="通用设置">
        <a-card title="基本配置">
          <a-form layout="vertical">
            <a-form-item label="系统名称">
              <a-input v-model:value="settings.systemName" />
            </a-form-item>
            <a-form-item label="默认语言">
              <a-select v-model:value="settings.language">
                <a-select-option value="zh-CN">中文</a-select-option>
                <a-select-option value="en-US">English</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="时区">
              <a-select v-model:value="settings.timezone">
                <a-select-option value="Asia/Shanghai">Asia/Shanghai</a-select-option>
                <a-select-option value="UTC">UTC</a-select-option>
              </a-select>
            </a-form-item>
          </a-form>
        </a-card>
      </a-tab-pane>
      
      <a-tab-pane key="graph" tab="图谱配置">
        <a-card title="知识图谱设置">
          <a-form layout="vertical">
            <a-form-item label="默认图谱">
              <a-select v-model:value="settings.defaultGraph">
                <a-select-option value="main">主图谱</a-select-option>
                <a-select-option value="test">测试图谱</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="节点显示限制">
              <a-input-number v-model:value="settings.nodeLimit" :min="100" :max="10000" />
            </a-form-item>
            <a-form-item label="关系显示限制">
              <a-input-number v-model:value="settings.relationLimit" :min="100" :max="10000" />
            </a-form-item>
          </a-form>
        </a-card>
      </a-tab-pane>
      
      <a-tab-pane key="api" tab="API配置">
        <a-card title="接口设置">
          <a-form layout="vertical">
            <a-form-item label="API基础地址">
              <a-input v-model:value="settings.apiBaseUrl" />
            </a-form-item>
            <a-form-item label="请求超时时间（秒）">
              <a-input-number v-model:value="settings.requestTimeout" :min="10" :max="300" />
            </a-form-item>
            <a-form-item label="批量操作大小">
              <a-input-number v-model:value="settings.batchSize" :min="10" :max="1000" />
            </a-form-item>
          </a-form>
        </a-card>
      </a-tab-pane>
      
      <a-tab-pane key="security" tab="安全设置">
        <a-card title="安全配置">
          <a-form layout="vertical">
            <a-form-item label="会话超时时间（分钟）">
              <a-input-number v-model:value="settings.sessionTimeout" :min="5" :max="1440" />
            </a-form-item>
            <a-form-item label="密码策略">
              <a-checkbox-group v-model:value="settings.passwordPolicy">
                <a-checkbox value="minLength">最小长度8位</a-checkbox>
                <a-checkbox value="requireNumber">包含数字</a-checkbox>
                <a-checkbox value="requireSymbol">包含特殊字符</a-checkbox>
                <a-checkbox value="requireUppercase">包含大写字母</a-checkbox>
              </a-checkbox-group>
            </a-form-item>
          </a-form>
        </a-card>
      </a-tab-pane>
    </a-tabs>
    
    <div class="settings-actions">
      <a-space>
        <a-button @click="resetSettings">重置</a-button>
        <a-button type="primary" @click="saveSettings" :loading="saving">
          保存设置
        </a-button>
      </a-space>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { message } from 'ant-design-vue'

const activeTab = ref('general')
const saving = ref(false)

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
    // 模拟保存API调用
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
</script>

<style scoped>
.settings-page {
  padding: 0;
}

.settings-actions {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
  text-align: right;
}
</style>