<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <a-row :gutter="16" style="margin-bottom: 24px;">
      <a-col :span="6">
        <a-card style="border: 1px solid #f0f0f0;">
          <a-statistic
            title="总用户数"
            :value="11893"
            :value-style="{ color: '#3f8600' }"
          >
            <template #prefix>
              <user-outlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card style="border: 1px solid #f0f0f0;">
          <a-statistic
            title="今日访问量"
            :value="2893"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <dashboard-outlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card style="border: 1px solid #f0f0f0;">
          <a-statistic
            title="查询总数"
            :value="48251"
            :value-style="{ color: '#722ed1' }"
          >
            <template #prefix>
              <search-outlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card style="border: 1px solid #f0f0f0;">
          <a-statistic
            title="文件上传"
            :value="1024"
            suffix="个"
            :value-style="{ color: '#cf1322' }"
          >
            <template #prefix>
              <upload-outlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表和表格区域 -->
    <a-row :gutter="16">
      <a-col :span="12">
        <a-card title="访问量趋势" style="margin-bottom: 16px; border: 1px solid #f0f0f0;">
          <div style="height: 300px; display: flex; align-items: center; justify-content: center; background: #fafafa;">
            <p style="color: #999;">这里可以集成图表组件 (如 ECharts)</p>
          </div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="查询分布" style="margin-bottom: 16px; border: 1px solid #f0f0f0;">
          <div style="height: 300px; display: flex; align-items: center; justify-content: center; background: #fafafa;">
            <p style="color: #999;">这里可以集成饼图组件</p>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 数据表格 -->
    <a-card title="最近活动" style="border: 1px solid #f0f0f0;">
      <a-table
        :columns="columns"
        :data-source="data"
        :pagination="{ pageSize: 5 }"
      />
    </a-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import {
  UserOutlined,
  DashboardOutlined,
  SearchOutlined,
  UploadOutlined
} from '@ant-design/icons-vue'

// 表格配置
const columns = [
  {
    title: '用户名',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: '操作',
    dataIndex: 'action',
    key: 'action',
  },
  {
    title: '时间',
    dataIndex: 'time',
    key: 'time',
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
  },
]

const data = ref([
  {
    key: '1',
    name: '张三',
    action: '执行查询',
    time: '2024-01-15 10:30:00',
    status: '成功',
  },
  {
    key: '2',
    name: '李四',
    action: '上传文件',
    time: '2024-01-15 10:25:00',
    status: '成功',
  },
  {
    key: '3',
    name: '王五',
    action: '修改配置',
    time: '2024-01-15 10:20:00',
    status: '失败',
  },
  {
    key: '4',
    name: '赵六',
    action: '查看看板',
    time: '2024-01-15 10:15:00',
    status: '成功',
  },
])
</script>

<style scoped>
.dashboard {
  padding: 0;
}
</style>