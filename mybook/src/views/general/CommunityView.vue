<template>
  <div class="community-page">
    <!-- Search and Sort Section -->
    <section class="search-sort-section">
      <div class="search-sort-container">
        <!-- Search Input -->
        <div class="search-input-wrapper">
          <input type="text" id="search-input" placeholder="검색어를 입력하세요..."
                 class="form-control search-input" v-model="currentSearchTerm">
          <i class="fas fa-search search-icon"></i>
        </div>

        <!-- Sort Options -->
        <div class="sort-options-wrapper">
          <label class="form-check form-check-inline">
            <input type="radio" name="sort-option" value="latest" v-model="currentSortOption"
                   class="form-check-input">
            <span class="form-check-label">최신순</span>
          </label>
          <label class="form-check form-check-inline">
            <input type="radio" name="sort-option" value="popular" v-model="currentSortOption"
                   class="form-check-input">
            <span class="form-check-label">인기순</span>
          </label>
          <label class="form-check form-check-inline">
            <input type="radio" name="sort-option" value="views" v-model="currentSortOption"
                   class="form-check-input">
            <span class="form-check-label">조회순</span>
          </label>
        </div>
      </div>

      <!-- Keyword Filters -->
      <div class="keyword-buttons-container">
        <button v-for="keyword in keywords" :key="keyword"
                :class="['keyword-button', { active: activeKeywords.has(keyword) }]"
                @click="toggleKeyword(keyword)">
          {{ keyword }}
        </button>
      </div>
    </section>

    <!-- Book List Section -->
    <section class="book-list-section">
      <h2 class="book-list-title">책 목록</h2>
      <div class="book-grid">
        <div v-if="filteredBooks.length === 0" class="no-books-message">
          해당하는 책이 없습니다.
        </div>
        <div v-for="book in filteredBooks" :key="book.id" class="book-card">
          <div class="book-cover-placeholder">책 표지</div>
          <div class="book-details-right">
            <div class="book-main-info">
              <div class="book-title">{{ book.title }}</div>
              <div class="book-author">{{ book.author }}</div>
              <div class="book-summary">{{ book.summary }}</div>
            </div>
            <div class="book-stats-and-actions">
              <div class="book-stats-full">
                <span>조회수 {{ book.views }}</span> |
                <span>평점 {{ book.rating }}</span> |
                <span>좋아요 {{ book.likes }}</span>
              </div>
              <button class="like-button">
                <i class="far fa-heart"></i> 좋아요
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Custom Message Box -->
    <div v-if="messageBox.isVisible" class="modal" @click.self="messageBox.isVisible = false">
      <div class="modal-content modal-sm">
        <span class="close-button" @click="messageBox.isVisible = false">&times;</span>
        <h2 class="modal-title">{{ messageBox.title }}</h2>
        <p class="modal-body">{{ messageBox.message }}</p>
        <button @click="messageBox.isVisible = false" class="btn btn-primary modal-confirm-btn">
          확인
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { initializeApp } from "https://www.gstatic.com/firebasejs/11.6.1/firebase-app.js";
import { getAuth, signInAnonymously, signInWithCustomToken, onAuthStateChanged } from "https://www.gstatic.com/firebasejs/11.6.1/firebase-auth.js";
import { getFirestore, doc, getDoc, addDoc, setDoc, updateDoc, deleteDoc, onSnapshot, collection, query, where, getDocs } from "https://www.gstatic.com/firebasejs/11.6.1/firebase-firestore.js";

// Firebase configuration from Canvas environment
const firebaseConfig = typeof __firebase_config !== 'undefined' ? JSON.parse(__firebase_config) : {};
const appId = typeof __app_id !== 'undefined' ? __app_id : 'default-app-id';
const initialAuthToken = typeof __initial_auth_token !== 'undefined' ? __initial_auth_token : null;

let firebaseApp: any = null;
let db: any = null;
let auth: any = null;
let userId: string | null = null;
const isAuthReady = ref(false);

