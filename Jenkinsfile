pipeline {
    agent any

    environment {
        // 컨테이너 이름 설정 (이전 설정 유지)
        BE_CONTAINER = "be-abooky"
        FE_CONTAINER = "fe-abooky"
        ENV_ID  = 'ABOOKY_ENV_FILE' // Credentials ID
    }

    stages {
        stage('Step 1: 환경 변수(.env) 주입') {
            steps {
                withCredentials([file(credentialsId: "${ENV_ID}", variable: 'envFile')]) {
                    script {
                        sh 'cp $envFile .env'
                        echo "✅ .env 주입 완료 (프로퍼티스는 Git 소스 사용)"
                    }
                }
            }
        }

        stage('Step 2: Backend 빌드 (Gradle)') {
            steps {
                dir('autobiography') {
                    sh 'chmod +x gradlew'
                    // JAR 파일 생성 (테스트 제외)
                    sh './gradlew clean build -x test'
                    echo "✅ 백엔드 JAR 빌드 완료"
                }
            }
        }

        // 🎯 Step 3 (NPM 빌드) 삭제
        // 이유: 프론트엔드 Dockerfile 내에서 node:20 이미지를 사용해 직접 빌드함

        stage('Step 3: 통합 배포 (Docker)') {
            steps {
                script {
                    // 기존 서비스 중지
                    sh "docker-compose-prod down || true"
                    
                    // --no-cache 옵션으로 Dockerfile의 빌드 스테이지부터 새로 실행 보장
                    // 이 과정에서 프론트엔드 npm install 및 build가 진행됩니다.
                    sh "docker-compose-prod build --no-cache"
                    
                    // 컨테이너 백그라운드 실행
                    sh "docker-compose-prod up -d"
                    echo "✅ 도커 컨테이너 배포 완료 (FE: Port 82, BE: Port 8081)"
                }
            }
        }
    }

    post {
        always {
            // 작업 공간 정리 및 미사용 도커 이미지 삭제 (디스크 공간 확보)
            cleanWs()
            sh "docker image prune -f"
        }
        success {
            echo "🎉 배포가 성공적으로 완료되었습니다!"
        }
        failure {
            echo "❌ 배포 중 오류가 발생했습니다. 로그를 확인하세요."
        }
    }
}
