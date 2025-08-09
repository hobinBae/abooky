<template>
  <div class="profile-edit-page">
    <div class="profile-container">
      <h1 class="page-title">내 정보 수정</h1>

      <section class="form-section">
        <h2 class="section-title">프로필 정보</h2>
        <form @submit.prevent="updateProfileInfo" class="form-layout">
          <div class="profile-pic-section">
            <img :src="profile.profilePicUrl || 'https://via.placeholder.com/150'" alt="Profile Picture" class="profile-pic">
            <div class="pic-actions">
              <input type="file" @change="onFileChange" ref="fileInput" hidden accept="image/*">
              <button type="button" @click="triggerFileUpload" class="btn btn-secondary">사진 변경</button>
              <p class="form-text">새로운 프로필 사진을 선택하세요.</p>
            </div>
          </div>

          <div class="form-group">
            <label for="nickname" class="form-label">닉네임 (필명)</label>
            <input type="text" v-model="profile.nickname" class="form-control" id="nickname">
          </div>
          <div class="form-group">
            <label for="bio" class="form-label">한 줄 소개 (작가의 말)</label>
            <textarea v-model="profile.bio" class="form-control" id="bio" rows="4"></textarea>
          </div>
          <button type="submit" class="btn btn-primary" :disabled="isSaving">
            {{ isSaving ? '저장 중...' : '프로필 정보 저장' }}
          </button>
        </form>
      </section>

      <hr class="divider">

      <!-- <section class="form-section">
        <h2 class="section-title">비밀번호 변경</h2>
        <form @submit.prevent="changePassword" class="form-layout">
          <div class="form-group">
            <label for="currentPassword" class="form-label">현재 비밀번호</label>
            <input type="password" v-model="password.current" class="form-control" id="currentPassword">
          </div>
          <div class="form-group">
            <label for="newPassword" class="form-label">새 비밀번호</label>
            <input type="password" v-model="password.new" class="form-control" id="newPassword">
          </div>
          <div class="form-group">
            <label for="confirmPassword" class="form-label">새 비밀번호 확인</label>
            <input type="password" v-model="password.confirm" class="form-control" id="confirmPassword">
          </div>
          <button type="submit" class="btn btn-primary" :disabled="isChangingPassword">
            {{ isChangingPassword ? '변경 중...' : '비밀번호 변경' }}
          </button>
        </form>
      </section> -->

      <div v-if="message" class="alert" :class="`alert-${messageType}`">
        {{ message }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import apiClient from '@/api';
import { useRouter } from 'vue-router';
import { AxiosError } from 'axios';

const authStore = useAuthStore();
const router = useRouter();

// --- Reactive State ---
const profile = ref({
  nickname: '',
  bio: '',
  profilePicUrl: '',
});
const newProfilePic = ref<File | null>(null);
const password = ref({
  current: '',
  new: '',
  confirm: '',
});

const fileInput = ref<HTMLInputElement | null>(null);

const message = ref('');
const messageType = ref<'success' | 'danger'>('success');
const isSaving = ref(false);
const isChangingPassword = ref(false);

// --- Functions ---
function loadUserProfile() {
  if (authStore.user) {
    profile.value.nickname = authStore.user.nickname;
    profile.value.bio = authStore.user.intro || ''; // intro 필드가 없을 경우 대비
    profile.value.profilePicUrl = authStore.user.profileImageUrl || '';
  }
}

function triggerFileUpload() {
  fileInput.value?.click();
}

function onFileChange(event: Event) {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files[0]) {
    const file = target.files[0];
    newProfilePic.value = file;
    profile.value.profilePicUrl = URL.createObjectURL(file);
  }
}

