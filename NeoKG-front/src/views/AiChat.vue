<template>
  <div class="ai-chat-container" :style="{ backgroundColor: themeStyles.contentBg }">
    <!-- 聊天消息区域 -->
    <div class="chat-messages" :style="{ backgroundColor: themeStyles.contentBg }" ref="messagesContainer">
      <div class="messages-wrapper">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome-message">
          <div class="welcome-content">
            <div class="logo-section">
              <div class="logo-icon">
                <!-- 添加暗夜模式图片处理 -->
                <img 
                  src="@/assets/NKG2.png" 
                  alt="NeoKG" 
                  class="logo-image"
                  :class="{ 'dark-mode-image': isDarkMode }"
                />
              </div>
            </div>
            <h1 class="welcome-title" :style="{ color: themeStyles.textColor }">您在忙什么？</h1>
            <p class="welcome-subtitle" :style="{ color: themeStyles.textSecondary }">
              我是您的AI助手，基于知识图谱为您提供智能问答服务
            </p>
          </div>
        </div>

        <!-- 聊天消息列表 -->
        <div v-for="(message, index) in messages" :key="index" class="message-item">
          <!-- 用户消息 -->
          <div v-if="message.type === 'user'" class="message-row user-message">
            <div class="message-content user-content">
              <p>{{ message.content }}</p>
            </div>
            <div class="user-avatar">
              <span>您</span>
            </div>
          </div>

          <!-- AI回复 -->
          <div v-if="message.type === 'ai'" class="message-row ai-message">
            <div class="ai-avatar">
              <!-- 添加暗夜模式图片处理 -->
              <img 
                src="@/assets/NKG2.png" 
                alt="NeoKG" 
                class="ai-avatar-image"
                :class="{ 'dark-mode-image': isDarkMode }"
              />
            </div>
            <div class="message-content ai-content" :style="{ 
              backgroundColor: themeStyles.aiMessageBg,
              color: themeStyles.textColor
            }">
              <div v-if="message.loading" class="loading-animation">
                <div class="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
              <div v-else class="ai-response" v-html="formatMessage(message.content)"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 - 固定在底部 -->
    <div class="chat-input-area" :style="{ 
      backgroundColor: themeStyles.inputAreaBg,
      borderTop: `1px solid ${themeStyles.borderColor}`
    }">
      <div class="input-wrapper">
        <div class="input-container" :style="{ 
          backgroundColor: themeStyles.inputBg,
          borderColor: themeStyles.inputBorderColor,
          boxShadow: themeStyles.inputShadow
        }">
          <div class="input-content">
            <textarea
              v-model="inputMessage"
              :placeholder="loading ? '正在思考中...' : '询问任何问题...'"
              :disabled="loading"
              @keydown="handleKeyDown"
              @input="adjustTextareaHeight"
              :style="{ 
                backgroundColor: 'transparent',
                color: themeStyles.textColor
              }"
              class="message-input"
              rows="1"
              ref="textareaRef"
            />
            <div class="input-actions">
              <button 
                v-if="inputMessage.trim()"
                :disabled="loading"
                @click="sendMessage"
                class="send-button"
                :class="{ 'loading': loading }"
              >
                <send-outlined v-if="!loading" />
                <loading-outlined v-else class="loading-icon" />
              </button>
              <button v-else class="voice-button" :style="{ color: themeStyles.textSecondary }">
                <audio-outlined />
              </button>
            </div>
          </div>
        </div>
        <div class="input-hint" :style="{ color: themeStyles.textSecondary }">
          按 Enter 发送，Shift + Enter 换行
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  SendOutlined,
  LoadingOutlined,
  AudioOutlined
} from '@ant-design/icons-vue'
import { isDarkMode } from '@/stores/theme'
import axios from 'axios'

// 类型定义
type ChatMessage = {
  type: 'user' | 'ai'
  content: string
  timestamp: number
  loading?: boolean
}

