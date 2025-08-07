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
        <div class="connection-status" v-if="connectionStatus">
          <span :class="`status-${connectionStatus.type}`">{{ connectionStatus.message }}</span>
        </div>
        <button @click="joinSession" class="btn btn-primary btn-join" :disabled="!canJoin">
          {{ isConnecting ? '연결 중...' : '그룹책 만들기 입장' }}
        </button>
      </div>
    </div>

    <div v-else class="workspace-container single-content">
      <div class="card video-grid-card expanded">
        <h3 class="card-header">
          참여자 ({{ Object.keys(remoteParticipants).length + 1 }}명)
          <span class="connection-indicator" :class="`status-${signalingStatus}`">{{ getStatusText }}</span>
        </h3>
        <div class="video-grid large" :style="{ gridTemplateColumns: getGridTemplate }">
          <!-- 로컬 비디오 -->
          <div class="video-participant">
            <video :srcObject="localStream" autoplay muted playsinline class="participant-video"></video>
            <div class="participant-name">
              <i class="bi me-1" :class="isAudioEnabled ? 'bi-mic-fill' : 'bi-mic-mute-fill'"></i>
              나 (You)
            </div>
          </div>
          <!-- 원격 참여자들 -->
          <div v-for="(participant, id) in remoteParticipants" :key="id" class="video-participant">
            <video 
              v-if="participant.stream" 
              :srcObject="participant.stream" 
              autoplay 
              playsinline 
              class="participant-video">
            </video>
            <div v-else class="participant-video-placeholder">{{ participant.name.charAt(0) }}</div>
            <div class="participant-name">
              <i class="bi me-1" :class="participant.isAudioEnabled ? 'bi-mic-fill' : 'bi-mic-mute-fill'"></i>
              {{ participant.name }}
              <span v-if="participant.connectionState" class="connection-state">
                ({{ getConnectionStateText(participant.connectionState) }})
              </span>
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
          <button @click="toggleScreenShare" class="btn btn-control" :class="{ 'active': isScreenSharing }">
            <i class="bi" :class="isScreenSharing ? 'bi-stop-circle-fill' : 'bi-share-fill'"></i>
            <span>{{ isScreenSharing ? '화면공유 중지' : '화면 공유' }}</span>
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
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';

// --- Interfaces ---
interface RemoteParticipant {
  id: string;
  name: string;
  stream?: MediaStream;
  isAudioEnabled: boolean;
  isVideoEnabled: boolean;
  connectionState?: string;
}

interface SignalingMessage {
  type: 'offer' | 'answer' | 'ice-candidate' | 'user-joined' | 'user-left' | 'media-state-change';
  data: any;
  from?: string;
  to?: string;
}

interface ConnectionStatus {
  type: 'info' | 'success' | 'warning' | 'error';
  message: string;
}

// --- Router ---
const route = useRoute();
const router = useRouter();
const groupId = route.query.groupId as string;

// --- Reactive State ---
const hasJoined = ref(false);
const isConnecting = ref(false);
const canJoin = ref(false);
const localVideo = ref<HTMLVideoElement | null>(null);
const localStream = ref<MediaStream | null>(null);
const isAudioEnabled = ref(true);
const isVideoEnabled = ref(true);
const isScreenSharing = ref(false);
const originalVideoTrack = ref<MediaStreamTrack | null>(null);

// 연결 관련 상태
const signalingStatus = ref<'disconnected' | 'connecting' | 'connected' | 'error'>('disconnected');
const connectionStatus = ref<ConnectionStatus | null>(null);

// 참여자 및 WebRTC 연결
const remoteParticipants = ref<Record<string, RemoteParticipant>>({});
const peerConnections = ref<Record<string, RTCPeerConnection>>({});
const ws = ref<WebSocket | null>(null);
const myId = ref<string>('');

// WebRTC 설정
const rtcConfig: RTCConfiguration = {
  iceServers: [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun1.l.google.com:19302' }
  ]
};

