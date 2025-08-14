<template>
  <div class="main-layout">
    <!-- 상단 제어 패널 -->
    <div class="control-panel">
      <div class="room-info">
        <div class="room-status">
          <div class="status-indicator" :class="connectionState"></div>
          <span class="room-name">{{ groupName || 'Group Book Room' }}</span>
          <span class="participant-count">({{ remoteParticipants.length + 1 }})</span>
        </div>
        <div v-if="connectionStatus" class="connection-status" :class="connectionStatus.type">
          {{ connectionStatus.message }}
        </div>
      </div>

      <div class="control-buttons">
        <button @click="toggleAudio" 
                class="control-btn" 
                :class="{ active: isAudioEnabled }"
                :title="isAudioEnabled ? '마이크 끄기' : '마이크 켜기'">
          <i class="bi" :class="isAudioEnabled ? 'bi-mic-fill' : 'bi-mic-mute-fill'"></i>
        </button>

        <button @click="toggleVideo" 
                class="control-btn" 
                :class="{ active: isVideoEnabled }"
                :title="isVideoEnabled ? '카메라 끄기' : '카메라 켜기'">
          <i class="bi" :class="isVideoEnabled ? 'bi-camera-video-fill' : 'bi-camera-video-off-fill'"></i>
        </button>

        <button @click="startScreenShare" 
                class="control-btn" 
                :class="{ active: isScreenSharing }"
                title="화면 공유">
          <i class="bi bi-display"></i>
        </button>

        <button @click="leaveRoom" 
                class="control-btn danger"
                title="나가기">
          <i class="bi bi-telephone-x-fill"></i>
        </button>
      </div>
    </div>

    <!-- 메인 콘텐츠 영역 -->
    <div class="content-layout">
      <!-- 비디오 영역 -->
      <div class="video-section">
        <!-- 로컬 비디오 -->
        <div class="video-container local-video">
          <video ref="localVideo" autoplay muted playsinline class="video-element"></video>
          <div class="video-overlay">
            <span class="participant-name">You</span>
          </div>
        </div>

        <!-- 원격 참여자 비디오 -->
        <div v-for="participant in remoteParticipants" 
             :key="participant.identity"
             class="video-container remote-video">
          <video :ref="(el) => setParticipantVideoRef(el, participant.identity)"
                 autoplay playsinline 
                 class="video-element"></video>
          <div class="video-overlay">
            <span class="participant-name">{{ participant.identity }}</span>
            <div class="participant-status">
              <i v-if="!participant.isMicrophoneEnabled" class="bi bi-mic-mute-fill muted-indicator"></i>
              <span v-if="participant.connectionQuality !== undefined" 
                    class="connection-quality"
                    :class="getConnectionQualityClass(participant.connectionQuality)">
                {{ getConnectionQualityText(participant.connectionQuality) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 채팅 패널 -->
      <div class="chat-panel">
        <div class="chat-header">
          <h3>채팅</h3>
        </div>
        <div ref="chatContainer" class="chat-messages">
          <div v-for="message in chatMessages" 
               :key="message.id"
               class="message" 
               :class="{ own: message.isOwn }">
            <div class="message-sender">{{ message.sender }}</div>
            <div class="message-content">{{ message.content }}</div>
            <div class="message-time">{{ formatTime(message.timestamp) }}</div>
          </div>
        </div>
        <div class="chat-input">
          <input v-model="newMessage" 
                 @keyup.enter="sendMessage"
                 placeholder="메시지를 입력하세요..."
                 type="text">
          <button @click="sendMessage" :disabled="!newMessage.trim()">
            <i class="bi bi-send-fill"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- 시작 버튼 (입장 전) -->
    <div v-if="!hasJoined" class="start-overlay">
      <div class="start-content">
        <h2>그룹책 만들기</h2>
        <p>{{ groupName }}에서 함께 책을 만들어보세요.</p>
        <button @click="startCreateBook" class="start-btn">
          시작하기
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { groupService } from '@/services/groupService';

// --- Types ---
interface RemoteParticipant {
  identity: string;
  isMicrophoneEnabled: boolean;
  isCameraEnabled: boolean;
  videoTrack?: Record<string, unknown>;
  audioTrack?: Record<string, unknown>;
  connectionQuality?: number;
}

interface ChatMessage {
  id: string;
  sender: string;
  content: string;
  timestamp: Date;
  isOwn: boolean;
}

interface ConnectionStatus {
  type: 'info' | 'success' | 'warning' | 'error';
  message: string;
}

// --- Props & Router ---
const route = useRoute();
const router = useRouter();
const groupId = route.query.groupId as string;
const groupName = route.query.groupName as string;

// --- Reactive State ---
const hasJoined = ref(false);

// UI References
const localVideo = ref<HTMLVideoElement | null>(null);
const localVideoElement = ref<HTMLVideoElement | null>(null);
const chatContainer = ref<HTMLElement | null>(null);

// Media State
const isAudioEnabled = ref(true);
const isVideoEnabled = ref(true);
const isScreenSharing = ref(false);

// Connection State
const connectionState = ref<'disconnected' | 'connecting' | 'connected' | 'reconnecting'>('disconnected');
const connectionStatus = ref<ConnectionStatus | null>(null);

// Participants
const remoteParticipants = ref<RemoteParticipant[]>([]);
const participantVideoRefs = ref<Map<string, HTMLVideoElement>>(new Map());

// Chat
const chatMessages = ref<ChatMessage[]>([]);
const newMessage = ref('');

// LiveKit Room Instance (not reactive)
let livekitRoom: any = null;

// --- Utility Functions ---
const formatTime = (date: Date): string => {
  return date.toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit'
  });
};

