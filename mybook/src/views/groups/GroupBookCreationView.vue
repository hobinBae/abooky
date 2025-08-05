<template>
  <div class="page-container group-book-creation-page">
    <div v-if="!hasJoined" class="lobby-container">
      <div class="lobby-card">
        <h1 class="lobby-title">화면 미리보기</h1>
        <p class="lobby-subtitle">입장하기 전, 카메라와 마이크 상태를 확인해 주세요.</p>
        <div class="video-preview-container">
          <video ref="localVideo" autoplay muted playsinline class="video-preview"></video>
          <div class="media-controls">
            <button @click="toggleAudio" class="btn-media" :class="{ 'is-muted': !isAudioEnabled }">
              <i class="bi" :class="isAudioEnabled ? 'bi-mic-fill' : 'bi-mic-mute-fill'"></i>
            </button>
            <button @click="toggleVideo" class="btn-media" :class="{ 'is-muted': !isVideoEnabled }">
              <i class="bi" :class="isVideoEnabled ? 'bi-camera-video-fill' : 'bi-camera-video-off-fill'"></i>
            </button>
          </div>
        </div>
        <button @click="joinSession" class="btn btn-primary btn-join">그룹책 만들기 입장</button>
      </div>
    </div>

    <div v-else class="workspace-container single-content">
      <div class="card video-grid-card expanded">
        <h3 class="card-header">참여자</h3>
        <div class="video-grid large" :style="{ gridTemplateColumns: getGridTemplate }">
          <div class="video-participant">
            <video :srcObject="localStream" autoplay muted playsinline class="participant-video"></video>
            <div class="participant-name">
              <i class="bi me-1" :class="isAudioEnabled ? 'bi-mic-fill' : 'bi-mic-mute-fill'"></i>
              나 (You)
            </div>
          </div>
          <div v-for="p in remoteParticipants" :key="p.id" class="video-participant">
            <div class="participant-video-placeholder">{{ p.name.charAt(0) }}</div>
            <div class="participant-name">
              <i class="bi me-1" :class="p.isAudioEnabled ? 'bi-mic-fill' : 'bi-mic-mute-fill'"></i>
              {{ p.name }}
            </div>
          </div>
        </div>
        <div class="main-controls">
          <button @click="toggleAudio" class="btn btn-control" :class="{ 'is-muted': !isAudioEnabled }">
            <i class="bi" :class="isAudioEnabled ? 'bi-mic-fill' : 'bi-mic-mute-fill'"></i>
            <span>{{ isAudioEnabled ? '음소거' : '음소거 해제' }}</span>
          </button>
          <button @click="toggleVideo" class="btn btn-control" :class="{ 'is-muted': !isVideoEnabled }">
            <i class="bi" :class="isVideoEnabled ? 'bi-camera-video-fill' : 'bi-camera-video-off-fill'"></i>
            <span>{{ isVideoEnabled ? '비디오 중지' : '비디오 시작' }}</span>
          </button>
           <button @click="saveAndExit" class="btn btn-control btn-leave">
            <i class="bi bi-box-arrow-right"></i>
            <span>나가기</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

// --- Mock/Placeholder Data & Interfaces ---
interface RemoteParticipant {
  id: string;
  name: string;
  stream?: MediaStream;
  isAudioEnabled: boolean;
}

// --- Router ---
const route = useRoute();
const router = useRouter();
const groupId = route.query.groupId as string;

// --- Reactive State ---
const hasJoined = ref(false);
const localVideo = ref<HTMLVideoElement | null>(null);
const localStream = ref<MediaStream | null>(null);
const isAudioEnabled = ref(true);
const isVideoEnabled = ref(true);
const remoteParticipants = ref<RemoteParticipant[]>([{ id: 'user2', name: '김철수', isAudioEnabled: true }, { id: 'user3', name: '이영희', isAudioEnabled: false }, { id: 'user4', name: '김아윤', isAudioEnabled: true }]);

// --- WebRTC & Media Functions ---
async function setupLocalMedia() {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
    localStream.value = stream;
    if (localVideo.value) {
      localVideo.value.srcObject = stream;
    }
  } catch (error) {
    console.error('미디어 장치에 접근할 수 없습니다:', error);
    // 실제 카메라 없을 때를 대비한 목업 스트림
    if (localVideo.value) {
      const canvas = document.createElement('canvas');
      canvas.width = 640;
      canvas.height = 480;
      const ctx = canvas.getContext('2d');
      if (ctx) {
        ctx.fillStyle = '#333';
        ctx.fillRect(0, 0, 640, 480);
        ctx.fillStyle = 'white';
        ctx.font = '30px Arial';
        ctx.fillText('No Camera', 230, 250);
      }
      localStream.value = canvas.captureStream();
      localVideo.value.srcObject = localStream.value;
    }
  }
}

function toggleAudio() {
  isAudioEnabled.value = !isAudioEnabled.value;
  localStream.value?.getAudioTracks().forEach(track => track.enabled = isAudioEnabled.value);
}

function toggleVideo() {
  isVideoEnabled.value = !isVideoEnabled.value;
  localStream.value?.getVideoTracks().forEach(track => track.enabled = isVideoEnabled.value);
}

