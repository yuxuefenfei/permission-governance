<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { deviceApi } from '@/api'
import { PERMISSION_CODES } from '@/constants/permissions'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
const auth=useAuthStore(),loading=ref(false),saving=ref(false),visible=ref(false)
const items=ref<{name:string;location:string}[]>([])
const form=reactive({name:'',location:''})
async function load(){loading.value=true;try{items.value=await deviceApi.list()}finally{loading.value=false}}
async function create(){if(!form.name.trim()||!form.location.trim())return ElMessage.warning('请填写设备名称和位置')
  saving.value=true;try{await deviceApi.create(form.name,form.location);ElMessage.success('设备已新增');visible.value=false;await load()}finally{saving.value=false}}
onMounted(load)
</script>
<template><div class="page-heading"><div><h2>消防设备示例</h2><p>演示页面级查看权限与动作级新增权限的差别</p></div>
  <el-button v-if="auth.hasPermission(PERMISSION_CODES.DEVICE_CREATE)" type="primary" @click="form.name='';form.location='';visible=true"><el-icon><Plus /></el-icon>　新增设备</el-button></div>
  <el-alert type="info" show-icon :closable="false" style="margin-bottom:18px" title="演示业务：operator 只拥有页面查看权限；admin 可以新增。设备存放在内存中，重启会重置。" />
  <el-card shadow="never" class="content-card"><el-table :data="items" v-loading="loading" stripe><el-table-column prop="name" label="设备名称"/><el-table-column prop="location" label="安装位置" />
    <el-table-column label="资产状态"><template #default><el-tag type="success">演示正常</el-tag></template></el-table-column></el-table>
    <div v-if="!auth.hasPermission(PERMISSION_CODES.DEVICE_CREATE)" class="form-hint" style="margin-top:15px">当前角色无“新增消防设备”权限，操作按钮被隐藏，API 仍会在服务端拒绝未经授权的 POST 请求。</div>
  </el-card>
  <el-dialog v-model="visible" title="新增消防设备" width="470px"><el-form :model="form" label-width="95px"><el-form-item label="设备名称"><el-input v-model="form.name" maxlength="100" /></el-form-item><el-form-item label="安装位置"><el-input v-model="form.location" maxlength="100" /></el-form-item></el-form>
    <template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" :loading="saving" @click="create">确认新增</el-button></template>
  </el-dialog>
</template>
