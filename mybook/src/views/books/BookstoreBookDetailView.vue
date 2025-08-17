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
          <button @click="toggleLike" class="btn-stat">
            <i class="bi" :class="book.isLiked ? 'bi-heart-fill' : 'bi-heart'"></i>
            <span>{{ book.likeCount }}</span>
          </button>
          <button @click="toggleBookmark" class="btn-stat">
            <i :class="book.isBookmarked ? 'bi bi-bookmark-fill' : 'bi bi-bookmark'"></i>
          </button>
          <span class="stat-item">
            <i class="bi bi-eye-fill"></i>
            <span>{{ book.viewCount }}</span>
          </span>
          <span class="stat-item" :title="`평균 평점: ${book.averageRating.toFixed(1)}`">
            <i class="bi bi-star-fill"></i>
            <span>{{ book.averageRating.toFixed(1) }}</span>
          </span>
        </div>

        <div class="author-controls">
          <button v-if="isAuthor" @click="deleteBook" class="btn btn-edit btn-delete">
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
              <li v-for="(episode, index) in book.communityEpisodes" :key="episode.episodeId" @click="selectEpisode(index)"
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
              <h3>댓글<span class="comment-count">{{ allComments.length }}</span></h3>
              <button @click="toggleCommentsVisibility" class="btn-toggle-comments">
                <i class="bi" :class="areCommentsVisible ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
                <span>{{ areCommentsVisible ? '숨기기' : '보기' }}</span>
              </button>
            </div>

            <div v-if="areCommentsVisible">
              <div class="comment-list">
                <div v-for="comment in paginatedComments" :key="comment.communityBookCommentId" class="comment-item">
                  <div v-if="editingCommentId === comment.communityBookCommentId" class="comment-edit-form">
                    <textarea v-model="editingCommentText" class="form-control"></textarea>
                    <div class="comment-actions">
                      <button @click="cancelEdit" class="btn btn-secondary">취소</button>
                      <button @click="saveEdit(comment.communityBookCommentId)" class="btn btn-primary">저장</button>
                    </div>
                  </div>

                  <div class="comment-content-wrapper">
                    <img :src="comment.profileImageUrl || '/images/profile.png'" alt="프로필 사진" class="comment-author-profile" />
                    <div class="comment-details">
                      <p class="comment-author">
                        <strong>
                          <router-link :to="`/author/${comment.memberId}`">{{ comment.nickname }}</router-link>
                        </strong>
                        <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
                      </p>
                      <p class="comment-text">{{ comment.content }}</p>
                      <div v-if="comment.memberId === authStore.user?.memberId" class="comment-actions">
                        <button @click="startEditing(comment)" class="btn-action">수정</button>
                        <button @click="deleteComment(comment.communityBookCommentId)" class="btn-action btn-delete">삭제</button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="pagination-container" v-if="totalPages > 1">
                <button @click="currentPage--" :disabled="currentPage === 0" class="page-btn">
                  <i class="bi bi-chevron-left"></i>
                </button>
                <template v-for="pageNumber in totalPages" :key="pageNumber">
                  <button
                    @click="currentPage = pageNumber - 1"
                    :class="['page-btn', { active: currentPage === pageNumber - 1 }]"
                  >
                    {{ pageNumber }}
                  </button>
                </template>
                <button @click="currentPage++" :disabled="currentPage >= totalPages - 1" class="page-btn">
                  <i class="bi bi-chevron-right"></i>
                </button>
              </div>

              <div v-if="authStore.isLoggedIn" class="comment-input-area">
                <div class="star-rating mb-3">
                  <span v-for="starIndex in 5" :key="starIndex" @mouseover="onHover(starIndex)" @click="onClick(starIndex)" @mouseleave="resetHover" class="star">
                    <i :class="getStarClass(starIndex)"></i>
                  </span>
                </div>
                <textarea v-model="newComment" placeholder="따뜻한 응원의 댓글을 남겨주세요." class="form-control"></textarea>
                <button @click="addComment" class="btn btn-primary" :disabled="!newComment.trim()">등록</button>
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
import { ref, onMounted, computed, watch, nextTick } from 'vue';
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
const allComments = ref<CommunityBookComment[]>([]);
const currentPage = ref(0);
const pageSize = 5;
const currentEpisodeIndex = ref<number | null>(null);
const newComment = ref('');
const newRating = ref(0);
const hoverRating = ref(0);
const areCommentsVisible = ref(false);
const editingCommentId = ref<number | null>(null);
const editingCommentText = ref('');
const bookmarkedBookIds = ref(new Set<number>());
const likedBookIds = ref(new Set<number>());

