<template>
  <div class="report-list page-container">
    <div class="page-header">
      <h2 class="title">测试报告</h2>
    </div>

    <el-card>
      <div class="search-form">
        <el-date-picker
          v-model="searchForm.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 260px"
        />
        <el-select v-model="searchForm.status" placeholder="执行状态" clearable style="width: 120px">
          <el-option label="全部" value="" />
          <el-option label="成功" value="SUCCESS" />
          <el-option label="失败" value="FAILED" />
        </el-select>
        <el-button type="primary" @click="loadReports">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table :data="reports" v-loading="loading" style="width: 100%; margin-top: 20px">
        <el-table-column prop="id" label="报告ID" width="100" />
        <el-table-column prop="executionId" label="执行ID" width="100" />
        <el-table-column prop="projectName" label="项目" width="150" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getExecutionTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ row.status === 'SUCCESS' ? '通过' : '失败' }}
            </el-tag>
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
        <el-table-column prop="passRate" label="通过率" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.passRate >= 80 ? '#67C23A' : row.passRate >= 60 ? '#E6A23C' : '#F56C6C' }">
              {{ row.passRate }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时" width="100">
          <template #default="{ row }">
            {{ row.duration }}ms
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="生成时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" @click="handleDownload(row)">下载</el-button>
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
        @size-change="loadReports"
        @current-change="loadReports"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reportApi } from '@/api/modules/report/report'
import { getExecutionTypeText } from '@/utils/formatters'

const loading = ref(false)

const searchForm = reactive({
  dateRange: [] as string[],
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const reports = ref<any[]>([])

async function loadReports() {
  loading.value = true
  try {
    const params = {
      status: searchForm.status || undefined,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await reportApi.query(params)
    reports.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('加载报告失败:', error)
    ElMessage.error('加载报告失败')
  } finally {
    loading.value = false
  }
}

function handleReset() {
  searchForm.dateRange = []
  searchForm.status = ''
  pagination.pageNum = 1
  loadReports()
}

function handleView(row: any) {
  window.open(`/reports/${row.id}`, '_blank')
}

async function handleDownload(row: any) {
  try {
    await ElMessageBox.confirm(`确定下载报告 #${row.id} 吗?`, '提示', { type: 'info' })
    ElMessage.info('报告下载功能开发中')
  } catch (error: any) {
    // 用户取消
  }
}

onMounted(() => {
  loadReports()
})
</script>

<style scoped lang="scss"></style>
