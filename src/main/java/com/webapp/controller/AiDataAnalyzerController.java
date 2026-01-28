package com.webapp.controller;

import com.webapp.service.HuggingFaceApiService;
import com.webapp.service.MenuService;
import com.webapp.util.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 데이터 분석 컨트롤러
 * - Hugging Face Inference API 사용 (통계 분석)
 * - 서버 사이드에서 처리
 */
@Slf4j
@Controller
@RequestMapping("/ai-data-analyzer")
@RequiredArgsConstructor
public class AiDataAnalyzerController {
    
    private final MenuService menuService;
    private final HuggingFaceApiService huggingFaceApiService;
    
    @GetMapping
    public String aiDataAnalyzerPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "ai-data-analyzer");
        return "ai-data-analyzer";
    }
    
    /**
     * API: 데이터 분석
     */
    @PostMapping("/api/analyze")
    @ResponseBody
    public Map<String, Object> analyzeData(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) request.get("data");
            
            if (data == null || data.isEmpty()) {
                return ResponseUtils.failure("분석할 데이터를 입력하세요.");
            }
            
            // Hugging Face API 서비스 호출 (통계 분석)
            return huggingFaceApiService.analyzeData(data);
            
        } catch (Exception e) {
            log.error("데이터 분석 오류", e);
            return ResponseUtils.failure("분석 오류", e);
        }
    }
}
