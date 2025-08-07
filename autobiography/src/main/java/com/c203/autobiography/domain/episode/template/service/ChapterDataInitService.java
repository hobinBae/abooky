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
        
        createChapter1(); // 기본 정보
        createChapter2(); // 성장 과정
        createChapter3(); // 사회 활동
        createChapter4(); // 개인 생활
        createChapter5(); // 성찰 및 유산
        
        log.info("고품질 챕터 기반 질문 데이터 초기화 완료");
    }
    
    private void createChapter1() {
        Chapter chapter = Chapter.builder()
                .chapterId("chapter1")
                .chapterName("기본 정보")
                .chapterOrder(1)
                .description("사용자의 기본적인 정보와 배경을 파악하는 단계")
                .build();
        chapterRepo.save(chapter);
        
        // 사용자 프로필 템플릿 - 고품질 버전
        ChapterTemplate userProfile = ChapterTemplate.builder()
                .templateId("userProfile")
                .stageName("나는 누구인가")
                .mainQuestion("지금의 당신을 한 문장으로 표현한다면 어떻게 말하고 싶으신가요? 이름과 나이, 현재 하고 계신 일을 포함해서 자유롭게 소개해주세요.")
                .templateOrder(1)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("사용자가 자신을 이렇게 소개했습니다: \"%s\". 이 소개에서 더 깊이 알고 싶은 부분이나 흥미로운 점에 대해 2-3개의 후속 질문을 만들어주세요. " +
                        "중요 주의사항: " +
                        "1. 한국어 이름을 정확히 인식하세요 (예: '배우빈'은 이름이지 배우 직업이 아님) " +
                        "2. 사용자가 실제로 언급한 내용만 바탕으로 질문하세요 " +
                        "3. 추측이나 가정된 정보로 질문하지 마세요 " +
                        "4. 감정이나 개인적 의미를 탐구하는 방향으로 질문해주세요")
                .chapter(chapter)
                .build();
        templateRepo.save(userProfile);
        
        // 출생 및 가족 배경 템플릿 - 감정 중심 개편
        ChapterTemplate birthFamily = ChapterTemplate.builder()
                .templateId("birthFamily")
                .stageName("뿌리와 시작")
                .mainQuestion("당신이 태어나고 자란 곳, 그리고 가족에 대해 이야기해주세요. 어떤 분위기의 집에서 자라셨나요?")
                .templateOrder(2)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(birthFamily);
        
        saveStaticFollowUps(birthFamily, Arrays.asList(
                "가족 중에서 가장 기억에 남는 사람은 누구이고, 그 이유는 무엇인가요?",
                "어린 시절 집에서 일어났던 일 중 지금도 생생하게 기억나는 순간이 있다면 들려주세요.",
                "가족들과 함께했던 시간 중 가장 행복했던 기억은 무엇인가요?"
        ));
    }
    
    private void createChapter2() {
        Chapter chapter = Chapter.builder()
                .chapterId("chapter2")
                .chapterName("성장 과정")
                .chapterOrder(2)
                .description("유년기부터 성인이 되기까지의 성장 경험")
                .build();
        chapterRepo.save(chapter);
        
        // 유년기 이야기 - 감정 깊이 강화
        ChapterTemplate childhood = ChapterTemplate.builder()
                .templateId("childhood")
                .stageName("어린 시절의 기억들")
                .mainQuestion("어린 시절, 당신을 가장 두근거리게 했던 순간이나 깊이 상처받았던 기억이 있나요? 그때의 감정과 함께 이야기해주세요.")
                .templateOrder(1)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("사용자가 어린 시절의 중요한 기억을 이렇게 공유했습니다: \"%s\". 이 경험이 사용자에게 어떤 영향을 미쳤는지, 그리고 그때의 감정이나 교훈에 대해 더 깊이 탐구할 수 있는 2-3개의 후속 질문을 만들어주세요. " +
                        "사용자가 실제로 언급한 내용만을 바탕으로 하고, 추측하지 마세요. 감정적 의미와 성장에 초점을 맞춰주세요.")
                .chapter(chapter)
                .build();
        templateRepo.save(childhood);
        
        // 학창 시절 - 성장과 관계 중심
        ChapterTemplate school = ChapterTemplate.builder()
                .templateId("school")
                .stageName("청춘의 흔적들")
                .mainQuestion("학창시절 중 가장 치열했던 순간이나 소중한 만남에 대해 들려주세요. 친구, 선생님, 또는 특별한 경험 중 지금의 당신을 만든 것이 있다면요.")
                .templateOrder(2)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(school);
        
        saveStaticFollowUps(school, Arrays.asList(
                "그 시절, 당신을 가장 힘들게 했던 것은 무엇이었고 어떻게 이겨냈나요?",
                "학창시절의 꿈과 현재의 모습을 비교해보면 어떤 생각이 드시나요?",
                "그때의 경험이 지금의 당신에게 어떤 힘이 되고 있나요?"
        ));
        
        // 교육 경험 (Static)
        ChapterTemplate education = ChapterTemplate.builder()
                .templateId("education")
                .stageName("학교 및 교육 경험")
                .mainQuestion("대학이나 직업 교육 과정 중 가장 인상 깊었던 수업이나 활동은 무엇인가요?")
                .templateOrder(3)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(education);
        
        saveStaticFollowUps(education, Arrays.asList(
                "그 활동이 당신에게 어떤 영감을 주었나요?",
                "어떤 도전이 있었고, 어떻게 극복했나요?",
                "당신의 진로 선택에 이 경험이 어떤 영향을 미쳤나요?"
        ));
    }
    
    private void createChapter3() {
        Chapter chapter = Chapter.builder()
                .chapterId("chapter3")
                .chapterName("사회 활동")
                .chapterOrder(3)
                .description("사회 진출과 커리어 발전 과정")
                .build();
        chapterRepo.save(chapter);
        
        // 커리어 (Dynamic)
        ChapterTemplate career = ChapterTemplate.builder()
                .templateId("career")
                .stageName("사회 진출 및 커리어")
                .mainQuestion("대학 졸업 후 또는 첫 직장 생활 중 가장 의미 있던 순간은 언제였나요?")
                .templateOrder(1)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("현재 '사회 진출' 단계입니다. 사용자가 답변한 내용: \"%s\". 이 답변에 기반해 2개의 후속 질문을 생성해주세요. 사용자가 실제로 언급한 내용만을 바탕으로 하고, 추측이나 가정하지 마세요.")
                .chapter(chapter)
                .build();
        templateRepo.save(career);
        
        // 업적 및 실패 (Static)
        ChapterTemplate achievements = ChapterTemplate.builder()
                .templateId("achievements")
                .stageName("업적 및 실패 경험")
                .mainQuestion("지금까지 인생에서 가장 큰 성취나 실패 경험은 무엇이며, 그 이유는 무엇인가요?")
                .templateOrder(2)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(achievements);
        
        saveStaticFollowUps(achievements, Arrays.asList(
                "성취(또는 실패) 과정에서 겪은 어려움은 무엇이었나요?",
                "그 경험을 통해 얻은 교훈은 무엇인가요?",
                "이 경험이 당신의 가치관에 어떤 영향을 미쳤나요?"
        ));
    }
    
    private void createChapter4() {
        Chapter chapter = Chapter.builder()
                .chapterId("chapter4")
                .chapterName("개인 생활")
                .chapterOrder(4)
                .description("개인적인 관계와 취미, 건강 등 일상생활")
                .build();
        chapterRepo.save(chapter);
        
        // 인간관계 - 사랑과 상처 중심
        ChapterTemplate relationships = ChapterTemplate.builder()
                .templateId("relationships")
                .stageName("마음을 나눈 사람들")
                .mainQuestion("당신의 인생에서 가장 깊이 사랑했던 사람이나 가장 큰 위로가 되었던 사람에 대해 이야기해주세요. 그 사람과의 만남이 당신에게 어떤 의미였나요?")
                .templateOrder(1)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(relationships);
        
        saveStaticFollowUps(relationships, Arrays.asList(
                "그 사람과 함께 보낸 시간 중 가장 소중한 순간을 구체적으로 묘사해주세요.",
                "헤어짐이나 이별의 아픔을 겪어본 적이 있다면, 그 경험이 당신을 어떻게 변화시켰나요?",
                "사랑이나 우정을 통해 깨달은 인생의 진리가 있다면 무엇인가요?"
        ));
        
        // 여행 (Dynamic)
        ChapterTemplate travel = ChapterTemplate.builder()
                .templateId("travel")
                .stageName("여행 및 모험")
                .mainQuestion("가장 기억에 남는 여행지는 어디이며, 그 이유는 무엇인가요?")
                .templateOrder(2)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("지금 '여행 및 모험' 단계입니다. 사용자가 답변한 내용: \"%s\". 사용자가 실제로 언급한 여행 경험만을 바탕으로 해당 여행을 깊이 탐구하기 위한 후속 질문 2개를 생성해주세요. 추측하지 마세요.")
                .chapter(chapter)
                .build();
        templateRepo.save(travel);
        
        // 취미 (Static)
        ChapterTemplate hobbies = ChapterTemplate.builder()
                .templateId("hobbies")
                .stageName("취미 및 관심사")
                .mainQuestion("당신이 오랫동안 즐겨온 취미나 활동은 무엇이며, 어떻게 시작하게 되었나요?")
                .templateOrder(3)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(hobbies);
        
        saveStaticFollowUps(hobbies, Arrays.asList(
                "이 취미를 통해 얻은 즐거움은 무엇인가요?",
                "취미 활동 중 기억에 남는 에피소드는 무엇인가요?",
                "이 경험이 당신의 삶에 어떤 변화를 주었나요?"
        ));
        
        // 건강 (Static)
        ChapterTemplate health = ChapterTemplate.builder()
                .templateId("health")
                .stageName("건강 및 웰빙")
                .mainQuestion("건강이나 웰빙을 위해 특별히 노력한 경험이 있나요? 그 이유는 무엇인가요?")
                .templateOrder(4)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(health);
        
        saveStaticFollowUps(health, Arrays.asList(
                "그 노력이 삶에 어떤 긍정적 변화를 가져왔나요?",
                "어려웠던 점은 무엇이며, 어떻게 극복했나요?",
                "앞으로 건강을 위해 계획하고 있는 것은 무엇인가요?"
        ));
    }
    
    private void createChapter5() {
        Chapter chapter = Chapter.builder()
                .chapterId("chapter5")
                .chapterName("성찰 및 유산")
                .chapterOrder(5)
                .description("인생을 돌아보고 미래에 대한 비전을 정리하는 단계")
                .build();
        chapterRepo.save(chapter);
        
        // 성찰 - 인생의 의미 탐구
        ChapterTemplate reflection = ChapterTemplate.builder()
                .templateId("reflection")
                .stageName("인생을 돌아보며")
                .mainQuestion("지금까지 살아오면서 가장 후회되는 일과 가장 감사한 일이 무엇인지, 그리고 만약 과거로 돌아갈 수 있다면 무엇을 다르게 하고 싶은지 솔직하게 이야기해주세요.")
                .templateOrder(1)
                .followUpType(FollowUpType.DYNAMIC)
                .dynamicPromptTemplate("사용자가 인생에 대한 깊은 성찰을 이렇게 나누었습니다: \"%s\". 사용자가 실제로 언급한 내용만을 바탕으로 이 답변에서 더 깊이 탐구할 수 있는 철학적이고 의미있는 후속 질문 2개를 만들어주세요. 추측이나 가정하지 말고, 인생의 의미, 가치, 성장에 대한 통찰을 이끌어낼 수 있도록 해주세요.")
                .chapter(chapter)
                .build();
        templateRepo.save(reflection);
        
        // 유산 - 삶의 메시지
        ChapterTemplate legacy = ChapterTemplate.builder()
                .templateId("legacy")
                .stageName("남기고 싶은 이야기")
                .mainQuestion("당신의 인생이 한 권의 책이라면, 마지막 페이지에 어떤 메시지를 남기고 싶으신가요? 당신을 기억할 사람들에게 전하고 싶은 진심어린 말이 있다면요.")
                .templateOrder(2)
                .followUpType(FollowUpType.STATIC)
                .chapter(chapter)
                .build();
        templateRepo.save(legacy);
        
        saveStaticFollowUps(legacy, Arrays.asList(
                "당신이 가장 소중하게 여기는 가치나 신념은 무엇이며, 이것을 어떻게 실천해왔나요?",
                "힘든 시기를 겪고 있는 누군가에게 해주고 싶은 조언이나 격려의 말이 있나요?",
                "당신의 삶을 통해 증명하고 싶었던 것은 무엇이고, 그것을 이루었다고 생각하시나요?"
        ));
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