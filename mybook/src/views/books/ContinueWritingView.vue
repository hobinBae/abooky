<template>
  <div class="continue-writing-page">
    <section class="load-book-section">
      <h2 class="section-title">작성 중인 책 목록</h2>
      <p class="section-subtitle">계속해서 이야기를 만들어갈 책을 선택하세요.</p>
      <div v-if="inProgressBooks.length > 0" class="book-list">
        <div v-for="book in inProgressBooks" :key="book.id" class="book-list-item" @click="editBook(book.id)">
          <div class="book-info">
            <span class="book-title">{{ book.title }}</span>
            <span class="book-last-saved">마지막 저장: {{ new Date(book.updatedAt).toLocaleString() }}</span>
          </div>
          <i class="bi bi-chevron-right"></i>
        </div>
      </div>
      <p v-else class="no-books-message">작성 중인 책이 없습니다. '새 책 만들기'로 시작해보세요!</p>
      <button @click="goBack" class="btn btn-outline mt-4">
        <i class="bi bi-arrow-left"></i> 뒤로가기
      </button>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

// --- Interfaces ---
interface Book {
  id: string;
  title: string;
  authorId: string;
  isPublished: boolean;
  episodes: { content: string }[];
  createdAt: Date;
  updatedAt: Date;
}

// --- Dummy Data ---
const DUMMY_IN_PROGRESS_BOOKS: Book[] = [
    {
    id: 'draft_book_1',
    title: '제주도 한 달 살기 (초안)',
    authorId: 'dummyUser1',
    isPublished: false,
    episodes: [
      { content: '협재 해변의 일몰은 정말 아름다웠다. 매일 봐도 질리지 않는 풍경.' },
      { content: '오름을 오르며 마주한 제주의 바람은 모든 시름을 잊게 했다.' },
    ],
    createdAt: new Date('2024-06-01T10:00:00Z'),
    updatedAt: new Date('2024-07-20T15:30:00Z'),
  },
  {
    id: 'draft_book_2',
    title: '나의 요리 레시피 북 (미완성)',
    authorId: 'dummyUser1',
    isPublished: false,
    episodes: [
      { content: '김치찌개 황금 레시피: 돼지고기 듬뿍, 신김치 필수!' },
    ],
    createdAt: new Date('2024-07-01T09:00:00Z'),
    updatedAt: new Date('2024-07-25T11:00:00Z'),
  },
];

// --- Router ---
const router = useRouter();

// --- Reactive State ---
const inProgressBooks = ref<Book[]>([]);

// --- Functions ---
function fetchInProgressBooks() {
  // In a real app, you would fetch this from an API
  inProgressBooks.value = DUMMY_IN_PROGRESS_BOOKS.filter(book => !book.isPublished);
}

function editBook(bookId: string) {
  router.push(`/book-editor/${bookId}`);
}

function goBack() {
    router.push('/create-book');
}

// --- Lifecycle Hooks ---
onMounted(() => {
  fetchInProgressBooks();
});
</script>

<style scoped>
/* --- Google Fonts Import --- */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&family=Pretendard:wght@400;500;700&display=swap');

/* --- 색상 변수 --- */
:root {
  --background-color: #F5F5DC;
  --surface-color: #FFFFFF;
  --primary-text-color: #3D2C20;
  --secondary-text-color: #6c757d;
  --accent-color: #8B4513;
  --border-color: #EAE0D5;
}

.continue-writing-page {
  padding: 80px 2rem 2rem;
  background-color: var(--background-color);
  color: var(--primary-text-color);
  min-height: calc(100vh - 56px);
  font-family: 'Pretendard', sans-serif;
  text-align: center;
}

.section-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 2.8rem;
  font-weight: 700;
  color: var(--primary-text-color);
  margin-bottom: 0.75rem;
}

.section-subtitle {
  font-size: 1.25rem;
  color: #5C4033;
  margin-bottom: 4rem;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.7;
}

.load-book-section {
  max-width: 700px;
  margin: 0 auto;
}

.book-list-item {
  background: var(--surface-color);
  padding: 1.25rem 2rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  border: 1px solid transparent;
  border-bottom: 1px solid var(--border-color);
  text-align: left;
  transition: background-color 0.2s;
}

.book-list-item:hover {
  background-color: #f8f6f2;
}

.book-info {
  display: flex;
  flex-direction: column;
}

.book-title {
  font-weight: 600;
  font-size: 1.1rem;
  margin-bottom: 0.25rem;
}

.book-last-saved {
  font-size: 0.9rem;
  color: var(--secondary-text-color);
}

.book-list-item i {
  color: var(--secondary-text-color);
}

.no-books-message {
  padding: 2rem;
}

.btn-outline {
    border: 1px solid #C8AD7F;
    background-color: transparent;
    color: #5C4033;
    padding: 0.6rem 1.2rem;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s ease-in-out;
    font-size: 0.95rem;
    font-weight: 500;
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    margin-top: 1rem;
}

.btn-outline:hover {
  background-color: #FDF8E7;
  border-color: var(--accent-color);
}
</style>
