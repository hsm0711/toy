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
 * AI 학습 도우미 컨트롤러
 * - OpenRouter API (Llama 3.2 3B Instruct) 사용
 * - 개념 설명, 문제 풀이, 퀴즈 생성
 */
@Slf4j
@Controller
@RequestMapping("/ai-study-helper")
@RequiredArgsConstructor
public class AiStudyHelperController {
    
    private final MenuService menuService;
    private final OpenRouterApiService openRouterApiService;
    
    @GetMapping
    public String aiStudyHelperPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "ai-study-helper");
        return "ai-study-helper";
    }
    
    /**
     * API: 학습 도움
     */
    @PostMapping("/api/help")
    @ResponseBody
    public Map<String, Object> studyHelp(@RequestBody Map<String, String> request) {
        try {
            String topic = request.get("topic");
            String type = request.get("type");
            String level = request.get("level");
            
            if (ValidationUtils.isEmpty(topic)) {
                return ResponseUtils.failure("학습할 주제를 입력하세요.");
            }
            
            if (topic.length() > 1000) {
                return ResponseUtils.failure("주제가 너무 깁니다. 1000자 이하로 줄여주세요.");
            }
            
            log.info("학습 도움 요청: type={}, level={}", type, level);
            
            return openRouterApiService.studyHelp(topic, type, level);
            
        } catch (Exception e) {
            log.error("학습 도움 오류", e);
            return ResponseUtils.failure("처리 오류", e);
        }
    }
}