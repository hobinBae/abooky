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
  members?: GroupMember[];
  managers?: GroupMember[];
}

export interface GroupMember {
  memberId: number;
  nickname: string;
  profileImageUrl: string;
  role?: 'LEADER' | 'MANAGER' | 'MEMBER';
  joinedAt?: string; // 가입일 필드 추가
  birthDate?: string; // 생일 필드 추가
}

export interface GroupRoleChangeRequest {
  role: 'MANAGER' | 'MEMBER';
}

export interface ActiveSession {
  groupId: number;
  groupName: string;
  hostName: string;
  startedAt: Date;
  participantCount: number;
}

export interface GroupInvite {
  groupApplyId: number;
  groupId: number;
  groupName: string;
  leaderId: number;
  leaderNickname: string;
  receiverNickname: string;
  status: 'PENDING' | 'ACCEPTED' | 'DENIED';
  invitedAt: string;
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
    // const token = this.getAccessToken();
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
      // 올바른 API 엔드포인트로 수정
      const response = await apiClient.get('/api/v1/members/me/groups');
      // 백엔드 응답 구조에 맞게 데이터 반환
      return response.data.data;
    } catch (error) {
      console.error('내 그룹 목록 조회 실패:', error);
      // API 호출 실패 시 빈 배열 반환 또는 에러 처리
      return [];
    }
  }

  async fetchGroupDetails(groupId: string): Promise<Group | null> {
    try {
      const response = await apiClient.get(`/api/v1/groups/${groupId}`);
      const group: Group = response.data.data;

      const members = await this.fetchGroupMembers(groupId);
      group.members = members;
      group.managers = members.filter(member => member.role === 'MANAGER');
      return group;

    } catch (error) {
      console.error(`그룹 상세 정보 조회 실패 (ID: ${groupId}):`, error);
      return null;
    }
  }

  async fetchGroupMembers(groupId: string): Promise<GroupMember[]> {
    try {
      const response = await apiClient.get(`/api/v1/groups/${groupId}/members`);
      return response.data.data;
    } catch (error) {
      console.error(`그룹 멤버 목록 조회 실패 (ID: ${groupId}):`, error);
      return [];
    }
  }

  async updateGroup(groupId: string, groupData: FormData): Promise<Group | null> {
    try {
      const response = await apiClient.patch(`/api/v1/groups/${groupId}`, groupData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      return response.data.data;
    } catch (error) {
      console.error(`그룹 정보 수정 실패 (ID: ${groupId}):`, error);
      return null;
    }
  }

  async kickMember(groupId: string, targetId: number): Promise<boolean> {
    try {
      await apiClient.delete(`/api/v1/groups/${groupId}/${targetId}`);
      return true;
    } catch (error) {
      console.error(`멤버 강퇴 실패 (Group: ${groupId}, Target: ${targetId}):`, error);
      return false;
    }
  }

  async leaveGroup(groupId: string): Promise<boolean> {
    try {
      await apiClient.delete(`/api/v1/groups/${groupId}/me`);
      return true;
    } catch (error) {
      console.error(`그룹 탈퇴 실패 (Group: ${groupId}):`, error);
      return false;
    }
  }

  async changeGroupMemberRole(groupId: string, memberId: number, role: 'MANAGER' | 'MEMBER'): Promise<boolean> {
    try {
      const requestData: GroupRoleChangeRequest = { role };
      await apiClient.patch(`/api/v1/groups/${groupId}/members/${memberId}/role`, requestData);
      return true;
    } catch (error) {
      console.error(`그룹원 역할 변경 실패 (Group: ${groupId}, Member: ${memberId}, Role: ${role}):`, error);
      return false;
    }
  }
  async fetchSentInvites(groupId: string): Promise<GroupInvite[]> {
    try {
      const response = await apiClient.get(`/api/v1/groups/${groupId}/invites`);
      return response.data.data;
    } catch (error) {
      console.error(`보낸 초대 목록 조회 실패 (Group: ${groupId}):`, error);
      return [];
    }
  }

  async inviteMember(groupId: string, receiverEmail: string): Promise<GroupInvite | null> {
    try {
      const response = await apiClient.post(`/api/v1/groups/${groupId}/invites`, { receiverEmail });
      return response.data.data;
    } catch (error) {
      console.error(`그룹원 초대 실패 (Group: ${groupId}, Email: ${receiverEmail}):`, error);
      return null;
    }
  }

  async handleInvite(groupId: string, groupApplyId: number, status: 'ACCEPTED' | 'DENIED'): Promise<GroupInvite | null> {
    try {
      const response = await apiClient.patch(`/api/v1/groups/${groupId}/invites/${groupApplyId}`, { status });
      return response.data.data;
    } catch (error) {
      console.error(`초대 처리 실패 (Group: ${groupId}, Apply: ${groupApplyId}):`, error);
      return null;
    }
  }

  async fetchMyInvites(): Promise<GroupInvite[]> {
    try {
      const response = await apiClient.get('/api/v1/members/me/invites');
      return response.data.data;
    } catch (error) {
      console.error('내 초대 목록 조회 실패:', error);
      return [];
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

  // private getDummyGroups(): Group[] {
  //   const currentUserId = this.getCurrentUserId();

  //   // 사용자별로 다른 그룹 반환
  //   if (currentUserId === 1001) { // 사용자 A
  //     return [
  //       {
  //         groupId: 1,
  //         groupName: "우리 가족",
  //         description: "가족들과 추억을 기록하는 공간",
  //         themeColor: "#FFCC00",
  //         groupImageUrl: "https://your-bucket.s3.ap-northeast-2.amazonaws.com/profiles/550e8400-e29b-41d4-a716-446655440000.jpg",
  //         leaderId: 1001,
  //         leaderNickname: "김싸피123",
  //         createdAt: "2025-07-22T10:00:00",
  //         updatedAt: "2025-07-22T11:00:00",
  //         members: [
  //           { memberId: 1001, nickname: '김싸피123', profileImageUrl: '' },
  //           { memberId: 1002, nickname: '엄마', profileImageUrl: '' },
  //           { memberId: 1003, nickname: '아빠', profileImageUrl: '' }
  //         ]
  //       },
  //       {
  //         groupId: 2,
  //         groupName: "대학 동기",
  //         description: "대학 동기들과 추억을 기록하는 공간",
  //         themeColor: "#42b983",
  //         groupImageUrl: "https://your-bucket.s3.ap-northeast-2.amazonaws.com/profiles/550e8400-e29b-41d4-a716-446655440000.jpg",
  //         leaderId: 5001,
  //         leaderNickname: "이싸피123",
  //         createdAt: "2025-07-22T10:00:00",
  //         updatedAt: "2025-07-22T11:00:00",
  //         members: [
  //           { memberId: 1001, nickname: '김싸피123', profileImageUrl: '' },
  //           { memberId: 5001, nickname: '이싸피123', profileImageUrl: '' },
  //           { memberId: 5002, nickname: '박싸피456', profileImageUrl: '' }
  //         ]
  //       }
  //     ];
  //   } else if (currentUserId === 5001) { // 사용자 B
  //     return [
  //       {
  //         groupId: 2,
  //         groupName: "대학 동기",
  //         description: "대학 동기들과 추억을 기록하는 공간",
  //         themeColor: "#42b983",
  //         groupImageUrl: "https://your-bucket.s3.ap-northeast-2.amazonaws.com/profiles/550e8400-e29b-41d4-a716-446655440000.jpg",
  //         leaderId: 5001,
  //         leaderNickname: "이싸피123",
  //         createdAt: "2025-07-22T10:00:00",
  //         updatedAt: "2025-07-22T11:00:00",
  //         members: [
  //           { memberId: 1001, nickname: '김싸피123', profileImageUrl: '' },
  //           { memberId: 5001, nickname: '이싸피123', profileImageUrl: '' },
  //           { memberId: 5002, nickname: '박싸피456', profileImageUrl: '' }
  //         ]
  //       },
  //       {
  //         groupId: 1,
  //         groupName: "우리 가족",
  //         description: "가족들과 추억을 기록하는 공간",
  //         themeColor: "#FFCC00",
  //         groupImageUrl: "https://your-bucket.s3.ap-northeast-2.amazonaws.com/profiles/550e8400-e29b-41d4-a716-446655440000.jpg",
  //         leaderId: 1001,
  //         leaderNickname: "김싸피123",
  //         createdAt: "2025-07-22T10:00:00",
  //         updatedAt: "2025-07-22T11:00:00",
  //         members: [
  //           { memberId: 1001, nickname: '김싸피123', profileImageUrl: '' },
  //           { memberId: 1002, nickname: '엄마', profileImageUrl: '' },
  //           { memberId: 1003, nickname: '아빠', profileImageUrl: '' }
  //         ]
  //       }
  //     ];
  //   }

  //   // 기본값
  //   return [];
  // }

  // private getDummySessions(): ActiveSession[] {
  //   // localStorage에서 먼저 확인하고, 없으면 초기 더미 데이터 생성
  //   const stored = this.getStoredSessions();
  //   if (stored.length > 0) {
  //     return stored;
  //   }
    
  //   // 초기 더미 데이터 - 테스트를 위해 일부 그룹이 활성화된 상태로 설정
  //   const initialSessions = [
  //     {
  //       groupId: 1,
  //       groupName: '독서 토론 모임',
  //       hostName: '이영희',
  //       startedAt: new Date(Date.now() - 10 * 60 * 1000), // 10분 전 시작
  //       participantCount: 2
  //     }
  //   ];
    
  //   // localStorage에 저장
  //   localStorage.setItem('activeGroupBookSessions', JSON.stringify(initialSessions));
    
  //   return initialSessions;
    
  //   // 모든 세션이 비활성화된 상태로 테스트하려면:
  //   // return [];
  // }

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

  async createGroupBook(groupId: string, bookData: { title: string; summary: string; categoryId: number | null }): Promise<any> {
    const formData = new FormData();
    formData.append('title', bookData.title);
    formData.append('summary', bookData.summary);
    if (bookData.categoryId) {
      formData.append('categoryId', String(bookData.categoryId));
    }

    try {
      const response = await apiClient.post(`/api/v1/groups/${groupId}/books`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      return response.data;
    } catch (error) {
      console.error(`그룹 책 생성 실패 (Group ID: ${groupId}):`, error);
      throw error;
    }
  }

  // 그룹 책에 새로운 에피소드를 추가합니다.
  async addEpisode(groupId: string, bookId: string, episodeData: { title: string }): Promise<any> {
    try {
      const response = await apiClient.post(`/api/v1/groups/${groupId}/books/${bookId}/episodes`, episodeData);
      return response.data;
    } catch (error) {
      console.error(`에피소드 추가 실패 (Group ID: ${groupId}, Book ID: ${bookId}):`, error);
      throw error;
    }
  }

  async deleteEpisode(groupId: string, bookId: string, episodeId: number): Promise<void> {
    try {
      await apiClient.delete(`/api/v1/groups/${groupId}/books/${bookId}/episodes/${episodeId}`);
    } catch (error) {
      console.error(`에피소드 삭제 실패 (Group ID: ${groupId}, Book ID: ${bookId}, Episode ID: ${episodeId}):`, error);
      throw error;
    }
  }

  // 그룹 책의 에피소드를 수정합니다.
  async updateEpisode(groupId: string, bookId: string, episodeId: number, episodeData: { title: string; content: string }): Promise<any> {
    try {
      const response = await apiClient.patch(`/api/v1/groups/${groupId}/books/${bookId}/episodes/${episodeId}`, episodeData);
      return response.data;
    } catch (error) {
      console.error(`에피소드 수정 실패 (Group ID: ${groupId}, Book ID: ${bookId}, Episode ID: ${episodeId}):`, error);
      throw error;
    }
  }

  // 그룹 책을 완성합니다.
  async completeGroupBook(groupId: string, bookId: string, tags: string[]): Promise<any> {
    try {
      const response = await apiClient.patch(`/api/v1/groups/${groupId}/books/${bookId}/completed`, { tags });
      return response.data;
    } catch (error) {
      console.error(`그룹 책 완성 실패 (Group ID: ${groupId}, Book ID: ${bookId}):`, error);
      throw error;
    }
  }

  // 그룹 책 에피소드에 이미지를 업로드합니다.
  async uploadEpisodeImage(groupId: string, bookId: string, episodeId: number, file: File): Promise<any> {
    const formData = new FormData();
    formData.append('file', file);
    try {
      const response = await apiClient.post(`/api/v1/groups/${groupId}/books/${bookId}/episodes/${episodeId}/images`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      return response.data;
    } catch (error) {
      console.error(`에피소드 이미지 업로드 실패 (Group ID: ${groupId}, Book ID: ${bookId}, Episode ID: ${episodeId}):`, error);
      throw error;
    }
  }

  // 그룹 책 에피소드의 이미지를 삭제합니다.
  async deleteEpisodeImage(groupId: string, bookId: string, episodeId: number, imageId: number): Promise<void> {
    try {
      await apiClient.delete(`/api/v1/groups/${groupId}/books/${bookId}/episodes/${episodeId}/images/${imageId}`);
    } catch (error) {
      console.error(`에피소드 이미지 삭제 실패 (Group ID: ${groupId}, Book ID: ${bookId}, Episode ID: ${episodeId}, Image ID: ${imageId}):`, error);
      throw error;
    }
  }

  // 그룹 책 상세 정보를 조회합니다.
  async getGroupBookDetails(groupId: string, bookId: string): Promise<any> {
    try {
      const response = await apiClient.get(`/api/v1/groups/${groupId}/books/${bookId}`);
      return response.data;
    } catch (error) {
      console.error(`그룹 책 상세 정보 조회 실패 (Group ID: ${groupId}, Book ID: ${bookId}):`, error);
      throw error;
    }
  }

  // 그룹 책 에피소드의 대화 세션을 시작합니다.
  async startConversation(groupId: string, bookId: string, episodeId: number): Promise<any> {
    try {
      const response = await apiClient.post(`/api/v1/groups/${groupId}/books/${bookId}/episodes/${episodeId}/sessions`);
      return response.data;
    } catch (error) {
      console.error(`대화 세션 시작 실패 (Group ID: ${groupId}, Book ID: ${bookId}, Episode ID: ${episodeId}):`, error);
      throw error;
    }
  }

  // SSE 스트림 연결을 종료합니다.
  async closeSseStream(groupId: string, bookId: string, episodeId: number, sessionId: string): Promise<void> {
    try {
      await apiClient.delete(`/api/v1/groups/${groupId}/books/${bookId}/episodes/${episodeId}/stream/${sessionId}`);
    } catch (error) {
      console.error(`SSE 스트림 연결 종료 실패 (Session ID: ${sessionId}):`, error);
      throw error;
    }
  }
  async fetchGroupBooks(groupId: string): Promise<any[]> {
    try {
      const response = await apiClient.get(`/api/v1/groups/${groupId}/books`);
      return response.data.data;
    } catch (error) {
      console.error(`그룹 책 목록 조회 실패 (ID: ${groupId}):`, error);
      return [];
    }
  }
}

export const groupService = new GroupService();
