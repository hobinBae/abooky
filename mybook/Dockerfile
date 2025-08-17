# 🚀 안전한 Frontend Dockerfile - 기존 기능 모두 보존
# Jenkins에서 이미 빌드된 dist 폴더를 직접 사용

FROM nginx:alpine

# 빌드 정보 (기존과 동일)
ARG BUILD_NUMBER
ARG BUILD_TIMESTAMP
ARG CACHEBUST=1

# 메타데이터 (기존과 동일)
LABEL maintainer="ahyoon_test_final <kimay9623@naver.com>"
LABEL description="SSAFY CICD Frontend - Optimized Vue.js Application with Nginx"
LABEL version="2.0.0"
LABEL build.number="${BUILD_NUMBER}"
LABEL build.timestamp="${BUILD_TIMESTAMP}"

# 시간대 설정 (기존과 동일)
ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# 헬스체크용 도구 설치 (기존과 동일)
RUN apk add --no-cache curl

# 🎯 핵심: Jenkins에서 빌드한 dist 폴더 직접 복사
COPY dist/ /usr/share/nginx/html/

# nginx 설정 파일 복사 (기존 위치에서 직접)
COPY nginx.conf /etc/nginx/nginx.conf

# 헬스체크용 엔드포인트 생성 (기존 헬스체크 경로 호환)
RUN echo '<!DOCTYPE html><html><head><title>Health Check</title></head><body><h1>OK</h1><p>Service is healthy</p></body></html>' > /usr/share/nginx/html/health

# 빌드 정보 파일 생성 (기존 기능 유지)
RUN echo "Build: ${BUILD_NUMBER:-unknown} at ${BUILD_TIMESTAMP:-unknown} (${CACHEBUST:-1})" > /usr/share/nginx/html/build-info.txt

# 권한 설정 (기존과 동일)
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    chown -R nginx:nginx /etc/nginx/conf.d && \
    touch /var/run/nginx.pid && \
    chown -R nginx:nginx /var/run/nginx.pid

# nginx 사용자로 실행 (기존과 동일)
USER nginx

# 포트 노출 (기존과 동일)
EXPOSE 80

# 헬스체크 (기존과 동일한 설정)
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost/health || exit 1

# nginx 실행 (기존과 동일)
CMD ["nginx", "-g", "daemon off;"]