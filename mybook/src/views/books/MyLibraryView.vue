<template>
  <div class="my-library-page">
    <section class="content-section representative-book-section">
      <h2 class="section-title">나의 대표 인생책</h2>
      <p class="section-subtitle">당신의 삶에 가장 큰 영감을 준 책을 설정해보세요.</p>
      <div class="representative-book-carousel">
        <button @click="prevRepBook" :disabled="representativeBooks.length <= 1" class="carousel-btn">
          <i class="bi bi-chevron-left"></i>
        </button>
        <div class="representative-book-wrapper">
          <div class="representative-book">
            {{ representativeBooks.length > 0 ? representativeBooks[currentRepBookIndex] : '대표책이 없습니다' }}
          </div>
        </div>
        <button @click="nextRepBook" :disabled="representativeBooks.length <= 1" class="carousel-btn">
          <i class="bi bi-chevron-right"></i>
        </button>
      </div>
      <button @click="openRepBookModal" class="btn btn-primary select-rep-btn">
        <i class="bi bi-pencil-square"></i> 대표책 선택
      </button>
    </section>

    <section class="content-section book-shelves-section">
      <div class="group-shelf-header">
        <h2 class="section-title">나의 책장</h2>
        <button @click="isGroupModalVisible = true" class="btn btn-primary add-group-btn">
          <i class="bi bi-plus-lg"></i> 그룹 추가
        </button>
      </div>
      <p class="section-subtitle">그룹 책장을 만들어 친구들과 함께 책을 완성하고 공유해보세요.</p>

      <div class="my-books-shelf-wrapper">
        <h3 class="shelf-title">내가 쓴 책들
          <small>책을 드래그하여 아래 그룹에 추가하세요</small>
        </h3>
        <div class="book-shelf-container my-books-container">
          <draggable v-model="myBooks" item-key="id" :group="{ name: 'myBooksSource', pull: 'clone' }"
            class="book-shelf" tag="div" @start="isDraggingBook = true" @end="isDraggingBook = false">
            <template #item="{ element: book, index }">
              <router-link :to="`/book-detail/${book.id}`" class="book-item my-book"
                :class="`book-color-${(index % 4) + 1}`" :title="book.title">
                {{ book.title }}
                <i v-if="isBookInAnyGroup(book.id)" class="bi bi-link-45deg group-link-icon"></i>
              </router-link>
            </template>
          </draggable>
        </div>
      </div>

      <div v-if="allGroups.length === 0" class="no-groups-message">
        <p>아직 생성된 그룹이 없습니다.</p>
        <span>'그룹 추가' 버튼을 눌러 새로운 그룹을 만들어보세요.</span>
      </div>

      <div v-else class="group-shelves-container">
        <div v-for="group in allGroups" :key="group.id" class="group-shelf-wrapper">
          <div class="group-shelf-title-bar">
            <router-link :to="`/group-timeline/${group.id}`" class="group-shelf-title"
              :title="`${group.groupName} 타임라인으로 이동`">
              {{ group.groupName }}
            </router-link>
          </div>
          <div class="group-bookshelf-inner">
            <div class="book-shelf-container">
              <div class="book-shelf group-shelf-horizontal">
                <div v-for="member in group.members" :key="member" class="book-item member-book"
                  :class="{ 'member-book-registered': isMemberRegistered(group, member), 'member-book-unregistered': !isMemberRegistered(group, member) }"
                  :title="member">
                  {{ member }}
                </div>

                <draggable v-model="group.books" item-key="id"
                  :group="{ name: 'groupBooksTarget', pull: true, put: ['myBooksSource'] }"
                  class="group-books-draggable-area" tag="div" @add="handleBookDrop($event, group.id)"
                  @change="handleGroupBookChange($event, group.id)" @start="isDraggingBook = true"
                  @end="isDraggingBook = false">
                  <template #item="{ element: book, index }">
                    <div class="book-item my-book" :class="`book-color-${(index % 4) + 1}`" :title="book.title">
                      {{ book.title }}
                      <button @click.stop="removeBookFromGroup(group.id, book.id)" class="remove-book-btn"
                        title="그룹에서 책 제거">
                        <i class="bi bi-x"></i>
                      </button>
                    </div>
                  </template>
                </draggable>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <div v-if="isRepBookModalVisible || isGroupModalVisible || isMessageBoxVisible" class="modal-backdrop">
      <div class="modal-content" :class="{ 'modal-sm': isMessageBoxVisible }">
        <button @click="closeAllModals" class="close-button" title="닫기">
          <i class="bi bi-x-lg"></i>
        </button>

        <div v-if="isRepBookModalVisible">
          <h2 class="modal-title">대표 인생책 선택</h2>
          <p class="modal-description">Ctrl/Cmd 키를 누른 채 여러 책을 선택할 수 있습니다.</p>
          <select v-model="selectedRepBooks" multiple class="form-select modal-select">
            <option v-for="book in myBooks" :key="book.id" :value="book.title">{{ book.title }}</option>
          </select>
          <button @click="saveRepresentativeBooksHandler" class="btn btn-primary modal-action-btn">
            저장하기
          </button>
        </div>

        <div v-if="isGroupModalVisible">
          <h2 class="modal-title">새 그룹 만들기</h2>
          <div class="form-group">
            <label for="group-name-input" class="form-label">그룹 이름</label>
            <input v-model="newGroupName" id="group-name-input" type="text" class="form-control"
              placeholder="예: 독서 모임 A">
          </div>
          <div class="form-group">
            <label for="group-members-input" class="form-label">그룹 멤버 (쉼표로 구분)</label>
            <input v-model="newGroupMembers" id="group-members-input" type="text" class="form-control"
              placeholder="예: 김철수, 이영희, 박지민">
          </div>
          <button @click="createGroupHandler" class="btn btn-primary modal-action-btn">
            그룹 생성
          </button>
        </div>

        <div v-if="isMessageBoxVisible">
          <h2 class="modal-title">{{ messageBoxTitle }}</h2>
          <p class="modal-body">{{ messageBoxContent }}</p>
          <button @click="isMessageBoxVisible = false" class="btn btn-primary modal-action-btn">
            확인
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { RouterLink } from 'vue-router';
import draggable from 'vuedraggable';

