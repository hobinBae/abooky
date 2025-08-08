<template>
  <div class="my-page-container">
    <button @click="goToProfileEdit" class="btn-edit-profile-icon" title="프로필 수정">
      <i class="bi bi-gear-fill"></i>
    </button>

    <section class="profile-section">
      <div class="profile-main-content">
        <div class="profile-left-section">
          <img :src="userProfile.profilePic || 'https://via.placeholder.com/150'" alt="Profile Picture"
            class="profile-pic">
          <div class="user-info">
            <h2 class="user-name">{{ userProfile.name }}</h2>
            <p class="user-penname">@{{ userProfile.penName }}</p>
          </div>
        </div>
        <div v-if="userProfile.authorMessage" class="author-message-box">
          <p class="author-message-title">작가의 말</p>
          <p class="author-message-content">{{ userProfile.authorMessage }}</p>
        </div>
      </div>
    </section>

    <hr class="divider">

    <section class="content-section">
      <h3 class="section-title">좋아요 누른 책</h3>
      <div v-if="paginatedLikedBooks.length > 0" class="book-list-grid">
        <router-link v-for="book in paginatedLikedBooks" :key="book.id" :to="`/book-detail/${book.id}`"
          class="book-item-card"
          :style="{ backgroundImage: `url(${book.coverUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
          <div class="list-title-box">
            <p class="list-title-box-title">{{ book.title }}</p>
            <p class="list-title-box-author">{{ book.authorName }}</p>
          </div>
        </router-link>
      </div>
      <p v-else class="no-content-message">아직 좋아요를 누른 책이 없습니다.</p>

      <div v-if="totalLikedBooksPages > 1" class="pagination-controls">
        <button @click="prevLikedBookPage" :disabled="likedBooksCurrentPage === 1" class="pagination-btn">
          이전
        </button>
        <span v-for="page in totalLikedBooksPages" :key="page" @click="goToLikedBookPage(page)"
          :class="['page-number', { active: likedBooksCurrentPage === page }]">
          {{ page }}
        </span>
        <button @click="nextLikedBookPage" :disabled="likedBooksCurrentPage === totalLikedBooksPages"
          class="pagination-btn">
          다음
        </button>
      </div>
    </section>

    <hr class="divider">

    <section class="content-section">
      <h3 class="section-title">내가 단 댓글</h3>
      <div v-if="paginatedComments.length > 0" class="comment-list-container">
        <div v-for="comment in paginatedComments" :key="comment.id" class="comment-item">
          <p class="comment-text">{{ comment.text }}</p>
          <p class="comment-meta">
            <router-link :to="`/book-detail/${comment.bookId}`" class="comment-book-link">{{
              getBookTitle(comment.bookId) }}</router-link>
            <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
          </p>
        </div>
      </div>
      <p v-else class="no-content-message">아직 작성한 댓글이 없습니다.</p>

      <div v-if="totalPages > 1" class="pagination-controls">
        <button @click="prevPage" :disabled="currentPage === 1" class="pagination-btn">
          이전
        </button>
        <span v-for="page in totalPages" :key="page" @click="goToPage(page)"
          :class="['page-number', { active: currentPage === page }]">
          {{ page }}
        </span>
        <button @click="nextPage" :disabled="currentPage === totalPages" class="pagination-btn">
          다음
        </button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';

interface Book {
  id: string;
  title: string;
  authorId: string;
  authorName?: string;
  coverUrl?: string;
}

interface Comment {
  id: string;
  bookId: string;
  episodeIndex: number;
  authorId: string;
  authorName?: string;
  text: string;
  createdAt: Date;
}

interface UserProfile {
  id: string;
  name: string;
  penName: string;
  profilePic?: string;
  authorMessage?: string;
}

const router = useRouter();

// Dummy Data
const DUMMY_USER_PROFILE: UserProfile = {
  id: 'dummyUser1',
  name: '홍길동',
  penName: '김작가',
  profilePic: 'https://images.unsplash.com/photo-1535713875002-d1d0cfd293ae?q=80&w=1780&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
  authorMessage: '안녕하세요! 글쓰기를 통해 세상과 소통하고 싶은 김작가입니다. 제 이야기가 여러분의 마음에 작은 울림이 되기를 바랍니다. 항상 새로운 영감을 찾아 헤매고 있으며, 독자분들과 함께 성장하는 작가가 되고 싶습니다. 많은 관심과 사랑 부탁드립니다!',
};

