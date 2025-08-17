<template>
  <div class="auth-page my-page">
    <CustomAlert ref="customAlert" @alert-closed="onAlertClose" />
    <BaseModal
      :is-visible="isDeleteModalVisible"
      title="회원 탈퇴"
      @close="closeDeleteModal"
      :close-on-overlay="true"
    >
      <div class="modal-body-content">
        <p>정말로 회원 탈퇴를 진행하시겠습니까? 이 작업은 되돌릴 수 없습니다.</p>
      </div>
      <template #footer>
        <button @click="closeDeleteModal" class="btn btn-secondary">취소</button>
        <button @click="handleDeleteConfirm" class="btn btn-danger">탈퇴</button>
      </template>
    </BaseModal>

    <div class="auth-wrapper my-page-wrapper">
      <!-- Left Section: User Profile -->
      <div class="auth-image-section my-page-profile-sidebar">
        <section v-if="user" class="profile-section">
          <img
            :src="getValidProfileImageUrl(user.profileImageUrl)"
            alt="Profile Image"
            class="profile-pic"
            @error="handleImageError"
          >
          <div class="user-info">
            <h2 class="user-name">{{ user.name }}</h2>
            <p class="user-penname">@{{ user.nickname }}</p>
          </div>
          <div v-if="user.intro" class="author-message-box">
            <p class="author-message-content">{{ user.intro }}</p>
          </div>
          <button @click="goToProfileEdit" class="btn btn-secondary w-100 edit-profile-btn">
            <i class="bi bi-pencil-square"></i> 프로필 수정
          </button>
        </section>
        <section v-else class="profile-section">
          <p>사용자 정보를 불러오는 중입니다...</p>
        </section>
      </div>

      <!-- Right Section: User Activity -->
      <div class="auth-container my-page-container">
        <section class="content-section">
          <h3 class="section-title">북마크한 책</h3>
          <div v-if="paginatedBookmarkedBooks.length > 0" class="book-list-grid">
            <router-link v-for="book in paginatedBookmarkedBooks" :key="book.communityBookId" :to="`/bookstore/book/${book.communityBookId}`" class="book-cover-link">
              <div class="book-cover-image"
                :style="{ backgroundImage: `url(${book.coverImageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
                <div class="list-title-box">
                  <p class="list-title-box-title">{{ book.title }}</p>
                  <p class="list-title-box-author">{{ book.authorNickname }}</p>
                </div>
              </div>
            </router-link>
          </div>
          <p v-else class="no-content-message">아직 북마크한 책이 없습니다.</p>
          <div v-if="totalBookmarkedBooksPages > 1" class="pagination-controls">
            <button @click="prevBookmarkedBookPage" :disabled="bookmarkedBooksCurrentPage === 1" class="pagination-btn"><</button>
            <span v-for="page in totalBookmarkedBooksPages" :key="page" @click="goToBookmarkedBookPage(page)"
              :class="['page-number', { active: bookmarkedBooksCurrentPage === page }]">
              {{ page }}
            </span>
            <button @click="nextBookmarkedBookPage" :disabled="bookmarkedBooksCurrentPage === totalBookmarkedBooksPages" class="pagination-btn">></button>
          </div>
        </section>

        <hr class="divider">

        <section class="content-section">
          <h3 class="section-title">내가 쓴 댓글</h3>
          <div v-if="paginatedComments.length > 0" class="comment-list-container">
            <div v-for="comment in paginatedComments" :key="comment.id" class="comment-item">
              <p class="comment-text">"{{ comment.text }}"</p>
              <p class="comment-meta">
                <router-link :to="`/book-detail/${comment.bookId}`" class="comment-book-link">{{ getBookTitle(comment.bookId) }}</router-link>
                <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
              </p>
            </div>
          </div>
          <p v-else class="no-content-message">아직 작성한 댓글이 없습니다.</p>
          <div v-if="totalPages > 1" class="pagination-controls">
            <button @click="prevPage" :disabled="currentPage === 1" class="pagination-btn">&lt;</button>
            <span v-for="page in totalPages" :key="page" @click="goToPage(page)"
              :class="['page-number', { active: currentPage === page }]">
              {{ page }}
            </span>
            <button @click="nextPage" :disabled="currentPage === totalPages" class="pagination-btn">&gt;</button>
          </div>
        </section>

        <div class="account-actions">
          <button @click="openDeleteModal" class="btn-auth-danger">회원 탈퇴</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import apiClient from '@/api';
