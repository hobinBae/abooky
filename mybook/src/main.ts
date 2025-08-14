import './assets/main.css'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'
import { setTokenProvider, setTokenRefresher } from '@/api'


const pinia = createPinia()
const app = createApp(App)

app.use(pinia)
// authStore를 사용하기 위해 라우터보다 먼저 Pinia를 등록해야 합니다.

async function initializeApp() {
  const authStore = useAuthStore()

  // API 클라이언트에 토큰 공급자 등록
  setTokenProvider(() => authStore.accessToken)
  
  // API 클라이언트에 토큰 재발급 함수 등록
  setTokenRefresher(async () => {
    try {
      return await authStore.refreshUserToken()
    } catch (error) {
      console.error('토큰 재발급 실패:', error)
      return null
    }
  })

  try {
    // 앱 시작 시 토큰 재발급을 시도하여 로그인 상태를 복원합니다.
    await authStore.refreshUserToken()
  } catch {
    // refreshUserToken 내부에서 에러 처리를 하므로 여기서는 무시합니다.
  }

  app.use(router)
  app.mount('#app')
}

initializeApp()
