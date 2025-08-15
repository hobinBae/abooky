<template>
  <div class="group-item" @click="$emit('select', group)">
    <div class="group-info">
      <div class="group-name-section">
        <h3>{{ group.groupName }}</h3>
      </div>
    </div>
    <div class="group-arrow">
      <i class="bi bi-chevron-right"></i>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Group } from '@/services/groupService';

interface Props {
  group: Group;
  currentUserId?: number;
  isActive?: boolean;
}

defineProps<Props>();

defineEmits<{
  select: [group: Group];
}>();
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

.group-name-section {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.25rem;
}

.group-name-section h3 {
  margin: 0;
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

  .group-info h3 {
    font-size: 1rem;
  }
}
</style>
