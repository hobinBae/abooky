<template>
  <div class="continue-writing-page">
    <div class="header-controls">
      <h2 class="section-title">작성 중인 책 목록</h2>
      <button @click="goBack" class="btn btn-primary">
        <i class="bi bi-arrow-left"></i> 뒤로가기
      </button>
    </div>
    <p class="section-subtitle">계속해서 이야기 작성 할 책을 선택하세요.</p>
    <section class="load-book-section">
      <div v-if="inProgressBooks.length > 0" class="book-list">
        <div v-for="book in paginatedBooks" :key="book.bookId" class="book-list-item">
          <div class="book-details" @click="editBook(book.bookId)">
            <span class="book-title">{{ book.title }}</span>
            <span class="book-last-saved">마지막 저장: {{ new Date(book.updatedAt).toLocaleString() }}</span>
          </div>
          <button @click="confirmDelete(book.bookId)" class="delete-btn">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
      </div>
      <div v-if="totalPages > 1" class="pagination-controls">
        <button @click="goToPage(currentPage - 1)" :disabled="currentPage === 1" class="pagination-btn">
          <i class="bi bi-chevron-left"></i>
        </button>
        <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
        <button @click="goToPage(currentPage + 1)" :disabled="currentPage === totalPages" class="pagination-btn">
          <i class="bi bi-chevron-right"></i>
        </button>
      </div>
      <p v-else class="no-books-message">작성 중인 책이 없습니다. '새 책 만들기'로 시작해보세요!</p>
      <button @click="goBack" class="btn btn-outline mt-4">
        <i class="bi bi-arrow-left"></i> 뒤로가기
      </button>
    </section>
    <CustomAlert ref="customAlert" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from '@/api';
import CustomAlert from '@/components/common/CustomAlert.vue';

// --- Interfaces ---
interface Book {
  bookId: string;
  title: string;
  updatedAt: string; // ISO 8601 형식의 문자열
  completed: boolean;
}

// --- Router ---
const router = useRouter();

// --- Reactive State ---
const inProgressBooks = ref<Book[]>([]);
const currentPage = ref(1);
const itemsPerPage = 5; // 5개 이상 목록이 길면 페이지네이션 적용
const customAlert = ref<InstanceType<typeof CustomAlert> | null>(null);

// --- Computed Properties ---
const paginatedBooks = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return inProgressBooks.value.slice(start, end);
});

const totalPages = computed(() => {
  return Math.ceil(inProgressBooks.value.length / itemsPerPage);
});

// --- Functions ---
async function fetchInProgressBooks() {
  try {
    const response = await apiClient.get('/api/v1/books');
    // API에서 받아온 전체 책 목록 중 완료되지 않은 책만 필터링
    inProgressBooks.value = response.data.data.filter((book: Book) => !book.completed);
  } catch (error) {
    console.error("작성 중인 책 목록을 불러오는데 실패했습니다:", error);
  }
}

async function confirmDelete(bookId: string) {
  if (customAlert.value) {
    const confirmed = await customAlert.value.showConfirm({
      title: '삭제 확인',
      message: '정말로 이 책을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.',
    });

    if (confirmed) {
      await deleteBook(bookId);
    }
  }
}

async function deleteBook(bookId: string) {
  try {
    await apiClient.delete(`/api/v1/books/${bookId}`);
    inProgressBooks.value = inProgressBooks.value.filter(book => book.bookId !== bookId);
    if (customAlert.value) {
      customAlert.value.showAlert({
        title: '삭제 완료',
        message: '책이 성공적으로 삭제되었습니다.',
      });
    }
  } catch (error) {
    console.error("책 삭제에 실패했습니다:", error);
    if (customAlert.value) {
      customAlert.value.showAlert({
        title: '삭제 실패',
        message: '책을 삭제하는 중 오류가 발생했습니다.',
      });
    }
  }
}

