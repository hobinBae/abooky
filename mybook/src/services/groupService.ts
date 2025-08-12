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
      const storedSessions = this.getStoredSessions();
      console.log('🔧 localStorage에서 가져온 세션:', storedSessions);
      return storedSessions;
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
    
    // 내 서재의 allGroups와 일치하는 더미 데이터 반환
    return [
      {
        groupId: 1, // 내 서재의 'group1'에 해당
        groupName: "독서 토론 모임",
        description: "독서를 통해 생각을 나누는 모임",
        themeColor: "#42b983",
        groupImageUrl: "https://images.unsplash.com/photo-1506894824902-72895a783ac0?w=500",
        leaderId: 1001,
        leaderNickname: "김작가",
        createdAt: "2025-01-01T10:00:00",
        updatedAt: "2025-01-01T11:00:00",
        members: ["김작가", "이영희", "박철수"]
      },
      {
        groupId: 2, // 내 서재의 'group2'에 해당  
        groupName: "글쓰기 동호회",
        description: "함께 글을 쓰며 성장하는 동호회",
        themeColor: "#FFCC00",
        groupImageUrl: "https://images.unsplash.com/photo-1455390582262-044cdead277a?w=500",
        leaderId: 1001,
        leaderNickname: "김작가",
        createdAt: "2025-01-02T10:00:00",
        updatedAt: "2025-01-02T11:00:00",
        members: ["김작가", "최수진"]
      },
      {
        groupId: 3, // 내 서재의 'group3'에 해당
        groupName: "여행 에세이 클럽",
        description: "여행의 경험을 글로 남기는 클럽",
        themeColor: "#3498db",
        groupImageUrl: "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=500",
        leaderId: 5001,
        leaderNickname: "정민준",
        createdAt: "2025-01-03T10:00:00",
        updatedAt: "2025-01-03T11:00:00",
        members: ["정민준", "김작가", "하은지"]
      }
    ];
  }

  private getDummySessions(): ActiveSession[] {
    // localStorage에서 먼저 확인하고, 없으면 초기 더미 데이터 생성
    const stored = this.getStoredSessions();
    if (stored.length > 0) {
      return stored;
    }
    
    // 초기 더미 데이터 - 테스트를 위해 일부 그룹이 활성화된 상태로 설정
    const initialSessions = [
      {
        groupId: 1,
        groupName: '독서 토론 모임',
        hostName: '이영희',
        startedAt: new Date(Date.now() - 10 * 60 * 1000), // 10분 전 시작
        participantCount: 2
      }
    ];
    
    // localStorage에 저장
    localStorage.setItem('activeGroupBookSessions', JSON.stringify(initialSessions));
    
    return initialSessions;
    
    // 모든 세션이 비활성화된 상태로 테스트하려면:
    // return [];
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