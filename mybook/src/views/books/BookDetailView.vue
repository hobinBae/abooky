<template>
  <div class="book-detail-page">
    <div v-if="initialCoverMode && book" class="initial-cover-view">
      <div class="book-cover-initial" :style="{ backgroundImage: `url(${coverImageUrl})` }" @click="openBook">
        <div class="title-box">
          <h1>{{ book.title }}</h1>
          <p class="author-in-box">{{ book.nickname }}</p>
        </div>
      </div>
    </div>

    <div v-else-if="book" class="book-wrapper" :class="{ 'is-open': bookIsOpened }">

      <aside class="left-panel">
        <div class="flipper-container">
          <div class="flipper" :class="{ 'is-flipped': isCoverFlipped }">
            <div class="flipper-front" @click="flipCover">
              <div class="book-cover-design" :style="{ backgroundImage: `url(${coverImageUrl})` }">
                <img v-if="isAuthor && isPublished" src="/images/complete.png" alt="출판 완료" class="published-sticker" />
                <div class="title-box">
                  <h1>{{ book.title }}</h1>
                  <p class="author-in-box">{{ book.nickname }}</p>
                </div>
              </div>
            </div>
            <div class="flipper-back" @click="flipCover">
              <h4>작가</h4>
              <p class="author-line">
                <router-link :to="`/author/${book.memberId}`" class="author-link">{{ book.nickname }}</router-link>
              </p>
              <hr class="divider" />
              <h4>장르</h4>
              <div class="genres-container">
                <span class="tag">{{ book.categoryName }}</span>
              </div>
              <hr class="divider" />
              <h4>태그</h4>
              <div class="tags-container">
                <span v-for="tag in book.tags" :key="tag" class="tag">#{{ tag }}</span>
              </div>
              <hr class="divider" />
              <h4>책 소개</h4>
              <p class="summary">{{ book.summary }}</p>
            </div>
          </div>
          <div class="cover-adornment">
            <i v-if="!isCoverFlipped" class="bi bi-hand-index flip-indicator"></i>
            <div v-if="isCoverFlipped" class="vertical-publish-date">
              {{ formattedPublicationDate }}
            </div>
          </div>
        </div>

        <div class="book-stats">
          <button @click="toggleLike" class="btn-stat" :class="{ liked: isLiked }">
            <i class="bi" :class="isLiked ? 'bi-heart-fill' : 'bi-heart'"></i>
            <span>{{ likeCount }}</span>
          </button>
          <span class="stat-item">
            <i class="bi bi-eye-fill"></i>
            <span>{{ book.viewCount }}</span>
          </span>
        </div>

        <div class="author-controls" v-if="isAuthor">
          <button @click="editBook" class="btn btn-edit">
            <i class="bi bi-pencil-square"></i> 책 편집하기
          </button>
          <button @click="deleteBook" class="btn btn-edit btn-delete">
            <i class="bi bi-trash"></i> 책 삭제하기
          </button>
        </div>
        <button v-if="isAuthor && !isPublished" @click="publishToBookstore" class="btn btn-edit btn-publish">
          <i class="bi bi-book"></i> 서점에 출판하기
        </button>
        <button v-if="isAuthor && isPublished" @click="unpublishFromBookstore" class="btn btn-edit btn-unpublish">
          <i class="bi bi-book-half"></i> 출판 취소하기
        </button>
      </aside>

      <main class="right-panel">
        <div class="right-panel-scroller">
          <article class="episode-content" v-if="currentEpisode">
            <button @click="goBackToList" class="btn-back">
              <i class="bi bi-arrow-left"></i> 목록으로
            </button>
            <header class="episode-header">
              <h2>{{ currentEpisode.title }}</h2>
              <p>{{ book.title }}</p>
            </header>
            <div class="content-body" v-html="formattedContent(truncatedContent)"></div>

            <div class="episode-navigation">
              <button v-if="hasPreviousEpisode" @click="goToPreviousEpisode" class="btn-nav prev">
                <i class="bi bi-arrow-left-circle"></i> 이전 이야기
              </button>
              <div v-else></div>
              <button v-if="hasNextEpisode" @click="goToNextEpisode" class="btn-nav next">
                다음 이야기 <i class="bi bi-arrow-right-circle"></i>
              </button>
            </div>
          </article>

          <section v-else class="other-episodes-section">
            <h3>목차</h3>
            <ul class="other-episodes-list">
              <li v-for="(episode, index) in book.episodes" :key="episode.episodeId" @click="selectEpisode(index)"
                :class="{ active: index === currentEpisodeIndex }">
                <div class="episode-list-item-title">
                  <span class="episode-number">{{ index + 1 }}.</span>
                  <span>{{ episode.title }}</span>
                </div>
                <p class="episode-list-item-snippet">{{ episode.content.substring(0, 80) }}...</p>
              </li>
            </ul>
          </section>

          <hr />

          <section class="comments-section">
            <div class="comments-header">
              <h3>댓글<span class="comment-count">{{ comments.length }}</span></h3>
              <button @click="toggleCommentsVisibility" class="btn-toggle-comments">
                <i class="bi" :class="areCommentsVisible ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
                <span>{{ areCommentsVisible ? '숨기기' : '보기' }}</span>
              </button>
            </div>

            <div v-if="areCommentsVisible">
              <div class="comment-list">
                <div v-for="comment in comments" :key="comment.id" class="comment-item">
                  <div v-if="editingCommentId === comment.id" class="comment-edit-form">
                    <textarea v-model="editingCommentText" class="form-control"></textarea>
                    <div class="comment-actions">
                      <button @click="cancelEdit" class="btn btn-secondary">취소</button>
                      <button @click="saveEdit(comment.id)" class="btn btn-primary">저장</button>
                    </div>
                  </div>

                  <div v-else>
                    <p class="comment-author">
                      <strong>
                        <router-link :to="`/author/${comment.authorId}`">{{ comment.authorName || '익명' }}</router-link>
                      </strong>
                      <span class="comment-date">{{ comment.createdAt.toLocaleDateString() }}</span>
                    </p>
                    <p class="comment-text">{{ comment.text }}</p>
                    <div v-if="comment.authorId === currentUserId" class="comment-actions">
                      <button @click="startEditing(comment)" class="btn-action">수정</button>
                      <button @click="deleteComment(comment.id)" class="btn-action btn-delete">삭제</button>
                    </div>
                  </div>
                </div>
              </div>

              <div class="comment-input-area">
                <textarea v-model="newComment" placeholder="따뜻한 응원의 댓글을 남겨주세요." class="form-control"></textarea>
                <button @click="addComment" class="btn btn-primary">등록</button>
              </div>
            </div>
          </section>
        </div>
      </main>
    </div>

    <div v-else class="loading-message">
      <p>책 정보를 불러오는 중입니다...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import apiClient from '@/api';
