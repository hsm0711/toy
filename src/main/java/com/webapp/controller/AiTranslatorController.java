package com.webapp.controller;

import com.webapp.service.MenuService;
import com.webapp.service.OpenRouterApiService;
import com.webapp.util.ResponseUtils;
import com.webapp.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 다국어 번역기 컨트롤러
 * - OpenRouter API (Qwen 2 7B Instruct) 사용
 * - 한국어 ↔ 영어/일본어/중국어 자연스러운 번역
 */
@Slf4j
@Controller
@RequestMapping("/ai-translator")
@RequiredArgsConstructor
public class AiTranslatorController {
    
    private final MenuService menuService;
    private final OpenRouterApiService openRouterApiService;
    
    @GetMapping
    public String aiTranslatorPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "ai-translator");
        return "ai-translator";
    }
    
    /**
     * API: 번역
     */
    @PostMapping("/api/translate")
    @ResponseBody
    public Map<String, Object> translate(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            String sourceLang = request.get("sourceLang");
            String targetLang = request.get("targetLang");
            
            if (ValidationUtils.isEmpty(text)) {
                return ResponseUtils.failure("번역할 텍스트를 입력하세요.");
            }
            
            if (text.length() > 2000) {
                return ResponseUtils.failure("텍스트가 너무 깁니다. 2000자 이하로 줄여주세요.");
            }
            
            log.info("번역 요청: {} -> {}", sourceLang, targetLang);
            
            return openRouterApiService.translateText(text, sourceLang, targetLang);
            
        } catch (Exception e) {
            log.error("번역 오류", e);
            return ResponseUtils.failure("번역 오류", e);
        }
    }
}