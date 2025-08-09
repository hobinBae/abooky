<template>
  <div class="page-container group-book-creation-page">
    <!-- 로비 화면 -->
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

        <button @click="joinRoom" class="btn btn-primary btn-join" :disabled="!canJoin || isConnecting">
          {{ isConnecting ? '입장 중...' : '그룹책 만들기 입장' }}
        </button>
      </div>
    </div>

    <!-- 비디오 통화 화면 -->
    <div v-else class="workspace-container">
      <div class="video-section">
        <div class="video-header">
          <h3 class="video-title">
            참여자 ({{ totalParticipants }}명)
            <span class="connection-indicator" :class="`status-${connectionState}`">
              {{ getConnectionStatusText }}
            </span>
          </h3>
        </div>

        <div class="video-grid-wrapper">
          <div class="video-grid" :class="`participants-${totalParticipants}`">
            <!-- 로컬 참여자 (나) -->
            <div class="video-participant local-participant">
              <video
                ref="localVideoElement"
                autoplay
                muted
                playsinline
                class="participant-video">
              </video>
              <div class="participant-info">
                <div class="participant-name">
                  <i class="bi me-1" :class="isAudioEnabled ? 'bi-mic-fill' : 'bi-mic-mute-fill'"></i>
                  나 (You)
                </div>
              </div>
            </div>

            <!-- 원격 참여자들 -->
            <div
              v-for="participant in remoteParticipants"
              :key="participant.identity"
              class="video-participant remote-participant">
              <video
                :ref="el => setParticipantVideoRef(el, participant.identity)"
                autoplay
                playsinline
                class="participant-video">
              </video>
              <div v-if="!participant.videoTrack" class="participant-video-placeholder">
                {{ participant.identity.charAt(0).toUpperCase() }}
              </div>
              <div class="participant-info">
                <div class="participant-name">
                  <i class="bi me-1" :class="participant.isMicrophoneEnabled ? 'bi-mic-fill' : 'bi-mic-mute-fill'"></i>
                  {{ participant.identity }}
                  <span v-if="participant.connectionQuality !== undefined" class="connection-quality">
                    {{ getConnectionQualityText(participant.connectionQuality) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="controls-section">
          <div class="main-controls">
            <button @click="toggleMicrophone" class="btn btn-control" :class="{ 'is-muted': !isAudioEnabled }">
              <i class="bi" :class="isAudioEnabled ? 'bi-mic-fill' : 'bi-mic-mute-fill'"></i>
              <span>{{ isAudioEnabled ? '음소거' : '음소거 해제' }}</span>
            </button>

            <button @click="toggleCamera" class="btn btn-control" :class="{ 'is-muted': !isVideoEnabled }">
              <i class="bi" :class="isVideoEnabled ? 'bi-camera-video-fill' : 'bi-camera-video-off-fill'"></i>
              <span>{{ isVideoEnabled ? '비디오 중지' : '비디오 시작' }}</span>
            </button>

            <button @click="toggleScreenShare" class="btn btn-control" :class="{ 'active': isScreenSharing }">
              <i class="bi" :class="isScreenSharing ? 'bi-stop-circle-fill' : 'bi-share-fill'"></i>
              <span>{{ isScreenSharing ? '화면공유 중지' : '화면 공유' }}</span>
            </button>

            <button @click="goToBookEditor" class="btn btn-control btn-book">
              <i class="bi bi-book-fill"></i>
              <span>책 만들기</span>
            </button>

            <button @click="leaveRoom" class="btn btn-control btn-leave">
              <i class="bi bi-box-arrow-right"></i>
              <span>나가기</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick, toRaw } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import apiClient from '@/api';

// LiveKit 타입 정의 (실제 환경에서는 npm install livekit-client 후 import 사용)
declare global {
  interface Window {
    LivekitClient: any;
  }
}

// --- Interfaces ---
interface RemoteParticipant {
  identity: string;
  isMicrophoneEnabled: boolean;
  isCameraEnabled: boolean;
  videoTrack?: any;
  audioTrack?: any;
  connectionQuality?: number;
}

interface ConnectionStatus {
  type: 'info' | 'success' | 'warning' | 'error';
  message: string;
}

// --- Router ---
const route = useRoute();
const router = useRouter();
const groupId = route.query.groupId as string || 'default-room';

// --- Reactive State ---
const hasJoined = ref(false);
const isConnecting = ref(false);
const canJoin = ref(false);

// 미디어 상태
const localVideo = ref<HTMLVideoElement | null>(null);
const localVideoElement = ref<HTMLVideoElement | null>(null);
const isAudioEnabled = ref(true);
const isVideoEnabled = ref(true);
const isScreenSharing = ref(false);

// 연결 상태
const connectionState = ref<'disconnected' | 'connecting' | 'connected' | 'reconnecting'>('disconnected');
const connectionStatus = ref<ConnectionStatus | null>(null);

// LiveKit 관련 - non-reactive storage for WebRTC objects
let livekitRoom: any = null;
let localMediaStream: MediaStream | null = null;

// UI state only (reactive)
const remoteParticipants = ref<RemoteParticipant[]>([]);
const participantVideoRefs = ref<Map<string, HTMLVideoElement>>(new Map());

// --- LiveKit Configuration ---
const LIVEKIT_URL = 'ws://localhost:7880'; // 백엔드 LiveKit 서버 URL (application.properties에서 관리)

// --- Computed Properties ---
const totalParticipants = computed(() => {
  return remoteParticipants.value.length + 1;
});

const getConnectionStatusText = computed(() => {
  switch (connectionState.value) {
    case 'connected': return '연결됨';
    case 'connecting': return '연결 중';
    case 'reconnecting': return '재연결 중';
    case 'disconnected': return '연결 끊김';
    default: return '';
  }
});

// --- Helper Functions ---
function getConnectionQualityText(quality: number): string {
  switch (quality) {
    case 0: return '연결 불량';
    case 1: return '나쁨';
    case 2: return '보통';
    case 3: return '좋음';
    case 4: return '매우 좋음';
    default: return '';
  }
}

function setParticipantVideoRef(el: HTMLVideoElement | null, identity: string) {
  if (el) {
    participantVideoRefs.value.set(identity, el);
  }
}

// --- LiveKit Functions ---
async function getAccessToken(): Promise<{ url: string, token: string}> {
  try {
    const userName = `User_${Date.now()}`;
    const response = await apiClient.post(`/api/v1/groups/${groupId}/rtc/token`, {
      userName
    });

    const data = response.data.data ?? response.data;
    if(!data?.token || !data?.url) throw new Error('응답에 url/token 없음');
    return { url: data.url, token: data.token };
  } catch (error) {
    console.error('토큰 발급 오류:', error);
    throw error;
  }
}

async function setupLocalMedia() {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      video: { width: 1280, height: 720 },
      audio: { echoCancellation: true, noiseSuppression: true }
    });

    if (localVideo.value) {
      localVideo.value.srcObject = stream;
    }

    canJoin.value = true;
    connectionStatus.value = { type: 'success', message: '카메라와 마이크가 준비되었습니다.' };
  } catch (error) {
    console.error('미디어 접근 실패:', error);
    connectionStatus.value = { type: 'warning', message: '카메라/마이크에 접근할 수 없습니다.' };
    canJoin.value = true; // 미디어 없이도 입장 허용
  }
}