const DUMMY_LIKED_BOOKS: Book[] = [
  { id: 'likedbook1', title: '별 헤는 밤', authorId: 'dummyUser2', authorName: '윤동주', coverUrl: 'https://images.unsplash.com/photo-1521587760476-6c12a4b040da?w=500' },
  { id: 'likedbook2', title: '어린 왕자', authorId: 'dummyUser3', authorName: '생텍쥐페리', coverUrl: 'https://images.unsplash.com/photo-1518621736915-f3b1c41bfd00?w=500' },
  { id: 'likedbook3', title: '가족의 발견', authorId: 'dummyUser4', authorName: '김작가', coverUrl: 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500' },
  { id: 'likedbook4', title: '취미로 시작하는 코딩', authorId: 'dummyUser5', authorName: '이개발', coverUrl: 'https://images.unsplash.com/photo-1550063873-ab792950096b?w=500' },
  { id: 'likedbook5', title: '사랑의 온도', authorId: 'dummyUser6', authorName: '박사랑', coverUrl: 'https://images.unsplash.com/photo-1518717758536-85ae29035b6d?w=500' },
  { id: 'likedbook6', title: '스포츠 심리학 개론', authorId: 'dummyUser7', authorName: '최건강', coverUrl: 'https://images.unsplash.com/photo-1517649763962-0c623066013b?w=500' },
  { id: 'likedbook7', title: '드래곤의 유산', authorId: 'dummyUser8', authorName: '김판타', coverUrl: 'https://images.unsplash.com/photo-1523586044048-b7d32d5da502?w=700&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8JUVEJThDJThDJUVDJUI2JUE5JUVCJUE1JTk4fGVufDB8fDB8fHww' },
  { id: 'likedbook8', title: '우주 탐사대의 기록', authorId: 'dummyUser9', authorName: '이공상', coverUrl: 'https://images.unsplash.com/photo-1534796636912-3b95b3ab5986?w=500' },
  { id: 'likedbook9', title: '조선 왕조 실록 이야기', authorId: 'dummyUser10', authorName: '정역사', coverUrl: 'https://images.unsplash.com/photo-1448523183439-d2ac62aca997?w=700&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Nnx8JUVEJTk1JTlDJUVBJUI1JUFEfGVufDB8fDB8fHww' },
  { id: 'likedbook10', title: '나는 오늘부터 성장한다', authorId: 'dummyUser11', authorName: '강성장', coverUrl: 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=500' },
  { id: 'likedbook11', title: '새로운 시작', authorId: 'dummyUser12', authorName: '최초', coverUrl: 'https://images.unsplash.com/photo-1507842217343-583fd046b7ea?w=500' },
  { id: 'likedbook12', title: '시간 여행자의 일기', authorId: 'dummyUser13', authorName: '과거', coverUrl: 'https://images.unsplash.com/photo-1524995997946-a1c2e315a724?w=500' },
];

const DUMMY_ALL_BOOKS: Book[] = [
  { id: 'mybook1', title: '나의 어린 시절 이야기', authorId: 'dummyUser1', authorName: '김작가' },
  { id: 'mybook2', title: '꿈을 향한 도전', authorId: 'dummyUser1', authorName: '김작가' },
  { id: 'mybook3', title: '여행의 기록', authorId: 'dummyUser1', authorName: '김작가' },
  { id: 'mybook4', title: '개발자의 삶', authorId: 'dummyUser1', authorName: '김작가' },
  ...DUMMY_LIKED_BOOKS,
];

const DUMMY_MY_COMMENTS: Comment[] = [
  { id: 'c1', bookId: 'mybook1', episodeIndex: 0, authorId: 'dummyUser1', authorName: '김작가', text: '이 책 정말 감동적이에요! 작가님의 다음 작품이 기대됩니다.', createdAt: new Date('2024-07-29T10:00:00Z') },
  { id: 'c2', bookId: 'likedbook1', episodeIndex: 0, authorId: 'dummyUser1', authorName: '김작가', text: '윤동주 시인의 감성이 잘 느껴지는 아름다운 시집입니다. 여러 번 읽어도 좋네요.', createdAt: new Date('2024-07-29T11:00:00Z') },
  { id: 'c3', bookId: 'mybook2', episodeIndex: 0, authorId: 'dummyUser1', authorName: '김작가', text: '도전적인 내용이 인상 깊었습니다. 저도 용기를 얻어 새로운 시작을 해보려 합니다.', createdAt: new Date('2024-07-30T14:30:00Z') },
  { id: 'c4', bookId: 'likedbook3', episodeIndex: 0, authorId: 'dummyUser1', authorName: '김작가', text: '가족의 소중함을 다시 한번 깨닫게 해주는 책이었습니다. 따뜻한 내용에 마음이 편안해지네요.', createdAt: new Date('2024-07-31T09:15:00Z') },
  { id: 'c5', bookId: 'mybook3', episodeIndex: 0, authorId: 'dummyUser1', authorName: '김작가', text: '여행의 기록을 읽고 저도 떠나고 싶어졌어요. 다음 여행은 어디로 가실 건가요?', createdAt: new Date('2024-08-01T16:00:00Z') },
  { id: 'c6', bookId: 'likedbook4', episodeIndex: 0, authorId: 'dummyUser1', authorName: '김작가', text: '코딩 입문자에게 정말 유용한 책입니다. 설명이 쉽고 예제도 좋아서 따라하기 편했어요.', createdAt: new Date('2024-08-02T10:30:00Z') },
  { id: 'c7', bookId: 'mybook4', episodeIndex: 0, authorId: 'dummyUser1', authorName: '김작가', text: '개발자의 삶에 대한 솔직한 이야기가 좋았습니다. 공감되는 부분이 많았어요.', createdAt: new Date('2024-08-03T13:00:00Z') },
  { id: 'c8', bookId: 'likedbook5', episodeIndex: 0, authorId: 'dummyUser1', authorName: '김작가', text: '사랑의 온도, 정말 마음을 울리는 이야기였습니다. 여운이 오래 남네요.', createdAt: new Date('2024-08-04T18:00:00Z') },
  { id: 'c9', bookId: 'likedbook6', episodeIndex: 0, authorId: 'dummyUser1', authorName: '김작가', text: '스포츠 심리학에 관심이 많았는데, 이 책으로 많은 것을 배웠습니다. 감사합니다.', createdAt: new Date('2024-08-05T09:00:00Z') },
  { id: 'c10', bookId: 'likedbook7', episodeIndex: 0, authorId: 'dummyUser1', authorName: '김작가', text: '드래곤의 유산, 판타지 소설의 진수를 보여주는 작품입니다. 몰입해서 읽었어요!', createdAt: new Date('2024-08-06T11:45:00Z') },
  { id: 'c11', bookId: 'likedbook8', episodeIndex: 0, authorId: 'dummyUser1', authorName: '김작가', text: '우주 탐사대의 기록, 상상력을 자극하는 멋진 SF였습니다. 다음 편이 기대됩니다.', createdAt: new Date('2024-08-07T15:20:00Z') },
];

const userProfile = ref<UserProfile>(DUMMY_USER_PROFILE);
const likedBooks = ref<Book[]>(DUMMY_LIKED_BOOKS);
const myComments = ref<Comment[]>(DUMMY_MY_COMMENTS);

// Liked Books Pagination
const likedBooksCurrentPage = ref(1);
const likedBooksPerPage = 5;

const paginatedLikedBooks = computed(() => {
  const start = (likedBooksCurrentPage.value - 1) * likedBooksPerPage;
  const end = start + likedBooksPerPage;
  return likedBooks.value.slice(start, end);
});

const totalLikedBooksPages = computed(() => {
  return Math.ceil(likedBooks.value.length / likedBooksPerPage);
});

const goToLikedBookPage = (page: number) => {
  likedBooksCurrentPage.value = page;
};

const prevLikedBookPage = () => {
  if (likedBooksCurrentPage.value > 1) {
    likedBooksCurrentPage.value--;
  }
};

const nextLikedBookPage = () => {
  if (likedBooksCurrentPage.value < totalLikedBooksPages.value) {
    likedBooksCurrentPage.value++;
  }
};

// Comments Pagination
const currentPage = ref(1);
const commentsPerPage = 5;

const paginatedComments = computed(() => {
  const start = (currentPage.value - 1) * commentsPerPage;
  const end = start + commentsPerPage;
  return myComments.value.slice(start, end);
});

const totalPages = computed(() => {
  return Math.ceil(myComments.value.length / commentsPerPage);
});

const goToPage = (page: number) => {
  currentPage.value = page;
};

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
};

// Helper Functions
const getBookTitle = (bookId: string) => {
  const book = DUMMY_ALL_BOOKS.find(b => b.id === bookId);
  return book ? book.title : '알 수 없는 책';
};

const goToProfileEdit = () => {
  router.push('/profile-edit');
};

const formatDate = (date: Date) => {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&family=Pretendard:wght@400;500;700&display=swap');
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

/* --- 기본 페이지 스타일 --- */
.my-page-container {
  padding: 8rem 2rem;
  max-width: 900px;
  margin: 0 auto;
  font-family: 'Pretendard', sans-serif;
  color: #333;
  background-color: #fff;
  position: relative;
}

/* --- 프로필 수정 버튼 --- */
.btn-edit-profile-icon {
  position: absolute;
  top: 4rem;
  right: 1rem;
  background: none;
  border: none;
  color: #868e96;
  font-size: 1.5rem;
  cursor: pointer;
  transition: color 0.2s ease;
  padding: 0.5rem;
  z-index: 10;
}
.btn-edit-profile-icon:hover {
  color: #333;
}

/* --- 프로필 섹션 --- */
.profile-section {
  padding: 1rem 0;
  margin-bottom: 1rem;
}

.profile-main-content {
  display: flex;
  align-items: flex-start;
  gap: 2.5rem;
}

.profile-left-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  flex-shrink: 0;
}

.profile-pic {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #525252;
}

.user-info {
  text-align: center;
}

.user-name {
  font-family: 'Noto Serif KR', serif;
  font-size: 1.8rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 0.25rem;
}

.user-penname {
  font-size: 1rem;
  color: #868e96;
  font-weight: 500;
}

.author-message-box {
  flex-grow: 1;
  background-color: #f8f9fa;
  border-left: 4px solid #dee2e6;
  padding: 2rem;
  border-radius: 8px;
}

.author-message-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 0.75rem 0;
}

