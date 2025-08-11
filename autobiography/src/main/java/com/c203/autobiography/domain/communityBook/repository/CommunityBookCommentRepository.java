package com.c203.autobiography.domain.communityBook.repository;

import com.c203.autobiography.domain.communityBook.dto.CommunityBookCommentDetailResponse;
import com.c203.autobiography.domain.communityBook.entity.CommunityBookComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CommunityBookCommentRepository extends JpaRepository<CommunityBookComment, Long>, JpaSpecificationExecutor<CommunityBookComment> {
    CommunityBookComment save(CommunityBookComment communityBookComment);

    Optional<CommunityBookComment> findByCommunityBookCommentIdAndCommunityBookCommunityBookId(Long communityBookCommentId, Long communityBookId);

    Page<CommunityBookComment> findByCommunityBookCommunityBookIdOrderByCreatedAtAsc(Long communityBookId, Pageable pageable);
}