async function joinRoom() {
  if (isConnecting.value) return;

  isConnecting.value = true;
  connectionState.value = 'connecting';

  try {
    // LiveKit SDK 로드 확인
    if (!window.LivekitClient) {
      throw new Error('LiveKit SDK가 로드되지 않았습니다.');
    }

    const { Room, RoomEvent, Track, RemoteTrack, ConnectionQuality } = window.LivekitClient;

    // Room 인스턴스 생성 - non-reactive
    livekitRoom = new Room({
      adaptiveStream: true,
      dynacast: true,
      videoCaptureDefaults: {
        resolution: { width: 1280, height: 720 }
      }
    });

    // 이벤트 리스너 설정
    setupRoomEventListeners();

    // 토큰 발급 및 연결
    const { url, token } = await getAccessToken();
    await livekitRoom.connect(url, token);

    // 로컬 미디어 퍼블리시
    await publishLocalMedia();

    // UI 전환 전 DOM 업데이트 대기
    await nextTick();

    hasJoined.value = true;
    connectionState.value = 'connected';
    connectionStatus.value = null;

  } catch (error) {
    console.error('룸 입장 실패:', error);
    connectionStatus.value = { type: 'error', message: `입장 실패: ${error.message || '알 수 없는 오류'}` };
    connectionState.value = 'disconnected';
  } finally {
    isConnecting.value = false;
  }
}

