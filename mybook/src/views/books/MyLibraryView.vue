<template>
  <div class="my-library-page">
    <!-- Representative Life Book Section -->
    <section class="representative-book-section">
      <h2 class="section-title">나의 대표 인생책</h2>
      <div class="representative-book-carousel">
        <button @click="prevRepBook" :disabled="representativeBooks.length <= 1" class="carousel-btn">
          <i class="fas fa-chevron-left"></i>
        </button>
        <div class="representative-book" :style="{ backgroundColor: representativeBooks.length > 0 ? '#5a2e2e' : '#777' }">
          {{ representativeBooks.length > 0 ? representativeBooks[currentRepBookIndex] : '대표책이 없습니다' }}
        </div>
        <button @click="nextRepBook" :disabled="representativeBooks.length <= 1" class="carousel-btn">
          <i class="fas fa-chevron-right"></i>
        </button>
      </div>
      <button @click="openRepBookModal" class="btn btn-primary select-rep-btn">
        대표책 선택
      </button>
    </section>

    <!-- My Books Shelf (Draggable) -->
    <div class="my-books-shelf-wrapper">
      <h3 class="shelf-subtitle">내가 쓴 책들 (책을 드래그하여 아래 그룹에 추가하세요)</h3>
      <draggable v-model="myBooks" item-key="id" :group="{ name: 'myBooksSource', pull: 'clone' }" class="book-shelf" tag="div" @start="isDraggingBook = true" @end="isDraggingBook = false">
        <template #item="{element: book}">
          <router-link :to="`/book-detail/${book.id}`" class="book-item my-book" :title="book.title">
            {{ book.title }}
            <i v-if="isBookInAnyGroup(book.id)" class="fas fa-link group-link-icon"></i>
          </router-link>
        </template>
      </draggable>
    </div>

    <!-- Book Shelves Section -->
    <section class="book-shelves-section">
      <h2 class="section-title">나의 책장</h2>

      

      <!-- Group Books Section (Droppable) -->
      <div class="group-books-wrapper">
        <h3 class="shelf-subtitle group-shelf-header">
          그룹 책장
          <button @click="isGroupModalVisible = true" class="btn btn-success add-group-btn">
            <i class="fas fa-plus me-2"></i>그룹 추가
          </button>
        </h3>
        <div class="group-shelves-container">
            <div v-for="group in allGroups" :key="group.id" class="group-shelf-wrapper">
                <!-- This div will act as the single horizontal bookshelf for group name, members, and added books -->
                <div class="book-shelf group-shelf-horizontal">
                    <!-- Group Name as the first, non-draggable book -->
                    <router-link :to="`/group-timeline/${group.id}`" class="book-item group-name-book" :title="group.groupName">
                        {{ group.groupName }}
                    </router-link>

                    <!-- Group Members as non-draggable books -->
                    <div v-for="member in group.members" :key="member" 
                         class="book-item member-book"
                         :class="{'member-book-registered': isMemberRegistered(group, member), 'member-book-unregistered': !isMemberRegistered(group, member)}"
                         :title="member">
                        {{ member }}
                    </div>

                    <!-- Draggable area for books in this group -->
                    <draggable v-model="group.books" item-key="id" :group="{ name: 'groupBooksTarget', pull: true, put: ['myBooksSource'] }" class="group-books-draggable-area" tag="div" @add="handleBookDrop($event, group.id)" @change="handleGroupBookChange($event, group.id)" @start="isDraggingBook = true" @end="isDraggingBook = false">
                        <template #item="{element: book}">
                            <div class="book-item my-book" :title="book.title">
                                {{ book.title }}
                                <button @click.stop="removeBookFromGroup(group.id, book.id)" class="remove-book-btn">x</button>
                            </div>
                        </template>
                    </draggable>
                </div>
            </div>
        </div>
        </div>
    </section>

    <!-- Modals -->
    <div v-if="isRepBookModalVisible" class="modal" style="display: flex;">
      <div class="modal-content">
        <span @click="isRepBookModalVisible = false" class="close-button">&times;</span>
        <h2 class="modal-title">대표 인생책 선택</h2>
        <p class="modal-description">Ctrl/Cmd 키를 누른 채 여러 책을 선택하세요:</p>
        <select v-model="selectedRepBooks" multiple class="form-select modal-select">
          <option v-for="book in myBooks" :key="book.id" :value="book.title">{{ book.title }}</option>
        </select>
        <button @click="saveRepresentativeBooksHandler" class="btn btn-primary modal-save-btn">
          저장
        </button>
      </div>
    </div>

    <div v-if="isGroupModalVisible" class="modal" style="display: flex;">
      <div class="modal-content">
        <span @click="isGroupModalVisible = false" class="close-button">&times;</span>
        <h2 class="modal-title">새 그룹 만들기</h2>
        <div class="mb-3">
          <label for="group-name-input" class="form-label">그룹 이름:</label>
          <input v-model="newGroupName" type="text" class="form-control" placeholder="예: 독서 모임 A">
        </div>
        <div class="mb-3">
          <label for="group-members-input" class="form-label">그룹 멤버 (쉼표로 구분):</label>
          <input v-model="newGroupMembers" type="text" class="form-control" placeholder="예: 김철수, 이영희, 박지민">
        </div>
        <button @click="createGroupHandler" class="btn btn-success modal-save-btn">
          그룹 생성
        </button>
      </div>
    </div>

    <div v-if="isMessageBoxVisible" class="modal" style="display: flex;">
      <div class="modal-content modal-sm">
        <span @click="isMessageBoxVisible = false" class="close-button">&times;</span>
        <h2 class="modal-title">{{ messageBoxTitle }}</h2>
        <p class="modal-body">{{ messageBoxContent }}</p>
        <button @click="isMessageBoxVisible = false" class="btn btn-primary modal-confirm-btn">
          확인
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
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
  members: string[];
  books: Book[]; // Added to hold books for the group
  createdAt: Date;
}

