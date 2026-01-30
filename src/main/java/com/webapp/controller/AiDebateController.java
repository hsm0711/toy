import org.springframework.beans.factory.annotation.Value;
import com.webapp.service.MenuService;
import com.webapp.service.OpenRouterApiService;
import com.webapp.util.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

/**
 * AI vs AI 토론 배틀 컨트롤러
 * - OpenRouter API를 사용하여 두 AI 모델 간의 토론을 시뮬레이션
 */
@Slf4j
@Controller
@RequestMapping("/ai-debate")
@RequiredArgsConstructor
public class AiDebateController {

    private final MenuService menuService;
    private final OpenRouterApiService openRouterApiService; // AI 모델과 통신하기 위한 서비스

    @Value("${ai.debate.max-turns}")
    private int maxDebateTurns;

    private static final String MODEL_AI1 = "meta-llama/llama-3.1-8b-instruct"; // AI 1 모델
    private static final String MODEL_AI2 = "qwen/qwen-2.5-7b-instruct";        // AI 2 모델

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
     * API: AI 토론 시작
     * 토론 주제를 받아 두 AI 모델 간의 토론을 시작하고 결과를 반환합니다.
     */
    @PostMapping("/api/start-debate")
    @ResponseBody
    public Map<String, Object> startDebate(@RequestBody Map<String, String> request) {
        try {
            String topic = request.get("topic");
            String turnsStr = request.get("turns");

            if (topic == null || topic.trim().isEmpty()) {
                return ResponseUtils.failure("토론 주제를 입력해주세요.");
            }

            int requestedTurns = maxDebateTurns; // 기본값은 서버 설정
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

            final int actualDebateTurns = Math.min(requestedTurns, maxDebateTurns);

            log.info("AI 토론 시작 요청 - 주제: {}, 요청 턴 수: {}, 실제 토론 턴 수: {}", topic, requestedTurns, actualDebateTurns);

            List<Map<String, String>> debateLogList = new ArrayList<>();
            StringBuilder conversationHistory = new StringBuilder();

            // AI 1 (찬성) 초기 프롬프트
            String initialPromptAi1 = String.format(
                "당신은 토론자 AI 1입니다. 주어진 주제에 대해 찬성하는 입장을 취하고, 간결하게 당신의 주장을 펼치세요. " +
                "이전 대화를 참고하여 논리적으로 반박하거나 주장을 강화하세요. 주제: %s", topic
            );

            // AI 2 (반대) 초기 프롬프트
            String initialPromptAi2 = String.format(
                "당신은 토론자 AI 2입니다. 주어진 주제에 대해 반대하는 입장을 취하고, 간결하게 당신의 주장을 펼치세요. " +
                "이전 대화를 참고하여 논리적으로 반박하거나 주장을 강화하세요. 주제: %s", topic
            );

            // 첫 번째 턴 (AI 1 시작)
            String ai1Response = callAiModel(MODEL_AI1, initialPromptAi1, 300, 0.7);
            if (ai1Response == null) return ResponseUtils.failure("AI 1 응답 생성 실패");
            debateLogList.add(Map.of("speaker", "AI 1", "message", ai1Response));
            conversationHistory.append("AI 1: ").append(ai1Response).append("\n");

            for (int i = 1; i < actualDebateTurns; i++) {
                // AI 2 턴
                String promptForAi2 = initialPromptAi2 + "\n\n현재까지의 토론:\n" + conversationHistory.toString();
                String ai2Response = callAiModel(MODEL_AI2, promptForAi2, 300, 0.7);
                if (ai2Response == null) return ResponseUtils.failure("AI 2 응답 생성 실패");
                debateLogList.add(Map.of("speaker", "AI 2", "message", ai2Response));
                conversationHistory.append("AI 2: ").append(ai2Response).append("\n");

                if (i < actualDebateTurns - 1) { // 마지막 턴은 AI 2가 마무리
                    // AI 1 턴
                    String promptForAi1 = initialPromptAi1 + "\n\n현재까지의 토론:\n" + conversationHistory.toString();
                    ai1Response = callAiModel(MODEL_AI1, promptForAi1, 300, 0.7);
                    if (ai1Response == null) return ResponseUtils.failure("AI 1 응답 생성 실패");
                    debateLogList.add(Map.of("speaker", "AI 1", "message", ai1Response));
                    conversationHistory.append("AI 1: ").append(ai1Response).append("\n");
                }
            }

            return ResponseUtils.success("AI 토론이 완료되었습니다.", "debateLog", debateLogList);

        } catch (Exception e) {
            log.error("AI 토론 시작 오류", e);
            return ResponseUtils.failure("AI 토론 시작 중 오류 발생", e);
        }
    }

    private String callAiModel(String model, String prompt, int maxTokens, double temperature) {
        Map<String, Object> apiResponse = openRouterApiService.callOpenRouterModel(model, prompt, maxTokens, temperature, null);
        if (apiResponse != null && (Boolean) apiResponse.get("success")) {
            return (String) apiResponse.get("result");
        }
        return null;
    }
}
