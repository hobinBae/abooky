package com.c203.autobiography.domain.book.repository;

import com.c203.autobiography.domain.book.entity.MemberRatingSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRatingSummaryRespository extends JpaRepository<MemberRatingSummary, Long> {
}