// 主题样式
const themeStyles = computed(() => {
  if (isDarkMode.value) {
    return {
      contentBg: '#1a1a1a',
      inputAreaBg: '#1a1a1a',
      inputBg: '#2d2d2d',
      inputBorderColor: '#404040',
      aiMessageBg: '#2d2d2d',
      textColor: '#ffffff',
      textSecondary: '#a0a0a0',
      borderColor: '#333333',
      inputShadow: '0 4px 12px rgba(0, 0, 0, 0.3)',
      gradientPrimary: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      gradientSecondary: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
    }
  } else {
    return {
      contentBg: '#ffffff',
      inputAreaBg: '#ffffff',
      inputBg: '#ffffff',
      inputBorderColor: '#e1e5e9',
      aiMessageBg: '#f8f9fa',
      textColor: '#2d3748',
      textSecondary: '#718096',
      borderColor: '#e2e8f0',
      inputShadow: '0 4px 12px rgba(0, 0, 0, 0.05)',
      gradientPrimary: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      gradientSecondary: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
    }
  }
})

// 状态管理
const inputMessage = ref('')
const loading = ref(false)
const messages = ref<ChatMessage[]>([])
const messagesContainer = ref<HTMLElement>()
const textareaRef = ref<HTMLTextAreaElement>()

// 格式化消息内容
const formatMessage = (content: string) => {
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code class="inline-code">$1</code>')
    .replace(/\n/g, '<br>')
}

// 调整textarea高度
const adjustTextareaHeight = () => {
  nextTick(() => {
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto'
      textareaRef.value.style.height = Math.min(textareaRef.value.scrollHeight, 120) + 'px'
    }
  })
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 处理键盘事件
const handleKeyDown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    if (inputMessage.value.trim() && !loading.value) {
      sendMessage()
    }
  }
}

// 发送消息 - 修改为调用后端接口
const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return

  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''
  
  // 重置textarea高度
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
  }

  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: userMessage,
    timestamp: Date.now()
  })

  // 添加AI加载消息
  messages.value.push({
    type: 'ai',
    content: '',
    timestamp: Date.now(),
    loading: true
  })

  scrollToBottom()
  loading.value = true

  try {
    // 调用后端聊天接口
    const formData = new FormData()
    formData.append('message', userMessage)
    
    const response = await axios.post('/api/chat', formData, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })
    
    // 移除加载消息
    messages.value.pop()
    
    // 添加AI回复
    messages.value.push({
      type: 'ai',
      content: response.data || '抱歉，我没有收到有效的回复。',
      timestamp: Date.now(),
      loading: false
    })
    
    scrollToBottom()
  } catch (error) {
    console.error('发送消息失败:', error)
    
    // 移除加载消息
    messages.value.pop()
    
    // 添加错误消息
    messages.value.push({
      type: 'ai',
      content: '抱歉，我遇到了一些问题，请稍后再试。如果问题持续出现，请联系技术支持。',
      timestamp: Date.now(),
      loading: false
    })
    
    message.error('发送消息失败，请稍后重试')
    scrollToBottom()
  } finally {
    loading.value = false
  }
}

// 页面加载时的初始化
onMounted(() => {
  console.log('AI问答页面已加载')
})
</script>

<style scoped>
.ai-chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 20px;
}

.messages-wrapper {
  padding: 0;
  max-width: 900px;
  margin: 0 auto;
  min-height: calc(100vh - 180px);
  display: flex;
  flex-direction: column;
}

/* 欢迎消息 */
.welcome-message {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 32px;
}

.welcome-content {
  text-align: center;
  max-width: 600px;
}

.logo-section {
  margin-bottom: 40px;
}

.logo-icon {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: v-bind('themeStyles.gradientPrimary');
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
  transition: transform 0.3s ease;
}

.logo-icon:hover {
  transform: translateY(-2px);
}

.logo-text {
  font-size: 32px;
  font-weight: bold;
  color: white;
  letter-spacing: -1px;
}

.welcome-title {
  font-size: 48px;
  font-weight: 600;
  margin: 0 0 16px 0;
  letter-spacing: -1px;
  background: v-bind('themeStyles.gradientPrimary');
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-subtitle {
  font-size: 18px;
  line-height: 1.6;
  margin: 0;
  opacity: 0.8;
}

/* 消息项 */
.message-item {
  margin-bottom: 32px;
  padding: 0 32px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  max-width: 100%;
}

.user-message {
  flex-direction: row-reverse;
  margin-left: 15%;
}

.ai-message {
  flex-direction: row;
  margin-right: 15%;
}

.user-avatar,
.ai-avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 600;
  color: white;
}

.user-avatar {
  background: v-bind('themeStyles.gradientSecondary');
}

