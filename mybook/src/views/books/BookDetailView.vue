<template>
  <div class="book-detail-page">
    <div v-if="book" class="book-container">
      <!-- Book Header (상세 정보 모드일 때만 표시) -->
      <header class="book-header" v-if="!isReadingMode">
        <div class="book-cover">
          <img :src="book.coverUrl || 'https://via.placeholder.com/150x220/5C4033/FFFFFF?text=Book+Cover'"
            alt="Book Cover">
        </div>
        <div class="book-meta">
          <h1 class="book-title">{{ book.title }}</h1>
          <p class="book-author">
            작가:
            <router-link v-if="isAuthor" to="/my-page">{{ book.authorName || '정보 없음' }}</router-link>
            <router-link v-else :to="`/author/${book.authorId}`">{{ book.authorName || '정보 없음' }}</router-link>
          </p>
          <p class="book-summary">{{ book.summary || '요약 정보가 없습니다.' }}</p>
          <div class="book-tags">
            <span v-for="tag in book.keywords" :key="tag" class="tag">#{{ tag }}</span>
          </div>
          <div class="book-actions">
            <button @click="toggleLike" class="btn btn-like" :class="{ liked: isLiked }">
              <i class="bi" :class="isLiked ? 'bi-heart-fill' : 'bi-heart'"></i> 좋아요 ({{ likeCount }})
            </button>
            
            <button v-if="isAuthor" @click="editBook" class="btn btn-edit-book">
              <i class="bi bi-pencil-square"></i> 책 편집하기
            </button>
            <button @click="startReading" class="btn btn-read-book">
              <i class="bi bi-book"></i> 책 읽기
            </button>
          </div>
        </div>
      </header>

      <!-- Reading Mode Content (책 읽기 모드일 때만 표시) -->
      <div v-if="isReadingMode" class="reading-mode-container">
        <div class="reading-mode-header">
          <button @click="endReading" class="btn btn-secondary">책 상세 정보로 돌아가기</button>
          <h2 class="reading-mode-title">{{ book.title }}</h2>
        </div>
        <div class="book-pages-wrapper">
          <div class="page-content">
            <main class="episode-viewer">
              <div class="episode-navigation">
                <button @click="prevEpisode" :disabled="currentEpisodeIndex === 0 && currentPage === 0"
                  class="btn btn-nav">이전</button>
                <h3 class="episode-title">에피소드 {{ currentEpisodeIndex + 1 }}</h3>
                <button @click="nextEpisode" :disabled="currentEpisodeIndex >= book.episodes.length - 1"
                  class="btn btn-nav">다음</button>
              </div>
              <div class="episode-content">
                <p>{{ currentEpisodeContent }}</p>
              </div>
              <div class="page-number-container">
                <span class="page-number">{{ totalCurrentPage }} p</span>
              </div>

            </main>
          </div>
        </div>
      </div>

      <!-- Comments Section (항상 표시) -->
      <section class="comments-section">
        <h3 class="comments-title">댓글</h3>
        <div class="comment-input-area">
          <textarea v-model="newComment" placeholder="댓글을 입력하세요..." class="form-control"></textarea>
          <button @click="addComment" class="btn btn-primary">등록</button>
        </div>
        <div class="comment-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <p class="comment-author">
              <strong>
                <router-link :to="`/author/${comment.authorId}`">{{ comment.authorName || '익명' }}</router-link>
              </strong>
              <span class="comment-date">{{ comment.createdAt.toLocaleString() }}</span>
            </p>
            <p class="comment-text">{{ comment.text }}</p>
          </div>
        </div>
      </section>

    </div>
    <div v-else class="loading-message">
      <p>책 정보를 불러오는 중입니다...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useRouter } from 'vue-router';

// --- Interfaces ---
interface Episode {
  content: string;
}

interface Book {
  id: string;
  title: string;
  authorId: string;
  authorName?: string;
  summary?: string;
  coverUrl?: string;
  keywords?: string[];
  episodes: Episode[];
  likes: number;
  views: number;
}

interface Comment {
  id: string;
  bookId: string; // Add bookId to comment interface
  episodeIndex: number; // Add episodeIndex to comment interface
  authorId: string;
  authorName?: string;
  text: string;
  createdAt: Date;
}

const totalCurrentPage = computed(() => {
  if (!book.value) return 0;

  let totalPagesBefore = 0;
  for (let i = 0; i < currentEpisodeIndex.value; i++) {
    const episodeContent = book.value.episodes[i].content;
    totalPagesBefore += Math.ceil(episodeContent.length / charsPerPage);
  }

  return totalPagesBefore + currentPage.value + 1;
});