const getConnectionQualityText = (quality: number): string => {
  switch (quality) {
    case 0: return '연결 불량';
    case 1: return '나쁨';
    case 2: return '보통';
    case 3: return '좋음';
    case 4: return '매우 좋음';
    default: return '';
  }
};

const getConnectionQualityClass = (quality: number): string => {
  if (quality <= 1) return 'poor';
  if (quality <= 2) return 'fair';
  if (quality <= 3) return 'good';
  return 'excellent';
};

const setParticipantVideoRef = (el: any, identity: string) => {
  if (el && el instanceof HTMLVideoElement) {
    participantVideoRefs.value.set(identity, el);
  }
};

// --- Core Functions ---
async function startCreateBook() {
  try {
    // 로컬 미디어 설정
    const stream = await navigator.mediaDevices.getUserMedia({
      video: { width: 1280, height: 720 },
      audio: { echoCancellation: true, noiseSuppression: true }
    });

    if (localVideo.value) {
      localVideo.value.srcObject = stream;
    }

    // LiveKit 룸 입장
    await joinRoom(groupId, handleDataReceived);
    
    hasJoined.value = true;
  } catch (error) {
    console.error('그룹책 세션 시작 실패:', error);
    connectionStatus.value = {
      type: 'error',
      message: '세션 시작에 실패했습니다. 다시 시도해주세요.'
    };
  }
}

async function joinRoom(groupId: string, onDataReceived?: (payload: any, participant: any) => void) {
  connectionState.value = 'connecting';

  try {
    if (!window.LivekitClient) {
      throw new Error('LiveKit SDK가 로드되지 않았습니다.');
    }

    const { Room } = window.LivekitClient;

    livekitRoom = new Room({
      adaptiveStream: true,
      dynacast: true,
      videoCaptureDefaults: {
        resolution: { width: 1280, height: 720 }
      }
    });

    setupRoomEventListeners(onDataReceived);

    const userName = `User_${Date.now()}`;
    const { url, token } = await groupService.getRTCToken(groupId, userName);
    await livekitRoom.connect(url, token);

    await publishLocalMedia();

    connectionState.value = 'connected';
    connectionStatus.value = null;

    return livekitRoom;
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
    throw error;
  }
}

