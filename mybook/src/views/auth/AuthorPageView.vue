<template>
  <div class="author-page-container">
    <div v-if="authorProfile" class="profile-header">
      <img :src="authorProfile.profilePic || 'https://via.placeholder.com/150'" alt="Profile Picture" class="profile-pic">
      <h2 class="author-penname">{{ authorProfile.penName }}</h2>
    </div>
    <div v-else class="loading-message">
      <p>작가 정보를 불러오는 중이거나, 해당 작가를 찾을 수 없습니다.</p>
    </div>

    <section v-if="authorProfile" class="authored-books-section">
      <h3 class="section-subtitle">작가가 쓴 책</h3>
      <div class="book-list">
        <router-link v-for="book in authoredBooks" :key="book.id" :to="`/book-detail/${book.id}`" class="book-item">
          {{ book.title }}
        </router-link>
      </div>
      <p v-if="authoredBooks.length === 0" class="no-books-message">작가가 쓴 책이 없습니다.</p>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';

interface Book {
  id: string;
  title: string;
  authorId: string;
  authorName?: string;
}

interface UserProfile {
  id: string;
  penName: string;
  profilePic?: string;
}

const route = useRoute();
const authorId = computed(() => route.params.authorId as string);

// Dummy Data
const DUMMY_AUTHORS: UserProfile[] = [
  { id: 'dummyUser1', penName: '김작가', profilePic: 'https://via.placeholder.com/150/FFD700/000000?text=김작가' },
  { id: 'dummyUser2', penName: '윤동주', profilePic: 'https://via.placeholder.com/150/8B4513/FFFFFF?text=윤동주' },
  { id: 'dummyUser3', penName: '생텍쥐페리', profilePic: 'https://via.placeholder.com/150/DAA520/FFFFFF?text=생텍쥐페리' },
  { id: 'user_dummy_1', penName: '글쓰는 여행가', profilePic: 'https://via.placeholder.com/150/ADD8E6/000000?text=여행가' },
  { id: 'user_dummy_2', penName: '그림 그리는 이야기꾼', profilePic: 'https://via.placeholder.com/150/90EE90/000000?text=이야기꾼' },
  { id: 'author4', penName: '이개발', profilePic: 'https://via.placeholder.com/150/FFB6C1/000000?text=이개발' },
  { id: 'author5', penName: '박사랑', profilePic: 'https://via.placeholder.com/150/DDA0DD/000000?text=박사랑' },
  { id: 'author6', penName: '최건강', profilePic: 'https://via.placeholder.com/150/87CEEB/000000?text=최건강' },
  { id: 'author7', penName: '김판타', profilePic: 'https://via.placeholder.com/150/FF6347/000000?text=김판타' },
  { id: 'author8', penName: '이공상', profilePic: 'https://via.placeholder.com/150/4682B4/000000?text=이공상' },
  { id: 'author9', penName: '정역사', profilePic: 'https://via.placeholder.com/150/D2B48C/000000?text=정역사' },
  { id: 'author10', penName: '강성장', profilePic: 'https://via.placeholder.com/150/FFD700/000000?text=강성장' },
];

const DUMMY_ALL_BOOKS: Book[] = [
  { id: 'mybook1', title: '나의 어린 시절 이야기', authorId: 'dummyUser1', authorName: '김작가' },
  { id: 'mybook2', title: '꿈을 향한 도전', authorId: 'dummyUser1', authorName: '김작가' },
  { id: 'mybook3', title: '여행의 기록', authorId: 'dummyUser1', authorName: '김작가' },
  { id: 'mybook4', title: '개발자의 삶', authorId: 'dummyUser1', authorName: '김작가' },
  { id: 'likedbook1', title: '별 헤는 밤', authorId: 'dummyUser2', authorName: '윤동주' },
  { id: 'likedbook2', title: '어린 왕자', authorId: 'dummyUser3', authorName: '생텍쥐페리' },
];

const authorProfile = ref<UserProfile | null>(null);
const authoredBooks = ref<Book[]>([]);

const fetchAuthorData = () => {
  console.log('AuthorPageView: Fetching data for authorId:', authorId.value); // Add this log
  const foundAuthor = DUMMY_AUTHORS.find(a => a.id === authorId.value);
  if (foundAuthor) {
    console.log('AuthorPageView: Found author:', foundAuthor); // Add this log
    authorProfile.value = foundAuthor;
    authoredBooks.value = DUMMY_ALL_BOOKS.filter(book => book.authorId === authorId.value);
    console.log('AuthorPageView: Authored books:', authoredBooks.value); // Add this log
  } else {
    console.log('AuthorPageView: Author not found for ID:', authorId.value); // Add this log
    authorProfile.value = null;
    authoredBooks.value = [];
  }
};

onMounted(() => {
  fetchAuthorData();
});

watch(authorId, fetchAuthorData);
</script>

<style scoped>
.author-page-container {
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

.author-penname {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 1rem;
}

.section-subtitle {
  font-size: 1.8rem;
  font-weight: bold;
  margin-top: 2rem;
  margin-bottom: 1.5rem;
  color: #5C4033;
  text-align: center;
}

.book-list {
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
</style>