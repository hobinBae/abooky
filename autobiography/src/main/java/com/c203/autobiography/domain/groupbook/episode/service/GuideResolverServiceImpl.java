package com.c203.autobiography.domain.groupbook.episode.service;

import com.c203.autobiography.domain.groupbook.entity.GroupBook;
import com.c203.autobiography.domain.groupbook.entity.GroupType;
import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisode;
import org.springframework.stereotype.Service;

import java.util.*;

@Service

public class GuideResolverServiceImpl implements GuideResolverService{
    private final Map<String, List<GuideQuestion>> templates = new HashMap<>();

    public GuideResolverServiceImpl() {
        // INTRO — 첫 화에서 그룹 타입 분기
        templates.put(key("INTRO", GroupType.FAMILY), List.of(
                new GuideQuestion("INTRO_FAMILY_1", "가족 구성원을 소개해주세요."),
                new GuideQuestion("INTRO_FAMILY_2", "가족의 일상이나 전통이 있다면 무엇인가요?"),
                new GuideQuestion("INTRO_FAMILY_3", "가족을 한 문장으로 표현한다면?")
        ));
        templates.put(key("INTRO", GroupType.FRIENDS), List.of(
                new GuideQuestion("INTRO_FRIENDS_1", "서로 어떻게 친해지게 되었나요?"),
                new GuideQuestion("INTRO_FRIENDS_2", "함께한 기억 중 가장 인상 깊었던 순간은 무엇이었나요?"),
                new GuideQuestion("INTRO_FRIENDS_3", "여러분의 관계를 한 문장으로 표현해보세요.")
        ));
        templates.put(key("INTRO", GroupType.COUPLE), List.of(
                new GuideQuestion("INTRO_COUPLE_1", "처음 만난 순간을 떠올려 주세요."),
                new GuideQuestion("INTRO_COUPLE_2", "서로에게 특별했던 순간은요?"),
                new GuideQuestion("INTRO_COUPLE_3", "두 분의 관계를 한 문장으로 표현한다면?")
        ));
        templates.put(key("INTRO", GroupType.TEAM), List.of(
                new GuideQuestion("INTRO_TEAM_1", "팀이 만들어진 배경과 역할을 소개해주세요."),
                new GuideQuestion("INTRO_TEAM_2", "함께 이룬 성과나 기억에 남는 협업 순간은 무엇인가요?"),
                new GuideQuestion("INTRO_TEAM_3", "팀을 한 문장으로 표현한다면?")
        ));
        templates.put(key("INTRO", GroupType.OTHER), List.of(
                new GuideQuestion("INTRO_OTHER_1", "어떤 모임인지 간단히 소개해주세요."),
                new GuideQuestion("INTRO_OTHER_2", "함께한 순간 중 기억에 남는 이야기가 있나요?"),
                new GuideQuestion("INTRO_OTHER_3", "여러분의 관계를 한 문장으로 표현해보세요.")
        ));

        // STORY — 이후 화 예시 (그룹 타입별 맞춤 질문)
        templates.put(key("STORY", GroupType.FAMILY), List.of(
                new GuideQuestion("STORY_FAMILY_1", "가족 모두가 함께했던 특별한 하루를 묘사해 주세요."),
                new GuideQuestion("STORY_FAMILY_2", "그날의 감정과 분위기를 더 자세히 들려주세요.")
        ));
        templates.put(key("STORY", GroupType.FRIENDS), List.of(
                new GuideQuestion("STORY_FRIENDS_1", "친구들과의 여행이나 모임 중 기억에 남는 장면이 있나요?"),
                new GuideQuestion("STORY_FRIENDS_2", "그때 나눈 대화나 감정을 덧붙여 주세요.")
        ));
        templates.put(key("STORY", GroupType.COUPLE), List.of(
                new GuideQuestion("STORY_COUPLE_1", "둘만의 소중한 순간 하나를 골라 자세히 들려주세요."),
                new GuideQuestion("STORY_COUPLE_2", "그 순간이 지금의 관계에 준 영향은 무엇인가요?")
        ));
        templates.put(key("STORY", GroupType.TEAM), List.of(
                new GuideQuestion("STORY_TEAM_1", "팀이 함께 해결했던 도전적인 과제를 이야기해 주세요."),
                new GuideQuestion("STORY_TEAM_2", "문제를 풀어가는 과정에서 각자의 역할은 어땠나요?")
        ));
        templates.put(key("STORY", GroupType.OTHER), List.of(
                new GuideQuestion("STORY_OTHER_1", "여러분 모임의 분위기를 보여주는 이야기를 들려주세요."),
                new GuideQuestion("STORY_OTHER_2", "그 이야기 속 핵심 장면을 한 문단으로 정리해 주세요.")
        ));

        templates.put(key("REFLECTION", GroupType.FRIENDS), List.of(
                new GuideQuestion("REFLECTION_FRIENDS_1", "친구들과 함께하면서 가장 많이 배운 것은 무엇인가요?"),
                new GuideQuestion("REFLECTION_FRIENDS_2", "서로에게 어떤 영향을 주고받았나요?"),
                new GuideQuestion("REFLECTION_FRIENDS_3", "이 우정이 각자에게 어떤 의미인가요?")
        ));
        templates.put(key("REFLECTION", GroupType.FAMILY), List.of(
                new GuideQuestion("REFLECTION_FAMILY_1", "가족으로서 함께 성장한 시간들을 돌아보며 어떤 생각이 드나요?"),
                new GuideQuestion("REFLECTION_FAMILY_2", "서로에게서 배운 가장 소중한 것은 무엇인가요?"),
                new GuideQuestion("REFLECTION_FAMILY_3", "우리 가족만의 특별함은 무엇이라고 생각하나요?")
        ));
        templates.put(key("REFLECTION", GroupType.COUPLE), List.of(
                new GuideQuestion("REFLECTION_COUPLE_1", "서로를 통해 가장 많이 성장한 부분은 무엇인가요?"),
                new GuideQuestion("REFLECTION_COUPLE_2", "힘들었던 순간들을 어떻게 함께 극복했나요?"),
                new GuideQuestion("REFLECTION_COUPLE_3", "이 관계가 각자에게 어떤 변화를 가져왔나요?")
        ));
        templates.put(key("REFLECTION", GroupType.TEAM), List.of(
                new GuideQuestion("REFLECTION_TEAM_1", "팀으로 활동하면서 가장 크게 성장한 부분은 무엇인가요?"),
                new GuideQuestion("REFLECTION_TEAM_2", "어려운 순간들을 어떻게 함께 헤쳐나갔나요?"),
                new GuideQuestion("REFLECTION_TEAM_3", "팀워크를 통해 얻은 가장 소중한 것은 무엇인가요?")
        ));

        // 🎯 FUTURE 템플릿 추가
        templates.put(key("FUTURE", GroupType.FRIENDS), List.of(
                new GuideQuestion("FUTURE_FRIENDS_1", "앞으로 함께 이루고 싶은 꿈이나 목표가 있나요?"),
                new GuideQuestion("FUTURE_FRIENDS_2", "10년 후에도 이 우정이 계속되길 바라는 이유는 무엇인가요?")
        ));
        templates.put(key("FUTURE", GroupType.FAMILY), List.of(
                new GuideQuestion("FUTURE_FAMILY_1", "우리 가족이 앞으로 함께 만들어가고 싶은 추억은 무엇인가요?"),
                new GuideQuestion("FUTURE_FAMILY_2", "가족으로서 이루고 싶은 공동의 꿈이 있다면 무엇인가요?")
        ));
        templates.put(key("FUTURE", GroupType.COUPLE), List.of(
                new GuideQuestion("FUTURE_COUPLE_1", "함께 그려가고 싶은 미래의 모습은 어떤가요?"),
                new GuideQuestion("FUTURE_COUPLE_2", "서로에게 약속하고 싶은 것이 있다면 무엇인가요?")
        ));
        templates.put(key("FUTURE", GroupType.TEAM), List.of(
                new GuideQuestion("FUTURE_TEAM_1", "팀으로서 앞으로 도전하고 싶은 새로운 목표는 무엇인가요?"),
                new GuideQuestion("FUTURE_TEAM_2", "이 팀워크를 바탕으로 이루고 싶은 더 큰 성과가 있나요?")
        ));

        // 🎯 OUTRO 템플릿 추가 (마무리)
        templates.put(key("OUTRO", GroupType.FRIENDS), List.of(
                new GuideQuestion("OUTRO_FRIENDS_1", "이 친구들에게 마지막으로 전하고 싶은 마음을 써주세요.")
        ));
        templates.put(key("OUTRO", GroupType.FAMILY), List.of(
                new GuideQuestion("OUTRO_FAMILY_1", "가족들에게 전하고 싶은 감사의 마음을 써주세요.")
        ));
        templates.put(key("OUTRO", GroupType.COUPLE), List.of(
                new GuideQuestion("OUTRO_COUPLE_1", "서로에게 전하고 싶은 마지막 사랑의 메시지를 써주세요.")
        ));
        templates.put(key("OUTRO", GroupType.TEAM), List.of(
                new GuideQuestion("OUTRO_TEAM_1", "팀원들에게 전하고 싶은 마지막 격려의 메시지를 써주세요.")
        ));

        // OTHER 그룹타입용 공통 템플릿들도 추가
        templates.put(key("REFLECTION", GroupType.OTHER), List.of(
                new GuideQuestion("REFLECTION_OTHER_1", "함께하면서 가장 많이 배운 것은 무엇인가요?"),
                new GuideQuestion("REFLECTION_OTHER_2", "이 관계가 각자에게 어떤 의미인가요?")
        ));
        templates.put(key("FUTURE", GroupType.OTHER), List.of(
                new GuideQuestion("FUTURE_OTHER_1", "앞으로 함께 이루고 싶은 목표가 있나요?")
        ));
        templates.put(key("OUTRO", GroupType.OTHER), List.of(
                new GuideQuestion("OUTRO_OTHER_1", "서로에게 전하고 싶은 마지막 메시지를 써주세요.")
        ));
    }

