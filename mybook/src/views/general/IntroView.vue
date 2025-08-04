<template>
  <div class="intro-container">
    <ThreeScene
      ref="threeSceneRef"
      @background-loaded="onBackgroundLoaded"
      @loaded="onSceneLoaded"
      @hotspot="onHotspotActivated"
    />

    <!-- 로딩 애니메이션 -->
    <div v-if="isLoading" class="loading-wrapper">
      <div class="loading-container">
        <div class="loading"></div>
        <div id="loading-text">책 펼치는 중</div>
      </div>
    </div>

    <!-- 애니메이션 텍스트 -->
    <div v-if="isTextVisible" class="animated-text-container">
      <p v-if="showLine1" class="text-line wipe-in-1">나만의 자서전, 우리들의 이야기</p>
      <p v-if="showLine2" class="text-line wipe-in-2">
        당신의 소중한 시간들을 책으로 엮어보세요
      </p>
    </div>

    <button v-if="showEnterButton" class="enter-button" @click="enterYard">
      시작하기
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

<script lang="ts">
export default {
  name: 'IntroView'
}
</script>

<script setup lang="ts">
import { ref, onActivated, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import ThreeScene from '@/components/ThreeScene.vue'
import { gsap } from 'gsap'

const emit = defineEmits(['intro-finished'])
const router = useRouter()
const route = useRoute()
const threeSceneRef = ref<InstanceType<typeof ThreeScene> | null>(null)
const showEnterButton = ref(false)
const activeHotspot = ref<string | null>(null)
const hasEntered = ref(false) // '들어가기' 버튼 클릭 여부

// 로딩 및 애니메이션 상태
const isLoading = ref(false) // 초기 상태를 false로 변경
const isModelLoaded = ref(false)
const isTextAnimationFinished = ref(false)
const isTextVisible = ref(false)
const showLine1 = ref(false)
const showLine2 = ref(false)

watch(
  () => route.query,
  (newQuery) => {
    if (newQuery.from === 'home') {
      returnToYard()
      // 쿼리 파라미터를 제거하여 뒤로가기 시에도 이동하는 것을 방지
      router.replace({ query: {} })
    }
  },
  { immediate: true }
)

onActivated(() => {
  if (hasEntered.value) {
    emit('intro-finished')
  }
})

const onBackgroundLoaded = () => {
  isTextVisible.value = true
  showLine1.value = true
  showLine2.value = true

  nextTick(() => {
    const tl = gsap.timeline({
      onComplete: () => {
        isTextAnimationFinished.value = true
        // 텍스트 애니메이션이 끝나면, 모델이 아직 로딩 중일 경우 로딩 아이콘 표시
        if (!isModelLoaded.value) {
          isLoading.value = true
        } else {
          // 모델 로딩이 이미 끝났다면 바로 버튼 표시
          showEnterButton.value = true
        }
      }
    })
    tl.from('.wipe-in-1', { clipPath: 'inset(0 100% 0 0)', duration: 2, ease: 'power2.inOut' })
      .from('.wipe-in-2', { clipPath: 'inset(0 100% 0 0)', duration: 2, ease: 'power2.inOut' }, '-=1.0')
  })
}

const onSceneLoaded = () => {
  isModelLoaded.value = true
  // 모델 로딩이 끝나면, 텍스트 애니메이션이 이미 끝났는지 확인
  if (isTextAnimationFinished.value) {
    // 텍스트 애니메이션이 끝나서 로딩 아이콘이 보이고 있다면, 숨기고 버튼 표시
    isLoading.value = false
    showEnterButton.value = true
  }
}

const enterYard = () => {
  if (threeSceneRef.value) {
    // 텍스트 사라지는 애니메이션과 카메라 이동을 동시에 시작
    gsap.to('.animated-text-container', {
      opacity: 0,
      duration: 0.7, // 카메라 이동 시간과 비슷하게 설정하여 자연스럽게
      ease: 'power1.in'
    });

    threeSceneRef.value.moveToYard();
    showEnterButton.value = false;
    hasEntered.value = true;
    emit('intro-finished');
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
.loading-wrapper {
  position: absolute;
  bottom: 25%;
  left: 50%;
  transform: translateX(-50%);
  z-index: 200; /* 다른 요소들 위에 표시 */
}

.animated-text-container {
  position: absolute;
  top: 40%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  text-align: center;
  z-index: 100;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.7);
}

.text-line {
  white-space: nowrap; /* 줄바꿈 방지 */
  margin: 10px 0;
  font-weight: bold;
}

.text-line.wipe-in-1 {
  font-size: 5rem;
}

.text-line.wipe-in-2 {
  font-size: 2.5rem;
  padding-top: 20px;
  opacity: 0.8;
}

/* GSAP가 애니메이션을 처리하므로 CSS 애니메이션은 제거합니다. */

/** BEGIN LOADING CSS **/
@keyframes rotate-loading {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes loading-text-opacity {
  0% { opacity: 0; }
  20% { opacity: 0; }
  50% { opacity: 1; }
  100% { opacity: 0; }
}

.loading-container,
.loading {
  height: 100px;
  position: relative;
  width: 100px;
  border-radius: 100%;
}

.loading-container {
  margin: 0; /* 위치를 정확히 맞추기 위해 margin 제거 */
}

.loading {
  border: 2px solid transparent;
  border-color: transparent #fff transparent #FFF;
  animation: rotate-loading 1.5s linear 0s infinite normal;
  transform-origin: 50% 50%;
  transition: all 0.5s ease-in-out;
}

/* .loading-container:hover .loading {
  border-color: transparent #E45635 transparent #E45635;
} */

#loading-text {
  animation: loading-text-opacity 2s linear 0s infinite normal;
  color: #ffffff;
  font-family: "Helvetica Neue", "Helvetica", "arial", sans-serif;
  font-size: 14px;
  font-weight: bold;
  margin-top: 41px;
  opacity: 0;
  position: absolute;
  text-align: center;
  text-transform: uppercase;
  top: 0;
  width: 100px;
}
/** END LOADING CSS **/

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
  bottom: 25%;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: transparent;
  color: #fff;
  border: 2px solid #fff;
  cursor: pointer;
  z-index: 100;
  font-size: 1.2rem;
  font-weight: bold;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.5s ease-in-out;
  overflow: hidden; /* 회전 효과를 위해 */
}

.enter-button::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  border: 2px solid transparent;
  border-top-color: #E45635;
  border-right-color: #E45635;
  border-radius: 50%;
  animation: rotate-loading 2s linear infinite;
  opacity: 0;
  transition: opacity 0.5s ease-in-out;
}

.enter-button:hover {
  background: rgba(255, 255, 255, 0.1);
}

.enter-button:hover::before {
  opacity: 1;
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

#vue-devtools {
  display: none !important;
}
</style>