import { communityService, type CommunityBook } from '@/services/communityService';
import BaseModal from '@/components/common/BaseModal.vue';
import CustomAlert from '@/components/common/CustomAlert.vue';

interface Book {
  id: string;
  title: string;
  authorId: string;
  authorName?: string;
  coverUrl?: string;
}

interface Comment {
  id: string;
  bookId: string;
  episodeIndex: number;
  authorId: string;
  authorName?: string;
  text: string;
  createdAt: Date;
}

const router = useRouter();
const authStore = useAuthStore();

const user = computed(() => authStore.user);

const isDeleteModalVisible = ref(false);
const customAlert = ref<InstanceType<typeof CustomAlert> | null>(null);
const isDeletionSuccess = ref(false);

// --- 북마크한 책과 댓글 상태 ---
const bookmarkedBooks = ref<CommunityBook[]>([]);
const myComments = ref<Comment[]>([]);
const allBooks = ref<Book[]>([]); // getBookTitle 헬퍼 함수용

// 북마크한 책 페이지네이션
const bookmarkedBooksCurrentPage = ref(1);
const bookmarkedBooksPerPage = 4; // 한 줄에 4개씩 표시
const paginatedBookmarkedBooks = computed(() => {
  const start = (bookmarkedBooksCurrentPage.value - 1) * bookmarkedBooksPerPage;
  const end = start + bookmarkedBooksPerPage;
  return bookmarkedBooks.value.slice(start, end);
});
const totalBookmarkedBooksPages = computed(() => Math.ceil(bookmarkedBooks.value.length / bookmarkedBooksPerPage));
const goToBookmarkedBookPage = (page: number) => { bookmarkedBooksCurrentPage.value = page; };
const prevBookmarkedBookPage = () => { if (bookmarkedBooksCurrentPage.value > 1) bookmarkedBooksCurrentPage.value--; };
const nextBookmarkedBookPage = () => { if (bookmarkedBooksCurrentPage.value < totalBookmarkedBooksPages.value) bookmarkedBooksCurrentPage.value++; };

// 댓글 페이지네이션
const currentPage = ref(1);
const commentsPerPage = 4;
const paginatedComments = computed(() => {
  const start = (currentPage.value - 1) * commentsPerPage;
  const end = start + commentsPerPage;
  return myComments.value.slice(start, end);
});
const totalPages = computed(() => Math.ceil(myComments.value.length / commentsPerPage));
const goToPage = (page: number) => { currentPage.value = page; };
const prevPage = () => { if (currentPage.value > 1) currentPage.value--; };
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++; };

// 유효한 프로필 이미지 URL 반환 (AWS default.png 제외)
const getValidProfileImageUrl = (url: string | null | undefined): string => {
  if (!url || !url.trim() || url.includes('default.png') || url.includes('/userProfile/default.png')) {
    return '/images/profile.png';
  }
  return url;
};

// 이미지 로드 실패 시 SVG 아바타로 대체
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement;
  img.src = createDefaultAvatar();
};

// SVG 기본 아바타 생성
const createDefaultAvatar = (): string => {
  const svg = `
    <svg width="120" height="120" viewBox="0 0 120 120" xmlns="http://www.w3.org/2000/svg">
      <circle cx="60" cy="60" r="60" fill="#e8e7dc"/>
      <circle cx="60" cy="50" r="18" fill="#ffffff"/>
      <ellipse cx="60" cy="95" rx="30" ry="22" fill="#ffffff"/>
    </svg>
  `;
  return `data:image/svg+xml;base64,${btoa(svg)}`;
};

// 헬퍼 함수
const getBookTitle = (bookId: string) => {
  const book = allBooks.value.find(b => b.id === bookId);
  return book ? book.title : '알 수 없는 책';
};
const formatDate = (date: Date) => new Intl.DateTimeFormat('ko-KR').format(date);

const goToProfileEdit = () => {
  router.push('/profile-edit');
};

const openDeleteModal = () => {
  isDeleteModalVisible.value = true;
};

const closeDeleteModal = () => {
  isDeleteModalVisible.value = false;
};

const handleDeleteConfirm = async () => {
  closeDeleteModal();
  try {
    await apiClient.delete('/api/v1/members/me');
    isDeletionSuccess.value = true;
    customAlert.value?.showAlert({ title: '성공', message: '회원 탈퇴가 완료되었습니다. 이용해주셔서 감사합니다.' });
  } catch (error) {
    console.error('Failed to delete account:', error);
    isDeletionSuccess.value = false;
    customAlert.value?.showAlert({ title: '오류', message: '회원 탈퇴 중 오류가 발생했습니다.' });
  }
};

