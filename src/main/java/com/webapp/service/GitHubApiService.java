package com.webapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * GitHub API 호출을 위한 공통 서비스
 * API 호출 로직을 중앙화하여 재사용성과 유지보수성 향상
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubApiService {
    
    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private static final String GITHUB_API_VERSION = "application/vnd.github.v3+json";
    
    private final RestTemplate restTemplate;
    
    /**
     * GitHub 토큰 검증
     */
    public boolean verifyToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        try {
            String url = GITHUB_API_BASE_URL + "/user";
            ResponseEntity<Map> response = executeGet(url, token, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Token verification failed", e);
            return false;
        }
    }
    
    /**
     * 저장소 정보 조회
     */
    public Map<String, Object> getRepositoryInfo(String owner, String repo, String token) {
        String url = String.format("%s/repos/%s/%s", GITHUB_API_BASE_URL, owner, repo);
        ResponseEntity<Map> response = executeGet(url, token, Map.class);
        return response.getBody();
    }
    
    /**
     * 최근 커밋 목록 조회
     */
    public List<Map<String, Object>> getRecentCommits(String owner, String repo, String token, int limit) {
        String url = String.format("%s/repos/%s/%s/commits?per_page=%d", 
            GITHUB_API_BASE_URL, owner, repo, limit);
        ResponseEntity<List> response = executeGet(url, token, List.class);
        return response.getBody();
    }
    
    /**
     * 파일/디렉토리 구조 조회
     */
    public List<Map<String, Object>> getContents(String owner, String repo, String path, String token) {
        String url = String.format("%s/repos/%s/%s/contents/%s", 
            GITHUB_API_BASE_URL, owner, repo, path);
        ResponseEntity<List> response = executeGet(url, token, List.class);
        return response.getBody();
    }
    
    /**
     * 파일 내용 조회 (단일 파일)
     */
    public Map<String, Object> getFileContent(String owner, String repo, String path, String token) {
        String url = String.format("%s/repos/%s/%s/contents/%s", 
            GITHUB_API_BASE_URL, owner, repo, path);
        ResponseEntity<Map> response = executeGet(url, token, Map.class);
        return response.getBody();
    }
    
    /**
     * Base64로 인코딩된 파일 내용 디코딩
     */
    public String decodeContent(String base64Content) {
        if (base64Content == null) {
            return null;
        }
        String cleanedContent = base64Content.replaceAll("\\s", "");
        return new String(Base64.getDecoder().decode(cleanedContent));
    }
    
    /**
     * GitHub API GET 요청 공통 메소드
     */
    private <T> ResponseEntity<T> executeGet(String url, String token, Class<T> responseType) {
        HttpHeaders headers = createAuthHeaders(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
        } catch (RestClientException e) {
            log.error("GitHub API call failed: {}", url, e);
            throw new GitHubApiException("Failed to call GitHub API: " + e.getMessage(), e);
        }
    }
    
    /**
     * 인증 헤더 생성
     */
    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "token " + token);
        headers.set(HttpHeaders.ACCEPT, GITHUB_API_VERSION);
        return headers;
    }
    
    /**
     * GitHub API 예외 클래스
     */
    public static class GitHubApiException extends RuntimeException {
        public GitHubApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}