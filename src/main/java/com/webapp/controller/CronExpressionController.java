package com.webapp.controller;

import com.webapp.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@Controller
@RequestMapping("/cron-expression")
@RequiredArgsConstructor
public class CronExpressionController {
    
    private final MenuService menuService;
    
    @GetMapping
    public String cronExpressionPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "cron-expression");
        return "cron-expression";
    }
    
    @PostMapping("/api/describe")
    @ResponseBody
    public Map<String, Object> describeCron(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String expression = request.get("expression");
            if (expression == null || expression.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Cron 표현식을 입력하세요.");
                return result;
            }
            
            String[] parts = expression.trim().split("\\s+");
            
            if (parts.length < 5 || parts.length > 6) {
                result.put("success", false);
                result.put("message", "올바른 Cron 표현식이 아닙니다. (5개 또는 6개 필드 필요)");
                return result;
            }
            
            StringBuilder description = new StringBuilder();
            
            // Spring Cron: 초 분 시 일 월 요일
            if (parts.length == 6) {
                description.append(describeField(parts[0], "초")).append(" ");
                description.append(describeField(parts[1], "분")).append(" ");
                description.append(describeField(parts[2], "시")).append(" ");
                description.append(describeField(parts[3], "일")).append(" ");
                description.append(describeField(parts[4], "월")).append(" ");
                description.append(describeField(parts[5], "요일"));
            } else {
                // Unix Cron: 분 시 일 월 요일
                description.append(describeField(parts[0], "분")).append(" ");
                description.append(describeField(parts[1], "시")).append(" ");
                description.append(describeField(parts[2], "일")).append(" ");
                description.append(describeField(parts[3], "월")).append(" ");
                description.append(describeField(parts[4], "요일"));
            }
            
            result.put("success", true);
            result.put("description", description.toString());
            result.put("expression", expression);
            result.put("type", parts.length == 6 ? "Spring Cron (6 fields)" : "Unix Cron (5 fields)");
            
        } catch (Exception e) {
            log.error("Cron 표현식 분석 오류", e);
            result.put("success", false);
            result.put("message", "분석 오류: " + e.getMessage());
        }
        
        return result;
    }
    
    @PostMapping("/api/generate")
    @ResponseBody
    public Map<String, Object> generateCron(
            @RequestParam String type,
            @RequestParam(required = false) String second,
            @RequestParam(required = false) String minute,
            @RequestParam(required = false) String hour,
            @RequestParam(required = false) String day,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String weekday) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            String expression;
            
            if ("spring".equals(type)) {
                expression = String.format("%s %s %s %s %s %s",
                    second != null && !second.isEmpty() ? second : "*",
                    minute != null && !minute.isEmpty() ? minute : "*",
                    hour != null && !hour.isEmpty() ? hour : "*",
                    day != null && !day.isEmpty() ? day : "*",
                    month != null && !month.isEmpty() ? month : "*",
                    weekday != null && !weekday.isEmpty() ? weekday : "*"
                );
            } else {
                expression = String.format("%s %s %s %s %s",
                    minute != null && !minute.isEmpty() ? minute : "*",
                    hour != null && !hour.isEmpty() ? hour : "*",
                    day != null && !day.isEmpty() ? day : "*",
                    month != null && !month.isEmpty() ? month : "*",
                    weekday != null && !weekday.isEmpty() ? weekday : "*"
                );
            }
            
            result.put("success", true);
            result.put("expression", expression);
            result.put("type", type);
            
        } catch (Exception e) {
            log.error("Cron 표현식 생성 오류", e);
            result.put("success", false);
            result.put("message", "생성 오류: " + e.getMessage());
        }
        
        return result;
    }
    
    private String describeField(String value, String fieldName) {
        if ("*".equals(value)) {
            return "매 " + fieldName;
        } else if (value.contains("/")) {
            String[] parts = value.split("/");
            return "매 " + parts[1] + fieldName + "마다";
        } else if (value.contains("-")) {
            String[] parts = value.split("-");
            return fieldName + " " + parts[0] + "부터 " + parts[1] + "까지";
        } else if (value.contains(",")) {
            return fieldName + " " + value.replace(",", ", ");
        } else {
            return fieldName + " " + value;
        }
    }
}
