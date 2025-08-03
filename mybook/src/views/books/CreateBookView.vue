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
        <button @click="step = 'initial'" class="btn btn-outline mt-4">
          <i class="bi bi-arrow-left"></i> 뒤로가기
        </button>
      </section>

      <section v-else-if="step === 'workspace'" class="workspace-section">
        <div class="workspace-header">
          <input type="text" v-model="currentBook.title" placeholder="매력적인 책 제목을 지어주세요" class="book-title-input">
          <div class="workspace-actions">
            <button @click="saveDraft" class="btn btn-outline">
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
              <li v-for="(episode, index) in currentBook.episodes" :key="index" @click="selectEpisode(index)"
                :class="{ active: index === currentEpisodeIndex }">
                <span>{{ `에피소드 ${index + 1}` }}</span>
              </li>
            </ul>
          </div>

          <div class="editor-area">
            <div v-if="currentEpisode" class="editor-content">
              <textarea v-model="currentEpisode.content" class="episode-content-editor"
                placeholder="이곳에 당신의 이야기를 적어내려가 보세요..."></textarea>
              <div class="editor-footer">
                <div class="ai-interaction-panel">
                  <button @click="startAiInterview" class="btn btn-outline">
                    <i class="bi bi-mic"></i> AI 인터뷰 시작
                  </button>
                  <button @click="uploadAudio" class="btn btn-outline">
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
    authorId: 'dummyUser1',
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
  inProgressBooks.value = DUMMY_IN_PROGRESS_BOOKS.filter(book => !book.isPublished);
}

function loadBook(book: Book) {
  currentBook.value = JSON.parse(JSON.stringify(book));
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
  alert('임시 저장되었습니다.');
}

async function publishBook() {
  if (!currentBook.value.title) {
    alert('책 제목을 입력해주세요.');
    return;
  }
  if (!confirm('책을 발행하시겠습니까?')) return;
  alert('책이 성공적으로 발행되었습니다!');
  router.push(`/book-detail/${currentBook.value.id}`);
}

function resetWorkspace() {
  if (confirm('작업을 종료하시겠습니까?')) {
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
  --shadow-color: rgba(0, 0, 0, 0.06);
  --paper-color: #FDFDF5;
}

.create-book-page {
  padding: 80px 2rem 2rem;
  background-color: var(--background-color);
  color: var(--primary-text-color);
  min-height: calc(100vh - 56px);
  font-family: 'Pretendard', sans-serif;
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

/* --- Transition Animation --- */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* --- 초기 선택 화면 --- */
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
  background: var(--surface-color);
  border-radius: 12px;
  padding: 2.5rem;
  border: 1px solid var(--border-color);
  box-shadow: 0 4px 15px var(--shadow-color);
  cursor: pointer;
  text-align: center;
  transition: box-shadow 0.3s, border-color 0.3s;
}

.choice-card:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border-color: var(--accent-color);
}

.card-icon {
  font-size: 2.5rem;
  color: var(--accent-color);
  margin-bottom: 1.5rem;
  line-height: 1;
}

.card-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
}

.card-description {
  color: var(--secondary-text-color);
  line-height: 1.6;
}

/* --- 이어쓰기 목록 --- */
.load-book-section {
  max-width: 700px;
  margin: 0 auto;
  text-align: center;
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

/* --- 작업 공간 (Workspace) --- */
.workspace-section {
  max-width: 100%;
  margin: 0 auto;
}

.workspace-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0 auto 2rem auto;
  max-width: 1400px;
  gap: 1.5rem;
}

.book-title-input {
  flex-grow: 1;
  font-family: 'Noto Serif KR', serif;
  font-size: 2rem;
  font-weight: 700;
  color: var(--primary-text-color);
  border: none;
  background: transparent;
  outline: none;
  padding: 0.5rem 0;
  border-bottom: 2px solid transparent;
  transition: border-color 0.3s;
}

.book-title-input:focus {
  border-bottom-color: var(--accent-color);
}

.book-title-input::placeholder {
  color: #b0a89f;
}

.workspace-actions {
  display: flex;
  gap: 0.75rem;
}

/* [수정] 모든 버튼의 기본 스타일 정의 */
.btn,
.btn-outline,
.btn-subtle {
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
}

.btn-primary {
  border: 1px solid var(--accent-color);
  background-color: #6a341a;
  border-color: #6a341a;

  color: #fff;
  padding: 0.6rem 1.2rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  font-size: 0.95rem;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.btn:hover,
.btn-outline:hover {
  background-color: #FDF8E7;
  border-color: var(--accent-color);
}


.btn-primary:hover {
  background-color: #6a341a;
  border-color: #6a341a;
}

.btn-subtle {
  border: none;
  background: none;
  color: var(--secondary-text-color);
}

.btn-subtle:hover {
  background-color: #e9e4d9;
}


.workspace-main {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 2rem;
  height: calc(100vh - 250px);
  max-width: 1400px;
  margin: 0 auto;
}

.episode-list-container {
  background: var(--surface-color);
  border-radius: 8px;
  border: 1px solid var(--border-color);
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
  border-bottom: 1px solid var(--border-color);
}

.episode-list-title {
  font-size: 1rem;
  font-weight: 700;
  color: #5C4033;
  margin: 0;
}

.btn-add-episode {
  background: none;
  border: 1px dashed var(--border-color);
  color: var(--secondary-text-color);
  border-radius: 50%;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  transition: all 0.2s;
}

.btn-add-episode:hover {
  border-color: var(--accent-color);
  color: var(--accent-color);
  transform: rotate(90deg);
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
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  color: #555;
  transition: background-color 0.2s, color 0.2s;
  border-left: 3px solid transparent;
}

.episode-list li:hover {
  background-color: #f8f6f2;
}

.episode-list li.active {
  background-color: #f8f6f2;
  color: var(--primary-text-color);
  font-weight: 700;
  border-left: 3px solid var(--accent-color);
}

.editor-area {
  background: var(--paper-color);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.editor-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.episode-content-editor {
  flex-grow: 1;
  width: 100%;
  padding: 2.5rem 3rem;
  resize: none;
  border: none;
  background: transparent;
  outline: none;
  font-family: 'Noto Serif KR', serif;
  font-size: 1.1rem;
  line-height: 1.9;
  color: var(--primary-text-color);
}

.episode-content-editor::placeholder {
  color: #b0a89f;
}

.editor-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  border-top: 1px solid var(--border-color);
  background-color: var(--surface-color);
}

.ai-interaction-panel {
  display: flex;
  gap: 1rem;
}

.page-number {
  font-size: 0.9rem;
  color: var(--secondary-text-color);
  font-weight: 500;
}

.no-episode-message {
  text-align: center;
  margin: auto;
  color: #b0a89f;
}

.no-episode-message i {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.no-books-message {
  padding: 2rem;
}
</style>