<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { menuApi, permissionApi } from '@/api'
import PermissionPicker from '@/components/PermissionPicker.vue'
import type { MenuPayload, MenuView, PermissionView } from '@/types'

interface MenuTree extends MenuView { children?: MenuTree[] }
const loading=ref(false),saving=ref(false),visible=ref(false),editingId=ref<number|null>(null)
const menus=ref<MenuView[]>([]),permissions=ref<PermissionView[]>([])
const formRef=ref<FormInstance>()
const form=reactive<MenuPayload>({parentId:null,displayName:'',routePath:'',icon:'Document',menuType:'PAGE',sortOrder:10,enabled:true,permissionCodes:[]})
const routes=[
  {label:'工作台',value:'/dashboard'},{label:'消防设备',value:'/devices'},
  {label:'权限资源',value:'/permissions'},{label:'菜单配置',value:'/menus'},
  {label:'角色授权',value:'/roles'},{label:'用户管理',value:'/users'},
  {label:'审计日志',value:'/audits'}
]
const rules:FormRules={displayName:[{required:true,message:'请填写菜单名称',trigger:'blur'}],
  menuType:[{required:true,message:'请选择类型',trigger:'change'}]}
const directories=computed(()=>menus.value.filter(m=>m.menuType==='DIRECTORY'&&m.id!==editingId.value))
const treeData=computed<MenuTree[]>(()=>{
  const map=new Map<number,MenuTree>()
  menus.value.forEach(item=>map.set(item.id,{...item,children:[]}))
  const roots:MenuTree[]=[]
  for(const m of map.values()){
    const parent=m.parentId==null?null:map.get(m.parentId)
    if(parent)parent.children!.push(m);else roots.push(m)
  }
  return roots
})
async function load(){loading.value=true;try{[menus.value,permissions.value]=await Promise.all([menuApi.list(),permissionApi.list()])}finally{loading.value=false}}
function open(row?:MenuView){editingId.value=row?.id??null
  Object.assign(form,row?{parentId:row.parentId,displayName:row.displayName,routePath:row.routePath,icon:row.icon,
    menuType:row.menuType,sortOrder:row.sortOrder,enabled:row.enabled,permissionCodes:[...row.permissionCodes]}:
    {parentId:null,displayName:'',routePath:'',icon:'Document',menuType:'PAGE',sortOrder:10,enabled:true,permissionCodes:[]})
  visible.value=true
}
async function save(){if(!await formRef.value?.validate().catch(()=>false))return
  if(form.menuType==='PAGE'&&(!form.routePath||form.permissionCodes.length===0))return ElMessage.warning('页面必须选择路由并绑定至少一个权限')
  saving.value=true
  try{const data:MenuPayload={...form,permissionCodes:form.menuType==='DIRECTORY'?[]:[...form.permissionCodes],routePath:form.menuType==='DIRECTORY'?'':form.routePath}
    if(editingId.value===null)await menuApi.create(data);else await menuApi.update(editingId.value,data)
    ElMessage.success('菜单配置已保存');visible.value=false;await load()
  }finally{saving.value=false}
}
async function remove(row:MenuView){try{await ElMessageBox.confirm(`确定删除“${row.displayName}”？有子菜单时后端会拒绝删除。`,'删除菜单',{type:'warning'})}catch{return}
  await menuApi.delete(row.id);ElMessage.success('已删除');await load()
}
onMounted(load)
</script>
<template><div class="page-heading"><div><h2>菜单配置</h2><p>目录组织导航 · 页面绑定权限 · 访问判断在后端完成</p></div>
  <el-button type="primary" @click="open()"><el-icon><Plus /></el-icon>　新增菜单</el-button></div>
  <el-card shadow="never" class="content-card"><el-alert type="info" show-icon :closable="false" style="margin-bottom:16px" title="页面菜单至少绑定一个权限码；目录不绑定权限。管理员可在资源池中找到代码扫描出来的可用权限。" />
    <el-table :data="treeData" row-key="id" :tree-props="{children:'children'}" :default-expand-all="true" v-loading="loading" stripe>
      <el-table-column prop="displayName" label="菜单名称" min-width="190" /><el-table-column label="类型" width="95"><template #default="{row}"><el-tag :type="row.menuType==='PAGE'?'success':'info'" size="small">{{row.menuType==='PAGE'?'页面':'目录'}}</el-tag></template></el-table-column>
      <el-table-column prop="routePath" label="页面路由" min-width="150"><template #default="{row}"><span class="code-text">{{row.routePath||'—'}}</span></template></el-table-column>
      <el-table-column label="绑定权限" min-width="190"><template #default="{row}"><el-tooltip :content="row.permissionCodes.join('、')||'无'"><span>{{row.permissionCodes.length}} 项权限</span></el-tooltip></template></el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="75" /><el-table-column label="启用" width="85"><template #default="{row}"><el-tag size="small" :type="row.enabled?'success':'danger'">{{row.enabled?'启用':'停用'}}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="150" fixed="right"><template #default="{row}"><el-button link type="primary" @click="open(row)">编辑</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column>
    </el-table></el-card>
  <el-dialog v-model="visible" :title="editingId===null?'新增菜单':'编辑菜单'" width="680px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="菜单名称" prop="displayName"><el-input v-model="form.displayName" maxlength="100" show-word-limit /></el-form-item>
      <el-form-item label="菜单类型" prop="menuType"><el-radio-group v-model="form.menuType"><el-radio value="DIRECTORY">目录</el-radio><el-radio value="PAGE">页面</el-radio></el-radio-group></el-form-item>
      <el-form-item label="上级目录"><el-select v-model="form.parentId" clearable placeholder="顶级菜单" style="width:100%"><el-option v-for="m in directories" :key="m.id" :label="m.displayName" :value="m.id" /></el-select></el-form-item>
      <el-form-item v-if="form.menuType==='PAGE'" label="页面路由" required><el-select v-model="form.routePath" filterable placeholder="选择已注册的前端路由" style="width:100%"><el-option v-for="r in routes" :key="r.value" :label="`${r.label} · ${r.value}`" :value="r.value" /></el-select></el-form-item>
      <el-form-item label="图标名称"><el-input v-model="form.icon" placeholder="如 Key / Menu / Document" /></el-form-item>
      <el-form-item v-if="form.menuType==='PAGE'" label="绑定权限" required><div style="width:100%"><PermissionPicker v-model="form.permissionCodes" :options="permissions" /><p class="form-hint">勾选任意一个权限即可显示页面；API 仍按各自注解独立校验。</p></div></el-form-item>
      <el-form-item label="显示排序"><el-input-number v-model="form.sortOrder" :min="0" :max="9999" /></el-form-item>
      <el-form-item label="启用状态"><el-switch v-model="form.enabled" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存配置</el-button></template>
  </el-dialog>
</template>
