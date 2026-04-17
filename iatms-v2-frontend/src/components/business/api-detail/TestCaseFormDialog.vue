<template>
  <el-dialog
    v-model="visible"
    title="添加测试用例"
    width="900px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form-item label="用例名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入用例名称" />
          </el-form-item>

          <el-form-item label="用例编码" prop="caseCode">
            <el-input v-model="formData.caseCode" placeholder="留空则自动生成" />
          </el-form-item>

          <el-form-item label="用例描述">
            <el-input v-model="formData.description" type="textarea" :rows="2" />
          </el-form-item>

          <el-form-item label="优先级" prop="priority">
            <el-select v-model="formData.priority" style="width: 100%">
              <el-option v-for="p in priorityOptions" :key="p.value" :label="p.label" :value="p.value" />
            </el-select>
          </el-form-item>

          <el-form-item label="严重程度" prop="severity">
            <el-select v-model="formData.severity" style="width: 100%">
              <el-option v-for="s in severityOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </el-form-item>

          <el-form-item label="是否启用">
            <el-switch v-model="formData.isEnabled" />
          </el-form-item>
        </el-tab-pane>

        <!-- 测试步骤 -->
        <el-tab-pane label="测试步骤" name="steps">
          <div class="steps-section">
            <div class="steps-header">
              <span>测试步骤列表</span>
              <el-button size="small" type="primary" @click="addStep">+ 添加步骤</el-button>
            </div>

            <div v-if="formData.testSteps.length > 0" class="steps-list">
              <div v-for="(step, index) in formData.testSteps" :key="index" class="step-item">
                <span class="step-num">{{ index + 1 }}</span>
                <el-input v-model="step.operation" placeholder="操作步骤" style="flex: 1" />
                <el-input v-model="step.expected" placeholder="预期结果" style="flex: 1" />
                <el-button type="danger" text @click="removeStep(index)">删除</el-button>
              </div>
            </div>

            <el-empty v-else description="暂无测试步骤" :image-size="60" />
          </div>
        </el-tab-pane>

        <!-- 请求参数 -->
        <el-tab-pane label="请求参数" name="request">
          <el-form-item label="前置条件">
            <el-input v-model="formData.preConditionsStr" type="textarea" :rows="3" placeholder="JSON格式" />
          </el-form-item>

          <div class="param-section">
            <div class="param-title">查询参数</div>
            <el-table :data="formData.overrideQueryParams" border size="small">
              <el-table-column label="参数名" width="150">
                <template #default="{ row }">
                  <el-input v-model="row.name" size="small" />
                </template>
              </el-table-column>
              <el-table-column label="参数值" width="150">
                <template #default="{ row }">
                  <el-input v-model="row.value" size="small" />
                </template>
              </el-table-column>
              <el-table-column label="描述">
                <template #default="{ row }">
                  <el-input v-model="row.description" size="small" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="60">
                <template #default="{ $index }">
                  <el-button type="danger" text size="small" @click="formData.overrideQueryParams.splice($index, 1)">删</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button size="small" @click="formData.overrideQueryParams.push({ name: '', value: '', description: '' })">
              + 添加
            </el-button>
          </div>

          <div class="param-section">
            <div class="param-title">请求头</div>
            <el-table :data="formData.overrideHeaders" border size="small">
              <el-table-column label="名称" width="150">
                <template #default="{ row }">
                  <el-input v-model="row.name" size="small" />
                </template>
              </el-table-column>
              <el-table-column label="值" width="200">
                <template #default="{ row }">
                  <el-input v-model="row.value" size="small" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="60">
                <template #default="{ $index }">
                  <el-button type="danger" text size="small" @click="formData.overrideHeaders.splice($index, 1)">删</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button size="small" @click="formData.overrideHeaders.push({ name: '', value: '' })">
              + 添加
            </el-button>
          </div>

          <div class="param-section">
            <div class="param-title">请求体</div>
            <el-input v-model="formData.overrideBody" type="textarea" :rows="4" placeholder="JSON格式" />
          </div>
        </el-tab-pane>

        <!-- 预期响应 -->
        <el-tab-pane label="预期响应" name="response">
          <el-form-item label="预期状态码">
            <el-input-number v-model="formData.expectedHttpStatus" :min="100" :max="599" />
          </el-form-item>

          <el-form-item label="预期响应体">
            <el-input v-model="formData.expectedResponseBody" type="textarea" :rows="6" />
          </el-form-item>

          <el-form-item label="响应Schema">
            <el-input v-model="formData.expectedResponseSchemaStr" type="textarea" :rows="4" />
          </el-form-item>
        </el-tab-pane>

        <!-- 断言规则 -->
        <el-tab-pane label="断言规则" name="assertions">
          <div class="assertions-section">
            <div class="section-header">
              <span>断言列表</span>
              <el-button size="small" type="primary" @click="addAssertion">+ 添加断言</el-button>
            </div>

            <div v-if="formData.assertions.length > 0" class="assertions-list">
              <div v-for="(assertion, index) in formData.assertions" :key="index" class="assertion-item">
                <el-select v-model="assertion.type" style="width: 140px">
                  <el-option label="状态码" value="status_code" />
                  <el-option label="JSON路径" value="json_path" />
                  <el-option label="JSON路径存在" value="json_path_exists" />
                  <el-option label="响应时间" value="response_time" />
                  <el-option label="包含文本" value="contains" />
                </el-select>
                <el-input
                  v-if="assertion.type === 'json_path' || assertion.type === 'json_path_exists'"
                  v-model="assertion.path"
                  placeholder="$.data.token"
                  style="flex: 1"
                />
                <el-input
                  v-if="assertion.type !== 'json_path_exists'"
                  v-model="assertion.expected"
                  placeholder="预期值"
                  style="flex: 1"
                />
                <el-button type="danger" text @click="removeAssertion(index)">删除</el-button>
              </div>
            </div>

            <el-empty v-else description="暂无断言规则" :image-size="60" />
          </div>
        </el-tab-pane>

        <!-- 提取规则 -->
        <el-tab-pane label="提取规则" name="extractors">
          <div class="extractors-section">
            <div class="section-header">
              <span>提取器列表</span>
              <el-button size="small" type="primary" @click="addExtractor">+ 添加提取器</el-button>
            </div>

            <div v-if="formData.extractors.length > 0" class="extractors-list">
              <div v-for="(extractor, index) in formData.extractors" :key="index" class="extractor-item">
                <el-input v-model="extractor.name" placeholder="变量名" style="width: 120px" />
                <el-input v-model="extractor.expression" placeholder="JSONPath表达式" style="flex: 2" />
                <el-input v-model="extractor.description" placeholder="描述" style="flex: 1" />
                <el-button type="danger" text @click="removeExtractor(index)">删除</el-button>
              </div>
            </div>

            <el-empty v-else description="暂无提取规则" :image-size="60" />

            <div class="extractor-tip">
              <el-icon color="#409eff"><InfoFilled /></el-icon>
              <span>提取器用于从响应中提取数据供后续用例使用</span>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="saving">创建用例</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  apiId: { type: Number, default: null }
})

