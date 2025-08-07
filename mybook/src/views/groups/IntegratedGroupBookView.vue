<template>
  <div class="integrated-group-book-page">
    <div v-if="book" class="book-container">
      <!-- Book Header -->
      <header class="book-header">
        <div class="book-cover">
          <img :src="book.coverUrl || 'https://via.placeholder.com/150x220/B8860B/FFFFFF?text=Group+Book'" alt="Group Book Cover">
        </div>
        <div class="book-meta">
          <h1 class="book-title">{{ book.title }}</h1>
          <p class="book-authors">참여 작가: {{ book.authorName || '정보 없음' }}</p>
          <p class="book-summary">{{ book.summary || '요약 정보가 없습니다.' }}</p>
          <div class="book-tags">
            <span v-for="tag in book.keywords" :key="tag" class="tag">#{{ tag }}</span>
          </div>
          <p class="book-updated-date">업데이트 날짜: {{ new Date(book.updatedAt).toLocaleDateString() }}</p>
          <div class="book-actions">
            <button @click="requestEdit" class="btn btn-primary">
              <i class="bi bi-pencil-square"></i> 통합 책 수정 요청
            </button>
          </div>
        </div>
      </header>

      <!-- Episode Viewer -->
      <main class="episode-viewer">
        <div class="episode-navigation">
          <button @click="prevEpisode" :disabled="currentEpisodeIndex === 0" class="btn btn-nav">이전</button>
          <div class="page-number-container">
            <span class="page-number">{{ totalCurrentPage }}p</span>
          </div>
          <button @click="nextEpisode" :disabled="currentEpisodeIndex >= book.episodes.length - 1" class="btn btn-nav">다음</button>
        </div>
        <div class="episode-content">
          <p>{{ currentEpisodeContent }}</p>
        </div>
      </main>

      <!-- Comments Section -->
      <section class="comments-section">
        <h3 class="comments-title">댓글</h3>
        <div class="comment-input-area">
          <textarea v-model="newComment" placeholder="댓글을 입력하세요..." class="form-control"></textarea>
          <button @click="addComment" class="btn btn-primary">등록</button>
        </div>
        <div class="comment-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <p class="comment-author"><strong>{{ comment.authorName || '익명' }}</strong> <span class="comment-date">{{ comment.createdAt.toLocaleString() }}</span></p>
            <p class="comment-text">{{ comment.text }}</p>
          </div>
        </div>
      </section>
    </div>
    <div v-else class="loading-message">
      <p>그룹 통합 책 정보를 불러오는 중입니다...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

// --- Interfaces ---
interface Episode {
  content: string;
}

interface Book {
  id: string;
  title: string;
  authorId: string;
  authorName?: string;
  summary?: string;
  coverUrl?: string;
  keywords?: string[];
  episodes: Episode[];
  likes: number;
  views: number;
  updatedAt: Date;
}

interface Comment {
  id: string;
  authorId: string;
  authorName?: string;
  text: string;
  createdAt: Date;
}

const totalCurrentPage = computed(() => {
  if (!book.value) return 0;

  let totalPagesBefore = 0;
  for (let i = 0; i < currentEpisodeIndex.value; i++) {
    const episodeContent = book.value.episodes[i].content;
    totalPagesBefore += Math.ceil(episodeContent.length / 800);
  }

  return totalPagesBefore + 1;
});

// --- Dummy Data ---
const DUMMY_INTEGRATED_BOOKS: Book[] = [
  {
    id: 'integrated_book_1',
    title: '공작소의 첫 작품',
    authorId: 'group_dummy_1',
    authorName: '공동 집필',
    summary: '우리들의 이야기 공작소에서 함께 만든 첫 번째 통합 책입니다. 각자의 개성이 담긴 에피소드들이 조화롭게 어우러져 새로운 이야기를 만들어냅니다.',
    coverUrl: 'https://via.placeholder.com/150x220/B8860B/FFFFFF?text=Integrated1',
    keywords: ['그룹', '협업', '이야기', '창작'],
    episodes: [
      { content: '그룹원들과의 첫 만남, 설렘 가득한 시작. 각자의 아이디어를 공유하며 책의 큰 그림을 그렸다.' },
      { content: '아이디어 브레인스토밍, 창의적인 생각들이 쏟아져 나왔다. 때로는 격렬한 토론도 있었지만, 모두가 만족하는 방향으로 나아갔다.' },
      { content: '함께 만들어가는 즐거움, 그리고 완성의 보람. 서로의 글을 읽고 피드백하며 더욱 풍성한 이야기가 되었다.' },
      { content: '마침내 완성된 우리의 첫 작품. 이 책은 단순한 글이 아니라, 우리의 우정과 열정의 증거이다.' },
    ],
    likes: 50,
    views: 300,
    updatedAt: new Date('2024-07-20T10:00:00Z'),
  },
  {
    id: 'integrated_book_2',
    title: '조선 왕조 비사',
    authorId: 'group_dummy_2',
    authorName: '공동 집필',
    summary: '역사 탐험대에서 함께 파헤친 조선 왕조의 숨겨진 이야기들. 교과서에서는 배울 수 없었던 흥미로운 사실들을 담았습니다.',
    coverUrl: 'https://via.placeholder.com/150x220/B8860B/FFFFFF?text=Integrated2',
    keywords: ['역사', '조선', '비사', '탐험'],
    episodes: [
      { content: '왕실의 비밀, 역사의 뒤안길에 숨겨진 진실. 사료를 통해 재구성한 흥미로운 사건들.' },
      { content: '흥미로운 인물들, 그들의 삶과 업적. 우리가 몰랐던 영웅과 악인들의 진짜 이야기.' },
      { content: '조선 시대의 일상, 백성들의 삶은 어떠했을까? 생생한 기록을 통해 엿보는 그들의 하루.' },
    ],
    likes: 40,
    views: 280,
    updatedAt: new Date('2024-06-15T14:00:00Z'),
  },
];

