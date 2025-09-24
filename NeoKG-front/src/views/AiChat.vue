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
                <span class="logo-text">NK</span>
              </div>
            </div>
            <h1 class="welcome-title" :style="{ color: themeStyles.textColor }">您好！我是 NeoKG 助手</h1>
            <p class="welcome-subtitle" :style="{ color: themeStyles.textSecondary }">
              基于知识图谱的智能问答助手，随时为您解答疑问
            </p>
          </div>
        </div>

        <!-- 聊天消息列表 -->
        <div v-for="(message, index) in messages" :key="index" class="message-item">
          <!-- AI回复 - 左侧 -->
          <div v-if="message.type === 'ai'" class="message-row ai-message">
            <div class="message-content ai-content">
              <div class="message-bubble ai-bubble" :style="{ 
                backgroundColor: themeStyles.aiBubbleBg,
                color: themeStyles.textColor
              }">
                <div v-if="message.loading" class="loading-animation">
                  <div class="typing-indicator">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                </div>
                <div v-else class="message-text" v-html="formatMessage(message.content)"></div>
              </div>
              <!-- 消息操作按钮 -->
              <div v-if="!message.loading" class="message-actions">
                <button 
                  class="action-button" 
                  @click="copyMessage(message.content)"
                  :style="{ color: themeStyles.textSecondary }"
                  title="复制"
                >
                  <copy-outlined />
                </button>
                <button 
                  class="action-button" 
                  @click="regenerateResponse(index)"
                  :style="{ color: themeStyles.textSecondary }"
                  title="重新生成"
                >
                  <reload-outlined />
                </button>
              </div>
            </div>
          </div>

          <!-- 用户消息 - 右侧 -->
          <div v-if="message.type === 'user'" class="message-row user-message">
            <div class="message-content user-content">
              <div class="message-bubble user-bubble">
                <div class="message-text">{{ message.content }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部占位，确保消息不会被输入框遮挡 -->
        <div class="messages-spacer"></div>
      </div>
    </div>

    <!-- 输入区域 - 固定在底部，避开侧边栏 -->
    <div class="chat-input-area" :style="{ 
      backgroundColor: 'transparent',
    }">
      <div class="input-wrapper">
        <div class="input-container" :style="{ 
          backgroundColor: themeStyles.inputBg,
          borderColor: inputFocused ? themeStyles.focusBorderColor : themeStyles.inputBorderColor,
          boxShadow: inputFocused ? themeStyles.focusBoxShadow : '0 4px 20px rgba(0, 0, 0, 0.08)'
        }">
          <div class="input-content">
            <textarea
              v-model="inputMessage"
              :placeholder="loading ? '正在思考中...' : '输入您的问题...'"
              :disabled="loading"
              @keydown="handleKeyDown"
              @input="adjustTextareaHeight"
              @focus="inputFocused = true"
              @blur="inputFocused = false"
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
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  SendOutlined,
  LoadingOutlined,
  CopyOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import { isDarkMode } from '@/stores/theme'

// 类型定义
type ChatMessage = {
  type: 'user' | 'ai'
  content: string
  timestamp: number
  loading?: boolean
}

// 主题样式 - 天蓝色主题
const themeStyles = computed(() => {
  if (isDarkMode.value) {
    return {
      contentBg: '#1a1a1a',
      inputBg: '#2d2d2d',
      inputBorderColor: '#404040',
      focusBorderColor: '#1890ff',
      focusBoxShadow: '0 0 0 3px rgba(24, 144, 255, 0.1)',
      aiBubbleBg: '#2d2d2d',
      textColor: '#ffffff',
      textSecondary: '#a0a0a0',
      borderColor: '#404040',
    }
  } else {
    return {
      contentBg: '#ffffff', // 修改为纯白色
      inputBg: '#ffffff',
      inputBorderColor: '#bfdbfe',
      focusBorderColor: '#1890ff',
      focusBoxShadow: '0 0 0 3px rgba(24, 144, 255, 0.1)',
      aiBubbleBg: '#f8fafc', // 稍微调整AI气泡背景色，保持层次感
      textColor: '#1e293b',
      textSecondary: '#64748b',
      borderColor: '#e2e8f0', // 修改边框颜色为更合适的灰色
    }
  }
})

// 检测侧边栏状态
const sidebarLeft = ref(240) // 默认展开状态

const detectSidebarState = () => {
  // 检测侧边栏元素
  const siderElement = document.querySelector('.ant-layout-sider')
  if (siderElement) {
    const width = siderElement.getBoundingClientRect().width
    sidebarLeft.value = width
  }
}

