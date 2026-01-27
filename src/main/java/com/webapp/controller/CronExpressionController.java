package com.webapp.controller;

import com.webapp.service.MenuService;
import com.webapp.util.ResponseUtils;
import com.webapp.util.ValidationUtils;
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
        try {
            String expression = request.get("expression");
            
            if (ValidationUtils.isEmpty(expression)) {
                return ResponseUtils.failure("Cron 표현식을 입력하세요.");
            }
            
            String[] parts = expression.trim().split("\\s+");
            
            if (parts.length < 5 || parts.length > 6) {
                return ResponseUtils.failure("올바른 Cron 표현식이 아닙니다. (5개 또는 6개 필드 필요)");
            }
            
            String description = buildDescription(parts);
            String type = parts.length == 6 ? "Spring Cron (6 fields)" : "Unix Cron (5 fields)";
            
            return ResponseUtils.builder()
                .message("분석 완료")
                .put("expression", expression)
                .put("type", type)
                .put("description", description)
                .build();
            
        } catch (Exception e) {
            log.error("Cron 표현식 분석 오류", e);
            return ResponseUtils.failure("분석 오류", e);
        }
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
        
        try {
            String expression;
            
            if ("spring".equals(type)) {
                expression = buildSpringCron(second, minute, hour, day, month, weekday);
            } else {
                expression = buildUnixCron(minute, hour, day, month, weekday);
            }
            
            return ResponseUtils.builder()
                .message("생성 완료")
                .put("expression", expression)
                .put("type", type)
                .build();
            
        } catch (Exception e) {
            log.error("Cron 표현식 생성 오류", e);
            return ResponseUtils.failure("생성 오류", e);
        }
    }
    
    // Private helper methods
    
    private String buildDescription(String[] parts) {
        StringBuilder description = new StringBuilder();
        
        if (parts.length == 6) {
            // Spring Cron: 초 분 시 일 월 요일
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
        
        return description.toString();
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
    
    private String buildSpringCron(String second, String minute, String hour, 
                                   String day, String month, String weekday) {
        return String.format("%s %s %s %s %s %s",
            defaultIfEmpty(second, "*"),
            defaultIfEmpty(minute, "*"),
            defaultIfEmpty(hour, "*"),
            defaultIfEmpty(day, "*"),
            defaultIfEmpty(month, "*"),
            defaultIfEmpty(weekday, "*")
        );
    }
    
    private String buildUnixCron(String minute, String hour, String day, 
                                 String month, String weekday) {
        return String.format("%s %s %s %s %s",
            defaultIfEmpty(minute, "*"),
            defaultIfEmpty(hour, "*"),
            defaultIfEmpty(day, "*"),
            defaultIfEmpty(month, "*"),
            defaultIfEmpty(weekday, "*")
        );
    }
    
    private String defaultIfEmpty(String value, String defaultValue) {
        return ValidationUtils.isEmpty(value) ? defaultValue : value;
    }
}