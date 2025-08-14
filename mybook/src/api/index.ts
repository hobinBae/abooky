import axios from 'axios';

const getBaseURL = () => {
  if (import.meta.env.DEV) {
    // 개발 환경: 직접 백엔드 포트
    return 'http://localhost:8080'
  } else {
    // 프로덕션 환경: 현재 도메인 사용 (Nginx 프록시 경유)
    return ''  // 빈 문자열 = 현재 도메인 기준 상대 경로
  }
}

const apiClient = axios.create({
  baseURL: getBaseURL(),
  withCredentials: true, // 쿠키를 포함하여 요청
  headers: {
    'Content-Type': 'application/json',
  },
});

// 토큰 공급자 함수를 저장할 변수
let getAccessToken: (() => string | null) | null = null;

// 토큰 공급자 등록 함수
export function setTokenProvider(provider: () => string | null) {
  getAccessToken = provider;
}

// 요청 인터셉터
apiClient.interceptors.request.use(
  (config) => {
    const token = getAccessToken?.();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 토큰 재발급 함수를 저장할 변수
let refreshToken: (() => Promise<string | null>) | null = null;

// 토큰 재발급 함수 등록
export function setTokenRefresher(refresher: () => Promise<string | null>) {
  refreshToken = refresher;
}

// 응답 인터셉터
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    console.log('❌ API 응답 에러:', {
      status: error.response?.status,
      url: error.config?.url,
      method: error.config?.method,
      message: error.message
    });
    
    const originalRequest = error.config;

    // 401 에러이고, 실패한 요청이 토큰 재발급 요청이 아니고, 이미 재시도하지 않은 경우에만 재시도
    if (error.response?.status === 401 &&
        originalRequest.url !== '/api/v1/auth/refresh-token' &&
        !originalRequest._retry) {
      console.log('🔄 401 에러 감지, 토큰 재발급 시도...');
      originalRequest._retry = true;
      
      if (refreshToken) {
        try {
          const newAccessToken = await refreshToken();
          if (newAccessToken) {
            console.log('✅ 토큰 재발급 성공, 요청 재시도');
            // 재발급 성공 시, 원래 요청의 헤더를 갱신하여 다시 시도
            originalRequest.headers['Authorization'] = 'Bearer ' + newAccessToken;
            return apiClient(originalRequest);
          } else {
            console.log('❌ 토큰 재발급 실패');
          }
        } catch (refreshError) {
          console.log('❌ 토큰 재발급 중 에러:', refreshError);
          return Promise.reject(refreshError);
        }
      } else {
        console.log('❌ 토큰 재발급 함수가 등록되지 않음');
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