async function initializeFirebase() {
  try {
    if (!firebaseConfig || Object.keys(firebaseConfig).length === 0) {
      console.error("Firebase config is missing or empty.");
      return;
    }

    firebaseApp = initializeApp(firebaseConfig);
    db = getFirestore(firebaseApp);
    auth = getAuth(firebaseApp);

    onAuthStateChanged(auth, async (user) => {
      if (user) {
        userId = user.uid;
        console.log("Firebase Auth State Changed: Logged in as", userId);
      } else {
        try {
          if (initialAuthToken) {
            await signInWithCustomToken(auth, initialAuthToken);
            userId = auth.currentUser.uid;
            console.log("Signed in with custom token:", userId);
          } else {
            await signInAnonymously(auth);
            userId = auth.currentUser.uid;
            console.log("Signed in anonymously:", userId);
          }
        } catch (error) {
          console.error("Firebase authentication failed:", error);
          userId = crypto.randomUUID();
          console.warn("Using a random UUID as userId due to auth failure:", userId);
        }
      }
      isAuthReady.value = true;
      console.log("Firebase initialized and auth ready. User ID:", userId);
      fetchBooks(); // Fetch books after auth is ready
    });
  } catch (error) {
    console.error("Error initializing Firebase:", error);
  }
}

const firestore = {
  getDoc, doc, addDoc, setDoc, updateDoc, deleteDoc, onSnapshot, collection, query, where, getDocs
};
const firebaseUtils = {
  appId
};

const allBooks = ref<any[]>([]);
const currentSearchTerm = ref('');
const currentSortOption = ref('latest');
const activeKeywords = ref(new Set<string>());

const keywords = ['자서전', '여행', '가족', '취미', '연애', '스포츠', '판타지', '공상과학', '역사', '자기계발'];

const messageBox = ref({
  isVisible: false,
  title: '',
  message: '',
});

function showMessageBox(message: string, title = '알림') {
  messageBox.value.title = title;
  messageBox.value.message = message;
  messageBox.value.isVisible = true;
}

async function fetchBooks() {
  // In a real application, you would fetch from a Firestore collection like:
  // const booksCollectionRef = firestore.collection(db, `artifacts/${firebaseUtils.appId}/public/data/community_books`);
  // const snapshot = await firestore.getDocs(booksCollectionRef);
  // allBooks.value = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

  // Sample data for demonstration
  allBooks.value = [
    { id: 'b1', title: '별 헤는 밤', author: '윤동주', views: 1200, rating: 4.8, likes: 250, summary: '어두운 밤하늘 아래 별들을 헤아리며 고향과 가족을 그리워하는 시인의 마음을 담은 아름다운 시집.', keywords: ['자서전', '감성', '시'] },
    { id: 'b2', title: '어린 왕자', author: '생텍쥐페리', views: 3500, rating: 4.9, likes: 780, summary: '사막에 불시착한 조종사가 만난 어린 왕자와의 이야기를 통해 삶의 진정한 의미를 탐구하는 철학 동화.', keywords: ['여행', '성장', '철학'] },
    { id: 'b3', title: '가족의 발견', author: '김작가', views: 800, rating: 4.5, likes: 120, summary: '현대 사회에서 가족의 의미를 되새기고, 다양한 가족 형태의 아름다움을 조명하는 에세이.', keywords: ['가족', '에세이', '사회'] },
    { id: 'b4', title: '취미로 시작하는 코딩', author: '이개발', views: 1500, rating: 4.7, likes: 300, summary: '코딩을 처음 접하는 사람들을 위한 쉽고 재미있는 입문서. 다양한 프로젝트를 통해 코딩의 즐거움을 알려준다.', keywords: ['취미', 'IT', '자기계발'] },
    { id: 'b5', title: '사랑의 온도', author: '박사랑', views: 2000, rating: 4.6, likes: 450, summary: '엇갈린 사랑과 인연 속에서 진정한 사랑의 의미를 찾아가는 연인들의 이야기. 감성적인 문체가 돋보인다.', keywords: ['연애', '로맨스', '감성'] },
    { id: 'b6', title: '스포츠 심리학 개론', author: '최건강', views: 950, rating: 4.4, likes: 180, summary: '운동선수들의 심리 상태와 경기력 향상을 위한 심리학적 접근을 다룬 전문 서적.', keywords: ['스포츠', '심리학', '건강'] },
    { id: 'b7', title: '드래곤의 유산', author: '김판타', views: 2500, rating: 4.8, likes: 600, summary: '고대 드래곤의 힘을 이어받은 주인공이 세상을 구하기 위해 모험을 떠나는 장대한 판타지 소설.', keywords: ['판타지', '모험', '영웅'] },
    { id: 'b8', title: '우주 탐사대의 기록', author: '이공상', views: 1800, rating: 4.7, likes: 380, summary: '미지의 행성을 탐사하는 우주선 승무원들의 생존과 발견을 다룬 SF 소설. 과학적 상상력이 돋보인다.', keywords: ['공상과학', '우주', '미래'] },
    { id: 'b9', title: '조선 왕조 실록 이야기', author: '정역사', views: 1100, rating: 4.5, likes: 200, summary: '조선 왕조 500년 역사를 쉽고 재미있게 풀어낸 역사 교양서. 흥미로운 에피소드와 인물 중심의 서술.', keywords: ['역사', '교양', '한국사'] },
    { id: 'b10', title: '나는 오늘부터 성장한다', author: '강성장', views: 2200, rating: 4.9, likes: 550, summary: '작은 습관의 변화가 삶을 어떻게 바꾸는지 보여주는 자기계발서. 긍정적인 메시지와 실천적인 조언.', keywords: ['자기계발', '성장', '동기부여'] },
  ];
}

