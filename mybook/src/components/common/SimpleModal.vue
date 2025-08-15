<template>
  <div v-if="isVisible" :class="['modal-overlay', { 'backdrop-blur': useBackdropBlur }]" @click="handleOverlayClick">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h2>{{ title }}</h2>
        <button 
          type="button"
          class="close-button"
          @click="debugClose"
          title="모달 닫기"
        >
          ✕
        </button>
      </div>
      <div class="modal-body">
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
  useBackdropBlur?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  closeOnOverlay: true,
  useBackdropBlur: false
});

const emit = defineEmits<{
  close: [];
}>();

const debugClose = () => {
  console.log('🔥🔥🔥 SimpleModal X 버튼 클릭됨!');
  console.log('emit close 호출 전 - isVisible:', props.isVisible);
  
  try {
    emit('close');
    console.log('✅ emit close 호출 성공!');
  } catch (error) {
    console.error('❌ emit close 호출 실패:', error);
  }
  
  // 추가 검증: emit이 실제로 동작했는지 확인
  setTimeout(() => {
    console.log('emit 후 0.1초 뒤 isVisible:', props.isVisible);
  }, 100);
};

const handleOverlayClick = () => {
  if (props.closeOnOverlay) {
    console.log('🔥🔥🔥 오버레이 클릭으로 모달 닫기');
    debugClose();
  }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  background-color: rgba(0, 0, 0, 0.5) !important;
  display: flex !important;
  justify-content: center !important;
  align-items: center !important;
  z-index: 999999 !important;
}

.modal-overlay.backdrop-blur {
  backdrop-filter: blur(4px);
  background-color: rgba(38, 30, 23, 0.6) !important;
}

.modal-content {
  background-color: white !important;
  border-radius: 15px !important;
  width: 90% !important;
  max-width: 500px !important;
  max-height: 80vh !important;
  overflow: hidden !important;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3) !important;
  animation: modalSlideIn 0.3s ease-out !important;
}

@keyframes modalSlideIn {
  from {
    transform: translateY(-50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  padding: 1.5rem !important;
  border-bottom: 1px solid #e0e0e0 !important;
}

.modal-header h2 {
  margin: 0 !important;
  font-size: 1.5rem !important;
  font-weight: 600 !important;
  color: #333 !important;
}

.close-button {
  background: none !important;
  border: none !important;
  font-size: 1.5rem !important;
  cursor: pointer !important;
  color: #666 !important;
  padding: 0.25rem !important;
  border-radius: 50% !important;
  width: 2rem !important;
  height: 2rem !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  transition: background-color 0.2s ease !important;
}

.close-button:hover {
  color: #333 !important;
}


.modal-body {
  padding: 0 !important;
}
</style>
