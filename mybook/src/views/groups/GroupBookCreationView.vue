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

        <!-- 수정된 연결 상태 표시 부분 -->
        <div v-if="connectionStatus" class="connection-status" :class="`connection-status--${connectionStatus.type}`">
          <i class="connection-status__icon" :class="getStatusIcon(connectionStatus.type)"></i>
          <span class="connection-status__message">{{ connectionStatus.message }}</span>
        </div>

        <button @click="joinRoom" class="btn btn-primary btn-join" :disabled="!canJoin || isConnecting">
          {{ isConnecting ? '입장 중...' : '그룹책 만들기 입장' }}
        </button>
      </div>
    </div>

    <!-- 비디오 통화 화면 -->
    <div v-else class="workspace-container">
      <div class="main-content">
        <div class="video-section">
          <div class="video-header">
            <h3 class="video-title">
              참여자 ({{ totalParticipants }}명)
              <span class="connection-indicator" :class="`connection-indicator--${connectionState}`">
                {{ getConnectionStatusText }}
              </span>
            </h3>
          </div>

          <div class="video-grid-wrapper">
            <!-- 화면 공유 모드가 아닐 때 - 기존 그리드 레이아웃 -->
            <div v-if="!isScreenSharing" class="video-grid" :class="`participants-${totalParticipants}`">
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

            <!-- 화면 공유 모드일 때 - 메인 화면 + 썸네일 레이아웃 -->
            <div v-else class="screen-share-layout">
              <!-- 메인 화면 공유 영역 (화면 공유하는 사람의 화면) -->
              <div class="main-screen-area">
                <video
                  ref="localVideoElement"
                  autoplay
                  muted
                  playsinline
                  class="main-screen-video">
                </video>
                <div class="main-screen-info">
                  <div class="sharing-indicator">
                    <i class="bi bi-share-fill me-2"></i>
                    나의 화면 공유 중
                  </div>
                </div>
              </div>

              <!-- 오른쪽 썸네일 영역 -->
              <div class="thumbnails-area">
                <div class="thumbnails-container">
                  <!-- 내 카메라 썸네일 (화면 공유 중이므로 카메라는 별도 표시) -->
                  <div class="thumbnail-participant">
                    <video
                      ref="localCameraThumbnail"
                      autoplay
                      muted
                      playsinline
                      class="thumbnail-video">
                    </video>
                    <div class="thumbnail-info">
                      <div class="thumbnail-name">
                        <i class="bi me-1" :class="isAudioEnabled ? 'bi-mic-fill' : 'bi-mic-mute-fill'"></i>
                        나
                      </div>
                    </div>
                  </div>

                  <!-- 원격 참여자 썸네일들 -->
                  <div
                    v-for="participant in remoteParticipants"
                    :key="participant.identity"
                    class="thumbnail-participant">
                    <video
                      :ref="el => setParticipantVideoRef(el, participant.identity)"
                      autoplay
                      playsinline
                      class="thumbnail-video">
                    </video>
                    <div v-if="!participant.videoTrack" class="thumbnail-video-placeholder">
                      {{ participant.identity.charAt(0).toUpperCase() }}
                    </div>
                    <div class="thumbnail-info">
                      <div class="thumbnail-name">
                        <i class="bi me-1" :class="participant.isMicrophoneEnabled ? 'bi-mic-fill' : 'bi-mic-mute-fill'"></i>
                        {{ participant.identity }}
                      </div>
                    </div>
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

      <!-- 채팅 섹션 (항상 표시) -->
      <div class="chat-section">
        <div class="chat-header">
          <h4 class="chat-title">
            <i class="bi bi-chat-dots-fill me-2"></i>
            그룹 채팅
          </h4>
        </div>

        <div class="chat-messages" ref="chatMessagesContainer">
          <div
            v-for="message in chatMessages"
            :key="message.id"
            class="chat-message"
            :class="{ 'chat-message--own': message.isOwn }">
            <div class="message-sender-name">{{ message.sender }}</div>
            <div class="message-bubble">
              <div class="message-content">{{ message.content }}</div>
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
            </div>
          </div>
          <div v-if="chatMessages.length === 0" class="chat-empty">
            아직 메시지가 없습니다. 첫 번째 메시지를 보내보세요!
          </div>
        </div>

        <div class="chat-input-section">
          <div class="chat-input-wrapper">
            <input
              v-model="newMessage"
              @keyup.enter="sendMessage"
              type="text"
              class="chat-input"
              placeholder="메시지를 입력하세요..."
              maxlength="500">
            <button @click="sendMessage" class="btn-send-message">
              전송
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import apiClient from '@/api';

