<template>
  <div class="bookstore-page">
    <section class="title-container">
      <h2 class="section-title"><span class="special-font">아 북 이</span> 작가들의 이야기</h2>
      <p class="section-subtitle1">독자들이 가장 많이 찾고,</p>
      <p class="section-subtitle2">사랑하는 책들을 만나 보세요.</p>
      <!-- <p class="section-subtitle3"></p> -->

    </section>

    <section class="carousel-section">
      <div class="perspective-carousel-container">
        <div class="perspective-carousel" :style="carouselStyle" @mousedown.prevent="onMouseDown"
          @touchstart.prevent="onTouchStart">
          <div v-for="(book, index) in topBooks" :key="book.communityBookId" class="carousel-item-3d"
            :style="{ transform: get3DTransform(index) }" @click="goToBookDetail(book.communityBookId)">
            <div class="book-model">
              <div class="book-face book-cover"
                :style="{ backgroundImage: `url(${book.coverImageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
                <div class="vertical-line-front-bright-effect"></div>
                <div class="title-box">
                  <h1>{{ book.title }}</h1>
                  <p class="author-in-box">{{ book.authorNickname }}</p>
                </div>
              </div>
              <div class="book-face book-spine"
                :style="{ backgroundImage: `url(${book.coverImageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
              </div>
              <div class="book-face book-side-edge"></div>
              <div class="book-face book-back-cover"
                :style="{ backgroundImage: `url(${book.coverImageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
                <div class="barcode-placeholder"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <button @click="prevBook" class="carousel-control-btn prev-btn"><i
          class="bi bi-chevron-compact-left"></i></button>
      <button @click="nextBook" class="carousel-control-btn next-btn"><i
          class="bi bi-chevron-compact-right"></i></button>
    </section>

    <section class="search-section">
      <div class="search-input-wrapper">
        <input type="text" v-model="searchTerm" class="form-control search-input"
          placeholder="책 제목, 작가, 장르, #태그 로 검색...">
        <i class="bi bi-search search-icon"></i>
      </div>
    </section>

    <div class="main-content-area">
      <aside class="left-sidebar">
        <div class="sidebar-block">
          <h4 class="sidebar-title">정렬</h4>
          <div class="sort-options-wrapper">
            <label class="radio-button" v-for="option in sortOptions" :key="option.value">
              <input type="radio" name="sort-option" :value="option.value" v-model="currentSortOption">
              <span>{{ option.text }}</span>
            </label>
          </div>
        </div>
        <div class="sidebar-block">
          <h4 class="sidebar-title">장르</h4>
          <div class="genre-buttons-container">
            <button v-for="genre in genres" :key="genre.id" @click="toggleGenre(genre)"
              :class="['genre-button', { active: activeGenreId === genre.id }]">
              {{ genre.name }}
            </button>
          </div>
        </div>
      </aside>

      <main class="book-list-main">
        <div v-if="paginatedBooks.length === 0" class="no-books-message">
          찾으시는 책이 없습니다.
        </div>
        <div v-else class="book-list-container">
          <div v-for="book in paginatedBooks" :key="book.communityBookId" class="book-list-item" @click="goToBookDetail(book.communityBookId)">
            <div class="book-cover-image"
              :style="{ backgroundImage: `url(${book.coverImageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
              <div class="list-title-box">
                <p class="list-title-box-title">{{ book.title }}</p>
                <p class="list-title-box-author">{{ book.authorNickname }}</p>
              </div>
            </div>
            <div class="book-details">
              <h5 class="book-title">{{ book.title }}</h5>
              <div class="author-info">
                <span class="book-author">{{ book.authorNickname }}</span>
                <span class="separator">·</span>
                <span class="publish-date">{{ formatDate(book.createdAt) }}</span>
              </div>
              <p class="book-summary">{{ book.summary }}</p>
              <div class="book-meta-tags">
                <span v-if="book.categoryName" class="meta-tag genre-tag">{{ book.categoryName }}</span>
                <span v-for="tag in book.tags" :key="tag.tagId" class="meta-tag user-tag">#{{ tag.tagName }}</span>
              </div>
              <div class="book-stats">
                <span><i class="bi bi-eye"></i> {{ book.viewCount }}</span>
                <span><i class="bi bi-heart"></i> {{ book.likeCount }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="pagination-container">
          <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1" class="page-btn">
            <i class="bi bi-chevron-left"></i>
          </button>
          <template v-for="(page, index) in pagination" :key="index">
            <span v-if="page === '...'" class="ellipsis">...</span>
            <button v-else @click="changePage(page as number)" :class="['page-btn', { active: currentPage === page }]">
              {{ page }}
            </button>
          </template>
          <button @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages" class="page-btn">
            <i class="bi bi-chevron-right"></i>
          </button>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { communityService, type CommunityBook, type SearchParams } from '@/services/communityService';

// --- Router ---
const router = useRouter();

// --- Reactive State ---
const allBooks = ref<CommunityBook[]>([]);
const topBooks = ref<CommunityBook[]>([]);
const searchTerm = ref('');
const currentSortOption = ref('recent'); // API 기본값
const genres = [
    { id: 1, name: '자서전' }, { id: 2, name: '일기' }, { id: 3, name: '소설/시' },
    { id: 4, name: '에세이' }, { id: 5, name: '자기계발' }, { id: 6, name: '역사' },
    { id: 7, name: '경제/경영' }, { id: 8, name: '사회/정치' }, { id: 9, name: '청소년' },
    { id: 10, name: '어린이/동화' }, { id: 11, name: '문화/예술' }, { id: 12, name: '종교' },
    { id: 13, name: '여행' }, { id: 14, name: '스포츠' }
];
const activeGenreId = ref<number | null>(null);
const currentPage = ref(1);
const totalPages = ref(0);
const itemsPerPage = 10;
const isLoading = ref(false);
const error = ref<string | null>(null);

const sortOptions = [
  { value: 'recent', text: '최신순' },
  { value: 'liked', text: '인기순' },
  { value: 'popular', text: '조회순' },
];

// --- Carousel State ---
const carouselRotation = ref(0);
const isDragging = ref(false);
const startX = ref(0);
const dragStartRotation = ref(0);
let autoSlideInterval: number;

// --- API-driven Data Fetching ---
const fetchBooks = async () => {
  isLoading.value = true;
  error.value = null;
  try {
    const params: SearchParams = {
      page: currentPage.value - 1,
      size: itemsPerPage,
      sortBy: currentSortOption.value,
    };

    if (searchTerm.value) {
        if (searchTerm.value.startsWith('#')) {
            params.tags = [searchTerm.value.substring(1)];
        } else {
            params.title = searchTerm.value;
        }
    }

    if (activeGenreId.value) {
      params.categoryId = activeGenreId.value;
    }

    const response = await communityService.searchCommunityBooks(params);
    allBooks.value = response.content;
    totalPages.value = response.totalPages;

    // 인기순으로 topBooks 업데이트 (최초 한 번만)
    if (topBooks.value.length === 0 && response.content.length > 0) {
        const topResponse = await communityService.searchCommunityBooks({ page: 0, size: 10, sortBy: 'likes' });
        topBooks.value = topResponse.content;
    }

  } catch (e) {
    console.error('Failed to fetch books:', e);
    error.value = '책을 불러오는 데 실패했습니다.';
  } finally {
    isLoading.value = false;
  }
};

// --- Computed Properties ---
const paginatedBooks = computed(() => allBooks.value);

const pagination = computed(() => {
  const pages = [];
  const sideCount = 1;
  const total = totalPages.value;
  const current = currentPage.value;

  if (total <= 5) {
    for (let i = 1; i <= total; i++) pages.push(i);
    return pages;
  }

  pages.push(1);
  if (current > sideCount + 2) pages.push('...');
  for (let i = Math.max(2, current - sideCount); i <= Math.min(total - 1, current + sideCount); i++) {
    pages.push(i);
  }
  if (current < total - sideCount - 1) pages.push('...');
  pages.push(total);

  return pages;
});

const carouselStyle = computed(() => ({
  transition: isDragging.value ? 'none' : 'transform 1s ease-in-out',
  transform: `rotateY(${carouselRotation.value}deg)`,
}));

// --- Watchers for Reactivity ---
watch([searchTerm, currentSortOption, activeGenreId], () => {
  if (currentPage.value !== 1) {
    currentPage.value = 1;
  } else {
    fetchBooks();
  }
}, { deep: true });

watch(currentPage, fetchBooks);


// --- Navigation ---
const goToBookDetail = (bookId: number) => {
  router.push({ name: 'BookstoreBookDetail', params: { id: bookId } });
};

// --- Carousel Logic ---
const get3DTransform = (index: number) => {
  const itemAngle = index * 36;
  return `rotateY(${itemAngle}deg) translateZ(450px)`;
};

const changeBook = (direction: number) => {
  const anglePerItem = 36;
  carouselRotation.value += direction * anglePerItem;
};

const prevBook = () => changeBook(1);
const nextBook = () => changeBook(-1);

const snapToNearestBook = () => {
  const anglePerItem = 36;
  carouselRotation.value = Math.round(carouselRotation.value / anglePerItem) * anglePerItem;
};

const onMouseMove = (event: MouseEvent) => {
  if (!isDragging.value) return;
  const deltaX = event.clientX - startX.value;
  carouselRotation.value = dragStartRotation.value + (deltaX * 0.3);
};

const onMouseUp = () => {
  isDragging.value = false;
  window.removeEventListener('mousemove', onMouseMove);
  window.removeEventListener('mouseup', onMouseUp);
  snapToNearestBook();
};

const onMouseDown = (event: MouseEvent) => {
  isDragging.value = true;
  startX.value = event.clientX;
  dragStartRotation.value = carouselRotation.value;
  window.addEventListener('mousemove', onMouseMove);
  window.addEventListener('mouseup', onMouseUp);
};

const onTouchMove = (event: TouchEvent) => {
  if (!isDragging.value || event.touches.length !== 1) return;
  const deltaX = event.touches[0].clientX - startX.value;
  carouselRotation.value = dragStartRotation.value + (deltaX * 0.3);
};

const onTouchEnd = () => {
  isDragging.value = false;
  window.removeEventListener('touchmove', onTouchMove);
  window.removeEventListener('touchend', onTouchEnd);
  snapToNearestBook();
};

const onTouchStart = (event: TouchEvent) => {
  if (event.touches.length !== 1) return;
  isDragging.value = true;
  startX.value = event.touches[0].clientX;
  dragStartRotation.value = carouselRotation.value;
  window.addEventListener('touchmove', onTouchMove);
  window.addEventListener('touchend', onTouchEnd);
};

// --- Other Functions ---
const changePage = (page: number) => {
  if (page > 0 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).format(date).replace(/\.$/, '');
};

function toggleGenre(genre: { id: number, name: string }) {
  if (activeGenreId.value === genre.id) {
    activeGenreId.value = null;
  } else {
    activeGenreId.value = genre.id;
  }
}

// --- Lifecycle Hooks ---
onMounted(() => {
  fetchBooks();
  autoSlideInterval = window.setInterval(nextBook, 3500);
});

onUnmounted(() => {
  clearInterval(autoSlideInterval);
  window.removeEventListener('mousemove', onMouseMove);
  window.removeEventListener('mouseup', onMouseUp);
  window.removeEventListener('touchmove', onTouchMove);
  window.removeEventListener('touchend', onTouchEnd);
});
</script>

<style>
/* ... 전역 스타일은 이전과 동일 ... */
:root {
  --book-width: 210px;
  --book-height: 310px;
  --book-depth: 20px;
}

.bookstore-page .book-model {
  width: 100%;
  height: 100%;
  transform-style: preserve-3d;
}

.bookstore-page .book-face {
  position: absolute;
  box-sizing: border-box;
  background-color: #e7e2d8;
  backface-visibility: hidden;
}

.bookstore-page .book-cover {
  width: var(--book-width);
  height: var(--book-height);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 2px 5px 5px 2px;
  box-shadow: inset -2px 0 5px rgba(0, 0, 0, 0.15);
  transform: translateZ(calc(var(--book-depth) / 2));
  background-size: cover;
  background-position: center;
}

.bookstore-page .vertical-line-front-bright-effect {
  position: absolute;
  left: 8px;
  top: 0;
  bottom: 0;
  width: 8px;
  background: linear-gradient(to right, rgba(255, 255, 255, 0.441), transparent);
}

.bookstore-page .book-back-cover {
  width: var(--book-width);
  height: var(--book-height);
  background-color: #e7e2d8;
  border-radius: 2px 2px 2px 2px;
  transform: rotateY(180deg) translateZ(calc(var(--book-depth) / 2));
  background-size: cover;
  background-position: center;
  filter: brightness(0.4) blur(3px);
  transform: rotateY(180deg) translateZ(calc(var(--book-depth) / 2)) scaleX(-1);
}

.bookstore-page .book-back-cover .barcode-placeholder {
  position: absolute;
  bottom: 15px;
  left: 20px;
  width: 60px;
  height: 30px;
  background-color: white;
  opacity: 0.9;
}

.bookstore-page .book-spine {
  width: var(--book-depth);
  height: var(--book-height);
  background-color: #e7e2d8;
  transform: rotateY(-90deg) translateZ(calc(var(--book-width) / 2 - 96px));
  background-size: cover;
  background-position: center;
  filter: brightness(0.7) blur(0.5px);
}

.bookstore-page .book-side-edge {
  width: var(--book-depth);
  height: var(--book-height);
  background-color: #a6916f;
  background-image: repeating-linear-gradient(to right,
      #ffffff27,
      #dfdedd 1px,
      #bbb 1px,
      #999590c8 3px);
  transform: rotateY(90deg) translateZ(calc(var(--book-width) / 2 + 90px));
}
</style>


<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&family=Pretendard:wght@400;500;700&display=swap');
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

.bookstore-page {
  padding: 2rem 2rem 2rem 2rem;
  background-color: var(--background-color);
  color: var(--primary-text-color);
  min-height: calc(100vh - 56px);
  font-family: 'SCDream4', sans-serif;
}

.title-container {
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  font-family: 'SCDream3', serif;
  font-size: 4rem;
  font-weight: 700;
  color: var(--primary-text-color);
  margin-bottom: -0.5rem;
  margin-left: 3rem;
  margin-right: auto;
}

.section-subtitle1 {
  font-family: 'SCDream4', serif;
  font-size: 3rem;
  color: rgba(116, 125, 76, 0.9);
  margin-left: 3.5rem;
  margin-right: auto;
  margin-bottom: -0.5rem;
}

.section-subtitle2 {
  font-family: 'SCDream4', serif;
  font-size: 3rem;
  color: rgba(141, 153, 109, 0.7);
  margin-left: 3.5rem;
  margin-right: auto;
  margin-bottom: 2rem;
}

.special-font {
  font-size: 5rem;
  font-family: 'EBSHunminjeongeumSaeronL', serif;
  position: relative;
  top: -0.1em;
  /* 살짝 위로 올리는 효과 */
}

/* .section-subtitle3 {
  font-family: 'SCDream4', serif;
  font-size: 3rem;
  color: rgba(147, 161, 89, 0.4);
  margin-left: 3.5rem;
  margin-right: auto;
  margin-bottom: 5rem;
} */

.carousel-section {
  position: relative;
  height: 450px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2rem;
}

.perspective-carousel-container {
  perspective: 2500px;
  width: var(--book-width);
  height: var(--book-height);
  position: relative;
  transform-style: preserve-3d;
}

.perspective-carousel-container::after {
  content: '';
  position: absolute;
  width: 100%;
  height: 10px;
  bottom: -40px;
  left: 0;
  background: rgba(0, 0, 0, 1);
  border-radius: 50%;
  filter: blur(25px);
  transform: scale(2);
  z-index: -1;
}

.perspective-carousel {
  transform-style: preserve-3d;
  width: 100%;
  height: 100%;
  position: absolute;
  cursor: grab;
}

.perspective-carousel:active {
  cursor: grabbing;
}

.carousel-item-3d {
  position: absolute;
  width: var(--book-width);
  height: var(--book-height);
  transform-style: preserve-3d;
  cursor: pointer;
  transition: transform 0.6s;
}

.carousel-control-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
  color: #5b673b;
  ;
  width: 50px;
  height: 50px;
  font-size: 2rem;
  transition: all 0.2s;

}

.carousel-control-btn:hover {
  color: #000000;
}

.prev-btn {
  left: calc(35% - 300px);
}

.next-btn {
  right: calc(35% - 300px);
}

.title-box {
  width: 63%;
  height: 58%;
  background-color: rgba(255, 255, 255, 0.97);
  padding: 15px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  text-align: left;
  color: #000000;
}

.title-box h1 {
  font-family: 'ChosunCentennial', serif;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.4;
  margin: 0;
}

.author-in-box {
  font-size: 10px;
  color: #333;
  font-weight: 600;
  margin: 0;
  font-family: 'NanumSquareR', serif;

}

.search-section {
  max-width: 1200px;
  margin: 0 auto 2rem auto;
  padding: 0 1rem;
}

.search-input-wrapper {
  position: relative;
}

.search-input {
  width: 100%;
  padding: 1rem 2rem 1rem 3.5rem;
  border: 3px solid #657143;
  border-radius: 30px;
  font-size: 1.2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  font-family: 'EBSHunminjeongeumSaeronL', serif;
}

.search-icon {
  position: absolute;
  left: 1.5rem;
  top: 50%;
  transform: translateY(-50%);
  color: #6F7D48;
  font-size: 1.2rem;
}

.main-content-area {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 3rem;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.left-sidebar {
  width: 100%;
}

.sidebar-block {
  margin-bottom: 2.5rem;
}

.sidebar-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1.5rem;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #5b673b;

}

.sort-options-wrapper,
.genre-buttons-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 1rem;
}