.author-message-content {
  font-size: 0.95rem;
  line-height: 1.7;
  color: #555;
}

/* --- 콘텐츠 섹션 --- */
.content-section {
  padding: 2rem 0;
}

.section-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 1.8rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 2rem;
  text-align: center;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f1f3f5;
}

/* --- 책 리스트 스타일 --- */
.book-list-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 2rem;
  justify-content: center;
  padding: 1rem 0;
}

.book-item-card {
  width: 140px;
  height: 210px;
  border-radius: 4px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  background-size: cover;
  background-position: center;
  text-decoration: none;
  color: inherit;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.book-item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.list-title-box {
  width: 80%;
  height: 70%;
  background-color: rgba(255, 255, 255, 0.95);
  padding: 10px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  text-align: left;
  color: #333;
  border-radius: 2px;
}

.list-title-box-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block; /* 표준 line-clamp를 위해 변경 */
  line-clamp: 3;
}

.list-title-box-author {
  font-size: 12px;
  font-weight: 500;
  margin: 0;
  color: #555;
}

/* --- 댓글 리스트 스타일 --- */
.comment-list-container {
  display: flex;
  flex-direction: column;
}

.comment-item {
  border-bottom: 1px solid #f1f3f5;
  padding: 1.5rem 0.5rem;
}
.comment-item:first-child {
  border-top: 1px solid #f1f3f5;
}

