import apiClient from '@/api';

// --- 인터페이스 정의 ---
export interface GroupBook {
  groupBookId: number;
  groupId: number;
  title: string;
  summary: string;
  groupType: 'FAMILY' | 'FRIENDS' | 'COUPLE' | 'TEAM' | 'OTHER';
  createdAt: string;
  updatedAt: string;
  episodes?: GroupEpisode[];
}

export interface GroupEpisode {
  groupEpisodeId: number;
  title: string;
  orderNo: number;
  status: 'DRAFT' | 'IN_PROGRESS' | 'COMPLETED';
  rawNotes?: string;
  editedContent?: string;
  currentStep?: number;
  template: 'INTRO' | 'STORY' | 'REFLECTION' | 'FUTURE';
  createdAt: string;
  updatedAt: string;
  imageUrl?: string;
  imageId?: number;
}

export interface GroupEpisodeImage {
  imageId: number;
  imageUrl: string;
  description?: string;
  orderNo: number;
  createdAt: string;
}

export interface GroupBookCreateRequest {
  title: string;
  summary?: string;
  groupType: string;
}

export interface GroupEpisodeCreateRequest {
  title: string;
  summary?: string;
  orderNo?: number;
}

export interface GroupEpisodeUpdateRequest {
  title?: string;
  summary?: string;
  orderNo?: number;
  template?: string;
  editedContent?: string;
}

export interface GroupAnswerRequest {
  answer: string;
  questionKey?: string;
  currentTemplate?: string;
  currentStep?: number;
}

export interface GroupBookCompleteRequest {
  tags: string[];
}

export interface ApiResponse<T> {
  success: boolean;
  status: number;
  message: string;
  data: T;
  timestamp: string;
  path: string;
}

class GroupBookService {
  private getHeaders() {
    return {
      'Content-Type': 'application/json'
    };
  }

  private getMultipartHeaders() {
    return {
      // FormData 사용 시 Content-Type을 undefined로 설정하여 브라우저가 자동으로 boundary와 함께 설정하도록 함
      'Content-Type': undefined
    };
  }

  // === 그룹북 관련 API ===

