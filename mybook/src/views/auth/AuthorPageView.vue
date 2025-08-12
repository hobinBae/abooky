<template>
  <div class="author-page-background">
    <div v-if="isLoading" class="loading-container">
      <p>작가 정보를 불러오는 중입니다...</p>
    </div>
    <div v-else-if="authorProfile" class="author-page-wrapper">
      <!-- 왼쪽 프로필 섹션 -->
      <div class="profile-sidebar">
        <div class="profile-content">
          <img :src="authorProfile.profileImageUrl || 'https://via.placeholder.com/150'" alt="Profile Picture" class="profile-pic">
          <h2 class="user-name">{{ authorProfile.name }}</h2>
          <p class="user-penname">@{{ authorProfile.nickname }}</p>
          <div v-if="authorProfile.intro" class="author-message">
            <p>{{ authorProfile.intro }}</p>
          </div>
        </div>
      </div>

      <!-- 오른쪽 콘텐츠 섹션 -->
      <div class="content-area">
        <section class="content-section">
          <h3 class="section-title">서점에 출판한 책</h3>
          <div v-if="authoredBooks.length > 0" class="book-list-grid">
            <router-link v-for="book in authoredBooks" :key="book.id" :to="`/book-detail/${book.id}`" class="book-item-card" :style="{ backgroundImage: `url(${book.coverUrl || 'https://via.placeholder.com/140x210'})` }">
              <div class="book-info-overlay">
                <p class="book-title">{{ book.title }}</p>
              </div>
            </router-link>
          </div>
          <p v-else class="no-content-message">아직 서점에 출판한 책이 없습니다.</p>
        </section>
      </div>
    </div>
    <div v-else class="loading-container">
      <p>해당 작가를 찾을 수 없습니다.</p>
    </div>
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
  coverUrl?: string;
}

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

const fetchAuthorData = async () => {
  if (!authorId.value) return;
  isLoading.value = true;

  try {
    const profileResponse = await apiClient.get(`/api/v1/members/${authorId.value}`);
    authorProfile.value = profileResponse.data.data;

    // 서점에 출판한 책 목록 API 호출 (실제 API 엔드포인트로 교체 필요)
    // const booksResponse = await apiClient.get(`/api/v1/bookstore/author/${authorId.value}`);
    // authoredBooks.value = booksResponse.data.data;

    // 임시 더미 데이터
    authoredBooks.value = [
      { id: 'book1', title: '작가의 첫번째 책', authorId: authorId.value, coverUrl: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974' },
      { id: 'book2', title: '두번째 모험 이야기', authorId: authorId.value, coverUrl: 'https://images.unsplash.com/photo-1589998059171-988d887df646?q=80&w=2070' },
    ];

  } catch (error) {
    console.error(`Failed to fetch author data for ID ${authorId.value}:`, error);
    authorProfile.value = null; // 에러 발생 시 프로필 정보 없음 처리
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchAuthorData);
watch(authorId, fetchAuthorData);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Pretendard:wght@400;500;700&display=swap');

.author-page-background {
  width: 100%;
  min-height: 100vh;
  background-color: #ffffff;
  padding: 4rem 2rem;
  box-sizing: border-box;
  font-family: 'Pretendard', sans-serif;
}

.loading-container {
  text-align: center;
  padding: 4rem;
  font-size: 1.2rem;
  color: #555;
}

.author-page-wrapper {
  display: flex;
  width: 100%;
  max-width: 1100px;
  min-height: 720px;
  background-color: #f4f3e8; /* 연한 올리브 배경 */
  margin: 0 auto;
  border-radius: 12px;
  box-shadow: 0 8px 25px rgba(0,0,0,0.08);
  overflow: hidden;
}

/* --- Sidebar --- */
.profile-sidebar {
  flex: 0 0 320px;
  background-color: #fff;
  padding: 3rem 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.profile-pic {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #6F7D48;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  margin-bottom: 1.5rem;
}

.user-name {
  font-size: 1.6rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 0.25rem;
}

.user-penname {
  font-size: 1rem;
  color: #555;
  margin-bottom: 1.5rem;
}

.author-message {
  font-size: 0.9rem;
  line-height: 1.6;
  color: #555;
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 8px;
}

/* --- Content Area --- */
.content-area {
  flex: 1;
  padding: 3rem;
}

.content-section {
  padding: 0;
}

.section-title {
  font-size: 1.6rem;
  font-weight: 700;
  color: #6F7D48;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #6F7D48;
}

.book-list-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 1.5rem;
}

.book-item-card {
  width: 100%;
  height: 225px;
  border-radius: 8px;
  background-size: cover;
  background-position: center;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.book-item-card:hover {
  transform: scale(1.05);
}

.book-info-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  background: linear-gradient(to top, rgba(0,0,0,0.8), transparent);
  padding: 1.5rem 1rem 1rem;
  color: #fff;
}

.book-title {
  font-size: 1rem;
  font-weight: 700;
  margin: 0;
}

.no-content-message {
  text-align: center;
  color: #868e96;
  font-size: 1rem;
  padding: 3rem 0;
  background-color: #ffffff;
  border-radius: 8px;
  margin-top: 1.5rem;
}
</style>