.radio-button span,
.genre-button {
  width: 100%;
  text-align: left;
  padding: 0.5rem 0;
  border: none;
  background: none;
  font-size: 1rem;
  color: #555;
  cursor: pointer;
  transition: color 0.2s;
  font-family: 'SCDream4', sans-serif;
}

.radio-button input[type="radio"] {
  display: none;
}

.radio-button input[type="radio"]:checked+span,
.genre-button.active {
  font-weight: 700;
  color: #000;
}

.radio-button span:hover,
.genre-button:hover {
  color: #000;
}

.book-list-main {
  width: 100%;
}

.book-list-container {
  display: flex;
  flex-direction: column;
}

.book-list-item {
  position: relative;
  display: flex;
  gap: 1.5rem;
  padding: 1.5rem;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s ease-in-out;
}

.book-list-item:hover {
  background-color: rgba(138, 154, 91, 0.1);
  transform: scale(1.02);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.07);
  z-index: 10;
}

.book-cover-image {
  width: 120px;
  height: 180px;
  object-fit: cover;
  border-radius: 4px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
  /* [추가] 캐러셀과 동일하게 flex 속성 추가 */
  display: flex;
  align-items: center;
  justify-content: center;
  background-size: cover;
  background-position: center;
  position: relative;
  /* 자식 요소 position absolute를 위해 추가 */
}

