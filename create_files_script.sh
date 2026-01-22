#!/bin/bash
# create-files.sh - toy 프로젝트 파일 자동 생성 스크립트
# 사용법: toy 폴더에서 ./create-files.sh 실행

set -e

echo "=== 프로젝트 파일 생성 시작 ==="

# .gitignore 생성
cat > .gitignore << 'EOF'
# Maven
target/
!.mvn/wrapper/maven-wrapper.jar
!**/src/main/**/target/
!**/src/test/**/target/

# IDE
.idea/
*.iws
*.iml
*.ipr
.vscode/
.classpath
.project
.settings/

# Eclipse
.metadata
bin/
tmp/
*.tmp
*.bak
*.swp
*~.nib

# Spring Boot
.springBeans
spring-boot-*.log

# Logs
logs/
*.log
*.log.*

# OS
.DS_Store
Thumbs.db

# Environment
.env
*.env

# Upload files
uploads/
static/uploads/

# Build output
out/
build/

# Package Files
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar
EOF

echo "✓ .gitignore 생성"

# README.md 생성
cat > README.md << 'EOF'
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
EOF

echo "✓ README.md 생성"

# pom.xml 생성
cat > pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <groupId>com.webapp</groupId>
    <artifactId>webapp</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starter Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Starter Thymeleaf -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>

        <!-- Spring Boot Starter Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- PostgreSQL Driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Apache PDFBox (PDF 처리) -->
        <dependency>
            <groupId>org.apache.pdfbox</groupId>
            <artifactId>pdfbox</artifactId>
            <version>3.0.1</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Spring Boot DevTools -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
EOF

echo "✓ pom.xml 생성"

# Jenkinsfile 생성
cat > Jenkinsfile << 'EOF'
pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }
    
    environment {
        APP_NAME = 'webapp'
        DEPLOY_SERVER = '192.168.1.100'
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
                    junit '**/target/surefire-reports/*.xml'
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
                        
                        scp target/${JAR_NAME} root@${DEPLOY_SERVER}:${DEPLOY_PATH}/
                        
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
EOF

echo "✓ Jenkinsfile 생성"

# deploy.sh 생성
cat > deploy.sh << 'EOF'
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
EOF

chmod +x deploy.sh
echo "✓ deploy.sh 생성"

# WebappApplication.java 생성
cat > src/main/java/com/webapp/WebappApplication.java << 'EOF'
package com.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebappApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebappApplication.class, args);
    }
}
EOF

echo "✓ WebappApplication.java 생성"

# Menu.java 생성
cat > src/main/java/com/webapp/model/Menu.java << 'EOF'
package com.webapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "menus")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Menu {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, length = 200)
    private String path;
    
    @Column(length = 50)
    private String icon;
    
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
EOF

echo "✓ Menu.java 생성"

# MenuRepository.java 생성
cat > src/main/java/com/webapp/repository/MenuRepository.java << 'EOF'
package com.webapp.repository;

import com.webapp.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    
    List<Menu> findByIsActiveTrueOrderByDisplayOrderAsc();
    
    List<Menu> findAllByOrderByDisplayOrderAsc();
}
EOF

echo "✓ MenuRepository.java 생성"

# MenuService.java 생성
cat > src/main/java/com/webapp/service/MenuService.java << 'EOF'
package com.webapp.service;

import com.webapp.model.Menu;
import com.webapp.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {
    
    private final MenuRepository menuRepository;
    
    public List<Menu> getActiveMenus() {
        return menuRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }
    
    public List<Menu> getAllMenus() {
        return menuRepository.findAllByOrderByDisplayOrderAsc();
    }
    
    public Optional<Menu> getMenuById(Long id) {
        return menuRepository.findById(id);
    }
    
    @Transactional
    public Menu createMenu(Menu menu) {
        return menuRepository.save(menu);
    }
    
    @Transactional
    public Menu updateMenu(Long id, Menu menuDetails) {
        Menu menu = menuRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Menu not found"));
        
        menu.setName(menuDetails.getName());
        menu.setPath(menuDetails.getPath());
        menu.setIcon(menuDetails.getIcon());
        menu.setDisplayOrder(menuDetails.getDisplayOrder());
        menu.setIsActive(menuDetails.getIsActive());
        
        return menuRepository.save(menu);
    }
    
    @Transactional
    public void deleteMenu(Long id) {
        menuRepository.deleteById(id);
    }
}
EOF

echo "✓ MenuService.java 생성"

