<template>
  <div class="my-library-page">
    <section class="content-section representative-book-section">
      <h2 class="section-title">나의 대표 인생책</h2>
      <p class="section-subtitle">당신의 삶에 가장 큰 영감을 준 책을 설정해보세요.</p>

      <div class="rep-book-display-area">
        <div v-if="currentRepBook" class="rep-book-container" @mousemove="handleMouseMove" @mouseleave="resetRotation">
          <div class="rep-book-shadow"></div>

          <div class="book-3d-wrapper" :style="repBookStyle">
            <div class="book-3d">
              <div class="book-face front" :style="{ backgroundImage: `url(${currentRepBook.coverUrl})` }">
                <div class="bright-edge-effect"></div>
                <div class="book-title-overlay">
                  <div class="book-title">{{ currentRepBook.title }}</div>
                  <div class="book-author">{{ currentRepBook.authorName }}</div>
                </div>
              </div>
              <div class="book-face back" :style="{ backgroundImage: `url(${currentRepBook.coverUrl})` }"></div>
              <div class="book-face left" :style="{ backgroundImage: `url(${currentRepBook.coverUrl})` }"></div>
              <div class="book-face right"></div>
              <div class="book-face top"></div>
              <div class="book-face bottom"></div>
            </div>
          </div>
        </div>
        <div v-else class="no-rep-book">
          대표책을 선택해주세요.
        </div>
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
                <div class="book-title-text-wrapper">
                  {{ truncateTitle(book.title) }}
                </div>
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
                  <div class="book-title-text-wrapper">
                    {{ truncateTitle(member) }}
                  </div>
                </div>

                <draggable v-model="group.books" item-key="id"
                  :group="{ name: 'groupBooksTarget', pull: true, put: ['myBooksSource'] }"
                  class="group-books-draggable-area" tag="div" @add="handleBookDrop($event, group.id)"
                  @change="handleGroupBookChange($event, group.id)" @start="isDraggingBook = true"
                  @end="isDraggingBook = false">
                  <template #item="{ element: book, index }">
                    <div class="book-item my-book" :class="`book-color-${(index % 4) + 1}`" :title="book.title">
                      <div class="book-title-text-wrapper">
                        {{ truncateTitle(book.title) }}
                      </div>
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
          <select v-model="selectedRepBookIds" multiple class="form-select modal-select">
            <option v-for="book in myBooks" :key="book.id" :value="book.id">{{ book.title }}</option>
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
  coverUrl?: string;
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
  { id: 'mybook1', title: '나의 어린 시절 이야기', authorId: 'dummyUser1', authorName: '김작가', coverUrl: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974' },
  { id: 'mybook2', title: '꿈을 향한 도전', authorId: 'dummyUser1', authorName: '김작가', coverUrl: 'https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b?w=500' },
  { id: 'mybook3', title: '여행의 기록', authorId: 'dummyUser1', authorName: '김작가', coverUrl: 'https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=500' },
  { id: 'mybook4', title: '개발자의 삶', authorId: 'dummyUser1', authorName: '김작가', coverUrl: 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=500' },
];
const DUMMY_GROUPS: Group[] = [
  { id: 'group1', groupName: '독서 토론 모임', ownerId: '김작가', managers: ['이영희'], members: ['김작가', '이영희', '박철수'], books: [{ id: 'mybook1', title: '나의 어린 시절 이야기', authorId: 'dummyUser1', authorName: '김작가', coverUrl: 'https://images.unsplash.com/photo-1506894824902-72895a783ac0?w=500' }], createdAt: new Date() },
  { id: 'group2', groupName: '글쓰기 동호회', ownerId: '김작가', managers: [], members: ['김작가', '최수진'], books: [], createdAt: new Date() },
  { id: 'group3', groupName: '여행 에세이 클럽', ownerId: '정민준', managers: [], members: ['정민준', '김작가', '하은지'], books: [{ id: 'mybook3', title: '여행의 기록', authorId: 'dummyUser1', authorName: '김작가', coverUrl: 'https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=500' }], createdAt: new Date() },
];

// --- Reactive State ---
const representativeBooks = ref<Book[]>([DUMMY_MY_BOOKS[0]]);
const myBooks = ref<Book[]>(DUMMY_MY_BOOKS);
const allGroups = ref<Group[]>(DUMMY_GROUPS);
const isDraggingBook = ref(false);
const isRepBookModalVisible = ref(false);
const selectedRepBookIds = ref<string[]>([]);
const isGroupModalVisible = ref(false);
const newGroupName = ref('');
const newGroupMembers = ref('');
const isMessageBoxVisible = ref(false);
const messageBoxTitle = ref('');
const messageBoxContent = ref('');
const repBookRotationY = ref(0);

// --- Computed Properties ---
const isBookInAnyGroup = computed(() => (bookId: string) => {
  return allGroups.value.some(group => group.books.some(book => book.id === bookId));
});
const currentRepBook = computed(() => {
  return representativeBooks.value.length > 0 ? representativeBooks.value[0] : null;
});
const repBookStyle = computed(() => ({
  transform: `rotateY(${repBookRotationY.value}deg)`,
  transition: isDraggingBook.value ? 'none' : 'transform 0.1s ease-out'
}));

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
function handleGroupBookChange(event: DraggableEvent, groupId: string) { }
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
function openRepBookModal() {
  selectedRepBookIds.value = representativeBooks.value.map(book => book.id);
  isRepBookModalVisible.value = true;
}
function saveRepresentativeBooksHandler() {
  representativeBooks.value = selectedRepBookIds.value
    .map(id => myBooks.value.find(book => book.id === id))
    .filter((book): book is Book => !!book);
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
function handleMouseMove(event: MouseEvent) {
  const target = event.currentTarget as HTMLElement;
  const rect = target.getBoundingClientRect();
  const x = event.clientX - rect.left;
  const width = rect.width;
  const mouseXPercent = (x / width) - 0.5;
  const maxRotation = 45; // [수정] 회전 각도 증가
  repBookRotationY.value = mouseXPercent * maxRotation * -1;
}
function resetRotation() {
  repBookRotationY.value = 0;
}

function truncateTitle(title: string): string {
  const maxLength = 13;
  if (title.length > maxLength) {
    return title.substring(0, maxLength) + '...';
  }
  return title;
}
</script>

<style>
/* --- 3D 책 모델 전용 전역 스타일 --- */
:root {
  /* -- 변수 선언: 이곳의 값을 바꾸면 책 전체 크기가 조절됩니다 -- */
  --rep-book-width: 300px;
  /* 책의 가로 너비 */
  --rep-book-height: 450px;
  /* 책의 세로 높이 */
  --rep-book-depth: 50px;
  /* 책의 두께 */
}

/* 3D 책 모델을 감싸는 전체 컨테이너 */
.book-3d-wrapper {
  width: var(--rep-book-width);
  /* 변수에서 너비 값 가져오기 */
  height: var(--rep-book-height);
  /* 변수에서 높이 값 가져오기 */
  transform-style: preserve-3d;
  /* 자식 요소들의 3D 공간을 유지시킴 (필수) */
  position: relative;
  /* 자식 요소 위치 지정의 기준점 */
}

/* 3D 책 모델의 실제 뼈대 */
.book-3d {
  width: 100%;
  height: 100%;
  position: relative;
  transform-style: preserve-3d;
  /* 자식 요소(각 면)들의 3D 공간을 유지시킴 (필수) */
  /* 책의 중심을 기준으로 회전하도록 Z축 위치 조정 */
  transform: translateZ(calc(var(--rep-book-depth) / -2));
}

/* 책의 각 면(앞, 뒤, 옆 등)에 대한 공통 스타일 */
.book-face {
  position: absolute;
  /* 모든 면을 같은 위치에 겹치게 배치 */
  box-sizing: border-box;
  /* 테두리를 크기에 포함 */
  backface-visibility: hidden;
  /* 요소의 뒷면이 보이지 않게 처리 */
  width: 100%;
  height: 100%;
  border-radius: 2px 4px 4px 2px;
  /* 모서리 둥글게 (왼쪽 위, 오른쪽 위, 오른쪽 아래, 왼쪽 아래 순) */
  border: 1px solid rgba(0, 0, 0, 0.1);
  /* 얇은 테두리 */
}

/* 표지 왼쪽 가장자리에 밝은 빛 효과 */
.book-face.front .bright-edge-effect {
  position: absolute;
  left: 8px;
  /* 왼쪽 끝에 붙임 */
  top: 0;
  bottom: 0;
  width: 8px;
  /* 효과 너비 */
  /* 오른쪽으로 갈수록 투명해지는 흰색 그라데이션 */
  background: linear-gradient(to right, rgba(255, 255, 255, 0.441), transparent);
  border-radius: 0 2px 2px 0;
  /* 효과가 표지 모서리를 따라 둥글게 */
  pointer-events: none;
  /* 마우스 상호작용(회전)을 방해하지 않도록 설정 */
}

/* 책 앞면 (표지) */
.book-face.front {
  background-size: cover;
  /* 이미지가 요소를 완전히 덮도록 설정 */
  background-position: center;
  /* 이미지를 중앙에 배치 */
  /* Z축으로 책 두께의 절반만큼 앞으로 이동시켜 표지를 만듦 */
  transform: translateZ(calc(var(--rep-book-depth)));
  display: flex;
  /* 내부 제목/저자 박스를 중앙에 배치하기 위함 */
  align-items: center;
  justify-content: center;
  color: #333;
}

/* 표지 위에 올라가는 흰색 정보 박스 */
.book-title-overlay {
  width: 60%;
  /* 표지 대비 가로 너비 */
  height: 60%;
  /* 표지 대비 세로 높이 */
  background-color: rgba(255, 255, 255, 0.95);
  /* 반투명한 흰색 배경 */
  padding: 1rem;
  box-sizing: border-box;
  display: flex;
  /* 제목과 저자를 위아래로 배치하기 위함 */
  flex-direction: column;
  justify-content: space-between;
  text-align: left;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.1);
  /* 부드러운 그림자 효과 */
}

/* 정보 박스 안의 책 제목 */
.book-title-overlay .book-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 25px;
  /* 글자 크기 */
  line-height: 1.4;
  /* 줄 간격 */
  font-weight: 700;
  color: #000000;
}