.comment-text {
  font-size: 1rem;
  line-height: 1.6;
  color: #555;
  margin-bottom: 0.75rem;
}

.comment-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.875rem;
  color: #868e96;
}

.comment-book-link {
  color: #868e96;
  text-decoration: none;
  font-weight: 600;
}

.comment-book-link:hover {
  text-decoration: underline;
}

/* --- 공통 스타일 --- */
.no-content-message {
  text-align: center;
  color: #999;
  font-size: 1rem;
  padding: 3rem 0;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-top: 1.5rem;
}

.divider {
  border: 0;
  border-top: 1px solid #f1f3f5;
  margin: 3rem 0;
}

/* --- 페이지네이션 스타일 --- */
.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.75rem;
  margin-top: 3rem;
}

.pagination-btn,
.page-number {
  font-family: 'Pretendard', sans-serif;
  background-color: #fff;
  border: 1px solid #dee2e6;
  padding: 0.5rem 0.8rem;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.9rem;
  color: #555;
}

.pagination-btn:hover:not(:disabled),
.page-number:hover {
  background-color: #f8f9fa;
  border-color: #ced4da;
}

.pagination-btn:disabled {
  color: #adb5bd;
  cursor: not-allowed;
  background-color: #f8f9fa;
}

.page-number.active {
  background-color: #333;
  color: white;
  border-color: #333;
  font-weight: 600;
}
</style>