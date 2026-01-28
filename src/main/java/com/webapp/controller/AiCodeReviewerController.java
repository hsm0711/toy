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
 * AI 코드 리뷰 컨트롤러
 * - 코드 품질 분석, 버그 발견, 개선 제안
 * - 서버 사이드 프록시로 Claude API 호출
 */
@Slf4j
@Controller
@RequestMapping("/ai-code-reviewer")
@RequiredArgsConstructor
public class AiCodeReviewerController {
    
    private final MenuService menuService;
    private final ClaudeApiProxyService claudeApiProxyService;
    
    @GetMapping
    public String aiCodeReviewerPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "ai-code-reviewer");
        return "ai-code-reviewer";
    }
    
    /**
     * API: 코드 리뷰 (서버 프록시)
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
            
            String languageName = getLanguageName(language);
            
            String prompt = String.format("""
                당신은 전문 코드 리뷰어입니다. 다음 %s 코드를 분석하고 상세한 리뷰를 제공해주세요.

                코드:
                ```%s
                %s
                ```

                다음 형식으로 리뷰해주세요:

                1. **코드 품질 점수** (0-100점): [점수]점
                   - 간단한 평가

                2. **발견된 문제점**
                   - 🔴 심각: [심각한 버그나 보안 이슈]
                   - 🟡 보통: [개선이 필요한 부분]
                   - 🟢 경미: [사소한 개선사항]

                3. **개선 제안**
                   - 구체적인 개선 방법
                   - 개선된 코드 예시

                4. **긍정적인 부분**
                   - 잘 작성된 부분

                5. **전체 평가**
                   - 종합 의견

                명확하고 구체적으로 작성해주세요.
                """, languageName, language, code);
            
            return claudeApiProxyService.callClaude(prompt, 3000);
            
        } catch (Exception e) {
            log.error("코드 리뷰 오류", e);
            return ResponseUtils.failure("리뷰 오류", e);
        }
    }
    
    private String getLanguageName(String lang) {
        return switch (lang) {
            case "java" -> "Java";
            case "python" -> "Python";
            case "javascript" -> "JavaScript";
            case "typescript" -> "TypeScript";
            case "cpp" -> "C++";
            case "csharp" -> "C#";
            case "go" -> "Go";
            case "rust" -> "Rust";
            default -> lang;
        };
    }
}