function setupRoomEventListeners() {
  if (!livekitRoom || !window.LivekitClient) return;

  const { RoomEvent, TrackEvent, ConnectionQuality, Track } = window.LivekitClient;

  // 참여자 연결 이벤트
  livekitRoom.on(RoomEvent.ParticipantConnected, (participant: any) => {
    console.log('참여자 입장:', participant.identity);
    addRemoteParticipant(participant);
  });

  // 참여자 연결 해제 이벤트
  livekitRoom.on(RoomEvent.ParticipantDisconnected, (participant: any) => {
    console.log('참여자 퇴장:', participant.identity);
    removeRemoteParticipant(participant.identity);
  });

  // 로컬 트랙 발행 이벤트
  livekitRoom.on(RoomEvent.LocalTrackPublished, (publication: any, participant: any) => {
    console.log('로컬 트랙 발행:', publication.kind);
    if (publication.kind === 'video') {
      // 로비 비디오 스트림을 중단하고 LiveKit 트랙으로 교체
      if (localVideo.value?.srcObject) {
        const stream = localVideo.value.srcObject as MediaStream;
        stream.getTracks().forEach(track => track.stop());
        localVideo.value.srcObject = null;
      }

      // 비디오 엘리먼트에 연결 (여러 번 시도)
      const attachVideoTrack = () => {
        if (publication.track && localVideoElement.value) {
          try {
            publication.track.attach(localVideoElement.value);
            console.log('로컬 비디오 트랙이 localVideoElement에 연결되었습니다.');
            return true;
          } catch (error) {
            console.warn('비디오 트랙 연결 실패:', error);
            return false;
          }
        }
        return false;
      };

      // 즉시 시도
      if (!attachVideoTrack()) {
        // 100ms 후 재시도
        setTimeout(() => {
          if (!attachVideoTrack()) {
            // 500ms 후 다시 재시도
            setTimeout(attachVideoTrack, 500);
          }
        }, 100);
      }
    }
  });

  // 트랙 구독 이벤트
  livekitRoom.on(RoomEvent.TrackSubscribed, (track: any, publication: any, participant: any) => {
    console.log('트랙 구독:', track.kind, participant.identity);
    handleTrackSubscribed(track, participant);
  });

  // 트랙 구독 해제 이벤트
  livekitRoom.on(RoomEvent.TrackUnsubscribed, (track: any, publication: any, participant: any) => {
    console.log('트랙 구독 해제:', track.kind, participant.identity);
    handleTrackUnsubscribed(track, participant);
  });

  // 연결 품질 변경 이벤트
  livekitRoom.on(RoomEvent.ConnectionQualityChanged, (quality: any, participant: any) => {
    updateParticipantConnectionQuality(participant.identity, quality);
  });

  // 연결 상태 변경 이벤트
  livekitRoom.on(RoomEvent.ConnectionStateChanged, (state: any) => {
    console.log('연결 상태 변경:', state);
    connectionState.value = state;
  });

  // 재연결 이벤트
  livekitRoom.on(RoomEvent.Reconnecting, () => {
    connectionState.value = 'reconnecting';
    connectionStatus.value = { type: 'warning', message: '연결이 끊어져 재연결 중입니다...' };
  });

  livekitRoom.on(RoomEvent.Reconnected, () => {
    connectionState.value = 'connected';
    connectionStatus.value = null;
  });
}

