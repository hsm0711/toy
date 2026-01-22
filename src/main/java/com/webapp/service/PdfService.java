package com.webapp.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PdfService {
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    public String mergePdfFiles(List<MultipartFile> files, List<Integer> order) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        List<File> tempFiles = new ArrayList<>();
        
        try {
            for (MultipartFile file : files) {
                String tempFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                File tempFile = new File(uploadPath.toFile(), tempFileName);
                file.transferTo(tempFile);
                tempFiles.add(tempFile);
            }
            
            PDFMergerUtility pdfMerger = new PDFMergerUtility();
            
            if (order != null && !order.isEmpty()) {
                for (Integer index : order) {
                    if (index >= 0 && index < tempFiles.size()) {
                        pdfMerger.addSource(tempFiles.get(index));
                    }
                }
            } else {
                for (File file : tempFiles) {
                    pdfMerger.addSource(file);
                }
            }
            
            String mergedFileName = "merged_" + System.currentTimeMillis() + ".pdf";
            File mergedFile = new File(uploadPath.toFile(), mergedFileName);
            pdfMerger.setDestinationFileName(mergedFile.getAbsolutePath());
            pdfMerger.mergeDocuments(null);
            
            log.info("PDF 병합 완료: {}", mergedFile.getAbsolutePath());
            
            return mergedFileName;
            
        } catch (Exception e) {
            log.error("PDF 병합 중 오류 발생", e);
            throw new IOException("PDF 병합 중 오류가 발생했습니다: " + e.getMessage());
        } finally {
            for (File tempFile : tempFiles) {
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        }
    }
    
    public boolean isPdfFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        
        return (contentType != null && contentType.equals("application/pdf")) ||
               (fileName != null && fileName.toLowerCase().endsWith(".pdf"));
    }
    
    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir, fileName);
            Files.deleteIfExists(filePath);
            log.info("파일 삭제: {}", fileName);
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", fileName, e);
        }
    }
}
