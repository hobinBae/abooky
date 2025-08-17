<template>
  <div class="empty-state">
    <div class="empty-icon">
      <i :class="iconClass"></i>
    </div>
    <h3>{{ title }}</h3>
    <p>{{ description }}</p>
    <button v-if="actionText" @click="$emit('action')" class="btn" :class="actionClass">
      {{ actionText }}
    </button>
  </div>
</template>

<script setup lang="ts">
interface Props {
  iconClass: string;
  title: string;
  description: string;
  actionText?: string;
  actionClass?: string;
}

withDefaults(defineProps<Props>(), {
  actionText: '',
  actionClass: 'btn-primary'
});

defineEmits<{
  action: [];
}>();
</script>

<style scoped>
.empty-state {
  text-align: center;
  padding: 2rem 1rem;
  color: #666;
}

.empty-icon {
  font-size: 3rem;
  color: #ddd;
  margin-bottom: 1rem;
}

.empty-state h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.2rem;
  color: #333;
}

.empty-state p {
  margin-bottom: 1.5rem;
  font-size: 0.9rem;
  line-height: 1.5;
}

.btn {
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.2s ease;
}

.btn-primary {
  background-color: #42b983;
  color: white;
}

.btn-primary:hover {
  background-color: #369870;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #5a6268;
}
</style>