async function publishLocalMedia() {
  if (!livekitRoom) return;

  try {
    // 카메라 퍼블리시 - 이벤트로 트랙 연결 처리
    if (isVideoEnabled.value) {
      await livekitRoom.localParticipant.enableCameraAndMicrophone();
    } else {
      await livekitRoom.localParticipant.enableMicrophone();
    }

    console.log('로컬 미디어 퍼블리시 완료 - 트랙은 LocalTrackPublished 이벤트에서 처리됨');

    // 대안: 직접 비디오 스트림 연결 시도
    if (localVideo.value?.srcObject && localVideoElement.value) {
      console.log('대안: 로비 비디오 스트림을 메인 화면에 복사');
      localVideoElement.value.srcObject = localVideo.value.srcObject;
    }

  } catch (error) {
    console.error('로컬 미디어 퍼블리시 실패:', error);
    // 오류 시 대안 스트림 사용
    if (localVideo.value?.srcObject && localVideoElement.value) {
      console.log('오류로 인한 대안: 로비 비디오 스트림 사용');
      localVideoElement.value.srcObject = localVideo.value.srcObject;
    }
  }
}

function addRemoteParticipant(participant: any) {
  const newParticipant: RemoteParticipant = {
    identity: participant.identity,
    isMicrophoneEnabled: participant.isMicrophoneEnabled,
    isCameraEnabled: participant.isCameraEnabled,
    connectionQuality: undefined
  };

  remoteParticipants.value.push(newParticipant);

  // 기존 트랙들 처리
  participant.videoTracks.forEach((publication: any) => {
    if (publication.track) {
      handleTrackSubscribed(publication.track, participant);
    }
  });

  participant.audioTracks.forEach((publication: any) => {
    if (publication.track) {
      handleTrackSubscribed(publication.track, participant);
    }
  });
}

function removeRemoteParticipant(identity: string) {
  const index = remoteParticipants.value.findIndex(p => p.identity === identity);
  if (index !== -1) {
    remoteParticipants.value.splice(index, 1);
  }
  participantVideoRefs.value.delete(identity);
}

function handleTrackSubscribed(track: any, participant: any) {
  const participantData = remoteParticipants.value.find(p => p.identity === participant.identity);
  if (!participantData) return;

  if (track.kind === 'video') {
    participantData.videoTrack = track;

    // 비디오 엘리먼트에 연결
    nextTick(() => {
      const videoElement = participantVideoRefs.value.get(participant.identity);
      if (videoElement) {
        track.attach(videoElement);
      }
    });
  } else if (track.kind === 'audio') {
    participantData.audioTrack = track;
    track.attach(); // 오디오는 자동 재생
  }
}

function handleTrackUnsubscribed(track: any, participant: any) {
  const participantData = remoteParticipants.value.find(p => p.identity === participant.identity);
  if (!participantData) return;

  if (track.kind === 'video') {
    participantData.videoTrack = undefined;
    track.detach();
  } else if (track.kind === 'audio') {
    participantData.audioTrack = undefined;
    track.detach();
  }
}

function updateParticipantConnectionQuality(identity: string, quality: number) {
  const participant = remoteParticipants.value.find(p => p.identity === identity);
  if (participant) {
    participant.connectionQuality = quality;
  }
}

// --- Media Control Functions ---
async function toggleAudio() {
  if (localVideo.value?.srcObject) {
    const stream = localVideo.value.srcObject as MediaStream;
    const audioTrack = stream.getAudioTracks()[0];
    if (audioTrack) {
      audioTrack.enabled = !audioTrack.enabled;
      isAudioEnabled.value = audioTrack.enabled;
    }
  }
}

async function toggleVideo() {
  if (localVideo.value?.srcObject) {
    const stream = localVideo.value.srcObject as MediaStream;
    const videoTrack = stream.getVideoTracks()[0];
    if (videoTrack) {
      videoTrack.enabled = !videoTrack.enabled;
      isVideoEnabled.value = videoTrack.enabled;
    }
  }
}

async function toggleMicrophone() {
  if (!livekitRoom) return;

  try {
    const enabled = !isAudioEnabled.value;
    await livekitRoom.localParticipant.setMicrophoneEnabled(enabled);
    isAudioEnabled.value = enabled;
  } catch (error) {
    console.error('마이크 토글 실패:', error);
  }
}

