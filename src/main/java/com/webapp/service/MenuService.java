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
        return menuRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
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
}
