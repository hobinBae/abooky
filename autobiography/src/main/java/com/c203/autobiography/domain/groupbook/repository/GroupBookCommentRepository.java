package com.c203.autobiography.domain.groupbook.repository;

import com.c203.autobiography.domain.groupbook.entity.GroupBookComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface GroupBookCommentRepository extends JpaRepository<GroupBookComment, Long>, JpaSpecificationExecutor<GroupBookComment> {
    Optional<GroupBookComment> findByGroupBookCommentIdAndGroupBookGroupBookId(Long groupBookCommentId, Long groupBookId);

    Page<GroupBookComment> findByGroupBookGroupBookIdOrderByCreatedAtAsc(Long groupBookId, Pageable pageable);
}
