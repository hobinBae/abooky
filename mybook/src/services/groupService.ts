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
      
      // 개발용 더미 데이터
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
      
      // 개발용 더미 데이터
      return this.getDummySessions();
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
        themeColor: "#FFFFFF",
        groupImageUrl: "https://your-bucket.s3.ap-northeast-2.amazonaws.com/profiles/550e8400-e29b-41d4-a716-446655440000.jpg",
        leaderId: 5001,
        leaderNickname: "이싸피123",
        createdAt: "2025-07-22T10:00:00",
        updatedAt: "2025-07-22T11:00:00",
        members: ["김싸피123", "이싸피123", "박싸피456"]
      }
    ];
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
}

export const groupService = new GroupService();