const totalPages = computed(() => Math.ceil(allComments.value.length / pageSize));
const paginatedComments = computed(() => {
  const start = currentPage.value * pageSize;
  const end = start + pageSize;
  return allComments.value.slice(start, end);
});

const coverImageUrl = computed(() => book.value?.coverImageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974');
const formattedPublicationDate = computed(() => {
  if (!book.value?.createdAt) return '';
  return formatDate(book.value.createdAt);
});
const currentEpisode = computed(() => {
  if (book.value && currentEpisodeIndex.value !== null) {
    return book.value.communityEpisodes[currentEpisodeIndex.value];
  }
  return null;
});
const isAuthor = computed(() => {
  return book.value && authStore.user && book.value.memberId === authStore.user.memberId;
});
const hasNextEpisode = computed(() => book.value && currentEpisodeIndex.value !== null && currentEpisodeIndex.value < book.value.communityEpisodes.length - 1);
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
  if (book.value && index >= 0 && index < book.value.communityEpisodes.length) {
    currentEpisodeIndex.value = index;
    document.querySelector('.right-panel-scroller')?.scrollTo(0, 0);
  }
}
function toggleCommentsVisibility() { areCommentsVisible.value = !areCommentsVisible.value; }
function flipCover() { isCoverFlipped.value = !isCoverFlipped.value; }

async function fetchBookmarkedStatus() {
  try {
    const response = await communityService.getBookmarkedCommunityBooks({ page: 0, size: 1000 }); // Fetch all bookmarks
    const ids = response.content.map(b => b.communityBookId);
    bookmarkedBookIds.value = new Set(ids);
  } catch (error) {
    console.error('북마크 목록을 불러오는데 실패했습니다:', error);
  }
}

async function fetchLikedStatus() {
  try {
    const response = await communityService.getLikedCommunityBooks({ page: 0, size: 1000 }); // Fetch all liked books
    const ids = response.content.map(b => b.communityBookId);
    likedBookIds.value = new Set(ids);
  } catch (error) {
    console.error('좋아요 목록을 불러오는데 실패했습니다:', error);
  }
}

async function fetchBookData() {
  try {
    book.value = await communityService.getCommunityBookDetail(bookId.value);
    if (book.value) {
      // 프론트엔드에서 북마크와 좋아요 상태를 확인하여 설정
      book.value.isBookmarked = bookmarkedBookIds.value.has(book.value.communityBookId);
      book.value.isLiked = likedBookIds.value.has(book.value.communityBookId);
    }
  } catch (error) {
    console.error('책 정보를 불러오는데 실패했습니다:', error);
    book.value = null;
  }
}

async function fetchAllComments() {
  if (!book.value) return;
  try {
    // Fetch all comments and sort them on the client-side
    const pageable: Pageable = { page: 0, size: 1000 };
    const response = await communityService.getCommunityBookComments(bookId.value, pageable);
    allComments.value = response.content.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  } catch (error) {
    console.error('댓글을 불러오는데 실패했습니다:', error);
    allComments.value = [];
  }
}

