<template>
  <Navbar />
  <RouterView />
  <Footer />  
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import Navbar from './components/Navbar.vue'
import Footer from './components/Footer.vue'


</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@400;700&display=swap');

body {
  font-family: 'Montserrat', sans-serif;
  padding-top: 56px; /* Adjust this value based on your navbar's height */
}
</style>