async function toggleCamera() {
  if (!livekitRoom) return;

  try {
    const enabled = !isVideoEnabled.value;
    await livekitRoom.localParticipant.setCameraEnabled(enabled);
    isVideoEnabled.value = enabled;

    console.log('카메라 토글:', enabled ? '오톱' : '오프');
  } catch (error) {
    console.error('카메라 토글 실패:', error);
  }
}

async function toggleScreenShare() {
  if (!livekitRoom) return;

  try {
    const enabled = !isScreenSharing.value;
    await livekitRoom.localParticipant.setScreenShareEnabled(enabled);
    isScreenSharing.value = enabled;
  } catch (error) {
    console.error('화면 공유 토글 실패:', error);
    connectionStatus.value = { type: 'error', message: '화면 공유를 시작할 수 없습니다.' };
  }
}

function goToBookEditor() {
  try {
    // 책 에디터 페이지를 새창으로 열기
    const bookEditorUrl = window.location.origin + '/book-editor';
    window.open(bookEditorUrl, '_blank', 'noopener,noreferrer');
  } catch (error) {
    console.error('책 에디터로 이동 실패:', error);
  }
}

async function leaveRoom() {
  try {
    if (livekitRoom) {
      await livekitRoom.disconnect();
      livekitRoom = null;
    }

    // 로컬 미디어 정리
    if (localMediaStream) {
      localMediaStream.getTracks().forEach(track => track.stop());
      localMediaStream = null;
    }

    if (localVideo.value?.srcObject) {
      const stream = localVideo.value.srcObject as MediaStream;
      stream.getTracks().forEach(track => track.stop());
      localVideo.value.srcObject = null;
    }

    // 상태 초기화
    hasJoined.value = false;
    connectionState.value = 'disconnected';
    remoteParticipants.value = [];
    participantVideoRefs.value.clear();

    // 라우터로 이동
    router.push(`/group-book-lobby`);
  } catch (error) {
    console.error('퇴장 중 오류:', error);
  }
}

// --- Lifecycle Hooks ---
onMounted(async () => {
  // LiveKit SDK 로드
  if (!window.LivekitClient) {
    const script = document.createElement('script');
    script.src = 'https://cdn.jsdelivr.net/npm/livekit-client/dist/livekit-client.umd.min.js';
    script.onload = () => {
      console.log('LiveKit SDK 로드 완료');
      setupLocalMedia();
    };
    script.onerror = () => {
      console.error('LiveKit SDK 로드 실패');
      connectionStatus.value = { type: 'error', message: 'LiveKit SDK를 로드할 수 없습니다.' };
    };
    document.head.appendChild(script);
  } else {
    await setupLocalMedia();
  }
});

onUnmounted(() => {
  // 정리 작업
  if (livekitRoom) {
    livekitRoom.disconnect();
    livekitRoom = null;
  }

  if (localMediaStream) {
    localMediaStream.getTracks().forEach(track => track.stop());
    localMediaStream = null;
  }

  if (localVideo.value?.srcObject) {
    const stream = localVideo.value.srcObject as MediaStream;
    stream.getTracks().forEach(track => track.stop());
  }
});
</script>

<style scoped>
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

/* 페이지 기본 스타일 */
.page-container {
  padding: 2rem;
  background-color: var(--color-bg);
  min-height: 100vh;
  color: var(--color-text);
  font-family: var(--font-main);
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
  z-index: 10;
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

.btn-join:hover:not(:disabled) {
  opacity: 0.8;
}

.btn-join:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

.connection-quality {
  font-size: 0.7rem;
  opacity: 0.8;
}

/* 워크스페이스 스타일 */
.workspace-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 4rem);
  max-width: 1200px;
  margin: 0 auto;
}

.video-section {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: var(--color-surface);
  border-radius: 12px;
  border: 1px solid var(--color-border);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  overflow: hidden;
}

.video-header {
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
  background-color: var(--color-surface);
}

