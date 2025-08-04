package com.company.los.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Document Storage Configuration
 * Баримт бичиг хадгалах тохиргоо
 * 
 * @author LOS Development Team
 * @version 1.0
 * @since 2025-08-03
 */
@Configuration
@ConfigurationProperties(prefix = "app.document.storage")
public class DocumentStorageConfig {
    
    private String uploadDir = "./uploads/documents";
    private Long maxFileSize = 52428800L; // 50MB default
    private Set<String> allowedTypes = Set.of("pdf", "jpg", "jpeg", "png", "doc", "docx");
    
    /**
     * Upload directory байршил
     */
    public String getUploadDir() {
        return uploadDir;
    }
    
    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
    
    /**
     * Файлын хамгийн их хэмжээ (bytes)
     */
    public Long getMaxFileSize() {
        return maxFileSize;
    }
    
    public void setMaxFileSize(Long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
    
    /**
     * Зөвшөөрөгдсөн файлын төрлүүд
     */
    public Set<String> getAllowedTypes() {
        return allowedTypes;
    }
    
    public void setAllowedTypes(Set<String> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }
    
    /**
     * Файлын төрөл зөвшөөрөгдсөн эсэхийг шалгах
     */
    public boolean isFileTypeAllowed(String fileType) {
        if (fileType == null || allowedTypes == null) {
            return false;
        }
        return allowedTypes.stream()
                .anyMatch(type -> fileType.toLowerCase().contains(type.toLowerCase()));
    }
    
    /**
     * Файлын хэмжээ зөвшөөрөгдсөн эсэхийг шалгах
     */
    public boolean isFileSizeAllowed(Long fileSize) {
        return fileSize != null && maxFileSize != null && fileSize <= maxFileSize;
    }
    
    @Override
    public String toString() {
        return "DocumentStorageConfig{" +
                "uploadDir='" + uploadDir + '\'' +
                ", maxFileSize=" + maxFileSize +
                ", allowedTypes=" + allowedTypes +
                '}';
    }
}