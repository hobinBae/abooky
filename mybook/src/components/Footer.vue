<template>
  <footer class="footer mt-auto">
    <div class="container text-center">
      <div class="footer-links">
        <router-link to="/bookstore">서점</router-link>
        <router-link to="/create-book">책 집필</router-link>
        <router-link to="/my-library">서재</router-link>
      </div>
      <div class="social-links">
        <a href="#" target="_blank" aria-label="KakaoTalk Channel"><i class="bi bi-chat-fill"></i> 공식 카카오톡 채널</a>
        <a href="#" target="_blank" aria-label="Instagram"><i class="bi bi-instagram"></i> 공식 인스타그램</a>
        <a href="#" target="_blank" aria-label="Facebook"><i class="bi bi-facebook"></i> 공식 페이스북</a>
      </div>
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
  padding-top: 4rem;
  /* Increased padding for content */
  padding-bottom: 2rem;
  /* More padding for content */

}

.social-links a {
  display: flex;
  /* Align icon and text */
  align-items: center;
  /* Center vertically */
  gap: 0.5rem;
  /* Space between icon and text */
}

.footer .container {
  font-size: 0.9rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  /* Space between sections */
}

.footer-links,
.social-links {
  font-size: 0.9rem;

  display: flex;
  gap: 1.5rem;
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
  font-size: 0.85rem;
  margin-top: 1rem;
  /* Space above copyright */
}
</style>
