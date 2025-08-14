<template>
  <div class="auth-page my-page">
    <CustomAlert ref="customAlert" @alert-closed="onAlertClose" />
    <div class="auth-wrapper my-page-wrapper">
      <!-- Left Section: User Profile -->
      <div class="auth-image-section my-page-profile-sidebar">
        <section v-if="authStore.user" class="profile-section">
          <img 
            :src="getValidProfileImageUrl(profile.profilePicUrl)" 
            alt="Profile Image"
            class="profile-pic"
            @error="handleImageError"
          >
          <div class="user-info">
            <h2 class="user-name">{{ authStore.user.name }}</h2>
            <p class="user-penname">@{{ profile.nickname }}</p>
          </div>
          <div class="author-message-box">
            <p class="author-message-content">{{ profile.bio || '한 줄 소개를 입력하여 작가님을 알려보세요.' }}</p>
          </div>
        </section>
      </div>

      <!-- Right Section: Edit Form -->
      <div class="auth-container my-page-container">
        <form @submit.prevent="updateProfileInfo" class="form-layout">
          
          <div class="form-section">
            <h2 class="section-title">프로필 사진</h2>
            <div class="profile-pic-section">
              <p class="form-text">새로운 프로필 사진을 선택하거나<br></br> 기본 이미지로 초기화하세요.</p>
              <div class="pic-actions">
                <input type="file" @change="onFileChange" ref="fileInput" hidden accept="image/*">
                <button type="button" @click="triggerFileUpload" class="btn btn-secondary">사진 변경</button>
                <button type="button" @click="resetProfilePic" class="btn btn-outline">기본값으로 변경</button>
              </div>
            </div>
          </div>

          <div class="form-section">
            <h2 class="section-title">프로필 정보</h2>
            <div class="form-group">
              <label for="nickname" class="form-label">닉네임 (필명) 수정</label>
              <input type="text" v-model="profile.nickname" class="form-control" id="nickname">
            </div>
            <div class="form-group">
              <label for="bio" class="form-label">한 줄 소개 (작가의 말) 수정</label>
              <textarea v-model="profile.bio" class="form-control" id="bio" rows="4"></textarea>
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary btn-auth" :disabled="isSaving">
              <span v-if="isSaving" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
              {{ isSaving ? '저장 중...' : '프로필 정보 저장' }}
            </button>
          </div>
        </form>
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
import CustomAlert from '@/components/common/CustomAlert.vue';

const authStore = useAuthStore();
const router = useRouter();

const customAlert = ref<InstanceType<typeof CustomAlert> | null>(null);
const wasSuccess = ref(false);

const profile = ref({
  nickname: '',
  bio: '',
  profilePicUrl: '',
});
const newProfilePic = ref<File | null>(null);

const fileInput = ref<HTMLInputElement | null>(null);

const isSaving = ref(false);

const getValidProfileImageUrl = (url: string | null | undefined): string => {
  if (!url || !url.trim() || url.includes('default.png') || url.includes('/userProfile/default.png')) {
    return '/images/profile.png';
  }
  return url;
};

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement;
  img.src = createDefaultAvatar();
};

const createDefaultAvatar = (): string => {
  const svg = `
    <svg width="150" height="150" viewBox="0 0 150 150" xmlns="http://www.w3.org/2000/svg">
      <circle cx="75" cy="75" r="75" fill="#e8e7dc"/>
      <circle cx="75" cy="60" r="20" fill="#ffffff"/>
      <ellipse cx="75" cy="110" rx="25" ry="20" fill="#ffffff"/>
    </svg>
  `;
  return `data:image/svg+xml;base64,${btoa(svg)}`;
};

