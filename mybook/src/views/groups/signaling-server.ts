// signaling-server.ts
import WebSocket from 'ws';
import { IncomingMessage } from 'http';
import { v4 as uuidv4 } from 'uuid';

// 타입 정의
interface User {
  id: string;
  ws: WebSocket;
  roomId: string;
  name: string;
  isHost: boolean;
}

interface Room {
  id: string;
  name: string;
  users: Map<string, User>;
  createdAt: Date;
  maxUsers: number;
}

interface MessageData {
  type: 'join' | 'leave' | 'offer' | 'answer' | 'ice-candidate' | 'user-list' | 'error';
  roomId?: string;
  userId?: string;
  userName?: string;
  payload?: any;
  targetUserId?: string;
}

interface WebSocketWithUser extends WebSocket {
  userId?: string;
  roomId?: string;
  userName?: string;
}

// 전역 상태 관리
const rooms: Map<string, Room> = new Map();
const users: Map<string, User> = new Map();

// WebSocket 서버 설정
const wss = new (WebSocket as any).Server({
  port: 3001,
  clientTracking: true
});

console.log('🚀 WebRTC 시그널링 서버가 포트 3001에서 실행 중입니다.');

// LiveKit 설정 (기존 설정 유지)
const LIVEKIT_URL: string = 'wss://your-project.livekit.cloud';
const API_URL: string = '/api/token';

// 유틸리티 함수들
function createRoom(roomId: string, roomName: string = roomId): Room {
  const room: Room = {
    id: roomId,
    name: roomName,
    users: new Map(),
    createdAt: new Date(),
    maxUsers: 10
  };
  rooms.set(roomId, room);
  console.log(`📋 방 생성: ${roomId}`);
  return room;
}

function removeRoom(roomId: string): void {
  if (rooms.has(roomId)) {
    rooms.delete(roomId);
    console.log(`🗑️ 방 삭제: ${roomId}`);
  }
}

function addUserToRoom(user: User, roomId: string): boolean {
  let room = rooms.get(roomId);

  if (!room) {
    room = createRoom(roomId);
  }

  if (room.users.size >= room.maxUsers) {
    return false; // 방이 가득참
  }

  // 첫 번째 사용자는 호스트로 설정
  user.isHost = room.users.size === 0;

  room.users.set(user.id, user);
  users.set(user.id, user);
  user.roomId = roomId;

  console.log(`👤 사용자 입장: ${user.name} -> ${roomId} (호스트: ${user.isHost})`);
  return true;
}

function removeUserFromRoom(userId: string): void {
  const user = users.get(userId);
  if (!user) return;

  const room = rooms.get(user.roomId);
  if (!room) return;

  room.users.delete(userId);
  users.delete(userId);

  console.log(`👋 사용자 퇴장: ${user.name} <- ${user.roomId}`);

  // 방이 비어있으면 삭제
  if (room.users.size === 0) {
    removeRoom(room.id);
  } else if (user.isHost) {
    // 호스트가 나가면 다른 사용자를 호스트로 승격
    const newHost = Array.from(room.users.values())[0];
    if (newHost) {
      newHost.isHost = true;
      broadcastToRoom(room.id, {
        type: 'user-list',
        payload: {
          users: Array.from(room.users.values()).map(u => ({
            id: u.id,
            name: u.name,
            isHost: u.isHost
          })),
          message: `${newHost.name}님이 새로운 호스트가 되었습니다.`
        }
      });
    }
  }
}

function broadcastToRoom(roomId: string, message: MessageData, excludeUserId?: string): void {
  const room = rooms.get(roomId);
  if (!room) return;

  const messageStr = JSON.stringify(message);

  room.users.forEach((user, userId) => {
    if (excludeUserId && userId === excludeUserId) return;

    if (user.ws.readyState === WebSocket.OPEN) {
      user.ws.send(messageStr);
    }
  });
}

function sendToUser(userId: string, message: MessageData): void {
  const user = users.get(userId);
  if (user && user.ws.readyState === WebSocket.OPEN) {
    user.ws.send(JSON.stringify(message));
  }
}

function getUsersInRoom(roomId: string): User[] {
  const room = rooms.get(roomId);
  return room ? Array.from(room.users.values()) : [];
}

// WebSocket 연결 처리
wss.on('connection', (ws: WebSocketWithUser, request: IncomingMessage) => {
  console.log('🔌 새로운 WebSocket 연결');

  ws.on('message', (data: WebSocket.Data) => {
    try {
      const message: MessageData = JSON.parse(data.toString());
      handleMessage(ws, message);
    } catch (error) {
      console.error('❌ 메시지 파싱 오류:', error);
      ws.send(JSON.stringify({
        type: 'error',
        payload: { message: '잘못된 메시지 형식입니다.' }
      }));
    }
  });

  ws.on('close', () => {
    console.log('🔌 WebSocket 연결 종료');
    if (ws.userId) {
      const user = users.get(ws.userId);
      if (user) {
        // 다른 사용자들에게 퇴장 알림
        broadcastToRoom(user.roomId, {
          type: 'leave',
          userId: user.id,
          payload: {
            userName: user.name,
            users: getUsersInRoom(user.roomId).filter(u => u.id !== user.id).map(u => ({
              id: u.id,
              name: u.name,
              isHost: u.isHost
            }))
          }
        }, user.id);

        removeUserFromRoom(ws.userId);
      }
    }
  });

  ws.on('error', (error: Error) => {
    console.error('❌ WebSocket 오류:', error);
  });
});