import { useAuthStore } from '@/stores/auth';

// --- Interfaces ---
interface Episode {
  episodeId: number;
  title: string;
  content: string;
}
interface Book {
  bookId: string;
  title: string;
  memberId: string;
  nickname?: string; // authorName -> nickname
  summary?: string;
  coverImageUrl?: string;
  categoryName?: string;
  tags?: string[];
  episodes: Episode[];
  likeCount: number;
  viewCount: number;
  createdAt: string; // ISO 8601 형식의 문자열로 받음
  completed: boolean;
}
interface Comment { id: string; bookId: string; authorId: string; authorName?: string; text: string; createdAt: Date; }

// --- Dummy Data ---
const DUMMY_COMMENTS: Comment[] = [{ id: 'c1', bookId: 'mybook1', authorId: 'dummyUser1', authorName: '독서광', text: '정말 재미있게 읽었습니다! 다음 에피소드도 기대됩니다.', createdAt: new Date('2024-07-28T10:00:00Z') }, { id: 'c2', bookId: 'mybook1', authorId: 'user_dummy_4', authorName: '여행자', text: '저도 어릴 적 생각이 많이 나네요. 글이 참 따뜻합니다.', createdAt: new Date('2024-07-28T11:30:00Z') }, { id: 'c3', bookId: 'mybook1', authorId: 'user_dummy_5', authorName: '음유시인', text: '할머니의 사랑이 느껴지는 글입니다. 감동받았어요.', createdAt: new Date('2024-07-27T14:00:00Z') }];

