package com.webapp.controller;

import com.webapp.service.MenuService;
import com.webapp.util.ResponseUtils;
import com.webapp.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/encode-decode")
@RequiredArgsConstructor
public class EncodeDecodeController {
    
    private final MenuService menuService;
    
    @GetMapping
    public String encodeDecodePage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "encode-decode");
        return "encode-decode";
    }
    
    @PostMapping("/api/base64-encode")
    @ResponseBody
    public Map<String, Object> base64Encode(@RequestBody Map<String, String> request) {
        try {
            String input = request.get("input");
            
            if (ValidationUtils.isEmpty(input)) {
                return ResponseUtils.failure("입력값이 비어있습니다.");
            }
            
            String encoded = Base64.getEncoder().encodeToString(
                input.getBytes(StandardCharsets.UTF_8)
            );
            
            return ResponseUtils.success("인코딩 완료", "result", encoded);
            
        } catch (Exception e) {
            log.error("Base64 인코딩 오류", e);
            return ResponseUtils.failure("인코딩 오류", e);
        }
    }
    
    @PostMapping("/api/base64-decode")
    @ResponseBody
    public Map<String, Object> base64Decode(@RequestBody Map<String, String> request) {
        try {
            String input = request.get("input");
            
            if (ValidationUtils.isEmpty(input)) {
                return ResponseUtils.failure("입력값이 비어있습니다.");
            }
            
            if (!ValidationUtils.isBase64(input)) {
                return ResponseUtils.failure("잘못된 Base64 형식입니다.");
            }
            
            byte[] decodedBytes = Base64.getDecoder().decode(input);
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
            
            return ResponseUtils.success("디코딩 완료", "result", decoded);
            
        } catch (IllegalArgumentException e) {
            log.error("Base64 디코딩 오류", e);
            return ResponseUtils.failure("잘못된 Base64 형식입니다.");
        } catch (Exception e) {
            log.error("Base64 디코딩 오류", e);
            return ResponseUtils.failure("디코딩 오류", e);
        }
    }
    
    @PostMapping("/api/url-encode")
    @ResponseBody
    public Map<String, Object> urlEncode(@RequestBody Map<String, String> request) {
        try {
            String input = request.get("input");
            
            if (ValidationUtils.isEmpty(input)) {
                return ResponseUtils.failure("입력값이 비어있습니다.");
            }
            
            String encoded = UriUtils.encode(input, StandardCharsets.UTF_8);
            return ResponseUtils.success("인코딩 완료", "result", encoded);
            
        } catch (Exception e) {
            log.error("URL 인코딩 오류", e);
            return ResponseUtils.failure("인코딩 오류", e);
        }
    }
    
    @PostMapping("/api/url-decode")
    @ResponseBody
    public Map<String, Object> urlDecode(@RequestBody Map<String, String> request) {
        try {
            String input = request.get("input");
            
            if (ValidationUtils.isEmpty(input)) {
                return ResponseUtils.failure("입력값이 비어있습니다.");
            }
            
            String decoded = UriUtils.decode(input, StandardCharsets.UTF_8);
            return ResponseUtils.success("디코딩 완료", "result", decoded);
            
        } catch (Exception e) {
            log.error("URL 디코딩 오류", e);
            return ResponseUtils.failure("디코딩 오류", e);
        }
    }
    
    @PostMapping("/api/html-escape")
    @ResponseBody
    public Map<String, Object> htmlEscape(@RequestBody Map<String, String> request) {
        try {
            String input = request.get("input");
            
            if (ValidationUtils.isEmpty(input)) {
                return ResponseUtils.failure("입력값이 비어있습니다.");
            }
            
            String escaped = HtmlUtils.htmlEscape(input);
            return ResponseUtils.success("변환 완료", "result", escaped);
            
        } catch (Exception e) {
            log.error("HTML Escape 오류", e);
            return ResponseUtils.failure("변환 오류", e);
        }
    }
    
    @PostMapping("/api/html-unescape")
    @ResponseBody
    public Map<String, Object> htmlUnescape(@RequestBody Map<String, String> request) {
        try {
            String input = request.get("input");
            
            if (ValidationUtils.isEmpty(input)) {
                return ResponseUtils.failure("입력값이 비어있습니다.");
            }
            
            String unescaped = HtmlUtils.htmlUnescape(input);
            return ResponseUtils.success("변환 완료", "result", unescaped);
            
        } catch (Exception e) {
            log.error("HTML Unescape 오류", e);
            return ResponseUtils.failure("변환 오류", e);
        }
    }
}