// LiveKit 타입 정의
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

interface ChatMessage {
  id: string;
  sender: string;
  content: string;
  timestamp: number;
  isOwn: boolean;
}

// --- Router ---
const route = useRoute();
const router = useRouter();
const groupId = (route.query.groupId as string) || 'default-room';

// --- Auth Store ---
import { useAuthStore } from '@/stores/auth';
const authStore = useAuthStore();

// --- Reactive State ---
const hasJoined = ref(false);
const isConnecting = ref(false);
const canJoin = ref(false);

// 미디어 상태
const localVideo = ref<HTMLVideoElement | null>(null);
const localVideoElement = ref<HTMLVideoElement | null>(null);
const localCameraThumbnail = ref<HTMLVideoElement | null>(null);
const isAudioEnabled = ref(true);
const isVideoEnabled = ref(true);
const isScreenSharing = ref(false);

// 연결 상태
const connectionState = ref<'disconnected' | 'connecting' | 'connected' | 'reconnecting'>('disconnected');
const connectionStatus = ref<ConnectionStatus | null>(null);

// LiveKit 관련 - non-reactive storage for WebRTC objects
let livekitRoom: any = null;

// UI state only (reactive)
const remoteParticipants = ref<RemoteParticipant[]>([]);
const participantVideoRefs = ref<Map<string, HTMLVideoElement>>(new Map());

// 채팅 상태
const newMessage = ref('');
const chatMessages = ref<ChatMessage[]>([]);
const chatMessagesContainer = ref<HTMLElement | null>(null);

// --- Computed Properties ---
const totalParticipants = computed(() => {
  return remoteParticipants.value.length + 1;
});

