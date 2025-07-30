<template>
  <div class="profile-edit-page">
    <div class="profile-container">
      <h1 class="page-title">내 정보 수정</h1>

      <!-- Profile Information Section -->
      <section class="profile-section">
        <h2>프로필 정보</h2>
        <form @submit.prevent="updateProfileInfo">
          <div class="mb-3">
            <label for="nickname" class="form-label">닉네임 (필명)</label>
            <input type="text" v-model="profile.nickname" class="form-control" id="nickname">
          </div>
          <div class="mb-3">
            <label for="bio" class="form-label">나를 소개하는 한마디 (작가의 말)</label>
            <textarea v-model="profile.bio" class="form-control" id="bio" rows="4"></textarea>
          </div>
          <!-- Profile picture upload can be added here -->
          <button type="submit" class="btn btn-primary" :disabled="isSaving">{{ isSaving ? '저장 중...' : '프로필 저장' }}</button>
        </form>
      </section>

      <!-- Change Password Section -->
      <section class="password-section">
        <h2>비밀번호 변경</h2>
        <form @submit.prevent="changePassword">
          <div class="mb-3">
            <label for="currentPassword" class="form-label">현재 비밀번호</label>
            <input type="password" v-model="password.current" class="form-control" id="currentPassword">
          </div>
          <div class="mb-3">
            <label for="newPassword" class="form-label">새 비밀번호</label>
            <input type="password" v-model="password.new" class="form-control" id="newPassword">
          </div>
          <div class="mb-3">
            <label for="confirmPassword" class="form-label">새 비밀번호 확인</label>
            <input type="password" v-model="password.confirm" class="form-control" id="confirmPassword">
          </div>
          <button type="submit" class="btn btn-secondary" :disabled="isChangingPassword">{{ isChangingPassword ? '변경 중...' : '비밀번호 변경' }}</button>
        </form>
      </section>

      <div v-if="message" class="alert mt-4" :class="messageType === 'success' ? 'alert-success' : 'alert-danger'">{{ message }}</div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

// --- Dummy Data ---
const DUMMY_USER_PROFILE = {
  nickname: '테스트유저',
  bio: '안녕하세요! MyBook에서 저의 이야기를 만들어가고 있습니다.',
};

// --- Reactive State ---
const profile = ref({
  nickname: '',
  bio: '',
});
const password = ref({
  current: '',
  new: '',
  confirm: '',
});
const message = ref('');
const messageType = ref('success'); // 'success' or 'error'
const isSaving = ref(false);
const isChangingPassword = ref(false);

// --- Functions ---
function loadUserProfile() {
  profile.value.nickname = DUMMY_USER_PROFILE.nickname;
  profile.value.bio = DUMMY_USER_PROFILE.bio;
}

async function updateProfileInfo() {
  isSaving.value = true;
  message.value = '';
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // In a real app, you would update the backend here
    DUMMY_USER_PROFILE.nickname = profile.value.nickname;
    DUMMY_USER_PROFILE.bio = profile.value.bio;

    showMessage('프로필 정보가 성공적으로 저장되었습니다. (더미)', 'success');
  } catch (error) {
    console.error("Error updating profile:", error);
    showMessage('프로필 저장에 실패했습니다. (더미)', 'error');
  } finally {
    isSaving.value = false;
  }
}

async function changePassword() {
  if (password.value.new !== password.value.confirm) {
    showMessage('새 비밀번호가 일치하지 않습니다.', 'error');
    return;
  }
  if (password.value.new.length < 6) {
    showMessage('새 비밀번호는 6자리 이상이어야 합니다.', 'error');
    return;
  }

  isChangingPassword.value = true;
  message.value = '';

  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000));

    if (password.value.current === '123456') { // Dummy current password
      showMessage('비밀번호가 성공적으로 변경되었습니다. (더미)', 'success');
      password.value = { current: '', new: '', confirm: '' }; // Reset form
    } else {
      showMessage('현재 비밀번호가 올바르지 않습니다. (더미)', 'error');
    }
  } catch (error) {
    console.error("Error changing password:", error);
    showMessage('비밀번호 변경에 실패했습니다. (더미)', 'error');
  } finally {
    isChangingPassword.value = false;
  }
}

function showMessage(msg: string, type: 'success' | 'error') {
  message.value = msg;
  messageType.value = type;
  setTimeout(() => message.value = '', 5000);
}

// --- Lifecycle Hooks ---
onMounted(() => {
  loadUserProfile();
});
</script>

<style scoped>
.profile-edit-page {
  padding: 80px 2rem 2rem;
  background-color: #F5F5DC;
  color: #3D2C20;
  min-height: calc(100vh - 56px);
  display: flex;
  justify-content: center;
}

.profile-container {
  width: 100%;
  max-width: 700px;
}

.page-title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 2rem;
}

.profile-section, .password-section {
  background: #fff;
  padding: 2rem;
  border-radius: 12px;
  margin-bottom: 2rem;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
}

h2 {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #EAE0D5;
}

.form-label {
  font-weight: 600;
}

.form-control {
  background-color: #F5F5DC;
  border-color: #B8860B;
}

.btn {
  font-weight: 600;
}

.btn-primary {
  background-color: #B8860B;
  border-color: #B8860B;
}

.btn-secondary {
    background-color: #5C4033;
    border-color: #5C4033;
}
</style>
