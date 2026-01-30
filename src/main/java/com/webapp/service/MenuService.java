package com.webapp.service;

import com.webapp.model.Menu;
import com.webapp.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {
    
    private final MenuRepository menuRepository;
    
    public List<Menu> getActiveMenus() {
        List<Menu> activeMenus = menuRepository.findByIsActiveTrueOrderByDisplayOrderAsc();

        // TODO: [임시] "AI vs AI 토론 배틀" 메뉴 항목을 코드상에서 임시로 추가합니다.
        // 이 부분은 실제 운영 환경에서는 데이터베이스를 통해 관리되어야 합니다.
        Menu aiDebateMenu = new Menu();
        aiDebateMenu.setId(-1L); // 임시 ID
        aiDebateMenu.setName("AI vs AI 토론 배틀");
        aiDebateMenu.setPath("/ai-debate");
        aiDebateMenu.setIcon("🤖"); // 이모지 아이콘
        aiDebateMenu.setDisplayOrder(100); // 다른 메뉴보다 높은 순서로 배치하여 마지막에 오도록
        aiDebateMenu.setIsActive(true);
        // createdAt, updatedAt은 JPA의 @PrePersist/@PreUpdate로 자동 생성되지만, 임시 객체이므로 수동 설정
        // 혹은 이 필드들이 null이어도 동작하도록 MenuService에서 해당 필드 접근 시 NFE 방지
        // 여기서는 그냥 추가만 하고 데이터베이스에 넣는 게 아니므로 시간 정보는 중요하지 않음.

        activeMenus.add(aiDebateMenu);

        // displayOrder를 기준으로 다시 정렬 (새로 추가된 메뉴 포함)
        activeMenus.sort(Comparator.comparing(Menu::getDisplayOrder));
        
        return activeMenus;
    }
    
    public List<Menu> getAllMenus() {
        return menuRepository.findAllByOrderByDisplayOrderAsc();
    }
    
    public Optional<Menu> getMenuById(Long id) {
        return menuRepository.findById(id);
    }
    
    @Transactional
    public Menu createMenu(Menu menu) {
        return menuRepository.save(menu);
    }
    
    @Transactional
    public Menu updateMenu(Long id, Menu menuDetails) {
        Menu menu = menuRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Menu not found"));
        
        menu.setName(menuDetails.getName());
        menu.setPath(menuDetails.getPath());
        menu.setIcon(menuDetails.getIcon());
        menu.setDisplayOrder(menuDetails.getDisplayOrder());
        menu.setIsActive(menuDetails.getIsActive());
        
        return menuRepository.save(menu);
    }
    
    @Transactional
    public void deleteMenu(Long id) {
        menuRepository.deleteById(id);
    }
    
    @Transactional
    public void updateMenuOrder(List<Long> menuIds) {
        for (int i = 0; i < menuIds.size(); i++) {
            Long menuId = menuIds.get(i);
            int newOrder = i;
            
            menuRepository.findById(menuId).ifPresent(menu -> {
                menu.setDisplayOrder(newOrder);
                menuRepository.save(menu);
            });
        }
    }
}