function joinSession() {
  hasJoined.value = true;
  // 로비에서 사용한 스트림 정지 (필요 시)
  if (localVideo.value) {
    localVideo.value.srcObject = null;
  }
}

function saveAndExit() {
  alert('작업을 종료합니다. (더미 기능)');
  router.push(`/group-book-lobby/${groupId || 'default'}`);
}

// --- Computed Properties ---
const getGridTemplate = computed(() => {
  const total = remoteParticipants.value.length + 1;
  let columns = 1;
  if (total <= 4) columns = total;
  else if (total <= 6) columns = 3;
  else if (total <= 9) columns = 3;
  else columns = 4;
  return `repeat(${columns}, 1fr)`;
});

// --- Lifecycle Hooks ---
onMounted(() => {
  setupLocalMedia();
});

onUnmounted(() => {
  localStream.value?.getTracks().forEach(track => track.stop());
});
</script>

<style scoped>
/* --- 폰트 Import --- */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&family=Pretendard:wght@400;500;700&display=swap');
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

/* --- [수정] CSS 변수 (현대적인 테마) --- */
:root {
  --font-main: 'Pretendard', sans-serif;
  --font-title: 'Noto Serif KR', serif;
  --color-bg: #f8f9fa;
  --color-text: #212529;
  --color-primary: #343a40;
  --color-danger: #fa5252;
  --color-surface: #ffffff;
  --color-border: #dee2e6;
  --color-muted-text: #868e96;
}

/* --- 페이지 기본 스타일 --- */
.page-container {
  padding: 2rem;
  background-color: var(--color-bg);
  min-height: 100vh;
  color: var(--color-text);
  font-family: var(--font-main);
}

.card {
  background-color: var(--color-surface);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  border: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.card-header {
  font-family: var(--font-main);
  font-size: 1.25rem;
  font-weight: 600;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 0;
  background-color: transparent;
  color: var(--color-text);
}

/* --- 로비 스타일 --- */
.lobby-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 4rem);
}

.lobby-card {
  width: 100%;
  max-width: 500px;
  background-color: var(--color-surface);
  padding: 2.5rem;
  border-radius: 12px;
  border: 1px solid var(--color-border);
  box-shadow: 0 8px 30px rgba(0,0,0,0.1);
}

.lobby-title {
  font-family: var(--font-main);
  font-size: 2rem;
  font-weight: 700;
  text-align: center;
  color: var(--color-text);
  margin-bottom: 0.75rem;
}

.lobby-subtitle {
  text-align: center;
  color: var(--color-muted-text);
  margin-bottom: 2rem;
  font-size: 1rem;
}

.video-preview-container {
  position: relative;
  width: 100%;
  padding-top: 56.25%; /* 16:9 Aspect Ratio */
  margin-bottom: 2rem;
  background: #f1f3f5;
  border-radius: 8px;
  overflow: hidden;
}

.video-preview {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.media-controls {
  position: absolute;
  bottom: 1rem;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 1rem;
}

.btn-media {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.4);
  color: #fff;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  border: none;
  font-size: 1.5rem;
  transition: all 0.2s ease-in-out;
  cursor: pointer;
}

.btn-media:hover {
  background-color: rgba(0, 0, 0, 0.6);
}

.btn-media.is-muted {
  background-color: var(--color-danger);
}

.btn-join {
  width: 100%;
  background-color: var(--color-primary);
  color: white;
  font-weight: 600;
  font-size: 1rem;
  padding: 0.8rem;
  border-radius: 8px;
  border: none;
  transition: background-color 0.2s ease;
}

.btn-join:hover {
  background-color: #555;
}

/* --- 워크스페이스 스타일 --- */
.workspace-container {
  display: flex;
  gap: 1.5rem;
  height: calc(100vh - 4rem);
}

.workspace-container.single-content {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 4rem);
  gap: 0;
}

.video-grid-card.expanded {
  flex-grow: 1;
  height: 100%;
}

.video-grid {
  padding: 1rem;
  display: grid;
  gap: 1rem;
  overflow-y: auto;
  flex-grow: 1;
}

.video-participant {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  background: #e9ecef;
  border-radius: 8px;
  overflow: hidden;
}

.participant-video, .participant-video-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 2rem;
  font-weight: bold;
  color: var(--color-muted-text);
}

.participant-name {
  position: absolute;
  bottom: 0.5rem;
  left: 0.5rem;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  padding: 0.25rem 0.6rem;
  border-radius: 4px;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
}

.main-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border-top: 1px solid var(--color-border);
}

.btn-control {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  background-color: var(--color-surface);
  color: var(--color-muted-text);
  border: 1px solid var(--color-border);
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-size: 0.8rem;
  min-width: 80px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-control:hover {
  background-color: var(--color-bg);
  border-color: #ced4da;
}

.btn-control i {
  font-size: 1.25rem;
}

.btn-control:not(.is-muted) {
  color: var(--color-text);
}

.btn-control.is-muted {
  color: var(--color-danger);
}

.btn-control.btn-leave {
  background-color: var(--color-danger);
  border-color: var(--color-danger);
  color: white;
}

.btn-control.btn-leave:hover {
  opacity: 0.8;
}

</style>