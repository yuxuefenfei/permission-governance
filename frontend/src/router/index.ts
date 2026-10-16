import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

/** 只挂载经过前端注册的已知业务页面，不从后端下发任意组件路径。 */
const routes = [
  { path: '/login', component: () => import('@/views/LoginView.vue'), meta: { title: '登录', public: true } },
  { path: '/', component: () => import('@/layouts/AppLayout.vue'), children: [
    { path: 'dashboard', component: () => import('@/views/DashboardView.vue'), meta: { title: '工作台' } },
    { path: 'devices', component: () => import('@/views/DeviceView.vue'), meta: { title: '消防设备' } },
    { path: 'permissions', component: () => import('@/views/PermissionView.vue'), meta: { title: '权限资源' } },
    { path: 'menus', component: () => import('@/views/MenuView.vue'), meta: { title: '菜单配置' } },
    { path: 'roles', component: () => import('@/views/RoleView.vue'), meta: { title: '角色授权' } },
    { path: 'users', component: () => import('@/views/UserView.vue'), meta: { title: '用户管理' } },
    { path: 'audits', component: () => import('@/views/AuditView.vue'), meta: { title: '审计日志' } }
  ] },
  { path: '/no-access', component: () => import('@/views/NoAccessView.vue'), meta: { title: '暂无访问权限' } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]
const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach(async to => {
  const auth = useAuthStore()
  await auth.initialize()
  if (!auth.user) return to.path === '/login' ? true : { path: '/login', query: { redirect: to.fullPath } }
  if (to.path === '/no-access') return true
  if (to.path === '/login' || to.path === '/') return auth.menus.find(m => m.menuType === 'PAGE')?.routePath || '/no-access'
  // 防止页面菜单被隐藏后手工输入地址进入空白页；服务端仍会独立执行鉴权。
  if (!auth.menus.some(m => m.menuType === 'PAGE' && m.routePath === to.path)) {
    const first = auth.menus.find(m => m.menuType === 'PAGE')?.routePath
    return first && first !== to.path ? first : '/no-access'
  }
  return true
})
export default router
