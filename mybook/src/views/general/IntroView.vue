<template>
  <div>
    <!-- 3D 씬 컴포넌트 -->
    <ThreeScene ref="threeScene" @loaded="onLoaded" @hotspot="onHotspot" />

    <!-- 모델 로딩 후 '들어가기' 버튼 표시 -->
    <button
      v-if="showEnterButton"
      class="enter-button"
      @click="enterMain"
    >
      들어가기
    </button>

    <!-- 핫스팟 도달 시 해당 위치의 버튼 표시 -->
    <button
      v-if="visibleButton === 'library'"
      class="action-button"
      @click="goTo('/my-library')"
    >
      내서재
    </button>

    <button
      v-if="visibleButton === 'store'"
      class="action-button"
      @click="goTo('/bookstore')"
    >
      서점
    </button>

    <button
      v-if="visibleButton === 'create'"
      class="action-button"
      @click="goTo('/create-book')"
    >
      책 만들기
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import ThreeScene from '@/components/ThreeScene.vue'

const router = useRouter()
const threeScene = ref<InstanceType<typeof ThreeScene> | null>(null);

// '들어가기' 버튼 표시 여부
const showEnterButton = ref(false)

// 현재 위치에 따라 표시할 버튼 상태
const visibleButton = ref<string | null>(null)

// 모델 로딩 완료 시 호출됨
function onLoaded() {
  showEnterButton.value = true
}

// 핫스팟 클릭 후 도달 시 호출됨
function onHotspot(target: string) {
  visibleButton.value = target
}

// '들어가기' 클릭 시 메인페이지 진입
function enterMain() {
  if (threeScene.value) {
    threeScene.value.moveToYard();
  }
  showEnterButton.value = false
  visibleButton.value = null

  // 내비게이션 바 표시를 위해 홈 경로로 라우팅
  // router.push('/home') // 홈으로 바로 이동하는 대신, 카메라 이동 후 다른 상호작용을 기다릴 수 있습니다.
}

// 라우팅 이동
function goTo(path: string) {
  router.push(path)
}
</script>

<style scoped>
div {
  position: relative;
}

.enter-button,
.action-button {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  font-size: 16px;
  z-index: 10;
  border: none;
  border-radius: 8px;
  background-color: #444;
  color: white;
  cursor: pointer;
  transition: 0.2s;
}

.enter-button {
  bottom: 80px;
}

.action-button {
  top: 50%;
  transform: translate(-50%, -50%);
}

.enter-button:hover,
.action-button:hover {
  background-color: #222;
}
</style>
