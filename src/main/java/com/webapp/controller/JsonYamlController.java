package com.webapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.webapp.service.MenuService;
import com.webapp.util.ResponseUtils;
import com.webapp.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        try {
            String json = request.get("input");
            
            if (ValidationUtils.isEmpty(json)) {
                return ResponseUtils.failure("JSON 데이터를 입력하세요.");
            }
            
            Object jsonObject = jsonMapper.readValue(json, Object.class);
            String yaml = yamlMapper.writeValueAsString(jsonObject);
            
            return ResponseUtils.success("변환 완료", "result", yaml);
            
        } catch (Exception e) {
            log.error("JSON to YAML 변환 오류", e);
            return ResponseUtils.failure("변환 오류: " + e.getMessage(), getErrorDetails(e));
        }
    }
    
    @PostMapping("/api/yaml-to-json")
    @ResponseBody
    public Map<String, Object> yamlToJson(@RequestBody Map<String, String> request) {
        try {
            String yaml = request.get("input");
            
            if (ValidationUtils.isEmpty(yaml)) {
                return ResponseUtils.failure("YAML 데이터를 입력하세요.");
            }
            
            Object yamlObject = yamlMapper.readValue(yaml, Object.class);
            String json = jsonMapper.writeValueAsString(yamlObject);
            
            return ResponseUtils.success("변환 완료", "result", json);
            
        } catch (Exception e) {
            log.error("YAML to JSON 변환 오류", e);
            return ResponseUtils.failure("변환 오류: " + e.getMessage(), getErrorDetails(e));
        }
    }
    
    @PostMapping("/api/format-json")
    @ResponseBody
    public Map<String, Object> formatJson(@RequestBody Map<String, String> request) {
        try {
            String json = request.get("input");
            
            if (ValidationUtils.isEmpty(json)) {
                return ResponseUtils.failure("JSON 데이터를 입력하세요.");
            }
            
            Object jsonObject = jsonMapper.readValue(json, Object.class);
            String formatted = jsonMapper.writeValueAsString(jsonObject);
            
            return ResponseUtils.success("포맷팅 완료", "result", formatted);
            
        } catch (Exception e) {
            log.error("JSON 포맷팅 오류", e);
            return ResponseUtils.failure("포맷팅 오류: " + e.getMessage(), getErrorDetails(e));
        }
    }
    
    // Private helper method
    
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