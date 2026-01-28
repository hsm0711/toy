package com.webapp.controller;

import com.webapp.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * AI 데이터 분석 컨트롤러
 * - CSV/JSON 데이터 분석 및 인사이트 제공
 */
@Slf4j
@Controller
@RequestMapping("/ai-data-analyzer")
@RequiredArgsConstructor
public class AiDataAnalyzerController {
    
    private final MenuService menuService;
    
    @GetMapping
    public String aiDataAnalyzerPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "ai-data-analyzer");
        return "ai-data-analyzer";
    }
}