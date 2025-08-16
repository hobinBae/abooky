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
            <div v-if="!isScreenSharing && !hasRemoteScreenShare" class="video-grid" :class="`participants-${totalParticipants}`">
              <!-- 로컬 참여자 (나) -->
              <div class="video-participant local-participant">
                <video
                  ref="localVideoElement"
                  autoplay
                  muted
                  playsinline
                  class="participant-video">
                </video>
                <div v-if="!isVideoEnabled" class="video-off-overlay">
                  <i class="bi bi-camera-video-off-fill"></i>
                </div>
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
                  :ref="(el: unknown) => setParticipantVideoRef(el as HTMLVideoElement | null, participant.identity)"
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
                    <span v-if="participant.isScreenSharing" class="screen-sharing-badge">
                      <i class="bi bi-share-fill"></i>
                    </span>
                    <span v-if="participant.connectionQuality !== undefined" class="connection-quality">
                      {{ getConnectionQualityText(participant.connectionQuality) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 화면 공유 모드일 때 (내가 공유하거나 다른 사람이 공유할 때) -->
            <div v-else class="screen-share-layout">
              <!-- 메인 화면 공유 영역 -->
              <div class="main-screen-area">
                <video
                  ref="screenShareVideoElement"
                  autoplay
                  playsinline
                  class="main-screen-video">
                </video>
                <div class="main-screen-info">
                  <div class="sharing-indicator">
                    <i class="bi bi-share-fill me-2"></i>
                    {{ screenSharingParticipant }}의 화면 공유 중
                  </div>
                </div>
              </div>

              <!-- 오른쪽 썸네일 영역 -->
              <div class="thumbnails-area">
                <div class="thumbnails-container">
                  <!-- 내 카메라 썸네일 (화면공유 중이 아닌 경우에만) -->
                  <div v-if="!isScreenSharing" class="thumbnail-participant">
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

                  <!-- 원격 참여자 썸네일들 (화면공유하는 사람 제외하고 모든 참여자) -->
                  <div
                    v-for="participant in remoteParticipants"
                    :key="participant.identity + '_thumb'"
                    v-show="!participant.isScreenSharing"
                    class="thumbnail-participant">
                    <video
                      :ref="(el: unknown) => setParticipantVideoRef(el as HTMLVideoElement | null, participant.identity + '_thumb')"
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

                  <!-- 내가 화면공유 중일 때 내 카메라 썸네일 -->
                  <div v-if="isScreenSharing" class="thumbnail-participant">
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

              <!-- 개발/디버깅 모드에서만 표시되는 버튼 -->
              <button v-if="route.query.debug === 'true'" @click="diagnoseScreenSharingIssue" class="btn btn-control" style="background-color: #6c757d; border-color: #6c757d;">
                <i class="bi bi-bug-fill"></i>
                <span>화면공유 진단</span>
              </button>

              <button @click="leaveRoom" class="btn btn-control btn-leave">
                <i class="bi bi-box-arrow-right"></i>
                <span>나가기</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 채팅 섹션 -->
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
const toError = (e: unknown): Error => (e instanceof Error ? e : new Error(String(e)));
import * as LK from 'livekit-client';

import { ref, onMounted, computed, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';

// LiveKit 타입 정의
declare global { interface Window { LivekitClient: typeof import('livekit-client'); } }

// --- Interfaces ---
interface RemoteParticipant {
  identity: string;
  isMicrophoneEnabled: boolean;
  isCameraEnabled: boolean;
  videoTrack?: any;
  audioTrack?: any;
  connectionQuality?: LK.ConnectionQuality;
  isScreenSharing?: boolean;
  screenShareTrack?: any;
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
const screenShareVideoElement = ref<HTMLVideoElement | null>(null);
const isAudioEnabled = ref(true);
const isVideoEnabled = ref(true);
const isScreenSharing = ref(false);

// 연결 상태
const connectionState = ref<LK.ConnectionState>(window.LivekitClient.ConnectionState.Disconnected);
const connectionStatus = ref<ConnectionStatus | null>(null);

// LiveKit 관련 - non-reactive storage for WebRTC objects
let livekitRoom: LK.Room | null = null;

// UI state only (reactive)
const remoteParticipants = ref<RemoteParticipant[]>([]);
const participantVideoRefs = ref<Map<string, HTMLVideoElement>>(new Map());

// 채팅 상태
const newMessage = ref('');
const chatMessages = ref<ChatMessage[]>([]);
const chatMessagesContainer = ref<HTMLElement | null>(null);

// 모달 스크롤 관리
function preventBodyScroll() {
  document.body.style.overflow = 'hidden';
}

function restoreBodyScroll() {
  document.body.style.overflow = '';
}

// --- Computed Properties ---
const totalParticipants = computed(() => {
  return remoteParticipants.value.length + 1;
});

const userNickname = computed(() => {
  return authStore.user?.nickname || '익명';
});

const getConnectionStatusText = computed(() => {
  if (!window.LivekitClient) return '';
  const { ConnectionState } = window.LivekitClient;
  switch (connectionState.value) {
    case ConnectionState.Connected: return '연결됨';
    case ConnectionState.Connecting: return '연결 중';
    case ConnectionState.Reconnecting: return '재연결 중';
    case ConnectionState.Disconnected: return '연결 끊김';
    default: return '';
  }
});

// 원격 참여자가 화면공유 중인지 확인
const hasRemoteScreenShare = computed(() => {
  return remoteParticipants.value.some(p => p.isScreenSharing);
});

// 화면공유 중인 참여자 이름
const screenSharingParticipant = computed(() => {
  if (isScreenSharing.value) {
    return '나';
  }
  const sharingParticipant = remoteParticipants.value.find(p => p.isScreenSharing);
  return sharingParticipant ? sharingParticipant.identity : '';
});

// --- Helper Functions ---
function getConnectionQualityText(quality: LK.ConnectionQuality): string {
  const { ConnectionQuality } = LK;
  switch (quality) {
    case ConnectionQuality.Poor: return '연결 불량';
    case ConnectionQuality.Lost: return '연결 끊김';
    case ConnectionQuality.Good: return '좋음';
    case ConnectionQuality.Excellent: return '매우 좋음';
    default: return '';
  }
}

function getStatusIcon(type: string): string {
  switch (type) {
    case 'info': return 'bi bi-info-circle-fill';
    case 'success': return 'bi bi-check-circle-fill';
    case 'warning': return 'bi bi-exclamation-triangle-fill';
    case 'error': return 'bi bi-x-circle-fill';
    default: return 'bi bi-info-circle-fill';
  }
}

function setParticipantVideoRef(el: HTMLVideoElement | null, identity: string) {
  if (el && el instanceof HTMLVideoElement) {
    participantVideoRefs.value.set(identity, el);
  }
}

// 화면공유 진단 함수
function diagnoseScreenSharingIssue() {
  console.log('=== 화면공유 연결 상태 진단 ===');

  if (!livekitRoom) {
    console.error('❌ LiveKit Room이 연결되지 않음');
    return;
  }

  try {
    const room = livekitRoom as any;
    console.log('🔌 룸 연결 상태:', {
      isConnected: room.state === 'connected',
      roomState: room.state,
      remoteParticipantsCount: room.remoteParticipants?.size || 0
    });

    // 로컬 참여자 트랙 발행 상태 확인
    const localParticipant = room.localParticipant;
    if (localParticipant) {
      const allVideoTracks: LK.TrackPublication[] = Array.from(localParticipant.videoTrackPublications?.values() || []);
      const screenShareTracks = allVideoTracks.filter((pub: LK.TrackPublication) =>
        pub.source === LK.Track.Source.ScreenShare ||
        pub.trackName?.includes('screen')
      );

      console.log('📤 로컬 트랙 발행 상태:', {
        totalVideoTracks: allVideoTracks.length,
        screenShareTracks: screenShareTracks.length,
        screenShareDetails: screenShareTracks.map((pub: LK.TrackPublication) => ({
          source: pub.source,
          name: pub.trackName,
          trackId: pub.trackSid,
          enabled: pub.isEnabled,
          muted: pub.isMuted,
          published: !!pub.track
        }))
      });

      if (screenShareTracks.length === 0) {
        console.warn('⚠️ 로컬에서 화면공유 트랙이 발행되지 않음');
      } else {
        console.log('✅ 로컬에서 화면공유 트랙이 발행되어 있음');
      }
    }

    // 원격 참여자의 트랙 수신 상태 확인
    const remoteParticipants = room.remoteParticipants;
    if (remoteParticipants && remoteParticipants.size > 0) {
      console.log(`📥 원격 참여자 ${remoteParticipants.size}명의 트랙 수신 상태:`);

      remoteParticipants.forEach((participant: LK.RemoteParticipant, identity: string) => {
        const allVideoTracks = Array.from(participant.videoTrackPublications.values() || []);
        const receivedScreenShare = allVideoTracks.filter((pub: LK.TrackPublication) =>
          pub.source === window.LivekitClient.Track.Source.ScreenShare ||
          pub.trackName?.includes('screen')
        );

        console.log(`참여자 ${identity}:`, {
          totalVideoTracks: allVideoTracks.length,
          receivedScreenShareTracks: receivedScreenShare.length,
          allTracks: allVideoTracks.map((pub: LK.TrackPublication) => ({
            source: pub.source,
            name: pub.trackName,
            subscribed: pub.isSubscribed,
            hasTrack: !!pub.track
          }))
        });

        if (receivedScreenShare.length === 0) {
          console.warn(`❌ 참여자 ${identity}가 화면공유 트랙을 수신하지 못함`);
        } else {
          console.log(`✅ 참여자 ${identity}가 화면공유 트랙을 수신함`);
        }
      });
    }

    console.log('=== 화면공유 진단 완료 ===');

  } catch (error) {
    console.error('진단 중 오류 발생:', error);
  }
}

// 로컬 화면공유 시작 시 모든 참여자를 썸네일로 이동 (로컬 카메라 포함)
function moveAllParticipantsToThumbnailsForLocalScreenShare() {
  console.log('로컬 화면공유 시작 - 모든 참여자 카메라를 썸네일로 이동');

  // 1. 로컬 카메라를 썸네일로 이동
  if (localVideoElement.value?.srcObject) {
    console.log('로컬 카메라를 썸네일로 이동...');
    moveLocalCameraToThumbnail();
  }

  // 2. 모든 원격 참여자를 썸네일로 재연결
  console.log('원격 참여자들을 썸네일 모드로 전환...');
  remoteParticipants.value.forEach(participant => {
    if (participant.videoTrack && !participant.isScreenSharing) {
      try {
        console.log(`참여자 ${participant.identity}를 썸네일로 이동`);
        // 화면공유 모드에서 썸네일로 연결
        attachVideoTrack(participant.videoTrack, participant.identity, '카메라', false);
      } catch (error) {
        console.warn(`참여자 ${participant.identity} 썸네일 이동 실패:`, error);
      }
    }
  });
}

// 원격 화면공유 시작 시 모든 참여자를 썸네일로 이동 (안전성 강화)
function moveAllParticipantsToThumbnails() {
  console.log('원격 화면공유 시작 - 모든 참여자 카메라를 썸네일로 이동');

  // DOM 준비 대기 후 실행
  nextTick(() => {
    setTimeout(() => {
      attemptMoveToThumbnails();
    }, 200);
  });
}

function attemptMoveToThumbnails() {
  console.log('썸네일 이동 시도 시작...');

  // 1. 로컬 카메라를 썸네일로 이동
  if (!localVideoElement.value) {
    console.warn('localVideoElement가 아직 준비되지 않음 - 재시도');
    setTimeout(() => {
      attemptMoveToThumbnails();
    }, 300);
    return;
  }

  const hasStream = localVideoElement.value.srcObject !== null;
  const stream = localVideoElement.value.srcObject as MediaStream;
  const hasVideoTracks = stream && stream.getVideoTracks().length > 0;
  const isConnected = !localVideoElement.value.paused || localVideoElement.value.readyState >= 2;
  const hasVideo = localVideoElement.value.videoWidth > 0 && localVideoElement.value.videoHeight > 0;

  console.log('로컬 카메라 상태 확인:', {
    hasStream,
    hasVideoTracks,
    isConnected,
    hasVideo,
    readyState: localVideoElement.value.readyState,
    videoWidth: localVideoElement.value.videoWidth,
    videoHeight: localVideoElement.value.videoHeight,
    paused: localVideoElement.value.paused
  });

  if (hasStream || isConnected || hasVideo) {
    console.log('로컬 카메라를 썸네일로 이동...');
    moveLocalCameraToThumbnailAdvanced();
  } else {
    console.warn('로컬 카메라가 아직 연결되지 않음 - 나중에 처리됨');
  }

  // 2. 모든 원격 참여자를 썸네일로 재연결
  console.log('원격 참여자들을 썸네일 모드로 전환...');
  remoteParticipants.value.forEach(participant => {
    if (participant.videoTrack && !participant.isScreenSharing) {
      try {
        console.log(`참여자 ${participant.identity}를 썸네일로 이동`);
        // 화면공유 모드에서 썸네일로 연결
        attachVideoTrack(participant.videoTrack, participant.identity, '카메라', false);
      } catch (error) {
        console.warn(`참여자 ${participant.identity} 썸네일 이동 실패:`, error);
      }
    }
  });
}

// 개선된 로컬 카메라 썸네일 이동 함수 (안전성 강화)
function moveLocalCameraToThumbnailAdvanced() {
  console.log('개선된 로컬 카메라 썸네일 이동 시작...');

  const mainElement = localVideoElement.value;
  const thumbnailElement = localCameraThumbnail.value;

  if (!mainElement) {
    console.warn('localVideoElement가 없어 썸네일 이동 불가');
    return;
  }

  if (!thumbnailElement) {
    console.warn('localCameraThumbnail가 없어 썸네일 이동 불가 - DOM 업데이트 대기 후 재시도');
    setTimeout(() => {
      if (localCameraThumbnail.value) {
        moveLocalCameraToThumbnailAdvanced();
      }
    }, 500);
    return;
  }

  try {
    console.log('썸네일 이동 방법 결정 중...');

    // 방법 1: srcObject가 있는 경우 (일반 스트림)
    if (mainElement.srcObject) {
      console.log('방법 1: srcObject 방식으로 썸네일 이동');
      const stream = mainElement.srcObject as MediaStream;

      thumbnailElement.srcObject = stream;
      thumbnailElement.muted = true;
      thumbnailElement.autoplay = true;
      thumbnailElement.playsInline = true;

      thumbnailElement.play().catch(e => console.warn('썸네일 재생 실패:', e));
      mainElement.srcObject = null;

      console.log('✅ srcObject 방식으로 썸네일 이동 완료');
      return;
    }

    // 방법 2: LiveKit 트랙이 연결된 경우
    if (livekitRoom && livekitRoom.localParticipant &&
        livekitRoom.localParticipant.videoTrackPublications &&
        livekitRoom.localParticipant.videoTrackPublications.size > 0) {

      console.log('방법 2: LiveKit 트랙 방식으로 썸네일 이동 시도');
      const localParticipant = livekitRoom.localParticipant;

      const cameraTrack = Array.from(localParticipant.videoTrackPublications.values())
        .find((pub: LK.TrackPublication) => pub.source === window.LivekitClient.Track.Source.Camera);

      if (cameraTrack && cameraTrack.track) {
        console.log('LiveKit 카메라 트랙을 썸네일에 연결');

        // 메인에서 트랙 해제
        try {
          cameraTrack.track.detach(mainElement);
        } catch (detachError) {
          console.warn('메인에서 트랙 해제 실패:', detachError);
        }

        // 썸네일에 트랙 연결
        cameraTrack.track.attach(thumbnailElement);
        thumbnailElement.muted = true;
        thumbnailElement.autoplay = true;
        thumbnailElement.playsInline = true;

        thumbnailElement.play().catch(e => console.warn('썸네일 재생 실패:', e));

        console.log('✅ LiveKit 트랙 방식으로 썸네일 이동 완료');
        return;
      }
    }

    // 방법 3: 현재 비디오가 재생 중인 경우 새로운 스트림 생성
    if (mainElement.videoWidth > 0 && mainElement.videoHeight > 0) {
      console.log('방법 3: 새로운 스트림 생성으로 썸네일 연결');

      navigator.mediaDevices.getUserMedia({
        video: { width: 1280, height: 720 }
      }).then(newStream => {
        thumbnailElement.srcObject = newStream;
        thumbnailElement.muted = true;
        thumbnailElement.autoplay = true;
        thumbnailElement.playsInline = true;
        thumbnailElement.play().catch(e => console.warn('썸네일 재생 실패:', e));

        console.log('✅ 새로운 스트림으로 썸네일 연결 완료');
      }).catch(error => {
        console.error('새로운 스트림 생성 실패:', error);
      });
      return;
    }

    console.warn('모든 썸네일 이동 방법이 실패했거나 적용할 수 없음');

  } catch (error) {
    console.error('로컬 카메라 썸네일 이동 중 오류:', error);
  }
}

// 로컬 카메라를 썸네일로 이동하는 함수 (디버깅 강화)
function moveLocalCameraToThumbnail() {
  console.log('로컬 카메라를 썸네일 영역으로 이동 시작...');

  // DOM 엘리먼트 존재 확인
  const mainElement = localVideoElement.value;
  const thumbnailElement = localCameraThumbnail.value;

  console.log('DOM 엘리먼트 상태:', {
    mainElement: !!mainElement,
    thumbnailElement: !!thumbnailElement,
    mainSrcObject: !!(mainElement?.srcObject),
    thumbnailSrcObject: !!(thumbnailElement?.srcObject)
  });

  if (!mainElement || !thumbnailElement) {
    console.warn('로컬 비디오 엘리먼트를 찾을 수 없음');
    console.log('DOM 엘리먼트 재시도 없이 종료 (화면공유 모드에서는 썸네일 영역이 즉시 사용 가능해야 함)');
    return;
  }

  try {
    // 메인 영역의 스트림을 썸네일로 복사
    if (mainElement.srcObject) {
      const stream = mainElement.srcObject as MediaStream;
      console.log('메인 스트림을 썸네일로 복사 중...', {
        streamId: stream.id,
        videoTracks: stream.getVideoTracks().length,
        audioTracks: stream.getAudioTracks().length
      });

      // 썸네일에 동일한 스트림 설정
      thumbnailElement.srcObject = stream;
      thumbnailElement.muted = true;
      thumbnailElement.autoplay = true;
      thumbnailElement.playsInline = true;

      thumbnailElement.play().catch(e => console.warn('썸네일 비디오 재생 실패:', e));

      // 메인 영역은 srcObject만 제거 (스트림은 정지하지 않음)
      mainElement.srcObject = null;

      console.log('✅ 로컬 카메라가 썸네일 영역으로 이동됨');
    } else {
      console.warn('메인 영역에 스트림이 없어서 이동할 수 없음');
    }
  } catch (error) {
    console.error('로컬 카메라 썸네일 이동 실패:', error);
  }
}

// 원격 화면공유 종료 후 로컬 카메라 복구 함수
async function restoreLocalCameraAfterRemoteScreenShare() {
  console.log('=== 원격 화면공유 종료로 인한 로컬 카메라 복구 시작 ===');

  if (!isVideoEnabled.value || !localVideoElement.value) {
    console.log('비디오 비활성화 상태이거나 엘리먼트가 없어 복구하지 않음');
    return;
  }

  // 현재 로컬 비디오 엘리먼트에 스트림이 있는지 확인
  if (localVideoElement.value.srcObject) {
    console.log('로컬 카메라가 이미 연결되어 있어 복구 불필요');
    return;
  }

  try {
    console.log('브라우저 API로 새로운 로컬 카메라 스트림 생성...');

    // 새로운 카메라 스트림 생성
    const newStream = await navigator.mediaDevices.getUserMedia({
      video: { width: 1280, height: 720 },
      audio: false
    });

    console.log('✅ 새로운 로컬 카메라 스트림 생성 완료');

    // DOM 엘리먼트에 연결
    localVideoElement.value.srcObject = newStream;
    localVideoElement.value.muted = true;
    localVideoElement.value.autoplay = true;
    localVideoElement.value.playsInline = true;

    await localVideoElement.value.play().catch(e =>
      console.warn('로컬 카메라 비디오 재생 실패:', e)
    );

    console.log('✅ 원격 화면공유 종료 후 로컬 카메라 복구 완료');

    // LiveKit에도 발행 시도 (선택사항)
    try {
      if (livekitRoom) {
        console.log('LiveKit에 복구된 카메라 트랙 발행 시도...');
        const localParticipant = livekitRoom.localParticipant;

        const videoTrack = newStream.getVideoTracks()[0];
        if (videoTrack) {
          await localParticipant.publishTrack(videoTrack, {
            source: window.LivekitClient.Track.Source.Camera,
            name: 'camera'
          });
          console.log('✅ LiveKit에 복구된 카메라 트랙 발행 성공');
        }
      }
    } catch (liveKitError) {
      console.warn('LiveKit 트랙 발행 실패 (DOM 연결은 성공):', liveKitError);
    }

  } catch (streamError) {
    console.error('로컬 카메라 스트림 생성 실패:', streamError);
  }
}

// 카메라 복구 함수 제거 - LiveKit이 자동으로 처리하도록 함
// restoreCameraAfterScreenShare 함수는 더 이상 사용하지 않음

// --- LiveKit Functions ---
async function getAccessToken(): Promise<{ url: string, token: string}> {
  try {
    const userName = userNickname.value || `User_${Date.now()}`;
    const { groupService } = await import('@/services/groupService');
    const { url, token } = await groupService.getRTCToken(route.query.groupId as string, userName);

    if (!token || !url) {
      throw new Error('토큰 발급 실패: url 또는 token이 없습니다');
    }

    return { url, token };
  } catch (error: unknown) {
    console.error('토큰 발급 오류:', error);
    throw error;
  }
}

async function setupLocalMedia() {
  try {
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
  } catch (error: unknown) {
    console.error('미디어 접근 실패:', error);
    connectionStatus.value = {
      type: 'warning',
      message: '카메라/마이크에 접근할 수 없습니다. 오디오만으로 참여할 수 있습니다.'
    };
    canJoin.value = true;
  }
}

async function joinRoom() {
  if (isConnecting.value) return;

  isConnecting.value = true;
  connectionState.value = window.LivekitClient.ConnectionState.Connecting;

  try {
    if (!window.LivekitClient) {
      throw new Error('LiveKit SDK가 로드되지 않았습니다.');
    }

    const { Room: LKRoom } = window.LivekitClient as typeof import('livekit-client');
    const { url, token } = await getAccessToken();

    connectionStatus.value = {
      type: 'info',
      message: 'LiveKit 서버에 연결하는 중...'
    };

    // LiveKit Room 생성
    livekitRoom = new LKRoom({
      adaptiveStream: true,
      dynacast: true,
      videoCaptureDefaults: {
        resolution: { width: 1280, height: 720 },
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
    await livekitRoom!.connect(url, token);
    console.log('✅ LiveKit 룸 연결 성공');

    // 기존 참여자들 추가
    const remoteParticipantsMap = (livekitRoom as any).remoteParticipants;
    if (remoteParticipantsMap && remoteParticipantsMap.size > 0) {
      console.log('기존 참여자 수:', remoteParticipantsMap.size);
      remoteParticipantsMap.forEach((participant: any) => {
        console.log('기존 참여자 추가:', participant.identity);
        addRemoteParticipant(participant);
      });
    }

    // UI 전환
    hasJoined.value = true;
    // 모달이 닫히므로 body 스크롤 복원
    restoreBodyScroll();
    connectionState.value = window.LivekitClient.ConnectionState.Connected;
    connectionStatus.value = null;

    await nextTick();

    // 로컬 미디어 발행
    setTimeout(async () => {
      await publishLocalMedia();
    }, 500);

  } catch (error: any) {
    console.error('룸 입장 실패:', error);

    let errorMessage = '룸 입장에 실패했습니다.';
    if (toError(error).message) {
      errorMessage = toError(error).message;
    }

    connectionStatus.value = {
      type: 'error',
      message: errorMessage
    };
    connectionState.value = window.LivekitClient.ConnectionState.Disconnected;

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

  const RoomEvent = window.LivekitClient.RoomEvent;

  // 참여자 연결 이벤트
  livekitRoom!.on(RoomEvent.ParticipantConnected, (participant: LK.RemoteParticipant) => {
    console.log('🔗 참여자 입장:', participant.identity);
    addRemoteParticipant(participant);
  });

  // 참여자 연결 해제 이벤트
  livekitRoom!.on(RoomEvent.ParticipantDisconnected, (participant: LK.RemoteParticipant) => {
    console.log('참여자 퇴장:', participant.identity);
    removeRemoteParticipant(participant.identity);
  });

  // 로컬 트랙 발행 이벤트 (화면공유 시 썸네일 이동 추가)
  livekitRoom!.on(RoomEvent.LocalTrackPublished, (publication: LK.LocalTrackPublication) => {
    console.log('🚀 로컬 트랙 발행:', {
      kind: publication.kind,
      source: publication.source,
      name: publication.trackName
    });

    if (publication.kind === 'video' && publication.track) {
      const track = publication.track as LK.LocalVideoTrack;

      if (publication.source === 'screen_share') {
        console.log('✅ 로컬 화면공유 트랙 발행됨');
        isScreenSharing.value = true;

        // 화면공유 시작 시 즉시 메인 화면에 연결
        nextTick(() => {
          if (screenShareVideoElement.value) {
            try {
              track.attach(screenShareVideoElement.value);
              screenShareVideoElement.value.play().catch(e => console.warn('화면공유 비디오 재생 실패:', e));
              console.log('✅ 화면공유 트랙이 메인 화면에 연결됨');
            } catch (error) {
              console.error('화면공유 트랙 연결 실패:', error);
            }
          }
        });

        // 🔥 중요: 로컬 화면공유 시작 시 모든 참여자를 썸네일로 이동
        setTimeout(() => {
          console.log('로컬 화면공유 시작 - 모든 참여자를 썸네일로 이동');
          moveAllParticipantsToThumbnailsForLocalScreenShare();
        }, 300);

      } else if (publication.source === 'camera') {
        console.log('✅ 로컬 카메라 트랙 발행됨');

        // 카메라 트랙은 항상 적절한 위치에 연결
        setTimeout(() => {
          connectLocalCameraTrack(track);
        }, 300);
      }
    }
  });

// 로컬 카메라 트랙 연결을 위한 함수 (디버깅 강화)
function connectLocalCameraTrack(track: LK.LocalVideoTrack) {
  console.log('로컬 카메라 트랙 연결 시작, 화면공유 상태:', isScreenSharing.value);
  console.log('트랙 정보:', {
    trackExists: !!track,
    trackId: track?.sid,
    trackKind: track?.kind
  });

  // 최대 재시도 횟수 설정
  let retryCount = 0;
  const maxRetries = 3;

  const attemptConnection = () => {
    console.log(`카메라 트랙 연결 시도 ${retryCount + 1}/${maxRetries}`);

    // 화면공유 상태에 따라 적절한 엘리먼트 선택
    let targetElement: HTMLVideoElement | null = null;
    let targetDescription = '';

    if (isScreenSharing.value) {
      // 화면공유 중: 썸네일 영역에 연결
      targetElement = localCameraThumbnail.value;
      targetDescription = '썸네일';

      // 썸네일 엘리먼트가 없으면 화면공유 모드가 아직 렌더링되지 않은 것
      if (!targetElement) {
        console.warn('썸네일 엘리먼트가 아직 렌더링되지 않음 - DOM 업데이트 대기');
        return false;
      }
    } else {
      // 일반 모드: 메인 영역에 연결
      targetElement = localVideoElement.value;
      targetDescription = '메인';
    }

    console.log(`타겟 엘리먼트 (${targetDescription}):`, {
      exists: !!targetElement,
      currentSrc: targetElement?.srcObject ? 'has stream' : 'no stream'
    });

    if (targetElement && track && track.attach) {
      try {
        // 기존 연결 정리
        if (targetElement.srcObject) {
          console.log(`기존 ${targetDescription} 스트림 정리 중...`);
          targetElement.srcObject = null;
        }

        // 기존 트랙 연결 해제 (안전하게)
        try {
          track.detach();
        } catch {
          // 아직 연결되지 않았을 수 있음
        }

        // 새로운 트랙 연결
        console.log(`새로운 트랙을 ${targetDescription} 엘리먼트에 연결 중...`);
        track.attach(targetElement);
        targetElement.muted = true;
        targetElement.autoplay = true;
        targetElement.playsInline = true;

        targetElement.play().catch(e => console.warn('비디오 재생 실패:', e));
        console.log(`✅ 카메라 트랙이 ${targetDescription} 엘리먼트에 연결됨`);
        return true;

      } catch (error) {
        console.error(`카메라 트랙을 ${targetDescription} 엘리먼트에 연결 실패:`, error);
        return false;
      }
    } else {
      console.warn(`카메라 트랙을 연결할 ${targetDescription} 엘리먼트를 찾을 수 없습니다.`, {
        targetElement: !!targetElement,
        track: !!track,
        trackAttach: !!(track && track.attach),
        isScreenSharing: isScreenSharing.value
      });
      return false;
    }
  };

  // 즉시 연결 시도
  if (attemptConnection()) {
    return; // 성공하면 종료
  }

  // 실패 시 제한된 재시도
  const retry = () => {
    retryCount++;
    if (retryCount >= maxRetries) {
      console.error('로컬 카메라 트랙 연결 최종 실패 - 재시도 횟수 초과');
      return;
    }

    setTimeout(() => {
      if (!attemptConnection()) {
        retry(); // 실패 시 다시 재시도
      }
    }, 500 * retryCount); // 재시도할 때마다 지연시간 증가
  };

  // DOM 업데이트 대기 후 재시도
  nextTick(() => {
    setTimeout(retry, 200);
  });
}

  // LocalTrackUnpublished 이벤트 리스너 (브라우저 API 직접 사용)
  livekitRoom!.on(RoomEvent.LocalTrackUnpublished, async (publication: LK.LocalTrackPublication) => {
    console.log('로컬 트랙 해제:', publication.kind, publication.source);

    if (publication.kind === 'video' && publication.source === 'screen_share') {
      console.log('✅ 화면공유 트랙 해제됨');
      isScreenSharing.value = false;

      // 화면공유 비디오 엘리먼트 정리
      if (screenShareVideoElement.value) {
        try {
          publication.track?.detach(screenShareVideoElement.value);
          screenShareVideoElement.value.srcObject = null;
          screenShareVideoElement.value.load();
          console.log('화면공유 비디오 엘리먼트 정리 완료');
        } catch (cleanupError) {
          console.warn('화면공유 엘리먼트 정리 중 오류:', cleanupError);
        }
      }

      console.log('화면공유 종료 후 복구 작업 시작...');

      // 1. 브라우저 API로 직접 새로운 카메라 스트림 생성
      setTimeout(async () => {
        try {
          if (isVideoEnabled.value && localVideoElement.value) {
            console.log('브라우저 API로 직접 카메라 스트림 생성 시작...');

            // 새로운 카메라 스트림 직접 생성
            const newStream = await navigator.mediaDevices.getUserMedia({
              video: { width: 1280, height: 720 },
              audio: false // 오디오는 별도로 처리
            });

            console.log('✅ 새로운 카메라 스트림 생성 완료');

            // DOM 엘리먼트에 직접 연결
            if (localVideoElement.value) {
              // 기존 스트림 정리
              if (localVideoElement.value.srcObject) {
                const oldStream = localVideoElement.value.srcObject as MediaStream;
                oldStream.getTracks().forEach(track => track.stop());
              }

              // 새 스트림 연결
              localVideoElement.value.srcObject = newStream;
              localVideoElement.value.muted = true;
              localVideoElement.value.autoplay = true;
              localVideoElement.value.playsInline = true;

              await localVideoElement.value.play().catch(e => console.warn('비디오 재생 실패:', e));
              console.log('✅ 로컬 카메라가 직접 DOM에 연결됨');
            }

            // LiveKit에도 새로운 트랙 발행 시도
            try {
              if (livekitRoom) {
                console.log('LiveKit에 새로운 카메라 트랙 발행 시도...');
                const localParticipant = livekitRoom.localParticipant;

                // 기존 카메라 트랙 제거
                await localParticipant.setCameraEnabled(false);
                await new Promise(resolve => setTimeout(resolve, 200));

                // 새로운 트랙을 LiveKit에 수동으로 추가
                const videoTrack = newStream.getVideoTracks()[0];
                if (videoTrack) {
                  await localParticipant.publishTrack(videoTrack, {
                    source: window.LivekitClient.Track.Source.Camera,
                    name: 'camera'
                  });
                  console.log('✅ LiveKit에 새 카메라 트랙 발행 성공');
                } else {
                  console.warn('새 스트림에서 비디오 트랙을 찾을 수 없음');
                }
              }
            } catch (liveKitError) {
              console.error('LiveKit 트랙 발행 실패:', liveKitError);
              console.log('DOM 연결만으로도 카메라가 보일 것입니다.');
            }

          }
        } catch (cameraError) {
          console.error('카메라 스트림 생성 실패:', cameraError);
        }
      }, 200);

      // 2. 원격 참여자들의 카메라 재연결
      setTimeout(() => {
        console.log('원격 참여자 카메라 재연결 시작...');
        remoteParticipants.value.forEach(participant => {
          if (participant.videoTrack) {
            try {
              console.log('원격 참여자 카메라 재연결:', participant.identity);
              attachVideoTrack(participant.videoTrack, participant.identity, '카메라', false);
            } catch (reattachError) {
              console.warn(`참여자 ${participant.identity} 카메라 재연결 실패:`, reattachError);
            }
          }
        });
        console.log('원격 참여자 재연결 완료');
      }, 500);
    }
  });

  // 원격 트랙 발행 이벤트 (매우 중요!)
  livekitRoom!.on(RoomEvent.TrackPublished, (publication: LK.RemoteTrackPublication, participant: LK.RemoteParticipant) => {
    const isScreenShare = publication.source === LK.Track.Source.ScreenShare ||
                         publication.trackName === 'screen_share';

    console.log('🚀 원격 트랙 발행 이벤트:', {
      kind: publication.kind,
      participant: participant.identity,
      source: publication.source,
      name: publication.trackName,
      subscribed: publication.isSubscribed,
      isScreenShare: isScreenShare
    });

    // 화면 공유 트랙을 포함한 모든 트랙을 즉시 구독
    if (!publication.isSubscribed) {
      console.log(`새로 발행된 ${isScreenShare ? '화면공유' : publication.kind} 트랙 자동 구독:`, publication.source);

      try {
        publication.setSubscribed(true);
        console.log('✅ 트랙 구독 요청 성공');
      } catch (subscribeError) {
        console.error('트랙 구독 요청 실패:', subscribeError);
      }
    }
  });

  // 트랙 구독 이벤트 (화면공유 감지 개선)
  livekitRoom!.on(RoomEvent.TrackSubscribed, (track: LK.RemoteTrack, publication: LK.RemoteTrackPublication, participant: LK.RemoteParticipant) => {
    const isScreenShare = publication.source === LK.Track.Source.ScreenShare ||
                         publication.trackName === 'screen_share';

    console.log('🎯 트랙 구독 이벤트:', {
      kind: track.kind,
      participant: participant.identity,
      source: publication.source,
      name: publication.trackName,
      isScreenShare: isScreenShare
    });

    if (isScreenShare) {
      console.log('🖥️ 원격 화면공유 트랙 구독 완료!');

      // 🔥 화면공유 시작 시 모든 참여자 카메라를 썸네일로 이동
      setTimeout(() => {
        moveAllParticipantsToThumbnails();
      }, 300);
    }

    handleTrackSubscribed(track, participant, publication);
  });

  // 트랙 구독 해제 이벤트 (화면공유 종료 감지 추가)
  livekitRoom!.on(RoomEvent.TrackUnsubscribed, (track: LK.RemoteTrack, publication: LK.RemoteTrackPublication, participant: LK.RemoteParticipant) => {
    console.log('트랙 구독 해제:', track.kind, participant.identity, publication.source);

    // 🔥 중요: 원격 참여자의 화면공유가 종료된 경우 로컬 카메라도 복구
    if (track.kind === 'video' &&
        (publication.source === LK.Track.Source.ScreenShare)) {
      console.log('🖥️ 원격 참여자의 화면공유 종료 감지:', participant.identity);

      // 로컬 카메라 복구 (다른 참여자들을 위해)
      setTimeout(async () => {
        try {
          console.log('원격 화면공유 종료로 인한 로컬 카메라 복구 시작...');
          await restoreLocalCameraAfterRemoteScreenShare();
        } catch (error) {
          console.error('원격 화면공유 종료 후 로컬 카메라 복구 실패:', error);
        }
      }, 300);
    }

    handleTrackUnsubscribed(track, participant, publication);
  });

  // 연결 품질 변경 이벤트
  livekitRoom!.on(RoomEvent.ConnectionQualityChanged, (quality: LK.ConnectionQuality, participant: LK.Participant) => {
    updateParticipantConnectionQuality(participant.identity, quality);
  });

  // 연결 상태 변경 이벤트
  livekitRoom!.on(RoomEvent.ConnectionStateChanged, (state: LK.ConnectionState) => {
    console.log('🔄 연결 상태 변경:', state);
    connectionState.value = state;
  });

  // 데이터 메시지 수신 이벤트 (채팅)
  livekitRoom!.on(RoomEvent.DataReceived, (payload: Uint8Array, participant?: LK.RemoteParticipant) => {
    try {
      const decoder = new TextDecoder();
      const messageStr = decoder.decode(payload);
      const messageData = JSON.parse(messageStr);

      if (messageData.type === 'chat') {
        const chatMessage: ChatMessage = {
          id: messageData.id,
          sender: messageData.senderNickname || participant?.identity,
          content: messageData.content,
          timestamp: messageData.timestamp,
          isOwn: false
        };

        chatMessages.value.push(chatMessage);
        scrollToBottom();
      }
    } catch (error: unknown) {
      console.error('데이터 메시지 파싱 실패:', error);
    }
  });

  // 재연결 이벤트
  livekitRoom!.on(RoomEvent.Reconnecting, () => {
    connectionState.value = window.LivekitClient.ConnectionState.Reconnecting;
    connectionStatus.value = {
      type: 'warning',
      message: '연결이 불안정합니다. 재연결을 시도하고 있습니다...'
    };
  });

  livekitRoom!.on(RoomEvent.Reconnected, () => {
    connectionState.value = window.LivekitClient.ConnectionState.Connected;
    connectionStatus.value = {
      type: 'success',
      message: '연결이 복구되었습니다.'
    };
    setTimeout(() => {
      connectionStatus.value = null;
    }, 3000);
  });
}

async function publishLocalMedia() {
  if (!livekitRoom) return;

  try {
    await nextTick();
    console.log('로컬 미디어 발행 시작...');

    // 마이크와 카메라 활성화
    await livekitRoom!.localParticipant.setMicrophoneEnabled(true);
    await livekitRoom!.localParticipant.setCameraEnabled(true);

    console.log('로컬 미디어 발행 완료');

  } catch (error: unknown) {
    console.error('로컬 미디어 발행 실패:', error);
  }
}

function addRemoteParticipant(participant: LK.RemoteParticipant) {
  const newParticipant: RemoteParticipant = {
    identity: participant.identity,
    isMicrophoneEnabled: participant.isMicrophoneEnabled,
    isCameraEnabled: participant.isCameraEnabled,
    connectionQuality: undefined,
    isScreenSharing: false,
    screenShareTrack: undefined
  };

  remoteParticipants.value.push(newParticipant);
  console.log('하위 참여자 추가:', participant.identity);

  // 기존 트랙들 처리
  participant.videoTrackPublications.forEach((publication: LK.RemoteTrackPublication) => {
    if (publication.track) {
      console.log('기존 비디오 트랙 처리:', publication.source);
      handleTrackSubscribed(publication.track, participant, publication);
    }
  });

  participant.audioTrackPublications.forEach((publication: LK.RemoteTrackPublication) => {
    if (publication.track) {
      console.log('기존 오디오 트랙 처리:', publication.source);
      handleTrackSubscribed(publication.track, participant, publication);
    }
  });

  console.log('참여자', participant.identity, '의 초기 트랙 처리 완료');
}

function removeRemoteParticipant(identity: string) {
  const index = remoteParticipants.value.findIndex(p => p.identity === identity);
  if (index !== -1) {
    remoteParticipants.value.splice(index, 1);
  }
  participantVideoRefs.value.delete(identity);
}

// 수정된 handleTrackSubscribed 함수
function handleTrackSubscribed(track: LK.Track, participant: LK.RemoteParticipant, publication?: LK.RemoteTrackPublication) {
  console.log('트랙 구독 처리:', track.kind, '참여자:', participant.identity);

  let participantData = remoteParticipants.value.find(p => p.identity === participant.identity);
  if (!participantData) {
    console.warn('참여자 데이터를 찾을 수 없음, 자동으로 추가합니다:', participant.identity);
    addRemoteParticipant(participant);
    participantData = remoteParticipants.value.find(p => p.identity === participant.identity);

    if (!participantData) {
      console.error('참여자 추가 실패:', participant.identity);
      return;
    }
  }

  if (track.kind === 'video') {
    const trackSource = track.source || publication?.source || '';
    const trackName = publication?.trackName || '';

    const isScreenShareTrack = trackSource === LK.Track.Source.ScreenShare ||
                              trackName === 'screen_share' ||
                              trackName.includes('screen') ||
                              trackName.includes('share');

    if (isScreenShareTrack) {
      console.log('🖥️ 원격 참여자의 화면 공유 트랙 감지:', participant.identity);
      participantData.isScreenSharing = true;
      participantData.screenShareTrack = track;

      // 화면 공유 트랙은 항상 메인 화면에 표시
      setTimeout(() => {
        attachVideoTrack(track, participant.identity, '화면공유', true);
      }, 200);

    } else {
      console.log('📹 원격 참여자의 카메라 트랙 감지:', participant.identity);
      participantData.videoTrack = track;

      // 🔥 중요: 현재 화면공유 상태에 따라 연결 위치 결정
      const hasAnyScreenShare = isScreenSharing.value || remoteParticipants.value.some(p => p.isScreenSharing);

      if (hasAnyScreenShare) {
        console.log('화면공유 중이므로 카메라를 썸네일에 표시:', participant.identity);
        setTimeout(() => {
          attachVideoTrack(track, participant.identity, '카메라', false);
        }, 200);
      } else {
        console.log('일반 모드이므로 카메라를 그리드에 표시:', participant.identity);
        setTimeout(() => {
          attachVideoTrack(track, participant.identity, '카메라', false);
        }, 200);
      }
    }

  } else if (track.kind === 'audio') {
    participantData.audioTrack = track;
    if (track.attach) {
      track.attach();
      console.log('✅ 오디오 트랙 연결 성공:', participant.identity);
    }
  }
}

// 수정된 attachVideoTrack 함수
function attachVideoTrack(track: LK.Track, participantId: string, trackType: string, isMainScreen: boolean = false) {
  console.log(`${trackType} 트랙 연결 시작:`, participantId, 'isMainScreen:', isMainScreen);

  const attachVideo = () => {
    let videoElement: HTMLVideoElement | undefined;

    if (isMainScreen) {
      // 화면공유는 메인 화면에 표시
      videoElement = screenShareVideoElement.value || undefined;
      console.log('메인 화면 엘리먼트 찾기:', !!videoElement);
    } else {
      // 일반 카메라는 현재 모드에 따라 적절한 위치에 표시
      const hasAnyScreenShare = isScreenSharing.value || remoteParticipants.value.some(p => p.isScreenSharing);

      if (hasAnyScreenShare) {
        // 화면공유 모드: 썸네일 영역에 표시
        videoElement = participantVideoRefs.value.get(participantId + '_thumb');
        console.log(`썸네일 모드에서 ${participantId} 엘리먼트 찾기:`, !!videoElement);
      } else {
        // 일반 모드: 그리드에 표시
        videoElement = participantVideoRefs.value.get(participantId);
        console.log(`그리드 모드에서 ${participantId} 엘리먼트 찾기:`, !!videoElement);
      }
    }

    if (videoElement && track && track.attach) {
      try {
        // 기존 연결 완전 정리
        if (videoElement.srcObject) {
          const existingStream = videoElement.srcObject as MediaStream;
          existingStream.getTracks().forEach(existingTrack => {
            try {
              existingTrack.stop();
            } catch (stopError) {
              console.warn('기존 트랙 정지 실패:', stopError);
            }
          });
          videoElement.srcObject = null;
        }

        // 기존 트랙 연결 해제 (안전하게)
        try {
          track.detach();
        } catch (detachError) {
          // 아직 연결되지 않았을 수 있음
        }

        // 새 트랙 연결
        track.attach(videoElement);

        // 비디오 속성 설정
        videoElement.muted = participantId === 'local' || isMainScreen;
        videoElement.autoplay = true;
        videoElement.playsInline = true;

        // 재생 시작
        videoElement.play().catch((playError: any) => {
          console.warn('비디오 자동 재생 실패:', playError);
        });

        console.log(`✅ ${trackType} 트랙 연결 성공:`, participantId);
        return true;
      } catch (error: unknown) {
        console.warn(`${trackType} 트랙 연결 실패:`, participantId, error);
        return false;
      }
    } else {
      console.warn('비디오 엘리먼트 또는 트랙 연결 함수를 찾을 수 없음:', {
        participantId,
        videoElement: !!videoElement,
        track: !!track,
        trackAttach: !!(track && track.attach),
        trackEnabled: 'N/A',
        isMainScreen,
        currentMode: isScreenSharing.value ? 'screen_sharing' : 'normal'
      });
      return false;
    }
  };

  // 즉시 시도
  if (!attachVideo()) {
    // DOM 업데이트 대기 후 재시도 (더 긴 지연시간)
    nextTick(() => {
      setTimeout(() => {
        if (!attachVideo()) {
          setTimeout(() => {
            if (!attachVideo()) {
              console.error(`${trackType} 트랙 연결 최종 실패:`, participantId);
            }
          }, 500);
        }
      }, 200);
    });
  }
}

function handleTrackUnsubscribed(track: LK.Track, participant: LK.RemoteParticipant, publication?: LK.RemoteTrackPublication) {
  console.log('트랙 구독 해제 처리:', track.kind, '참여자:', participant.identity);

  const participantData = remoteParticipants.value.find(p => p.identity === participant.identity);
  if (!participantData) return;

  if (track.kind === 'video') {
    const trackSource = track.source || publication?.source || '';
    const trackName = publication?.trackName || '';

    const isScreenShareTrack = trackSource === LK.Track.Source.ScreenShare ||
                              trackName === 'screen_share';

    if (isScreenShareTrack) {
      console.log('🖥️ 원격 참여자의 화면 공유 해제:', participant.identity);
      participantData.isScreenSharing = false;
      participantData.screenShareTrack = undefined;

      // 화면 공유 해제 후 카메라 트랙이 있다면 자동 전환
      if (participantData.videoTrack) {
        console.log('화면 공유 해제 후 카메라 트랙으로 전환:', participant.identity);
        nextTick(() => {
          attachVideoTrack(participantData.videoTrack, participant.identity, '카메라', false);
        });
      }

    } else {
      console.log('📹 원격 참여자의 카메라 해제:', participant.identity);
      participantData.videoTrack = undefined;
    }

    track.detach();
  } else if (track.kind === 'audio') {
    participantData.audioTrack = undefined;
    track.detach();
  }
}

function updateParticipantConnectionQuality(identity: string, quality: LK.ConnectionQuality) {
  const participant = remoteParticipants.value.find(p => p.identity === identity);
  if (participant) {
    participant.connectionQuality = quality;
  }
}

// --- Media Control Functions ---
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
    await livekitRoom.localParticipant.setMicrophoneEnabled(enabled);
    isAudioEnabled.value = enabled;
  } catch (error: unknown) {
    console.error('마이크 토글 실패:', error);
  }
}

async function toggleCamera() {
  if (!livekitRoom) return;

  try {
    const enabled = !isVideoEnabled.value;
    await livekitRoom.localParticipant.setCameraEnabled(enabled);
    isVideoEnabled.value = enabled;
  } catch (error: unknown) {
    console.error('카메라 토글 실패:', error);
  }
}

// 수정된 화면공유 토글 함수
async function toggleScreenShare() {
  console.log('=== 화면공유 토글 시작 ===');
  if (!livekitRoom) {
    console.error('❌ LiveKit Room이 연결되지 않았습니다.');
    return;
  }

  try {
    const willEnableScreenShare = !isScreenSharing.value;
    console.log('목표 상태:', willEnableScreenShare ? '시작' : '종료');

    // LiveKit API를 사용하여 화면 공유 상태 변경
    await livekitRoom.localParticipant.setScreenShareEnabled(willEnableScreenShare);

    console.log('✅ 화면 공유 처리 완료');
  } catch (error: unknown) {
    console.error('화면 공유 토글 실패:', error);
  }
}

function goToBookEditor() {
  try {
    const bookEditorUrl = window.location.origin + '/group-book-editor';
    window.open(bookEditorUrl, '_blank', 'noopener,noreferrer');
  } catch (error: unknown) {
    console.error('책 에디터로 이동 실패:', error);
  }
}

async function leaveRoom() {
  try {
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
      await (livekitRoom as { disconnect: () => Promise<void> }).disconnect();
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
    // 모달이 닫히므로 body 스크롤 방지
    preventBodyScroll();
    connectionState.value = window.LivekitClient.ConnectionState.Disconnected;
    remoteParticipants.value = [];
    participantVideoRefs.value.clear();

    router.push(`/group-book-lobby`);
  } catch (error: unknown) {
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
    const chatMessage = {
      type: 'chat',
      id: `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      content: message,
      timestamp: Date.now(),
      senderNickname: userNickname.value
    };

    const encoder = new TextEncoder();
    const data = encoder.encode(JSON.stringify(chatMessage));
    await livekitRoom!.localParticipant.publishData(data, {
      reliable: true
    });

    const localChatMessage: ChatMessage = {
      ...chatMessage,
      sender: userNickname.value,
      isOwn: true
    };

    chatMessages.value.push(localChatMessage);
    newMessage.value = '';
    scrollToBottom();

  } catch (error: unknown) {
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
    } catch (error: unknown) {
      console.error('LiveKit SDK 로드 오류:', error);
    }
  } else {
    await setupLocalMedia();
  }

});


const cleanup = async () => {
  const groupId = route.query.groupId;
  if (groupId) {
    try {
      const { groupService } = await import('@/services/groupService');
      await groupService.endGroupBookSession(parseInt(groupId.toString()));
    } catch (error: unknown) {
      console.error('페이지 종료 시 그룹 세션 정리 실패:', error);
    }
  }

  if (livekitRoom) {
    (livekitRoom as { disconnect: () => void }).disconnect();
    livekitRoom = null;
  }

  if (localVideo.value?.srcObject) {
    const stream = localVideo.value.srcObject as MediaStream;
    stream.getTracks().forEach(track => track.stop());
  }
};

window.addEventListener('beforeunload', cleanup);
</script>

<style scoped>
@import '../../styles/group-book-creation.css';
</style>