// --- Dummy Data ---
const DUMMY_BOOKS: Book[] = [
  {
    id: 'mybook1',
    title: '나의 어린 시절 이야기',
    authorId: 'dummyUser1',
    authorName: '김작가',
    summary: '어린 시절의 소중한 추억들을 담은 자서전입니다. 골목길에서의 모험, 할머니의 따뜻한 손길, 그리고 친구들과의 우정까지, 순수했던 그 시절의 이야기들이 펼쳐집니다.',
    coverUrl: 'https://via.placeholder.com/150x220/5C4033/FFFFFF?text=MyBook1',
    keywords: ['자서전', '어린시절', '추억', '성장', '가족', '친구'],
    episodes: [
      { content: '어릴 적 살던 동네의 골목길은 언제나 모험의 시작이었다. 낡은 담벼락을 따라 이어진 좁은 길은 우리에게 미지의 세계로 통하는 문과 같았다. 해 질 녘까지 술래잡기를 하고, 숨바꼭질을 하며 뛰어놀던 그곳은 단순한 길이 아니라, 우리의 꿈과 상상력이 자라나던 놀이터였다. 골목길 모퉁이를 돌 때마다 새로운 친구를 만나고, 새로운 비밀을 발견하곤 했다. 여름날 소나기가 내린 뒤 흙냄새가 진동하던 골목길, 겨울날 눈이 소복이 쌓여 발자국을 남기던 골목길의 풍경은 아직도 내 기억 속에 선명하게 남아있다. 그 시절의 골목길은 우리에게 자유와 행복을 선물해준 소중한 공간이었다. 우리는 그곳에서 넘어지고, 울고, 웃으며 함께 성장했다. 골목길의 작은 돌멩이 하나하나에도 우리의 추억이 깃들어 있었다. 지금은 사라지고 없는 그 골목길이 가끔씩 그리워지는 것은, 아마도 그 시절의 순수했던 나 자신을 다시 만나고 싶어서일 것이다. 그 골목길은 내 어린 시절의 가장 아름다운 배경이었다. 어릴 적 살던 동네의 골목길은 언제나 모험의 시작이었다. 낡은 담벼락을 따라 이어진 좁은 길은 우리에게 미지의 세계로 통하는 문과 같았다. 해 질 녘까지 술래잡기를 하고, 숨바꼭질을 하며 뛰어놀던 그곳은 단순한 길이 아니라, 우리의 꿈과 상상력이 자라나던 놀이터였다. 골목길 모퉁이를 돌 때마다 새로운 친구를 만나고, 새로운 비밀을 발견하곤 했다. 여름날 소나기가 내린 뒤 흙냄새가 진동하던 골목길, 겨울날 눈이 소복이 쌓여 발자국을 남기던 골목길의 풍경은 아직도 내 기억 속에 선명하게 남아있다. 그 시절의 골목길은 우리에게 자유와 행복을 선물해준 소중한 공간이었다. 우리는 그곳에서 넘어지고, 울고, 웃으며 함께 성장했다. 골목길의 작은 돌멩이 하나하나에도 우리의 추억이 깃들어 있었다. 지금은 사라지고 없는 그 골목길이 가끔씩 그리워지는 것은, 아마도 그 시절의 순수했던 나 자신을 다시 만나고 싶어서일 것이다. 그 골목길은 내 어린 시절의 가장 아름다운 배경이었다.' },
      { content: '할머니의 따뜻한 손길과 맛있는 음식은 잊을 수 없는 기억이다. 할머니 댁에 가면 언제나 구수한 된장찌개 냄새가 나를 반겼다. 할머니는 내가 좋아하는 반찬들을 한가득 차려주셨고, 나는 할머니의 사랑이 담긴 밥상을 마주할 때마다 세상에서 가장 행복한 아이가 되었다. 특히 할머니가 직접 만들어주시던 쑥떡과 식혜는 그 어떤 고급 디저트보다도 맛있었다. 할머니는 음식을 만들 때마다 정성을 다하셨고, 그 정성이 음식 맛에 그대로 배어 있었다. 할머니의 손은 언제나 따뜻하고 부드러웠다. 내가 아플 때면 할머니는 내 이마에 손을 얹고 열을 재셨고, 밤새도록 내 옆을 지켜주셨다. 할머니의 품은 세상에서 가장 안전하고 포근한 안식처였다. 할머니는 나에게 삶의 지혜와 사랑을 가르쳐주셨다. 할머니의 잔잔한 목소리로 들려주시던 옛날이야기는 나를 꿈의 세계로 이끌었고, 할머니의 따뜻한 미소는 나에게 용기를 주었다. 할머니는 내 어린 시절의 가장 큰 버팀목이자, 영원한 사랑이었다. 지금도 할머니의 손맛과 따뜻한 품이 그리워진다.' },
      { content: '친구들과 함께 뛰어놀던 운동장은 우리만의 작은 세상이었다. 학교가 끝나면 우리는 약속이라도 한 듯 운동장으로 달려갔다. 축구공 하나만 있으면 시간 가는 줄 모르고 뛰어놀았고, 해가 져서 어두워질 때까지 운동장을 떠나지 않았다. 운동장 한쪽에는 낡은 철봉과 미끄럼틀이 있었는데, 우리는 그곳에서 온갖 기상천외한 놀이를 만들어냈다. 때로는 싸우기도 하고, 때로는 화해하기도 하면서 우리는 서로를 알아갔다. 운동장에서 흘린 땀방울만큼 우리의 우정은 더욱 단단해졌다. 운동장 구석에 피어있던 들꽃, 운동장 한가운데 서 있던 커다란 나무는 우리의 비밀을 지켜주는 친구였다. 우리는 그곳에서 꿈을 키웠고, 미래를 상상했다. 운동장은 우리에게 단순한 놀이 공간이 아니라, 함께 성장하고 추억을 쌓아가던 소중한 공간이었다. 지금도 가끔씩 그 운동장을 떠올리면, 함께 웃고 떠들던 친구들의 얼굴이 아른거린다. 그 시절의 운동장은 우리에게 영원히 잊을 수 없는 행복한 기억으로 남아있다.' },
      { content: '여름방학은 나에게 최고의 선물이었다. 매일 아침 늦잠을 자고, 점심에는 엄마가 해주신 맛있는 음식을 먹었다. 오후에는 친구들과 동네 개울가로 달려가 물장구를 치고, 물고기를 잡았다. 시원한 물속에서 첨벙거리며 놀다 보면 더위는 금세 사라졌다. 저녁에는 평상에 앉아 수박을 먹으며 할머니가 들려주시는 옛날이야기를 들었다. 밤하늘에는 수많은 별들이 쏟아질 듯 반짝였고, 우리는 별똥별을 보며 소원을 빌었다. 여름방학은 나에게 자유와 행복을 안겨주었고, 잊지 못할 추억들을 선물해주었다. 그 시절의 여름방학은 마치 꿈처럼 아름다웠다. 매일매일이 새로운 모험의 연속이었고, 모든 순간이 소중했다. 여름방학이 끝나가는 것이 아쉬워 잠 못 이루던 밤도 있었다. 그만큼 여름방학은 나에게 특별한 시간이었다. 지금도 여름이 되면 그 시절의 여름방학이 떠오르곤 한다. 그 순수하고 행복했던 시간들이 나를 미소 짓게 한다.' },
      { content: '겨울은 나에게 또 다른 즐거움을 선사했다. 눈이 내리면 우리는 운동장으로 달려가 눈싸움을 하고, 눈사람을 만들었다. 손이 시리고 발이 꽁꽁 얼어도 우리는 아랑곳하지 않고 눈밭을 뛰어다녔다. 언덕에서는 썰매를 타고 내려오며 스릴을 만끽했다. 집으로 돌아오면 엄마가 끓여주신 따뜻한 어묵탕이 우리를 기다리고 있었다. 김이 모락모락 나는 어묵탕을 먹으며 우리는 추위에 얼었던 몸을 녹였다. 겨울밤에는 이불 속에 들어가 만화책을 읽거나, 가족들과 함께 보드게임을 했다. 따뜻한 방 안에서 가족들과 함께 보내는 시간은 그 어떤 것과도 바꿀 수 없는 소중한 시간이었다. 겨울은 나에게 포근함과 따뜻함을 안겨주었고, 가족의 소중함을 일깨워주었다. 그 시절의 겨울은 차가운 계절이 아니라, 따뜻한 추억으로 가득한 계절이었다. 지금도 겨울이 되면 그 시절의 따뜻한 기억들이 나를 감싸 안는 듯하다.' }
    ],
    likes: 20,
    views: 150,
  },
  {
    id: 'mybook2',
    title: '꿈을 향한 도전',
    authorId: 'dummyUser1',
    authorName: '김작가',
    summary: '꿈을 쫓아 달려온 나의 열정적인 삶의 기록입니다.',
    coverUrl: 'https://via.placeholder.com/150x220/8B4513/FFFFFF?text=MyBook2',
    keywords: ['도전', '성장', '열정'],
    episodes: [
      { content: '새로운 목표를 설정하고 첫 발을 내디뎠을 때의 설렘.' },
      { content: '수많은 실패와 좌절 속에서도 포기하지 않았던 이유.' },
      { content: '마침내 꿈을 이뤘을 때의 감격과 새로운 시작.' },
    ],
    likes: 30,
    views: 200,
  },
  {
    id: 'mybook3',
    title: '여행의 기록',
    authorId: 'dummyUser1',
    authorName: '김작가',
    summary: '세계를 여행하며 만난 사람들과 풍경에 대한 이야기입니다.',
    coverUrl: 'https://via.placeholder.com/150x220/DAA520/FFFFFF?text=MyBook3',
    keywords: ['여행', '세계', '경험'],
    episodes: [
      { content: '낯선 도시에서 길을 잃었을 때의 당황스러움과 새로운 발견.' },
      { content: '현지인들과의 소통을 통해 배운 삶의 지혜.' },
      { content: '여행은 나를 성장시키는 가장 좋은 방법이다.' },
    ],
    likes: 25,
    views: 180,
  },
  {
    id: 'mybook4',
    title: '개발자의 삶',
    authorId: 'dummyUser1',
    authorName: '김작가',
    summary: '개발자로 살아가며 겪는 일상과 생각들을 담은 에세이입니다.',
    coverUrl: 'https://via.placeholder.com/150x220/4A90E2/FFFFFF?text=MyBook4',
    keywords: ['개발', '일상', '에세이'],
    episodes: [
      { content: '새로운 기술을 배우는 것은 언제나 즐겁다.' },
      { content: '버그와의 사투는 개발자의 숙명.' },
      { content: '코드 한 줄이 세상을 바꿀 수 있다는 믿음.' },
    ],
    likes: 10,
    views: 50,
  },
  {
    id: 'likedbook1',
    title: '별 헤는 밤',
    authorId: 'dummyUser2',
    authorName: '윤동주',
    summary: '어두운 밤하늘 아래 별들을 헤아리며 고향과 가족을 그리워하는 시인의 마음을 담은 아름다운 시집.',
    coverUrl: 'https://via.placeholder.com/150x220/5C4033/FFFFFF?text=LikedBook1',
    keywords: ['자서전', '감성', '시'],
    episodes: [
      { content: '별 하나에 추억과 별 하나에 사랑과 별 하나에 쓸쓸함과.' },
      { content: '어머니, 그리고 당신은 멀리 북간도에 계십니다.' },
    ],
    likes: 250,
    views: 1200,
  },
  {
    id: 'likedbook2',
    title: '어린 왕자',
    authorId: 'dummyUser3',
    authorName: '생텍쥐페리',
    summary: '사막에 불시착한 조종사가 만난 어린 왕자와의 이야기를 통해 삶의 진정한 의미를 탐구하는 철학 동화.',
    coverUrl: 'https://via.placeholder.com/150x220/8B4513/FFFFFF?text=LikedBook2',
    keywords: ['여행', '성장', '철학'],
    episodes: [
      { content: '가장 중요한 것은 눈에 보이지 않아.' },
      { content: '네 장미꽃이 그토록 소중한 것은 네가 그 꽃에 들인 시간 때문이야.' },
    ],
    likes: 780,
    views: 3500,
  },
  {
    id: 'b1',
    title: '나의 첫 유럽 여행기',
    authorId: 'user_dummy_1',
    authorName: '글쓰는 여행가',
    summary: '파리, 로마, 바르셀로나를 여행하며 겪은 잊지 못할 순간들에 대한 기록입니다.',
    coverUrl: 'https://via.placeholder.com/150x220/5C4033/FFFFFF?text=Book+Cover+1',
    keywords: ['여행', '유럽', '에세이'],
    episodes: [
      { content: '첫째 날, 파리의 낭만에 빠지다. 에펠탑 아래에서의 피크닉은 정말 환상적이었다.' },
      { content: '로마에서는 콜로세움의 웅장함에 압도당했다. 고대 로마인들의 함성이 들리는 듯했다.' },
      { content: '바르셀로나의 가우디 건축물들은 마치 동화 속에 들어온 것 같은 기분을 느끼게 해주었다.' },
      { content: '여행의 마지막 날, 아쉬움을 뒤로하고 다음을 기약하며 비행기에 올랐다.' },
    ],
    likes: 15,
    views: 120,
  },
  {
    id: 'b2',
    title: '코딩, 내 인생의 새로운 챕터',
    authorId: 'user_dummy_2',
    authorName: '그림 그리는 이야기꾼',
    summary: '그림만 그리던 내가 코딩을 배우며 겪은 성장통과 새로운 가능성에 대한 이야기.',
    coverUrl: 'https://via.placeholder.com/150x220/8B4513/FFFFFF?text=Book+Cover+2',
    keywords: ['코딩', '자기계발', '커리어'],
    episodes: [
      { content: '"Hello World"를 처음 화면에 띄웠을 때의 그 짜릿함은 아직도 잊을 수 없다.' },
      { content: '수많은 오류와 싸우며 밤을 새웠지만, 결국 문제를 해결했을 때의 희열은 그 모든 고통을 잊게 했다.' },
      { content: '이제 나는 코드로 그림을 그린다. 기술과 예술의 만남은 내게 새로운 세상을 열어주었다.' },
    ],
    likes: 32,
    views: 250,
  },
  {
    id: 'b3',
    title: '제주도 한 달 살기 (초안)',
    authorId: 'user_dummy_1',
    authorName: '글쓰는 여행가',
    summary: '제주도에서 한 달간 머물며 느낀 자연의 아름다움과 소소한 일상.',
    coverUrl: 'https://via.placeholder.com/150x220/DAA520/FFFFFF?text=Book+Cover+3',
    keywords: ['여행', '제주', '일상'],
    episodes: [
      { content: '협재 해변의 일몰은 정말 아름다웠다. 매일 봐도 질리지 않는 풍경.' },
      { content: '오름을 오르며 마주한 제주의 바람은 모든 시름을 잊게 했다.' },
    ],
    likes: 5,
    views: 30,
  },
  {
    id: 'b4',
    title: '취미로 시작하는 코딩',
    authorId: 'author4',
    authorName: '이개발',
    summary: '코딩을 처음 접하는 사람들을 위한 쉽고 재미있는 입문서. 다양한 프로젝트를 통해 코딩의 즐거움을 알려준다.',
    coverUrl: 'https://via.placeholder.com/150x220/4A90E2/FFFFFF?text=Book+Cover+4',
    keywords: ['취미', 'IT', '자기계발'],
    episodes: [
      { content: '변수 선언부터 함수 호출까지, 코딩의 기초를 다져보자.' },
      { content: '간단한 웹 페이지를 만들며 HTML, CSS, JavaScript를 익힌다.' },
    ],
    likes: 150,
    views: 800,
  },
  {
    id: 'b5',
    title: '사랑의 온도',
    authorId: 'author5',
    authorName: '박사랑',
    summary: '엇갈린 사랑과 인연 속에서 진정한 사랑의 의미를 찾아가는 연인들의 이야기. 감성적인 문체가 돋보인다.',
    coverUrl: 'https://via.placeholder.com/150x220/FF6347/FFFFFF?text=Book+Cover+5',
    keywords: ['연애', '로맨스', '감성'],
    episodes: [
      { content: '첫 만남의 설렘, 잊을 수 없는 그 순간.' },
      { content: '사랑과 이별, 그리고 다시 찾아온 인연.' },
    ],
    likes: 450,
    views: 2000,
  },
  {
    id: 'b6',
    title: '스포츠 심리학 개론',
    authorId: 'author6',
    authorName: '최건강',
    summary: '운동선수들의 심리 상태와 경기력 향상을 위한 심리학적 접근을 다룬 전문 서적.',
    coverUrl: 'https://via.placeholder.com/150x220/20B2AA/FFFFFF?text=Book+Cover+6',
    keywords: ['스포츠', '심리학', '건강'],
    episodes: [
      { content: '승리하는 마음가짐: 멘탈 트레이닝의 중요성.' },
      { content: '슬럼프 극복: 심리적 요인이 경기력에 미치는 영향.' },
    ],
    likes: 180,
    views: 950,
  },
  {
    id: 'b7',
    title: '드래곤의 유산',
    authorId: 'author7',
    authorName: '김판타',
    summary: '고대 드래곤의 힘을 이어받은 주인공이 세상을 구하기 위해 모험을 떠나는 장대한 판타지 소설.',
    coverUrl: 'https://via.placeholder.com/150x220/8A2BE2/FFFFFF?text=Book+Cover+7',
    keywords: ['판타지', '모험', '영웅'],
    episodes: [
      { content: '어둠의 세력에 맞서 싸우는 용감한 전사들.' },
      { content: '고대 유적에서 발견된 드래곤의 비밀.' },
    ],
    likes: 600,
    views: 2500,
  },
  {
    id: 'b8',
    title: '우주 탐사대의 기록',
    authorId: 'author8',
    authorName: '이공상',
    summary: '미지의 행성을 탐사하는 우주선 승무원들의 생존과 발견을 다룬 SF 소설. 과학적 상상력이 돋보인다.',
    coverUrl: 'https://via.placeholder.com/150x220/4682B4/FFFFFF?text=Book+Cover+8',
    keywords: ['공상과학', '우주', '미래'],
    episodes: [
      { content: '미지의 행성에서 발견된 생명체의 흔적.' },
      { content: '우주선 고장, 생존을 위한 사투.' },
    ],
    likes: 380,
    views: 1800,
  },
  {
    id: 'b9',
    title: '조선 왕조 실록 이야기',
    authorId: 'author9',
    authorName: '정역사',
    summary: '조선 왕조 500년 역사를 쉽고 재미있게 풀어낸 역사 교양서. 흥미로운 에피소드와 인물 중심의 서술.',
    coverUrl: 'https://via.placeholder.com/150x220/D2B48C/FFFFFF?text=Book+Cover+9',
    keywords: ['역사', '교양', '한국사'],
    episodes: [
      { content: '태조 이성계, 새로운 왕조를 열다.' },
      { content: '세종대왕, 한글을 창제하다.' },
    ],
    likes: 200,
    views: 1100,
  },
  {
    id: 'b10',
    title: '나는 오늘부터 성장한다',
    authorId: 'author10',
    authorName: '강성장',
    summary: '작은 습관의 변화가 삶을 어떻게 바꾸는지 보여주는 자기계발서. 긍정적인 메시지와 실천적인 조언.',
    coverUrl: 'https://via.placeholder.com/150x220/FFD700/FFFFFF?text=Book+Cover+10',
    keywords: ['자기계발', '성장', '동기부여'],
    episodes: [
      { content: '아침형 인간 되기: 하루를 두 배로 사는 법.' },
      { content: '긍정적인 생각의 힘: 삶을 변화시키는 마법.' },
    ],
    likes: 550,
    views: 2200,
  },
  {
    id: 'draft_book_1',
    title: '제주도 한 달 살기 (초안)',
    authorId: 'dummyUser1',
    authorName: '김작가',
    summary: '제주도에서 한 달간 머물며 느낀 자연의 아름다움과 소소한 일상.',
    coverUrl: 'https://via.placeholder.com/150x220/DAA520/FFFFFF?text=DraftBook1',
    keywords: ['여행', '제주', '일상'],
    episodes: [
      { content: '협재 해변의 일몰은 정말 아름다웠다. 매일 봐도 질리지 않는 풍경.' },
      { content: '오름을 오르며 마주한 제주의 바람은 모든 시름을 잊게 했다.' },
    ],
    likes: 5,
    views: 30,
  },
  {
    id: 'draft_book_2',
    title: '나의 요리 레시피 북 (미완성)',
    authorId: 'dummyUser1',
    authorName: '김작가',
    summary: '나만의 특별한 요리 레시피를 모아둔 책입니다.',
    coverUrl: 'https://via.placeholder.com/150x220/DAA520/FFFFFF?text=DraftBook2',
    keywords: ['요리', '레시피', '일상'],
    episodes: [
      { content: '김치찌개 황금 레시피: 돼지고기 듬뿍, 신김치 필수!' },
    ],
    likes: 2,
    views: 10,
  },
  {
    id: 'integrated_book_1',
    title: '공작소의 첫 작품',
    authorId: 'group_dummy_1',
    authorName: '공동 집필',
    summary: '우리들의 이야기 공작소에서 함께 만든 첫 번째 통합 책입니다.',
    coverUrl: 'https://via.placeholder.com/150x220/B8860B/FFFFFF?text=Integrated1',
    keywords: ['그룹', '협업', '이야기'],
    episodes: [
      { content: '그룹원들과의 첫 만남, 설렘 가득한 시작.' },
      { content: '아이디어 브레인스토밍, 창의적인 생각들이 쏟아져 나왔다.' },
      { content: '함께 만들어가는 즐거움, 그리고 완성의 보람.' },
    ],
    likes: 50,
    views: 300,
  },
  {
    id: 'integrated_book_2',
    title: '조선 왕조 비사',
    authorId: 'group_dummy_2',
    authorName: '공동 집필',
    summary: '역사 탐험대에서 함께 파헤친 조선 왕조의 숨겨진 이야기들.',
    coverUrl: 'https://via.placeholder.com/150x220/B8860B/FFFFFF?text=Integrated2',
    keywords: ['역사', '조선', '비사'],
    episodes: [
      { content: '왕실의 비밀, 역사의 뒤안길에 숨겨진 진실.' },
      { content: '흥미로운 인물들, 그들의 삶과 업적.' },
    ],
    likes: 40,
    views: 280,
  },
];

