package com.c203.autobiography.domain.groupbook.service;

import com.c203.autobiography.domain.groupbook.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GroupBookService {
    GroupBookResponse createBook(Long groupId, Long memberId, GroupBookCreateRequest request, MultipartFile file);
    
    GroupBookCreateResponse createBookSimple(Long groupId, Long memberId, GroupBookCreateRequest request, MultipartFile file);

    GroupBookResponse updateBook(Long groupId, Long memberId, Long bookId, GroupBookUpdateRequest request, MultipartFile file);

    Void deleteBook(Long groupId, Long memberId, Long bookId);

    GroupBookResponse completeBook(Long groupId, Long memberId, Long bookId, List<String> tags);

    GroupBookResponse getBookDetail(Long groupId, Long memberId, Long bookId);

    List<GroupBookResponse> getMyGroupBooks(Long groupId, Long memberId);

//    BookCopyResponse copyBook(Long memberId, Long bookId, BookCopyRequest request);

    Page<GroupBookResponse> searchBooks(String title, Long categoryId,List<String> tags, Pageable pageable);

//    LikeResponse toggleLike(Long bookId, Long memberId);
//
//    BookRatingResponse rateBook(Long memberId, Long bookId, BookRatingRequest request);
//
//    BookRatingResponse getBookRating(Long memberId, Long bookId);

    GroupBookCommentCreateResponse createGroupBookComment(Long memberId, @Valid GroupBookCommentCreateRequest request);

    GroupBookCommentListResponse getGroupBookComments(Long memberId, Long groupBookId, Pageable pageable);

    GroupBookCommentDeleteResponse deleteGroupBookComment(Long groupBookId, Long groupBookCommentId, Long memberId);
}
