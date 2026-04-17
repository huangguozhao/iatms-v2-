<template>
  <div v-if="visible" class="tab-content params-content">
    <div class="params-card">
      <!-- Headers -->
      <div class="params-header">
        <h3 class="params-title">Headers</h3>
      </div>
      <el-table :data="localHeaderParams" class="params-table" border size="small">
        <el-table-column label="参数名" width="200">
          <template #default="{ row, $index }">
            <el-input v-model="localHeaderParams[$index].name" size="small" placeholder="参数名" @input="emitUpdate" />
          </template>
        </el-table-column>
        <el-table-column label="参数值" width="200">
          <template #default="{ row, $index }">
            <el-input v-model="localHeaderParams[$index].value" size="small" placeholder="参数值" @input="emitUpdate" />
          </template>
        </el-table-column>
        <el-table-column label="描述">
          <template #default="{ row, $index }">
            <el-input v-model="localHeaderParams[$index].description" size="small" placeholder="描述" @input="emitUpdate" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ $index }">
            <el-button size="small" text type="danger" @click="removeHeader($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="add-param-btn">
        <el-button size="small" @click="addHeader">+ 添加参数</el-button>
      </div>

      <!-- Query Params -->
      <div class="params-header">
        <h3 class="params-title">Query Params</h3>
      </div>
      <el-table :data="localQueryParams" class="params-table" border size="small">
        <el-table-column label="参数名" width="200">
          <template #default="{ row, $index }">
            <el-input v-model="localQueryParams[$index].name" size="small" placeholder="参数名" @input="emitUpdate" />
          </template>
        </el-table-column>
        <el-table-column label="参数值" width="200">
          <template #default="{ row, $index }">
            <el-input v-model="localQueryParams[$index].value" size="small" placeholder="参数值" @input="emitUpdate" />
          </template>
        </el-table-column>
        <el-table-column label="描述">
          <template #default="{ row, $index }">
            <el-input v-model="localQueryParams[$index].description" size="small" placeholder="描述" @input="emitUpdate" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ $index }">
            <el-button size="small" text type="danger" @click="removeQueryParam($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="add-param-btn">
        <el-button size="small" @click="addQueryParam">+ 添加参数</el-button>
      </div>

      <!-- Body -->
      <div class="params-header params-header--with-controls">
        <h3 class="params-title">Body</h3>
        <el-radio-group v-model="localBodyType" size="small" class="body-type-selector" @change="emitBodyType">
          <el-radio-button label="json">JSON</el-radio-button>
          <el-radio-button label="form-data">form-data</el-radio-button>
          <el-radio-button label="raw">raw</el-radio-button>
        </el-radio-group>
      </div>

      <!-- JSON Body -->
      <div v-if="localBodyType === 'json'" class="body-section">
        <el-table :data="localBodyParams" class="params-table" border size="small">
          <el-table-column label="变量名" width="200">
            <template #default="{ row, $index }">
              <el-input v-model="localBodyParams[$index].name" size="small" placeholder="变量名" @input="emitBodyParams" />
            </template>
          </el-table-column>
          <el-table-column label="变量值" width="200">
            <template #default="{ row, $index }">
              <el-input v-model="localBodyParams[$index].value" size="small" placeholder="变量值" @input="emitBodyParams" />
            </template>
          </el-table-column>
          <el-table-column label="描述">
            <template #default="{ row, $index }">
              <el-input v-model="localBodyParams[$index].description" size="small" placeholder="描述" @input="emitBodyParams" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="{ $index }">
              <el-button size="small" text type="danger" @click="removeBodyParam($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="add-param-btn">
          <el-button size="small" @click="addBodyParam">+ 添加变量</el-button>
        </div>
      </div>

      <!-- Raw Body -->
      <div v-else-if="localBodyType === 'raw'" class="body-section">
        <el-input v-model="localRawBody" type="textarea" :rows="10" placeholder="Raw body content" @input="emitRawBody" />
      </div>

      <!-- Form Data Body -->
      <div v-else-if="localBodyType === 'form-data'" class="body-section">
        <el-table :data="localFormDataParams" class="params-table" border size="small">
          <el-table-column label="参数名" width="200">
            <template #default="{ row, $index }">
              <el-input v-model="localFormDataParams[$index].name" size="small" placeholder="参数名" @input="emitFormDataParams" />
            </template>
          </el-table-column>
          <el-table-column label="参数值" width="200">
            <template #default="{ row, $index }">
              <el-input v-model="localFormDataParams[$index].value" size="small" placeholder="参数值" @input="emitFormDataParams" />
            </template>
          </el-table-column>
          <el-table-column label="描述">
            <template #default="{ row, $index }">
              <el-input v-model="localFormDataParams[$index].description" size="small" placeholder="描述" @input="emitFormDataParams" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="{ $index }">
              <el-button size="small" text type="danger" @click="removeFormDataParam($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="add-param-btn">
          <el-button size="small" @click="addFormDataParam">+ 添加参数</el-button>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="params-actions">
        <el-button @click="$emit('save-params')">保存参数</el-button>
        <el-button @click="$emit('format-params')">格式化</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: true },
  headerParams: { type: Array, default: () => [] },
  queryParams: { type: Array, default: () => [] },
  bodyParams: { type: Array, default: () => [] },
  formDataParams: { type: Array, default: () => [] },
  rawBody: { type: String, default: '' },
  bodyType: { type: String, default: 'json' }
})