    @Override
    public GuideQuestion resolveFirst(GroupType groupType, String template) {
        List<GuideQuestion> list = templates.getOrDefault(key(template, groupType),
                templates.getOrDefault(key(template, GroupType.OTHER), List.of()));
        if (list.isEmpty()) {
            // 템플릿이 없을 때의 안전장치
            return new GuideQuestion("FALLBACK_1", "이 에피소드에서 가장 먼저 기록하고 싶은 이야기는 무엇인가요?");
        }
        return list.get(0);
    }

    @Override
    public String solveFirst(GroupBook gb, String template) {
        GuideQuestion firstQuestion = resolveFirst(gb.getGroupType(), template);
        return firstQuestion.question();
    }

    @Override
    public GuideQuestion questionForStep(GroupEpisode ep, int stepNo) {
        // EpisodeGuideState에서 해당 step의 질문을 찾는 로직
        // 현재는 간단히 구현
        return new GuideQuestion("STEP_" + stepNo, "Step " + stepNo + " 질문");
    }

    @Override
    public Optional<GuideQuestion> resolveNext(GroupType groupType, String template, int currentStep, String lastAnswer) {
        // 분기 규칙(간단 예시): INTRO의 첫 답변에 특정 키워드가 있으면 다음 질문을 바꿀 수 있다
        // 현재는 템플릿 배열에서 currentStep(1-based)의 다음 인덱스를 반환
        List<GuideQuestion> list = templates.getOrDefault(key(template, groupType),
                templates.getOrDefault(key(template, GroupType.OTHER), List.of()));
        int nextIndex = currentStep; // currentStep 이 1이면 다음은 index=1(두 번째 질문)
        if (nextIndex < 0) nextIndex = 0;
        if (nextIndex >= list.size()) return Optional.empty();
        return Optional.of(list.get(nextIndex));
    }

    private String key(String template, GroupType type) {
        return template.toUpperCase(Locale.ROOT) + ":" + type.name();
    }
}
