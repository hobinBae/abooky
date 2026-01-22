pipeline {
    agent any

    environment {
        // 컨테이너 이름 설정
        BE_CONTAINER = "be-abooky"
        FE_CONTAINER = "fe-abooky"
        
        // Jenkins Credentials ID (.env 파일)
        ENV_ID  = 'ABOOKY_ENV_FILE'
        
        // 도커 컴포즈 파일명 명시
        DOCKER_COMPOSE_FILE = 'docker-compose-prod.yml'
    }

    stages {
        stage('Step 1: 환경 변수(.env) 및 권한 설정') {
            steps {
                script {
                    // 1. 보안 주입: .env 파일을 프로젝트 루트로 복사
                    withCredentials([file(credentialsId: "${ENV_ID}", variable: 'envFile')]) {
                        sh 'cp $envFile .env'
                    }
                    
                    // 2. 권한 부여: 도커 내부 빌드를 위해 Gradle 래퍼 실행 권한 확인
                    dir('autobiography') {
                        sh 'chmod +x gradlew'
                    }
                    
                    echo "✅ 환경 변수 주입 및 빌드 준비 완료"
                }
            }
        }

        /* 💡 참고: 젠킨스에서 직접 빌드(Step 2, 3)를 수행하지 않습니다.
           백엔드와 프론트엔드 Dockerfile 내부에서 각각 Gradle과 NPM 빌드가 진행됩니다.
        */

        stage('Step 2: 통합 빌드 및 배포 (Docker)') {
            steps {
                script {
                    echo "🛠️ 도커 멀티 스테이지 빌드 시작..."

                    // 1. 기존 컨테이너 및 미사용 리소스 정리
                    sh "docker-compose -f ${DOCKER_COMPOSE_FILE} down || true"
                    
                    // 2. 통합 빌드 실행 (--no-cache로 소스 코드 변경사항 즉시 반영)
                    // 이 과정에서 백엔드(JAR 빌드)와 프론트엔드(NPM 빌드)가 동시에 진행됩니다.
                    sh "docker-compose -f ${DOCKER_COMPOSE_FILE} build --no-cache"
                    
                    // 3. 컨테이너 백그라운드 실행
                    sh "docker-compose -f ${DOCKER_COMPOSE_FILE} up -d"
                    
                    echo "✅ 아북이 서비스 배포 성공 (FE: 82, BE: 8081)"
                }
            }
        }

        stage('Step 3: 상태 확인 (Health Check)') {
            steps {
                script {
                    echo "⏳ 서비스 안정화 대기 (30초)..."
                    sleep 30

                    // 컨테이너 실행 상태 확인
                    def beStatus = sh(script: "docker inspect --format='{{.State.Status}}' ${BE_CONTAINER}", returnStdout: true).trim()
                    def feStatus = sh(script: "docker inspect --format='{{.State.Status}}' ${FE_CONTAINER}", returnStdout: true).trim()

                    if (beStatus == 'running' && feStatus == 'running') {
                        echo "🎉 [SUCCESS] 모든 서비스가 정상 작동 중입니다."
                    } else {
                        error "🚨 [FAILURE] 서비스 상태 이상 (BE: ${beStatus}, FE: ${feStatus})"
                    }
                }
            }
        }
    }

    post {
        always {
            // 빌드 후 불필요한 이미지 정리 및 워크스페이스 청소
            sh "docker image prune -f"
            cleanWs()
        }
        success {
            echo "✨ 배포 프로세스가 완료되었습니다."
        }
        failure {
            echo "🔥 배포 실패! 젠킨스 로그와 도커 로그(docker logs [컨테이너명])를 확인하세요."
        }
    }
}