# ---------- 1단계: 빌드 스테이지 (Node.js) ----------
# 도커 컨테이너 내부에서 소스 코드를 빌드합니다.
FROM node:20-alpine AS builder
WORKDIR /app

# 의존성 설치를 위해 package 파일들 먼저 복사 (캐시 최적화)
COPY package*.json ./
RUN npm ci

# 전체 소스 복사 및 빌드 실행 (dist 폴더 생성)
COPY . .
RUN npm run build

# ---------- 2단계: 실행 스테이지 (Nginx) ----------
# 빌드된 결과물만 가져와서 가벼운 Nginx 서버로 실행합니다.
FROM nginx:alpine

# 빌드 정보 및 메타데이터 (기존 설정 유지)
ARG BUILD_NUMBER
ARG BUILD_TIMESTAMP
LABEL maintainer="ahyoon_test_final <kimay9623@naver.com>"
LABEL description="SSAFY CICD Frontend - Optimized Vue.js Application with Nginx"
LABEL version="2.0.0"

# 시간대 설정 (Asia/Seoul)
ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata curl && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# 🎯 핵심: 1단계(builder)에서 생성된 dist 폴더를 Nginx 문서 루트로 복사
COPY --from=builder /app/dist /usr/share/nginx/html/

# nginx 설정 파일 복사
COPY nginx.conf /etc/nginx/nginx.conf

# 헬스체크용 엔드포인트 및 빌드 정보 파일 생성
RUN echo '<!DOCTYPE html><html><body><h1>OK</h1></body></html>' > /usr/share/nginx/html/health
RUN echo "Build: ${BUILD_NUMBER:-unknown} at ${BUILD_TIMESTAMP:-unknown}" > /usr/share/nginx/html/build-info.txt

# 권한 설정 (기존 보안 설정 유지)
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    chown -R nginx:nginx /etc/nginx/conf.d && \
    touch /var/run/nginx.pid && \
    chown -R nginx:nginx /var/run/nginx.pid

# 포트 노출 (82번 포트 사용)
EXPOSE 82

# 헬스체크 설정 (82번 포트 명시)
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:82/health || exit 1

# Nginx 실행
CMD ["nginx", "-g", "daemon off;"]