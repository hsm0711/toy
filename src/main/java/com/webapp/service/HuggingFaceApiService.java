package com.webapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Hugging Face Inference API 서비스
 * - 무료 티어 사용 (월 사용량 제한 있음)
 * - 서버에서 API 호출하여 프론트엔드에 전달
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HuggingFaceApiService {
    
    private final RestTemplate restTemplate;
    
    @Value("${huggingface.api.key:}")
    private String apiKey;
    
    private static final String HF_API_URL = "https://api-inference.huggingface.co/models/";
    
    // 모델 목록
    private static final String MODEL_SUMMARIZATION = "facebook/bart-large-cnn";
    private static final String MODEL_SENTIMENT = "distilbert-base-uncased-finetuned-sst-2-english";
    private static final String MODEL_TRANSLATION_EN_KO = "Helsinki-NLP/opus-mt-en-ko";
    private static final String MODEL_ZERO_SHOT = "facebook/bart-large-mnli";
    
    /**
     * 텍스트 요약
     */
    public Map<String, Object> summarize(String text, int maxLength) {
        if (!isApiKeyConfigured()) {
            return createErrorResponse("Hugging Face API 키가 설정되지 않았습니다. application.properties에 huggingface.api.key를 추가하세요.");
        }
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", text);
            requestBody.put("parameters", Map.of(
                "max_length", maxLength,
                "min_length", 30,
                "do_sample", false
            ));
            
            Object response = callHuggingFaceApi(MODEL_SUMMARIZATION, requestBody);
            
            // 배열 응답 처리
            if (response instanceof List) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response;
                if (!results.isEmpty() && results.get(0).containsKey("summary_text")) {
                    String summary = (String) results.get(0).get("summary_text");
                    return createSuccessResponse(summary);
                }
            }
            
            // Map 응답 처리
            if (response instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) response;
                if (map.containsKey("error")) {
                    return createErrorResponse("요약 실패: " + map.get("error"));
                }
                if (map.containsKey("summary_text")) {
                    return createSuccessResponse((String) map.get("summary_text"));
                }
            }
            
            return createErrorResponse("요약 결과를 파싱할 수 없습니다.");
            
        } catch (Exception e) {
            log.error("요약 중 오류 발생", e);
            return createErrorResponse("요약 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 감정 분석
     */
    public Map<String, Object> analyzeSentiment(String text) {
        if (!isApiKeyConfigured()) {
            return createErrorResponse("Hugging Face API 키가 설정되지 않았습니다.");
        }
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", text);
            
            Object response = callHuggingFaceApi(MODEL_SENTIMENT, requestBody);
            
            // 배열 응답 처리
            if (response instanceof List) {
                List<List<Map<String, Object>>> results = (List<List<Map<String, Object>>>) response;
                if (!results.isEmpty() && !results.get(0).isEmpty()) {
                    Map<String, Object> sentiment = results.get(0).get(0);
                    
                    String label = (String) sentiment.get("label");
                    Double score = (Double) sentiment.get("score");
                    
                    // 한글 변환
                    String labelKo = convertSentimentLabel(label);
                    double confidence = score * 100;
                    
                    String analysis = String.format("**감정**: %s (확신도: %.1f%%)\n\n", labelKo, confidence);
                    analysis += getSentimentDescription(label);
                    
                    return createSuccessResponse(analysis);
                }
            }
            
            return createErrorResponse("감정 분석 결과를 파싱할 수 없습니다.");
            
        } catch (Exception e) {
            log.error("감정 분석 중 오류 발생", e);
            return createErrorResponse("감정 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 번역 (영어 → 한국어)
     */
    public Map<String, Object> translate(String text) {
        if (!isApiKeyConfigured()) {
            return createErrorResponse("Hugging Face API 키가 설정되지 않았습니다.");
        }
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", text);
            
            Object response = callHuggingFaceApi(MODEL_TRANSLATION_EN_KO, requestBody);
            
            // 배열 응답 처리
            if (response instanceof List) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response;
                if (!results.isEmpty() && results.get(0).containsKey("translation_text")) {
                    String translation = (String) results.get(0).get("translation_text");
                    return createSuccessResponse(translation);
                }
            }
            
            return createErrorResponse("번역 결과를 파싱할 수 없습니다.");
            
        } catch (Exception e) {
            log.error("번역 중 오류 발생", e);
            return createErrorResponse("번역 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * Zero-shot 분류 (키워드 추출, 문장 개선 등에 활용)
     */
    public Map<String, Object> zeroShotClassification(String text, List<String> candidateLabels) {
        if (!isApiKeyConfigured()) {
            return createErrorResponse("Hugging Face API 키가 설정되지 않았습니다.");
        }
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", text);
            requestBody.put("parameters", Map.of("candidate_labels", candidateLabels));
            
            Object response = callHuggingFaceApi(MODEL_ZERO_SHOT, requestBody);
            
            if (response instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) response;
                if (map.containsKey("error")) {
                    return createErrorResponse("분류 실패: " + map.get("error"));
                }
            }
            
            return Map.of("success", true, "data", response);
            
        } catch (Exception e) {
            log.error("Zero-shot 분류 중 오류 발생", e);
            return createErrorResponse("분류 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * Hugging Face API 호출 (공통)
     */
    private Object callHuggingFaceApi(String modelName, Map<String, Object> requestBody) {
        String url = HF_API_URL + modelName;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<Object> response = restTemplate.postForEntity(url, request, Object.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            
            return Map.of("error", "API 호출 실패: " + response.getStatusCode());
            
        } catch (Exception e) {
            log.error("Hugging Face API 호출 실패: {}", url, e);
            
            // 모델 로딩 중 오류 처리
            if (e.getMessage() != null && e.getMessage().contains("loading")) {
                return Map.of("error", "모델이 로딩 중입니다. 약 20초 후 다시 시도해주세요.");
            }
            
            return Map.of("error", "API 호출 중 오류: " + e.getMessage());
        }
    }
    
    /**
     * API 키 설정 확인
     */
    private boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
    
    /**
     * 감정 라벨 한글 변환
     */
    private String convertSentimentLabel(String label) {
        return switch (label.toUpperCase()) {
            case "POSITIVE" -> "긍정";
            case "NEGATIVE" -> "부정";
            case "NEUTRAL" -> "중립";
            default -> label;
        };
    }
    
    /**
     * 감정 설명
     */
    private String getSentimentDescription(String label) {
        return switch (label.toUpperCase()) {
            case "POSITIVE" -> "이 텍스트는 긍정적인 감정을 담고 있습니다. 만족, 기쁨, 희망 등의 긍정적 정서가 느껴집니다.";
            case "NEGATIVE" -> "이 텍스트는 부정적인 감정을 담고 있습니다. 불만, 실망, 우려 등의 부정적 정서가 느껴집니다.";
            case "NEUTRAL" -> "이 텍스트는 중립적인 감정을 담고 있습니다. 객관적이거나 사실 전달 위주의 내용입니다.";
            default -> "감정을 파악할 수 없습니다.";
        };
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
