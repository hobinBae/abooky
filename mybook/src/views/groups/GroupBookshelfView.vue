<template>
  <div class="page-container group-bookshelf-page">
    <div v-if="group" class="content-wrapper">
      <!-- Group Header -->
      <div class="group-header card">
        <h1 class="group-title">{{ group.groupName }}</h1>
        <p class="group-description">{{ group.description || '그룹 설명이 없습니다.' }}</p>
        <div class="group-actions">
          <button @click="showInviteModal = true" class="btn btn-primary">멤버 초대</button>
          <router-link :to="`/group-timeline/${groupId}`" class="btn btn-secondary">그룹 타임라인</router-link>
        </div>
      </div>

      <!-- Bookshelf Container -->
      <div class="bookshelf-container">
        <!-- Group Integrated Book -->
        <section class="bookshelf-section card">
          <h2 class="card-header">그룹 통합 책</h2>
          <div class="card-body">
            <div v-if="integratedBook" class="integrated-book-wrapper">
              <router-link :to="`/integrated-group-book/${integratedBook.id}`" class="book-item integrated-book">
                <div class="book-cover"></div>
                <div class="book-title">{{ integratedBook.title }}</div>
              </router-link>
            </div>
            <div v-else class="no-books-message">
              <p>아직 그룹 통합 책이 없습니다.</p>
              <router-link :to="`/group-book-creation?groupId=${groupId}`" class="btn btn-primary mt-2">통합 책 만들기</router-link>
            </div>
          </div>
        </section>

        <!-- Member Books -->
        <section class="bookshelf-section card">
          <h2 class="card-header">그룹 멤버들의 책</h2>
          <div class="card-body">
            <div v-if="memberBooks.length > 0" class="book-shelf">
              <router-link v-for="book in memberBooks" :key="book.id" :to="`/book-detail/${book.id}`" class="book-item member-book">
                <div class="book-cover"></div>
                <div class="book-title">{{ book.title }}</div>
                <div class="book-author">{{ book.authorName }}</div>
              </router-link>
            </div>
            <p v-else class="no-books-message">그룹 멤버들이 아직 발행한 책이 없습니다.</p>
          </div>
        </section>
      </div>
    </div>

    <!-- Loading Message -->
    <div v-else class="loading-message">
      <p>그룹 정보를 불러오는 중입니다...</p>
    </div>

    
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter, RouterLink } from 'vue-router';

// --- Interfaces ---
interface Group {
  id: string;
  groupName: string;
  description?: string;
  members: string[]; // Array of user IDs or names
  integratedBookId?: string;
}

interface Book {
  id: string;
  title: string;
  authorId: string;
  authorName?: string;
}

// --- Dummy Data ---
const DUMMY_GROUPS: Group[] = [
  {
    id: 'group_dummy_1',
    groupName: '우리들의 이야기 공작소',
    description: '글과 그림으로 우리만의 세계를 만들어가는 창작 모임입니다.',
    members: ['글쓰는 여행가', '그림 그리는 이야기꾼'],
    integratedBookId: 'integrated_book_1',
  },
  {
    id: 'group_dummy_2',
    groupName: '역사 탐험대',
    description: '역사 속 숨겨진 이야기를 찾아 책으로 기록합니다.',
    members: ['역사 덕후', '고고학자'],
    integratedBookId: 'integrated_book_2',
  },
  {
    id: 'group_dummy_3',
    groupName: '판타지 소설 창작팀',
    description: '상상의 세계를 현실로 만드는 판타지 소설 집필 그룹입니다.',
    members: ['판타지 마스터', '세계관 설계자'],
  },
];

const DUMMY_BOOKS: Book[] = [
  { id: 'book_dummy_1', title: '나의 첫 유럽 여행기', authorId: 'user_dummy_1', authorName: '글쓰는 여행가' },
  { id: 'book_dummy_2', title: '코딩, 내 인생의 새로운 챕터', authorId: 'user_dummy_2', authorName: '그림 그리는 이야기꾼' },
  { id: 'book_dummy_3', title: '제주도 한 달 살기 (초안)', authorId: 'user_dummy_1', authorName: '글쓰는 여행가' },
  { id: 'integrated_book_1', title: '공작소의 첫 작품', authorId: 'group_dummy_1', authorName: '공동 집필' },
  { id: 'integrated_book_2', title: '조선 왕조 비사', authorId: 'group_dummy_2', authorName: '공동 집필' },
];

// --- Router ---
const route = useRoute();
const router = useRouter();
const groupId = ref(route.params.id as string);

