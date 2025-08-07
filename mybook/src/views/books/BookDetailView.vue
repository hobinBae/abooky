<template>
  <div class="book-detail-page">
    <div v-if="initialCoverMode && book" class="initial-cover-view">
      <div class="book-cover-initial" :style="{ backgroundImage: `url(${coverImageUrl})` }" @click="openBook">
        <div class="title-box">
          <h1>{{ book.title }}</h1>
          <p class="author-in-box">{{ book.authorName }}</p>
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
                  <p class="author-in-box">{{ book.authorName }}</p>
                </div>
              </div>
            </div>
            <div class="flipper-back" @click="flipCover">
              <h4>작가</h4>
              <p class="author-line">
                <router-link :to="`/author/${book.authorId}`" class="author-link">{{ book.authorName }}</router-link>
              </p>
              <hr class="divider" />
              <h4>장르</h4>
              <div class="genres-container">
                <span v-for="genre in book.genres" :key="genre" class="tag">{{ genre }}</span>
              </div>
              <hr class="divider" />
              <h4>태그</h4>
              <div class="tags-container">
                <span v-for="tag in book.tags" :key="tag" class="tag">#{{ tag }}</span>
              </div>
              <hr class="divider" />
              <h4>줄거리</h4>
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
            <span>{{ book.views }}</span>
          </span>
        </div>

        <button v-if="isAuthor" @click="editBook" class="btn btn-edit">
          <i class="bi bi-pencil-square"></i> 책 편집하기
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
            <div class="content-body" v-html="formattedContent(currentEpisode.content)"></div>
          </article>

          <section v-else class="other-episodes-section">
            <h3>전체 에피소드</h3>
            <ul class="other-episodes-list">
              <li v-for="(episode, index) in book.episodes" :key="index" @click="selectEpisode(index)"
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

// --- Interfaces ---
interface Episode { title: string; content: string; }
interface Book { id: string; title: string; authorId: string; authorName?: string; summary?: string; coverUrl?: string; genres?: string[]; tags?: string[]; episodes: Episode[]; likes: number; views: number; publicationDate?: Date; }
interface Comment { id: string; bookId: string; authorId: string; authorName?: string; text: string; createdAt: Date; }

// --- Dummy Data ---
const DUMMY_BOOKS: Book[] = [{
  id: 'mybook1',
  title: '안녕, 급식실 첫번째 이야기',
  authorId: 'dummyUser1',
  authorName: '문혜성',
  summary: '어린 시절의 소중한 추억들을 담은 자서전입니다. 골목길에서의 모험, 할머니의 따뜻한 손길, 그리고 친구들과의 우정까지, 순수했던 그 시절의 이야기들이 펼쳐집니다.',
  coverUrl: '',
  publicationDate: new Date('2024-05-20T00:00:00Z'),
  genres: ['에세이', '자서전'],
  tags: ['어린시절', '추억', '성장', '가족', '친구'],
  episodes: [
    { title: '골목길의 추억', content: '어릴 적 살던 동네의 골목길은 언제나 모험의 시작이었다.\n낡은 담벼락을 따라 이어진 좁은 길은 우리에게 미지의 세계로 통하는 문과 같았다. 해 질 녘까지 술래잡기를 하고, 숨바꼭질을 하며 뛰어놀던 그곳은 단순한 길이 아니라, 우리의 꿈과 상상력이 자라나던 놀이터였다.' }, 
    { title: '할머니의 손맛', content: '할머니의 따뜻한 손길과 맛있는 음식은 잊을 수 없는 기억이다.\n할머니 댁에 가면 언제나 구수한 된장찌개 냄새가 나를 반겼다. 할머니는 내가 좋아하는 반찬들을 한가득 차려주셨고, 나는 할머니의 사랑이 담긴 밥상을 마주할 때마다 세상에서 가장 행복한 아이가 되었다.' }, 
    { title: '우리들의 운동장', content: '친구들과 함께 뛰어놀던 운동장은 우리만의 작은 세상이었다.\n학교가 끝나면 우리는 약속이라도 한 듯 운동장으로 달려갔다. 축구공 하나만 있으면 시간 가는 줄 모르고 뛰어놀았고, 해가 져서 어두워질 때까지 운동장을 떠나지 않았다.' }
  ], 
  likes: 20, views: 150,
},];
const DUMMY_COMMENTS: Comment[] = [{ id: 'c1', bookId: 'mybook1', authorId: 'dummyUser1', authorName: '독서광', text: '정말 재미있게 읽었습니다! 다음 에피소드도 기대됩니다.', createdAt: new Date('2024-07-28T10:00:00Z') }, { id: 'c2', bookId: 'mybook1', authorId: 'user_dummy_4', authorName: '여행자', text: '저도 어릴 적 생각이 많이 나네요. 글이 참 따뜻합니다.', createdAt: new Date('2024-07-28T11:30:00Z') }, { id: 'c3', bookId: 'mybook1', authorId: 'user_dummy_5', authorName: '음유시인', text: '할머니의 사랑이 느껴지는 글입니다. 감동받았어요.', createdAt: new Date('2024-07-27T14:00:00Z') }];

