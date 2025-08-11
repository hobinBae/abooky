import apiClient from '@/api';

export interface Group {
  groupId: number;
  groupName: string;
  description: string;
  themeColor: string;
  groupImageUrl: string;
  leaderId: number;
  leaderNickname: string;
  createdAt: string;
  updatedAt: string;
  members?: string[];
}

export interface ActiveSession {
  groupId: number;
  groupName: string;
  hostName: string;
  startedAt: Date;
  participantCount: number;
}

export interface ApiResponse<T> {
  success: boolean;
  status: number;
  message: string;
  data: {
    content: T[];
    pageable: {
      page: number;
      size: number;
      totalElements: number;
      totalPages: number;
    };
  };
  timestamp: string;
  path: string;
}

class GroupService {
  private getAccessToken(): string {
    return localStorage.getItem('accessToken') || '';
  }

  private getCurrentUserId(): number {
    const token = this.getAccessToken();
    // JWT 토큰에서 사용자 ID 추출하거나 localStorage에서 직접 가져오기
    const userId = localStorage.getItem('userId');
    return userId ? parseInt(userId) : 1001; // 기본값
  }

  private getStoredSessions(): ActiveSession[] {
    const sessions = localStorage.getItem('activeGroupBookSessions');
    return sessions ? JSON.parse(sessions) : [];
  }

  private storeSession(session: ActiveSession): void {
    const sessions = this.getStoredSessions();
    const existingIndex = sessions.findIndex(s => s.groupId === session.groupId);
    
    if (existingIndex >= 0) {
      sessions[existingIndex] = session;
    } else {
      sessions.push(session);
    }
    
    localStorage.setItem('activeGroupBookSessions', JSON.stringify(sessions));
  }

  private removeSession(groupId: number): void {
    const sessions = this.getStoredSessions();
    const filteredSessions = sessions.filter(s => s.groupId !== groupId);
    localStorage.setItem('activeGroupBookSessions', JSON.stringify(filteredSessions));
  }