.video-title {
  font-family: var(--font-main);
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
  color: var(--color-text);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.video-grid-wrapper {
  flex: 1;
  padding: 1rem;
  overflow-y: auto;
  background-color: #f8f9fa;
}

.video-grid {
  display: grid;
  gap: 1rem;
  width: 100%;
  height: 100%;
  grid-auto-rows: 1fr;
}

/* 참여자 수에 따른 그리드 레이아웃 */
.participants-1 {
  grid-template-columns: 1fr;
  max-width: 600px;
  margin: 0 auto;
}

.participants-2 {
  grid-template-columns: repeat(2, 1fr);
}

.participants-3 {
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: 1fr 1fr;
}

.participants-3 .video-participant:first-child {
  grid-column: 1 / 3;
}

.participants-4 {
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
}

.participants-5,
.participants-6 {
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(2, 1fr);
}

.participants-7,
.participants-8,
.participants-9 {
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(3, 1fr);
}

.participants-10,
.participants-11,
.participants-12 {
  grid-template-columns: repeat(4, 1fr);
  grid-template-rows: repeat(3, 1fr);
}

.video-participant {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  background: #e9ecef;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #dee2e6;
  transition: all 0.2s ease;
}

.video-participant:hover {
  border-color: var(--color-info);
  transform: scale(1.02);
}

.video-participant.local-participant {
  border-color: var(--color-primary);
}

.participant-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.participant-video-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 2rem;
  font-weight: bold;
  color: var(--color-muted-text);
  background: linear-gradient(135deg, #e9ecef 0%, #f8f9fa 100%);
}

.participant-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  padding: 0.5rem;
}

.participant-name {
  color: #fff;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  gap: 0.25rem;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.8);
}

.controls-section {
  border-top: 1px solid var(--color-border);
  background-color: var(--color-surface);
}

.main-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
}

.btn-control {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  background-color: var(--color-surface);
  color: var(--color-muted-text);
  border: 1px solid var(--color-border);
  padding: 0.75rem 1rem;
  border-radius: 8px;
  font-size: 0.8rem;
  min-width: 80px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-control:hover {
  background-color: var(--color-bg);
  border-color: #ced4da;
  transform: translateY(-1px);
}

.btn-control i {
  font-size: 1.25rem;
}

.btn-control:not(.is-muted):not(.btn-leave):not(.active) {
  color: var(--color-text);
}

.btn-control.is-muted {
  color: var(--color-danger);
  border-color: var(--color-danger);
  background-color: rgba(250, 82, 82, 0.1);
}

.btn-control.active {
  background-color: var(--color-success);
  border-color: var(--color-success);
  color: white;
}

.btn-control.active:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.btn-control.btn-book {
  background-color: var(--color-primary);
  border-color: var(--color-primary);
  color: white;
}

.btn-control.btn-book:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.btn-control.btn-leave {
  background-color: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}

.btn-control.btn-leave:hover {
  background-color: #ffe6e6;
  border-color: #ffb3b3;
  color: #c53030;
  transform: translateY(-1px);
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .page-container {
    padding: 1rem;
  }

  .lobby-card {
    padding: 2rem;
  }

  .main-controls {
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .btn-control {
    min-width: 70px;
    padding: 0.5rem 0.75rem;
    font-size: 0.75rem;
  }

  .video-grid {
    gap: 0.5rem;
  }

  /* 모바일에서 그리드 최적화 */
  .participants-3,
  .participants-4,
  .participants-5,
  .participants-6 {
    grid-template-columns: repeat(2, 1fr);
  }

  .participants-3 .video-participant:first-child {
    grid-column: auto;
  }

  .participants-7,
  .participants-8,
  .participants-9,
  .participants-10,
  .participants-11,
  .participants-12 {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .main-controls {
    grid-template-columns: repeat(2, 1fr);
    display: grid;
  }

  .btn-control {
    min-width: auto;
  }

  /* 작은 화면에서는 모든 참여자를 1열로 */
  .video-grid {
    grid-template-columns: 1fr !important;
    grid-template-rows: auto !important;
  }

  .participants-3 .video-participant:first-child {
    grid-column: auto !important;
  }
}
</style>
