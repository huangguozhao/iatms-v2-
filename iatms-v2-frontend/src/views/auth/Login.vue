<template>
  <div class="login-page" :class="{ 'keyboard-visible': keyboardVisible, 'low-end-device': isLowEndDevice }">
    <!-- 左侧信息面板 -->
    <div class="login-page__info-panel">
      <div class="info-panel__content">
        <h1 class="info-panel__title">接口自动化管理平台</h1>
        <p class="info-panel__subtitle">
          专业的企业级接口测试解决方案,提升测试效率,保障接口质量
        </p>

        <div class="info-panel__features">
          <div class="feature-item" style="--delay: 0s">
            <el-icon class="feature-item__icon"><Check /></el-icon>
            <span class="feature-item__text">可视化接口测试与管理</span>
          </div>
          <div class="feature-item" style="--delay: 0.1s">
            <el-icon class="feature-item__icon"><Check /></el-icon>
            <span class="feature-item__text">自动化测试与持续集成</span>
          </div>
          <div class="feature-item" style="--delay: 0.2s">
            <el-icon class="feature-item__icon"><Check /></el-icon>
            <span class="feature-item__text">团队协作与权限管理</span>
          </div>
          <div class="feature-item" style="--delay: 0.3s">
            <el-icon class="feature-item__icon"><Check /></el-icon>
            <span class="feature-item__text">详尽的测试报告与分析</span>
          </div>
        </div>

        <div class="info-panel__chart">
          <el-icon class="chart-icon"><TrendCharts /></el-icon>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-page__form-panel">
      <div class="form-panel__content">
        <div class="form-panel__header">
          <div class="header__logo">
            <div class="logo-icon">A</div>
          </div>
          <h2 class="header__title">接口自动化管理</h2>
        </div>

        <div class="form-panel__welcome">
          <h3 class="welcome__title">欢迎回来</h3>
          <p class="welcome__subtitle">请输入您的账号密码登录系统</p>
        </div>

        <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form" @submit.prevent="handleLogin">
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名或邮箱"
              size="large"
              :prefix-icon="User"
              :disabled="isLoading"
              autocomplete="username"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              :disabled="isLoading"
              autocomplete="current-password"
              @keyup.enter="handleLogin"
            >
              <template #suffix>
                <el-icon class="password-toggle" @click="togglePasswordVisibility">
                  <View v-if="!showPassword" />
                  <Hide v-else />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="loginForm.rememberMe" :disabled="isLoading">记住我</el-checkbox>
          </div>

          <el-form-item>
            <el-button type="primary" size="large" class="login-button" :loading="isLoading" @click="handleLogin">
              {{ isLoading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-panel__footer">
          <p class="footer-text">
            还没有账号?
            <el-link type="primary" @click="router.push('/register')">立即注册</el-link>
          </p>
        </div>
      </div>
    </div>

    <!-- 登录成功加载动画 -->
    <LoadingTransition
      v-model:visible="showLoadingTransition"
      title="欢迎回来"
      subtitle="正在为您准备系统..."
      :duration="2500"
      @complete="handleLoadingComplete"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, TrendCharts, User, Lock, View, Hide } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import LoadingTransition from '@/components/common/LoadingTransition.vue'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const isLoading = ref(false)
const showPassword = ref(false)
const showLoadingTransition = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 设备检测
const isMobile = ref(false)
const keyboardVisible = ref(false)
const viewportHeight = ref(window.innerHeight)
const isLowEndDevice = ref(false)

const detectLowEndDevice = () => {
  const connection = (navigator as any).connection || (navigator as any).mozConnection || (navigator as any).webkitConnection
  const isSlowConnection = connection && (connection.effectiveType === 'slow-2g' || connection.effectiveType === '2g')
  const isLowCPU = navigator.hardwareConcurrency && navigator.hardwareConcurrency <= 2
  const isLowMemory = (navigator as any).deviceMemory && (navigator as any).deviceMemory <= 2
  return isSlowConnection || isLowCPU || isLowMemory
}

const throttle = (fn: Function, limit: number) => {
  let inThrottle = false
  return (...args: any[]) => {
    if (!inThrottle) {
      fn(...args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

const detectMobile = () => /android|avantgo|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i.test(navigator.userAgent)

const handleViewportChange = throttle(() => {
  const currentHeight = window.innerHeight
  const heightDiff = viewportHeight.value - currentHeight
  keyboardVisible.value = heightDiff > 150
  viewportHeight.value = currentHeight
}, 100)

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value
}

const handleLoadingComplete = () => {
  showLoadingTransition.value = false
  router.push('/dashboard')
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return

    isLoading.value = true
    await userStore.login(loginForm.username, loginForm.password)
    ElMessage.success('登录成功')

    if (loginForm.rememberMe) {
      localStorage.setItem('rememberedUsername', loginForm.username)
    } else {
      localStorage.removeItem('rememberedUsername')
    }

    showLoadingTransition.value = true
  } catch (error: any) {
    console.error('登录错误:', error)
    ElMessage.error(error.message || '登录失败，请稍后重试')
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    router.push('/dashboard')
    return
  }

  isMobile.value = detectMobile()
  isLowEndDevice.value = detectLowEndDevice()

  const rememberedUsername = localStorage.getItem('rememberedUsername')
  if (rememberedUsername) {
    loginForm.username = rememberedUsername
    loginForm.rememberMe = true
  }

  if (isMobile.value) {
    window.addEventListener('resize', handleViewportChange, { passive: true })
    viewportHeight.value = window.innerHeight
  }
})

onUnmounted(() => {
  if (isMobile.value) {
    window.removeEventListener('resize', handleViewportChange)
  }
})
</script>

<style scoped lang="scss">
/* 信息面板内容 */
.info-panel__content {
  color: white;
  text-align: center;
  z-index: 2;
  position: relative;
  animation: contentFadeIn 1s ease-out;
}

.info-panel__title {
  font-size: 48px;
  font-weight: bold;
  margin: 0 0 20px 0;
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  animation: titleGlow 3s ease-in-out infinite alternate;
}

.info-panel__subtitle {
  font-size: 18px;
  margin: 0 0 40px 0;
  opacity: 0.9;
  line-height: 1.6;
  animation: subtitleSlideIn 0.8s ease-out 0.3s both;
}

.info-panel__features {
  text-align: left;
  max-width: 400px;
  margin: 0 auto 40px;
  animation: featuresFadeIn 0.8s ease-out 0.6s both;
}

.feature-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  font-size: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: featureItemSlideIn 0.6s ease-out calc(0.8s + var(--delay, 0)) both;
}

.feature-item:hover {
  transform: translateY(-2px) scale(1.02);
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.feature-item__icon {
  margin-right: 16px;
  font-size: 20px;
  transition: transform 0.3s ease;
}

.feature-item:hover .feature-item__icon {
  transform: scale(1.1) rotate(5deg);
}

.feature-item__text {
  flex: 1;
  font-weight: 400;
}

.info-panel__chart {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
}

.chart-icon {
  font-size: 120px;
  opacity: 0.3;
}

/* 头部区域 */
.form-panel__header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
  animation: headerFadeIn 0.6s ease-out 0.6s both;
}

.header__logo {
  margin-right: 16px;
}

.logo-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: bold;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: logoFloat 4s ease-in-out infinite;
}