// 状态管理
const inputMessage = ref('')
const loading = ref(false)
const inputFocused = ref(false)
const messages = ref<ChatMessage[]>([])
const messagesContainer = ref<HTMLElement>()
const textareaRef = ref<HTMLTextAreaElement>()

// 生成随机会话ID
const generateSessionId = () => {
  return 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

const sessionId = ref(generateSessionId())

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
      messagesContainer.value.scrollTo({
        top: messagesContainer.value.scrollHeight,
        behavior: 'smooth'
      })
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

// 复制消息
const copyMessage = async (content: string) => {
  try {
    await navigator.clipboard.writeText(content)
    message.success('消息已复制')
  } catch (err) {
    message.error('复制失败')
  }
}

// 重新生成回复
const regenerateResponse = async (messageIndex: number) => {
  if (loading.value) return
  
  // 找到对应的用户消息
  const userMessageIndex = messageIndex - 1
  if (userMessageIndex >= 0 && messages.value[userMessageIndex]?.type === 'user') {
    const userMessage = messages.value[userMessageIndex].content
    
    // 移除当前AI回复
    messages.value.splice(messageIndex, 1)
    
    // 重新发送请求
    await sendAIMessage(userMessage)
  }
}

// 发送AI消息的通用方法 - 使用SSE
const sendAIMessage = async (userMessage: string) => {
  // 添加AI加载消息
  const aiMessageIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    timestamp: Date.now(),
    loading: true
  })

  scrollToBottom()
  loading.value = true

  try {
    // 构建SSE请求URL
    const params = new URLSearchParams({
      message: userMessage,
      sessionId: sessionId.value
    })
    
    const eventSource = new EventSource(`/api/chat/ask?${params.toString()}`)
    
    // 移除加载状态，开始接收流式数据
    messages.value[aiMessageIndex].loading = false
    
    eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        
        // 如果是流式数据，追加到现有内容
        if (data.data) {
          messages.value[aiMessageIndex].content += data.data
        } else if (typeof data === 'string') {
          messages.value[aiMessageIndex].content += data
        }
        
        scrollToBottom()
      } catch (error) {
        console.error('解析SSE数据失败:', error)
        // 如果不是JSON，直接作为文本处理
        messages.value[aiMessageIndex].content += event.data
        scrollToBottom()
      }
    }
    
    eventSource.onerror = (error) => {
      console.error('SSE连接错误:', error)
      eventSource.close()
      loading.value = false
      
      // 移除连接中断的错误提示，静默处理
      if (!messages.value[aiMessageIndex].content) {
        messages.value[aiMessageIndex].content = '抱歉，我遇到了一些问题，请稍后再试。'
      }
    }
    
    eventSource.addEventListener('end', () => {
      console.log('SSE流结束')
      eventSource.close()
      loading.value = false
    })
    
    // 监听连接关闭事件
    eventSource.addEventListener('close', () => {
      console.log('SSE连接正常关闭')
      eventSource.close()
      loading.value = false
    })
    
    // 设置超时关闭
    setTimeout(() => {
      if (eventSource.readyState !== EventSource.CLOSED) {
        eventSource.close()
        loading.value = false
      }
    }, 30000) // 30秒超时
    
  } catch (error) {
    console.error('发送消息失败:', error)
    
    // 更新AI消息为错误状态
    messages.value[aiMessageIndex] = {
      type: 'ai',
      content: '抱歉，我遇到了一些问题，请稍后再试。',
      timestamp: Date.now(),
      loading: false
    }
    
    scrollToBottom()
    loading.value = false
  }
}

// 发送消息
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

  await sendAIMessage(userMessage)
}

// 监听窗口变化和侧边栏变化
const resizeObserver = ref<ResizeObserver>()

// 页面加载时的初始化
onMounted(() => {
  console.log('AI问答页面已加载，会话ID:', sessionId.value)
  
  // 初始检测
  detectSidebarState()
  
  // 监听窗口变化
  window.addEventListener('resize', detectSidebarState)
  
  // 使用 ResizeObserver 监听侧边栏变化
  const siderElement = document.querySelector('.ant-layout-sider')
  if (siderElement && window.ResizeObserver) {
    resizeObserver.value = new ResizeObserver(() => {
      detectSidebarState()
    })
    resizeObserver.value.observe(siderElement)
  }
  
  // 使用 MutationObserver 监听侧边栏 class 变化
  const observer = new MutationObserver(() => {
    setTimeout(detectSidebarState, 100) // 延迟检测，等待动画完成
  })
  
  if (siderElement) {
    observer.observe(siderElement, {
      attributes: true,
      attributeFilter: ['class', 'style']
    })
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', detectSidebarState)
  if (resizeObserver.value) {
    resizeObserver.value.disconnect()
  }
})
</script>

<style scoped>
.ai-chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 0;
  padding-bottom: 120px; /* 为固定的输入框留出空间 */
}

