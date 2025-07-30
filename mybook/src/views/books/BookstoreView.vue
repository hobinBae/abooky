<template>
  <div class="bookstore-page">
    <!-- Search and Sort Section -->
    <section class="search-sort-section">
      <div class="search-sort-container">
        <div class="search-input-wrapper">
          <input v-model="searchTerm" type="text" placeholder="검색어를 입력하세요..." class="form-control search-input">
          <i class="fas fa-search search-icon"></i>
        </div>
        <div class="sort-options-wrapper">
          <label class="form-check form-check-inline" v-for="option in sortOptions" :key="option.value">
            <input type="radio" name="sort-option" :value="option.value" v-model="currentSortOption" class="form-check-input">
            <span class="form-check-label">{{ option.text }}</span>
          </label>
        </div>
      </div>
      <div class="keyword-buttons-container">
        <button v-for="keyword in keywords" :key="keyword" @click="toggleKeyword(keyword)" :class="{ active: activeKeywords.has(keyword) }" class="keyword-button">
          {{ keyword }}
        </button>
      </div>
    </section>

    <!-- Book List Section -->
    <section class="book-list-section">
      <h2 class="book-list-title">책 목록</h2>
      <div v-if="filteredBooks.length > 0" class="book-grid">
        <router-link v-for="book in filteredBooks" :key="book.id" :to="`/book-detail/${book.id}`" class="book-card-link">
          <div class="book-card">
            <div class="book-cover-placeholder">책 표지</div>
            <div class="book-details-right">
              <div class="book-main-info">
                <div class="book-title">{{ book.title }}</div>
                <div class="book-author">
                  <router-link :to="`/author/${book.authorId}`">{{ book.authorName || '작자 미상' }}</router-link>
                </div>
                <div class="book-summary">{{ book.summary }}</div>
              </div>
              <div class="book-stats-and-actions">
                <div class="book-stats-full">
                  <span>조회수 {{ book.views || 0 }}</span> |
                  <span>좋아요 {{ book.likes || 0 }}</span>
                </div>
                <button @click.prevent="toggleLike(book)" :class="{ liked: isLiked(book.id) }" class="like-button">
                  <i :class="isLiked(book.id) ? 'fas' : 'far'" class="fa-heart"></i> 좋아요
                </button>
              </div>
            </div>
          </div>
        </router-link>
      </div>
      <div v-else class="no-books-message">
        <p>해당하는 책이 없습니다.</p>
      </div>
    </section>

    <!-- Custom Message Box -->
    <div v-if="isMessageBoxVisible" class="modal" style="display: flex;">
      <div class="modal-content modal-sm">
        <span @click="isMessageBoxVisible = false" class="close-button">&times;</span>
        <h2 class="modal-title">{{ messageBoxTitle }}</h2>
        <p class="modal-body">{{ messageBoxContent }}</p>
        <button @click="isMessageBoxVisible = false" class="btn btn-primary modal-confirm-btn">확인</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { RouterLink } from 'vue-router';

// --- Interfaces ---
interface Book {
  id: string;
  title: string;
  authorId: string;
  authorName?: string;
  summary?: string;
  keywords?: string[];
  views?: number;
  likes?: number;
  createdAt: Date;
}