const userNickname = computed(() => {
  return authStore.user?.nickname || '익명';
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

// 상태 아이콘 반환 함수
function getStatusIcon(type: string): string {
  switch (type) {
    case 'info': return 'bi bi-info-circle-fill';
    case 'success': return 'bi bi-check-circle-fill';
    case 'warning': return 'bi bi-exclamation-triangle-fill';
    case 'error': return 'bi bi-x-circle-fill';
    default: return 'bi bi-info-circle-fill';
  }
}

function setParticipantVideoRef(el: any, identity: string) {
  if (el && el instanceof HTMLVideoElement) {
    participantVideoRefs.value.set(identity, el);
  }
}

// --- LiveKit Functions ---
async function getAccessToken(): Promise<{ url: string, token: string}> {
  try {
    const userName = authStore.user?.nickname || `User_${Date.now()}`;
    
    console.log('토큰 발급 요청:', {
      groupId,
      userName,
      endpoint: `/api/v1/groups/${groupId}/rtc/token`
    });

    const response = await apiClient.post(`/api/v1/groups/${groupId}/rtc/token`, {
      userName
    });

    console.log('토큰 발급 응답:', response.data);

    const data = response.data.data ?? response.data;
    if (!data?.token || !data?.url) {
      throw new Error('응답에 url/token 없음');
    }
    
    console.log('토큰 발급 성공:', { url: data.url, hasToken: !!data.token });
    return { url: data.url, token: data.token };
    
  } catch (error) {
    console.error('토큰 발급 실패:', error);
    
    if (error.response?.status === 500) {
      console.error('서버 내부 오류 상세:', error.response?.data);
      console.error('백엔드 LiveKit 설정 확인이 필요합니다:');
      console.error('1. LiveKit 서버 URL이 올바른지 확인');
      console.error('2. LiveKit API Key/Secret이 올바른지 확인');
      console.error('3. LiveKit 서버가 실행 중인지 확인');
      throw new Error('LiveKit 서버 연결에 문제가 있습니다. 잠시 후 다시 시도해주세요.');
    } else if (error.response?.status === 404) {
      throw new Error('그룹을 찾을 수 없습니다. 그룹 ID를 확인해주세요.');
    } else if (error.response?.status === 403) {
      throw new Error('이 그룹에 접근할 권한이 없습니다.');
    } else if (error.code === 'NETWORK_ERROR' || error.code === 'ERR_NETWORK') {
      throw new Error('네트워크 연결을 확인해주세요.');
    }
    
    throw error;
  }
}

async function setupLocalMedia() {
  try {
    // 초기 로딩 상태 표시
    connectionStatus.value = {
      type: 'info',
      message: '카메라와 마이크 권한을 확인하고 있습니다...'
    };

    const stream = await navigator.mediaDevices.getUserMedia({
      video: { width: 1280, height: 720 },
      audio: { echoCancellation: true, noiseSuppression: true }
    });

    if (localVideo.value) {
      localVideo.value.srcObject = stream;
    }

    canJoin.value = true;
    connectionStatus.value = {
      type: 'success',
      message: '카메라와 마이크가 준비되었습니다.'
    };
  } catch (error) {
    console.error('미디어 접근 실패:', error);
    connectionStatus.value = {
      type: 'warning',
      message: '카메라/마이크에 접근할 수 없습니다. 오디오만으로 참여할 수 있습니다.'
    };
    canJoin.value = true; // 미디어 없이도 입장 허용
  }
}

async function joinRoom() {
  if (isConnecting.value) return;

  isConnecting.value = true;
  connectionState.value = 'connecting';

  try {
    // LiveKit 연결 정보 가져오기
    connectionStatus.value = {
      type: 'info',
      message: '서버 연결 정보를 가져오는 중...'
    };

    const { url, token } = await getAccessToken();

    connectionStatus.value = {
      type: 'info',
      message: 'LiveKit 서버에 연결하는 중...'
    };

    // LiveKit Room 생성 및 연결
    const { Room } = window.LivekitClient;
    livekitRoom = new Room({
      adaptiveStream: true,
      dynacast: true,
      videoCaptureDefaults: {
        resolution: {
          width: 1280,
          height: 720
        },
        facingMode: 'user'
      },
      audioCaptureDefaults: {
        autoGainControl: false,
        noiseSuppression: false,
        echoCancellation: false
      }
    });

    // 룸 이벤트 리스너 설정
    setupRoomEventListeners();

    // 룸 연결
    await livekitRoom.connect(url, token);

    // UI 전환
    hasJoined.value = true;
    connectionState.value = 'connected';
    connectionStatus.value = null;

    // DOM 업데이트 대기
    await nextTick();

    // 로컬 미디어 발행
    setTimeout(async () => {
      await publishLocalMedia();
    }, 100);

  } catch (error: any) {
    console.error('룸 입장 실패:', error);
    
    let errorMessage = '룸 입장에 실패했습니다.';
    
    if (error.message?.includes('LiveKit 토큰')) {
      errorMessage = error.message;
    } else if (error.message?.includes('서버에서')) {
      errorMessage = error.message;
    } else if (error.response?.status === 500) {
      errorMessage = 'LiveKit 서버 설정에 문제가 있습니다. 관리자에게 문의해주세요.';
    } else if (error.name === 'ConnectError') {
      errorMessage = 'LiveKit 서버에 연결할 수 없습니다. 네트워크 상태를 확인해주세요.';
    } else if (error.message?.includes('token')) {
      errorMessage = '인증 토큰에 문제가 있습니다. 다시 시도해주세요.';
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    connectionStatus.value = {
      type: 'error',
      message: errorMessage
    };
    connectionState.value = 'disconnected';
    
    // 5초 후 에러 메시지 자동 제거
    setTimeout(() => {
      if (connectionStatus.value?.type === 'error') {
        connectionStatus.value = null;
      }
    }, 5000);
    
  } finally {
    isConnecting.value = false;
  }
}

function setupRoomEventListeners() {
  if (!livekitRoom || !window.LivekitClient) return;

  const { RoomEvent } = window.LivekitClient;

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
  livekitRoom.on(RoomEvent.LocalTrackPublished, (publication: any) => {
    console.log('로컬 트랙 발행:', publication.kind, publication.source);
    
    // 화면공유 트랙 발행 시 상태 업데이트
    if (publication.kind === 'video' && 
        (publication.source === 'screen_share' || 
         publication.name === 'screen_share')) {
      console.log('✅ 화면공유 트랙 발행됨');
      isScreenSharing.value = true;
    }
    
    if (publication.kind === 'video') {
      // 로비 비디오 스트림을 중단하고 LiveKit 트랙으로 교체
      if (localVideo.value?.srcObject && publication.source === 'camera') {
        const stream = localVideo.value.srcObject as MediaStream;
        stream.getTracks().forEach(track => track.stop());
        localVideo.value.srcObject = null;
      }

      // 비디오 엘리먼트에 연결 (카메라 또는 화면공유 모두 처리)
      const attachVideoTrack = () => {
        if (publication.track && localVideoElement.value) {
          try {
            publication.track.attach(localVideoElement.value);
            console.log(`로컬 ${publication.source} 트랙이 localVideoElement에 연결되었습니다.`);
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

  // 로컬 트랙 해제 이벤트
  livekitRoom.on(RoomEvent.LocalTrackUnpublished, (publication: any) => {
    console.log('로컬 트랙 해제:', publication.kind, publication.source);
    
    // 화면공유 트랙 해제 시 상태 업데이트
    if (publication.kind === 'video' && 
        (publication.source === 'screen_share' || 
         publication.name === 'screen_share')) {
      console.log('✅ 화면공유 트랙 해제됨');
      isScreenSharing.value = false;
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

  // 데이터 메시지 수신 이벤트 (채팅)
  livekitRoom.on(RoomEvent.DataReceived, (payload: any, participant: any) => {
    try {
      const decoder = new TextDecoder();
      const messageStr = decoder.decode(payload);
      const messageData = JSON.parse(messageStr);

      if (messageData.type === 'chat') {
        // 채팅 메시지 수신
        const chatMessage: ChatMessage = {
          id: messageData.id,
          sender: messageData.senderNickname || participant.identity,
          content: messageData.content,
          timestamp: messageData.timestamp,
          isOwn: false
        };

        chatMessages.value.push(chatMessage);
        scrollToBottom();
      }
    } catch (error) {
      console.error('데이터 메시지 파싱 실패:', error);
    }
  });

  // 재연결 이벤트
  livekitRoom.on(RoomEvent.Reconnecting, () => {
    connectionState.value = 'reconnecting';
    connectionStatus.value = {
      type: 'warning',
      message: '연결이 불안정합니다. 재연결을 시도하고 있습니다...'
    };
  });

  livekitRoom.on(RoomEvent.Reconnected, () => {
    connectionState.value = 'connected';
    connectionStatus.value = {
      type: 'success',
      message: '연결이 복구되었습니다.'
    };
    // 3초 후 메시지 자동 숨김
    setTimeout(() => {
      connectionStatus.value = null;
    }, 3000);
  });
}

async function publishLocalMedia() {
  if (!livekitRoom) return;

  try {
    await nextTick();

    // 카메라와 마이크 활성화
    await livekitRoom.localParticipant.enableCameraAndMicrophone();

    console.log('로컬 미디어 발행 완료');

  } catch (error) {
    console.error('로컬 미디어 발행 실패:', error);
  }
}

// 원래 카메라 스트림으로 복구하는 함수
async function restoreCameraStream() {
  try {
    if (localVideoElement.value) {
      // 현재 화면 공유 스트림 정리
      const currentStream = localVideoElement.value.srcObject as MediaStream;
      if (currentStream) {
        currentStream.getTracks().forEach(track => {
          console.log('화면 공유 트랙 정지:', track.kind);
          track.stop();
        });
      }

      // 비디오 요소 속성 복구
      localVideoElement.value.controls = false;
      localVideoElement.value.muted = true;
      localVideoElement.value.autoplay = true;
      localVideoElement.value.playsInline = true;

      if (localVideo.value?.srcObject) {
        // 로비에서 사용하던 카메라 스트림으로 복구
        console.log('로비 카메라 스트림으로 복구 중...');
        localVideoElement.value.srcObject = localVideo.value.srcObject;

        // 비디오 재생 시작
        try {
          await localVideoElement.value.play();
          console.log('원래 카메라 스트림으로 복구 및 재생 성공');
        } catch (playError) {
          console.warn('카메라 비디오 자동 재생 실패:', playError);
        }
      } else {
        // 새로운 카메라 스트림 생성
        console.log('새로운 카메라 스트림 생성 중...');
        const cameraStream = await navigator.mediaDevices.getUserMedia({
          video: { width: 1280, height: 720 },
          audio: true
        });

        localVideoElement.value.srcObject = cameraStream;

        // 비디오 재생 시작
        try {
          await localVideoElement.value.play();
          console.log('새로운 카메라 스트림 생성, 복구 및 재생 성공');
        } catch (playError) {
          console.warn('새로운 카메라 비디오 자동 재생 실패:', playError);
        }
      }
    }
  } catch (error) {
    console.error('카메라 스트림 복구 실패:', error);
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
  if (hasJoined.value && livekitRoom) {
    // 입장 후에는 LiveKit을 통해 제어
    await toggleMicrophone();
  } else {
    // 입장 전에는 로컬 스트림 제어
    if (localVideo.value?.srcObject) {
      const stream = localVideo.value.srcObject as MediaStream;
      const audioTrack = stream.getAudioTracks()[0];
      if (audioTrack) {
        audioTrack.enabled = !audioTrack.enabled;
        isAudioEnabled.value = audioTrack.enabled;
      }
    }
  }
}

async function toggleVideo() {
  if (hasJoined.value && livekitRoom) {
    // 입장 후에는 LiveKit을 통해 제어
    await toggleCamera();
  } else {
    // 입장 전에는 로컬 스트림 제어
    if (localVideo.value?.srcObject) {
      const stream = localVideo.value.srcObject as MediaStream;
      const videoTrack = stream.getVideoTracks()[0];
      if (videoTrack) {
        videoTrack.enabled = !videoTrack.enabled;
        isVideoEnabled.value = videoTrack.enabled;
      }
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
  } catch (error) {
    console.error('카메라 토글 실패:', error);
  }
}

async function toggleScreenShare() {
  console.log('=== 화면공유 토글 시작 ===');
  console.log('LiveKit Room 상태:', !!livekitRoom);
  console.log('현재 화면공유 상태:', isScreenSharing.value);
  console.log('현재 URL 프로토콜:', window.location.protocol);
  
  if (!livekitRoom) {
    console.error('❌ LiveKit Room이 연결되지 않았습니다.');
    connectionStatus.value = {
      type: 'error',
      message: 'WebRTC 연결이 필요합니다. 먼저 방에 입장해주세요.'
    };
    return;
  }

  // HTTPS 환경 확인 (개발 환경에서는 경고만 표시)
  if (window.location.protocol !== 'https:' && 
      window.location.hostname !== 'localhost' && 
      window.location.hostname !== '127.0.0.1') {
    console.warn('⚠️ HTTPS 환경이 아닙니다. 일부 브라우저에서 화면공유가 제한될 수 있습니다.');
    connectionStatus.value = {
      type: 'warning',
      message: 'HTTPS 환경에서 더 안정적인 화면공유가 가능합니다.'
    };
    // return을 제거하여 계속 진행
  }

  try {
    const enabled = !isScreenSharing.value;
    console.log('목표 상태:', enabled ? '시작' : '종료');

    if (enabled) {
      console.log('🖥️ 화면 공유 시작 프로세스...');
      
      // 브라우저 지원 확인
      if (!navigator.mediaDevices || !navigator.mediaDevices.getDisplayMedia) {
        throw new Error('이 브라우저는 화면 공유를 지원하지 않습니다.');
      }

      console.log('브라우저 지원 확인 완료');
      
      // LiveKit SDK 로드 상태 확인
      console.log('LiveKit SDK 상태:');
      console.log('- window.LivekitClient:', !!window.LivekitClient);
      console.log('- TrackSource:', window.LivekitClient?.TrackSource);
      console.log('- 사용 가능한 속성들:', Object.keys(window.LivekitClient?.TrackSource || {}));
      
      // 직접 화면공유 스트림 획득하여 LiveKit에 전달
      console.log('화면공유 스트림 요청...');
      const screenStream = await navigator.mediaDevices.getDisplayMedia({ 
        video: {
          width: { ideal: 1920, max: 1920 },
          height: { ideal: 1080, max: 1080 },
          frameRate: { ideal: 15, max: 30 }
        },
        audio: true
      });
      
      console.log('화면공유 스트림 획득 성공:', screenStream);
      
      const videoTrack = screenStream.getVideoTracks()[0];
      const audioTrack = screenStream.getAudioTracks()[0];
      
      // 스트림 상세 정보 로그
      console.log('비디오 트랙 정보:', {
        id: videoTrack?.id,
        label: videoTrack?.label,
        enabled: videoTrack?.enabled,
        muted: videoTrack?.muted,
        readyState: videoTrack?.readyState,
        settings: videoTrack?.getSettings?.()
      });
      
      console.log('오디오 트랙 정보:', {
        id: audioTrack?.id,
        label: audioTrack?.label,
        enabled: audioTrack?.enabled,
        muted: audioTrack?.muted,
        readyState: audioTrack?.readyState
      });
      
      // HTTP 환경 경고
      if (window.location.protocol === 'http:') {
        console.warn('⚠️ HTTP 환경에서는 화면공유가 제한될 수 있습니다.');
        console.warn('브라우저에서 화면공유를 허용했지만 실제 화면 데이터가 차단될 수 있습니다.');
        connectionStatus.value = {
          type: 'warning',
          message: 'HTTP 환경에서는 화면이 검은색으로 나올 수 있습니다. HTTPS 환경을 권장합니다.'
        };
      }
      
      // 화면공유가 종료되면 자동 정리
      if (videoTrack) {
        videoTrack.addEventListener('ended', () => {
          console.log('사용자가 화면공유를 종료함');
          isScreenSharing.value = false;
        });
      }
      
      // 트랙 활성화 검증 - 임시 비디오 엘리먼트로 테스트
      console.log('화면공유 트랙 활성화 검증...');
      if (videoTrack) {
        const testVideo = document.createElement('video');
        testVideo.muted = true;
        testVideo.autoplay = true;
        testVideo.srcObject = new MediaStream([videoTrack]);
        
        try {
          await testVideo.play();
          console.log('✅ 비디오 트랙 활성화 검증 완료');
          testVideo.srcObject = null;
        } catch (error) {
          console.warn('⚠️ 비디오 트랙 활성화 검증 실패:', error);
        }
      }
      
      // 스트림 안정화를 위한 짧은 대기
      console.log('화면공유 스트림 안정화 대기...');
      await new Promise(resolve => setTimeout(resolve, 200));
      
      // 스트림 상태 재검증
      if (videoTrack && videoTrack.readyState === 'ended') {
        throw new Error('화면공유 스트림이 이미 종료되었습니다.');
      }
      
      console.log('스트림 준비 완료, LiveKit 퍼블리시 진행...');
      
      // LiveKit을 통해 스트림 퍼블리시
      console.log('LiveKit을 통한 화면공유 트랙 퍼블리시...');
      
      // 기존 카메라 트랙 일시 중지
      await livekitRoom.localParticipant.setCameraEnabled(false);
      
      // 화면공유 트랙 퍼블리시 (문자열 기반)
      if (videoTrack) {
        console.log('화면공유 비디오 트랙 퍼블리시...');
        await livekitRoom.localParticipant.publishTrack(videoTrack, {
          source: 'screen_share',
          name: 'screen_share'
        });
        console.log('✅ 화면공유 비디오 트랙 퍼블리시 완료');
      }
      
      // 오디오 트랙이 있으면 함께 퍼블리시 (silence detection 비활성화)
      if (audioTrack) {
        console.log('화면공유 오디오 트랙 퍼블리시...');
        await livekitRoom.localParticipant.publishTrack(audioTrack, {
          source: 'screen_share_audio',
          name: 'screen_share_audio',
          dtx: false,  // DTX (Discontinuous Transmission) 비활성화
          red: false   // RED (Redundant Encoding) 비활성화
        });
        console.log('✅ 화면공유 오디오 트랙 퍼블리시 완료');
      }
      
      console.log('✅ 화면 공유 트랙 퍼블리시 완료');
      
    } else {
      console.log('🛑 화면 공유 종료 프로세스...');
      
      // 화면공유 트랙들을 직접 찾아서 중지
      const participant = livekitRoom.localParticipant;
      
      console.log('현재 발행된 트랙들 확인:');
      
      // 안전한 트랙 접근
      const videoTracks = participant.videoTracks || new Map();
      const audioTracks = participant.audioTracks || new Map();
      
      console.log('- 비디오 트랙:', Array.from(videoTracks.values()).map(p => ({
        source: p.source,
        name: p.name,
        kind: p.kind
      })));
      console.log('- 오디오 트랙:', Array.from(audioTracks.values()).map(p => ({
        source: p.source,
        name: p.name,
        kind: p.kind
      })));
      
      // 화면공유 비디오 트랙 중지
      for (const [, publication] of videoTracks) {
        if (publication.source === 'screen_share' ||
            publication.name === 'screen_share') {
          console.log('화면공유 비디오 트랙 중지:', publication.name);
          try {
            await participant.unpublishTrack(publication.track);
            publication.track?.stop();
            console.log('✅ 비디오 트랙 중지 완료');
          } catch (error) {
            console.error('비디오 트랙 중지 실패:', error);
          }
        }
      }
      
      // 화면공유 오디오 트랙 중지
      for (const [, publication] of audioTracks) {
        if (publication.source === 'screen_share_audio' ||
            publication.name === 'screen_share_audio') {
          console.log('화면공유 오디오 트랙 중지:', publication.name);
          try {
            await participant.unpublishTrack(publication.track);
            publication.track?.stop();
            console.log('✅ 오디오 트랙 중지 완료');
          } catch (error) {
            console.error('오디오 트랙 중지 실패:', error);
          }
        }
      }
      
      // 카메라 다시 활성화 (비디오가 켜져있던 경우)
      if (isVideoEnabled.value) {
        await participant.setCameraEnabled(true);
      }
      
      console.log('✅ 화면 공유 종료 완료');
    }

    // 상태는 이벤트 리스너에서 자동으로 업데이트됨
    console.log('화면 공유 처리 완료, 현재 상태:', isScreenSharing.value);
    
  } catch (error) {
    console.error('❌ 화면 공유 실패:', error);
    console.error('에러 상세:', {
      name: error.name,
      message: error.message,
      stack: error.stack
    });
    
    isScreenSharing.value = false;
    
    let errorMessage = '화면 공유를 시작할 수 없습니다.';
    
    if (error.name === 'NotAllowedError') {
      errorMessage = '화면 공유 권한이 거부되었습니다. 팝업을 허용하고 다시 시도해주세요.';
    } else if (error.name === 'NotSupportedError') {
      errorMessage = '이 브라우저는 화면 공유를 지원하지 않습니다.';
    } else if (error.name === 'NotFoundError') {
      errorMessage = '공유할 화면을 찾을 수 없습니다.';
    } else if (error.name === 'AbortError') {
      errorMessage = '화면 공유가 취소되었습니다.';
    } else if (error.message?.includes('Permission')) {
      errorMessage = '화면 공유 권한이 필요합니다. 브라우저 설정을 확인해주세요.';
    } else if (error.message) {
      errorMessage = `화면 공유 오류: ${error.message}`;
    }
    
    connectionStatus.value = {
      type: 'error',
      message: errorMessage
    };
    
    setTimeout(() => {
      if (connectionStatus.value?.type === 'error') {
        connectionStatus.value = null;
      }
    }, 7000);
  }
  
  console.log('=== 화면공유 토글 종료 ===');
}

function goToBookEditor() {
  try {
    // 그룹 책 에디터 페이지를 새창으로 열기
    const bookEditorUrl = window.location.origin + '/group-book-editor';
    window.open(bookEditorUrl, '_blank', 'noopener,noreferrer');
  } catch (error) {
    console.error('책 에디터로 이동 실패:', error);
  }
}

async function leaveRoom() {
  try {
    // 그룹 세션 종료
    const groupId = route.query.groupId;
    if (groupId) {
      try {
        const { groupService } = await import('@/services/groupService');
        await groupService.endGroupBookSession(parseInt(groupId.toString()));
      } catch (sessionError) {
        console.error('그룹 세션 종료 실패:', sessionError);
      }
    }

    if (livekitRoom) {
      await livekitRoom.disconnect();
      livekitRoom = null;
    }

    // 로컬 미디어 정리
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

// --- Chat Functions ---

async function sendMessage() {
  const message = newMessage.value.trim();
  if (!message || !livekitRoom) {
    return;
  }

  try {
    // 메시지 객체 생성
    const chatMessage = {
      type: 'chat',
      id: `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      content: message,
      timestamp: Date.now(),
      senderNickname: userNickname.value
    };

    // 다른 참여자들에게 전송
    const encoder = new TextEncoder();
    const data = encoder.encode(JSON.stringify(chatMessage));
    await livekitRoom.localParticipant.publishData(data, {
      reliable: true
    });

    // 로컬에도 메시지 추가
    const localChatMessage: ChatMessage = {
      ...chatMessage,
      sender: userNickname.value,
      isOwn: true
    };

    chatMessages.value.push(localChatMessage);
    newMessage.value = '';
    scrollToBottom();

  } catch (error) {
    console.error('메시지 전송 실패:', error);
  }
}

function formatTime(timestamp: number): string {
  const date = new Date(timestamp);
  const now = new Date();
  const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

  if (diffInSeconds < 60) {
    return '방금 전';
  } else if (diffInSeconds < 3600) {
    const minutes = Math.floor(diffInSeconds / 60);
    return `${minutes}분 전`;
  } else if (diffInSeconds < 86400) {
    const hours = Math.floor(diffInSeconds / 3600);
    return `${hours}시간 전`;
  } else {
    return date.toLocaleDateString('ko-KR', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (chatMessagesContainer.value) {
      chatMessagesContainer.value.scrollTop = chatMessagesContainer.value.scrollHeight;
    }
  });
}

// --- Lifecycle Hooks ---
onMounted(async () => {
  // LiveKit SDK 로드
  if (!window.LivekitClient) {
    try {
      const script = document.createElement('script');
      script.src = 'https://unpkg.com/livekit-client/dist/livekit-client.umd.js';
      script.onload = () => {
        console.log('LiveKit SDK 로드 완료');
        setupLocalMedia();
      };
      script.onerror = () => {
        console.error('LiveKit SDK 로드 실패');
        connectionStatus.value = {
          type: 'error',
          message: 'LiveKit SDK를 로드할 수 없습니다.'
        };
      };
      document.head.appendChild(script);
    } catch (error) {
      console.error('LiveKit SDK 로드 오류:', error);
    }
  } else {
    await setupLocalMedia();
  }
});

onUnmounted(() => {
  // 정리 작업
  cleanup();
});

// 페이지 언로드 시에도 세션 종료
const cleanup = async () => {
  const groupId = route.query.groupId;
  if (groupId) {
    try {
      const { groupService } = await import('@/services/groupService');
      await groupService.endGroupBookSession(parseInt(groupId.toString()));
    } catch (error) {
      console.error('페이지 종료 시 그룹 세션 정리 실패:', error);
    }
  }

  if (livekitRoom) {
    livekitRoom.disconnect();
    livekitRoom = null;
  }

  if (localVideo.value?.srcObject) {
    const stream = localVideo.value.srcObject as MediaStream;
    stream.getTracks().forEach(track => track.stop());
  }
};

// beforeunload 이벤트 리스너 추가 (브라우저 종료/새로고침 시)
window.addEventListener('beforeunload', cleanup);
</script>

<style scoped>
@import '../../styles/group-book-creation.css';
</style>