const DUMMY_COMMENTS: Comment[] = [
  { id: 'c1', bookId: 'b1', episodeIndex: 0, authorId: 'user_dummy_3', authorName: '독서광', text: '정말 재미있게 읽었습니다! 다음 여행기도 기대됩니다.', createdAt: new Date('2024-07-28T10:00:00Z') },
  { id: 'c2', bookId: 'b1', episodeIndex: 0, authorId: 'user_dummy_4', authorName: '여행자', text: '저도 유럽 여행 가고 싶네요. 대리만족했습니다.', createdAt: new Date('2024-07-28T11:30:00Z') },
  { id: 'c3', bookId: 'b2', episodeIndex: 0, authorId: 'user_dummy_5', authorName: '코린이', text: '코딩에 대한 두려움을 없애준 책입니다. 감사합니다!', createdAt: new Date('2024-07-27T14:00:00Z') },
  { id: 'c4', bookId: 'b1', episodeIndex: 1, authorId: 'user_dummy_3', authorName: '독서광', text: '콜로세움 정말 멋지죠!', createdAt: new Date('2024-07-28T15:00:00Z') },
  { id: 'c5', bookId: 'mybook1', episodeIndex: 0, authorId: 'user_dummy_1', authorName: '김작가', text: '이 에피소드는 저에게도 특별한 기억입니다.', createdAt: new Date('2024-07-29T09:00:00Z') },
];

