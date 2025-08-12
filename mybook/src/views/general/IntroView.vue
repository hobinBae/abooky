<template>
  <div class="intro-container">
    <ThreeScene
      ref="threeSceneRef"
      @background-loaded="onBackgroundLoaded"
      @loaded="onSceneLoaded"
      @hotspot="onHotspotActivated"
      @yard-animation-finished="onYardAnimationFinished"
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

    <!-- Hotspot 활성화 시 UI -->
    <div v-if="activeHotspot" class="hotspot-ui-container">
      <!-- 중앙 액션 버튼 -->
      <div class="hotspot-actions">
        <button class="action-button" @click="goToPage">
          {{ getButtonLabel(activeHotspot) }}
        </button>
        <button class="return-button" @click="returnToYard">
          마당으로
        </button>
      </div>

      <!-- 좌우 화살표 네비게이션 -->
      <div class="nav-control left" @click="navigateLeft">
        <div class="nav-arrow">
          <span>&larr;</span>
        </div>
        <span class="nav-text">{{ leftHotspotLabel }}</span>
      </div>
      <div class="nav-control right" @click="navigateRight">
        <span class="nav-text">{{ rightHotspotLabel }}</span>
        <div class="nav-arrow">
          <span>&rarr;</span>
        </div>
        
      </div>
    </div>

    <!-- Hotspot Title Text -->
    <div v-if="activeHotspot" class="hotspot-title-container">
      <p :key="activeHotspot" class="hotspot-title-text">
        <template v-for="(item, index) in hotspotTitleChars" :key="index">
          <br v-if="item.type === 'br'">
          <span v-else class="char">{{ item.value === ' ' ? '&nbsp;' : item.value }}</span>
        </template>
      </p>
    </div>

    <!-- 마당 UI -->
    <div v-if="!activeHotspot && hasEntered && isYardReady" class="hotspot-ui-container">
      <div class="nav-control left" @click="goToHotspot('library')">
        <div class="nav-arrow">
          <span>&larr;</span>
        </div>
        <span class="nav-text">서재</span>
      </div>
      <div class="hotspot-actions">
        <button class="return-button" @click="goToHotspot('store')">
          서점
        </button>
      </div>
      <div class="nav-control right" @click="goToHotspot('create')">
        <span class="nav-text">책 집필</span>
        <div class="nav-arrow">
          <span>&rarr;</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
export default {
  name: 'IntroView'
}
</script>

<script setup lang="ts">
import { ref, onActivated, watch, nextTick, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import ThreeScene from '@/components/ThreeScene.vue'
import { gsap } from 'gsap'

const emit = defineEmits(['intro-finished', 'yard-ready'])
const router = useRouter()
const route = useRoute()
const threeSceneRef = ref<InstanceType<typeof ThreeScene> | null>(null)
const showEnterButton = ref(false)
const hasEntered = ref(false)
const isYardReady = ref(false)

const isLoading = ref(false)
const isTextVisible = ref(false)
const showLine1 = ref(false)
const showLine2 = ref(false)

const activeHotspot = ref<string | null>(null);
const hotspots = ['store', 'create', 'library'];

const leftHotspotLabel = computed(() => {
  if (!activeHotspot.value) return '';
  const currentIndex = hotspots.indexOf(activeHotspot.value);
  const prevIndex = (currentIndex - 1 + hotspots.length) % hotspots.length;
  return getNavLabel(hotspots[prevIndex]);
});

const rightHotspotLabel = computed(() => {
  if (!activeHotspot.value) return '';
  const currentIndex = hotspots.indexOf(activeHotspot.value);
  const nextIndex = (currentIndex + 1) % hotspots.length;
  return getNavLabel(hotspots[nextIndex]);
});

const hotspotTitleChars = computed(() => {
  if (!activeHotspot.value) return [];
  const chars = [];
  for (const char of getHotspotTitle(activeHotspot.value)) {
    if (char === '\n') {
      chars.push({ type: 'br' });
    } else {
      chars.push({ type: 'char', value: char });
    }
  }
  return chars;
});

const hotspotCameraPositions: { [key: string]: any } = {
  store: { x: -5.5, y: 2.5, z: 3.5, targetX: -1, targetY: 2, targetZ: 3.579 },
  create: { x: 0, y: 2.5, z: 3.5, targetX: 0, targetY: 2, targetZ: 3.579 },
  library: { x: 5.5, y: 2.5, z: 3.5, targetX: 1, targetY: 2, targetZ: 3.579 },
  center: { x: 5.5, y: 2.5, z: 14, targetX: 1, targetY: 3, targetZ: 3.579 }
};

watch(activeHotspot, (newVal) => {
  if (newVal) {
    nextTick(() => {
      gsap.fromTo('.char', 
        { opacity: 0, y: 20 },
        { 
          opacity: 1, 
          y: 0,
          duration: 0.5, 
          stagger: 0.08,
          ease: 'power2.out' 
        }
      );
    });
  }
});

watch(
  () => route.query,
  (newQuery) => {
    if (newQuery.from === 'home') {
      returnToYard()
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
        if (threeSceneRef.value) {
          isLoading.value = true
          threeSceneRef.value.loadModel()
        }
      }
    })
    tl.from('.wipe-in-1', { clipPath: 'inset(0 100% 0 0)', duration: 2, ease: 'power2.inOut' })
      .from('.wipe-in-2', { clipPath: 'inset(0 100% 0 0)', duration: 2, ease: 'power2.inOut' }, '-=1.0')
  })
}