// --- Interfaces ---
interface Book {
  id: string;
  title: string;
  authorId: string;
  authorName?: string;
}
interface Group {
  id: string;
  groupName: string;
  ownerId: string;
  managers: string[];
  members: string[];
  books: Book[];
  createdAt: Date;
}
interface DraggableEvent {
  added?: { element: Book; newIndex: number };
  removed?: { element: Book; oldIndex: number };
  moved?: { element: Book; newIndex: number; oldIndex: number };
}

// --- Dummy Data ---
const currentUserNickname = ref('김작가');
const DUMMY_MY_BOOKS: Book[] = [
  { id: 'mybook1', title: '나의 어린 시절 이야기', authorId: 'dummyUser1', authorName: '김작가' },
  { id: 'mybook2', title: '꿈을 향한 도전', authorId: 'dummyUser1', authorName: '김작가' },
  { id: 'mybook3', title: '여행의 기록', authorId: 'dummyUser1', authorName: '김작가' },
  { id: 'mybook4', title: '개발자의 삶', authorId: 'dummyUser1', authorName: '김작가' },
];
const DUMMY_GROUPS: Group[] = [
  { id: 'group1', groupName: '독서 토론 모임', ownerId: '김작가', managers: ['이영희'], members: ['김작가', '이영희', '박철수'], books: [{ id: 'mybook1', title: '나의 어린 시절 이야기', authorId: 'dummyUser1', authorName: '김작가' }], createdAt: new Date() },
  { id: 'group2', groupName: '글쓰기 동호회', ownerId: '김작가', managers: [], members: ['김작가', '최수진'], books: [], createdAt: new Date() },
  { id: 'group3', groupName: '여행 에세이 클럽', ownerId: '정민준', managers: [], members: ['정민준', '김작가', '하은지'], books: [{ id: 'mybook3', title: '여행의 기록', authorId: 'dummyUser1', authorName: '김작가' }], createdAt: new Date() },
];

// --- Reactive State ---
const representativeBooks = ref<string[]>(['나의 어린 시절 이야기']);
const currentRepBookIndex = ref(0);
const myBooks = ref<Book[]>(DUMMY_MY_BOOKS);
const allGroups = ref<Group[]>(DUMMY_GROUPS);
const isDraggingBook = ref(false);
const isRepBookModalVisible = ref(false);
const selectedRepBooks = ref<string[]>([]);
const isGroupModalVisible = ref(false);
const newGroupName = ref('');
const newGroupMembers = ref('');
const isMessageBoxVisible = ref(false);
const messageBoxTitle = ref('');
const messageBoxContent = ref('');

// --- Computed Properties ---
const isBookInAnyGroup = computed(() => (bookId: string) => {
  return allGroups.value.some(group => group.books.some(book => book.id === bookId));
});