// --- 로직 ---
const route = useRoute();
const router = useRouter();
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
const currentUserId = ref('dummyUser1');
const areCommentsVisible = ref(false);
const editingCommentId = ref<string | null>(null);
const editingCommentText = ref('');
const coverImageUrl = computed(() => { return book.value?.coverUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'; });
// [수정] 발행일 형식 변경 (YYYY.MM.DD)
const formattedPublicationDate = computed(() => { 
  if (!book.value?.publicationDate) return '';
  const date = book.value.publicationDate;
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}.${month}.${day}`;
});
const currentEpisode = computed(() => { if (book.value && currentEpisodeIndex.value !== null) { return book.value.episodes[currentEpisodeIndex.value]; } return null; });
const isAuthor = computed(() => book.value && book.value.authorId === currentUserId.value);
const formattedContent = (content: string) => content.replace(/\n/g, '<br />');
function openBook() { initialCoverMode.value = false; setTimeout(() => { bookIsOpened.value = true; }, 10); }
function goBackToList() { currentEpisodeIndex.value = null; }
function fetchBookData() { const foundBook = DUMMY_BOOKS.find(b => b.id === bookId.value); if (foundBook) { book.value = { ...foundBook }; likeCount.value = book.value.likes || 0; } else { book.value = null; } }
function fetchComments() { comments.value = DUMMY_COMMENTS.filter(c => c.bookId === bookId.value).sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime()); }
function toggleLike() { isLiked.value = !isLiked.value; likeCount.value += isLiked.value ? 1 : -1; }
function editBook() { if (book.value) { router.push({ name: 'BookEditor', params: { bookId: book.value.id } }); } }
function selectEpisode(index: number) { if (book.value && index >= 0 && index < book.value.episodes.length) { currentEpisodeIndex.value = index; document.querySelector('.right-panel-scroller')?.scrollTo(0, 0); } }
function toggleCommentsVisibility() { areCommentsVisible.value = !areCommentsVisible.value; }
function addComment() { if (!newComment.value.trim() || !book.value) return; const comment: Comment = { id: `c${Date.now()}`, bookId: book.value.id, authorId: currentUserId.value, authorName: '현재 사용자', text: newComment.value, createdAt: new Date() }; comments.value.unshift(comment); newComment.value = ''; areCommentsVisible.value = true; }
function deleteComment(commentId: string) { if (confirm('정말로 삭제하시겠습니까?')) { comments.value = comments.value.filter(c => c.id !== commentId); } }
function startEditing(comment: Comment) { editingCommentId.value = comment.id; editingCommentText.value = comment.text; }
function cancelEdit() { editingCommentId.value = null; editingCommentText.value = ''; }
function saveEdit(commentId: string) { const comment = comments.value.find(c => c.id === commentId); if (comment) { comment.text = editingCommentText.value; } cancelEdit(); }
function flipCover() { isCoverFlipped.value = !isCoverFlipped.value; }
onMounted(() => { fetchBookData(); fetchComments(); setTimeout(() => { openBook(); }, 1500); });
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
.right-panel { transform: translateX(150%); opacity: 0; }
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
  display: inline-block; /* transform이 잘 적용되도록 추가 */
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
.right-panel-scroller { height: 100%; overflow-y: auto; padding: 40px 8%; }
.btn-back { background: none; border: 1px solid #ddd; padding: 8px 16px; border-radius: 20px; font-size: 14px; font-weight: 500; cursor: pointer; display: inline-flex; align-items: center; gap: 6px; margin-bottom: 32px; transition: all 0.2s ease; }
.btn-back:hover { background-color: #f8f8f8; border-color: #ccc; transform: scale(1.03); }
hr { border: 0; border-top: 1px solid #eee; margin: 40px 0; }
.episode-header { text-align: left; margin-bottom: 40px; }
.episode-header h2 { font-family: 'Noto Serif KR', serif; font-size: 32px; font-weight: 700; margin-bottom: 4px; }
.episode-header p { font-size: 15px; color: #999; }
.content-body { font-family: 'Noto Serif KR', serif; font-size: 16px; line-height: 2.0; color: #333; padding-bottom: 60px; text-align: justify; }
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