<template>
  <div class="create-book-page">
    <!-- Step 1: Initial Choice -->
    <section v-if="step === 'initial'" class="initial-choice-section text-center">
      <h2 class="section-title">책 만들기</h2>
      <p class="section-subtitle">새로운 이야기를 시작하거나, 기존 작업을 이어가세요.</p>
      <div class="choice-cards">
        <div class="choice-card" @click="startNewBook">
          <div class="card-icon"><i class="bi bi-plus-circle-dotted"></i></div>
          <h3 class="card-title">새 책 생성</h3>
          <p class="card-description">AI와 함께 새로운 자서전, 일기, 소설을 만들어보세요.</p>
        </div>
        <div class="choice-card" @click="goToGroupCreate">
          <div class="card-icon"><i class="bi bi-people-fill"></i></div>
          <h3 class="card-title">그룹책 만들기</h3>
          <p class="card-description">친구, 가족과 함께 특별한 이야기를 만들어보세요.</p>
        </div>
        <div class="choice-card" @click="step = 'load'">
          <div class="card-icon"><i class="bi bi-journal-arrow-down"></i></div>
          <h3 class="card-title">작성 중인 책 불러오기</h3>
          <p class="card-description">임시 저장된 책을 불러와 계속해서 이야기를 완성하세요.</p>
        </div>
      </div>
    </section>

    <!-- Step 2: Load In-Progress Book -->
    <section v-if="step === 'load'" class="load-book-section">
      <h2 class="section-title">작성 중인 책 목록</h2>
      <div v-if="inProgressBooks.length > 0" class="book-list">
        <div v-for="book in inProgressBooks" :key="book.id" class="book-list-item" @click="loadBook(book)">
          <span class="book-title">{{ book.title }}</span>
          <span class="book-last-saved">마지막 저장: {{ book.updatedAt.toLocaleString() }}</span>
        </div>
      </div>
      <p v-else class="no-books-message">작성 중인 책이 없습니다.</p>
      <button @click="step = 'initial'" class="btn btn-secondary mt-4">뒤로가기</button>
    </section>

    <!-- Step 3: Creation Workspace -->
    <section v-if="step === 'workspace'" class="workspace-section">
      <div class="workspace-header">
        <input type="text" v-model="currentBook.title" placeholder="책 제목을 입력하세요" class="form-control book-title-input">
        <div class="workspace-actions">
          <button @click="saveDraft" class="btn btn-outline-primary">임시 저장</button>
          <button @click="publishBook" class="btn btn-primary">발행하기</button>
          <button @click="resetWorkspace" class="btn btn-danger">나가기</button>
        </div>
      </div>

      <div class="workspace-main">
        <!-- Episode List -->
        <div class="episode-list-container">
          <h3 class="episode-list-title">에피소드</h3>
          <ul class="episode-list">
            <li v-for="(episode, index) in currentBook.episodes" :key="index" @click="selectEpisode(index)" :class="{ active: index === currentEpisodeIndex }">
              {{ `에피소드 ${index + 1}` }}
            </li>
          </ul>
          <button @click="addEpisode" class="btn btn-success btn-sm add-episode-btn">에피소드 추가</button>
        </div>

        <!-- Editor/Interaction Area -->
        <div class="editor-area">
          <div v-if="currentEpisode">
            <div class="page-number-container">
              <span class="page-number">{{ totalCurrentPage }}p</span>
            </div>
            <textarea v-model="currentEpisode.content" class="form-control episode-content-editor" placeholder="이곳에 내용을 입력하거나 AI와 대화하세요..."></textarea>
            <div class="ai-interaction-panel">
              <button @click="startAiInterview" class="btn btn-info">AI 인터뷰 시작</button>
              <button @click="uploadAudio" class="btn btn-warning">녹음 파일 업로드</button>
            </div>
          </div>
          <p v-else class="no-episode-message">왼쪽에서 에피소드를 선택하거나 새로 추가해주세요.</p>
        </div>
      </div>
    </section>

    <!-- Modals and Overlays can be added here -->

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, defineProps, watch } from 'vue';
import { useRouter } from 'vue-router';

// --- Props ---
const props = defineProps({
  bookId: { type: String, default: null }
});

// --- Interfaces ---
interface Episode {
  content: string;
}

interface Book {
  id: string;
  title: string;
  authorId: string;
  isPublished: boolean;
  episodes: Episode[];
  createdAt: Date;
  updatedAt: Date;
  likes?: number;
  views?: number;
}

