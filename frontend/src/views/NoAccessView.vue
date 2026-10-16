<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
const router = useRouter(), auth = useAuthStore()
async function exit() { await auth.logout(); await router.replace('/login') }
</script>
<template><div class="center"><el-result icon="warning" title="当前没有可访问的页面" sub-title="请联系管理员为你的角色授予菜单对应的页面权限"><template #extra><el-button type="primary" @click="exit">切换账号</el-button></template></el-result></div></template>
<style scoped>.center{min-height:95vh;display:grid;place-items:center}</style>
