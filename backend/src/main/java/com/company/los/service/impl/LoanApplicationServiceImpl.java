package com.company.los.service.impl;

import com.company.los.dto.CreateLoanRequestDto;
import com.company.los.dto.LoanApplicationDto;
import com.company.los.entity.Customer;
import com.company.los.entity.Document;
import com.company.los.entity.LoanApplication;
import com.company.los.entity.LoanProduct;
import com.company.los.enums.LoanStatus;
import com.company.los.exception.ResourceNotFoundException;
import com.company.los.repository.CustomerRepository;
import com.company.los.repository.LoanApplicationRepository;
import com.company.los.repository.LoanProductRepository;
import com.company.los.service.LoanApplicationService;
import com.company.los.service.DocumentService;
import com.company.los.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Зээлийн хүсэлтийн үйлчилгээний логикийг хэрэгжүүлсэн класс.
 *
 * @author LOS Development Team
 * @version 1.1.1
 * @since 2025-08-03
 */
@Service
@RequiredArgsConstructor
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(LoanApplicationServiceImpl.class);

    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final LoanProductRepository loanProductRepository;
    private final DocumentService documentService; // Баримт сервис
    private final NotificationService notificationService; // Мэдэгдлийн сервис

    /**
     * Бүх зээлийн хүсэлтийг хуудаслаж авах.
     *
     * @param pageable Хуудаслалтын мэдээлэл
     * @return Зээлийн хүсэлтүүдийн хуудасласан жагсаалт
     */
    @Override
    @Transactional(readOnly = true)
    public Page<LoanApplicationDto> getAllLoanApplications(Pageable pageable) {
        logger.debug("Fetching all loan applications with pageable: {}", pageable);
        return loanApplicationRepository.findAll(pageable)
                .map(LoanApplicationDto::fromEntity);
    }

    /**
     * ID-гаар зээлийн хүсэлт авах.
     *
     * @param id Зээлийн хүсэлтийн ID
     * @return Зээлийн хүсэлтийн мэдээлэл
     * @throws ResourceNotFoundException Хэрэв зээлийн хүсэлт олддохгүй бол
     */
    @Override
    @Transactional(readOnly = true)
    public LoanApplicationDto getLoanApplicationById(UUID id) {
        logger.debug("Fetching loan application by ID: {}", id);
        return loanApplicationRepository.findById(id)
                .map(LoanApplicationDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found with ID: " + id));
    }

    /**
     * Шинэ зээлийн хүсэлт үүсгэх.
     *
     * @param createRequestDto Үүсгэх зээлийн хүсэлтийн мэдээлэл
     * @return Үүсгэсэн зээлийн хүсэлтийн мэдээлэл
     * @throws ResourceNotFoundException Хэрэв харилцагч олддохгүй бол
     * @throws IllegalArgumentException  Хэрэв зээлийн хүсэлтийн дугаар давхардсан бол
     */
    @Override
    @Transactional
    public LoanApplicationDto createLoanApplication(CreateLoanRequestDto createRequestDto) {
        logger.info("Creating new loan application for customer ID: {}", createRequestDto.getCustomerId());

        Customer customer = customerRepository.findById(createRequestDto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + createRequestDto.getCustomerId()));

        // Validation
        if (!isValidLoanRequest(createRequestDto)) {
            throw new IllegalArgumentException("Invalid loan application data provided");
        }

        if (!isWithinLoanTypeLimits(createRequestDto)) {
            throw new IllegalArgumentException("Loan amount exceeds type limits");
        }

        // Check if customer can apply for loan
        if (!canCustomerApplyForLoan(customer)) {
            throw new IllegalArgumentException("Customer is not eligible to apply for loan");
        }

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setId(UUID.randomUUID());
        loanApplication.setCustomer(customer);
        loanApplication.setLoanType(createRequestDto.getLoanType());
        loanApplication.setRequestedAmount(createRequestDto.getRequestedAmount());
        loanApplication.setRequestedTermMonths(createRequestDto.getRequestedTermMonths());
        loanApplication.setPurpose(createRequestDto.getPurpose());
        loanApplication.setDeclaredIncome(createRequestDto.getDeclaredIncome());
        loanApplication.setPriority(createRequestDto.getPriority() != null ? createRequestDto.getPriority() : 3);
        loanApplication.setAssignedTo(createRequestDto.getAssignedTo());
        loanApplication.setCreatedAt(LocalDateTime.now());
        loanApplication.setUpdatedAt(LocalDateTime.now());

        // Зээлийн хүсэлтийн дугаар үүсгэх (жишээ: LN-YYYY-XXXX)
        String applicationNumber = generateUniqueApplicationNumber();
        // ⭐ ЗАСВАР: Давхардсан дугаар байгаа эсэхийг шалгах ⭐
        if (loanApplicationRepository.findByApplicationNumber(applicationNumber).isPresent()) {
            // Энэ нь маш ховор тохиолдолд үүсэх ёстой, учир нь UUID-аас үүсгэсэн.
            // Гэхдээ тестүүдэд зориулж энд exception үүсгэж болно.
            throw new IllegalArgumentException("Application number already exists: " + applicationNumber);
        }
        loanApplication.setApplicationNumber(applicationNumber);

        // Set status and submitted date if not saving as draft
        if (createRequestDto.isSaveAsDraft()) {
            loanApplication.setStatus(LoanApplication.ApplicationStatus.DRAFT);
        } else {
            loanApplication.setStatus(LoanApplication.ApplicationStatus.SUBMITTED);
        }
        
        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        
        logger.info("Loan application created successfully with ID: {}", savedApplication.getId());

        // Notification service дуудах
        try {
            notificationService.sendApplicationCreatedNotification(savedApplication);
        } catch (Exception e) {
            logger.warn("Failed to send application created notification: {}", e.getMessage());
        }
        
        return LoanApplicationDto.fromEntity(savedApplication);
    }

    /**
     * Зээлийн хүсэлтийг шинэчлэх.
     * Зөвхөн DRAFT төлөвтэй хүсэлтийг шинэчлэх боломжтой.
     *
     * @param id               Зээлийн хүсэлтийн ID
     * @param loanApplicationDto Шинэчлэх мэдээлэл
     * @return Шинэчлэгдсэн зээлийн хүсэлтийн мэдээлэл
     * @throws ResourceNotFoundException Хэрэв зээлийн хүсэлт олддохгүй бол
     * @throws IllegalArgumentException  Хэрэв зээлийн хүсэлт DRAFT төлөвт биш бол
     */
    @Override
    @Transactional
    public LoanApplicationDto updateLoanApplication(UUID id, LoanApplicationDto loanApplicationDto) {
        logger.info("Updating loan application with ID: {}", id);

        LoanApplication existingApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found with ID: " + id));

        // Check if can be edited
        if (!canBeEdited(existingApplication)) {
            throw new IllegalStateException("Loan application cannot be edited in current status");
        }

        // Validation
        if (!isValidLoanRequest(loanApplicationDto)) {
            throw new IllegalArgumentException("Invalid loan application data");
        }
        if (!isWithinLoanTypeLimits(loanApplicationDto)) {
            throw new IllegalArgumentException("Loan amount exceeds type limits");
        }

        // Update fields
        existingApplication.setLoanType(loanApplicationDto.getLoanType());
        existingApplication.setRequestedAmount(loanApplicationDto.getRequestedAmount());
        existingApplication.setRequestedTermMonths(loanApplicationDto.getRequestedTermMonths());
        existingApplication.setPurpose(loanApplicationDto.getPurpose());
        existingApplication.setDeclaredIncome(loanApplicationDto.getDeclaredIncome());
        existingApplication.setPriority(loanApplicationDto.getPriority());
        existingApplication.setAssignedTo(loanApplicationDto.getAssignedTo());
        existingApplication.setUpdatedAt(LocalDateTime.now());

        LoanApplication savedApplication = loanApplicationRepository.save(existingApplication);
        logger.info("Loan application updated successfully with ID: {}", savedApplication.getId());

        return LoanApplicationDto.fromEntity(savedApplication);
    }

    /**
     * Зээлийн хүсэлтийн статусыг шинэчлэх.
     *
     * @param id     Зээлийн хүсэлтийн ID
     * @param status Шинэ статус
     * @return Шинэчлэгдсэн зээлийн хүсэлтийн мэдээлэл
     * @throws ResourceNotFoundException Хэрэв зээлийн хүсэлт олддохгүй бол
     */
    @Override
    @Transactional
    public LoanApplicationDto updateLoanApplicationStatus(UUID id, LoanStatus status) {
        logger.info("Updating status for loan application ID: {} to {}", id, status);
        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found with ID: " + id));

        LoanApplication.ApplicationStatus targetStatus = convertLoanStatusToApplicationStatus(status);

        // ⭐ ЗАСВАР: Status transition validation нэмсэн ⭐
        if (!isValidStatusTransition(loanApplication.getStatus(), targetStatus)) {
            throw new IllegalStateException("Invalid status transition from " + loanApplication.getStatus() + " to " + targetStatus);
        }

        loanApplication.setStatus(targetStatus);
        loanApplication.setUpdatedAt(LocalDateTime.now());

        LoanApplication updatedApplication = loanApplicationRepository.save(loanApplication);
        logger.info("Loan application status updated successfully for ID: {}", id);

        // Мэдэгдэл илгээх
        notificationService.sendStatusUpdateNotification(updatedApplication);

        return LoanApplicationDto.fromEntity(updatedApplication);
    }

    /**
     * Зээлийн хүсэлтийг устгах.
     * Зөвхөн DRAFT төлөвтэй, баримтгүй хүсэлтийг устгах боломжтой.
     *
     * @param id Устгах зээлийн хүсэлтийн ID
     * @throws ResourceNotFoundException Хэрэв зээлийн хүсэлт олддохгүй бол
     * @throws IllegalArgumentException  Хэрэв зээлийн хүсэлт DRAFT төлөвт биш эсвэл баримттай бол
     */
    @Override
    @Transactional
    public void deleteLoanApplication(UUID id) {
        logger.info("Deleting loan application with ID: {}", id);
        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found with ID: " + id));

        if (loanApplication.getStatus() != LoanApplication.ApplicationStatus.DRAFT) {
            throw new IllegalArgumentException("Only DRAFT loan applications can be deleted. Current status: " + loanApplication.getStatus());
        }

        // ⭐ ЗАСВАР: Холбоотой баримт байгаа эсэхийг шалгах ⭐
        // documentService.findByLoanApplicationId нь Page буцаадаг тул content-ийг шалгана.
        if (documentService != null) {
            try {
                Page<Document> documents = documentService.findByLoanApplicationId(id, Pageable.unpaged());
                if (documents != null && documents.hasContent()) {
                    throw new IllegalArgumentException("Cannot delete loan application with associated documents. Please remove documents first.");
                }
            } catch (Exception e) {
                logger.warn("Could not check for associated documents: {}", e.getMessage());
                // Алдаа гарсан ч устгахыг зөвшөөрөхгүй байх
                throw new IllegalStateException("Failed to check for associated documents, cannot proceed with deletion.");
            }
        }

        loanApplicationRepository.delete(loanApplication);
        logger.info("Loan application deleted successfully with ID: {}", id);
    }

    @Override
    public LoanApplicationDto restoreLoanApplication(UUID id) {
        logger.info("Restoring loan application with ID: {}", id);

        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found with ID: " + id));

        loanApplication.setIsDeleted(false);
        loanApplication.setUpdatedAt(LocalDateTime.now());
        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);

        logger.info("Loan application restored successfully with ID: {}", id);
        return LoanApplicationDto.fromEntity(savedApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplicationDto getLoanApplicationByNumber(String applicationNumber) {
        logger.debug("Getting loan application by number: {}", applicationNumber);

        LoanApplication loanApplication = loanApplicationRepository.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found with number: " + applicationNumber));

        return LoanApplicationDto.fromEntity(loanApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanApplicationDto> getLoanApplicationsByCustomer(UUID customerId, Pageable pageable) {
        logger.debug("Getting loan applications by customer: {}", customerId);

        Page<LoanApplication> applications = loanApplicationRepository.findByCustomer_Id(customerId, pageable);
        return applications.map(LoanApplicationDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanApplicationDto> getLoanApplicationsByStatus(LoanStatus status, Pageable pageable) {
        logger.debug("Getting loan applications by status: {}", status);

        // Convert LoanStatus to ApplicationStatus for repository call
        LoanApplication.ApplicationStatus appStatus = convertLoanStatusToApplicationStatus(status);
        Page<LoanApplication> applications = loanApplicationRepository.findByStatus(appStatus, pageable);
        return applications.map(LoanApplicationDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanApplicationDto> getLoanApplicationsByType(LoanApplication.LoanType loanType, Pageable pageable) {
        logger.debug("Getting loan applications by type: {}", loanType);

        Page<LoanApplication> applications = loanApplicationRepository.findByLoanType(loanType, pageable);
        return applications.map(LoanApplicationDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanApplicationDto> searchLoanApplications(String searchTerm, Pageable pageable) {
        logger.debug("Searching loan applications with term: {}", searchTerm);

        // ⭐ Fallback logic: Хэрэв findByApplicationNumberContainingIgnoreCase method байхгүй бол findAll ашиглана ⭐
        try {
            return loanApplicationRepository.findByApplicationNumberContainingIgnoreCase(searchTerm, pageable)
                    .map(LoanApplicationDto::fromEntity);
        } catch (UnsupportedOperationException e) {
            logger.warn("findByApplicationNumberContainingIgnoreCase not supported, falling back to in-memory filter for query: {}", searchTerm);
            List<LoanApplication> allApplications = loanApplicationRepository.findAll();
            List<LoanApplication> filteredApplications = allApplications.stream()
                    .filter(app -> app.getApplicationNumber().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                   (app.getPurpose() != null && app.getPurpose().toLowerCase().contains(searchTerm.toLowerCase())) ||
                                   (app.getCustomer() != null && app.getCustomer().getFirstName() != null && app.getCustomer().getFirstName().toLowerCase().contains(searchTerm.toLowerCase())) ||
                                   (app.getCustomer() != null && app.getCustomer().getLastName() != null && app.getCustomer().getLastName().toLowerCase().contains(searchTerm.toLowerCase())))
                    .collect(Collectors.toList());

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredApplications.size());
            List<LoanApplicationDto> pageContent = filteredApplications.subList(start, end).stream()
                                                    .map(LoanApplicationDto::fromEntity)
                                                    .collect(Collectors.toList());
            return new PageImpl<>(pageContent, pageable, filteredApplications.size());
        }
    }

    @Override
    public LoanApplicationDto submitLoanApplication(UUID id) {
        logger.info("Submitting loan application: {}", id);

        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found with ID: " + id));

        // Check status using ApplicationStatus enum
        if (loanApplication.getStatus() != LoanApplication.ApplicationStatus.DRAFT) {
            throw new IllegalArgumentException("Only draft applications can be submitted");
        }

        if (!isValidForSubmission(loanApplication)) {
            throw new IllegalArgumentException("Application data is incomplete");
        }

        loanApplication.submit();
        loanApplication.setUpdatedAt(LocalDateTime.now());

        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        logger.info("Loan application submitted successfully with ID: {}", id);
        return LoanApplicationDto.fromEntity(savedApplication);
    }

    @Override
    public LoanApplicationDto approveLoanApplication(UUID id, BigDecimal approvedAmount,
                                                    Integer approvedTermMonths, BigDecimal approvedRate,
                                                    String reason) {
        logger.info("Approving loan application: {}", id);

        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found with ID: " + id));

        if (!canBeApproved(loanApplication)) {
            throw new IllegalStateException("Loan application cannot be approved in current status");
        }

        loanApplication.approve("system", approvedAmount, approvedTermMonths, approvedRate);
        loanApplication.setDecisionReason(reason);
        BigDecimal monthlyPayment = calculateMonthlyPayment(approvedAmount, approvedTermMonths, approvedRate);
        loanApplication.setMonthlyPayment(monthlyPayment);
        loanApplication.setUpdatedAt(LocalDateTime.now());

        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        logger.info("Loan application approved successfully: {}", id);
        return LoanApplicationDto.fromEntity(savedApplication);
    }

    @Override
    public LoanApplicationDto rejectLoanApplication(UUID id, String reason) {
        logger.info("Rejecting loan application: {}", id);

        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found with ID: " + id));

        if (!canBeRejected(loanApplication)) {
            throw new IllegalStateException("Loan application cannot be rejected in current status");
        }

        loanApplication.reject("system", reason);
        loanApplication.setUpdatedAt(LocalDateTime.now());

        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        logger.info("Loan application rejected successfully: {}", id);
        return LoanApplicationDto.fromEntity(savedApplication);
    }

    @Override
    public LoanApplicationDto disburseLoan(UUID id) {
        logger.info("Disbursing loan: {}", id);

        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found with ID: " + id));

        if (loanApplication.getStatus() != LoanApplication.ApplicationStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved loans can be disbursed");
        }

        loanApplication.disburse("system", loanApplication.getApprovedAmount());
        loanApplication.setUpdatedAt(LocalDateTime.now());

        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        logger.info("Loan disbursed successfully: {}", id);
        return LoanApplicationDto.fromEntity(savedApplication);
    }

    @Override
    public LoanApplicationDto cancelLoanApplication(UUID id, String reason) {
        logger.info("Cancelling loan application: {}", id);

        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found with ID: " + id));

        if (isFinalStatus(loanApplication)) {
            throw new IllegalArgumentException("Final status applications cannot be cancelled");
        }

        loanApplication.setStatus(LoanApplication.ApplicationStatus.CANCELLED);
        loanApplication.setDecisionReason(reason);
        loanApplication.setUpdatedAt(LocalDateTime.now());

        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        logger.info("Loan application cancelled successfully: {}", id);
        return LoanApplicationDto.fromEntity(savedApplication);
    }

    @Override
    public LoanApplicationDto requestAdditionalInfo(UUID id, String requestedInfo) {
        logger.info("Requesting additional info for loan application: {}", id);

        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found with ID: " + id));

        loanApplication.setStatus(LoanApplication.ApplicationStatus.PENDING_DOCUMENTS);
        loanApplication.setDecisionReason(requestedInfo);
        loanApplication.setUpdatedAt(LocalDateTime.now());

        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        logger.info("Additional info requested for loan application: {}", id);
        return LoanApplicationDto.fromEntity(savedApplication);
    }

    @Override
    public BigDecimal calculateMonthlyPayment(BigDecimal principal, Integer termMonths, BigDecimal annualRate) {
        logger.debug("Calculating monthly payment for principal: {}, term: {}, rate: {}", principal, termMonths, annualRate);

        if (principal == null || termMonths == null || annualRate == null || termMonths <= 0) {
            return BigDecimal.ZERO;
        }

        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        }

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal denominator = BigDecimal.ONE.subtract(
                BigDecimal.ONE.divide(onePlusRate.pow(termMonths), 10, RoundingMode.HALF_UP)
        );

        // ⭐ ЗАСВАР: Тэгээр хуваахаас сэргийлэх ⭐
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return principal.multiply(monthlyRate).divide(denominator, 2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateTotalPayment(BigDecimal principal, Integer termMonths, BigDecimal annualRate) {
        BigDecimal monthlyPayment = calculateMonthlyPayment(principal, termMonths, annualRate);
        return monthlyPayment.multiply(BigDecimal.valueOf(termMonths)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateTotalInterest(BigDecimal principal, Integer termMonths, BigDecimal annualRate) {
        BigDecimal totalPayment = calculateTotalPayment(principal, termMonths, annualRate);
        return totalPayment.subtract(principal).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public List<Map<String, Object>> generateAmortizationSchedule(BigDecimal principal, Integer termMonths, BigDecimal annualRate) {
        logger.debug("Generating amortization schedule");

        List<Map<String, Object>> schedule = new ArrayList<>();
        BigDecimal monthlyPayment = calculateMonthlyPayment(principal, termMonths, annualRate);
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal remainingBalance = principal;

        for (int month = 1; month <= termMonths; month++) {
            BigDecimal interestPayment = remainingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPayment = monthlyPayment.subtract(interestPayment).setScale(2, RoundingMode.HALF_UP);

            // Сүүлийн төлбөр дээр үлдэгдлийг тааруулах
            if (month == termMonths) {
                principalPayment = remainingBalance;
                monthlyPayment = principalPayment.add(interestPayment).setScale(2, RoundingMode.HALF_UP);
            }

            remainingBalance = remainingBalance.subtract(principalPayment).setScale(2, RoundingMode.HALF_UP);

            Map<String, Object> payment = new HashMap<>();
            payment.put("month", month);
            payment.put("payment", monthlyPayment);
            payment.put("principal", principalPayment);
            payment.put("interest", interestPayment);
            payment.put("balance", remainingBalance.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remainingBalance); // Сөрөг утгаас сэргийлэх

            schedule.add(payment);
        }

        return schedule;
    }

    @Override
    public boolean existsByApplicationNumber(String applicationNumber) {
        return loanApplicationRepository.existsByApplicationNumber(applicationNumber);
    }

    @Override
    public boolean validateLoanApplication(LoanApplicationDto loanApplicationDto) {
        return isValidLoanRequest(loanApplicationDto) && isWithinLoanTypeLimits(loanApplicationDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canEditLoanApplication(UUID id) {
        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found with ID: " + id));

        return canBeEdited(loanApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canApproveLoanApplication(UUID id) {
        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found with ID: " + id));

        return canBeApproved(loanApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalLoanApplicationCount() {
        logger.debug("Getting total loan application count");
        return loanApplicationRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getLoanApplicationStatistics() {
        logger.debug("Getting loan application statistics");

        Map<String, Object> stats = new HashMap<>();

        long totalApplications = loanApplicationRepository.count();
        stats.put("totalApplications", totalApplications);

        Map<LoanApplication.ApplicationStatus, Long> byStatus = loanApplicationRepository.findAll().stream()
                .collect(Collectors.groupingBy(LoanApplication::getStatus, Collectors.counting()));
        stats.put("byStatus", byStatus);

        Map<LoanApplication.LoanType, Long> byType = loanApplicationRepository.findAll().stream()
                .collect(Collectors.groupingBy(LoanApplication::getLoanType, Collectors.counting()));
        stats.put("byType", byType);

        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        long todaySubmissions = loanApplicationRepository.findAll().stream()
                .filter(app -> app.getCreatedAt() != null && app.getCreatedAt().isAfter(startOfDay))
                .count();
        stats.put("todaySubmissions", todaySubmissions);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<LoanStatus, Long> getLoanApplicationCountByStatus() {
        return loanApplicationRepository.findAll().stream()
                .collect(Collectors.groupingBy(app -> convertApplicationStatusToLoanStatus(app.getStatus()), Collectors.counting()));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<LoanApplication.LoanType, Long> getLoanApplicationCountByType() {
        return loanApplicationRepository.findAll().stream()
                .collect(Collectors.groupingBy(LoanApplication::getLoanType, Collectors.counting()));
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageProcessingDays() {
        List<LoanApplication> processedApplications = loanApplicationRepository.findAll().stream()
                .filter(app -> app.getCreatedAt() != null && app.getDecisionDate() != null)
                .filter(app -> app.getStatus() == LoanApplication.ApplicationStatus.APPROVED || app.getStatus() == LoanApplication.ApplicationStatus.REJECTED)
                .collect(Collectors.toList());

        if (processedApplications.isEmpty()) {
            return 0.0;
        }

        long totalDays = processedApplications.stream()
                .mapToLong(app -> java.time.Duration.between(app.getCreatedAt(), app.getDecisionDate()).toDays())
                .sum();

        return (double) totalDays / processedApplications.size();
    }

    @Override
    public Page<LoanApplicationDto> searchLoanApplicationsWithFilters(LoanStatus status,
                                                                     LoanApplication.LoanType loanType,
                                                                     com.company.los.entity.Customer.CustomerType customerType,
                                                                     BigDecimal minAmount, BigDecimal maxAmount,
                                                                     LocalDateTime startDate, LocalDateTime endDate,
                                                                     String assignedTo, Integer priority,
                                                                     Pageable pageable) {
        // ⭐ ЗАСВАР: Repository method ашиглах эсвэл fallback логик ⭐
        try {
            LoanApplication.ApplicationStatus statusEnum = status != null ? convertLoanStatusToApplicationStatus(status) : null;
            return loanApplicationRepository.findByCustomer_CustomerTypeAndLoanTypeAndStatusAndRequestedAmountBetweenAndCreatedAtBetween(
                    customerType, loanType, statusEnum, minAmount, maxAmount, startDate, endDate, pageable)
                    .map(LoanApplicationDto::fromEntity);
        } catch (UnsupportedOperationException e) {
            logger.warn("Repository method for searchLoanApplicationsWithFilters not available, falling back to in-memory filter: {}", e.getMessage());
            List<LoanApplication> allApplications = loanApplicationRepository.findAll();
            List<LoanApplication> filteredApplications = allApplications.stream()
                    .filter(app -> {
                        if (status != null && app.getStatus() != convertLoanStatusToApplicationStatus(status)) return false;
                        if (loanType != null && app.getLoanType() != loanType) return false;
                        if (customerType != null && app.getCustomer() != null && app.getCustomer().getCustomerType() != customerType) return false;
                        if (minAmount != null && app.getRequestedAmount() != null && app.getRequestedAmount().compareTo(minAmount) < 0) return false;
                        if (maxAmount != null && app.getRequestedAmount() != null && app.getRequestedAmount().compareTo(maxAmount) > 0) return false;
                        if (startDate != null && app.getCreatedAt() != null && app.getCreatedAt().isBefore(startDate)) return false;
                        if (endDate != null && app.getCreatedAt() != null && app.getCreatedAt().isAfter(endDate)) return false;
                        if (assignedTo != null && app.getAssignedTo() != null && !app.getAssignedTo().equalsIgnoreCase(assignedTo)) return false;
                        if (priority != null && app.getPriority() != null && app.getPriority() != priority) return false;
                        return true;
                    })
                    .collect(Collectors.toList());
            
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredApplications.size());
            List<LoanApplicationDto> pageContent = filteredApplications.subList(start, end).stream()
                                                    .map(LoanApplicationDto::fromEntity)
                                                    .collect(Collectors.toList());
            return new PageImpl<>(pageContent, pageable, filteredApplications.size());
        }
    }

    @Override
    public LoanApplicationDto assignLoanApplication(UUID id, String assignedTo) {
        logger.info("Assigning loan application {} to {}", id, assignedTo);

        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found with ID: " + id));

        loanApplication.setAssignedTo(assignedTo);
        loanApplication.setUpdatedAt(LocalDateTime.now());

        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        logger.info("Loan application assigned successfully");
        return LoanApplicationDto.fromEntity(savedApplication);
    }

    @Override
    public int assignLoanApplications(List<UUID> applicationIds, String assignedTo) {
        int updatedCount = 0;
        for (UUID id : applicationIds) {
            try {
                assignLoanApplication(id, assignedTo);
                updatedCount++;
            } catch (Exception e) {
                logger.error("Failed to assign application: {}", id, e);
            }
        }

        return updatedCount;
    }

    @Override
    public LoanApplicationDto updatePriority(UUID id, Integer priority) {
        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found with ID: " + id));

        loanApplication.setPriority(priority);
        loanApplication.setUpdatedAt(LocalDateTime.now());

        loanApplicationRepository.save(loanApplication);
        return getLoanApplicationById(id);
    }

    @Override
    public Page<LoanApplicationDto> getLoanApplicationsByPriority(Integer priority, Pageable pageable) {
        return loanApplicationRepository.findByPriority(priority, pageable)
                .map(LoanApplicationDto::fromEntity);
    }

    @Override
    public LoanApplicationDto performRiskAssessment(UUID id) {
        logger.info("Performing risk assessment for loan application: {}", id);

        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found with ID: " + id));

        Customer customer = customerRepository.findById(loanApplication.getCustomer().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + loanApplication.getCustomer().getId()));

        // Simple risk assessment logic
        String assessmentResult;
        Integer assessmentScore;
        String assessmentNotes;

        BigDecimal customerMonthlyIncome = customer.getMonthlyIncome() != null ? customer.getMonthlyIncome() : BigDecimal.ZERO;

        if (customer.getCreditScore() != null && customer.getCreditScore() >= 700 && customerMonthlyIncome.compareTo(new BigDecimal("1500000")) >= 0) {
            assessmentResult = "APPROVED";
            assessmentScore = 85;
            assessmentNotes = "Customer is eligible for loan";
        } else {
            assessmentResult = "REJECTED";
            assessmentScore = 35;
            assessmentNotes = "Insufficient credit score, low income or unemployed";
        }

        loanApplication.setAssessmentResult(assessmentResult);
        loanApplication.setAssessmentScore(assessmentScore);
        loanApplication.setAssessmentNotes(assessmentNotes);
        loanApplication.setAssessedAt(LocalDateTime.now());
        loanApplication.setUpdatedAt(LocalDateTime.now());

        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        logger.info("Risk assessment performed for loan application: {}", id);
        return LoanApplicationDto.fromEntity(savedApplication);
    }

    @Override
    public BigDecimal calculateCreditScore(UUID customerId) {
        // ⭐ PLACEHOLDER: Бодит кредит оноо тооцоолох логик нэмэх ⭐
        return BigDecimal.valueOf(650);
    }

    @Override
    public Page<LoanApplicationDto> getHighRiskApplications(BigDecimal riskThreshold, Pageable pageable) {
        // ⭐ PLACEHOLDER: Өндөр эрсдэлтэй хүсэлтүүдийг шүүх логик нэмэх ⭐
        return loanApplicationRepository.findAll(pageable)
                .map(LoanApplicationDto::fromEntity);
    }

    @Override
    public Page<LoanApplicationDto> getLowRiskApplications(BigDecimal riskThreshold, Pageable pageable) {
        // ⭐ PLACEHOLDER: Бага эрсдэлтэй хүсэлтүүдийг шүүх логик нэмэх ⭐
        return loanApplicationRepository.findAll(pageable)
                .map(LoanApplicationDto::fromEntity);
    }

    @Override
    public Page<LoanApplicationDto> getOverdueApplications(Pageable pageable) {
        LocalDateTime overdueThreshold = LocalDateTime.now().minusDays(7);
        return loanApplicationRepository.findByStatusAndCreatedAtBefore(LoanApplication.ApplicationStatus.PENDING, overdueThreshold, pageable)
                .map(LoanApplicationDto::fromEntity);
    }

    @Override
    public List<LoanApplicationDto> getPendingTooLong(LoanStatus status, int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        LoanApplication.ApplicationStatus appStatus = convertLoanStatusToApplicationStatus(status);
        return loanApplicationRepository.findByStatusAndCreatedAtBefore(appStatus, threshold)
                .stream()
                .map(LoanApplicationDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getMonthlyLoanApplicationStats(int months) {
        logger.debug("Getting monthly loan application stats for last {} months", months);
        List<LoanApplication> allApplications = loanApplicationRepository.findAll();
        
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        Map<String, Long> monthlyCounts = allApplications.stream()
            .filter(app -> app.getCreatedAt() != null && app.getCreatedAt().isAfter(startDate))
            .collect(Collectors.groupingBy(
                app -> app.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")),
                Collectors.counting()
            ));

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            LocalDateTime month = LocalDateTime.now().minusMonths(i);
            String monthKey = month.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", monthKey);
            monthData.put("count", monthlyCounts.getOrDefault(monthKey, 0L));
            result.add(monthData);
        }
        return result;
    }

    @Override
    public Map<String, Object> getApprovalRates(LocalDateTime startDate) {
        Map<String, Object> rates = new HashMap<>();
        List<LoanApplication> applications = loanApplicationRepository.findAll().stream()
                .filter(app -> app.getDecisionDate() != null && app.getDecisionDate().isAfter(startDate))
                .collect(Collectors.toList());

        long approved = applications.stream().filter(app -> app.getStatus() == LoanApplication.ApplicationStatus.APPROVED).count();
        long rejected = applications.stream().filter(app -> app.getStatus() == LoanApplication.ApplicationStatus.REJECTED).count();
        long total = approved + rejected;

        rates.put("approved", approved);
        rates.put("rejected", rejected);
        rates.put("total", total);
        rates.put("approvalRate", total > 0 ? (double) approved / total : 0.0);
        rates.put("rejectionRate", total > 0 ? (double) rejected / total : 0.0);
        return rates;
    }

    @Override
    public Page<LoanApplicationDto> getFastestApprovedApplications(Pageable pageable) {
        // ⭐ PLACEHOLDER: Хамгийн хурдан батлагдсан хүсэлтүүдийг авах логик нэмэх ⭐
        return loanApplicationRepository.findAll(pageable)
                .map(LoanApplicationDto::fromEntity);
    }

    @Override
    public Map<String, Object> getTodayDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();

        long todaySubmitted = loanApplicationRepository.findAll().stream()
                .filter(app -> app.getCreatedAt() != null && app.getCreatedAt().isAfter(startOfDay) && app.getStatus() == LoanApplication.ApplicationStatus.SUBMITTED)
                .count();
        long pending = loanApplicationRepository.findAll().stream()
                .filter(app -> app.getStatus() == LoanApplication.ApplicationStatus.PENDING || app.getStatus() == LoanApplication.ApplicationStatus.UNDER_REVIEW)
                .count();
        long todayApproved = loanApplicationRepository.findAll().stream()
                .filter(app -> app.getDecisionDate() != null && app.getDecisionDate().isAfter(startOfDay) && app.getStatus() == LoanApplication.ApplicationStatus.APPROVED)
                .count();
        long todayDisbursed = loanApplicationRepository.findAll().stream()
                .filter(app -> app.getDisbursedAt() != null && app.getDisbursedAt().isAfter(startOfDay) && app.getStatus() == LoanApplication.ApplicationStatus.DISBURSED)
                .count();

        stats.put("todaySubmitted", todaySubmitted);
        stats.put("pending", pending);
        stats.put("todayApproved", todayApproved);
        stats.put("todayDisbursed", todayDisbursed);
        return stats;
    }

    @Override
    public Map<String, Object> getThisMonthDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        long thisMonthSubmitted = loanApplicationRepository.findAll().stream()
                .filter(app -> app.getCreatedAt() != null && app.getCreatedAt().isAfter(startOfMonth) && app.getStatus() == LoanApplication.ApplicationStatus.SUBMITTED)
                .count();
        BigDecimal thisMonthApprovedAmount = loanApplicationRepository.findAll().stream()
                .filter(app -> app.getDecisionDate() != null && app.getDecisionDate().isAfter(startOfMonth) && app.getStatus() == LoanApplication.ApplicationStatus.APPROVED)
                .map(LoanApplication::getApprovedAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long thisMonthApproved = loanApplicationRepository.findAll().stream()
                .filter(app -> app.getDecisionDate() != null && app.getDecisionDate().isAfter(startOfMonth) && app.getStatus() == LoanApplication.ApplicationStatus.APPROVED)
                .count();

        stats.put("thisMonthSubmitted", thisMonthSubmitted);
        stats.put("thisMonthApprovedAmount", thisMonthApprovedAmount);
        stats.put("thisMonthApproved", thisMonthApproved);
        return stats;
    }

    @Override
    public List<Map<String, Object>> getLoanReport(LocalDateTime startDate, LocalDateTime endDate) {
        return loanApplicationRepository.findAll().stream()
                .filter(app -> app.getCreatedAt() != null && app.getCreatedAt().isAfter(startDate) && app.getCreatedAt().isBefore(endDate))
                .map(app -> {
                    Map<String, Object> reportData = new HashMap<>();
                    reportData.put("applicationNumber", app.getApplicationNumber());
                    reportData.put("customerName", app.getCustomer() != null ? app.getCustomer().getFirstName() + " " + app.getCustomer().getLastName() : "N/A");
                    reportData.put("loanType", app.getLoanType());
                    reportData.put("requestedAmount", app.getRequestedAmount());
                    reportData.put("status", app.getStatus());
                    reportData.put("createdAt", app.getCreatedAt());
                    reportData.put("approvedAmount", app.getApprovedAmount());
                    reportData.put("decisionDate", app.getDecisionDate());
                    reportData.put("decisionReason", app.getDecisionReason());
                    return reportData;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getPerformanceReport(LocalDateTime startDate, LocalDateTime endDate) {
        // ⭐ PLACEHOLDER: Гүйцэтгэлийн тайлангийн логик нэмэх ⭐
        return new ArrayList<>();
    }

    @Override
    public byte[] exportLoanApplicationsToExcel(List<UUID> applicationIds) {
        logger.warn("exportLoanApplicationsToExcel not implemented yet");
        return new byte[0];
    }

    @Override
    public LoanApplicationDto getLatestLoanApplicationByCustomer(UUID customerId) {
        List<LoanApplication> applications = loanApplicationRepository.findByCustomer_Id(customerId,
                PageRequest.of(0, 1, org.springframework.data.domain.Sort.by("createdAt").descending())).getContent();

        if (!applications.isEmpty()) {
            return LoanApplicationDto.fromEntity(applications.get(0));
        }
        return null;
    }

    @Override
    public int getActiveLoansCountForCustomer(UUID customerId) {
        return (int) loanApplicationRepository.findByCustomer_IdAndStatusIn(customerId,
                Arrays.asList(LoanApplication.ApplicationStatus.APPROVED, LoanApplication.ApplicationStatus.DISBURSED)).size();
    }

    @Override
    public List<LoanApplicationDto> getCustomerLoanHistory(UUID customerId) {
        return loanApplicationRepository.findByCustomer_Id(customerId, Pageable.unpaged())
                .getContent()
                .stream()
                .map(LoanApplicationDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int updateStatusForApplications(List<UUID> applicationIds, LoanStatus currentStatus, LoanStatus newStatus) {
        int updatedCount = 0;
        for (UUID id : applicationIds) {
            try {
                updateLoanApplicationStatus(id, newStatus);
                updatedCount++;
            } catch (Exception e) {
                logger.error("Failed to update application: {}", id, e);
            }
        }

        return updatedCount;
    }

    @Override
    public boolean checkLoanLimits(UUID customerId, BigDecimal requestedAmount) {
        // ⭐ PLACEHOLDER: Зээлийн хязгаарыг шалгах логик нэмэх ⭐
        return true;
    }

    @Override
    public Map<String, Object> assessLoanCapacity(UUID customerId, BigDecimal requestedAmount) {
        // ⭐ PLACEHOLDER: Зээлийн чадварыг үнэлэх логик нэмэх ⭐
        return new HashMap<>();
    }

    @Override
    public boolean sendStatusChangeNotification(UUID id) {
        // ⭐ PLACEHOLDER: Статус өөрчлөгдсөн мэдэгдэл илгээх логик нэмэх ⭐
        return true;
    }

    @Override
    public boolean sendOverdueNotification(UUID id) {
        // ⭐ PLACEHOLDER: Хугацаа хэтэрсэн мэдэгдэл илгээх логик нэмэх ⭐
        return true;
    }

    @Override
    public boolean checkAutoApprovalEligibility(UUID id) {
        // ⭐ PLACEHOLDER: Автомат батлах боломжтой эсэхийг шалгах логик нэмэх ⭐
        return false;
    }

    @Override
    public LoanApplicationDto processAutoApproval(UUID id) {
        // ⭐ PLACEHOLDER: Автомат батлах логик нэмэх ⭐
        return getLoanApplicationById(id);
    }

    public Map<String, Object> reviewLoanApplication(UUID id) {
        // ⭐ PLACEHOLDER: Зээлийн хүсэлтийг хянах логик нэмэх ⭐
        return new HashMap<>();
    }

    public Map<String, Object> validateDataIntegrity() {
        // ⭐ PLACEHOLDER: Өгөгдлийн бүрэн бүтэн байдлыг шалгах логик нэмэх ⭐
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> getLoanApplicationAuditHistory(UUID id) {
        // ⭐ PLACEHOLDER: Аудит түүхийг авах логик нэмэх ⭐
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getLoanApplicationActivityLog(UUID id) {
        // ⭐ PLACEHOLDER: Үйл ажиллагааны түүхийг авах логик нэмэх ⭐
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationDto> getPendingApplications() {
        LoanApplication.ApplicationStatus status = LoanApplication.ApplicationStatus.SUBMITTED;
        List<LoanApplication> applications = loanApplicationRepository.findByStatus(status,
                PageRequest.of(0, 1000)).getContent();
        return applications.stream()
                .map(LoanApplicationDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationDto> getApplicationsForReview() {
        LoanApplication.ApplicationStatus status = LoanApplication.ApplicationStatus.UNDER_REVIEW;
        List<LoanApplication> applications = loanApplicationRepository.findByStatus(status,
                PageRequest.of(0, 1000)).getContent();
        return applications.stream()
                .map(LoanApplicationDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ⭐ ЗАСВАРЛАСАН: Status transition validation logic засварласан ⭐
    private boolean isValidStatusTransition(LoanApplication.ApplicationStatus from, LoanApplication.ApplicationStatus to) {
        // Define valid transitions
        switch (from) {
            case DRAFT:
                return to == LoanApplication.ApplicationStatus.SUBMITTED || to == LoanApplication.ApplicationStatus.CANCELLED;
            case SUBMITTED:
                return to == LoanApplication.ApplicationStatus.UNDER_REVIEW || 
                       to == LoanApplication.ApplicationStatus.PENDING_DOCUMENTS ||
                       to == LoanApplication.ApplicationStatus.CANCELLED ||
                       to == LoanApplication.ApplicationStatus.APPROVED || // Шууд батлах боломжтой
                       to == LoanApplication.ApplicationStatus.REJECTED; // Шууд татгалзах боломжтой
            case UNDER_REVIEW:
                return to == LoanApplication.ApplicationStatus.APPROVED || 
                       to == LoanApplication.ApplicationStatus.REJECTED ||
                       to == LoanApplication.ApplicationStatus.PENDING_DOCUMENTS ||
                       to == LoanApplication.ApplicationStatus.CANCELLED;
            case PENDING_DOCUMENTS:
                return to == LoanApplication.ApplicationStatus.UNDER_REVIEW ||
                       to == LoanApplication.ApplicationStatus.CANCELLED ||
                       to == LoanApplication.ApplicationStatus.SUBMITTED; // Баримт ирсний дараа дахин илгээх
            case APPROVED:
                return to == LoanApplication.ApplicationStatus.DISBURSED ||
                       to == LoanApplication.ApplicationStatus.CANCELLED; // Батлагдсан ч цуцлах боломжтой
            case REJECTED:
            case CANCELLED:
            case DISBURSED:
                return false; // Terminal states - no further transitions
            case PENDING: // PENDING status-ийг нэмсэн
                return to == LoanApplication.ApplicationStatus.SUBMITTED ||
                       to == LoanApplication.ApplicationStatus.UNDER_REVIEW ||
                       to == LoanApplication.ApplicationStatus.CANCELLED;
            default:
                return false;
        }
    }

    // Helper methods
    private boolean canCustomerApplyForLoan(Customer customer) {
        return customer.isKycCompleted() && customer.getIsActive();
    }

    private boolean canBeEdited(LoanApplication loanApplication) {
        return loanApplication.getStatus() == LoanApplication.ApplicationStatus.DRAFT ||
               loanApplication.getStatus() == LoanApplication.ApplicationStatus.SUBMITTED ||
               loanApplication.getStatus() == LoanApplication.ApplicationStatus.PENDING_DOCUMENTS;
    }

    private boolean canBeDeleted(LoanApplication loanApplication) {
        return loanApplication.getStatus() == LoanApplication.ApplicationStatus.DRAFT ||
               loanApplication.getStatus() == LoanApplication.ApplicationStatus.CANCELLED;
    }

    private boolean canBeApproved(LoanApplication loanApplication) {
        return loanApplication.getStatus() == LoanApplication.ApplicationStatus.SUBMITTED ||
               loanApplication.getStatus() == LoanApplication.ApplicationStatus.UNDER_REVIEW ||
               loanApplication.getStatus() == LoanApplication.ApplicationStatus.PENDING_DOCUMENTS; // Баримт ирсний дараа батлах
    }

    private boolean canBeRejected(LoanApplication loanApplication) {
        return loanApplication.getStatus() != LoanApplication.ApplicationStatus.DRAFT &&
               loanApplication.getStatus() != LoanApplication.ApplicationStatus.APPROVED &&
               loanApplication.getStatus() != LoanApplication.ApplicationStatus.DISBURSED &&
               loanApplication.getStatus() != LoanApplication.ApplicationStatus.REJECTED &&
               loanApplication.getStatus() != LoanApplication.ApplicationStatus.CANCELLED;
    }

    private boolean isValidForSubmission(LoanApplication loanApplication) {
        return loanApplication.getCustomer() != null &&
               loanApplication.getLoanType() != null &&
               loanApplication.getRequestedAmount() != null &&
               loanApplication.getRequestedAmount().compareTo(BigDecimal.ZERO) > 0 &&
               loanApplication.getRequestedTermMonths() != null &&
               loanApplication.getRequestedTermMonths() > 0;
    }

    private String generateUniqueApplicationNumber() {
        // Жишээ: LN-YYYY-UUID-ийн эхний 8 тэмдэгт
        String year = String.valueOf(LocalDateTime.now().getYear());
        String uuidPart = UUID.randomUUID().toString().substring(0, 8);
        return "LN-" + year + "-" + uuidPart.toUpperCase();
    }

    private boolean isFinalStatus(LoanApplication loanApplication) {
        LoanApplication.ApplicationStatus status = loanApplication.getStatus();
        return status == LoanApplication.ApplicationStatus.APPROVED ||
               status == LoanApplication.ApplicationStatus.REJECTED ||
               status == LoanApplication.ApplicationStatus.CANCELLED ||
               status == LoanApplication.ApplicationStatus.DISBURSED;
    }

    /**
     * Зээлийн статусыг хүсэлтийн статус руу хөрвүүлэх
     */
    private LoanApplication.ApplicationStatus convertLoanStatusToApplicationStatus(LoanStatus loanStatus) {
        switch (loanStatus) {
            case DRAFT:
                return LoanApplication.ApplicationStatus.DRAFT;
            case SUBMITTED:
                return LoanApplication.ApplicationStatus.SUBMITTED;
            case UNDER_REVIEW:
                return LoanApplication.ApplicationStatus.UNDER_REVIEW;
            case APPROVED:
                return LoanApplication.ApplicationStatus.APPROVED;
            case REJECTED:
                return LoanApplication.ApplicationStatus.REJECTED;
            case CANCELLED:
                return LoanApplication.ApplicationStatus.CANCELLED;
            case DISBURSED:
                return LoanApplication.ApplicationStatus.DISBURSED;
            case PENDING_DOCUMENTS:
                return LoanApplication.ApplicationStatus.PENDING_DOCUMENTS;
            case PENDING:
                return LoanApplication.ApplicationStatus.PENDING;
            default:
                return LoanApplication.ApplicationStatus.DRAFT;
        }
    }

    /**
     * Хүсэлтийн статусыг Зээлийн статус руу хөрвүүлэх
     */
    private LoanStatus convertApplicationStatusToLoanStatus(LoanApplication.ApplicationStatus applicationStatus) {
        switch (applicationStatus) {
            case DRAFT:
                return LoanStatus.DRAFT;
            case SUBMITTED:
                return LoanStatus.SUBMITTED;
            case UNDER_REVIEW:
                return LoanStatus.UNDER_REVIEW;
            case APPROVED:
                return LoanStatus.APPROVED;
            case REJECTED:
                return LoanStatus.REJECTED;
            case CANCELLED:
                return LoanStatus.CANCELLED;
            case DISBURSED:
                return LoanStatus.DISBURSED;
            case PENDING_DOCUMENTS:
                return LoanStatus.PENDING_DOCUMENTS;
            case PENDING:
                return LoanStatus.PENDING;
            default:
                return LoanStatus.DRAFT;
        }
    }

    private boolean isValidLoanRequest(CreateLoanRequestDto createRequest) {
        return createRequest.getCustomerId() != null &&
               createRequest.getLoanType() != null &&
               createRequest.getRequestedAmount() != null &&
               createRequest.getRequestedAmount().compareTo(BigDecimal.ZERO) > 0 &&
               createRequest.getRequestedTermMonths() != null &&
               createRequest.getRequestedTermMonths() > 0;
    }

    private boolean isValidLoanRequest(LoanApplicationDto loanApplicationDto) {
        return loanApplicationDto.getCustomerId() != null &&
               loanApplicationDto.getLoanType() != null &&
               loanApplicationDto.getRequestedAmount() != null &&
               loanApplicationDto.getRequestedAmount().compareTo(BigDecimal.ZERO) > 0 &&
               loanApplicationDto.getRequestedTermMonths() != null &&
               loanApplicationDto.getRequestedTermMonths() > 0;
    }

    private boolean isWithinLoanTypeLimits(CreateLoanRequestDto createRequest) {
        // Жишээ нь, 1 тэрбум төгрөгөөс ихгүй байх
        return createRequest.getRequestedAmount().compareTo(new BigDecimal("1000000000")) <= 0;
    }

    private boolean isWithinLoanTypeLimits(LoanApplicationDto loanApplicationDto) {
        // Жишээ нь, 1 тэрбум төгрөгөөс ихгүй байх
        return loanApplicationDto.getRequestedAmount().compareTo(new BigDecimal("1000000000")) <= 0;
    }
}