const onSceneLoaded = () => {
  isLoading.value = false
  showEnterButton.value = true
}

const enterYard = () => {
  if (threeSceneRef.value) {
    gsap.to('.animated-text-container', {
      opacity: 0,
      duration: 0.7,
      ease: 'power1.in',
      onComplete: () => {
        isTextVisible.value = false
      }
    })
    threeSceneRef.value.moveToYard();
    showEnterButton.value = false;
    hasEntered.value = true;
    emit('intro-finished');
  }
}

const onHotspotActivated = (hotspotName: string) => {
  activeHotspot.value = hotspotName
}

const onYardAnimationFinished = () => {
  isYardReady.value = true;
}

watch(isYardReady, (newValue) => {
  if (newValue) {
    emit('yard-ready');
  }
});

const returnToYard = () => {
  if (threeSceneRef.value?.moveCameraTo) {
    const { x, y, z, targetX, targetY, targetZ } = hotspotCameraPositions.center;
    threeSceneRef.value.moveCameraTo(x, y, z, targetX, targetY, targetZ)
    activeHotspot.value = null
  }
}

const navigate = (direction: 'left' | 'right') => {
  if (!activeHotspot.value || !threeSceneRef.value) return;

  const currentIndex = hotspots.indexOf(activeHotspot.value);
  let nextIndex;

  if (direction === 'right') {
    nextIndex = (currentIndex + 1) % hotspots.length;
  } else {
    nextIndex = (currentIndex - 1 + hotspots.length) % hotspots.length;
  }
  
  const nextHotspotName = hotspots[nextIndex];

  // 1. 마당으로 돌아옵니다.
  returnToYard();

  // 2. 마당으로 돌아오는 애니메이션 시간(2초) 후,
  //    마치 사용자가 다음 hotspot을 클릭한 것처럼 동작을 트리거합니다.
  setTimeout(() => {
    const hotspotObject = threeSceneRef.value?.getHotspotByName(nextHotspotName);
    if (hotspotObject && hotspotObject.userData.onClick) {
      hotspotObject.userData.onClick();
    }
  }, 2100); // 애니메이션 시간(2초) + 약간의 여유
}

const navigateLeft = () => navigate('left');
const navigateRight = () => navigate('right');

const goToHotspot = (hotspotName: string) => {
  if (threeSceneRef.value) {
    const hotspotObject = threeSceneRef.value?.getHotspotByName(hotspotName);
    if (hotspotObject && hotspotObject.userData.onClick) {
      hotspotObject.userData.onClick();
    }
  }
};

const getHotspotTitle = (hotspot: string) => {
  switch (hotspot) {
    case 'library':
      return '추억을 보관하고\n함께 읽어보세요'
    case 'store':
      return '사람들의 이야기를\n 둘러보세요'
    case 'create':
      return '세상에 단 하나뿐인\n 책을 만들어보세요'
    default:
      return ''
  }
};

const getButtonLabel = (hotspot: string | null) => {
  if (!hotspot) return '';
  switch (hotspot) {
    case 'library': return '내서재 가기'
    case 'store': return '서점 가기'
    case 'create': return '책 만들러 가기'
    default: return ''
  }
}

