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
 * AI 코드 리뷰어 컨트롤러
 * - Hugging Face Inference API 사용 (정적 분석)
 * - 서버 사이드에서 처리
 */
@Slf4j
@Controller
@RequestMapping("/ai-code-reviewer")
@RequiredArgsConstructor
public class AiCodeReviewerController {
    
    private final MenuService menuService;
    private final HuggingFaceApiService huggingFaceApiService;
    
    @GetMapping
    public String aiCodeReviewerPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "ai-code-reviewer");
        return "ai-code-reviewer";
    }
    
    /**
     * API: 코드 리뷰
     */
    @PostMapping("/api/review")
    @ResponseBody
    public Map<String, Object> reviewCode(@RequestBody Map<String, String> request) {
        try {
            String code = request.get("code");
            String language = request.get("language");
            
            if (ValidationUtils.isEmpty(code)) {
                return ResponseUtils.failure("리뷰할 코드를 입력하세요.");
            }
            
            // Hugging Face API 서비스 호출
            return huggingFaceApiService.reviewCode(code, language);
            
        } catch (Exception e) {
            log.error("코드 리뷰 오류", e);
            return ResponseUtils.failure("리뷰 오류", e);
        }
    }
}
