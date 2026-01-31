package com.webapp.service;

import com.webapp.util.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate; // WebSocket 메시지 전송용
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiDebateService {

    private final OpenRouterApiService openRouterApiService;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket 메시지 전송용

    @Value("${ai.debate.max-turns}")
    private int maxDebateTurns;

    private static final String MODEL_AI1 = "meta-llama/llama-3.1-8b-instruct"; // AI 1 모델
    private static final String MODEL_AI2 = "qwen/qwen-2.5-7b-instruct";        // AI 2 모델

    // 진행 중인 토론의 상태를 관리 (예: sessionId -> conversationHistory)
    private final Map<String, StringBuilder> ongoingDebates = new ConcurrentHashMap<>();

    @Async // 비동기적으로 실행
    public void startDebateAsync(String sessionId, String topic, int requestedTurns) {
        log.info("비동기 AI 토론 시작 - 세션 ID: {}, 주제: {}, 요청 턴 수: {}", sessionId, topic, requestedTurns);

        StringBuilder conversationHistory = new StringBuilder();
        ongoingDebates.put(sessionId, conversationHistory); // 새 토론 등록

        try {
            final int actualDebateTurns = Math.min(requestedTurns, maxDebateTurns);

            // AI 1 (찬성) 초기 프롬프트 (침착맨 스타일)
            String initialPromptAi1 = String.format(
                "당신은 인터넷 방송인 침착맨처럼 행동하는 AI 토론자입니다. 주어진 주제에 대해 찬성하는 입장을 유쾌하고 다소 엉뚱한 비유와 함께 주장해주세요. 너무 진지하지 않게, 살짝 능청스럽거나 허를 찌르는 발언으로 토론을 이끌어가세요. 이전 대화를 참고하되, 핵심을 찌르거나 시시콜콜한 농담을 섞어 논리적으로 반박하거나 주장을 강화하세요. 주제: %s", topic
            );

            // AI 2 (반대) 초기 프롬프트 (주호민 스타일)
            String initialPromptAi2 = String.format(
                "당신은 웹툰 작가 주호민처럼 행동하는 AI 토론자입니다. 주어진 주제에 대해 반대하는 입장을 현실적이고 냉소적인 시각으로 주장해주세요. 간결하면서도 핵심을 꿰뚫는 분석과 함께, 때로는 엉뚱하지만 설득력 있는 논리를 펼치세요. 이전 대화를 참고하여 불편한 진실을 끄집어내거나, 예상치 못한 방향으로 토론을 이끌어 반박하거나 주장을 강화하세요. 주제: %s", topic
            );

            // 첫 번째 턴 (AI 1 시작)
            String ai1Response = callAiModel(MODEL_AI1, initialPromptAi1, 300, 0.7);
            if (ai1Response == null) {
                sendErrorToClient(sessionId, "AI 1 응답 생성 실패");
                return;
            }
            sendDebateUpdate(sessionId, "AI 1", ai1Response, false);
            conversationHistory.append("AI 1: ").append(ai1Response).append("\n");
            Thread.sleep(1000); // UI 업데이트를 위한 딜레이

            for (int i = 1; i < actualDebateTurns; i++) {
                // AI 2 턴
                String promptForAi2 = initialPromptAi2 + "\n\n현재까지의 토론:\n" + conversationHistory.toString();
                String ai2Response = callAiModel(MODEL_AI2, promptForAi2, 300, 0.7);
                if (ai2Response == null) {
                    sendErrorToClient(sessionId, "AI 2 응답 생성 실패");
                    return;
                }
                sendDebateUpdate(sessionId, "AI 2", ai2Response, false);
                conversationHistory.append("AI 2: ").append(ai2Response).append("\n");
                Thread.sleep(1000); // UI 업데이트를 위한 딜레이

                if (i < actualDebateTurns - 1) { // 마지막 턴은 AI 2가 마무리
                    // AI 1 턴
                    String promptForAi1 = initialPromptAi1 + "\n\n현재까지의 토론:\n" + conversationHistory.toString();
                    ai1Response = callAiModel(MODEL_AI1, promptForAi1, 300, 0.7);
                    if (ai1Response == null) {
                        sendErrorToClient(sessionId, "AI 1 응답 생성 실패");
                        return;
                    }
                    sendDebateUpdate(sessionId, "AI 1", ai1Response, false);
                    conversationHistory.append("AI 1: ").append(ai1Response).append("\n");
                    Thread.sleep(1000); // UI 업데이트를 위한 딜레이
                }
            }
            sendDebateUpdate(sessionId, "System", "AI 토론이 완료되었습니다.", true); // 토론 완료 메시지

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("AI 토론 비동기 스레드 중단됨", e);
            sendErrorToClient(sessionId, "AI 토론 중단됨");
        } catch (Exception e) {
            log.error("비동기 AI 토론 중 오류 발생", e);
            sendErrorToClient(sessionId, "AI 토론 중 오류 발생: " + e.getMessage());
        } finally {
            ongoingDebates.remove(sessionId); // 토론 종료 후 상태 제거
            log.info("비동기 AI 토론 종료 - 세션 ID: {}", sessionId);
        }
    }

    private String callAiModel(String model, String prompt, int maxTokens, double temperature) {
        Map<String, Object> apiResponse = openRouterApiService.callOpenRouterModel(model, prompt, maxTokens, temperature, null);
        if (apiResponse != null && (Boolean) apiResponse.get("success")) {
            return (String) apiResponse.get("result");
        }
        return null;
    }

    private void sendDebateUpdate(String sessionId, String speaker, String message, boolean isCompleted) {
        Map<String, Object> payload = Map.of(
            "sessionId", sessionId,
            "speaker", speaker,
            "message", message,
            "isCompleted", isCompleted
        );
        // 클라이언트의 고유한 세션 ID를 사용하여 특정 클라이언트에게 메시지를 보냄
        // /topic/ai-debate-updates/{sessionId} 로 메시지를 보냄
        messagingTemplate.convertAndSend("/topic/ai-debate-updates/" + sessionId, payload);
        log.debug("Debate update sent to sessionId {}: {} - {}", sessionId, speaker, message);
    }

    public void sendErrorToClient(String sessionId, String errorMessage) {
        Map<String, Object> payload = Map.of(
            "sessionId", sessionId,
            "speaker", "Error",
            "message", errorMessage,
            "isCompleted", true
        );
        messagingTemplate.convertAndSend("/topic/ai-debate-updates/" + sessionId, payload);
        log.error("Debate error sent to sessionId {}: {}", sessionId, errorMessage);
    }
}