async function updateProfileInfo() {
  isSaving.value = true;
  message.value = '';
  try {
    const formData = new FormData();
    formData.append('nickname', profile.value.nickname);
    formData.append('intro', profile.value.bio);
    if (newProfilePic.value) {
      formData.append('file', newProfilePic.value);
    }

    await apiClient.patch('/api/v1/members/me', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    await authStore.fetchUserInfo(); // 스토어 정보 갱신
    showMessage('프로필 정보가 성공적으로 저장되었습니다.', 'success');
    router.push('/mypage'); // 마이페이지로 이동

  } catch (error) {
    console.error("Error updating profile:", error);
    let errorMessage = '프로필 저장에 실패했습니다.';
    if (error instanceof AxiosError && error.response) {
      errorMessage = error.response.data.message || errorMessage;
    }
    showMessage(errorMessage, 'danger');
  } finally {
    isSaving.value = false;
  }
}

async function changePassword() {
  if (password.value.new !== password.value.confirm) {
    showMessage('새 비밀번호가 일치하지 않습니다.', 'danger');
    return;
  }
  if (password.value.new.length < 8) {
    showMessage('새 비밀번호는 8자리 이상이어야 합니다.', 'danger');
    return;
  }

  isChangingPassword.value = true;
  message.value = '';

  try {
    await new Promise(resolve => setTimeout(resolve, 1000));
    if (password.value.current === '123456') {
      showMessage('비밀번호가 성공적으로 변경되었습니다.', 'success');
      password.value = { current: '', new: '', confirm: '' };
    } else {
      showMessage('현재 비밀번호가 올바르지 않습니다.', 'danger');
    }
  } catch (error) {
    console.error("Error changing password:", error);
    showMessage('비밀번호 변경에 실패했습니다.', 'danger');
  } finally {
    isChangingPassword.value = false;
  }
}

function showMessage(msg: string, type: 'success' | 'danger') {
  message.value = msg;
  messageType.value = type;
  setTimeout(() => message.value = '', 5000);
}

onMounted(() => {
  loadUserProfile();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&family=Pretendard:wght@400;500;700&display=swap');

/* --- 기본 페이지 스타일 --- */
.profile-edit-page {
  padding: 4rem 2rem;
  background-color: #fff; /* 흰색 배경 */
  color: #333;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  font-family: 'Pretendard', sans-serif;
}

.profile-container {
  width: 100%;
  max-width: 700px;
}

.page-title {
  text-align: center;
  font-family: 'Noto Serif KR', serif;
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 3rem;
}

/* --- 폼 섹션 스타일 --- */
.form-section {
  padding: 1rem 0;
}

.section-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 2.5rem;
}

.form-layout {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* --- 프로필 사진 섹션 --- */
.profile-pic-section {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  background-color: #f8f9fa;
  padding: 1.5rem;
  border-radius: 8px;
}

.profile-pic {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #dee2e6;
}

.pic-actions .btn {
  margin-bottom: 0.5rem;
}

.form-text {
  font-size: 0.875rem;
  color: #6c757d;
}

/* --- 폼 요소 스타일 --- */
.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.form-label {
  font-weight: 600;
  font-size: 1rem;
}

.form-control {
  width: 100%;
  padding: 0.8rem 1rem;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.2s ease;
}

.form-control:focus {
  outline: none;
  border-color: #868e96;
}

textarea.form-control {
  resize: vertical;
  min-height: 120px;
}

/* --- 버튼 스타일 --- */
.btn {
  padding: 0.8rem 1.25rem;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #333;
  transition: all 0.2s ease;
  width: fit-content;
  align-self: flex-end; /* 버튼을 오른쪽으로 정렬 */
}

.btn-primary {
  color: #fff;
  background-color: #333;
}
.btn-primary:hover:not(:disabled) {
  background-color: #000;
  border-color: #000;
}
.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  color: #333;
  background-color: #fff;
  border-color: #ccc;
}
.btn-secondary:hover {
  background-color: #f8f9fa;
}

/* --- 구분선 --- */
.divider {
  border: 0;
  border-top: 1px solid #f1f3f5;
  margin: 3rem 0;
}

/* --- 알림창 스타일 --- */
.alert {
  padding: 1rem 1.25rem;
  margin-top: 2rem;
  border-radius: 4px;
  border: 1px solid transparent;
}
.alert-success {
  color: #155724;
  background-color: #d4edda;
  border-color: #c3e6cb;
}
.alert-danger {
  color: #721c24;
  background-color: #f8d7da;
  border-color: #f5c6cb;
}
</style>
