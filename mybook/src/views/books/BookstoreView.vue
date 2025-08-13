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
          <div v-for="(book, index) in topBooks" :key="book.id" class="carousel-item-3d"
            :style="{ transform: get3DTransform(index) }" @click="goToBookDetail(book.id)">
            <div class="book-model">
              <div class="book-face book-cover"
                :style="{ backgroundImage: `url(${book.coverUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
                <div class="vertical-line-front-bright-effect"></div>
                <div class="title-box">
                  <h1>{{ book.title }}</h1>
                  <p class="author-in-box">{{ book.authorName }}</p>
                </div>
              </div>
              <div class="book-face book-spine"
                :style="{ backgroundImage: `url(${book.coverUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
              </div>
              <div class="book-face book-side-edge"></div>
              <div class="book-face book-back-cover"
                :style="{ backgroundImage: `url(${book.coverUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
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
            <button v-for="genre in genres" :key="genre" @click="toggleGenre(genre)"
              :class="['genre-button', { active: activeGenre === genre }]">
              {{ genre }}
            </button>
          </div>
        </div>
      </aside>

      <main class="book-list-main">
        <div v-if="paginatedBooks.length === 0" class="no-books-message">
          찾으시는 책이 없습니다.
        </div>
        <div v-else class="book-list-container">
          <div v-for="book in paginatedBooks" :key="book.id" class="book-list-item" @click="goToBookDetail(book.id)">
            <div class="book-cover-image"
              :style="{ backgroundImage: `url(${book.coverUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1974'})` }">
              <div class="list-title-box">
                <p class="list-title-box-title">{{ book.title }}</p>
                <p class="list-title-box-author">{{ book.authorName }}</p>
              </div>
            </div>
            <div class="book-details">
              <h5 class="book-title">{{ book.title }}</h5>
              <div class="author-info">
                <span class="book-author">{{ book.authorName }}</span>
                <span class="separator">·</span>
                <span class="publish-date">{{ formatDate(book.createdAt) }}</span>
              </div>
              <p class="book-summary">{{ book.summary }}</p>
              <div class="book-meta-tags">
                <span v-for="genre in book.genres" :key="genre" class="meta-tag genre-tag">{{ genre }}</span>
                <span v-for="tag in book.tags" :key="tag" class="meta-tag user-tag">#{{ tag }}</span>
              </div>
              <div class="book-stats">
                <span><i class="bi bi-eye"></i> {{ book.views }}</span>
                <span><i class="bi bi-heart"></i> {{ book.likes }}</span>
                <span><i class="bi bi-chat-dots"></i> {{ book.commentCount }}</span>
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
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';

// --- Interfaces & Types ---
interface Book {
  id: string;
  title: string;
  authorId: string;
  authorName?: string;
  summary?: string;
  coverUrl?: string;
  genres?: string[];
  tags?: string[];
  views?: number;
  likes?: number;
  commentCount?: number;
  createdAt: Date;
}

type SortOption = 'latest' | 'popular' | 'views';

// --- Router ---
const router = useRouter();