// --- WebRTC & Signaling Functions ---
function connectToSignalingServer() {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const wsUrl = `${protocol}//${window.location.host}/ws/group/${groupId}`;
  
  // 개발 환경에서는 로컬 시그널링 서버 사용
  const devWsUrl = 'ws://localhost:3001';
  
  try {
    ws.value = new WebSocket(devWsUrl);
    signalingStatus.value = 'connecting';
    connectionStatus.value = { type: 'info', message: '시그널링 서버에 연결 중...' };

    ws.value.onopen = () => {
      console.log('시그널링 서버 연결됨');
      signalingStatus.value = 'connected';
      connectionStatus.value = { type: 'success', message: '서버에 연결되었습니다.' };
      canJoin.value = true;
      
      // 서버에 참여 요청
      sendSignalingMessage({
        type: 'user-joined',
        data: {
          groupId,
          name: '나' // 실제로는 사용자 이름을 받아와야 함
        }
      });
    };

    ws.value.onmessage = (event) => {
      const message: SignalingMessage = JSON.parse(event.data);
      handleSignalingMessage(message);
    };

    ws.value.onerror = (error) => {
      console.error('시그널링 서버 오류:', error);
      signalingStatus.value = 'error';
      connectionStatus.value = { type: 'error', message: '서버 연결 실패. 로컬 모드로 실행됩니다.' };
      // 로컬 모드로 전환
      canJoin.value = true;
      setupLocalMode();
    };

    ws.value.onclose = () => {
      console.log('시그널링 서버 연결 종료');
      signalingStatus.value = 'disconnected';
      if (hasJoined.value) {
        connectionStatus.value = { type: 'warning', message: '서버 연결이 끊어졌습니다.' };
      }
    };
  } catch (error) {
    console.error('WebSocket 연결 실패:', error);
    signalingStatus.value = 'error';
    connectionStatus.value = { type: 'error', message: '서버 연결 실패. 로컬 모드로 실행됩니다.' };
    canJoin.value = true;
    setupLocalMode();
  }
}

function setupLocalMode() {
  // 서버가 없는 경우 로컬 모드로 실행 (데모용 참여자 추가)
  setTimeout(() => {
    if (!hasJoined.value) return;
    
    const mockParticipants = [
      { id: 'user2', name: '김철수', isAudioEnabled: true, isVideoEnabled: true },
      { id: 'user3', name: '이영희', isAudioEnabled: false, isVideoEnabled: true }
    ];

    mockParticipants.forEach(p => {
      remoteParticipants.value[p.id] = p;
    });
  }, 2000);
}

function sendSignalingMessage(message: SignalingMessage) {
  if (ws.value && ws.value.readyState === WebSocket.OPEN) {
    ws.value.send(JSON.stringify(message));
  }
}

async function handleSignalingMessage(message: SignalingMessage) {
  console.log('시그널링 메시지 수신:', message);

  switch (message.type) {
    case 'user-joined':
      if (message.data.id !== myId.value) {
        await handleUserJoined(message.data);
      } else {
        myId.value = message.data.id;
      }
      break;
    case 'user-left':
      handleUserLeft(message.data.id);
      break;
    case 'offer':
      await handleOffer(message.data, message.from!);
      break;
    case 'answer':
      await handleAnswer(message.data, message.from!);
      break;
    case 'ice-candidate':
      await handleIceCandidate(message.data, message.from!);
      break;
    case 'media-state-change':
      handleMediaStateChange(message.data, message.from!);
      break;
  }
}

async function handleUserJoined(userData: any) {
  const { id, name } = userData;
  
  // 새 참여자 추가
  remoteParticipants.value[id] = {
    id,
    name,
    isAudioEnabled: true,
    isVideoEnabled: true,
    connectionState: 'connecting'
  };

  // WebRTC 연결 생성
  const pc = createPeerConnection(id);
  peerConnections.value[id] = pc;

  // 로컬 스트림 추가
  if (localStream.value) {
    localStream.value.getTracks().forEach(track => {
      pc.addTrack(track, localStream.value!);
    });
  }

  // Offer 생성 및 전송
  try {
    const offer = await pc.createOffer();
    await pc.setLocalDescription(offer);
    
    sendSignalingMessage({
      type: 'offer',
      data: offer,
      to: id
    });
  } catch (error) {
    console.error('Offer 생성 실패:', error);
  }
}

