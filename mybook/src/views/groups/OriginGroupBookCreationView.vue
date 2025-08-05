<template>
  <div class="page-container group-book-creation-page">
    <!-- Step 1: Lobby / Pre-join screen -->
    <div v-if="!hasJoined" class="lobby-container">
      <div class="auth-container">
        <h1 class="auth-title">화면 미리보기</h1>
        <p class="auth-subtitle">입장하기 전, 카메라와 마이크 상태를 확인해 주세요.</p>
        
        <div class="video-preview-container">
          <video ref="localVideo" autoplay muted playsinline class="video-preview"></video>
          <div class="media-controls">
            <button @click="toggleAudio" class="btn btn-media" :class="{ active: isAudioEnabled }">
              <i class="bi" :class="isAudioEnabled ? 'bi-mic-fill' : 'bi-mic-mute-fill'"></i>
            </button>
            <button @click="toggleVideo" class="btn btn-media" :class="{ active: isVideoEnabled }">
              <i class="bi" :class="isVideoEnabled ? 'bi-camera-video-fill' : 'bi-camera-video-off-fill'"></i>
            </button>
          </div>
        </div>
        <button @click="joinSession" class="btn btn-primary w-100 btn-auth">그룹책 만들기 입장</button>
      </div>
    </div>

    <!-- Step 2: Main collaborative workspace -->
    <div v-else class="workspace-container">
      <!-- Main Content: Video + AI Helper -->
      <div class="main-content">
        <div class="card video-grid-card">
          <h3 class="card-header">참여자</h3>
          <div class="video-grid">
            <div class="video-participant">
              <video :srcObject="localStream" autoplay muted playsinline class="participant-video"></video>
              <div class="participant-name local-participant-name">
                <i class="bi me-2" :class="isAudioEnabled ? 'bi-mic-fill' : 'bi-mic-mute-fill'"></i>
              나
              </div>
            </div>
            <div v-for="p in remoteParticipants" :key="p.id" class="video-participant">
              <div class="participant-video-placeholder">{{ p.name }}</div>
              <div class="participant-name">
                <i class="bi me-2" :class="p.isAudioEnabled ? 'bi-mic-fill' : 'bi-mic-mute-fill'"></i>
                {{ p.name }}
              </div>
            </div>
          </div>
        </div>

        <div class="card ai-helper-card">
           <h3 class="card-header">글벗, 한석봉</h3>
          <div class="ai-transcript">
            <p v-for="(line, index) in transcript" :key="index" :class="`speaker-${line.speaker}`"><strong>{{ line.speaker }}:</strong> {{ line.text }}</p>
          </div>
          <div class="ai-actions">
            <button class="btn btn-info btn-sm">글 요약하기</button>
          </div>
        </div>
      </div>

      <!-- Sidebar: Shared Editor + Controls -->
      <div class="sidebar">
        <div class="card editor-card">
          <h3 class="card-header">우리의 이야기</h3>
          <textarea v-model="sharedContent" @input="onEditorChange" class="shared-editor" placeholder="이곳에 동료들과 함께 이야기의 첫 문장을 써내려가 보세요..."></textarea>
          <div class="editor-actions">
          </div>
        </div>
        <div class="card controls-card">
          <h3 class="card-header">마무리</h3>
          <div class="session-controls">
            <button @click="saveAndExit" class="btn btn-secondary w-100">임시저장하고 나가기</button>
            <button @click="publishAndExit" class="btn btn-primary w-100">완성하기</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

// --- Mock/Placeholder Data & Interfaces ---
interface TranscriptLine {
  speaker: string;
  text: string;
}
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

