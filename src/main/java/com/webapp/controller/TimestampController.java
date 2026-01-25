package com.webapp.controller;

import com.webapp.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/timestamp")
@RequiredArgsConstructor
public class TimestampController {
    
    private final MenuService menuService;
    
    @GetMapping
    public String timestampPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "timestamp");
        return "timestamp";
    }
    
    @PostMapping("/api/convert")
    @ResponseBody
    public Map<String, Object> convertTimestamp(
            @RequestParam(required = false) Long timestamp,
            @RequestParam(required = false) String dateTime,
            @RequestParam(required = false, defaultValue = "UTC") String timezone,
            @RequestParam(required = false, defaultValue = "false") boolean isMillis) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            ZoneId zoneId = ZoneId.of(timezone);
            
            // Timestamp -> DateTime 변환
            if (timestamp != null) {
                Instant instant;
                if (isMillis) {
                    instant = Instant.ofEpochMilli(timestamp);
                } else {
                    instant = Instant.ofEpochSecond(timestamp);
                }
                
                ZonedDateTime zdt = instant.atZone(zoneId);
                
                result.put("success", true);
                result.put("timestamp", timestamp);
                result.put("timestampSeconds", instant.getEpochSecond());
                result.put("timestampMillis", instant.toEpochMilli());
                result.put("dateTime", zdt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                result.put("iso8601", zdt.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
                result.put("readable", zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
                result.put("timezone", timezone);
            }
            // DateTime -> Timestamp 변환
            else if (dateTime != null && !dateTime.isEmpty()) {
                LocalDateTime ldt = LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                ZonedDateTime zdt = ldt.atZone(zoneId);
                Instant instant = zdt.toInstant();
                
                result.put("success", true);
                result.put("dateTime", dateTime);
                result.put("timestampSeconds", instant.getEpochSecond());
                result.put("timestampMillis", instant.toEpochMilli());
                result.put("iso8601", zdt.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
                result.put("timezone", timezone);
            } else {
                result.put("success", false);
                result.put("message", "timestamp 또는 dateTime 중 하나는 필수입니다.");
            }
            
        } catch (Exception e) {
            log.error("Timestamp 변환 오류", e);
            result.put("success", false);
            result.put("message", "변환 오류: " + e.getMessage());
        }
        
        return result;
    }
    
    @GetMapping("/api/current")
    @ResponseBody
    public Map<String, Object> getCurrentTime(
            @RequestParam(required = false, defaultValue = "UTC") String timezone) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            ZoneId zoneId = ZoneId.of(timezone);
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            Instant instant = now.toInstant();
            
            result.put("success", true);
            result.put("timestampSeconds", instant.getEpochSecond());
            result.put("timestampMillis", instant.toEpochMilli());
            result.put("dateTime", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            result.put("iso8601", now.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
            result.put("readable", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
            result.put("timezone", timezone);
            
        } catch (Exception e) {
            log.error("현재 시간 조회 오류", e);
            result.put("success", false);
            result.put("message", "오류: " + e.getMessage());
        }
        
        return result;
    }
}
