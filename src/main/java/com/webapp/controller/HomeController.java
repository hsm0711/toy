package com.webapp.controller;

import com.webapp.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MenuService menuService;

    @GetMapping("/")
    public String home(Model model) {

        log.info("HomeController / 요청 진입");

        var menus = menuService.getActiveMenus();

        log.info("메뉴 조회 결과: {}", menus);
        log.info("메뉴 개수: {}", menus != null ? menus.size() : "null");

        model.addAttribute("menus", menus);
        model.addAttribute("currentPage", "home");

        log.info("Model에 menus, currentPage 주입 완료");
        log.info("index.html 반환");

        return "index";
    }
}
