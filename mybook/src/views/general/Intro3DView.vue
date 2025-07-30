<template>
  <div ref="threeContainer" class="three-scene"></div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import init3DScene from '@/three/init3DScene'

// DOM 참조용 ref 선언
const threeContainer = ref<HTMLDivElement | null>(null)

onMounted(() => {
  if (threeContainer.value) {
    init3DScene(threeContainer.value)
  }
})
</script>

<style scoped>
.three-scene {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  position: relative;
}
</style>