function handleUserLeft(userId: string) {
  // 연결 정리
  if (peerConnections.value[userId]) {
    peerConnections.value[userId].close();
    delete peerConnections.value[userId];
  }
  
  // 참여자 제거
  delete remoteParticipants.value[userId];
}

async function handleOffer(offer: RTCSessionDescriptionInit, fromId: string) {
  const pc = peerConnections.value[fromId] || createPeerConnection(fromId);
  peerConnections.value[fromId] = pc;

  try {
    await pc.setRemoteDescription(offer);
    
    // 로컬 스트림 추가
    if (localStream.value) {
      localStream.value.getTracks().forEach(track => {
        pc.addTrack(track, localStream.value!);
      });
    }

    // Answer 생성 및 전송
    const answer = await pc.createAnswer();
    await pc.setLocalDescription(answer);
    
    sendSignalingMessage({
      type: 'answer',
      data: answer,
      to: fromId
    });
  } catch (error) {
    console.error('Offer 처리 실패:', error);
  }
}

async function handleAnswer(answer: RTCSessionDescriptionInit, fromId: string) {
  const pc = peerConnections.value[fromId];
  if (pc) {
    try {
      await pc.setRemoteDescription(answer);
    } catch (error) {
      console.error('Answer 처리 실패:', error);
    }
  }
}

async function handleIceCandidate(candidate: RTCIceCandidateInit, fromId: string) {
  const pc = peerConnections.value[fromId];
  if (pc) {
    try {
      await pc.addIceCandidate(candidate);
    } catch (error) {
      console.error('ICE candidate 추가 실패:', error);
    }
  }
}

function handleMediaStateChange(data: any, fromId: string) {
  if (remoteParticipants.value[fromId]) {
    remoteParticipants.value[fromId].isAudioEnabled = data.isAudioEnabled;
    remoteParticipants.value[fromId].isVideoEnabled = data.isVideoEnabled;
  }
}

function createPeerConnection(peerId: string): RTCPeerConnection {
  const pc = new RTCPeerConnection(rtcConfig);

  // ICE candidate 이벤트 핸들러
  pc.onicecandidate = (event) => {
    if (event.candidate) {
      sendSignalingMessage({
        type: 'ice-candidate',
        data: event.candidate,
        to: peerId
      });
    }
  };

  // 원격 스트림 수신 이벤트
  pc.ontrack = (event) => {
    console.log('원격 스트림 수신:', event);
    if (remoteParticipants.value[peerId]) {
      remoteParticipants.value[peerId].stream = event.streams[0];
    }
  };

  // 연결 상태 변경 이벤트
  pc.onconnectionstatechange = () => {
    console.log(`Peer ${peerId} 연결 상태:`, pc.connectionState);
    if (remoteParticipants.value[peerId]) {
      remoteParticipants.value[peerId].connectionState = pc.connectionState;
    }
  };

  return pc;
}

// --- Media Functions ---
async function setupLocalMedia() {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ 
      video: { width: 1280, height: 720 }, 
      audio: { echoCancellation: true, noiseSuppression: true } 
    });
    localStream.value = stream;
    originalVideoTrack.value = stream.getVideoTracks()[0];
    
    if (localVideo.value) {
      localVideo.value.srcObject = stream;
    }
  } catch (error) {
    console.error('미디어 장치에 접근할 수 없습니다:', error);
    connectionStatus.value = { type: 'warning', message: '카메라/마이크에 접근할 수 없습니다.' };
    
    // Mock 스트림 생성 (개발용)
    createMockStream();
  }
}

