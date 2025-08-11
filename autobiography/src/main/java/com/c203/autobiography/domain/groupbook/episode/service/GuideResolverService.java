package com.c203.autobiography.domain.groupbook.episode.service;

import com.c203.autobiography.domain.groupbook.entity.GroupBook;
import com.c203.autobiography.domain.groupbook.entity.GroupType;
import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisode;

import java.util.Optional;

public interface GuideResolverService {
//    String firstQuestion(GroupBook gb, String template);
//    GuideQuestion questionForStep(GroupEpisode ep, int stepNo);
//    GuideQuestion nextQuestion(GroupEpisode ep, int stepNo);
    GuideQuestion resolveFirst(GroupType groupType, String template);
    Optional<GuideQuestion> resolveNext(GroupType groupType, String template, int currentStep, String lastAnswer);
}