.messages-wrapper {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.messages-spacer {
  height: 20px;
  flex-shrink: 0;
}

/* 欢迎消息 */
.welcome-message {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 24px;
  min-height: calc(100vh - 400px);
}

.welcome-content {
  text-align: center;
  max-width: 500px;
}

.logo-section {
  margin-bottom: 32px;
}

.logo-icon {
  width: 80px;
  height: 80px;
  border-radius: 24px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  box-shadow: 0 8px 32px rgba(24, 144, 255, 0.3);
  color: white;
}

.logo-text {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -1px;
}

.welcome-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 16px 0;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.welcome-subtitle {
  font-size: 18px;
  line-height: 1.6;
  margin: 0;
  opacity: 0.8;
}

/* 消息项 */
.message-item {
  margin-bottom: 24px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  max-width: 100%;
}

.user-message {
  flex-direction: row-reverse;
  justify-content: flex-start;
}

.ai-message {
  flex-direction: row;
  justify-content: flex-start;
}

/* 消息内容 */
.message-content {
  flex: 1;
  max-width: calc(100% - 16px);
}

.user-content {
  display: flex;
  justify-content: flex-end;
}

.ai-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.message-bubble {
  max-width: 70%;
  word-wrap: break-word;
  border-radius: 20px;
  position: relative;
}

.user-bubble {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
  padding: 14px 20px;
  border-bottom-right-radius: 6px;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.ai-bubble {
  padding: 16px 20px;
  border-bottom-left-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid;
  border-color: v-bind('themeStyles.borderColor');
}

.message-text {
  line-height: 1.6;
  font-size: 15px;
  word-wrap: break-word;
}

/* 消息操作按钮 */
.message-actions {
  display: flex;
  gap: 4px;
  margin-top: 8px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.message-item:hover .message-actions {
  opacity: 1;
}

.action-button {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  background: transparent;
  transition: all 0.2s ease;
}

.action-button:hover {
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff !important;
}

/* 加载动画 - 天蓝色 */
.loading-animation {
  display: flex;
  align-items: center;
  padding: 4px 0;
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
  background: #1890ff;
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
    transform: translateY(-4px);
  }
}

/* 输入区域 - 固定在底部，避开侧边栏，无背景色 */
.chat-input-area {
  position: fixed;
  bottom: 0;
  left: v-bind('sidebarLeft + "px"'); /* 动态设置左边距 */
  right: 0;
  padding: 20px 0;
  backdrop-filter: blur(10px);
  z-index: 1000;
  transition: left 0.2s ease;
}

.input-wrapper {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 20px;
}

.input-container {
  border: 2px solid;
  border-radius: 24px;
  transition: all 0.3s ease;
}

.input-content {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 16px 20px;
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

.send-button {
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
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
}

.send-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(24, 144, 255, 0.5);
}

.send-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 内联代码样式 - 天蓝色 */
:deep(.inline-code) {
  background: rgba(24, 144, 255, 0.1);
  padding: 3px 8px;
  border-radius: 6px;
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', monospace;
  font-size: 14px;
  color: #096dd9;
}

/* 滚动条样式 - 天蓝色 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(24, 144, 255, 0.3);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(24, 144, 255, 0.5);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .messages-wrapper {
    max-width: 100%;
    padding: 16px;
  }
  
  .input-wrapper {
    padding: 0 16px;
  }
  
  .welcome-title {
    font-size: 28px;
  }
  
  .welcome-subtitle {
    font-size: 16px;
  }
  
  .message-bubble {
    max-width: 85%;
  }
  
  .chat-messages {
    padding-bottom: 140px; /* 移动端留更多空间 */
  }
  
  .chat-input-area {
    left: 0 !important; /* 移动端覆盖侧边栏样式 */
  }
}

@media (max-width: 480px) {
  .input-content {
    padding: 12px 16px;
  }
  
  .send-button {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }
  
  .message-bubble {
    max-width: 90%;
  }
}
</style>