<template>
  <Navbar v-if="isNavbarVisible" />
  <RouterView @update-navbar="updateNavbarVisibility" />
  <Footer v-if="isNavbarVisible" />
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, RouterView } from 'vue-router'
import Navbar from './components/Navbar.vue'
import Footer from './components/Footer.vue'

const route = useRoute()
const isNavbarVisible = ref(false)

const updateNavbarVisibility = (isVisible: boolean) => {
  isNavbarVisible.value = isVisible
}

// 경로가 변경될 때마다 네비게이션 바 상태를 재설정
watch(
  () => route.path,
  (newPath) => {
    // 인트로 페이지('/')가 아니면 항상 네비게이션 바를 표시
    if (newPath !== '/') {
      isNavbarVisible.value = true
    } else {
      // 인트로 페이지로 돌아오면 네비게이션 바를 숨김 (초기 상태)
      isNavbarVisible.value = false
    }
  }
)
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@400;700&display=swap');

body {
  font-family: 'Montserrat', sans-serif;
  padding-top: 56px; /* Adjust this value based on your navbar's height */
}
</style>
