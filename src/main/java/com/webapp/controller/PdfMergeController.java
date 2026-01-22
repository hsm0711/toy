package com.webapp.controller;

import com.webapp.service.MenuService;
import com.webapp.service.PdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/pdf-merge")
@RequiredArgsConstructor
public class PdfMergeController {
    
    private final MenuService menuService;
    private final PdfService pdfService;
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    @GetMapping
    public String pdfMergePage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "pdf-merge");
        return "pdf-merge";
    }
    
    @PostMapping("/merge")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mergePdfs(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "order", required = false) List<Integer> order) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (files == null || files.isEmpty()) {
                response.put("success", false);
                response.put("message", "파일을 선택해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            for (MultipartFile file : files) {
                if (!pdfService.isPdfFile(file)) {
                    response.put("success", false);
                    response.put("message", "PDF 파일만 업로드 가능합니다: " + file.getOriginalFilename());
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            String mergedFileName = pdfService.mergePdfFiles(files, order);
            
            response.put("success", true);
            response.put("message", "PDF 병합이 완료되었습니다.");
            response.put("fileName", mergedFileName);
            response.put("downloadUrl", "/pdf-merge/download/" + mergedFileName);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("PDF 병합 오류", e);
            response.put("success", false);
            response.put("message", "PDF 병합 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadPdf(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            Resource resource = new FileSystemResource(filePath);
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("파일 다운로드 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
