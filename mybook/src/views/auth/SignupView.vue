<template>
  <div class="auth-page">
    <div class="auth-container">
      <h1 class="auth-title">회원가입</h1>
      <p class="auth-subtitle">당신의 이야기, 지금 바로 시작해보세요.</p>
      
      <form @submit.prevent="handleSignup">
        <div class="row">
          <div class="col-md-6 mb-3">
            <label for="name" class="form-label">이름</label>
            <input type="text" v-model="form.name" class="form-control" id="name" required>
          </div>
          <div class="col-md-6 mb-3">
            <label for="nickname" class="form-label">닉네임 (필명)</label>
            <input type="text" v-model="form.nickname" class="form-control" id="nickname" required>
          </div>
        </div>

        <div class="mb-3">
          <label for="email" class="form-label">이메일 (ID)</label>
          <input type="email" v-model="form.email" class="form-control" id="email" required>
        </div>

        <div class="mb-3">
          <label for="password" class="form-label">비밀번호</label>
          <input type="password" v-model="form.password" class="form-control" id="password" required>
        </div>

        <div class="mb-3">
          <label for="confirmPassword" class="form-label">비밀번호 확인</label>
          <input type="password" v-model="form.confirmPassword" class="form-control" id="confirmPassword" required>
        </div>
        
        <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
        
        <button type="submit" class="btn btn-primary w-100 btn-auth" :disabled="isLoading">
          <span v-if="isLoading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
          {{ isLoading ? '가입하는 중...' : '회원가입' }}
        </button>
      </form>

      <div class="auth-links">
        <span>이미 계정이 있으신가요?</span>
        <router-link to="/login">로그인</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter, RouterLink } from 'vue-router';

// --- Router ---
const router = useRouter();

// --- Reactive State ---
const form = ref({
  name: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: '',
});
const errorMessage = ref('');
const isLoading = ref(false);

// --- Functions ---
async function handleSignup() {
  // Validation
  if (Object.values(form.value).some(v => !v)) {
    errorMessage.value = '모든 필드를 입력해주세요.';
    return;
  }
  if (form.value.password !== form.value.confirmPassword) {
    errorMessage.value = '비밀번호가 일치하지 않습니다.';
    return;
  }
  if (form.value.password.length < 6) {
    errorMessage.value = '비밀번호는 6자리 이상이어야 합니다.';
    return;
  }

  isLoading.value = true;
  errorMessage.value = '';

  // Simulate API call
  await new Promise(resolve => setTimeout(resolve, 1500)); 

  // Simulate success or failure based on email for testing
  if (form.value.email === 'test@example.com') {
    errorMessage.value = '이미 사용 중인 이메일입니다. (더미)';
  } else if (form.value.email === 'fail@example.com') {
    errorMessage.value = '회원가입에 실패했습니다. (더미)';
  } else {
    alert('회원가입 성공! (더미)');
    router.push('/login'); // Simulate successful signup
  }
  isLoading.value = false;
}

</script>

<style scoped>
/* Using the same styles as LoginView for consistency */
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #F5F5DC;
  padding: 2rem;
}

.auth-container {
  width: 100%;
  max-width: 500px; /* Slightly wider for signup form */
  background-color: #FFFFFF;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.1);
}

.auth-title {
  font-size: 2.2rem;
  font-weight: 700;
  text-align: center;
  color: #3D2C20;
  margin-bottom: 0.5rem;
}

.auth-subtitle {
  text-align: center;
  color: #5C4033;
  margin-bottom: 2rem;
}

.form-label {
  font-weight: 600;
  color: #3D2C20;
}

.form-control {
  background-color: #F5F5DC;
  border: 1px solid #B8860B;
  padding: 0.75rem 1rem;
}
.form-control:focus {
    background-color: #fff;
    box-shadow: 0 0 0 0.25rem rgba(184, 134, 11, 0.25);
}

.btn-auth {
  padding: 0.75rem;
  font-size: 1rem;
  font-weight: 600;
  background-color: #B8860B;
  border-color: #B8860B;
  transition: background-color 0.3s;
  margin-top: 1rem;
}

.btn-auth:hover {
  background-color: #DAA520;
}

.auth-links {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 1.5rem;
  font-size: 0.9rem;
  color: #5C4033;
}

.auth-links a {
  color: #8B4513;
  text-decoration: none;
  font-weight: 600;
}
</style>
