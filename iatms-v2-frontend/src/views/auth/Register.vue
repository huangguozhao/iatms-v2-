<template>
  <div class="register-page">
    <!-- 左侧信息面板 -->
    <div class="register-page__info-panel">
      <div class="info-panel__content">
        <h1 class="info-panel__title">加入接口自动化管理平台</h1>
        <p class="info-panel__subtitle">
          快速创建账号，开启高效的接口测试之旅
        </p>

        <div class="info-panel__features">
          <div class="feature-item" style="--delay: 0s">
            <el-icon class="feature-item__icon"><Check /></el-icon>
            <span class="feature-item__text">一键创建项目，快速上手</span>
          </div>
          <div class="feature-item" style="--delay: 0.1s">
            <el-icon class="feature-item__icon"><Check /></el-icon>
            <span class="feature-item__text">团队协作，共享测试资源</span>
          </div>
          <div class="feature-item" style="--delay: 0.2s">
            <el-icon class="feature-item__icon"><Check /></el-icon>
            <span class="feature-item__text">自动化测试，提升效率</span>
          </div>
          <div class="feature-item" style="--delay: 0.3s">
            <el-icon class="feature-item__icon"><Check /></el-icon>
            <span class="feature-item__text">AI 智能诊断，快速定位问题</span>
          </div>
        </div>

        <div class="info-panel__chart">
          <el-icon class="chart-icon"><TrendCharts /></el-icon>
        </div>
      </div>
    </div>

    <!-- 右侧注册表单 -->
    <div class="register-page__form-panel">
      <div class="form-panel__content">
        <!-- 头部 -->
        <div class="form-panel__header">
          <div class="header__logo">
            <div class="logo-icon">A</div>
          </div>
          <h2 class="header__title">接口自动化管理</h2>
        </div>

        <!-- 欢迎信息 -->
        <div class="form-panel__welcome">
          <h3 class="welcome__title">创建账号</h3>
          <p class="welcome__subtitle">填写以下信息完成注册</p>
        </div>

        <!-- 注册表单 -->
        <el-form ref="formRef" :model="form" :rules="rules" class="register-form" @submit.prevent="handleRegister">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
              :disabled="isLoading"
            >
              <template #label><span class="label-text">用户名</span></template>
            </el-input>
          </el-form-item>

          <el-form-item prop="displayName">
            <el-input
              v-model="form.displayName"
              placeholder="请输入显示名称"
              size="large"
              :prefix-icon="UserFilled"
              :disabled="isLoading"
            >
              <template #label><span class="label-text">显示名称</span></template>
            </el-input>
          </el-form-item>

          <el-form-item prop="email">
            <el-input
              v-model="form.email"
              placeholder="请输入邮箱"
              size="large"
              :prefix-icon="Message"
              :disabled="isLoading"
            >
              <template #label><span class="label-text">邮箱</span></template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              :disabled="isLoading"
            >
              <template #label><span class="label-text">密码</span></template>
              <template #suffix>
                <el-icon class="password-toggle" @click="togglePasswordVisibility">
                  <View v-if="!showPassword" />
                  <Hide v-else />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请再次输入密码"
              size="large"
              :prefix-icon="Lock"
              :disabled="isLoading"
            >
              <template #label><span class="label-text">确认密码</span></template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" size="large" class="login-button" :loading="isLoading" @click="handleRegister">
              {{ isLoading ? '注册中...' : '注 册' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-panel__footer">
          <p class="footer-text">
            已有账号?
            <el-link type="primary" @click="router.push('/login')">立即登录</el-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, TrendCharts, User, UserFilled, Message, Lock, View, Hide } from '@element-plus/icons-vue'
import { authApi } from '@/api/modules/auth/auth'

const router = useRouter()

const formRef = ref()
const isLoading = ref(false)
const showPassword = ref(false)

const form = reactive({
  username: '',
  displayName: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  displayName: [
    { required: true, message: '请输入显示名称', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  isLoading.value = true
  try {
    await authApi.register({
      username: form.username,
      displayName: form.displayName,
      email: form.email,
      password: form.password
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error: any) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    isLoading.value = false
  }
}
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

/* 表单样式 */
.register-form {
  animation: formFadeIn 0.8s ease-out 1s both;
}

@keyframes formFadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.label-text {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
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

/* 移动端适配 */
@media (max-width: 768px) {
  .register-page { flex-direction: column; }
  .register-page__info-panel {
    flex: none;
    height: 25vh;
    min-height: 200px;
    padding: 24px 20px;
  }
  .register-page__form-panel {
    flex: 1;
    min-height: 75vh;
    padding: 20px 16px;
  }
  .form-panel__content {
    padding: 28px 24px;
    margin-top: 20px;
  }
  .info-panel__features,
  .info-panel__subtitle,
  .info-panel__chart { display: none; }
  .info-panel__title { font-size: 24px; margin-bottom: 12px; }
  .form-panel__header { margin-bottom: 24px; }
  .logo-icon { width: 48px; height: 48px; font-size: 24px; }
  .header__title { font-size: 20px; }
  .form-panel__welcome { margin-bottom: 24px; }
  .welcome__title { font-size: 22px; }
  .form-panel__footer { margin-top: 20px; }
}

@media (max-width: 480px) {
  .register-page__info-panel { height: 22vh; min-height: 160px; }
  .register-page__form-panel { min-height: 78vh; padding: 16px; }
  .form-panel__content { padding: 24px 20px; margin-top: 16px; }
  .info-panel__title { font-size: 20px; }
  .logo-icon { width: 44px; height: 44px; font-size: 22px; }
  .header__title { font-size: 18px; }
  .welcome__title { font-size: 20px; }
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
