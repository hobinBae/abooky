import apiClient, { apiClientPublic } from '@/api';

// =================================================================
// Type Definitions
// =================================================================

export interface Pageable {
  page: number;
  size: number;
  sort?: string;
}

export interface SearchParams {
  page?: number;
  size?: number;
  sortBy?: string;
  title?: string;
  tags?: string[];
  categoryId?: number;
  bookType?: string;
}

export interface Tag {
  tagId: number;
  tagName: string;
}

export interface CommunityBook {
  communityBookId: number;
  memberId: number;
  title: string;
  summary: string;
  description: string; // Note: description is not in DetailResponse, but might be in ListResponse. Keeping for flexibility.
  coverImageUrl: string;
  authorNickname: string; // This needs to be mapped from member info. Assuming it comes with the list response.
  categoryName?: string;
  createdAt: string;
  updatedAt: string;
  likeCount: number;
  isLiked: boolean;
  isBookmarked: boolean;
  viewCount: number;
  tags: Tag[];
  averageRating: number;
}

export interface CommunityBookDetailResponse extends CommunityBook {
  communityEpisodes: CommunityBookEpisode[];
  categoryName?: string;
}

export interface CommunityBookEpisode {
    episodeId: number;
    title: string;
    content: string;
    createdAt: string;
    updatedAt: string;
    imageUrl?: string;
}

export interface CommunityBookListResponse {
  content: CommunityBook[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

export interface CommunityBookComment {
    communityBookCommentId: number;
    memberId: number;
    nickname: string;
    profileImageUrl?: string;
    content: string;
    createdAt: string;
    updatedAt: string;
}

export interface CommunityBookCommentCreateRequest {
    communityBookId: number;
    content: string;
}

export type CommunityBookCommentCreateResponse = CommunityBookComment;

export interface CommunityBookCommentListResponse {
    content: CommunityBookComment[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
}

export interface CommunityBookCommentDeleteResponse {
    id: number;
}

export interface LikeResponse {
    liked: boolean;
    likeCount: number;
}

export interface CommunityBookLikeCntResponse {
    communityBookId: number;
    likeCount: number;
}

export interface CommunityBookRatingRequest {
    communityBookId: number;
    score: number;
}

export interface CommunityBookRatingResponse {
    communityBookId: number;
    rating: number;
}

export interface CommunityBookCreateResponse {
    communityBookId: number;
}


// =================================================================
// Service
// =================================================================

const BASE_URL = '/api/v1/communities/community-book';
const BOOK_BASE_URL = '/api/v1/books';

export const communityService = {
  // 커뮤니티 책 상세 조회
  getCommunityBookDetail(communityBookId: number): Promise<CommunityBookDetailResponse> {
    return apiClient.get(`${BASE_URL}/detail/${communityBookId}`).then(res => res.data.data);
  },

  // 커뮤니티 책 목록 조회
  getCommunityBooks(pageable: Pageable, sortBy: string): Promise<CommunityBookListResponse> {
    return apiClient.get(`${BASE_URL}/books`, { params: { ...pageable, sortBy } }).then(res => res.data.data);
  },

  // 커뮤니티 책 검색
  searchCommunityBooks(params: SearchParams): Promise<CommunityBookListResponse> {
    return apiClientPublic.get(`${BASE_URL}/search`, { params }).then(res => res.data.data);
  },

  // 특정 작가가 작성한 커뮤니티 책 목록 조회
  getMemberCommunityBooks(memberId: number, params: SearchParams): Promise<CommunityBookListResponse> {
    return apiClient.get(`${BASE_URL}/${memberId}`, { params }).then(res => res.data.data);
  },

  // 내가 작성한 커뮤니티 책 목록 조회
  getMyCommunityBooks(params: SearchParams): Promise<CommunityBookListResponse> {
    return apiClient.get(BASE_URL, { params }).then(res => res.data.data);
  },

  // 커뮤니티 책 삭제
  deleteCommunityBook(communityBookId: number): Promise<void> {
    return apiClient.delete(`${BASE_URL}/books/${communityBookId}`);
  },

  // 커뮤니티 책 댓글 생성
  createCommunityBookComment(communityBookId: number, data: CommunityBookCommentCreateRequest): Promise<CommunityBookCommentCreateResponse> {
    return apiClient.post(`${BASE_URL}/${communityBookId}/comments`, data).then(res => res.data.data);
  },

  // 커뮤니티 책 댓글 목록 조회
  getCommunityBookComments(communityBookId: number, pageable: Pageable): Promise<CommunityBookCommentListResponse> {
    return apiClient.get(`${BASE_URL}/${communityBookId}/comments`, { params: pageable }).then(res => res.data.data);
  },

  // 커뮤니티 책 댓글 삭제
  deleteCommunityBookComment(communityBookId: number, communityBookCommentId: number): Promise<CommunityBookCommentDeleteResponse> {
    return apiClient.delete(`${BASE_URL}/${communityBookId}/comments/${communityBookCommentId}`).then(res => res.data.data);
  },

  // 커뮤니티 책 좋아요/좋아요 취소
  toggleLike(communityBookId: number): Promise<LikeResponse> {
    return apiClient.post(`${BASE_URL}/${communityBookId}/likes`).then(res => res.data.data);
  },

  // 커뮤니티 북 좋아요 수 조회
  getLikeCount(communityBookId: number): Promise<CommunityBookLikeCntResponse> {
    return apiClient.get(`${BASE_URL}/${communityBookId}/likes/count`).then(res => res.data.data);
  },

  // 커뮤니티 북 북마크/북마크 취소
  toggleBookmark(communityBookId: number): Promise<{ bookmarked: boolean }> {
    return apiClient.post(`${BASE_URL}/${communityBookId}/bookmark`).then(res => res.data.data);
  },

  // 커뮤니티 책 평점 생성
  createOrUpdateRating(data: CommunityBookRatingRequest): Promise<CommunityBookRatingResponse> {
    return apiClient.post(`${BASE_URL}/ratings`, data).then(res => res.data.data);
  },

  // 커뮤니티 책 평점 조회
  getRating(communityBookId: number): Promise<CommunityBookRatingResponse> {
    return apiClient.get(`${BASE_URL}/ratings/${communityBookId}`).then(res => res.data.data);
  },

  // 개인 책을 커뮤니티로 내보내기
  exportBookToCommunity(bookId: number): Promise<CommunityBookCreateResponse> {
    return apiClient.post(`${BOOK_BASE_URL}/${bookId}/export/community`).then(res => res.data.data);
  },

  // 북마크한 커뮤니티 책 목록 조회
  getBookmarkedCommunityBooks(pageable: Pageable): Promise<CommunityBookListResponse> {
    return apiClient.get(`${BASE_URL}/bookmarks`, { params: pageable }).then(res => res.data.data);
  },

  // 좋아요한 커뮤니티 책 목록 조회
  getLikedCommunityBooks(pageable: Pageable): Promise<CommunityBookListResponse> {
    return apiClient.get(`${BASE_URL}/likes`, { params: pageable }).then(res => res.data.data);
  }
};
