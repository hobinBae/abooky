<template>
  <div class="bookstore-page">
    <h2 class="section-title">지금 가장 인기있는 책 TOP 10</h2>
    <p class="section-subtitle">독자들이 가장 많이 찾고 사랑하는 책들을 만나보세요.</p>
    <section class="carousel-section">
      <div class="perspective-carousel-container">
        <div class="perspective-carousel" :style="carouselStyle" @mousedown.prevent="onMouseDown"
          @touchstart.prevent="onTouchStart">
          
          <div v-for="(book, index) in topBooks" :key="book.id" class="carousel-item-3d"
               :style="{ transform: get3DTransform(index) }"
               @click="goToBookDetail(book.id)">
            <div class="book-model">
                <div class="book-face book-cover">
                    <div class="vertical-line-face"></div>{{ book.title }}
                </div>
                <div class="book-face book-spine"></div>
                <div class="book-face book-side-edge"></div>
                <div class="book-face book-back-cover"><div class="vertical-line-back"></div></div>
            </div>
          </div>

        </div>
      </div>
      <button @click="prevBook" class="carousel-control-btn prev-btn"><i class="bi bi-chevron-left"></i></button>
      <button @click="nextBook" class="carousel-control-btn next-btn"><i class="bi bi-chevron-right"></i></button>
    </section>

    <section class="filter-section">
      <div class="search-input-wrapper">
        <input type="text" v-model="searchTerm" class="form-control search-input" placeholder="책 제목, 작가, 내용으로 검색...">
        <i class="bi bi-search search-icon"></i>
      </div>
      <div class="sort-options-wrapper">
        <label class="radio-button" v-for="option in sortOptions" :key="option.value">
          <input type="radio" name="sort-option" :value="option.value" v-model="currentSortOption">
          <span>{{ option.text }}</span>
        </label>
      </div>
      <div class="keyword-buttons-container">
        <button v-for="keyword in keywords" :key="keyword" @click="toggleKeyword(keyword)"
          :class="['keyword-button', { active: activeKeywords.has(keyword) }]">
          #{{ keyword }}
        </button>
      </div>
    </section>

    <section class="book-list-section">
      <div class="row g-4">
        <div v-if="filteredBooks.length === 0" class="col-12">
          <div class="no-books-message">
            해당하는 책이 없습니다.
          </div>
        </div>
        <div v-for="book in filteredBooks" :key="book.id" class="col-12">
          <div class="book-card" @click="goToBookDetail(book.id)">
            <div class="book-cover-placeholder">표지</div>
            <div class="book-details">
              <h5 class="book-title">{{ book.title }}</h5>
              <h6 class="book-author">{{ book.authorName }}</h6>
              <p class="book-summary">{{ book.summary }}</p>
              <div class="book-stats">
                <span><i class="bi bi-eye"></i> {{ book.views }}</span>
                <span><i class="bi bi-heart-fill"></i> {{ book.likes }}</span>
              </div>
              <button class="btn btn-sm like-button" @click.stop="toggleLike(book)"
                :class="{ liked: isLiked(book.id) }">
                <i class="bi bi-heart"></i> 좋아요
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';

// --- Interfaces & Types ---
interface Book {
  id: string;
  title: string;
  authorId: string;
  authorName?: string;
  summary?: string;
  keywords?: string[];
  views?: number;
  likes?: number;
  createdAt: Date;
}

type SortOption = 'latest' | 'popular' | 'views';

// --- Router ---
const router = useRouter();

