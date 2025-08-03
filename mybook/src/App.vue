<template>
  <Navbar :is-intro-active="isIntroActive" :is-home="isIntro" />
  <main class="main-content">
    <RouterView @intro-finished="onIntroFinished" />
  </main>
  <Footer v-if="!isIntro" />
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRoute, RouterView } from 'vue-router'
import Navbar from './components/Navbar.vue'
import Footer from './components/Footer.vue'

const route = useRoute()

// 현재 경로가 인트로('/')인지 여부
const isIntro = computed(() => route.path === '/')

// 인트로 애니메이션이 활성 상태인지 (들어가기 전)
const isIntroActive = ref(isIntro.value)

// IntroView에서 '들어가기'를 클릭하면 호출될 함수
const onIntroFinished = () => {
  isIntroActive.value = false
}

// 경로가 변경될 때마다 상태를 다시 동기화
watch(
  () => route.path,
  (newPath) => {
    isIntroActive.value = newPath === '/'
  },
  { immediate: true }
)
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@400;700&display=swap');

#app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

body {
  font-family: 'Montserrat', sans-serif;
  transition: padding-top 0.3s ease; /* 부드러운 전환 효과 추가 */
}

.main-content {
  flex: 1;
}
</style>