interface DraggableEvent {
  added?: { element: Book; newIndex: number };
  removed?: { element: Book; oldIndex: number };
  moved?: { element: Book; newIndex: number; oldIndex: number };
}

// --- Dummy Data ---
const DUMMY_MY_BOOKS: Book[] = [
  { id: 'mybook1', title: '나의 어린 시절 이야기', authorId: 'dummyUser1', authorName: '김작가' },
  { id: 'mybook2', title: '꿈을 향한 도전', authorId: 'dummyUser1', authorName: '김작가' },
  { id: 'mybook3', title: '여행의 기록', authorId: 'dummyUser1', authorName: '김작가' },
  { id: 'mybook4', title: '개발자의 삶', authorId: 'dummyUser1', authorName: '김작가' },
];

const DUMMY_GROUPS: Group[] = [
  { id: 'group1', groupName: '독서 토론 모임', members: ['김작가', '이영희', '박철수'], books: [{ id: 'mybook1', title: '나의 어린 시절 이야기', authorId: 'dummyUser1', authorName: '김작가' }], createdAt: new Date() },
  { id: 'group2', groupName: '글쓰기 동호회', members: ['김작가', '최수진'], books: [], createdAt: new Date() },
  { id: 'group3', groupName: '여행 에세이 클럽', members: ['김작가', '정민준', '하은지'], books: [{ id: 'mybook3', title: '여행의 기록', authorId: 'dummyUser1', authorName: '김작가' }], createdAt: new Date() },
  { id: 'group4', groupName: '코딩 스터디', members: ['김작가', '강현우'], books: [], createdAt: new Date() },
];

// --- Reactive State ---
const representativeBooks = ref<string[]>(['나의 어린 시절 이야기']);
const currentRepBookIndex = ref(0);
const myBooks = ref<Book[]>(DUMMY_MY_BOOKS);
const allGroups = ref<Group[]>(DUMMY_GROUPS);

// Modals
const isRepBookModalVisible = ref(false);
const selectedRepBooks = ref<string[]>([]);
const isGroupModalVisible = ref(false);
const newGroupName = ref('');
const newGroupMembers = ref('');

// New state for drag-and-drop logic
const bookBeingDraggedFromGroup = ref<string | null>(null);
let removalTimeout: ReturnType<typeof setTimeout> | null = null;
const isDraggingBook = ref(false);

// Message Box
const isMessageBoxVisible = ref(false);
const messageBoxTitle = ref('');
const messageBoxContent = ref('');

// --- Computed Properties ---
const isBookInAnyGroup = computed(() => (bookId: string) => {
  return allGroups.value.some(group => group.books.some(book => book.id === bookId));
});

// --- Functions ---
function showMessageBox(message: string, title = '알림') {
  messageBoxTitle.value = title;
  messageBoxContent.value = message;
  isMessageBoxVisible.value = true;
}

function handleBookDrop(event: DraggableEvent, groupId: string) {
  // event.added가 없으면 함수 종료
  if (!event.added) return;

  const bookTitle = event.added.element.title;
  const group = allGroups.value.find(g => g.id === groupId);

  if (group) {
    // If a book was just dragged from another group, clear the timeout
    if (bookBeingDraggedFromGroup.value === event.added.element.id) {
      if (removalTimeout) {
        clearTimeout(removalTimeout);
        removalTimeout = null;
      }
      bookBeingDraggedFromGroup.value = null; // Reset
    }

    showMessageBox(`'${bookTitle}'을(를) '${group.groupName}' 그룹에 추가했습니다.`);
    // In a real application, you would make an API call here
    // to persist this change on the backend.
    console.log(`Book ${event.added.element.id} moved to Group ${groupId}`);
  }
}

