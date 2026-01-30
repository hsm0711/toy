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
 * - 다양한 무료 AI 모델 지원
 * - Llama, Qwen, Hermes 등
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenRouterApiService {
    
    private final RestTemplate restTemplate;
    
    @Value("${openrouter.api.key:}")
    private String apiKey;
    
    private static final String OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions";
    
    // 모델 목록
    private static final String MODEL_LLAMA_3_1_8B = "meta-llama/llama-3.1-8b-instruct";
    private static final String MODEL_LLAMA_3_2_3B = "meta-llama/llama-3.2-3b-instruct";
    private static final String MODEL_QWEN_2_5_7B = "qwen/qwen-2.5-7b-instruct";
    private static final String MODEL_HERMES_405B = "nousresearch/hermes-3-llama-3.1-405b";
    
    /**
     * 글쓰기 생성 (Llama 3.2 3B)
     */
    public Map<String, Object> generateWriting(String topic, String type, String tone, String length) {
        if (!isApiKeyConfigured()) {
            return createErrorResponse("OpenRouter API 키가 설정되지 않았습니다.");
        }
        
        try {
            String typePrompt = getWritingTypePrompt(type);
            String tonePrompt = getWritingTonePrompt(tone);
            String lengthPrompt = getWritingLengthPrompt(length);
            
            String prompt = String.format(
                "%s\n\n주제: %s\n\n%s\n%s\n\n생성된 글만 출력하고 다른 설명은 하지 마세요.",
                typePrompt, topic, tonePrompt, lengthPrompt
            );
            
            return callOpenRouterModel(MODEL_LLAMA_3_2_3B, prompt, 1000, 0.7, null);
            
        } catch (Exception e) {
            log.error("글쓰기 생성 오류", e);
            return createErrorResponse("생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 코드 설명 (Qwen 2.5 7B)
     */
    public Map<String, Object> explainCode(String code, String language, String level) {
        if (!isApiKeyConfigured()) {
            return createErrorResponse("OpenRouter API 키가 설정되지 않았습니다.");
        }
        
        try {
            String levelPrompt = getCodeLevelPrompt(level);
            
            String prompt = String.format(
                "%s\n\n프로그래밍 언어: %s\n\n코드:\n```\n%s\n```\n\n" +
                "다음 형식으로 설명해주세요:\n" +
                "1. 전체 개요\n" +
                "2. 단계별 상세 설명\n" +
                "3. 시간 복잡도 분석\n" +
                "4. 개선 제안",
                levelPrompt, language, code
            );
            
            return callOpenRouterModel(MODEL_QWEN_2_5_7B, prompt, 1500, 0.3, null);
            
        } catch (Exception e) {
            log.error("코드 설명 오류", e);
            return createErrorResponse("설명 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 스토리 생성 (Hermes 3 405B)
     */
    public Map<String, Object> generateStory(String prompt, String genre, String length) {
        if (!isApiKeyConfigured()) {
            return createErrorResponse("OpenRouter API 키가 설정되지 않았습니다.");
        }
        
        try {
            String genrePrompt = getStoryGenrePrompt(genre);
            String lengthPrompt = getStoryLengthPrompt(length);
            
            String fullPrompt = String.format(
                "창작 소설을 작성해주세요.\n\n" +
                "장르: %s\n" +
                "길이: %s\n" +
                "아이디어: %s\n\n" +
                "생생하고 흥미로운 스토리를 작성하되, 제목, 줄거리, 캐릭터 묘사를 포함하세요.",
                genrePrompt, lengthPrompt, prompt
            );
            
            return callOpenRouterModel(MODEL_HERMES_405B, fullPrompt, 2000, 0.8, null);
            
        } catch (Exception e) {
            log.error("스토리 생성 오류", e);
            return createErrorResponse("생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 학습 도우미 (Llama 3.2 3B)
     */
    public Map<String, Object> studyHelp(String topic, String type, String level) {
        if (!isApiKeyConfigured()) {
            return createErrorResponse("OpenRouter API 키가 설정되지 않았습니다.");
        }
        
        try {
            String typePrompt = getStudyTypePrompt(type);
            String levelPrompt = getStudyLevelPrompt(level);
            
            String prompt = String.format(
                "%s\n\n주제: %s\n\n학습 수준: %s\n\n" +
                "명확하고 이해하기 쉽게 설명해주세요.",
                typePrompt, topic, levelPrompt
            );
            
            return callOpenRouterModel(MODEL_LLAMA_3_2_3B, prompt, 1200, 0.5, null);
            
        } catch (Exception e) {
            log.error("학습 도우미 오류", e);
            return createErrorResponse("오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 번역 (Qwen 2.5 7B)
     */
    public Map<String, Object> translateText(String text, String sourceLang, String targetLang) {
        if (!isApiKeyConfigured()) {
            return createErrorResponse("OpenRouter API 키가 설정되지 않았습니다.");
        }
        
        try {
            String prompt = String.format(
                "다음 텍스트를 %s에서 %s로 번역해주세요.\n\n" +
                "원문:\n%s\n\n" +
                "번역된 텍스트만 출력하고 다른 설명은 하지 마세요.",
                getLanguageName(sourceLang), getLanguageName(targetLang), text
            );
            
            return callOpenRouterModel(MODEL_QWEN_2_5_7B, prompt, 1000, 0.3, null);
            
        } catch (Exception e) {
            log.error("번역 오류", e);
            return createErrorResponse("번역 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 아이콘 추천 (Qwen 2.5 7B)
     */
    public Map<String, Object> suggestIcons(String menuName) {
        if (!isApiKeyConfigured()) {
            return createErrorResponse("OpenRouter API 키가 설정되지 않았습니다.");
        }
        
        try {
            String prompt = String.format(
                "메뉴명 \"%s\"에 가장 적합한 이모지 아이콘 10개를 추천해주세요.\n\n" +
                "요구사항:\n" +
                "1. 메뉴명의 의미와 기능을 정확히 파악하여 추천\n" +
                "2. 직관적이고 시각적으로 명확한 이모지 선택\n" +
                "3. 다양한 스타일 제공 (기본, 창의적, 전문적)\n" +
                "4. 각 이모지에 대한 간단한 설명 포함\n\n" +
                "응답 형식 (JSON만 반환, 다른 텍스트 없이):\n" +
                "{\n" +
                "  \"icons\": [\n" +
                "    {\"emoji\": \"🔧\", \"description\": \"도구/설정\"},\n" +
                "    {\"emoji\": \"⚙️\", \"description\": \"설정\"}\n" +
                "  ]\n" +
                "}\n\n" +
                "중요: JSON 형식만 반환하고, 마크다운 코드 블록이나 다른 텍스트를 포함하지 마세요.",
                menuName
            );
            
            return callOpenRouterModel(MODEL_QWEN_2_5_7B, prompt, 500, 0.5, "json_object");
            
        } catch (Exception e) {
            log.error("아이콘 추천 오류", e);
            return createErrorResponse("아이콘 추천 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 문장 톤 변환 (Llama 3.1 8B)
     */
    public Map<String, Object> transformTone(String text, String tone) {
        if (!isApiKeyConfigured()) {
            return createErrorResponse("OpenRouter API 키가 설정되지 않았습니다.");
        }
        
        try {
            Map<String, String> tonePrompts = Map.of(
                "polite", "정중하고 공손한 말투로 변환해주세요.",
                "aggressive", "강하고 공격적인 말투로 변환해주세요.",
                "developer", "개발자 특유의 말투로 변환해주세요.",
                "formal", "격식있고 공식적인 말투로 변환해주세요.",
                "casual", "편안하고 친근한 말투로 변환해주세요.",
                "professional", "전문가다운 말투로 변환해주세요.",
                "friendly", "친절하고 따뜻한 말투로 변환해주세요.",
                "humorous", "유머러스하고 재치있는 말투로 변환해주세요."
            );
            
            String instruction = tonePrompts.getOrDefault(tone, "다른 톤으로 변환해주세요.");
            
            String prompt = String.format(
                "%s\n\n원문: %s\n\n변환된 문장만 출력하고 다른 설명은 하지 마세요.",
                instruction, text
            );
            
            return callOpenRouterModel(MODEL_LLAMA_3_1_8B, prompt, 500, 0.7, null);
            
        } catch (Exception e) {
            log.error("톤 변환 오류", e);
            return createErrorResponse("변환 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * OpenRouter API 호출 (공통)
     */
    private Map<String, Object> callOpenRouterModel(String model, String prompt, int maxTokens, double temperature, String responseFormat) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);
            
            if (responseFormat != null) {
                requestBody.put("response_format", Map.of("type", responseFormat));
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("HTTP-Referer", "https://toy.playcloud8.com");
            headers.set("X-Title", "Playground AI Tools");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            log.info("OpenRouter API 호출: model={}", model);
            
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
            log.error("OpenRouter API 호출 오류", e);
            return createErrorResponse("오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    // ========== Helper Methods ==========
    
    private String getWritingTypePrompt(String type) {
        return switch (type) {
            case "blog" -> "블로그 포스트를 작성해주세요. SEO 친화적이고 독자 친화적으로 작성하세요.";
            case "email" -> "이메일 본문을 작성해주세요. 명확하고 예의바르게 작성하세요.";
            case "sns" -> "SNS 게시물을 작성해주세요. 짧고 임팩트있게, 해시태그 포함하세요.";
            case "ad" -> "광고 카피를 작성해주세요. 설득력있고 흥미로운 문구로 작성하세요.";
            default -> "글을 작성해주세요.";
        };
    }
    
    private String getWritingTonePrompt(String tone) {
        return switch (tone) {
            case "professional" -> "전문적이고 신뢰감있는 톤으로 작성하세요.";
            case "casual" -> "편안하고 친근한 톤으로 작성하세요.";
            case "formal" -> "격식있고 공식적인 톤으로 작성하세요.";
            case "creative" -> "창의적이고 독창적인 톤으로 작성하세요.";
            default -> "";
        };
    }
    
    private String getWritingLengthPrompt(String length) {
        return switch (length) {
            case "short" -> "짧게 (200-300자) 작성하세요.";
            case "medium" -> "적당한 길이로 (400-600자) 작성하세요.";
            case "long" -> "길게 (800-1000자) 작성하세요.";
            default -> "";
        };
    }
    
    private String getCodeLevelPrompt(String level) {
        return switch (level) {
            case "beginner" -> "프로그래밍 초보자도 이해할 수 있도록 쉽게 설명해주세요.";
            case "intermediate" -> "중급 개발자 수준으로 설명해주세요.";
            case "advanced" -> "고급 개발자 수준으로 상세히 설명해주세요.";
            default -> "명확하게 설명해주세요.";
        };
    }
    
    private String getStoryGenrePrompt(String genre) {
        return switch (genre) {
            case "fantasy" -> "판타지";
            case "scifi" -> "SF";
            case "mystery" -> "미스터리";
            case "romance" -> "로맨스";
            case "horror" -> "호러";
            case "adventure" -> "모험";
            default -> "일반";
        };
    }
    
    private String getStoryLengthPrompt(String length) {
        return switch (length) {
            case "short" -> "단편 (500-800자)";
            case "medium" -> "중편 (1000-1500자)";
            case "long" -> "장편 (1500-2000자)";
            default -> "적당한 길이";
        };
    }
    
    private String getStudyTypePrompt(String type) {
        return switch (type) {
            case "explain" -> "개념을 쉽게 설명해주세요.";
            case "solve" -> "문제 풀이 과정을 단계별로 설명해주세요.";
            case "summarize" -> "핵심 내용을 요약해주세요.";
            case "quiz" -> "이해도를 확인할 수 있는 퀴즈를 만들어주세요.";
            default -> "학습 내용을 제공해주세요.";
        };
    }
    
    private String getStudyLevelPrompt(String level) {
        return switch (level) {
            case "elementary" -> "초등학생";
            case "middle" -> "중학생";
            case "high" -> "고등학생";
            case "university" -> "대학생";
            default -> "일반";
        };
    }
    
    private String getLanguageName(String code) {
        return switch (code) {
            case "ko" -> "한국어";
            case "en" -> "영어";
            case "ja" -> "일본어";
            case "zh" -> "중국어";
            default -> code;
        };
    }
    
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