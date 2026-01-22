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
        }
        
        stage('Deploy to Server') {
            steps {
                echo '=== 서버에 배포 시작 ==='
                echo 'Jenkins SSH 설정 필요'
            }
        }
    }
    
    post {
        success {
            echo '🎉 빌드 성공!'
        }
        failure {
            echo '❌ 빌드 실패!'
        }
    }
}
