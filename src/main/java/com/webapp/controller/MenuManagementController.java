package com.webapp.controller;

import com.webapp.model.Menu;
import com.webapp.service.OpenRouterApiService;
import com.webapp.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/mngt/menu")
@RequiredArgsConstructor
public class MenuManagementController {
    
    private final MenuService menuService;
    private final OpenRouterApiService openRouterApiService;
    
    /**
     * 메뉴 관리 페이지 (숨겨진 URL)
     */
    @GetMapping
    public String menuManagementPage(Model model) {
        log.info("메뉴 관리 페이지 접근");
        // 메뉴를 표시하지 않기 위해 빈 리스트 전달
        model.addAttribute("menus", List.of());
        model.addAttribute("currentPage", "menu-management");
        return "menu-management";
    }

    /**
     * API: AI 아이콘 추천
     */
    @PostMapping("/api/suggest-icon")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> suggestIcon(@RequestBody Map<String, String> request) {
        String menuName = request.get("menuName");
        if (menuName == null || menuName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "메뉴명이 필요합니다."));
        }
        
        log.info("AI 아이콘 추천 API 호출: menuName={}", menuName);
        
        return ResponseEntity.ok(openRouterApiService.suggestIcons(menuName));
    }
    
    /**
     * API: 전체 메뉴 조회
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<List<Menu>> getAllMenus() {
        log.info("전체 메뉴 조회 API 호출");
        List<Menu> menus = menuService.getAllMenus();
        return ResponseEntity.ok(menus);
    }
    
    /**
     * API: 메뉴 생성
     */
    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createMenu(@RequestBody Menu menu) {
        log.info("메뉴 생성 API 호출: {}", menu);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Menu created = menuService.createMenu(menu);
            response.put("success", true);
            response.put("message", "메뉴가 생성되었습니다.");
            response.put("data", created);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("메뉴 생성 실패", e);
            response.put("success", false);
            response.put("message", "메뉴 생성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * API: 메뉴 수정
     */
    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateMenu(
            @PathVariable Long id,
            @RequestBody Menu menu) {
        log.info("메뉴 수정 API 호출: id={}, menu={}", id, menu);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Menu updated = menuService.updateMenu(id, menu);
            response.put("success", true);
            response.put("message", "메뉴가 수정되었습니다.");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("메뉴 수정 실패", e);
            response.put("success", false);
            response.put("message", "메뉴 수정 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * API: 메뉴 삭제
     */
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteMenu(@PathVariable Long id) {
        log.info("메뉴 삭제 API 호출: id={}", id);
        Map<String, Object> response = new HashMap<>();
        
        try {
            menuService.deleteMenu(id);
            response.put("success", true);
            response.put("message", "메뉴가 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("메뉴 삭제 실패", e);
            response.put("success", false);
            response.put("message", "메뉴 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * API: 메뉴 활성화/비활성화
     */
    @PatchMapping("/api/toggle/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleMenu(@PathVariable Long id) {
        log.info("메뉴 토글 API 호출: id={}", id);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Menu menu = menuService.getMenuById(id)
                .orElseThrow(() -> new RuntimeException("메뉴를 찾을 수 없습니다."));
            
            menu.setIsActive(!menu.getIsActive());
            Menu updated = menuService.updateMenu(id, menu);
            
            response.put("success", true);
            response.put("message", updated.getIsActive() ? "메뉴가 활성화되었습니다." : "메뉴가 비활성화되었습니다.");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("메뉴 토글 실패", e);
            response.put("success", false);
            response.put("message", "메뉴 토글 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}