package com.c203.autobiography.domain.groupbook.episode.service;

import com.c203.autobiography.domain.groupbook.entity.GroupBook;
import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisode;

public interface GuideResolver {
    String firstQuestion(GroupBook gb, String template);
    GuideQuestion questionForStep(GroupEpisode ep, int stepNo);
    GuideQuestion nextQuestion(GroupEpisode ep, int stepNo);

}