/* 정보 박스 안의 저자 이름 */
.book-title-overlay .book-author {
  font-size: 12px;
  /* 글자 크기 */
  font-weight: 600;
  color: #333;
}

/* 책 뒷면 */
.book-face.back {
  background-size: cover;
  background-position: center;
  /* Y축으로 180도 회전 후, Z축으로 책 두께의 절반만큼 이동 */
  transform: rotateY(180deg) translateZ(calc(var(--rep-book-depth) / 2));
  filter: brightness(0.7) blur(2px);
  /* 뒷면은 어둡고 흐리게 처리 */
}

/* 책등 (왼쪽 면) */
.book-face.left {
  width: var(--rep-book-depth);
  /* 너비를 책 두께와 동일하게 설정 */
  background-color: #f3f1ed;
  /* 기본 배경색 */
  /* Y축으로 -90도 회전 후, 책등이 제자리에 오도록 위치 조정 */
  transform: rotateY(-90deg) translateZ(-0.3px) scaleY(0.995);
  transform-origin: left;
  /* 왼쪽을 축으로 회전 */
  filter: brightness(0.7) blur(0.5px);
  /* 책등은 약간 어둡고 흐릿하게 처리 */

}

/* 책 펼치는 쪽 (오른쪽 면) */
.book-face.right {
  width: var(--rep-book-depth);
  /* 너비를 책 두께와 동일하게 설정 */
  /* 종이가 여러 장 겹쳐진 듯한 효과 */
  background-color: #a6916f;
  background-image: repeating-linear-gradient(to right,
      #ffffff27,
      #dfdedd 1px,
      #bbb 1px,
      #999590c8 3px);
  /* Y축으로 90도 회전 후, 제자리에 오도록 위치 조정 */
  transform: rotateY(90deg) translateZ(calc(var(--rep-book-width) - var(--rep-book-depth))) translateZ(-2px) scaleY(0.995);
  transform-origin: right;
  /* 오른쪽을 축으로 회전 */
}

/* 책 윗면 */
.book-face.top {
  height: var(--rep-book-depth);
  /* 높이를 책 두께와 동일하게 설정 */
  background-color: #e0dace;
  /* X축으로 90도 회전 후, 제자리에 오도록 위치 조정 */
  transform: rotateX(90deg) translateZ(0);
  transform-origin: top;
  /* 위쪽을 축으로 회전 */
}

/* 책 아랫면 */
.book-face.bottom {
  height: var(--rep-book-depth);
  /* 높이를 책 두께와 동일하게 설정 */
  background-color: #d3c8ba;
  /* X축으로 -90도 회전 후, 제자리에 오도록 위치 조정 */
  transform: rotateX(-90deg) translateZ(calc(var(--rep-book-height) - var(--rep-book-depth)));
  transform-origin: bottom;
  /* 아래쪽을 축으로 회전 */
}
</style>


<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&family=Pretendard:wght@400;500;700&display=swap');
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

/* --- Base Page Layout --- */
.my-library-page {
  padding: 80px 2rem 2rem;
  background-color: #ffffff;
  color: #261E17;
  min-height: calc(100vh - 56px);
  font-family: 'Pretendard', sans-serif;
}

.content-section {
  padding: 2.5rem;
  margin: 0 auto 3rem auto;
  max-width: 1200px;
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
  color: #000000;
}

.book-shelves-section .section-title {
  color: #000000;
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
.rep-book-display-area {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 500px;
  /* 그림자 공간을 위해 높이 확보 */
  margin-bottom: 2rem;
}

.rep-book-container {
  perspective: 1200px;
  cursor: grab;
  position: relative;
  /* 그림자 위치의 기준점 */
  display: flex;
  align-items: center;
  justify-content: center;
  width: var(--rep-book-width);
  height: var(--rep-book-height);
}

.rep-book-container:active {
  cursor: grabbing;
}

/* [추가] 3D 책 모델 아래의 그림자 스타일 */
.rep-book-shadow {
  position: absolute;
  bottom: -30px;
  /* 그림자가 책 아래에 위치하도록 조정 */
  left: 50%;
  transform: translateX(-50%);
  /* 가운데 정렬 */
  width: 350px;
  /* 그림자 너비 조절 */
  height: 40px;
  /* 그림자 높이 (흐릿한 정도) 조절 */
  background-color: rgba(38, 30, 23, 0.6);
  /* 그림자 색상 및 투명도 */
  border-radius: 100%;
  /* 둥근 그림자 형태 */
  filter: blur(30px);
  /* 흐릿한 효과 */
  z-index: 0;
  /* 책 모델(z-index:1 이상) 뒤에 위치 */
  pointer-events: none;
  /* 마우스 이벤트 방해 방지 */
  transition: opacity 0.1s ease-out;
}

.book-3d-wrapper {
  position: relative;
  z-index: 1;
  /* 그림자보다 위에 보이도록 설정 */
}

.no-rep-book {
  width: var(--rep-book-width);
  height: var(--rep-book-height);
  border: 2px dashed #E0E0E0;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #BEB4A7;
  font-weight: 500;
}

.select-rep-btn {
  display: block;
  margin: 0 auto;
}

.book-shelves-section {
  background-color: #ffffff;
  border: 1px solid #ffffff;
}

.book-shelves-section .section-subtitle,
.book-shelves-section .shelf-title,
.book-shelves-section .shelf-title small {
  color: #000000;
}

.book-shelves-section .btn-primary {
  background-color: #D4A373;
  border-color: #D4A373;
  color: #261E17;
}

.my-books-shelf-wrapper {
  position: sticky;
  top: 56px;
  z-index: 100;
  background-color: #e6e6e6;
  padding: 2.5rem 3rem 1rem 3rem;
  margin: 0;
  border-radius: 10px 10px 0 0;
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

.book-shelf-container {
  padding: 2rem 1rem 0.3rem 0.8rem;
}

.my-books-container {
  background-color: #ffffff;
  box-shadow: inset 0 4px 8px rgba(0, 0, 0, 0.3);
  /* border: 1px solid #000000; */
}

.book-shelf {
  display: flex;
  gap: 0.75rem;
  /* 책 사이 간격 줄임 */
  overflow-x: auto;
  min-height: 240px;
  align-items: flex-end;
  padding-bottom: 0;
  /* 책과 책꽂이 바닥 사이 여백 제거 */
}

.book-shelf::-webkit-scrollbar {
  height: 10px;
}

.book-shelf::-webkit-scrollbar-track {
  background: transparent;
}

.book-shelf::-webkit-scrollbar-thumb {
  background: #D4A373;
  border-radius: 5px;
  border: 1px solid #594C40;
}


.book-item {
  flex-shrink: 0;
  width: 45px;
  height: 240px;

  /* 심플한 단색 배경과 텍스트 색상 */
  background-color: #a5eada;
  /* 매우 밝은 회색 배경 */
  color: #000000;
  /* 어두운 회색 텍스트 */

  /* 단순화된 테두리와 그림자 */
  border: 1px solid #dee2e6;
  border-radius: 4px 4px 4px 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  /* 그림자 약하게 */

  /* 제목 표시 관련 스타일 */
  writing-mode: vertical-rl;
  /* 세로쓰기 유지 */
  text-orientation: mixed;
  white-space: normal;
  /* 제목이 길 경우 자연스럽게 다음 줄로 넘어가도록 변경 */
  overflow: hidden;
  /* 영역을 넘어가는 텍스트는 숨김 */
  display: flex;
  justify-content: flex-start; /* 상단 정렬 */
  align-items: center; /* 좌우 중앙 정렬 */
  text-align: center;
  /* 텍스트 자체의 정렬 (필요시) */
  padding: 0.5rem 0.3rem 1.5rem;
  /* 제목이 위아래, 좌우 여백을 갖도록 조정 */
  font-size: 0.9rem;
  /* 폰트 크기를 살짝 줄여 가독성 확보 */
  font-weight: 600;
  line-height: 1.5;

  /* 기타 스타일 */
  position: relative;
  cursor: grab;
  text-decoration: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.book-title-text-wrapper {
  /* --- 크기 고정 --- */
  width: 80%;
  /* 책 너비의 80%로 고정 */
  height: 85%;
  /* 책 높이의 85%로 고정 (책 길이보다 약간 짧게) */

  /* --- 위치 및 정렬 --- */
  position: relative;
  /* absolute에서 변경하여 flex-item으로 동작하게 함 */
  top: auto;
  /* absolute 관련 속성 제거 */
  left: auto;
  /* absolute 관련 속성 제거 */
  transform: none;
  /* absolute 관련 속성 제거 */
  display: flex;
  justify-content: flex-start; /* 상단 정렬 (main axis for vertical-rl content) */
  align-items: center; /* 좌우 중앙 정렬 (cross axis for vertical-rl content) */ 
  
  /* --- 디자인 --- */
  background-color: white;
  padding: 0.2rem 0.4rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #dee2e6;
  /* 빨간색 임시 테두리 대신 은은한 회색 테두리 적용 */
}

.book-item:hover {
  transform: translateY(-5px);
  /* 호버 시 살짝 위로 이동 */
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.08);
  /* 그림자 약간 더 선명하게 */
  border-color: #adb5bd;
  /* 테두리 색상 강조 */
}

/* draggable 라이브러리에서 사용하는 클래스 */
.sortable-ghost {
  opacity: 0.4;
}



.member-book {
  cursor: default;
  height: 260px;
}

.member-book-registered {
  background-color: #D4A373;
  color: #FFFFFF;
}

.member-book-unregistered {
  background-color: #ccc;
  color: #555;
  opacity: 0.8;
}

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

.book-item:hover .remove-book-btn {
  opacity: 1;
}

.remove-book-btn:hover {
  background-color: rgba(239, 68, 68, 1);
  transform: scale(1.1);
}

.group-shelf-header {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

.group-shelves-container {
  margin-top: 0;
  display: flex;
  flex-direction: column;
  gap: 0rem;
}

.group-shelf-wrapper {
  background-color: #e6e6e6;
  /* border: 1px solid #000000; */
  position: relative;
  padding: 1rem 3rem 0.5rem 3rem;
  margin: 0;
  box-shadow: none;
}

.group-shelf-wrapper:last-child {
  padding-bottom: 3rem;
  border-radius: 0 0 10px 10px;
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
  color: #000000;
  text-decoration: none;
  transition: color 0.2s;
}

.group-shelf-title:hover {
  color: #D4A373;
  text-decoration: underline;
}

.group-bookshelf-inner {
  background-color: #ffffff;
  box-shadow: inset 0 4px 8px rgba(0, 0, 0, 0.3);
  padding: 2rem 1rem 0.3rem 0.8rem;
  /* border: 1px solid #000000; */
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
  gap: 0.75rem;
  /* 책 사이 간격 줄임 */
  overflow-x: auto;
  min-height: 240px;
  padding-bottom: 0;
  /* 책과 책꽂이 바닥 사이 여백 제거 */
}

.group-shelf-horizontal::-webkit-scrollbar {
  height: 10px;
}

.group-shelf-horizontal::-webkit-scrollbar-track {
  background: transparent;
}

.group-shelf-horizontal::-webkit-scrollbar-thumb {
  background: #D4A373;
  border-radius: 5px;
  border: 2px solid #594C40;
}

.group-books-draggable-area {
  display: flex;
  gap: 0.75rem;
  /* 책 사이 간격 줄임 */
  flex-grow: 1;
  min-width: 120px;
  min-height: 250px;
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

.modal-content.modal-sm {
  max-width: 400px;
}

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

.close-button:hover {
  color: #261E17;
}

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