function createMockStream() {
  if (localVideo.value) {
    const canvas = document.createElement('canvas');
    canvas.width = 640;
    canvas.height = 480;
    const ctx = canvas.getContext('2d');
    if (ctx) {
      ctx.fillStyle = '#2c3e50';
      ctx.fillRect(0, 0, 640, 480);
      ctx.fillStyle = 'white';
      ctx.font = '24px Arial';
      ctx.fillText('No Camera Available', 180, 250);
    }
    localStream.value = canvas.captureStream();
    localVideo.value.srcObject = localStream.value;
  }
}

function toggleAudio() {
  isAudioEnabled.value = !isAudioEnabled.value;
  localStream.value?.getAudioTracks().forEach(track => {
    track.enabled = isAudioEnabled.value;
  });
  
  // 다른 참여자들에게 오디오 상태 변경 알림
  broadcastMediaStateChange();
}

function toggleVideo() {
  isVideoEnabled.value = !isVideoEnabled.value;
  localStream.value?.getVideoTracks().forEach(track => {
    track.enabled = isVideoEnabled.value;
  });
  
  broadcastMediaStateChange();
}

async function toggleScreenShare() {
  if (!isScreenSharing.value) {
    try {
      const screenStream = await navigator.mediaDevices.getDisplayMedia({ 
        video: true, 
        audio: true 
      });
      
      const videoTrack = screenStream.getVideoTracks()[0];
      
      // 화면공유 종료 이벤트 리스너
      videoTrack.onended = () => {
        isScreenSharing.value = false;
        restoreVideoTrack();
      };
      
      // 모든 peer connection에서 비디오 트랙 교체
      Object.values(peerConnections.value).forEach(pc => {
        const sender = pc.getSenders().find(s => 
          s.track && s.track.kind === 'video'
        );
        if (sender) {
          sender.replaceTrack(videoTrack);
        }
      });
      
      // 로컬 비디오도 교체
      if (localStream.value) {
        const oldVideoTrack = localStream.value.getVideoTracks()[0];
        localStream.value.removeTrack(oldVideoTrack);
        localStream.value.addTrack(videoTrack);
      }
      
      isScreenSharing.value = true;
      
    } catch (error) {
      console.error('화면 공유 실패:', error);
      connectionStatus.value = { type: 'error', message: '화면 공유를 시작할 수 없습니다.' };
    }
  } else {
    restoreVideoTrack();
  }
}

async function restoreVideoTrack() {
  try {
    let videoTrack = originalVideoTrack.value;
    
    if (!videoTrack || videoTrack.readyState === 'ended') {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true });
      videoTrack = stream.getVideoTracks()[0];
      originalVideoTrack.value = videoTrack;
    }
    
    // 모든 peer connection에서 비디오 트랙 복원
    Object.values(peerConnections.value).forEach(pc => {
      const sender = pc.getSenders().find(s => 
        s.track && s.track.kind === 'video'
      );
      if (sender && videoTrack) {
        sender.replaceTrack(videoTrack);
      }
    });
    
    // 로컬 비디오도 복원
    if (localStream.value && videoTrack) {
      const oldVideoTrack = localStream.value.getVideoTracks()[0];
      if (oldVideoTrack) {
        localStream.value.removeTrack(oldVideoTrack);
      }
      localStream.value.addTrack(videoTrack);
    }
    
    isScreenSharing.value = false;
    
  } catch (error) {
    console.error('비디오 트랙 복원 실패:', error);
  }
}

function broadcastMediaStateChange() {
  sendSignalingMessage({
    type: 'media-state-change',
    data: {
      isAudioEnabled: isAudioEnabled.value,
      isVideoEnabled: isVideoEnabled.value
    }
  });
}

async function joinSession() {
  if (isConnecting.value) return;
  
  isConnecting.value = true;
  
  try {
    // 미디어 스트림이 없으면 다시 설정
    if (!localStream.value) {
      await setupLocalMedia();
    }
    
    hasJoined.value = true;
    connectionStatus.value = null;
    
    // 시그널링 서버가 연결되어 있지 않다면 로컬 모드로 실행
    if (signalingStatus.value !== 'connected') {
      setupLocalMode();
    }
    
  } catch (error) {
    console.error('세션 참여 실패:', error);
    connectionStatus.value = { type: 'error', message: '세션 참여에 실패했습니다.' };
  } finally {
    isConnecting.value = false;
  }
}

