<template>
  <div class="my-library-page">
    <section class="content-section representative-book-section">
      <h2 class="section-title">나의 대표 인생책</h2>
      <p class="section-subtitle">당신의 삶에 가장 큰 영감을 준 책을 설정해보세요.</p>
      <div class="rep-book-display-area">
        <div v-if="currentRepBook" class="rep-book-container" @mousemove="handleMouseMove" @mouseleave="resetRotation" @click="selectShelfBook(currentRepBook)">
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
        <div class="shelf-book-container my-books-container">
          <draggable v-model="myBooks" item-key="id" :group="{ name: 'myBooksSource', pull: 'clone' }"
            class="shelf-book-list" tag="div" @start="startDrag" @end="endDrag">
            <template #item="{ element: book }">
              <div class="shelf-book-item-3d" @click="selectShelfBook(book)" :title="book.title">

                <div class="shelf-book-model">
                  <div class="shelf-book-face shelf-book-cover"
                    :style="{ backgroundImage: `url(${book.coverUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
                    <img v-if="book.isPublished" src="/images/complete.png" alt="출판 완료" class="published-sticker-shelf" />
                    <div class="shelf-bright-edge-effect"></div>
                    <div class="shelf-book-title-overlay">
                      <div class="shelf-book-title">{{ book.title }}</div>
                      <div class="shelf-book-author">{{ book.authorName }}</div>
                    </div>
                  </div>
                  <div class="shelf-book-face shelf-book-spine"
                    :style="{ backgroundImage: `url(${book.coverUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
                    <div class="spine-title-box">
                      <div class="spine-title">{{ book.title }}</div>
                    </div>
                  </div>
                  <div class="shelf-book-face shelf-book-side-edge"></div>
                  <div class="shelf-book-face shelf-book-back-cover"></div>
                  <div class="shelf-book-face shelf-book-top"></div>
                  <div class="shelf-book-face shelf-book-bottom"></div>
                </div>
              </div>
            </template>
          </draggable>
        </div>
      </div>

      <div class="group-shelves-container">
        <div v-for="group in displayedGroups" :key="group.id" class="group-shelf-wrapper">
          <div class="group-shelf-title-bar">
            <router-link v-if="group.id" :to="`/group-timeline/${group.id}`" class="group-shelf-title"
              :title="`${group.groupName} 타임라인으로 이동`">
              {{ group.groupName }}
            </router-link>
            <span v-else class="group-shelf-title-placeholder">{{ group.groupName }}</span>
          </div>
          <div v-for="(bookChunk, chunkIndex) in chunkedBooks(group.books)" :key="chunkIndex" class="group-bookshelf-inner">
            <div class="shelf-book-container">
              <div class="shelf-book-list group-shelf-horizontal">
                <draggable :list="bookChunk" item-key="id"
                  :group="{ name: 'groupBooksTarget', pull: true, put: ['myBooksSource'] }"
                  class="group-books-draggable-area" tag="div" @add="handleBookDrop($event, group.id)"
                  @change="(event: any) => handleGroupBookChange(event, group.id, chunkIndex)" @start="isDraggingBook = true"
                  @end="isDraggingBook = false">
                  <template #item="{ element: book }">
                    <div class="shelf-book-item-3d" @click="selectShelfBook(book)" :title="book.title">

                      <div class="shelf-book-model">
                        <div class="shelf-book-face shelf-book-cover"
                          :style="{ backgroundImage: `url(${book.coverUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
                          <div class="shelf-bright-edge-effect"></div>
                          <div class="shelf-book-title-overlay">
                            <div class="shelf-book-title">{{ book.title }}</div>
                            <div class="shelf-book-author">{{ book.authorName }}</div>
                          </div>
                        </div>
                        <div class="shelf-book-face shelf-book-spine"
                          :style="{ backgroundImage: `url(${book.coverUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
                          <div class="spine-title-box">
                            <div class="spine-title">{{ book.title }}</div>
                          </div>
                        </div>
                        <div class="shelf-book-face shelf-book-side-edge"></div>
                        <div class="shelf-book-face shelf-book-back-cover"></div>
                        <div class="shelf-book-face shelf-book-top"></div>
                        <div class="shelf-book-face shelf-book-bottom"></div>
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
        <button @click="closeAllModals" class="close-button" title="닫기"><i class="bi bi-x-lg"></i></button>
        <div v-if="isRepBookModalVisible">
          <h2 class="modal-title">대표 인생책 선택</h2>
          <p class="modal-description">나의 대표 인생책을 한권 선택해주세요.</p>
          <div class="book-selection-list">
            <label v-for="book in myBooks" :key="book.id" class="book-selection-item">
              <input type="radio" :value="book.id" v-model="selectedRepBookId" name="rep-book" />
              <img :src="book.coverUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'" alt="Book Cover" class="book-cover-thumbnail" />
              <span class="book-title-radio">{{ book.title }}</span>
            </label>
          </div>
          <button @click="saveRepresentativeBookHandler" class="btn btn-primary modal-action-btn">저장하기</button>
        </div>
        <div v-if="isGroupModalVisible">
          <h2 class="modal-title">새 그룹 만들기</h2>
          <div class="form-group">
            <label for="group-name-input" class="form-label">그룹 이름</label>
            <input v-model="newGroupName" id="group-name-input" type="text" class="form-control"
              placeholder="예: 독서 모임 A">
          </div>
          <button @click="createGroupHandler" class="btn btn-primary modal-action-btn">그룹 생성</button>
        </div>
        <div v-if="isMessageBoxVisible">
          <h2 class="modal-title">{{ messageBoxTitle }}</h2>
          <p class="modal-body">{{ messageBoxContent }}</p>
          <button @click="isMessageBoxVisible = false" class="btn btn-primary modal-action-btn">확인</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import draggable from 'vuedraggable';

