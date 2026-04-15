<template>
  <div class="login-container">
    <div class="login-background">
      <div class="bg-gradient"></div>
      <div class="bg-particles"></div>
    </div>

    <div class="login-wrapper">
      <div class="login-header">
        <h1 class="system-title text-gradient">接口自动化管理</h1>
        <p class="system-subtitle">Intelligent API Testing Management System</p>
      </div>

      <div class="login-card glass-card">
        <div class="login-card-header">
          <h2>用户注册</h2>
          <p>创建您的账号开始使用</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="login-form"
        >
          <el-form-item prop="username" class="form-item-modern">
            <template #label>
              <span class="label-text">用户名</span>
            </template>
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="displayName" class="form-item-modern">
            <template #label>
              <span class="label-text">显示名称</span>
            </template>
            <el-input
              v-model="form.displayName"
              placeholder="请输入显示名称"
              size="large"
              :prefix-icon="UserFilled"
            />
          </el-form-item>

          <el-form-item prop="email" class="form-item-modern">
            <template #label>
              <span class="label-text">邮箱</span>
            </template>
            <el-input
              v-model="form.email"
              placeholder="请输入邮箱"
              size="large"
              :prefix-icon="Message"
            />
          </el-form-item>

          <el-form-item prop="password" class="form-item-modern">
            <template #label>
              <span class="label-text">密码</span>
            </template>
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>

          <el-form-item prop="confirmPassword" class="form-item-modern">
            <template #label>
              <span class="label-text">确认密码</span>
            </template>
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>

          <el-form-item class="form-item-modern">
            <el-button
              type="primary"
              :loading="loading"
              class="login-btn"
              size="large"
              @click="handleRegister"
            >
              <span v-if="!loading">注 册</span>
              <span v-else>注册中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <span class="footer-text">已有账号？</span>
          <el-link type="primary" @click="router.push('/login')">立即登录</el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, UserFilled, Message, Lock } from '@element-plus/icons-vue'
import { authApi } from '@/api/modules/auth/auth'

const router = useRouter()

const formRef = ref()
const loading = ref(false)

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

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
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
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.bg-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 50%, #69c0ff 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.bg-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image:
    radial-gradient(circle at 20% 80%, rgba(255,255,255,0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255,255,255,0.1) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(255,255,255,0.05) 0%, transparent 30%);
}

.login-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.system-title {
  font-family: '楷体', 'KaiTi', serif;
  font-size: 36px;
  font-weight: bold;
  margin: 0 0 10px 0;
  letter-spacing: 2px;
}

.text-gradient {
  background: linear-gradient(135deg, #ffffff 0%, #f0f0f0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.system-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  letter-spacing: 1px;
}

.login-card {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.1),
    0 2px 8px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

.login-card-header {
  text-align: center;
  margin-bottom: 30px;

  h2 {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin: 0 0 8px 0;
  }

  p {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }
}

.glass-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.login-form {
  :deep(.el-form-item__label) {
    padding: 0 0 8px 0;
  }

  :deep(.el-input__wrapper) {
    background: rgba(255, 255, 255, 0.8);
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    padding: 12px 16px;
    border: 1px solid rgba(228, 231, 237, 0.8);
    transition: all 0.2s ease;
  }

  :deep(.el-input__wrapper:hover) {
    border-color: #1890ff;
    box-shadow: 0 2px 12px rgba(24, 144, 255, 0.15);
  }

  :deep(.el-input__wrapper.is-focus) {
    border-color: #1890ff;
    box-shadow:
      0 0 0 3px rgba(24, 144, 255, 0.1),
      0 2px 12px rgba(24, 144, 255, 0.15);
  }

  :deep(.el-input__inner) {
    font-size: 15px;
  }
}

.label-text {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.3);
  transition: all 0.3s ease;
  letter-spacing: 2px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 24px rgba(24, 144, 255, 0.4);
  }

  &:active {
    transform: translateY(0);
  }
}

.login-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid rgba(228, 231, 237, 0.5);

  .footer-text {
    font-size: 14px;
    color: #909399;
  }
}

@media (max-width: 480px) {
  .login-card {
    width: 100%;
    max-width: 360px;
    padding: 30px 20px;
  }

  .system-title {
    font-size: 28px;
  }
}
</style>
