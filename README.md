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

## 로컬 개발 환경 실행

```bash
mvn clean install
mvn spring-boot:run
```

애플리케이션은 `http://localhost:8080`에서 실행됩니다.

## 배포

Jenkins 파이프라인을 통해 자동 배포됩니다.

## 라이센스

MIT License

test


