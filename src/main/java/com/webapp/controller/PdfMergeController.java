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
import java.util.List;
import java.util.Map;

/**
 * PDF 병합 기능 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/pdf-merge")
@RequiredArgsConstructor
public class PdfMergeController {
    
    private static final String VIEW_NAME = "pdf-merge";
    private static final String CURRENT_PAGE = "pdf-merge";
    
    private final MenuService menuService;
    private final PdfService pdfService;
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    /**
     * PDF 병합 페이지
     */
    @GetMapping
    public String pdfMergePage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", CURRENT_PAGE);
        return VIEW_NAME;
    }
    
    /**
     * PDF 병합 API
     */
    @PostMapping("/merge")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mergePdfs(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "order", required = false) List<Integer> order) {
        
        // 파일 검증
        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(createResponse(false, "파일을 선택해주세요.", null, null));
        }
        
        for (MultipartFile file : files) {
            if (!pdfService.isPdfFile(file)) {
                return ResponseEntity.badRequest()
                    .body(createResponse(false, 
                        "PDF 파일만 업로드 가능합니다: " + file.getOriginalFilename(), 
                        null, null));
            }
        }
        
        // PDF 병합 처리
        try {
            String mergedFileName = pdfService.mergePdfFiles(files, order);
            String downloadUrl = "/pdf-merge/download/" + mergedFileName;
            
            return ResponseEntity.ok(
                createResponse(true, "PDF 병합이 완료되었습니다.", mergedFileName, downloadUrl)
            );
        } catch (IOException e) {
            log.error("PDF 병합 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createResponse(false, 
                    "PDF 병합 중 오류가 발생했습니다: " + e.getMessage(), 
                    null, null));
        }
    }
    
    /**
     * PDF 파일 다운로드
     */
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
    
    /**
     * 응답 객체 생성 헬퍼 메소드
     */
    private Map<String, Object> createResponse(
            boolean success, 
            String message, 
            String fileName, 
            String downloadUrl) {
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", success);
        response.put("message", message);
        
        if (fileName != null) {
            response.put("fileName", fileName);
        }
        if (downloadUrl != null) {
            response.put("downloadUrl", downloadUrl);
        }
        
        return response;
    }
}