pipeline {
    agent any

    environment {
        BE_CONTAINER = "be-abooky"
        FE_CONTAINER = "fe-abooky"
        ENV_ID  = 'ABOOKY_ENV_FILE' // .env 파일만 크리덴셜에서 관리
    }

    stages {
        stage('Step 1: 환경 변수(.env) 주입') {
            steps {
                // 프로퍼티스는 Git에 있으므로 .env만 가져옵니다.
                withCredentials([file(credentialsId: "${ENV_ID}", variable: 'envFile')]) {
                    script {
                        sh "cp ${envFile} .env"
                        echo "✅ .env 주입 완료 (프로퍼티스는 Git 소스 사용)"
                    }
                }
            }
        }

        stage('Step 2: Backend 빌드 (Gradle)') {
            steps {
                dir('autobiography') {
                    sh 'chmod +x gradlew'
                    // 빌드 시점에 테스트는 제외하고 빌드합니다.
                    sh './gradlew clean build -x test'
                }
            }
        }

        stage('Step 3: Frontend 빌드 (NPM)') {
            steps {
                dir('mybook') {
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Step 4: 통합 배포 (Docker)') {
            steps {
                script {
                    sh "docker-compose down || true"
                    // --no-cache로 새 빌드 파일 반영 보장
                    sh "docker-compose build --no-cache"
                    sh "docker-compose up -d"
                }
            }
        }
    }

    post {
        always {
            cleanWs()
            sh "docker image prune -f"
        }
    }
}