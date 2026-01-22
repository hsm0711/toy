#!/bin/bash
set -e

APP_NAME="webapp"
APP_DIR="/var/www/webapp"
JAR_NAME="webapp-1.0.0.jar"
SERVICE_NAME="webapp"

echo "=== $APP_NAME 배포 시작 ==="

cd $APP_DIR

if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
    echo "✓ 환경변수 로드됨"
fi

echo "애플리케이션 재시작 중..."
systemctl restart $SERVICE_NAME

echo "=== 배포 완료 ==="