async function addComment() {
  if (!authStore.isLoggedIn) {
    alert('로그인이 필요합니다.');
    return;
  }
  if (!newComment.value.trim() || !book.value) return;
  try {
    await communityService.createCommunityBookComment(bookId.value, { communityBookId: bookId.value, content: newComment.value });

    if (newRating.value > 0) {
      try {
        await communityService.createOrUpdateRating({ communityBookId: bookId.value, score: newRating.value });
      } catch (ratingError) {
        console.warn('평점 등록/수정에 실패했습니다. 댓글은 정상적으로 등록됩니다.', ratingError);
      }
    }

    await fetchAllComments();

    await nextTick();
    currentPage.value = 0; // Go to the first page to see the newest comment

    newComment.value = '';
    newRating.value = 0;
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
    if (!authStore.isLoggedIn) {
        alert('로그인이 필요합니다.');
        router.push('/login');
        return;
    }
    if (!book.value) return;

    // 1. UI를 먼저 업데이트합니다 (낙관적 업데이트).
    const originalIsLiked = book.value.isLiked;
    const originalLikeCount = book.value.likeCount;

    book.value.isLiked = !book.value.isLiked;
    if (book.value.isLiked) {
        book.value.likeCount++;
    } else {
        book.value.likeCount--;
    }

    // 2. 서버에 API 요청을 보냅니다.
    try {
        await communityService.toggleLike(book.value.communityBookId);
        // 성공 시 아무것도 하지 않음 (이미 UI가 업데이트됨)
    } catch (error) {
        // 3. 에러 발생 시 원래 상태로 롤백합니다.
        console.error('좋아요 처리에 실패했습니다:', error);
        alert('좋아요 처리에 실패했습니다. 다시 시도해주세요.');
        if (book.value) {
            book.value.isLiked = originalIsLiked;
            book.value.likeCount = originalLikeCount;
        }
    }
}

async function toggleBookmark() {
  if (!book.value) return;

  const originalBookmarkedState = book.value.isBookmarked;
  book.value.isBookmarked = !book.value.isBookmarked;

  try {
    await communityService.toggleBookmark(book.value.communityBookId);
  } catch (error) {
    console.error('북마크 처리에 실패했습니다:', error);
    book.value.isBookmarked = originalBookmarkedState;
    alert('북마크 처리에 실패했습니다. 다시 시도해주세요.');
  }
}

async function deleteComment(commentId: number) {
  if (!book.value) return;
  if (confirm('정말로 댓글을 삭제하시겠습니까?')) {
    try {
      await communityService.deleteCommunityBookComment(book.value.communityBookId, commentId);
      await fetchAllComments();
    } catch (error) {
      console.error('댓글 삭제에 실패했습니다:', error);
    }
  }
}

function startEditing(comment: CommunityBookComment) {
  editingCommentId.value = comment.communityBookCommentId;
  editingCommentText.value = comment.content;
}

function cancelEdit() {
  editingCommentId.value = null;
  editingCommentText.value = '';
}

function getStarClass(starIndex: number) {
  const rating = hoverRating.value || newRating.value;
  if (rating >= starIndex) return "bi bi-star-fill";
  return "bi bi-star";
}

function onHover(starIndex: number) {
  hoverRating.value = starIndex;
}

function onClick(starIndex: number) {
  newRating.value = starIndex;
}

function resetHover() {
  hoverRating.value = 0;
}

function saveEdit(commentId: number) {
  // TODO: Implement comment update API call
  console.log(`Saving comment ${commentId} with text: ${editingCommentText.value}`);
  const comment = allComments.value.find(c => c.communityBookCommentId === commentId);
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

  // 사용자 정보를 먼저 가져온 후, 북마크, 좋아요, 책 상세 정보를 병렬로 가져옵니다.
  await Promise.all([fetchBookmarkedStatus(), fetchLikedStatus()]);
  await fetchBookData();

  if (book.value) {
    fetchAllComments();
  }
  setTimeout(() => { openBook(); }, 1000);
});

watch(bookId, async () => {
  // 책 ID가 변경될 때 북마크, 좋아요, 책 정보를 다시 가져옵니다.
  await Promise.all([fetchBookmarkedStatus(), fetchLikedStatus(), fetchBookData()]);

  allComments.value = [];
  currentPage.value = 0;
  if (book.value) {
    fetchAllComments();
  }
  currentEpisodeIndex.value = null;
  isCoverFlipped.value = false;
});
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
.author-controls { display: flex; gap: 6px; width: 100%; padding: 10px 8px; box-sizing: border-box; min-height: 54px; justify-content: center; }
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
.comment-content-wrapper { display: flex; align-items: flex-start; gap: 12px; }
.comment-author-profile { width: 32px; height: 32px; border-radius: 50%; object-fit: cover; }
.comment-details { flex: 1; }
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

.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 2rem;
  gap: 0.4rem;
}

.page-btn {
  background-color: #fff;
  border: 1px solid #ddd;
  color: #555;
  padding: 0.4rem;
  border-radius: 40px;
  cursor: pointer;
  transition: all 0.2s;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-btn:hover:not(:disabled) {
  background-color: #f5f5f5;
  border-color: #aeaeae;
}

.page-btn.active {
  background-color: #6F7D48;
  color: white;
  border-color: #5b673b;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.star-rating {
  display: flex;
  justify-content: center;
  align-self: center;
  gap: 0.3rem;
  font-size: 1.5rem;
  color: #666;
  cursor: pointer;
  margin-bottom: 10px;
}

.star i {
  transition: color 0.2s;
}

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
