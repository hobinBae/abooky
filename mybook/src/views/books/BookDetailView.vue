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
            </header>
            <div class="content-body" v-html="formattedContent(truncatedContent)"></div>

            <div v-if="currentEpisode.imageUrl" class="episode-image-container">
              <img :src="currentEpisode.imageUrl" alt="에피소드 이미지" class="episode-image" />
            </div>

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
    <CustomAlert ref="customAlert" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import apiClient from '@/api';
import { useAuthStore } from '@/stores/auth';
import { communityService } from '@/services/communityService';
import CustomAlert from '@/components/common/CustomAlert.vue';

// --- Interfaces ---
interface Episode {
  episodeId: number;
  title: string;
  content: string;
  imageUrl?: string;
  imageId?: number; // Add imageId to Episode interface
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
const likeCount = ref(0);
const currentUserId = ref('dummyUser1'); // TODO: 실제 사용자 ID로 교체
const areCommentsVisible = ref(false);
const editingCommentId = ref<string | null>(null);
const editingCommentText = ref('');
const isPublished = ref(false); // 출판 상태를 독립적으로 관리
const customAlert = ref<InstanceType<typeof CustomAlert> | null>(null);
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
function goToNextEpisode() { if (hasNextEpisode.value) { selectEpisode(currentEpisodeIndex.value! + 1); } }
function goToPreviousEpisode() { if (hasPreviousEpisode.value) { selectEpisode(currentEpisodeIndex.value! - 1); } }
async function fetchBookData() {
  try {
    const response = await apiClient.get(`/api/v1/books/${bookId.value}`);
    const bookData = response.data.data;
    book.value = {
      ...bookData,
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      episodes: bookData.episodes?.map((e: any) => ({
        ...e,
        imageUrl: e.imageUrl,
        imageId: e.imageId // Ensure imageId is mapped
      })) || []
    };
    if (book.value) {
      likeCount.value = book.value.likeCount || 0;
    }
  } catch (error) {
    console.error('책 정보를 불러오는데 실패했습니다:', error);
    book.value = null;
  }
}
function fetchComments() { comments.value = DUMMY_COMMENTS.filter(c => c.bookId === bookId.value).sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()); }
async function editBook() {
  if (book.value && customAlert.value) {
    const result = await customAlert.value.showConfirm({
      title: '책 편집',
      message: '책을 편집하시겠습니까?'
    });
    if (result) {
      router.push({
        name: 'BookEditor',
        params: { bookId: book.value.bookId },
        query: { start_editing: 'true' }
      });
    }
  }
}

async function publishToBookstore() {
  if (book.value && customAlert.value) {
    const result = await customAlert.value.showConfirm({
      title: '서점 출판',
      message: `'${book.value.title}'을(를) 서점에 출판하시겠습니까? 커뮤니티에 공유되어 다른 사용자가 볼 수 있게 됩니다.`
    });
    if (result) {
      try {
        const response = await communityService.exportBookToCommunity(Number(book.value.bookId));
        isPublished.value = true;
        await customAlert.value.showAlert({ title: '성공', message: '책이 성공적으로 서점에 출판되었습니다.' });
        router.push({ name: 'BookstoreBookDetail', params: { id: response.communityBookId } });
      } catch (error) {
        console.error('서점 출판에 실패했습니다:', error);
        await customAlert.value.showAlert({ title: '오류', message: '출판 처리 중 오류가 발생했습니다.' });
      }
    }
  }
}

async function unpublishFromBookstore() {
  if (customAlert.value) {
    const result = await customAlert.value.showConfirm({
      title: '출판 취소',
      message: '출판을 취소하시겠습니까?'
    });
    if (result) {
      isPublished.value = false;
      await customAlert.value.showAlert({ title: '알림', message: '출판이 취소되었습니다. (현재는 프론트엔드에서만 반영)' });
    }
  }
}

async function deleteBook() {
  if (!book.value || !customAlert.value) return;

  const result = await customAlert.value.showConfirm({
    title: '책 삭제',
    message: `'${book.value.title}'을(를) 정말로 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`
  });

  if (result) {
    try {
      if (book.value.episodes && book.value.episodes.length > 0) {
        for (const episode of book.value.episodes) {
          await apiClient.delete(`/api/v1/books/${book.value.bookId}/episodes/${episode.episodeId}`);
        }
      }
      await apiClient.delete(`/api/v1/books/${book.value.bookId}`);
      await customAlert.value.showAlert({ title: '성공', message: '책과 모든 에피소드가 삭제되었습니다.' });
      router.push('/my-library');
    } catch (error) {
      console.error('책 또는 에피소드 삭제에 실패했습니다:', error);
      await customAlert.value.showAlert({ title: '오류', message: '삭제 처리 중 오류가 발생했습니다.' });
    }
  }
}