const remoteParticipants = ref<RemoteParticipant[]>([{ id: 'user2', name: '김철수', isAudioEnabled: true }, { id: 'user3', name: '이영희', isAudioEnabled: false }]);
const transcript = ref<TranscriptLine[]>([
  { speaker: '나', text: '안녕하세요, 오늘 회의를 시작하겠습니다.' },
  { speaker: '김철수', text: '네, 안녕하세요. 첫 번째 에피소드부터 이야기해볼까요?' },
]);
const sharedContent = ref('그룹 통합 책의 내용이 여기에 정리됩니다.');
// --- WebRTC & Media Functions (Simplified/Mocked) ---
async function setupLocalMedia() {
  try {
    localStream.value = new MediaStream();
    if (localVideo.value) {
      // In a real app: localVideo.value.srcObject = await navigator.mediaDevices.getUserMedia(...)
    }
  } catch (error) {
    console.error('Error accessing media devices (mocked):', error);
  }
}

function toggleAudio() {
  isAudioEnabled.value = !isAudioEnabled.value;
}

function toggleVideo() {
  isVideoEnabled.value = !isVideoEnabled.value;
}

function joinSession() {
  hasJoined.value = true;
}

// --- Editor & Session Functions ---
function onEditorChange() {
  console.log('Shared content updated:', sharedContent.value);
}

function saveAndExit() {
  alert('내용이 임시 저장되었습니다. (더미 기능)');
  router.push(`/group-book-lobby/${groupId || 'default'}`);
}

function publishAndExit() {
  alert('책이 발행되었습니다. (더미 기능)');
  router.push(`/group-book-lobby/${groupId || 'default'}`);
}


// --- Lifecycle Hooks ---
onMounted(() => {
  setupLocalMedia();
});

