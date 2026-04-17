<template>
  <div v-if="visible" class="tab-content">
    <div class="basic-info-card">
      <!-- 项目选择 -->
      <div class="form-section">
        <div class="section-title">所属项目</div>
        <el-select
          v-model="localApiData.projectId"
          placeholder="请选择项目"
          class="form-select"
          :loading="projectsLoading"
          @change="onProjectChange"
        >
          <el-option
            v-for="project in availableProjects"
            :key="project.id"
            :label="project.name"
            :value="project.id"
          />
        </el-select>
      </div>

      <!-- 所属模块 -->
      <div class="form-section">
        <div class="section-title">所属模块</div>
        <el-select
          v-model="localApiData.moduleId"
          placeholder="请选择模块"
          class="form-select"
          :loading="modulesLoading"
          @change="onModuleChange"
        >
          <el-option
            v-for="module in availableModules"
            :key="module.id"
            :label="module.name"
            :value="module.id"
          />
        </el-select>
      </div>

      <!-- 接口名称 -->
      <div class="form-section">
        <div class="section-title">接口名称</div>
        <el-input v-model="localApiData.name" placeholder="请输入接口名称" />
      </div>

      <!-- Base Path -->
      <div class="form-section">
        <div class="section-title">Base URL</div>
        <el-input v-model="localApiData.basePath" placeholder="如：https://api.example.com" />
      </div>

      <!-- 接口路径 -->
      <div class="form-section">
        <div class="section-title">接口路径</div>
        <el-input v-model="localApiData.url" placeholder="如：/api/user/login" />
      </div>

      <!-- 请求方法 -->
      <div class="form-section">
        <div class="section-title">请求方法</div>
        <el-radio-group v-model="localApiData.httpMethod">
          <el-radio v-for="m in methods" :key="m" :label="m">{{ m }}</el-radio>
        </el-radio-group>
      </div>

      <!-- 接口描述 -->
      <div class="form-section">
        <div class="section-title">接口描述</div>
        <el-input v-model="localApiData.description" type="textarea" :rows="3" placeholder="请输入接口描述" />
      </div>

      <!-- 标签 -->
      <div class="form-section">
        <div class="section-title">标签</div>
        <div class="tag-list">
          <el-tag
            v-for="(tag, index) in localApiData.tags"
            :key="index"
            closable
            @close="removeTag(index)"
          >
            {{ tag }}
          </el-tag>
          <el-button v-if="!showTagInput" size="small" text @click="showTagInput = true">
            + 添加标签
          </el-button>
          <el-input
            v-if="showTagInput"
            v-model="newTag"
            size="small"
            placeholder="输入标签名"
            style="width: 100px"
            @keyup.enter="addTag"
            @blur="addTag"
          />
        </div>
      </div>

      <!-- 认证方式 -->
      <div class="form-section">
        <div class="section-title">认证方式</div>
        <el-select v-model="localApiData.authType" placeholder="请选择认证方式" style="width: 100%">
          <el-option v-for="auth in authOptions" :key="auth.value" :label="auth.label" :value="auth.value" />
        </el-select>
      </div>

      <!-- 超时时间 -->
      <div class="form-section">
        <div class="section-title">超时时间（秒）</div>
        <el-input-number v-model="localApiData.timeoutSeconds" :min="1" :max="300" />
      </div>

      <!-- 请求体类型 -->
      <div class="form-section">
        <div class="section-title">请求体类型</div>
        <el-select v-model="localApiData.requestBodyType" placeholder="请选择请求体类型" style="width: 100%">
          <el-option label="JSON" value="json" />
          <el-option label="Form Data" value="form-data" />
          <el-option label="X-www-form-urlencoded" value="x-www-form-urlencoded" />
          <el-option label="XML" value="xml" />
          <el-option label="Raw" value="raw" />
          <el-option label="None" value="none" />
        </el-select>
      </div>

      <!-- 状态 -->
      <div class="form-section">
        <div class="section-title">状态</div>
        <el-select v-model="localApiData.status" placeholder="请选择状态" style="width: 100%">
          <el-option label="激活" value="active" />
          <el-option label="未激活" value="inactive" />
          <el-option label="已废弃" value="deprecated" />
        </el-select>
      </div>

      <!-- 版本 -->
      <div class="form-section">
        <div class="section-title">版本</div>
        <el-input v-model="localApiData.version" placeholder="如：1.0" />
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button type="primary" @click="$emit('save')">保存修改</el-button>
        <el-button @click="$emit('test')">执行测试</el-button>
        <el-button type="danger" @click="$emit('delete')">删除接口</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { ApiData } from '@/composables/useApiData'

const props = defineProps({
  visible: { type: Boolean, default: true },
  apiData: { type: Object as () => ApiData, required: true },
  availableProjects: { type: Array, default: () => [] },
  projectsLoading: { type: Boolean, default: false },
  availableModules: { type: Array, default: () => [] },
  modulesLoading: { type: Boolean, default: false }
})

const emit = defineEmits(['project-change', 'module-change', 'save', 'test', 'delete'])

// 使用 v-model 直接绑定到父组件的 apiData，不使用本地副本
const localApiData = props.apiData

const showTagInput = ref(false)
const newTag = ref('')

const methods = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH']

const authOptions = [
  { label: '无认证', value: 'none' },
  { label: 'Bearer Token', value: 'bearer' },
  { label: 'Basic Auth', value: 'basic' },
  { label: 'API Key', value: 'api_key' },
  { label: 'OAuth 2.0', value: 'oauth2' }
]

function onProjectChange(val: number) {
  // 切换项目时，清空模块选择
  localApiData.moduleId = null
  localApiData.moduleName = ''
  emit('project-change', val)
}

function onModuleChange(val: number) {
  const module = availableModules.find(m => m.id === val)
  localApiData.moduleName = module?.name || ''
  emit('module-change', val)
}

function addTag() {
  if (newTag.value) {
    if (!localApiData.tags) localApiData.tags = []
    if (!localApiData.tags.includes(newTag.value)) {
      localApiData.tags.push(newTag.value)
    }
  }
  newTag.value = ''
  showTagInput.value = false
}

function removeTag(index: number) {
  localApiData.tags.splice(index, 1)
}
</script>

<style scoped lang="scss">
.basic-info-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 6px 20px rgba(16, 24, 40, 0.06);
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 30px rgba(16, 24, 40, 0.12);
  }
}

.form-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-select {
  width: 100%;
}

.readonly-field {
  padding: 0 11px;
  height: 32px;
  line-height: 32px;
  color: #606266;
  font-size: 14px;
  background: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.form-actions {
  display: flex;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
  margin-top: 20px;
}

.tag-list {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}
</style>
