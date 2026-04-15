<template>
  <div class="execution-list page-container">
    <div class="page-header">
      <h2 class="title">执行记录</h2>
    </div>

    <el-card>
      <div class="search-form">
        <el-select v-model="searchForm.type" placeholder="执行类型" clearable style="width: 120px">
          <el-option label="接口" value="API" />
          <el-option label="用例" value="TEST_CASE" />
          <el-option label="套件" value="TEST_SUITE" />
        </el-select>
        <el-select v-model="searchForm.status" placeholder="执行状态" clearable style="width: 120px">
          <el-option label="待执行" value="PENDING" />
          <el-option label="执行中" value="RUNNING" />
          <el-option label="成功" value="SUCCESS" />
          <el-option label="失败" value="FAILED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-button type="primary" @click="loadExecutions">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table :data="executions" v-loading="loading" style="width: 100%; margin-top: 20px">
        <el-table-column prop="id" label="执行ID" width="100" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getExecutionTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetName" label="执行目标" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="total" label="总数" width="80" />
        <el-table-column prop="passed" label="通过" width="80">
          <template #default="{ row }">
            <span style="color: #67C23A">{{ row.passed }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="failed" label="失败" width="80">
          <template #default="{ row }">
            <span style="color: #F56C6C">{{ row.failed }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时" width="100">
          <template #default="{ row }">
            {{ row.duration }}ms
          </template>
        </el-table-column>
        <el-table-column prop="executor" label="执行人" width="100" />
        <el-table-column prop="createdAt" label="开始时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="danger" @click="handleCancel(row)" :disabled="row.status === 'RUNNING'">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="loadExecutions"
        @current-change="loadExecutions"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { executionApi } from '@/api/modules/testing/execution'
import { getStatusType, getExecutionTypeText } from '@/utils/formatters'

const loading = ref(false)

const searchForm = reactive({
  type: '',
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const executions = ref<any[]>([])

async function loadExecutions() {
  loading.value = true
  try {
    const params = {
      type: searchForm.type || undefined,
      status: searchForm.status || undefined,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await executionApi.query(params)
    executions.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('加载执行记录失败:', error)
    ElMessage.error('加载执行记录失败')
  } finally {
    loading.value = false
  }
}

function handleReset() {
  searchForm.type = ''
  searchForm.status = ''
  pagination.pageNum = 1
  loadExecutions()
}

function handleView(row: any) {
  window.open(`/reports/${row.id}`, '_blank')
}

async function handleCancel(row: any) {
  try {
    await ElMessageBox.confirm(`确定取消执行 #${row.id} 吗?`, '提示', { type: 'warning' })
    await executionApi.cancel(row.id)
    ElMessage.success('取消成功')
    loadExecutions()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消失败')
    }
  }
}

onMounted(() => {
  loadExecutions()
})
</script>

<style scoped lang="scss"></style>