async function fetchEpisodeImages(episodeId: number) {
  if (!book.value) return;
  try {
    const response = await apiClient.get(`/api/v1/books/${book.value.bookId}/episodes/${episodeId}/images`);
    if (response.data.data && response.data.data.length > 0) {
      const episode = book.value.episodes.find(e => e.episodeId === episodeId);
      if (episode) {
        episode.imageUrl = response.data.data[0].imageUrl;
        episode.imageId = response.data.data[0].imageId;
      }
    }
  } catch (error) {
    console.error(`${episodeId}번 에피소드의 이미지 정보를 불러오는데 실패했습니다.`, error);
  }
}

async function selectEpisode(index: number) {
  if (book.value && index >= 0 && index < book.value.episodes.length) {
    currentEpisodeIndex.value = index;
    const episode = book.value.episodes[index];
    if (episode && !episode.imageUrl) {
      await fetchEpisodeImages(episode.episodeId);
    }
    document.querySelector('.right-panel-scroller')?.scrollTo(0, 0);
  }
}
function toggleCommentsVisibility() { areCommentsVisible.value = !areCommentsVisible.value; }
function addComment() { if (!newComment.value.trim() || !book.value) return; const comment: Comment = { id: `c${Date.now()}`, bookId: book.value.bookId, authorId: currentUserId.value, authorName: '현재 사용자', text: newComment.value, createdAt: new Date() }; comments.value.unshift(comment); newComment.value = ''; areCommentsVisible.value = true; }
async function deleteComment(commentId: string) {
  if (customAlert.value) {
    const result = await customAlert.value.showConfirm({
      title: '댓글 삭제',
      message: '정말로 삭제하시겠습니까?'
    });
    if (result) {
      comments.value = comments.value.filter(c => c.id !== commentId);
    }
  }
}
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
/* --- 전체적인 크기를 약 20% 축소한 버전입니다 --- */

/* --- 폰트 Import --- */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&family=Pretendard:wght@400;500;700&display=swap');
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

/* --- 애니메이션 --- */
@keyframes pulse {
  0% { opacity: 0.7; transform: scale(1) rotate(-15deg); }
  50% { opacity: 1; transform: scale(1.08) rotate(-15deg); }
  100% { opacity: 0.7; transform: scale(1) rotate(-15deg); }
}