const filteredBooks = computed(() => {
  const books = allBooks.value.filter(book => {
    const matchesSearch = book.title.toLowerCase().includes(currentSearchTerm.value.toLowerCase()) ||
                          book.author.toLowerCase().includes(currentSearchTerm.value.toLowerCase()) ||
                          book.summary.toLowerCase().includes(currentSearchTerm.value.toLowerCase());

    const matchesKeywords = activeKeywords.value.size === 0 ||
                            (book.keywords && Array.from(activeKeywords.value).every(keyword => book.keywords.includes(keyword)));
    return matchesSearch && matchesKeywords;
  });

  if (currentSortOption.value === 'latest') {
    books.sort((a, b) => b.id.localeCompare(a.id));
  } else if (currentSortOption.value === 'popular') {
    books.sort((a, b) => b.likes - a.likes);
  } else if (currentSortOption.value === 'views') {
    books.sort((a, b) => b.views - a.views);
  }
  return books;
});

const toggleKeyword = (keyword: string) => {
  if (activeKeywords.value.has(keyword)) {
    activeKeywords.value.delete(keyword);
  } else {
    activeKeywords.value.add(keyword);
  }
};

watch([currentSearchTerm, currentSortOption, activeKeywords], () => {
  // No explicit re-render needed here, computed property handles it
});

onMounted(() => {
  initializeFirebase();
});
</script>

<style scoped>
.community-page {
  padding-top: 80px; /* Adjust for fixed navbar */
  padding-bottom: 80px; /* Adjust for fixed footer */
  background-color: #F5F5DC; /* Cream/Ivory background for the page */
  color: #3D2C20; /* Dark brown text for contrast */
  min-height: calc(100vh - 136px); /* Full height minus navbar and footer */
  padding-left: 1rem;
  padding-right: 1rem;
}

.search-sort-section,
.book-list-section {
  background-color: #F5F5DC; /* Cream/Ivory background */
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  padding: 2rem;
  margin-bottom: 2rem;
}

.search-sort-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 1.5rem;
}

@media (min-width: 768px) {
  .search-sort-container {
    flex-direction: row;
    justify-content: space-between;
    margin-bottom: 1rem;
  }
}

.search-input-wrapper {
  position: relative;
  flex-grow: 1;
  width: 100%;
  margin-bottom: 1rem;
}

@media (min-width: 768px) {
  .search-input-wrapper {
    width: auto;
    margin-bottom: 0;
    margin-right: 1.5rem;
  }
}

.search-input {
  width: 100%;
  padding-left: 2.5rem;
  padding-right: 1rem;
  padding-top: 0.75rem;
  padding-bottom: 0.75rem;
  border: 1px solid #B8860B; /* Gold border */
  border-radius: 9999px; /* Pill shape */
  background-color: #F5F5DC; /* Cream/Ivory background */
  color: #3D2C20; /* Dark brown text */
  font-size: 1rem;
}

.search-input::placeholder {
  color: #8B4513; /* SaddleBrown placeholder */
}

.search-input:focus {
  outline: none;
  box-shadow: 0 0 0 0.25rem rgba(184, 134, 11, 0.25); /* Gold glow */
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #B8860B; /* Gold icon */
  font-size: 1.2rem;
}

.sort-options-wrapper {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  justify-content: center;
}

.form-check-label {
  color: #3D2C20; /* Dark brown text */
  font-weight: 500;
}

.form-check-input:checked {
  background-color: #B8860B; /* Gold checked state */
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
  border-radius: 9999px; /* Pill shape */
  font-size: 0.9rem;
  font-weight: 500;
  transition: background-color 0.2s ease-in-out, color 0.2s ease-in-out;
  white-space: nowrap;
  border: 1px solid #B8860B; /* Gold border */
  background-color: transparent;
  color: #B8860B; /* Gold text */
}

