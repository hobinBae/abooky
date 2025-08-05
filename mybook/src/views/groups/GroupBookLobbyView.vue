<template>
  <div class="lobby-container">
    <div class="lobby-content">
      <div class="lobby-header">
        <h1>그룹 책 만들기</h1>
        <p>새로운 그룹 책을 만들거나 기존 그룹 책에 참여하세요.</p>
      </div>
      <div class="button-container">
        <button class="lobby-button create-button" @click="goToCreate">
          <h2>그룹책 방 만들기</h2>
          <p>새로운 그룹 책을 만들어보세요.</p>
        </button>
        <button class="lobby-button join-button" @click="goToJoin">
          <h2>그룹책 방 참여하기</h2>
          <p>초대 코드로 그룹 책에 참여하세요.</p>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

const goToCreate = () => {
  router.push({ name: 'group-book-creation' });
};

const goToJoin = () => {
  // 참여 로직 구현 필요
  // 예: router.push({ name: 'GroupBookJoin' });
};
</script>

<style scoped>
.lobby-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 120px); /* Navbar and Footer height */
  background-color: #f4f4f9;
  padding: 2rem;
}

.lobby-content {
  text-align: center;
  background-color: white;
  padding: 3rem;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  max-width: 900px;
  width: 100%;
}

.lobby-header {
  margin-bottom: 3rem;
}

.lobby-header h1 {
  font-size: 2.5rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 0.5rem;
}

.lobby-header p {
  font-size: 1.2rem;
  color: #666;
}

.button-container {
  display: flex;
  justify-content: space-around;
  gap: 2rem;
}

.lobby-button {
  flex: 1;
  padding: 2rem;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  background-color: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.lobby-button:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.15);
}

.lobby-button h2 {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
}

.lobby-button p {
  font-size: 1rem;
  color: #555;
}

.create-button {
  border: 2px solid #42b983;
  color: #42b983;
}

.create-button:hover {
  background-color: #42b983;
  color: white;
}

.join-button {
  border: 2px solid #3498db;
  color: #3498db;
}

.join-button:hover {
  background-color: #3498db;
  color: white;
}
</style>
