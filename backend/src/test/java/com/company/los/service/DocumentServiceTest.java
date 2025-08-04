package com.company.los.service;

import com.company.los.entity.Customer;
import com.company.los.entity.Document;
import com.company.los.entity.DocumentType;
import com.company.los.entity.LoanApplication;
import com.company.los.dto.DocumentDto;
import com.company.los.exception.ResourceNotFoundException;
import com.company.los.repository.CustomerRepository;
import com.company.los.repository.DocumentRepository;
import com.company.los.repository.DocumentTypeRepository;
import com.company.los.repository.LoanApplicationRepository;
import com.company.los.service.impl.DocumentServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

/**
 * ⭐ FINAL FIXED DocumentService Test v6.0 ⭐
 * FIXES APPLIED:
 * ✅ LoanApplication mock setup fixed
 * ✅ Error messages aligned with English
 * ✅ Mock invocation counts fixed
 * ✅ File operations fixed with proper file existence checks
 * ✅ Business validation fixed
 * ✅ Path operations error fixed
 * ✅ NullPointerException fixed with proper mocking
 * ✅ findById duplicate call fixed
 * ✅ maxFileSize configuration added via ReflectionTestUtils
 * ✅ FileNotFoundException test fixed with proper file path handling
 * ✅ Delete test fixed with proper file cleanup
 * ✅ All edge cases covered with proper error handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentService Tests - FINAL COMPLETELY FIXED v6.0")
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private DocumentTypeRepository documentTypeRepository;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @TempDir
    Path tempDir;

    private Customer testCustomer;
    private Document testDocument;
    private DocumentDto testDocumentDto;
    private LoanApplication testLoanApplication;
    private MultipartFile testFile;
    private DocumentType testDocumentType;

    @BeforeEach
    void setUp() throws IOException {
        // ⭐ ЗАСВАРЛАСАН: maxFileSize-г ReflectionTestUtils ашиглан тохируулах ⭐
        ReflectionTestUtils.setField(documentService, "maxFileSize", 52428800L); // 50MB
        ReflectionTestUtils.setField(documentService, "documentStoragePath", tempDir.toString());

        // Initialize DocumentType
        testDocumentType = new DocumentType();
        testDocumentType.setId(UUID.randomUUID());
        testDocumentType.setName("IDENTITY_CARD");
        testDocumentType.setDescription("Иргэний үнэмлэх");
        testDocumentType.setIsRequired(true);
        // ⭐ ЗАСВАРЛАСАН: setMaxFileSize метод байхгүй тул арилгасан ⭐

        testCustomer = new Customer();
        testCustomer.setId(UUID.randomUUID());
        testCustomer.setFirstName("Батбаяр");
        testCustomer.setLastName("Болд");
        testCustomer.setEmail("batbayar@test.com");
        testCustomer.setRegisterNumber("УГ99123456");
        testCustomer.setIsActive(true);

        testLoanApplication = new LoanApplication();
        testLoanApplication.setId(UUID.randomUUID());
        testLoanApplication.setCustomer(testCustomer);
        testLoanApplication.setApplicationNumber("LN-2025-0001");
        testLoanApplication.setLoanType(LoanApplication.LoanType.PERSONAL);
        testLoanApplication.setRequestedAmount(new BigDecimal("5000000"));
        testLoanApplication.setStatus(LoanApplication.ApplicationStatus.PENDING);
        testLoanApplication.setCreatedAt(LocalDateTime.now());

        // ⭐ ЗАСВАРЛАСАН: Create actual test file in temp directory ⭐
        Path testFilePath = tempDir.resolve("test-document.pdf");
        Files.write(testFilePath, "Test document content".getBytes());

        testDocument = new Document();
        testDocument.setId(UUID.randomUUID());
        testDocument.setCustomer(testCustomer);
        testDocument.setLoanApplication(testLoanApplication);
        testDocument.setDocumentType(testDocumentType);
        testDocument.setStoredFilename("identity_card.pdf");
        testDocument.setOriginalFilename("Иргэний үнэмлэх.pdf");
        testDocument.setFileSize(1024L);
        testDocument.setContentType("application/pdf");
        testDocument.setFilePath(testFilePath.toString()); // ⭐ ЗАСВАРЛАСАН: Valid file path ⭐
        testDocument.setDescription("Иргэний үнэмлэхийн хуулбар");
        testDocument.setUploadedAt(LocalDateTime.now());
        testDocument.setCreatedAt(LocalDateTime.now());
        testDocument.setVerificationStatus(Document.VerificationStatus.PENDING);

        testDocumentDto = new DocumentDto();
        testDocumentDto.setLoanApplicationId(testLoanApplication.getId());
        testDocumentDto.setDocumentType(testDocumentType);
        testDocumentDto.setDescription("Иргэний үнэмлэхийн хуулбар");

        testFile = new MockMultipartFile(
                "file",
                "test-document.pdf",
                "application/pdf",
                "Test document content".getBytes()
        );
    }

    @Test
    @DisplayName("Файл upload хийх - Амжилттай")
    void uploadDocument_Success() throws IOException {
        // Given - ⭐ ЗАСВАРЛАСАН: Proper mocking setup ⭐
        given(customerRepository.findById(testCustomer.getId())).willReturn(Optional.of(testCustomer));
        given(loanApplicationRepository.findById(testLoanApplication.getId())).willReturn(Optional.of(testLoanApplication));
        given(documentRepository.findByCustomerIdAndDocumentType(testCustomer.getId(), testDocumentType))
                .willReturn(Optional.empty()); // No existing document
        given(documentRepository.findPotentialDuplicates(anyString(), anyString(), anyLong(), any()))
                .willReturn(Arrays.asList()); // No duplicates
        given(documentRepository.save(any(Document.class))).willReturn(testDocument);

        // When
        DocumentDto result = documentService.uploadDocument(
            testCustomer.getId(), 
            testLoanApplication.getId(),
            testDocumentType, 
            testFile, 
            "Test description", 
            "test"
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOriginalFilename()).isEqualTo("Иргэний үнэмлэх.pdf");
        assertThat(result.getContentType()).isEqualTo("application/pdf");
        assertThat(result.getFileSize()).isEqualTo(1024L);
        assertThat(result.getDocumentType()).isEqualTo(testDocumentType);

        verify(customerRepository).findById(testCustomer.getId());
        verify(loanApplicationRepository).findById(testLoanApplication.getId());
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    @DisplayName("⭐ ЗАСВАРЛАСАН: Файл upload хийх - Зээлийн хүсэлт олдсонгүй ⭐")
    void uploadDocument_LoanApplicationNotFound() {
        // Given - ⭐ ЗАСВАРЛАСАН: Customer found, но LoanApplication олдсонгүй ⭐
        given(customerRepository.findById(testCustomer.getId())).willReturn(Optional.of(testCustomer));
        given(loanApplicationRepository.findById(testLoanApplication.getId())).willReturn(Optional.empty());

        // When & Then - ⭐ ЗАСВАРЛАСАН: English error message ⭐
        assertThatThrownBy(() -> documentService.uploadDocument(
                testCustomer.getId(),
                testLoanApplication.getId(),
                testDocumentType, 
                testFile, 
                "Test description", 
                "test"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Loan application not found with ID: " + testLoanApplication.getId());

        verify(customerRepository).findById(testCustomer.getId());
        verify(loanApplicationRepository).findById(testLoanApplication.getId());
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    @DisplayName("Файл upload хийх - Харилцагч олдсонгүй")
    void uploadDocument_CustomerNotFound() {
        // Given
        given(customerRepository.findById(testCustomer.getId())).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> documentService.uploadDocument(
                testCustomer.getId(),
                testLoanApplication.getId(),
                testDocumentType, 
                testFile, 
                "Test description", 
                "test"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found with ID: " + testCustomer.getId());

        verify(customerRepository).findById(testCustomer.getId());
        verify(loanApplicationRepository, never()).findById(any());
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    @DisplayName("Файл upload хийх - Хоосон файл")
    void uploadDocument_EmptyFile() {
        // Given
        MultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        // When & Then
        assertThatThrownBy(() -> documentService.uploadDocument(
                testCustomer.getId(),
                testLoanApplication.getId(),
                testDocumentType, 
                emptyFile, 
                "Test description", 
                "test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File is empty");

        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    @DisplayName("Файл upload хийх - Дэмжигдээгүй файлын төрөл")
    void uploadDocument_UnsupportedFileType() {
        // Given
        MultipartFile unsupportedFile = new MockMultipartFile(
                "file",
                "test.exe",
                "application/x-msdownload",
                "Executable content".getBytes()
        );

        // When & Then
        assertThatThrownBy(() -> documentService.uploadDocument(
                testCustomer.getId(),
                testLoanApplication.getId(),
                testDocumentType, 
                unsupportedFile, 
                "Test description", 
                "test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File type not allowed");

        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    @DisplayName("⭐ ЗАСВАРЛАСАН: Файл download хийх - Амжилттай ⭐")
    void downloadDocument_Success() throws IOException {
        // Given - ⭐ ЗАСВАРЛАСАН: Valid file path with actual file ⭐
        Path testFilePath = tempDir.resolve("download-test.pdf");
        Files.write(testFilePath, "Test content for download".getBytes());
        
        Document documentWithValidPath = new Document();
        documentWithValidPath.setId(testDocument.getId());
        documentWithValidPath.setFilePath(testFilePath.toString());

        given(documentRepository.findById(testDocument.getId())).willReturn(Optional.of(documentWithValidPath));

        // When
        byte[] result = documentService.downloadDocument(testDocument.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(new String(result)).isEqualTo("Test content for download");

        verify(documentRepository).findById(testDocument.getId());
    }

    @Test
    @DisplayName("Файл download хийх - Баримт олдсонгүй")
    void downloadDocument_DocumentNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        given(documentRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> documentService.downloadDocument(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Document not found with ID: " + nonExistentId);

        verify(documentRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("⭐ ЗАСВАРЛАСАН: Файл download хийх - Файл олдсонгүй ⭐")
    void downloadDocument_FileNotFound() {
        // Given - ⭐ ЗАСВАРЛАСАН: Non-existent file path ⭐
        Path nonExistentPath = tempDir.resolve("non-existent-file.pdf");
        
        Document documentWithInvalidPath = new Document();
        documentWithInvalidPath.setId(testDocument.getId());
        documentWithInvalidPath.setFilePath(nonExistentPath.toString());

        given(documentRepository.findById(testDocument.getId())).willReturn(Optional.of(documentWithInvalidPath));

        // When & Then - ⭐ ЗАСВАРЛАСАН: RuntimeException with proper message ⭐
        assertThatThrownBy(() -> documentService.downloadDocument(testDocument.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("File not found");

        verify(documentRepository).findById(testDocument.getId());
    }

    @Test
    @DisplayName("ID-аар баримт авах - Амжилттай")
    void getDocumentById_Success() {
        // Given
        given(documentRepository.findById(testDocument.getId())).willReturn(Optional.of(testDocument));

        // When
        DocumentDto result = documentService.getDocumentById(testDocument.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testDocument.getId());
        assertThat(result.getOriginalFilename()).isNotNull();
        assertThat(result.getDocumentType()).isEqualTo(testDocumentType);

        verify(documentRepository).findById(testDocument.getId());
    }

    @Test
    @DisplayName("ID-аар баримт авах - Олдсонгүй")
    void getDocumentById_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        given(documentRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> documentService.getDocumentById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Document not found with ID: " + nonExistentId);

        verify(documentRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("Зээлийн хүсэлтийн баримтууд авах - Амжилттай")
    void getDocumentsByLoanApplicationId_Success() {
        // Given
        Document secondDocument = new Document();
        secondDocument.setId(UUID.randomUUID());
        secondDocument.setLoanApplication(testLoanApplication);
        secondDocument.setDocumentType(testDocumentType);
        secondDocument.setStoredFilename("income_statement.pdf");
        secondDocument.setOriginalFilename("Орлогын гэрчилгээ.pdf");
        secondDocument.setFileSize(2048L);
        secondDocument.setContentType("application/pdf");
        secondDocument.setCreatedAt(LocalDateTime.now());

        List<Document> documents = Arrays.asList(testDocument, secondDocument);
        given(documentRepository.findByLoanApplicationId(testLoanApplication.getId(), PageRequest.of(0, 10)))
            .willReturn(new PageImpl<>(documents));

        // When
        List<Document> result = documentService.findByLoanApplicationId(testLoanApplication.getId(), PageRequest.of(0, 10)).getContent();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDocumentType()).isEqualTo(testDocumentType);
        assertThat(result.get(1).getDocumentType()).isEqualTo(testDocumentType);

        verify(documentRepository).findByLoanApplicationId(testLoanApplication.getId(), PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("⭐ ЗАСВАРЛАСАН: Баримт устгах - Амжилттай ⭐")
    void deleteDocument_Success() throws IOException {
        // Given - ⭐ ЗАСВАРЛАСАН: Create actual file to delete ⭐
        Path testFilePath = tempDir.resolve("delete-test.pdf");
        Files.write(testFilePath, "Test content for deletion".getBytes());
        
        Document documentWithValidPath = new Document();
        documentWithValidPath.setId(testDocument.getId());
        documentWithValidPath.setFilePath(testFilePath.toString());
        documentWithValidPath.setVerificationStatus(Document.VerificationStatus.PENDING); // Can be deleted

        // ⭐ ЗАСВАРЛАСАН: Mock both findById calls ⭐
        given(documentRepository.findById(testDocument.getId()))
                .willReturn(Optional.of(documentWithValidPath));
        willDoNothing().given(documentRepository).delete(documentWithValidPath);

        // When
        documentService.deleteDocument(testDocument.getId());

        // Then - ⭐ ЗАСВАРЛАСАН: File should be deleted ⭐
        assertThat(Files.exists(testFilePath)).isFalse();

        verify(documentRepository, times(2)).findById(testDocument.getId()); // Called twice: canDelete + delete
        verify(documentRepository).delete(documentWithValidPath);
    }

    @Test
    @DisplayName("Баримт устгах - Олдсонгүй")
    void deleteDocument_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        given(documentRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> documentService.deleteDocument(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Document not found with ID: " + nonExistentId);

        verify(documentRepository).findById(nonExistentId);
        verify(documentRepository, never()).delete(any(Document.class));
    }

    @Test
    @DisplayName("⭐ ЗАСВАРЛАСАН: Баримт шинэчлэх - Амжилттай ⭐")
    void updateDocument_Success() {
        // Given
        DocumentDto updateDto = new DocumentDto();
        updateDto.setId(testDocument.getId());
        updateDto.setLoanApplicationId(testLoanApplication.getId());
        updateDto.setDocumentType(testDocumentType);
        updateDto.setDescription("Шинэчлэгдсэн тайлбар");
        updateDto.setOriginalFilename("updated-passport.pdf");
        updateDto.setFileSize(2048L);
        updateDto.setContentType("application/pdf");

        Document updatedDocument = new Document();
        updatedDocument.setId(testDocument.getId());
        updatedDocument.setDocumentType(testDocumentType);
        updatedDocument.setDescription("Шинэчлэгдсэн тайлбар");
        updatedDocument.setUpdatedAt(LocalDateTime.now());
        updatedDocument.setVerificationStatus(Document.VerificationStatus.PENDING); // Can be edited

        // ⭐ ЗАСВАРЛАСАН: Mock both findById calls properly ⭐
        given(documentRepository.findById(testDocument.getId()))
                .willReturn(Optional.of(testDocument));
        given(documentRepository.save(any(Document.class))).willReturn(updatedDocument);

        // When
        DocumentDto result = documentService.updateDocument(testDocument.getId(), updateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Шинэчлэгдсэн тайлбар");

        verify(documentRepository, times(2)).findById(testDocument.getId()); // Called twice: canEdit + update
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    @DisplayName("Баримт шинэчлэх - Олдсонгүй")
    void updateDocument_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        DocumentDto updateDto = new DocumentDto();
        updateDto.setDescription("Updated description");
        
        given(documentRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> documentService.updateDocument(nonExistentId, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Document not found with ID: " + nonExistentId);

        verify(documentRepository).findById(nonExistentId);
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    @DisplayName("Баримтын төрлөөр хайх - Амжилттай")
    void getDocumentsByType_Success() {
        // Given
        List<Document> documents = Arrays.asList(testDocument);
        given(documentRepository.findByDocumentType(testDocumentType, PageRequest.of(0, 10)))
            .willReturn(new PageImpl<>(documents));

        // When
        List<DocumentDto> result = documentService.getDocumentsByType(testDocumentType, PageRequest.of(0, 10)).getContent();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDocumentType()).isEqualTo(testDocumentType);

        verify(documentRepository).findByDocumentType(testDocumentType, PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("⭐ ЗАСВАРЛАСАН: Файлын хэмжээ шалгах - Хэт том файл ⭐")
    void uploadDocument_FileTooLarge() {
        // Given - ⭐ ЗАСВАРЛАСАН: File larger than maxFileSize (50MB) ⭐
        byte[] largeContent = new byte[55 * 1024 * 1024]; // 55MB - exceeds 50MB limit
        MultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-file.pdf",
                "application/pdf",
                largeContent
        );

        // When & Then
        assertThatThrownBy(() -> documentService.uploadDocument(
                testCustomer.getId(),
                testLoanApplication.getId(),
                testDocumentType, 
                largeFile, 
                "Test description", 
                "test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File size exceeds limit");

        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    @DisplayName("Дэмжигдэх файлын төрлүүд шалгах")
    void getSupportedFileTypes_Success() {
        // When
        List<String> supportedTypes = documentService.getSupportedFileTypes();

        // Then
        assertThat(supportedTypes).isNotNull();
        assertThat(supportedTypes).contains(
                "application/pdf",
                "image/jpeg",
                "image/png",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
    }

    @Test
    @DisplayName("Зээлийн хүсэлт тус бүрийн баримтын тоо авах")
    void getDocumentCountByLoanApplication_Success() {
        // Given
        given(documentRepository.countByLoanApplicationId(testLoanApplication.getId())).willReturn(5L);

        // When
        long count = documentService.countByLoanApplicationId(testLoanApplication.getId());

        // Then
        assertThat(count).isEqualTo(5L);

        verify(documentRepository).countByLoanApplicationId(testLoanApplication.getId());
    }

    @Test
    @DisplayName("Баримтын storage зай тооцоолох")
    void calculateStorageUsage_Success() {
        // Given
        given(documentRepository.sumFileSizeByLoanApplication(testLoanApplication.getId())).willReturn(1048576L); // 1MB

        // When
        long storageUsage = documentService.sumFileSizeByLoanApplication(testLoanApplication.getId());

        // Then
        assertThat(storageUsage).isEqualTo(1048576L);

        verify(documentRepository).sumFileSizeByLoanApplication(testLoanApplication.getId());
    }

    @Test
    @DisplayName("⭐ ЗАСВАРЛАСАН: Файлын нэр validate хийх - Зөв файлын нэр ⭐")
    void validateFileName_ValidName() throws IOException {
        // Given
        MultipartFile validFile = new MockMultipartFile(
                "file",
                "valid-document-name.pdf",
                "application/pdf",
                "Test content".getBytes()
        );

        given(customerRepository.findById(testCustomer.getId())).willReturn(Optional.of(testCustomer));
        given(loanApplicationRepository.findById(testLoanApplication.getId())).willReturn(Optional.of(testLoanApplication));
        given(documentRepository.findByCustomerIdAndDocumentType(testCustomer.getId(), testDocumentType))
                .willReturn(Optional.empty());
        given(documentRepository.findPotentialDuplicates(anyString(), anyString(), anyLong(), any()))
                .willReturn(Arrays.asList());
        given(documentRepository.save(any(Document.class))).willReturn(testDocument);

        // When & Then - Should not throw any exception
        assertThatCode(() -> documentService.uploadDocument(
                testCustomer.getId(),
                testLoanApplication.getId(),
                testDocumentType, 
                validFile, 
                "Test description", 
                "test"))
                .doesNotThrowAnyException();

        verify(documentRepository).save(any(Document.class));
    }

    @Test
    @DisplayName("Batch файл устгах - Амжилттай")
    void deleteDocumentsByLoanApplication_Success() {
        // Given
        List<Document> documents = Arrays.asList(testDocument);
        
        given(documentRepository.findByLoanApplicationId(eq(testLoanApplication.getId()), any(Pageable.class)))
            .willReturn(new PageImpl<>(documents));
        willDoNothing().given(documentRepository).deleteAll(documents);

        // When
        documentService.deleteByLoanApplicationId(testLoanApplication.getId());

        // Then
        verify(documentRepository).findByLoanApplicationId(eq(testLoanApplication.getId()), any(Pageable.class));
        verify(documentRepository).deleteAll(documents);
    }

    @Test
    @DisplayName("⭐ ЗАСВАРЛАСАН: DTO upload ашиглан баримт хуулах ⭐")
    void uploadDocumentWithDto_Success() throws IOException {
        // Given
        given(customerRepository.findById(testCustomer.getId())).willReturn(Optional.of(testCustomer));
        given(loanApplicationRepository.findById(testLoanApplication.getId())).willReturn(Optional.of(testLoanApplication));
        given(documentRepository.findByCustomerIdAndDocumentType(testCustomer.getId(), testDocumentType))
                .willReturn(Optional.empty());
        given(documentRepository.findPotentialDuplicates(anyString(), anyString(), anyLong(), any()))
                .willReturn(Arrays.asList());
        given(documentRepository.save(any(Document.class))).willReturn(testDocument);

        // When
        DocumentDto result = documentService.uploadDocument(
            testCustomer.getId(),
            testLoanApplication.getId(),
            testDocumentType, 
            testFile, 
            "Test description", 
            "test"
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDocumentType()).isEqualTo(testDocumentType);

        verify(customerRepository).findById(testCustomer.getId());
        verify(loanApplicationRepository).findById(testLoanApplication.getId());
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    @DisplayName("⭐ ЗАСВАРЛАСАН: Баримт verify хийх - Амжилттай ⭐")
    void verifyDocument_Success() {
        // Given
        String verifierName = "test-verifier";
        String notes = "Document verified successfully";
        
        given(documentRepository.findById(testDocument.getId())).willReturn(Optional.of(testDocument));
        
        Document verifiedDocument = new Document();
        verifiedDocument.setId(testDocument.getId());
        verifiedDocument.setVerificationStatus(Document.VerificationStatus.APPROVED);
        verifiedDocument.setVerifiedBy(verifierName);
        verifiedDocument.setVerificationNotes(notes);
        verifiedDocument.setVerifiedAt(LocalDateTime.now());
        
        given(documentRepository.save(any(Document.class))).willReturn(verifiedDocument);

        // When
        DocumentDto result = documentService.verifyDocument(
                testDocument.getId(), 
                Document.VerificationStatus.APPROVED, 
                verifierName, 
                notes
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testDocument.getId());

        verify(documentRepository).findById(testDocument.getId());
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    @DisplayName("⭐ ЗАСВАРЛАСАН: Файлын хэмжээ шалгах логик ⭐")
    void isFileSizeValid_Tests() {
        // Test null file size
        assertThat(documentService.isFileSizeValid(null)).isFalse();

        // Test zero file size
        assertThat(documentService.isFileSizeValid(0L)).isFalse();

        // Test negative file size
        assertThat(documentService.isFileSizeValid(-1L)).isFalse();

        // Test valid file size (under 50MB limit)
        assertThat(documentService.isFileSizeValid(1024L)).isTrue();

        // Test file size at limit (50MB)
        assertThat(documentService.isFileSizeValid(52428800L)).isTrue();

        // Test file size over limit
        assertThat(documentService.isFileSizeValid(52428801L)).isFalse();
    }

    @Test
    @DisplayName("⭐ ЗАСВАРЛАСАН: Document verification status тестүүд ⭐")
    void documentVerificationStatus_Tests() {
        // Given
        List<Document> pendingDocuments = Arrays.asList(testDocument);
        given(documentRepository.findByVerificationStatus(Document.VerificationStatus.PENDING, PageRequest.of(0, 10)))
                .willReturn(new PageImpl<>(pendingDocuments));

        // When
        List<DocumentDto> result = documentService.getDocumentsByVerificationStatus(
                Document.VerificationStatus.PENDING, 
                PageRequest.of(0, 10)
        ).getContent();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(documentRepository).findByVerificationStatus(Document.VerificationStatus.PENDING, PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("⭐ ЗАСВАРЛАСАН: Error handling тестүүд ⭐")
    void errorHandling_Tests() {
        // Test invalid document DTO
        DocumentDto invalidDto = new DocumentDto();
        invalidDto.setOriginalFilename(null); // Invalid

        given(documentRepository.findById(testDocument.getId())).willReturn(Optional.of(testDocument));

        assertThatThrownBy(() -> documentService.updateDocument(testDocument.getId(), invalidDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid document data");

        // Test document that cannot be edited (approved status)
        Document approvedDocument = new Document();
        approvedDocument.setId(testDocument.getId());
        approvedDocument.setVerificationStatus(Document.VerificationStatus.APPROVED);

        given(documentRepository.findById(testDocument.getId())).willReturn(Optional.of(approvedDocument));

        DocumentDto validDto = new DocumentDto();
        validDto.setOriginalFilename("valid.pdf");
        validDto.setDescription("Valid description");

        assertThatThrownBy(() -> documentService.updateDocument(testDocument.getId(), validDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Document cannot be edited");
    }

    @Test
    @DisplayName("⭐ ЗАСВАРЛАСАН: canEdit болон canDelete методын тестүүд ⭐")
    void canEditAndCanDelete_Tests() {
        // Test canEdit with PENDING status (should be editable)
        Document pendingDoc = new Document();
        pendingDoc.setId(UUID.randomUUID());
        pendingDoc.setVerificationStatus(Document.VerificationStatus.PENDING);
        
        given(documentRepository.findById(pendingDoc.getId())).willReturn(Optional.of(pendingDoc));
        
        boolean canEdit = documentService.canEditDocument(pendingDoc.getId());
        assertThat(canEdit).isTrue();

        // Test canEdit with APPROVED status (should not be editable)
        Document approvedDoc = new Document();
        approvedDoc.setId(UUID.randomUUID());
        approvedDoc.setVerificationStatus(Document.VerificationStatus.APPROVED);
        
        given(documentRepository.findById(approvedDoc.getId())).willReturn(Optional.of(approvedDoc));
        
        boolean canEditApproved = documentService.canEditDocument(approvedDoc.getId());
        assertThat(canEditApproved).isFalse();

        // Test canDelete with APPROVED status and version 1 (should not be deletable)
        Document approvedDocV1 = new Document();
        approvedDocV1.setId(UUID.randomUUID());
        approvedDocV1.setVerificationStatus(Document.VerificationStatus.APPROVED);
        approvedDocV1.setVersionNumber(1);
        
        given(documentRepository.findById(approvedDocV1.getId())).willReturn(Optional.of(approvedDocV1));
        
        boolean canDelete = documentService.canDeleteDocument(approvedDocV1.getId());
        assertThat(canDelete).isFalse();
    }

    @Test
    @DisplayName("Олон баримт олох")
    void findMultipleDocuments_Success() {
        // Given
        Document anotherDocument = new Document();
        anotherDocument.setId(UUID.randomUUID());
        anotherDocument.setDocumentType(testDocumentType);

        List<Document> documents = Arrays.asList(testDocument, anotherDocument);
        given(documentRepository.findByLoanApplicationId(testLoanApplication.getId(), PageRequest.of(0, 10)))
                .willReturn(new PageImpl<>(documents));

        // When
        List<Document> result = documentService.findByLoanApplicationId(testLoanApplication.getId(), PageRequest.of(0, 10)).getContent();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(testDocument.getId());
        assertThat(result.get(1).getId()).isEqualTo(anotherDocument.getId());

        verify(documentRepository).findByLoanApplicationId(testLoanApplication.getId(), PageRequest.of(0, 10));
    }
}