// --- Dummy Data ---
const DUMMY_BOOKS: Book[] = [
  { id: 'b1', title: '별 헤는 밤', authorId: 'author1', authorName: '윤동주', coverUrl: 'https://images.unsplash.com/photo-1521587760476-6c12a4b040da?w=500', views: 1200, likes: 250, commentCount: 45, summary: '어두운 밤하늘 아래 별들을 헤아리며 고향과 가족을 그리워하는 시인의 마음을 담은 아름다운 시집.', genres: ['자서전', '소설/시', '에세이'], tags: ['고향', '그리움', '밤하늘'], createdAt: new Date('2023-01-15') },
  { id: 'b2', title: '어린 왕자', authorId: 'author2', authorName: '생텍쥐페리', coverUrl: 'https://images.unsplash.com/photo-1518621736915-f3b1c41bfd00?w=500', views: 3500, likes: 780, commentCount: 128, summary: '사막에 불시착한 조종사가 만난 어린 왕자와의 이야기를 통해 삶의 진정한 의미를 탐구하는 철학 동화.', genres: ['여행', '어린이/동화', '소설/시'], tags: ['사막', '장미', '여우', '관계'], createdAt: new Date('2022-11-01') },
  { id: 'b3', title: '가족의 발견', authorId: 'author3', authorName: '김작가', coverUrl: 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500', views: 800, likes: 120, commentCount: 22, summary: '현대 사회에서 가족의 의미를 되새기고, 다양한 가족 형태의 아름다움을 조명하는 에세이.', genres: ['에세이', '사회/정치'], tags: ['가족', '공동체', '현대사회'], createdAt: new Date('2024-03-20') },
  { id: 'b4', title: '취미로 시작하는 코딩', authorId: 'author4', authorName: '이개발', coverUrl: 'https://images.unsplash.com/photo-1550063873-ab792950096b?w=500', views: 1500, likes: 300, commentCount: 88, summary: '코딩을 처음 접하는 사람들을 위한 쉽고 재미있는 입문서. 다양한 프로젝트를 통해 코딩의 즐거움을 알려준다.', genres: ['자기계발'], tags: ['코딩', '입문', '프로그래밍'], createdAt: new Date('2023-09-10') },
  { id: 'b5', title: '사랑의 온도', authorId: 'author5', authorName: '박사랑', coverUrl: 'https://images.unsplash.com/photo-1518717758536-85ae29035b6d?w=500', views: 2000, likes: 450, commentCount: 95, summary: '엇갈린 사랑과 인연 속에서 진정한 사랑의 의미를 찾아가는 연인들의 이야기. 감성적인 문체가 돋보인다.', genres: ['소설/시', '에세이'], tags: ['사랑', '연인', '감성글'], createdAt: new Date('2024-01-05') },
  { id: 'b6', title: '스포츠 심리학 개론', authorId: 'author6', authorName: '최건강', coverUrl: 'https://images.unsplash.com/photo-1517649763962-0c623066013b?w=500', views: 950, likes: 180, commentCount: 15, summary: '운동선수들의 심리 상태와 경기력 향상을 위한 심리학적 접근을 다룬 전문 서적.', genres: ['스포츠', '사회/정치'], tags: ['운동선수', '멘탈관리'], createdAt: new Date('2023-05-22') },
  { id: 'b7', title: '드래곤의 유산', authorId: 'author7', authorName: '김판타', coverUrl: 'https://images.unsplash.com/photo-1523586044048-b7d32d5da502?w=700&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8JUVEJThDJThDJUVDJUI2JUE5JUVCJUE1JTk4fGVufDB8fDB8fHww', views: 2500, likes: 600, commentCount: 250, summary: '고대 드래곤의 힘을 이어받은 주인공이 세상을 구하기 위해 모험을 떠나는 장대한 판타지 소설.', genres: ['소설/시', '청소년'], tags: ['판타지', '드래곤', '모험'], createdAt: new Date('2022-08-01') },
  { id: 'b8', title: '우주 탐사대의 기록', authorId: 'author8', authorName: '이공상', coverUrl: 'https://images.unsplash.com/photo-1534796636912-3b95b3ab5986?w=500', views: 1800, likes: 380, commentCount: 76, summary: '미지의 행성을 탐사하는 우주선 승무원들의 생존과 발견을 다룬 SF 소설. 과학적 상상력이 돋보인다.', genres: ['소설/시'], tags: ['SF', '우주탐사', '미래'], createdAt: new Date('2024-02-28') },
  { id: 'b9', title: '조선 왕조 실록 이야기', authorId: 'author9', authorName: '정역사', coverUrl: 'https://images.unsplash.com/photo-1448523183439-d2ac62aca997?w=700&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Nnx8JUVEJTk1JTlDJUVBJUI1JUFEfGVufDB8fDB8fHww', views: 1100, likes: 200, commentCount: 30, summary: '조선 왕조 500년 역사를 쉽고 재미있게 풀어낸 역사 교양서. 흥미로운 에피소드와 인물 중심의 서술.', genres: ['역사', '사회/정치'], tags: ['조선', '역사이야기', '실록'], createdAt: new Date('2023-07-07') },
  { id: 'b10', title: '나는 오늘부터 성장한다', authorId: 'author10', authorName: '강성장', coverUrl: 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=500', views: 2200, likes: 550, commentCount: 110, summary: '작은 습관의 변화가 삶을 어떻게 바꾸는지 보여주는 자기계발서. 긍정적인 메시지와 실천적인 조언.', genres: ['자기계발'], tags: ['성공', '습관', '동기부여'], createdAt: new Date('2024-04-12') },
];

// --- Reactive State ---
const publishedBooksRaw = JSON.parse(localStorage.getItem('publishedBooks') || '[]') as any[];
const publishedBooks = publishedBooksRaw.map(book => ({
  ...book,
  createdAt: new Date(book.createdAt || book.publicationDate),
})) as Book[];
const allBooks = ref<Book[]>([...publishedBooks, ...DUMMY_BOOKS.filter(b => !publishedBooks.some(pb => pb.id === b.id))]);
const searchTerm = ref('');
const currentSortOption = ref<SortOption>('latest');
const activeGenre = ref<string | null>(null);
const likedBookIds = ref<Set<string>>(new Set(['b1', 'b3']));
const genres = ['자서전', '여행', '스포츠', '소설/시', '에세이', '자기계발', '경제/경영', '사회/정치', '문화/예술', '역사', '종교', '청소년', '어린이/동화',];
const currentPage = ref(1);
const itemsPerPage = 10;
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

    const matchesGenres = !activeGenre.value || (book.genres && book.genres.includes(activeGenre.value));

    let matchesSearch = true;
    if (search) {
      if (search.startsWith('#')) {
        const tagToFind = search.substring(1);
        matchesSearch = book.tags?.some(tag => tag.toLowerCase().includes(tagToFind)) ?? false;
      } else {
        const searchTarget = [
          book.title,
          book.authorName,
          book.summary,
          ...(book.genres || [])
        ].join(' ').toLowerCase();
        matchesSearch = searchTarget.includes(search);
      }
    }

    return matchesSearch && matchesGenres;
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

const totalPages = computed(() => {
  return Math.ceil(filteredBooks.value.length / itemsPerPage);
});

const paginatedBooks = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return filteredBooks.value.slice(start, end);
});

