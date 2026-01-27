package com.webapp.controller;

import com.webapp.service.MenuService;
import com.webapp.util.ResponseUtils;
import com.webapp.util.ValidationUtils;
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
        
        try {
            if (ValidationUtils.isEmpty(host)) {
                return ResponseUtils.failure("호스트를 입력하세요.");
            }
            
            if (!ValidationUtils.isValidPort(port)) {
                return ResponseUtils.failure("포트는 1~65535 사이여야 합니다.");
            }
            
            long startTime = System.currentTimeMillis();
            boolean isOpen = checkPortConnection(host, port, timeout);
            long responseTime = System.currentTimeMillis() - startTime;
            
            Map<String, Object> data = new HashMap<>();
            data.put("host", host);
            data.put("port", port);
            data.put("isOpen", isOpen);
            data.put("responseTime", responseTime);
            data.put("status", isOpen ? "OPEN" : "CLOSED");
            
            return ResponseUtils.success("체크 완료", data);
            
        } catch (Exception e) {
            log.error("포트 체크 오류", e);
            return ResponseUtils.failure("체크 오류", e);
        }
    }
    
    @PostMapping("/api/url-check")
    @ResponseBody
    public Map<String, Object> checkUrl(
            @RequestParam String url,
            @RequestParam(defaultValue = "5000") int timeout) {
        
        try {
            if (ValidationUtils.isEmpty(url)) {
                return ResponseUtils.failure("URL을 입력하세요.");
            }
            
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            
            long startTime = System.currentTimeMillis();
            UrlCheckResult result = checkUrlConnection(url);
            long responseTime = System.currentTimeMillis() - startTime;
            
            Map<String, Object> data = new HashMap<>();
            data.put("url", url);
            data.put("statusCode", result.statusCode);
            data.put("statusText", result.statusText);
            data.put("isSuccess", result.isSuccess);
            data.put("responseTime", responseTime);
            
            return ResponseUtils.success("체크 완료", data);
            
        } catch (Exception e) {
            log.error("URL 체크 오류", e);
            return ResponseUtils.failure("체크 오류", e);
        }
    }
    
    // Private helper methods
    
    private boolean checkPortConnection(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return socket.isConnected();
        } catch (IOException e) {
            log.debug("포트 연결 실패: {}:{} - {}", host, port, e.getMessage());
            return false;
        }
    }
    
    private UrlCheckResult checkUrlConnection(String url) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                String.class
            );
            
            return new UrlCheckResult(
                response.getStatusCode().value(),
                response.getStatusCode().toString(),
                response.getStatusCode().is2xxSuccessful()
            );
            
        } catch (Exception e) {
            log.debug("URL 체크 실패: {} - {}", url, e.getMessage());
            return new UrlCheckResult(0, "ERROR: " + e.getMessage(), false);
        }
    }
    
    // Inner class for URL check result
    private static class UrlCheckResult {
        final int statusCode;
        final String statusText;
        final boolean isSuccess;
        
        UrlCheckResult(int statusCode, String statusText, boolean isSuccess) {
            this.statusCode = statusCode;
            this.statusText = statusText;
            this.isSuccess = isSuccess;
        }
    }
}