// --- Dummy Data ---
const DUMMY_BOOKS: Book[] = [
  { id: 'b1', title: '별 헤는 밤', authorId: 'author1', authorName: '윤동주', views: 1200, likes: 250, summary: '어두운 밤하늘 아래 별들을 헤아리며 고향과 가족을 그리워하는 시인의 마음을 담은 아름다운 시집.', keywords: ['자서전', '감성', '시'], createdAt: new Date('2023-01-15') },
  { id: 'b2', title: '어린 왕자', authorId: 'author2', authorName: '생텍쥐페리', views: 3500, likes: 780, summary: '사막에 불시착한 조종사가 만난 어린 왕자와의 이야기를 통해 삶의 진정한 의미를 탐구하는 철학 동화.', keywords: ['여행', '성장', '철학'], createdAt: new Date('2022-11-01') },
  { id: 'b3', title: '가족의 발견', authorId: 'author3', authorName: '김작가', views: 800, likes: 120, summary: '현대 사회에서 가족의 의미를 되새기고, 다양한 가족 형태의 아름다움을 조명하는 에세이.', keywords: ['가족', '에세이', '사회'], createdAt: new Date('2024-03-20') },
  { id: 'b4', title: '취미로 시작하는 코딩', authorId: 'author4', authorName: '이개발', views: 1500, likes: 300, summary: '코딩을 처음 접하는 사람들을 위한 쉽고 재미있는 입문서. 다양한 프로젝트를 통해 코딩의 즐거움을 알려준다.', keywords: ['취미', 'IT', '자기계발'], createdAt: new Date('2023-09-10') },
  { id: 'b5', title: '사랑의 온도', authorId: 'author5', authorName: '박사랑', views: 2000, likes: 450, summary: '엇갈린 사랑과 인연 속에서 진정한 사랑의 의미를 찾아가는 연인들의 이야기. 감성적인 문체가 돋보인다.', keywords: ['연애', '로맨스', '감성'], createdAt: new Date('2024-01-05') },
  { id: 'b6', title: '스포츠 심리학 개론', authorId: 'author6', authorName: '최건강', views: 950, likes: 180, summary: '운동선수들의 심리 상태와 경기력 향상을 위한 심리학적 접근을 다룬 전문 서적.', keywords: ['스포츠', '심리학', '건강'], createdAt: new Date('2023-05-22') },
  { id: 'b7', title: '드래곤의 유산', authorId: 'author7', authorName: '김판타', views: 2500, likes: 600, summary: '고대 드래곤의 힘을 이어받은 주인공이 세상을 구하기 위해 모험을 떠나는 장대한 판타지 소설.', keywords: ['판타지', '모험', '영웅'], createdAt: new Date('2022-08-01') },
  { id: 'b8', title: '우주 탐사대의 기록', authorId: 'author8', authorName: '이공상', views: 1800, likes: 380, summary: '미지의 행성을 탐사하는 우주선 승무원들의 생존과 발견을 다룬 SF 소설. 과학적 상상력이 돋보인다.', keywords: ['공상과학', '우주', '미래'], createdAt: new Date('2024-02-28') },
  { id: 'b9', title: '조선 왕조 실록 이야기', authorId: 'author9', authorName: '정역사', views: 1100, likes: 200, summary: '조선 왕조 500년 역사를 쉽고 재미있게 풀어낸 역사 교양서. 흥미로운 에피소드와 인물 중심의 서술.', keywords: ['역사', '교양', '한국사'], createdAt: new Date('2023-07-07') },
  { id: 'b10', title: '나는 오늘부터 성장한다', authorId: 'author10', authorName: '강성장', views: 2200, likes: 550, summary: '작은 습관의 변화가 삶을 어떻게 바꾸는지 보여주는 자기계발서. 긍정적인 메시지와 실천적인 조언.', keywords: ['자기계발', '성장', '동기부여'], createdAt: new Date('2024-04-12') },
];

// --- Reactive State ---
const allBooks = ref<Book[]>(DUMMY_BOOKS);
const searchTerm = ref('');
const currentSortOption = ref<SortOption>('latest');
const activeKeywords = ref<Set<string>>(new Set());
const likedBookIds = ref<Set<string>>(new Set(['b1', 'b3']));
const keywords = ['자서전', '여행', '가족', '취미', '연애', '스포츠', '판타지', '공상과학', '역사', '자기계발'];
const sortOptions = [
  { value: 'latest', text: '최신순' },
  { value: 'popular', text: '인기순' },
  { value: 'views', text: '조회순' },
];

// --- Computed Properties ---
const topBooks = computed(() => {
  return [...allBooks.value].sort((a, b) => (b.likes || 0) - (a.likes || 0)).slice(0, 10);
});

const filteredBooks = computed(() => {
  const books = allBooks.value.filter(book => {
    const search = searchTerm.value.toLowerCase();
    const matchesSearch = book.title.toLowerCase().includes(search) ||
      (book.authorName || '').toLowerCase().includes(search) ||
      (book.summary || '').toLowerCase().includes(search);

    const matchesKeywords = activeKeywords.value.size === 0 ||
      (book.keywords && Array.from(activeKeywords.value).every(kw => book.keywords!.includes(kw)));

    return matchesSearch && matchesKeywords;
  });

  return books.sort((a, b) => {
    switch (currentSortOption.value) {
      case 'popular': return (b.likes || 0) - (a.likes || 0);
      case 'views': return (b.views || 0) - (a.views || 0);
      case 'latest':
      default:
        return b.createdAt.getTime() - a.createdAt.getTime();
    }
  });
});

// --- Navigation ---
const goToBookDetail = (bookId: string) => {
  router.push({ name: 'book-detail', params: { id: bookId } });
};

// --- Carousel Logic ---
const carouselRotation = ref(0);
const isDragging = ref(false);
const startX = ref(0);
const dragStartRotation = ref(0);

