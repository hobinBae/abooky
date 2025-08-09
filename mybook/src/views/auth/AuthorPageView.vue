<template>
  <div class="author-page-container">
    <div v-if="isLoading" class="loading-message">
      <p>작가 정보를 불러오는 중입니다...</p>
    </div>
    <div v-else-if="authorProfile" class="profile-header">
      <img :src="authorProfile.profileImageUrl || 'https://via.placeholder.com/150'" alt="Profile Picture" class="profile-pic">
      <h2 class="author-penname">{{ authorProfile.nickname }}</h2>
    </div>
    <div v-else class="loading-message">
      <p>해당 작가를 찾을 수 없습니다.</p>
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
import apiClient from '@/api';

interface Book {
  id: string;
  title: string;
  authorId: string;
  authorName?: string;
}

// Matches MemberResponse from backend
interface AuthorProfile {
  memberId: number;
  email: string;
  name: string;
  nickname: string;
  profileImageUrl?: string;
  intro?: string;
}

const route = useRoute();
const authorId = computed(() => route.params.authorId as string);

const authorProfile = ref<AuthorProfile | null>(null);
const authoredBooks = ref<Book[]>([]);
const isLoading = ref(true);

// 복원된 더미 데이터
const DUMMY_ALL_BOOKS: Book[] = [
  { id: 'mybook1', title: '나의 어린 시절 이야기', authorId: '7', authorName: '김작가' },
  { id: 'mybook2', title: '꿈을 향한 도전', authorId: '7', authorName: '김작가' },
  { id: 'mybook3', title: '여행의 기록', authorId: '8', authorName: 'test2' },
  { id: 'mybook4', title: '개발자의 삶', authorId: '8', authorName: 'test2' },
];

const fetchAuthorData = async () => {
  if (!authorId.value) return;
  isLoading.value = true;
  authorProfile.value = null;
  authoredBooks.value = [];

  try {
    const response = await apiClient.get(`/api/v1/members/${authorId.value}`);
    authorProfile.value = response.data.data;

    // 작가가 쓴 책 목록 더미 데이터 로직 복원
    authoredBooks.value = DUMMY_ALL_BOOKS.filter(book => book.authorId === authorId.value);

  } catch (error) {
    console.error(`Failed to fetch author data for ID ${authorId.value}:`, error);
    // API 호출에 실패하더라도, 더미 데이터는 보여주도록 필터링 로직을 catch 블록에도 추가합니다.
    authoredBooks.value = DUMMY_ALL_BOOKS.filter(book => book.authorId === authorId.value);
  } finally {
    isLoading.value = false;
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
