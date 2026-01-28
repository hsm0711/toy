package com.webapp.controller;

import com.webapp.service.ClaudeApiProxyService;
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
 * - 요약, 분석, 키워드 추출 등
 * - 서버 사이드 프록시로 Claude API 호출
 */
@Slf4j
@Controller
@RequestMapping("/ai-text-analyzer")
@RequiredArgsConstructor
public class AiTextAnalyzerController {
    
    private final MenuService menuService;
    private final ClaudeApiProxyService claudeApiProxyService;
    
    @GetMapping
    public String aiTextAnalyzerPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "ai-text-analyzer");
        return "ai-text-analyzer";
    }
    
    /**
     * API: 텍스트 분석 (서버 프록시)
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
            
            // 프롬프트 생성
            String prompt = buildPrompt(analysisType, text);
            
            // Claude API 호출 (서버 프록시)
            return claudeApiProxyService.callClaude(prompt, 2000);
            
        } catch (Exception e) {
            log.error("텍스트 분석 오류", e);
            return ResponseUtils.failure("분석 오류", e);
        }
    }
    
    private String buildPrompt(String type, String text) {
        return switch (type) {
            case "summary" -> "다음 텍스트를 3-5문장으로 핵심만 간결하게 요약해주세요:\n\n" + text;
            case "keywords" -> "다음 텍스트에서 가장 중요한 키워드 10개를 추출하고, 각 키워드에 대해 간단히 설명해주세요:\n\n" + text;
            case "sentiment" -> "다음 텍스트의 감정을 분석하고 (긍정/부정/중립), 그 이유를 설명해주세요:\n\n" + text;
            case "translate" -> "다음 텍스트를 한국어로 자연스럽게 번역해주세요:\n\n" + text;
            case "improve" -> "다음 텍스트를 더 명확하고 전문적으로 개선해주세요. 원래 의미는 유지하되 문법, 어휘, 구조를 개선해주세요:\n\n" + text;
            case "explain" -> "다음 텍스트를 중학생도 이해할 수 있도록 쉽게 설명해주세요. 어려운 용어는 풀어서 설명해주세요:\n\n" + text;
            default -> "다음 텍스트를 3-5문장으로 요약해주세요:\n\n" + text;
        };
    }
}