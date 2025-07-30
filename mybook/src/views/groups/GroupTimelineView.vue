<template>
  <div class="group-timeline-page">
    <div v-if="group" class="timeline-container">
      <header class="timeline-header">
        <h1>{{ group.groupName }} 타임라인</h1>
        <p>우리 그룹의 소중한 순간들과 이야기의 흐름을 확인해보세요.</p>
      </header>

      <!-- Add Event Section -->
      <section class="add-event-section">
        <h3>새로운 타임라인 이벤트 추가</h3>
        <div class="event-form">
          <input type="date" v-model="newEvent.date" class="form-control">
          <input type="text" v-model="newEvent.title" placeholder="이벤트 제목" class="form-control">
          <textarea v-model="newEvent.description" placeholder="이벤트 내용" class="form-control"></textarea>
          <button @click="addTimelineEvent" class="btn btn-primary">이벤트 추가</button>
        </div>
      </section>

      <!-- Timeline -->
      <div class="timeline">
        <div v-for="event in sortedTimeline" :key="event.id" class="timeline-item">
          <div class="timeline-icon">
            <i :class="event.icon || 'bi bi-calendar-event'"></i>
          </div>
          <div class="timeline-content">
            <span class="timeline-date">{{ new Date(event.date).toLocaleDateString() }}</span>
            <h4 class="timeline-title">{{ event.title }}</h4>
            <p class="timeline-description">{{ event.description }}</p>
            <router-link v-if="event.episodeLink" :to="event.episodeLink" class="episode-link">
              관련 에피소드 보기
            </router-link>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="loading-message">
      <p>타임라인 정보를 불러오는 중입니다...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, RouterLink } from 'vue-router';

// --- Interfaces ---
interface Group {
  id: string;
  groupName: string;
}

interface TimelineEvent {
  id: string;
  date: string;
  title: string;
  description: string;
  icon?: string;
  episodeLink?: string;
  groupId: string; // ← 추가
}

// --- Dummy Data ---
const DUMMY_GROUPS: Group[] = [
  { id: 'group1', groupName: '독서 토론 모임' },
  { id: 'group2', groupName: '글쓰기 동호회' },
  { id: 'group3', groupName: '여행 에세이 클럽' },
  { id: 'group4', groupName: '코딩 스터디' },
];

const DUMMY_TIMELINE_EVENTS: TimelineEvent[] = [
  { id: 't1', groupId: 'group1', date: '2024-01-01', title: '그룹 결성', description: '새로운 독서 토론 모임이 시작되었습니다.', icon: 'bi-star-fill' },
  { id: 't2', groupId: 'group1', date: '2024-01-15', title: '첫 토론 주제 선정', description: `'데미안'을 첫 토론 도서로 선정했습니다.`, icon: 'bi-lightbulb-fill', episodeLink: '/book-detail/b1' },
  { id: 't3', groupId: 'group2', date: '2024-02-01', title: '글쓰기 워크숍', description: `'나만의 에세이 쓰기' 워크숍을 진행했습니다.`, icon: 'bi-pencil-square' },
  { id: 't4', groupId: 'group1', date: '2024-03-10', title: '정기 모임', description: `'나의 첫 유럽 여행기'에 대한 심도 깊은 토론을 진행했습니다.`, icon: 'bi-book-fill', episodeLink: '/book-detail/b1' },
  { id: 't5', groupId: 'group3', date: '2024-04-05', title: '여행 계획 공유', description: '다음 여행지인 제주도에 대한 계획을 공유했습니다.', icon: 'bi-geo-alt-fill' },
];

// --- Router ---
const route = useRoute();
const groupId = computed(() => route.params.id as string);

// --- Reactive State ---
const group = ref<Group | null>(null);
const timeline = ref<TimelineEvent[]>([]);
const newEvent = ref({
  date: new Date().toISOString().split('T')[0], // Today's date
  title: '',
  description: '',
});

// --- Computed Properties ---
const sortedTimeline = computed(() => {
  return [...timeline.value].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
});

// --- Functions ---
function fetchGroupInfo() {
  const foundGroup = DUMMY_GROUPS.find(g => g.id === groupId.value);
  if (foundGroup) {
    group.value = foundGroup;
  } else {
    console.error('Group not found for ID:', groupId.value);
    // Optionally, redirect or show error
  }
}

function setupTimelineListener() {
  // Filter dummy events by current groupId
  timeline.value = DUMMY_TIMELINE_EVENTS.filter(event => event.groupId === groupId.value);
}

function addTimelineEvent() {
  if (!newEvent.value.title || !newEvent.value.description) {
    alert('이벤트 제목과 내용을 모두 입력해주세요.');
    return;
  }

  const newId = `t${DUMMY_TIMELINE_EVENTS.length + 1}`;
  const event: TimelineEvent = {
    id: newId,
    groupId: groupId.value, // Assign to current group
    date: newEvent.value.date,
    title: newEvent.value.title,
    description: newEvent.value.description,
    icon: 'bi-plus-circle', // Default icon for new events
  };
  timeline.value.unshift(event); // Add to the beginning for display
  DUMMY_TIMELINE_EVENTS.push(event); // Add to dummy data source

  // Reset form
  newEvent.value = {
    date: new Date().toISOString().split('T')[0],
    title: '',
    description: '',
  };
  alert('이벤트가 추가되었습니다.');
}

// --- Lifecycle Hooks ---
onMounted(() => {
  fetchGroupInfo();
  setupTimelineListener();
});
</script>

<style scoped>
.group-timeline-page {
  padding: 80px 2rem 2rem;
  background-color: #F5F5DC;
  color: #3D2C20;
  min-height: calc(100vh - 56px);
}

.timeline-container {
  max-width: 800px;
  margin: 0 auto;
}

.timeline-header {
  text-align: center;
  margin-bottom: 3rem;
}

.timeline-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
}

.timeline-header p {
  font-size: 1.1rem;
  color: #5C4033;
}

/* Add Event Section */
.add-event-section {
  background: #fff;
  padding: 2rem;
  border-radius: 12px;
  margin-bottom: 3rem;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
}

.add-event-section h3 {
  text-align: center;
  margin-bottom: 1.5rem;
}

.event-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.event-form .form-control {
  background-color: #F5F5DC;
  border-color: #B8860B;
}

.event-form textarea {
  grid-column: 1 / -1;
  height: 100px;
}

.event-form button {
  grid-column: 1 / -1;
  background-color: #B8860B;
  border-color: #B8860B;
}

/* Timeline */
.timeline {
  position: relative;
  padding: 1rem 0;
}

.timeline::before {
  content: '';
  position: absolute;
  top: 0;
  left: 18px;
  height: 100%;
  width: 4px;
  background: #D2B48C;
}

.timeline-item {
  position: relative;
  margin-bottom: 2rem;
}

.timeline-icon {
  position: absolute;
  left: 0;
  top: 0;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #B8860B;
  color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.5rem;
  border: 3px solid #F5F5DC;
}

.timeline-content {
  margin-left: 60px;
  background: #fff;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.timeline-date {
  font-size: 0.9rem;
  color: #8B4513;
  font-weight: 600;
  margin-bottom: 0.5rem;
  display: block;
}

.timeline-title {
  font-size: 1.4rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.episode-link {
  margin-top: 1rem;
  display: inline-block;
  color: #B8860B;
  font-weight: bold;
  text-decoration: none;
}

.loading-message {
  text-align: center;
  padding: 4rem;
  font-size: 1.2rem;
}
</style>