onUnmounted(() => {
  if (localStream.value) {
    localStream.value.getTracks().forEach(track => track.stop());
  }
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&family=Pretendard:wght@400;500;700&display=swap');

:root {
  --font-main: 'Pretendard', sans-serif;
  --font-title: 'Noto Serif KR', serif;
  --color-bg: #F5F5DC; /* Beige */
  --color-text: #3D2C20; /* Dark Brown */
  --color-primary: #B8860B; /* Dark Goldenrod */
  --color-surface: #FFFDF5; /* Creamy White */
  --color-border: #DCD0B9; /* Soft Brown */
  --color-accent: #8B4513; /* Saddle Brown */
}

/* General Page Styles */
.page-container {
  padding: 80px 2rem 2rem;
  background-color: var(--color-bg);
  min-height: 100vh;
  color: var(--color-text);
  font-family: 'Pretendard', sans-serif;
}

.card {
  background-color: var(--color-surface);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  border: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.card-header {
  font-family: 'Noto Serif KR', serif;
  font-size: 1.5rem;
  font-weight: 600;
  padding: 0.8rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 0;
  background-color: rgba(210, 180, 140, 0.2); /* Light Tan */
  color: var(--color-accent);
}

/* --- Lobby Styles --- */
.lobby-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 2rem;
}

.auth-container {
  width: 100%;
  max-width: 550px;
  background-color: var(--color-surface);
  padding: 2.5rem 3rem;
  border-radius: 8px;
  border: 1px solid var(--color-border);
  box-shadow: 0 5px 20px rgba(0,0,0,0.1);
}

.auth-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 2.8rem;
  font-weight: 700;
  text-align: center;
  color: var(--color-accent);
  margin-bottom: 0.75rem;
}

.auth-subtitle {
  text-align: center;
  color: var(--color-text);
  margin-bottom: 2.5rem;
  font-size: 1.25rem;
  line-height: 1.7;
}

.video-preview-container {
  position: relative;
  width: 100%;
  padding-top: 75%; /* 4:3 Aspect Ratio */
  margin: 0 auto 2rem auto;
  background: #EAE0C8;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--color-border);
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
  background: rgb(61, 44, 32);
  padding: 0.5rem 1rem;
  border-radius: 20px;
}

.btn-media {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fffbe6;
  color: #B8860B;
  border-radius: 50%;
  width: 44px;
  height: 44px;
  border: 2px solid #B8860B;
  font-size: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.2s ease-in-out;
  cursor: pointer; /* ✨ 커서 추가 */
}

.btn-media:hover {
  background-color: #fff3c4;
  border-color: #B8860B;
  opacity: 0.7; /* ✨ 살짝 투명해짐 */
}


.btn-media.active {
  background: #fffbe6;
  color: #fff;
  border-color: #B8860B;
}

.btn-auth {
  color: white;
  font-weight: 700;
  font-size: 1.1rem;
  padding: 0.75rem;
  border-radius: 8px;
  transition: background-color 0.2s ease;
}

/* Icon color transitions for mic/camera buttons */
.btn-media i {
  transition: color 0.2s;
  color: #e53935 !important;
}
.btn-media:not(.active) i {
  color: #e53935 !important;
}
.btn-media.active i {
  color: #e53935 !important;
}


.btn-auth:hover {
  background-color: #a7770a;
}


/* --- Workspace Styles --- */
.workspace-container {
  display: flex;
  gap: 1.5rem;
  height: calc(100vh - 80px - 4rem);
}

.main-content {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.sidebar {
  width: 380px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.video-grid-card {
  flex-grow: 1;
}

.video-grid {
  padding: 1.5rem;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 1.5rem;
  overflow-y: auto;
  flex-grow: 1;
}

.video-participant {
  position: relative;
  width: 100%;
  padding-top: 75%; /* 4:3 aspect ratio */
  background: #EAE0C8;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
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
  font-weight: bold;
  color: var(--color-text);
}

.participant-name {
  position: absolute;
  bottom: 0.5rem;
  left: 0.5rem;
  background: rgba(61, 44, 32, 0.7);
  color: #fff;
  padding: 0.3rem 0.6rem;
  border-radius: 4px;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
}

.local-participant-name {
  cursor: pointer;
}

.ai-helper-card {
  height: 280px;
}

.ai-transcript {
  flex-grow: 1;
  overflow-y: auto;
  font-size: 1rem;
  padding: 1rem 1.5rem;
  line-height: 1.6;
}

.ai-actions {
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--color-border);
  display: flex;
  gap: 0.75rem;
}

.editor-card {
  flex-grow: 1;
}

.shared-editor {
  width: 100%;
  flex-grow: 1;
  resize: none;
  border: none;
  padding: 1.5rem;
  font-family: 'Noto Serif KR', serif;
  font-size: 1.1rem;
  line-height: 1.9;
  color: var(--color-text);
  background-color: transparent;
  outline: none;
  background-size: 100% 2.8em;
}

.editor-actions {
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--color-border);
}

.controls-card .session-controls {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

/* General Button Styles in Workspace */
.btn {
  font-family: var(--font-main);
  font-weight: 600;
  border-radius: 6px;
  padding: 0.5rem 1rem;
  transition: all 0.2s ease;
}

.btn-primary {
  background-color: #a7770a;
  border-color: #a7770a;
  color: #fffbe6; 
}
.btn-primary:hover {
  opacity: 0.8;
}

.btn-secondary {
  background-color: #7a3d10;
  border-color: #7a3d10;
  color: #fffbe6;
}
.btn-secondary:hover {
  opacity: 0.8;
}


.btn-info, .btn-success {
  background-color: #8B795E; /* Muted Brown */
  border-color: #8B795E;
  color: white;
}
.btn-info:hover, .btn-success:hover {
  background-color: #7a6a50;
}

.btn-outline-secondary {
  color: var(--color-accent);
  border-color: var(--color-border);
}
.btn-outline-secondary:hover {
  background-color: rgba(210, 180, 140, 0.2);
}


@media (max-width: 992px) {
  .workspace-container {
    flex-direction: column;
    height: auto;
  }
  .sidebar {
    width: 100%;
    flex-direction: row;
  }
  .editor-card, .controls-card {
    width: 50%;
  }
}

@media (max-width: 768px) {
  .page-container {
    padding: 70px 1rem 1rem;
  }
  .sidebar {
    flex-direction: column;
  }
  .editor-card, .controls-card {
    width: 100%;
  }
}
</style>