  async fetchMyGroups(): Promise<Group[]> {
    try {
      const response = await apiClient.get('/api/v1/groups/me', {
        headers: {
          'Authorization': `Bearer ${this.getAccessToken()}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.data.success) {
        const basicGroups = response.data.data.content;
        
        // 각 그룹의 상세 정보 가져오기
        const groupsWithDetails = await Promise.all(
          basicGroups.map(async (group: Group) => {
            try {
              const detailResponse = await apiClient.get(`/api/v1/groups/${group.groupId}/details`, {
                headers: {
                  'Authorization': `Bearer ${this.getAccessToken()}`,
                  'Content-Type': 'application/json'
                }
              });
              
              if (detailResponse.status === 200) {
                return {
                  ...group,
                  members: detailResponse.data.data.members || []
                };
              } else {
                console.warn(`그룹 ${group.groupId} 상세 정보 조회 실패`);
                return group;
              }
            } catch (error) {
              console.error(`그룹 ${group.groupId} 상세 정보 조회 오류:`, error);
              return group;
            }
          })
        );
        
        return groupsWithDetails;
      } else {
        throw new Error('API 응답 실패: ' + response.data.message);
      }
    } catch (error) {
      console.error('그룹 목록 조회 실패:', error);
      
      // 개발용 더미 데이터 - 현재 사용자에 따라 다른 그룹 반환
      return this.getDummyGroups();
    }
  }

  async fetchActiveGroupBookSessions(): Promise<ActiveSession[]> {
    try {
      const response = await apiClient.get('/api/v1/group-books/active-sessions', {
        headers: {
          'Authorization': `Bearer ${this.getAccessToken()}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.data.success) {
        return response.data.data.content;
      } else {
        throw new Error('API 응답 실패: ' + response.data.message);
      }
    } catch (error) {
      console.error('활성화된 세션 조회 실패:', error);
      
      // localStorage에서 세션 목록 조회 (실시간 업데이트)
      return this.getStoredSessions();
    }
  }

  async getRTCToken(groupId: string, userName: string): Promise<{ url: string, token: string }> {
    const response = await apiClient.post(`/api/v1/groups/${groupId}/rtc/token`, {
      userName
    });
    
    const data = response.data.data ?? response.data;
    if (!data?.token || !data?.url) {
      throw new Error('응답에 url/token 없음');
    }
    return { url: data.url, token: data.token };
  }

  private getDummyGroups(): Group[] {
    const currentUserId = this.getCurrentUserId();
    
    // 사용자별로 다른 그룹 반환
    if (currentUserId === 1001) { // 사용자 A
      return [
        {
          groupId: 1,
          groupName: "우리 가족",
          description: "가족들과 추억을 기록하는 공간",
          themeColor: "#FFCC00",
          groupImageUrl: "https://your-bucket.s3.ap-northeast-2.amazonaws.com/profiles/550e8400-e29b-41d4-a716-446655440000.jpg",
          leaderId: 1001,
          leaderNickname: "김싸피123",
          createdAt: "2025-07-22T10:00:00",
          updatedAt: "2025-07-22T11:00:00",
          members: ["김싸피123", "엄마", "아빠"]
        },
        {
          groupId: 2,
          groupName: "대학 동기",
          description: "대학 동기들과 추억을 기록하는 공간",
          themeColor: "#42b983",
          groupImageUrl: "https://your-bucket.s3.ap-northeast-2.amazonaws.com/profiles/550e8400-e29b-41d4-a716-446655440000.jpg",
          leaderId: 5001,
          leaderNickname: "이싸피123",
          createdAt: "2025-07-22T10:00:00",
          updatedAt: "2025-07-22T11:00:00",
          members: ["김싸피123", "이싸피123", "박싸피456"]
        }
      ];
    } else if (currentUserId === 5001) { // 사용자 B
      return [
        {
          groupId: 2,
          groupName: "대학 동기",
          description: "대학 동기들과 추억을 기록하는 공간",
          themeColor: "#42b983",
          groupImageUrl: "https://your-bucket.s3.ap-northeast-2.amazonaws.com/profiles/550e8400-e29b-41d4-a716-446655440000.jpg",
          leaderId: 5001,
          leaderNickname: "이싸피123",
          createdAt: "2025-07-22T10:00:00",
          updatedAt: "2025-07-22T11:00:00",
          members: ["김싸피123", "이싸피123", "박싸피456"]
        },
        {
          groupId: 1,
          groupName: "우리 가족",
          description: "가족들과 추억을 기록하는 공간",
          themeColor: "#FFCC00",
          groupImageUrl: "https://your-bucket.s3.ap-northeast-2.amazonaws.com/profiles/550e8400-e29b-41d4-a716-446655440000.jpg",
          leaderId: 1001,
          leaderNickname: "김싸피123",
          createdAt: "2025-07-22T10:00:00",
          updatedAt: "2025-07-22T11:00:00",
          members: ["김싸피123", "엄마", "아빠"]
        }
      ];
    }
    
    // 기본값
    return [];
  }

  private getDummySessions(): ActiveSession[] {
    return [
      {
        groupId: 2,
        groupName: '대학 동기',
        hostName: '이싸피123',
        startedAt: new Date(),
        participantCount: 1
      },
      {
        groupId: 99,
        groupName: '다른 사람 그룹',
        hostName: '타인123',
        startedAt: new Date(),
        participantCount: 3
      }
    ];
  }

  // 그룹책 세션 시작 (방 만들기)
  async startGroupBookSession(groupId: number, groupName: string): Promise<void> {
    const currentUserId = this.getCurrentUserId();
    const userName = currentUserId === 1001 ? '김싸피123' : '이싸피123';
    
    const session: ActiveSession = {
      groupId,
      groupName,
      hostName: userName,
      startedAt: new Date(),
      participantCount: 1
    };
    
    this.storeSession(session);
    console.log('그룹책 세션 시작:', session);
  }

  // 그룹책 세션 종료 (방 나가기)
  async endGroupBookSession(groupId: number): Promise<void> {
    this.removeSession(groupId);
    console.log('그룹책 세션 종료:', groupId);
  }
}

export const groupService = new GroupService();