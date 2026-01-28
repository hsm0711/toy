package com.webapp.controller;

import com.webapp.service.ClaudeApiProxyService;
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
 * - CSV/JSON 데이터 분석 및 인사이트 제공
 * - 서버 사이드 프록시로 Claude API 호출
 */
@Slf4j
@Controller
@RequestMapping("/ai-data-analyzer")
@RequiredArgsConstructor
public class AiDataAnalyzerController {
    
    private final MenuService menuService;
    private final ClaudeApiProxyService claudeApiProxyService;
    
    @GetMapping
    public String aiDataAnalyzerPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "ai-data-analyzer");
        return "ai-data-analyzer";
    }
    
    /**
     * API: 데이터 분석 (서버 프록시)
     */
    @PostMapping("/api/analyze")
    @ResponseBody
    public Map<String, Object> analyzeData(@RequestBody Map<String, Object> request) {
        try {
            List<Map<String, Object>> data = (List<Map<String, Object>>) request.get("data");
            
            if (data == null || data.isEmpty()) {
                return ResponseUtils.failure("분석할 데이터를 입력하세요.");
            }
            
            // 샘플 데이터만 전송 (최대 20개)
            List<Map<String, Object>> sampleData = data.size() > 20 
                ? data.subList(0, 20) 
                : data;
            
            String dataStr = new com.fasterxml.jackson.databind.ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(sampleData);
            
            String prompt = String.format("""
                다음 데이터를 분석하고 인사이트를 제공해주세요:

                데이터 (총 %d개 중 샘플):
                ```json
                %s
                ```

                다음 형식으로 분석해주세요:

                1. **기본 통계**
                   - 데이터 개수
                   - 컬럼 수
                   - 주요 필드

                2. **주요 발견사항** (3-5개)
                   - 데이터의 패턴이나 특징
                   - 눈에 띄는 인사이트

                3. **추세 및 상관관계**
                   - 발견된 추세
                   - 변수 간 관계

                4. **이상치 탐지**
                   - 비정상적인 데이터 포인트

                5. **제안사항**
                   - 데이터 활용 방안
                   - 추가 분석 제안

                구체적이고 실행 가능한 인사이트를 제공해주세요.
                """, data.size(), dataStr);
            
            return claudeApiProxyService.callClaude(prompt, 2500);
            
        } catch (Exception e) {
            log.error("데이터 분석 오류", e);
            return ResponseUtils.failure("분석 오류", e);
        }
    }
}