// --- Functions ---
function closeAllModals() {
  isRepBookModalVisible.value = false;
  isGroupModalVisible.value = false;
  isMessageBoxVisible.value = false;
}
function showMessageBox(message: string, title = '알림') {
  messageBoxTitle.value = title;
  messageBoxContent.value = message;
  isMessageBoxVisible.value = true;
}
function handleBookDrop(event: DraggableEvent, groupId: string) {
  if (!event.added) return;
  const bookTitle = event.added.element.title;
  const group = allGroups.value.find(g => g.id === groupId);
  if (group) {
    const isAlreadyInGroup = group.books.filter(b => b.id === event.added!.element.id).length > 1;
    if (!isAlreadyInGroup) {
      showMessageBox(`'${bookTitle}'을(를) '${group.groupName}' 그룹에 추가했습니다.`);
    }
  }
}
function handleGroupBookChange(event: DraggableEvent, groupId: string) {}
function removeBookFromGroup(groupId: string, bookId: string) {
  const group = allGroups.value.find(g => g.id === groupId);
  if (group) {
    const book = group.books.find(b => b.id === bookId);
    if (book && confirm(`'${book.title}' 책을 '${group.groupName}' 그룹에서 제거하시겠습니까?`)) {
      group.books = group.books.filter(b => b.id !== bookId);
      showMessageBox(`책이 '${group.groupName}' 그룹에서 제거되었습니다.`);
    }
  }
}
function isMemberRegistered(group: Group, memberName: string): boolean {
  return group.members.includes(memberName);
}
function prevRepBook() {
  if (representativeBooks.value.length > 0) {
    currentRepBookIndex.value = (currentRepBookIndex.value - 1 + representativeBooks.value.length) % representativeBooks.value.length;
  }
}
function nextRepBook() {
  if (representativeBooks.value.length > 0) {
    currentRepBookIndex.value = (currentRepBookIndex.value + 1) % representativeBooks.value.length;
  }
}
function openRepBookModal() {
  selectedRepBooks.value = [...representativeBooks.value];
  isRepBookModalVisible.value = true;
}
function saveRepresentativeBooksHandler() {
  representativeBooks.value = [...selectedRepBooks.value];
  currentRepBookIndex.value = 0;
  isRepBookModalVisible.value = false;
  showMessageBox('대표책이 저장되었습니다.');
}
function createGroupHandler() {
  const groupName = newGroupName.value.trim();
  const members = newGroupMembers.value.split(',').map(m => m.trim()).filter(m => m);
  if (!members.includes(currentUserNickname.value)) {
    members.unshift(currentUserNickname.value);
  }
  if (!groupName) {
    showMessageBox('그룹 이름을 입력해주세요.', '경고');
    return;
  }
  if (members.length === 0) {
    members.push(currentUserNickname.value);
  }
  const newGroup: Group = {
    id: `group${Date.now()}`,
    groupName: groupName,
    ownerId: currentUserNickname.value,
    managers: [],
    members: members,
    books: [],
    createdAt: new Date(),
  };
  allGroups.value.unshift(newGroup);
  isGroupModalVisible.value = false;
  newGroupName.value = '';
  newGroupMembers.value = '';
  showMessageBox('그룹이 성공적으로 생성되었습니다.');
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&family=Pretendard:wght@400;500;700&display=swap');
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

/* --- Base Page Layout --- */
.my-library-page {
  padding: 80px 2rem 2rem;
  background-color: #F5F5F3;
  color: #261E17;
  min-height: calc(100vh - 56px);
  font-family: 'Pretendard', sans-serif;
}
.content-section {
  background: #FFFFFF;
  border-radius: 16px;
  padding: 2.5rem;
  margin: 0 auto 3rem auto;
  max-width: 1200px;
  box-shadow: 0 8px 25px rgba(38, 30, 23, 0.08);
  border: 1px solid #E0E0E0;
}
.section-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 0.75rem;
  text-align: center;
}
.section-subtitle {
  font-size: 1.1rem;
  opacity: 0.8;
  margin-bottom: 3rem;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.7;
  text-align: center;
}

/* --- Section Title Colors --- */
.representative-book-section .section-title {
  color: #594C40;
}
.book-shelves-section .section-title {
  color: #F5F5F3;
}

