pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }
    
    environment {
        APP_NAME = 'webapp'
        DEPLOY_SERVER = '192.168.1.112'
        DEPLOY_PATH = '/var/www/webapp'
        JAR_NAME = 'webapp-1.0.0.jar'
        GIT_REPO = 'https://github.com/hsm0711/toy.git'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo '=== Git 저장소에서 코드 가져오기 ==='
                git branch: 'main',
                    credentialsId: 'github-credentials',
                    url: "${GIT_REPO}"
            }
        }
        
        stage('Build') {
            steps {
                echo '=== Maven 빌드 시작 ==='
                sh 'mvn clean package -DskipTests'
            }
        }
        
        stage('Test') {
            steps {
                echo '=== 단위 테스트 실행 ==='
                sh 'mvn test'
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }
        
        stage('Code Quality Analysis') {
            steps {
                echo '=== 코드 품질 분석 ==='
                sh 'mvn verify'
            }
        }
        
        stage('Archive Artifacts') {
            steps {
                echo '=== 빌드 산출물 아카이빙 ==='
                archiveArtifacts artifacts: "target/${JAR_NAME}", 
                                fingerprint: true
            }
        }
        
        stage('Deploy to Server') {
            steps {
                echo '=== 서버에 배포 시작 ==='
                sshagent(['webapp-server-ssh']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no root@${DEPLOY_SERVER} '
                            mkdir -p ${DEPLOY_PATH}/backup
                        '
                        
                        ssh root@${DEPLOY_SERVER} '
                            if [ -f ${DEPLOY_PATH}/${JAR_NAME} ]; then
                                cp ${DEPLOY_PATH}/${JAR_NAME} ${DEPLOY_PATH}/backup/${JAR_NAME}.\$(date +%Y%m%d_%H%M%S)
                            fi
                        '
                        
                        scp target/${JAR_NAME} root@${DEPLOY_SERVER}:${DEPLOY_PATH}
                        
                        if [ -f .env.production ]; then
                            scp .env.production root@${DEPLOY_SERVER}:${DEPLOY_PATH}/.env
                        fi
                        
                        ssh root@${DEPLOY_SERVER} '
                            systemctl restart webapp
                            sleep 5
                            systemctl status webapp
                        '
                    """
                }
            }
        }
        
        stage('Health Check') {
            steps {
                echo '=== 애플리케이션 상태 확인 ==='
                script {
                    def response = sh(
                        script: "curl -s -o /dev/null -w '%{http_code}' http://${DEPLOY_SERVER}:8080/",
                        returnStdout: true
                    ).trim()
                    
                    if (response == '200') {
                        echo "✅ 애플리케이션 정상 동작 중 (HTTP ${response})"
                    } else {
                        error "❌ 애플리케이션 상태 이상 (HTTP ${response})"
                    }
                }
            }
        }
    }
    
    post {
        success {
            echo '🎉 배포 성공!'
            emailext(
                subject: "✅ 배포 성공: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    프로젝트: ${env.JOB_NAME}
                    빌드 번호: ${env.BUILD_NUMBER}
                    상태: 성공
                    
                    빌드 URL: ${env.BUILD_URL}
                    
                    배포 서버: ${DEPLOY_SERVER}
                    배포 시간: ${new Date()}
                """,
                to: 'your-email@example.com'
            )
        }
        failure {
            echo '❌ 배포 실패!'
            emailext(
                subject: "❌ 배포 실패: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    프로젝트: ${env.JOB_NAME}
                    빌드 번호: ${env.BUILD_NUMBER}
                    상태: 실패
                    
                    빌드 URL: ${env.BUILD_URL}
                    
                    로그를 확인해주세요.
                """,
                to: 'your-email@example.com'
            )
        }
        always {
            echo '=== 빌드 완료 ==='
            cleanWs()
        }
    }
}