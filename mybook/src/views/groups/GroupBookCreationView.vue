<template>
  <div class="group-book-creation-page">
    <!-- Step 1: Lobby / Pre-join screen -->
    <div v-if="!hasJoined" class="lobby-container">
      <h1 class="lobby-title">화상 채팅 준비</h1>
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
      <button @click="joinSession" class="btn btn-primary btn-lg join-btn">참여하기</button>
    </div>

    <!-- Step 2: Main collaborative workspace -->
    <div v-else class="workspace-container">
      <!-- Main Content: Video + AI Helper -->
      <div class="main-content">
        <div class="video-grid">
          <div class="video-participant">
            <video :srcObject="localStream" autoplay muted playsinline class="participant-video"></video>
            <div class="participant-name">나</div>
          </div>
          <!-- Remote participants would be added here -->
          <div v-for="(p, index) in remoteParticipants" :key="index" class="video-participant">
            <div class="participant-video-placeholder">{{ p.name }}</div>
            <div class="participant-name">{{ p.name }}</div>
          </div>
        </div>

        <div class="ai-helper-panel">
          <h3 class="ai-helper-title">AI 작성 도우미 (한석봉)</h3>
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
        <div class="shared-editor-container">
          <h3 class="sidebar-title">통합 책 내용</h3>
          <textarea v-model="sharedContent" @input="onEditorChange" class="shared-editor"></textarea>
        </div>
        <div class="session-controls">
          <h3 class="sidebar-title">세션 관리</h3>
          <button @click="saveAndExit" class="btn btn-secondary">임시 저장 및 종료</button>
          <button @click="publishAndExit" class="btn btn-primary">발행 및 종료</button>
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

const remoteParticipants = ref<RemoteParticipant[]>([{ id: 'user2', name: '김철수' }, { id: 'user3', name: '이영희' }]);
const transcript = ref<TranscriptLine[]>([
  { speaker: '나', text: '안녕하세요, 오늘 회의를 시작하겠습니다.' },
  { speaker: '김철수', text: '네, 안녕하세요. 첫 번째 에피소드부터 이야기해볼까요?' },
]);
const sharedContent = ref('그룹 통합 책의 내용이 여기에 실시간으로 작성됩니다.');

// --- WebRTC & Media Functions (Simplified/Mocked) ---
async function setupLocalMedia() {
  try {
    // Mock media stream for demonstration without actual camera/mic access
    // In a real app, this would be: await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
    localStream.value = new MediaStream(); // Create a dummy MediaStream
    if (localVideo.value) {
      // localVideo.value.srcObject = localStream.value; // Would assign real stream
      // For dummy, we can just show a placeholder or static image
    }
  } catch (error) {
    console.error('Error accessing media devices (mocked):', error);
    // alert('카메라와 마이크에 접근할 수 없습니다. 권한을 확인해주세요.');
  }
}

function toggleAudio() {
  isAudioEnabled.value = !isAudioEnabled.value;
  alert(`마이크 ${isAudioEnabled.value ? '켜짐' : '꺼짐'}`);
}

function toggleVideo() {
  isVideoEnabled.value = !isVideoEnabled.value;
  alert(`카메라 ${isVideoEnabled.value ? '켜짐' : '꺼짐'}`);
}

function joinSession() {
  // In a real app, this is where you'd initiate WebRTC connections
  // For dummy, just switch to workspace view
  hasJoined.value = true;
  alert('화상 채팅에 참여했습니다. (더미 기능)');
}

// --- Shared Editor Functions (Local State) ---
function onEditorChange() {
  // In a real app, this would send updates to Firestore/backend
  console.log('Shared content updated locally:', sharedContent.value);
}

// --- Session Control Functions ---
function saveAndExit() {
  alert('내용이 임시 저장되었습니다. (더미 기능)');
  router.push(`/group-bookshelf/${groupId}`);
}

function publishAndExit() {
  alert('책이 발행되었습니다. (더미 기능)');
  router.push(`/group-bookshelf/${groupId}`);
}

// --- AI Helper Functions (Dummy) ---
function summarizeContent() {
  alert('내용 요약 기능은 개발 중입니다.');
}

function generateImage() {
  alert('이미지 생성 기능은 개발 중입니다.');
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
.group-book-creation-page {
  width: 100%;
  height: 100vh;
  background-color: #3D2C20;
  color: #F5F5DC;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* Lobby */
.lobby-container {
  text-align: center;
  background-color: #5C4033;
  padding: 3rem;
  border-radius: 12px;
}

.lobby-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 2rem;
}

.video-preview-container {
  position: relative;
  width: 480px;
  height: 360px;
  margin: 0 auto 2rem auto;
  background: #2c1e12;
  border-radius: 8px;
}

.video-preview {
  width: 100%;
  height: 100%;
  border-radius: 8px;
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
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.2rem;
}

.btn-media.active {
  background: #B8860B;
  color: #fff;
}

.join-btn {
  background-color: #B8860B;
  border-color: #B8860B;
}

/* Workspace */
.workspace-container {
  display: flex;
  width: 100%;
  height: 100%;
  padding: 1rem;
  gap: 1rem;
}

.main-content {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.video-grid {
  flex-grow: 1;
  background: #2c1e12;
  border-radius: 8px;
  padding: 1rem;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
  overflow-y: auto;
}

.video-participant {
  position: relative;
}

.participant-video, .participant-video-placeholder {
  width: 100%;
  aspect-ratio: 4 / 3;
  background: #5C4033;
  border-radius: 6px;
  object-fit: cover;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
}

.participant-name {
  position: absolute;
  bottom: 0.5rem;
  left: 0.5rem;
  background: rgba(0,0,0,0.6);
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.9rem;
}

.ai-helper-panel {
  height: 250px;
  background: #5C4033;
  border-radius: 8px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
}

.ai-helper-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.ai-transcript {
  flex-grow: 1;
  overflow-y: auto;
  font-size: 0.9rem;
  padding-right: 0.5rem;
}

.ai-actions {
  margin-top: 0.5rem;
  display: flex;
  gap: 0.5rem;
}

.sidebar {
  width: 350px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.shared-editor-container {
  flex-grow: 1;
  background: #fff;
  border-radius: 8px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
}

.sidebar-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #3D2C20;
}

.shared-editor {
  width: 100%;
  flex-grow: 1;
  resize: none;
  border: 1px solid #ccc;
  border-radius: 4px;
  padding: 0.5rem;
  color: #3D2C20;
  background-color: #F5F5DC;
}

.session-controls {
  background: #5C4033;
  border-radius: 8px;
  padding: 1rem;
}

.session-controls .btn {
  width: 100%;
  margin-top: 0.5rem;
}
</style>
