package com.c203.autobiography.domain.communityBook.repository;

import com.c203.autobiography.domain.communityBook.entity.CommunityBook;
import com.c203.autobiography.domain.communityBook.entity.CommunityBookComment;
import com.c203.autobiography.domain.communityBook.entity.CommunityBookEpisode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CommunityBookEpisodeRepository extends JpaRepository<CommunityBookEpisode, Long>, JpaSpecificationExecutor<CommunityBookEpisode> {
    CommunityBook save(CommunityBook communityBook);
}
