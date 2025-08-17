<template>
  <div class="about-page">
    <!-- Hero Section -->
    <section class="hero-section text-center">
      <div class="container">
        <h1 class="hero-title">당신의 이야기를 영원히 간직하세요</h1>
        <p class="hero-subtitle">MyBook은 AI와 함께 당신의 삶, 생각, 경험을 아름다운 책으로 만들어주는 특별한 공간입니다.</p>
        <router-link to="/create-book" class="btn btn-primary btn-lg">지금, 첫 페이지 작성하기</router-link>
      </div>
    </section>

    <!-- Features Section -->
    <section class="features-section">
      <div class="container">
        <div class="row">
          <div class="col-md-4 feature-item">
            <div class="feature-icon"><i class="bi bi-robot"></i></div>
            <h3 class="feature-title">AI와의 대화로 책 만들기</h3>
            <p>AI 작가 '한석봉'과 대화하듯 질문에 답하다 보면, 어느새 당신의 이야기가 한 편의 글로 완성됩니다. 글쓰기가 막막했던 분들도 쉽게 시작할 수 있습니다.</p>
          </div>
          <div class="col-md-4 feature-item">
            <div class="feature-icon"><i class="bi bi-people-fill"></i></div>
            <h3 class="feature-title">실시간 공동 집필</h3>
            <p>가족, 친구, 동료와 함께 화상으로 대화하며 하나의 책을 만들어보세요. 각자의 소중한 기억을 모아 그룹의 역사를 기록할 수 있습니다.</p>
          </div>
          <div class="col-md-4 feature-item">
            <div class="feature-icon"><i class="bi bi-cloud-arrow-down-fill"></i></div>
            <h3 class="feature-title">영원히 보관되는 당신의 역사</h3>
            <p>완성된 책은 안전한 클라우드에 영구히 보관됩니다. 언제 어디서든 당신의 이야기를 열람하고, 소중한 사람들과 공유하세요.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- How It Works Section -->
    <section class="how-it-works-section">
      <div class="container">
        <h2 class="section-title">MyBook, 이렇게 만들어집니다</h2>
        <div class="timeline">
          <div class="timeline-item"><strong>1단계:</strong> AI 인터뷰 또는 직접 작성으로 이야기 시작</div>
          <div class="timeline-item"><strong>2단계:</strong> AI가 대화 내용을 바탕으로 초고 작성 및 정리</div>
          <div class="timeline-item"><strong>3단계:</strong> 사진, 음성 파일 등을 추가하여 에피소드 완성</div>
          <div class="timeline-item"><strong>4단계:</strong> 표지 이미지 생성 및 책 발행</div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router';
</script>

<style scoped>
.about-page {
  padding-top: 60px;
  background-color: #F5F5DC;
  color: #3D2C20;
}

.hero-section {
  background-color: #5C4033;
  color: #F5F5DC;
  padding: 5rem 0;
}

.hero-title {
  font-size: 3rem;
  font-weight: 700;
}

.hero-subtitle {
  font-size: 1.2rem;
  max-width: 600px;
  margin: 1rem auto 2rem auto;
}

.btn-primary {
  background-color: #B8860B;
  border-color: #B8860B;
  font-weight: bold;
}

.features-section, .how-it-works-section {
  padding: 5rem 0;
}

.feature-item {
  text-align: center;
  padding: 0 2rem;
}

.feature-icon {
  font-size: 3.5rem;
  color: #B8860B;
  margin-bottom: 1.5rem;
}

.feature-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.section-title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 3rem;
}

.timeline {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 2px;
  background-color: #D2B48C;
  transform: translateX(-50%);
}

.timeline-item {
  padding: 1rem 2rem;
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 1rem;
  width: 50%;
  position: relative;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.timeline-item:nth-child(odd) {
  left: 0;
  transform: translateX(-20px);
}

.timeline-item:nth-child(even) {
  left: 50%;
  transform: translateX(20px);
}

.timeline-item::after {
  content: '';
  position: absolute;
  top: 20px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: #B8860B;
  border: 3px solid #F5F5DC;
}

.timeline-item:nth-child(odd)::after {
  right: -10px;
  transform: translateX(50%);
}

.timeline-item:nth-child(even)::after {
  left: -10px;
  transform: translateX(-50%);
}

</style>
