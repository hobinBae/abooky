<template>
  <div class="session-item" @click="$emit('join', session)">
    <div class="session-avatar">
      <i class="bi bi-book-fill"></i>
    </div>
    <div class="session-info">
      <h3>{{ session.groupName }}</h3>
      <p class="session-host">방장: {{ session.hostName }}</p>
      <div class="session-details">
        <span class="participant-count">
          <i class="bi bi-people-fill me-1"></i>
          {{ session.participantCount }}명 참여 중
        </span>
        <span class="session-time">{{ getTimeAgo(session.startedAt) }}</span>
      </div>
      <div class="session-status">
        <span class="status-badge active-badge">
          <i class="bi bi-circle-fill"></i>
          활성화
        </span>
      </div>
    </div>
    <div class="session-arrow">
      <i class="bi bi-chevron-right"></i>
    </div>
  </div>
</template>

<script setup lang="ts">
interface ActiveSession {
  groupId: number;
  groupName: string;
  hostName: string;
  startedAt: Date;
  participantCount: number;
}

interface Props {
  session: ActiveSession;
}

defineProps<Props>();

defineEmits<{
  join: [session: ActiveSession];
}>();

const getTimeAgo = (date: Date) => {
  const now = new Date();
  const diffInMinutes = Math.floor((now.getTime() - date.getTime()) / (1000 * 60));
  
  if (diffInMinutes < 1) return '방금 전';
  if (diffInMinutes < 60) return `${diffInMinutes}분 전`;
  
  const diffInHours = Math.floor(diffInMinutes / 60);
  if (diffInHours < 24) return `${diffInHours}시간 전`;
  
  const diffInDays = Math.floor(diffInHours / 24);
  return `${diffInDays}일 전`;
};
</script>

<style scoped>
.session-item {
  display: flex;
  align-items: center;
  padding: 1rem 1.5rem;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}

.session-item:hover {
  background-color: #f8f9fa;
}

.session-item:last-child {
  border-bottom: none;
}

.session-avatar {
  width: 50px;
  height: 50px;
  background-color: #3498db;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1rem;
  flex-shrink: 0;
}

.session-avatar i {
  color: white;
  font-size: 1.5rem;
}

.session-info {
  flex-grow: 1;
  text-align: left;
}

.session-info h3 {
  margin: 0 0 0.25rem 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
}

.session-host {
  margin: 0 0 0.5rem 0;
  font-size: 0.9rem;
  color: #666;
  line-height: 1.4;
}

.session-details {
  margin-bottom: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.participant-count {
  font-size: 0.85rem;
  color: #3498db;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.session-time {
  font-size: 0.8rem;
  color: #888;
  font-style: italic;
}

.session-status {
  display: flex;
  align-items: center;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.2rem 0.6rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
}

.active-badge {
  background-color: #d4edda;
  color: #155724;
}

.active-badge i {
  font-size: 0.6rem;
  color: #28a745;
}

.session-arrow {
  color: #ccc;
  font-size: 1rem;
  margin-left: 1rem;
  flex-shrink: 0;
}

.session-item:hover .session-arrow {
  color: #3498db;
}
</style>