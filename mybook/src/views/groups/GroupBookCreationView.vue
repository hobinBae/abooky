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
    const userName = `User_${Date.now()}`;
    
    // 로컬 테스트를 위한 더미 토큰/URL 반환
    console.log('🔧 로컬 테스트 모드: 더미 토큰 사용');
    return { 
      url: 'ws://localhost:7880', 
      token: 'dummy-token-for-local-test' 
    };
    
    // 실제 API 호출은 주석 처리
    // const response = await apiClient.post(`/api/v1/groups/${groupId}/rtc/token`, {
    //   userName
    // });
    // 
    // const data = response.data.data ?? response.data;
    // if (!data?.token || !data?.url) {
    //   throw new Error('응답에 url/token 없음');
    // }
    // return { url: data.url, token: data.token };
  } catch (error) {
    console.error('토큰 발급 오류:', error);
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
    console.log('🔧 로컬 테스트 모드: 실제 LiveKit 연결 없이 진행');
    
    // 로컬 테스트를 위한 더미 연결 시뮬레이션
    await new Promise(resolve => setTimeout(resolve, 1500)); // 1.5초 로딩 시뮬레이션
    
    // 더미 livekitRoom 객체 생성 (기본 기능만)
    livekitRoom = {
      localParticipant: {
        identity: 'LocalUser',
        enableCameraAndMicrophone: async () => console.log('더미: 카메라/마이크 활성화'),
        enableMicrophone: async () => console.log('더미: 마이크 활성화'),
        setMicrophoneEnabled: async (enabled: boolean) => {
          console.log('더미: 마이크', enabled ? '활성화' : '비활성화');
          isAudioEnabled.value = enabled;
        },
        setCameraEnabled: async (enabled: boolean) => {
          console.log('더미: 카메라', enabled ? '활성화' : '비활성화');
          isVideoEnabled.value = enabled;
        },
        setScreenShareEnabled: async (enabled: boolean) => {
          console.log('더미: 화면공유', enabled ? '시작' : '중지');
          isScreenSharing.value = enabled;
        },
        publishData: async (data: Uint8Array) => {
          console.log('더미: 데이터 전송', data);
        }
      },
      disconnect: async () => {
        console.log('더미: 방 나가기');
      }
    };

    // UI 전환 먼저 수행
    hasJoined.value = true;
    connectionState.value = 'connected';
    connectionStatus.value = null;

    // DOM 업데이트 대기
    await nextTick();
    
    // DOM이 준비된 후 로컬 비디오를 워킹스페이스 영역으로 이동
    setTimeout(async () => {
      await publishLocalMedia();
    }, 100);

    // 더미 원격 참여자 추가 (테스트용)
    setTimeout(() => {
      addDummyRemoteParticipant();
    }, 2000);

    console.log('🔧 로컬 테스트 모드: 방 입장 완료 (더미 연결)');

  } catch (error: any) {
    console.error('룸 입장 실패:', error);
    connectionStatus.value = { 
      type: 'error', 
      message: `입장 실패: ${error?.message || '알 수 없는 오류가 발생했습니다'}` 
    };
    connectionState.value = 'disconnected';
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
    console.log('로컬 트랙 발행:', publication.kind);
    if (publication.kind === 'video') {
      // 로비 비디오 스트림을 중단하고 LiveKit 트랙으로 교체
      if (localVideo.value?.srcObject) {
        const stream = localVideo.value.srcObject as MediaStream;
        stream.getTracks().forEach(track => track.stop());
        localVideo.value.srcObject = null;
      }
      
      // 비디오 엘리먼트에 연결
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
          sender: participant.identity,
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
  console.log('🔧 로컬 테스트 모드: 더미 퍼블리시');
  
  try {
    // 잠시 기다린 후 DOM이 준비되었는지 확인
    await nextTick();
    
    console.log('localVideo.value:', !!localVideo.value);
    console.log('localVideoElement.value:', !!localVideoElement.value);
    console.log('localVideo stream:', !!localVideo.value?.srcObject);
    
    // 로컬 테스트에서는 단순히 로비 비디오를 메인 화면으로 복사
    if (localVideo.value?.srcObject && localVideoElement.value) {
      console.log('로비 비디오 스트림을 메인 화면에 복사');
      localVideoElement.value.srcObject = localVideo.value.srcObject;
      
      // 비디오 재생 시작
      try {
        await localVideoElement.value.play();
        console.log('로컬 비디오 재생 시작');
      } catch (playError) {
        console.warn('비디오 자동 재생 실패:', playError);
      }
    } else {
      console.warn('로비 비디오 스트림이 없거나 메인 비디오 엘리먼트가 없음');
      
      // 대안: 새로운 미디어 스트림 생성
      if (localVideoElement.value && isVideoEnabled.value) {
        console.log('새로운 미디어 스트림 생성 시도');
        try {
          const stream = await navigator.mediaDevices.getUserMedia({
            video: { width: 1280, height: 720 },
            audio: true
          });
          localVideoElement.value.srcObject = stream;
          await localVideoElement.value.play();
          console.log('새로운 미디어 스트림으로 비디오 시작');
        } catch (mediaError) {
          console.error('새로운 미디어 스트림 생성 실패:', mediaError);
        }
      }
    }
    
    console.log('더미 로컬 미디어 퍼블리시 완료');

  } catch (error) {
    console.error('로컬 미디어 퍼블리시 실패:', error);
  }
}

// 더미 원격 참여자 추가 함수 (로컬 테스트용)
function addDummyRemoteParticipant() {
  console.log('🔧 더미 원격 참여자 추가');
  
  const dummyParticipant: RemoteParticipant = {
    identity: '테스트유저1',
    isMicrophoneEnabled: true,
    isCameraEnabled: false, // 비디오 없는 참여자로 시뮬레이션
    connectionQuality: 3
  };
  
  remoteParticipants.value.push(dummyParticipant);
  
  // 5초 후 다른 참여자 추가
  setTimeout(() => {
    const dummyParticipant2: RemoteParticipant = {
      identity: '테스트유저2',
      isMicrophoneEnabled: false,
      isCameraEnabled: true,
      connectionQuality: 4
    };
    
    remoteParticipants.value.push(dummyParticipant2);
    console.log('🔧 두 번째 더미 참여자 추가');
  }, 5000);
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
    
    console.log('카메라 토글:', enabled ? '온' : '오프');
  } catch (error) {
    console.error('카메라 토글 실패:', error);
  }
}

