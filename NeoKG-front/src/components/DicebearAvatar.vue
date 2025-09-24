<template>
  <div class="dicebear-avatar" @click="handleClick" :style="{ cursor: clickable ? 'pointer' : 'default' }">
    <img
      :src="avatarUrl"
      :alt="alt"
      :width="size"
      :height="size"
      :style="{
        borderRadius: '50%',
        border: border,
        boxShadow: shadow,
        transition: 'all 0.3s ease',
        objectFit: 'cover'
      }"
      @error="handleImageError"
      @load="handleImageLoad"
    />
    
    <!-- 加载指示器 -->
    <div 
      v-if="loading" 
      :style="{
        position: 'absolute',
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        width: size + 'px',
        height: size + 'px',
        borderRadius: '50%',
        background: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        color: 'white',
        fontSize: '12px'
      }"
    >
      ...
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, readonly } from 'vue'

interface Props {
  seed?: string // 用户ID或用户名，用于生成头像
  avatarStyle?: 'adventurer' | 'avataaars' | 'bottts' | 'pixel-art' | 'croodles' | 'micah' | 'personas'
  size?: number
  format?: 'svg' | 'png'
  clickable?: boolean // 是否可以点击更换头像
  border?: string
  shadow?: string
  alt?: string
  backgroundColor?: string[]
  hairColor?: string[]
  skinColor?: string[]
}

interface Emits {
  (e: 'click', newSeed: string): void
  (e: 'change', newUrl: string): void
}

const props = withDefaults(defineProps<Props>(), {
  seed: '',
  avatarStyle: 'adventurer',
  size: 40,
  format: 'svg',
  clickable: false,
  border: 'none',
  shadow: 'none',
  alt: '用户头像',
  backgroundColor: () => ['b6e3f4', 'c0aede', 'd1d4f9', 'ffd5dc', 'ffdfbf'],
  hairColor: () => ['724133', 'a55728', 'd2691e', 'cc9966', '8b4513'],
  skinColor: () => ['f8d25c', 'fdbcb4', 'ecad80', 'd08b5b', 'ae5d29']
})

const emit = defineEmits<Emits>()

const loading = ref(false)
const currentSeed = ref(props.seed || generateRandomSeed())

// 生成随机种子
function generateRandomSeed(): string {
  return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15)
}

// 生成头像URL
const avatarUrl = computed(() => {
  const baseUrl = `https://api.dicebear.com/7.x/${props.avatarStyle}/${props.format}`
  const params = new URLSearchParams({
    seed: currentSeed.value,
  })
  
  // 添加颜色配置
  if (props.backgroundColor.length > 0) {
    params.append('backgroundColor', props.backgroundColor.join(','))
  }
  
  // 根据不同风格添加特定参数
  if (props.avatarStyle === 'adventurer' || props.avatarStyle === 'avataaars') {
    if (props.hairColor.length > 0) {
      params.append('hairColor', props.hairColor.join(','))
    }
    if (props.skinColor.length > 0) {
      params.append('skinColor', props.skinColor.join(','))
    }
  }
  
  return `${baseUrl}?${params.toString()}`
})

// 处理点击事件
const handleClick = () => {
  if (!props.clickable) return
  
  const newSeed = generateRandomSeed()
  currentSeed.value = newSeed
  emit('click', newSeed)
  emit('change', avatarUrl.value)
}

// 处理图片加载错误
const handleImageError = () => {
  console.error('头像加载失败，尝试重新生成')
  if (props.clickable) {
    currentSeed.value = generateRandomSeed()
  }
}

// 处理图片加载完成
const handleImageLoad = () => {
  loading.value = false
}

// 监听 seed 变化
watch(() => props.seed, (newSeed) => {
  if (newSeed) {
    currentSeed.value = newSeed
  }
}, { immediate: true })

// 暴露方法供父组件调用
const generateNewAvatar = () => {
  const newSeed = generateRandomSeed()
  currentSeed.value = newSeed
  emit('change', avatarUrl.value)
  return newSeed
}

defineExpose({
  generateNewAvatar,
  currentSeed: readonly(currentSeed),
  avatarUrl: readonly(avatarUrl)
})
</script>

<style scoped>
.dicebear-avatar {
  position: relative;
  display: inline-block;
}

.dicebear-avatar:hover img {
  transform: scale(1.05);
}
</style>