// --- Router ---
const route = useRoute();
const router = useRouter(); // Add useRouter
const bookId = computed(() => route.params.id as string);

// --- Reactive State ---
const book = ref<Book | null>(null);
const comments = ref<Comment[]>([]);
const currentEpisodeIndex = ref(0);
const newComment = ref('');
const isLiked = ref(false);
const likeCount = ref(0);
const currentUserId = ref('dummyUser1'); // Dummy current user ID for testing author check
const isReadingMode = ref(false); // New state for reading mode

// Pagination state
const charsPerPage = 1000; // 한 페이지당 글자 수 (조정 가능)
const currentPage = ref(0);
const totalPages = ref(1);

// --- Computed Properties ---
const paginatedEpisodeContent = computed<string[]>(() => {
  if (book.value && book.value.episodes && book.value.episodes[currentEpisodeIndex.value]) {
    const content = book.value.episodes[currentEpisodeIndex.value].content;
    const pages: string[] = [];
    for (let i = 0; i < content.length; i += charsPerPage) {
      pages.push(content.substring(i, i + charsPerPage));
    }
    return pages.length > 0 ? pages : ['']; // 최소 한 페이지는 있도록
  }
  return ['에피소드 내용이 없습니다.'];
});

const currentEpisodeContent = computed(() => {
  return paginatedEpisodeContent.value[currentPage.value] || '에피소드 내용이 없습니다.';
});

