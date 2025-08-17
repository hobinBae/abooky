<template>
  <footer class="footer mt-auto">
    <div class="container text-center">
      <img src="/aaddi.png" alt="Logo" class="footer-logo">
      
      <div class="footer-links">
        <router-link to="/bookstore">서점</router-link>
        <router-link to="/create-book">책 집필</router-link>
        <router-link to="/my-library">내 서재</router-link>
      </div>
      
      <div class="social-links">
        <span>공식 카카오톡 채널</span>
        <span>공식 인스타그램</span>
        <span>공식 페이스북</span>
      </div>
      <span class="c203">Made By 5 TO 0</span>
      <span class="c203">5-CJW | 6-LCE | 7-JUH | 8-JHB | 9-KAY | 0-BHB</span>
      <span class="copyright">&copy; 2025 아 북 이</span>


    </div>
  </footer>
</template>

<script setup lang="ts">
</script>

<style scoped>
.footer {
  width: 100%;
  position: relative;
  z-index: 1;
  /* 다른 요소들 위에 오도록 z-index 설정 */
  background-color: #656b5b;
  /* Dark grey background */
  color: #f8f9fa;
  /* Light text color */
  font-family: 'SCDream4', sans-serif;
  /* Consistent font */
  padding-top: 2rem;
  /* Increased padding for content */
  padding-bottom: 1.8rem;
  /* More padding for content */

}

.social-links a {
  display: flex;
  /* Align icon and text */
  align-items: center;
  /* Center vertically */
  gap: 0.45rem;
  /* Space between icon and text */
  margin-bottom: 1rem;
}

.footer .container {
  font-size: 0.8rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
  /* Space between sections */
}

.footer-links,
.social-links {
  font-size: 0.8rem;

  display: flex;
  gap: 1.35rem;
  /* Space between links */
  flex-wrap: wrap;
  /* Allow links to wrap on smaller screens */
  justify-content: center;
}

.footer-links a,
.social-links a {
  color: #f8f9fa;
  /* Light text color for links */
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s ease-in-out;
}

.footer-links a:hover,
.social-links a:hover {
  color: #ced4da;
  /* Slightly lighter on hover */
}

.copyright {
  font-size: 0.75rem;
  margin-top: 0.9rem;
  /* Space above copyright */
}

.c203 {
  font-size: 0.75rem;
  margin-bottom: 0rem;
  font-family: 'MaruBuri-Light', sans-serif;
}
.footer-logo {
  max-width: 50px;
  /* Limit logo size */
  height: auto;
  margin-bottom: 10px;
}
</style>
