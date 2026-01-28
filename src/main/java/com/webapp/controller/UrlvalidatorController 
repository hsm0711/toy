package com.webapp.controller;

import com.webapp.service.MenuService;
import com.webapp.service.UrlValidationService;
import com.webapp.util.ResponseUtils;
import com.webapp.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * URL 안전성 검증 컨트롤러
 * 피싱/사기 사이트 여부를 확인합니다.
 */
@Slf4j
@Controller
@RequestMapping("/url-validator")
@RequiredArgsConstructor
public class UrlValidatorController {
    
    private final MenuService menuService;
    private final UrlValidationService urlValidationService;
    
    @GetMapping
    public String urlValidatorPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "url-validator");
        return "url-validator";
    }
    
    /**
     * URL 안전성 검증 API
     */
    @PostMapping("/api/validate")
    @ResponseBody
    public Map<String, Object> validateUrl(@RequestBody Map<String, String> request) {
        try {
            String url = request.get("url");
            
            if (ValidationUtils.isEmpty(url)) {
                return ResponseUtils.failure("URL을 입력하세요.");
            }
            
            // URL 형식 보정
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            
            // URL 검증 수행
            Map<String, Object> result = urlValidationService.validateUrl(url);
            
            if ((Boolean) result.get("success")) {
                return ResponseUtils.builder()
                    .message("검증 완료")
                    .putAll(result)
                    .build();
            } else {
                return ResponseUtils.failure((String) result.get("message"));
            }
            
        } catch (Exception e) {
            log.error("URL 검증 오류", e);
            return ResponseUtils.failure("검증 오류", e);
        }
    }
}