function setupRoomEventListeners(onDataReceived?: (payload: any, participant: any) => void) {
  if (!livekitRoom || !window.LivekitClient) return;

  const { RoomEvent } = window.LivekitClient;

  livekitRoom.on(RoomEvent.ParticipantConnected, (participant: any) => {
    console.log('참여자 입장:', participant.identity);
    addRemoteParticipant(participant);
  });

  livekitRoom.on(RoomEvent.ParticipantDisconnected, (participant: any) => {
    console.log('참여자 퇴장:', participant.identity);
    removeRemoteParticipant(participant.identity);
  });

  livekitRoom.on(RoomEvent.TrackSubscribed, (track: any, publication: any, participant: any) => {
    console.log('트랙 구독:', track.kind, participant.identity);
    handleTrackSubscribed(track, participant);
  });

  livekitRoom.on(RoomEvent.TrackUnsubscribed, (track: any, publication: any, participant: any) => {
    console.log('트랙 구독 해제:', track.kind, participant.identity);
    handleTrackUnsubscribed(track, participant);
  });

  livekitRoom.on(RoomEvent.ConnectionQualityChanged, (quality: any, participant: any) => {
    updateParticipantConnectionQuality(participant.identity, quality);
  });

  livekitRoom.on(RoomEvent.ConnectionStateChanged, (state: any) => {
    console.log('연결 상태 변경:', state);
    connectionState.value = state;
  });

  if (onDataReceived) {
    livekitRoom.on(RoomEvent.DataReceived, onDataReceived);
  }

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
    setTimeout(() => {
      connectionStatus.value = null;
    }, 3000);
  });
}

