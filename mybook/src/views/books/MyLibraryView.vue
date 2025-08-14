<template>
  <div class="my-library-page">
    <aside class="representative-book-section">
      <h2 class="representative-title">나의 대표 책</h2>
      <p class="representative-subtitle">당신의 삶에 가장 큰 영감을 준 책을 설정해보세요.</p>
      <div class="rep-book-display-area">
        <div v-if="currentRepBook" class="rep-book-container" @mousemove="handleMouseMove"
          @mouseenter="isMouseOver = true" @mouseleave="resetRotation(); isMouseOver = false"
          @click="selectShelfBook(currentRepBook)">
          <div class="rep-book-shadow"></div>
          <div class="book-3d-wrapper" :style="repBookStyle" :class="{ 'animated': !isMouseOver }">
            <div class="book-3d">
              <div class="book-face front" :style="{ backgroundImage: `url(${currentRepBook.coverImageUrl})` }">
                <div class="bright-edge-effect"></div>
                <div class="book-title-overlay">
                  <div class="book-title">{{ currentRepBook.title }}</div>
                  <div class="book-author">{{ currentRepBook.authorName }}</div>
                </div>
              </div>
              <div class="book-face back" :style="{ backgroundImage: `url(${currentRepBook.coverImageUrl})` }"></div>
              <div class="book-face left" :style="{ backgroundImage: `url(${currentRepBook.coverImageUrl})` }"></div>
              <div class="book-face right"></div>
              <div class="book-face top"></div>
              <div class="book-face bottom"></div>
            </div>
          </div>
        </div>
        <div v-else class="no-rep-book">
          대표 책을 선택해주세요.
        </div>
      </div>
      <button @click="openRepBookModal" class="btn btn-primary select-rep-btn">
        <i class="bi bi-pencil-square"></i> 대표 책 선택
      </button>
    </aside>

    <main class="book-shelves-section">
      <div class="group-shelf-header">
        <h2 class="shelves-title">나의 책장</h2>
        <button @click="isGroupModalVisible = true" class="btn btn-primary add-group-btn">
          <i class="bi bi-plus-lg"></i> 그룹 추가
        </button>
      </div>
      <p class="shelves-subtitle">그룹 책장을 만들어 친구들과 함께 책을 완성하고 공유해보세요.</p>

      <div class="my-books-shelf-wrapper">
        <div class="shelf-title-container">
          <h3 class="shelf-title">내가 쓴 책들
            <small>책을 드래그하여 아래 그룹에 추가하세요</small>
          </h3>
          <div class="group-shortcut-wrapper">
            <button @click="toggleGroupList" class="btn btn-secondary group-shortcut-btn">
              그룹 바로가기 <i :class="['bi', isGroupToggleVisible ? 'bi-chevron-up' : 'bi-chevron-down']"></i>
            </button>
            <transition name="fade">
              <draggable v-if="isGroupToggleVisible" v-model="allGroups" item-key="groupId" tag="div"
                class="group-shortcut-list" handle=".drag-handle-shortcut">
                <template #item="{ element: group }">
                  <div class="group-shortcut-item">
                    <i class="bi bi-grip-vertical drag-handle-shortcut"></i>
                    <a @click.prevent="scrollToGroup(group.groupId)" href="#" class="group-name-link">
                      {{ group.groupName }}
                    </a>
                  </div>
                </template>
              </draggable>
            </transition>
          </div>
        </div>
        <div class="shelf-book-container my-books-container">
          <draggable v-model="myBooks" item-key="bookId" :group="{ name: 'myBooksSource', pull: 'clone' }"
            class="shelf-book-list" tag="div">
            <template #item="{ element: book }">
              <div class="shelf-book-item-3d" @click="selectShelfBook(book)" :title="book.title">
                <div class="shelf-book-model">
                  <div class="shelf-book-face shelf-book-cover"
                    :style="{ backgroundImage: `url(${book.coverImageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
                    <div v-if="book.isCommunityBook" class="community-sash">서점 출판완료</div>
                    <img v-if="book.isCommunityBook" src="/images/complete.png" alt="커뮤니티 책"
                      class="published-sticker-shelf" />
                    <div class="shelf-bright-edge-effect"></div>
                    <div class="shelf-book-title-overlay">
                      <div class="shelf-book-title">{{ book.title }}</div>
                      <div class="shelf-book-author">{{ book.authorName }}</div>
                    </div>
                    <div class="post-it-container">
                      <div v-for="groupIndex in bookGroupIndices[book.bookId]" :key="groupIndex" class="post-it">
                        {{ toRoman(groupIndex + 1) }}
                      </div>
                    </div>
                  </div>
                  <div class="shelf-book-face shelf-book-spine"
                    :style="{ backgroundImage: `url(${book.coverImageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
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
        <div v-for="(group, index) in displayedGroups" :key="group.groupId" :id="`group-shelf-${group.groupId}`"
          class="group-shelf-wrapper">
          <div class="group-shelf-title-bar">
            <router-link v-if="group.groupId" :to="`/group-timeline/${group.groupId}`" class="group-shelf-title"
              :title="`${group.groupName} 타임라인으로 이동`">
              <span class="roman-numeral">{{ toRoman(index + 1) }}. </span>{{ group.groupName }}
            </router-link>
            <span v-else class="group-shelf-title-placeholder">{{ group.groupName }}</span>
            <button v-if="group.groupId" @click="toggleEditMode(group.groupId)"
              class="btn btn-secondary edit-group-btn">
              {{ editingGroupId === group.groupId ? '완료' : '책 삭제' }}
            </button>
          </div>
          <div class="group-bookshelf-inner">
            <div class="shelf-book-container">
              <draggable :list="group.books" item-key="bookId"
                :group="{ name: 'groupBooksTarget', pull: true, put: ['myBooksSource'] }"
                class="shelf-book-list group-shelf-horizontal" tag="div" @change="handleChange($event, group.groupId)"
                :disabled="editingGroupId !== null">
                <template #item="{ element: book }">
                  <div class="shelf-book-wrapper">

                    <div class="shelf-book-item-3d" @click="selectShelfBook(book)" :title="book.title"
                      :class="{ 'editing': editingGroupId === group.groupId }">
                      <div class="shelf-book-model">
                        <div class="shelf-book-face shelf-book-cover"
                          :style="{ backgroundImage: `url(${book.coverImageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
                          <div class="shelf-bright-edge-effect"></div>
                          <div class="shelf-book-title-overlay">
                            <div class="shelf-book-title">{{ book.title }}</div>
                            <div class="shelf-book-author">{{ book.authorName }}</div>
                          </div>
                        </div>
                        <div class="shelf-book-face shelf-book-spine"
                          :style="{ backgroundImage: `url(${book.coverImageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
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

                    <button v-if="editingGroupId === group.groupId"
                      @click.stop="removeBookFromGroup(group.groupId, book.bookId)" class="remove-book-btn-spine"
                      title="그룹에서 책 제거">
                      <i class="bi bi-trash"></i>
                    </button>

                  </div>
                </template>
              </draggable>
            </div>
          </div>
        </div>
      </div>
    </main>

    <div v-if="isRepBookModalVisible || isGroupModalVisible || isMessageBoxVisible" class="modal-backdrop">
      <div class="modal-content" :class="{ 'modal-sm': isMessageBoxVisible }">
        <button @click="closeAllModals" class="close-button" title="닫기"><i class="bi bi-x-lg"></i></button>
        <div v-if="isRepBookModalVisible">
          <h2 class="modal-title">대표 책 선택</h2>
          <p class="modal-description">나의 대표 책을 한권 선택해주세요.</p>
          <div class="book-selection-list">
            <label v-for="book in myBooks" :key="book.bookId" class="book-selection-item">
              <input type="radio" :value="book.bookId" v-model="selectedRepBookId" name="rep-book" />
              <img :src="book.coverImageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'"
                alt="Book Cover" class="book-cover-thumbnail" />
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
              placeholder="예: 아북고 친구들 , 아북이 가족 " />
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
import apiClient from '@/api';
import { communityService, type CommunityBook } from '@/services/communityService';

