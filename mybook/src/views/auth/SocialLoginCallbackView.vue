<template>
  <div class="callback-container">
    <div class="callback-content">
      <div v-if="isLoading" class="loading-state">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로그인 처리 중...</span>
        </div>
        <h3>소셜 로그인 처리 중...</h3>
        <p>잠시만 기다려주세요.</p>
      </div>
      
      <div v-else-if="error" class="error-state">
        <i class="bi bi-exclamation-triangle text-danger" style="font-size: 3rem;"></i>
        <h3>로그인 실패</h3>
        <p>{{ error }}</p>
        <button @click="goToLogin" class="btn btn-primary">로그인 페이지로 이동</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isLoading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    // URL에서 토큰 파라미터 추출
    const token = route.query.token as string
    const errorParam = route.query.error as string
    
    if (errorParam) {
      error.value = '소셜 로그인에 실패했습니다. 다시 시도해주세요.'
      isLoading.value = false
      return
    }
    
    if (!token) {
      error.value = '인증 토큰을 받지 못했습니다.'
      isLoading.value = false
      return
    }
    
    // 토큰을 auth store에 저장
    authStore.setSocialLoginToken(token)
    
    // 사용자 정보 가져오기
    await authStore.fetchUserInfo()
    
    // 성공적으로 로그인되면 메인 페이지로 리다이렉트
    const redirectPath = sessionStorage.getItem('socialLoginRedirect') || '/'
    sessionStorage.removeItem('socialLoginRedirect')
    
    router.replace(redirectPath)
    
  } catch (err) {
    console.error('Social login callback error:', err)
    error.value = '로그인 처리 중 오류가 발생했습니다.'
    isLoading.value = false
  }
})

function goToLogin() {
  router.push('/login')
}
</script>

<style scoped>
.callback-container {
  min-height: calc(100vh - 56px);
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--background-color);
}

.callback-content {
  text-align: center;
  padding: 2rem;
  max-width: 400px;
}

.loading-state, .error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.loading-state h3, .error-state h3 {
  color: #333;
  margin: 0;
  font-family: 'SCDream4', sans-serif;
}

.loading-state p, .error-state p {
  color: #666;
  margin: 0;
}

.btn {
  margin-top: 1rem;
  background-color: #8A9A5B;
  border-color: #8A9A5B;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
}

.btn:hover {
  background-color: #6F7D48;
  border-color: #6F7D48;
}
</style>