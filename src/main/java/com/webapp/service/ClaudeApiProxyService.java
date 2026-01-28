package com.webapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Claude API 프록시 서비스
 * - 브라우저에서 직접 호출할 수 없는 Claude API를 서버에서 프록시
 * - API 키를 안전하게 서버에서 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeApiProxyService {
    
    private final RestTemplate restTemplate;
    
    @Value("${claude.api.key:}")
    private String apiKey;
    
    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-4-20250514";
    
    /**
     * Claude API 호출 (간단 버전)
     */
    public Map<String, Object> callClaude(String prompt, int maxTokens) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("Claude API key가 설정되지 않았습니다.");
            return createErrorResponse("API 키가 설정되지 않았습니다. 관리자에게 문의하세요.");
        }
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
            ));
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                CLAUDE_API_URL,
                request,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> content = (List<Map<String, Object>>) responseBody.get("content");
                
                if (content != null && !content.isEmpty()) {
                    String text = (String) content.get(0).get("text");
                    return createSuccessResponse(text);
                }
            }
            
            return createErrorResponse("AI 응답을 받을 수 없습니다.");
            
        } catch (Exception e) {
            log.error("Claude API 호출 실패", e);
            return createErrorResponse("AI 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 성공 응답 생성
     */
    private Map<String, Object> createSuccessResponse(String result) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("result", result);
        return response;
    }
    
    /**
     * 에러 응답 생성
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}