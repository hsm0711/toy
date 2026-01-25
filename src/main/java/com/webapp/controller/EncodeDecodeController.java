package com.webapp.controller;

import com.webapp.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
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
        Map<String, Object> result = new HashMap<>();
        
        try {
            String input = request.get("input");
            if (input == null || input.isEmpty()) {
                result.put("success", false);
                result.put("message", "입력값이 비어있습니다.");
                return result;
            }
            
            String encoded = Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
            
            result.put("success", true);
            result.put("result", encoded);
            
        } catch (Exception e) {
            log.error("Base64 인코딩 오류", e);
            result.put("success", false);
            result.put("message", "인코딩 오류: " + e.getMessage());
        }
        
        return result;
    }
    
    @PostMapping("/api/base64-decode")
    @ResponseBody
    public Map<String, Object> base64Decode(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String input = request.get("input");
            if (input == null || input.isEmpty()) {
                result.put("success", false);
                result.put("message", "입력값이 비어있습니다.");
                return result;
            }
            
            byte[] decodedBytes = Base64.getDecoder().decode(input);
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
            
            result.put("success", true);
            result.put("result", decoded);
            
        } catch (IllegalArgumentException e) {
            log.error("Base64 디코딩 오류", e);
            result.put("success", false);
            result.put("message", "잘못된 Base64 형식입니다.");
        } catch (Exception e) {
            log.error("Base64 디코딩 오류", e);
            result.put("success", false);
            result.put("message", "디코딩 오류: " + e.getMessage());
        }
        
        return result;
    }
    
    @PostMapping("/api/url-encode")
    @ResponseBody
    public Map<String, Object> urlEncode(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String input = request.get("input");
            if (input == null || input.isEmpty()) {
                result.put("success", false);
                result.put("message", "입력값이 비어있습니다.");
                return result;
            }
            
            String encoded = UriUtils.encode(input, StandardCharsets.UTF_8);
            
            result.put("success", true);
            result.put("result", encoded);
            
        } catch (Exception e) {
            log.error("URL 인코딩 오류", e);
            result.put("success", false);
            result.put("message", "인코딩 오류: " + e.getMessage());
        }
        
        return result;
    }
    
    @PostMapping("/api/url-decode")
    @ResponseBody
    public Map<String, Object> urlDecode(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String input = request.get("input");
            if (input == null || input.isEmpty()) {
                result.put("success", false);
                result.put("message", "입력값이 비어있습니다.");
                return result;
            }
            
            String decoded = UriUtils.decode(input, StandardCharsets.UTF_8);
            
            result.put("success", true);
            result.put("result", decoded);
            
        } catch (Exception e) {
            log.error("URL 디코딩 오류", e);
            result.put("success", false);
            result.put("message", "디코딩 오류: " + e.getMessage());
        }
        
        return result;
    }
    
    @PostMapping("/api/html-escape")
    @ResponseBody
    public Map<String, Object> htmlEscape(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String input = request.get("input");
            if (input == null || input.isEmpty()) {
                result.put("success", false);
                result.put("message", "입력값이 비어있습니다.");
                return result;
            }
            
            String escaped = HtmlUtils.htmlEscape(input);
            
            result.put("success", true);
            result.put("result", escaped);
            
        } catch (Exception e) {
            log.error("HTML Escape 오류", e);
            result.put("success", false);
            result.put("message", "변환 오류: " + e.getMessage());
        }
        
        return result;
    }
    
    @PostMapping("/api/html-unescape")
    @ResponseBody
    public Map<String, Object> htmlUnescape(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String input = request.get("input");
            if (input == null || input.isEmpty()) {
                result.put("success", false);
                result.put("message", "입력값이 비어있습니다.");
                return result;
            }
            
            String unescaped = HtmlUtils.htmlUnescape(input);
            
            result.put("success", true);
            result.put("result", unescaped);
            
        } catch (Exception e) {
            log.error("HTML Unescape 오류", e);
            result.put("success", false);
            result.put("message", "변환 오류: " + e.getMessage());
        }
        
        return result;
    }
}