const pagination = computed(() => {
  const pages = [];
  const sideCount = 1; // 현재 페이지 양쪽에 표시할 페이지 수
  const total = totalPages.value;
  const current = currentPage.value;

  if (total <= 5) {
    for (let i = 1; i <= total; i++) {
      pages.push(i);
    }
    return pages;
  }

  pages.push(1);

  if (current > sideCount + 2) {
    pages.push('...');
  }

  for (let i = Math.max(2, current - sideCount); i <= Math.min(total - 1, current + sideCount); i++) {
    pages.push(i);
  }

  if (current < total - sideCount - 1) {
    pages.push('...');
  }

  pages.push(total);

  return pages;
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
  transition: isDragging.value ? 'none' : 'transform 1s ease-in-out',
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

const changePage = (page: number) => {
  if (page > 0 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

const formatDate = (date: Date) => {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).format(date).replace(/\.$/, '');
};

let autoSlideInterval: number;

onMounted(() => {
  autoSlideInterval = window.setInterval(() => {
    nextBook();
  }, 3500);
});

onUnmounted(() => {
  clearInterval(autoSlideInterval);
  window.removeEventListener('mousemove', onMouseMove);
  window.removeEventListener('mouseup', onMouseUp);
  window.removeEventListener('touchmove', onTouchMove);
  window.removeEventListener('touchend', onTouchEnd);
});

// --- General Functions ---
function toggleGenre(genre: string) {
  if (activeGenre.value === genre) {
    activeGenre.value = null;
  } else {
    activeGenre.value = genre;
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
  font-family: 'EBSHunminjeongeumSaeronL', serif;
  position: relative;
  top: -0.1em; /* 살짝 위로 올리는 효과 */
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