const router = useRouter();

// --- Interfaces & Types ---
interface Book { id: string; title: string; authorId: string; authorName?: string; coverUrl?: string; isPublished?: boolean; }
interface Group { id: string; groupName: string; ownerId: string; managers: string[]; members: string[]; books: Book[]; createdAt: Date; }
interface DraggableEvent { added?: { element: Book; newIndex: number }; removed?: { element: Book; oldIndex: number }; moved?: { element: Book; newIndex: number; oldIndex: number }; }

// --- Dummy Data ---
const currentUserNickname = ref('김작가');
const DUMMY_MY_BOOKS: Book[] = [
  { id: 'mybook1', title: '나의 어린 시절 이야기', authorId: 'dummyUser1', authorName: '김작가', coverUrl: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974' },
  { id: 'mybook2', title: '꿈을 향한 도전', authorId: 'dummyUser1', authorName: '김작가', coverUrl: 'https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b?w=500' },
  { id: 'mybook3', title: '여행의 기록', authorId: 'dummyUser1', authorName: '김작가', coverUrl: 'https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=500' },
  { id: 'mybook4', title: '개발자의 삶', authorId: 'dummyUser1', authorName: '김작가', coverUrl: 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=500' },
  { id: 'mybook5', title: '다섯번째 책', authorId: 'dummyUser1', authorName: '김작가', coverUrl: 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=500' },
  { id: 'mybook6', title: '여섯번째 책', authorId: 'dummyUser1', authorName: '김작가', coverUrl: 'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=500' },
];
const DUMMY_GROUPS: Group[] = [
  { id: 'group1', groupName: '독서 토론 모임', ownerId: '김작가', managers: ['이영희'], members: ['김작가', '이영희', '박철수'], books: [{ id: 'mybook1', title: '나의 어린 시절 이야기', authorId: 'dummyUser1', authorName: '김작가', coverUrl: 'https://images.unsplash.com/photo-1506894824902-72895a783ac0?w=500' }], createdAt: new Date() },
  { id: 'group2', groupName: '글쓰기 동호회', ownerId: '김작가', managers: [], members: ['김작가', '최수진'], books: [], createdAt: new Date() },
  { id: 'group3', groupName: '여행 에세이 클럽', ownerId: '정민준', managers: [], members: ['정민준', '김작가', '하은지'], books: [{ id: 'mybook3', title: '여행의 기록', authorId: 'dummyUser1', authorName: '김작가', coverUrl: 'https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=500' }], createdAt: new Date() },
];

// --- Reactive State ---
const representativeBooks = ref<Book[]>([DUMMY_MY_BOOKS[0]]);
const myBooks = ref<Book[]>([]);
const allGroups = ref<Group[]>(DUMMY_GROUPS);
const isDraggingBook = ref(false);
let scrollInterval: number | null = null;
const isRepBookModalVisible = ref(false);
const selectedRepBookId = ref<string | null>(null);
const isGroupModalVisible = ref(false);
const newGroupName = ref('');
const isMessageBoxVisible = ref(false);
const messageBoxTitle = ref('');
const messageBoxContent = ref('');
const repBookRotationY = ref(0);

// --- Computed Properties ---
const currentRepBook = computed(() => (representativeBooks.value.length > 0 ? representativeBooks.value[0] : null));
const repBookStyle = computed(() => ({ transform: `rotateY(${repBookRotationY.value}deg)`, transition: isDraggingBook.value ? 'none' : 'transform 0.1s ease-out' }));
const displayedGroups = computed(() => {
  if (allGroups.value.length === 0) {
    return [{
      id: '',
      groupName: '그룹추가를 눌러 그룹을 만들어 보세요.',
      ownerId: '',
      managers: [],
      members: [],
      books: [],
      createdAt: new Date(),
    }];
  }
  return allGroups.value;
});

const chunkedBooks = (books: Book[]) => {
  const chunkSize = 13;
  const chunks = [];
  for (let i = 0; i < books.length; i += chunkSize) {
    chunks.push(books.slice(i, i + chunkSize));
  }
  return chunks.length > 0 ? chunks : [[]];
};

// --- Functions ---
function loadMyBooks() {
  const publishedBooks = JSON.parse(localStorage.getItem('publishedBooks') || '[]') as Book[];
  myBooks.value = DUMMY_MY_BOOKS.map(book => {
    const publishedBook = publishedBooks.find(pb => pb.id === book.id);
    return { ...book, isPublished: !!publishedBook };
  });
}
// 클릭 시 바로 상세 페이지로 이동하도록 변경
function selectShelfBook(book: Book) {
  router.push(`/book-detail/${book.id}`);
}
function closeAllModals() {
  isRepBookModalVisible.value = false;
  isGroupModalVisible.value = false;
  isMessageBoxVisible.value = false;
}
function showMessageBox(message: string, title = '알림') { messageBoxTitle.value = title; messageBoxContent.value = message; isMessageBoxVisible.value = true; }

async function handleBookDrop(event: DraggableEvent, groupId: string) {
  if (!event || !event.added) {
    return;
  }

  // vuedraggable이 DOM을 업데이트한 후, Vue가 반응성 데이터를 업데이트할 때까지 기다립니다.
  await nextTick();

  const { element: droppedBook, newIndex } = event.added;
  const group = allGroups.value.find(g => g.id === groupId);

  if (!group || typeof newIndex !== 'number') {
    return;
  }

  // vuedraggable이 책을 배열에 자동으로 추가한 후 이 함수가 호출됩니다.
  // 따라서 중복 여부를 확인하기 위해 배열에 해당 책 ID가 몇 번 나타나는지 셉니다.
  const occurrences = group.books.filter(b => b.id === droppedBook.id).length;

  // 책을 배열에서 제거해야 할 때 사용할 함수입니다.
  const removeBook = () => {
    // newIndex가 유효한 범위 내에 있는지 확인합니다.
    if (newIndex >= 0 && newIndex < group.books.length) {
      const newBooks = [...group.books];
      newBooks.splice(newIndex, 1);
      group.books = newBooks;
    }
  };

  if (occurrences > 1) {
    // 중복인 경우, 사용자에게 알리고 드래그된 책을 배열에서 제거합니다.
    showMessageBox(`'${droppedBook.title}'은(는) 이미 그룹에 있습니다.`, '중복 알림');
    removeBook();
    return;
  }

  // 중복이 아닌 경우, 사용자에게 추가할지 확인하는 대화상자를 띄웁니다.
  if (confirm(`'${droppedBook.title}' 책을 '${group.groupName}' 그룹에 추가하시겠습니까?`)) {
    // 사용자가 '확인'을 누르면, 책이 이미 배열에 있으므로 아무 작업도 하지 않습니다.
  } else {
    // 사용자가 '취소'를 누르면, 드래그된 책을 배열에서 제거합니다.
    removeBook();
  }
}

function handleGroupBookChange(event: DraggableEvent, groupId: string, chunkIndex: number) {
  const group = allGroups.value.find(g => g.id === groupId);
  if (!group) return;

  if (event.added) {
    const { element, newIndex } = event.added;
    const originalIndex = chunkIndex * 13 + newIndex;
    group.books.splice(originalIndex, 0, element);
  } else if (event.removed) {
  } else if (event.removed) {
    const { element, oldIndex } = event.removed;
    const originalIndex = chunkIndex * 13 + oldIndex;
    group.books.splice(originalIndex, 1);
  } else if (event.moved) {
  } else if (event.moved) {
    const { element, oldIndex, newIndex } = event.moved;
    const originalOldIndex = chunkIndex * 13 + oldIndex;
    const originalNewIndex = chunkIndex * 13 + newIndex;
    group.books.splice(originalOldIndex, 1);
    group.books.splice(originalNewIndex, 0, element);
  }
}

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
function openRepBookModal() {
  selectedRepBookId.value = representativeBooks.value.length > 0 ? representativeBooks.value[0].id : null;
  isRepBookModalVisible.value = true;
}
function saveRepresentativeBookHandler() {
  if (selectedRepBookId.value) {
    const selectedBook = myBooks.value.find(book => book.id === selectedRepBookId.value);
    if (selectedBook) {
      representativeBooks.value = [selectedBook];
      isRepBookModalVisible.value = false;
      showMessageBox('대표책이 저장되었습니다.');
    }
  }
}
async function createGroupHandler() {
  const groupName = newGroupName.value.trim();
  if (!groupName) {
    showMessageBox('그룹 이름을 입력해주세요.', '경고');
    return;
  }

  const members = [currentUserNickname.value];
  const newGroup: Group = {
    id: `group${Date.now()}`,
    groupName: groupName,
    ownerId: currentUserNickname.value,
    managers: [],
    members: members,
    books: [],
    createdAt: new Date()
  };
  allGroups.value.push(newGroup);
  isGroupModalVisible.value = false;
  newGroupName.value = '';
  showMessageBox('그룹이 성공적으로 생성되었습니다.');

  await nextTick();
  const newGroupElement = document.getElementById(`group-${newGroup.id}`);
  if (newGroupElement) {
    newGroupElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }
}
function handleMouseMove(event: MouseEvent) {
  const target = event.currentTarget as HTMLElement;
  const rect = target.getBoundingClientRect();
  const x = event.clientX - rect.left;
  const width = rect.width;
  const mouseXPercent = (x / width) - 0.5;
  const maxRotation = 45;
  repBookRotationY.value = mouseXPercent * maxRotation * -1;
}
function resetRotation() { repBookRotationY.value = 0; }
function truncateTitle(title: string): string {
  const maxLength = 13;
  if (title.length > maxLength) { return title.substring(0, maxLength) + '...'; }
  return title;
}

function startDrag() {
  isDraggingBook.value = true;
  document.addEventListener('dragover', handleDragOver);
}

function endDrag() {
  isDraggingBook.value = false;
  document.removeEventListener('dragover', handleDragOver);
  if (scrollInterval) {
    clearInterval(scrollInterval);
    scrollInterval = null;
  }
}

function handleDragOver(event: MouseEvent) {
  // 현재 마우스 커서의 Y 좌표를 가져옵니다.
  const { clientY } = event;
  // 브라우저 창의 전체 높이를 가져옵니다.
  const windowHeight = window.innerHeight;
  // 스크롤이 시작될 화면 가장자리로부터의 거리(px)입니다.
  const scrollZone = 500;
  // 스크롤의 최대 속도입니다.
  const maxScrollSpeed = 300;

  // 이전에 설정된 스크롤 인터벌이 있다면 초기화합니다.
  // 이렇게 하지 않으면 스크롤이 여러 번 중복 실행될 수 있습니다.
  if (scrollInterval) {
    clearInterval(scrollInterval);
    scrollInterval = null;
  }

  // 마우스 커서가 위쪽 스크롤 영역에 있는지 확인합니다.
  if (clientY < scrollZone) {
    // 커서가 스크롤 영역 내에서 얼마나 위쪽에 있는지 비율을 계산합니다 (0.0 ~ 1.0).
    // 가장 위쪽에 가까울수록 1.0에 가까워집니다.
    const speedFactor = 1 - (clientY / scrollZone);
    // 스크롤 영역의 80% 이상을 차지하는 부분에서는 최대 속도를, 그 외에는 비율에 따라 속도를 조절합니다.
    const speed = speedFactor > 0.3 ? maxScrollSpeed : maxScrollSpeed * speedFactor;
    // 설정된 속도로 위로 스크롤하는 인터벌을 시작합니다.
    scrollInterval = setInterval(() => {
      window.scrollBy(0, -speed);
    }, 10);
  }
  // 마우스 커서가 아래쪽 스크롤 영역에 있는지 확인합니다.
  else if (clientY > windowHeight - scrollZone) {
    // 커서가 스크롤 영역 내에서 얼마나 아래쪽에 있는지 비율을 계산합니다 (0.0 ~ 1.0).
    // 가장 아래쪽에 가까울수록 1.0에 가까워집니다.
    const speedFactor = (clientY - (windowHeight - scrollZone)) / scrollZone;
    // 스크롤 영역의 80% 이상을 차지하는 부분에서는 최대 속도를, 그 외에는 비율에 따라 속도를 조절합니다.
    const speed = speedFactor > 0.3 ? maxScrollSpeed : maxScrollSpeed * speedFactor;
    // 설정된 속도로 아래로 스크롤하는 인터벌을 시작합니다.
    scrollInterval = setInterval(() => {
      window.scrollBy(0, speed);
    }, 10);
  }
}

onMounted(loadMyBooks);
</script>

<style>
/* --- 3D 책 모델 전용 전역 스타일 --- */
:root {
  /* 대표 책 크기 변수 */
  --rep-book-width: 300px;
  --rep-book-height: 450px;
  --rep-book-depth: 50px;

  /* 책꽂이 책 크기 변수 */
  --shelf-book-width: 170px;
  --shelf-book-height: 250px;
  --shelf-book-depth: 40px;
}

/* --- 대표 책 모델 --- */
.book-3d-wrapper {
  width: var(--rep-book-width);
  height: var(--rep-book-height);
  transform-style: preserve-3d;
  position: relative;
}

.book-3d {
  width: 100%;
  height: 100%;
  position: relative;
  transform-style: preserve-3d;
  transform: translateZ(calc(var(--rep-book-depth) / -2));
}

.book-face {
  position: absolute;
  box-sizing: border-box;
  backface-visibility: hidden;
  width: 100%;
  height: 100%;
  border-radius: 2px 4px 4px 2px;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.book-face.front .bright-edge-effect {
  position: absolute;
  left: 8px;
  top: 0;
  bottom: 0;
  width: 8px;
  background: linear-gradient(to right, rgba(255, 255, 255, 0.441), transparent);
  border-radius: 0 2px 2px 0;
  pointer-events: none;
}

.book-face.front {
  background-size: cover;
  background-position: center;
  transform: translateZ(calc(var(--rep-book-depth)));
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
}

.book-title-overlay {
  width: 60%;
  height: 60%;
  background-color: rgba(255, 255, 255, 0.95);
  padding: 1rem;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  text-align: left;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.1);
}

.book-title-overlay .book-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 25px;
  line-height: 1.4;
  font-weight: 700;
  color: #000000;
}

.book-title-overlay .book-author {
  font-size: 12px;
  font-weight: 600;
  color: #333;
}

.book-face.back {
  background-size: cover;
  background-position: center;
  transform: rotateY(180deg) translateZ(calc(var(--rep-book-depth) / 2));
  filter: brightness(0.7) blur(2px);
}

.book-face.left {
  width: var(--rep-book-depth);
  background-color: #f3f1ed;
  transform: rotateY(-90deg) translateZ(-0.3px) scaleY(0.995);
  transform-origin: left;
  filter: brightness(0.7) blur(0.5px);
}

.book-face.right {
  width: var(--rep-book-depth);
  background-color: #a6916f;
  background-image: repeating-linear-gradient(to right, #ffffff27, #dfdedd 1px, #bbb 1px, #999590c8 3px);
  transform: rotateY(90deg) translateZ(calc(var(--rep-book-width) - var(--rep-book-depth))) translateZ(-2px) scaleY(0.995);
  transform-origin: right;
}

.book-face.top {
  height: var(--rep-book-depth);
  background-color: #e0dace;
  transform: rotateX(90deg) translateZ(0);
  transform-origin: top;
}

.book-face.bottom {
  height: var(--rep-book-depth);
  background-color: #d3c8ba;
  transform: rotateX(-90deg) translateZ(calc(var(--rep-book-height) - var(--rep-book-depth)));
  transform-origin: bottom;
}

/* --- 책꽂이 3D 책 모델 --- */
.shelf-book-model {
  width: 100%;
  height: 100%;
  position: relative;
  transform-style: preserve-3d;
  transform: translateZ(calc(var(--shelf-book-depth) / -2));
  z-index: 1;
  /* 그림자 위에 책이 오도록 설정 */
}

.shelf-book-face {
  position: absolute;
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  background-color: #f0e9dd;
  border: 1px solid rgba(0, 0, 0, 0.1);
  backface-visibility: hidden;
}

.shelf-book-cover {
  width: var(--shelf-book-width);
  height: var(--shelf-book-height);
  transform: translateZ(var(--shelf-book-depth));
  background-size: cover;
  background-position: center;
  border-radius: 2px 4px 4px 2px;
}

.shelf-book-back-cover {
  width: var(--shelf-book-width);
  height: var(--shelf-book-height);
  transform: rotateY(180deg) translateZ(0);
}

.shelf-book-spine {
  width: var(--shelf-book-depth);
  height: var(--shelf-book-height);
  transform: rotateY(-90deg) translateZ(0);
  transform-origin: left;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  /* z-index를 위한 position: relative를 이곳에 둡니다. */
  position: relative;
  /* box-shadow는 아래 ::after로 이동합니다. */
  ;
}

/* [추가] 그림자 효과를 위한 가상 요소 */
.shelf-book-spine::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  /* box-shadow를 부모 대신 이곳에 적용합니다. */
  box-shadow: inset 7px 0 10px -3px rgba(0, 0, 0, 0.4), inset -7px 0 10px -3px rgba(0, 0, 0, 0.6);
  /* 자식 요소(제목 박스) 위에 그림자가 보이도록 z-index 설정 */
  z-index: 1;
  /* 마우스 클릭 등 이벤트를 방해하지 않도록 설정 */
  pointer-events: none;
  ;
}

/* [추가] 책등의 흰색 제목 박스 스타일 */
.spine-title-box {
  background-color: rgba(255, 255, 255, 0.85);
  height: 90%;
  width: 70%;
  border-radius: 2px;
  display: flex;
  align-items: flex-start;
  /* [수정] 상단 정렬 */
  padding: 0.8rem 0;
  /* [수정] 위쪽 여백 추가 */
  justify-content: center;
  padding: 0.5rem 0;
  box-sizing: border-box;
}

/* [추가] 책등의 세로 제목 텍스트 스타일 */
.spine-title {
  writing-mode: vertical-rl;
  text-orientation: mixed;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-height: 100%;
  font-family: 'Noto Serif KR', serif;
  /* [추가] 표지와 동일한 글꼴 */

  font-size: 12px;
  font-weight: 600;
  color: #000000;
}

.shelf-book-side-edge {
  width: var(--shelf-book-depth);
  height: var(--shelf-book-height);
  transform: rotateY(90deg) translateZ(calc(var(--shelf-book-width) - var(--shelf-book-depth)));
  transform-origin: right;
  background-image: repeating-linear-gradient(to bottom, #d9d1c3, #c9bfad 1px, #d9d1c3 1px, #d9d1c3 2px);
}

.shelf-book-top {
  height: var(--shelf-book-depth);
  transform: rotateX(90deg) translateZ(0px);
  transform-origin: top;
  background: #e0dace;
}

.shelf-book-bottom {
  height: var(--shelf-book-depth);
  transform: rotateX(-90deg) translateZ(calc(var(--shelf-book-height) - var(--shelf-book-depth)));
  transform-origin: bottom;
  background: #d3c8ba;
}
</style>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&family=Pretendard:wght@400;500;700&display=swap');
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

/* --- 페이지 기본 & 버튼 --- */
.my-library-page {
  padding: 80px 2rem 2rem;
  background-color: #ffffff;
  color: #261E17;
  min-height: calc(100vh - 56px);
  font-family: 'Pretendard', sans-serif;
}

.content-section {
  padding: 0 2.5rem 2.5rem 2.5rem;
  margin: 0 auto 3rem auto;
  max-width: 1200px;
}

.section-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 2.8rem;
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

.representative-book-section .section-title {
  color: #26250F;
}

.book-shelves-section .section-title {
  color: #000000;
}

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

/* --- 대표 책 섹션 --- */
.rep-book-display-area {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 500px;
  margin-bottom: 2rem;
}

.rep-book-container {
  perspective: 1200px;
  cursor: grab;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: var(--rep-book-width);
  height: var(--rep-book-height);
}

.rep-book-container:active {
  cursor: grabbing;
}

.rep-book-shadow {
  position: absolute;
  bottom: -30px;
  left: 50%;
  transform: translateX(-50%);
  width: 350px;
  height: 40px;
  background-color: rgba(38, 30, 23, 0.6);
  border-radius: 100%;
  filter: blur(30px);
  z-index: 0;
  pointer-events: none;
  transition: opacity 0.1s ease-out;
}

.book-3d-wrapper {
  position: relative;
  z-index: 1;
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

/* --- 책꽂이 공통 --- */
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
  background-color: #6B4F3A;
  /* Fallback color */
  background-image: url('https://plus.unsplash.com/premium_photo-1671612828903-dc019accc402?q=80&w=774&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D');
  background-size: cover;
  background-position: center;
  padding: 1.5rem 3rem 1rem 3rem;
  margin: 0;
  border-radius: 10px 10px 0 0;
  border-top: 2px solid rgb(142, 142, 142);
  border-right: 2px solid rgb(115, 115, 115);
  border-left: 2px solid rgb(115, 115, 115);
  border-bottom: none;
  box-shadow: none;
  box-sizing: border-box;

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

.shelf-book-container {
  padding: 2rem 1rem 0.8rem 0.8rem;
  perspective: 1500px;
}

.my-books-container,
.group-bookshelf-inner {
  background-color: #4b3d2a4f;
  box-shadow: inset 0 0 15px 5px rgba(0, 0, 0, 0.6);
  border: 2px solid rgb(115, 115, 115);
}

.shelf-book-list,
.group-shelf-horizontal {
  display: flex;
  gap: 0.5rem;
  min-height: auto;
  /* [수정] 최소 높이 제거 */
  align-items: flex-end;
  /* [수정] 책을 아래쪽 기준으로 정렬 */
  padding: 0rem 1rem 0rem 4rem;
  /* [수정] 아래쪽 여백(padding-bottom)을 0으로 설정 */
}


/* --- 3D 책 아이템 --- */
.shelf-book-item-3d {
  width: var(--shelf-book-width);
  height: var(--shelf-book-height);
  flex-shrink: 0;
  cursor: grab;
  transform-style: preserve-3d;
  position: relative;
  transform: rotateY(70deg) translateX(-30px);
  /* 시각적 오버랩 */
  transition: transform 0.6s cubic-bezier(0.25, 1, 0.5, 1);
  margin-left: -60px;
  margin-right: -60px;

}

/* .shelf-book-item-3d:not(.selected):hover {
  transform: rotateY(70deg) translateZ(20px) scale(1.02);
} */

.shelf-book-item-3d:hover {
  transform: rotateY(0deg) translateZ(60px) scale(1.05);
  z-index: 10;
}

.sortable-ghost .shelf-book-item-3d {
  opacity: 0.4;
}

/* --- 표지 효과 --- */
.shelf-bright-edge-effect {
  position: absolute;
  left: 5px;
  top: 0;
  bottom: 0;
  width: 5px;
  background: linear-gradient(to right, rgba(255, 255, 255, 0.4), transparent);
  pointer-events: none;
}

.shelf-book-face.shelf-book-cover {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
  filter: brightness(0.5);
  transition: filter 0.6s cubic-bezier(0.25, 1, 0.5, 1);
}

.shelf-book-item-3d:hover .shelf-book-face.shelf-book-cover {
  filter: brightness(1);
}

.shelf-book-title-overlay {
  width: 65%;
  height: 60%;
  background-color: rgba(255, 255, 255, 0.95);
  padding: 0.5rem;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  /* 제목을 위로, 저자를 아래로 보냅니다 */
  align-items: flex-start;
  /* 모든 내용을 왼쪽으로 정렬합니다 */
  text-align: left;
  /* 텍스트를 왼쪽 정렬합니다 */
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  transition: opacity 0.6s cubic-bezier(0.25, 1, 0.5, 1);
}

.shelf-book-item-3d:not(.selected) .shelf-book-title-overlay {
  opacity: 0.5;
}

.shelf-book-item-3d:hover .shelf-book-title-overlay {
  opacity: 1;
}

.shelf-book-title {
  font-family: 'Noto Serif KR', serif;
  /* 대표책과 동일한 글꼴 적용 */
  font-size: 1rem;
  /* 글자 크기 조정 */
  font-weight: 600;
  /* 굵기 조정 */
  line-height: 1.4;
  color: #000000;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.shelf-book-author {
  font-size: 0.75rem;
  /* 글자 크기 조정 */
  font-weight: 600;
  /* 굵기 조정 */
  color: #333;
}


/* --- UI & 그룹 --- */
.remove-book-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 20;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  border-radius: 50%;
  width: 22px;
  height: 22px;
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s;
}

.shelf-book-item-3d:hover .remove-book-btn {
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
  background-color: #6B4F3A;
  /* Fallback color */
  background-image: url('https://plus.unsplash.com/premium_photo-1671612828903-dc019accc402?q=80&w=774&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D');
  background-size: cover;
  background-position: center;
  position: relative;
  padding: 0rem 3rem 1rem 3rem;
  margin: 0;
  box-shadow: none;
  border-left: 2px solid rgb(115, 115, 115);
  border-right: 2px solid rgb(115, 115, 115);
}

.group-shelf-wrapper:last-child {
  padding-bottom: 3rem;
  border-radius: 0 0 10px 10px;
  border-bottom: 2px solid black;
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
  transform: scale(1.05);
  color: #333;
  text-decoration: none;
}

.group-shelf-title-placeholder {
  font-family: 'Noto Serif KR', serif;
  font-size: 1.4rem;
  font-weight: 700;
  color: #888;
  font-style: italic;
}

.group-bookshelf-inner {
  padding: 2rem 1rem 0.8rem 0.8rem;
}

.group-shelf-wrapper .shelf-book-container {
  background-color: transparent;
  border: none;
  box-shadow: none;
  padding: 0;
}


.group-books-draggable-area {
  display: flex;
  gap: 0.5rem;
  flex-grow: 1;
  min-width: 120px;
  min-height: 250px;
  border-radius: 6px;
  align-items: center;
}

.no-groups-message {
  text-align: center;
  color: #888;
  padding: 2rem;
  border: 2px dashed #eee;
  border-radius: 8px;
  margin-top: 2rem;
}

/* --- 모달 --- */
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

.book-selection-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  max-height: 300px;
  overflow-y: auto;
  margin-bottom: 1.5rem;
  border: 1px solid #E0E0E0;
  border-radius: 8px;
  padding: 0.75rem;
}

.book-selection-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.book-selection-item:hover {
  background-color: #f5f5f5;
}

.book-selection-item input[type="radio"] {
  margin-right: 0.5rem;
}

.book-cover-thumbnail {
  width: 40px;
  height: 60px;
  object-fit: cover;
  border-radius: 2px;
}

.book-title-radio {
  font-weight: 500;
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

/* 책꽂이 책 아래 그림자 효과 (디버깅용) */
.shelf-book-item-3d::after {
  content: '';
  position: absolute;
  bottom: -11px;
  /* 확실히 보이도록 아래로 이동 */
  right: 20%;
  /* 
  transform: translateX(-30%); */
  width: 100%;
  height: 20px;
  background-color: black;
  /* 완전한 검은색 */
  border-radius: 10%;
  filter: blur(30px);
  /* 흐림 효과 제거 */
  opacity: 0.9;
  /* 완전 불투명 */
  transform: rotateX(100deg);
  z-index: 0;
}

.shelf-book-item-3d:hover::after {
  opacity: 0.9;
  /* 호버 시 반투명하게 */
  /* transition: opacity 0.3s ease-in-out; */
  filter: blur(5px);
  /* 호버 시 흐림 효과  */
  bottom: -11px;
  /* 호버 시 아래로 이동 */
  right: 0%;
  width: 100%;
  transform: rotateX(20deg);
  border-radius: 80%;
}

.published-sticker-shelf {
  position: absolute;
  bottom: 5px;
  right: 5px;
  width: 60px;
  height: 60px;
  z-index: 10;
  transform: rotate(15deg);
}
</style>