const totalCurrentPage = computed(() => {
  if (!currentBook.value.episodes) return 0;

  let totalPagesBefore = 0;
  for (let i = 0; i < currentEpisodeIndex.value; i++) {
    const episodeContent = currentBook.value.episodes[i].content;
    totalPagesBefore += Math.ceil(episodeContent.length / 800);
  }

  return totalPagesBefore + 1;
});

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
  // Add some published books to dummy data for editing purposes
  {
    id: 'mybook1',
    title: '나의 어린 시절 이야기',
    authorId: 'dummyUser1',
    isPublished: true,
    episodes: [
      { content: '어릴 적 살던 동네의 골목길은 언제나 모험의 시작이었다.' },
      { content: '할머니의 따뜻한 손길과 맛있는 음식은 잊을 수 없는 기억이다.' },
      { content: '친구들과 함께 뛰어놀던 운동장은 우리만의 작은 세상이었다.' },
    ],
    createdAt: new Date('2023-01-01T10:00:00Z'),
    updatedAt: new Date('2023-01-01T10:00:00Z'),
  },
  {
    id: 'mybook2',
    title: '꿈을 향한 도전',
    authorId: 'dummyUser1',
    isPublished: true,
    episodes: [
      { content: '새로운 목표를 설정하고 첫 발을 내디뎠을 때의 설렘.' },
      { content: '수많은 실패와 좌절 속에서도 포기하지 않았던 이유.' },
      { content: '마침내 꿈을 이뤘을 때의 감격과 새로운 시작.' },
    ],
    likes: 30,
    views: 200,
    createdAt: new Date('2023-01-01T10:00:00Z'),
    updatedAt: new Date('2023-01-01T10:00:00Z'),
  },
  {
    id: 'mybook3',
    title: '여행의 기록',
    authorId: 'dummyUser1',
    isPublished: true,
    episodes: [
      { content: '낯선 도시에서 길을 잃었을 때의 당황스러움과 새로운 발견.' },
      { content: '현지인들과의 소통을 통해 배운 삶의 지혜.' },
      { content: '여행은 나를 성장시키는 가장 좋은 방법이다.' },
    ],
    likes: 25,
    views: 180,
    createdAt: new Date('2023-01-01T10:00:00Z'),
    updatedAt: new Date('2023-01-01T10:00:00Z'),
  },
  {
    id: 'mybook4',
    title: '개발자의 삶',
    authorId: 'dummyUser1',
    isPublished: true,
    episodes: [
      { content: '새로운 기술을 배우는 것은 언제나 즐겁다.' },
      { content: '버그와의 사투는 개발자의 숙명.' },
      { content: '코드 한 줄이 세상을 바꿀 수 있다는 믿음.' },
    ],
    likes: 10,
    views: 50,
    createdAt: new Date('2023-01-01T10:00:00Z'),
    updatedAt: new Date('2023-01-01T10:00:00Z'),
  },
];

// --- Router ---
const router = useRouter();

// --- Reactive State ---
const step = ref('initial'); // 'initial', 'load', 'workspace'
const inProgressBooks = ref<Book[]>([]);
const currentBook = ref<Partial<Book>>({});
const currentEpisodeIndex = ref(-1);

// --- Computed Properties ---
const currentEpisode = computed(() => {
  if (currentBook.value.episodes && currentEpisodeIndex.value !== -1) {
    return currentBook.value.episodes[currentEpisodeIndex.value];
  }
  return null;
});

// --- Functions ---
function startNewBook() {
  currentBook.value = {
    id: `new_book_${Date.now()}`,
    title: '무제',
    authorId: 'dummyUser1', // Assuming a dummy current user
    isPublished: false,
    episodes: [{ content: '' }],
    createdAt: new Date(),
    updatedAt: new Date(),
  };
  currentEpisodeIndex.value = 0;
  step.value = 'workspace';
}

function goToGroupCreate() {
  router.push('/group-book-creation');
}

function fetchInProgressBooks() {
  // In a real app, this would fetch books from a backend API
  inProgressBooks.value = DUMMY_IN_PROGRESS_BOOKS.filter(book => !book.isPublished);
}

function loadBook(book: Book) {
  currentBook.value = JSON.parse(JSON.stringify(book)); // Deep copy
  currentEpisodeIndex.value = 0;
  step.value = 'workspace';
}

function selectEpisode(index: number) {
  currentEpisodeIndex.value = index;
}

function addEpisode() {
  if (currentBook.value.episodes) {
    currentBook.value.episodes.push({ content: '' });
    currentEpisodeIndex.value = currentBook.value.episodes.length - 1;
  }
}

async function saveDraft() {
  if (!currentBook.value.title) {
    alert('책 제목을 입력해주세요.');
    return;
  }

  // Simulate saving to a dummy data source
  currentBook.value.updatedAt = new Date();
  const existingIndex = DUMMY_IN_PROGRESS_BOOKS.findIndex(b => b.id === currentBook.value.id);
  if (existingIndex !== -1) {
    DUMMY_IN_PROGRESS_BOOKS[existingIndex] = JSON.parse(JSON.stringify(currentBook.value)) as Book;
  } else {
    DUMMY_IN_PROGRESS_BOOKS.push(JSON.parse(JSON.stringify(currentBook.value)) as Book);
  }
  alert('임시 저장되었습니다. (더미)');
}

async function publishBook() {
  if (!confirm('책을 발행하시겠습니까? 발행 후에는 일부 내용만 수정할 수 있습니다.')) return;
  
  await saveDraft(); // Save one last time before publishing
  if (!currentBook.value.id) return;

  // Simulate publishing
  const bookToPublish = DUMMY_IN_PROGRESS_BOOKS.find(b => b.id === currentBook.value.id);
  if (bookToPublish) {
    bookToPublish.isPublished = true;
    // In a real app, this would move the book to a 'published' collection or update its status
    alert('책이 성공적으로 발행되었습니다! (더미)');
    router.push(`/book-detail/${currentBook.value.id}`);
  }
}

