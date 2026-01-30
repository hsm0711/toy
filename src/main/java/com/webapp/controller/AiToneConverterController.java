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
 * AI 문장 톤 변환기 컨트롤러
 * - OpenRouter API (Llama 3.1 8B Instruct) 사용
 * - 서버 사이드에서 API 호출
 */
@Slf4j
@Controller
@RequestMapping("/ai-tone-converter")
@RequiredArgsConstructor
public class AiToneConverterController {
    
    private final MenuService menuService;
    private final OpenRouterApiService openRouterApiService;
    
    @GetMapping
    public String aiToneConverterPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "ai-tone-converter");
        return "ai-tone-converter";
    }
    
    /**
     * API: 문장 톤 변환
     */
    @PostMapping("/api/convert")
    @ResponseBody
    public Map<String, Object> convertTone(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            String tone = request.get("tone");
            
            // 입력 검증
            if (ValidationUtils.isEmpty(text)) {
                return ResponseUtils.failure("변환할 문장을 입력하세요.");
            }
            
            if (text.length() > 2000) {
                return ResponseUtils.failure("텍스트가 너무 깁니다. 2000자 이하로 줄여주세요.");
            }
            
            if (ValidationUtils.isEmpty(tone)) {
                tone = "polite"; // 기본값: 정중한 톤
            }
            
            log.info("톤 변환 요청: tone={}, length={}", tone, text.length());
            
            // OpenRouter API 호출
            return openRouterApiService.transformTone(text, tone);
            
        } catch (Exception e) {
            log.error("톤 변환 오류", e);
            return ResponseUtils.failure("변환 오류", e);
        }
    }
}