<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
const router = useRouter(), route = useRoute(), auth = useAuthStore()
const busy = ref(false)
const form = reactive({username: 'admin', password: ''})
async function submit() {
  if (!form.username.trim() || !form.password) return ElMessage.warning('请填写用户名与密码')
  busy.value = true
  try {
    await auth.login(form.username.trim(), form.password)
    ElMessage.success('登录成功')
    const first = auth.menus.find(m => m.menuType === 'PAGE')?.routePath || '/no-access'
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : first
    await router.replace(redirect === '/login' ? first : redirect)
  } catch { ElMessage.error('登录失败，请核对账号和密码') }
  finally { busy.value = false }
}
</script>

<template><div class="login-root">
  <section class="login-intro"><div class="intro-chip">PERMISSION GOVERNANCE · V1.0</div>
    <div class="intro-symbol">◆</div><h1>让权限定义<br/>回归代码本身。</h1>
    <p>从注解扫描，到资源注册，再到菜单与角色授权。<br/>一个真正打通开发端与管理端的示例工程。</p>
    <div class="intro-flow"><span>代码声明</span><b>→</b><span>自动注册</span><b>→</b><span>授权闭环</span></div>
    <div class="intro-foot">Spring Boot · Spring Security · Vue 3 · TypeScript</div>
  </section>
  <section class="login-panel"><div class="login-box"><div class="welcome-icon">P</div>
    <h2>欢迎回来</h2><p class="sub">登录权限治理控制台</p>
    <el-form :model="form" label-position="top" size="large" @submit.prevent="submit">
      <el-form-item label="用户名"><el-input v-model="form.username" placeholder="请输入用户名" autocomplete="username" /></el-form-item>
      <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password placeholder="请输入密码" autocomplete="current-password" @keyup.enter="submit" /></el-form-item>
      <el-button class="login-button" type="primary" size="large" :loading="busy" @click="submit">登录控制台 <el-icon class="el-icon--right"><ArrowRight /></el-icon></el-button>
    </el-form>
    <div class="demo-box"><b>本地演示账号</b><p>管理员：<code>admin / Admin@123456</code></p><p>查看员：<code>operator / Operator@123456</code></p><small>仅供本地学习，禁止直接部署到公网。</small></div>
  </div></section>
</div></template>

<style scoped>
.login-root{min-height:100vh;display:grid;grid-template-columns:1.06fr 1fr}.login-intro{background:linear-gradient(145deg,#102541,#183d67 65%,#276ab4);color:white;padding:11% 12%;display:flex;flex-direction:column;justify-content:center}.intro-chip{color:#98c4fb;font-size:12px;letter-spacing:1.8px}.intro-symbol{font-size:68px;color:#4fa2ff;margin:30px 0 10px}.login-intro h1{font-size:clamp(38px,4vw,65px);letter-spacing:-2px;line-height:1.25;font-weight:800;margin-bottom:22px}.login-intro p{color:#b7cae0;font-size:15px;line-height:2}.intro-flow{display:flex;align-items:center;gap:18px;margin-top:44px;font-size:13px}.intro-flow span{padding:9px 13px;border:1px solid #6b9cca6c;border-radius:24px}.intro-flow b{color:#77b5f9}.intro-foot{font-size:12px;color:#7fa5ca;margin-top:90px}.login-panel{background:#f9fbfe;display:grid;place-items:center;padding:30px}.login-box{width:min(420px,100%)}.welcome-icon{width:44px;height:44px;display:grid;place-items:center;background:#2275e8;color:white;border-radius:13px;font-weight:900;font-size:25px;margin-bottom:28px}.login-box h2{font-size:32px;color:#152b45;margin:0 0 8px}.sub{color:#8190a3;margin-bottom:35px}.login-button{width:100%;margin-top:12px}.demo-box{background:#eef4fc;border-radius:12px;padding:18px 22px;margin-top:40px;color:#526a85;font-size:13px}.demo-box p{margin:10px 0}.demo-box small{color:#a07741}.demo-box code{font-size:12px}@media(max-width:900px){.login-root{grid-template-columns:1fr}.login-intro{display:none}}
</style>
