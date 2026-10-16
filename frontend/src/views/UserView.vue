<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { userApi, roleApi } from '@/api'
import type { RoleView, UserView } from '@/types'
const busy=ref(false),saving=ref(false),visible=ref(false),passwordVisible=ref(false)
const users=ref<UserView[]>([]),roles=ref<RoleView[]>([]),selected=ref<UserView|null>(null)
const formRef=ref<FormInstance>(),passwordRef=ref<FormInstance>()
const form=reactive({username:'',displayName:'',password:'',enabled:true,roleIds:[] as number[]})
const passForm=reactive({newPassword:''})
const rules:FormRules={username:[{required:true,message:'请输入账号',trigger:'blur'},
  {pattern:/^[a-zA-Z][a-zA-Z0-9_]{2,31}$/,message:'3-32 位字母、数字和下划线'}],
  displayName:[{required:true,message:'请输入姓名',trigger:'blur'}],
  password:[{min:8,max:72,message:'密码长度 8-72 位',trigger:'blur'}]}
const passwordRules:FormRules={newPassword:[{required:true,message:'请输入新密码',trigger:'blur'},
  {min:8,max:72,message:'密码长度 8-72 位',trigger:'blur'}]}
async function load(){busy.value=true;try{[users.value,roles.value]=await Promise.all([userApi.list(),roleApi.list()])}finally{busy.value=false}}
function open(row?:UserView){selected.value=row||null
  Object.assign(form,row?{username:row.username,displayName:row.displayName,password:'',enabled:row.enabled,roleIds:[...row.roleIds]}:
    {username:'',displayName:'',password:'',enabled:true,roleIds:[]})
  visible.value=true
}
async function save(){if(!await formRef.value?.validate().catch(()=>false))return
  if(!selected.value && form.password.length<8)return ElMessage.warning('请设置至少 8 位密码')
  saving.value=true
  try{if(selected.value) await userApi.update(selected.value.id,{displayName:form.displayName,enabled:form.enabled,roleIds:[...form.roleIds]})
    else await userApi.create({username:form.username,displayName:form.displayName,password:form.password,roleIds:[...form.roleIds]})
    ElMessage.success('用户信息已保存');visible.value=false;await load()
  }finally{saving.value=false}
}
function resetPassword(row:UserView){selected.value=row;passForm.newPassword='';passwordVisible.value=true}
async function savePassword(){if(!selected.value||!await passwordRef.value?.validate().catch(()=>false))return
  saving.value=true;try{await userApi.resetPassword(selected.value.id,passForm.newPassword)
    ElMessage.success('密码已重置，下次登录使用新密码');passwordVisible.value=false}
  finally{saving.value=false}}
function roleNames(row:UserView){return row.roleIds.map(id=>roles.value.find(r=>r.id===id)?.displayName||`#${id}`).join('、')||'未分配'}
onMounted(load)
</script>
<template><div class="page-heading"><div><h2>用户管理</h2><p>账号维护 · 分配角色 · 启停账户 · 重置密码</p></div>
  <el-button type="primary" @click="open()"><el-icon><Plus /></el-icon>　新增用户</el-button></div>
  <el-card class="content-card" shadow="never"><el-alert type="info" title="角色权限以实时鉴权为准；管理员账号在本示例中受到额外保护，不可在此页面禁用。" :closable="false" style="margin-bottom:16px" />
    <el-table :data="users" v-loading="busy" stripe>
      <el-table-column prop="username" label="登录账号" min-width="150" /><el-table-column prop="displayName" label="显示名称" min-width="130" />
      <el-table-column label="分配角色" min-width="200"><template #default="{row}">{{roleNames(row)}}</template></el-table-column>
      <el-table-column label="账号状态" width="110"><template #default="{row}"><el-tag :type="row.enabled?'success':'danger'">{{row.enabled?'正常':'已停用'}}</el-tag></template></el-table-column>
      <el-table-column label="超级管理员" width="110"><template #default="{row}"><el-tag v-if="row.superAdmin" type="warning">是</el-tag><span v-else class="muted">否</span></template></el-table-column>
      <el-table-column label="操作" width="180" fixed="right"><template #default="{row}"><el-button link type="primary" :disabled="row.superAdmin" @click="open(row)">编辑/授权</el-button><el-button link :disabled="row.superAdmin" @click="resetPassword(row)">重置密码</el-button></template></el-table-column>
    </el-table></el-card>
  <el-dialog v-model="visible" :title="selected?'编辑用户':'新增用户'" width="590px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="登录账号" prop="username"><el-input v-model="form.username" :disabled="!!selected" autocomplete="off" /></el-form-item>
      <el-form-item label="显示名称" prop="displayName"><el-input v-model="form.displayName" maxlength="100" /></el-form-item>
      <el-form-item v-if="!selected" label="登录密码" prop="password"><el-input v-model="form.password" type="password" show-password autocomplete="new-password" /><p class="form-hint">8-72 位。生产环境建议添加复杂度校验、限流与临时密码流程。</p></el-form-item>
      <el-form-item label="关联角色"><el-select v-model="form.roleIds" multiple filterable placeholder="选择一个或多个角色" style="width:100%"><el-option v-for="role in roles" :key="role.id" :value="role.id" :label="`${role.displayName} (${role.roleCode})`" /></el-select></el-form-item>
      <el-form-item label="账号启用"><el-switch v-model="form.enabled" :disabled="!selected" /><span v-if="!selected" class="form-hint">新账号默认为启用</span></el-form-item>
    </el-form><template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存</el-button></template>
  </el-dialog>
  <el-dialog v-model="passwordVisible" :title="`重置密码 · ${selected?.username||''}`" width="470px" destroy-on-close>
    <el-form ref="passwordRef" :model="passForm" :rules="passwordRules" label-width="100px"><el-form-item label="新密码" prop="newPassword"><el-input v-model="passForm.newPassword" type="password" show-password autocomplete="new-password" /></el-form-item></el-form>
    <el-alert title="新密码不会写入审计日志，但已有登录会话可能需要管理员另外强制下线。" type="warning" :closable="false" show-icon />
    <template #footer><el-button @click="passwordVisible=false">取消</el-button><el-button type="primary" :loading="saving" @click="savePassword">确认重置</el-button></template>
  </el-dialog>
</template>
