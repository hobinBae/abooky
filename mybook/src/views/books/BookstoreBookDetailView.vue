<template>
  <div class="book-detail-page">
    <div v-if="initialCoverMode && book" class="initial-cover-view">
      <div class="book-cover-initial" :style="{ backgroundImage: `url(${coverImageUrl})` }" @click="openBook">
        <div class="title-box">
          <h1>{{ book.title }}</h1>
          <p class="author-in-box">{{ book.authorNickname }}</p>
        </div>
      </div>
    </div>

    <div v-else-if="book" class="book-wrapper" :class="{ 'is-open': bookIsOpened }">

      <aside class="left-panel">
        <div class="flipper-container">
          <div class="flipper" :class="{ 'is-flipped': isCoverFlipped }">
            <div class="flipper-front" @click="flipCover">
              <div class="book-cover-design" :style="{ backgroundImage: `url(${coverImageUrl})` }">
                <div class="title-box">
                  <h1>{{ book.title }}</h1>
                  <p class="author-in-box">{{ book.authorNickname }}</p>
                </div>
              </div>
            </div>
            <div class="flipper-back" @click="flipCover">
              <h4>작가</h4>
              <p class="author-line">
                <router-link :to="`/author/${book.memberId}`" class="author-link">{{ book.authorNickname }}</router-link>
              </p>
              <hr class="divider" />
              <h4>장르</h4>
              <div class="genres-container">
                <span class="tag">{{ book.categoryName }}</span>
              </div>
              <hr class="divider" />
              <h4>태그</h4>
              <div class="tags-container">
                <span v-for="tag in book.tags" :key="tag.tagId" class="tag">#{{ tag.tagName }}</span>
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
          <button @click="toggleLike" class="btn-stat" :class="{ liked: book.isLiked }">
            <i class="bi" :class="book.isLiked ? 'bi-heart-fill' : 'bi-heart'"></i>
            <span>{{ book.likeCount }}</span>
          </button>
          <span class="stat-item">
            <i class="bi bi-eye-fill"></i>
            <span>{{ book.viewCount }}</span>
          </span>
        </div>

        <div class="author-controls" v-if="isAuthor">
          <button @click="deleteBook" class="btn btn-edit btn-delete">
            <i class="bi bi-trash"></i> 책 삭제하기
          </button>
        </div>
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
            <div class="content-body" v-html="formattedContent(currentEpisode.content)"></div>

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
                <div v-for="comment in comments" :key="comment.commentId" class="comment-item">
                  <div v-if="editingCommentId === comment.commentId" class="comment-edit-form">
                    <textarea v-model="editingCommentText" class="form-control"></textarea>
                    <div class="comment-actions">
                      <button @click="cancelEdit" class="btn btn-secondary">취소</button>
                      <button @click="saveEdit(comment.commentId)" class="btn btn-primary">저장</button>
                    </div>
                  </div>

                  <div v-else>
                    <p class="comment-author">
                      <strong>
                        <router-link :to="`/author/${comment.memberId}`">{{ comment.nickname }}</router-link>
                      </strong>
                      <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
                    </p>
                    <p class="comment-text">{{ comment.content }}</p>
                    <div v-if="comment.memberId === authStore.user?.memberId" class="comment-actions">
                      <button @click="startEditing(comment)" class="btn-action">수정</button>
                      <button @click="deleteComment(comment.commentId)" class="btn-action btn-delete">삭제</button>
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
import { useAuthStore } from '@/stores/auth';
import { communityService, type CommunityBookDetailResponse, type CommunityBookComment, type Pageable } from '@/services/communityService';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const bookId = computed(() => Number(route.params.id));

const initialCoverMode = ref(true);
const bookIsOpened = ref(false);
const isCoverFlipped = ref(false);
const book = ref<CommunityBookDetailResponse | null>(null);
const comments = ref<CommunityBookComment[]>([]);
const commentsPage = ref(0);
const commentsTotalPages = ref(0);
const currentEpisodeIndex = ref<number | null>(null);
const newComment = ref('');
const areCommentsVisible = ref(false);
const editingCommentId = ref<number | null>(null);
const editingCommentText = ref('');

