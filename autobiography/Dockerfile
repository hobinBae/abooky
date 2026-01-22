# autobiography/Dockerfile
FROM openjdk:17-jdk-slim

# 시간대 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Step 2에서 생성된 빌드 결과물(JAR)을 컨테이너 내부로 복사
# 파일명이 다를 수 있으니 build/libs/*.jar 로 지정합니다.
COPY build/libs/*.jar app.jar

# 실행 권한 부여 및 실행
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "/app.jar"]