// --- 로직 ---
const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const bookId = computed(() => route.params.id as string);
const initialCoverMode = ref(true);
const bookIsOpened = ref(false);
const isCoverFlipped = ref(false);
const book = ref<Book | null>(null);
const comments = ref<Comment[]>([]);
const currentEpisodeIndex = ref<number | null>(null);
const newComment = ref('');
const isLiked = ref(false);
const likeCount = ref(0);
const currentUserId = ref('dummyUser1'); // TODO: 실제 사용자 ID로 교체
const areCommentsVisible = ref(false);
const editingCommentId = ref<string | null>(null);
const editingCommentText = ref('');
const isPublished = ref(false); // 출판 상태를 독립적으로 관리
const coverImageUrl = computed(() => { return book.value?.coverImageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'; });
const formattedPublicationDate = computed(() => {
  if (!book.value?.createdAt) return '';
  const date = new Date(book.value.createdAt);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}.${month}.${day}`;
});
const currentEpisode = computed(() => { if (book.value && currentEpisodeIndex.value !== null) { return book.value.episodes[currentEpisodeIndex.value]; } return null; });
const isAuthor = computed(() => {
  if (!book.value || !authStore.user) {
    return false;
  }
  // 두 ID를 모두 문자열로 변환하여 비교
  return String(book.value.memberId) === String(authStore.user.memberId);
});

const truncatedContent = computed(() => {
  if (currentEpisode.value) {
    return currentEpisode.value.content.substring(0, 1000);
  }
  return '';
});

const hasNextEpisode = computed(() => {
  return book.value && currentEpisodeIndex.value !== null && currentEpisodeIndex.value < book.value.episodes.length - 1;
});

const hasPreviousEpisode = computed(() => {
  return currentEpisodeIndex.value !== null && currentEpisodeIndex.value > 0;
});

const formattedContent = (content: string) => content.replace(/\n/g, '<br />');
function openBook() { initialCoverMode.value = false; setTimeout(() => { bookIsOpened.value = true; }, 10); }
function goBackToList() { currentEpisodeIndex.value = null; }

function goToNextEpisode() {
  if (hasNextEpisode.value) {
    selectEpisode(currentEpisodeIndex.value! + 1);
  }
}

function goToPreviousEpisode() {
  if (hasPreviousEpisode.value) {
    selectEpisode(currentEpisodeIndex.value! - 1);
  }
}
async function fetchBookData() {
  try {
    const response = await apiClient.get(`/api/v1/books/${bookId.value}`);
    book.value = response.data.data;
    if (book.value) {
      likeCount.value = book.value.likeCount || 0;
      // isPublished.value = book.value.completed; // completed 상태와 출판 상태 분리
    }
  } catch (error) {
    console.error('책 정보를 불러오는데 실패했습니다:', error);
    book.value = null;
    // TODO: 404 또는 에러 페이지로 리디렉션하는 로직 추가
  }
}
function fetchComments() { comments.value = DUMMY_COMMENTS.filter(c => c.bookId === bookId.value).sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()); }
function toggleLike() { isLiked.value = !isLiked.value; likeCount.value += isLiked.value ? 1 : -1; }
function editBook() {
  if (book.value) {
    router.push({
      name: 'BookEditor',
      params: { bookId: book.value.bookId },
      query: { start_editing: 'true' }
    });
  }
}

async function publishToBookstore() {
  if (book.value && confirm(`'${book.value.title}'을(를) 서점에 출판하시겠습니까?`)) {
    try {
      // 출판을 위해 complete API를 호출하되, UI 상태는 isPublished로 별도 관리
      await apiClient.patch(`/api/v1/books/${book.value.bookId}/complete`, { tags: book.value.tags || [] });
      isPublished.value = true; // UI 상태를 '출판됨'으로 변경
      book.value.completed = true; // 데이터 일관성을 위해 로컬 book 객체도 업데이트
      alert('책이 성공적으로 출판되었습니다.');
    } catch (error) {
      console.error('출판에 실패했습니다:', error);
      alert('출판 처리 중 오류가 발생했습니다.');
    }
  }
}

function unpublishFromBookstore() {
  // TODO: 백엔드에 출판 취소 API가 필요합니다.
  // 임시로 프론트엔드 상태만 변경
  if (confirm('출판을 취소하시겠습니까?')) {
    isPublished.value = false;
    alert('출판이 취소되었습니다. (현재는 프론트엔드에서만 반영)');
  }
}

async function deleteBook() {
  if (!book.value) return;

  if (confirm(`'${book.value.title}'을(를) 정말로 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`)) {
    try {
      // 1. 모든 에피소드 삭제
      if (book.value.episodes && book.value.episodes.length > 0) {
        for (const episode of book.value.episodes) {
          await apiClient.delete(`/api/v1/books/${book.value.bookId}/episodes/${episode.episodeId}`);
        }
      }

      // 2. 책 삭제
      await apiClient.delete(`/api/v1/books/${book.value.bookId}`);

      alert('책과 모든 에피소드가 삭제되었습니다.');
      router.push('/my-library'); // 내 서재로 이동
    } catch (error) {
      console.error('책 또는 에피소드 삭제에 실패했습니다:', error);
      alert('삭제 처리 중 오류가 발생했습니다.');
    }
  }
}

function selectEpisode(index: number) { if (book.value && index >= 0 && index < book.value.episodes.length) { currentEpisodeIndex.value = index; document.querySelector('.right-panel-scroller')?.scrollTo(0, 0); } }
function toggleCommentsVisibility() { areCommentsVisible.value = !areCommentsVisible.value; }
function addComment() { if (!newComment.value.trim() || !book.value) return; const comment: Comment = { id: `c${Date.now()}`, bookId: book.value.bookId, authorId: currentUserId.value, authorName: '현재 사용자', text: newComment.value, createdAt: new Date() }; comments.value.unshift(comment); newComment.value = ''; areCommentsVisible.value = true; }
function deleteComment(commentId: string) { if (confirm('정말로 삭제하시겠습니까?')) { comments.value = comments.value.filter(c => c.id !== commentId); } }
function startEditing(comment: Comment) { editingCommentId.value = comment.id; editingCommentText.value = comment.text; }
function cancelEdit() { editingCommentId.value = null; editingCommentText.value = ''; }
function saveEdit(commentId: string) { const comment = comments.value.find(c => c.id === commentId); if (comment) { comment.text = editingCommentText.value; } cancelEdit(); }
function flipCover() { isCoverFlipped.value = !isCoverFlipped.value; }
onMounted(async () => {
  if (authStore.accessToken && !authStore.user) {
    await authStore.fetchUserInfo();
  }
  await fetchBookData();
  fetchComments();
  setTimeout(() => { openBook(); }, 1000);
});
watch(bookId, () => { fetchBookData(); fetchComments(); currentEpisodeIndex.value = null; isCoverFlipped.value = false; });
</script>

<style scoped>
/* --- 폰트 Import --- */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&family=Pretendard:wght@400;500;700&display=swap');
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

/* --- 애니메이션 --- */
/* [수정] 기존 @keyframes 내용을 아래 코드로 교체 */
@keyframes pulse {
  0% {
    opacity: 0.7;
    transform: scale(1) rotate(-15deg); /* <-- rotate 추가 */
  }
  50% {
    opacity: 1;
    transform: scale(1.1) rotate(-15deg); /* <-- rotate 추가 */
  }
  100% {
    opacity: 0.7;
    transform: scale(1) rotate(-15deg); /* <-- rotate 추가 */
  }
}

/* --- 기본 페이지 스타일 --- */
.book-detail-page { background-color: #fff; min-height: 100vh; font-family: 'Pretendard', sans-serif; color: #333; overflow: hidden; display: flex; justify-content: center; align-items: center; }
.loading-message { text-align: center; padding: 4rem; font-size: 1.2rem; }
.initial-cover-view { display: flex; justify-content: center; align-items: center; height: 100vh; }
.book-cover-initial { width: 300px; height: 450px; border-radius: 5px; background-size: cover; background-position: center; position: relative; display: flex; justify-content: center; align-items: center; color: #333; box-shadow: 0 10px 30px rgba(0,0,0,0.15); overflow: hidden; cursor: pointer; transition: all 0.5s ease; }
.book-cover-initial:hover { transform: scale(1.05); box-shadow: 0 15px 45px rgba(0,0,0,0.2); }
.book-wrapper { display: grid; grid-template-columns: 320px 1fr; max-width: 1200px; width: 100%; align-items: center; gap: 40px; }
.left-panel, .right-panel { height: 85vh; max-height: 750px; background-color: #fff; transition: all 0.8s cubic-bezier(0.25, 1, 0.5, 1); }
.left-panel { position: relative; z-index: 10; display: flex; flex-direction: column; justify-content: center; align-items: center; transform: translateX(-150%); opacity: 0; }
.book-wrapper.is-open .left-panel { transform: translateX(0); opacity: 1; }
.right-panel { transform: translateX(150%); opacity: 0; overflow-y: auto; }
.book-wrapper.is-open .right-panel { transform: translateX(0); opacity: 1; transition-delay: 0.1s; }

/* --- 책 뒤집기(Flip) 스타일 --- */
.flipper-container { position: relative; width: 300px; height: 450px; perspective: 1000px; transition: transform 0.3s ease; }
.flipper-container:hover { transform: scale(1.05); }
.flipper { width: 100%; height: 100%; position: relative; transform-style: preserve-3d; transition: transform 0.8s cubic-bezier(0.68, -0.55, 0.27, 1.55); }
.flipper.is-flipped { transform: rotateY(180deg); }
.flipper-front, .flipper-back { position: absolute; width: 100%; height: 100%; backface-visibility: hidden; cursor: pointer; border-radius: 5px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.15); }
.flipper-front { z-index: 2; transform: rotateY(0deg); }
.flipper-back { transform: rotateY(180deg); background-color: #f8f9fa; padding: 24px; display: flex; flex-direction: column; color: #343a40; }
.flipper-back h4 { font-size: 14px; font-weight: 700; margin: 0 0 8px 0; color: #868e96; }
.flipper-back .summary { font-size: 14px; line-height: 1.5; flex-grow: 1; overflow-y: auto; margin: 0; }
.flipper-back .tags-container, .flipper-back .genres-container { display: flex; flex-wrap: wrap; gap: 8px; }
/* [수정] 장르, 태그 색상 통일 */
.flipper-back .tag { background-color: #e9ecef; padding: 4px 8px; border-radius: 30px; font-size: 13px; color: #343a40; }
.flipper-back .author-line { font-size: 16px; font-weight: 500; margin: 0; }
.flipper-back .author-link { color: #343a40; text-decoration: none; display: inline-block; transition: transform 0.2s ease; }
.flipper-back .author-link:hover { text-decoration: underline; transform: scale(1.05); }
.flipper-back .divider { border: 0; border-top: 1px solid #e9ecef; margin: 15px 0; }

/* --- 왼쪽 패널 내부 스타일 --- */
.book-cover-design { width: 100%; height: 100%; background-size: cover; background-position: center; position: relative; display: flex; justify-content: center; align-items: center; color: #333; }
.title-box { width: 60%; height: 60%; background-color: rgba(255, 255, 255, 0.95); padding: 20px; box-sizing: border-box; display: flex; flex-direction: column; justify-content: space-between; text-align: left; }
.title-box h1 { font-family: 'Noto Serif KR', serif; font-size: 25px; font-weight: 700; line-height: 1.4; margin: 0; }
.author-in-box { font-size: 12px; color: #333; font-weight: 600; margin: 0; }
.book-stats { display: flex; align-items: center; justify-content: center; gap: 24px; font-size: 18px; color: #666; margin-top: 24px; flex-shrink: 0; }
.btn-stat { background: none; border: none; padding: 0; color: #666; cursor: pointer; display:flex; align-items:center; gap: 6px; transition: transform 0.2s ease; }
.btn-stat:hover { transform: scale(1.1); }
.stat-item { display:flex; align-items:center; gap: 6px;}
.btn-edit { background: none; border: 1px solid #ddd; color: #333; font-size: 14px; padding: 10px 16px; border-radius: 4px; cursor: pointer; transition: all 0.2s; margin-top: 16px; flex-shrink: 0; }
.btn-edit:hover { transform: scale(1.03); }
.btn-edit i { margin-right: 8px; }

.author-controls {
  display: flex;
  gap: 8px;
  width: 100%;
  padding: 0 10px;
  box-sizing: border-box;
}

.author-controls .btn-edit {
  flex: 1;
}

.btn-unpublish {
  background-color: #6c757d;
  color: white;
  border-color: #6c757d;
}
.btn-unpublish:hover {
  background-color: #5a6268;
  border-color: #545b62;
}

.published-sticker {
  position: absolute;
  bottom: 8px;
  right: 8px;
  width: 100px;
  height: 100px;
  z-index: 15;
  transform: rotate(15deg);
  pointer-events: none;
}

/* [추가] 아이콘과 날짜를 감싸는 컨테이너 */
.cover-adornment {
  position: absolute;
  bottom: 8px;
  right: -25px; /* 책 바깥쪽으로 위치 조정 */
  height: 150px; /* 세로 텍스트/아이콘이 들어갈 공간 확보 */
  display: flex;
  align-items: flex-end;
  pointer-events: none; /* 클릭 방지 */
}

/* [수정] 기존 transform 속성 삭제 및 display 추가 */
.flip-indicator {
  display: inline-block; /* transform이 잘 적용'복사본으로 발행'되도록 추가 */
  font-size: 20px;
  color: #ccc;
  animation: pulse 2.5s infinite ease-in-out;

}

.vertical-publish-date {
  writing-mode: vertical-lr; /* [수정] 글자 방향을 바깥쪽으로 */
  text-orientation: mixed;
  color: #ccc;
  font-size: 12px;
  letter-spacing: 2px;
}

/* --- 오른쪽 패널 내부 스타일 --- */
.right-panel-scroller { padding: 40px 8%; }
.btn-back { background: none; border: 1px solid #ddd; padding: 8px 16px; border-radius: 20px; font-size: 14px; font-weight: 500; cursor: pointer; display: inline-flex; align-items: center; gap: 6px; margin-bottom: 32px; transition: all 0.2s ease; }
.btn-back:hover { background-color: #f8f8f8; border-color: #ccc; transform: scale(1.03); }
hr { border: 0; border-top: 1px solid #eee; margin: 40px 0; }
.episode-header { text-align: left; margin-bottom: 40px; }
.episode-header h2 { font-family: 'Noto Serif KR', serif; font-size: 32px; font-weight: 700; margin-bottom: 4px; }
.episode-header p { font-size: 15px; color: #999; }
.content-body { font-family: 'Noto Serif KR', serif; font-size: 16px; line-height: 2.0; color: #333; padding-bottom: 60px; text-align: justify; }

.episode-navigation {
  display: flex;
  justify-content: space-between;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.btn-nav {
  background: none;
  border: 1px solid #ddd;
  padding: 10px 20px;
  border-radius: 20px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.btn-nav:hover {
  background-color: #f8f8f8;
  border-color: #ccc;
  transform: scale(1.03);
}

.other-episodes-section { padding-top: 40px; }
.other-episodes-section:first-child { padding-top: 0; border-top: none; }
.other-episodes-section h3 { font-size: 18px; font-weight: 700; margin-bottom: 24px; }
.other-episodes-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 16px; }
.other-episodes-list li { padding: 20px; border: 1px solid #eee; border-radius: 8px; cursor: pointer; transition: all 0.2s ease; }
.other-episodes-list li:hover { border-color: #ccc; transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.other-episodes-list li.active { background-color: #f8f8f8; border-color: #999; }
.episode-list-item-title { display: flex; align-items: center; gap: 8px; font-weight: 600; margin-bottom: 8px; }
.episode-number { color: #999; }
.episode-list-item-snippet { font-size: 14px; color: #666; line-height: 1.6; }

/* --- 댓글 섹션 스타일 --- */
.comments-section { padding-top: 40px; }
.comments-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.comments-header h3 { font-size: 18px; font-weight: 700; margin: 0; }
.comment-count { color: #999; font-weight: 500; margin-left: 8px; }
.btn-toggle-comments { background: none; border: none; color: #666; cursor: pointer; font-size: 14px; font-weight: 500; display: flex; align-items: center; gap: 4px; padding: 4px 8px; border-radius: 4px; transition: all 0.2s ease; }
.btn-toggle-comments:hover { background-color: #f1f1f1; transform: scale(1.05); }
.comment-list { display: flex; flex-direction: column; }
.comment-item { border-top: 1px solid #f1f3f5; padding: 24px 0; }
.comment-author { font-size: 14px; margin-bottom: 12px; display: flex; align-items: center; }
.comment-author strong a { font-weight: 700; text-decoration: none; color: #333; }
.comment-author .comment-date { font-size: 13px; color: #999; margin-left: 12px; }
.comment-text { color: #555; line-height: 1.7; font-size: 15px; }
.comment-input-area { display: flex; flex-direction: column; align-items: flex-end; gap: 12px; margin-top: 2.5rem; padding-top: 2.5rem; border-top: 1px solid #f1f3f5; }
.form-control { width: 100%; border: 1px solid #ddd; border-radius: 4px; padding: 12px; font-size: 14px; min-height: 80px; resize: vertical; }
.form-control:focus { outline: none; border-color: #999; }
.btn-primary, .btn-secondary { background: none; border: none; padding: 0; font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.2s ease; }
.btn-primary:hover, .btn-secondary:hover { text-decoration: underline; transform: scale(1.1); }
.btn-primary { color: #333; }
.btn-secondary { color: #868e96; }
.comment-actions { display: flex; gap: 16px; margin-top: 12px; justify-content: flex-end; }
.btn-action { background: none; border: none; padding: 0; margin: 0; font-size: 13px; color: #868e96; cursor: pointer; transition: transform 0.2s ease; }
.btn-action:hover { text-decoration: underline; transform: scale(1.1); }
.btn-delete { color: #fa5252; }
.comment-edit-form textarea { min-height: 100px; }
</style>
