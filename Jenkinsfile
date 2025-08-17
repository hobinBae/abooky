pipeline {
    agent any

    tools {
        jdk 'OpenJDK-17'
        nodejs 'NodeJS 22.18.0'
    } 

    environment {
        DOCKER_BUILDKIT = '1'

        GRADLE_OPTS = '''
            -Dorg.gradle.daemon=true 
            -Dorg.gradle.parallel=true 
            -Dorg.gradle.caching=true
            -Dorg.gradle.configureondemand=true
            -Dorg.gradle.jvmargs=-Xmx2g
            -Dorg.gradle.workers.max=4
        '''

        BACKEND_IMAGE = 'autobiography-backend'
        FRONTEND_IMAGE = 'autobiography-frontend'
        BUILD_NUMBER_TAG = "${BUILD_NUMBER}"
        LATEST_TAG = 'latest'

        PROJECT_ROOT = "${WORKSPACE}"
        BACKEND_PATH = "autobiography"
        FRONTEND_PATH = "mybook"

        COMPOSE_FILE = 'docker-compose.prod.yml'
        DEPLOY_PATH = '/opt/autobiography-deploy'

        AWS_S3_ACCESS_KEY = credentials('AWS_S3_ACCESS_KEY')
        AWS_S3_SECRET_KEY = credentials('AWS_S3_SECRET_KEY')
        DB_PASSWORD = credentials('DB_PASSWORD')
        JWT_SECRET = credentials('JWT_SECRET')
        GOOGLE_CLIENT_ID = credentials('GOOGLE_CLIENT_ID')
        GOOGLE_CLIENT_SECRET = credentials('GOOGLE_CLIENT_SECRET')
        MAIL_USERNAME = credentials('MAIL_USERNAME')
        MAIL_PASSWORD = credentials('MAIL_PASSWORD')
        CLOVA_STT_API_KEY = credentials('CLOVA_STT_API_KEY')
        OPENAI_API_KEY = credentials('OPENAI_API_KEY')
        LIVEKIT_API_KEY = credentials('LIVEKIT_API_KEY')
        LIVEKIT_API_SECRET = credentials('LIVEKIT_API_SECRET')

        // 캐시 경로
        NPM_CACHE_DIR = "/var/jenkins_home/.npm-cache"
        GRADLE_CACHE_DIR = "/var/jenkins_home/.gradle-cache"
    }

    options {
        timeout(time: 45, unit: 'MINUTES')
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    stages {
        stage('🏗️ Environment Setup') {
            steps {
                sh '''
                    mkdir -p ${NPM_CACHE_DIR}
                    mkdir -p ${GRADLE_CACHE_DIR}
                    echo "=== 시스템 정보 ==="
                    docker --version
                    docker compose --version
                    java -version
                    node --version
                    npm --version
                '''
            }
        }

        stage('📥 Source Code Analysis') {
            steps {
                sh """
                    echo "=== 소스코드 분석 ==="
                    ls -la ${BACKEND_PATH}/
                    ls -la ${FRONTEND_PATH}/
                """
            }
        }

        stage('🧪 Test & Build Applications') {
            parallel {
                stage('Backend Test & Build (with Cache)') {
                    steps {
                        dir("${BACKEND_PATH}") {
                            sh '''
                                export GRADLE_USER_HOME=${GRADLE_CACHE_DIR}
                                chmod +x gradlew
                                ./gradlew build -x test \
                                    --build-cache \
                                    --parallel \
                                    --configure-on-demand \
                                    --daemon \
                                    --info
                            '''
                        }
                    }
                    post {
                        always {
                            archiveArtifacts artifacts: "${BACKEND_PATH}/build/libs/*.jar", allowEmptyArchive: true, fingerprint: true
                        }
                    }
                }

                stage('Frontend Test & Build (with Cache)') {
                    steps {
                        dir("${FRONTEND_PATH}") {
                            sh '''
                                npm config set cache ${NPM_CACHE_DIR} --global
                                npm config set prefer-offline true --global
                                npm config set audit false --global
                                npm config set fund false --global
                                npm config set update-notifier false --global

                                echo "=== 의존성 설치 시작 ==="
                                if [ -f "package-lock.json" ]; then
                                    echo "npm ci 사용 (package-lock.json 발견)"
                                    npm ci --cache ${NPM_CACHE_DIR} --prefer-offline --no-audit --no-fund
                                else
                                    echo "npm install 사용 (package-lock.json 없음)"
                                    npm install --cache ${NPM_CACHE_DIR} --prefer-offline --no-audit --no-fund
                                fi

                                echo "=== 프론트엔드 빌드 시작 ==="
                                npm run build
                            '''
                        }
                    }
                }
            }
        }

        stage('🐳 Build Docker Images') {
            parallel {
                stage('Build Backend Image') {
                    steps {
                        dir("${BACKEND_PATH}") {
                            sh '''
                                docker build \
                                    --cache-from ${BACKEND_IMAGE}:${LATEST_TAG} \
                                    --build-arg BUILDKIT_INLINE_CACHE=1 \
                                    --progress=plain \
                                    -t ${BACKEND_IMAGE}:${BUILD_NUMBER_TAG} .
                                docker tag ${BACKEND_IMAGE}:${BUILD_NUMBER_TAG} ${BACKEND_IMAGE}:${LATEST_TAG}
                            '''
                        }
                    }
                }

                stage('Build Frontend Image') {
                    steps {
                        dir("${FRONTEND_PATH}") {
                            sh '''
                                docker build \
                                    --cache-from ${FRONTEND_IMAGE}:${LATEST_TAG} \
                                    --build-arg BUILDKIT_INLINE_CACHE=1 \
                                    --progress=plain \
                                    -t ${FRONTEND_IMAGE}:${BUILD_NUMBER_TAG} .
                                docker tag ${FRONTEND_IMAGE}:${BUILD_NUMBER_TAG} ${FRONTEND_IMAGE}:${LATEST_TAG}
                            '''
                        }
                    }
                }
            }
        }

        stage('🚀 Deploy Application - Docker Compose V2') {
            parallel{
                stage('Depoly'){
                    steps {
                        sh '''
                            cd ${PROJECT_ROOT}
                            export BACKEND_IMAGE_TAG=${BUILD_NUMBER_TAG}
                            export FRONTEND_IMAGE_TAG=${BUILD_NUMBER_TAG}
                            
                            echo "=== 기존 애플리케이션 배포 ==="
                            docker compose -f ${COMPOSE_FILE} up -d
                            
                            echo "=== SSL 프록시 배포 ==="
                            # 기존 80포트 사용 컨테이너가 있으면 중지
                            docker stop nginx-ssl-proxy || true
                            docker rm nginx-ssl-proxy || true
                            
                            # SSL 프록시 시작
                            docker run -d --name nginx-ssl-proxy \
                                -p 80:80 -p 443:443 \
                                --restart unless-stopped \
                                -v /home/ubuntu/nginx-ssl/nginx.conf:/etc/nginx/nginx.conf:ro \
                                -v /home/ubuntu/certbot/www:/var/www/certbot:ro \
                                -v /home/ubuntu/certbot/conf:/etc/letsencrypt:ro \
                                nginx:alpine
                            
                            echo "=== 배포 완료 ==="
                            docker ps
                        '''
                    }
                }
            }
        }

        stage('🏥 Health Check & Verification') {
            steps {
                sh '''
                    sleep 20
                    curl -f https://i13c203.p.ssafy.io/actuator/health
                    curl -f https://i13c203.p.ssafy.io/health
                '''
            }
        }
    }

    post {
        always {
           cleanWs()  // 간단하게 전체 워크스페이스 정리
        }
        success {
            script {
                // Docker 정리
                sh '''
                    docker image prune -f
                    docker container prune -f
                '''

                // 성공 메시지
                def duration = currentBuild.durationString.replace(' and counting', '')
                echo """
                🎉 Build & Deploy 성공!
                📊 배포 정보:
                    - 빌드 번호: #${BUILD_NUMBER}
                    - Git 커밋: ${env.GIT_COMMIT}
                    - 브랜치: ${env.GIT_BRANCH}
                    - 소요 시간: ${duration}
                """
            }
        }
        failure {
            echo "❌ Build 실패. 로그를 확인하세요."
        }
    }
}