const isAuthor = computed(() => {
  return book.value && book.value.authorId === currentUserId.value;
});

// --- Functions ---
function fetchBookData() {
  console.log('Fetching book data for bookId:', bookId.value);
  const foundBook = DUMMY_BOOKS.find(b => b.id === bookId.value);
  if (foundBook) {
    console.log('Found book:', foundBook);
    book.value = { ...foundBook }; // Create a copy to avoid direct mutation of dummy data
    likeCount.value = book.value.likes || 0;
    // Simulate view count increment
    if (book.value) book.value.views = (book.value.views || 0) + 1;
    console.log('Book ref updated:', book.value);
  } else {
    console.error('Book not found for ID:', bookId.value);
    book.value = null; // Ensure book is null if not found
  }
}

function fetchComments() {
  // Filter comments by bookId and currentEpisodeIndex
  comments.value = DUMMY_COMMENTS.filter(
    c => c.bookId === bookId.value && c.episodeIndex === currentEpisodeIndex.value
  ).sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime());
}

function addComment() {
  if (!newComment.value.trim()) {
    alert('댓글을 입력해주세요.');
    return;
  }
  if (!book.value) {
    alert('책 정보를 불러올 수 없습니다.');
    return;
  }

  const newId = `c${DUMMY_COMMENTS.length + 1}`;
  const comment: Comment = {
    id: newId,
    bookId: book.value.id,
    episodeIndex: currentEpisodeIndex.value,
    authorId: 'dummyUser_current', // Replace with actual current user ID
    authorName: '현재 사용자', // Replace with actual current user name
    text: newComment.value,
    createdAt: new Date(),
  };
  DUMMY_COMMENTS.push(comment); // Add to dummy data for persistence in this session
  comments.value.unshift(comment); // Add to the beginning of displayed comments
  newComment.value = '';
  alert('댓글이 등록되었습니다.');
}

