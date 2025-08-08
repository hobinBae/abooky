# 백엔드 Dockerfile - Spring Boot 애플리케이션 (jdk 용)

# 캐시 무력화를 위한 빌드 인자 (맨 위로 이동)
ARG BUILD_NUMBER
ARG BUILD_TIMESTAMP  
ARG CACHEBUST=1

# Java 17을 베이스 이미지로 사용
FROM openjdk:17-jdk-slim

# 메타데이터 추가
LABEL maintainer="ahyoon_test_final <kimay9623@naver.com>"
LABEL description="SSAFY CICD Backend - Spring Boot Application"
LABEL version="1.0.0"

# 작업 디렉토리 설정
WORKDIR /app

# 시간대 설정 (한국 시간)
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 캐시 무력화를 위한 파일 생성 (중요!)
RUN echo "Build: ${BUILD_NUMBER:-unknown} at ${BUILD_TIMESTAMP:-unknown} (${CACHEBUST:-1})" > /app/build-info.txt

# 시스템 패키지 업데이트 및 필요한 도구 설치
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

# JAR 파일 복사 (빌드된 결과물)
COPY build/libs/autobiography-0.0.1-SNAPSHOT.jar app.jar

# 애플리케이션 실행을 위한 사용자 생성 (보안)
RUN groupadd -r spring && useradd -r -g spring spring
RUN chown -R spring:spring /app
USER spring

# 포트 노출
EXPOSE 8081

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1
    

# JVM 옵션 설정 및 애플리케이션 실행
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=prod", \
    "-Xms256m", \
    "-Xmx512m", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=200", \
    "-jar", \
    "app.jar"]


# # 백엔드 Dockerfile - Spring Boot 애플리케이션 (jre용)
# # 캐시 무력화를 위한 빌드 인자 (맨 위로 이동)
# ARG BUILD_NUMBER
# ARG BUILD_TIMESTAMP
# ARG CACHEBUST=1

# # Java 17을 베이스 이미지로 사용
# FROM eclipse-temurin:17-jre-alpine

# # 메타데이터 추가
# LABEL maintainer="ahyoon_test_final <kimay9623@naver.com>"
# LABEL description="SSAFY CICD Backend - Spring Boot Application"
# LABEL version="1.0.0"

# # 작업 디렉토리 설정
# WORKDIR /app

# # 시간대 설정 (한국 시간) - Alpine용
# ENV TZ=Asia/Seoul
# RUN apk add --no-cache tzdata && \
#     ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
#     echo $TZ > /etc/timezone

# # 캐시 무력화를 위한 파일 생성 (중요!)
# RUN echo "Build: ${BUILD_NUMBER:-unknown} at ${BUILD_TIMESTAMP:-unknown} (${CACHEBUST:-1})" > /app/build-info.txt

# # 시스템 패키지 업데이트 및 필요한 도구 설치 - Alpine용
# RUN apk add --no-cache curl shadow

# # JAR 파일 복사 (빌드된 결과물)
# COPY build/libs/app.jar app.jar

# # 애플리케이션 실행을 위한 사용자 생성 (보안)
# RUN addgroup -S spring && adduser -S spring -G spring
# RUN chown -R spring:spring /app
# USER spring

# # 포트 노출
# EXPOSE 8081

# # 헬스체크 추가
# HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
#     CMD curl -f http://localhost:8081/actuator/health || exit 1

# # JVM 옵션 설정 및 애플리케이션 실행
# ENTRYPOINT ["java", \
#     "-Djava.security.egd=file:/dev/./urandom", \
#     "-Dspring.profiles.active=prod", \
#     "-Xms256m", \
#     "-Xmx512m", \
#     "-XX:+UseG1GC", \
#     "-XX:MaxGCPauseMillis=200", \
#     "-jar", \
#     "app.jar"]