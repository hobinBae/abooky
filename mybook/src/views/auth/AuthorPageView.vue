<template>
  <div class="auth-page my-page">
    <div v-if="isLoading" class="loading-container">
      <p>작가 정보를 불러오는 중입니다...</p>
    </div>
    <div v-else-if="authorProfile" class="auth-wrapper my-page-wrapper">
      <!-- 왼쪽 프로필 섹션 -->
      <div class="auth-image-section my-page-profile-sidebar">
        <section class="profile-section">
          <img :src="authorProfile.profileImageUrl && authorProfile.profileImageUrl.trim() ? authorProfile.profileImageUrl : '/images/profile.png'" alt="Profile Picture" class="profile-pic">
          <div class="user-info">
            <h2 class="user-name">{{ authorProfile.name }}</h2>
            <p class="user-penname">@{{ authorProfile.nickname }}</p>
          </div>
          <div v-if="authorProfile.intro" class="author-message-box">
            <p class="author-message-content">{{ authorProfile.intro }}</p>
          </div>
        </section>
      </div>

      <!-- 오른쪽 콘텐츠 섹션 -->
      <div class="auth-container my-page-container">
        <section class="content-section">
          <h3 class="section-title">서점에 출판한 책</h3>
          <div v-if="authoredBooks.length > 0" class="book-list-grid">
            <router-link v-for="book in authoredBooks" :key="book.communityBookId" :to="`/bookstore/book/${book.communityBookId}`" class="book-cover-link">
              <div class="book-cover-image"
                :style="{ backgroundImage: `url(${book.coverImageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
                <div class="list-title-box">
                  <p class="list-title-box-title">{{ book.title }}</p>
                </div>
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
import { communityService, type CommunityBook } from '@/services/communityService';

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
const authoredBooks = ref<CommunityBook[]>([]);
const isLoading = ref(true);

const fetchAuthorData = async () => {
  if (!authorId.value) return;
  isLoading.value = true;

  try {
    const profilePromise = apiClient.get(`/api/v1/members/${authorId.value}`);
    const booksPromise = communityService.getMemberCommunityBooks(Number(authorId.value), { size: 100 }); // 충분히 큰 사이즈

    const [profileResponse, booksResponse] = await Promise.all([profilePromise, booksPromise]);

    authorProfile.value = profileResponse.data.data;
    authoredBooks.value = booksResponse.content;

  } catch (error) {
    console.error(`Failed to fetch author data for ID ${authorId.value}:`, error);
    authorProfile.value = null;
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchAuthorData);
watch(authorId, fetchAuthorData);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Pretendard:wght@400;500;700&display=swap');
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

.auth-page {
  padding: 2rem;
  color: #333;
  min-height: 100vh;
  font-family: 'SCDream4', sans-serif;
  display: flex;
  align-items: flex-start; /* 위쪽으로 정렬 */
  justify-content: center;
}

.loading-container {
  text-align: center;
  padding: 4rem;
  font-size: 1.2rem;
  color: #555;
}

.auth-wrapper {
  display: flex;
  width: 100%;
  max-width: 819px;
  min-height: 576px;
  background-color: #fff;
  border-radius: 13px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  border: 2px solid #6F7D48;
}

.my-page-profile-sidebar {
  flex: 0 0 256px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 2rem;
  background-color: #f4f3e8;
  border-right: 1px solid #e8e7dc;
}

.my-page-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 2rem 2.4rem;
}

/* --- 프로필 섹션 --- */
.profile-section {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.profile-pic {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  margin-bottom: 0.8rem;
}

.user-name {
  font-size: 1.44rem;
  font-weight: 700;
  margin-bottom: 0.2rem;
}

.user-penname {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 1.2rem;
}

.author-message-box {
  width: 100%;
  background-color: rgba(255, 255, 255, 0.5);
  padding: 0.8rem;
  border-radius: 6px;
  font-size: 0.7rem;
  line-height: 1.6;
  color: #555;
  white-space: pre-wrap;
  margin-bottom: 1.2rem;
}

/* --- 콘텐츠 섹션 --- */
.content-section {
  width: 100%;
  padding: 1.2rem 0;
}

.section-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 1.2rem;
  padding-bottom: 0.6rem;
  border-bottom: 1px solid #eee;
}

/* --- 책 리스트 --- */
.book-list-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(96px, 1fr));
  gap: 1.2rem;
}

.book-cover-link {
  text-decoration: none;
}

.book-cover-image {
  width: 96px;
  height: 144px;
  object-fit: cover;
  border-radius: 3px;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-size: cover;
  background-position: center;
  position: relative;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.book-cover-image:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.list-title-box {
  width: 80%;
  height: 70%;
  background-color: rgba(255, 255, 255, 0.95);
  padding: 8px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  text-align: left;
  color: #333;
  border-radius: 2px;
  opacity: 1;
  transition: opacity 0.3s ease;
}

.list-title-box-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 10px;
  font-weight: 600;
  line-height: 1.4;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.no-content-message {
  text-align: center;
  color: #868e96;
  font-size: 0.8rem;
  padding: 2.4rem 0;
}

/* --- 반응형 --- */
@media (max-width: 992px) {
  .auth-wrapper {
    flex-direction: column;
    min-height: auto;
    max-width: 480px;
  }
  .my-page-profile-sidebar {
    border-right: none;
    border-bottom: 1px solid #e8e7dc;
  }
}

@media (max-width: 576px) {
  .auth-page {
    padding: 1rem;
  }
  .my-page-container {
    padding: 1.2rem;
  }
  .book-list-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.8rem;
  }
}
</style>