function toggleLike() {
  // Simulate like/unlike locally
  isLiked.value = !isLiked.value;
  if (isLiked.value) {
    likeCount.value++;
    alert('좋아요를 눌렀습니다!');
  } else {
    likeCount.value--;
    alert('좋아요를 취소했습니다.');
  }
}



function updatePagination() {
  currentPage.value = 0; // 에피소드 변경 시 첫 페이지로 초기화
  totalPages.value = paginatedEpisodeContent.value.length;
}

function prevEpisode() {
  if (currentPage.value > 0) {
    currentPage.value--; // 현재 에피소드 내에서 이전 페이지로 이동
  } else if (currentEpisodeIndex.value > 0) {
    currentEpisodeIndex.value--; // 이전 에피소드로 이동
    updatePagination(); // 새 에피소드의 페이지 정보 업데이트
    fetchComments(); // 새 에피소드의 댓글 불러오기
  }
}

function nextEpisode() {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++; // 현재 에피소드 내에서 다음 페이지로 이동
  } else if (book.value && currentEpisodeIndex.value < book.value.episodes.length - 1) {
    currentEpisodeIndex.value++; // 다음 에피소드로 이동
    updatePagination(); // 새 에피소드의 페이지 정보 업데이트
    fetchComments(); // 새 에피소드의 댓글 불러오기
  }
}

