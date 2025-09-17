import { ref } from 'vue'

// 全局暗黑模式状态
export const isDarkMode = ref(false)

// 切换暗黑模式的函数
export const toggleDarkMode = (): void => {
  isDarkMode.value = !isDarkMode.value
  // 保存到localStorage
  localStorage.setItem('darkMode', JSON.stringify(isDarkMode.value))
  
  // 更新body主题
  updateBodyTheme()
}

// 更新body主题
const updateBodyTheme = (): void => {
  if (isDarkMode.value) {
    document.body.style.backgroundColor = '#141414'
    document.documentElement.style.backgroundColor = '#141414'
    document.body.classList.add('dark-theme')
  } else {
    document.body.style.backgroundColor = '#ffffff'
    document.documentElement.style.backgroundColor = '#ffffff'
    document.body.classList.remove('dark-theme')
  }
}

// 初始化主题
export const initTheme = (): void => {
  const savedDarkMode = localStorage.getItem('darkMode')
  if (savedDarkMode) {
    isDarkMode.value = JSON.parse(savedDarkMode)
  }
  
  // 设置初始背景色
  updateBodyTheme()
}