function handleGroupBookChange(event: DraggableEvent, groupId: string) {
  if (event.removed) {
    const removedBook = event.removed.element;
    const group = allGroups.value.find(g => g.id === groupId);

    if (!group || !removedBook) {
      console.error("Could not find group or removed book for confirmation.");
      return;
    }

    // If cancelled, re-add the book to the original group
    // This part is for when a book is dragged out and then the user cancels the removal.
    // The actual removal logic is now handled by the removeBookFromGroup function.
    // This ensures that if a book is dragged out and not dropped into another valid target,
    // it remains in its original group until explicitly removed by the button.
    if (!confirm(`'${removedBook.title}' 책을 '${group.groupName}' 그룹에서 제거하시겠습니까?`)) {
      group.books.splice(event.removed.oldIndex, 0, removedBook);
      showMessageBox(`'${removedBook.title}' 책 제거를 취소했습니다.`);
    } else {
      // If confirmed, the book is removed from this group.
      // No need to explicitly remove from all groups here, as the button handles it.
      showMessageBox(`'${removedBook.title}' 책을 '${group.groupName}' 그룹에서 제거했습니다.`);
    }
  }
}

function removeBookFromGroup(groupId: string, bookId: string) {
  const group = allGroups.value.find(g => g.id === groupId);
  if (group) {
    if (confirm(`'${group.groupName}' 그룹에서 이 책을 제거하시겠습니까?`)) {
      group.books = group.books.filter(book => book.id !== bookId);
      showMessageBox(`책이 '${group.groupName}' 그룹에서 제거되었습니다.`);
    }
  }
}

function isMemberRegistered(group: Group, memberName: string): boolean {
  // This is a dummy check. In a real app, you'd check if a book by this member
  // is actually associated with this group in your backend data.
  return group.books.some(book => book.authorName === memberName);
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

  if (!groupName) {
    showMessageBox('그룹 이름을 입력해주세요.', '경고');
    return;
  }

  const newGroup: Group = {
    id: `group${allGroups.value.length + 1}`,
    groupName: groupName,
    members: members,
    books: [], // Initialize with empty books array
    createdAt: new Date(),
  };
  allGroups.value.push(newGroup);
  isGroupModalVisible.value = false;
  newGroupName.value = '';
  newGroupMembers.value = '';
  showMessageBox('그룹이 생성되었습니다.');
}

onMounted(() => {
  // Dummy data is used, no fetch needed.
});
</script>

<style scoped>
.my-library-page {
  padding-top: 56px; /* Navbar 높이와 동일하게 조정 */
  padding-bottom: 80px;
  background-color: #F5F5DC;
  color: #3D2C20;
  min-height: 100vh; /* Set min-height to 100vh for overall page scrolling */
  padding-left: 1rem;
  padding-right: 1rem;
}

.representative-book-section,
.book-shelves-section {
  background-color: #F5F5DC;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  padding: 2rem;
  margin-bottom: 2rem;
  overflow: visible; /* Ensure sticky works within this section */
}

.section-title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 1.5rem;
  color: #3D2C20;
  text-align: center;
}

