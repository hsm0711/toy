# Web Application - PDF Merge Tool

Spring Boot 기반의 PDF 병합 웹 애플리케이션입니다.

## 기술 스택

- **Backend**: Spring Boot 3.2.0, Java 17
- **Database**: PostgreSQL
- **Build Tool**: Maven
- **Web Server**: Nginx
- **CI/CD**: Jenkins
- **Version Control**: Git

## 주요 기능

- 📄 **PDF 병합**: 여러 PDF 파일을 하나로 병합
- 🔄 **순서 조정**: 드래그 앤 드롭으로 PDF 순서 변경
- 📊 **메뉴 관리**: PostgreSQL 기반 동적 메뉴 관리

## 프로젝트 구조

```
webapp/
├── src/
│   ├── main/
│   │   ├── java/com/webapp/
│   │   │   ├── WebappApplication.java
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── model/
│   │   │   └── config/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       └── templates/
│   └── test/
├── pom.xml
├── Jenkinsfile
└── README.md
```

## 환경 설정

### 필수 요구사항

- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Nginx

### 환경변수 설정

`.env` 파일을 프로젝트 루트에 생성:

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=webapp_db
DB_USER=webapp_user
DB_PASSWORD=your_password
SERVER_PORT=8080
UPLOAD_DIR=/var/www/webapp/uploads
```

### 데이터베이스 초기화

```sql
CREATE DATABASE webapp_db;
CREATE USER webapp_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE webapp_db TO webapp_user;
```

## 로컬 개발 환경 실행

```bash
# 의존성 설치 및 빌드
mvn clean install

# 애플리케이션 실행
mvn spring-boot:run

# 또는 JAR 파일 실행
java -jar target/webapp-1.0.0.jar
```

애플리케이션은 `http://localhost:8080`에서 실행됩니다.

## 배포

Jenkins 파이프라인을 통해 자동 배포됩니다.

```bash
# 수동 배포
./deploy.sh
```

## API 엔드포인트

- `GET /` - 홈페이지
- `GET /pdf-merge` - PDF 병합 페이지
- `POST /pdf-merge/merge` - PDF 병합 API
- `GET /pdf-merge/download/{fileName}` - PDF 다운로드

## 테스트

```bash
mvn test
```

## 라이센스

MIT License

## 기여자

- hsm0711
