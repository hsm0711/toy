package com.webapp.util;

import java.util.regex.Pattern;

/**
 * 입력 검증을 위한 유틸리티 클래스
 */
public class ValidationUtils {
    
    // 정규식 패턴
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)?[\\w.-]+\\.[a-zA-Z]{2,}(/.*)?$"
    );
    
    private static final Pattern IP_PATTERN = Pattern.compile(
        "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"
    );
    
    /**
     * 빈 문자열 체크
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    /**
     * 비어있지 않은지 체크
     */
    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }
    
    /**
     * 필수 입력 검증
     */
    public static void requireNonEmpty(String value, String fieldName) {
        if (isEmpty(value)) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수 입력 항목입니다.");
        }
    }
    
    /**
     * 숫자 범위 검증
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }
    
    /**
     * 숫자 범위 검증 (long)
     */
    public static boolean isInRange(long value, long min, long max) {
        return value >= min && value <= max;
    }
    
    /**
     * 범위 검증 및 예외 발생
     */
    public static void requireInRange(int value, int min, int max, String fieldName) {
        if (!isInRange(value, min, max)) {
            throw new IllegalArgumentException(
                fieldName + "은(는) " + min + "~" + max + " 사이여야 합니다."
            );
        }
    }
    
    /**
     * 이메일 형식 검증
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * URL 형식 검증
     */
    public static boolean isValidUrl(String url) {
        if (isEmpty(url)) {
            return false;
        }
        return URL_PATTERN.matcher(url).matches();
    }
    
    /**
     * IP 주소 형식 검증
     */
    public static boolean isValidIp(String ip) {
        if (isEmpty(ip)) {
            return false;
        }
        return IP_PATTERN.matcher(ip).matches();
    }
    
    /**
     * 포트 번호 검증
     */
    public static boolean isValidPort(int port) {
        return isInRange(port, 1, 65535);
    }
    
    /**
     * 파일 확장자 검증
     */
    public static boolean hasExtension(String filename, String... extensions) {
        if (isEmpty(filename)) {
            return false;
        }
        
        String lowerFilename = filename.toLowerCase();
        for (String ext : extensions) {
            if (lowerFilename.endsWith("." + ext.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * PDF 파일 여부 검증
     */
    public static boolean isPdfFile(String filename, String contentType) {
        return hasExtension(filename, "pdf") || 
               (contentType != null && contentType.equals("application/pdf"));
    }
    
    /**
     * Base64 문자열 검증
     */
    public static boolean isBase64(String value) {
        if (isEmpty(value)) {
            return false;
        }
        return value.matches("^[A-Za-z0-9+/]*={0,2}$");
    }
    
    /**
     * CIDR 표기법 검증
     */
    public static boolean isValidCidr(String cidr) {
        if (isEmpty(cidr)) {
            return false;
        }
        
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            return false;
        }
        
        if (!isValidIp(parts[0])) {
            return false;
        }
        
        try {
            int prefix = Integer.parseInt(parts[1]);
            return isInRange(prefix, 0, 32);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}