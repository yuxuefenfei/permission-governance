<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { auditApi } from '@/api'
import type { AuditEntry } from '@/types'
const loading=ref(false),keyword=ref(''),items=ref<AuditEntry[]>([])
const filtered=computed(()=>items.value.filter(a=>!keyword.value||
  `${a.actor} ${a.action} ${a.resourceType} ${a.resourceId} ${a.detail}`.toLowerCase().includes(keyword.value.toLowerCase())))
async function load(){loading.value=true;try{items.value=await auditApi.list()}finally{loading.value=false}}
onMounted(load)
</script>
<template><div class="page-heading"><div><h2>审计日志</h2><p>敏感配置变更追踪 · 当前展示最近 200 条记录</p></div><el-button @click="load"><el-icon><Refresh /></el-icon>　刷新</el-button></div>
  <el-card class="content-card" shadow="never"><div class="panel-toolbar"><el-input v-model="keyword" placeholder="检索操作者 / 动作 / 对象" clearable style="width:300px"><template #prefix><el-icon><Search /></el-icon></template></el-input><span class="muted">{{filtered.length}} 条</span></div>
    <el-table :data="filtered" v-loading="loading" stripe><el-table-column prop="createdAt" label="发生时间" min-width="172"><template #default="{row}">{{row.createdAt.replace('T',' ')}}</template></el-table-column>
      <el-table-column prop="actor" label="操作人" width="125" /><el-table-column prop="action" label="动作" width="145"><template #default="{row}"><el-tag size="small" type="info">{{row.action}}</el-tag></template></el-table-column>
      <el-table-column prop="resourceType" label="资源类型" width="130" /><el-table-column prop="resourceId" label="资源标识" min-width="160" /><el-table-column prop="detail" label="备注" min-width="220" /></el-table>
  </el-card>
</template>
