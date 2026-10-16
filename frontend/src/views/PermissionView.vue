<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { permissionApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { PERMISSION_CODES } from '@/constants/permissions'
import type { PermissionView } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
const store = useAuthStore()
const busy = ref(false), syncing = ref(false), keyword = ref(''), moduleName = ref(''), status = ref('')
const items = ref<PermissionView[]>([]), selected = ref<PermissionView | null>(null), drawer = ref(false)
const modules = computed(() => [...new Set(items.value.map(p => p.moduleName))].sort())
const filtered = computed(() => items.value.filter(p =>
  (!keyword.value || `${p.code} ${p.displayName}`.toLowerCase().includes(keyword.value.toLowerCase())) &&
  (!moduleName.value || p.moduleName === moduleName.value) && (!status.value || p.status === status.value)))
async function load() { busy.value = true; try { items.value = await permissionApi.list() } finally { busy.value = false } }
async function sync() {
  try { await ElMessageBox.confirm('将当前代码中的权限写入注册中心，并把本服务已消失的权限标记为废弃。已有授权保留。', '确认扫描同步', {type:'warning'}) }
  catch { return }
  syncing.value = true
  try { const result = await permissionApi.sync(); ElMessage.success(`扫描 ${result.discovered} 项权限，端点 ${result.endpoints} 个，废弃 ${result.deprecated} 项`); await load() }
  finally { syncing.value = false }
}
function show(row: PermissionView) { selected.value = row; drawer.value = true }
onMounted(load)
</script>
<template>
  <div class="page-heading"><div><h2>权限资源池</h2><p>代码声明自动入库 · 权限码不可在管理端随意修改</p></div>
    <el-button v-if="store.hasPermission(PERMISSION_CODES.PERMISSION_SYNC)" type="primary" :loading="syncing" @click="sync"><el-icon><Refresh /></el-icon>　重新扫描同步</el-button></div>
  <el-card shadow="never" class="content-card">
    <div class="panel-toolbar"><el-input v-model="keyword" placeholder="搜索权限名称 / 编码" clearable style="width:260px"><template #prefix><el-icon><Search /></el-icon></template></el-input>
      <el-select v-model="moduleName" placeholder="全部模块" clearable style="width:180px"><el-option v-for="m in modules" :key="m" :label="m" :value="m" /></el-select>
      <el-select v-model="status" placeholder="全部状态" clearable style="width:145px"><el-option label="有效" value="ACTIVE" /><el-option label="已废弃" value="DEPRECATED" /></el-select>
      <span class="muted">共 {{filtered.length}} 条</span></div>
    <el-table :data="filtered" v-loading="busy" stripe style="width:100%" table-layout="auto">
      <el-table-column label="权限名称" prop="displayName" min-width="170"><template #default="{row}"><b>{{row.displayName}}</b></template></el-table-column>
      <el-table-column label="权限编码" min-width="280"><template #default="{row}"><span class="code-text">{{row.code}}</span></template></el-table-column>
      <el-table-column label="模块" prop="moduleName" min-width="120" /><el-table-column label="类型" width="100"><template #default="{row}"><el-tag size="small" type="info">{{row.kind}}</el-tag></template></el-table-column>
      <el-table-column label="状态" width="110"><template #default="{row}"><el-tag size="small" :type="row.status==='ACTIVE'?'success':'warning'">{{row.status==='ACTIVE'?'有效':'已废弃'}}</el-tag></template></el-table-column>
      <el-table-column label="端点" width="80"><template #default="{row}">{{row.endpoints.length}}</template></el-table-column>
      <el-table-column label="操作" width="90" fixed="right"><template #default="{row}"><el-button link type="primary" @click="show(row)">详情</el-button></template></el-table-column>
    </el-table>
  </el-card>
  <el-drawer v-model="drawer" title="权限资源详情" size="min(600px, 95vw)"><template v-if="selected">
    <el-descriptions :column="1" border><el-descriptions-item label="名称">{{selected.displayName}}</el-descriptions-item><el-descriptions-item label="编码"><span class="code-text">{{selected.code}}</span></el-descriptions-item>
      <el-descriptions-item label="所属模块">{{selected.moduleName}}</el-descriptions-item><el-descriptions-item label="来源服务">{{selected.serviceName}}</el-descriptions-item>
      <el-descriptions-item label="说明">{{selected.description||'无'}}</el-descriptions-item><el-descriptions-item label="最近发现">{{selected.lastSeenAt.replace('T',' ')}}</el-descriptions-item></el-descriptions>
    <h4 style="margin-top:30px">绑定的后端端点（{{selected.endpoints.length}}）</h4><el-table :data="selected.endpoints" border>
      <el-table-column prop="httpMethod" label="方法" width="90"/><el-table-column prop="urlPath" label="接口路径" min-width="175"/>
      <el-table-column prop="handler" label="处理器" min-width="160"/></el-table>
    <el-alert style="margin-top:24px" type="info" :closable="false" title="权限由 @RequirePermission 注解维护。角色授予与菜单绑定在相应页面完成。" />
  </template></el-drawer>
</template>