const emit = defineEmits([
  'update:headerParams',
  'update:queryParams',
  'update:bodyParams',
  'update:formDataParams',
  'update:rawBody',
  'update:bodyType',
  'save-params',
  'format-params'
])

// Local copies
const localHeaderParams = ref(JSON.parse(JSON.stringify(props.headerParams || [])))
const localQueryParams = ref(JSON.parse(JSON.stringify(props.queryParams || [])))
const localBodyParams = ref(JSON.parse(JSON.stringify(props.bodyParams || [])))
const localFormDataParams = ref(JSON.parse(JSON.stringify(props.formDataParams || [])))
const localRawBody = ref(props.rawBody || '')
const localBodyType = ref(props.bodyType || 'json')

// Watch for prop changes
watch(() => props.headerParams, (v) => {
  localHeaderParams.value = JSON.parse(JSON.stringify(v || []))
}, { deep: true })
watch(() => props.queryParams, (v) => {
  localQueryParams.value = JSON.parse(JSON.stringify(v || []))
}, { deep: true })
watch(() => props.bodyParams, (v) => {
  localBodyParams.value = JSON.parse(JSON.stringify(v || []))
}, { deep: true })
watch(() => props.formDataParams, (v) => {
  localFormDataParams.value = JSON.parse(JSON.stringify(v || []))
}, { deep: true })
watch(() => props.rawBody, (v) => {
  localRawBody.value = v || ''
})
watch(() => props.bodyType, (v) => {
  localBodyType.value = v || 'json'
})

// Emit updates
const emitUpdate = () => {
  emit('update:headerParams', JSON.parse(JSON.stringify(localHeaderParams.value)))
  emit('update:queryParams', JSON.parse(JSON.stringify(localQueryParams.value)))
}

const emitBodyParams = () => {
  emit('update:bodyParams', JSON.parse(JSON.stringify(localBodyParams.value)))
}

const emitFormDataParams = () => {
  emit('update:formDataParams', JSON.parse(JSON.stringify(localFormDataParams.value)))
}

const emitRawBody = () => {
  emit('update:rawBody', localRawBody.value)
}

const emitBodyType = () => {
  emit('update:bodyType', localBodyType.value)
}

// Header methods
const addHeader = () => {
  localHeaderParams.value.push({ name: '', value: '', description: '' })
  emitUpdate()
}
const removeHeader = (index: number) => {
  localHeaderParams.value.splice(index, 1)
  emitUpdate()
}

// Query param methods
const addQueryParam = () => {
  localQueryParams.value.push({ name: '', value: '', description: '' })
  emitUpdate()
}
const removeQueryParam = (index: number) => {
  localQueryParams.value.splice(index, 1)
  emitUpdate()
}

// Body param methods
const addBodyParam = () => {
  localBodyParams.value.push({ name: '', value: '', description: '' })
  emitBodyParams()
}
const removeBodyParam = (index: number) => {
  localBodyParams.value.splice(index, 1)
  emitBodyParams()
}

// Form data methods
const addFormDataParam = () => {
  localFormDataParams.value.push({ name: '', value: '', description: '' })
  emitFormDataParams()
}
const removeFormDataParam = (index: number) => {
  localFormDataParams.value.splice(index, 1)
  emitFormDataParams()
}
</script>

<style scoped lang="scss">
$card-radius: 12px;

.params-card {
  background: #fff;
  border-radius: $card-radius;
  padding: 16px;
  box-shadow: 0 6px 18px rgba(16, 24, 40, 0.04);
  transition: transform 0.18s ease, box-shadow 0.18s ease;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 14px 36px rgba(16, 24, 40, 0.08);
  }
}

.params-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  margin-top: 24px;

  &:first-of-type {
    margin-top: 0;
  }
}

.params-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.body-type-selector {
  margin-left: auto;
}

.add-param-btn {
  margin-bottom: 24px;
  display: flex;
  justify-content: flex-start;
}

.params-actions {
  display: flex;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid #e4e7ed;
  margin-top: 24px;
}

.body-section {
  margin-top: 16px;
}

:deep(.el-input__inner) {
  border: none;
  background: transparent;
  transition: box-shadow 0.12s ease;
}

:deep(.el-input__inner:focus) {
  box-shadow: 0 8px 18px rgba(64, 158, 255, 0.06);
  border-radius: 4px;
  background: #fff;
}
</style>
