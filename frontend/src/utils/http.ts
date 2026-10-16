import axios, { AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types'

/** 同源 Cookie Session。切勿将密码、长期 Token 放 localStorage。 */
const client = axios.create({ baseURL: '/api', withCredentials: true, timeout: 15000 })

function readCookie(name: string): string | undefined {
  const part = document.cookie.split('; ').find(item => item.startsWith(`${name}=`))
  return part ? decodeURIComponent(part.substring(name.length + 1)) : undefined
}

client.interceptors.request.use(config => {
  const token = readCookie('XSRF-TOKEN')
  if (token) config.headers.set('X-XSRF-TOKEN', token)
  return config
})

client.interceptors.response.use(response => response, (error: AxiosError<ApiResponse<unknown>>) => {
  const message = error.response?.data?.message || error.message || '请求失败'
  // 未认证不自动跳转：交给路由守卫 / 页面处理，防止循环重定向。
  if (error.response?.status !== 401) ElMessage.error(message)
  return Promise.reject(error)
})

export async function request<T>(method: 'GET' | 'POST' | 'PUT' | 'DELETE',
                                 url: string, data?: unknown, params?: object): Promise<T> {
  const response = await client.request<ApiResponse<T>>({ method, url, data, params })
  return response.data?.data as T
}
