package com.webapp.controller;

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
 */
@Slf4j
@Controller
@RequestMapping("/ai-text-analyzer")
@RequiredArgsConstructor
public class AiTextAnalyzerController {
    
    private final MenuService menuService;
    
    @GetMapping
    public String aiTextAnalyzerPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "ai-text-analyzer");
        return "ai-text-analyzer";
    }
    
    /**
     * API: 텍스트 분석 (브라우저에서 직접 Claude API 호출)
     * 서버는 단순히 페이지만 제공
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
            
            // 실제 분석은 클라이언트 측에서 Claude API를 직접 호출하도록 함
            // 이 엔드포인트는 검증 목적으로만 사용
            return ResponseUtils.builder()
                .message("브라우저에서 직접 AI 분석을 수행합니다.")
                .put("text", text)
                .put("analysisType", analysisType)
                .build();
            
        } catch (Exception e) {
            log.error("텍스트 분석 오류", e);
            return ResponseUtils.failure("분석 오류", e);
        }
    }
}