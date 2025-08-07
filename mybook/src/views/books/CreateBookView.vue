<template>
  <div class="create-book-page">
    <section class="initial-choice-section text-center">
      <h2 class="section-title">새로운 이야기의 시작</h2>
      <p class="section-subtitle">AI와 함께, 또는 친구들과 함께 세상에 단 하나뿐인 당신의 책을 만들어보세요.</p>
      <div class="choice-cards">
        <div class="choice-card" @click="startNewBook">
          <div class="card-icon"><i class="bi bi-stars"></i></div>
          <h3 class="card-title">새 책 만들기</h3>
          <p class="card-description">AI의 도움을 받아 자서전, 일기, 소설 등 새로운 이야기를 시작합니다.</p>
        </div>
        <div class="choice-card" @click="goToGroupCreate">
          <div class="card-icon"><i class="bi bi-people-fill"></i></div>
          <h3 class="card-title">그룹 책 만들기</h3>
          <p class="card-description">친구, 가족과 함께 교환일기나 여행기 등 특별한 추억을 만듭니다.</p>
        </div>
        <div class="choice-card" @click="continueWriting">
          <div class="card-icon"><i class="bi bi-arrow-down-circle"></i></div>
          <h3 class="card-title">이어 쓰기</h3>
          <p class="card-description">작성 중인 책을 불러와 중단했던 지점부터 다시 시작합니다.</p>
        </div>
      </div>
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
  padding: 80px 2rem 2rem;
  background-color: var(--background-color);
  color: var(--primary-text-color);
  min-height: calc(100vh - 56px);
  font-family: 'Pretendard', sans-serif;
}

.section-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 2.8rem;
  font-weight: 700;
  color: var(--primary-text-color);
  margin-bottom: 0.75rem;
}

.section-subtitle {
  font-size: 1.25rem;
  color: #5C4033;
  margin-bottom: 4rem;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.7;
}

.initial-choice-section {
  max-width: 1200px;
  margin: 0 auto;
}

.choice-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2.5rem;
}

.choice-card {
  background: var(--surface-color);
  border-radius: 12px;
  padding: 2.5rem;
  border: 1px solid var(--border-color);
  box-shadow: 0 4px 15px var(--shadow-color);
  cursor: pointer;
  text-align: center;
  transition: box-shadow 0.3s, border-color 0.3s;
}

.choice-card:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border-color: var(--accent-color);
}

.card-icon {
  font-size: 2.5rem;
  color: var(--accent-color);
  margin-bottom: 1.5rem;
  line-height: 1;
}

.card-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
}

.card-description {
  color: var(--secondary-text-color);
  line-height: 1.6;
}
</style>