/* --- General Button Styles --- */
.btn {
  border: 1px solid #E0E0E0;
  background-color: #fff;
  color: #594C40;
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  font-size: 0.95rem;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: 0 2px 4px rgba(38, 30, 23, 0.08);
}
.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(38, 30, 23, 0.08);
  border-color: #594C40;
}
.btn-primary {
  background-color: #D4A373;
  border-color: #D4A373;
  color: #FFFFFF;
}
.btn-primary:hover {
  background-color: #c79561;
  border-color: #c79561;
}

/* --- Representative Book Section --- */
.representative-book-carousel {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1.5rem;
  margin-bottom: 2rem;
}
.carousel-btn {
  background-color: #FFFFFF;
  border: 1px solid #E0E0E0;
  color: #261E17;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  font-size: 1.5rem;
}
.carousel-btn:hover:not(:disabled) {
  background-color: #D4A373;
  color: #FFFFFF;
}
.carousel-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.representative-book-wrapper {
  perspective: 1000px;
}
.representative-book {
  width: 180px;
  height: 250px;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  background-color: #594C40;
  color: #D4A373;
  font-family: 'Noto Serif KR', serif;
  font-size: 1.5rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 1.5rem;
  writing-mode: vertical-rl;
  text-orientation: upright;
  word-break: break-word;
  transition: transform 0.4s ease;
  border: 2px solid rgba(0, 0, 0, 0.2);
}
.representative-book-wrapper:hover .representative-book {
  transform: rotateY(10deg);
}
.select-rep-btn {
  display: block;
  margin: 0 auto;
}

/* --- Centered Bookshelves Section --- */
.book-shelves-section {
  background-color: #8C6A56;
  border: 4px solid #594C40; /* 👈 양옆 테두리 추가 */
  padding-bottom: 2.5rem; /* 아래쪽 패딩 추가 */
}
.book-shelves-section .section-subtitle,
.book-shelves-section .shelf-title,
.book-shelves-section .shelf-title small {
  color: #F5F5F3;
}
.book-shelves-section .btn-primary {
  background-color: #D4A373;
  border-color: #D4A373;
  color: #261E17;
}

/* --- Sticky "My Books" Shelf --- */
.my-books-shelf-wrapper {
  position: sticky;
  top: 56px;
  z-index: 100;
  background-color: #8C6A56;
  padding: 2.5rem 0 1.5rem 0;
  margin: 0;
  border: none;
  box-shadow: none;
}
.shelf-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 1.4rem;
  font-weight: 600;
  margin-bottom: 1rem;
}
.shelf-title small {
  font-family: 'Pretendard', sans-serif;
  font-size: 0.9rem;
  font-weight: 400;
  opacity: 0.7;
  margin-left: 0.75rem;
}

/* --- Book Shelves & Items --- */
.book-shelf-container {
  padding: 1rem;
}
.my-books-container {
  background-color: #594C40;
  border-radius: 8px;
  box-shadow: inset 0 4px 8px rgba(0, 0, 0, 0.3);
  border: 2px solid #4a3f35;
}
.book-shelf {
  display: flex;
  gap: 1.25rem;
  overflow-x: auto;
  min-height: 180px;
  align-items: flex-end;
  padding-bottom: 10px;
}
.book-shelf::-webkit-scrollbar { height: 10px; }
.book-shelf::-webkit-scrollbar-track { background: transparent; }
.book-shelf::-webkit-scrollbar-thumb {
  background: #D4A373;
  border-radius: 5px;
  border: 2px solid #594C40;
}
.book-item {
  flex-shrink: 0;
  width: 45px;
  height: 160px;
  padding-top: 15px;
  border-radius: 4px 4px 2px 2px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  text-align: center;
  font-size: 1rem;
  font-weight: 600;
  cursor: grab;
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
  writing-mode: vertical-rl;
  text-orientation: mixed;
  text-decoration: none;
  position: relative;
  overflow: hidden;
  white-space: nowrap;
  border: 1px solid rgba(0, 0, 0, 0.4);
}
.book-item:hover {
  transform: translateY(-8px) scale(1.05);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
}
.sortable-ghost { opacity: 0.4; }

