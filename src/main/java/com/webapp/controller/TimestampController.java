package com.webapp.controller;

import com.webapp.service.MenuService;
import com.webapp.util.ResponseUtils;
import com.webapp.util.ValidationUtils;
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
        
        try {
            ZoneId zoneId = ZoneId.of(timezone);
            
            // Timestamp -> DateTime 변환
            if (timestamp != null) {
                return convertFromTimestamp(timestamp, zoneId, isMillis, timezone);
            }
            // DateTime -> Timestamp 변환
            else if (ValidationUtils.isNotEmpty(dateTime)) {
                return convertFromDateTime(dateTime, zoneId, timezone);
            } else {
                return ResponseUtils.failure("timestamp 또는 dateTime 중 하나는 필수입니다.");
            }
            
        } catch (DateTimeException e) {
            log.error("Timezone 오류", e);
            return ResponseUtils.failure("잘못된 타임존입니다.", e);
        } catch (Exception e) {
            log.error("Timestamp 변환 오류", e);
            return ResponseUtils.failure("변환 오류", e);
        }
    }
    
    @GetMapping("/api/current")
    @ResponseBody
    public Map<String, Object> getCurrentTime(
            @RequestParam(required = false, defaultValue = "UTC") String timezone) {
        
        try {
            ZoneId zoneId = ZoneId.of(timezone);
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            Instant instant = now.toInstant();
            
            Map<String, Object> data = new HashMap<>();
            data.put("timestampSeconds", instant.getEpochSecond());
            data.put("timestampMillis", instant.toEpochMilli());
            data.put("dateTime", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            data.put("iso8601", now.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
            data.put("readable", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
            data.put("timezone", timezone);
            
            return ResponseUtils.success("조회 완료", data);
            
        } catch (Exception e) {
            log.error("현재 시간 조회 오류", e);
            return ResponseUtils.failure("오류", e);
        }
    }
    
    // Private helper methods
    
    private Map<String, Object> convertFromTimestamp(Long timestamp, ZoneId zoneId, boolean isMillis, String timezone) {
        Instant instant = isMillis ? 
            Instant.ofEpochMilli(timestamp) : 
            Instant.ofEpochSecond(timestamp);
        
        ZonedDateTime zdt = instant.atZone(zoneId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", timestamp);
        data.put("timestampSeconds", instant.getEpochSecond());
        data.put("timestampMillis", instant.toEpochMilli());
        data.put("dateTime", zdt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        data.put("iso8601", zdt.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        data.put("readable", zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
        data.put("timezone", timezone);
        
        return ResponseUtils.success("변환 완료", data);
    }
    
    private Map<String, Object> convertFromDateTime(String dateTime, ZoneId zoneId, String timezone) {
        LocalDateTime ldt = LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        ZonedDateTime zdt = ldt.atZone(zoneId);
        Instant instant = zdt.toInstant();
        
        Map<String, Object> data = new HashMap<>();
        data.put("dateTime", dateTime);
        data.put("timestampSeconds", instant.getEpochSecond());
        data.put("timestampMillis", instant.toEpochMilli());
        data.put("iso8601", zdt.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        data.put("timezone", timezone);
        
        return ResponseUtils.success("변환 완료", data);
    }
}