async function toggleScreenShare() {
  if (!livekitRoom) return;

  try {
    const enabled = !isScreenSharing.value;
    
    if (enabled) {
      // 화면 공유 시작 시 카메라 끄기
      await livekitRoom.localParticipant.setScreenShareEnabled(true);
      await livekitRoom.localParticipant.setCameraEnabled(false);
    } else {
      // 화면 공유 종료 시 카메라 다시 켜기
      await livekitRoom.localParticipant.setScreenShareEnabled(false);
      if (isVideoEnabled.value) {
        await livekitRoom.localParticipant.setCameraEnabled(true);
      }
    }
    
    isScreenSharing.value = enabled;
    console.log('화면 공유:', enabled ? '시작' : '종료');
  } catch (error) {
    console.error('화면 공유 토글 실패:', error);
    connectionStatus.value = { 
      type: 'error', 
      message: '화면 공유를 시작할 수 없습니다. 권한을 확인해주세요.' 
    };
    // 5초 후 오류 메시지 자동 숨김
    setTimeout(() => {
      if (connectionStatus.value?.type === 'error') {
        connectionStatus.value = null;
      }
    }, 5000);
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
  console.log('🔧 로컬 테스트 모드: 채팅 메시지 전송');
  const message = newMessage.value.trim();
  console.log('메시지 내용:', message);
  
  if (!message) {
    console.log('메시지가 비어있음');
    return;
  }

  try {
    // 메시지 객체 생성 (로컬 테스트용)
    const chatMessage: ChatMessage = {
      id: `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      sender: 'LocalUser', // 더미 사용자 이름
      content: message,
      timestamp: Date.now(),
      isOwn: true
    };

    // 로컬에 메시지 추가
    chatMessages.value.push(chatMessage);

    // 입력 필드 초기화
    newMessage.value = '';

    // 채팅 스크롤을 아래로 이동
    scrollToBottom();

    // 로컬 테스트: 3초 후 더미 응답 메시지 추가
    setTimeout(() => {
      const dummyResponse: ChatMessage = {
        id: `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        sender: remoteParticipants.value[0]?.identity || '테스트유저1',
        content: `"${message}"에 대한 응답입니다! 👍`,
        timestamp: Date.now(),
        isOwn: false
      };
      
      chatMessages.value.push(dummyResponse);
      scrollToBottom();
      console.log('🔧 더미 응답 메시지 추가');
    }, 3000);

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
  console.log('🔧 로컬 테스트 모드: LiveKit SDK 로딩 건너뛰기');
  
  // LiveKit SDK 로드 건너뛰고 바로 로컬 미디어 설정
  await setupLocalMedia();
});

onUnmounted(() => {
  // 정리 작업
  if (livekitRoom) {
    livekitRoom.disconnect();
    livekitRoom = null;
  }


  if (localVideo.value?.srcObject) {
    const stream = localVideo.value.srcObject as MediaStream;
    stream.getTracks().forEach(track => track.stop());
  }
});
</script>

<style scoped>
@import '../../styles/group-book-creation.css';
</style>
