<template>
  <div class="home-container">
    <!-- Hero Section -->
    <section class="hero-section text-center text-white">
      <div class="hero-overlay">
        <h1 class="hero-title">나만의 자서전, 우리들의 이야기</h1>
        <p class="hero-subtitle">AI와 함께 당신의 소중한 순간들을 책으로 엮어보세요.</p>
        <router-link to="/create-book" class="btn btn-primary btn-lg hero-btn">첫 책 만들기 시작</router-link>
      </div>
    </section>

    <!-- Features Section -->
    <section class="features-section">
      <div class="container">
        <div class="row text-center">
          <!-- Feature 1: My Library -->
          <div class="col-lg-4 col-md-6 mb-4">
            <router-link to="/my-library" class="feature-card-link">
              <div class="feature-card h-100">
                <div class="feature-icon">
                  <i class="bi bi-book-half"></i>
                </div>
                <h3 class="feature-title">내 책방 가기</h3>
                <p class="feature-description">내가 쓰고, 좋아요 한 책들을 모아보세요. 그룹 책장도 관리할 수 있습니다.</p>
              </div>
            </router-link>
          </div>

          <!-- Feature 2: Bookstore -->
          <div class="col-lg-4 col-md-6 mb-4">
            <router-link to="/bookstore" class="feature-card-link">
              <div class="feature-card h-100">
                <div class="feature-icon">
                  <i class="bi bi-shop"></i>
                </div>
                <h3 class="feature-title">저잣거리 서점</h3>
                <p class="feature-description">다른 작가들의 책을 읽고, 영감을 얻으세요. 인기, 최신, 검색 기능으로 새로운 이야기를 발견할 수 있습니다.</p>
              </div>
            </router-link>
          </div>

          <!-- Feature 3: Create Book -->
          <div class="col-lg-4 col-md-12 mb-4">
            <router-link to="/create-book" class="feature-card-link">
              <div class="feature-card h-100">
                <div class="feature-icon">
                  <i class="bi bi-pencil-square"></i>
                </div>
                <h3 class="feature-title">책 생성하기</h3>
                <p class="feature-description">AI 인터뷰, 직접 작성, 녹음 등 다양한 방법으로 당신만의, 혹은 그룹의 책을 만들어보세요.</p>
              </div>
            </router-link>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router';
</script>

<style scoped>
.home-container {
  background-color: #F5F5DC; /* Creamy background for the whole page */
  color: #3D2C20; /* Dark brown text */
}

/* Hero Section */
.hero-section {
  position: relative;
  padding: 8rem 0;
  background: url('https://images.unsplash.com/photo-1521587760476-6c12a4b040da?q=80&w=2070&auto=format&fit=crop') no-repeat center center/cover;
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 700;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.hero-subtitle {
  font-size: 1.5rem;
  margin-top: 1rem;
  max-width: 600px;
}

.hero-btn {
  margin-top: 2rem;
  background-color: #B8860B; /* Gold */
  border-color: #B8860B;
  color: #FFFFFF;
  padding: 0.75rem 2rem;
  font-size: 1.2rem;
  font-weight: bold;
  transition: background-color 0.3s, transform 0.3s;
}

.hero-btn:hover {
  background-color: #DAA520; /* Darker Gold */
  transform: translateY(-2px);
}

/* Features Section */
.features-section {
  padding: 5rem 0;
}

.feature-card-link {
  text-decoration: none;
  color: inherit;
}

.feature-card {
  background-color: #FFFFFF; /* White card background */
  border: 1px solid #E0E0E0; /* Light grey border */
  border-radius: 12px;
  padding: 2.5rem;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 3.5rem;
  color: #B8860B; /* Gold icon color */
  margin-bottom: 1.5rem;
}

.feature-title {
  font-size: 1.75rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: #3D2C20;
}

.feature-description {
  font-size: 1rem;
  color: #5C4033; /* Lighter brown text */
  line-height: 1.6;
}

/* Responsive Design */
@media (max-width: 992px) {
  .hero-title {
    font-size: 2.8rem;
  }
  .hero-subtitle {
    font-size: 1.2rem;
  }
  .feature-card {
    padding: 2rem;
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2.2rem;
  }
  .hero-subtitle {
    font-size: 1rem;
  }
  .features-section {
    padding: 3rem 0;
  }
}
</style>
