<template>
  <div v-show="isVisible" class="simple-modal-overlay" @click="handleOverlayClick">
    <div class="simple-modal-content" @click.stop>
      <div class="simple-modal-header">
        <h2>{{ title }}</h2>
        <span class="simple-close-btn" @click="handleClose">&times;</span>
      </div>
      <div class="simple-modal-body">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  isVisible: boolean;
  title: string;
  closeOnOverlay?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  closeOnOverlay: true
});

const emit = defineEmits<{
  close: [];
}>();

const handleClose = () => {
  emit('close');
};

const handleOverlayClick = () => {
  if (props.closeOnOverlay) {
    handleClose();
  }
};
</script>

<style scoped>
.simple-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.simple-modal-content {
  background-color: white;
  border-radius: 15px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.simple-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e0e0e0;
}

.simple-modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
}

.simple-close-btn {
  font-size: 2rem;
  cursor: pointer;
  color: #666;
  line-height: 1;
  user-select: none;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s ease;
}

.simple-close-btn:hover {
  background-color: #f0f0f0;
  color: #333;
}

.simple-modal-body {
  padding: 0;
  overflow-y: auto;
  max-height: calc(80vh - 100px);
}

@media (max-width: 768px) {
  .simple-modal-content {
    width: 95%;
    margin: 1rem;
  }
}
</style>