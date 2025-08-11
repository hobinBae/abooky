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

        // 딱딱한 정보 질문 전에, 부드러운 감정 질문으로 대화의 문을 엽니다.
        ChapterTemplate currentFeeling = ChapterTemplate.builder()
                .templateId("prologue_current_feeling") // 신규
                .stageName("오늘의 나를 비추는 창")
                .mainQuestion("당신의 자서전, 그 첫 페이지를 함께 열게 되어 기쁩니다. 본격적인 이야기에 앞서, 요즘 당신의 하루를 채우고 있는 가장 익숙한 감정이나 생각은 무엇인가요?")
                .templateOrder(1)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("INTRO")
                .chapter(chapter)
                .build();
        templateRepo.save(currentFeeling);

        // 기존의 길었던 자기소개 질문을 '역할'과 '출생'으로 명확히 나눕니다.
        ChapterTemplate nameAndRole = ChapterTemplate.builder()
                .templateId("prologue_name_and_role") // 신규 (intro_profile 분리)
                .stageName("나의 이름과 역할")
                .mainQuestion("이야기 속에서 당신을 어떻게 부르면 좋을까요? 그리고 현재 당신의 삶에서 스스로 가장 중요하다고 생각하는 역할이 있다면 무엇인지 들려주세요.")
                .templateOrder(2)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("INTRO")
                .chapter(chapter)
                .build();
        templateRepo.save(nameAndRole);

        ChapterTemplate birthInfo = ChapterTemplate.builder()
                .templateId("prologue_birth_info") // 신규 (intro_profile 분리)
                .stageName("나의 시작점")
                .mainQuestion("당신은 어떤 계절에, 어디에서 태어나셨나요? 태어난 곳에 대한 어렴풋한 기억이나, 부모님께 전해 들은 이야기가 있다면 궁금합니다.")
                .templateOrder(3)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("INTRO")
                .chapter(chapter)
                .build();
        templateRepo.save(birthInfo);

        // 기록의 동기를 묻는 질문을 구체화하고 Static 후속 질문으로 방향을 명확히 합니다.
        ChapterTemplate motivation = ChapterTemplate.builder()
                .templateId("prologue_motivation_static") // 기존 intro_static 대체
                .stageName("기록의 이유")
                .mainQuestion("이번 자서전을 기록하기로 마음먹은 특별한 계기가 있으신가요?")
                .templateOrder(4)
                .stageLevel(lv++)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(motivation);

        saveStaticFollowUps(motivation, Arrays.asList(
                "이 기록을 통해 가장 보여주고 싶은 자신의 모습은 무엇인가요?",
                "이 기록을 가장 읽어주었으면 하는 사람이 있나요? (가족, 친구, 혹은 미래의 나 자신 등)",
                "반대로, 이번 이야기에서는 잠시 덮어두고 싶은 주제가 있으신가요?"
        ));

        ChapterTemplate expectation = ChapterTemplate.builder()
                .templateId("prologue_expectation") // 신규
                .stageName("기록의 여정")
                .mainQuestion("이 자서전을 써 내려가는 과정 자체에 기대하는 것이 있으신가요? 예를 들어, 잊었던 나를 발견하는 즐거움이나, 누군가에게 나의 이야기를 들려주는 보람 같은 것들이요.")
                .templateOrder(5) // 순서 조정
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("INTRO") // 기존 프롬프트 활용 가능
                .chapter(chapter)
                .build();
        templateRepo.save(expectation);

        // 프롤로그의 마지막을 장식하는 강력한 '첫 장면'으로, 이야기의 시작을 알립니다.
        ChapterTemplate openingScene = ChapterTemplate.builder()
                .templateId("prologue_opening_scene") // 기존 intro_identity_scene 대체
                .stageName("이야기의 첫 장면")
                .mainQuestion("자, 이제 당신의 이야기를 시작해볼까요? 지금의 당신을 가장 잘 보여주는 '하루의 한 장면'을 생생하게 묘사해주시면서, 이야기의 문을 열어주세요.")
                .templateOrder(6)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("INTRO_SCENE")
                .chapter(chapter)
                .build();
        templateRepo.save(openingScene);


        return lv;
    }

    private int createChapter2(int lv) {
        Chapter chapter = Chapter.builder()
                .chapterId("chapter2")
                .chapterName("유년기")
                .chapterOrder(2)
                .description("출생 배경, 가족 분위기, 초기 성격/기질을 구체 장면과 감정으로 수집")
                .build();
        chapterRepo.save(chapter);

        // 집/동네/일상 환경 — 공간/리듬/규칙을 파악
        ChapterTemplate homeEnv = ChapterTemplate.builder()
                .templateId("childhood_home_env")
                .stageName("기억의 풍경: 집과 동네")
                .mainQuestion(
                        "가장 먼저, 어린 시절 당신이 살던 집과 동네의 모습을 떠올려볼까요? " +
                                "집 안에서 들리던 소리, 창문으로 들어오던 빛, 특별한 냄새 같은 기억이 있다면 들려주세요."
                )
                .templateOrder(1)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("CHILDHOOD_HOME")
                .chapter(chapter)
                .build();
        templateRepo.save(homeEnv);

        ChapterTemplate caregivers = ChapterTemplate.builder()
                .templateId("childhood_caregivers")
                .stageName("풍경 속 사람들")
                .mainQuestion(
                        "그 풍경 속에는 주로 누가 함께 있었나요? 당시 당신을 돌봐주셨던 분들의 말투, 자주 하던 말, 기억나는 손길이나 습관이 있다면 이야기해주세요."
                )
                .templateOrder(2)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("CHILDHOOD_CAREGIVER")
                .chapter(chapter)
                .build();
        templateRepo.save(caregivers);

        ChapterTemplate personality = ChapterTemplate.builder()
                .templateId("childhood_personality") // 신규
                .stageName("어른들 눈에 비친 아이")
                .mainQuestion("그 시절, 주변 어른들은 당신을 보통 어떤 아이라고 이야기하셨나요? 혹시 스스로 생각하는 '어린 시절의 나'는 다른 모습이었는지도 궁금합니다. (예: 겉으로는 조용했지만 속으로는 호기심이 많았다 등)")
                .templateOrder(3)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("CHILDHOOD_PERSONALITY") // 프롬프트 키 신규
                .chapter(chapter)
                .build();
        templateRepo.save(personality);


        // '구체적인 놀이와 일상'을 물어 기억을 쉽게 이끌어냅니다.
        ChapterTemplate playtime = ChapterTemplate.builder()
                .templateId("childhood_playtime") // dailyLife를 세분화
                .stageName("하루의 조각: 놀이와 장난감")
                .mainQuestion("혼자 있거나, 혹은 형제나 친구와 함께일 때, 주로 무엇을 하고 놀았나요? 가장 아끼던 장난감이나, 닳도록 읽었던 동화책이 있었나요?")
                .templateOrder(4)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("CHILDHOOD_PLAY")
                .chapter(chapter)
                .build();
        templateRepo.save(playtime);

        ChapterTemplate favorites = ChapterTemplate.builder()
                .templateId("childhood_favorites") // 신규
                .stageName("하루의 조각: 나의 우상")
                .mainQuestion("그 시절 당신의 마음을 사로잡았던 '최애' 캐릭터가 있었나요? 만화, 영화, 게임 속 주인공이나, 좋아했던 연예인도 좋습니다. 왜 그 캐릭터를 특별히 좋아했는지 기억나시나요?")
                .templateOrder(5)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("CHILDHOOD_FAVORITES") // 프롬프트 키 신규
                .chapter(chapter)
                .build();
        templateRepo.save(favorites);


        // 생생한 한 장면 — 감각·감정의 첫 기억(장면 집중)
        ChapterTemplate vividScene = ChapterTemplate.builder()
                .templateId("childhood_vivid_scene")
                .stageName("선명한 한 장면")
                .mainQuestion(
                        "이번에는 유년기 기억 중 가장 선명하게 남아있는 '한 장면'을 들려주세요. " +
                                "아주 기뻤던 순간도, 슬펐던 순간도 좋습니다. 그때 어디서 무엇을 하고 있었고, 왜 그 장면이 특별하게 기억에 남았나요?"
                )
                .templateOrder(6)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("CHILDHOOD_SCENE")
                .chapter(chapter)
                .build();
        templateRepo.save(vividScene);

        // 2-4) 정리(Static) — 가치/기질/지금의 나와의 연결 이건 좀 수정해야함
        ChapterTemplate wrapUp = ChapterTemplate.builder()
                .templateId("childhood_wrapup_static")
                .stageName("씨앗과 연결")
                .mainQuestion(
                        "유년기의 경험들이 모여 지금의 당신이 되었을 겁니다. 어린 시절을 되돌아볼 때, " +
                                "현재 당신의 모습에 가장 큰 영향을 주었다고 생각되는 점은 무엇인가요?"
                )
                .templateOrder(7)
                .stageLevel(lv++)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(wrapUp);

        saveStaticFollowUps(wrapUp, Arrays.asList(
                "그 시절, 당신은 스스로를 어떤 아이라고 생각했나요? (예: 활발한, 조용한, 호기심 많은 등)",
                "어린 시절의 경험 중, 어른이 된 지금도 당신을 웃게 하거나 혹은 마음 아프게 하는 것이 있다면 무엇인가요?",
                "만약 타임머신을 타고 그때의 나에게 돌아가 딱 한 가지를 선물할 수 있다면, 무엇을 주고 싶으신가요? (물건이 아니어도 좋습니다)"
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

        ChapterTemplate schoolScenery = ChapterTemplate.builder()
                .templateId("upper_elem_school_scenery") // ID 세분화
                .stageName("집 밖의 세계: 학교의 풍경")
                .mainQuestion("초등학교 시절, 매일같이 오가던 학교의 모습은 어땠나요? 교실 창밖으로 보이던 풍경이나, 유난히 시끄러웠던 쉬는 시간의 교실 모습이 기억나시나요?")
                .templateOrder(1)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("UPPER_ELEM_SCHOOL")
                .chapter(chapter)
                .build();
        templateRepo.save(schoolScenery);

        ChapterTemplate schoolSenses = ChapterTemplate.builder()
                .templateId("upper_elem_school_senses") // ID 세분화
                .stageName("집 밖의 세계: 학교의 감각")
                .mainQuestion("이번에는 학교에서만 느낄 수 있었던 특별한 냄새나 소리를 떠올려볼까요? 예를 들어, 낡은 목재 복도를 걷는 소리나, 점심시간 급식실의 냄새 같은 것들이요.")
                .templateOrder(2)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("UPPER_ELEM_SCHOOL") // 같은 프롬프트 키 사용 가능
                .chapter(chapter)
                .build();
        templateRepo.save(schoolSenses);

        ChapterTemplate dailyPleasures = ChapterTemplate.builder()
                .templateId("upper_elem_daily_joy") // 신규
                .stageName("학교의 작은 즐거움")
                .mainQuestion("학교에서의 일주일 중, 어떤 요일이나 시간을 가장 기다렸나요? 예를 들어, 특별히 좋아하던 급식 메뉴가 나오던 날, 좋아하던 과목 시간, 혹은 친구들과 마음껏 놀 수 있었던 쉬는 시간 같은 거요.")
                .templateOrder(3)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("UPPER_ELEM_SCHOOL") // 기존 프롬프트 활용 가능
                .chapter(chapter)
                .build();
        templateRepo.save(dailyPleasures);


        ChapterTemplate bestFriend = ChapterTemplate.builder()
                .templateId("upper_elem_best_friend")
                .stageName("친구 이야기: 단짝 친구")
                .mainQuestion("초등학교 시절, 가장 친했던 '단짝' 친구의 이름과 그 친구와 함께했던 가장 기억에 남는 놀이는 무엇이었나요?")
                .templateOrder(4)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("UPPER_ELEM_PEOPLE")
                .chapter(chapter)
                .build();
        templateRepo.save(bestFriend);

        ChapterTemplate peerPlaytime = ChapterTemplate.builder()
                .templateId("upper_elem_playtime") // 신규
                .stageName("친구 이야기: 우리들의 놀이터")
                .mainQuestion("그 친구, 혹은 다른 친구들과 쉬는 시간이나 방과 후에 주로 무엇을 하면서 시간을 보냈나요? 기억에 남는 놀이나 아지트 같은 장소가 있다면 들려주세요.")
                .templateOrder(5)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("UPPER_ELEM_PEOPLE")
                .chapter(chapter)
                .build();
        templateRepo.save(peerPlaytime);

        ChapterTemplate peerRole = ChapterTemplate.builder()
                .templateId("upper_elem_peer_role") // ID 세분화
                .stageName("친구라는 세계: 무리 속의 나")
                .mainQuestion("친구들과의 관계는 어땠나요? 주로 무리를 이끌었나요, 아니면 조용히 따르는 편이었나요? 친구들 사이에서만 불리던 별명이 있었는지도 궁금합니다.")
                .templateOrder(6)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("UPPER_ELEM_PEOPLE") // 같은 프롬프트 키 사용 가능
                .chapter(chapter)
                .build();
        templateRepo.save(peerRole);

        ChapterTemplate peerConflict = ChapterTemplate.builder()
                .templateId("upper_elem_peer_conflict") // 신규
                .stageName("친구 이야기: 다툼과 화해")
                .mainQuestion("가장 친했던 친구와 크게 다투거나 혹은 오해가 생겨 속상했던 기억도 있나요? 어떻게 화해했는지, 그 일을 통해 친구 관계에 대해 무엇을 배우게 되었는지 궁금합니다.")
                .templateOrder(7)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("UPPER_ELEM_PEOPLE")
                .chapter(chapter)
                .build();
        templateRepo.save(peerConflict);


        ChapterTemplate talents = ChapterTemplate.builder()
                .templateId("upper_elem_talents") // ID 통합 및 수정
                .stageName("나의 발견: 잘하는 것과 어려운 것")
                .mainQuestion("공부나 예체능 활동 중에서, 처음으로 '아, 나 이거 정말 잘한다!'며 칭찬받아 어깨가 으쓱했던 경험이 있나요? 반대로, 아무리 노력해도 잘 되지 않아 유독 속상했던 과목이나 활동은 무엇이었나요?")
                .templateOrder(8)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("UPPER_ELEM")
                .chapter(chapter)
                .build();
        templateRepo.save(talents);


        ChapterTemplate dislikedActivities = ChapterTemplate.builder()
                .templateId("upper_elem_disliked_activity") // 신규
                .stageName("나의 발견: 어려웠던 것")
                .mainQuestion("반대로, 학교나 집에서 억지로 해야만 해서 정말 하기 싫었던 활동도 있었나요? 예를 들어, 특정 과목 공부, 방 청소, 학원 가기 같은 것들이요.")
                .templateOrder(9)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("UPPER_ELEM")
                .chapter(chapter)
                .build();
        templateRepo.save(dislikedActivities);


        ChapterTemplate dreamJob = ChapterTemplate.builder()
                .templateId("upper_elem_dream_job") // 신규
                .stageName("하루의 조각: 나의 꿈")
                .mainQuestion("어른이 되면 무엇이 되고 싶으셨나요? 어릴 적 장래희망은 무엇이었고, 왜 그 꿈을 꾸게 되었는지 이야기해주세요.")
                .templateOrder(10)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("UPPER_DREAM_JOB") // 프롬프트 키 신규
                .chapter(chapter)
                .build();
        templateRepo.save(dreamJob);

        ChapterTemplate pureJoy = ChapterTemplate.builder()
                .templateId("upper_elem_pure_joy") // ID 세분화
                .stageName("배움과 놀이: 순수한 즐거움")
                .mainQuestion("성적과 상관없이, 시간 가는 줄 모르고 푹 빠졌던 자신만의 '놀이'나 취미가 있었나요? 예를 들어, 특정 장난감 수집, 만화책 보기, 컴퓨터 게임, 운동 등이요.")
                .templateOrder(11)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("UPPER_ELEM") // 같은 프롬프트 키 사용 가능
                .chapter(chapter)
                .build();
        templateRepo.save(pureJoy);

        ChapterTemplate schoolEvents = ChapterTemplate.builder()
                .templateId("upper_elem_events") // 신규
                .stageName("특별한 하루: 소풍과 운동회")
                .mainQuestion("친구들과 함께 떠났던 소풍이나 현장체험학습, 혹은 모두가 참여했던 운동회 중에 가장 기억에 남는 하루가 있나요? 어디로 가서 무엇을 했고, 왜 그날이 특별하게 기억에 남았나요?")
                .templateOrder(12)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("UPPER_ELEM_EVENT")
                .chapter(chapter)
                .build();
        templateRepo.save(schoolEvents);

        ChapterTemplate schoolFestival = ChapterTemplate.builder()
                .templateId("upper_elem_festival") // 신규
                .stageName("특별한 하루: 무대 위의 나")
                .mainQuestion("많은 사람 앞에서 나의 장기를 뽐냈던 학예회나 장기자랑에 대한 기억도 궁금합니다. 무대 위에서, 혹은 무대를 보면서 느꼈던 떨림이나 짜릿함이 기억나시나요?")
                .templateOrder(13)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("UPPER_ELEM_EVENT")
                .chapter(chapter)
                .build();
        templateRepo.save(schoolFestival);

        ChapterTemplate definingMoment = ChapterTemplate.builder()
                .templateId("upper_elem_wrapup_static")
                .stageName("성장의 발자국")
                .mainQuestion("초등학교 시절을 통틀어, 지금의 '나'에게 가장 큰 영향을 준 하나의 기억을 꼽는다면 무엇일까요?")
                .templateOrder(14) // 마지막 순서
                .stageLevel(lv++)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(definingMoment);


        saveStaticFollowUps(definingMoment, Arrays.asList(
                "그 기억이 지금의 당신에게 어떤 의미로 남아있나요?",
                "그 경험을 통해 얻게 된 교훈이나 삶의 태도가 있다면 무엇인가요?",
                "그때의 나에게 돌아가 딱 한마디만 해줄 수 있다면, 뭐라고 말해주고 싶으세요?"
        ));

        return lv;
    }

    private int createChapter4(int lv) {
        Chapter chapter = Chapter.builder()
                .chapterId("chapter4")
                .chapterName("중학교: 질풍노도의 시절") // 챕터 이름 변경
                .chapterOrder(4)
                .description("급격한 변화 속에서 겪었던 내면의 혼란, 세상의 중심이었던 친구 관계, 학업과 꿈에 대한 고민을 탐색합니다.") // 설명 변경
                .build();
        chapterRepo.save(chapter);

        // --- 변화의 시작, 낯선 내 모습 (사춘기) ---

        ChapterTemplate pubertyChanges = ChapterTemplate.builder()
                .templateId("middle_school_changes")
                .stageName("변화의 시작: 낯선 내 모습")
                .mainQuestion("중학생이 되면서 스스로 가장 크게 달라졌다고 느낀 점은 무엇이었나요? 갑자기 변한 외모나 목소리, 혹은 이전과 달라진 감정 기복 때문에 당황스럽거나 낯설게 느껴졌던 경험이 있나요?")
                .templateOrder(1)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("MIDDLE_PUBERTY")
                .chapter(chapter)
                .build();
        templateRepo.save(pubertyChanges);

        ChapterTemplate mediaInfluence = ChapterTemplate.builder()
                .templateId("middle_school_media")
                .stageName("나의 세계: 음악과 연예인")
                .mainQuestion("그 혼란스러운 시기에, 나의 마음을 알아주는 것 같았던 노래나 영화, 혹은 푹 빠져 지냈던 연예인이나 책이 있었나요? 그것들이 당신에게 어떤 위로와 즐거움을 주었는지 궁금합니다.")
                .templateOrder(2)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("MIDDLE_MEDIA")
                .chapter(chapter)
                .build();
        templateRepo.save(mediaInfluence);


        // --- 세상의 중심, 친구와 설렘 (친구, 연애) ---

        ChapterTemplate peerGroup = ChapterTemplate.builder()
                .templateId("middle_school_peer_group")
                .stageName("세상의 중심: 친구")
                .mainQuestion("초등학교 때와는 또 달랐을 것 같아요. 중학교 시절, 당신은 주로 어떤 친구들과 어울렸나요? 그 무리 안에서 당신의 역할이나 이미지는 어땠나요?")
                .templateOrder(3)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("MIDDLE_PEOPLE")
                .chapter(chapter)
                .build();
        templateRepo.save(peerGroup);

        ChapterTemplate firstCrush = ChapterTemplate.builder()
                .templateId("middle_school_first_crush")
                .stageName("두근거림의 시작: 첫사랑")
                .mainQuestion("이 시기에 처음으로 누군가를 짝사랑하거나, 이성 친구에게 설렘을 느꼈던 기억이 있나요? 그 사람을 생각하면 어떤 감정이나 장면이 떠오르나요?")
                .templateOrder(4)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("MIDDLE_PEOPLE")
                .chapter(chapter)
                .build();
        templateRepo.save(firstCrush);


        // ---  책상의 무게, 학업과 꿈 (공부) ---

        ChapterTemplate academicStress = ChapterTemplate.builder()
                .templateId("middle_school_academics")
                .stageName("책상의 무게: 공부와 성적")
                .mainQuestion("본격적으로 '성적'과 '입시'라는 무게를 느끼기 시작하던 때였을 것 같아요. 학업에 대한 스트레스는 없었나요? 부모님이나 선생님의 기대가 부담으로 다가왔던 적도 있었나요?")
                .templateOrder(5)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("MIDDLE_ACADEMICS")
                .chapter(chapter)
                .build();
        templateRepo.save(academicStress);

        ChapterTemplate futureDream = ChapterTemplate.builder()
                .templateId("middle_school_future_dream")
                .stageName("어른이 된다는 것: 장래희망")
                .mainQuestion("어릴 적 장래희망과 비교했을 때, 중학생이 된 당신의 꿈은 어떻게 변했나요? 더 현실적인 고민을 하기 시작했나요, 아니면 새로운 꿈을 발견했나요?")
                .templateOrder(6)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("MIDDLE_ACADEMICS")
                .chapter(chapter)
                .build();
        templateRepo.save(futureDream);


        // ---  반항과 성장, 기억에 남는 순간 (성찰) ---

        ChapterTemplate rebellion = ChapterTemplate.builder()
                .templateId("middle_school_rebellion")
                .stageName("나만의 규칙: 작은 반항")
                .mainQuestion("어른들이 만든 규칙이나 세상에 대해 처음으로 '왜?'라는 의문을 품거나, 사소한 반항을 했던 기억이 있나요? 그 행동의 계기는 무엇이었나요?")
                .templateOrder(7)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("MIDDLE_GROWTH")
                .chapter(chapter)
                .build();
        templateRepo.save(rebellion);

        // 기존의 가장 추상적이고 어려웠던 질문을, 모든 구체적인 기억을 회상한 뒤 마지막에 배치하여 깊이 있는 성찰을 유도합니다.
        ChapterTemplate wrapUp = ChapterTemplate.builder()
                .templateId("middle_school_wrapup_static")
                .stageName("나를 바꾼 순간")
                .mainQuestion("중학교 시절을 모두 되돌아봤을 때, 지금의 나에게 가장 큰 영향을 남긴 하나의 사건, 한 명의 사람, 혹은 한 가지 깨달음이 있다면 무엇인가요?")
                .templateOrder(8)
                .stageLevel(lv++)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(wrapUp);

        saveStaticFollowUps(wrapUp, Arrays.asList(
                "그 경험 이후, 당신의 어떤 점이 가장 크게 변했다고 생각하시나요?",
                "만약 그때의 나에게 돌아가 조언을 해줄 수 있다면, 어떤 위로나 응원을 해주고 싶으신가요?"
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

        ChapterTemplate futureGoal = ChapterTemplate.builder()
                .templateId("high_school_goal") // ID 신규
                .stageName("마주해야 했던 세계: 목표와 꿈")
                .mainQuestion("고등학생이 된 당신의 가장 큰 목표는 무엇이었나요? '좋은 대학에 가야 한다'는 막연한 목표 외에, 구체적으로 가고 싶었던 학과나 이루고 싶었던 꿈이 있었는지 궁금합니다.")
                .templateOrder(1)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("HIGH_ACADEMICS") // 프롬프트 키 신규
                .chapter(chapter)
                .build();
        templateRepo.save(futureGoal);


        ChapterTemplate struggle = ChapterTemplate.builder()
                .templateId("high_school_struggle") // ID 신규
                .stageName("마주해야 했던 세계: 압박과 현실")
                .mainQuestion("그 목표를 향해 달려가면서 가장 힘들었던 점은 무엇이었나요? 끝이 보이지 않는 공부의 양, 주변의 기대, 혹은 친구와의 경쟁 때문에 지쳤던 순간이 있었나요?")
                .templateOrder(2)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("HIGH_ACADEMICS")
                .chapter(chapter)
                .build();
        templateRepo.save(struggle);

        ChapterTemplate healingHobby = ChapterTemplate.builder()
                .templateId("high_school_healing_hobby") // ID 신규
                .stageName("나만의 작은 세계: 위로와 즐거움")
                .mainQuestion("그렇게 치열하고 힘든 시기를 버티게 해준 자신만의 '숨 쉴 공간'이 있었나요? 예를 들어, 밤늦게 듣던 음악, 챙겨보던 드라마나 애니메이션, 혹은 푹 빠져있던 취미 활동 같은 것들이요.")
                .templateOrder(3)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("HIGH_HOBBY") // 프롬프트 키 신규
                .chapter(chapter)
                .build();
        templateRepo.save(healingHobby);

        ChapterTemplate healingFriend = ChapterTemplate.builder()
                .templateId("high_school_healing_friend") // ID 신규
                .stageName("나만의 작은 세계: 소중한 친구")
                .mainQuestion("지친 마음을 털어놓을 수 있었던 소중한 친구가 있었나요? 함께 웃고 떠들며 스트레스를 풀었던 친구와의 추억이 있다면 들려주세요.")
                .templateOrder(4)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("HIGH_FRIEND") // 프롬프트 키 신규
                .chapter(chapter)
                .build();
        templateRepo.save(healingFriend);

        ChapterTemplate memorableDay = ChapterTemplate.builder()
                .templateId("high_school_memorable_day") // ID 신규
                .stageName("잊을 수 없는 하루")
                .mainQuestion("고등학교 3년 동안, 가장 기억에 남는 하루를 꼽는다면 언젠가요? 큰 시험을 마친 날, 축제, 혹은 친구들과의 특별한 여행처럼, 그날의 분위기와 당신의 감정이 어땠는지 궁금합니다.")
                .templateOrder(5)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("HIGH_EVENT") // 프롬프트 키 신규
                .chapter(chapter)
                .build();
        templateRepo.save(memorableDay);

        ChapterTemplate influence = ChapterTemplate.builder()
                .templateId("high_school_influence") // ID 신규
                .stageName("나의 방향키")
                .mainQuestion("인생의 중요한 갈림길이었을 시기인 만큼, 당신의 진로나 가치관에 큰 영향을 준 선생님, 친구, 혹은 책 속의 한 문장이 있었나요?")
                .templateOrder(6)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("HIGH_GROWTH") // 프롬프트 키 신규
                .chapter(chapter)
                .build();
        templateRepo.save(influence);

        ChapterTemplate wrapUp = ChapterTemplate.builder()
                .templateId("high_school_wrapup_static") // ID 변경
                .stageName("뒤돌아본 나의 성장")
                .mainQuestion("치열했던 고등학교 시절을 졸업하며, 당신은 어떤 사람으로 성장했다고 느끼셨나요?")
                .templateOrder(7)
                .stageLevel(lv++)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(wrapUp);


        saveStaticFollowUps(wrapUp, Arrays.asList(
                "그 시기를 통해 얻은 가장 큰 교훈은 무엇인가요?",
                "만약 시간을 되돌려 그때의 나에게 딱 한마디를 해줄 수 있다면, 어떤 말을 해주고 싶으신가요?"
        ));

        return lv;
    }

    private int createChapter6(int lv) {
        Chapter chapter = Chapter.builder()
                .chapterId("chapter6")
                .chapterName("홀로서기, 사회의 첫걸음") // 챕터 이름 변경
                .chapterOrder(6)
                .description("고등학교 졸업 후, 각자의 길 위에서 겪었던 선택과 고민, 새로운 시작과 성장의 경험을 탐색합니다.") // 설명 변경
                .build();
        chapterRepo.save(chapter);

        // --- 인생의 큰 선택 (공통 질문) ---
        ChapterTemplate theChoice = ChapterTemplate.builder()
                .templateId("transition_the_choice") // ID 재사용 및 역할 재정의
                .stageName("인생의 큰 선택")
                .mainQuestion("고등학교 졸업은 인생의 큰 갈림길이죠. 당시 대학 진학, 취업 등 앞으로의 길에 대해 어떤 고민을 하셨고, 최종적으로 어떤 선택을 내리셨나요? 그 결정의 배경이 궁금합니다.")
                .templateOrder(1)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("TRANSITION")
                .chapter(chapter)
                .build();
        templateRepo.save(theChoice);


        // --- 캠퍼스의 낭만과 현실 (대학 진학자들을 위한 질문) ---
        ChapterTemplate collegeMajor = ChapterTemplate.builder()
                .templateId("college_major_satisfaction") // 신규
                .stageName("캠퍼스 이야기: 전공과 공부")
                .mainQuestion("대학에서는 어떤 전공을 공부하셨나요? 그 전공을 선택한 특별한 이유가 있었는지, 그리고 직접 공부해 보니 만족도는 어땠는지 궁금합니다.")
                .templateOrder(2)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("COLLEGE_OR_WORK")
                .chapter(chapter)
                .build();
        templateRepo.save(collegeMajor);

        ChapterTemplate collegeLife = ChapterTemplate.builder()
                .templateId("college_life_memories") // 신규
                .stageName("캠퍼스 이야기: 낭만과 추억")
                .mainQuestion("대학 생활의 기억 중 가장 먼저 떠오르는 것은 무엇인가요? 즐거웠던 동아리 활동, 새로운 친구들과의 만남, 혹은 잊을 수 없는 수업이나 과제에 대한 추억도 좋습니다.")
                .templateOrder(3)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("COLLEGE_OR_WORK")
                .chapter(chapter)
                .build();
        templateRepo.save(collegeLife);


        // ---  첫 월급의 무게 (사회 초년생들을 위한 질문) ---
        ChapterTemplate firstJob = ChapterTemplate.builder()
                .templateId("first_job_story") // 신규
                .stageName("사회 초년생 이야기: 첫 직장")
                .mainQuestion("처음으로 '내 힘으로 돈을 번다'는 경험은 어떠셨나요? 첫 직장이나 아르바이트를 구하게 된 과정과, 그곳에서 맡았던 역할에 대해 이야기해주세요.")
                .templateOrder(4)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("COLLEGE_OR_WORK")
                .chapter(chapter)
                .build();
        templateRepo.save(firstJob);

        ChapterTemplate workLessons = ChapterTemplate.builder()
                .templateId("work_life_lessons") // 신규
                .stageName("사회 초년생 이야기: 배움과 현실")
                .mainQuestion("사회생활을 일찍 시작하며 배운 가장 큰 교훈은 무엇이었나요? 학생일 때와는 다른 책임감이나 어려움을 느꼈던 순간이 있었다면 들려주세요.")
                .templateOrder(5)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("COLLEGE_OR_WORK")
                .chapter(chapter)
                .build();
        templateRepo.save(workLessons);


        // --- 홀로서기의 시작 (공통 질문) ---
        ChapterTemplate independence = ChapterTemplate.builder()
                .templateId("independence_and_freedom") // 신규
                .stageName("홀로서기의 시작")
                .mainQuestion("스무 살이 넘으면서 이전과는 다른 자유를 느끼셨을 것 같아요. 처음으로 진짜 어른이 되었다고 실감했던 순간은 언제였나요?")
                .templateOrder(6)
                .stageLevel(lv++)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("TRANSITION")
                .chapter(chapter)
                .build();
        templateRepo.save(independence);

        // --- 되돌아보는 나의 선택 (마무리 성찰) ---
        ChapterTemplate wrapUp = ChapterTemplate.builder()
                .templateId("transition_wrapup_static") // ID 재사용 및 역할 재정의
                .stageName("되돌아보는 나의 선택")
                .mainQuestion("그 모든 선택과 경험들이 쌓여 지금의 당신이 되었습니다. 20대의 문턱에서 내렸던 선택들을 되돌아볼 때, 그 시간이 지금의 당신에게 남긴 가장 큰 선물은 무엇이라고 생각하시나요?")
                .templateOrder(7)
                .stageLevel(lv++)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(wrapUp);

        saveStaticFollowUps(wrapUp, Arrays.asList(
                "만약 그 시절의 나에게 돌아가 딱 하나의 조언을 해줄 수 있다면, 무엇을 말해주고 싶으신가요?",
                "그 선택의 결과에 대해, 시간이 지나면서 생각이 달라진 부분이 있나요?"
        ));

        return lv;
    }




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