function saveAndExit() {
  // 모든 연결 정리
  Object.values(peerConnections.value).forEach(pc => pc.close());
  
  // 시그널링 서버에 퇴장 알림
  if (ws.value) {
    sendSignalingMessage({
      type: 'user-left',
      data: { id: myId.value }
    });
    ws.value.close();
  }
  
  // 미디어 스트림 정리
  localStream.value?.getTracks().forEach(track => track.stop());
  
  router.push(`/group-book-lobby/${groupId || 'default'}`);
}

// --- Computed Properties ---
const getGridTemplate = computed(() => {
  const total = Object.keys(remoteParticipants.value).length + 1;
  let columns = 1;
  if (total <= 4) columns = Math.ceil(Math.sqrt(total));
  else if (total <= 6) columns = 3;
  else if (total <= 9) columns = 3;
  else columns = 4;
  return `repeat(${columns}, 1fr)`;
});

const getStatusText = computed(() => {
  switch (signalingStatus.value) {
    case 'connected': return '연결됨';
    case 'connecting': return '연결 중';
    case 'disconnected': return '연결 끊김';
    case 'error': return '로컬 모드';
    default: return '';
  }
});

function getConnectionStateText(state: string): string {
  switch (state) {
    case 'connecting': return '연결 중';
    case 'connected': return '연결됨';
    case 'disconnected': return '연결 끊김';
    case 'failed': return '연결 실패';
    default: return state;
  }
}

// --- Lifecycle Hooks ---
onMounted(async () => {
  await setupLocalMedia();
  connectToSignalingServer();
});

onUnmounted(() => {
  // 정리 작업
  localStream.value?.getTracks().forEach(track => track.stop());
  Object.values(peerConnections.value).forEach(pc => pc.close());
  if (ws.value) {
    ws.value.close();
  }
});
</script>

<style scoped>
/* 기존 스타일에 추가 */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&family=Pretendard:wght@400;500;700&display=swap');
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

:root {
  --font-main: 'Pretendard', sans-serif;
  --font-title: 'Noto Serif KR', serif;
  --color-bg: #f8f9fa;
  --color-text: #212529;
  --color-primary: #343a40;
  --color-success: #28a745;
  --color-warning: #ffc107;
  --color-danger: #fa5252;
  --color-info: #17a2b8;
  --color-surface: #ffffff;
  --color-border: #dee2e6;
  --color-muted-text: #868e96;
}

/* 연결 상태 스타일 */
.connection-status {
  text-align: center;
  padding: 0.5rem;
  border-radius: 6px;
  margin-bottom: 1rem;
  font-size: 0.9rem;
}

.status-info { color: var(--color-info); }
.status-success { color: var(--color-success); }
.status-warning { color: var(--color-warning); }
.status-error { color: var(--color-danger); }

.connection-indicator {
  font-size: 0.8rem;
  float: right;
  padding: 0.2rem 0.5rem;
  border-radius: 10px;
  background: var(--color-bg);
}

.connection-state {
  font-size: 0.7rem;
  opacity: 0.8;
}

/* 화면공유 버튼 활성화 상태 */
.btn-control.active {
  background-color: var(--color-success);
  color: white;
  border-color: var(--color-success);
}

.btn-control.active:hover {
  opacity: 0.8;
}

/* 비활성화된 버튼 */
button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 페이지 기본 스타일 */
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

/* 로비 스타일 */
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
  padding-top: 56.25%;
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
  background-color: #555;
  color: white;
  font-weight: 600;
  font-size: 1rem;
  padding: 0.8rem;
  border-radius: 8px;
  border: none;
  transition: background-color 0.2s ease;
}

.btn-join:hover {
  opacity: 0.8;
}

/* 워크스페이스 스타일 */
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