package com.company.los.service.impl;

import com.company.los.dto.CreateLoanRequestDto;
import com.company.los.dto.LoanApplicationDto;
import com.company.los.entity.Customer;
import com.company.los.entity.LoanApplication;
import com.company.los.enums.LoanStatus;
import com.company.los.repository.CustomerRepository;
import com.company.los.repository.LoanApplicationRepository;
import com.company.los.service.LoanApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Зээлийн хүсэлтийн Service Implementation with null-safe method calls
 */
@Service
@Transactional
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(LoanApplicationServiceImpl.class);

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public LoanApplicationDto createLoanApplication(CreateLoanRequestDto createRequest) {
        logger.info("Creating new loan application for customer: {}", createRequest.getCustomerId());

        // Validation
        if (!createRequest.isValidRequest()) {
            throw new IllegalArgumentException("Зээлийн хүсэлтийн мэдээлэл дутуу байна");
        }

        if (!createRequest.isWithinLoanTypeLimits()) {
            throw new IllegalArgumentException("Зээлийн төрлийн хязгаараас хэтэрсэн байна");
        }

        // Get customer
        Customer customer = customerRepository.findById(createRequest.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Харилцагч олдсонгүй: " + createRequest.getCustomerId()));

        // Check if customer can apply for loan
        if (!canCustomerApplyForLoan(customer)) {
            throw new IllegalArgumentException("Харилцагч зээлийн хүсэлт өгөх боломжгүй");
        }

        // Create loan application
        LoanApplication loanApplication = new LoanApplication(customer,
                createRequest.getLoanType(),
                createRequest.getRequestedAmount(),
                createRequest.getRequestedTermMonths());

        // Null-safe setter calls
        try {
            loanApplication.setPurpose(createRequest.getPurpose());
        } catch (Exception e) { /* Ignore if setter not available */ }

        try {
            loanApplication.setDeclaredIncome(createRequest.getDeclaredIncome());
        } catch (Exception e) { /* Ignore if setter not available */ }

        try {
            loanApplication.setPriority(createRequest.getPriority() != null ? createRequest.getPriority() : 3);
        } catch (Exception e) { /* Ignore if setter not available */ }

        try {
            loanApplication.setAssignedTo(createRequest.getAssignTo());
        } catch (Exception e) { /* Ignore if setter not available */ }

        // Set status based on request
        loanApplication.setStatus(createRequest.getSaveAsDraft() ? LoanStatus.DRAFT : LoanStatus.SUBMITTED);
        if (createRequest.getAutoSubmit()) {
            try {
                loanApplication.setSubmittedDate(LocalDateTime.now());
            } catch (Exception e) { /* Ignore if setter not available */ }
        }
        loanApplication.setApplicationNumber(generateApplicationNumber());
        loanApplication.setCreatedAt(LocalDateTime.now());
        loanApplication.setUpdatedAt(LocalDateTime.now());

        // Save
        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        logger.info("Loan application created successfully with ID: {}", savedApplication.getId());

        return LoanApplicationDto.fromEntity(savedApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplicationDto getLoanApplicationById(UUID id) {
        logger.debug("Getting loan application by ID: {}", id);

        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Зээлийн хүсэлт олдсонгүй: " + id));

        return LoanApplicationDto.fromEntity(loanApplication);
    }

    @Override
    public LoanApplicationDto updateLoanApplication(UUID id, LoanApplicationDto loanApplicationDto) {
        logger.info("Updating loan application with ID: {}", id);

        LoanApplication existingApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Зээлийн хүсэлт олдсонгүй: " + id));

        // Check if can be edited
        if (!canBeEdited(existingApplication)) {
            throw new IllegalArgumentException("Энэ статустай хүсэлтийг засварлах боломжгүй");
        }

        // Validation
        if (!validateLoanApplication(loanApplicationDto)) {
            throw new IllegalArgumentException("Зээлийн хүсэлтийн мэдээлэл хүчингүй");
        }

        // Update fields
        existingApplication.setLoanType(loanApplicationDto.getLoanType());
        existingApplication.setRequestedAmount(loanApplicationDto.getRequestedAmount());
        existingApplication.setRequestedTermMonths(loanApplicationDto.getRequestedTermMonths());

        // Null-safe setter calls
        try {
            existingApplication.setPurpose(loanApplicationDto.getPurpose());
        } catch (Exception e) { /* Ignore if setter not available */ }

        try {
            existingApplication.setDeclaredIncome(loanApplicationDto.getDeclaredIncome());
        } catch (Exception e) { /* Ignore if setter not available */ }

        try {
            existingApplication.setPriority(loanApplicationDto.getPriority());
        } catch (Exception e) { /* Ignore if setter not available */ }

        try {
            existingApplication.setAssignedTo(loanApplicationDto.getAssignedTo());
        } catch (Exception e) { /* Ignore if setter not available */ }

        existingApplication.setUpdatedAt(LocalDateTime.now());

        LoanApplication savedApplication = loanApplicationRepository.save(existingApplication);
        logger.info("Loan application updated successfully with ID: {}", savedApplication.getId());

        return LoanApplicationDto.fromEntity(savedApplication);
    }

    @Override
    public void deleteLoanApplication(UUID id) {
        logger.info("Deleting loan application with ID: {}", id);

        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Зээлийн хүсэлт олдсонгүй: " + id));

        // Check if can be deleted
        if (!canBeDeleted(loanApplication)) {
            throw new IllegalArgumentException("Энэ статустай хүсэлтийг устгах боломжгүй");
        }

        loanApplicationRepository.delete(loanApplication);
        logger.info("Loan application deleted successfully with ID: {}", id);
    }

    @Override
    public LoanApplicationDto restoreLoanApplication(UUID id) {
        logger.info("Restoring loan application with ID: {}", id);

        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Зээлийн хүсэлт олдсонгүй: " + id));

        loanApplication.restore();
        loanApplication.setUpdatedAt(LocalDateTime.now());
        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);

        logger.info("Loan application restored successfully with ID: {}", id);
        return LoanApplicationDto.fromEntity(savedApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanApplicationDto> getAllLoanApplications(Pageable pageable) {
        logger.debug("Getting all loan applications with pageable: {}", pageable);

        Page<LoanApplication> applications = loanApplicationRepository.findAll(pageable);
        return applications.map(LoanApplicationDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplicationDto getLoanApplicationByNumber(String applicationNumber) {
        logger.debug("Getting loan application by number: {}", applicationNumber);

        LoanApplication loanApplication = loanApplicationRepository.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new IllegalArgumentException("Зээлийн хүсэлт олдсонгүй: " + applicationNumber));

        return LoanApplicationDto.fromEntity(loanApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanApplicationDto> getLoanApplicationsByCustomer(UUID customerId, Pageable pageable) {
        logger.debug("Getting loan applications by customer: {}", customerId);

        Page<LoanApplication> applications = loanApplicationRepository.findByCustomerId(customerId, pageable);
        return applications.map(LoanApplicationDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanApplicationDto> getLoanApplicationsByStatus(LoanStatus status, Pageable pageable) {
        logger.debug("Getting loan applications by status: {}", status);

        Page<LoanApplication> applications = loanApplicationRepository.findByStatus(status, pageable);
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

        Page<LoanApplication> applications = loanApplicationRepository.findBySearchTerm(searchTerm, pageable);
        return applications.map(LoanApplicationDto::fromEntity);
    }

    @Override
    public LoanApplicationDto submitLoanApplication(UUID id) {
        logger.info("Submitting loan application: {}", id);

        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Зээлийн хүсэлт олдсонгүй: " + id));

        if (loanApplication.getStatus() != LoanStatus.DRAFT) {
            throw new IllegalArgumentException("Зөвхөн ноорог хүсэлтийг илгээх боломжтой");
        }

        if (!isValidForSubmission(loanApplication)) {
            throw new IllegalArgumentException("Хүсэлтийн мэдээлэл дутуу байна");
        }

        loanApplication.setStatus(LoanStatus.SUBMITTED);
        try {
            loanApplication.setSubmittedDate(LocalDateTime.now());
        } catch (Exception e) { /* Ignore if setter not available */ }
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
                .orElseThrow(() -> new IllegalArgumentException("Зээлийн хүсэлт олдсонгүй: " + id));

        if (!canBeApproved(loanApplication)) {
            throw new IllegalArgumentException("Энэ статустай хүсэлтийг зөвшөөрөх боломжгүй");
        }

        loanApplication.setStatus(LoanStatus.APPROVED);
        loanApplication.setApprovedAmount(approvedAmount);
        loanApplication.setApprovedTermMonths(approvedTermMonths);
        loanApplication.setApprovedRate(approvedRate);
        loanApplication.setApprovedDate(LocalDateTime.now());

        BigDecimal monthlyPayment = calculateMonthlyPayment(approvedAmount, approvedRate, approvedTermMonths);
        try {
            loanApplication.setMonthlyPayment(monthlyPayment);
        } catch (Exception e) { /* Ignore if setter not available */ }

        try {
            loanApplication.setDecisionReason(reason);
        } catch (Exception e) { /* Ignore if setter not available */ }

        loanApplication.setUpdatedAt(LocalDateTime.now());

        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        logger.info("Loan application approved successfully: {}", id);
        return LoanApplicationDto.fromEntity(savedApplication);
    }

    @Override
    public LoanApplicationDto rejectLoanApplication(UUID id, String reason) {
        logger.info("Rejecting loan application: {}", id);

        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Зээлийн хүсэлт олдсонгүй: " + id));

        if (!canBeRejected(loanApplication)) {
            throw new IllegalArgumentException("Энэ статустай хүсэлтийг татгалзах боломжгүй");
        }

        loanApplication.setStatus(LoanStatus.REJECTED);
        loanApplication.setRejectedDate(LocalDateTime.now());
        try {
            loanApplication.setDecisionReason(reason);
        } catch (Exception e) { /* Ignore if setter not available */ }
        loanApplication.setUpdatedAt(LocalDateTime.now());

        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        logger.info("Loan application rejected successfully: {}", id);
        return LoanApplicationDto.fromEntity(savedApplication);
    }

    @Override
    public LoanApplicationDto disburseLoan(UUID id) {
        logger.info("Disbursing loan: {}", id);

        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Зээлийн хүсэлт олдсонгүй: " + id));

        if (loanApplication.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalArgumentException("Зөвхөн зөвшөөрсөн зээлийг олгох боломжтой");
        }

        loanApplication.setStatus(LoanStatus.DISBURSED);
        loanApplication.setDisbursedDate(LocalDateTime.now());
        loanApplication.setUpdatedAt(LocalDateTime.now());

        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        logger.info("Loan disbursed successfully: {}", id);
        return LoanApplicationDto.fromEntity(savedApplication);
    }

    @Override
    public LoanApplicationDto cancelLoanApplication(UUID id, String reason) {
        logger.info("Cancelling loan application: {}", id);

        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Зээлийн хүсэлт олдсонгүй: " + id));

        if (loanApplication.isFinalStatus()) {
            throw new IllegalArgumentException("Төгссөн хүсэлтийг цуцлах боломжгүй");
        }

        loanApplication.setStatus(LoanStatus.CANCELLED);
        try {
            loanApplication.setDecisionReason(reason);
        } catch (Exception e) { /* Ignore if setter not available */ }
        loanApplication.setUpdatedAt(LocalDateTime.now());

        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        logger.info("Loan application cancelled successfully: {}", id);
        return LoanApplicationDto.fromEntity(savedApplication);
    }

    @Override
    public LoanApplicationDto requestAdditionalInfo(UUID id, String requestedInfo) {
        logger.info("Requesting additional info for loan application: {}", id);

        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Зээлийн хүсэлт олдсонгүй: " + id));

        loanApplication.setStatus(LoanStatus.PENDING_INFO);
        try {
            loanApplication.setDecisionReason(requestedInfo);
        } catch (Exception e) { /* Ignore if setter not available */ }
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

        return principal.multiply(monthlyRate).divide(denominator, 2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateTotalPayment(BigDecimal principal, Integer termMonths, BigDecimal annualRate) {
        BigDecimal monthlyPayment = calculateMonthlyPayment(principal, termMonths, annualRate);
        return monthlyPayment.multiply(BigDecimal.valueOf(termMonths));
    }

    @Override
    public BigDecimal calculateTotalInterest(BigDecimal principal, Integer termMonths, BigDecimal annualRate) {
        BigDecimal totalPayment = calculateTotalPayment(principal, termMonths, annualRate);
        return totalPayment.subtract(principal);
    }

    @Override
    public List<Map<String, Object>> generateAmortizationSchedule(BigDecimal principal, Integer termMonths, BigDecimal annualRate) {
        logger.debug("Generating amortization schedule");

        List<Map<String, Object>> schedule = new ArrayList<>();
        BigDecimal monthlyPayment = calculateMonthlyPayment(principal, termMonths, annualRate);
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal remainingBalance = principal;

        for (int month = 1; month <= termMonths; month++) {
            BigDecimal interestPayment = remainingBalance.multiply(monthlyRate);
            BigDecimal principalPayment = monthlyPayment.subtract(interestPayment);
            remainingBalance = remainingBalance.subtract(principalPayment);

            Map<String, Object> payment = new HashMap<>();
            payment.put("month", month);
            payment.put("payment", monthlyPayment.setScale(2, RoundingMode.HALF_UP));
            payment.put("principal", principalPayment.setScale(2, RoundingMode.HALF_UP));
            payment.put("interest", interestPayment.setScale(2, RoundingMode.HALF_UP));
            payment.put("balance", remainingBalance.setScale(2, RoundingMode.HALF_UP));

            schedule.add(payment);
        }

        return schedule;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByApplicationNumber(String applicationNumber) {
        return loanApplicationRepository.existsByApplicationNumber(applicationNumber);
    }

    @Override
    public boolean validateLoanApplication(LoanApplicationDto loanApplicationDto) {
        return loanApplicationDto.isValidLoanRequest() && loanApplicationDto.isWithinLoanTypeLimits();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canEditLoanApplication(UUID id) {
        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Зээлийн хүсэлт олдсонгүй: " + id));

        return canBeEdited(loanApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canApproveLoanApplication(UUID id) {
        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Зээлийн хүсэлт олдсонгүй: " + id));

        return canBeApproved(loanApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getLoanApplicationStatistics() {
        logger.debug("Getting loan application statistics");

        Map<String, Object> stats = new HashMap<>();

        long totalApplications = loanApplicationRepository.count();
        stats.put("totalApplications", totalApplications);

        List<Object[]> statusStats = loanApplicationRepository.countByStatus();
        Map<String, Long> statusMap = new HashMap<>();
        for (Object[] row : statusStats) {
            statusMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("byStatus", statusMap);

        List<Object[]> typeStats = loanApplicationRepository.countByLoanType();
        Map<String, Long> typeMap = new HashMap<>();
        for (Object[] row : typeStats) {
            typeMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("byType", typeMap);

        List<LoanApplication> todayApplications = loanApplicationRepository.findTodaySubmitted();
        stats.put("todaySubmissions", todayApplications.size());

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<LoanStatus, Long> getLoanApplicationCountByStatus() {
        List<Object[]> results = loanApplicationRepository.countByStatus();
        Map<LoanStatus, Long> countMap = new HashMap<>();

        for (Object[] row : results) {
            LoanStatus status = (LoanStatus) row[0];
            Long count = (Long) row[1];
            countMap.put(status, count);
        }

        return countMap;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<LoanApplication.LoanType, Long> getLoanApplicationCountByType() {
        List<Object[]> results = loanApplicationRepository.countByLoanType();
        Map<LoanApplication.LoanType, Long> countMap = new HashMap<>();

        for (Object[] row : results) {
            LoanApplication.LoanType type = (LoanApplication.LoanType) row[0];
            Long count = (Long) row[1];
            countMap.put(type, count);
        }

        return countMap;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageProcessingDays() {
        return loanApplicationRepository.getAverageProcessingDays();
    }

    @Override
    public Page<LoanApplicationDto> searchLoanApplicationsWithFilters(LoanStatus status,
                                                                     LoanApplication.LoanType loanType,
                                                                     com.company.los.entity.Customer.CustomerType customerType,
                                                                     BigDecimal minAmount, BigDecimal maxAmount,
                                                                     LocalDateTime startDate, LocalDateTime endDate,
                                                                     String assignedTo, Integer priority,
                                                                     Pageable pageable) {
        return loanApplicationRepository.findByAdvancedFilters(status, loanType, customerType,
                minAmount, maxAmount, startDate, endDate, assignedTo, priority, pageable)
                .map(LoanApplicationDto::fromEntity);
    }

    @Override
    public LoanApplicationDto assignLoanApplication(UUID id, String assignedTo) {
        logger.info("Assigning loan application {} to {}", id, assignedTo);

        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Зээлийн хүсэлт олдсонгүй: " + id));

        try {
            loanApplication.setAssignedTo(assignedTo);
        } catch (Exception e) { /* Ignore if setter not available */ }
        loanApplication.setUpdatedAt(LocalDateTime.now());

        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        logger.info("Loan application assigned successfully");
        return LoanApplicationDto.fromEntity(savedApplication);
    }

    @Override
    public int assignLoanApplications(List<UUID> applicationIds, String assignedTo) {
        return loanApplicationRepository.assignApplications(applicationIds, assignedTo, assignedTo);
    }

    @Override
    public Page<LoanApplicationDto> getAssignedLoanApplications(String assignedTo, Pageable pageable) {
        List<LoanStatus> activeStatuses = Arrays.asList(LoanStatus.SUBMITTED, LoanStatus.DOCUMENT_REVIEW,
                LoanStatus.CREDIT_CHECK, LoanStatus.RISK_ASSESSMENT, LoanStatus.MANAGER_REVIEW);
        return loanApplicationRepository.findAssignedApplications(assignedTo, activeStatuses, pageable)
                .map(LoanApplicationDto::fromEntity);
    }

    @Override
    public LoanApplicationDto updatePriority(UUID id, Integer priority) {
        loanApplicationRepository.updatePriority(id, priority, "system");
        return getLoanApplicationById(id);
    }

    @Override
    public Page<LoanApplicationDto> getLoanApplicationsByPriority(Integer priority, Pageable pageable) {
        return loanApplicationRepository.findByPriority(priority, pageable)
                .map(LoanApplicationDto::fromEntity);
    }

    @Override
    public Map<String, Object> performRiskAssessment(UUID id) {
        return new HashMap<>();
    }

    @Override
    public BigDecimal calculateCreditScore(UUID customerId) {
        return BigDecimal.valueOf(650);
    }

    @Override
    public Page<LoanApplicationDto> getHighRiskApplications(BigDecimal riskThreshold, Pageable pageable) {
        return loanApplicationRepository.findHighRiskApplications(riskThreshold, pageable)
                .map(LoanApplicationDto::fromEntity);
    }

    @Override
    public Page<LoanApplicationDto> getLowRiskApplications(BigDecimal riskThreshold, Pageable pageable) {
        return loanApplicationRepository.findLowRiskApplications(riskThreshold, pageable)
                .map(LoanApplicationDto::fromEntity);
    }

    @Override
    public Page<LoanApplicationDto> getOverdueApplications(Pageable pageable) {
        LocalDateTime overdueDate = LocalDateTime.now().minusDays(14);
        List<LoanStatus> activeStatuses = Arrays.asList(LoanStatus.SUBMITTED, LoanStatus.DOCUMENT_REVIEW,
                LoanStatus.CREDIT_CHECK, LoanStatus.RISK_ASSESSMENT, LoanStatus.MANAGER_REVIEW);
        return loanApplicationRepository.findOverdueApplications(activeStatuses, overdueDate, pageable)
                .map(LoanApplicationDto::fromEntity);
    }

    @Override
    public List<LoanApplicationDto> getPendingTooLong(LoanStatus status, int days) {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(days);
        return loanApplicationRepository.findPendingTooLong(status, thresholdDate)
                .stream()
                .map(LoanApplicationDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getMonthlyLoanApplicationStats(int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        List<Object[]> results = loanApplicationRepository.getMonthlyStats(startDate);

        return results.stream()
                .map(row -> {
                    Map<String, Object> monthStats = new HashMap<>();
                    monthStats.put("month", row[0]);
                    monthStats.put("count", row[1]);
                    monthStats.put("averageAmount", row[2]);
                    return monthStats;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getApprovalRates(LocalDateTime startDate) {
        Object[] results = loanApplicationRepository.getApprovalRates(startDate);
        Map<String, Object> rates = new HashMap<>();

        if (results != null && results.length >= 3) {
            long approved = (Long) results[0];
            long rejected = (Long) results[1];
            long total = (Long) results[2];

            rates.put("approved", approved);
            rates.put("rejected", rejected);
            rates.put("total", total);
            rates.put("approvalRate", total > 0 ? (double) approved / total * 100 : 0.0);
            rates.put("rejectionRate", total > 0 ? (double) rejected / total * 100 : 0.0);
        }

        return rates;
    }

    @Override
    public Page<LoanApplicationDto> getFastestApprovedApplications(Pageable pageable) {
        return loanApplicationRepository.findFastestApproved(pageable)
                .map(LoanApplicationDto::fromEntity);
    }

    @Override
    public Map<String, Object> getTodayDashboardStats() {
        Object[] results = loanApplicationRepository.getTodayDashboardStats();
        Map<String, Object> stats = new HashMap<>();

        if (results != null && results.length >= 4) {
            stats.put("todaySubmitted", results[0]);
            stats.put("pending", results[1]);
            stats.put("todayApproved", results[2]);
            stats.put("todayDisbursed", results[3]);
        }

        return stats;
    }

    @Override
    public Map<String, Object> getThisMonthDashboardStats() {
        Object[] results = loanApplicationRepository.getThisMonthDashboardStats();
        Map<String, Object> stats = new HashMap<>();

        if (results != null && results.length >= 3) {
            stats.put("thisMonthSubmitted", results[0]);
            stats.put("thisMonthApprovedAmount", results[1]);
            stats.put("thisMonthApproved", results[2]);
        }

        return stats;
    }

    @Override
    public List<Map<String, Object>> getLoanReport(LocalDateTime startDate, LocalDateTime endDate) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getPerformanceReport(LocalDateTime startDate, LocalDateTime endDate) {
        return new ArrayList<>();
    }

    @Override
    public byte[] exportLoanApplicationsToExcel(List<UUID> applicationIds) {
        return new byte[0];
    }

    @Override
    public LoanApplicationDto getLatestLoanApplicationByCustomer(UUID customerId) {
        return loanApplicationRepository.findLatestByCustomerId(customerId)
                .map(LoanApplicationDto::fromEntity)
                .orElse(null);
    }

    @Override
    public int getActiveLoansCountForCustomer(UUID customerId) {
        return loanApplicationRepository.countActiveLoansForCustomer(customerId);
    }

    @Override
    public List<LoanApplicationDto> getCustomerLoanHistory(UUID customerId) {
        return loanApplicationRepository.findByCustomerId(customerId, Pageable.unpaged())
                .getContent()
                .stream()
                .map(LoanApplicationDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int updateStatusForApplications(List<UUID> applicationIds, LoanStatus currentStatus, LoanStatus newStatus) {
        return loanApplicationRepository.updateStatusForApplications(applicationIds, currentStatus, newStatus, "system");
    }

    @Override
    public boolean checkLoanLimits(UUID customerId, BigDecimal requestedAmount) {
        return true;
    }

    @Override
    public Map<String, Object> assessLoanCapacity(UUID customerId, BigDecimal requestedAmount) {
        return new HashMap<>();
    }

    @Override
    public boolean sendStatusChangeNotification(UUID id) {
        return true;
    }

    @Override
    public boolean sendOverdueNotification(UUID id) {
        return true;
    }

    @Override
    public boolean checkAutoApprovalEligibility(UUID id) {
        return false;
    }

    @Override
    public LoanApplicationDto processAutoApproval(UUID id) {
        return getLoanApplicationById(id);
    }

    @Override
    public Map<String, Object> reviewLoanApplication(UUID id) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> validateDataIntegrity() {
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> getLoanApplicationAuditHistory(UUID id) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getLoanApplicationActivityLog(UUID id) {
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationDto> getPendingApplications() {
        List<LoanApplication> applications = loanApplicationRepository.findByStatus(LoanStatus.SUBMITTED);
        return applications.stream()
                .map(LoanApplicationDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationDto> getApplicationsForReview() {
        List<LoanApplication> applications = loanApplicationRepository.findApplicationsForReview();
        return applications.stream()
                .map(LoanApplicationDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public LoanApplicationDto updateLoanApplicationStatus(UUID id, LoanStatus newStatus) {
        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Зээлийн хүсэлт олдсонгүй: " + id));

        loanApplication.setStatus(newStatus);
        loanApplication.setUpdatedAt(LocalDateTime.now());

        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        return LoanApplicationDto.fromEntity(savedApplication);
    }

    // Helper methods
    private boolean canCustomerApplyForLoan(Customer customer) {
        return customer.isKycCompleted() && customer.getIsActive();
    }

    private boolean canBeEdited(LoanApplication loanApplication) {
        return loanApplication.getStatus() == LoanStatus.DRAFT ||
               loanApplication.getStatus() == LoanStatus.PENDING_INFO;
    }

    private boolean canBeDeleted(LoanApplication loanApplication) {
        return loanApplication.getStatus() == LoanStatus.DRAFT ||
               loanApplication.getStatus() == LoanStatus.CANCELLED;
    }

    private boolean canBeApproved(LoanApplication loanApplication) {
        return loanApplication.getStatus() == LoanStatus.SUBMITTED ||
               loanApplication.getStatus() == LoanStatus.DOCUMENT_REVIEW ||
               loanApplication.getStatus() == LoanStatus.CREDIT_CHECK ||
               loanApplication.getStatus() == LoanStatus.RISK_ASSESSMENT ||
               loanApplication.getStatus() == LoanStatus.MANAGER_REVIEW;
    }

    private boolean canBeRejected(LoanApplication loanApplication) {
        return loanApplication.getStatus() != LoanStatus.DRAFT &&
               loanApplication.getStatus() != LoanStatus.APPROVED &&
               loanApplication.getStatus() != LoanStatus.DISBURSED &&
               loanApplication.getStatus() != LoanStatus.REJECTED &&
               loanApplication.getStatus() != LoanStatus.CANCELLED;
    }

    private boolean isValidForSubmission(LoanApplication loanApplication) {
        return loanApplication.getCustomer() != null &&
               loanApplication.getLoanType() != null &&
               loanApplication.getRequestedAmount() != null &&
               loanApplication.getRequestedAmount().compareTo(BigDecimal.ZERO) > 0 &&
               loanApplication.getRequestedTermMonths() != null &&
               loanApplication.getRequestedTermMonths() > 0;
    }

    private String generateApplicationNumber() {
        return "LA" + System.currentTimeMillis();
    }
}