<template>
  <div class="create-book-page">
    <Transition name="fade" mode="out-in">
      <section v-if="step === 'initial'" class="initial-choice-section text-center">
        <h2 class="section-title">새로운 이야기의 시작</h2>
        <p class="section-subtitle">AI와 함께, 또는 친구들과 함께 세상에 단 하나뿐인 당신의 책을 만들어보세요.</p>
        <div class="choice-cards">
          <div class="choice-card" @click="startNewBook">
            <div class="card-icon"><i class="bi bi-stars"></i></div>
            <h3 class="card-title">새 책 만들기</h3>
            <p class="card-description">AI의 도움을 받아 자서전, 일기, 소설 등 새로운 이야기를 시작합니다.</p>
          </div>
          <div class="choice-card" @click="goToGroupCreate">
            <div class="card-icon"><i class="bi bi-people-fill"></i></div>
            <h3 class="card-title">그룹 책 만들기</h3>
            <p class="card-description">친구, 가족과 함께 교환일기나 여행기 등 특별한 추억을 만듭니다.</p>
          </div>
          <div class="choice-card" @click="step = 'load'">
            <div class="card-icon"><i class="bi bi-arrow-down-circle"></i></div>
            <h3 class="card-title">이어 쓰기</h3>
            <p class="card-description">작성 중인 책을 불러와 중단했던 지점부터 다시 시작합니다.</p>
          </div>
        </div>
      </section>

      <section v-else-if="step === 'load'" class="load-book-section">
        <h2 class="section-title">작성 중인 책 목록</h2>
        <p class="section-subtitle">계속해서 이야기를 만들어갈 책을 선택하세요.</p>
        <div v-if="inProgressBooks.length > 0" class="book-list">
          <div v-for="book in inProgressBooks" :key="book.id" class="book-list-item" @click="loadBook(book)">
            <div class="book-info">
              <span class="book-title">{{ book.title }}</span>
              <span class="book-last-saved">마지막 저장: {{ new Date(book.updatedAt).toLocaleString() }}</span>
            </div>
            <i class="bi bi-chevron-right"></i>
          </div>
        </div>
        <p v-else class="no-books-message">작성 중인 책이 없습니다. '새 책 만들기'로 시작해보세요!</p>
        <button @click="step = 'initial'" class="btn btn-secondary mt-4">
          <i class="bi bi-arrow-left"></i> 뒤로가기
        </button>
      </section>

      <section v-else-if="step === 'workspace'" class="workspace-section">
        <div class="workspace-header">
          <input type="text" v-model="currentBook.title" placeholder="매력적인 책 제목을 지어주세요" class="book-title-input">
          <div class="workspace-actions">
            <button @click="saveDraft" class="btn btn-outline-secondary">
              <i class="bi bi-cloud-arrow-down"></i> 임시 저장
            </button>
            <button @click="publishBook" class="btn btn-primary">
              <i class="bi bi-send-check"></i> 발행하기
            </button>
            <button @click="resetWorkspace" class="btn btn-subtle">
              <i class="bi bi-x"></i> 나가기
            </button>
          </div>
        </div>

        <div class="workspace-main">
          <div class="episode-list-container">
            <div class="episode-list-header">
              <h3 class="episode-list-title">에피소드</h3>
              <button @click="addEpisode" class="btn-add-episode" title="에피소드 추가">
                <i class="bi bi-plus-lg"></i>
              </button>
            </div>
            <ul class="episode-list">
              <li v-for="(episode, index) in currentBook.episodes" :key="index" @click="selectEpisode(index)" :class="{ active: index === currentEpisodeIndex }">
                <span>{{ `에피소드 ${index + 1}` }}</span>
              </li>
            </ul>
          </div>

          <div class="editor-area">
            <div v-if="currentEpisode" class="editor-content">
              <textarea v-model="currentEpisode.content" class="episode-content-editor" placeholder="이곳에 당신의 이야기를 적어내려가 보세요..."></textarea>
              <div class="editor-footer">
                <div class="ai-interaction-panel">
                  <button @click="startAiInterview" class="btn btn-secondary">
                    <i class="bi bi-mic"></i> AI 인터뷰 시작
                  </button>
                  <button @click="uploadAudio" class="btn btn-secondary">
                    <i class="bi bi-paperclip"></i> 녹음 파일 업로드
                  </button>
                </div>
                <span class="page-number">{{ totalCurrentPage }} / {{ totalPages }} 페이지</span>
              </div>
            </div>
            <div v-else class="no-episode-message">
              <i class="bi bi-journal-plus"></i>
              <p>왼쪽에서 에피소드를 선택하거나<br>새 에피소드를 추가해주세요.</p>
            </div>
          </div>
        </div>
      </section>
    </Transition>
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

