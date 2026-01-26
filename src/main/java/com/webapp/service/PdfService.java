package com.webapp.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PDF 파일 처리 서비스
 */
@Slf4j
@Service
public class PdfService {
    
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String PDF_EXTENSION = ".pdf";
    private static final String MERGED_FILE_PREFIX = "merged_";
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    /**
     * PDF 파일 병합
     */
    public String mergePdfFiles(List<MultipartFile> files, List<Integer> order) throws IOException {
        ensureUploadDirectoryExists();
        
        List<File> tempFiles = new ArrayList<>();
        
        try {
            tempFiles = saveTempFiles(files);
            File mergedFile = createMergedFile(tempFiles, order);
            
            log.info("PDF 병합 완료: {}", mergedFile.getAbsolutePath());
            return mergedFile.getName();
        } catch (Exception e) {
            log.error("PDF 병합 중 오류 발생", e);
            throw new IOException("PDF 병합 중 오류가 발생했습니다: " + e.getMessage(), e);
        } finally {
            cleanupTempFiles(tempFiles);
        }
    }
    
    /**
     * PDF 파일 여부 검증
     */
    public boolean isPdfFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        
        return (contentType != null && contentType.equals(PDF_CONTENT_TYPE)) ||
               (fileName != null && fileName.toLowerCase().endsWith(PDF_EXTENSION));
    }
    
    /**
     * 파일 삭제
     */
    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir, fileName);
            Files.deleteIfExists(filePath);
            log.info("파일 삭제: {}", fileName);
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", fileName, e);
        }
    }
    
    // ========== Private Helper Methods ==========
    
    /**
     * 업로드 디렉토리 생성
     */
    private void ensureUploadDirectoryExists() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("업로드 디렉토리 생성: {}", uploadPath);
        }
    }
    
    /**
     * 임시 파일 저장
     */
    private List<File> saveTempFiles(List<MultipartFile> files) throws IOException {
        List<File> tempFiles = new ArrayList<>();
        Path uploadPath = Paths.get(uploadDir);
        
        for (MultipartFile file : files) {
            String tempFileName = generateTempFileName(file.getOriginalFilename());
            File tempFile = uploadPath.resolve(tempFileName).toFile();
            file.transferTo(tempFile);
            tempFiles.add(tempFile);
            log.debug("임시 파일 저장: {}", tempFileName);
        }
        
        return tempFiles;
    }
    
    /**
     * 병합된 파일 생성
     */
    private File createMergedFile(List<File> tempFiles, List<Integer> order) throws IOException {
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        
        // 파일 순서 지정 또는 기본 순서로 병합
        if (order != null && !order.isEmpty()) {
            addFilesInOrder(pdfMerger, tempFiles, order);
        } else {
            addFilesInDefaultOrder(pdfMerger, tempFiles);
        }
        
        // 병합 파일 생성
        String mergedFileName = generateMergedFileName();
        File mergedFile = Paths.get(uploadDir, mergedFileName).toFile();
        pdfMerger.setDestinationFileName(mergedFile.getAbsolutePath());
        pdfMerger.mergeDocuments(null);
        
        return mergedFile;
    }
    
    /**
     * 지정된 순서로 파일 추가
     * @throws FileNotFoundException
     */
    private void addFilesInOrder(PDFMergerUtility merger, List<File> files, List<Integer> order) throws FileNotFoundException {
        for (Integer index : order) {
            if (index >= 0 && index < files.size()) {
                merger.addSource(files.get(index));
            }
        }
    }
    
    /**
     * 기본 순서로 파일 추가
     */
    private void addFilesInDefaultOrder(PDFMergerUtility merger, List<File> files) {
        files.forEach(t -> {
            try {
                merger.addSource(t);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 임시 파일명 생성
     */
    private String generateTempFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "_" + originalFileName;
    }
    
    /**
     * 병합 파일명 생성
     */
    private String generateMergedFileName() {
        return MERGED_FILE_PREFIX + System.currentTimeMillis() + PDF_EXTENSION;
    }
    
    /**
     * 임시 파일 정리
     */
    private void cleanupTempFiles(List<File> tempFiles) {
        for (File tempFile : tempFiles) {
            if (tempFile.exists() && tempFile.delete()) {
                log.debug("임시 파일 삭제: {}", tempFile.getName());
            }
        }
    }
}