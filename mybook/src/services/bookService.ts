import apiClient from '@/api';

interface RepresentBookRequest {
  isRepresentative: boolean;
}

interface RepresentBookResponse {
  memberId: number;
  representBookId: number | null;
}

class BookService {
  async setRepresentativeBook(bookId: string, isRepresentative: boolean): Promise<RepresentBookResponse> {
    const request: RepresentBookRequest = { isRepresentative };
    const response = await apiClient.patch(`/api/v1/members/me/books/${bookId}/represent`, request);
    return response.data.data;
  }
}

export const bookService = new BookService();
