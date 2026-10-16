<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { MenuView } from '@/types'
import { ElMessage } from 'element-plus'
const route = useRoute()
const router = useRouter()
const store = useAuthStore()
const collapsed = ref(false)
const menus = computed(() => store.menus.filter(m => m.parentId === null).sort((a,b) => a.sortOrder-b.sortOrder))
function children(id: number): MenuView[] {
  return store.menus.filter(m => m.parentId === id).sort((a,b) => a.sortOrder-b.sortOrder)
}
async function exit() {
  await store.logout()
  ElMessage.success('已安全退出')
  await router.replace('/login')
}
</script>

<template>
  <div class="shell">
    <aside :class="['sidebar', { collapsed }]">
      <div class="brand"><span class="brand-mark">P</span><div v-if="!collapsed"><strong>Permission Hub</strong><small>统一权限治理平台</small></div></div>
      <div v-if="!collapsed" class="menu-label">工作空间 · WORKSPACE</div>
      <el-menu :default-active="route.path" :collapse="collapsed" :collapse-transition="false" router background-color="#152b45" text-color="#b9c8da" active-text-color="#ffffff">
        <template v-for="menu in menus" :key="menu.id">
          <el-sub-menu v-if="menu.menuType === 'DIRECTORY' && children(menu.id).length" :index="String(menu.id)">
            <template #title><el-icon><component :is="menu.icon || 'Folder'" /></el-icon><span>{{menu.displayName}}</span></template>
            <el-menu-item v-for="child in children(menu.id)" :key="child.id" :index="child.routePath">
              <el-icon><component :is="child.icon || 'Document'" /></el-icon><span>{{child.displayName}}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else-if="menu.menuType === 'PAGE'" :index="menu.routePath">
            <el-icon><component :is="menu.icon || 'Document'" /></el-icon><template #title>{{menu.displayName}}</template>
          </el-menu-item>
        </template>
      </el-menu>
      <div v-if="!collapsed" class="sidebar-foot">SPRING SECURITY · VUE 3</div>
    </aside>
    <section class="main-area">
      <header class="topbar">
        <div class="topbar-left"><el-button link @click="collapsed=!collapsed"><el-icon size="20"><Fold v-if="!collapsed" /><Expand v-else /></el-icon></el-button><span class="crumb">权限治理 / {{String(route.meta.title || '工作台')}}</span></div>
        <div class="topbar-right"><el-tag size="small" type="info" effect="plain">{{store.user?.superAdmin?'超级管理员':'普通账号'}}</el-tag>
          <span class="user-circle">{{store.user?.displayName.slice(0,1)}}</span><strong>{{store.user?.displayName}}</strong>
          <el-button link type="primary" @click="exit">退出登录</el-button></div>
      </header>
      <main class="content"><router-view /></main>
    </section>
  </div>
</template>

<style scoped>
.shell{display:flex;min-height:100vh}.sidebar{width:240px;flex-shrink:0;background:#152b45;color:#dae5f0;min-height:100vh;position:relative;transition:width .2s}.sidebar.collapsed{width:64px}.brand{height:80px;padding:20px;display:flex;align-items:center;gap:12px}.brand-mark{display:flex;align-items:center;justify-content:center;background:#3484fa;border-radius:12px;width:38px;height:38px;font-size:24px;font-weight:800;color:white;flex-shrink:0}.brand strong{white-space:nowrap;color:#fff;font-size:15px;display:block}.brand small{display:block;color:#8aa6c3;margin-top:5px;font-size:11px;white-space:nowrap}.menu-label{padding:20px 24px 10px;font-size:10px;letter-spacing:1px;color:#7490af}.sidebar .el-menu{border-right:none}.sidebar :deep(.el-menu-item.is-active){background:#2765bd!important;border-left:3px solid #6aafff}.sidebar-foot{position:absolute;bottom:22px;padding:0 24px;color:#6f8dad;font-size:10px;letter-spacing:.7px}.main-area{flex:1;min-width:0}.topbar{height:70px;background:#fff;border-bottom:1px solid #e8ecf1;padding:0 30px;display:flex;justify-content:space-between;align-items:center;gap:15px}.topbar-left,.topbar-right{display:flex;align-items:center;gap:14px}.topbar-right{font-size:13px}.crumb{color:#576b83;font-size:13px}.user-circle{height:28px;width:28px;display:grid;place-items:center;border-radius:50%;background:#e1edff;color:#2261c8;font-weight:700}.content{padding:28px;max-width:1600px;margin:auto}@media(max-width:800px){.topbar{padding:0 12px}.content{padding:14px}.sidebar{width:65px}.sidebar .brand{padding:14px}.sidebar .menu-label,.sidebar-foot,.brand div{display:none}.sidebar :deep(.el-menu){width:65px}.topbar-right strong,.crumb{display:none}}
</style>