// 메시지 처리 함수
function handleMessage(ws: WebSocketWithUser, message: MessageData): void {
  const { type, roomId, userId, userName, payload, targetUserId } = message;

  switch (type) {
    case 'join':
      handleJoinRoom(ws, roomId!, userName || '익명');
      break;

    case 'leave':
      handleLeaveRoom(ws);
      break;

    case 'offer':
    case 'answer':
    case 'ice-candidate':
      handleWebRTCMessage(ws, message);
      break;

    default:
      console.warn('⚠️ 알 수 없는 메시지 타입:', type);
      ws.send(JSON.stringify({
        type: 'error',
        payload: { message: `알 수 없는 메시지 타입: ${type}` }
      }));
  }
}

function handleJoinRoom(ws: WebSocketWithUser, roomId: string, userName: string): void {
  // 이미 방에 입장한 사용자인지 확인
  if (ws.userId && users.has(ws.userId)) {
    ws.send(JSON.stringify({
      type: 'error',
      payload: { message: '이미 방에 입장해 있습니다.' }
    }));
    return;
  }

  const userId = uuidv4();
  const user: User = {
    id: userId,
    ws: ws,
    roomId: roomId,
    name: userName,
    isHost: false
  };

  // 방에 사용자 추가
  if (!addUserToRoom(user, roomId)) {
    ws.send(JSON.stringify({
      type: 'error',
      payload: { message: '방이 가득 찼습니다.' }
    }));
    return;
  }

  // WebSocket에 사용자 정보 저장
  ws.userId = userId;
  ws.roomId = roomId;
  ws.userName = userName;

  // 입장 성공 응답
  ws.send(JSON.stringify({
    type: 'join',
    userId: userId,
    roomId: roomId,
    payload: {
      success: true,
      isHost: user.isHost,
      users: getUsersInRoom(roomId).map(u => ({
        id: u.id,
        name: u.name,
        isHost: u.isHost
      }))
    }
  }));

  // 다른 사용자들에게 입장 알림
  broadcastToRoom(roomId, {
    type: 'user-list',
    payload: {
      newUser: {
        id: userId,
        name: userName,
        isHost: user.isHost
      },
      users: getUsersInRoom(roomId).map(u => ({
        id: u.id,
        name: u.name,
        isHost: u.isHost
      })),
      message: `${userName}님이 입장했습니다.`
    }
  }, userId);
}

function handleLeaveRoom(ws: WebSocketWithUser): void {
  if (!ws.userId) {
    return;
  }

  const user = users.get(ws.userId);
  if (user) {
    // 다른 사용자들에게 퇴장 알림
    broadcastToRoom(user.roomId, {
      type: 'leave',
      userId: user.id,
      payload: {
        userName: user.name,
        users: getUsersInRoom(user.roomId).filter(u => u.id !== user.id).map(u => ({
          id: u.id,
          name: u.name,
          isHost: u.isHost
        }))
      }
    }, user.id);

    removeUserFromRoom(ws.userId);
  }

  ws.userId = undefined;
  ws.roomId = undefined;
  ws.userName = undefined;
}

function handleWebRTCMessage(ws: WebSocketWithUser, message: MessageData): void {
  const { targetUserId, payload } = message;

  if (!targetUserId) {
    // 타겟이 없으면 같은 방의 모든 사용자에게 브로드캐스트
    if (ws.roomId) {
      broadcastToRoom(ws.roomId, {
        ...message,
        userId: ws.userId
      }, ws.userId);
    }
  } else {
    // 특정 사용자에게만 전송
    sendToUser(targetUserId, {
      ...message,
      userId: ws.userId
    });
  }
}

// 서버 상태 모니터링
setInterval(() => {
  const roomCount = rooms.size;
  const userCount = users.size;

  if (roomCount > 0 || userCount > 0) {
    console.log(`📊 서버 상태 - 방: ${roomCount}개, 사용자: ${userCount}명`);

    // 상세 정보
    rooms.forEach((room, roomId) => {
      console.log(`  📋 ${roomId}: ${room.users.size}명 (최대: ${room.maxUsers}명)`);
    });
  }
}, 30000); // 30초마다

// 연결 끊어진 WebSocket 정리
setInterval(() => {
  let cleanedCount = 0;

  users.forEach((user, userId) => {
    if (user.ws.readyState === WebSocket.CLOSED) {
      removeUserFromRoom(userId);
      cleanedCount++;
    }
  });

  if (cleanedCount > 0) {
    console.log(`🧹 정리된 연결: ${cleanedCount}개`);
  }
}, 10000); // 10초마다

// 프로세스 종료 처리
process.on('SIGINT', () => {
  console.log('\n🛑 서버를 종료합니다...');

  wss.clients.forEach((ws: WebSocket) => {
    ws.close();
  });

  wss.close(() => {
    console.log('✅ 서버가 안전하게 종료되었습니다.');
    process.exit(0);
  });
});

export { wss, rooms, users };
export type { User, Room, MessageData };
