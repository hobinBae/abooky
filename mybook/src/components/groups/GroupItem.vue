<template>
  <div class="group-item" @click="$emit('select', group)">
    <div class="group-avatar" :style="{ backgroundColor: group.themeColor || '#42b983' }">
      <img 
        v-if="group.groupImageUrl" 
        :src="group.groupImageUrl" 
        :alt="group.groupName"
        class="group-image"
      />
      <i v-else class="bi bi-people-fill"></i>
    </div>
    <div class="group-info">
      <h3>{{ group.groupName }}</h3>
      <p v-if="group.description" class="group-description">{{ group.description }}</p>
      <div class="group-details">
        <span class="leader-info">
          <i class="bi bi-crown-fill me-1"></i>
          방장: {{ group.leaderNickname }}
        </span>
        <span class="created-date">{{ formatDate(group.createdAt) }} 생성</span>
      </div>
      <div v-if="isOwner" class="group-badge owner-badge">
        <i class="bi bi-crown-fill"></i>
        <span>방장</span>
      </div>
    </div>
    <div class="group-arrow">
      <i class="bi bi-chevron-right"></i>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
interface Group {
  groupId: number;
  groupName: string;
  description: string;
  themeColor: string;
  groupImageUrl: string;
  leaderId: number;
  leaderNickname: string;
  createdAt: string;
  updatedAt: string;
  members?: string[];
}

interface Props {
  group: Group;
  currentUserId?: number;
}

const props = defineProps<Props>();

defineEmits<{
  select: [group: Group];
}>();

const isOwner = computed(() => {
  return props.group.leaderId === props.currentUserId;
});

const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};
</script>

<style scoped>
.group-item {
  display: flex;
  align-items: center;
  padding: 1rem 1.5rem;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}

.group-item:hover {
  background-color: #f8f9fa;
}

.group-item:last-child {
  border-bottom: none;
}

.group-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1rem;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
}

.group-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.group-avatar i {
  color: white;
  font-size: 1.5rem;
}

.group-info {
  flex-grow: 1;
  text-align: left;
}

.group-info h3 {
  margin: 0 0 0.25rem 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
}

.group-description {
  margin: 0 0 0.5rem 0;
  font-size: 0.85rem;
  color: #666;
  line-height: 1.4;
}

.group-details {
  margin-top: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.leader-info {
  font-size: 0.8rem;
  color: #42b983;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.leader-info i {
  color: #ffd700;
  margin-right: 0.25rem;
}

.created-date {
  font-size: 0.8rem;
  color: #888;
  font-style: italic;
}

.group-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.2rem 0.6rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  margin-top: 0.5rem;
}

.owner-badge {
  background-color: #ffd700;
  color: #8b6914;
}

.group-arrow {
  color: #ccc;
  font-size: 1rem;
  margin-left: 1rem;
  flex-shrink: 0;
}

.group-item:hover .group-arrow {
  color: #42b983;
}

@media (max-width: 768px) {
  .group-item {
    padding: 0.75rem 1rem;
  }
  
  .group-avatar {
    width: 40px;
    height: 40px;
    margin-right: 0.75rem;
  }
  
  .group-avatar i {
    font-size: 1.25rem;
  }
  
  .group-info h3 {
    font-size: 1rem;
  }
  
  .group-description {
    font-size: 0.8rem;
  }
}
</style>