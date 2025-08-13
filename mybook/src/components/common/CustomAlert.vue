<template>
  <div v-if="isVisible" class="custom-alert-overlay">
    <div class="custom-alert-box">
      <h2>{{ currentTitle }}</h2>
      <p>{{ currentMessage }}</p>
      <button @click="closeAlert">{{ buttonText }}</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const props = defineProps({
  buttonText: {
    type: String,
    default: '확인',
  },
});

const emits = defineEmits(['alert-opened', 'alert-closed']);

const isVisible = ref(false);
const currentTitle = ref('알림');
const currentMessage = ref('');

interface ShowAlertOptions {
  title?: string;
  message: string;
}

const showAlert = (options: ShowAlertOptions) => {
  currentTitle.value = options.title || '알림';
  currentMessage.value = options.message;
  isVisible.value = true;
  emits('alert-opened');
};

const closeAlert = () => {
  isVisible.value = false;
  emits('alert-closed');
};

defineExpose({
  showAlert,
  closeAlert,
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
  background-color: #8b8b8b; /* Olive green button from LoginView */
  color: white;
  border: none;
  padding: 4px 15px; /* Larger button padding */
  border-radius: 15px; /* Rounded button corners */
  border: 1.5px solid #000000; /* Olive border from LoginView */

  cursor: pointer;
  font-size: 1rem; /* Consistent font size */
  font-weight: 500;
  transition: background-color 0.3s ease;
  margin-top: auto; /* Push button to the bottom */
}

.alert-actions {
  text-align: right;
  margin-top: 20px; /* Add some space above the button */
}

.custom-alert-box button:hover {
  background-color: #464646; /* Darker olive on hover */
}
</style>
