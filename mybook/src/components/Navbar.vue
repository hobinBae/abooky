<template>
  <nav class="navbar navbar-expand-lg navbar-dark fixed-top" style="background-color: #3D2C20; z-index: 1030;">
    <div class="container-fluid">
      <router-link class="navbar-brand" to="/">
        <span class="navbar-brand" style="color: white; font-size: 1.5rem; font-weight: bold;">MyBook</span>
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
          <li class="nav-item" v-if="isLoggedIn"> <!-- Add v-if="isLoggedIn" here -->
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

const isLoggedIn = ref(false); // Dummy login state

const logout = () => {
  isLoggedIn.value = false;
  alert('로그아웃 되었습니다.');
  // In a real app, you would clear user session/token here
};
</script>

<style scoped>
.nav-link {
  color: rgb(255, 255, 255) !important;
  font-size: 20px !important;
  opacity: 1 !important;
  visibility: visible !important;
  z-index: 9999 !important;
}
</style>