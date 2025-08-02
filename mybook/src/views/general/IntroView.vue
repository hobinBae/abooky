<template>
  <div class="intro-container">
    <ThreeScene ref="threeSceneRef" @loaded="onSceneLoaded" @hotspot="onHotspotActivated" />
    <button v-if="showEnterButton" class="enter-button" @click="enterYard">
      들어가기
    </button>
    <div v-if="activeHotspot" class="hotspot-actions">
      <button class="action-button" @click="goToPage">
        {{ getButtonLabel(activeHotspot) }}
      </button>
      <button class="return-button" @click="returnToYard">
        <img src="/3D/back_icon.png" alt="돌아가기" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import ThreeScene from '@/components/ThreeScene.vue'

const emit = defineEmits(['intro-finished'])
const router = useRouter()
const threeSceneRef = ref<InstanceType<typeof ThreeScene> | null>(null)
const showEnterButton = ref(false)
const activeHotspot = ref<string | null>(null)

const onSceneLoaded = () => {
  showEnterButton.value = true
}

const enterYard = () => {
  if (threeSceneRef.value) {
    threeSceneRef.value.moveToYard()
    showEnterButton.value = false
    emit('intro-finished')
  }
}

const onHotspotActivated = (hotspotName: string) => {
  activeHotspot.value = hotspotName
}

const returnToYard = () => {
  if (threeSceneRef.value?.moveCameraTo) {
    threeSceneRef.value.moveCameraTo(5.5, 2.5, 14, 1, 3, 3.579)
    activeHotspot.value = null
  }
}

const getButtonLabel = (hotspot: string) => {
  switch (hotspot) {
    case 'library': return '내서재 가기'
    case 'store': return '서점 가기'
    case 'create': return '책 만들러 가기'
    default: return ''
  }
}

const goToPage = () => {
  if (!activeHotspot.value) return
  const pathMap: { [key: string]: string } = {
    library: '/my-library',
    store: '/bookstore',
    create: '/create-book'
  }
  router.push(pathMap[activeHotspot.value])
}
</script>

<style scoped>
.intro-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

.enter-button {
  position: absolute;
  bottom: 10%;
  left: 50%;
  transform: translateX(-50%);
  padding: 16px 32px;
  font-size: 1.2rem;
  border-radius: 8px;
  border: none;
  background: #2d5c2f;
  color: #fff;
  cursor: pointer;
  z-index: 100;
  transition: opacity 0.5s;
}

.hotspot-actions {
  position: absolute;
  bottom: 10%;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  z-index: 100;
}

.action-button {
  padding: 12px 24px;
  font-size: 1rem;
  border-radius: 8px;
  border: none;
  background: rgba(45, 92, 47, 0.8);
  color: #fff;
  cursor: pointer;
}

.return-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}

.return-button img {
  width: 60px;
  height: 60px;
  display: block;
}
</style>
