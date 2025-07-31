// ✅ Whisper STT 전용 클라이언트 (WAV 지원 버전, Usage 매핑)

package com.c203.autobiography.domain.stt.client;

import com.c203.autobiography.domain.stt.dto.SttResponse;
import java.io.IOException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class WhisperSttClient implements SttClient {

    @Value("${openai.api.key}")
    private String apiKey;

    private RestTemplate restTemplate;

    @PostConstruct
    public void initRestTemplate() {
        this.restTemplate = new RestTemplate();

        // GMS 프록시를 위한 헤더 인터셉터 추가
        this.restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("Authorization", "Bearer " + apiKey);

            log.info("인터셉터에서 헤더 재설정: Authorization=Bearer {}",
                    apiKey != null ? apiKey.substring(0, Math.min(15, apiKey.length())) + "..." : "null");
            log.info("모든 요청 헤더: {}", request.getHeaders().keySet());

            return execution.execute(request, body);
        });

        log.info("RestTemplate 초기화 완료 - API Key: {}",
                apiKey != null ? apiKey.substring(0, Math.min(15, apiKey.length())) + "..." : "null");
    }

    private static final String URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/audio/transcriptions";

    @Override
    public SttResponse recognize(MultipartFile audio) {
        return recognize(audio, null);
    }

    /**
     * 사용자별 맞춤 고유명사를 포함한 음성 인식
     * 
     * @param audio             오디오 파일
     * @param customProperNouns 사용자별 추가 고유명사 (예: 프로젝트명, 팀명, 특별한 용어들)
     */
    public SttResponse recognize(MultipartFile audio, String customProperNouns) {
        try {
            HttpHeaders headers = new HttpHeaders();
            // Authorization 헤더는 인터셉터에서 자동 설정됨

            log.info("🔑 사용할 API 키: {}", apiKey);
            log.info("📡 인터셉터가 Authorization 헤더를 자동 설정함");

            // 파일 리소스 생성 (PCM WAV 등 지원)
            ByteArrayResource resource = new ByteArrayResource(audio.getBytes()) {
                @Override
                public String getFilename() {
                    String name = audio.getOriginalFilename();
                    return (name != null && !name.isEmpty()) ? name : "audio.webm";
                }
            };

            // 파트별 Content-Type 설정
            HttpHeaders partHeaders = new HttpHeaders();
            partHeaders.setContentType(MediaType.parseMediaType(
                    audio.getContentType() != null ? audio.getContentType() : "audio/webm"));
            HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(resource, partHeaders);

            // multipart body 구성 (고유명사 인식 개선)
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", filePart);
            body.add("model", "whisper-1");
            body.add("language", "ko");

            // 🎯 고유명사 인식 개선을 위한 추가 파라미터들

            // 1. 프롬프트: 예상되는 고유명사와 컨텍스트 제공
            String prompt = buildPromptForProperNouns(customProperNouns);
            body.add("prompt", prompt);

            // 2. 온도: 0으로 설정하여 일관된 결과 보장
            body.add("temperature", "0");

            // 3. 응답 형식: JSON으로 구조화된 응답
            body.add("response_format", "json");

            log.info("🎯 고유명사 인식용 프롬프트: {}", prompt);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            log.info("🚀 Whisper WAV 요청: filename={}, size={} bytes", resource.getFilename(), audio.getSize());

            // ResponseEntity<SttResponse>로 매핑
            ResponseEntity<SttResponse> response = restTemplate.postForEntity(URL, request, SttResponse.class);
            SttResponse sttResponse = response.getBody();
            log.info("✅ Whisper 응답: text='{}', usage={}s",
                    sttResponse.getText(),
                    sttResponse.getUsage() != null ? sttResponse.getUsage().getSeconds() : null);
            String output = sttResponse.getText();
            if(output.matches(".*(시청|구독|감사|좋아요|안녕히).*")){
                log.warn("할루미네이션 제거", output);
                sttResponse.setText(" ");
                return sttResponse;
            }
            return sttResponse;

        } catch (IOException e) {
            log.error("Whisper STT 처리 중 IO 오류", e);
            throw new RuntimeException("Whisper STT 처리 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 고유명사 인식 정확도 향상을 위한 프롬프트 생성
     * OpenAI Whisper는 프롬프트를 통해 예상되는 단어들을 미리 알려주면 인식률이 크게 향상됩니다.
     * 
     * @param customProperNouns 사용자별 추가 고유명사 (null 가능)
     */
    private String buildPromptForProperNouns(String customProperNouns) {
        StringBuilder promptBuilder = new StringBuilder();

        // 자서전 특화 프롬프트
        promptBuilder.append(String.join(" ",
                // 기본 인사말과 컨텍스트
                "안녕하세요.", "저는", "태어났습니다.", "자랐습니다.", "다녔습니다.",

                // 교육기관 (유치원~대학교)
                "어린이집", "유치원", "초등학교", "중학교", "고등학교", "대학교", "대학원",
                "서울대학교", "연세대학교", "고려대학교", "성균관대학교", "한양대학교", "중앙대학교",
                "경희대학교", "건국대학교", "동국대학교", "홍익대학교", "숭실대학교", "국민대학교",

                // 지역명 (출생지, 거주지, 여행지)
                "서울", "인천", "부산", "대구", "광주", "대전", "울산", "세종", "경기도", "강원도",
                "충청도", "전라도", "경상도", "제주도", "강남구", "서초구", "종로구", "중구",
                "마포구", "용산구", "성동구", "광진구", "동대문구", "성북구", "도봉구", "노원구",

                // 가족 관련 호칭
                "아버지", "어머니", "아빠", "엄마", "할아버지", "할머니", "형", "누나", "언니", "오빠",
                "동생", "삼촌", "이모", "고모", "외할머니", "외할아버지", "사촌", "조카",

                // 일반적인 한국 성씨와 이름
                "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오", "서", "신", "권",
                "황", "안", "송", "류", "전", "홍", "고", "문", "양", "손", "배", "조", "백", "허", "유",
                "민수", "지영", "서연", "준호", "하늘", "민지", "상우", "예진", "도현", "수빈",

                // 학과/전공
                "컴퓨터공학과", "전자공학과", "기계공학과", "산업공학과", "경영학과", "경제학과",
                "국어국문학과", "영어영문학과", "수학과", "물리학과", "화학과", "생물학과", "의학과",
                "법학과", "심리학과", "사회학과", "정치외교학과", "건축학과", "디자인학과",

                // 직업/직장
                "회사원", "공무원", "교사", "의사", "간호사", "엔지니어", "디자이너", "개발자",
                "프로그래머", "연구원", "교수", "변호사", "회계사", "상담사", "기자", "작가",

                // 회사명/기관명
                "삼성", "LG", "SK", "현대", "기아", "네이버", "카카오", "라인", "쿠팡", "배달의민족",
                "토스", "당근마켓", "우아한형제들", "NHN", "NC소프트", "넥슨", "CJ", "롯데",

                // 기술/IT 용어
                "SSAFY", "싸피", "삼성청년소프트웨어아카데미", "프로그래밍", "코딩", "개발",
                "Java", "자바", "Python", "파이썬", "JavaScript", "자바스크립트", "React", "리액트",
                "Spring", "스프링", "Node.js", "MySQL", "Git", "GitHub", "깃허브",

                // 취미/관심사
                "독서", "영화", "음악", "게임", "축구", "야구", "농구", "배드민턴", "테니스", "수영",
                "등산", "여행", "사진", "그림", "요리", "운동", "헬스", "요가", "필라테스",

                // 문화/엔터테인먼트
                "BTS", "블랙핑크", "트와이스", "아이유", "박진영", "이수만", "방시혁", "YG", "SM", "JYP",
                "넷플릭스", "유튜브", "인스타그램", "페이스북", "틱톡", "카카오톡",

                // 교통/장소
                "지하철", "버스", "기차", "KTX", "비행기", "택시", "자전거", "자동차",
                "강남역", "홍대", "명동", "이태원", "가로수길", "압구정", "청담동", "한강",

                // 음식/레스토랑
                "한식", "중식", "일식", "양식", "김치", "불고기", "비빔밥", "냉면", "삼겹살",
                "치킨", "피자", "햄버거", "커피", "스타벅스", "투썸플레이스", "카페베네",

                // 특별한 경험/이벤트
                "졸업식", "입학식", "수학여행", "체육대회", "축제", "동아리", "아르바이트", "인턴십",
                "취업", "이직", "승진", "결혼", "신혼여행", "이사", "군대", "병역", "제대",

                // 감정/상태 표현
                "기뻤습니다", "슬펐습니다", "힘들었습니다", "즐거웠습니다", "감사했습니다",
                "후회했습니다", "자랑스러웠습니다", "보람찼습니다", "아쉬웠습니다",

                // 마무리 문장
                "그때부터", "그 이후로", "지금까지", "앞으로도", "감사합니다.", "이상입니다."));

        // 사용자별 맞춤 고유명사 추가
        if (customProperNouns != null && !customProperNouns.trim().isEmpty()) {
            promptBuilder.append(" ").append(customProperNouns.trim());
            log.info("🎯 사용자 맞춤 고유명사 추가: {}", customProperNouns);
        }

        return promptBuilder.toString();
    }
}
