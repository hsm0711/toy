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
        CONFIG_BACKUP_DIR = '/var/www/webapp/config-backup'
        DISCORD_WEBHOOK_URL = 'https://discord.com/api/webhooks/1466744325269491723/1oapLRbzkuk4lN89KQEFDVrfFOt9goxmThZ1k1EW0PPutI2gHPd355T3NonbrLKHnQM'
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
        
        stage('Backup Configuration') {
            steps {
                echo '=== 서버 설정 파일 백업 ==='
                sshagent(['webapp-server-ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no root@192.168.1.112 '
                            # 백업 디렉토리 생성
                            mkdir -p /var/www/webapp/config-backup
                            
                            # 타임스탬프
                            TIMESTAMP=$(date +%Y%m%d_%H%M%S)
                            
                            # .env.production 백업
                            if [ -f /var/www/webapp/.env.production ]; then
                                echo "✅ .env.production 백업: /var/www/webapp/config-backup/.env.production.${TIMESTAMP}"
                                cp /var/www/webapp/.env.production /var/www/webapp/config-backup/.env.production.${TIMESTAMP}
                            else
                                echo "⚠️  경고: .env.production 파일이 서버에 없습니다!"
                                echo "   경로: /var/www/webapp/.env.production"
                            fi
                            
                            # application.properties 백업 (있는 경우)
                            if [ -f /var/www/webapp/application.properties ]; then
                                echo "✅ application.properties 백업: /var/www/webapp/config-backup/application.properties.${TIMESTAMP}"
                                cp /var/www/webapp/application.properties /var/www/webapp/config-backup/application.properties.${TIMESTAMP}
                            fi
                            
                            # 오래된 백업 삭제 (30일 이상)
                            echo "🗑️  30일 이상된 백업 파일 삭제 중..."
                            find /var/www/webapp/config-backup -name ".env.production.*" -mtime +30 -delete
                            find /var/www/webapp/config-backup -name "application.properties.*" -mtime +30 -delete
                        '
                    '''
                }
            }
        }
        
        stage('Deploy to Server') {
            steps {
                echo '=== 서버에 배포 시작 ==='
                sshagent(['webapp-server-ssh']) {
                    sh '''
                        # 필수 설정 파일 존재 여부 사전 확인
                        echo "📋 필수 파일 확인 중..."
                        ssh -o StrictHostKeyChecking=no root@192.168.1.112 '
                            if [ ! -f /var/www/webapp/.env.production ]; then
                                echo "❌ 오류: .env.production 파일이 없습니다!"
                                echo "   경로: /var/www/webapp/.env.production"
                                echo ""
                                echo "📝 파일 생성 방법:"
                                echo "   1. ssh root@192.168.1.112"
                                echo "   2. cd /var/www/webapp"
                                echo "   3. nano .env.production"
                                echo "   4. 필수 환경 변수 입력 후 저장"
                                exit 1
                            else
                                echo "✅ .env.production 파일 존재 확인"
                            fi
                        '
                        
                        # 기존 JAR 백업
                        echo "💾 기존 JAR 파일 백업 중..."
                        ssh root@192.168.1.112 '
                            mkdir -p /var/www/webapp/backup
                            if [ -f /var/www/webapp/webapp-1.0.0.jar ]; then
                                TIMESTAMP=$(date +%Y%m%d_%H%M%S)
                                cp /var/www/webapp/webapp-1.0.0.jar /var/www/webapp/backup/webapp-1.0.0.jar.${TIMESTAMP}
                                echo "✅ 백업 완료: /var/www/webapp/backup/webapp-1.0.0.jar.${TIMESTAMP}"
                                
                                # 오래된 JAR 백업 삭제 (7일 이상)
                                find /var/www/webapp/backup -name "webapp-1.0.0.jar.*" -mtime +7 -delete
                            fi
                        '
                        
                        # 새 JAR 파일만 배포
                        echo "📦 새 JAR 파일 배포 중..."
                        scp target/webapp-1.0.0.jar root@192.168.1.112:/var/www/webapp/
                        echo "✅ JAR 파일 배포 완료"
                        
                        # 애플리케이션 재시작
                        echo "🔄 애플리케이션 재시작 중..."
                        ssh root@192.168.1.112 '
                            systemctl restart webapp
                            sleep 5
                            
                            # 서비스 상태 확인
                            if systemctl is-active --quiet webapp; then
                                echo "✅ webapp 서비스 정상 실행 중"
                            else
                                echo "❌ webapp 서비스 시작 실패!"
                                systemctl status webapp
                                exit 1
                            fi
                        '
                    '''
                }
            }
        }
        
        stage('Health Check') {
            steps {
                echo '=== 애플리케이션 상태 확인 ==='
                sshagent(['webapp-server-ssh']) {
                    sh '''
                        ssh root@192.168.1.112 '
                            echo "🏥 Health Check 시작..."
                            
                            for i in {1..15}; do
                                HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/ 2>/dev/null || echo "000")
                                
                                if [ "$HTTP_CODE" = "200" ]; then
                                    echo "✅ Health Check 성공 (HTTP $HTTP_CODE)"
                                    echo "🎉 애플리케이션이 정상적으로 실행 중입니다!"
                                    exit 0
                                fi
                                
                                echo "⏳ 대기중... [$i/15] (HTTP $HTTP_CODE)"
                                sleep 3
                            done
                            
                            echo "❌ Health Check 실패!"
                            echo "📋 서비스 로그:"
                            journalctl -u webapp -n 50 --no-pager
                            exit 1
                        '
                    '''
                }
            }
        }
        
        stage('Verify Configuration') {
            steps {
                echo '=== 배포 후 설정 확인 ==='
                sshagent(['webapp-server-ssh']) {
                    sh '''
                        ssh root@192.168.1.112 '
                            echo "📊 배포 상태 요약"
                            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                            echo "📁 배포 경로: /var/www/webapp"
                            echo "📦 JAR 파일: $(ls -lh /var/www/webapp/webapp-1.0.0.jar | awk '\''{print $9, $5}'\'')"
                            echo "⚙️  환경 설정: /var/www/webapp/.env.production"
                            echo "🔧 서비스 상태: $(systemctl is-active webapp)"
                            echo "💾 최근 백업: $(ls -t /var/www/webapp/backup/webapp-1.0.0.jar.* 2>/dev/null | head -1 | xargs basename)"
                            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                        '
                    '''
                }
            }
        }
    }
    
    post {
        success {
            echo '🎉 배포 성공!'
            script {
                def deployTime = new Date().format('yyyy-MM-dd HH:mm:ss')
                def discordSuccessMessage = """
                    {
                      "username": "Jenkins Pipeline",
                      "avatar_url": "https://www.jenkins.io/images/logos/jenkins/jenkins.png",
                      "embeds": [
                        {
                          "title": "✅ 배포 성공: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                          "description": "🎉 애플리케이션이 정상적으로 실행 중입니다!",
                          "color": 65280,
                          "fields": [
                            {
                              "name": "📋 빌드 정보",
                              "value": "• 프로젝트: ${env.JOB_NAME}\\n• 빌드 번호: ${env.BUILD_NUMBER}\\n• 빌드 URL: ${env.BUILD_URL}"
                            },
                            {
                              "name": "🚀 배포 정보",
                              "value": "• 배포 서버: ${DEPLOY_SERVER}\\n• 배포 경로: ${DEPLOY_PATH}\\n• JAR 파일: ${JAR_NAME}\\n• 배포 시간: ${deployTime}"
                            }
                          ],
                          "footer": {
                            "text": "Jenkins CI/CD"
                          },
                          "timestamp": "${deployTime}"
                        }
                      ]
                    }
                """
                sh "curl -H \"Content-Type: application/json\" -X POST -d '${discordSuccessMessage}' ${DISCORD_WEBHOOK_URL}"
            }
        }
        failure {
            echo '❌ 배포 실패!'
            script {
                def deployTime = new Date().format('yyyy-MM-dd HH:mm:ss')
                def discordFailureMessage = """
                    {
                      "username": "Jenkins Pipeline",
                      "avatar_url": "https://www.jenkins.io/images/logos/jenkins/jenkins.png",
                      "embeds": [
                        {
                          "title": "❌ 배포 실패: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                          "description": "배포 중 오류가 발생했습니다. 자세한 내용은 빌드 로그를 확인하세요.",
                          "color": 16711680,
                          "fields": [
                            {
                              "name": "📋 빌드 정보",
                              "value": "• 프로젝트: ${env.JOB_NAME}\\n• 빌드 번호: ${env.BUILD_NUMBER}\\n• 빌드 URL: ${env.BUILD_URL}"
                            },
                            {
                              "name": "🔍 일반적인 원인 및 복구 방법",
                              "value": "1. .env.production 파일 누락\\n   → 경로: ${DEPLOY_PATH}/.env.production\\n\\n2. Maven 빌드 오류\\n   → 로그 확인 필요\\n\\n3. Health Check 실패\\n   → journalctl -u webapp -n 100\\n\\n4. 배포 서버 연결 실패\\n   → SSH 연결 확인\\n\\n5. 데이터베이스 연결 오류\\n   → .env.production의 DB 설정 확인\\n\\n📝 복구 방법\\nssh root@${DEPLOY_SERVER}\\ncd ${DEPLOY_PATH}\\nls -lh backup/  # 백업 파일 확인\\ncp backup/${JAR_NAME}.YYYYMMDD_HHMMSS ${JAR_NAME}\\nsystemctl restart webapp"
                            }
                          ],
                          "footer": {
                            "text": "Jenkins CI/CD"
                          },
                          "timestamp": "${deployTime}"
                        }
                      ]
                    }
                """
                sh "curl -H \"Content-Type: application/json\" -X POST -d '${discordFailureMessage}' ${DISCORD_WEBHOOK_URL}"
            }
        always {
            echo '=== 빌드 완료 ==='
            cleanWs()
        }
    }
}