package com.webapp.controller;

import com.webapp.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/uuid-generator")
@RequiredArgsConstructor
public class UuidGeneratorController {
    
    private final MenuService menuService;
    private static final SecureRandom random = new SecureRandom();
    
    @GetMapping
    public String uuidGeneratorPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "uuid-generator");
        return "uuid-generator";
    }
    
    @GetMapping("/api/uuid")
    @ResponseBody
    public Map<String, Object> generateUuid(@RequestParam(defaultValue = "1") int count) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (count < 1 || count > 100) {
                result.put("success", false);
                result.put("message", "생성 개수는 1~100 사이여야 합니다.");
                return result;
            }
            
            List<String> uuids = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                uuids.add(UUID.randomUUID().toString());
            }
            
            result.put("success", true);
            result.put("uuids", uuids);
            result.put("count", count);
            
        } catch (Exception e) {
            log.error("UUID 생성 오류", e);
            result.put("success", false);
            result.put("message", "생성 오류: " + e.getMessage());
        }
        
        return result;
    }
    
    @PostMapping("/api/random-string")
    @ResponseBody
    public Map<String, Object> generateRandomString(
            @RequestParam int length,
            @RequestParam(defaultValue = "true") boolean includeDigits,
            @RequestParam(defaultValue = "true") boolean includeLowercase,
            @RequestParam(defaultValue = "true") boolean includeUppercase,
            @RequestParam(defaultValue = "false") boolean includeSpecial,
            @RequestParam(defaultValue = "1") int count) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (length < 1 || length > 1000) {
                result.put("success", false);
                result.put("message", "길이는 1~1000 사이여야 합니다.");
                return result;
            }
            
            if (count < 1 || count > 100) {
                result.put("success", false);
                result.put("message", "생성 개수는 1~100 사이여야 합니다.");
                return result;
            }
            
            StringBuilder charset = new StringBuilder();
            if (includeDigits) charset.append("0123456789");
            if (includeLowercase) charset.append("abcdefghijklmnopqrstuvwxyz");
            if (includeUppercase) charset.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
            if (includeSpecial) charset.append("!@#$%^&*()-_=+[]{}|;:,.<>?");
            
            if (charset.length() == 0) {
                result.put("success", false);
                result.put("message", "최소 하나의 문자 집합을 선택해야 합니다.");
                return result;
            }
            
            List<String> randomStrings = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                randomStrings.add(generateString(charset.toString(), length));
            }
            
            result.put("success", true);
            result.put("strings", randomStrings);
            result.put("count", count);
            result.put("length", length);
            
        } catch (Exception e) {
            log.error("랜덤 문자열 생성 오류", e);
            result.put("success", false);
            result.put("message", "생성 오류: " + e.getMessage());
        }
        
        return result;
    }
    
    private String generateString(String charset, int length) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(charset.length());
            result.append(charset.charAt(index));
        }
        return result.toString();
    }
}
