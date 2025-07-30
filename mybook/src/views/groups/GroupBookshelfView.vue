<template>
  <div class="group-bookshelf-page">
    <div v-if="group" class="group-header">
      <h1 class="group-title">{{ group.groupName }}</h1>
      <p class="group-description">{{ group.description || '그룹 설명이 없습니다.' }}</p>
      <div class="group-actions">
        <button @click="showInviteModal = true" class="btn btn-outline-primary">멤버 초대</button>
        <router-link :to="`/group-timeline/${groupId}`" class="btn btn-outline-secondary">그룹 타임라인 보기</router-link>
      </div>
    </div>

    <div v-if="group" class="bookshelf-container">
      <!-- Group Integrated Book -->
      <section class="bookshelf-section">
        <h2 class="section-title">그룹 통합 책</h2>
        <div v-if="integratedBook" class="integrated-book-wrapper">
          <router-link :to="`/integrated-group-book/${integratedBook.id}`" class="book-item integrated-book">
            {{ integratedBook.title }}
          </router-link>
        </div>
        <div v-else class="no-books-message">
          <p>아직 그룹 통합 책이 없습니다.</p>
          <router-link :to="`/group-book-creation?groupId=${groupId}`" class="btn btn-primary mt-2">통합 책 만들기</router-link>
        </div>
      </section>

      <!-- Member Books -->
      <section class="bookshelf-section">
        <h2 class="section-title">그룹 멤버들의 책</h2>
        <div v-if="memberBooks.length > 0" class="book-shelf">
          <router-link v-for="book in memberBooks" :key="book.id" :to="`/book-detail/${book.id}`" class="book-item member-book">
            <div class="book-title">{{ book.title }}</div>
            <div class="book-author">{{ book.authorName }}</div>
          </router-link>
        </div>
        <p v-else class="no-books-message">그룹 멤버들이 아직 발행한 책이 없습니다.</p>
      </section>
    </div>

    <div v-else class="loading-message">
      <p>그룹 정보를 불러오는 중입니다...</p>
    </div>

    <!-- Invite Modal -->
    <div v-if="showInviteModal" class="modal" style="display: flex;">
      <div class="modal-content">
        <span @click="showInviteModal = false" class="close-button">&times;</span>
        <h2 class="modal-title">그룹에 멤버 초대하기</h2>
        <div class="mb-3">
          <label for="invite-input" class="form-label">초대할 멤버의 아이디 또는 닉네임:</label>
          <input v-model="inviteQuery" type="text" class="form-control" placeholder="아이디 또는 닉네임 입력">
        </div>
        <button @click="inviteMember" class="btn btn-primary">초대 보내기</button>
      </div>
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
const showInviteModal = ref(false);
const inviteQuery = ref('');

// --- Functions ---
function fetchGroupData() {
  const foundGroup = DUMMY_GROUPS.find(g => g.id === groupId.value);
  if (foundGroup) {
    group.value = foundGroup;
    fetchBooks(foundGroup);
  } else {
    console.error('Group not found');
    router.push('/my-library'); // Redirect if group not found
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

function inviteMember() {
  if (!inviteQuery.value.trim()) {
    alert('초대할 멤버의 정보를 입력하세요.');
    return;
  }
  alert(`'${inviteQuery.value}'님을 초대했습니다. (더미 기능)`);
  showInviteModal.value = false;
  inviteQuery.value = '';
}

// --- Lifecycle Hooks ---
onMounted(() => {
  fetchGroupData();
});
</script>

<style scoped>
.group-bookshelf-page {
  padding: 80px 2rem 2rem;
  background-color: #F5F5DC;
  color: #3D2C20;
  min-height: calc(100vh - 56px);
}

.group-header {
  text-align: center;
  margin-bottom: 3rem;
  padding: 2rem;
  background-color: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
}

.group-title {
  font-size: 2.8rem;
  font-weight: 700;
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

.bookshelf-container {
  max-width: 1200px;
  margin: 0 auto;
}

.bookshelf-section {
  margin-bottom: 3rem;
}

.section-title {
  font-size: 1.8rem;
  font-weight: 600;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #B8860B;
  margin-bottom: 1.5rem;
}

.integrated-book-wrapper {
  display: flex;
  justify-content: center;
}

.book-shelf {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 1.5rem;
}

.book-item {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.05);
  text-align: center;
  padding: 1rem;
  transition: transform 0.3s, box-shadow 0.3s;
  text-decoration: none;
  color: inherit;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.book-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}

.integrated-book {
  background-color: #B8860B;
  color: #FFFFFF;
  font-weight: bold;
  font-size: 1.2rem;
  width: 200px;
  height: 280px;
}

.member-book {
  background-color: #8B4513;
  color: #F5F5DC;
  height: 200px;
}

.book-title {
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.book-author {
  font-size: 0.9rem;
  color: #D2B48C;
}

.no-books-message, .loading-message {
  text-align: center;
  padding: 2rem;
  background-color: #fff;
  border-radius: 8px;
  color: #5C4033;
}

/* Modal Styles */
.modal {
  position: fixed;
  z-index: 1000;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.6);
  justify-content: center;
  align-items: center;
}

.modal-content {
  background-color: #5C4033;
  padding: 2rem;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.5);
  position: relative;
  color: #F5F5DC;
}

.close-button {
  color: #F5F5DC;
  font-size: 28px;
  font-weight: bold;
  position: absolute;
  top: 10px;
  right: 20px;
  cursor: pointer;
}

.modal-title {
  font-size: 1.8rem;
  font-weight: bold;
  margin-bottom: 1.5rem;
  color: #B8860B;
}
</style>
