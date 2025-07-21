package com.los.service.impl;

import com.los.dto.DocumentDto;
import com.los.entity.Customer;
import com.los.entity.Document;
import com.los.entity.LoanApplication;
import com.los.enums.DocumentType;
import com.los.repository.CustomerRepository;
import com.los.repository.DocumentRepository;
import com.los.repository.LoanApplicationRepository;
import com.los.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Баримт бичгийн Service Implementation
 * Document Service Implementation
 */
@Service
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentServiceImpl.class);

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Value("${app.document.storage.path:./uploads/documents}")
    private String documentStoragePath;

    @Value("${app.document.max-size:52428800}") // 50MB
    private Long maxFileSize;

    // CRUD операциуд
    @Override
    public DocumentDto uploadDocument(UUID customerId, UUID loanApplicationId, DocumentType documentType,
                                    MultipartFile file, String description, String tags) {
        logger.info("Uploading document for customer: {}, type: {}", customerId, documentType);
        
        // Validation
        validateFileUpload(file, documentType);
        
        // Get customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Харилцагч олдсонгүй: " + customerId));
        
        // Get loan application if provided
        LoanApplication loanApplication = null;
        if (loanApplicationId != null) {
            loanApplication = loanApplicationRepository.findById(loanApplicationId)
                    .orElseThrow(() -> new IllegalArgumentException("Зээлийн хүсэлт олдсонгүй: " + loanApplicationId));
        }
        
        // Check for existing document of same type
        Optional<Document> existingDocument = documentRepository.findByCustomerIdAndDocumentType(customerId, documentType);
        if (existingDocument.isPresent() && !existingDocument.get().needsResubmission()) {
            throw new IllegalArgumentException("Энэ төрлийн баримт аль хэдийн байна");
        }
        
        try {
            // Store file
            String storedFilename = generateStoredFilename(file.getOriginalFilename());
            Path filePath = storeFile(file, storedFilename);
            String checksum = calculateChecksum(file.getBytes());
            
            // Check for duplicates
            List<Document> duplicates = documentRepository.findDuplicates(
                    file.getOriginalFilename(), file.getSize(), checksum, customer);
            if (!duplicates.isEmpty()) {
                logger.warn("Duplicate document found for customer: {}", customerId);
            }
            
            // Create document entity
            Document document = new Document(customer, documentType, file.getOriginalFilename(),
                    storedFilename, filePath.toString(), file.getContentType(), file.getSize());
            
            document.setChecksum(checksum);
            document.setDescription(description);
            document.setTags(tags);
            document.setLoanApplication(loanApplication);
            
            // Set required flag based on document type
            document.setIsRequired(documentType.isRequired());
            
            // Handle versioning if replacing existing document
            if (existingDocument.isPresent()) {
                Document existing = existingDocument.get();
                document.setVersionNumber(existing.getVersionNumber() + 1);
                document.setPreviousDocumentId(existing.getId());
                
                // Mark old document as replaced
                existing.setVerificationStatus(Document.VerificationStatus.EXPIRED);
                documentRepository.save(existing);
            }
            
            // Save document
            Document savedDocument = documentRepository.save(document);
            logger.info("Document uploaded successfully with ID: {}", savedDocument.getId());
            
            return DocumentDto.fromEntity(savedDocument);
            
        } catch (IOException e) {
            logger.error("Error uploading document for customer: {}", customerId, e);
            throw new RuntimeException("Файл хадгалахад алдаа гарлаа: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDto getDocumentById(UUID id) {
        logger.debug("Getting document by ID: {}", id);
        
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + id));
        
        return DocumentDto.fromEntity(document);
    }

    @Override
    public DocumentDto updateDocument(UUID id, DocumentDto documentDto) {
        logger.info("Updating document with ID: {}", id);
        
        Document existingDocument = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + id));
        
        // Check if can be edited
        if (!canEditDocument(id)) {
            throw new IllegalArgumentException("Баримт засах боломжгүй");
        }
        
        // Update editable fields
        existingDocument.setDescription(documentDto.getDescription());
        existingDocument.setTags(documentDto.getTags());
        existingDocument.setExpiryDate(documentDto.getExpiryDate());
        
        Document savedDocument = documentRepository.save(existingDocument);
        logger.info("Document updated successfully with ID: {}", savedDocument.getId());
        
        return DocumentDto.fromEntity(savedDocument);
    }

    @Override
    public void deleteDocument(UUID id) {
        logger.info("Deleting document with ID: {}", id);
        
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + id));
        
        // Check if can be deleted
        if (!canDeleteDocument(id)) {
            throw new IllegalArgumentException("Баримт устгах боломжгүй");
        }
        
        document.markAsDeleted();
        documentRepository.save(document);
        
        logger.info("Document deleted successfully with ID: {}", id);
    }

    @Override
    public DocumentDto restoreDocument(UUID id) {
        logger.info("Restoring document with ID: {}", id);
        
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + id));
        
        document.restore();
        Document savedDocument = documentRepository.save(document);
        
        logger.info("Document restored successfully with ID: {}", id);
        return DocumentDto.fromEntity(savedDocument);
    }

    @Override
    public DocumentDto replaceDocument(UUID id, MultipartFile newFile, String reason) {
        logger.info("Replacing document with ID: {}", id);
        
        Document existingDocument = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + id));
        
        // Upload new version
        DocumentDto newDocument = uploadDocument(
                existingDocument.getCustomer().getId(),
                existingDocument.getLoanApplication() != null ? existingDocument.getLoanApplication().getId() : null,
                existingDocument.getDocumentType(),
                newFile,
                reason,
                existingDocument.getTags()
        );
        
        logger.info("Document replaced successfully, new ID: {}", newDocument.getId());
        return newDocument;
    }

    // File operations
    @Override
    @Transactional(readOnly = true)
    public byte[] downloadDocument(UUID id) {
        logger.debug("Downloading document with ID: {}", id);
        
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + id));
        
        try {
            Path filePath = Paths.get(document.getFilePath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            logger.error("Error downloading document: {}", id, e);
            throw new RuntimeException("Файл татахад алдаа гарлаа: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] previewDocument(UUID id) {
        logger.debug("Previewing document with ID: {}", id);
        
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + id));
        
        // Only allow preview for images and PDFs
        if (!document.isImage() && !document.isPdf()) {
            throw new IllegalArgumentException("Энэ төрлийн файлын урьдчилан харах боломжгүй");
        }
        
        return downloadDocument(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getFileInfo(UUID id) {
        logger.debug("Getting file info for document: {}", id);
        
        DocumentDto document = getDocumentById(id);
        
        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("id", document.getId());
        fileInfo.put("originalFilename", document.getOriginalFilename());
        fileInfo.put("contentType", document.getContentType());
        fileInfo.put("fileSize", document.getFileSize());
        fileInfo.put("fileSizeFormatted", document.getFileSizeFormatted());
        fileInfo.put("uploadedAt", document.getUploadedAt());
        fileInfo.put("checksum", document.getChecksum());
        
        return fileInfo;
    }

    // Хайлт операциуд
    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDto> getAllDocuments(Pageable pageable) {
        logger.debug("Getting all documents with pageable: {}", pageable);
        
        Page<Document> documents = documentRepository.findAll(pageable);
        return documents.map(DocumentDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDto> getDocumentsByCustomer(UUID customerId, Pageable pageable) {
        logger.debug("Getting documents by customer: {}", customerId);
        
        Page<Document> documents = documentRepository.findByCustomerId(customerId, pageable);
        return documents.map(DocumentDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDto> getDocumentsByLoanApplication(UUID loanApplicationId, Pageable pageable) {
        logger.debug("Getting documents by loan application: {}", loanApplicationId);
        
        Page<Document> documents = documentRepository.findByLoanApplicationId(loanApplicationId, pageable);
        return documents.map(DocumentDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDto> getDocumentsByType(DocumentType documentType, Pageable pageable) {
        logger.debug("Getting documents by type: {}", documentType);
        
        Page<Document> documents = documentRepository.findByDocumentType(documentType, pageable);
        return documents.map(DocumentDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDto> getDocumentsByVerificationStatus(Document.VerificationStatus status, Pageable pageable) {
        logger.debug("Getting documents by verification status: {}", status);
        
        Page<Document> documents = documentRepository.findByVerificationStatus(status, pageable);
        return documents.map(DocumentDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDto getDocumentByCustomerAndType(UUID customerId, DocumentType documentType) {
        logger.debug("Getting document by customer: {} and type: {}", customerId, documentType);
        
        Document document = documentRepository.findByCustomerIdAndDocumentType(customerId, documentType)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй"));
        
        return DocumentDto.fromEntity(document);
    }

    // Баталгаажуулалт
    @Override
    public DocumentDto approveDocument(UUID id, String verifierName, String notes) {
        logger.info("Approving document: {} by: {}", id, verifierName);
        
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + id));
        
        document.approve(verifierName, notes);
        Document savedDocument = documentRepository.save(document);
        
        logger.info("Document approved successfully: {}", id);
        return DocumentDto.fromEntity(savedDocument);
    }

    @Override
    public DocumentDto rejectDocument(UUID id, String verifierName, String reason) {
        logger.info("Rejecting document: {} by: {}", id, verifierName);
        
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + id));
        
        document.reject(verifierName, reason);
        Document savedDocument = documentRepository.save(document);
        
        logger.info("Document rejected successfully: {}", id);
        return DocumentDto.fromEntity(savedDocument);
    }

    @Override
    public DocumentDto startReview(UUID id, String reviewerName) {
        logger.info("Starting review for document: {} by: {}", id, reviewerName);
        
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + id));
        
        document.startReview(reviewerName);
        Document savedDocument = documentRepository.save(document);
        
        logger.info("Document review started: {}", id);
        return DocumentDto.fromEntity(savedDocument);
    }

    @Override
    public DocumentDto pauseReview(UUID id, String reason) {
        logger.info("Pausing review for document: {} with reason: {}", id, reason);
        
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + id));
        
        document.setVerificationStatus(Document.VerificationStatus.ON_HOLD);
        document.setVerificationNotes(reason);
        Document savedDocument = documentRepository.save(document);
        
        logger.info("Document review paused: {}", id);
        return DocumentDto.fromEntity(savedDocument);
    }

    @Override
    public DocumentDto requireResubmission(UUID id, String verifierName, String reason) {
        logger.info("Requiring resubmission for document: {} by: {}", id, verifierName);
        
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + id));
        
        document.setVerificationStatus(Document.VerificationStatus.RESUBMIT_REQUIRED);
        document.setVerifiedBy(verifierName);
        document.setVerifiedAt(LocalDateTime.now());
        document.setVerificationNotes(reason);
        
        Document savedDocument = documentRepository.save(document);
        
        logger.info("Document resubmission required: {}", id);
        return DocumentDto.fromEntity(savedDocument);
    }

    // Validation
    @Override
    public boolean isFileTypeAllowed(DocumentType documentType, String contentType, String filename) {
        String extension = getFileExtension(filename);
        return documentType.isExtensionAllowed(extension);
    }

    @Override
    public boolean isFileSizeValid(Long fileSize) {
        return fileSize != null && fileSize > 0 && fileSize <= maxFileSize;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canEditDocument(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + id));
        
        return document.getVerificationStatus() == Document.VerificationStatus.PENDING ||
               document.getVerificationStatus() == Document.VerificationStatus.RESUBMIT_REQUIRED;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteDocument(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + id));
        
        return document.getVerificationStatus() != Document.VerificationStatus.APPROVED ||
               document.getVersionNumber() > 1;
    }

    // Helper methods
    private void validateFileUpload(MultipartFile file, DocumentType documentType) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл хоосон байна");
        }
        
        if (!isFileSizeValid(file.getSize())) {
            throw new IllegalArgumentException("Файлын хэмжээ хэтэрсэн байна");
        }
        
        if (!isFileTypeAllowed(documentType, file.getContentType(), file.getOriginalFilename())) {
            throw new IllegalArgumentException("Файлын төрөл зөвшөөрөгдөөгүй байна");
        }
    }

    private String generateStoredFilename(String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return timestamp + "_" + uuid + "." + extension;
    }

    private Path storeFile(MultipartFile file, String storedFilename) throws IOException {
        // Create directories if they don't exist
        Path uploadPath = Paths.get(documentStoragePath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Create subdirectory by date
        String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path dateUploadPath = uploadPath.resolve(dateFolder);
        if (!Files.exists(dateUploadPath)) {
            Files.createDirectories(dateUploadPath);
        }
        
        // Store file
        Path targetLocation = dateUploadPath.resolve(storedFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        return targetLocation;
    }

    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error calculating checksum", e);
            return null;
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1).toLowerCase() : "";
    }

    // Placeholder implementations for interface completeness
    @Override
    public Page<DocumentDto> searchDocumentsWithFilters(DocumentType documentType,
                                                       Document.VerificationStatus verificationStatus,
                                                       com.los.entity.Customer.CustomerType customerType,
                                                       String verifiedBy, Long minSize, Long maxSize,
                                                       LocalDateTime startDate, LocalDateTime endDate,
                                                       Boolean hasExpiry, Pageable pageable) {
        return documentRepository.findByAdvancedFilters(documentType, verificationStatus, customerType,
                verifiedBy, minSize, maxSize, startDate, endDate, hasExpiry, pageable)
                .map(DocumentDto::fromEntity);
    }

    @Override
    public Page<DocumentDto> getPendingVerificationDocuments(Pageable pageable) {
        return documentRepository.findPendingVerification(pageable)
                .map(DocumentDto::fromEntity);
    }

    @Override
    public List<DocumentDto> getDocumentsInReviewByReviewer(String reviewerName) {
        return documentRepository.findInReviewByReviewer(reviewerName)
                .stream()
                .map(DocumentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<DocumentDto> getApprovedDocuments(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return documentRepository.findApprovedBetween(startDate, endDate, pageable)
                .map(DocumentDto::fromEntity);
    }

    @Override
    public Page<DocumentDto> getRejectedDocuments(String reviewerName, Pageable pageable) {
        return documentRepository.findRejectedByReviewer(reviewerName, pageable)
                .map(DocumentDto::fromEntity);
    }

    @Override
    public Page<DocumentDto> getDocumentsRequiringResubmission(Pageable pageable) {
        return documentRepository.findRequiringResubmission(pageable)
                .map(DocumentDto::fromEntity);
    }

    @Override
    public List<DocumentDto> getExpiredDocuments() {
        return documentRepository.findExpiredDocuments()
                .stream()
                .map(DocumentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentDto> getDocumentsExpiringSoon() {
        java.time.LocalDate thirtyDaysLater = java.time.LocalDate.now().plusDays(30);
        return documentRepository.findExpiringsoon(thirtyDaysLater)
                .stream()
                .map(DocumentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public DocumentDto extendDocumentExpiry(UUID id, java.time.LocalDate newExpiryDate) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + id));
        
        document.setExpiryDate(newExpiryDate);
        return DocumentDto.fromEntity(documentRepository.save(document));
    }

    // Additional placeholder implementations
    @Override
    public DocumentDto startOcrProcessing(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + id));
        
        document.setProcessingStatus("PROCESSING");
        return DocumentDto.fromEntity(documentRepository.save(document));
    }

    @Override
    public DocumentDto saveOcrResults(UUID id, String ocrText, String extractedData, BigDecimal confidenceScore) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + id));
        
        document.updateOcrResults(ocrText, extractedData, confidenceScore);
        return DocumentDto.fromEntity(documentRepository.save(document));
    }

    @Override
    public List<DocumentDto> getOcrFailedDocuments() {
        return documentRepository.findOcrFailed()
                .stream()
                .map(DocumentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<DocumentDto> getHighConfidenceDocuments(BigDecimal minScore, Pageable pageable) {
        return documentRepository.findByHighConfidenceScore(minScore, pageable)
                .map(DocumentDto::fromEntity);
    }

    @Override
    public List<DocumentDto> getLowConfidenceDocuments(BigDecimal threshold) {
        return documentRepository.findLowConfidenceDocuments(threshold)
                .stream()
                .map(DocumentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentDto> findDuplicateDocuments(String filename, Long fileSize, String checksum, UUID excludeCustomerId) {
        Customer excludeCustomer = customerRepository.findById(excludeCustomerId).orElse(null);
        return documentRepository.findDuplicates(filename, fileSize, checksum, excludeCustomer)
                .stream()
                .map(DocumentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentDto> findDocumentsByChecksum(String checksum, UUID excludeCustomerId) {
        Customer excludeCustomer = customerRepository.findById(excludeCustomerId).orElse(null);
        return documentRepository.findByChecksumAndCustomerNot(checksum, excludeCustomer)
                .stream()
                .map(DocumentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentType> getMissingRequiredDocuments(UUID loanApplicationId) {
        return documentRepository.findMissingRequiredDocuments(loanApplicationId);
    }

    @Override
    public Map<DocumentType, Document.VerificationStatus> getRequiredDocumentStatus(UUID customerId) {
        List<DocumentType> requiredTypes = Arrays.asList(DocumentType.getRequiredDocuments());
        List<Object[]> results = documentRepository.getRequiredDocumentStatus(customerId, requiredTypes);
        
        Map<DocumentType, Document.VerificationStatus> statusMap = new HashMap<>();
        for (Object[] row : results) {
            DocumentType type = (DocumentType) row[0];
            Document.VerificationStatus status = (Document.VerificationStatus) row[1];
            statusMap.put(type, status);
        }
        
        return statusMap;
    }

    @Override
    public List<DocumentType> getRequiredDocumentsForLoanType(String loanType) {
        return Arrays.asList(DocumentType.getDocumentsByLoanType(loanType));
    }

    // Statistics and additional methods
    @Override
    public Map<String, Object> getDocumentStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalDocuments = documentRepository.count();
        stats.put("totalDocuments", totalDocuments);
        
        // By type
        List<Object[]> typeStats = documentRepository.countByDocumentType();
        Map<String, Long> typeMap = new HashMap<>();
        for (Object[] row : typeStats) {
            typeMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("byType", typeMap);
        
        // By verification status
        List<Object[]> statusStats = documentRepository.countByVerificationStatus();
        Map<String, Long> statusMap = new HashMap<>();
        for (Object[] row : statusStats) {
            statusMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("byVerificationStatus", statusMap);
        
        return stats;
    }

    @Override
    public Map<DocumentType, Long> getDocumentCountByType() {
        List<Object[]> results = documentRepository.countByDocumentType();
        Map<DocumentType, Long> countMap = new HashMap<>();
        
        for (Object[] row : results) {
            DocumentType type = (DocumentType) row[0];
            Long count = (Long) row[1];
            countMap.put(type, count);
        }
        
        return countMap;
    }

    @Override
    public Map<Document.VerificationStatus, Long> getDocumentCountByVerificationStatus() {
        List<Object[]> results = documentRepository.countByVerificationStatus();
        Map<Document.VerificationStatus, Long> countMap = new HashMap<>();
        
        for (Object[] row : results) {
            Document.VerificationStatus status = (Document.VerificationStatus) row[0];
            Long count = (Long) row[1];
            countMap.put(status, count);
        }
        
        return countMap;
    }

    @Override
    public List<Map<String, Object>> getMonthlyDocumentStats(int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        List<Object[]> results = documentRepository.getMonthlyDocumentStats(startDate);
        
        return results.stream()
                .map(row -> {
                    Map<String, Object> monthStats = new HashMap<>();
                    monthStats.put("month", row[0]);
                    monthStats.put("count", row[1]);
                    monthStats.put("averageSize", row[2]);
                    return monthStats;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getVerifierStats() {
        List<Object[]> results = documentRepository.getVerifierStats();
        
        return results.stream()
                .map(row -> {
                    Map<String, Object> verifierStats = new HashMap<>();
                    verifierStats.put("verifier", row[0]);
                    verifierStats.put("count", row[1]);
                    verifierStats.put("averageHours", row[2]);
                    return verifierStats;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Double getAverageVerificationTimeHours() {
        return documentRepository.getAverageVerificationTimeHours();
    }

    @Override
    public Page<DocumentDto> getSlowestVerifiedDocuments(Pageable pageable) {
        return documentRepository.findSlowestVerified(pageable)
                .map(DocumentDto::fromEntity);
    }

    @Override
    public Map<String, Object> getTodayDocumentStats() {
        Object[] results = documentRepository.getTodayDocumentStats();
        Map<String, Object> stats = new HashMap<>();
        
        if (results != null && results.length >= 4) {
            stats.put("todayUploaded", results[0]);
            stats.put("pendingVerification", results[1]);
            stats.put("todayVerified", results[2]);
            stats.put("rejected", results[3]);
        }
        
        return stats;
    }

    @Override
    public Map<String, Long> getTopContentTypes() {
        List<Object[]> results = documentRepository.getTopContentTypes();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]));
    }

    // Additional placeholder implementations for remaining interface methods
    @Override
    public DocumentDto getLatestDocumentVersion(UUID customerId, DocumentType documentType) {
        return documentRepository.findLatestVersion(customerId, documentType)
                .map(DocumentDto::fromEntity)
                .orElse(null);
    }

    @Override
    public List<DocumentDto> getAllDocumentVersions(UUID customerId, DocumentType documentType) {
        return documentRepository.findAllVersions(customerId, documentType)
                .stream()
                .map(DocumentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public DocumentDto createNewVersion(UUID originalDocumentId, MultipartFile newFile, String changeReason) {
        Document originalDocument = documentRepository.findById(originalDocumentId)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + originalDocumentId));
        
        return uploadDocument(
                originalDocument.getCustomer().getId(),
                originalDocument.getLoanApplication() != null ? originalDocument.getLoanApplication().getId() : null,
                originalDocument.getDocumentType(),
                newFile,
                changeReason,
                originalDocument.getTags()
        );
    }

    @Override
    public int updateVerificationStatusForDocuments(List<UUID> documentIds, 
                                                   Document.VerificationStatus newStatus,
                                                   String verifierName, String notes) {
        return documentRepository.updateVerificationStatus(documentIds, newStatus, 
                verifierName, LocalDateTime.now(), notes);
    }

    @Override
    public int markExpiredDocuments() {
        return documentRepository.markExpiredDocuments();
    }

    @Override
    public byte[] exportDocumentsToZip(List<UUID> documentIds) {
        // ZIP export implementation
        return new byte[0];
    }

    @Override
    public int cleanupDeletedDocuments() {
        // Cleanup implementation
        return 0;
    }

    @Override
    public int cleanupUnusedDocuments(int unusedDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(unusedDays);
        List<Document> unusedDocuments = documentRepository.findUnusedDocuments(cutoffDate);
        // Mark as deleted
        return unusedDocuments.size();
    }

    @Override
    public List<String> getDeletedDocumentPaths() {
        return documentRepository.findDeletedDocumentPaths();
    }

    // Notification and other methods - placeholder implementations
    @Override
    public boolean sendExpiryNotification(UUID documentId) { return true; }

    @Override
    public boolean sendResubmissionNotification(UUID documentId) { return true; }

    @Override
    public boolean sendVerificationResultNotification(UUID documentId) { return true; }

    @Override
    public DocumentDto addTagsToDocument(UUID id, String tags) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + id));
        
        String existingTags = document.getTags();
        String newTags = existingTags != null ? existingTags + "," + tags : tags;
        document.setTags(newTags);
        
        return DocumentDto.fromEntity(documentRepository.save(document));
    }

    @Override
    public DocumentDto removeTagsFromDocument(UUID id, String tags) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Баримт олдсонгүй: " + id));
        
        String existingTags = document.getTags();
        if (existingTags != null) {
            String newTags = existingTags.replace(tags, "").replace(",,", ",");
            document.setTags(newTags);
        }
        
        return DocumentDto.fromEntity(documentRepository.save(document));
    }

    @Override
    public Page<DocumentDto> getDocumentsByTags(String tags, Pageable pageable) {
        // Implementation would need a query method in repository
        return Page.empty();
    }

    @Override
    public List<Map<String, Object>> getDocumentAuditHistory(UUID id) { return new ArrayList<>(); }

    @Override
    public List<Map<String, Object>> getDocumentActivityLog(UUID id) { return new ArrayList<>(); }

    @Override
    public Map<String, Object> reviewDocumentQuality(UUID id) { return new HashMap<>(); }

    @Override
    public Map<String, Object> validateDataIntegrity() { return new HashMap<>(); }
}