function addRemoteParticipant(participant: any) {
  const newParticipant: RemoteParticipant = {
    identity: participant.identity as string,
    isMicrophoneEnabled: participant.isMicrophoneEnabled as boolean,
    isCameraEnabled: participant.isCameraEnabled as boolean,
    connectionQuality: undefined
  };

  remoteParticipants.value.push(newParticipant);

  participant.videoTracks.forEach((publication: any) => {
    if (publication.track) {
      handleTrackSubscribed(publication.track, participant);
    }
  });

  const audioTracks = participant.audioTracks as { track?: Record<string, unknown> }[];
  audioTracks?.forEach((publication) => {
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

function handleTrackSubscribed(track: Record<string, unknown>, participant: Record<string, unknown>) {
  const participantData = remoteParticipants.value.find(p => p.identity === (participant.identity as string));
  if (!participantData) return;

  if ((track.kind as string) === 'video') {
    participantData.videoTrack = track;
    
    setTimeout(() => {
      const videoElement = participantVideoRefs.value.get(participant.identity);
      if (videoElement) {
        (track.attach as (el: HTMLVideoElement) => void)(videoElement);
      }
    }, 100);
  } else if (track.kind === 'audio') {
    participantData.audioTrack = track;
    track.attach();
  }
}

function handleTrackUnsubscribed(track: Record<string, unknown>, participant: Record<string, unknown>) {
  const participantData = remoteParticipants.value.find(p => p.identity === (participant.identity as string));
  if (!participantData) return;

  if ((track.kind as string) === 'video') {
    participantData.videoTrack = undefined;
    (track.detach as () => void)();
  } else if ((track.kind as string) === 'audio') {
    participantData.audioTrack = undefined;
    (track.detach as () => void)();
  }
}

function updateParticipantConnectionQuality(identity: string, quality: number) {
  const participant = remoteParticipants.value.find(p => p.identity === identity);
  if (participant) {
    participant.connectionQuality = quality;
  }
}

async function publishLocalMedia() {
  if (!livekitRoom) return;

  try {
    if (isVideoEnabled.value) {
      await livekitRoom.localParticipant.enableCameraAndMicrophone();
    } else {
      await livekitRoom.localParticipant.enableMicrophone();
    }
    console.log('로컬 미디어 퍼블리시 완료');
  } catch (error) {
    console.error('로컬 미디어 퍼블리시 실패:', error);
  }
}

async function toggleAudio() {
  if (hasJoined.value && livekitRoom) {
    await toggleMicrophone();
  } else {
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
    await toggleCamera();
  } else {
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
    await (livekitRoom as { localParticipant: { setMicrophoneEnabled: (enabled: boolean) => Promise<void> } }).localParticipant.setMicrophoneEnabled(enabled);
    isAudioEnabled.value = enabled;
  } catch (error) {
    console.error('마이크 토글 실패:', error);
  }
}

async function toggleCamera() {
  if (!livekitRoom) return;

  try {
    const enabled = !isVideoEnabled.value;
    await (livekitRoom as { localParticipant: { setCameraEnabled: (enabled: boolean) => Promise<void> } }).localParticipant.setCameraEnabled(enabled);
    isVideoEnabled.value = enabled;
  } catch (error) {
    console.error('카메라 토글 실패:', error);
  }
}

async function startScreenShare() {
  console.log('🖥️ 화면 공유 시작 요청...');
  
  try {
    if (!livekitRoom) {
      throw new Error('LiveKit Room이 연결되지 않았습니다.');
    }
    
    if (isScreenSharing.value) {
      await livekitRoom.localParticipant.setScreenShareEnabled(false);
      
      if (isVideoEnabled.value) {
        await (livekitRoom as { localParticipant: { setCameraEnabled: (enabled: boolean) => Promise<void> } }).localParticipant.setCameraEnabled(true);
      }
      
      isScreenSharing.value = false;
      console.log('✅ 화면 공유 종료 완료');
    } else {
      const stream = await navigator.mediaDevices.getDisplayMedia({
        video: {
          width: { ideal: 1920 },
          height: { ideal: 1080 },
          frameRate: { ideal: 30 }
        },
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true
        }
      });
      
      const videoTrack = stream.getVideoTracks()[0];
      const audioTrack = stream.getAudioTracks()[0];
      
      if (!videoTrack) {
        throw new Error('화면 공유 비디오 트랙을 찾을 수 없습니다.');
      }
      
      await livekitRoom.localParticipant.setCameraEnabled(false);
      
      if (videoTrack) {
        await livekitRoom.localParticipant.publishTrack(videoTrack, {
          source: 'screen_share',
          name: 'screen_share'
        });
      }
      
      if (audioTrack) {
        await livekitRoom.localParticipant.publishTrack(audioTrack, {
          source: 'screen_share_audio',
          name: 'screen_share_audio',
          dtx: false,
          red: false
        });
      }
      
      isScreenSharing.value = true;
      console.log('✅ 화면 공유 시작 완료');
    }
  } catch (error: any) {
    console.error('❌ 화면 공유 처리 실패:', error);
    
    if (error.name === 'NotAllowedError') {
      console.log('사용자가 화면 공유를 취소했습니다.');
    } else {
      connectionStatus.value = {
        type: 'error',
        message: '화면 공유를 시작할 수 없습니다. 권한을 확인해주세요.'
      };
      
      setTimeout(() => {
        if (connectionStatus.value?.type === 'error') {
          connectionStatus.value = null;
        }
      }, 5000);
    }
  }
}

function handleDataReceived(payload: any, participant: any) {
  try {
    const decoder = new TextDecoder();
    const messageStr = decoder.decode(payload);
    const messageData = JSON.parse(messageStr);

    if (messageData.type === 'chat') {
      const chatMessage: ChatMessage = {
        id: messageData.id,
        sender: participant.identity,
        content: messageData.content,
        timestamp: new Date(messageData.timestamp),
        isOwn: false
      };

      chatMessages.value.push(chatMessage);
      scrollToBottom();
    }
  } catch (error) {
    console.error('데이터 메시지 파싱 실패:', error);
  }
}

function sendMessage() {
  if (newMessage.value.trim() && livekitRoom) {
    const message: ChatMessage = {
      id: Date.now().toString(),
      sender: 'You',
      content: newMessage.value.trim(),
      timestamp: new Date(),
      isOwn: true
    };

    chatMessages.value.push(message);

    const messageData = {
      type: 'chat',
      id: message.id,
      content: message.content,
      timestamp: message.timestamp.toISOString()
    };

    const encoder = new TextEncoder();
    const data = encoder.encode(JSON.stringify(messageData));

    livekitRoom.localParticipant.publishData(data);

    newMessage.value = '';
    nextTick(() => {
      scrollToBottom();
    });
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    }
  });
}

async function leaveRoom() {
  try {
    if (livekitRoom) {
      await livekitRoom.disconnect();
      livekitRoom = null;
    }

    connectionState.value = 'disconnected';
    remoteParticipants.value = [];
    participantVideoRefs.value.clear();

    router.push('/group-book-lobby');
  } catch (error) {
    console.error('퇴장 중 오류:', error);
  }
}

// --- Lifecycle ---
onMounted(async () => {
  if (route.query.mode === 'join') {
    console.log('기존 세션에 참여 모드');
    startCreateBook();
  } else {
    console.log('새 세션 생성 모드');
  }
});

const cleanup = () => {
  if (livekitRoom) {
    console.log('정리 작업: LiveKit 연결 해제');
    livekitRoom.disconnect();
    livekitRoom = null;
  }
};

window.addEventListener('beforeunload', cleanup);
</script>

<style scoped>
@import '../../styles/group-book-creation.css';
</style>