/* --- 기본 페이지 스타일 --- */
.book-detail-page { background-color: #fff; min-height: 100vh; font-family: 'SCDream4', sans-serif; color: #333; overflow: hidden; display: flex; justify-content: center; align-items: flex-start; padding-top: 1rem;}
.loading-message { text-align: center; padding: 3.2rem; font-size: 1rem; }
.initial-cover-view { display: flex; justify-content: center; align-items: flex-start; height: 100vh; padding-top: 5rem; }
.book-cover-initial { width: 240px; height: 360px; border-radius: 4px; background-size: cover; background-position: center; position: relative; display: flex; justify-content: center; align-items: center; color: #333; box-shadow: 0 8px 24px rgba(0,0,0,0.15); overflow: hidden; cursor: pointer; transition: all 0.5s ease; }
.book-cover-initial:hover { transform: scale(1.05); box-shadow: 0 12px 36px rgba(0,0,0,0.2); }
.book-wrapper { display: grid; grid-template-columns: 256px 1fr; max-width: 960px; width: 100%; align-items: center; gap: 32px; }
.left-panel, .right-panel { height: 85vh; max-height: 600px; background-color: #fff; transition: all 0.8s cubic-bezier(0.25, 1, 0.5, 1); }
.left-panel { position: relative; z-index: 10; display: flex; flex-direction: column; justify-content: center; align-items: center; transform: translateX(-150%); opacity: 0; }
.book-wrapper.is-open .left-panel { transform: translateX(0); opacity: 1; }
.right-panel { transform: translateX(150%); opacity: 0; overflow-y: auto; }
.book-wrapper.is-open .right-panel { transform: translateX(0); opacity: 1; transition-delay: 0.1s; }

/* --- 책 뒤집기(Flip) 스타일 --- */
.flipper-container { position: relative; width: 240px; height: 360px; perspective: 800px; transition: transform 0.3s ease; }
.flipper-container:hover { transform: scale(1.05); }
.flipper { width: 100%; height: 100%; position: relative; transform-style: preserve-3d; transition: transform 0.8s cubic-bezier(0.68, -0.55, 0.27, 1.55); }
.flipper.is-flipped { transform: rotateY(180deg); }
.flipper-front, .flipper-back { position: absolute; width: 100%; height: 100%; backface-visibility: hidden; cursor: pointer; border-radius: 4px; overflow: hidden; box-shadow: 0 8px 24px rgba(0,0,0,0.15); }
.flipper-front { z-index: 2; transform: rotateY(0deg); }
.flipper-back { transform: rotateY(180deg); background-color: #f8f9fa; padding: 19px; display: flex; flex-direction: column; color: #343a40; }
.flipper-back h4 { font-size: 11px; font-weight: 700; margin: 0 0 6px 0; color: #868e96; }
.flipper-back .summary { font-size: 11px; line-height: 1.5; flex-grow: 1; overflow-y: auto; margin: 0; }
.flipper-back .tags-container, .flipper-back .genres-container { display: flex; flex-wrap: wrap; gap: 6px; }
.flipper-back .tag { background-color: #e9ecef; padding: 3px 6px; border-radius: 20px; font-size: 10px; color: #343a40; }
.flipper-back .author-line { font-size: 13px; font-weight: 500; margin: 0; }
.flipper-back .author-link { color: #343a40; text-decoration: none; display: inline-block; transition: transform 0.2s ease; }
.flipper-back .author-link:hover { text-decoration: underline; transform: scale(1.05); }
.flipper-back .divider { border: 0; border-top: 1px solid #e9ecef; margin: 12px 0; }

/* --- 왼쪽 패널 내부 스타일 --- */
.book-cover-design { width: 100%; height: 100%; background-size: cover; background-position: center; position: relative; display: flex; justify-content: center; align-items: center; color: #333; }
.title-box { width: 60%; height: 60%; background-color: rgba(255, 255, 255, 0.95); padding: 16px; box-sizing: border-box; display: flex; flex-direction: column; justify-content: space-between; text-align: left; }
.title-box h1 { font-family: 'ChosunCentennial', serif; font-size: 20px; font-weight: 700; line-height: 1.4; margin: 0; }
.author-in-box { font-family: 'NanumSquareR', serif;font-size: 10px; color: #333; font-weight: 600; margin: 0; }
.book-stats { display: flex; align-items: center; justify-content: center; gap: 19px; font-size: 14px; color: #666; margin-top: 19px; flex-shrink: 0; }
.btn-stat { background: none; border: none; padding: 0; color: #666; cursor: pointer; display:flex; align-items:center; gap: 5px; transition: transform 0.2s ease; }
.btn-stat:hover { transform: scale(1.1); }
.stat-item { display:flex; align-items:center; gap: 5px;}
.btn-edit { background: none; border: 1px solid #ddd; color: #333; font-size: 11px; padding: 8px 13px; border-radius: 10px; cursor: pointer; transition: all 0.2s; margin-top: 13px; flex-shrink: 0; }
.btn-edit:hover { transform: scale(1.03); }
.btn-edit i { margin-right: 6px; }
.author-controls { display: flex; gap: 6px; width: 100%; padding: 0 8px; padding-top: 13px; box-sizing: border-box; }
.author-controls .btn-edit { flex: 1; }
.btn-unpublish { background-color: #6c757d; color: white; border-color: #6c757d; }
.btn-unpublish:hover { background-color: #5a6268; border-color: #545b62; }
.published-sticker { position: absolute; bottom: 6px; right: 6px; width: 80px; height: 80px; z-index: 15; transform: rotate(15deg); pointer-events: none; }
.cover-adornment { position: absolute; bottom: 6px; right: -20px; height: 120px; display: flex; align-items: flex-end; pointer-events: none; }
.flip-indicator { display: inline-block; font-size: 16px; color: #ccc; animation: pulse 2.5s infinite ease-in-out; }
.vertical-publish-date { writing-mode: vertical-lr; text-orientation: mixed; color: #ccc; font-size: 10px; letter-spacing: 2px; }

/* --- 오른쪽 패널 내부 스타일 --- */
.right-panel-scroller { padding: 32px 8%; }
.btn-back { background: none; border: 1px solid #ddd; padding: 6px 13px; border-radius: 16px; font-size: 11px; font-weight: 500; cursor: pointer; display: inline-flex; align-items: center; gap: 5px; margin-bottom: 26px; transition: all 0.2s ease; }
.btn-back:hover { background-color: #f8f8f8; border-color: #ccc; transform: scale(1.03); }
hr { border: 0; border-top: 1px solid #eee; margin: 32px 0; }
.episode-header { text-align: left; margin-bottom: 32px; }
.episode-header h2 { font-family: 'ChosunCentennial', serif; font-size: 26px; font-weight: 700; margin-bottom: 3px; }
.episode-header p { font-size: 12px; color: #999; }
.content-body { font-family: 'ChosunCentennial', serif; font-size: 13px; line-height: 2.0; color: #333; text-align: justify; }
.episode-navigation { display: flex; justify-content: space-between; margin-top: 32px; padding-top: 16px; }
.btn-nav { background: none; border: 1px solid #ddd; padding: 8px 16px; border-radius: 16px; font-size: 12px; font-weight: 600; cursor: pointer; display: inline-flex; align-items: center; gap: 6px; transition: all 0.2s ease; }
.btn-nav:hover { background-color: #f8f8f8; border-color: #ccc; transform: scale(1.03); }
.episode-image-container {
  width: 100%;
  aspect-ratio: 12 / 10;
  border-radius: 6px;
  overflow: hidden;
  margin: 1.5rem 0;
  border: 1px solid var(--border-color);
  background-color: #f8f9fa;
}
.episode-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.other-episodes-section { padding-top: 32px; }
.other-episodes-section:first-child { padding-top: 0; border-top: none; }
.other-episodes-section h3 { font-size: 14px; font-weight: 700; margin-bottom: 19px; }
.other-episodes-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 13px; }
.other-episodes-list li { padding: 16px; border: 1px solid #eee; border-radius: 6px; cursor: pointer; transition: all 0.2s ease; }
.other-episodes-list li:hover { border-color: #ccc; transform: translateY(-2px); box-shadow: 0 3px 10px rgba(0,0,0,0.05); }
.other-episodes-list li.active { background-color: #f8f8f8; border-color: #999; }
.episode-list-item-title { display: flex; align-items: center; gap: 6px; font-weight: 700; margin-bottom: 6px; font-family: 'ChosunCentennial', serif; font-size: 16px; }
.episode-number { color: #999; }
.episode-list-item-snippet { font-size: 11px; color: #666; line-height: 1.6; font-family: 'ChosunCentennial', serif; }

/* --- 댓글 섹션 스타일 --- */
.comments-section { padding-top: 32px; }
.comments-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 19px; }
.comments-header h3 { font-size: 14px; font-weight: 700; margin: 0; }
.comment-count { color: #999; font-weight: 500; margin-left: 6px; }
.btn-toggle-comments { background: none; border: none; color: #666; cursor: pointer; font-size: 11px; font-weight: 500; display: flex; align-items: center; gap: 3px; padding: 3px 6px; border-radius: 4px; transition: all 0.2s ease; }
.btn-toggle-comments:hover { background-color: #f1f1f1; transform: scale(1.05); }
.comment-list { display: flex; flex-direction: column; }
.comment-item { border-top: 1px solid #f1f3f5; padding: 19px 0; }
.comment-author { font-size: 11px; margin-bottom: 10px; display: flex; align-items: center; }
.comment-author strong a { font-weight: 700; text-decoration: none; color: #333; }
.comment-author .comment-date { font-size: 10px; color: #999; margin-left: 10px; }
.comment-text { color: #555; line-height: 1.7; font-size: 12px; }
.comment-input-area { display: flex; flex-direction: column; align-items: flex-end; gap: 10px; margin-top: 2rem; padding-top: 2rem; border-top: 1px solid #f1f3f5; }
.form-control { width: 100%; border: 1px solid #ddd; border-radius: 4px; padding: 10px; font-size: 11px; min-height: 64px; resize: vertical; }
.form-control:focus { outline: none; border-color: #999; }
.btn-primary, .btn-secondary { background: none; border: none; padding: 0; font-size: 11px; font-weight: 600; cursor: pointer; transition: all 0.2s ease; }
.btn-primary:hover, .btn-secondary:hover { text-decoration: underline; transform: scale(1.1); }
.btn-primary { color: #333; }
.btn-secondary { color: #868e96; }
.comment-actions { display: flex; gap: 13px; margin-top: 10px; justify-content: flex-end; }
.btn-action { background: none; border: none; padding: 0; margin: 0; font-size: 10px; color: #868e96; cursor: pointer; transition: transform 0.2s ease; }
.btn-action:hover { text-decoration: underline; transform: scale(1.1); }
.btn-delete { color: #fa5252; }
.comment-edit-form textarea { min-height: 80px; }

@media (max-width: 1024px) {
  .book-wrapper {
    grid-template-columns: 1fr;
    gap: 1.5rem;
    padding: 1rem;
  }

  .left-panel, .right-panel {
    height: auto;
    max-height: none;
    transform: none !important;
    opacity: 1 !important;
  }

  .right-panel-scroller {
    padding: 1.5rem;
  }

  .author-controls {
    justify-content: center;
  }

  .author-controls .btn-edit {
    flex: initial;
  }
}
</style>