const router = useRouter();

// --- Interfaces & Types ---
interface Book {
  bookId: string;
  title: string;
  memberId: string;
  authorName?: string;
  coverImageUrl?: string;
  completed?: boolean;
  published?: boolean;
  isGroupBook?: boolean;
  isCommunityBook?: boolean; // 커뮤니티 책 여부 플래그
  groupId?: string;
}
interface GroupResponse {
  groupId: string;
  groupName: string;
  ownerId: string;
  managers: string[];
  members: string[];
  createdAt: string; // API 응답은 보통 ISO 문자열로 오므로 string으로 받음
  deletedAt?: string | null; // soft-delete 필드 추가
}
// `GroupResponse`에서 `createdAt`을 제외하고, 새로운 `createdAt`과 `books`를 추가합니다.
type Group = Omit<GroupResponse, 'createdAt'> & {
  books: Book[];
  createdAt: Date; // 프론트엔드에서는 Date 객체로 변환하여 사용
};
interface GroupBook {
  groupBookId: string;
  title: string;
  authorNickname: string;
  coverImageUrl?: string;
}
interface ChangeEvent {
  added?: {
    element: Book;
    newIndex: number;
  };
  removed?: {
    element: Book;
    oldIndex: number;
  };
  moved?: {
    element: Book;
    newIndex: number;
    oldIndex: number;
  };
}

