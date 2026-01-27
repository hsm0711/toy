package com.webapp.util;

import java.util.HashMap;
import java.util.Map;

/**
 * API 응답 생성을 위한 유틸리티 클래스
 * 모든 컨트롤러에서 일관된 응답 형식을 사용하도록 지원
 */
public class ResponseUtils {
    
    /**
     * 성공 응답 생성 (데이터 없음)
     */
    public static Map<String, Object> success(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }
    
    /**
     * 성공 응답 생성 (데이터 포함)
     */
    public static Map<String, Object> success(String message, Map<String, Object> data) {
        Map<String, Object> response = success(message);
        if (data != null) {
            response.putAll(data);
        }
        return response;
    }
    
    /**
     * 성공 응답 생성 (단일 데이터)
     */
    public static Map<String, Object> success(String message, String key, Object value) {
        Map<String, Object> response = success(message);
        response.put(key, value);
        return response;
    }
    
    /**
     * 실패 응답 생성
     */
    public static Map<String, Object> failure(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
    
    /**
     * 실패 응답 생성 (상세 정보 포함)
     */
    public static Map<String, Object> failure(String message, String details) {
        Map<String, Object> response = failure(message);
        response.put("details", details);
        return response;
    }
    
    /**
     * 실패 응답 생성 (예외 정보 포함)
     */
    public static Map<String, Object> failure(String message, Exception e) {
        Map<String, Object> response = failure(message);
        response.put("details", e.getMessage());
        return response;
    }
    
    /**
     * 데이터 응답 생성 (성공 플래그 자동 설정)
     */
    public static Map<String, Object> data(String key, Object value) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put(key, value);
        return response;
    }
    
    /**
     * 빌더 패턴을 위한 ResponseBuilder 클래스
     */
    public static class Builder {
        private final Map<String, Object> response = new HashMap<>();
        
        public Builder() {
            response.put("success", true);
        }
        
        public Builder success(boolean success) {
            response.put("success", success);
            return this;
        }
        
        public Builder message(String message) {
            response.put("message", message);
            return this;
        }
        
        public Builder put(String key, Object value) {
            response.put(key, value);
            return this;
        }
        
        public Builder putAll(Map<String, Object> data) {
            if (data != null) {
                response.putAll(data);
            }
            return this;
        }
        
        public Map<String, Object> build() {
            return response;
        }
    }
    
    /**
     * 빌더 인스턴스 생성
     */
    public static Builder builder() {
        return new Builder();
    }
}