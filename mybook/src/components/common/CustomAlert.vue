<template>
  <div v-if="isVisible" class="custom-alert-overlay">
    <div class="custom-alert-box">
      <h2>{{ currentTitle }}</h2>
      <p>{{ currentMessage }}</p>
      <div class="alert-actions">
        <button v-if="isConfirm" @click="handleCancel" class="cancel-btn">{{ cancelButtonText }}</button>
        <button @click="handleConfirm" class="confirm-btn">{{ confirmButtonText }}</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref,defineEmits } from 'vue';

const emit = defineEmits(['alert-closed']);

const props = defineProps({
  confirmButtonText: {
    type: String,
    default: '확인',
  },
  cancelButtonText: {
    type: String,
    default: '취소',
  },
});

const isVisible = ref(false);
const isConfirm = ref(false); // To distinguish between alert and confirm
const currentTitle = ref('알림');
const currentMessage = ref('');
const confirmButtonText = ref(props.confirmButtonText);
const cancelButtonText = ref(props.cancelButtonText);

let resolvePromise: ((value: boolean) => void) | null = null;

interface ShowAlertOptions {
  title?: string;
  message: string;
  confirmButtonText?: string;
  cancelButtonText?: string;
}

const showAlert = (options: ShowAlertOptions) => {
  currentTitle.value = options.title || '알림';
  currentMessage.value = options.message;
  isConfirm.value = false;
  isVisible.value = true;
};

const showConfirm = (options: ShowAlertOptions): Promise<boolean> => {
  currentTitle.value = options.title || '확인';
  currentMessage.value = options.message;
  confirmButtonText.value = options.confirmButtonText || '확인';
  cancelButtonText.value = options.cancelButtonText || '취소';
  isConfirm.value = true;
  isVisible.value = true;
  return new Promise<boolean>((resolve) => {
    resolvePromise = resolve;
  });
};

const handleConfirm = () => {
  isVisible.value = false;
  if (isConfirm.value && resolvePromise) {
    resolvePromise(true);
  } else {
    emit('alert-closed');
  }
  resolvePromise = null;
};

const handleCancel = () => {
  isVisible.value = false;
  if (isConfirm.value && resolvePromise) {
    resolvePromise(false);
  }
  emit ('alert-closed');
  resolvePromise = null;
};

defineExpose({
  showAlert,
  showConfirm,
});
</script>

<style scoped>
.custom-alert-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6); /* Darker overlay */
  z-index: 9999; /* Ensure it's always on top */
  font-family: '', sans-serif; /* Apply font */
}

.custom-alert-box {
  position: absolute; /* Position relative to the overlay */
  top: 50%; /* Center vertically */
  left: 50%;
  transform: translate(-50%, -50%); /* Center horizontally and vertically */
  background: #ffffff; /* Light olive background from LoginView */
  padding: 30px; /* More padding */
  border-radius: 10px; /* Rounded corners from LoginView */
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.4); /* Stronger shadow */
  text-align: center;
  max-width: 500px; /* Max width for responsiveness */
  width: 90%; /* Responsive width */
  color: #333; /* Dark text color */
  display: flex; /* Enable flexbox */
  flex-direction: column; /* Stack children vertically */
  justify-content: space-between; /* Distribute space between items */
}
.custom-alert-box h2 {
  margin-top: 0;
  font-size: 1.5rem; /* Larger title */
  font-weight: 600;
  color: #333; /* Dark text color */
  margin-bottom: 15px;
}

.custom-alert-box p {
  font-size: 1rem;
  font-weight: 600;

  color: #666; /* Slightly lighter text color */
  margin-bottom: 30px; /* More space before button */
  line-height: 1.5;
}

.custom-alert-box button {
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 15px;
  border: 1.5px solid #000000;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  transition: background-color 0.3s ease;
}

.alert-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 20px;
}

.confirm-btn {
  background-color: #d9534f; /* Red for delete/confirm */
}

.confirm-btn:hover {
  background-color: #c9302c;
}

.cancel-btn {
  background-color: #8b8b8b;
}

.cancel-btn:hover {
  background-color: #464646;
}
</style>
