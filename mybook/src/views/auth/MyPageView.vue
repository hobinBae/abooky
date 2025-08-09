<template>
  <div class="my-page-container">
    <div class="top-actions">
      <button @click="goToProfileEdit" class="btn-top-action" title="프로필 수정">
        <i class="bi bi-gear-fill"></i>
      </button>
      <button @click="handleLogout" class="btn-top-action" title="로그아웃">
        <i class="bi bi-box-arrow-right"></i>
      </button>
    </div>

    <section v-if="user" class="profile-section">
      <div class="profile-main-content">
        <div class="profile-left-section">
          <img :src="user.profileImageUrl || 'https://via.placeholder.com/150'" alt="Profile Picture"
            class="profile-pic">
          <div class="user-info">
            <h2 class="user-name">{{ user.name }}</h2>
            <p class="user-penname">@{{ user.nickname }}</p>
          </div>
        </div>
        <div v-if="user.intro" class="author-message-box">
          <p class="author-message-title">작가의 말</p>
          <p class="author-message-content">{{ user.intro }}</p>
        </div>
      </div>
    </section>
    <section v-else class="profile-section">
      <p>사용자 정보를 불러오는 중입니다...</p>
    </section>

    <hr class="divider">

    <section class="content-section">
      <h3 class="section-title">좋아요 누른 책</h3>
      <div v-if="paginatedLikedBooks.length > 0" class="book-list-grid">
        <router-link v-for="book in paginatedLikedBooks" :key="book.id" :to="`/book-detail/${book.id}`"
          class="book-item-card"
          :style="{ backgroundImage: `url(${book.coverUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
          <div class="list-title-box">
            <p class="list-title-box-title">{{ book.title }}</p>
            <p class="list-title-box-author">{{ book.authorName }}</p>
          </div>
        </router-link>
      </div>
      <p v-else class="no-content-message">아직 좋아요를 누른 책이 없습니다.</p>

      <div v-if="totalLikedBooksPages > 1" class="pagination-controls">
        <button @click="prevLikedBookPage" :disabled="likedBooksCurrentPage === 1" class="pagination-btn">
          이전
        </button>
        <span v-for="page in totalLikedBooksPages" :key="page" @click="goToLikedBookPage(page)"
          :class="['page-number', { active: likedBooksCurrentPage === page }]">
          {{ page }}
        </span>
        <button @click="nextLikedBookPage" :disabled="likedBooksCurrentPage === totalLikedBooksPages"
          class="pagination-btn">
          다음
        </button>
      </div>
    </section>

    <hr class="divider">

    <section class="content-section">
      <h3 class="section-title">내가 쓴 댓글</h3>
      <div v-if="paginatedComments.length > 0" class="comment-list-container">
        <div v-for="comment in paginatedComments" :key="comment.id" class="comment-item">
          <p class="comment-text">{{ comment.text }}</p>
          <p class="comment-meta">
            <router-link :to="`/book-detail/${comment.bookId}`" class="comment-book-link">{{
              getBookTitle(comment.bookId) }}</router-link>
            <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
          </p>
        </div>
      </div>
      <p v-else class="no-content-message">아직 작성한 댓글이 없습니다.</p>

      <div v-if="totalPages > 1" class="pagination-controls">
        <button @click="prevPage" :disabled="currentPage === 1" class="pagination-btn">
          이전
        </button>
        <span v-for="page in totalPages" :key="page" @click="goToPage(page)"
          :class="['page-number', { active: currentPage === page }]">
          {{ page }}
        </span>
        <button @click="nextPage" :disabled="currentPage === totalPages" class="pagination-btn">
          다음
        </button>
      </div>
    </section>

    <hr class="divider">

    <section class="danger-zone">
      <h3 class="section-title danger-title">계정 관리</h3>
      <div class="danger-content">
        <p>계정을 삭제하면 모든 데이터가 영구적으로 사라지며 복구할 수 없습니다.</p>
        <button @click="deleteAccount" class="btn btn-danger">회원 탈퇴</button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import apiClient from '@/api';

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

