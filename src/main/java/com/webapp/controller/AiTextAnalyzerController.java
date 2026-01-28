package com.webapp.controller;

import com.webapp.service.HuggingFaceApiService;
import com.webapp.service.MenuService;
import com.webapp.util.ResponseUtils;
import com.webapp.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 텍스트 분석 컨트롤러
 * - Hugging Face Inference API 사용 (무료 티어)
 * - 서버 사이드에서 API 호출
 */
@Slf4j
@Controller
@RequestMapping("/ai-text-analyzer")
@RequiredArgsConstructor
public class AiTextAnalyzerController {
    
    private final MenuService menuService;
    private final HuggingFaceApiService huggingFaceApiService;
    
    @GetMapping
    public String aiTextAnalyzerPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "ai-text-analyzer");
        return "ai-text-analyzer";
    }
    
    /**
     * API: 텍스트 분석
     */
    @PostMapping("/api/analyze")
    @ResponseBody
    public Map<String, Object> analyzeText(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            String analysisType = request.get("analysisType");
            
            if (ValidationUtils.isEmpty(text)) {
                return ResponseUtils.failure("분석할 텍스트를 입력하세요.");
            }
            
            if (text.length() > 5000) {
                return ResponseUtils.failure("텍스트가 너무 깁니다. 5000자 이하로 줄여주세요.");
            }
            
            // 분석 유형에 따라 API 호출
            return switch (analysisType) {
                case "summary" -> huggingFaceApiService.summarize(text, 150, 30);
                case "keywords" -> huggingFaceApiService.extractKeywords(text);
                case "sentiment" -> huggingFaceApiService.analyzeSentiment(text);
                case "translate" -> huggingFaceApiService.translate(text);
                case "improve", "explain" -> {
                    // 통계 기반 분석
                    String result = analysisType.equals("improve") ? 
                        improveSentence(text) : explainSimply(text);
                    yield ResponseUtils.success("분석 완료", "result", result);
                }
                default -> huggingFaceApiService.summarize(text, 150, 30);
            };
            
        } catch (Exception e) {
            log.error("텍스트 분석 오류", e);
            return ResponseUtils.failure("분석 오류", e);
        }
    }
    
    // ========== Helper Methods ==========
    
    private String improveSentence(String text) {
        StringBuilder result = new StringBuilder();
        result.append("**개선된 텍스트:**\n\n");
        
        String improved = text;
        improved = improved.replaceAll("(\\b\\w+\\b)(\\s+\\1)+", "$1"); // 반복 단어 제거
        improved = improved.replaceAll("\\s+", " ").trim(); // 공백 정리
        improved = improved.replaceAll("\\s+([.,!?])", "$1"); // 문장 부호 정리
        
        result.append(improved).append("\n\n");
        result.append("**개선 사항:**\n");
        result.append("- 반복 단어 및 과도한 공백 제거\n");
        result.append("- 문장 부호 정리\n\n");
        result.append("**추가 제안:**\n");
        result.append("- 문장 길이가 적절한지 확인하세요\n");
        result.append("- 능동태 사용을 권장합니다");
        
        return result.toString();
    }
    
    private String explainSimply(String text) {
        StringBuilder result = new StringBuilder();
        result.append("**쉬운 설명 제안:**\n\n");
        result.append("**설명 가이드:**\n");
        result.append("1. 문장을 짧게 나누세요\n");
        result.append("2. 전문 용어 대신 일상 언어를 사용하세요\n");
        result.append("3. 예시를 들어 설명하세요\n");
        result.append("4. '무엇을', '왜', '어떻게'를 명확히 하세요\n\n");
        result.append("**예시:**\n");
        result.append("어렵게: '해당 시스템은 고효율 알고리즘을 활용합니다'\n");
        result.append("쉽게: '이 프로그램은 빠른 방법으로 작업합니다'");
        
        return result.toString();
    }
}
