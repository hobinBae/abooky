<template>
  <Navbar :is-intro-active="isIntroActive" :is-home="isIntro" />
  <main class="main-content">
    <router-view v-slot="{ Component }">
      <keep-alive include="IntroView">
        <component :is="Component" @intro-finished="onIntroFinished" />
      </keep-alive>
    </router-view>
  </main>
  <Footer v-if="!isIntro" />
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, RouterView } from 'vue-router'
import Navbar from './components/Navbar.vue'
import Footer from './components/Footer.vue'

const route = useRoute()

// 현재 경로가 인트로('/')인지 여부
const isIntro = computed(() => route.path === '/')

// '들어가기'를 눌렀는지 여부를 저장하는 상태
const hasIntroBeenFinished = ref(false)

// 인트로 애니메이션이 활성 상태인지 여부를 계산
const isIntroActive = computed(() => {
  // isIntro는 현재 경로가 '/'인지 여부
  // hasIntroBeenFinished는 '들어가기'를 눌렀는지 여부
  return isIntro.value && !hasIntroBeenFinished.value
})

// IntroView에서 '들어가기'를 클릭하면 호출될 함수
const onIntroFinished = () => {
  hasIntroBeenFinished.value = true
}

onMounted(() => {
  // 앱 초기화 로직은 main.ts로 이동되었습니다.
})
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
