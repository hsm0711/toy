package com.webapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * URL 안전성 검증 서비스
 * 피싱, 사기 사이트 여부를 확인합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UrlValidationService {
    
    private final RestTemplate restTemplate;
    
    // 의심스러운 키워드 패턴
    private static final List<String> SUSPICIOUS_KEYWORDS = Arrays.asList(
        "login", "signin", "verify", "secure", "account", "update", 
        "confirm", "suspended", "billing", "payment", "credential",
        "password", "urgent", "action-required", "click-here"
    );
    
    // 의심스러운 TLD (Top Level Domain)
    private static final List<String> SUSPICIOUS_TLDS = Arrays.asList(
        ".tk", ".ml", ".ga", ".cf", ".gq", ".xyz", ".top", ".pw"
    );
    
    // IP 주소 패턴
    private static final Pattern IP_PATTERN = Pattern.compile(
        "^(https?://)?((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}"
    );
    
    // URL 단축 서비스
    private static final List<String> URL_SHORTENERS = Arrays.asList(
        "bit.ly", "tinyurl.com", "goo.gl", "ow.ly", "t.co", 
        "is.gd", "buff.ly", "adf.ly"
    );
    
    /**
     * URL 종합 검증
     */
    public Map<String, Object> validateUrl(String urlString) {
        Map<String, Object> result = new HashMap<>();
        List<String> warnings = new ArrayList<>();
        List<String> checks = new ArrayList<>();
        int riskScore = 0;
        
        try {
            URL url = new URL(urlString);
            String host = url.getHost().toLowerCase();
            String path = url.getPath().toLowerCase();
            String fullUrl = urlString.toLowerCase();
            
            // 1. IP 주소 체크
            if (IP_PATTERN.matcher(urlString).find()) {
                warnings.add("⚠️ IP 주소를 직접 사용하고 있습니다 (도메인명 대신)");
                riskScore += 30;
            } else {
                checks.add("✓ 도메인명 사용");
            }
            
            // 2. HTTPS 체크
            if (!urlString.startsWith("https://")) {
                warnings.add("⚠️ HTTPS가 아닌 HTTP를 사용합니다 (보안 취약)");
                riskScore += 20;
            } else {
                checks.add("✓ HTTPS 사용");
            }
            
            // 3. 의심스러운 TLD 체크
            boolean suspiciousTld = SUSPICIOUS_TLDS.stream()
                .anyMatch(host::endsWith);
            if (suspiciousTld) {
                warnings.add("⚠️ 의심스러운 최상위 도메인 (.tk, .ml 등)");
                riskScore += 25;
            } else {
                checks.add("✓ 일반적인 도메인 확장자");
            }
            
            // 4. URL 단축 서비스 체크
            boolean isShortened = URL_SHORTENERS.stream()
                .anyMatch(host::contains);
            if (isShortened) {
                warnings.add("⚠️ URL 단축 서비스 사용 (실제 목적지 불명확)");
                riskScore += 15;
            } else {
                checks.add("✓ 직접 URL");
            }
            
            // 5. 의심스러운 키워드 체크
            long suspiciousCount = SUSPICIOUS_KEYWORDS.stream()
                .filter(fullUrl::contains)
                .count();
            if (suspiciousCount >= 2) {
                warnings.add("⚠️ 의심스러운 키워드 다수 포함 (" + suspiciousCount + "개)");
                riskScore += (int)(suspiciousCount * 10);
            } else if (suspiciousCount == 1) {
                warnings.add("ℹ️ 주의가 필요한 키워드 포함");
                riskScore += 5;
            } else {
                checks.add("✓ 의심스러운 키워드 없음");
            }
            
            // 6. 과도한 서브도메인 체크
            String[] domainParts = host.split("\\.");
            if (domainParts.length > 4) {
                warnings.add("⚠️ 과도한 서브도메인 사용");
                riskScore += 15;
            } else {
                checks.add("✓ 정상적인 도메인 구조");
            }
            
            // 7. 의심스러운 문자 체크
            if (host.contains("--") || host.contains("..")) {
                warnings.add("⚠️ 비정상적인 문자 패턴");
                riskScore += 20;
            } else {
                checks.add("✓ 정상적인 문자 사용");
            }
            
            // 8. 유명 브랜드 모방 체크
            if (checkBrandSpoofing(host)) {
                warnings.add("🚨 유명 브랜드 도메인 모방 의심");
                riskScore += 40;
            } else {
                checks.add("✓ 브랜드 모방 없음");
            }
            
            // 9. 포트 번호 체크
            int port = url.getPort();
            if (port != -1 && port != 80 && port != 443) {
                warnings.add("⚠️ 비표준 포트 사용: " + port);
                riskScore += 10;
            } else {
                checks.add("✓ 표준 포트 사용");
            }
            
            // 10. 과도하게 긴 URL 체크
            if (urlString.length() > 200) {
                warnings.add("⚠️ 비정상적으로 긴 URL");
                riskScore += 10;
            } else {
                checks.add("✓ 적절한 URL 길이");
            }
            
            // 위험도 판정
            String riskLevel;
            String recommendation;
            
            if (riskScore >= 70) {
                riskLevel = "HIGH_RISK";
                recommendation = "🚨 매우 위험: 접속하지 마세요!";
            } else if (riskScore >= 40) {
                riskLevel = "MEDIUM_RISK";
                recommendation = "⚠️ 주의 필요: 신뢰할 수 있는 출처인지 확인하세요";
            } else if (riskScore >= 20) {
                riskLevel = "LOW_RISK";
                recommendation = "ℹ️ 낮은 위험: 주의하여 접속하세요";
            } else {
                riskLevel = "SAFE";
                recommendation = "✅ 안전: 일반적으로 안전한 URL입니다";
            }
            
            result.put("success", true);
            result.put("url", urlString);
            result.put("host", host);
            result.put("riskScore", riskScore);
            result.put("riskLevel", riskLevel);
            result.put("recommendation", recommendation);
            result.put("warnings", warnings);
            result.put("checks", checks);
            result.put("warningCount", warnings.size());
            result.put("checkCount", checks.size());
            
        } catch (Exception e) {
            log.error("URL 검증 오류", e);
            result.put("success", false);
            result.put("message", "URL 분석 실패: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 유명 브랜드 도메인 모방 체크
     */
    private boolean checkBrandSpoofing(String host) {
        Map<String, String> brands = new HashMap<>();
        brands.put("google", "google.com");
        brands.put("facebook", "facebook.com");
        brands.put("amazon", "amazon.com");
        brands.put("paypal", "paypal.com");
        brands.put("apple", "apple.com");
        brands.put("microsoft", "microsoft.com");
        brands.put("netflix", "netflix.com");
        brands.put("instagram", "instagram.com");
        brands.put("twitter", "twitter.com");
        brands.put("linkedin", "linkedin.com");
        
        for (Map.Entry<String, String> entry : brands.entrySet()) {
            String brand = entry.getKey();
            String legitDomain = entry.getValue();
            
            // 브랜드명을 포함하지만 정식 도메인이 아닌 경우
            if (host.contains(brand) && !host.equals(legitDomain) && !host.endsWith("." + legitDomain)) {
                // 예: google-login.com, paypal-verify.tk 등
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 도메인 연령 확인 (간이 버전)
     */
    public Map<String, Object> checkDomainAge(String domain) {
        Map<String, Object> result = new HashMap<>();
        
        // 실제 구현에서는 WHOIS API를 사용
        // 여기서는 간단한 체크만 수행
        result.put("checked", true);
        result.put("message", "도메인 연령 확인 기능은 추가 API 키가 필요합니다.");
        
        return result;
    }
}