const carouselStyle = computed(() => ({
  transition: isDragging.value ? 'none' : 'transform 0.6s cubic-bezier(0.25, 1, 0.5, 1)',
  transform: `rotateY(${carouselRotation.value}deg)`,
}));

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

const onMouseDown = (event: MouseEvent) => {
  isDragging.value = true;
  startX.value = event.clientX;
  dragStartRotation.value = carouselRotation.value;
  window.addEventListener('mousemove', onMouseMove);
  window.addEventListener('mouseup', onMouseUp);
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

const onTouchStart = (event: TouchEvent) => {
  if (event.touches.length !== 1) return;
  isDragging.value = true;
  startX.value = event.touches[0].clientX;
  dragStartRotation.value = carouselRotation.value;
  window.addEventListener('touchmove', onTouchMove);
  window.addEventListener('touchend', onTouchEnd);
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

const snapToNearestBook = () => {
  const anglePerItem = 36;
  carouselRotation.value = Math.round(carouselRotation.value / anglePerItem) * anglePerItem;
};

onUnmounted(() => {
  window.removeEventListener('mousemove', onMouseMove);
  window.removeEventListener('mouseup', onMouseUp);
  window.removeEventListener('touchmove', onTouchMove);
  window.removeEventListener('touchend', onTouchEnd);
});

// --- General Functions ---
function toggleKeyword(keyword: string) {
  if (activeKeywords.value.has(keyword)) {
    activeKeywords.value.delete(keyword);
  } else {
    activeKeywords.value.add(keyword);
  }
}

function isLiked(bookId: string) {
  return likedBookIds.value.has(bookId);
}

function toggleLike(book: Book) {
  if (isLiked(book.id)) {
    likedBookIds.value.delete(book.id);
    book.likes = (book.likes || 1) - 1;
  } else {
    likedBookIds.value.add(book.id);
    book.likes = (book.likes || 0) + 1;
  }
}
</script>

<!-- [수정됨] 3D 캐러셀 스타일만 scoped가 없는 별도의 style 태그로 분리 -->
<style>
/* CSS 변수로 책 크기 및 두께 정의 */
:root {
  --book-width: 200px;
  --book-height: 300px;
  --book-depth: 30px;
}

/* 3D Book Model Styling */
.bookstore-page .book-model {
    width: 100%;
    height: 100%;
    transform-style: preserve-3d;
}

.bookstore-page .book-face {
    position: absolute;
    box-sizing: border-box;
    background-color: #8C6A56;
    backface-visibility: hidden;
}

.bookstore-page .book-cover {
    width: var(--book-width);
    height: var(--book-height);
    color: #F2EAD0;
    display: flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    padding: 1.5rem;
    font-family: 'Noto Serif KR', serif;
    font-size: 1.4rem;
    font-weight: 600;
    border-radius: 2px 2px 2px 2px;
    box-shadow: inset -2px 0 5px rgba(0, 0, 0, 0.15);
    transform: translateZ(calc(var(--book-depth) / 2));
}

.bookstore-page .vertical-line-face {
    position: absolute;
    left: 15px;
    width: 3px;
    background-color: #574130;
    height: var(--book-height);
}
.bookstore-page .vertical-line-back {
    position: absolute;
    right: 15px;
    width: 3px;
    background-color: #574130;
    height: var(--book-height);
}
.bookstore-page .book-back-cover {
    width: var(--book-width);
    height: var(--book-height);
    background-color: #8C6A56;
    border-radius: 2px 2px 2px 2px;
    transform: rotateY(180deg) translateZ(calc(var(--book-depth) / 2));
}

.bookstore-page .book-spine {
    width: var(--book-depth);
    height: var(--book-height);
    background-color: #7f604e;
    transform: rotateY(-90deg) translateZ(calc(var(--book-width) / 2 - 86px));
}

.bookstore-page .book-side-edge {
    width: var(--book-depth);
    height: var(--book-height);
    background-color: #a6916f;
    background-image: repeating-linear-gradient(to right,
            #362e23,
            #a6916f 1px,
            #bbb 1px,
            #362e23 4px);
    transform: rotateY(90deg) translateZ(calc(var(--book-width) / 2 + 83px));
}
</style>


<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&family=Pretendard:wght@400;500;700&display=swap');
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

.bookstore-page {
  padding: 80px 2rem 2rem;
  background-color: #F2EAD0;
  color: #403023;
  min-height: calc(100vh - 56px);
  font-family: 'Pretendard', sans-serif;
}

.section-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 2.8rem;
  font-weight: 700;
  color: #26250F;
  margin-bottom: 0.75rem;
  text-align: center;
}

.section-subtitle {
  font-size: 1.25rem;
  color: #403023;
  margin-bottom: 3rem;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.7;
  text-align: center;
}

/* Carousel Section */
.carousel-section {
  position: relative;
  height: 450px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2rem;
}

.perspective-carousel-container {
  perspective: 3000px;
  width: var(--book-width);
  height: var(--book-height);
  position: relative;
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
  background-color: rgba(242, 234, 208, 0.7);
  border: 1px solid #8C6A56;
  color: #403023;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  font-size: 1.5rem;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(5px);
}

.carousel-control-btn:hover {
  background-color: #8C4332;
  border-color: #8C4332;
  color: #F2EAD0;
}

.prev-btn {
  left: 15%;
}

.next-btn {
  right: 15%;
}

/* Filter Section */
.filter-section {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  padding: 2rem;
  margin: 0 auto 3rem auto;
  max-width: 1000px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.06);
  border: 1px solid #8C6A56;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.search-input-wrapper {
  position: relative;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1.5rem 0.75rem 3rem;
  border: 1px solid #8C6A56;
  border-radius: 9999px;
  background-color: #F2EAD0;
  color: #403023;
  font-size: 1rem;
}

.search-input:focus {
  box-shadow: 0 0 0 0.25rem rgba(140, 67, 50, 0.25);
  border-color: #8C4332;
  outline: none;
}

.search-icon {
  position: absolute;
  left: 1.2rem;
  top: 50%;
  transform: translateY(-50%);
  color: #8C6A56;
}

.sort-options-wrapper {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.radio-button input[type="radio"] { display: none; }
.radio-button span {
  display: block;
  padding: 0.5rem 1.2rem;
  border: 1px solid #8C6A56;
  border-radius: 20px;
  background-color: transparent;
  color: #403023;
  transition: all 0.2s;
  cursor: pointer;
}
.radio-button input[type="radio"]:checked+span {
  background-color: #8C4332;
  color: #F2EAD0;
  border-color: #8C4332;
}

.keyword-buttons-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  justify-content: center;
}

.keyword-button {
  padding: 0.5rem 1rem;
  border-radius: 9999px;
  border: 1px solid #8C6A56;
  background-color: transparent;
  color: #403023;
  transition: all 0.2s;
}

.keyword-button.active,
.keyword-button:hover {
  background-color: #8C4332;
  color: #F2EAD0;
  border-color: #8C4332;
}

/* Book List Section */
.book-list-section {
  max-width: 1000px;
  margin: 0 auto;
}

.book-card {
  background: #F2EAD0;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.06);
  display: flex;
  gap: 1.5rem;
  padding: 1.5rem;
  transition: transform 0.3s, box-shadow 0.3s, border-color 0.3s;
  cursor: pointer;
  border: 1px solid #8C6A56;
  margin-bottom: 1.5rem;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(38, 37, 15, 0.1);
  border-color: #8C4332;
}

.book-cover-placeholder {
  flex-shrink: 0;
  width: 120px;
  height: 170px;
  background-color: #8C6A56;
  color: #F2EAD0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  text-align: center;
  padding: 0.5rem;
}

.book-details {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.book-title {
  font-family: 'Noto Serif KR', serif;
  font-weight: 700;
  font-size: 1.3rem;
  margin-bottom: 0.5rem;
  color: #26250F;
}

.book-author {
  color: #403023;
  margin-bottom: 0.75rem;
  font-size: 1rem;
}

.book-summary {
  font-size: 0.95rem;
  flex-grow: 1;
  margin-bottom: 1rem;
  line-height: 1.6;
  color: #403023;
}

.book-stats {
  display: flex;
  gap: 1rem;
  font-size: 0.9rem;
  color: #8C6A56;
  margin-bottom: 1rem;
}

.book-stats i { margin-right: 0.3rem; }

.like-button {
  background-color: transparent;
  border: 1px solid #8C6A56;
  color: #8C4332;
  align-self: flex-start;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.2s;
}

.like-button.liked,
.like-button:hover {
  background-color: #8C4332;
  color: #F2EAD0;
  border-color: #8C4332;
}

.no-books-message {
  text-align: center;
  padding: 3rem;
  font-size: 1.2rem;
  color: #8C6A56;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  border: 1px solid #8C6A56;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .bookstore-page { padding: 60px 1rem 1rem; }
  .section-title { font-size: 2.2rem; }
  .carousel-section { height: 400px; }
  .prev-btn { left: 5%; }
  .next-btn { right: 5%; }
  .filter-section, .book-list-section { padding: 1.5rem; }
  .book-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  .book-cover-placeholder { margin-bottom: 1rem; }
  .book-details { align-items: center; }
  .like-button { align-self: center; }
}
</style>
