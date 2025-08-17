import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import apiClient from '@/api'
import router from '@/router'

interface User {
  memberId: number;
  email: string;
  name: string;
  nickname: string;
  intro?: string;
  profileImageUrl?: string;
  // 필요에 따라 다른 사용자 정보 필드 추가
}

export const useAuthStore = defineStore('auth', () => {
  // 상태(State)
  const accessToken = ref<string | null>(null)
  const user = ref<User | null>(null) // 사용자 정보를 저장할 상태

  // 게터(Getters)
  const isLoggedIn = computed(() => !!accessToken.value)

  // 액션(Actions)
  async function login(loginData: { email: string; password: string }) {
    try {
      const response = await apiClient.post('/api/v1/auth/login', loginData);
      accessToken.value = response.data.data.accessToken;
      // 백엔드에서 refreshToken은 HttpOnly 쿠키로 설정해줄 것으로 가정
      await fetchUserInfo(); // 로그인 후 사용자 정보 가져오기
    } catch (error) {
      clearSession(); // 로그인 과정 중 어느 단계에서든 실패하면 세션 정리
      throw error; // 오류를 호출한 컴포넌트로 다시 던져서 처리하도록 함
    }
  }

  async function logout() {
    try {
      await apiClient.post('/api/v1/auth/logout')
    } catch (error) {
      // logout 실패는 세션 종료에 영향을 주지 않으므로 에러 로그만 남김
      console.error('Logout API call failed:', error)
    } finally {
      clearSession()
      router.push('/login')
    }
  }

  async function refreshUserToken() {
    try {
      // 백엔드에 /refresh-token 요청 시 브라우저가 자동으로 HttpOnly 쿠키를 포함하여 보냄
      // @RequestBody가 있는 API와의 호환성을 위해 Content-Type을 JSON으로 명시하고 빈 객체를 전송
      const response = await apiClient.post('/api/v1/auth/refresh-token', {});
      accessToken.value = response.data.data.accessToken
      await fetchUserInfo() // 토큰 재발급 후 사용자 정보도 갱신
      return accessToken.value
    } catch (error) {
      clearSession()
      return null
    }
  }

  async function fetchUserInfo() {
    if (!accessToken.value) return
    try {
      // '/api/v1/members/me' 와 같은 사용자 정보 조회 엔드포인트가 있다고 가정
      const response = await apiClient.get('/api/v1/members/me')
      const userData = response.data.data
      
      // 프로필 이미지가 없거나 빈 값인 경우 기본 이미지로 설정
      if (!userData.profileImageUrl || userData.profileImageUrl.trim() === '') {
        userData.profileImageUrl = '/images/profile.png'
      }
      
      user.value = userData
    } catch (error) {
      console.error('Failed to fetch user info:', error)
      // clearSession()은 login 함수에서 처리하므로 여기서는 오류만 던짐
      throw error;
    }
  }

  function clearSession() {
    accessToken.value = null
    user.value = null
    // localStorage 관련 코드는 모두 제거
  }

  // 소셜 로그인용 토큰 직접 설정 함수
  function setSocialLoginToken(token: string) {
    accessToken.value = token
  }

  return {
    accessToken,
    user,
    isLoggedIn,
    login,
    logout,
    refreshUserToken,
    fetchUserInfo,
    clearSession,
    setSocialLoginToken
  }
})
