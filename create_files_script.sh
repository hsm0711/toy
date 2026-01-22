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
EOF

echo "✓ README.md 생성"

# pom.xml 생성
cat > pom.xml << 'EOFPOM'
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
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.apache.pdfbox</groupId>
            <artifactId>pdfbox</artifactId>
            <version>3.0.1</version>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>

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
EOFPOM

echo "✓ pom.xml 생성"

# Jenkinsfile 생성
cat > Jenkinsfile << 'EOFJENKINS'
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
EOFJENKINS

echo "✓ Jenkinsfile 생성"

# deploy.sh 생성
cat > deploy.sh << 'EOFDEPLOY'
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
EOFDEPLOY

chmod +x deploy.sh
echo "✓ deploy.sh 생성"

# Java 파일들 생성
cat > src/main/java/com/webapp/WebappApplication.java << 'EOFJAVA1'
package com.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebappApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebappApplication.class, args);
    }
}
EOFJAVA1

echo "✓ WebappApplication.java 생성"

cat > src/main/java/com/webapp/model/Menu.java << 'EOFJAVA2'
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
EOFJAVA2

echo "✓ Menu.java 생성"

cat > src/main/java/com/webapp/repository/MenuRepository.java << 'EOFJAVA3'
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
EOFJAVA3

echo "✓ MenuRepository.java 생성"

cat > src/main/java/com/webapp/service/MenuService.java << 'EOFJAVA4'
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
EOFJAVA4

echo "✓ MenuService.java 생성"

cat > src/main/java/com/webapp/service/PdfService.java << 'EOFJAVA5'
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
EOFJAVA5

echo "✓ PdfService.java 생성"

cat > src/main/java/com/webapp/controller/HomeController.java << 'EOFJAVA6'
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
EOFJAVA6

echo "✓ HomeController.java 생성"

cat > src/main/java/com/webapp/controller/PdfMergeController.java << 'EOFJAVA7'
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
EOFJAVA7

echo "✓ PdfMergeController.java 생성"

# application.properties 생성
cat > src/main/resources/application.properties << 'EOFPROP1'
server.port=${SERVER_PORT:8080}

spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:webapp_db}
spring.datasource.username=${DB_USER:webapp_user}
spring.datasource.password=${DB_PASSWORD:}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=${MAX_FILE_SIZE:50MB}
spring.servlet.multipart.max-request-size=${MAX_REQUEST_SIZE:50MB}

app.upload.dir=${UPLOAD_DIR:/var/www/webapp/uploads}

spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

logging.level.org.springframework.web=INFO
logging.level.com.webapp=DEBUG
EOFPROP1

echo "✓ application.properties 생성"

# application-prod.properties 생성
cat > src/main/resources/application-prod.properties << 'EOFPROP2'
server.port=8080
server.compression.enabled=true

spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:webapp_db}
spring.datasource.username=${DB_USER:webapp_user}
spring.datasource.password=${DB_PASSWORD}

spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

logging.level.root=INFO
logging.level.com.webapp=INFO
logging.level.org.springframework.web=WARN

spring.thymeleaf.cache=true
EOFPROP2

echo "✓ application-prod.properties 생성"

# HTML/CSS/JS 파일 생성은 다음 스크립트에서 계속...
echo ""
echo "=== Part 1 완료! ==="
echo "이제 create-files-part2.sh를 실행하세요."