.book-color-1 { background-color: #8E6E53; color: #fff; }
.book-color-2 { background-color: #E4C5AF; color: #261E17; }
.book-color-3 { background-color: #6D7275; color: #fff; }
.book-color-4 { background-color: #B0A295; color: #261E17; }

.member-book { cursor: default; height: 150px; }
.member-book-registered { background-color: #D4A373; color: #FFFFFF; }
.member-book-unregistered { background-color: #ccc; color: #555; opacity: 0.8; }
.group-link-icon {
  position: absolute;
  bottom: 5px;
  right: 5px;
  font-size: 1.2rem;
  writing-mode: horizontal-tb;
  transform: rotate(45deg);
}
.remove-book-btn {
  position: absolute;
  top: 2px;
  right: 2px;
  background-color: rgba(0, 0, 0, 0.4);
  color: white;
  border: none;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s;
  writing-mode: horizontal-tb;
}
.book-item:hover .remove-book-btn { opacity: 1; }
.remove-book-btn:hover { background-color: rgba(239, 68, 68, 1); transform: scale(1.1); }

/* --- Group Shelves Section --- */
.group-shelf-header {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.5rem;
}
.group-shelves-container {
  margin-top: 2.5rem;
  display: flex;
  flex-direction: column;
  gap: 2.5rem;
}
.group-shelf-wrapper {
  background: none;
  border: none;
  position: relative;
  padding: 0;
}
.group-shelf-title-bar {
  display: flex;
  align-items: center;
  margin-bottom: 0.7rem;
  gap: 1rem;
}
.group-shelf-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 1.4rem;
  font-weight: 700;
  color: #F5F5F3;
  text-decoration: none;
  transition: color 0.2s;
}
.group-shelf-title:hover {
  color: #D4A373;
  text-decoration: underline;
}
.group-bookshelf-inner {
  background-color: #594C40;
  border-radius: 8px;
  box-shadow: inset 0 4px 12px rgba(0, 0, 0, 0.18);
  padding: 1rem 0.5rem;
  border: 2px solid #4a3f35;
}
.group-shelf-wrapper .book-shelf-container {
  background-color: transparent;
  border: none;
  box-shadow: none;
  padding: 0;
}
.group-shelf-horizontal {
  display: flex;
  align-items: flex-end;
  gap: 1.25rem;
  overflow-x: auto;
  min-height: 180px;
  padding-bottom: 1px;
}
.group-shelf-horizontal::-webkit-scrollbar { height: 10px; }
.group-shelf-horizontal::-webkit-scrollbar-track { background: transparent; }
.group-shelf-horizontal::-webkit-scrollbar-thumb {
  background: #D4A373;
  border-radius: 5px;
  border: 2px solid #594C40;
}
.group-books-draggable-area {
  display: flex;
  gap: 1.25rem;
  flex-grow: 1;
  min-width: 120px;
  min-height: 170px;
  border-radius: 6px;
  align-items: flex-end;
}
.no-groups-message {
  text-align: center;
  color: #F5F5F3;
  padding: 2rem;
  border: 2px dashed #F5F5F3;
  border-radius: 8px;
  margin-top: 2rem;
}


/* --- Modal Styles --- */
.modal-backdrop {
  position: fixed;
  z-index: 1000;
  inset: 0;
  background-color: rgba(38, 30, 23, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
}
.modal-content {
  background-color: #FFFFFF;
  margin: 1rem;
  padding: 2rem 2.5rem;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.3);
  position: relative;
  border: 1px solid #E0E0E0;
}
.modal-content.modal-sm { max-width: 400px; }
.close-button {
  color: #594C40;
  position: absolute;
  top: 1rem;
  right: 1rem;
  cursor: pointer;
  background: none;
  border: none;
  font-size: 1.5rem;
  line-height: 1;
  padding: 0.5rem;
}
.close-button:hover { color: #261E17; }
.modal-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  text-align: center;
}
.modal-description,
.modal-body {
  margin-bottom: 1.5rem;
  opacity: 0.8;
  text-align: center;
  line-height: 1.6;
}
.modal-select {
  background-color: #F5F5F3;
  border: 1px solid #E0E0E0;
  border-radius: 8px;
  padding: 0.5rem;
  width: 100%;
  height: 180px;
  margin-bottom: 1.5rem;
}
.modal-select:focus,
.form-control:focus {
  outline: none;
  border-color: #D4A373;
  box-shadow: 0 0 0 3px rgba(212, 163, 115, 0.4);
}
.form-group {
  margin-bottom: 1.25rem;
  text-align: left;
}
.form-label {
  display: block;
  font-weight: 600;
  margin-bottom: 0.5rem;
  font-size: 1rem;
}
.form-control {
  background-color: #F5F5F3;
  border: 1px solid #E0E0E0;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  width: 100%;
  box-sizing: border-box;
  font-size: 1rem;
}
.modal-action-btn {
  width: 100%;
  padding: 0.8rem;
  font-size: 1.05rem;
  margin-top: 0.5rem;
}
</style>