// --- Dummy Data ---
const DUMMY_BOOKS: Book[] = [
  { id: 'b1', title: '별 헤는 밤', authorId: 'author1', authorName: '윤동주', views: 1200, likes: 250, summary: '어두운 밤하늘 아래 별들을 헤아리며 고향과 가족을 그리워하는 시인의 마음을 담은 아름다운 시집.', keywords: ['자서전', '감성', '시'], createdAt: new Date('2023-01-15') },
  { id: 'b2', title: '어린 왕자', authorId: 'author2', authorName: '생텍쥐페리', views: 3500, likes: 780, summary: '사막에 불시착한 조종사가 만난 어린 왕자와의 이야기를 통해 삶의 진정한 의미를 탐구하는 철학 동화.', keywords: ['여행', '성장', '철학'], createdAt: new Date('2022-11-01') },
  { id: 'b3', title: '가족의 발견', authorId: 'author3', authorName: '김작가', views: 800, likes: 120, summary: '현대 사회에서 가족의 의미를 되새기고, 다양한 가족 형태의 아름다움을 조명하는 에세이.', keywords: ['가족', '에세이', '사회'], createdAt: new Date('2024-03-20') },
  { id: 'b4', title: '취미로 시작하는 코딩', authorId: 'author4', authorName: '이개발', views: 1500, likes: 300, summary: '코딩을 처음 접하는 사람들을 위한 쉽고 재미있는 입문서. 다양한 프로젝트를 통해 코딩의 즐거움을 알려준다.', keywords: ['취미', 'IT', '자기계발'], createdAt: new Date('2023-09-10') },
  { id: 'b5', title: '사랑의 온도', authorId: 'author5', authorName: '박사랑', views: 2000, likes: 450, summary: '엇갈린 사랑과 인연 속에서 진정한 사랑의 의미를 찾아가는 연인들의 이야기. 감성적인 문체가 돋보인다.', keywords: ['연애', '로맨스', '감성'], createdAt: new Date('2024-01-05') },
  { id: 'b6', title: '스포츠 심리학 개론', authorId: 'author6', authorName: '최건강', views: 950, likes: 180, summary: '운동선수들의 심리 상태와 경기력 향상을 위한 심리학적 접근을 다룬 전문 서적.', keywords: ['스포츠', '심리학', '건강'], createdAt: new Date('2023-05-22') },
  { id: 'b7', title: '드래곤의 유산', authorId: 'author7', authorName: '김판타', views: 2500, likes: 600, summary: '고대 드래곤의 힘을 이어받은 주인공이 세상을 구하기 위해 모험을 떠나는 장대한 판타지 소설.', keywords: ['판타지', '모험', '영웅'], createdAt: new Date('2022-08-01') },
  { id: 'b8', title: '우주 탐사대의 기록', authorId: 'author8', authorName: '이공상', views: 1800, likes: 380, summary: '미지의 행성을 탐사하는 우주선 승무원들의 생존과 발견을 다룬 SF 소설. 과학적 상상력이 돋보인다.', keywords: ['공상과학', '우주', '미래'], createdAt: new Date('2024-02-28') },
  { id: 'b9', title: '조선 왕조 실록 이야기', authorId: 'author9', authorName: '정역사', views: 1100, likes: 200, summary: '조선 왕조 500년 역사를 쉽고 재미있게 풀어낸 역사 교양서. 흥미로운 에피소드와 인물 중심의 서술.', keywords: ['역사', '교양', '한국사'], createdAt: new Date('2023-07-07') },
  { id: 'b10', title: '나는 오늘부터 성장한다', authorId: 'author10', authorName: '강성장', views: 2200, likes: 550, summary: '작은 습관의 변화가 삶을 어떻게 바꾸는지 보여주는 자기계발서. 긍정적인 메시지와 실천적인 조언.', keywords: ['자기계발', '성장', '동기부여'], createdAt: new Date('2024-04-12') },
];

// --- Reactive State ---
const allBooks = ref<Book[]>(DUMMY_BOOKS);
const likedBookIds = ref<Set<string>>(new Set(['b1', 'b3'])); // Some dummy liked books
const searchTerm = ref('');
const currentSortOption = ref('latest');
const activeKeywords = ref<Set<string>>(new Set());

const keywords = ['자서전', '여행', '가족', '취미', '연애', '스포츠', '판타지', '공상과학', '역사', '자기계발'];
const sortOptions = [
  { value: 'latest', text: '최신순' },
  { value: 'popular', text: '인기순' },
  { value: 'views', text: '조회순' },
];

// Message Box
const isMessageBoxVisible = ref(false);
const messageBoxTitle = ref('');
const messageBoxContent = ref('');

// --- Computed Properties ---
const filteredBooks = computed(() => {
  const books = allBooks.value.filter(book => {
    const search = searchTerm.value.toLowerCase();
    const matchesSearch = book.title.toLowerCase().includes(search) ||
                          (book.authorName || '').toLowerCase().includes(search) ||
                          (book.summary || '').toLowerCase().includes(search);

    const matchesKeywords = activeKeywords.value.size === 0 ||
                            (book.keywords && Array.from(activeKeywords.value).every(kw => book.keywords!.includes(kw)));

    return matchesSearch && matchesKeywords;
  });

  return books.sort((a, b) => {
    switch (currentSortOption.value) {
      case 'popular':
        return (b.likes || 0) - (a.likes || 0);
      case 'views':
        return (b.views || 0) - (a.views || 0);
      case 'latest':
      default:
        return b.createdAt.getTime() - a.createdAt.getTime();
    }
  });
});

// --- Functions ---
function showMessageBox(message: string, title = '알림') {
  messageBoxTitle.value = title;
  messageBoxContent.value = message;
  isMessageBoxVisible.value = true;
}

function toggleKeyword(keyword: string) {
  if (activeKeywords.value.has(keyword)) {
    activeKeywords.value.delete(keyword);
  } else {
    activeKeywords.value.add(keyword);
  }
}

function isLiked(bookId: string) {
    return likedBookIds.value.has(bookId);
}

function toggleLike(book: Book) {
    // Simulate like/unlike locally
    if (isLiked(book.id)) {
        likedBookIds.value.delete(book.id);
        book.likes = (book.likes || 1) - 1;
        showMessageBox('좋아요를 취소했습니다.');
    } else {
        likedBookIds.value.add(book.id);
        book.likes = (book.likes || 0) + 1;
        showMessageBox('좋아요를 눌렀습니다!');
    }
}

// --- Initialization ---
onMounted(() => {
  // No Firebase initialization needed for dummy data
});
</script>

