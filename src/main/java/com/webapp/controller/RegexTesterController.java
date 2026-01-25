package com.webapp.controller;

import com.webapp.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.*;

@Slf4j
@Controller
@RequestMapping("/regex-tester")
@RequiredArgsConstructor
public class RegexTesterController {
    
    private final MenuService menuService;
    
    @GetMapping
    public String regexTesterPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "regex-tester");
        return "regex-tester";
    }
    
    @PostMapping("/api/test")
    @ResponseBody
    public Map<String, Object> testRegex(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String regex = request.get("regex");
            String testString = request.get("testString");
            String flags = request.get("flags");
            
            if (regex == null || regex.isEmpty()) {
                result.put("success", false);
                result.put("message", "정규식을 입력하세요.");
                return result;
            }
            
            if (testString == null) {
                testString = "";
            }
            
            int regexFlags = 0;
            if (flags != null) {
                if (flags.contains("i")) regexFlags |= Pattern.CASE_INSENSITIVE;
                if (flags.contains("m")) regexFlags |= Pattern.MULTILINE;
                if (flags.contains("s")) regexFlags |= Pattern.DOTALL;
            }
            
            Pattern pattern = Pattern.compile(regex, regexFlags);
            Matcher matcher = pattern.matcher(testString);
            
            List<Map<String, Object>> matches = new ArrayList<>();
            int matchCount = 0;
            
            while (matcher.find()) {
                matchCount++;
                Map<String, Object> match = new HashMap<>();
                match.put("index", matchCount);
                match.put("value", matcher.group());
                match.put("start", matcher.start());
                match.put("end", matcher.end());
                
                List<String> groups = new ArrayList<>();
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    groups.add(matcher.group(i));
                }
                match.put("groups", groups);
                
                matches.add(match);
            }
            
            result.put("success", true);
            result.put("matched", matchCount > 0);
            result.put("matchCount", matchCount);
            result.put("matches", matches);
            
        } catch (PatternSyntaxException e) {
            log.error("정규식 구문 오류", e);
            result.put("success", false);
            result.put("message", "정규식 구문 오류: " + e.getDescription());
        } catch (Exception e) {
            log.error("정규식 테스트 오류", e);
            result.put("success", false);
            result.put("message", "테스트 오류: " + e.getMessage());
        }
        
        return result;
    }
}