const coverImageUrl = computed(() => book.value?.coverImageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974');
const formattedPublicationDate = computed(() => {
  if (!book.value?.createdAt) return '';
  return formatDate(book.value.createdAt);
});
const currentEpisode = computed(() => {
  if (book.value && currentEpisodeIndex.value !== null) {
    return book.value.episodes[currentEpisodeIndex.value];
  }
  return null;
});
const isAuthor = computed(() => {
  return book.value && authStore.user && book.value.memberId === authStore.user.memberId;
});
const hasNextEpisode = computed(() => book.value && currentEpisodeIndex.value !== null && currentEpisodeIndex.value < book.value.episodes.length - 1);
const hasPreviousEpisode = computed(() => currentEpisodeIndex.value !== null && currentEpisodeIndex.value > 0);
const formattedContent = (content: string) => content.replace(/\n/g, '<br />');

function openBook() {
  initialCoverMode.value = false;
  setTimeout(() => { bookIsOpened.value = true; }, 10);
}
function goBackToList() { currentEpisodeIndex.value = null; }
function goToNextEpisode() { if (hasNextEpisode.value) selectEpisode(currentEpisodeIndex.value! + 1); }
function goToPreviousEpisode() { if (hasPreviousEpisode.value) selectEpisode(currentEpisodeIndex.value! - 1); }
function selectEpisode(index: number) {
  if (book.value && index >= 0 && index < book.value.episodes.length) {
    currentEpisodeIndex.value = index;
    document.querySelector('.right-panel-scroller')?.scrollTo(0, 0);
  }
}
function toggleCommentsVisibility() { areCommentsVisible.value = !areCommentsVisible.value; }
function flipCover() { isCoverFlipped.value = !isCoverFlipped.value; }

async function fetchBookData() {
  try {
    book.value = await communityService.getCommunityBookDetail(bookId.value);
  } catch (error) {
    console.error('책 정보를 불러오는데 실패했습니다:', error);
    book.value = null;
  }
}

async function fetchComments() {
  if (!book.value || (commentsPage.value > 0 && commentsPage.value >= commentsTotalPages.value)) return;
  try {
    const pageable: Pageable = { page: commentsPage.value, size: 5, sort: 'createdAt,asc' };
    const response = await communityService.getCommunityBookComments(bookId.value, pageable);
    comments.value.push(...response.content);
    commentsTotalPages.value = response.totalPages;
    commentsPage.value++;
  } catch (error) {
    console.error('댓글을 불러오는데 실패했습니다:', error);
  }
}

async function addComment() {
  if (!newComment.value.trim() || !book.value) return;
  try {
    const response = await communityService.createCommunityBookComment(bookId.value, { communityBookId: bookId.value, content: newComment.value });
    comments.value.push(response);
    newComment.value = '';
    areCommentsVisible.value = true;
  } catch (error) {
    console.error('댓글 작성에 실패했습니다:', error);
  }
}

async function deleteBook() {
  if (!book.value) return;
  if (confirm(`'${book.value.title}'을(를) 정말로 삭제하시겠습니까?`)) {
    try {
      await communityService.deleteCommunityBook(book.value.communityBookId);
      alert('책이 삭제되었습니다.');
      router.push('/bookstore');
    } catch (error) {
      console.error('책 삭제에 실패했습니다:', error);
      alert('삭제 처리 중 오류가 발생했습니다.');
    }
  }
}

async function toggleLike() {
    if (!book.value) return;
    try {
        const response = await communityService.toggleLike(book.value.communityBookId);
        book.value.isLiked = response.liked;
        const countResponse = await communityService.getLikeCount(book.value.communityBookId);
        book.value.likeCount = countResponse.likeCount;
    } catch (error) {
        console.error('좋아요 처리에 실패했습니다:', error);
    }
}

async function deleteComment(commentId: number) {
  if (!book.value) return;
  if (confirm('정말로 댓글을 삭제하시겠습니까?')) {
    try {
      await communityService.deleteCommunityBookComment(book.value.communityBookId, commentId);
      comments.value = comments.value.filter(c => c.commentId !== commentId);
    } catch (error) {
      console.error('댓글 삭제에 실패했습니다:', error);
    }
  }
}

function startEditing(comment: CommunityBookComment) {
  editingCommentId.value = comment.commentId;
  editingCommentText.value = comment.content;
}

function cancelEdit() {
  editingCommentId.value = null;
  editingCommentText.value = '';
}

function saveEdit(commentId: number) {
  // TODO: Implement comment update API call
  console.log(`Saving comment ${commentId} with text: ${editingCommentText.value}`);
  const comment = comments.value.find(c => c.commentId === commentId);
  if (comment) {
    comment.content = editingCommentText.value;
  }
  cancelEdit();
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).format(date).replace(/\.$/, '');
};