const onAlertClose = async () => {
  if (isDeletionSuccess.value) {
    await authStore.logout();
    router.push('/');
  }
};

onMounted(async () => {
  if (!authStore.user) {
    await authStore.fetchUserInfo();
  }
  try {
    const response = await communityService.getBookmarkedCommunityBooks({ page: 0, size: 100 });
    bookmarkedBooks.value = response.content;
  } catch (error) {
    console.error('북마크한 책 목록을 불러오는데 실패했습니다:', error);
  }
  // TODO: 댓글 API 연동
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

.edit-profile-btn {
  width: 100%;
  padding: 0.6rem;
  font-size: 0.75rem;
  font-weight: 600;
  background-color: #fff;
  color: #6F7D48;
  border: 1px solid #e8e7dc;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.edit-profile-btn:hover {
  background-color: #6F7D48;
  color: #fff;
  border-color: #6F7D48;
}

/* --- 콘텐츠 섹션 --- */
.content-section {
  width: 100%;
  padding: 1.2rem 0;
}

.section-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 1.2rem;
  padding-bottom: 0.6rem;
  border-bottom: 1px solid #eee;
}

/* --- 책 리스트 --- */
.book-list-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(96px, 1fr));
  gap: 1.2rem;
}

.book-cover-link {
  text-decoration: none;
}

.book-cover-image {
  width: 96px;
  height: 144px;
  object-fit: cover;
  border-radius: 3px;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-size: cover;
  background-position: center;
  position: relative;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.book-cover-image:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.list-title-box {
  width: 80%;
  height: 70%;
  background-color: rgba(255, 255, 255, 0.95);
  padding: 8px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  text-align: left;
  color: #333;
  border-radius: 2px;
  opacity: 1;
  transition: opacity 0.3s ease;
}

.list-title-box-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 10px;
  font-weight: 600;
  line-height: 1.4;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.list-title-box-author {
  font-size: 8px;
  font-weight: 500;
  margin: 0;
  color: #555;
}

/* --- 댓글 리스트 --- */
.comment-list-container {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.comment-item {
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.comment-text {
  font-size: 0.75rem;
  line-height: 1.6;
  color: #333;
  margin-bottom: 0.6rem;
}

.comment-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.7rem;
  color: #868e96;
}

.comment-book-link {
  color: #6F7D48;
  text-decoration: none;
  font-weight: 600;
}

.comment-book-link:hover {
  text-decoration: underline;
}

/* --- 기타 --- */
.account-actions {
  margin-top: auto; /* 푸터처럼 하단에 위치 */
  padding-top: 1.2rem;
  text-align: right;
}

.btn-auth-danger {
  border: none;
  border-radius: 6px;
  padding: 0.5rem 1rem;
  font-size: 0.7rem;
  background: transparent;
  color: #c92a2a;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-auth-danger:hover {
  background-color: #c92a2a;
  color: white;
}

.no-content-message {
  text-align: center;
  color: #868e96;
  font-size: 0.8rem;
  padding: 2.4rem 0;
}

.divider {
  border: 0;
  border-top: 1px solid #e9ecef;
  margin: 1.2rem 0;
}

.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.4rem;
  margin-top: 1.6rem;
}

.pagination-btn, .page-number {
  border: 1px solid #dee2e6;
  background-color: #fff;
  color: #555;
  border-radius: 50%;
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.7rem;
}

.pagination-btn:hover:not(:disabled), .page-number:hover {
  border-color: #6F7D48;
  color: #6F7D48;
}

.page-number.active {
  background-color: #6F7D48;
  color: white;
  border-color: #6F7D48;
}

.modal-body-content {
  padding: 1.5rem;
  font-size: 1rem;
  color: #333;
  line-height: 1.6;
}

.modal-footer .btn {
  padding: 0.5rem 1rem;
  border-radius: 6px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.modal-footer .btn-secondary {
  background-color: #e9ecef;
  color: #495057;
}

.modal-footer .btn-secondary:hover {
  background-color: #dee2e6;
}

.modal-footer .btn-danger {
  background-color: #c92a2a;
  color: white;
}

.modal-footer .btn-danger:hover {
  background-color: #a52222;
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
  .my-page-container {
    padding: 1.2rem;
  }
  .book-list-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.8rem;
  }
}
</style>
