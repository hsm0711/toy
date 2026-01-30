package com.webapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * OpenRouter API 서비스
 * - Llama 3.1 8B Instruct 모델 사용
 * - 무료 티어 지원
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenRouterApiService {
    
    private final RestTemplate restTemplate;
    
    @Value("${openrouter.api.key:}")
    private String apiKey;
    
    private static final String OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL = "meta-llama/llama-3.1-8b-instruct";
    
    // 톤별 시스템 프롬프트
    private static final Map<String, String> TONE_PROMPTS = Map.of(
        "polite", "정중하고 공손한 말투로 변환해주세요. 존댓말을 사용하고 부드럽고 예의바른 표현을 사용하세요.",
        "aggressive", "강하고 공격적인 말투로 변환해주세요. 단호하고 직설적인 표현을 사용하되 비속어는 사용하지 마세요.",
        "developer", "개발자 특유의 말투로 변환해주세요. 기술 용어와 개발자 커뮤니티 표현을 활용하세요.",
        "formal", "격식있고 공식적인 말투로 변환해주세요. 비즈니스 문서나 공식 서신에 어울리는 표현을 사용하세요.",
        "casual", "편안하고 친근한 말투로 변환해주세요. 일상 대화처럼 자연스럽고 부담없는 표현을 사용하세요.",
        "professional", "전문가다운 말투로 변환해주세요. 명확하고 신뢰감 있는 표현을 사용하세요.",
        "friendly", "친절하고 따뜻한 말투로 변환해주세요. 긍정적이고 친근한 표현을 사용하세요.",
        "humorous", "유머러스하고 재치있는 말투로 변환해주세요. 재미있는 표현과 농담을 적절히 섞어주세요."
    );
    
    /**
     * 문장 톤 변환
     */
    public Map<String, Object> transformTone(String text, String tone) {
        if (!isApiKeyConfigured()) {
            return createErrorResponse("OpenRouter API 키가 설정되지 않았습니다. application.properties에 openrouter.api.key를 추가하세요.");
        }
        
        try {
            String instruction = TONE_PROMPTS.getOrDefault(tone, 
                "다른 톤으로 변환해주세요. 원문의 의미는 유지하되 톤만 변경하세요.");
            
            String prompt = String.format(
                "%s\n\n원문: %s\n\n변환된 문장만 출력하고 다른 설명은 하지 마세요.",
                instruction, text
            );
            
            Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", List.of(
                    Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 500,
                "temperature", 0.7
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("HTTP-Referer", "https://toy.playcloud8.com");
            headers.set("X-Title", "Playground Tone Transformer");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            log.info("OpenRouter API 호출: tone={}", tone);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                OPENROUTER_API_URL, request, Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String result = extractResultFromResponse(response.getBody());
                if (result != null) {
                    return createSuccessResponse(result.trim());
                }
            }
            
            return createErrorResponse("AI 응답을 받을 수 없습니다.");
            
        } catch (HttpClientErrorException e) {
            log.error("HTTP 에러: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return createErrorResponse("API 키가 유효하지 않습니다.");
            } else if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                return createErrorResponse("API 호출 한도 초과. 잠시 후 다시 시도하세요.");
            }
            
            return createErrorResponse("API 호출 실패: " + e.getMessage());
            
        } catch (Exception e) {
            log.error("문장 톤 변환 오류", e);
            return createErrorResponse("변환 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 응답에서 결과 추출
     */
    private String extractResultFromResponse(Map<String, Object> response) {
        try {
            if (response.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                
                if (!choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    
                    if (message != null && message.containsKey("content")) {
                        return (String) message.get("content");
                    }
                }
            }
        } catch (Exception e) {
            log.error("응답 파싱 실패", e);
        }
        return null;
    }
    
    private boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
    
    private Map<String, Object> createSuccessResponse(String result) {
        return Map.of("success", true, "result", result);
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        return Map.of("success", false, "message", message);
    }
}