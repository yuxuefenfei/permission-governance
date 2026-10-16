<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { roleApi, permissionApi } from '@/api'
import PermissionPicker from '@/components/PermissionPicker.vue'
import type { PermissionView, RolePayload, RoleView } from '@/types'
const loading=ref(false),saving=ref(false),modal=ref(false),grantModal=ref(false)
const selected=ref<RoleView|null>(null),roles=ref<RoleView[]>([]),permissions=ref<PermissionView[]>([])
const codes=ref<string[]>([]),formRef=ref<FormInstance>()
const form=reactive<RolePayload>({roleCode:'',displayName:'',description:''})
const rules:FormRules={roleCode:[{required:true,message:'请输入角色编码',trigger:'blur'},
  {pattern:/^[A-Z][A-Z0-9_]{2,63}$/,message:'大写字母开头，支持数字与下划线'}],
  displayName:[{required:true,message:'请输入角色名称',trigger:'blur'}]}
async function load(){loading.value=true;try{[roles.value,permissions.value]=await Promise.all([roleApi.list(),permissionApi.list()])}finally{loading.value=false}}
function edit(row?:RoleView){selected.value=row||null
  Object.assign(form,row?{roleCode:row.roleCode,displayName:row.displayName,description:row.description}:{roleCode:'',displayName:'',description:''});modal.value=true}
async function save(){if(!await formRef.value?.validate().catch(()=>false))return
  saving.value=true;try{if(selected.value)await roleApi.update(selected.value.id,{...form});else await roleApi.create({...form});
    ElMessage.success('角色已保存');modal.value=false;await load()}finally{saving.value=false}}
function grant(row:RoleView){selected.value=row;codes.value=[...row.permissionCodes];grantModal.value=true}
async function saveGrant(){if(!selected.value)return;saving.value=true
  try{await roleApi.grant(selected.value.id,[...codes.value]);ElMessage.success('授权已更新，使用新注解的接口立即生效');grantModal.value=false;await load()}
  finally{saving.value=false}}
async function remove(row:RoleView){try{await ElMessageBox.confirm(`确定删除角色“${row.displayName}”？将同步解除该角色关联。`,'删除角色',{type:'warning'})}catch{return}
  await roleApi.delete(row.id);ElMessage.success('角色已删除');await load()}
onMounted(load)
</script>
<template><div class="page-heading"><div><h2>角色与权限</h2><p>RBAC 角色授权中心 · 直接从代码发现的权限资源中选择</p></div>
  <el-button type="primary" @click="edit()"><el-icon><Plus /></el-icon>　新增角色</el-button></div>
  <el-card class="content-card" shadow="never"><el-table :data="roles" v-loading="loading" stripe>
    <el-table-column prop="displayName" label="角色名称" min-width="145" /><el-table-column prop="roleCode" label="角色编码" min-width="180"><template #default="{row}"><span class="code-text">{{row.roleCode}}</span></template></el-table-column>
    <el-table-column prop="description" label="说明" min-width="190" /><el-table-column label="内置" width="85"><template #default="{row}"><el-tag size="small" :type="row.builtIn?'info':'success'">{{row.builtIn?'是':'否'}}</el-tag></template></el-table-column>
    <el-table-column label="授权数量" width="95"><template #default="{row}">{{row.permissionCodes.length}} 项</template></el-table-column>
    <el-table-column label="操作" width="230" fixed="right"><template #default="{row}"><el-button link type="primary" @click="grant(row)">配置权限</el-button><el-button link :disabled="row.builtIn" @click="edit(row)">编辑</el-button><el-button link type="danger" :disabled="row.builtIn" @click="remove(row)">删除</el-button></template></el-table-column>
  </el-table></el-card>
  <el-dialog v-model="modal" :title="selected?'编辑角色':'新增角色'" width="540px" destroy-on-close><el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
    <el-form-item label="角色编码" prop="roleCode"><el-input v-model="form.roleCode" placeholder="例如 ROLE_SECURITY" maxlength="64" /></el-form-item>
    <el-form-item label="角色名称" prop="displayName"><el-input v-model="form.displayName" maxlength="100" /></el-form-item>
    <el-form-item label="角色说明"><el-input v-model="form.description" type="textarea" :rows="3" maxlength="255" /></el-form-item>
  </el-form><template #footer><el-button @click="modal=false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存</el-button></template></el-dialog>
  <el-dialog v-model="grantModal" :title="`配置权限 · ${selected?.displayName||''}`" width="720px" destroy-on-close>
    <el-alert title="授权变更对新注解使用实时查询；旧 hasAnyAuthority 会在用户下次登录时更新。" type="info" :closable="false" show-icon style="margin-bottom:20px" />
    <div style="margin-bottom:10px;font-weight:600">选择权限（已选 {{codes.length}} 项）</div>
    <PermissionPicker v-model="codes" :options="permissions" /><div class="selected-list"><el-tag v-for="code in codes" :key="code" closable @close="codes=codes.filter(c=>c!==code)">{{code}}</el-tag></div>
    <template #footer><el-button @click="grantModal=false">取消</el-button><el-button type="primary" :loading="saving" @click="saveGrant">确认授权</el-button></template>
  </el-dialog>
</template>
<style scoped>.selected-list{display:flex;flex-wrap:wrap;gap:8px;max-height:270px;overflow:auto;margin-top:20px}.selected-list .el-tag{max-width:100%}</style>