.ai-avatar {
  background: v-bind('themeStyles.gradientPrimary');
}

.ai-logo {
  font-size: 16px;
  letter-spacing: -0.5px;
}

.message-content {
  flex: 1;
  border-radius: 18px;
  word-wrap: break-word;
  line-height: 1.6;
  font-size: 16px;
  max-width: calc(100% - 56px);
  position: relative;
}

.user-content {
  background: v-bind('themeStyles.gradientPrimary');
  color: white;
  padding: 16px 20px;
  border-bottom-right-radius: 6px;
}

.user-content p {
  margin: 0;
}

.ai-content {
  padding: 20px 24px;
  border-bottom-left-radius: 6px;
  border: 1px solid v-bind('themeStyles.borderColor');
}

.ai-response {
  margin: 0;
}

/* 加载动画 */
.loading-animation {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  align-items: center;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #667eea;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% {
    opacity: 0.3;
    transform: translateY(0);
  }
  40% {
    opacity: 1;
    transform: translateY(-8px);
  }
}

/* 输入区域 */
.chat-input-area {
  padding: 24px 32px 32px;
  border-top: 1px solid;
}

.input-wrapper {
  max-width: 900px;
  margin: 0 auto;
}

.input-container {
  border: 2px solid;
  border-radius: 24px;
  padding: 4px;
  transition: all 0.3s ease;
  background: white;
}

.input-container:focus-within {
  border-color: #667eea;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
}

.input-content {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 12px 16px;
}

.message-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 16px;
  line-height: 1.5;
  resize: none;
  background: transparent;
  min-height: 24px;
  max-height: 120px;
  overflow-y: auto;
  font-family: inherit;
}

.message-input::placeholder {
  color: v-bind('themeStyles.textSecondary');
}

.input-actions {
  display: flex;
  align-items: center;
}

.send-button,
.voice-button {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all 0.3s ease;
  background: v-bind('themeStyles.gradientPrimary');
  color: white;
}

.send-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.send-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.voice-button {
  background: transparent;
  border: 2px solid v-bind('themeStyles.borderColor');
}

.voice-button:hover {
  background: v-bind('themeStyles.borderColor');
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.input-hint {
  text-align: center;
  font-size: 12px;
  margin-top: 12px;
  opacity: 0.7;
}

/* 内联代码样式 */
:deep(.inline-code) {
  background: rgba(102, 126, 234, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(102, 126, 234, 0.3);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(102, 126, 234, 0.5);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .welcome-title {
    font-size: 36px;
  }
  
  .welcome-subtitle {
    font-size: 16px;
  }
  
  .message-item {
    padding: 0 16px;
  }
  
  .chat-input-area {
    padding: 16px 16px 24px;
  }
  
  .user-message {
    margin-left: 5%;
  }
  
  .ai-message {
    margin-right: 5%;
  }
  
  .logo-icon {
    width: 60px;
    height: 60px;
  }
  
  .logo-text {
    font-size: 24px;
  }
}

/* 暗夜模式图片处理 */
.dark-mode-image {
  filter: brightness(0.4) contrast(1.5);
  transition: filter 0.3s ease;
}

.logo-image {
  width: 84px;  /* 修改为固定尺寸 */
  height: 84px; /* 修改为固定尺寸 */
  object-fit: contain;
  transition: filter 0.3s ease;
  border-radius: 12px; /* 添加圆角 */
}

.ai-avatar-image {
  width: 40px;  /* 调整为合适尺寸 */
  height: 40px; /* 调整为合适尺寸 */
  object-fit: contain;
  transition: filter 0.3s ease;
  border-radius: 8px; /* 添加圆角 */
}

/* 暗夜模式下的图片悬停效果 */
.dark-mode-image:hover {
  filter: brightness(0.9) contrast(1.2);
}

/* 为logo区域添加更多样式效果 */
.logo-icon {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: v-bind('themeStyles.gradientPrimary');
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
  transition: transform 0.3s ease;
  overflow: hidden; /* 确保图片圆角效果 */
}

.logo-icon:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.4); /* 增强悬停阴影 */
}

/* AI头像容器也添加圆角 */
.ai-avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 600;
  color: white;
  background: v-bind('themeStyles.gradientPrimary');
  overflow: hidden; /* 确保图片圆角效果 */
}
</style>