function editBook() {
  if (book.value) {
    router.push({ name: 'CreateBookView', params: { bookId: book.value.id } }); // Navigate to CreateBookView with bookId
  }
}

function startReading() {
  isReadingMode.value = true;
  currentEpisodeIndex.value = 0; // Start from the first episode
  updatePagination(); // 페이지네이션 정보 초기화
  fetchComments(); // Load comments for the first episode
}

function endReading() {
  isReadingMode.value = false;
}

// --- Lifecycle Hooks ---
onMounted(() => {
  fetchBookData();
  fetchComments();
});

// Watch for changes in currentEpisodeIndex to update pagination
watch(currentEpisodeIndex, () => {
  updatePagination();
});
</script>

<style scoped>
.book-detail-page {
  padding: 80px 2rem 4rem;
  background-color: #EAE0D5;
  /* 은은한 배경색 */
  color: #3D2C20;
  min-height: calc(100vh - 56px);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.book-container {
  max-width: 900px;
  margin: 0 auto;
  background-color: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 2rem;
}

/* Header */
.book-header {
  display: flex;
  gap: 2rem;
  border-bottom: 1px solid #EAE0D5;
  padding-bottom: 2rem;
  margin-bottom: 2rem;
}

.book-cover img {
  width: 150px;
  height: 220px;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.book-meta {
  flex-grow: 1;
}

.book-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.book-author {
  font-size: 1.1rem;
  color: #8B4513;
  margin-bottom: 1rem;
}

.book-summary {
  font-size: 1rem;
  line-height: 1.6;
  margin-bottom: 1rem;
}

.book-tags {
  margin-bottom: 1.5rem;
}

.tag {
  display: inline-block;
  background-color: #EAE0D5;
  color: #5C4033;
  padding: 0.3rem 0.8rem;
  border-radius: 15px;
  font-size: 0.9rem;
  margin-right: 0.5rem;
}

.book-actions {
  display: flex;
  flex-wrap: wrap;
  /* 버튼이 많아지면 줄바꿈되도록 */
  gap: 0.8rem;
  /* 버튼 간 간격 조정 */
  margin-top: 1rem;
  /* 버튼 위 여백 추가 */
}

.btn-like.liked {
  background-color: #CD5C5C;
  color: #fff;
  border-color: #CD5C5C;
}

.btn-read-book {
  background-color: #4CAF50;
  /* Green color for read button */
  color: white;
}

.btn-read-book:hover {
  background-color: #45a049;
}

/* Reading Mode Specific Styles */
.reading-mode-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 1rem;
  width: 100%;
  /* 전체 너비 사용 */
}

.reading-mode-header {
  width: 100%;
  max-width: 900px;
  /* book-container와 동일하게 설정 */
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding: 0 2rem;
  /* 좌우 패딩 추가 */
  box-sizing: border-box;
  /* 패딩이 너비에 포함되도록 */
}

.reading-mode-title {
  font-size: 2rem;
  font-weight: 700;
  color: #3D2C20;
}

.book-pages-wrapper {
  width: 700px;
  /* 단일 페이지 너비 */
  height: 800px;
  /* 책의 높이 */
  background-color: #FDF8E7;
  /* 종이 색상 */
  border-radius: 4px;
  /* 페이지 모서리 둥글게 */
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2),
    /* 큰 그림자 */
    0 0 0 1px rgba(0, 0, 0, 0.05);
  /* 얇은 테두리 그림자 */
  position: relative;
  transform-style: preserve-3d;
  perspective: 1000px;
  display: flex;
  /* 내부 콘텐츠 정렬을 위해 */
  flex-direction: column;
  /* 세로 정렬 */
}

