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
 * AI 코드 리뷰 컨트롤러
 * - 코드 품질 분석, 버그 발견, 개선 제안
 */
@Slf4j
@Controller
@RequestMapping("/ai-code-reviewer")
@RequiredArgsConstructor
public class AiCodeReviewerController {
    
    private final MenuService menuService;
    
    @GetMapping
    public String aiCodeReviewerPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "ai-code-reviewer");
        return "ai-code-reviewer";
    }
}