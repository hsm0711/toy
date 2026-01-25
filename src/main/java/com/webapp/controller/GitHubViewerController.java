package com.webapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/github-viewer")
public class GitHubViewerController {
    
    private static final String OWNER = "hsm0711";
    private static final String REPO = "toy";
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * GitHub 소스 뷰어 메인 페이지
     * Token 필수
     */
    @GetMapping
    public String viewerPage(
            @RequestParam(required = false) String token,
            Model model) {
        
        if (token == null || token.trim().isEmpty()) {
            model.addAttribute("error", "Access denied: Token required");
            return "github-viewer-error";
        }
        
        // Token 검증
        if (!verifyToken(token)) {
            model.addAttribute("error", "Access denied: Invalid token");
            return "github-viewer-error";
        }
        
        model.addAttribute("owner", OWNER);
        model.addAttribute("repo", REPO);
        model.addAttribute("token", token);
        return "github-viewer";
    }
    
    /**
     * API: 저장소 구조 조회
     */
    @GetMapping("/api/structure")
    @ResponseBody
    public ResponseEntity<?> getRepoStructure(
            @RequestParam String token,
            @RequestParam(defaultValue = "") String path) {
        
        if (!verifyToken(token)) {
            return ResponseEntity.status(403).body(Map.of("error", "Invalid token"));
        }
        
        try {
            String url = String.format(
                "https://api.github.com/repos/%s/%s/contents/%s",
                OWNER, REPO, path
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + token);
            headers.set("Accept", "application/vnd.github.v3+json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<List> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, List.class
            );
            
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Failed to get repo structure", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
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
        
        if (!verifyToken(token)) {
            return ResponseEntity.status(403).body(Map.of("error", "Invalid token"));
        }
        
        try {
            String url = String.format(
                "https://api.github.com/repos/%s/%s/contents/%s",
                OWNER, REPO, path
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + token);
            headers.set("Accept", "application/vnd.github.v3+json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class
            );
            
            Map<String, Object> fileData = response.getBody();
            
            // Base64 디코딩
            String content = (String) fileData.get("content");
            if (content != null) {
                content = new String(Base64.getDecoder().decode(
                    content.replaceAll("\\s", "")
                ));
                fileData.put("decoded_content", content);
            }
            
            return ResponseEntity.ok(fileData);
        } catch (Exception e) {
            log.error("Failed to get file content", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 간단한 텍스트 뷰 (Claude가 읽기 쉬운 형식)
     * Token 필수
     */
    @GetMapping("/text-view")
    @ResponseBody
    public ResponseEntity<String> getTextView(@RequestParam String token) {
        
        if (!verifyToken(token)) {
            return ResponseEntity.status(403)
                .body("Access denied: Invalid or missing token");
        }
        
        try {
            StringBuilder sb = new StringBuilder();
            
            sb.append("=== GitHub Repository: ").append(OWNER).append("/").append(REPO).append(" ===\n\n");
            
            // 저장소 정보
            Map<String, Object> repoInfo = getRepoInfo(token);
            sb.append("Description: ").append(repoInfo.get("description")).append("\n");
            sb.append("Default Branch: ").append(repoInfo.get("default_branch")).append("\n\n");
            
            // 최근 커밋
            sb.append("=== Recent Commits ===\n");
            List<Map<String, Object>> commits = getRecentCommits(token);
            for (int i = 0; i < Math.min(5, commits.size()); i++) {
                Map<String, Object> commit = commits.get(i);
                Map<String, Object> commitInfo = (Map<String, Object>) commit.get("commit");
                String sha = commit.get("sha").toString().substring(0, 7);
                String message = (String) commitInfo.get("message");
                sb.append("- ").append(sha).append(": ").append(message.split("\n")[0]).append("\n");
            }
            
            sb.append("\n=== File Structure ===\n");
            appendFileTree(sb, "", 0, token);
            
            // Java 파일들의 내용
            sb.append("\n=== Java Source Files ===\n\n");
            appendJavaFiles(sb, "src/main/java", token);
            
            // 설정 파일들
            sb.append("\n=== Configuration Files ===\n\n");
            appendConfigFiles(sb, token);
            
            // HTML 템플릿들
            sb.append("\n=== HTML Templates ===\n\n");
            appendHtmlFiles(sb, "src/main/resources/templates", token);

        // CSS 파일들 추가 ⭐
        sb.append("\n=== CSS Files ===\n\n");
        appendCssFiles(sb, "src/main/resources/static/css", token);
        
        // JavaScript 파일들 추가 ⭐
        sb.append("\n=== JavaScript Files ===\n\n");
        appendJsFiles(sb, "src/main/resources/static/js", token);
            
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(sb.toString());
        } catch (Exception e) {
            log.error("Failed to generate text view", e);
            return ResponseEntity.status(500)
                .body("Error: " + e.getMessage());
        }
    }
    
    // Helper methods
    
    /**
     * Token 검증
     */
    private boolean verifyToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        try {
            // GitHub API로 Token 유효성 검증
            String url = "https://api.github.com/user";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Token verification failed", e);
            return false;
        }
    }
    
    private Map<String, Object> getRepoInfo(String token) {
        String url = String.format("https://api.github.com/repos/%s/%s", OWNER, REPO);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        return response.getBody();
    }
    
    private List<Map<String, Object>> getRecentCommits(String token) {
        String url = String.format("https://api.github.com/repos/%s/%s/commits?per_page=5", OWNER, REPO);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
        return response.getBody();
    }
    
    private List<Map<String, Object>> getFileTree(String path, String token) {
        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", OWNER, REPO, path);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            return response.getBody();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    private void appendFileTree(StringBuilder sb, String path, int depth, String token) {
        List<Map<String, Object>> items = getFileTree(path, token);
        String indent = "  ".repeat(depth);
        
        for (Map<String, Object> item : items) {
            String name = (String) item.get("name");
            String type = (String) item.get("type");
            
            // target, .git 제외
            if (name.equals("target") || name.equals(".git") || name.equals("node_modules")) {
                continue;
            }
            
            sb.append(indent).append(type.equals("dir") ? "📁 " : "📄 ").append(name).append("\n");
            
            if (type.equals("dir") && depth < 3) {
                appendFileTree(sb, path.isEmpty() ? name : path + "/" + name, depth + 1, token);
            }
        }
    }
    
    private void appendJavaFiles(StringBuilder sb, String path, String token) {
        List<Map<String, Object>> items = getFileTree(path, token);
        
        for (Map<String, Object> item : items) {
            String name = (String) item.get("name");
            String type = (String) item.get("type");
            String itemPath = (String) item.get("path");
            
            if (type.equals("dir")) {
                appendJavaFiles(sb, itemPath, token);
            } else if (name.endsWith(".java")) {
                try {
                    sb.append("\n━━━ ").append(itemPath).append(" ━━━\n");
                    String content = getFileContentString(itemPath, token);
                    sb.append(content).append("\n");
                } catch (Exception e) {
                    sb.append("Error reading file: ").append(e.getMessage()).append("\n");
                }
            }
        }
    }
    
    private void appendConfigFiles(StringBuilder sb, String token) {
        String[] configFiles = {
            "pom.xml",
            "src/main/resources/application.properties",
            "src/main/resources/application-prod.properties"
        };
        
        for (String path : configFiles) {
            try {
                sb.append("\n━━━ ").append(path).append(" ━━━\n");
                String content = getFileContentString(path, token);
                sb.append(content).append("\n");
            } catch (Exception e) {
                // 파일이 없을 수 있음
            }
        }
    }
    
    private void appendHtmlFiles(StringBuilder sb, String path, String token) {
        List<Map<String, Object>> items = getFileTree(path, token);
        
        for (Map<String, Object> item : items) {
            String name = (String) item.get("name");
            String type = (String) item.get("type");
            String itemPath = (String) item.get("path");
            
            if (type.equals("file") && name.endsWith(".html")) {
                try {
                    sb.append("\n━━━ ").append(itemPath).append(" ━━━\n");
                    String content = getFileContentString(itemPath, token);
                    sb.append(content).append("\n");
                } catch (Exception e) {
                    sb.append("Error reading file: ").append(e.getMessage()).append("\n");
                }
            }
        }
    }
    
    private String getFileContentString(String path, String token) {
        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", OWNER, REPO, path);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        String content = (String) response.getBody().get("content");
        
        return new String(Base64.getDecoder().decode(content.replaceAll("\\s", "")));
    }

// CSS 파일 추가 메서드 ⭐
private void appendCssFiles(StringBuilder sb, String path, String token) {
    List<Map<String, Object>> items = getFileTree(path, token);
    
    for (Map<String, Object> item : items) {
        String name = (String) item.get("name");
        String type = (String) item.get("type");
        String itemPath = (String) item.get("path");
        
        if (type.equals("file") && name.endsWith(".css")) {
            try {
                sb.append("\n━━━ ").append(itemPath).append(" ━━━\n");
                String content = getFileContentString(itemPath, token);
                sb.append(content).append("\n");
            } catch (Exception e) {
                sb.append("Error reading file: ").append(e.getMessage()).append("\n");
            }
        }
    }
}

// JavaScript 파일 추가 메서드 ⭐
private void appendJsFiles(StringBuilder sb, String path, String token) {
    List<Map<String, Object>> items = getFileTree(path, token);
    
    for (Map<String, Object> item : items) {
        String name = (String) item.get("name");
        String type = (String) item.get("type");
        String itemPath = (String) item.get("path");
        
        if (type.equals("file") && name.endsWith(".js")) {
            try {
                sb.append("\n━━━ ").append(itemPath).append(" ━━━\n");
                String content = getFileContentString(itemPath, token);
                sb.append(content).append("\n");
            } catch (Exception e) {
                sb.append("Error reading file: ").append(e.getMessage()).append("\n");
            }
        }
    }
}
}