// --- Reactive State ---
const group = ref<Group | null>(null);
const integratedBook = ref<Book | null>(null);
const memberBooks = ref<Book[]>([]);


// --- Functions ---
function fetchGroupData() {
  let currentGroupId = groupId.value;
  if (!currentGroupId) {
    // 임시로 첫 번째 더미 그룹 ID 사용
    currentGroupId = DUMMY_GROUPS[0]?.id; 
    if (!currentGroupId) {
      console.error('No dummy groups available.');
      router.push('/my-library');
      return;
    }
  }

  const foundGroup = DUMMY_GROUPS.find(g => g.id === currentGroupId);
  if (foundGroup) {
    group.value = foundGroup;
    fetchBooks(foundGroup);
  } else {
    console.error('Group not found for ID:', currentGroupId);
    router.push('/my-library'); // Still redirect if a specific ID is given but not found
  }
}

function fetchBooks(currentGroup: Group) {
  // Fetch integrated book if it exists
  if (currentGroup.integratedBookId) {
    integratedBook.value = DUMMY_BOOKS.find(b => b.id === currentGroup.integratedBookId) || null;
  }

  // Fetch member books (simplified: just find books by authorName in dummy members)
  memberBooks.value = DUMMY_BOOKS.filter(book => 
    currentGroup.members.includes(book.authorName || '') && book.id.startsWith('book_dummy')
  );
}



// --- Lifecycle Hooks ---
onMounted(() => {
  fetchGroupData();
});
</script>

<style scoped>
/* General Page Styles */
.page-container {
  padding: 80px 2rem 2rem; /* Add top padding for navbar */
  background-color: #F5F5DC;
  min-height: 100vh;
  color: #3D2C20;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
}

.card {
  background-color: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.07);
  border: 1px solid #EFEBE9;
  margin-bottom: 2rem;
}

.card-header {
  font-size: 1.5rem;
  font-weight: 700;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #EFEBE9;
  margin-bottom: 0;
}

.card-body {
  padding: 1.5rem;
}

/* Group Header */
.group-header {
  text-align: center;
  padding: 2rem;
}

.group-title {
  font-size: 2.5rem;
  font-weight: 800;
  color: #3D2C20;
}

.group-description {
  font-size: 1.1rem;
  color: #5C4033;
  margin-top: 0.5rem;
  margin-bottom: 1.5rem;
}

.group-actions .btn {
  margin: 0 0.5rem;
}

/* Bookshelf */
.book-shelf {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 1.5rem;
}

.integrated-book-wrapper {
  display: flex;
  justify-content: center;
}

.book-item {
  text-decoration: none;
  color: inherit;
  text-align: center;
  transition: transform 0.3s, box-shadow 0.3s;
}

.book-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}

.book-cover {
  width: 100%;
  padding-top: 140%; /* Aspect ratio for book cover */
  background-color: #EFEBE9;
  border-radius: 8px;
  margin-bottom: 1rem;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.integrated-book .book-cover {
  background-color: #B8860B;
}

.member-book .book-cover {
  background-color: #8B4513;
}

.book-title {
  font-weight: 600;
  font-size: 1rem;
  margin-bottom: 0.25rem;
}

.book-author {
  font-size: 0.9rem;
  color: #5C4033;
}

.no-books-message {
  text-align: center;
  padding: 2rem;
  color: #5C4033;
}

.loading-message {
  text-align: center;
  padding-top: 5rem;
  font-size: 1.2rem;
}

/* Modal Styles (Bootstrap-like) */
.modal-backdrop {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1050;
}

.modal-dialog {
  width: 100%;
  max-width: 500px;
  margin: 1.75rem auto;
}

.modal-content {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  pointer-events: auto;
  background-color: #fff;
  background-clip: padding-box;
  border: 1px solid rgba(0,0,0,.2);
  border-radius: 0.5rem;
  outline: 0;
}

.modal-header {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1rem;
  border-bottom: 1px solid #dee2e6;
  border-top-left-radius: calc(0.5rem - 1px);
  border-top-right-radius: calc(0.5rem - 1px);
}

.modal-title { font-size: 1.25rem; font-weight: 500; }
.modal-body { position: relative; flex: 1 1 auto; padding: 1rem; }
.modal-footer {
  display: flex;
  flex-wrap: wrap;
  flex-shrink: 0;
  align-items: center;
  justify-content: flex-end;
  padding: 0.75rem;
  border-top: 1px solid #dee2e6;
}
.modal-footer > .btn { margin: 0.25rem; }

.btn-close {
  padding: 0.5rem;
  margin: -0.5rem -0.5rem -0.5rem auto;
}
</style>
