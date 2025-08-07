<template>
  <div class="my-page-container">
    <div class="profile-header">
      <img :src="userProfile.profilePic || 'https://via.placeholder.com/150'" alt="Profile Picture" class="profile-pic">
      <h2 class="user-penname">{{ userProfile.penName }}</h2>
      <button @click="goToProfileEdit" class="btn btn-secondary">설정</button>
    </div>

    <section class="liked-books-section">
      <h3 class="section-subtitle">좋아요 누른 책</h3>
      <div class="book-list">
        <router-link v-for="book in likedBooks" :key="book.id" :to="`/book-detail/${book.id}`" class="book-item">
          {{ book.title }}
        </router-link>
      </div>
    </section>

    <section class="my-comments-section">
      <h3 class="section-subtitle">내가 단 댓글</h3>
      <div class="comment-list">
        <div v-for="comment in myComments" :key="comment.id" class="comment-item">
          <p class="comment-text">{{ comment.text }}</p>
          <p class="comment-meta">on <router-link :to="`/book-detail/${comment.bookId}`">{{ getBookTitle(comment.bookId) }}</router-link> - {{ comment.createdAt.toLocaleString() }}</p>
        </div>
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
  penName: string;
  profilePic?: string;
}

const router = useRouter();

// Dummy Data (will be replaced by actual data fetching)
const DUMMY_USER_PROFILE: UserProfile = {
  id: 'dummyUser1',
  penName: '김작가',
  profilePic: 'https://via.placeholder.com/150/FFD700/000000?text=김작가',
};

const DUMMY_LIKED_BOOKS: Book[] = [
  { id: 'likedbook1', title: '별 헤는 밤', authorId: 'dummyUser2', authorName: '윤동주' },
  { id: 'likedbook2', title: '어린 왕자', authorId: 'dummyUser3', authorName: '생텍쥐페리' },
];

const DUMMY_ALL_BOOKS: Book[] = [ // To get book titles for comments
  { id: 'mybook1', title: '나의 어린 시절 이야기', authorId: 'dummyUser1', authorName: '김작가' },
  { id: 'mybook2', title: '꿈을 향한 도전', authorId: 'dummyUser1', authorName: '김작가' },
  { id: 'mybook3', title: '여행의 기록', authorId: 'dummyUser1', authorName: '김작가' },
  { id: 'mybook4', title: '개발자의 삶', authorId: 'dummyUser1', authorName: '김작가' },
  { id: 'likedbook1', title: '별 헤는 밤', authorId: 'dummyUser2', authorName: '윤동주' },
  { id: 'likedbook2', title: '어린 왕자', authorId: 'dummyUser3', authorName: '생텍쥐페리' },
];

const DUMMY_MY_COMMENTS: Comment[] = [
  { id: 'c1', bookId: 'mybook1', episodeIndex: 0, authorId: 'dummyUser1', authorName: '김작가', text: '이 책 정말 감동적이에요!', createdAt: new Date('2024-07-29T10:00:00Z') },
  { id: 'c2', bookId: 'likedbook1', episodeIndex: 0, authorId: 'dummyUser1', authorName: '김작가', text: '윤동주 시인의 감성이 잘 느껴집니다.', createdAt: new Date('2024-07-29T11:00:00Z') },
];

const userProfile = ref<UserProfile>(DUMMY_USER_PROFILE);
const likedBooks = ref<Book[]>(DUMMY_LIKED_BOOKS);
const myComments = ref<Comment[]>(DUMMY_MY_COMMENTS);

const getBookTitle = (bookId: string) => {
  const book = DUMMY_ALL_BOOKS.find(b => b.id === bookId);
  return book ? book.title : '알 수 없는 책';
};

const goToProfileEdit = () => {
  router.push('/profile-edit');
};
</script>

<style scoped>
.my-page-container {
  padding: 80px 2rem 2rem;
  background-color: #F5F5DC;
  color: #3D2C20;
  min-height: calc(100vh - 56px); /* Adjust based on navbar height */
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 2rem;
  background-color: #FFFFFF;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

.profile-pic {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 1rem;
  border: 3px solid #B8860B;
}

.user-penname {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 1rem;
}

.btn-secondary {
  background-color: #D2B48C;
  color: #3D2C20;
  border: none;
  padding: 0.5rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-secondary:hover {
  background-color: #B8860B;
}

.section-subtitle {
  font-size: 1.8rem;
  font-weight: bold;
  margin-top: 2rem;
  margin-bottom: 1.5rem;
  color: #5C4033;
  text-align: center;
}

.book-list, .comment-list {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  justify-content: center;
  margin-bottom: 2rem;
}

.book-item {
  width: 100px;
  height: 150px;
  background-color: #8B4513;
  color: #F5F5DC;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  font-size: 0.8rem;
  font-weight: bold;
  padding: 0.5rem;
  word-break: break-word;
  line-height: 1.3;
  text-decoration: none;
  transition: transform 0.2s ease-in-out;
}

.book-item:hover {
  transform: translateY(-5px);
}

.comment-item {
  background-color: #FFFFFF;
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  width: 100%;
  max-width: 600px;
}

.comment-text {
  font-size: 1rem;
  margin-bottom: 0.5rem;
}

.comment-meta {
  font-size: 0.8rem;
  color: #8B4513;
  text-align: right;
}

.comment-meta a {
  color: #B8860B;
  text-decoration: none;
}

.comment-meta a:hover {
  text-decoration: underline;
}
</style>
