<template>
  <!-- 인트로 페이지가 아닐 때만 Navbar와 Footer 보여줌 -->
  <Navbar v-if="showLayout" />
  <RouterView />
  <Footer v-if="showLayout" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { RouterView } from 'vue-router'
import Navbar from './components/Navbar.vue'
import Footer from './components/Footer.vue'

// 현재 라우트 가져오기
const route = useRoute()

// 인트로 페이지('/')일 경우 레이아웃 요소 숨김
const showLayout = computed(() => route.path !== '/')
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@400;700&display=swap');

body {
  font-family: 'Montserrat', sans-serif;
  padding-top: 56px; /* Adjust this value based on your navbar's height */
}
</style>
