package com.webapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // "/topic"으로 시작하는 메시지를 브로커가 처리
        config.setApplicationDestinationPrefixes("/app"); // "/app"으로 시작하는 메시지는 @MessageMapping 메서드로 라우팅
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결을 위한 STOMP 엔드포인트 "/ws" 등록
        // SockJS를 사용하여 WebSocket을 지원하지 않는 브라우저를 위한 Fallback 옵션 활성화
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
    }
}