function loadUserProfile() {
  if (authStore.user) {
    profile.value.nickname = authStore.user.nickname;
    profile.value.bio = authStore.user.intro || '';
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

function resetProfilePic() {
  newProfilePic.value = null;
  profile.value.profilePicUrl = '';
  if (fileInput.value) {
    fileInput.value.value = '';
  }
}

async function updateProfileInfo() {
  isSaving.value = true;
  wasSuccess.value = false;
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

    await authStore.fetchUserInfo();
    wasSuccess.value = true;
    showMessage('프로필 정보가 성공적으로 저장되었습니다.', 'success');

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

function showMessage(msg: string, type: 'success' | 'danger') {
  const title = type === 'success' ? '성공' : '오류';
  customAlert.value?.showAlert({ title: title, message: msg });
}

function onAlertClose() {
  if (wasSuccess.value) {
    router.push('/my-page');
  }
}

onMounted(() => {
  loadUserProfile();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Pretendard:wght@400;500;700&display=swap');
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

.auth-page {
  padding: 2rem;
  color: #333;
  min-height: 100vh;
  font-family: 'SCDream4', sans-serif;
  display: flex;
  align-items: flex-start; /* 위쪽으로 정렬 */
  justify-content: center;
}

.auth-wrapper {
  display: flex;
  width: 100%;
  max-width: 819px;
  min-height: 576px;
  background-color: #fff;
  border-radius: 13px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  border: 2px solid #6F7D48;
}

.my-page-profile-sidebar {
  flex: 0 0 256px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 2rem;
  background-color: #f4f3e8;
  border-right: 1px solid #e8e7dc;
}

.my-page-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 2rem 2.4rem;
}

/* --- 프로필 섹션 --- */
.profile-section {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.profile-pic {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  margin-bottom: 0.8rem;
}

.user-name {
  font-size: 1.44rem;
  font-weight: 700;
  margin-bottom: 0.2rem;
}

.user-penname {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 1.2rem;
}

.author-message-box {
  width: 100%;
  background-color: rgba(255, 255, 255, 0.5);
  padding: 0.8rem;
  border-radius: 6px;
  font-size: 0.7rem;
  line-height: 1.6;
  color: #555;
  white-space: pre-wrap;
  margin-bottom: 1.2rem;
}

/* --- 콘텐츠 섹션 (폼) --- */
.section-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 1.2rem;
  padding-bottom: 0.6rem;
  border-bottom: 1px solid #eee;
}

.form-layout {
  display: flex;
  flex-direction: column;
  gap: 1.6rem;
  width: 100%;
}

.form-section {
  padding-top: 1.6rem;
}

.form-section:first-of-type {
  padding-top: 0;
}

.profile-pic-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  width: 100%;
}

.form-text {
  font-size: 1rem;
  color: #6c757d;
  margin: 0;
}

.pic-actions {
  display: flex;
  flex-direction: row;
  gap: 0.6rem;
}

.pic-actions .btn {
  padding: 0.4rem 0.8rem;
  font-size: 0.8rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.form-label {
  font-weight: 600;
  font-size: 0.8rem;
  color: #555;
}

.form-control {
  background-color: #f8f9fa;
  border: 1px solid #dee2e6;
  padding: 0.6rem 0.8rem;
  border-radius: 6px;
  transition: background-color 0.2s, border-color 0.2s;
  font-family: inherit;
  font-size: 0.8rem;
  margin-bottom: 1rem;
}

.form-control:focus {
  background-color: #fff;
  border-color: #8A9A5B;
  box-shadow: 0 0 0 0.2rem rgba(138, 154, 91, 0.25);
  outline: none;
}

textarea.form-control {
  resize: vertical;
  min-height: 80px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 0rem;

  padding-top: 1.6rem;
}

.btn, .btn-primary, .btn-secondary, .btn-outline {
  padding: 0.6rem 1.2rem;
  font-size: 0.8rem;
  font-weight: 600;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.btn-primary {
  background-color: #6F7D48;
  border-color: #6F7D48;
  color: #fff;
}
.btn-primary:hover:not(:disabled) {
  background-color: #5a663a;
}

.btn-secondary {
  background-color: #fff;
  border-color: #ced4da;
  color: #495057;
}
.btn-secondary:hover {
  background-color: #f8f9fa;
}

.btn-outline {
  background-color: transparent;
  border-color: #ced4da;
  color: #495057;
}
.btn-outline:hover {
  background-color: #f8f9fa;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.alert {
  padding: 0.8rem;
  margin-top: 1.2rem;
  border-radius: 6px;
  border: 1px solid transparent;
  text-align: center;
  font-size: 0.8rem;
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

/* --- 반응형 --- */
@media (max-width: 992px) {
  .auth-wrapper {
    flex-direction: column;
    min-height: auto;
    max-width: 480px;
  }
  .my-page-profile-sidebar {
    border-right: none;
    border-bottom: 1px solid #e8e7dc;
  }
}

@media (max-width: 576px) {
  .auth-page {
    padding: 1rem;
  }
  .my-page-container, .my-page-profile-sidebar {
    padding: 1.2rem;
  }
  .profile-pic-section {
    flex-direction: column;
    align-items: flex-start;
    text-align: left;
  }
}
</style>