// --- Reactive State ---
const representativeBooks = ref<Book[]>([]);
const myBooks = ref<Book[]>([]);
const allGroups = ref<Group[]>([]);
const isRepBookModalVisible = ref(false);
const selectedRepBookId = ref<string | null>(null);
const isGroupModalVisible = ref(false);
const newGroupName = ref('');
const isMessageBoxVisible = ref(false);
const messageBoxTitle = ref('');
const messageBoxContent = ref('');
const repBookRotationY = ref(0);
const isGroupToggleVisible = ref(false);
const editingGroupId = ref<string | null>(null);
const isMouseOver = ref(false);

// --- Computed Properties ---
const currentRepBook = computed(() => (representativeBooks.value.length > 0 ? representativeBooks.value[0] : null));
const repBookStyle = computed(() => ({ transform: `rotateY(${repBookRotationY.value}deg)`, transition: 'transform 0.1s ease-out' }));

const bookGroupIndices = computed(() => {
  const indices: { [bookId: string]: number[] } = {};
  myBooks.value.forEach(book => {
    indices[book.bookId] = [];
    allGroups.value.forEach((group, groupIndex) => {
      if (group.books.some(b => b.bookId === book.bookId)) {
        indices[book.bookId].push(groupIndex);
      }
    });
  });
  return indices;
});

