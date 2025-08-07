<template>
  <div class="hello-backend">
    <div class="container">
      <h1>백엔드 String + hello 테스트</h1>
      <p class="description">버튼을 클릭하면 백엔드에서 "ahyoon"을 받아와서 "hello ahyoon"으로 표시합니다</p>

      <button @click="fetchFromBackend" :disabled="loading" class="fetch-btn">
        {{ loading ? '로딩 중...' : '🚀 백엔드에서 데이터 가져오기' }}
      </button>

      <div v-if="result" class="result-box">
        <h2>결과:</h2>
        <p class="result-text">{{ result }}</p>
      </div>

      <div v-if="error" class="error-box">
        <h2>에러:</h2>
        <p>{{ error }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios, { type AxiosError } from 'axios'

// Reactive state
const loading = ref<boolean>(false)
const result = ref<string>('')
const error = ref<string>('')

// Methods
const fetchFromBackend = async (): Promise<void> => {
  loading.value = true
  result.value = ''
  error.value = ''

  try {
    // 백엔드 API 호출 (/cicd/ahyoon → "ahyoon" 반환)
    const response = await axios.get<string>('http://i13c203.p.ssafy.io:8081/cicd/ahyoon')
    const backendData = response.data // 백엔드에서 "ahyoon" 문자열 받아옴

    // 받은 데이터에 "hello " 붙이기
    result.value = `hello ${backendData}`

    console.log('백엔드에서 받은 데이터:', backendData)
    console.log('hello를 붙인 최종 결과:', result.value)

  } catch (err) {
    const axiosError = err as AxiosError
    error.value = `백엔드 연결 실패: ${axiosError.message}`
    console.error('API 에러:', err)

    // 백엔드가 꺼져있을 때 대비한 fallback
    if (axiosError.code === 'ERR_NETWORK') {
      error.value = '백엔드 서버에 연결할 수 없습니다. http://i13c203.p.ssafy.io:8081이 실행 중인지 확인해주세요.'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.hello-backend {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.container {
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  text-align: center;
  max-width: 500px;
  width: 100%;
}

h1 {
  color: #333;
  margin-bottom: 15px;
  font-size: 28px;
  font-weight: 700;
}

.description {
  color: #666;
  margin-bottom: 30px;
  font-size: 14px;
  line-height: 1.5;
}

.fetch-btn {
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 15px 30px;
  font-size: 16px;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.fetch-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.6);
}

.fetch-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.result-box {
  margin-top: 30px;
  padding: 20px;
  background: linear-gradient(45deg, #4facfe, #00f2fe);
  border-radius: 15px;
  color: white;
  animation: fadeIn 0.5s ease-in;
}

.result-text {
  font-size: 24px;
  font-weight: bold;
  margin: 10px 0 0 0;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.2);
}

.error-box {
  margin-top: 30px;
  padding: 20px;
  background: linear-gradient(45deg, #ff6b6b, #ee5a52);
  border-radius: 15px;
  color: white;
  animation: fadeIn 0.5s ease-in;
}

.result-box h2,
.error-box h2 {
  margin: 0 0 10px 0;
  font-size: 18px;
}

.error-box p {
  margin: 0;
  font-size: 14px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 반응형 */
@media (max-width: 600px) {
  .container {
    padding: 30px 20px;
  }

  h1 {
    font-size: 24px;
  }

  .fetch-btn {
    padding: 12px 25px;
    font-size: 14px;
  }

  .result-text {
    font-size: 20px;
  }
}
</style>
