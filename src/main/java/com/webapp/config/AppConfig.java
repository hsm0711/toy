package com.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 애플리케이션 전역 설정
 */
@Configuration
public class AppConfig {
    
    /**
     * RestTemplate Bean 등록
     * - 여러 서비스에서 재사용 가능
     * - 필요시 인터셉터, 에러 핸들러 등 추가 설정 가능
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}