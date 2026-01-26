package com.webapp.controller;

import com.webapp.service.GitHubApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * GitHub 저장소 뷰어 컨트롤러
 * - 공통 서비스를 활용하여 중복 코드 제거
 * - 일관된 에러 처리 및 응답 구조
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/github-viewer")
public class GitHubViewerController {
    
    private static final String OWNER = "hsm0711";
    private static final String REPO = "toy";
    private static final String ERROR_VIEW = "github-viewer-error";
    private static final String VIEWER_VIEW = "github-viewer";
    
    // 에러 메시지 상수
    private static final String ERROR_TOKEN_REQUIRED = "Access denied: Token required";
    private static final String ERROR_INVALID_TOKEN = "Access denied: Invalid token";
    private static final String ERROR_API_CALL_FAILED = "Failed to retrieve data from GitHub";
    
    private final GitHubApiService gitHubApiService;
    
    /**
     * GitHub 소스 뷰어 메인 페이지
     */
    @GetMapping
    public String viewerPage(@RequestParam(required = false) String token, Model model) {
        if (token == null || token.trim().isEmpty()) {
            return renderError(model, ERROR_TOKEN_REQUIRED);
        }
        
        if (!gitHubApiService.verifyToken(token)) {
            return renderError(model, ERROR_INVALID_TOKEN);
        }
        
        model.addAttribute("owner", OWNER);
        model.addAttribute("repo", REPO);
        model.addAttribute("token", token);
        return VIEWER_VIEW;
    }
    
    /**
     * API: 저장소 구조 조회
     */
    @GetMapping("/api/structure")
    @ResponseBody
    public ResponseEntity<?> getRepoStructure(
            @RequestParam String token,
            @RequestParam(defaultValue = "") String path) {
        
        if (!gitHubApiService.verifyToken(token)) {
            return ResponseEntity.status(403).body(createErrorResponse(ERROR_INVALID_TOKEN));
        }
        
        try {
            List<Map<String, Object>> contents = gitHubApiService.getContents(OWNER, REPO, path, token);
            return ResponseEntity.ok(contents);
        } catch (Exception e) {
            log.error("Failed to get repo structure for path: {}", path, e);
            return ResponseEntity.status(500).body(createErrorResponse(ERROR_API_CALL_FAILED));
        }
    }
    
    /**
     * API: 파일 내용 조회
     */
    @GetMapping("/api/file")
    @ResponseBody
    public ResponseEntity<?> getFileContent(
            @RequestParam String token,
            @RequestParam String path) {
        
        if (!gitHubApiService.verifyToken(token)) {
            return ResponseEntity.status(403).body(createErrorResponse(ERROR_INVALID_TOKEN));
        }
        
        try {
            Map<String, Object> fileData = gitHubApiService.getFileContent(OWNER, REPO, path, token);
            
            // Base64 디코딩
            String content = (String) fileData.get("content");
            if (content != null) {
                String decodedContent = gitHubApiService.decodeContent(content);
                fileData.put("decoded_content", decodedContent);
            }
            
            return ResponseEntity.ok(fileData);
        } catch (Exception e) {
            log.error("Failed to get file content for path: {}", path, e);
            return ResponseEntity.status(500).body(createErrorResponse(ERROR_API_CALL_FAILED));
        }
    }
    
    /**
     * 간단한 텍스트 뷰 (Claude가 읽기 쉬운 형식)
     */
    @GetMapping("/text-view")
    @ResponseBody
    public ResponseEntity<String> getTextView(@RequestParam String token) {
        if (!gitHubApiService.verifyToken(token)) {
            return ResponseEntity.status(403)
                .body("Access denied: Invalid or missing token");
        }
        
        try {
            String textView = generateTextView(token);
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(textView);
        } catch (Exception e) {
            log.error("Failed to generate text view", e);
            return ResponseEntity.status(500)
                .body("Error: " + e.getMessage());
        }
    }
    
    // ========== Private Helper Methods ==========
    
    /**
     * 에러 뷰 렌더링
     */
    private String renderError(Model model, String errorMessage) {
        model.addAttribute("error", errorMessage);
        return ERROR_VIEW;
    }
    
    /**
     * 에러 응답 생성
     */
    private Map<String, String> createErrorResponse(String message) {
        return Map.of("error", message);
    }
    