.page-content {
  flex: 1;
  /* 남은 공간을 모두 차지 */
  padding: 2rem 3rem;
  /* 페이지 내부 여백 */
  line-height: 1.8;
  /* 줄 간격 */
  font-family: 'serif';
  /* 가독성 좋은 폰트 */
  color: #3D2C20;
  text-align: justify;
  /* 양쪽 정렬 */
  position: relative;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.1);
  /* 페이지 내부 그림자 */
}

/* Episode Viewer adjustments */
.episode-viewer {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  height: 100%;
  /* 부모 요소의 높이를 채우도록 */
}

.episode-navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.episode-title {
  font-size: 1.5rem;
  font-weight: 600;
  text-align: center;
  flex-grow: 1;
}

.episode-content {
  background-color: transparent;
  /* 페이지 배경색과 동일하게 */
  padding: 0;
  border-radius: 0;
  min-height: auto;
  line-height: inherit;
  white-space: pre-wrap;
}

.episode-content p {
  margin-bottom: 1em;
  /* 단락 간격 */
}

.page-number-container {
  position: absolute;
  bottom: 2rem;
  left: 0;
  right: 0;
  text-align: center;
}

.page-number {
  font-size: 1rem; /* 크기 줄임 */
  font-weight: normal; /* 볼드체 제거 */
}

/* Comments Section */
.comments-section {
  border-top: 1px solid #EAE0D5;
  padding-top: 2rem;
}

.comments-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.comment-input-area {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.comment-input-area textarea {
  flex-grow: 1;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.comment-item {
  background-color: #F5F5DC;
  padding: 1rem;
  border-radius: 8px;
}

.comment-author {
  font-size: 1rem;
  margin-bottom: 0.5rem;
}

.comment-author .comment-date {
  font-size: 0.85rem;
  color: #8B4513;
  margin-left: 0.5rem;
}

.loading-message {
  text-align: center;
  padding: 4rem;
  font-size: 1.2rem;
}
</style>