// --- 좋아요 누른 책과 댓글 상태 (API 연동 보류) ---
const likedBooks = ref<Book[]>([]);
const myComments = ref<Comment[]>([]);
const allBooks = ref<Book[]>([]); // getBookTitle 헬퍼 함수용

// 좋아요 누른 책 페이지네이션
const likedBooksCurrentPage = ref(1);
const likedBooksPerPage = 5;
const paginatedLikedBooks = computed(() => {
  const start = (likedBooksCurrentPage.value - 1) * likedBooksPerPage;
  const end = start + likedBooksPerPage;
  return likedBooks.value.slice(start, end);
});
const totalLikedBooksPages = computed(() => Math.ceil(likedBooks.value.length / likedBooksPerPage));
const goToLikedBookPage = (page: number) => { likedBooksCurrentPage.value = page; };
const prevLikedBookPage = () => { if (likedBooksCurrentPage.value > 1) likedBooksCurrentPage.value--; };
const nextLikedBookPage = () => { if (likedBooksCurrentPage.value < totalLikedBooksPages.value) likedBooksCurrentPage.value++; };

// 댓글 페이지네이션
const currentPage = ref(1);
const commentsPerPage = 5;
const paginatedComments = computed(() => {
  const start = (currentPage.value - 1) * commentsPerPage;
  const end = start + commentsPerPage;
  return myComments.value.slice(start, end);
});
const totalPages = computed(() => Math.ceil(myComments.value.length / commentsPerPage));
const goToPage = (page: number) => { currentPage.value = page; };
const prevPage = () => { if (currentPage.value > 1) currentPage.value--; };
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++; };

// 헬퍼 함수
const getBookTitle = (bookId: string) => {
  const book = allBooks.value.find(b => b.id === bookId);
  return book ? book.title : '알 수 없는 책';
};
const formatDate = (date: Date) => new Intl.DateTimeFormat('ko-KR').format(date);

const goToProfileEdit = () => {
  router.push('/profile-edit');
};

const handleLogout = async () => {
  if (confirm('로그아웃 하시겠습니까?')) {
    await authStore.logout();
  }
};

const deleteAccount = async () => {
  if (confirm('정말로 회원 탈퇴를 진행하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
    try {
      await apiClient.delete('/api/v1/members/me');
      alert('회원 탈퇴가 완료되었습니다. 이용해주셔서 감사합니다.');
      await authStore.logout();
    } catch (error) {
      console.error('Failed to delete account:', error);
      alert('회원 탈퇴 중 오류가 발생했습니다.');
    }
  }
};

onMounted(() => {
  if (!authStore.user) {
    authStore.fetchUserInfo();
  }
  // TODO: API가 준비되면 더미 데이터 대신 실제 데이터를 호출합니다.
  // fetchLikedBooks();
  // fetchMyComments();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&family=Pretendard:wght@400;500;700&display=swap');
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

/* --- 기본 페이지 스타일 --- */
.my-page-container {
  padding: 4rem 2rem;
  max-width: 900px;
  margin: 0 auto;
  font-family: 'Pretendard', sans-serif;
  color: #333;
  background-color: #fff;
  position: relative;
}

.top-actions {
  position: absolute;
  top: 1.5rem;
  right: 1.5rem;
  display: flex;
  gap: 0.5rem;
  z-index: 10;
}

.btn-top-action {
  background: none;
  border: none;
  color: #868e96;
  font-size: 1.5rem;
  cursor: pointer;
  transition: color 0.2s ease;
  padding: 0.5rem;
}
.btn-top-action:hover {
  color: #333;
}

/* --- 프로필 섹션 --- */
.profile-section {
  padding: 1rem 0;
  margin-bottom: 1rem;
}

.profile-main-content {
  display: flex;
  align-items: flex-start;
  gap: 2.5rem;
}

.profile-left-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  flex-shrink: 0;
}

.profile-pic {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #525252;
}

.user-info {
  text-align: center;
}

.user-name {
  font-family: 'Noto Serif KR', serif;
  font-size: 1.8rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 0.25rem;
}

.user-penname {
  font-size: 1rem;
  color: #868e96;
  font-weight: 500;
}

.author-message-box {
  flex-grow: 1;
  background-color: #f8f9fa;
  border-left: 4px solid #dee2e6;
  padding: 2rem;
  border-radius: 8px;
}

.author-message-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 0.75rem 0;
}

.author-message-content {
  font-size: 0.95rem;
  line-height: 1.7;
  color: #555;
}

/* --- 콘텐츠 섹션 --- */
.content-section {
  padding: 2rem 0;
}

.section-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 1.8rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 2rem;
  text-align: center;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f1f3f5;
}