  /**
   * 그룹북 생성
   */
  async createGroupBook(groupId: number, data: GroupBookCreateRequest): Promise<GroupBook | null> {
    try {
      console.log('API 요청 URL:', `/api/v1/groups/${groupId}/books`);
      console.log('API 요청 데이터:', data);
      console.log('API 요청 헤더:', this.getHeaders());
      
      const response = await apiClient.post(`/api/v1/groups/${groupId}/books`, data);
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.message || '그룹북 생성 실패');
      }
    } catch (error) {
      console.error('그룹북 생성 실패:', error);
      throw error;
    }
  }

  /**
   * 그룹북 조회
   */
  async getGroupBook(groupId: number, groupBookId: number): Promise<GroupBook | null> {
    try {
      const response = await apiClient.get(`/api/v1/groups/${groupId}/books/${groupBookId}`, {
        headers: this.getHeaders()
      });
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.message || '그룹북 조회 실패');
      }
    } catch (error) {
      console.error('그룹북 조회 실패:', error);
      return null;
    }
  }

  /**
   * 그룹북 목록 조회
   */
  async getGroupBooks(groupId: number): Promise<GroupBook[]> {
    try {
      const response = await apiClient.get(`/api/v1/groups/${groupId}/books`, {
        headers: this.getHeaders()
      });
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.message || '그룹북 목록 조회 실패');
      }
    } catch (error) {
      console.error('그룹북 목록 조회 실패:', error);
      return [];
    }
  }

  /**
   * 그룹북 수정
   */
  async updateGroupBook(groupId: number, groupBookId: number, data: Partial<GroupBookCreateRequest> | FormData): Promise<GroupBook | null> {
    try {
      // FormData인지 일반 객체인지 확인하여 헤더를 다르게 설정
      const isFormData = data instanceof FormData;
      const headers = isFormData ? this.getMultipartHeaders() : this.getHeaders();
      
      console.log('그룹북 수정 API 호출:', {
        url: `/api/v1/groups/${groupId}/books/${groupBookId}`,
        method: 'PATCH',
        dataType: isFormData ? 'FormData' : 'JSON',
        headers: headers
      });

      const response = await apiClient.patch(`/api/v1/groups/${groupId}/books/${groupBookId}`, data, {
        headers: headers
      });
      
      console.log('그룹북 수정 API 응답:', response.data);
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.message || '그룹북 수정 실패');
      }
    } catch (error) {
      console.error('그룹북 수정 실패:', error);
      console.error('에러 상세:', {
        message: error.message,
        response: error.response?.data,
        status: error.response?.status,
        statusText: error.response?.statusText
      });
      throw error;
    }
  }

  /**
   * 그룹북 삭제
   */
  async deleteGroupBook(groupId: number, groupBookId: number): Promise<boolean> {
    try {
      const response = await apiClient.delete(`/api/v1/groups/${groupId}/books/${groupBookId}`, {
        headers: this.getHeaders()
      });
      
      return response.data.success;
    } catch (error) {
      console.error('그룹북 삭제 실패:', error);
      return false;
    }
  }

  /**
   * 그룹북 발행 완료
   */
  async completeGroupBook(groupId: number, groupBookId: number, data: GroupBookCompleteRequest): Promise<boolean> {
    try {
      const response = await apiClient.patch(`/api/v1/groups/${groupId}/books/${groupBookId}/completed`, data, {
        headers: this.getHeaders()
      });
      
      return response.data.success;
    } catch (error) {
      console.error('그룹북 발행 실패:', error);
      throw error;
    }
  }

  // === 에피소드 관련 API ===

  /**
   * 에피소드 생성
   */
  async createEpisode(groupId: number, groupBookId: number, data: GroupEpisodeCreateRequest): Promise<GroupEpisode | null> {
    try {
      const response = await apiClient.post(`/api/v1/groups/${groupId}/books/${groupBookId}/episodes`, data, {
        headers: this.getHeaders()
      });
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.message || '에피소드 생성 실패');
      }
    } catch (error) {
      console.error('에피소드 생성 실패:', error);
      throw error;
    }
  }

  /**
   * 에피소드 조회
   */
  async getEpisode(groupId: number, groupBookId: number, episodeId: number): Promise<GroupEpisode | null> {
    try {
      const response = await apiClient.get(`/api/v1/groups/${groupId}/books/${groupBookId}/episodes/${episodeId}`, {
        headers: this.getHeaders()
      });
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.message || '에피소드 조회 실패');
      }
    } catch (error) {
      console.error('에피소드 조회 실패:', error);
      return null;
    }
  }

  /**
   * 에피소드 목록 조회
   */
  async getEpisodes(groupId: number, groupBookId: number): Promise<GroupEpisode[]> {
    try {
      const response = await apiClient.get(`/api/v1/groups/${groupId}/books/${groupBookId}/episodes`, {
        headers: this.getHeaders()
      });
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.message || '에피소드 목록 조회 실패');
      }
    } catch (error) {
      console.error('에피소드 목록 조회 실패:', error);
      return [];
    }
  }

  /**
   * 에피소드 수정
   */
  async updateEpisode(groupId: number, groupBookId: number, episodeId: number, data: GroupEpisodeUpdateRequest): Promise<GroupEpisode | null> {
    try {
      const response = await apiClient.patch(`/api/v1/groups/${groupId}/books/${groupBookId}/episodes/${episodeId}`, data, {
        headers: this.getHeaders()
      });
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.message || '에피소드 수정 실패');
      }
    } catch (error) {
      console.error('에피소드 수정 실패:', error);
      throw error;
    }
  }

  /**
   * 에피소드 삭제
   */
  async deleteEpisode(groupId: number, groupBookId: number, episodeId: number): Promise<boolean> {
    try {
      const response = await apiClient.delete(`/api/v1/groups/${groupId}/books/${groupBookId}/episodes/${episodeId}`, {
        headers: this.getHeaders()
      });
      
      return response.data.success;
    } catch (error) {
      console.error('에피소드 삭제 실패:', error);
      return false;
    }
  }

  /**
   * 에피소드 이미지 조회
   */
  async getEpisodeImage(groupId: number, groupBookId: number, episodeId: number): Promise<GroupEpisodeImage | null> {
    try {
      const response = await apiClient.get(`/api/v1/groups/${groupId}/books/${groupBookId}/episodes/${episodeId}/images`, {
        headers: this.getHeaders()
      });
      
      if (response.data.success) {
        const images = response.data.data;
        // 이미지 리스트에서 첫 번째 이미지만 반환 (한 장만 업로드 가능하므로)
        return images && images.length > 0 ? images[0] : null;
      } else {
        throw new Error(response.data.message || '에피소드 이미지 조회 실패');
      }
    } catch (error) {
      console.error('에피소드 이미지 조회 실패:', error);
      return null;
    }
  }
  // === 대화 세션 관련 API ===

  /**
   * 대화 세션 시작
   */
  async startConversation(groupId: number, groupBookId: number, episodeId: number): Promise<string> {
    try {
      const response = await apiClient.post(`/api/v1/groups/${groupId}/books/${groupBookId}/episodes/${episodeId}/sessions`, {}, {
        headers: this.getHeaders()
      });
      
      if (response.data.success) {
        return response.data.data.sessionId;
      } else {
        throw new Error(response.data.message || '세션 시작 실패');
      }
    } catch (error) {
      console.error('세션 시작 실패:', error);
      throw error;
    }
  }

  /**
   * SSE 스트림 연결
   */
  async establishConversationStream(groupId: number, groupBookId: number, episodeId: number, sessionId: string): Promise<EventSource> {
    const baseURL = apiClient.defaults?.baseURL || '';
    const sseUrl = `${baseURL}/api/v1/groups/${groupId}/books/${groupBookId}/episodes/${episodeId}/${sessionId}/stream`;
    
    // SSE는 인증 헤더를 직접 설정할 수 없으므로 URL에 토큰을 포함하거나 쿠키를 사용해야 함
    // 현재 구현에서는 SecurityConfig에서 해당 경로를 permitAll로 설정했음ㅇ
    const eventSource = new EventSource(sseUrl, { 
      withCredentials: true 
    });
    
    return eventSource;
  }

  /**
   * 다음 질문 요청
   */
  async getNextQuestion(groupId: number, groupBookId: number, episodeId: number, sessionId: string): Promise<boolean> {
    try {
      const response = await apiClient.post(
        `/api/v1/groups/${groupId}/books/${groupBookId}/episodes/${episodeId}/conversation/next?sessionId=${sessionId}`, 
        {}, 
        {
          headers: this.getHeaders()
        }
      );
      
      return response.data.success;
    } catch (error) {
      console.error('다음 질문 요청 실패:', error);
      throw error;
    }
  }

  /**
   * 답변 제출
   */
  async submitAnswer(groupId: number, groupBookId: number, episodeId: number, sessionId: string, answerData: GroupAnswerRequest): Promise<boolean> {
    try {
      const response = await apiClient.post(
        `/api/v1/groups/${groupId}/books/${groupBookId}/episodes/${episodeId}/conversation/answer?sessionId=${sessionId}`,
        answerData,
        {
          headers: this.getHeaders()
        }
      );
      
      return response.data.success;
    } catch (error) {
      console.error('답변 제출 실패:', error);
      throw error;
    }
  }

  /**
   * SSE 연결 종료
   */
  async closeSseStream(groupId: number, groupBookId: number, episodeId: number, sessionId: string): Promise<boolean> {
    try {
      const response = await apiClient.delete(`/api/v1/groups/${groupId}/books/${groupBookId}/episodes/${episodeId}/stream/${sessionId}`, {
        headers: this.getHeaders()
      });
      
      return response.data.success;
    } catch (error) {
      console.error('SSE 연결 종료 실패:', error);
      return false;
    }
  }

  // === 이미지 관련 API ===

  /**
   * 에피소드 이미지 업로드
   */
  async uploadEpisodeImage(groupId: number, groupBookId: number, episodeId: number, file: File, description?: string, orderNo?: number): Promise<GroupEpisodeImage | null> {
    try {
      console.log('에피소드 이미지 업로드 API 호출 시작:', {
        url: `/api/v1/groups/${groupId}/books/${groupBookId}/episodes/${episodeId}/images`,
        fileName: file.name,
        fileSize: file.size,
        fileType: file.type,
        description,
        orderNo
      });

      const formData = new FormData();
      formData.append('file', file);
      if (description) formData.append('description', description);
      if (orderNo) formData.append('orderNo', orderNo.toString());

      const response = await apiClient.post(
        `/api/v1/groups/${groupId}/books/${groupBookId}/episodes/${episodeId}/images`,
        formData,
        {
          headers: this.getMultipartHeaders()
        }
      );
      
      console.log('에피소드 이미지 업로드 API 응답:', response.data);
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.message || '이미지 업로드 실패');
      }
    } catch (error) {
      console.error('이미지 업로드 실패:', error);
      console.error('에러 상세:', {
        message: error.message,
        response: error.response?.data,
        status: error.response?.status,
        statusText: error.response?.statusText
      });
      throw error;
    }
  }

  /**
   * 에피소드 이미지 목록 조회
   */
  async getEpisodeImages(groupId: number, groupBookId: number, episodeId: number): Promise<GroupEpisodeImage[]> {
    try {
      const response = await apiClient.get(`/api/v1/groups/${groupId}/books/${groupBookId}/episodes/${episodeId}/images`, {
        headers: this.getHeaders()
      });
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.message || '이미지 목록 조회 실패');
      }
    } catch (error) {
      console.error('이미지 목록 조회 실패:', error);
      return [];
    }
  }

  /**
   * 에피소드 이미지 삭제
   */
  async deleteEpisodeImage(groupId: number, groupBookId: number, episodeId: number, imageId: number): Promise<boolean> {
    try {
      const response = await apiClient.delete(`/api/v1/groups/${groupId}/books/${groupBookId}/episodes/${episodeId}/images/${imageId}`, {
        headers: this.getHeaders()
      });
      
      return response.data.success;
    } catch (error) {
      console.error('이미지 삭제 실패:', error);
      return false;
    }
  }

  /**
   * 다음 템플릿 에피소드 생성
   */
  async createNextTemplateEpisode(groupId: number, groupBookId: number, currentTemplate: string): Promise<GroupEpisode | null> {
    try {
      const response = await apiClient.post(
        `/api/v1/groups/${groupId}/books/${groupBookId}/episodes/next-template?currentTemplate=${currentTemplate}`,
        {},
        {
          headers: this.getHeaders()
        }
      );
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.message || '다음 템플릿 에피소드 생성 실패');
      }
    } catch (error) {
      console.error('다음 템플릿 에피소드 생성 실패:', error);
      throw error;
    }
  }

  // === 유틸리티 메서드 ===

  /**
   * 그룹 타입을 한글로 변환
   */
  getGroupTypeKorean(groupType: string): string {
    const typeMap: Record<string, string> = {
      'FAMILY': '가족',
      'FRIENDS': '친구들',
      'COUPLE': '커플',
      'TEAM': '팀',
      'OTHER': '기타'
    };
    return typeMap[groupType] || '기타';
  }

  /**
   * 템플릿 이름을 한글로 변환
   */
  getTemplateKorean(template: string): string {
    const templateMap: Record<string, string> = {
      'INTRO': '소개',
      'STORY': '이야기',
      'REFLECTION': '회상',
      'FUTURE': '미래'
    };
    return templateMap[template] || '소개';
  }

  /**
   * 에피소드 상태를 한글로 변환
   */
  getStatusKorean(status: string): string {
    const statusMap: Record<string, string> = {
      'DRAFT': '초안',
      'IN_PROGRESS': '진행중',
      'COMPLETED': '완료'
    };
    return statusMap[status] || '초안';
  }
}

// 싱글톤 인스턴스 생성 및 내보내기
export const groupBookService = new GroupBookService();