/* [추가] 목록 내 표지 위의 제목/작가 박스 스타일 */
.list-title-box {
  width: 80%;
  height: 70%;
  background-color: rgba(255, 255, 255, 0.95);
  padding: 10px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  text-align: left;
  color: #333;
  border-radius: 2px;
}

.list-title-box-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.4;
  margin: 0;
  /* 말줄임 처리 */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  line-clamp: 3;
  /* 표준 속성으로 변경 */
  -webkit-box-orient: vertical;
}

.list-title-box-author {
  font-size: 10px;
  font-weight: 500;
  margin: 0;
  color: #555;
}


.book-details {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.book-title {
  font-family: 'Noto Serif KR', serif;
  font-weight: 700;
  font-size: 1.25rem;
  margin-bottom: 0.25rem;
  color: #26250F;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #888;
  margin-bottom: 1rem;
  font-size: 0.9rem;
}

.separator {
  color: #ccc;
}

.book-summary {
  font-size: 0.9rem;
  line-height: 1.6;
  color: #555;
  margin-bottom: 1rem;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  line-clamp: 3;
  /* 표준 속성으로 변경 */
  -webkit-box-orient: vertical;
}

.book-meta-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: auto;
  padding-bottom: 1rem;
}

.meta-tag {
  color: #5b673b;
  padding: 0.25rem 0.75rem;
  border-radius: 30px;
  font-size: 0.8rem;
  font-weight: 500;
  background-color: rgba(138, 154, 91, 0.4);
}
.meta-tag.genre-tag {
  background-color: rgba(169, 131, 103, 0.4);
  color: #6D4C41;
}

.book-stats {
  display: flex;
  gap: 1rem;
  font-size: 0.85rem;
  color: #aaa;
  padding-top: 1rem;
}

.book-stats i {
  margin-right: 0.2rem;
}

.no-books-message {
  text-align: center;
  padding: 4rem;
  font-size: 1.2rem;
  color: #ccc;
  border: 1px dashed #eee;
  border-radius: 8px;
}

.filter-section,
.book-list-section {
  display: none;
}

.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 2.5rem;
  gap: 0.5rem;
}

.page-btn {
  background-color: #fff;
  border: 1px solid #ddd;
  color: #555;
  padding: 0.5rem 0.5rem;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover {
  background-color: #f5f5f5;
  border-color: #aeaeae;
}

.page-btn.active {
  background-color: #6F7D48;
  color: white;
  border-color: #5b673b;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.ellipsis {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.5rem 1rem;
  color: #999;
}
</style>