// --- Dummy Data ---
// (기존 더미 데이터와 동일하므로 생략)
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

const calculatePage = (content: string) => Math.max(1, Math.ceil((content?.length || 0) / 800));

const totalPages = computed(() => {
  if (!currentBook.value.episodes) return 0;
  return currentBook.value.episodes.reduce((sum, episode) => sum + calculatePage(episode.content), 0);
});

const totalCurrentPage = computed(() => {
  if (!currentBook.value.episodes || currentEpisodeIndex.value < 0) return 0;

  let pagesBefore = 0;
  for (let i = 0; i < currentEpisodeIndex.value; i++) {
    pagesBefore += calculatePage(currentBook.value.episodes[i].content);
  }

  const currentPageInEpisode = calculatePage(currentBook.value.episodes[currentEpisodeIndex.value].content);
  return pagesBefore + currentPageInEpisode;
});

// --- Functions ---
function startNewBook() {
  currentBook.value = {
    id: `new_book_${Date.now()}`,
    title: '',
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
  currentEpisodeIndex.value = book.episodes.length > 0 ? 0 : -1;
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
  alert('임시 저장되었습니다.');
}

async function publishBook() {
  if (!currentBook.value.title) {
    alert('책 제목을 입력해주세요.');
    return;
  }
  if (!confirm('책을 발행하시겠습니까? 발행 후에는 일부 내용만 수정할 수 있습니다.')) return;
  
  await saveDraft(); 
  if (!currentBook.value.id) return;

  // Simulate publishing
  const bookToPublish = DUMMY_IN_PROGRESS_BOOKS.find(b => b.id === currentBook.value.id);
  if (bookToPublish) {
    bookToPublish.isPublished = true;
    alert('책이 성공적으로 발행되었습니다!');
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
  alert('AI 인터뷰 기능은 현재 개발 중입니다.');
}

function uploadAudio() {
  alert('녹음 파일 업로드 기능은 현재 개발 중입니다.');
}

// --- Lifecycle Hooks ---
onMounted(() => {
  if (props.bookId) {
    const foundBook = DUMMY_IN_PROGRESS_BOOKS.find(b => b.id === props.bookId);
    if (foundBook) {
      loadBook(foundBook);
    }
  } else {
    fetchInProgressBooks();
  }
});

watch(() => props.bookId, (newBookId) => {
  if (newBookId) {
    const foundBook = DUMMY_IN_PROGRESS_BOOKS.find(b => b.id === newBookId);
    if (foundBook) loadBook(foundBook);
  } else {
    step.value = 'initial';
    currentBook.value = {};
    currentEpisodeIndex.value = -1;
    fetchInProgressBooks();
  }
});
</script>

<style scoped>
/* CSS 변수를 사용해 색상 관리 용이성 증대 */
:root {
  --background-color: #f8f9fa;
  --surface-color: #ffffff;
  --primary-text-color: #212529;
  --secondary-text-color: #6c757d;
  --accent-color: #0d6efd;
  --border-color: #dee2e6;
  --shadow-color: rgba(0, 0, 0, 0.05);
}

.create-book-page {
  padding: 80px 2rem 2rem;
  background-color: var(--background-color, #f8f9fa);
  color: var(--primary-text-color, #212529);
  min-height: calc(100vh - 56px);
  font-family: 'Pretendard', sans-serif;
}

.section-title {
  font-size: 2.8rem;
  font-weight: 800;
  margin-bottom: 0.75rem;
}

.section-subtitle {
  font-size: 1.25rem;
  color: var(--secondary-text-color, #6c757d);
  margin-bottom: 4rem;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

/* Transition Animation */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Initial Choice Section */
.initial-choice-section {
  max-width: 1200px;
  margin: 0 auto;
}

.choice-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2.5rem;
}

.choice-card {
  background: var(--surface-color, #fff);
  border-radius: 16px;
  padding: 2.5rem;
  border: 1px solid var(--border-color, #dee2e6);
  box-shadow: 0 4px 15px var(--shadow-color, rgba(0,0,0,0.05));
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s, border-color 0.3s;
}

.choice-card:hover {
  transform: translateY(-12px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);
  border-color: var(--accent-color, #0d6efd);
}

.card-icon {
  font-size: 2.5rem;
  color: var(--accent-color, #0d6efd);
  margin-bottom: 1.5rem;
  line-height: 1;
}

.card-title {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 0.75rem;
}

.card-description {
  color: var(--secondary-text-color, #6c757d);
  line-height: 1.6;
}

/* Load Book Section */
.load-book-section {
  max-width: 700px;
  margin: 0 auto;
  text-align: center;
}

.book-list-item {
  background: var(--surface-color, #fff);
  padding: 1.25rem 2rem;
  border-radius: 12px;
  margin-bottom: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  border: 1px solid var(--border-color, #dee2e6);
  text-align: left;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.book-list-item:hover {
  border-color: var(--accent-color, #0d6efd);
  box-shadow: 0 4px 15px var(--shadow-color, rgba(0,0,0,0.05));
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
  color: var(--secondary-text-color, #6c757d);
}

.book-list-item i {
  color: var(--secondary-text-color, #6c757d);
  transition: color 0.2s;
}
.book-list-item:hover i {
  color: var(--accent-color, #0d6efd);
}


/* Workspace Section */
.workspace-section {
  max-width: 1400px;
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
  font-size: 2rem;
  font-weight: 700;
  border: none;
  background: transparent;
  outline: none;
  padding: 0.5rem 0;
  border-bottom: 2px solid transparent;
  transition: border-color 0.3s;
}
.book-title-input:focus {
  border-bottom-color: var(--accent-color, #0d6efd);
}

.workspace-actions {
  display: flex;
  gap: 0.75rem;
}

.workspace-actions .btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  font-weight: 600;
}
.btn-subtle {
    background: transparent;
    color: var(--secondary-text-color);
    border: none;
}
.btn-subtle:hover {
    background: #e9ecef;
}

.workspace-main {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 2rem;
  height: calc(100vh - 250px);
}

.episode-list-container {
  background: var(--surface-color, #fff);
  border-radius: 12px;
  border: 1px solid var(--border-color, #dee2e6);
  padding: 1rem;
  display: flex;
  flex-direction: column;
}

.episode-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem;
  margin-bottom: 0.5rem;
  border-bottom: 1px solid var(--border-color, #dee2e6);
}

.episode-list-title {
  font-size: 1.1rem;
  font-weight: 700;
  margin: 0;
}

.btn-add-episode {
  background: none;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  transition: background-color 0.2s, color 0.2s;
}
.btn-add-episode i {
  color: var(--secondary-text-color, #6c757d); /* 아이콘 색상을 원래대로 되돌림 */
}
.btn-add-episode:hover {
  background-color: #e9ecef;
  color: var(--accent-color, #0d6efd);
}

.episode-list {
  list-style: none;
  padding: 0;
  margin: 0;
  overflow-y: auto;
  flex-grow: 1;
}

.episode-list li {
  padding: 0.8rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s, color 0.2s;
}

.episode-list li:hover {
  background-color: #e9ecef;
}

.episode-list li.active {
  background-color: var(--accent-color, #0d6efd);
  color: #fff;
  font-weight: 600;
}

.editor-area {
  background: var(--surface-color, #fff);
  border-radius: 12px;
  border: 1px solid var(--border-color, #dee2e6);
  display: flex;
  flex-direction: column;
}

.editor-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.episode-content-editor {
  flex-grow: 1;
  width: 100%;
  padding: 1.5rem 2rem;
  resize: none;
  border: none;
  background: transparent;
  outline: none;
  font-size: 1.1rem;
  line-height: 1.8;
  color: var(--primary-text-color, #212529);
}

.editor-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  border-top: 1px solid var(--border-color, #dee2e6);
}

.ai-interaction-panel {
  display: flex;
  gap: 1rem;
}

.page-number {
  font-size: 0.9rem;
  color: var(--secondary-text-color, #6c757d);
  font-weight: 500;
}

.no-episode-message {
  text-align: center;
  margin: auto;
  color: var(--secondary-text-color, #6c757d);
}

.no-episode-message i {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.no-books-message {
  padding: 2rem;
}

 @media (max-width: 992px) {
  .workspace-main {
    grid-template-columns: 220px 1fr;
  }
}

 @media (max-width: 768px) {
  .workspace-main {
    grid-template-columns: 1fr;
    height: auto;
  }
  .episode-list-container {
    height: 200px;
  }
  .workspace-header {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>