.keyword-button.active {
  background-color: #B8860B; /* Gold for active */
  color: #3D2C20; /* Dark brown text */
}

.keyword-button:not(.active):hover {
  background-color: rgba(184, 134, 11, 0.1); /* Light gold hover */
}

.book-list-section {
  background-color: #F5F5DC; /* Cream/Ivory background */
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  padding: 2rem;
}

.book-list-title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 1.5rem;
  color: #3D2C20; /* Dark brown title */
  text-align: center;
}

.book-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}

@media (min-width: 768px) {
  .book-grid {
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); /* Adjusted for more flexible cards */
  }
}

.book-card {
  background-color: #5C4033; /* Darker brown for card background */
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  cursor: pointer;
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
  padding: 1.5rem;
  gap: 1.5rem;
  color: #F5F5DC; /* Cream/Ivory text */
  min-height: 250px; /* Ensure a minimum height for the card */
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.2);
}

.book-cover-placeholder {
  flex-shrink: 0;
  width: 120px;
  height: 180px;
  background-color: #8B4513; /* SaddleBrown for cover placeholder */
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
  color: #F5F5DC; /* Cream/Ivory text */
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

.book-details-right {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0; /* Allow content to shrink */
}

.book-main-info {
  margin-bottom: 1rem;
}

.book-title {
  font-weight: bold;
  font-size: 1.5rem;
  color: #F5F5DC; /* Cream/Ivory title */
  margin-bottom: 0.5rem;
  line-height: 1.3;
}

.book-author {
  font-size: 1rem;
  color: #D2B48C; /* Tan for author */
  margin-bottom: 0.8rem;
}

.book-summary {
  font-size: 0.9rem;
  color: #F5F5DC; /* Cream/Ivory summary */
  max-height: 80px;
  overflow-y: auto;
  line-height: 1.6;
  word-break: break-word; /* Allow long words to break */
}

.book-stats-and-actions {
  display: flex;
  flex-wrap: wrap; /* Allow items to wrap */
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 1rem;
  border-top: 1px solid rgba(245, 245, 220, 0.2); /* Light border */
  gap: 0.5rem; /* Add gap for wrapped items */
}

.book-stats-full {
  font-size: 0.9rem;
  color: #D2B48C; /* Tan for stats */
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.like-button {
  background-color: #B8860B; /* Gold background */
  color: #3D2C20; /* Dark brown text */
  padding: 0.5rem 1rem;
  border-radius: 9999px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: background-color 0.2s ease-in-out;
  border: none;
}

.like-button:hover {
  background-color: #DAA520; /* Darker gold on hover */
}

/* Modal Styles */
.modal {
  display: none; /* Hidden by default */
  position: fixed; /* Stay in place */
  z-index: 1000; /* Sit on top */
  left: 0;
  top: 0;
  width: 100%; /* Full width */
  height: 100%; /* Full height */
  overflow: auto; /* Enable scroll if needed */
  background-color: rgba(0,0,0,0.6); /* Darker overlay */
  justify-content: center;
  align-items: center;
}
.modal-content {
  background-color: #5C4033; /* Dark brown modal background */
  margin: auto;
  padding: 2rem;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.5);
  position: relative;
  color: #F5F5DC; /* Cream/Ivory text */
}
.modal-sm {
  max-width: 400px;
}
.close-button {
  color: #F5F5DC; /* Cream/Ivory close button */
  float: right;
  font-size: 28px;
  font-weight: bold;
  position: absolute;
  top: 10px;
  right: 20px;
}
.close-button:hover,
.close-button:focus {
  color: #B8860B; /* Gold on hover */
  text-decoration: none;
  cursor: pointer;
}
.modal-title {
  font-size: 1.8rem;
  font-weight: bold;
  margin-bottom: 1rem;
  color: #B8860B; /* Gold title */
}
.modal-body {
  margin-bottom: 1.5rem;
  color: #F5F5DC; /* Cream/Ivory body text */
}
.modal-confirm-btn {
  background-color: #B8860B; /* Gold button */
  color: #3D2C20; /* Dark brown text */
  font-weight: bold;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  transition: background-color 0.2s ease-in-out;
  border: none;
  width: 100%;
}
.modal-confirm-btn:hover {
  background-color: #DAA520; /* Darker gold on hover */
}

.no-books-message {
  text-align: center;
  font-size: 1.2rem;
  color: #8B4513; /* SaddleBrown for no books message */
  margin-top: 2rem;
}
</style>