.representative-book-carousel {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.carousel-btn {
  background-color: #B8860B;
  color: #3D2C20;
  font-weight: bold;
  padding: 0.75rem 1rem;
  border-radius: 9999px;
  border: none;
}

.carousel-btn:hover:not(:disabled) {
  background-color: #DAA520;
}

.carousel-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.representative-book {
  width: 180px;
  height: 250px;
  border-radius: 12px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
  background-color: #5C4033;
  color: #F5F5DC;
  font-size: 1.3rem;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 1.5rem;
  cursor: pointer;
  writing-mode: vertical-rl;
  text-orientation: upright;
  word-break: break-word;
}

.select-rep-btn {
  background-color: #B8860B;
  color: #3D2C20;
  font-weight: bold;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  border: none;
  display: block;
  margin: 0 auto;
}

.select-rep-btn:hover {
  background-color: #DAA520;
}

.shelf-subtitle {
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 1rem;
  color: #3D2C20;
}

.my-books-shelf-wrapper {
  margin-bottom: 2rem;
  position: sticky; /* Changed back to sticky positioning */
  top: 56px; /* Adjust based on your navbar's height */
  z-index: 100; /* Ensure it stays above other content */
  background-color: #F5F5DC; /* Add background to prevent content from showing through */
  padding-top: 1rem; /* Add some padding to separate from navbar */
  padding-bottom: 1rem; /* Add some padding below the sticky element */
}

.liked-books-shelf-wrapper, .group-books-wrapper {
    margin-bottom: 2rem;
}

.group-shelf-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.add-group-btn {
  background-color: #B8860B;
  color: #3D2C20;
  font-weight: bold;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  border: none;
}

.add-group-btn:hover {
  background-color: #DAA520;
}

.book-shelf {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  overflow-x: auto;
  background-color: #5C4033;
  border-radius: 8px;
  box-shadow: inset 0 2px 5px rgba(0, 0, 0, 0.1);
  min-height: 170px;
}

.book-shelf::-webkit-scrollbar {
  display: none;
}

.book-item {
  flex-shrink: 0;
  width: 100px;
  height: 150px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  font-size: 0.8rem;
  font-weight: bold;
  color: #3D2C20;
  padding: 0.5rem;
  word-break: break-word;
  line-height: 1.3;
  cursor: grab;
  transition: transform 0.2s ease-in-out;
  writing-mode: vertical-rl;
  text-orientation: upright;
  text-decoration: none;
  position: relative; /* For positioning the icon */
}

.book-item:hover {
  transform: translateY(-5px);
}

.my-book {
  background-color: #8B4513;
  color: #F5F5DC;
}

.liked-book {
  background-color: #CD5C5C;
  color: #F5F5DC;
  cursor: pointer;
}

.group-shelves-container {
  margin-top: 1rem;
  margin-bottom: 1rem;
}

.group-shelf-wrapper {
  background-color: #D2B48C;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1.5rem;
}

.group-shelf-horizontal {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  overflow-x: auto;
  background-color: #5C4033;
  border-radius: 8px;
  box-shadow: inset 0 2px 5px rgba(0, 0, 0, 0.1);
  min-height: 170px;
}

.group-books-draggable-area {
  display: flex;
  gap: 1rem;
  flex-grow: 1; /* Allow it to take remaining space */
  min-width: 100px; /* Ensure it's visible even if empty */
}

.member-book {
  cursor: default; /* Members are not draggable */
}

.member-book-registered {
  background-color: #DAA520; /* Darker gold for registered members */
  color: #3D2C20;
}

.member-book-unregistered {
  background-color: rgba(218, 165, 32, 0.5); /* Transparent darker gold for unregistered members */
  color: rgba(61, 44, 32, 0.7); /* Slightly transparent text */
}

.group-name-book {
  background-color: #B8860B; /* Gold for group name book */
  color: #3D2C20; /* Dark brown text */
  cursor: pointer; /* Make it clickable */
}

.group-shelf {
  background-color: #5C4033;
  border-radius: 8px;
  box-shadow: inset 0 2px 5px rgba(0, 0, 0, 0.1);
  padding: 1rem;
}

.pagination-controls {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 1.5rem;
}

.pagination-btn {
  background-color: #B8860B;
  color: #3D2C20;
  font-weight: bold;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  border: none;
}

.pagination-btn:hover:not(:disabled) {
  background-color: #DAA520;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.group-link-icon {
  position: absolute;
  top: 5px;
  right: 5px;
  color: #F5F5DC; /* Light color for visibility */
  font-size: 0.9rem;
}

.remove-book-btn {
  position: absolute;
  top: 2px;
  right: 2px;
  background-color: rgba(255, 0, 0, 0.7); /* Semi-transparent red */
  color: white;
  border: none;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  font-size: 0.7rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0; /* Hidden by default */
  transition: opacity 0.2s ease-in-out;
}

.book-item:hover .remove-book-btn {
  opacity: 1; /* Show on hover */
}

/* Modal Styles */
.modal {
  position: fixed;
  z-index: 1000;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  overflow: auto;
  background-color: rgba(0,0,0,0.6);
  justify-content: center;
  align-items: center;
}

.modal-content {
  background-color: #5C4033;
  margin: auto;
  padding: 2rem;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.5);
  position: relative;
  color: #F5F5DC;
}

.modal-sm {
  max-width: 400px;
}

.close-button {
  color: #F5F5DC;
  float: right;
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
  margin-bottom: 1rem;
  color: #B8860B;
}

.modal-description, .modal-body {
  margin-bottom: 1rem;
  color: #F5F5DC;
}

.modal-select {
  background-color: #F5F5DC;
  color: #3D2C20;
  border: 1px solid #B8860B;
  border-radius: 8px;
  padding: 0.5rem;
  width: 100%;
  height: 160px;
  margin-bottom: 1.5rem;
}

.modal-save-btn,
.modal-confirm-btn {
  background-color: #B8860B;
  color: #3D2C20;
  font-weight: bold;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  border: none;
  width: 100%;
}

.form-label {
  color: #F5F5DC;
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.form-control {
  background-color: #F5F5DC;
  color: #3D2C20;
  border: 1px solid #B8860B;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  width: 100%;
}
</style>