const getNavLabel = (hotspot: string) => {
  switch (hotspot) {
    case 'library': return '서재'
    case 'store': return '서점'
    case 'create': return '책 집필'
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
  z-index: 200;
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
  white-space: nowrap;
  margin: 10px 0;
  font-weight: bold;
  font-family: 'EBSHunminjeongeumSaeronL', sans-serif;
}

.text-line.wipe-in-1 {
  font-size: 5rem;
  
}

.text-line.wipe-in-2 {
  font-size: 2.5rem;
  padding-top: 20px;
  opacity: 0.8;
}

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
  font-family: 'EBSHunminjeongeumSaeronL', sans-serif;

}

.loading-container {
  margin: 0;
}

.loading {
  border: 2px solid transparent;
  border-color: transparent #fff transparent #FFF;
  animation: rotate-loading 1.5s linear 0s infinite normal;
  transform-origin: 50% 50%;
  transition: all 0.5s ease-in-out;
}

#loading-text {
  animation: loading-text-opacity 2s linear 0s infinite normal;
  color: #ffffff;
  font-family: 'EBSHunminjeongeumSaeronL', sans-serif;
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
  overflow: hidden;
    font-family: 'EBSHunminjeongeumSaeronL', sans-serif;

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

.hotspot-ui-container {
  position: absolute;
  bottom: 5%;
  left: 0;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding: 0 5%;
  box-sizing: border-box;
  z-index: 100;
}

.hotspot-actions {
  position: absolute;
  bottom: 5%;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0;
}

.action-button {
  position: relative;
  overflow: hidden;
  z-index: 1;
  padding: 8px 20px;
  font-size: 1.2rem;
  border-radius: 30px;
  border: 1px solid white;
  background: transparent;
  color: #fff;
  cursor: pointer;
  font-family: 'EBSHunminjeongeumSaeronL', sans-serif;
  margin-bottom: 200px; /* 마당으로 가기 버튼과의 간격 */
  transition: transform 0.2s ease-in-out, color 0.4s ease;
}

.action-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(138, 154, 91, 0.5);
  transform-origin: top;
  transform: scaleY(0);
  transition: transform 0.4s ease-in-out;
  z-index: -1;
}

.action-button:hover {
  transform: scale(1.1);
  color: white; /* 텍스트 색상 유지 또는 변경 */
}

.action-button:hover::before {
  transform-origin: bottom;
  transform: scaleY(1);
}

.return-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  font-family: 'EBSHunminjeongeumSaeronL', sans-serif;
  font-size: 2rem;
  font-weight: normal;
  text-shadow: 1px 1px 3px rgba(0,0,0,0.5);
  transition: transform 0.2s ease-in-out;
  color: rgb(255, 255, 255);
}

.return-button:hover {
  transform: scale(1.1);
}

.nav-control {
  display: flex;
  align-items: center;
  gap: 1rem;
  color: white;
  cursor: pointer;
  transition: transform 0.2s ease-in-out;
}

.nav-control:hover {
  transform: scale(1.1);
}
.nav-arrow {
  font-family: 'MaruBuri', sans-serif;
  background: none;
  color: rgb(255, 255, 255);
  width: 50px;
  height: 50px;
  border-radius: 50%;
  font-size: 3rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}
.nav-text {
  font-family: 'EBSHunminjeongeumSaeronL', sans-serif; /* 새로 추가한 폰트 적용 */
  font-size: 3rem; /* 폰트 크기 조정 */
  font-weight: normal; /* ttf 폰트는 bold 대신 normal로 설정 */
  text-shadow: 1px 1px 3px rgba(0,0,0,0.5);
  color: rgb(255, 255, 255);
}

.hotspot-title-container {
  position: absolute;
  top: 20%;
  left: 50%;
  transform: translateX(-50%);
  color: white;
  text-align: center;
  z-index: 100;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.7);
  pointer-events: none;
}

.hotspot-title-text {
  font-family: 'EBSHunminjeongeumSaeronL', sans-serif;
  font-size: 6rem;
  font-weight: normal;
  white-space: pre-wrap;
  color: rgb(255, 255, 255);
}

.char {
  display: inline-block;
}

</style>
