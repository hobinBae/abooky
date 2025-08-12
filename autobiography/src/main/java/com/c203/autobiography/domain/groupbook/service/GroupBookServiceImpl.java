package com.c203.autobiography.domain.groupbook.service;

import com.c203.autobiography.domain.book.entity.BookCategory;
import com.c203.autobiography.domain.book.entity.Tag;
import com.c203.autobiography.domain.book.repository.BookCategoryRepository;
import com.c203.autobiography.domain.book.repository.TagRepository;
import com.c203.autobiography.domain.group.entity.Group;
import com.c203.autobiography.domain.group.repository.GroupMemberRepository;
import com.c203.autobiography.domain.group.repository.GroupRepository;
import com.c203.autobiography.domain.groupbook.dto.GroupBookCreateRequest;
import com.c203.autobiography.domain.groupbook.dto.GroupBookResponse;
import com.c203.autobiography.domain.groupbook.dto.GroupBookUpdateRequest;
import com.c203.autobiography.domain.groupbook.entity.GroupBook;
import com.c203.autobiography.domain.groupbook.episode.dto.GroupEpisodeResponse;
import com.c203.autobiography.domain.groupbook.episode.repository.GroupEpisodeRepository;
import com.c203.autobiography.domain.groupbook.episode.service.GuideQuestion;
import com.c203.autobiography.domain.groupbook.episode.service.GuideResolverService;
import com.c203.autobiography.domain.groupbook.repository.GroupBookRepository;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import com.c203.autobiography.global.s3.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupBookServiceImpl implements GroupBookService {

    private final GroupBookRepository groupBookRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final BookCategoryRepository bookCategoryRepository;
    private final FileStorageService fileStorageService;
    private final GroupEpisodeRepository episodeRepository;
    private final TagRepository tagRepository;
    private final GuideResolverService guideResolver;

    @Override
    @Transactional
    public GroupBookResponse createBook(Long groupId, Long memberId, GroupBookCreateRequest request, MultipartFile file) {
        Group group = groupRepository.findByGroupIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        BookCategory category = switch (request.getBookType()) {
            case AUTO -> bookCategoryRepository
                    .findByCategoryName("자서전")
                    .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
            case DIARY -> bookCategoryRepository
                    .findByCategoryName("일기")
                    .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
            case FREE_FORM -> {
                if (request.getCategoryId() == null) {
                    throw new ApiException(ErrorCode.VALIDATION_FAILED);
                }
                yield bookCategoryRepository
                        .findById(request.getCategoryId())
                        .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
            }
            default -> throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        };

        String coverImageUrl = null;
        if (file != null && !file.isEmpty()) {
            coverImageUrl = fileStorageService.store(file, "groupBook");
        }
        GroupBook book = request.toEntity(member, group, category, coverImageUrl);

        GroupBook saved = groupBookRepository.save(book);

        // 🎯 첫 질문 미리 준비 (핵심!)
        GuideQuestion firstQuestion = guideResolver.resolveFirst(saved.getGroupType(), "INTRO");

        // 🎯 첫 질문과 함께 응답 생성
        return GroupBookResponse.ofWithFirstQuestion(
                saved,
                List.of(),    // 아직 에피소드 없음
                List.of(),    // 아직 태그 없음
                firstQuestion.question(),  // 첫 질문 텍스트
                firstQuestion.key()        // 질문 키
        );
    }

    @Override
    @Transactional
    public GroupBookResponse updateBook(Long groupId, Long memberId, Long bookId, GroupBookUpdateRequest request, MultipartFile file) {
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        groupRepository.findByGroupIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        if(!groupBook.getMember().getMemberId().equals(memberId)){
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        BookCategory category = null;

        if(request.getCategoryId() != null) {
            category = bookCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
        }

        String newImageUrl = groupBook.getCoverImageUrl();
        if(file!=null && !file.isEmpty()) {
            String currentImageUrl = groupBook.getCoverImageUrl();
            if(currentImageUrl != null && !currentImageUrl.isBlank()) {
                fileStorageService.delete(currentImageUrl);
            }
            newImageUrl = fileStorageService.store(file, "groupBook");
        }
        groupBook.updateGroupBook(request.getTitle(), newImageUrl, request.getSummary(), category);
        return GroupBookResponse.of(groupBook);
    }

    @Override
    @Transactional
    public Void deleteBook(Long groupId, Long memberId, Long bookId) {
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        groupRepository.findByGroupIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        if(!groupBook.getMember().getMemberId().equals(memberId)){
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        groupBook.softDelete();
        return null;
    }

    @Override
    @Transactional
    public GroupBookResponse completeBook(Long groupId, Long memberId, Long bookId, List<String> tags) {
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        groupRepository.findByGroupIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        if(!groupBook.getMember().getMemberId().equals(memberId)){
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        groupBook.markCompleted();

        if(tags!=null && !tags.isEmpty()) {
            groupBook.getTags().clear();
            tags.stream().distinct().forEach(name -> {
                Tag tag = tagRepository.findByTagName(name)
                        .orElseGet(() -> tagRepository.save(
                                Tag.builder().tagName(name).build()
                        ));
                groupBook.addTag(tag);
            });
        }

        List<GroupEpisodeResponse> episodes = episodeRepository
                .findByGroupBook_GroupBookIdOrderByOrderNoAscCreatedAtAsc(bookId)
                .stream()
                .map(GroupEpisodeResponse::of)
                .toList();


        List<String> tagNames = groupBook.getTags().stream()
                .map(bt -> bt.getTag().getTagName())
                .toList();

        return GroupBookResponse.of(groupBook, episodes, tagNames);
    }

    @Override
    public GroupBookResponse getBookDetail(Long groupId, Long memberId, Long bookId) {
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        groupRepository.findByGroupIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        boolean isMember = groupMemberRepository.findByGroupIdAndMemberIdAndDeletedAtIsNull(groupId, memberId)
                .isPresent();
        if (!isMember) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        // 에피소드 목록 조회 + DTO 매핑
        List<GroupEpisodeResponse> episodes = episodeRepository
                .findByGroupBook_GroupBookIdOrderByOrderNoAscCreatedAtAsc(bookId)
                .stream()
                .map(GroupEpisodeResponse::of)
                .toList();

        // tag
        List<String> tagNames = groupBook.getTags().stream()
                .map(bt -> bt.getTag().getTagName())
                .toList();

        return GroupBookResponse.of(groupBook, episodes, tagNames);
    }

    @Override
    public List<GroupBookResponse> getMyGroupBooks(Long groupId, Long memberId) {
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        groupRepository.findByGroupIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));

        boolean isMember = groupMemberRepository.findByGroupIdAndMemberIdAndDeletedAtIsNull(groupId, memberId)
                .isPresent();
        if (isMember) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        return groupBookRepository
                .findAllByGroupGroupIdAndDeletedAtIsNull(groupId)
                .stream()
                .map(groupBook -> {
                        List<GroupEpisodeResponse> episodes = List.of();

                        // 실제 붙어있는 태그 이름만 추출
                        List<String> tagNames = groupBook.getTags().stream()
                                .map(bt-> bt.getTag().getTagName())
                                .toList();

                        return GroupBookResponse.of(groupBook, episodes, tagNames);
                })
                .toList();
    }

    @Override
    public Page<GroupBookResponse> searchBooks(String title, Long categoryId, List<String> tags, Pageable pageable) {
        return null;
    }
}