.logo-icon:hover {
  transform: scale(1.05) rotate(5deg);
  box-shadow: 0 12px 32px rgba(64, 158, 255, 0.4);
}

.header__title {
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
  margin: 0;
}

/* 欢迎信息 */
.form-panel__welcome {
  margin-bottom: 32px;
  text-align: center;
  animation: welcomeFadeIn 0.8s ease-out 0.8s both;
}

.welcome__title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.welcome__subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* 表单 */
.login-form {
  margin-bottom: 32px;
  animation: formFadeIn 0.8s ease-out 1s both;
}

@keyframes formFadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

/* 密码切换 */
.password-toggle {
  cursor: pointer;
  color: #c0c4cc;
  transition: all 0.3s;
}

.password-toggle:hover {
  color: #409eff;
  transform: scale(1.1);
}

/* 选项区域 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  animation: optionsFadeIn 0.6s ease-out 1.2s both;
}

/* 页脚 */
.form-panel__footer {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid rgba(228, 231, 237, 0.5);
}

.footer-text {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* 移动端小屏幕适配 */
@media (max-width: 768px) {
  .info-panel__title { font-size: 24px; margin-bottom: 12px; }
  .form-panel__header { margin-bottom: 24px; }
  .logo-icon { width: 48px; height: 48px; font-size: 24px; }
  .header__title { font-size: 20px; }
  .form-panel__welcome { margin-bottom: 24px; }
  .welcome__title { font-size: 22px; }
  .login-form { margin-bottom: 24px; }
  .login-form :deep(.el-form-item) { margin-bottom: 20px; }
  .form-panel__footer { margin-top: 20px; }
}

@media (max-width: 480px) {
  .info-panel__title { font-size: 20px; }
  .logo-icon { width: 44px; height: 44px; font-size: 22px; }
  .header__title { font-size: 18px; }
  .welcome__title { font-size: 20px; }
  .login-form :deep(.el-form-item) { margin-bottom: 18px; }
}

/* 移动端触摸优化 */
@media (hover: none) and (pointer: coarse) {
  .login-button:active {
    transform: scale(0.98);
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.25);
  }
  .password-toggle:active {
    transform: scale(1.1);
    background-color: rgba(64, 158, 255, 0.1);
    border-radius: 6px;
  }
}
</style>
