package com.webapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.webapp.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/json-yaml")
@RequiredArgsConstructor
public class JsonYamlController {
    
    private final MenuService menuService;
    private final ObjectMapper jsonMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    private final ObjectMapper yamlMapper = new ObjectMapper(
            new YAMLFactory()
                    .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
    );
    
    @GetMapping
    public String jsonYamlPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "json-yaml");
        return "json-yaml";
    }
    
    @PostMapping("/api/json-to-yaml")
    @ResponseBody
    public Map<String, Object> jsonToYaml(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String json = request.get("input");
            if (json == null || json.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "JSON 데이터를 입력하세요.");
                return result;
            }
            
            Object jsonObject = jsonMapper.readValue(json, Object.class);
            String yaml = yamlMapper.writeValueAsString(jsonObject);
            
            result.put("success", true);
            result.put("result", yaml);
            
        } catch (Exception e) {
            log.error("JSON to YAML 변환 오류", e);
            result.put("success", false);
            result.put("message", "변환 오류: " + e.getMessage());
            result.put("details", getErrorDetails(e));
        }
        
        return result;
    }
    
    @PostMapping("/api/yaml-to-json")
    @ResponseBody
    public Map<String, Object> yamlToJson(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String yaml = request.get("input");
            if (yaml == null || yaml.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "YAML 데이터를 입력하세요.");
                return result;
            }
            
            Object yamlObject = yamlMapper.readValue(yaml, Object.class);
            String json = jsonMapper.writeValueAsString(yamlObject);
            
            result.put("success", true);
            result.put("result", json);
            
        } catch (Exception e) {
            log.error("YAML to JSON 변환 오류", e);
            result.put("success", false);
            result.put("message", "변환 오류: " + e.getMessage());
            result.put("details", getErrorDetails(e));
        }
        
        return result;
    }
    
    @PostMapping("/api/format-json")
    @ResponseBody
    public Map<String, Object> formatJson(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String json = request.get("input");
            if (json == null || json.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "JSON 데이터를 입력하세요.");
                return result;
            }
            
            Object jsonObject = jsonMapper.readValue(json, Object.class);
            String formatted = jsonMapper.writeValueAsString(jsonObject);
            
            result.put("success", true);
            result.put("result", formatted);
            
        } catch (Exception e) {
            log.error("JSON 포맷팅 오류", e);
            result.put("success", false);
            result.put("message", "포맷팅 오류: " + e.getMessage());
            result.put("details", getErrorDetails(e));
        }
        
        return result;
    }
    
    private String getErrorDetails(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return "Unknown error";
        }
        
        if (message.contains("line")) {
            return message.substring(0, Math.min(message.length(), 200));
        }
        
        return message.length() > 200 ? message.substring(0, 200) + "..." : message;
    }
}
