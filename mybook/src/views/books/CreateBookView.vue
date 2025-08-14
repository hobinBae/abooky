<template>
  <div class="create-book-page">
    <section class="initial-choice-section">
      <h2 class="section-title">새로운 이야기의 시작</h2>
      <p class="section-subtitle1">AI 도우미 아띠와 함께,</p>
      <p class="section-subtitle2">가족ㆍ친구ㆍ연인과 함께,</p>
      <p class="section-subtitle3">세상에 단 하나뿐인 책을 만들어보세요.</p>

      <section class="choice-section">
        <div class="choice-cards">
          <div class="choice-card" @click="startNewBook">
            <div class="card-icon"><i class="bi bi-book"></i></div>
            <h3 class="card-title">새 책 만들기</h3>
            <p class="card-description">AI의 도움을 받아 자서전, 일기, 소설 등 새로운 이야기를 시작합니다.</p>
          </div>
          <div class="choice-card" @click="goToGroupCreate">
            <div class="card-icon"><i class="bi bi-people"></i></div>
            <h3 class="card-title">그룹 책 만들기</h3>
            <p class="card-description">가족, 친구, 연인과 함께 그룹을 이루고 특별한 추억을 만듭니다.</p>
          </div>
          <div class="choice-card" @click="continueWriting">
            <div class="card-icon"><i class="bi bi-upload"></i></div>
            <h3 class="card-title">이어 쓰기</h3>
            <p class="card-description">작성 중인 책을 불러와 중단했던 지점부터 다시 시작합니다.</p>
          </div>
        </div>
      </section>

    </section>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

function startNewBook() {
  router.push('/book-editor');
}

function goToGroupCreate() {
  router.push('/group-book-lobby');
}

function continueWriting() {
  router.push('/continue-writing');
}
</script>

<style scoped>
/* --- Google Fonts Import --- */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&family=Pretendard:wght@400;500;700&display=swap');

/* --- 색상 변수 --- */
:root {
  --background-color: #F5F5DC;
  --surface-color: #FFFFFF;
  --primary-text-color: #3D2C20;
  --secondary-text-color: #6c757d;
  --accent-color: #8B4513;
  --border-color: #EAE0D5;
  --shadow-color: rgba(0, 0, 0, 0.06);
}

.create-book-page {
  padding: 1.6em 1.6rem 3.2rem 1.6rem;
  background-color: var(--background-color);
  color: var(--primary-text-color);
  min-height: calc(100vh - 56px);
  /* font-family: 'Pretendard', sans-serif; */
}

.section-title {
  font-family: 'SCDream3', serif;
  font-size: 3.2rem;
  font-weight: 700;
  color: var(--primary-text-color);
  margin-bottom: 0rem;
  margin-left: 2.4rem;
  margin-right: auto;
}

.section-subtitle1 {
  font-family: 'SCDream4', serif;
  font-size: 2.4rem;
  color: rgba(116, 125, 76, 0.9);
  margin-left: 2.8rem;
  margin-right: auto;
  margin-bottom: -0.4rem;
}

.section-subtitle2 {
  font-family: 'SCDream4', serif;
  font-size: 2.4rem;
  color: rgba(141, 153, 109, 0.7);
  margin-left: 2.8rem;
  margin-right: auto;
  margin-bottom: -0.4rem;

}

.section-subtitle3 {
  font-family: 'SCDream4', serif;
  font-size: 2.4rem;
  color: rgba(147, 161, 89, 0.4);
  margin-left: 2.8rem;
  margin-right: auto;
  margin-bottom: 4rem;
}

.initial-choice-section {
  max-width: 960px;
  margin: 0 auto;
}

.choice-section {
   max-width: 960px;
}

.choice-cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 280px)); /* 3개의 열, 각 열의 최대 너비 280px */
  gap: 2rem; /* 카드 사이 간격 조정 */
  justify-content: center; /* 카드들을 중앙에 정렬 */
}

.choice-card {
  background: var(--surface-color);
  border-radius: 40px;
  padding: 2rem;
  border: 3px solid #657143;
  box-shadow: 0 4px 15px var(--shadow-color);
  cursor: pointer;
  text-align: center;
  transition: color 0.4s ease, box-shadow 0.3s;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.choice-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(138, 154, 91, 0.4);
  transform-origin: top;
  transform: scaleY(0);
  transition: transform 0.5s ease-in-out;
  z-index: -1;
}

.choice-card:hover::before {
  transform-origin: bottom;
  transform: scaleY(1);
}

.choice-card:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  color: white;
  /* 텍스트 색상을 변경하여 가독성 확보 */
}

.card-icon {
  font-size: 2.4rem;
  color: var(--accent-color);
  margin-bottom: 0.8rem;
  line-height: 1;
}

.card-title {
  font-family: 'EBSHunminjeongeumSaeronL', serif;
  font-size: 1.4rem;
  font-weight: 600;
  margin-bottom: 0.7rem;
}

.card-description {
  font-family: 'SCDream4', serif;
  color: var(--secondary-text-color);
  line-height: 1.6;
}

/* --- 반응형 디자인 --- */
@media (max-width: 1200px) {
  .section-title {
    font-size: 2.8rem;
    margin-left: 1.6rem;
  }
  
  .section-subtitle1,
  .section-subtitle2,
  .section-subtitle3 {
    font-size: 2rem;
    margin-left: 2rem;
  }
  
  .choice-cards {
    grid-template-columns: repeat(3, minmax(0, 240px));
    gap: 1.6rem;
  }
}

@media (max-width: 992px) {
  .create-book-page {
    padding: 1.2rem;
  }
  
  .section-title {
    font-size: 2.4rem;
    margin-left: 1.2rem;
  }
  
  .section-subtitle1,
  .section-subtitle2,
  .section-subtitle3 {
    font-size: 1.6rem;
    margin-left: 1.6rem;
    margin-bottom: -0.2rem;
  }
  
  .section-subtitle3 {
    margin-bottom: 2.4rem;
  }
  
  .choice-cards {
    grid-template-columns: repeat(2, minmax(0, 224px));
    gap: 1.2rem;
  }
  
  .choice-card {
    padding: 1.6rem;
  }
}

@media (max-width: 768px) {
  .create-book-page {
    padding: 0.8rem;
  }
  
  .section-title {
    font-size: 2rem;
    margin-left: 0.8rem;
    text-align: center;
  }
  
  .section-subtitle1,
  .section-subtitle2,
  .section-subtitle3 {
    font-size: 1.2rem;
    margin-left: 0.8rem;
    text-align: center;
  }
  
  .choice-cards {
    grid-template-columns: 1fr;
    gap: 1.2rem;
    max-width: 320px;
    margin: 0 auto;
  }
  
  .choice-card {
    padding: 1.2rem;
  }
  
  .card-icon {
    font-size: 2rem;
  }
  
  .card-title {
    font-size: 1.2rem;
  }
  
  .card-description {
    font-size: 0.7rem;
  }
}

@media (max-width: 480px) {
  .section-title {
    font-size: 1.6rem;
  }
  
  .section-subtitle1,
  .section-subtitle2,
  .section-subtitle3 {
    font-size: 1rem;
  }
  
  .choice-card {
    padding: 0.8rem;
    border-radius: 20px;
  }
  
  .card-icon {
    font-size: 1.6rem;
  }
  
  .card-title {
    font-size: 1rem;
  }
  
  .card-description {
    font-size: 0.7rem;
  }
}
</style>
