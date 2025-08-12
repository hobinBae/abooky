import { ref, nextTick } from 'vue';

export interface ChatMessage {
  id: string;
  sender: string;
  content: string;
  timestamp: number;
  isOwn: boolean;
}

export function useChat() {
  const newMessage = ref('');
  const chatMessages = ref<ChatMessage[]>([]);
  const chatMessagesContainer = ref<HTMLElement | null>(null);

  const sendMessage = async (livekitRoom: any) => {
    const message = newMessage.value.trim();
    if (!message || !livekitRoom) return;

    try {
      // 메시지 객체 생성
      const chatMessage: ChatMessage = {
        id: `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        sender: livekitRoom.localParticipant.identity,
        content: message,
        timestamp: Date.now(),
        isOwn: true
      };

      // 로컬에 메시지 추가
      chatMessages.value.push(chatMessage);

      // DataChannel을 통해 다른 참여자들에게 전송
      const encoder = new TextEncoder();
      const data = encoder.encode(JSON.stringify({
        type: 'chat',
        ...chatMessage,
        isOwn: false // 수신자에게는 isOwn을 false로 설정
      }));

      await livekitRoom.localParticipant.publishData(data, 'chat');

      // 입력 필드 초기화
      newMessage.value = '';

      // 채팅 스크롤을 아래로 이동
      scrollToBottom();

    } catch (error) {
      console.error('메시지 전송 실패:', error);
    }
  };

  const handleDataReceived = (payload: any, participant: any) => {
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
  };

  const formatTime = (timestamp: number): string => {
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
  };

  const scrollToBottom = () => {
    nextTick(() => {
      if (chatMessagesContainer.value) {
        chatMessagesContainer.value.scrollTop = chatMessagesContainer.value.scrollHeight;
      }
    });
  };

  return {
    newMessage,
    chatMessages,
    chatMessagesContainer,
    sendMessage,
    handleDataReceived,
    formatTime,
    scrollToBottom
  };
}