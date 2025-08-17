const toError = (e: unknown): Error => (e instanceof Error ? e : new Error(String(e)));
declare global { interface Window { LivekitClient: typeof import('livekit-client'); } }
import { ref } from 'vue';
import type * as LK from 'livekit-client';

import { groupService } from '@/services/groupService';

export interface RemoteParticipant {
  identity: string;
  isMicrophoneEnabled: boolean;
  isCameraEnabled: boolean;
  videoTrack?: any;
  audioTrack?: any;
  connectionQuality?: number;
}

export interface ConnectionStatus {
  type: 'info' | 'success' | 'warning' | 'error';
  message: string;
}

export function useWebRTC() {
  // 미디어 상태
  const isAudioEnabled = ref(true);
  const isVideoEnabled = ref(true);
  const isScreenSharing = ref(false);

  // 연결 상태
  const connectionState = ref<'disconnected' | 'connecting' | 'connected' | 'reconnecting'>('disconnected');
  const connectionStatus = ref<ConnectionStatus | null>(null);

  // 참여자 관리
  const remoteParticipants = ref<RemoteParticipant[]>([]);
  const participantVideoRefs = ref<Map<string, HTMLVideoElement>>(new Map());

  // LiveKit 관련 - non-reactive storage for WebRTC objects
  let livekitRoom: LK.Room | null = null;
  let localMediaStream: MediaStream | null = null;

  const getConnectionStatusText = () => {
    switch (connectionState.value) {
      case 'connected': return '연결됨';
      case 'connecting': return '연결 중';
      case 'reconnecting': return '재연결 중';
      case 'disconnected': return '연결 끊김';
      default: return '';
    }
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

  const setupLocalMedia = async (): Promise<MediaStream | null> => {
    try {
      connectionStatus.value = { 
        type: 'info', 
        message: '카메라와 마이크 권한을 확인하고 있습니다...' 
      };

      const stream = await navigator.mediaDevices.getUserMedia({
        video: { width: 1280, height: 720 },
        audio: { echoCancellation: true, noiseSuppression: true }
      });

      connectionStatus.value = { 
        type: 'success', 
        message: '카메라와 마이크가 준비되었습니다.' 
      };

      return stream;
    } catch (error: unknown) {
      console.error('미디어 접근 실패:', error);
      connectionStatus.value = { 
        type: 'warning', 
        message: '카메라/마이크에 접근할 수 없습니다. 오디오만으로 참여할 수 있습니다.' 
      };
      return null;
    }
  };

  const joinRoom = async (groupId: string, onDataReceived?: (payload: any, participant: any) => void) => {
    connectionState.value = 'connecting';

    try {
      // LiveKit SDK 로드 확인
      if (!window.LivekitClient) {
        throw new Error('LiveKit SDK가 로드되지 않았습니다.');
      }

      const { Room: LKRoom, RoomEvent } = window.LivekitClient as typeof import('livekit-client');

      // Room 인스턴스 생성
      livekitRoom = new LKRoom({
        adaptiveStream: true,
        dynacast: true,
        videoCaptureDefaults: {
          resolution: { width: 1280, height: 720 }
        },
        audioCaptureDefaults: {
          autoGainControl: false,
          noiseSuppression: false,
          echoCancellation: false
        }
      });

      // 이벤트 리스너 설정
      setupRoomEventListeners(onDataReceived);

      // 토큰 발급 및 연결
      const userName = `User_${Date.now()}`;
      const { url, token } = await groupService.getRTCToken(groupId, userName);
      await livekitRoom!.connect(url, token);

      // 로컬 미디어 퍼블리시
      await publishLocalMedia();

      connectionState.value = 'connected';
      connectionStatus.value = null;

      return livekitRoom;
    } catch (error: any) {
      console.error('룸 입장 실패:', error);
      connectionStatus.value = { 
        type: 'error', 
        message: `입장 실패: ${error?.message || '알 수 없는 오류가 발생했습니다'}` 
      };
      connectionState.value = 'disconnected';
      throw error;
    }
  };

  const setupRoomEventListeners = (onDataReceived?: (payload: any, participant: any) => void) => {
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

    // 데이터 메시지 수신 이벤트
    if (onDataReceived) {
      livekitRoom.on(RoomEvent.DataReceived, onDataReceived);
    }

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
      setTimeout(() => {
        connectionStatus.value = null;
      }, 3000);
    });
  };

  const publishLocalMedia = async () => {
    if (!livekitRoom) return;

    try {
      if (isVideoEnabled.value) {
        await livekitRoom!.localParticipant.setCameraEnabled(true);
        await livekitRoom!.localParticipant.setMicrophoneEnabled(true);
      } else {
        await livekitRoom!.localParticipant.setMicrophoneEnabled(true);
      }
      console.log('로컬 미디어 퍼블리시 완료');
    } catch (error: unknown) {
      console.error('로컬 미디어 퍼블리시 실패:', error);
    }
  };

  const addRemoteParticipant = (participant: any) => {
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
  };

  const removeRemoteParticipant = (identity: string) => {
    const index = remoteParticipants.value.findIndex(p => p.identity === identity);
    if (index !== -1) {
      remoteParticipants.value.splice(index, 1);
    }
    participantVideoRefs.value.delete(identity);
  };

  const handleTrackSubscribed = (track: any, participant: any) => {
    const participantData = remoteParticipants.value.find(p => p.identity === participant.identity);
    if (!participantData) return;

    if (track.kind === 'video') {
      participantData.videoTrack = track;
      
      // 비디오 엘리먼트에 연결
      setTimeout(() => {
        const videoElement = participantVideoRefs.value.get(participant.identity);
        if (videoElement) {
          track.attach(videoElement);
        }
      }, 100);
    } else if (track.kind === 'audio') {
      participantData.audioTrack = track;
      track.attach(); // 오디오는 자동 재생
    }
  };

  const handleTrackUnsubscribed = (track: any, participant: any) => {
    const participantData = remoteParticipants.value.find(p => p.identity === participant.identity);
    if (!participantData) return;

    if (track.kind === 'video') {
      participantData.videoTrack = undefined;
      track.detach();
    } else if (track.kind === 'audio') {
      participantData.audioTrack = undefined;
      track.detach();
    }
  };

  const updateParticipantConnectionQuality = (identity: string, quality: number) => {
    const participant = remoteParticipants.value.find(p => p.identity === identity);
    if (participant) {
      participant.connectionQuality = quality;
    }
  };

  const toggleMicrophone = async () => {
    if (!livekitRoom) return;

    try {
      const enabled = !isAudioEnabled.value;
      await livekitRoom!.localParticipant.setMicrophoneEnabled(enabled);
      isAudioEnabled.value = enabled;
    } catch (error: unknown) {
      console.error('마이크 토글 실패:', error);
    }
  };

  const toggleCamera = async () => {
    if (!livekitRoom) return;

    try {
      const enabled = !isVideoEnabled.value;
      await livekitRoom!.localParticipant.setCameraEnabled(enabled);
      isVideoEnabled.value = enabled;
    } catch (error: unknown) {
      console.error('카메라 토글 실패:', error);
    }
  };

  const toggleScreenShare = async () => {
    if (!livekitRoom) return;

    try {
      const enabled = !isScreenSharing.value;
      
      if (enabled) {
        await livekitRoom!.localParticipant.setScreenShareEnabled(true);
        await livekitRoom!.localParticipant.setCameraEnabled(false);
      } else {
        await livekitRoom!.localParticipant.setScreenShareEnabled(false);
        if (isVideoEnabled.value) {
          await livekitRoom!.localParticipant.setCameraEnabled(true);
        }
      }
      
      isScreenSharing.value = enabled;
    } catch (error: unknown) {
      console.error('화면 공유 토글 실패:', error);
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
  };

  const leaveRoom = async () => {
    try {
      if (livekitRoom) {
        await livekitRoom!.disconnect();
        livekitRoom = null;
      }

      if (localMediaStream) {
        localMediaStream.getTracks().forEach(track => track.stop());
        localMediaStream = null;
      }

      connectionState.value = 'disconnected';
      remoteParticipants.value = [];
      participantVideoRefs.value.clear();
    } catch (error: unknown) {
      console.error('퇴장 중 오류:', error);
    }
  };

  const setParticipantVideoRef = (el: any, identity: string) => {
    if (el && el instanceof HTMLVideoElement) {
      participantVideoRefs.value.set(identity, el);
    }
  };

  return {
    // 상태
    isAudioEnabled,
    isVideoEnabled,
    isScreenSharing,
    connectionState,
    connectionStatus,
    remoteParticipants,
    participantVideoRefs,

    // 계산된 속성
    getConnectionStatusText,
    getConnectionQualityText,

    // 메서드
    setupLocalMedia,
    joinRoom,
    toggleMicrophone,
    toggleCamera,
    toggleScreenShare,
    leaveRoom,
    setParticipantVideoRef,

    // LiveKit 룸 인스턴스 접근
    getLivekitRoom: () => livekitRoom
  };
}