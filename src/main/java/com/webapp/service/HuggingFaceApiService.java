package com.webapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;

/**
 * Hugging Face Inference API 서비스
 * - 엔드포인트 수정: router.huggingface.co
 * - 응답 파싱 개선
 * - 에러 처리 강화
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HuggingFaceApiService {
    
    private final RestTemplate restTemplate;
    
    @Value("${huggingface.api.key:}")
    private String apiKey;
    
    // ✅ 수정된 엔드포인트
    private static final String HF_API_URL = "https://router.huggingface.co/models/";
    
    // 모델 목록
    private static final String MODEL_SUMMARIZATION = "facebook/bart-large-cnn";
    private static final String MODEL_SENTIMENT = "distilbert-base-uncased-finetuned-sst-2-english";
    private static final String MODEL_TRANSLATION_EN_KO = "Helsinki-NLP/opus-mt-en-ko";
    private static final String MODEL_ZERO_SHOT = "facebook/bart-large-mnli";
    
    /**
     * 텍스트 요약
     */
    public Map<String, Object> summarize(String text, int maxLength, int minLength) {
        if (!isApiKeyConfigured()) {
            return createErrorResponse("Hugging Face API 키가 설정되지 않았습니다. application.properties에 huggingface.api.key를 추가하세요.");
        }
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", text);
            requestBody.put("parameters", Map.of(
                "max_length", maxLength,
                "min_length", minLength,
                "do_sample", false
            ));
            
            Object response = callHuggingFaceApi(MODEL_SUMMARIZATION, requestBody);
            
            log.info("요약 응답: {}", response);
            
            // 응답 파싱 개선
            String summary = extractSummaryFromResponse(response);
            if (summary != null) {
                return createSuccessResponse(summary);
            }
            
            return createErrorResponse("요약 결과를 추출할 수 없습니다. 응답: " + response);
            
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
            
            log.info("감정 분석 응답: {}", response);
            
            // 응답 파싱 개선
            Map<String, Object> sentiment = extractSentimentFromResponse(response);
            if (sentiment != null) {
                String label = (String) sentiment.get("label");
                Double score = (Double) sentiment.get("score");
                
                String labelKo = convertSentimentLabel(label);
                double confidence = score * 100;
                
                String analysis = String.format("**감정**: %s (확신도: %.1f%%)\n\n", labelKo, confidence);
                analysis += getSentimentDescription(label);
                
                return createSuccessResponse(analysis);
            }
            
            return createErrorResponse("감정 분석 결과를 추출할 수 없습니다. 응답: " + response);
            
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
            
            log.info("번역 응답: {}", response);
            
            // 응답 파싱 개선
            String translation = extractTranslationFromResponse(response);
            if (translation != null) {
                return createSuccessResponse(translation);
            }
            
            return createErrorResponse("번역 결과를 추출할 수 없습니다. 응답: " + response);
            
        } catch (Exception e) {
            log.error("번역 중 오류 발생", e);
            return createErrorResponse("번역 중 오류가 발생했습니다: " + e.getMessage());
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
            log.info("API 호출: {} with body: {}", url, requestBody);
            
            ResponseEntity<Object> response = restTemplate.postForEntity(url, request, Object.class);
            
            log.info("API 응답 상태: {}", response.getStatusCode());
            log.info("API 응답 본문: {}", response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            
            return Map.of("error", "API 호출 실패: " + response.getStatusCode());
            
        } catch (HttpClientErrorException e) {
            log.error("HTTP 에러: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Map.of("error", "API 호출 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("Hugging Face API 호출 실패: {}", url, e);
            
            if (e.getMessage() != null && e.getMessage().contains("loading")) {
                return Map.of("error", "모델이 로딩 중입니다. 약 20초 후 다시 시도해주세요.");
            }
            
            return Map.of("error", "API 호출 중 오류: " + e.getMessage());
        }
    }
    
    /**
     * 요약 응답에서 텍스트 추출
     */
    private String extractSummaryFromResponse(Object response) {
        try {
            // Case 1: List<Map>
            if (response instanceof List) {
                List<?> list = (List<?>) response;
                if (!list.isEmpty() && list.get(0) instanceof Map) {
                    Map<String, Object> first = (Map<String, Object>) list.get(0);
                    if (first.containsKey("summary_text")) {
                        return (String) first.get("summary_text");
                    }
                    if (first.containsKey("generated_text")) {
                        return (String) first.get("generated_text");
                    }
                }
            }
            
            // Case 2: Map
            if (response instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) response;
                if (map.containsKey("summary_text")) {
                    return (String) map.get("summary_text");
                }
                if (map.containsKey("generated_text")) {
                    return (String) map.get("generated_text");
                }
            }
        } catch (Exception e) {
            log.error("요약 응답 파싱 실패", e);
        }
        return null;
    }
    
    /**
     * 감정 분석 응답에서 정보 추출
     */
    private Map<String, Object> extractSentimentFromResponse(Object response) {
        try {
            // Case 1: List<List<Map>>
            if (response instanceof List) {
                List<?> list = (List<?>) response;
                if (!list.isEmpty()) {
                    Object first = list.get(0);
                    
                    // Nested list
                    if (first instanceof List) {
                        List<?> nested = (List<?>) first;
                        if (!nested.isEmpty() && nested.get(0) instanceof Map) {
                            return (Map<String, Object>) nested.get(0);
                        }
                    }
                    
                    // Direct map
                    if (first instanceof Map) {
                        return (Map<String, Object>) first;
                    }
                }
            }
            
            // Case 2: Map directly
            if (response instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) response;
                if (map.containsKey("label") && map.containsKey("score")) {
                    return map;
                }
            }
        } catch (Exception e) {
            log.error("감정 분석 응답 파싱 실패", e);
        }
        return null;
    }
    
    /**
     * 번역 응답에서 텍스트 추출
     */
    private String extractTranslationFromResponse(Object response) {
        try {
            // Case 1: List<Map>
            if (response instanceof List) {
                List<?> list = (List<?>) response;
                if (!list.isEmpty() && list.get(0) instanceof Map) {
                    Map<String, Object> first = (Map<String, Object>) list.get(0);
                    if (first.containsKey("translation_text")) {
                        return (String) first.get("translation_text");
                    }
                    if (first.containsKey("generated_text")) {
                        return (String) first.get("generated_text");
                    }
                }
            }
            
            // Case 2: Map
            if (response instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) response;
                if (map.containsKey("translation_text")) {
                    return (String) map.get("translation_text");
                }
                if (map.containsKey("generated_text")) {
                    return (String) map.get("generated_text");
                }
            }
        } catch (Exception e) {
            log.error("번역 응답 파싱 실패", e);
        }
        return null;
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
     * 키워드 추출 (통계 기반)
     */
    public Map<String, Object> extractKeywords(String text) {
        try {
            Set<String> stopwords = new HashSet<>(Arrays.asList(
                "이", "그", "저", "것", "수", "등", "들", "및", "때", "등등",
                "하다", "있다", "되다", "않다", "없다", "아니다",
                "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
                "of", "is", "are", "was", "were", "be", "been", "being",
                "have", "has", "had", "do", "does", "did", "will", "would",
                "can", "could", "should", "may", "might", "must"
            ));

            String[] words = text.toLowerCase()
                .replaceAll("[^\\w\\s가-힣]", " ")
                .split("\\s+");
            
            List<String> filteredWords = Arrays.stream(words)
                .filter(word -> word.length() > 2 && !stopwords.contains(word))
                .toList();

            Map<String, Integer> frequency = new HashMap<>();
            for (String word : filteredWords) {
                frequency.put(word, frequency.getOrDefault(word, 0) + 1);
            }

            List<Map.Entry<String, Integer>> topKeywords = frequency.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .toList();

            StringBuilder result = new StringBuilder("**추출된 주요 키워드:**\n\n");
            int index = 1;
            for (Map.Entry<String, Integer> entry : topKeywords) {
                result.append(String.format("%d. **%s** (%d회 등장)\n", 
                    index++, entry.getKey(), entry.getValue()));
            }

            return createSuccessResponse(result.toString());

        } catch (Exception e) {
            log.error("키워드 추출 오류", e);
            return createErrorResponse("키워드 추출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 코드 리뷰 (정적 분석)
     */
    public Map<String, Object> reviewCode(String code, String language) {
        try {
            List<Map<String, String>> issues = new ArrayList<>();
            int score = 100;

            int lines = code.split("\n").length;
            if (lines > 100) {
                issues.add(Map.of(
                    "severity", "medium",
                    "message", String.format("코드가 %d줄로 너무 깁니다. 함수를 분리하는 것을 권장합니다.", lines)
                ));
                score -= 10;
            }

            long indentIssues = Arrays.stream(code.split("\n"))
                .filter(line -> line.matches("^\\s{1,3}\\S.*") || line.matches("^\\t\\S.*"))
                .count();
            
            if (indentIssues > lines * 0.3) {
                issues.add(Map.of(
                    "severity", "low",
                    "message", "일관되지 않은 들여쓰기가 발견되었습니다."
                ));
                score -= 5;
            }

            long commentLines = Arrays.stream(code.split("\n"))
                .filter(line -> {
                    String trimmed = line.trim();
                    return trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("#");
                })
                .count();
            
            if (commentLines < lines * 0.1) {
                issues.add(Map.of(
                    "severity", "low",
                    "message", "주석이 부족합니다. 복잡한 로직에는 설명을 추가하세요."
                ));
                score -= 5;
            }

            long hardcodedStrings = code.split("\"[^\"]{20,}\"").length - 1;
            if (hardcodedStrings > 3) {
                issues.add(Map.of(
                    "severity", "medium",
                    "message", String.format("긴 문자열 %d개가 하드코딩되어 있습니다. 상수로 분리하세요.", hardcodedStrings)
                ));
                score -= 10;
            }

            boolean hasErrorHandling = code.contains("try") || code.contains("catch") || 
                                    code.contains("except") || code.contains("error");
            
            if (!hasErrorHandling && lines > 20) {
                issues.add(Map.of(
                    "severity", "high",
                    "message", "에러 처리가 없습니다. try-catch 또는 에러 검사를 추가하세요."
                ));
                score -= 15;
            }

            long shortVarNames = code.split("\\b[a-z]\\b").length - 1;
            if (shortVarNames > 5) {
                issues.add(Map.of(
                    "severity", "low",
                    "message", "한 글자 변수명이 많습니다. 의미있는 이름을 사용하세요."
                ));
                score -= 5;
            }

            int maxNesting = calculateMaxNesting(code);
            if (maxNesting > 4) {
                issues.add(Map.of(
                    "severity", "high",
                    "message", String.format("중첩 깊이가 %d단계로 너무 깊습니다. 코드를 리팩토링하세요.", maxNesting)
                ));
                score -= 15;
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("**코드 품질 점수**: %d/100\n\n", Math.max(0, score)));
            
            if (issues.isEmpty()) {
                result.append("✅ 발견된 문제가 없습니다!\n\n");
                result.append("**긍정적인 부분:**\n");
                result.append("- 코드가 깔끔하게 작성되었습니다.\n");
                result.append("- 가독성이 좋습니다.\n");
            } else {
                result.append("**발견된 문제점:**\n\n");
                
                List<Map<String, String>> highIssues = issues.stream()
                    .filter(i -> i.get("severity").equals("high"))
                    .toList();
                List<Map<String, String>> mediumIssues = issues.stream()
                    .filter(i -> i.get("severity").equals("medium"))
                    .toList();
                List<Map<String, String>> lowIssues = issues.stream()
                    .filter(i -> i.get("severity").equals("low"))
                    .toList();
                
                if (!highIssues.isEmpty()) {
                    result.append("🔴 **심각:**\n");
                    for (Map<String, String> issue : highIssues) {
                        result.append("  - ").append(issue.get("message")).append("\n");
                    }
                    result.append("\n");
                }
                
                if (!mediumIssues.isEmpty()) {
                    result.append("🟡 **보통:**\n");
                    for (Map<String, String> issue : mediumIssues) {
                        result.append("  - ").append(issue.get("message")).append("\n");
                    }
                    result.append("\n");
                }
                
                if (!lowIssues.isEmpty()) {
                    result.append("🟢 **경미:**\n");
                    for (Map<String, String> issue : lowIssues) {
                        result.append("  - ").append(issue.get("message")).append("\n");
                    }
                    result.append("\n");
                }
            }

            result.append("**개선 제안:**\n");
            result.append("- 함수는 한 가지 일만 하도록 작성하세요\n");
            result.append("- 변수와 함수 이름은 명확하고 의미있게 지으세요\n");
            result.append("- 복잡한 로직은 주석으로 설명하세요\n");
            result.append("- 에러 처리를 빠짐없이 추가하세요\n\n");

            result.append("**전체 평가:**\n");
            if (score >= 80) {
                result.append("우수한 코드입니다. 계속 이런 스타일을 유지하세요!");
            } else if (score >= 60) {
                result.append("양호한 코드입니다. 몇 가지 개선이 필요합니다.");
            } else {
                result.append("개선이 필요한 코드입니다. 위의 제안사항을 참고하세요.");
            }

            return createSuccessResponse(result.toString());

        } catch (Exception e) {
            log.error("코드 리뷰 오류", e);
            return createErrorResponse("코드 리뷰 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private int calculateMaxNesting(String code) {
        int maxDepth = 0;
        int currentDepth = 0;
        
        for (char c : code.toCharArray()) {
            if (c == '{' || c == '(') {
                currentDepth++;
                maxDepth = Math.max(maxDepth, currentDepth);
            } else if (c == '}' || c == ')') {
                currentDepth--;
            }
        }
        
        return maxDepth;
    }

    public Map<String, Object> analyzeData(List<Map<String, Object>> data) {
        try {
            if (data == null || data.isEmpty()) {
                return createErrorResponse("유효한 데이터가 없습니다.");
            }

            Map<String, Object> sample = data.get(0);
            Set<String> columns = sample.keySet();
            
            StringBuilder result = new StringBuilder();
            result.append("**기본 통계:**\n\n");
            result.append(String.format("- 데이터 개수: %d개\n", data.size()));
            result.append(String.format("- 컬럼 수: %d개\n", columns.size()));
            result.append(String.format("- 주요 필드: %s\n\n", String.join(", ", columns)));

            result.append("**주요 발견사항:**\n\n");

            int index = 1;
            for (String col : columns) {
                List<Object> values = data.stream()
                    .map(row -> row.get(col))
                    .filter(v -> v != null)
                    .toList();
                
                Set<Object> uniqueValues = new HashSet<>(values);
                
                result.append(String.format("%d. **%s**\n", index++, col));
                result.append(String.format("   - 고유값: %d개\n", uniqueValues.size()));
                
                List<Double> numericValues = values.stream()
                    .filter(v -> {
                        try {
                            Double.parseDouble(v.toString());
                            return true;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    })
                    .map(v -> Double.parseDouble(v.toString()))
                    .toList();
                
                if (!numericValues.isEmpty()) {
                    double sum = numericValues.stream().mapToDouble(Double::doubleValue).sum();
                    double avg = sum / numericValues.size();
                    double min = numericValues.stream().mapToDouble(Double::doubleValue).min().orElse(0);
                    double max = numericValues.stream().mapToDouble(Double::doubleValue).max().orElse(0);
                    
                    result.append(String.format("   - 평균: %.2f\n", avg));
                    result.append(String.format("   - 최소: %.2f, 최대: %.2f\n", min, max));
                } else {
                    Map<Object, Long> frequency = values.stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                            v -> v, java.util.stream.Collectors.counting()
                        ));
                    
                    Map.Entry<Object, Long> mostCommon = frequency.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .orElse(null);
                    
                    if (mostCommon != null) {
                        result.append(String.format("   - 최빈값: \"%s\" (%d회)\n", 
                            mostCommon.getKey(), mostCommon.getValue()));
                    }
                }
                result.append("\n");
            }

            result.append("**추세 및 상관관계:**\n");
            result.append(String.format("- 데이터 분포가 %s\n", 
                data.size() > 100 ? "충분합니다" : "더 필요할 수 있습니다"));
            result.append(String.format("- %d개 변수 간의 관계를 시각화하면 더 많은 인사이트를 얻을 수 있습니다\n\n", 
                columns.size()));

            result.append("**제안사항:**\n");
            result.append("- 그래프를 그려 시각적으로 확인하세요\n");
            result.append("- 이상치(outlier)가 있는지 확인하세요\n");
            result.append("- 결측값(null)이 있다면 처리 방법을 결정하세요\n");
            result.append("- 시계열 데이터라면 트렌드를 분석하세요\n");

            return createSuccessResponse(result.toString());

        } catch (Exception e) {
            log.error("데이터 분석 오류", e);
            return createErrorResponse("데이터 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    private Map<String, Object> createSuccessResponse(String result) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("result", result);
        return response;
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}