const displayedGroups = computed(() => {
  if (allGroups.value.length === 0) {
    return [{
      groupId: '',
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

// --- Functions ---
async function loadMyBooks() {
  try {
    const personalBooksPromise = apiClient.get('/api/v1/books');
    const communityBooksPromise = communityService.getMyCommunityBooks({ size: 100 }); // 충분히 큰 사이즈로 모든 책을 가져옴

    const [personalBooksResponse, communityBooksResponse] = await Promise.all([personalBooksPromise, communityBooksPromise]);

    const personalBooks = personalBooksResponse.data.data
      .filter((book: Book) => book.completed)
      .map((book: Book) => ({ ...book, isCommunityBook: false, authorName: book.authorName }));

    const communityBooks = communityBooksResponse.content.map((book: CommunityBook) => ({
      bookId: String(book.communityBookId),
      title: book.title,
      memberId: String(book.memberId),
      authorName: book.authorNickname,
      coverImageUrl: book.coverImageUrl,
      completed: true,
      published: true,
      isCommunityBook: true,
    }));

    myBooks.value = [...personalBooks, ...communityBooks];

    if (myBooks.value.length > 0) {
      representativeBooks.value = [myBooks.value[0]];
    }
  } catch (error) {
    console.error("내 책 목록을 불러오는데 실패했습니다:", error);
  }
}

async function loadMyGroups() {
  try {
    const groupResponse = await apiClient.get<{ data: GroupResponse[] }>('/api/v1/members/me/groups', {
      headers: {
        'Cache-Control': 'no-cache',
      },
    });

    const groupsWithBooks = await Promise.all(
      groupResponse.data.data.map(async (group) => {
        try {
          const booksResponse = await apiClient.get(`/api/v1/groups/${group.groupId}/books`);
          const books = booksResponse.data.data.map((book: GroupBook) => ({
            bookId: book.groupBookId, // groupBookId를 bookId로 매핑
            title: book.title,
            authorName: book.authorNickname,
            coverImageUrl: book.coverImageUrl,
            isGroupBook: true,
            groupId: group.groupId
            // 필요한 다른 필드들도 여기에 매핑
          }));
          return {
            ...group,
            books: books,
            createdAt: new Date(group.createdAt),
          };
        } catch (error) {
          console.error(`그룹 ${group.groupId}의 책 목록을 불러오는데 실패했습니다:`, error);
          return {
            ...group,
            books: [], // 에러 발생 시 빈 배열로 처리
            createdAt: new Date(group.createdAt),
          };
        }
      })
    );

    allGroups.value = groupsWithBooks;
  } catch (error) {
    console.error("내 그룹 목록을 불러오는데 실패했습니다:", error);
    showMessageBox('그룹 목록을 가져오는 데 실패했습니다. 다시 시도해주세요.', '오류');
  }
}
// 클릭 시 바로 상세 페이지로 이동하도록 변경
function selectShelfBook(book: Book) {
  if (editingGroupId.value) return;

  // isGroupBook 플래그를 확인하여 분기
  if (book.isGroupBook && book.groupId) {
    router.push(`/group-book-detail/${book.groupId}/${book.bookId}`);
  } else if (book.isCommunityBook) {
    router.push({ name: 'BookstoreBookDetail', params: { id: book.bookId } });
  }
  else {
    router.push(`/book-detail/${book.bookId}`);
  }
}
function closeAllModals() {
  isRepBookModalVisible.value = false;
  isGroupModalVisible.value = false;
  isMessageBoxVisible.value = false;
}
function showMessageBox(message: string, title = '알림') { messageBoxTitle.value = title; messageBoxContent.value = message; isMessageBoxVisible.value = true; }

async function handleChange(event: ChangeEvent, groupId: string) {
  console.log('handleChange triggered!', { event, groupId });

  if (!event.added) {
    console.log('No item added, exiting.');
    return;
  }

  const { element: droppedBook } = event.added;
  console.log('Dropped book:', droppedBook);
  const group = allGroups.value.find(g => g.groupId === groupId);

  if (!group) {
    console.log('Group not found, exiting.');
    return;
  }

  // API 호출
  try {
    await apiClient.post(`/api/v1/books/${droppedBook.bookId}/export/group/${groupId}`);
    showMessageBox(`'${droppedBook.title}' 책이 '${group.groupName}' 그룹에 추가되었습니다.`);

    // 성공 시 그룹 목록을 다시 로드하여 UI를 최신 상태로 업데이트
    await loadMyGroups();

  } catch (error) {
    console.error("그룹에 책 추가 실패:", error);
    showMessageBox('그룹에 책을 추가하는 데 실패했습니다.', '오류');

    // 에러 발생 시에도 그룹 목록을 다시 로드하여 UI를 서버 상태와 동기화
    await loadMyGroups();
  }
}

function removeBookFromGroup(groupId: string, bookId: string) {
  const group = allGroups.value.find(g => g.groupId === groupId);
  if (group) {
    const book = group.books.find(b => b.bookId === bookId);
    if (book && confirm(`'${book.title}' 책을 '${group.groupName}' 그룹에서 제거하시겠습니까?`)) {
      group.books = group.books.filter(b => b.bookId !== bookId);
      showMessageBox(`책이 '${group.groupName}' 그룹에서 제거되었습니다.`);
    }
  }
}
function openRepBookModal() {
  selectedRepBookId.value = representativeBooks.value.length > 0 ? representativeBooks.value[0].bookId : null;
  isRepBookModalVisible.value = true;
}
function saveRepresentativeBookHandler() {
  if (selectedRepBookId.value) {
    const selectedBook = myBooks.value.find(book => book.bookId === selectedRepBookId.value);
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

  try {
    const formData = new FormData();
    formData.append('groupName', groupName);

    const response = await apiClient.post('/api/v1/groups', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    const newGroupData = response.data.data;

    // API 응답을 기반으로 새 그룹 객체 생성
    const newGroup: Group = {
      ...newGroupData,
      books: [], // 새 그룹에는 아직 책이 없음
      createdAt: new Date(newGroupData.createdAt), // ISO 문자열을 Date 객체로 변환
    };

    allGroups.value.push(newGroup);
    isGroupModalVisible.value = false;
    newGroupName.value = '';
    showMessageBox('그룹이 성공적으로 생성되었습니다.');

    await nextTick();
    const newGroupElement = document.getElementById(`group-shelf-${newGroup.groupId}`);
    if (newGroupElement) {
      newGroupElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  } catch (error) {
    console.error("그룹 생성에 실패했습니다:", error);
    showMessageBox('그룹 생성에 실패했습니다. 다시 시도해주세요.', '오류');
  }
}

function toggleGroupList() {
  isGroupToggleVisible.value = !isGroupToggleVisible.value;
}

function scrollToGroup(groupId: string) {
  const groupElement = document.getElementById(`group-shelf-${groupId}`);
  if (groupElement) {
    groupElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
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

function toggleEditMode(groupId: string) {
  if (editingGroupId.value === groupId) {
    editingGroupId.value = null;
  } else {
    editingGroupId.value = groupId;
  }
}

function toRoman(num: number): string {
  const romanNumerals: { [key: string]: number } = {
    M: 1000, CM: 900, D: 500, CD: 400, C: 100, XC: 90, L: 50, XL: 40, X: 10, IX: 9, V: 5, IV: 4, I: 1
  };
  let result = '';
  for (const key in romanNumerals) {
    while (num >= romanNumerals[key]) {
      result += key;
      num -= romanNumerals[key];
    }
  }
  return result;
}

onMounted(() => {
  loadMyBooks();
  loadMyGroups();
});
</script>

<style>
/* --- 3D 책 모델 전용 전역 스타일 --- */
:root {
  /* 대표 책 크기 변수 */
  --rep-book-width: 173px;
  --rep-book-height: 259px;
  --rep-book-depth: 29px;

  /* 책꽂이 책 크기 변수 */
  --shelf-book-width: 153px;
  --shelf-book-height: 225px;
  --shelf-book-depth: 36px;
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
  left: 7px;
  top: 0;
  bottom: 0;
  width: 7px;
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
  padding: 13px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  text-align: left;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.1);
}

.book-title-overlay .book-title {
  /* 대표책 표지 위 제목 */
  font-family: 'ChosunCentennial', serif;
  font-size: 13px;
  line-height: 1.4;
  font-weight: 700;
  color: #000000;
}

.book-title-overlay .book-author {
  /* 대표책 표지 위 저자 */
  font-family: 'NanumSquareR', serif;
  font-size: 9px;
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
  transform: rotateY(-90deg) translateZ(0) translateY(-225px);
  transform-origin: left;
  background-size: cover;
  background-position: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding-top: 0.9rem;
  box-sizing: border-box;
  position: relative;

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
  box-shadow: inset 6px 0 9px -3px rgba(0, 0, 0, 0.4), inset -6px 0 9px -3px rgba(0, 0, 0, 0.6);
  /* 자식 요소(제목 박스) 위에 그림자가 보이도록 z-index 설정 */
  z-index: 1;
  /* 마우스 클릭 등 이벤트를 방해하지 않도록 설정 */
  pointer-events: none;
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
  padding: 0.45rem 0;
  /* [수정] 위쪽 여백 추가 */
  justify-content: center;
  box-sizing: border-box;
  z-index: 2;
}

/* [추가] 책등의 세로 제목 텍스트 스타일 */
.spine-title {
  /* 책꽂이 책 등(세로) 제목 */
  writing-mode: vertical-rl;
  text-orientation: mixed;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-height: 100%;
  font-family: 'ChosunCentennial', serif;
  /* [추가] 표지와 동일한 글꼴 */
  font-size: 13px;
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
@import url('https://fonts.googleapis.com/css2?family=Gaegu&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&family=Pretendard:wght@400;500;700&display=swap');
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

/* --- 페이지 기본 & 버튼 --- */
.my-library-page {
  display: flex;
  align-items: flex-start;
  background-color: #ffffff;
  color: #261E17;
  min-height: 100vh;
  font-family: 'SCDream4', sans-serif;
  padding: 0 5.4rem;
}


.representative-book-section {
  width: 25%;
  flex-shrink: 0;
  position: sticky;
  top: 88px;
  height: calc(100vh - 56px);
  padding: 1.8rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  box-sizing: border-box;
  z-index: 2;
}

.book-shelves-section {
  flex-grow: 1;
  padding: 1.8rem 2.25rem;
}

/* --- 글씨 스타일 --- */
.representative-title {
  font-family: 'SCDream3', serif;
  font-size: 1.8rem;
  font-weight: 700;
  text-align: center;
  margin-top: 1.8rem;
  color: #26250F;
}

.representative-subtitle {
  font-size: 0.9rem;
  opacity: 0.8;
  max-width: 225px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.7;
  text-align: center;
}

.shelves-title {
  font-family: 'SCDream3', serif;
  font-size: 2.7rem;
  font-weight: 700;
  margin-bottom: 0.65rem;
  text-align: center;
  color: #000000;
  margin-top: 0;
}

.shelves-subtitle {
  font-size: 1rem;
  opacity: 0.8;
  margin-bottom: 1.35rem;
  max-width: 540px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.7;
  text-align: center;
}

.btn {
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  font-size: 0.85rem;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.45rem;
  padding: 0.5rem 1.1rem;
}

@keyframes fill-animation {
  0% {
    transform-origin: top;
    transform: scaleY(0);
  }

  50% {
    transform-origin: top;
    transform: scaleY(1);
  }

  50.1% {
    transform-origin: bottom;
    transform: scaleY(1);
  }

  100% {
    transform-origin: bottom;
    transform: scaleY(0);
  }
}

.btn.btn-primary {
  position: relative;
  overflow: hidden;
  z-index: 1;
  display: inline-block;
  border: 3px solid #5b673b !important;
  border-radius: 18px !important;
  margin-left: 0.9rem !important;
  margin-right: 0.9rem !important;
  padding: 0.45rem 1.1rem !important;
  font-size: 0.9rem !important;
  white-space: nowrap;
  font-family: 'SCDream5', sans-serif;
  transition: color 0.5s ease;
  background-color: transparent;
  color: #000000;
}

.btn.btn-primary::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(185, 174, 122, 0.4);
  transform: scaleY(0);
  z-index: -1;
  animation: fill-animation 3s infinite ease-in-out;
}

.btn-primary:hover {
  color: white !important;
  border-color: #dee2e6 !important;
  background-color: transparent;
}

/* --- 대표 책 섹션 --- */
.rep-book-display-area {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  min-height: 270px;
  margin: 1.8rem 0;
  transition: margin 0.3s ease, min-height 0.3s ease;
}

.rep-book-container {
  perspective: 1080px;
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
  bottom: -18px;
  left: 50%;
  transform: translateX(-50%);
  width: 270px;
  height: 27px;
  background-color: rgba(38, 30, 23, 0.6);
  border-radius: 100%;
  filter: blur(27px);
  z-index: 0;
  pointer-events: none;
  transition: opacity 0.1s ease-out;
}

@keyframes gentle-rotate {
  0% {
    transform: rotateY(0deg);
  }

  25% {
    transform: rotateY(-25deg);
  }

  50% {
    transform: rotateY(0deg);
  }

  75% {
    transform: rotateY(25deg);
  }

  100% {
    transform: rotateY(0deg);
  }
}

.book-3d-wrapper {
  position: relative;
  z-index: 1;
}

.book-3d-wrapper.animated {
  animation: gentle-rotate 5s ease-in-out infinite;
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
  margin-top: 1.8rem;
  margin-bottom: 1.8rem;
  flex-shrink: 0;
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

.my-books-shelf-wrapper {
  background-color: #6B4F3A;
  background-image: url('https://plus.unsplash.com/premium_photo-1671612828903-dc019accc402?q=80&w=774&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG9otby1wYWdlfHx8fGVufDB8fHx8fA%3D%3D');
  background-size: cover;
  background-position: center;
  padding: 1.35rem 2.7rem 0.9rem 2.7rem;
  margin: 0;
  border-radius: 9px 9px 0 0;
  border-top: 2px solid rgb(142, 142, 142);
  border-right: 2px solid rgb(115, 115, 115);
  border-left: 2px solid rgb(115, 115, 115);
  border-bottom: none;
  box-shadow: none;
  box-sizing: border-box;

}

.shelf-title-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.9rem;
}

.shelf-title {
  font-family: 'EBSHunminjeongeumSaeronL', serif;
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0;
}

.shelf-title small {
  font-family: 'SCDream4', sans-serif;
  font-size: 0.8rem;
  font-weight: 400;
  opacity: 0.7;
  margin-left: 0.65rem;
}

.shelf-book-container {
  padding: 1.8rem 0.9rem 0.7rem 0.7rem;
  perspective: 1350px;
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
  flex-wrap: wrap;
  gap: 0.45rem;
  min-height: 234px;
  align-items: flex-end;
  padding: 0rem 0.9rem 0rem 3.6rem;
}


/* --- 3D 책 아이템 --- */
.shelf-book-item-3d {
  width: var(--shelf-book-width);
  height: var(--shelf-book-height);
  flex-shrink: 0;
  cursor: grab;
  transform-style: preserve-3d;
  position: relative;
  transform: rotateY(70deg) translateX(-27px);
  transition: transform 0.6s cubic-bezier(0.25, 1, 0.5, 1);

}

.shelf-book-item-3d.editing {
  cursor: default;
}

.shelf-book-item-3d:not(.editing):hover {
  transform: rotateY(0deg) translateZ(54px) scale(1.05);
  z-index: 10;
}

.sortable-ghost .shelf-book-item-3d {
  opacity: 0.4;
}

/* --- 표지 효과 --- */
.shelf-bright-edge-effect {
  position: absolute;
  left: 4px;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(to right, rgba(255, 255, 255, 0.4), transparent);
  pointer-events: none;
}

.shelf-book-face.shelf-book-cover {
  color: #333;
  filter: brightness(0.5);
  transition: filter 0.6s cubic-bezier(0.25, 1, 0.5, 1);
  position: relative;
}

.shelf-book-item-3d:not(.editing):hover .shelf-book-face.shelf-book-cover {
  filter: brightness(1);
}

.shelf-book-title-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 65%;
  height: 60%;
  background-color: rgba(255, 255, 255, 0.95);
  padding: 11px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-start;
  text-align: left;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.shelf-book-item-3d:not(.selected) .shelf-book-title-overlay {
  opacity: 0.5;
}

.shelf-book-item-3d:not(.editing):hover .shelf-book-title-overlay {
  opacity: 1;
}

.shelf-book-title {
  font-family: 'ChosunCentennial', serif;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.4;
  color: #000000;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.shelf-book-author {
  font-size: 9px;
  font-weight: 600;
  color: #333;
}

.post-it-container {
  position: absolute;
  top: 9px;
  right: -18px;
  z-index: 20;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.post-it {
  background-color: #ffc;
  padding: 4px 7px;
  font-family: 'Gaegu', cursive;
  font-size: 12px;
  color: #333;
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.2);
  transform: rotate(15deg);
  width: 27px;
  text-align: center;
}

.my-books-container .shelf-book-item-3d,
.group-shelf-horizontal .shelf-book-wrapper {
  margin-left: -54px;
  margin-right: -54px;
}

.shelf-book-wrapper {
  position: relative;
  width: var(--shelf-book-width);
  height: var(--shelf-book-height);
  flex-shrink: 0;
}

.remove-book-btn-spine {
  position: absolute;
  z-index: 20;
  top: -29px;
  left: 29px;
  border-radius: 27px;
  color: #333;
  border: none;
  width: 25px;
  height: 25px;
  font-size: 18px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.remove-book-btn-spine:hover {
  color: #000000;
  transform: scale(1.3);
}

.shelf-book-item-3d.editing+.remove-book-btn-spine {
  opacity: 1;
  visibility: visible;
}

.remove-book-btn-spine {
  opacity: 0;
  visibility: hidden;
}

.group-shelf-header {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.9rem;
  margin-bottom: 0.45rem;
}

.group-shelves-container {
  margin-top: 0;
  display: flex;
  flex-direction: column;
  gap: 0rem;
}

.group-shelf-wrapper {
  background-color: #6B4F3A;
  background-image: url('https://plus.unsplash.com/premium_photo-1671612828903-dc019accc402?q=80&w=774&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG9otby1wYWdlfHx8fGVufDB8fHx8fA%3D%3D');
  background-size: cover;
  background-position: center;
  position: relative;
  padding: 0rem 2.7rem 0.9rem 2.7rem;
  margin: 0;
  box-shadow: none;
  border-left: 2px solid rgb(115, 115, 115);
  border-right: 2px solid rgb(115, 115, 115);
}

.group-shelf-wrapper:last-child {
  padding-bottom: 2.7rem;
  border-radius: 0 0 9px 9px;
  border-bottom: 2px solid black;
}

.group-shelf-title-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.6rem;
  gap: 0.9rem;
}

.group-shelf-title {
  font-family: 'EBSHunminjeongeumSaeronL', serif;
  font-size: 1.25rem;
  font-weight: 700;
  color: #000000;
  text-decoration: none;
  transition: color 0.2s, transform 0.2s;
}

.group-shelf-title:hover {
  transform: scale(1.05);
  color: #333;
  text-decoration: none;
}

.roman-numeral {
  font-weight: 600;
  margin-right: 0.45rem;
  color: #000000;
}

.group-shelf-title-placeholder {
  font-family: 'SCDream5', serif;
  font-size: 0.9rem;
  font-weight: 500;
  color: #4f4f4f;
}

.edit-group-btn {
  padding: 0.25rem 0.7rem;
  font-size: 0.8rem;
  background-color: transparent;
  color: #333;
  border-radius: 7px;
  border: 1px solid #000000;
  transition: transform 0.2s ease;
}

.edit-group-btn:hover {
  transform: scale(1.05);
}

.group-bookshelf-inner {
  padding: 1.8rem 0.9rem 0.7rem 0.7rem;
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
  min-width: 108px;
  min-height: 225px;
  border-radius: 5px;
  align-items: center;
}

.no-groups-message {
  text-align: center;
  color: #888;
  padding: 1.8rem;
  border: 2px dashed #eee;
  border-radius: 7px;
  margin-top: 1.8rem;
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
  padding: 1.8rem 2.25rem;
  border-radius: 11px;
  width: 90%;
  max-width: 450px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.3);
  position: relative;
  border: 1px solid #E0E0E0;
  text-align: center;
}

.modal-content.modal-sm {
  max-width: 360px;
}

.close-button {
  color: #594C40;
  position: absolute;
  top: 0.9rem;
  right: 0.9rem;
  cursor: pointer;
  background: none;
  border: none;
  font-size: 1.35rem;
  line-height: 1;
  padding: 0.45rem;
}

.close-button:hover {
  color: #261E17;
}

.modal-title {
  font-family: 'SCDream4', serif;
  font-size: 1.6rem;
  font-weight: 600;
  margin-bottom: 0.65rem;
  text-align: center;
}

.modal-description,
.modal-body {
  margin-bottom: 1.35rem;
  opacity: 0.8;
  text-align: center;
  line-height: 1.6;
}

.book-selection-list {
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
  max-height: 270px;
  overflow-y: auto;
  margin-bottom: 1.35rem;
  border: 1px solid #E0E0E0;
  border-radius: 7px;
  padding: 0.65rem;
}

.book-selection-item {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  padding: 0.45rem;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.book-selection-item:hover {
  background-color: #f5f5f5;
}

.book-selection-item input[type="radio"] {
  margin-right: 0.45rem;
}

.book-cover-thumbnail {
  width: 36px;
  height: 54px;
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
  margin-bottom: 1.1rem;
  text-align: left;
}

.form-label {
  display: block;
  font-weight: 600;
  margin-bottom: 0.45rem;
  font-size: 0.9rem;
}

.form-control {
  background-color: #F5F5F3;
  border: 1px solid #E0E0E0;
  border-radius: 7px;
  padding: 0.65rem 0.9rem;
  width: 100%;
  box-sizing: border-box;
  font-size: 0.9rem;
}

.modal-action-btn {
  width: auto;
  padding: 0.7rem 1.8rem;
  font-size: 0.95rem;
  margin-top: 0.45rem;
  background-color: #5b673b;
  color: white;
  border: none !important;
}

.modal-action-btn.btn-primary::before {
  display: none;
}

.modal-action-btn.btn-primary:hover {
  background-color: #4a552a;
  border-color: transparent !important;
  color: white !important;
}


.published-sticker-shelf {
  position: absolute;
  bottom: 4px;
  right: 4px;
  width: 54px;
  height: 54px;
  z-index: 10;
  transform: rotate(15deg);
}

/* --- 그룹 바로가기 --- */
.group-shortcut-wrapper {
  position: relative;
}

.group-shortcut-btn {
  background-color: transparent;
  color: #333;
  border: 1px solid #000000;
  padding: 0.35rem 0.9rem;
  border-radius: 7px;
  transition: transform 0.2s ease;
}

.group-shortcut-btn:hover {
  transform: scale(1.05);
}

.group-shortcut-list {
  position: absolute;
  top: 100%;
  right: 0;
  background-color: white;
  border: 1px solid #e0e0e0;
  border-radius: 7px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;
  width: 180px;
  max-height: 270px;
  overflow-y: auto;
  padding: 0.45rem 0;
  margin-top: 4px;
}

.group-shortcut-item {
  display: flex;
  align-items: center;
  padding: 0.65rem 0.9rem;
  color: #1b1b1b;
  text-decoration: none;
  transition: background-color 0.2s;
}

.group-shortcut-item:hover {
  background-color: #f1f1f1;
}

.drag-handle-shortcut {
  cursor: grab;
  margin-right: 0.65rem;
  color: #a0a0a0;
}

.group-name-link {
  color: inherit;
  text-decoration: none;
  flex-grow: 1;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.community-sash {
  position: absolute;
  top: 9px;
  left: -31px;
  background-color: #D4A373;
  color: white;
  padding: 4px 27px;
  font-size: 11px;
  font-weight: bold;
  transform: rotate(-45deg);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  z-index: 11;
}

@media (max-width: 1200px) {
  .my-library-page {
    flex-direction: column;
    align-items: stretch;
  }

  .representative-book-section {
    width: 100%;
    position: relative;
    top: 0;
    height: auto;
    padding: 1.8rem 0.9rem;

  }

  .book-shelves-section {
    padding: 1.35rem;
  }

  .representative-title,
  .shelves-title {
    font-size: 2.25rem;
  }
}

@media (max-height: 800px) {
  .representative-book-section {
    padding: 0.9rem;
  }

  .rep-book-display-area {
    margin: 0.9rem 0;
    min-height: 252px;
  }
}
</style>