onMounted(async () => {
  if (authStore.accessToken && !authStore.user) {
    await authStore.fetchUserInfo();
  }
  await fetchBookData();
  if (book.value) {
    fetchComments();
  }
  setTimeout(() => { openBook(); }, 1000);
});

watch(bookId, () => {
  fetchBookData();
  comments.value = [];
  commentsPage.value = 0;
  fetchComments();
  currentEpisodeIndex.value = null;
  isCoverFlipped.value = false;
});
</script>

<style scoped>
/* --- 폰트 Import --- */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&family=Pretendard:wght@400;500;700&display=swap');
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

/* --- 애니메이션 --- */
@keyframes pulse {
  0% {
    opacity: 0.7;
    transform: scale(1) rotate(-15deg);
  }
  50% {
    opacity: 1;
    transform: scale(1.1) rotate(-15deg);
  }
  100% {
    opacity: 0.7;
    transform: scale(1) rotate(-15deg);
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
.author-controls { display: flex; gap: 8px; width: 100%; padding: 0 10px; box-sizing: border-box; }
.author-controls .btn-edit { flex: 1; }
.cover-adornment { position: absolute; bottom: 8px; right: -25px; height: 150px; display: flex; align-items: flex-end; pointer-events: none; }
.flip-indicator { display: inline-block; font-size: 20px; color: #ccc; animation: pulse 2.5s infinite ease-in-out; }
.vertical-publish-date { writing-mode: vertical-lr; text-orientation: mixed; color: #ccc; font-size: 12px; letter-spacing: 2px; }

/* --- 오른쪽 패널 내부 스타일 --- */
.right-panel-scroller { padding: 40px 8%; }
.btn-back { background: none; border: 1px solid #ddd; padding: 8px 16px; border-radius: 20px; font-size: 14px; font-weight: 500; cursor: pointer; display: inline-flex; align-items: center; gap: 6px; margin-bottom: 32px; transition: all 0.2s ease; }
.btn-back:hover { background-color: #f8f8f8; border-color: #ccc; transform: scale(1.03); }
hr { border: 0; border-top: 1px solid #eee; margin: 40px 0; }
.episode-header { text-align: left; margin-bottom: 40px; }
.episode-header h2 { font-family: 'Noto Serif KR', serif; font-size: 32px; font-weight: 700; margin-bottom: 4px; }
.episode-header p { font-size: 15px; color: #999; }
.content-body { font-family: 'Noto Serif KR', serif; font-size: 16px; line-height: 2.0; color: #333; padding-bottom: 60px; text-align: justify; }
.episode-navigation { display: flex; justify-content: space-between; margin-top: 40px; padding-top: 20px; border-top: 1px solid #eee; }
.btn-nav { background: none; border: 1px solid #ddd; padding: 10px 20px; border-radius: 20px; font-size: 15px; font-weight: 600; cursor: pointer; display: inline-flex; align-items: center; gap: 8px; transition: all 0.2s ease; }
.btn-nav:hover { background-color: #f8f8f8; border-color: #ccc; transform: scale(1.03); }
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
