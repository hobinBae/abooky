<template>
  <nav class="navbar navbar-expand-lg fixed-top" 
       :class="{ 'navbar-content-hidden': isIntroActive, 'navbar-dark': isHome, 'navbar-light': !isHome }">
    <div class="container-fluid">
      <router-link class="navbar-brand" to="/">
        <span style="font-size: 1.5rem; font-weight: bold;">MyBook</span>
      </router-link>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0">
          <li class="nav-item">
            <router-link class="nav-link" to="/bookstore">서점</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/create-book">책만들기</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/my-library">내서재</router-link>
          </li>
          <li class="nav-item" v-if="isLoggedIn">
            <router-link class="nav-link" to="/my-page">마이페이지</router-link>
          </li>
        </ul>
        <ul class="navbar-nav">
          <template v-if="!isLoggedIn">
            <li class="nav-item">
              <router-link class="nav-link" to="/login">로그인</router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" to="/signup">회원가입</router-link>
            </li>
          </template>
          <template v-else>
            <li class="nav-item">
              <router-link class="nav-link" to="/my-page">마이페이지</router-link>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="#" @click.prevent="logout">로그아웃</a>
            </li>
          </template>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref } from 'vue';

defineProps({
  isIntroActive: Boolean,
  isHome: Boolean
});

const isLoggedIn = ref(false); // Dummy login state

const logout = () => {
  isLoggedIn.value = false;
  alert('로그아웃 되었습니다.');
};
</script>

<style scoped>
.navbar {
  background-color: transparent !important;
  z-index: 1030;
  transition: background-color 0.5s ease;
}

/* 인트로 활성 상태일 때 내용 숨김 */
.navbar-content-hidden .navbar-brand,
.navbar-content-hidden .navbar-toggler,
.navbar-content-hidden .nav-link {
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.5s ease, visibility 0.5s ease;
}

/* 기본 상태 (내용 보임) */
.navbar-brand,
.navbar-toggler,
.nav-link {
  opacity: 1;
  visibility: visible;
  transition: opacity 0.5s ease, visibility 0.5s ease, color 0.5s ease;
  font-size: 20px !important;
}
</style>