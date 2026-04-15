<template>
  <el-tag :type="type" :size="size" :effect="effect">
    {{ text }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getStatusType, getStatusText } from '@/utils/formatters'

const props = defineProps<{
  status: string
  text?: string
  type?: string
  size?: 'small' | 'default' | 'large'
  effect?: 'light' | 'dark' | 'plain'
}>()

const computedType = computed(() => {
  if (props.type) return props.type
  return getStatusType(props.status)
})

const computedText = computed(() => {
  if (props.text) return props.text
  return getStatusText(props.status)
})
</script>