const emit = defineEmits<{
  'update:modelValue': [val: boolean]
  'success': []
}>()

const visible = ref(props.modelValue)
watch(() => props.modelValue, (v) => { visible.value = v })
watch(visible, (v) => { emit('update:modelValue', v) })

const formRef = ref<FormInstance>()
const saving = ref(false)
const activeTab = ref('basic')

const priorityOptions = [
  { label: 'P0（最高优先级）', value: 'P0' },
  { label: 'P1（高优先级）', value: 'P1' },
  { label: 'P2（中等优先级）', value: 'P2' },
  { label: 'P3（低优先级）', value: 'P3' }
]

const severityOptions = [
  { label: '严重', value: 'critical' },
  { label: '高', value: 'high' },
  { label: '中', value: 'medium' },
  { label: '低', value: 'low' }
]

const formData = reactive({
  name: '',
  caseCode: '',
  description: '',
  priority: 'P2',
  severity: 'medium',
  isEnabled: true,
  testSteps: [] as any[],
  preConditionsStr: '',
  overrideQueryParams: [] as any[],
  overrideHeaders: [] as any[],
  overrideBody: '',
  expectedHttpStatus: 200,
  expectedResponseBody: '',
  expectedResponseSchemaStr: '',
  assertions: [] as any[],
  extractors: [] as any[]
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入用例名称', trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

function resetForm() {
  Object.assign(formData, {
    name: '',
    caseCode: '',
    description: '',
    priority: 'P2',
    severity: 'medium',
    isEnabled: true,
    testSteps: [],
    preConditionsStr: '',
    overrideQueryParams: [],
    overrideHeaders: [],
    overrideBody: '',
    expectedHttpStatus: 200,
    expectedResponseBody: '',
    expectedResponseSchemaStr: '',
    assertions: [],
    extractors: []
  })
}

function addStep() {
  formData.testSteps.push({ operation: '', expected: '' })
}

function removeStep(index: number) {
  formData.testSteps.splice(index, 1)
}

function addAssertion() {
  formData.assertions.push({ type: 'status_code', path: '', expected: '' })
}

function removeAssertion(index: number) {
  formData.assertions.splice(index, 1)
}

function addExtractor() {
  formData.extractors.push({ name: '', expression: '', description: '' })
}

function removeExtractor(index: number) {
  formData.extractors.splice(index, 1)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    // TODO: 调用创建用例API
    ElMessage.success('用例创建成功')
    visible.value = false
    resetForm()
    emit('success')
  } catch (e: any) {
    ElMessage.error(e.message || '创建失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped lang="scss">
.steps-section,
.assertions-section,
.extractors-section {
  padding: 12px 0;
}

.steps-header,
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-weight: 500;
}

.steps-list,
.assertions-list,
.extractors-list {
  margin-bottom: 16px;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.step-num {
  width: 24px;
  height: 24px;
  background: #409eff;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.assertion-item,
.extractor-item {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}

.param-section {
  margin-bottom: 20px;
}

.param-title {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 8px;
}

.extractor-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #ecf5ff;
  border-radius: 6px;
  font-size: 13px;
  color: #606266;
  margin-top: 16px;
}
</style>