/* --- 책 리스트 스타일 --- */
.book-list-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 2rem;
  justify-content: center;
  padding: 1rem 0;
}

.book-item-card {
  width: 140px;
  height: 210px;
  border-radius: 4px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  background-size: cover;
  background-position: center;
  text-decoration: none;
  color: inherit;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.book-item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.list-title-box {
  width: 80%;
  height: 70%;
  background-color: rgba(255, 255, 255, 0.95);
  padding: 10px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  text-align: left;
  color: #333;
  border-radius: 2px;
}

.list-title-box-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block; /* 표준 line-clamp를 위해 변경 */
  line-clamp: 3;
}

.list-title-box-author {
  font-size: 12px;
  font-weight: 500;
  margin: 0;
  color: #555;
}

/* --- 댓글 리스트 스타일 --- */
.comment-list-container {
  display: flex;
  flex-direction: column;
}

.comment-item {
  border-bottom: 1px solid #f1f3f5;
  padding: 1.5rem 0.5rem;
}
.comment-item:first-child {
  border-top: 1px solid #f1f3f5;
}

.comment-text {
  font-size: 1rem;
  line-height: 1.6;
  color: #555;
  margin-bottom: 0.75rem;
}

.comment-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.875rem;
  color: #868e96;
}

.comment-book-link {
  color: #868e96;
  text-decoration: none;
  font-weight: 600;
}

.comment-book-link:hover {
  text-decoration: underline;
}

.danger-zone {
  background-color: #fff5f5;
  border: 1px solid #ffc9c9;
  border-radius: 8px;
  padding: 2rem;
}

.danger-title {
  color: #c92a2a;
  border-bottom-color: #ffc9c9;
}

.danger-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.danger-content p {
  margin: 0;
  color: #555;
}

.btn-danger {
  background-color: #e03131;
  color: white;
  border: none;
  font-weight: 600;
}

.btn-danger:hover {
  background-color: #c92a2a;
}

/* --- 공통 스타일 --- */
.no-content-message {
  text-align: center;
  color: #999;
  font-size: 1rem;
  padding: 3rem 0;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-top: 1.5rem;
}

.divider {
  border: 0;
  border-top: 1px solid #f1f3f5;
  margin: 3rem 0;
}

/* --- 페이지네이션 스타일 --- */
.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.75rem;
  margin-top: 3rem;
}

.pagination-btn,
.page-number {
  font-family: 'Pretendard', sans-serif;
  background-color: #fff;
  border: 1px solid #dee2e6;
  padding: 0.5rem 0.8rem;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.9rem;
  color: #555;
}

.pagination-btn:hover:not(:disabled),
.page-number:hover {
  background-color: #f8f9fa;
  border-color: #ced4da;
}

.pagination-btn:disabled {
  color: #adb5bd;
  cursor: not-allowed;
  background-color: #f8f9fa;
}

.page-number.active {
  background-color: #333;
  color: white;
  border-color: #333;
  font-weight: 600;
}
</style>