function editBook(bookId: string) {
  router.push(`/book-editor/${bookId}`);
}

function goBack() {
  router.push('/create-book');
}

function goToPage(page: number) {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
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
  padding: 1.5rem;
  /* Match auth-page padding */
  background-color: var(--background-color);
  color: var(--primary-text-color);
  min-height: 100vh;
  /* Use 100vh for full height */
  font-family: 'SCDream4', sans-serif;
  /* Match MyPageView font */
  display: flex;
  flex-direction: column;
  align-items: center;
  /* Center content horizontally */
  justify-content: flex-start;
  /* Align content to the top */
}

.section-title {
  font-family: 'SCDream3', serif;
  font-size:2.5rem;
  /* Adjusted to be slightly smaller than MyPageView's main title */
  font-weight: 700;
  color: #333;
  /* Match MyPageView's primary text color */
  margin-bottom: 0.5rem;
  /* Handled by parent flex container */
}

.section-subtitle {
  font-family: 'SCDream4', serif;
  font-size: 16px;
  /* Adjusted font size */
  color: #666;
  /* Match MyPageView's secondary text color */
  margin-bottom: 1.5rem;
  max-width: 600px;
  line-height: 1.7;
  text-align: center;
  /* Center the subtitle */
}

.header-controls {
  display: flex;
  justify-content: center;
  /* Center the title and button */
  align-items: center;
  gap: 1.5rem;
  /* Space between title and button */
  margin-bottom: 2px;

}



.load-book-section {
  max-width: 820px;
  /* Match auth-wrapper max-width */
  width: 80%;
  /* Ensure it takes 80% width */
  min-height: 460px;
  /* Match auth-wrapper min-height */
  background-color: #fff;
  /* Match auth-wrapper background */
  border-radius: 13px;
  /* Match auth-wrapper border-radius */
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  /* Subtle shadow */
  border: 2px solid #6F7D48;
  /* Match auth-wrapper border */
  padding: 1.5rem;
  /* Adjusted padding */
  display: flex;
  /* Use flexbox for internal layout */
  flex-direction: column;
  align-items: center;
  /* Center content within the section */
  margin: 0 auto;
  /* Center the section itself */
}

.book-list {
  width: 100%;
}

.book-list-item {
  background: var(--surface-color);
  padding: 1rem 1.5rem;
  /* Adjusted padding */
  border-radius: 10px;
  /* More rounded corners */
  margin-bottom: 0.7rem;
  /* Slightly less margin */
  display: flex;
  justify-content: space-between;
  /* Distribute items */
  align-items: center;
  cursor: pointer;
  border: 1px solid var(--border-color);
  /* Add a subtle border */
  text-align: left;
  transition: all 0.2s ease-in-out;
  /* Smooth transition for all properties */
  width: 100%;
  /* Ensure it takes 100% width of its parent */
  box-sizing: border-box;
  /* Include padding in width calculation */
  max-width: 100%;
  /* Ensure it doesn't exceed parent's content area */
}

.book-list-item:hover {
  background-color: #fcfaf7;
  /* Lighter hover background */
  transform: translateY(-3px);
  /* Lift effect on hover */
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.08);
  /* More pronounced shadow on hover */
}

.book-title {
  font-family: 'ChosunCentennial', serif;
  font-weight: 600;
  font-size: 1.2rem;
  /* Slightly larger */
  color: var(--primary-text-color);
  white-space: nowrap;
  /* Prevent wrapping */
  overflow: hidden;
  /* Hide overflow */
  text-overflow: ellipsis;
  /* Add ellipsis for overflow */
  flex-grow: 1;
  /* Allow title to grow */
  margin-right: 1rem;
  /* Space between title and last saved */
}

.book-last-saved {
  font-size: 0.8rem;
  /* Slightly larger */
  color: var(--secondary-text-color);
  flex-shrink: 0;
  /* Prevent shrinking */
}

