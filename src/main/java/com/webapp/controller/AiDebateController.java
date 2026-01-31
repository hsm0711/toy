package com.webapp.controller;

import com.webapp.service.AiDebateService;
import com.webapp.service.MenuService;
import com.webapp.util.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * AI vs AI 토론 배틀 컨트롤러
 * - OpenRouter API를 사용하여 두 AI 모델 간의 토론을 시뮬레이션
 */
@Slf4j
@Controller
@RequestMapping("/ai-debate") // HTTP 엔드포인트를 위해 @RequestMapping 유지
@RequiredArgsConstructor
public class AiDebateController {

    private final MenuService menuService;
    private final AiDebateService aiDebateService; // AiDebateService 주입

    @Value("${ai.debate.max-turns}")
    private int maxDebateTurns;

    /**
     * AI 토론 배틀 페이지 렌더링
     */
    @GetMapping
    public String aiDebatePage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "ai-debate");
        return "ai-debate"; // ai-debate.html 템플릿 반환
    }

    /**
     * HTTP API: AI 토론 초기화 (세션 ID 발급)
     * 클라이언트가 WebSocket 연결 후 사용할 세션 ID를 발급합니다.
     */
    @PostMapping("/api/start-debate")
    @ResponseBody
    public Map<String, Object> initializeDebate(@RequestBody Map<String, String> request) {
        String topic = request.get("topic"); // 주제는 여기서 검증만 하고 실제 시작은 WebSocket으로
        String turnsStr = request.get("turns");

        if (topic == null || topic.trim().isEmpty()) {
            return ResponseUtils.failure("토론 주제를 입력해주세요.");
        }

        int requestedTurns = maxDebateTurns;
        if (turnsStr != null && !turnsStr.trim().isEmpty()) {
            try {
                requestedTurns = Integer.parseInt(turnsStr);
                if (requestedTurns <= 0) {
                    return ResponseUtils.failure("토론 턴 수는 1 이상이어야 합니다.");
                }
            } catch (NumberFormatException e) {
                return ResponseUtils.failure("유효하지 않은 토론 턴 수입니다.");
            }
        }
        
        // 고유한 세션 ID 생성
        String sessionId = UUID.randomUUID().toString();
        log.info("AI 토론 초기화 요청 - 세션 ID: {}, 주제: {}, 요청 턴 수: {}", sessionId, topic, requestedTurns);

        // 여기서는 실제 토론을 시작하지 않고 세션 ID만 반환
        return ResponseUtils.success("AI 토론 세션이 준비되었습니다.", "sessionId", sessionId);
    }

    /**
     * WebSocket STOMP: AI 토론 시작
     * 클라이언트로부터 STOMP 메시지를 받아 실제 토론을 시작합니다.
     */
    @MessageMapping("/ai-debate/start") // 클라이언트에서 /app/ai-debate/start 로 메시지 전송
    public void startDebateViaWebSocket(@Payload Map<String, String> request) {
        try {
            String sessionId = request.get("sessionId");
            String topic = request.get("topic");
            String turnsStr = request.get("turns");

            if (sessionId == null || sessionId.trim().isEmpty()) {
                log.error("Missing sessionId for WebSocket debate start.");
                return; // 클라이언트에게 오류 알림 로직 추가 필요
            }
            if (topic == null || topic.trim().isEmpty()) {
                aiDebateService.sendErrorToClient(sessionId, "토론 주제를 입력해주세요.");
                return;
            }

            int requestedTurns = maxDebateTurns;
            if (turnsStr != null && !turnsStr.trim().isEmpty()) {
                try {
                    requestedTurns = Integer.parseInt(turnsStr);
                    if (requestedTurns <= 0) {
                        aiDebateService.sendErrorToClient(sessionId, "토론 턴 수는 1 이상이어야 합니다.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    aiDebateService.sendErrorToClient(sessionId, "유효하지 않은 토론 턴 수입니다.");
                    return;
                }
            }

            final int actualDebateTurns = Math.min(requestedTurns, maxDebateTurns);

            log.info("WebSocket AI 토론 시작 - 세션 ID: {}, 주제: {}, 요청 턴 수: {}, 실제 토론 턴 수: {}", sessionId, topic, requestedTurns, actualDebateTurns);

            // 비동기 서비스 호출
            aiDebateService.startDebateAsync(sessionId, topic, actualDebateTurns);

        } catch (Exception e) {
            log.error("WebSocket AI 토론 시작 중 오류 발생", e);
            // 클라이언트에게 오류 메시지를 보낼 수 있도록 AiDebateService에 메서드 추가 필요
            // (이미 AiDebateService에 sendErrorToClient 있음)
            String sessionId = request.get("sessionId");
            if (sessionId != null) {
                aiDebateService.sendErrorToClient(sessionId, "AI 토론 시작 중 오류 발생: " + e.getMessage());
            }
        }
    }
}