# PdfService.java 생성 (계속...)
cat > src/main/java/com/webapp/service/PdfService.java << 'EOF'
package com.webapp.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PdfService {
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    public String mergePdfFiles(List<MultipartFile> files, List<Integer> order) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        List<File> tempFiles = new ArrayList<>();
        
        try {
            for (MultipartFile file : files) {
                String tempFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                File tempFile = new File(uploadPath.toFile(), tempFileName);
                file.transferTo(tempFile);
                tempFiles.add(tempFile);
            }
            
            PDFMergerUtility pdfMerger = new PDFMergerUtility();
            
            if (order != null && !order.isEmpty()) {
                for (Integer index : order) {
                    if (index >= 0 && index < tempFiles.size()) {
                        pdfMerger.addSource(tempFiles.get(index));
                    }
                }
            } else {
                for (File file : tempFiles) {
                    pdfMerger.addSource(file);
                }
            }
            
            String mergedFileName = "merged_" + System.currentTimeMillis() + ".pdf";
            File mergedFile = new File(uploadPath.toFile(), mergedFileName);
            pdfMerger.setDestinationFileName(mergedFile.getAbsolutePath());
            pdfMerger.mergeDocuments(null);
            
            log.info("PDF 병합 완료: {}", mergedFile.getAbsolutePath());
            
            return mergedFileName;
            
        } catch (Exception e) {
            log.error("PDF 병합 중 오류 발생", e);
            throw new IOException("PDF 병합 중 오류가 발생했습니다: " + e.getMessage());
        } finally {
            for (File tempFile : tempFiles) {
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        }
    }
    
    public boolean isPdfFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        
        return (contentType != null && contentType.equals("application/pdf")) ||
               (fileName != null && fileName.toLowerCase().endsWith(".pdf"));
    }
    
    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir, fileName);
            Files.deleteIfExists(filePath);
            log.info("파일 삭제: {}", fileName);
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", fileName, e);
        }
    }
}
EOF

echo "✓ PdfService.java 생성"

# HomeController.java 생성
cat > src/main/java/com/webapp/controller/HomeController.java << 'EOF'
package com.webapp.controller;

import com.webapp.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    
    private final MenuService menuService;
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "home");
        return "index";
    }
}
EOF

echo "✓ HomeController.java 생성"

# PdfMergeController.java 생성
cat > src/main/java/com/webapp/controller/PdfMergeController.java << 'EOF'
package com.webapp.controller;

import com.webapp.service.MenuService;
import com.webapp.service.PdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/pdf-merge")
@RequiredArgsConstructor
public class PdfMergeController {
    
    private final MenuService menuService;
    private final PdfService pdfService;
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    @GetMapping
    public String pdfMergePage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "pdf-merge");
        return "pdf-merge";
    }
    
    @PostMapping("/merge")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mergePdfs(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "order", required = false) List<Integer> order) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (files == null || files.isEmpty()) {
                response.put("success", false);
                response.put("message", "파일을 선택해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            for (MultipartFile file : files) {
                if (!pdfService.isPdfFile(file)) {
                    response.put("success", false);
                    response.put("message", "PDF 파일만 업로드 가능합니다: " + file.getOriginalFilename());
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            String mergedFileName = pdfService.mergePdfFiles(files, order);
            
            response.put("success", true);
            response.put("message", "PDF 병합이 완료되었습니다.");
            response.put("fileName", mergedFileName);
            response.put("downloadUrl", "/pdf-merge/download/" + mergedFileName);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("PDF 병합 오류", e);
            response.put("success", false);
            response.put("message", "PDF 병합 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadPdf(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            Resource resource = new FileSystemResource(filePath);
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("파일 다운로드 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
EOF

echo "✓ PdfMergeController.java 생성"

# application.properties 생성
cat > src/main/resources/application.properties << 'EOF'
# Server Configuration
server.port=${SERVER_PORT:8080}

# Database Configuration
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:webapp_db}
spring.datasource.username=${DB_USER:webapp_user}
spring.datasource.password=${DB_PASSWORD:}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# File Upload Configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=${MAX_FILE_SIZE:50MB}
spring.servlet.multipart.max-request-size=${MAX_REQUEST_SIZE:50MB}

# Upload Directory
app.upload.dir=${UPLOAD_DIR:/var/www/webapp/uploads}

# Thymeleaf Configuration
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# Logging
logging.level.org.springframework.web=INFO
logging.level.com.webapp=DEBUG
EOF

echo "✓ application.properties 생성"

# application-prod.properties 생성
cat > src/main/resources/application-prod.properties << 'EOF'
# Production Profile Configuration

# Server Configuration
server.port=8080
server.compression.enabled=true

# Database Configuration - Production
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:webapp_db}
spring.datasource.username=${DB_USER:webapp_user}
spring.datasource.password=${DB_PASSWORD}

# Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

#