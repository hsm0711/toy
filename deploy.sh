#!/bin/bash
# deploy.sh - 서버 측 배포 스크립트

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

mkdir -p $UPLOAD_DIR
chmod 755 $UPLOAD_DIR
echo "✓ 업로드 디렉토리 생성: $UPLOAD_DIR"

echo "애플리케이션 중지 중..."
if systemctl is-active --quiet $SERVICE_NAME; then
    systemctl stop $SERVICE_NAME
    echo "✓ 애플리케이션 중지됨"
else
    echo "⚠ 애플리케이션이 실행 중이 아닙니다"
fi

echo "애플리케이션 시작 중..."
systemctl daemon-reload
systemctl start $SERVICE_NAME
systemctl enable $SERVICE_NAME

sleep 5

if systemctl is-active --quiet $SERVICE_NAME; then
    echo "✅ 애플리케이션이 성공적으로 시작되었습니다"
    systemctl status $SERVICE_NAME --no-pager
else
    echo "❌ 애플리케이션 시작 실패"
    systemctl status $SERVICE_NAME --no-pager
    exit 1
fi

echo ""
echo "=== 최근 로그 ==="
journalctl -u $SERVICE_NAME -n 20 --no-pager

echo ""
echo "=== 배포 완료 ==="
echo "URL: http://localhost:8080"
