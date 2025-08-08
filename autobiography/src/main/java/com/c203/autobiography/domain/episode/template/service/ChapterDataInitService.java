package com.c203.autobiography.domain.episode.template.service;

import com.c203.autobiography.domain.episode.template.dto.FollowUpType;
import com.c203.autobiography.domain.episode.template.entity.*;
import com.c203.autobiography.domain.episode.template.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChapterDataInitService {

    private final ChapterRepository chapterRepo;
    private final ChapterTemplateRepository templateRepo;
    private final FollowUpQuestionRepository followUpRepo;

    /**
     * 사용자가 제안한 구조를 기반으로 챕터와 템플릿 데이터 초기화
     */
    public void initializeChapterData() {
        log.info("챕터 기반 질문 데이터 초기화 시작");

        // 기존 데이터 정리 - 고품질 템플릿으로 교체
        clearExistingData();

        int lv = 1;

        lv = createChapter1(lv); // 기본 정보
        lv = createChapter2(lv); // 성장 과정
        lv = createChapter3(lv); // 사회 활동
        lv = createChapter4(lv); // 개인 생활
        lv = createChapter5(lv); // 성찰 및 유산
        lv = createChapter6(lv);
        lv = createChapter7(lv);


        log.info("고품질 챕터 기반 질문 데이터 초기화 완료");
    }
    private int createChapter1(int lv) {
        Chapter chapter = Chapter.builder()
                .chapterId("chapter1")
                .chapterName("시작/기본정보")
                .chapterOrder(1)
                .description("현재 자기소개, 거주지, 역할, 기록 목적 파악")
                .build();
        chapterRepo.save(chapter);

        ChapterTemplate intro = ChapterTemplate.builder()
                .templateId("intro_profile")
                .stageName("나는 누구인가")
                .mainQuestion("지금의 당신을 한 문장으로 소개한다면요? 이름(또는 호칭), 현재 하고 있는 일, 자신을 설명하는 표현을 포함해 자유롭게 소개해주세요.")
                .templateOrder(1)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                // 섹션 키워드를 앞에 붙여 저장해 둠
                .dynamicPromptTemplate("INTRO")
                .chapter(chapter)
                .build();
        templateRepo.save(intro);

        ChapterTemplate introStatic = ChapterTemplate.builder()
                .templateId("intro_static")
                .stageName("생활 맥락")
                .mainQuestion("요즘의 생활을 가장 잘 보여주는 장면을 하나만 꼽는다면 언제인가요?")
                .templateOrder(2)
                .stageLevel(lv++)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(introStatic);

        saveStaticFollowUps(introStatic, Arrays.asList(
                "현재 거주하는 곳과 그곳을 선택한 이유가 있나요?",
                "이번 기록에서 꼭 남기고 싶은 주제나 메시지가 있나요?",
                "하루 일과 중 자신을 가장 잘 드러내는 순간은 언제인가요?"
        ));

        return lv;
    }

    private int createChapter2(int lv) {
        Chapter chapter = Chapter.builder()
                .chapterId("chapter2")
                .chapterName("유년기")
                .chapterOrder(2)
                .description("출생 배경, 가족 분위기, 초기 성격/기질")
                .build();
        chapterRepo.save(chapter);

        ChapterTemplate birthFamily = ChapterTemplate.builder()
                .templateId("childhood_family")
                .stageName("뿌리와 시작")
                .mainQuestion("당신이 태어나고 자란 곳, 그리고 가족에 대해 이야기해주세요. 어떤 분위기의 집에서 자라셨나요?")
                .templateOrder(1)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("CHILDHOOD")
                .chapter(chapter)
                .build();
        templateRepo.save(birthFamily);

        ChapterTemplate childhoodStatic = ChapterTemplate.builder()
                .templateId("childhood_static")
                .stageName("어린 시절의 기억들")
                .mainQuestion("지금도 선명한 어린 시절의 장면이 있다면 하나만 묘사해 주세요.")
                .templateOrder(2)
                .stageLevel(lv++)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(childhoodStatic);

        saveStaticFollowUps(childhoodStatic, Arrays.asList(
                "어린 시절에 자주 듣던 말이나 가훈이 있었나요?",
                "가족 중 당신에게 가장 큰 영향을 준 사람은 누구이며, 이유는 무엇인가요?",
                "그 장면에서 떠오르는 냄새, 소리, 색감 같은 디테일이 있다면요?"
        ));

        return lv;
    }

    private int createChapter3(int lv) {
        Chapter chapter = Chapter.builder()
                .chapterId("chapter3")
                .chapterName("아동기 후반~초등 고학년")
                .chapterOrder(3)
                .description("학교 적응, 또래 관계, 취미/관심사의 발아, 첫 성취/실패")
                .build();
        chapterRepo.save(chapter);

        ChapterTemplate elem = ChapterTemplate.builder()
                .templateId("upper_elem_activity")
                .stageName("몰입의 시작")
                .mainQuestion("초등 시절, 당신을 설레게 하거나 몰입하게 만든 활동이 있었나요? 그때의 감정과 계기를 들려주세요.")
                .templateOrder(1)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("UPPER_ELEM")
                .chapter(chapter)
                .build();
        templateRepo.save(elem);

        ChapterTemplate elemStatic = ChapterTemplate.builder()
                .templateId("upper_elem_static")
                .stageName("관계와 성향")
                .mainQuestion("그 시기에 영향을 준 사람이나 사건이 있었나요?")
                .templateOrder(2)
                .stageLevel(lv++)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(elemStatic);

        saveStaticFollowUps(elemStatic, Arrays.asList(
                "기억에 남는 선생님이나 친구와의 에피소드가 있나요?",
                "처음으로 ‘잘한다/어렵다’고 느낀 분야는 무엇이었나요?",
                "그 경험이 지금의 성향이나 선택에 이어진 점이 있다면 무엇인가요?"
        ));

        return lv;
    }

    private int createChapter4(int lv) {
        Chapter chapter = Chapter.builder()
                .chapterId("chapter4")
                .chapterName("중학교")
                .chapterOrder(4)
                .description("정체성 탐색의 시작, 또래/집단의 영향, 자기인식 변화")
                .build();
        chapterRepo.save(chapter);

        ChapterTemplate middle = ChapterTemplate.builder()
                .templateId("middle_identity")
                .stageName("변곡점")
                .mainQuestion("중학교 시절, ‘나’를 바라보는 시각이 달라지게 만든 사건이나 관계가 있었나요?")
                .templateOrder(1)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("MIDDLE")
                .chapter(chapter)
                .build();
        templateRepo.save(middle);

        ChapterTemplate middleStatic = ChapterTemplate.builder()
                .templateId("middle_static")
                .stageName("취향과 관계")
                .mainQuestion("이 시기에 즐겨 듣던 음악/콘텐츠/취미가 있었다면, 그것이 주는 감정은 무엇이었나요?")
                .templateOrder(2)
                .stageLevel(lv++)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(middleStatic);

        saveStaticFollowUps(middleStatic, Arrays.asList(
                "갈등이나 다툼을 겪었다면, 그 경험이 당신의 대화 방식이나 관계 맺음에 어떤 흔적을 남겼나요?",
                "스스로 규칙을 세우거나 바꿨던 순간이 있나요? 왜였나요?",
                "그 시기의 나에게 한마디를 건넨다면?"
        ));
        return lv;
    }

    private int createChapter5(int lv) {
        Chapter chapter = Chapter.builder()
                .chapterId("chapter5")
                .chapterName("고등학교")
                .chapterOrder(5)
                .description("학업/진로 압력, 의미 있는 목표/실패, 규율 형성")
                .build();
        chapterRepo.save(chapter);

        ChapterTemplate high = ChapterTemplate.builder()
                .templateId("high_commitment")
                .stageName("치열함과 배움")
                .mainQuestion("고등학교 시절 가장 치열하게 몰입했던 일은 무엇이었고, 그 과정에서 무엇을 배우셨나요?")
                .templateOrder(1)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("HIGH")
                .chapter(chapter)
                .build();
        templateRepo.save(high);

        ChapterTemplate highStatic = ChapterTemplate.builder()
                .templateId("high_static")
                .stageName("기준과 영향")
                .mainQuestion("당신의 선택에 영향을 준 사람이나 말이 있었나요?")
                .templateOrder(2)
                .stageLevel(lv++)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(highStatic);

        saveStaticFollowUps(highStatic, Arrays.asList(
                "기대와 현실 사이에서 가장 힘들었던 순간은 언제였나요? 어떻게 버텼나요?",
                "성공/실패의 기준이 이때 어떻게 정의되었나요?",
                "그 경험이 남긴 태도나 습관이 있다면요?"
        ));
        return lv;
    }

    private int createChapter6(int lv) {
        Chapter chapter = Chapter.builder()
                .chapterId("chapter6")
                .chapterName("졸업 전환기")
                .chapterOrder(6)
                .description("선택의 갈림길, 현실 요소, 자율성 확대")
                .build();
        chapterRepo.save(chapter);

        ChapterTemplate trans = ChapterTemplate.builder()
                .templateId("transition_choice")
                .stageName("갈림길의 선택")
                .mainQuestion("졸업 전후의 중요한 선택(진학, 취업, 휴식, 재도전 등)을 하게 된 배경과 마음의 흐름을 이야기해주세요.")
                .templateOrder(1)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("TRANSITION")
                .chapter(chapter)
                .build();
        templateRepo.save(trans);

        ChapterTemplate transStatic = ChapterTemplate.builder()
                .templateId("transition_static")
                .stageName("균형과 재해석")
                .mainQuestion("주변의 기대와 당신의 내적 목소리 사이에서 어떤 균형을 택했나요?")
                .templateOrder(2)
                .stageLevel(lv++)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(transStatic);

        saveStaticFollowUps(transStatic, Arrays.asList(
                "결과가 주는 감정은 시간이 지나며 어떻게 달라졌나요?",
                "같은 선택을 다시 해야 한다면 무엇을 다르게 하실 건가요?",
                "그 선택이 남긴 배움은 무엇인가요?"
        ));
        return lv;
    }

    private int createChapter7(int lv) {
        Chapter chapter = Chapter.builder()
                .chapterId("chapter7")
                .chapterName("대학 초반/사회 진입 초기")
                .chapterOrder(7)
                .description("새 환경 적응, 자율학습, 첫 성취/좌절, 관계망 확장")
                .build();
        chapterRepo.save(chapter);

        ChapterTemplate start = ChapterTemplate.builder()
                .templateId("college_or_work_start")
                .stageName("새로운 시작")
                .mainQuestion("대학 생활(또는 첫 직장/병역/대안 경로)의 초반에 가장 인상 깊었던 사건은 무엇이었나요? 그 경험이 당신을 어떻게 바꾸었나요?")
                .templateOrder(1)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("COLLEGE_OR_WORK")
                .chapter(chapter)
                .build();
        templateRepo.save(start);

        ChapterTemplate startStatic = ChapterTemplate.builder()
                .templateId("college_or_work_static")
                .stageName("자유와 책임")
                .mainQuestion("처음 맞닥뜨린 자유나 책임 중 가장 큰 것은 무엇이었나요?")
                .templateOrder(2)
                .stageLevel(lv++)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(startStatic);

        saveStaticFollowUps(startStatic, Arrays.asList(
                "기대와 다른 현실을 마주했을 때 어떤 선택을 했나요?",
                "이 시기에 만난 사람들 중 지금도 남은 영향이 있다면 무엇인가요?",
                "그 경험이 가져온 자신감 또는 관점의 변화가 있었나요?"
        ));
        return lv;
    }

//    private int createChapter1(int lv) {
//        Chapter chapter = Chapter.builder()
//                .chapterId("chapter1")
//                .chapterName("기본 정보")
//                .chapterOrder(1)
//                .description("사용자의 기본적인 정보와 배경을 파악하는 단계")
//                .build();
//        chapterRepo.save(chapter);
//
//        // 사용자 프로필 템플릿 - 고품질 버전
//        ChapterTemplate userProfile = ChapterTemplate.builder()
//                .templateId("userProfile")
//                .stageName("나는 누구인가")
//                .mainQuestion("지금의 당신을 한 문장으로 표현한다면 어떻게 말하고 싶으신가요? 이름과 나이, 현재 하고 계신 일을 포함해서 자유롭게 소개해주세요.")
//                .templateOrder(1)
//                .stageLevel(lv++)
//                .followUpType(FollowUpType.DYNAMIC)
//                .dynamicPromptTemplate("사용자가 자신을 이렇게 소개했습니다: \"%s\". 이 소개에서 더 깊이 알고 싶은 부분이나 흥미로운 점에 대해 2-3개의 후속 질문을 만들어주세요. " +
//                        "중요 주의사항: " +
//                        "1. 한국어 이름을 정확히 인식하세요" +
//                        "2. 사용자가 실제로 언급한 내용만 바탕으로 질문하세요 " +
//                        "3. 추측이나 가정된 정보로 질문하지 마세요 " +
//                        "4. 감정이나 개인적 의미를 탐구하는 방향으로 질문해주세요")
//                .chapter(chapter)
//                .build();
//        templateRepo.save(userProfile);
//
//        // 출생 및 가족 배경 템플릿 - 감정 중심 개편
//        ChapterTemplate birthFamily = ChapterTemplate.builder()
//                .templateId("birthFamily")
//                .stageName("뿌리와 시작")
//                .mainQuestion("당신이 태어나고 자란 곳, 그리고 가족에 대해 이야기해주세요. 어떤 분위기의 집에서 자라셨나요?")
//                .templateOrder(2)
//                .stageLevel(lv++)
//                .followUpType(FollowUpType.STATIC)
//                .chapter(chapter)
//                .build();
//        templateRepo.save(birthFamily);
//
//        saveStaticFollowUps(birthFamily, Arrays.asList(
//                "가족 중에서 가장 기억에 남는 사람은 누구이고, 그 이유는 무엇인가요?",
//                "어린 시절 집에서 일어났던 일 중 지금도 생생하게 기억나는 순간이 있다면 들려주세요.",
//                "가족들과 함께했던 시간 중 가장 행복했던 기억은 무엇인가요?"
//        ));
//        return lv;
//    }
//
//    private int createChapter2(int lv) {
//        Chapter chapter = Chapter.builder()
//                .chapterId("chapter2")
//                .chapterName("성장 과정")
//                .chapterOrder(2)
//                .description("유년기부터 성인이 되기까지의 성장 경험")
//                .build();
//        chapterRepo.save(chapter);
//
//        // 유년기 이야기 - 감정 깊이 강화
//        ChapterTemplate childhood = ChapterTemplate.builder()
//                .templateId("childhood")
//                .stageName("어린 시절의 기억들")
//                .mainQuestion("어린 시절, 당신을 가장 두근거리게 했던 순간이나 깊이 상처받았던 기억이 있나요? 그때의 감정과 함께 이야기해주세요.")
//                .templateOrder(1)
//                .stageLevel(lv++)
//                .followUpType(FollowUpType.DYNAMIC)
//                .dynamicPromptTemplate("사용자가 어린 시절의 중요한 기억을 이렇게 공유했습니다: \"%s\". 이 경험이 사용자에게 어떤 영향을 미쳤는지, 그리고 그때의 감정이나 교훈에 대해 더 깊이 탐구할 수 있는 2-3개의 후속 질문을 만들어주세요. " +
//                        "사용자가 실제로 언급한 내용만을 바탕으로 하고, 추측하지 마세요. 감정적 의미와 성장에 초점을 맞춰주세요.")
//                .chapter(chapter)
//                .build();
//        templateRepo.save(childhood);
//
//        // 학창 시절 - 성장과 관계 중심
//        ChapterTemplate school = ChapterTemplate.builder()
//                .templateId("school")
//                .stageName("청춘의 흔적들")
//                .mainQuestion("학창시절 중 가장 치열했던 순간이나 소중한 만남에 대해 들려주세요. 친구, 선생님, 또는 특별한 경험 중 지금의 당신을 만든 것이 있다면요.")
//                .templateOrder(2)
//                .stageLevel(lv++)
//                .followUpType(FollowUpType.STATIC)
//                .chapter(chapter)
//                .build();
//        templateRepo.save(school);
//
//        saveStaticFollowUps(school, Arrays.asList(
//                "그 시절, 당신을 가장 힘들게 했던 것은 무엇이었고 어떻게 이겨냈나요?",
//                "학창시절의 꿈과 현재의 모습을 비교해보면 어떤 생각이 드시나요?",
//                "그때의 경험이 지금의 당신에게 어떤 힘이 되고 있나요?"
//        ));
//
//        // 교육 경험 (Static)
//        ChapterTemplate education = ChapterTemplate.builder()
//                .templateId("education")
//                .stageName("학교 및 교육 경험")
//                .mainQuestion("대학이나 직업 교육 과정 중 가장 인상 깊었던 수업이나 활동은 무엇인가요?")
//                .templateOrder(3)
//                .stageLevel(lv++)
//                .followUpType(FollowUpType.STATIC)
//                .chapter(chapter)
//                .build();
//        templateRepo.save(education);
//
//        saveStaticFollowUps(education, Arrays.asList(
//                "그 활동이 당신에게 어떤 영감을 주었나요?",
//                "어떤 도전이 있었고, 어떻게 극복했나요?",
//                "당신의 진로 선택에 이 경험이 어떤 영향을 미쳤나요?"
//        ));
//        return lv;
//    }
//
//    private int createChapter3(int lv) {
//        Chapter chapter = Chapter.builder()
//                .chapterId("chapter3")
//                .chapterName("사회 활동")
//                .chapterOrder(3)
//                .description("사회 진출과 커리어 발전 과정")
//                .build();
//        chapterRepo.save(chapter);
//
//        // 커리어 (Dynamic)
//        ChapterTemplate career = ChapterTemplate.builder()
//                .templateId("career")
//                .stageName("사회 진출 및 커리어")
//                .mainQuestion("대학 졸업 후 또는 첫 직장 생활 중 가장 의미 있던 순간은 언제였나요?")
//                .templateOrder(1)
//                .stageLevel(lv++)
//                .followUpType(FollowUpType.DYNAMIC)
//                .dynamicPromptTemplate("현재 '사회 진출' 단계입니다. 사용자가 답변한 내용: \"%s\". 이 답변에 기반해 2개의 후속 질문을 생성해주세요. 사용자가 실제로 언급한 내용만을 바탕으로 하고, 추측이나 가정하지 마세요.")
//                .chapter(chapter)
//                .build();
//        templateRepo.save(career);
//
//        // 업적 및 실패 (Static)
//        ChapterTemplate achievements = ChapterTemplate.builder()
//                .templateId("achievements")
//                .stageName("업적 및 실패 경험")
//                .mainQuestion("지금까지 인생에서 가장 큰 성취나 실패 경험은 무엇이며, 그 이유는 무엇인가요?")
//                .templateOrder(2)
//                .stageLevel(lv++)
//                .followUpType(FollowUpType.STATIC)
//                .chapter(chapter)
//                .build();
//        templateRepo.save(achievements);
//
//        saveStaticFollowUps(achievements, Arrays.asList(
//                "성취(또는 실패) 과정에서 겪은 어려움은 무엇이었나요?",
//                "그 경험을 통해 얻은 교훈은 무엇인가요?",
//                "이 경험이 당신의 가치관에 어떤 영향을 미쳤나요?"
//        ));
//        return lv;
//    }
//
//    private int createChapter4(int lv) {
//        Chapter chapter = Chapter.builder()
//                .chapterId("chapter4")
//                .chapterName("개인 생활")
//                .chapterOrder(4)
//                .description("개인적인 관계와 취미, 건강 등 일상생활")
//                .build();
//        chapterRepo.save(chapter);
//
//        // 인간관계 - 사랑과 상처 중심
//        ChapterTemplate relationships = ChapterTemplate.builder()
//                .templateId("relationships")
//                .stageName("마음을 나눈 사람들")
//                .mainQuestion("당신의 인생에서 가장 깊이 사랑했던 사람이나 가장 큰 위로가 되었던 사람에 대해 이야기해주세요. 그 사람과의 만남이 당신에게 어떤 의미였나요?")
//                .templateOrder(1)
//                .stageLevel(lv++)
//                .followUpType(FollowUpType.STATIC)
//                .chapter(chapter)
//                .build();
//        templateRepo.save(relationships);
//
//        saveStaticFollowUps(relationships, Arrays.asList(
//                "그 사람과 함께 보낸 시간 중 가장 소중한 순간을 구체적으로 묘사해주세요.",
//                "헤어짐이나 이별의 아픔을 겪어본 적이 있다면, 그 경험이 당신을 어떻게 변화시켰나요?",
//                "사랑이나 우정을 통해 깨달은 인생의 진리가 있다면 무엇인가요?"
//        ));
//
//        // 여행 (Dynamic)
//        ChapterTemplate travel = ChapterTemplate.builder()
//                .templateId("travel")
//                .stageName("여행 및 모험")
//                .mainQuestion("가장 기억에 남는 여행지는 어디이며, 그 이유는 무엇인가요?")
//                .templateOrder(2)
//                .stageLevel(lv++)
//                .followUpType(FollowUpType.DYNAMIC)
//                .dynamicPromptTemplate("지금 '여행 및 모험' 단계입니다. 사용자가 답변한 내용: \"%s\". 사용자가 실제로 언급한 여행 경험만을 바탕으로 해당 여행을 깊이 탐구하기 위한 후속 질문 2개를 생성해주세요. 추측하지 마세요.")
//                .chapter(chapter)
//                .build();
//        templateRepo.save(travel);
//
//        // 취미 (Static)
//        ChapterTemplate hobbies = ChapterTemplate.builder()
//                .templateId("hobbies")
//                .stageName("취미 및 관심사")
//                .mainQuestion("당신이 오랫동안 즐겨온 취미나 활동은 무엇이며, 어떻게 시작하게 되었나요?")
//                .templateOrder(3)
//                .stageLevel(lv++)
//                .followUpType(FollowUpType.STATIC)
//                .chapter(chapter)
//                .build();
//        templateRepo.save(hobbies);
//
//        saveStaticFollowUps(hobbies, Arrays.asList(
//                "이 취미를 통해 얻은 즐거움은 무엇인가요?",
//                "취미 활동 중 기억에 남는 에피소드는 무엇인가요?",
//                "이 경험이 당신의 삶에 어떤 변화를 주었나요?"
//        ));
//
//        // 건강 (Static)
//        ChapterTemplate health = ChapterTemplate.builder()
//                .templateId("health")
//                .stageName("건강 및 웰빙")
//                .mainQuestion("건강이나 웰빙을 위해 특별히 노력한 경험이 있나요? 그 이유는 무엇인가요?")
//                .templateOrder(4)
//                .stageLevel(lv++
//                )
//                .followUpType(FollowUpType.STATIC)
//                .chapter(chapter)
//                .build();
//        templateRepo.save(health);
//
//        saveStaticFollowUps(health, Arrays.asList(
//                "그 노력이 삶에 어떤 긍정적 변화를 가져왔나요?",
//                "어려웠던 점은 무엇이며, 어떻게 극복했나요?",
//                "앞으로 건강을 위해 계획하고 있는 것은 무엇인가요?"
//        ));
//        return lv;
//    }
//
//    private int createChapter5(int lv) {
//        Chapter chapter = Chapter.builder()
//                .chapterId("chapter5")
//                .chapterName("성찰 및 유산")
//                .chapterOrder(5)
//                .description("인생을 돌아보고 미래에 대한 비전을 정리하는 단계")
//                .build();
//        chapterRepo.save(chapter);
//
//        // 성찰 - 인생의 의미 탐구
//        ChapterTemplate reflection = ChapterTemplate.builder()
//                .templateId("reflection")
//                .stageName("인생을 돌아보며")
//                .mainQuestion("지금까지 살아오면서 가장 후회되는 일과 가장 감사한 일이 무엇인지, 그리고 만약 과거로 돌아갈 수 있다면 무엇을 다르게 하고 싶은지 솔직하게 이야기해주세요.")
//                .templateOrder(1)
//                .stageLevel(lv++)
//                .followUpType(FollowUpType.DYNAMIC)
//                .dynamicPromptTemplate("사용자가 인생에 대한 깊은 성찰을 이렇게 나누었습니다: \"%s\". 사용자가 실제로 언급한 내용만을 바탕으로 이 답변에서 더 깊이 탐구할 수 있는 철학적이고 의미있는 후속 질문 2개를 만들어주세요. 추측이나 가정하지 말고, 인생의 의미, 가치, 성장에 대한 통찰을 이끌어낼 수 있도록 해주세요.")
//                .chapter(chapter)
//                .build();
//        templateRepo.save(reflection);
//
//        // 유산 - 삶의 메시지
//        ChapterTemplate legacy = ChapterTemplate.builder()
//                .templateId("legacy")
//                .stageName("남기고 싶은 이야기")
//                .mainQuestion("당신의 인생이 한 권의 책이라면, 마지막 페이지에 어떤 메시지를 남기고 싶으신가요? 당신을 기억할 사람들에게 전하고 싶은 진심어린 말이 있다면요.")
//                .templateOrder(2)
//                .stageLevel(lv++)
//                .followUpType(FollowUpType.STATIC)
//                .chapter(chapter)
//                .build();
//        templateRepo.save(legacy);
//
//        saveStaticFollowUps(legacy, Arrays.asList(
//                "당신이 가장 소중하게 여기는 가치나 신념은 무엇이며, 이것을 어떻게 실천해왔나요?",
//                "힘든 시기를 겪고 있는 누군가에게 해주고 싶은 조언이나 격려의 말이 있나요?",
//                "당신의 삶을 통해 증명하고 싶었던 것은 무엇이고, 그것을 이루었다고 생각하시나요?"
//        ));
//        return lv;
//    }

    private void saveStaticFollowUps(ChapterTemplate template, List<String> questions) {
        for (int i = 0; i < questions.size(); i++) {
            FollowUpQuestion followUp = FollowUpQuestion.builder()
                    .questionText(questions.get(i))
                    .questionOrder(i + 1)
                    .chapterTemplate(template)
                    .build();
            followUpRepo.save(followUp);
        }
    }

    private void clearExistingData() {
        followUpRepo.deleteAll();
        templateRepo.deleteAll();
        chapterRepo.deleteAll();
    }
} 