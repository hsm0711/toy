package com.webapp.controller;

import com.webapp.service.MenuService;
import com.webapp.util.ResponseUtils;
import com.webapp.util.ValidationUtils;
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
        try {
            ValidationUtils.requireInRange(count, 1, 100, "생성 개수");
            
            List<String> uuids = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                uuids.add(UUID.randomUUID().toString());
            }
            
            return ResponseUtils.builder()
                .message("UUID 생성 완료")
                .put("uuids", uuids)
                .put("count", count)
                .build();
            
        } catch (IllegalArgumentException e) {
            return ResponseUtils.failure(e.getMessage());
        } catch (Exception e) {
            log.error("UUID 생성 오류", e);
            return ResponseUtils.failure("생성 오류", e);
        }
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
        
        try {
            ValidationUtils.requireInRange(length, 1, 1000, "길이");
            ValidationUtils.requireInRange(count, 1, 100, "생성 개수");
            
            StringBuilder charset = new StringBuilder();
            if (includeDigits) charset.append("0123456789");
            if (includeLowercase) charset.append("abcdefghijklmnopqrstuvwxyz");
            if (includeUppercase) charset.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
            if (includeSpecial) charset.append("!@#$%^&*()-_=+[]{}|;:,.<>?");
            
            if (charset.length() == 0) {
                return ResponseUtils.failure("최소 하나의 문자 집합을 선택해야 합니다.");
            }
            
            List<String> randomStrings = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                randomStrings.add(generateString(charset.toString(), length));
            }
            
            return ResponseUtils.builder()
                .message("문자열 생성 완료")
                .put("strings", randomStrings)
                .put("count", count)
                .put("length", length)
                .build();
            
        } catch (IllegalArgumentException e) {
            return ResponseUtils.failure(e.getMessage());
        } catch (Exception e) {
            log.error("랜덤 문자열 생성 오류", e);
            return ResponseUtils.failure("생성 오류", e);
        }
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