function resetWorkspace() {
  if (confirm('작업을 종료하시겠습니까? 저장하지 않은 내용은 사라집니다.')) {
    currentBook.value = {};
    currentEpisodeIndex.value = -1;
    step.value = 'initial';
  }
}

function startAiInterview() {
  alert('AI 인터뷰 기능은 현재 개발 중입니다. (더미)');
}

function uploadAudio() {
  alert('녹음 파일 업로드 기능은 현재 개발 중입니다. (더미)');
}

// --- Lifecycle Hooks ---
onMounted(() => {
  if (props.bookId) {
    const foundBook = DUMMY_IN_PROGRESS_BOOKS.find(b => b.id === props.bookId);
    if (foundBook) {
      loadBook(foundBook);
    } else {
      console.warn(`Book with ID ${props.bookId} not found in dummy data.`);
      // Optionally, redirect or show an error message
    }
  } else {
    fetchInProgressBooks();
  }
});

// Watch for changes in bookId prop (e.g., if navigating from one book edit to another)
watch(() => props.bookId, (newBookId) => {
  if (newBookId) {
    const foundBook = DUMMY_IN_PROGRESS_BOOKS.find(b => b.id === newBookId);
    if (foundBook) {
      loadBook(foundBook);
    } else {
      console.warn(`Book with ID ${newBookId} not found in dummy data.`);
    }
  } else {
    // If bookId becomes null, reset to initial state or new book creation
    step.value = 'initial';
    currentBook.value = {};
    currentEpisodeIndex.value = -1;
    fetchInProgressBooks();
  }
});
</script>

<style scoped>
.create-book-page {
  padding: 80px 2rem 2rem;
  background-color: #F5F5DC;
  color: #3D2C20;
  min-height: calc(100vh - 56px);
}

.section-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  color: #3D2C20;
}

.section-subtitle {
  font-size: 1.2rem;
  color: #5C4033;
  margin-bottom: 3rem;
}

/* Initial Choice Section */
.initial-choice-section {
  max-width: 1200px;
  margin: 0 auto;
}

.choice-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2rem;
}

.choice-card {
  background: #fff;
  border-radius: 12px;
  padding: 2.5rem;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.choice-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.card-icon {
  font-size: 3rem;
  color: #B8860B;
  margin-bottom: 1rem;
}

.card-title {
  font-size: 1.75rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.card-description {
  color: #5C4033;
}

/* Load Book Section */
.load-book-section {
  max-width: 700px;
  margin: 0 auto;
  text-align: center;
}

.book-list-item {
  background: #fff;
  padding: 1rem 1.5rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.2s;
}

.book-list-item:hover {
  background-color: #EAE0D5;
}

.book-title {
  font-weight: 600;
}

.book-last-saved {
  font-size: 0.9rem;
  color: #8B4513;
}

.no-books-message {
  padding: 2rem;
  background: #fff;
  border-radius: 8px;
}

/* Workspace Section */
.workspace-section {
  max-width: 1200px;
  margin: 0 auto;
}

.workspace-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  gap: 1rem;
}

.book-title-input {
  flex-grow: 1;
  font-size: 1.5rem;
  font-weight: bold;
  padding: 0.5rem 1rem;
}

.workspace-actions {
  display: flex;
  gap: 0.5rem;
}

.workspace-main {
  display: flex;
  gap: 2rem;
  height: calc(100vh - 280px);
}

.episode-list-container {
  flex: 0 0 250px;
  background: #fff;
  border-radius: 8px;
  padding: 1rem;
  overflow-y: auto;
}

.episode-list-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #EAE0D5;
}

.episode-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.episode-list li {
  padding: 0.75rem;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.episode-list li:hover {
  background-color: #EAE0D5;
}

.episode-list li.active {
  background-color: #B8860B;
  color: #fff;
  font-weight: bold;
}

.add-episode-btn {
  width: 100%;
  margin-top: 1rem;
}

.editor-area {
  flex-grow: 1;
  background: #fff;
  border-radius: 8px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
}

.episode-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.page-number-container {
  position: absolute;
  bottom: 2rem;
  left: 0;
  right: 0;
  text-align: center;
}

.page-number {
  font-size: 1rem;
  font-weight: normal;
}

.episode-content-editor {
  flex-grow: 1;
  width: 100%;
  min-height: 500px;
  resize: none;
  border: none;
  box-shadow: none;
}

.ai-interaction-panel {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #EAE0D5;
  display: flex;
  gap: 1rem;
}

.no-episode-message {
  text-align: center;
  margin: auto;
  color: #8B4513;
}

@media (max-width: 992px) {
  .choice-cards {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .choice-cards {
    grid-template-columns: 1fr;
  }
  .workspace-main {
    flex-direction: column;
    height: auto;
  }
  .episode-list-container {
    flex: 0 0 auto;
    height: 200px;
  }
}
</style>