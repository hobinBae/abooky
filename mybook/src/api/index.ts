import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true, // 쿠키를 포함하여 요청
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터
apiClient.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore();
    const token = authStore.accessToken;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // 401 에러이고, 실패한 요청이 토큰 재발급 요청이 아닐 경우에만 재시도
    if (error.response.status === 401 && originalRequest.url !== '/api/v1/auth/refresh-token') {
      const authStore = useAuthStore();
      try {
        const newAccessToken = await authStore.refreshUserToken();
        if (newAccessToken) {
          // 재발급 성공 시, 원래 요청의 헤더를 갱신하여 다시 시도
          originalRequest.headers['Authorization'] = 'Bearer ' + newAccessToken;
          return apiClient(originalRequest);
        }
      } catch (refreshError) {
        // refreshUserToken 내부에서 이미 세션 정리 및 에러 처리를 하므로,
        // 여기서는 추가 작업 없이 에러를 그대로 반환
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
