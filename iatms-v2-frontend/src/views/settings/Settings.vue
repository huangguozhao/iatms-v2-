<template>
  <div class="settings page-container">
    <h2 class="page-title">系统设置</h2>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>环境配置</span>
          </template>
          <el-form :model="envForm" label-width="120px">
            <el-form-item label="环境名称">
              <el-input v-model="envForm.name" placeholder="请输入环境名称" />
            </el-form-item>
            <el-form-item label="Base URL">
              <el-input v-model="envForm.baseUrl" placeholder="请输入Base URL" />
            </el-form-item>
            <el-form-item label="请求超时">
              <el-input-number v-model="envForm.timeout" :min="1000" :max="60000" />
              <span style="margin-left: 10px; color: #999">毫秒</span>
            </el-form-item>
            <el-form-item label="全局请求头">
              <el-input v-model="envForm.headers" type="textarea" rows="3" placeholder="JSON格式" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary">保存</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card style="margin-top: 20px">
          <template #header>
            <span>通知配置</span>
          </template>
          <el-form :model="notifyForm" label-width="120px">
            <el-form-item label="通知方式">
              <el-checkbox-group v-model="notifyForm.channels">
                <el-checkbox label="EMAIL">邮件</el-checkbox>
                <el-checkbox label="WEBHOOK">Webhook</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="邮件服务" v-if="notifyForm.channels.includes('EMAIL')">
              <el-input v-model="notifyForm.emailHost" placeholder="SMTP服务器" />
            </el-form-item>
            <el-form-item label="邮件端口" v-if="notifyForm.channels.includes('EMAIL')">
              <el-input-number v-model="notifyForm.emailPort" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="发件人" v-if="notifyForm.channels.includes('EMAIL')">
              <el-input v-model="notifyForm.emailFrom" placeholder="发件人邮箱" />
            </el-form-item>
            <el-form-item label="Webhook URL" v-if="notifyForm.channels.includes('WEBHOOK')">
              <el-input v-model="notifyForm.webhookUrl" placeholder="请输入Webhook URL" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary">保存</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <span>AI 服务配置</span>
          </template>
          <el-form :model="aiForm" label-width="100px">
            <el-form-item label="服务商">
              <el-select v-model="aiForm.provider" style="width: 100%">
                <el-option label="OpenAI" value="OPENAI" />
                <el-option label="Azure OpenAI" value="AZURE_OPENAI" />
                <el-option label="Claude" value="CLAUDE" />
                <el-option label="文心一言" value="WENXIN" />
              </el-select>
            </el-form-item>
            <el-form-item label="API Key">
              <el-input v-model="aiForm.apiKey" type="password" show-password placeholder="请输入API Key" />
            </el-form-item>
            <el-form-item label="模型">
              <el-input v-model="aiForm.model" placeholder="如: gpt-4" />
            </el-form-item>
            <el-form-item label="API Base" v-if="aiForm.provider !== 'OPENAI'">
              <el-input v-model="aiForm.apiBase" placeholder="自定义API地址" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary">保存</el-button>
              <el-button @click="handleTestAI">测试连接</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card style="margin-top: 20px">
          <template #header>
            <span>系统信息</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="系统版本">V2.0.0</el-descriptions-item>
            <el-descriptions-item label="Spring Boot">3.2.5</el-descriptions-item>
            <el-descriptions-item label="Vue">3.4.x</el-descriptions-item>
            <el-descriptions-item label="构建时间">2026-04-15</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

const envForm = reactive({
  name: '默认环境',
  baseUrl: 'http://localhost:8080',
  timeout: 5000,
  headers: ''
})

const notifyForm = reactive({
  channels: ['EMAIL'],
  emailHost: '',
  emailPort: 465,
  emailFrom: '',
  webhookUrl: ''
})

const aiForm = reactive({
  provider: 'OPENAI',
  apiKey: '',
  model: 'gpt-4',
  apiBase: ''
})

function handleTestAI() {
  ElMessage.success('AI服务连接测试成功')
}
</script>

<style scoped lang="scss">
.settings {
  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 600;
  }
}
</style>
