<template>
  <div class="page-container group-book-creation-page">
    <!-- Step 1: Lobby / Pre-join screen -->
    <div v-if="!hasJoined" class="lobby-container">
      <div class="auth-container">
        <h1 class="auth-title">그룹 책 만들기 준비</h1>
        <p class="auth-subtitle">참여하기 전에 카메라와 마이크를 확인하세요.</p>
        
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
        <button @click="joinSession" class="btn btn-primary w-100 btn-auth">참여하기</button>
      </div>
    </div>

    <!-- Step 2: Main collaborative workspace -->
    <div v-else class="workspace-container">
      <!-- Main Content: Video + AI Helper -->
      <div class="main-content">
        <div class="card video-grid-card">
          <h3 class="card-header">참여자 영상</h3>
          <div class="video-grid">
            <div class="video-participant">
              <video :srcObject="localStream" autoplay muted playsinline class="participant-video"></video>
              <div class="participant-name local-participant-name" @click="toggleAudio">
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
           <h3 class="card-header">AI 작성 도우미 (한석봉)</h3>
          <div class="ai-transcript">
            <p v-for="(line, index) in transcript" :key="index" :class="`speaker-${line.speaker}`"><strong>{{ line.speaker }}:</strong> {{ line.text }}</p>
          </div>
          <div class="ai-actions">
            <button class="btn btn-info btn-sm">내용 요약</button>
            <button class="btn btn-success btn-sm">이미지 생성</button>
          </div>
        </div>
      </div>

      <!-- Sidebar: Shared Editor + Controls -->
      <div class="sidebar">
        <div class="card editor-card">
          <h3 class="card-header">통합 책 내용</h3>
          <textarea v-model="sharedContent" @input="onEditorChange" class="shared-editor"></textarea>
        </div>
        <div class="card controls-card">
          <h3 class="card-header">세션 관리</h3>
          <div class="session-controls">
            <button @click="saveAndExit" class="btn btn-secondary w-100">임시 저장 및 종료</button>
            <button @click="publishAndExit" class="btn btn-primary w-100 mt-2">발행 및 종료</button>
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
const sharedContent = ref('그룹 통합 책의 내용이 여기에 실시간으로 작성됩니다.');

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
  router.push(`/group-bookshelf/${groupId || 'default'}`);
}

function publishAndExit() {
  alert('책이 발행되었습니다. (더미 기능)');
  router.push(`/group-bookshelf/${groupId || 'default'}`);
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
/* General Page Styles */
.page-container {
  padding: 80px 2rem 2rem;
  background-color: #F5F5DC;
  min-height: 100vh;
  color: #3D2C20;
}

.card {
  background-color: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.07);
  border: 1px solid #EFEBE9;
  display: flex;
  flex-direction: column;
}

.card-header {
  font-size: 1.25rem;
  font-weight: 700;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #EFEBE9;
  margin-bottom: 0;
}

/* Lobby Styles (similar to LoginView) */
.lobby-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 2rem;
}

.auth-container {
  width: 100%;
  max-width: 500px;
  background-color: #FFFFFF;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.1);
}

.auth-title {
  font-size: 2.2rem;
  font-weight: 700;
  text-align: center;
  color: #3D2C20;
  margin-bottom: 0.5rem;
}

.auth-subtitle {
  text-align: center;
  color: #5C4033;
  margin-bottom: 2rem;
}

.video-preview-container {
  position: relative;
  width: 100%;
  padding-top: 75%; /* 4:3 Aspect Ratio */
  margin: 0 auto 2rem auto;
  background: #EFEBE9;
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
  background: rgba(0,0,0,0.5);
  padding: 0.5rem 1rem;
  border-radius: 20px;
}

.btn-media {
  background: #fff;
  color: #3D2C20;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  border: none;
  font-size: 1.2rem;
}

.btn-media.active {
  background: #B8860B;
  color: #fff;
}

.btn-auth {
  background-color: #B8860B;
  border-color: #B8860B;
  font-weight: 600;
}

/* Workspace Styles */
.workspace-container {
  display: flex;
  gap: 1.5rem;
  height: calc(100vh - 80px - 4rem); /* Full height minus padding */
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
  gap: 1rem;
  overflow-y: auto;
  flex-grow: 1;
}

.video-participant {
  position: relative;
  width: 100%;
  padding-top: 75%; /* 4:3 aspect ratio */
  background: #EFEBE9;
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
  font-weight: bold;
  color: #5C4033;
}

.participant-name {
  position: absolute;
  bottom: 0.5rem;
  left: 0.5rem;
  background: rgba(0,0,0,0.6);
  color: #fff;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
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
  font-size: 0.9rem;
  padding: 1rem 1.5rem;
}

.ai-actions {
  padding: 1rem 1.5rem;
  border-top: 1px solid #EFEBE9;
  display: flex;
  gap: 0.5rem;
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
  font-family: 'Montserrat', sans-serif;
  color: #3D2C20;
  background-color: transparent;
  outline: none;
}

.controls-card .session-controls {
  padding: 1.5rem;
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
