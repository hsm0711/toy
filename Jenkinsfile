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
                    sh """
                        ssh -o StrictHostKeyChecking=no root@${DEPLOY_SERVER} '
                            # 백업 디렉토리 생성
                            mkdir -p ${CONFIG_BACKUP_DIR}
                            
                            # 타임스탬프
                            TIMESTAMP=\$(date +%Y%m%d_%H%M%S)
                            
                            # .env.production 백업
                            if [ -f ${DEPLOY_PATH}/.env.production ]; then
                                echo "✅ .env.production 백업: ${CONFIG_BACKUP_DIR}/.env.production.\${TIMESTAMP}"
                                cp ${DEPLOY_PATH}/.env.production ${CONFIG_BACKUP_DIR}/.env.production.\${TIMESTAMP}
                            else
                                echo "⚠️  경고: .env.production 파일이 서버에 없습니다!"
                                echo "   경로: ${DEPLOY_PATH}/.env.production"
                            fi
                            
                            # application.properties 백업 (있는 경우)
                            if [ -f ${DEPLOY_PATH}/application.properties ]; then
                                echo "✅ application.properties 백업: ${CONFIG_BACKUP_DIR}/application.properties.\${TIMESTAMP}"
                                cp ${DEPLOY_PATH}/application.properties ${CONFIG_BACKUP_DIR}/application.properties.\${TIMESTAMP}
                            fi
                            
                            # 오래된 백업 삭제 (30일 이상)
                            echo "🗑️  30일 이상된 백업 파일 삭제 중..."
                            find ${CONFIG_BACKUP_DIR} -name "*.env.production.*" -mtime +30 -delete
                            find ${CONFIG_BACKUP_DIR} -name "*.application.properties.*" -mtime +30 -delete
                        '
                    """
                }
            }
        }
        
        stage('Deploy to Server') {
            steps {
                echo '=== 서버에 배포 시작 ==='
                sshagent(['webapp-server-ssh']) {
                    sh """
                        # 필수 설정 파일 존재 여부 사전 확인
                        echo "📋 필수 파일 확인 중..."
                        ssh -o StrictHostKeyChecking=no root@${DEPLOY_SERVER} '
                            if [ ! -f ${DEPLOY_PATH}/.env.production ]; then
                                echo "❌ 오류: .env.production 파일이 없습니다!"
                                echo "   경로: ${DEPLOY_PATH}/.env.production"
                                echo ""
                                echo "📝 파일 생성 방법:"
                                echo "   1. ssh root@${DEPLOY_SERVER}"
                                echo "   2. cd ${DEPLOY_PATH}"
                                echo "   3. nano .env.production"
                                echo "   4. 필수 환경 변수 입력 후 저장"
                                exit 1
                            else
                                echo "✅ .env.production 파일 존재 확인"
                            fi
                        '
                        
                        # 기존 JAR 백업
                        echo "💾 기존 JAR 파일 백업 중..."
                        ssh root@${DEPLOY_SERVER} '
                            mkdir -p ${DEPLOY_PATH}/backup
                            if [ -f ${DEPLOY_PATH}/${JAR_NAME} ]; then
                                TIMESTAMP=\$(date +%Y%m%d_%H%M%S)
                                cp ${DEPLOY_PATH}/${JAR_NAME} ${DEPLOY_PATH}/backup/${JAR_NAME}.\${TIMESTAMP}
                                echo "✅ 백업 완료: ${DEPLOY_PATH}/backup/${JAR_NAME}.\${TIMESTAMP}"
                                
                                # 오래된 JAR 백업 삭제 (7일 이상)
                                find ${DEPLOY_PATH}/backup -name "${JAR_NAME}.*" -mtime +7 -delete
                            fi
                        '
                        
                        # 새 JAR 파일만 배포
                        echo "📦 새 JAR 파일 배포 중..."
                        scp target/${JAR_NAME} root@${DEPLOY_SERVER}:${DEPLOY_PATH}/
                        echo "✅ JAR 파일 배포 완료"
                        
                        # 애플리케이션 재시작
                        echo "🔄 애플리케이션 재시작 중..."
                        ssh root@${DEPLOY_SERVER} '
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
                    """
                }
            }
        }
        
        stage('Health Check') {
            steps {
                echo '=== 애플리케이션 상태 확인 ==='
                sshagent(['webapp-server-ssh']) {
                    sh """
                        ssh root@${DEPLOY_SERVER} '
                            echo "🏥 Health Check 시작..."
                            
                            for i in {1..15}; do
                                HTTP_CODE=\$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/ 2>/dev/null || echo "000")
                                
                                if [ "\$HTTP_CODE" = "200" ]; then
                                    echo "✅ Health Check 성공 (HTTP \$HTTP_CODE)"
                                    echo "🎉 애플리케이션이 정상적으로 실행 중입니다!"
                                    exit 0
                                fi
                                
                                echo "⏳ 대기중... [\$i/15] (HTTP \$HTTP_CODE)"
                                sleep 3
                            done
                            
                            echo "❌ Health Check 실패!"
                            echo "📋 서비스 로그:"
                            journalctl -u webapp -n 50 --no-pager
                            exit 1
                        '
                    """
                }
            }
        }
        
        stage('Verify Configuration') {
            steps {
                echo '=== 배포 후 설정 확인 ==='
                sshagent(['webapp-server-ssh']) {
                    sh """
                        ssh root@${DEPLOY_SERVER} '
                            echo "📊 배포 상태 요약"
                            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                            echo "📁 배포 경로: ${DEPLOY_PATH}"
                            echo "📦 JAR 파일: \$(ls -lh ${DEPLOY_PATH}/${JAR_NAME} | awk \"{print \\$9, \\$5}\")"
                            echo "⚙️  환경 설정: ${DEPLOY_PATH}/.env.production"
                            echo "🔧 서비스 상태: \$(systemctl is-active webapp)"
                            echo "💾 최근 백업: \$(ls -t ${DEPLOY_PATH}/backup/${JAR_NAME}.* 2>/dev/null | head -1 | xargs basename)"
                            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                        '
                    """
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
                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                    🎉 배포 성공!
                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                    
                    📋 빌드 정보
                    • 프로젝트: ${env.JOB_NAME}
                    • 빌드 번호: ${env.BUILD_NUMBER}
                    • 빌드 URL: ${env.BUILD_URL}
                    
                    🚀 배포 정보
                    • 배포 서버: ${DEPLOY_SERVER}
                    • 배포 경로: ${DEPLOY_PATH}
                    • JAR 파일: ${JAR_NAME}
                    • 배포 시간: ${new Date().format('yyyy-MM-dd HH:mm:ss')}
                    
                    ⚙️  설정 파일
                    • 환경 변수: ${DEPLOY_PATH}/.env.production
                    • 설정 백업: ${CONFIG_BACKUP_DIR}
                    
                    ✅ Health Check 통과
                    • 애플리케이션이 정상적으로 실행 중입니다.
                    
                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                """,
                to: 'your-email@example.com'
            )
        }
        failure {
            echo '❌ 배포 실패!'
            emailext(
                subject: "❌ 배포 실패: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                    ❌ 배포 실패!
                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                    
                    📋 빌드 정보
                    • 프로젝트: ${env.JOB_NAME}
                    • 빌드 번호: ${env.BUILD_NUMBER}
                    • 빌드 URL: ${env.BUILD_URL}
                    
                    🔍 일반적인 원인
                    1. .env.production 파일 누락
                       → 경로: ${DEPLOY_PATH}/.env.production
                    
                    2. Maven 빌드 오류
                       → 로그 확인 필요
                    
                    3. Health Check 실패
                       → journalctl -u webapp -n 100
                    
                    4. 배포 서버 연결 실패
                       → SSH 연결 확인
                    
                    5. 데이터베이스 연결 오류
                       → .env.production의 DB 설정 확인
                    
                    📝 복구 방법
                    ssh root@${DEPLOY_SERVER}
                    cd ${DEPLOY_PATH}
                    ls -lh backup/  # 백업 파일 확인
                    cp backup/${JAR_NAME}.YYYYMMDD_HHMMSS ${JAR_NAME}
                    systemctl restart webapp
                    
                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
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