    /**
     * 텍스트 뷰 생성 (전체 저장소 요약)
     */
    private String generateTextView(String token) {
        StringBuilder sb = new StringBuilder();
        
        // 헤더
        sb.append("=== GitHub Repository: ").append(OWNER).append("/").append(REPO).append(" ===\n\n");
        
        // 저장소 정보
        appendRepositoryInfo(sb, token);
        
        // 최근 커밋
        appendRecentCommits(sb, token);
        
        // 파일 구조
        sb.append("\n=== File Structure ===\n");
        appendFileTree(sb, "", 0, token);
        
        // 소스 파일들
        sb.append("\n=== Java Source Files ===\n\n");
        appendJavaFiles(sb, "src/main/java", token);
        
        sb.append("\n=== Configuration Files ===\n\n");
        appendConfigFiles(sb, token);
        
        sb.append("\n=== HTML Templates ===\n\n");
        appendHtmlFiles(sb, "src/main/resources/templates", token);
        
        return sb.toString();
    }
    
    /**
     * 저장소 정보 추가
     */
    private void appendRepositoryInfo(StringBuilder sb, String token) {
        Map<String, Object> repoInfo = gitHubApiService.getRepositoryInfo(OWNER, REPO, token);
        sb.append("Description: ").append(repoInfo.get("description")).append("\n");
        sb.append("Default Branch: ").append(repoInfo.get("default_branch")).append("\n\n");
    }
    
    /**
     * 최근 커밋 정보 추가
     */
    private void appendRecentCommits(StringBuilder sb, String token) {
        sb.append("=== Recent Commits ===\n");
        List<Map<String, Object>> commits = gitHubApiService.getRecentCommits(OWNER, REPO, token, 5);
        
        for (Map<String, Object> commit : commits) {
            Map<String, Object> commitInfo = (Map<String, Object>) commit.get("commit");
            String sha = commit.get("sha").toString().substring(0, 7);
            String message = (String) commitInfo.get("message");
            sb.append("- ").append(sha).append(": ").append(message.split("\n")[0]).append("\n");
        }
    }
    
    /**
     * 파일 트리 추가 (재귀)
     */
    private void appendFileTree(StringBuilder sb, String path, int depth, String token) {
        try {
            List<Map<String, Object>> items = gitHubApiService.getContents(OWNER, REPO, path, token);
            String indent = "  ".repeat(depth);
            
            for (Map<String, Object> item : items) {
                String name = (String) item.get("name");
                String type = (String) item.get("type");
                
                if (shouldSkipItem(name)) {
                    continue;
                }
                
                sb.append(indent)
                    .append(type.equals("dir") ? "📁 " : "📄 ")
                    .append(name)
                    .append("\n");
                
                if (type.equals("dir") && depth < 3) {
                    String newPath = path.isEmpty() ? name : path + "/" + name;
                    appendFileTree(sb, newPath, depth + 1, token);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get file tree for path: {}", path, e);
        }
    }
    
    /**
     * Java 파일 내용 추가
     */
    private void appendJavaFiles(StringBuilder sb, String path, String token) {
        try {
            List<Map<String, Object>> items = gitHubApiService.getContents(OWNER, REPO, path, token);
            
            for (Map<String, Object> item : items) {
                String name = (String) item.get("name");
                String type = (String) item.get("type");
                String itemPath = (String) item.get("path");
                
                if (type.equals("dir")) {
                    appendJavaFiles(sb, itemPath, token);
                } else if (name.endsWith(".java")) {
                    appendFileContent(sb, itemPath, token);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to append Java files from path: {}", path, e);
        }
    }
    
    /**
     * 설정 파일 내용 추가
     */
    private void appendConfigFiles(StringBuilder sb, String token) {
        String[] configFiles = {
            "pom.xml",
            "src/main/resources/application.properties",
            "src/main/resources/application-prod.properties"
        };
        
        for (String path : configFiles) {
            appendFileContent(sb, path, token);
        }
    }
    
    /**
     * HTML 파일 내용 추가
     */
    private void appendHtmlFiles(StringBuilder sb, String path, String token) {
        try {
            List<Map<String, Object>> items = gitHubApiService.getContents(OWNER, REPO, path, token);
            
            for (Map<String, Object> item : items) {
                String name = (String) item.get("name");
                String type = (String) item.get("type");
                String itemPath = (String) item.get("path");
                
                if (type.equals("file") && name.endsWith(".html")) {
                    appendFileContent(sb, itemPath, token);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to append HTML files from path: {}", path, e);
        }
    }
    
    /**
     * 파일 내용 추가
     */
    private void appendFileContent(StringBuilder sb, String path, String token) {
        try {
            sb.append("\n━━━ ").append(path).append(" ━━━\n");
            Map<String, Object> fileData = gitHubApiService.getFileContent(OWNER, REPO, path, token);
            String content = (String) fileData.get("content");
            String decodedContent = gitHubApiService.decodeContent(content);
            sb.append(decodedContent).append("\n");
        } catch (Exception e) {
            log.debug("File not found or error reading: {}", path);
        }
    }
    
    /**
     * 제외할 항목 판단
     */
    private boolean shouldSkipItem(String name) {
        return name.equals("target") || 
               name.equals(".git") || 
               name.equals("node_modules");
    }
}