const DUMMY_COMMENTS: Comment[] = [
  { id: 'ic1', authorId: 'user_dummy_3', authorName: '그룹원A', text: '정말 멋진 통합 책이네요! 다들 수고 많으셨습니다.', createdAt: new Date('2024-07-28T10:00:00Z') },
  { id: 'ic2', authorId: 'user_dummy_4', authorName: '그룹원B', text: '다음 작품도 기대됩니다! 아이디어가 정말 좋아요.', createdAt: new Date('2024-07-28T11:30:00Z') },
];

// --- Router ---
const route = useRoute();
const router = useRouter();
const bookId = computed(() => route.params.id as string);

// --- Reactive State ---
const book = ref<Book | null>(null);
const comments = ref<Comment[]>([]);
const currentEpisodeIndex = ref(0);
const newComment = ref('');

// --- Computed Properties ---
const currentEpisodeContent = computed(() => {
  if (book.value && book.value.episodes && book.value.episodes[currentEpisodeIndex.value]) {
    return book.value.episodes[currentEpisodeIndex.value].content;
  }
  return '에피소드 내용이 없습니다.';
});

// --- Functions ---
function fetchBookData() {
  const foundBook = DUMMY_INTEGRATED_BOOKS.find(b => b.id === bookId.value);
  if (foundBook) {
    book.value = { ...foundBook };
    // Simulate view count increment
    if (book.value) book.value.views = (book.value.views || 0) + 1;
  } else {
    console.error('Integrated book not found for ID:', bookId.value);
    book.value = null;
  }
}

function fetchComments() {
  // For simplicity, all dummy comments are shown for any integrated book
  comments.value = DUMMY_COMMENTS.sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime());
}

function addComment() {
  if (!newComment.value.trim()) {
    alert('댓글을 입력해주세요.');
    return;
  }
  const newId = `ic${DUMMY_COMMENTS.length + 1}`;
  const comment: Comment = {
    id: newId,
    authorId: 'dummyUser_current',
    authorName: '현재 사용자',
    text: newComment.value,
    createdAt: new Date(),
  };
  comments.value.unshift(comment);
  newComment.value = '';
  alert('댓글이 등록되었습니다.');
}

function requestEdit() {
  // Navigate to GroupBookCreationView with the current group ID
  // Assuming the integrated book ID can be used to find the group ID
  const groupId = book.value?.authorId; // In dummy, authorId is groupId for integrated books
  if (groupId) {
    router.push(`/group-book-creation?groupId=${groupId}`);
  } else {
    alert('그룹 정보를 찾을 수 없습니다.');
  }
}

function prevEpisode() {
  if (currentEpisodeIndex.value > 0) {
    currentEpisodeIndex.value--;
  }
}

function nextEpisode() {
  if (book.value && currentEpisodeIndex.value < book.value.episodes.length - 1) {
    currentEpisodeIndex.value++;
  }
}

// --- Lifecycle Hooks ---
onMounted(() => {
  fetchBookData();
  fetchComments();
});
</script>

<style scoped>
.integrated-group-book-page {
  padding: 80px 2rem 2rem;
  background-color: #F5F5DC;
  color: #3D2C20;
  min-height: calc(100vh - 56px);
}

.book-container {
  max-width: 900px;
  margin: 0 auto;
  background-color: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  padding: 2rem;
}

/* Header */
.book-header {
  display: flex;
  gap: 2rem;
  border-bottom: 1px solid #EAE0D5;
  padding-bottom: 2rem;
  margin-bottom: 2rem;
}

.book-cover img {
  width: 150px;
  height: 220px;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.book-meta {
  flex-grow: 1;
}

.book-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.book-authors {
  font-size: 1.1rem;
  color: #8B4513;
  margin-bottom: 1rem;
}

.book-summary {
  font-size: 1rem;
  line-height: 1.6;
  margin-bottom: 1rem;
}

.book-tags {
  margin-bottom: 1.5rem;
}

.tag {
  display: inline-block;
  background-color: #EAE0D5;
  color: #5C4033;
  padding: 0.3rem 0.8rem;
  border-radius: 15px;
  font-size: 0.9rem;
  margin-right: 0.5rem;
}

.book-updated-date {
  font-size: 0.9rem;
  color: #8B4513;
  margin-bottom: 1.5rem;
}

.book-actions .btn {
  margin-right: 1rem;
  background-color: #B8860B;
  border-color: #B8860B;
  color: #FFFFFF;
  font-weight: bold;
}

.book-actions .btn:hover {
  background-color: #DAA520;
  border-color: #DAA520;
}

/* Episode Viewer */
.episode-viewer {
  margin-bottom: 2rem;
}

.episode-navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.episode-title {
  font-size: 1.8rem;
  font-weight: 600;
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

.episode-content {
  background-color: #F5F5DC;
  padding: 2rem;
  border-radius: 8px;
  min-height: 500px;
  line-height: 1.7;
  white-space: pre-wrap;
}

/* Comments Section */
.comments-section {
  border-top: 1px solid #EAE0D5;
  padding-top: 2rem;
}

.comments-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.comment-input-area {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.comment-input-area textarea {
  flex-grow: 1;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.comment-item {
  background-color: #F5F5DC;
  padding: 1rem;
  border-radius: 8px;
}

.comment-author {
  font-size: 1rem;
  margin-bottom: 0.5rem;
}

.comment-author .comment-date {
  font-size: 0.85rem;
  color: #8B4513;
  margin-left: 0.5rem;
}

.loading-message {
  text-align: center;
  padding: 4rem;
  font-size: 1.2rem;
}
</style>
