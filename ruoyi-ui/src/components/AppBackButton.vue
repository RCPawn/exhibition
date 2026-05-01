<template>
  <router-link
    v-if="to"
    :to="to"
    class="hui-back"
    :class="variantClass"
    @pointerdown.stop
    @click.stop="emit('click', $event)"
  >
    <el-icon v-if="showIcon"><ArrowLeft /></el-icon>
    <span><slot>{{ displayLabel }}</slot></span>
  </router-link>
  <button
    v-else
    type="button"
    class="hui-back"
    :class="variantClass"
    :disabled="disabled"
    @pointerdown.stop
    @click.stop="onActivate"
  >
    <el-icon v-if="showIcon"><ArrowLeft /></el-icon>
    <span><slot>{{ displayLabel }}</slot></span>
  </button>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { APP_BACK_PRESET } from '@/constants/portalNavLabels'

const props = defineProps({
  to: [String, Object],
  fallback: {
    type: String,
    default: '/display/home'
  },
  /** Non-empty overrides preset */
  label: {
    type: String,
    default: ''
  },
  /**
   * Preset label key; see constants/portalNavLabels APP_BACK_PRESET
   */
  preset: {
    type: String,
    default: 'simple',
    validator: (v) => ['simple', 'portal', 'cover'].includes(v)
  },
  showIcon: {
    type: Boolean,
    default: true
  },
  disabled: {
    type: Boolean,
    default: false
  },
  variant: {
    type: String,
    default: ''
  },
  plain: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])
const router = useRouter()

const displayLabel = computed(() => {
  if (props.label != null && props.label !== '') return props.label
  return APP_BACK_PRESET[props.preset] || APP_BACK_PRESET.simple
})

const variantClass = computed(() => {
  const v = props.variant
  if (v === 'gallery') return 'hui-back--gallery'
  if (v === 'floating' || v === 'deck') return 'hui-back--floating'
  return ''
})

function onActivate(e) {
  if (props.disabled) return
  emit('click', e)
  if (props.plain) return

  try {
    if (typeof window !== 'undefined' && window.history.length > 1) {
      router.back()
      return
    }
  } catch {
    /* ignore */
  }
  router.push(props.fallback)
}
</script>

<style scoped>
button.hui-back:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