<style scoped>
.bookstore-page {
  padding-top: 80px;
  padding-bottom: 80px;
  background-color: #F5F5DC;
  color: #3D2C20;
  min-height: calc(100vh - 136px);
  padding-left: 1rem;
  padding-right: 1rem;
}

.search-sort-section {
  background-color: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
  padding: 2rem;
  margin: 0 auto 2rem auto;
  max-width: 1200px;
}

.search-sort-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
}

@media (min-width: 768px) {
  .search-sort-container {
    flex-direction: row;
    justify-content: space-between;
  }
}

.search-input-wrapper {
  position: relative;
  flex-grow: 1;
  width: 100%;
}

.search-input {
  width: 100%;
  padding-left: 2.5rem;
  padding-right: 1rem;
  padding-top: 0.75rem;
  padding-bottom: 0.75rem;
  border: 1px solid #B8860B;
  border-radius: 9999px;
  background-color: #F5F5DC;
  color: #3D2C20;
  font-size: 1rem;
}

.search-input::placeholder {
  color: #8B4513;
}

.search-input:focus {
  outline: none;
  box-shadow: 0 0 0 0.25rem rgba(184, 134, 11, 0.25);
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #B8860B;
  font-size: 1.2rem;
}

.sort-options-wrapper {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  justify-content: center;
}

.form-check-label {
  color: #3D2C20;
  font-weight: 500;
}

.form-check-input:checked {
  background-color: #B8860B;
  border-color: #B8860B;
}

.keyword-buttons-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  justify-content: center;
  margin-top: 1.5rem;
}

.keyword-button {
  padding: 0.5rem 1rem;
  border-radius: 9999px;
  font-size: 0.9rem;
  font-weight: 500;
  transition: background-color 0.2s ease-in-out, color 0.2s ease-in-out;
  white-space: nowrap;
  border: 1px solid #B8860B;
  background-color: transparent;
  color: #B8860B;
}

.keyword-button.active {
  background-color: #B8860B;
  color: #FFFFFF;
}

.keyword-button:not(.active):hover {
  background-color: rgba(184, 134, 11, 0.1);
}

.book-list-section {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.book-list-title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 1.5rem;
  color: #3D2C20;
  text-align: center;
}

.book-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}

.book-card-link {
    text-decoration: none;
    color: inherit;
}

.book-card {
  background-color: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  padding: 1.5rem;
  gap: 1.5rem;
  color: #3D2C20;
}

.book-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.book-cover-placeholder {
  flex-shrink: 0;
  width: 120px;
  height: 180px;
  background-color: #EAE0D5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
  color: #5C4033;
  border-radius: 8px;
}

.book-details-right {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
  height: 180px; /* Match cover height */
}

.book-main-info {
    flex-grow: 1;
}

.book-title {
    font-size: 1.25rem;
    font-weight: 600;
    margin-bottom: 0.5rem;
}

.book-author {
    font-size: 0.9rem;
    color: #8B4513;
    margin-bottom: 0.75rem;
}

.book-summary {
  font-size: 0.9rem;
  color: #5C4033;
  max-height: 60px; /* Approx 3 lines */
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.6;
}

.book-stats-and-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  padding-top: 1rem;
  gap: 0.5rem;
}

.book-stats-full {
    font-size: 0.8rem;
    color: #8B4513;
}

.like-button {
    background-color: transparent;
    border: 1px solid #CD5C5C; /* IndianRed */
    color: #CD5C5C;
    padding: 0.4rem 0.8rem;
    border-radius: 20px;
    cursor: pointer;
    transition: background-color 0.2s, color 0.2s;
}

.like-button.liked, .like-button:hover {
    background-color: #CD5C5C;
    color: #FFFFFF;
}

.like-button i {
    margin-right: 0.4rem;
}

.no-books-message {
    text-align: center;
    padding: 3rem;
    font-size: 1.2rem;
    color: #8B4513;
}

/* Modal Styles */
.modal {
  display: none; /* Hidden by default, shown by v-if */
  position: fixed;
  z-index: 1000;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  overflow: auto;
  background-color: rgba(0,0,0,0.6);
  justify-content: center;
  align-items: center;
}

.modal-content {
  background-color: #5C4033;
  margin: auto;
  padding: 2rem;
  border-radius: 12px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.5);
  position: relative;
  color: #F5F5DC;
}

.close-button {
  color: #F5F5DC;
  float: right;
  font-size: 28px;
  font-weight: bold;
  position: absolute;
  top: 10px;
  right: 20px;
  cursor: pointer;
}

.modal-title {
  font-size: 1.8rem;
  font-weight: bold;
  margin-bottom: 1rem;
  color: #B8860B;
}

.modal-body {
    margin-bottom: 1.5rem;
}

.modal-confirm-btn {
  background-color: #B8860B;
  color: #3D2C20;
  font-weight: bold;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  transition: background-color 0.2s ease-in-out;
  border: none;
  width: 100%;
}

.modal-confirm-btn:hover {
  background-color: #DAA520;
}
</style>
