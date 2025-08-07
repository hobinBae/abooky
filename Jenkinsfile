pipeline {
    agent any

    tools {
        jdk 'OpenJDK-17'
        nodejs 'NodeJS 22.18.0'
    }

    environment {
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
        NPM_CACHE_DIR = "${WORKSPACE}/.npm-cache"
        GRADLE_CACHE_DIR = "${WORKSPACE}/.gradle-cache"
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
                                ./gradlew build -x test
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
                                npm ci || npm install
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
                                docker build --cache-from ${BACKEND_IMAGE}:${LATEST_TAG} -t ${BACKEND_IMAGE}:${BUILD_NUMBER_TAG} .
                                docker tag ${BACKEND_IMAGE}:${BUILD_NUMBER_TAG} ${BACKEND_IMAGE}:${LATEST_TAG}
                            '''
                        }
                    }
                }

                stage('Build Frontend Image') {
                    steps {
                        dir("${FRONTEND_PATH}") {
                            sh '''
                                docker build --cache-from ${FRONTEND_IMAGE}:${LATEST_TAG} -t ${FRONTEND_IMAGE}:${BUILD_NUMBER_TAG} .
                                docker tag ${FRONTEND_IMAGE}:${BUILD_NUMBER_TAG} ${FRONTEND_IMAGE}:${LATEST_TAG}
                            '''
                        }
                    }
                }
            }
        }

        stage('🚀 Deploy Application - Docker Compose V2') {
            steps {
                sh '''
                    cd ${PROJECT_ROOT}
                    export BACKEND_IMAGE_TAG=${BUILD_NUMBER_TAG}
                    export FRONTEND_IMAGE_TAG=${BUILD_NUMBER_TAG}
                    docker compose -f ${COMPOSE_FILE} up -d
                '''
            }
        }

        stage('🏥 Health Check & Verification') {
            steps {
                sh '''
                    sleep 20
                    curl -f http://i13c203.p.ssafy.io:8081/cicd/health
                    curl -f http://i13c203.p.ssafy.io:3000/health
                '''
            }
        }
    }

    post {
        always {
            cleanWs()
            sh '''
                docker image prune -f
                docker container prune -f
            '''
        }
        success {
            script {
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
