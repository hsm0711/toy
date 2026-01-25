package com.webapp.controller;

import com.webapp.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/health-check")
@RequiredArgsConstructor
public class HealthCheckController {
    
    private final MenuService menuService;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @GetMapping
    public String healthCheckPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "health-check");
        return "health-check";
    }
    
    @PostMapping("/api/port-check")
    @ResponseBody
    public Map<String, Object> checkPort(
            @RequestParam String host,
            @RequestParam int port,
            @RequestParam(defaultValue = "5000") int timeout) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (host == null || host.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "호스트를 입력하세요.");
                return result;
            }
            
            if (port < 1 || port > 65535) {
                result.put("success", false);
                result.put("message", "포트는 1~65535 사이여야 합니다.");
                return result;
            }
            
            long startTime = System.currentTimeMillis();
            boolean isOpen = false;
            
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), timeout);
                isOpen = socket.isConnected();
            } catch (IOException e) {
                log.debug("포트 연결 실패: {}:{} - {}", host, port, e.getMessage());
            }
            
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            
            result.put("success", true);
            result.put("host", host);
            result.put("port", port);
            result.put("isOpen", isOpen);
            result.put("responseTime", responseTime);
            result.put("status", isOpen ? "OPEN" : "CLOSED");
            
        } catch (Exception e) {
            log.error("포트 체크 오류", e);
            result.put("success", false);
            result.put("message", "체크 오류: " + e.getMessage());
        }
        
        return result;
    }
    
    @PostMapping("/api/url-check")
    @ResponseBody
    public Map<String, Object> checkUrl(
            @RequestParam String url,
            @RequestParam(defaultValue = "5000") int timeout) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (url == null || url.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "URL을 입력하세요.");
                return result;
            }
            
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            
            long startTime = System.currentTimeMillis();
            int statusCode = 0;
            String statusText = "";
            boolean isSuccess = false;
            
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    String.class
                );
                
                statusCode = response.getStatusCode().value();
                statusText = response.getStatusCode().toString();
                isSuccess = response.getStatusCode().is2xxSuccessful();
                
            } catch (Exception e) {
                log.debug("URL 체크 실패: {} - {}", url, e.getMessage());
                statusText = "ERROR: " + e.getMessage();
            }
            
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            
            result.put("success", true);
            result.put("url", url);
            result.put("statusCode", statusCode);
            result.put("statusText", statusText);
            result.put("isSuccess", isSuccess);
            result.put("responseTime", responseTime);
            
        } catch (Exception e) {
            log.error("URL 체크 오류", e);
            result.put("success", false);
            result.put("message", "체크 오류: " + e.getMessage());
        }
        
        return result;
    }
}
