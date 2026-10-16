<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { dashboardApi } from '@/api'
import type { DashboardData } from '@/types'
const data = ref<DashboardData | null>(null)
const loading = ref(true)
onMounted(async () => { try { data.value = await dashboardApi.overview() } finally { loading.value = false } })
const stats: { key: keyof DashboardData; label: string; icon: string; suffix: string }[] = [
  { key:'permissionCount',label:'权限资源数',icon:'Key',suffix:'项' },
  { key:'roleCount',label:'角色总数',icon:'UserRoundCheck',suffix:'个' },
  { key:'userCount',label:'用户总数',icon:'Users',suffix:'人' },
  { key:'menuCount',label:'菜单节点数',icon:'Menu',suffix:'个' }
]
</script>
<template><div class="page-heading"><div><h2>工作台概览</h2><p>权限治理数据实时概况 · 数据来自后端数据库</p></div><el-tag effect="plain">Permission Registry</el-tag></div>
  <el-skeleton :loading="loading" animated><template #default>
    <div class="stats"><el-card v-for="item in stats" :key="item.key" shadow="never" class="content-card stat"><div class="stat-top"><span>{{item.label}}</span><el-icon size="22"><component :is="item.icon" /></el-icon></div><strong>{{data?.[item.key] ?? 0}}</strong><span class="suffix">{{item.suffix}}</span></el-card></div>
    <el-card class="content-card" shadow="never"><template #header><b>治理流程说明</b></template>
      <el-steps :active="4" align-center finish-status="success"><el-step title="代码声明" description="统一权限常量 + 注解"/><el-step title="自动发现" description="扫描 Controller 路由"/><el-step title="权限绑定" description="菜单与角色关联"/><el-step title="实时鉴权" description="后端独立验证"/></el-steps>
      <el-alert v-if="data?.deprecatedCount" style="margin-top:30px" type="warning" show-icon :closable="false" :title="`当前有 ${data.deprecatedCount} 项已废弃权限，请检查引用关系`" />
      <div class="explain">本演示采用 Session + CSRF、防止仅通过隐藏前端按钮代替后端授权。同步不会覆盖已有角色、用户及菜单授权。</div>
    </el-card>
  </template></el-skeleton>
</template>
<style scoped>.stats{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:18px;margin-bottom:25px}.stat{padding:6px}.stat-top{display:flex;justify-content:space-between;color:#7e8ea2;font-size:13px;margin:8px 0 22px}.stat-top .el-icon{color:#468ced}.stat strong{font-size:36px;color:#152b45}.suffix{font-size:13px;color:#8c9aaa;margin-left:8px}.explain{background:#f3f7fc;border-radius:10px;padding:18px;color:#61768e;font-size:13px;margin-top:40px;line-height:1.8}@media(max-width:1000px){.stats{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:550px){.stats{grid-template-columns:1fr}}</style>
