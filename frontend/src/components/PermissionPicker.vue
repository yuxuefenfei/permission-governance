<script setup lang="ts">
import { computed } from 'vue'
import type { PermissionView } from '@/types'
const props = defineProps<{ modelValue: string[]; options: PermissionView[]; placeholder?: string }>()
const emit = defineEmits<{ 'update:modelValue': [codes: string[]] }>()
const grouped = computed(() => {
  const groups = new Map<string, PermissionView[]>()
  for (const p of props.options) {
    const items = groups.get(p.moduleName) || []
    items.push(p)
    groups.set(p.moduleName, items)
  }
  return Array.from(groups.entries()).map(([module, items]) => ({ module, items }))
})
function onChange(value: string[]) { emit('update:modelValue', value) }
</script>
<template><el-select :model-value="modelValue" @update:model-value="onChange"
    multiple filterable clearable collapse-tags collapse-tags-tooltip :max-collapse-tags="2"
    :placeholder="placeholder || '按中文名称搜索并选择权限'" style="width:100%">
  <el-option-group v-for="group in grouped" :key="group.module" :label="group.module">
    <el-option v-for="p in group.items" :key="p.code" :value="p.code" :label="`${p.displayName} · ${p.code}`" :disabled="p.status !== 'ACTIVE'">
      <div class="pick-option"><strong>{{p.displayName}}</strong><span class="code-text">{{p.code}}</span></div>
    </el-option>
  </el-option-group></el-select>
</template>
<style scoped>.pick-option{display:flex;align-items:center;justify-content:space-between;gap:16px}.pick-option strong{font-weight:500}.pick-option .code-text{font-size:11px;overflow:hidden;text-overflow:ellipsis}</style>
