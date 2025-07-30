<template>
  <div class="bookstore-page">
    <!-- 베스트 10 캐러셀 섹션 -->
    <section class="carousel-section">
      <h2 class="section-title">지금 가장 인기있는 책 TOP 10</h2>
      <div class="perspective-carousel-container">
        <div class="perspective-carousel" :style="carouselStyle" @mousedown.prevent="onMouseDown" @touchstart.prevent="onTouchStart">
          <div v-for="(book, index) in topBooks" :key="book.id" class="carousel-item-3d" :style="get3DStyle(index)" @click="goToBookDetail(book.id)">
            <div class="book-cover-3d">
              {{ book.title }}
            </div>
          </div>
        </div>
      </div>
      <button @click="prevBook" class="carousel-control-btn prev-btn"><i class="fas fa-chevron-left"></i></button>
      <button @click="nextBook" class="carousel-control-btn next-btn"><i class="fas fa-chevron-right"></i></button>
    </section>

    <!-- 검색 및 필터 섹션 -->
    <section class="filter-section">
      <div class="search-input-wrapper">
        <input type="text" v-model="searchTerm" class="form-control search-input" placeholder="책 제목, 작가, 내용으로 검색...">
        <i class="fas fa-search search-icon"></i>
      </div>
      <div class="sort-options-wrapper">
        <label class="form-check form-check-inline" v-for="option in sortOptions" :key="option.value">
          <input type="radio" name="sort-option" :value="option.value" v-model="currentSortOption" class="form-check-input">
          <span class="form-check-label">{{ option.text }}</span>
        </label>
      </div>
      <div class="keyword-buttons-container">
        <button v-for="keyword in keywords" :key="keyword" @click="toggleKeyword(keyword)" :class="['keyword-button', { active: activeKeywords.has(keyword) }]">
          #{{ keyword }}
        </button>
      </div>
    </section>

    <!-- 책 리스트 섹션 -->
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
                <span><i class="fas fa-eye"></i> {{ book.views }}</span>
                <span><i class="fas fa-heart"></i> {{ book.likes }}</span>
              </div>
              <button class="btn btn-sm like-button" @click.stop="toggleLike(book)" :class="{ liked: isLiked(book.id) }">
                <i class="fas fa-heart"></i> 좋아요
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

const get3DStyle = (index: number) => {
  const itemAngle = index * 36;
  const totalRotation = carouselRotation.value + itemAngle;
  const normalizedAngle = Math.abs(totalRotation % 360);
  const angleToFront = Math.min(normalizedAngle, 360 - normalizedAngle);

  const angleFactor = angleToFront / 180;
  const effectFactor = Math.pow(angleFactor, 1.5);

  const opacity = Math.max(0.2, 1 - effectFactor * 0.8);
  const blur = effectFactor * 5;

  return {
    transform: `rotateY(${itemAngle}deg) translateZ(400px)`,
    opacity: opacity,
    filter: `blur(${blur}px)`,
  };
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

<style scoped>
.bookstore-page {
  padding: 2rem 1rem;
  background-color: #F5F5DC;
  color: #3D2C20;
  min-height: 100vh;
}

.section-title {
  font-size: 2.2rem;
  font-weight: 700;
  color: #3D2C20;
  margin-bottom: 2rem;
  text-align: left;
  width: 100%;
  padding-left: 15rem;
}

/* Carousel Section */
.carousel-section {
  position: relative;
  height: 480px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-bottom: 3rem;
}

.perspective-carousel-container {
  perspective: 2500px;
  width: 300px;
  height: 400px;
  position: relative;
}

.perspective-carousel {
  transform-style: preserve-3d;
  width: 100%;
  height: 100%;
  position: absolute;
  transform-origin: center center;
  cursor: grab;
}

.perspective-carousel:active { cursor: grabbing; }

.carousel-item-3d {
  position: absolute;
  left: 50px;
  top: 50px;
  width: 200px;
  height: 300px;
  transform-style: preserve-3d;
  transform-origin: center center;
  cursor: pointer;
  transition: transform 0.6s, opacity 0.6s, filter 0.6s;
}

.book-cover-3d {
  width: 100%;
  height: 100%;
  background-color: #5C4033;
  color: #F5F5DC;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 1rem;
  font-size: 1.2rem;
  font-weight: 600;
  box-shadow: 0 5px 15px rgba(0,0,0,0.2);
  border: 2px solid #8B4513;
  border-radius: 8px; /* Re-added border-radius */
}

.carousel-control-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
  background-color: rgba(184, 134, 11, 0.7);
  border: none;
  color: white;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  font-size: 1.5rem;
  transition: background-color 0.2s;
}
.carousel-control-btn:hover { background-color: #B8860B; }
.prev-btn { left: 10%; }
.next-btn { right: 10%; }

/* Filter Section */
.filter-section {
  background-color: #FFFFFF;
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 3rem;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
}

.search-input-wrapper {
  position: relative;
  margin-bottom: 1.5rem;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1.5rem 0.75rem 3rem;
  border: 1px solid #B8860B;
  border-radius: 9999px;
  background-color: #F5F5DC;
}
.search-input:focus { box-shadow: 0 0 0 0.25rem rgba(184, 134, 11, 0.25); }

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #B8860B;
}

.sort-options-wrapper {
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin-bottom: 1.5rem;
}

.form-check-input:checked { background-color: #B8860B; border-color: #B8860B; }

.keyword-buttons-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  justify-content: center;
}

.keyword-button {
  padding: 0.5rem 1rem;
  border-radius: 9999px;
  border: 1px solid #B8860B;
  background-color: transparent;
  color: #B8860B;
  transition: all 0.2s;
}
.keyword-button.active, .keyword-button:hover {
  background-color: #B8860B;
  color: #FFFFFF;
}

/* Book List Section */
.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.book-card {
  background-color: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  display: flex;
  gap: 1rem;
  padding: 1rem;
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
}
.book-card:hover { transform: translateY(-5px); box-shadow: 0 8px 20px rgba(0,0,0,0.1); }

.book-cover-placeholder {
  flex-shrink: 0;
  width: 100px;
  height: 140px;
  background-color: #8B4513;
  color: #F5F5DC;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
}

.book-details {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.book-title { font-weight: 700; margin-bottom: 0.25rem; }
.book-author { color: #5C4033; margin-bottom: 0.5rem; }
.book-summary { font-size: 0.9rem; flex-grow: 1; margin-bottom: 0.75rem; }

.book-stats {
  display: flex;
  gap: 1rem;
  font-size: 0.85rem;
  color: #8B4513;
  margin-bottom: 0.75rem;
}

.like-button {
  background-color: transparent;
  border: 1px solid #CD5C5C;
  color: #CD5C5C;
  align-self: flex-start;
}
.like-button.liked, .like-button:hover {
  background-color: #CD5C5C;
  color: #FFFFFF;
}

.no-books-message {
  text-align: center;
  padding: 3rem;
  font-size: 1.2rem;
}
</style>