.book-details {
  flex-grow: 1;
  display: flex;
  align-items: center;
  cursor: pointer;
  overflow: hidden;
}

.delete-btn {
  background: transparent;
  border: none;
  color: var(--secondary-text-color);
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0 0 0 1rem;
  transition: color 0.3s ease;
  flex-shrink: 0;
}

.delete-btn:hover {
  color: #d9534f; /* Red on hover */
}

.no-books-message {
  padding: 2.5rem;
  font-size: 1.1rem;
  /* Slightly larger */
  color: var(--secondary-text-color);
  background-color: #fdf8e7;
  /* Light background for message */
  border-radius: 8px;
  margin-top: 2rem;
  border: 1px dashed var(--border-color);
}

.btn-outline {
  border: 1px solid var(--accent-color);
  /* Use accent color for border */
  background-color: transparent;
  color: var(--accent-color);
  /* Use accent color for text */
  padding: 0.8rem 1.5rem;
  /* Larger padding */
  border-radius: 8px;
  /* More rounded */
  cursor: pointer;
  transition: all 0.3s ease-in-out;
  font-size: 1rem;
  /* Slightly larger font */
  font-weight: 600;
  /* Bolder font */
  display: inline-flex;
  align-items: center;
  gap: 0.6rem;
  margin-top: 2.5rem;
  /* More margin */
}



.btn-outline:hover {
  background-color: var(--accent-color);
  /* Fill with accent color on hover */
  color: var(--surface-color);
  /* White text on hover */
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.3);
  /* Shadow for button */
  transform: translateY(-2px);
  /* Lift effect */
}

/* --- Button Styles from MyLibraryView.vue --- */
.btn.btn-primary {
  position: relative;
  overflow: hidden;
  z-index: 1;
  display: inline-block;
  border: 3px solid #5b673b !important;
  border-radius: 18px !important;
  padding: 0.45rem 1.1rem !important;
  font-size: 0.9rem !important;
  white-space: nowrap;
  font-family: 'SCDream5', sans-serif;
  transition: color 0.5s ease;
  background-color: transparent;
  color: #000000;
}

.btn.btn-primary::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(185, 174, 122, 0.4);
  transform: scaleY(0);
  z-index: -1;
  animation: fill-animation 3s infinite ease-in-out;
}

.btn-primary:hover {
  color: white !important;
  border-color: #dee2e6 !important;
  background-color: transparent;
}

@keyframes fill-animation {
  0% {
    transform-origin: top;
    transform: scaleY(0);
  }
  50% {
    transform-origin: top;
    transform: scaleY(1);
  }
  50.1% {
    transform-origin: bottom;
    transform: scaleY(1);
  }
  100% {
    transform-origin: bottom;
    transform: scaleY(0);
  }
}

/* Hide old btn-outline styles */
.btn-outline {
  display: none; /* Hide the old button style */
}

.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 1.6rem;
  /* Match MyPageView pagination margin */
  gap: 0.4rem;
  /* Match MyPageView pagination gap */
}

.pagination-btn,
.page-number {
  /* Match MyPageView pagination border */
  background-color: #fff;
  /* Match MyPageView pagination background */
  color: #939393;
  /* Match MyPageView pagination color */
  border-radius: 50%;
  /* Match MyPageView pagination border-radius */
  width: 26px;
  /* Match MyPageView pagination width */
  height: 26px;
  /* Match MyPageView pagination height */
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 1rem;
}


.pagination-btn:hover:not(:disabled),
.page-number:hover {
  border-color: #6F7D48;
  /* Match MyPageView pagination hover border-color */
  color: #000000;
  /* Match MyPageView pagination hover color */
}

.page-number.active {
  background-color: #6F7D48;
  /* Match MyPageView pagination active background */
  color: white;
  /* Match MyPageView pagination active color */
  border-color: #6F7D48;
  /* Match MyPageView pagination active border-color */
}
</style>
