package com.company.los.service.impl;

import com.company.los.dto.CustomerDto;
import com.company.los.entity.Customer;
import com.company.los.repository.CustomerRepository;
import com.company.los.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Харилцагчийн Service Implementation
 * Customer Service Implementation
 */
@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    @Autowired
    private CustomerRepository customerRepository;

    // CRUD операциуд
    @Override
    public CustomerDto createCustomer(CustomerDto customerDto) {
        logger.info("Creating new customer with register number: {}", customerDto.getRegisterNumber());
        
        // Validation
        validateCustomerData(customerDto);
        
        // Check for duplicates
        if (existsByRegisterNumber(customerDto.getRegisterNumber())) {
            throw new IllegalArgumentException("Регистрийн дугаар аль хэдийн байна: " + customerDto.getRegisterNumber());
        }
        
        if (customerDto.getPhone() != null && existsByPhone(customerDto.getPhone())) {
            throw new IllegalArgumentException("Утасны дугаар аль хэдийн байна: " + customerDto.getPhone());
        }
        
        if (customerDto.getEmail() != null && existsByEmail(customerDto.getEmail())) {
            throw new IllegalArgumentException("И-мэйл аль хэдийн байна: " + customerDto.getEmail());
        }
        
        // Convert to entity
        Customer customer = customerDto.toEntity();
        customer.setKycStatus(Customer.KycStatus.PENDING);
        
        // Save
        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Customer created successfully with ID: {}", savedCustomer.getId());
        
        return CustomerDto.fromEntity(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto getCustomerById(UUID id) {
        logger.debug("Getting customer by ID: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Харилцагч олдсонгүй: " + id));
        
        return CustomerDto.fromEntity(customer);
    }

    @Override
    public CustomerDto updateCustomer(UUID id, CustomerDto customerDto) {
        logger.info("Updating customer with ID: {}", id);
        
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Харилцагч олдсонгүй: " + id));
        
        // Validation
        validateCustomerData(customerDto);
        
        // Check duplicates (excluding current customer)
        if (!existingCustomer.getRegisterNumber().equals(customerDto.getRegisterNumber()) &&
            existsByRegisterNumber(customerDto.getRegisterNumber())) {
            throw new IllegalArgumentException("Регистрийн дугаар аль хэдийн байна: " + customerDto.getRegisterNumber());
        }
        
        // Update fields
        updateCustomerFields(existingCustomer, customerDto);
        
        Customer savedCustomer = customerRepository.save(existingCustomer);
        logger.info("Customer updated successfully with ID: {}", savedCustomer.getId());
        
        return CustomerDto.fromEntity(savedCustomer);
    }

    @Override
    public void deleteCustomer(UUID id) {
        logger.info("Deleting customer with ID: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Харилцагч олдсонгүй: " + id));
        
        customer.markAsDeleted();
        customerRepository.save(customer);
        
        logger.info("Customer deleted successfully with ID: {}", id);
    }

    @Override
    public CustomerDto restoreCustomer(UUID id) {
        logger.info("Restoring customer with ID: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Харилцагч олдсонгүй: " + id));
        
        customer.restore();
        Customer savedCustomer = customerRepository.save(customer);
        
        logger.info("Customer restored successfully with ID: {}", id);
        return CustomerDto.fromEntity(savedCustomer);
    }

    // Хайлт операциуд
    @Override
    @Transactional(readOnly = true)
    public Page<CustomerDto> getAllCustomers(Pageable pageable) {
        logger.debug("Getting all customers with pageable: {}", pageable);
        
        Page<Customer> customers = customerRepository.findAll(pageable);
        return customers.map(CustomerDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto getCustomerByRegisterNumber(String registerNumber) {
        logger.debug("Getting customer by register number: {}", registerNumber);
        
        Customer customer = customerRepository.findByRegisterNumber(registerNumber)
                .orElseThrow(() -> new IllegalArgumentException("Харилцагч олдсонгүй: " + registerNumber));
        
        return CustomerDto.fromEntity(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto getCustomerByPhone(String phone) {
        logger.debug("Getting customer by phone: {}", phone);
        
        Customer customer = customerRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("Харилцагч олдсонгүй: " + phone));
        
        return CustomerDto.fromEntity(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto getCustomerByEmail(String email) {
        logger.debug("Getting customer by email: {}", email);
        
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Харилцагч олдсонгүй: " + email));
        
        return CustomerDto.fromEntity(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerDto> searchCustomers(String searchTerm, Pageable pageable) {
        logger.debug("Searching customers with term: {}", searchTerm);
        
        Page<Customer> customers = customerRepository.findBySearchTerm(searchTerm, pageable);
        return customers.map(CustomerDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDto> quickSearchCustomers(String quickSearch) {
        logger.debug("Quick searching customers with term: {}", quickSearch);
        
        List<Customer> customers = customerRepository.quickSearch(quickSearch);
        return customers.stream()
                .map(CustomerDto::createSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerDto> getCustomersByType(Customer.CustomerType customerType, Pageable pageable) {
        logger.debug("Getting customers by type: {}", customerType);
        
        Page<Customer> customers = customerRepository.findByCustomerType(customerType, pageable);
        return customers.map(CustomerDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerDto> getCustomersByKycStatus(Customer.KycStatus kycStatus, Pageable pageable) {
        logger.debug("Getting customers by KYC status: {}", kycStatus);
        
        Page<Customer> customers = customerRepository.findByKycStatus(kycStatus, pageable);
        return customers.map(CustomerDto::fromEntity);
    }

    // Дэвшилтэт хайлт
    @Override
    @Transactional(readOnly = true)
    public Page<CustomerDto> searchCustomersWithFilters(Customer.CustomerType customerType,
                                                       Customer.KycStatus kycStatus,
                                                       String city, String province,
                                                       BigDecimal minIncome, BigDecimal maxIncome,
                                                       LocalDateTime startDate, LocalDateTime endDate,
                                                       Pageable pageable) {
        logger.debug("Searching customers with advanced filters");
        
        Page<Customer> customers = customerRepository.findByAdvancedFilters(
                customerType, kycStatus, city, province, minIncome, maxIncome,
                startDate, endDate, pageable);
        
        return customers.map(CustomerDto::fromEntity);
    }

    // KYC удирдлага
    @Override
    public CustomerDto startKycProcess(UUID customerId) {
        logger.info("Starting KYC process for customer: {}", customerId);
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Харилцагч олдсонгүй: " + customerId));
        
        customer.setKycStatus(Customer.KycStatus.IN_PROGRESS);
        Customer savedCustomer = customerRepository.save(customer);
        
        logger.info("KYC process started for customer: {}", customerId);
        return CustomerDto.fromEntity(savedCustomer);
    }

    @Override
    public CustomerDto completeKyc(UUID customerId, String completedBy) {
        logger.info("Completing KYC for customer: {} by: {}", customerId, completedBy);
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Харилцагч олдсонгүй: " + customerId));
        
        customer.completeKyc();
        customer.setUpdatedBy(completedBy);
        Customer savedCustomer = customerRepository.save(customer);
        
        logger.info("KYC completed for customer: {}", customerId);
        return CustomerDto.fromEntity(savedCustomer);
    }

    @Override
    public CustomerDto retryKyc(UUID customerId, String reason) {
        logger.info("Retrying KYC for customer: {} with reason: {}", customerId, reason);
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Харилцагч олдсонгүй: " + customerId));
        
        customer.setKycStatus(Customer.KycStatus.PENDING);
        customer.setKycCompletedAt(null);
        Customer savedCustomer = customerRepository.save(customer);
        
        logger.info("KYC retry initiated for customer: {}", customerId);
        return CustomerDto.fromEntity(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerDto> getIncompleteKycCustomers(Pageable pageable) {
        logger.debug("Getting customers with incomplete KYC");
        
        Page<Customer> customers = customerRepository.findIncompleteKyc(pageable);
        return customers.map(CustomerDto::fromEntity);
    }

    // Дупликат шалгалт
    @Override
    @Transactional(readOnly = true)
    public List<CustomerDto> findDuplicateCustomers(CustomerDto customerDto) {
        logger.debug("Finding duplicate customers for: {}", customerDto.getRegisterNumber());
        
        List<Customer> duplicates = new ArrayList<>();
        
        // Check by register number
        customerRepository.findByRegisterNumber(customerDto.getRegisterNumber())
                .ifPresent(duplicates::add);
        
        // Check by phone
        if (customerDto.getPhone() != null) {
            customerRepository.findByPhone(customerDto.getPhone())
                    .ifPresent(customer -> {
                        if (!duplicates.contains(customer)) {
                            duplicates.add(customer);
                        }
                    });
        }
        
        // Check by email
        if (customerDto.getEmail() != null) {
            customerRepository.findByEmail(customerDto.getEmail())
                    .ifPresent(customer -> {
                        if (!duplicates.contains(customer)) {
                            duplicates.add(customer);
                        }
                    });
        }
        
        return duplicates.stream()
                .map(CustomerDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDto> findSimilarCustomers(String firstName, String lastName, 
                                                 java.time.LocalDate birthDate, UUID excludeId) {
        logger.debug("Finding similar customers with name: {} {}", firstName, lastName);
        
        List<Customer> similarCustomers = customerRepository.findSimilarCustomers(
                firstName, lastName, birthDate, excludeId);
        
        return similarCustomers.stream()
                .map(CustomerDto::fromEntity)
                .collect(Collectors.toList());
    }

    // Validation
    @Override
    @Transactional(readOnly = true)
    public boolean existsByRegisterNumber(String registerNumber) {
        return customerRepository.existsByRegisterNumber(registerNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPhone(String phone) {
        return customerRepository.existsByPhone(phone);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    @Override
    public boolean validateCustomerData(CustomerDto customerDto) {
        logger.debug("Validating customer data for: {}", customerDto.getRegisterNumber());
        
        if (customerDto.getCustomerType() == Customer.CustomerType.INDIVIDUAL) {
            return customerDto.isValidForIndividual();
        } else {
            return customerDto.isValidForBusiness();
        }
    }

    // Статистик
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getCustomerStatistics() {
        logger.debug("Getting customer statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Total customers
        long totalCustomers = customerRepository.count();
        stats.put("totalCustomers", totalCustomers);
        
        // By type
        List<Object[]> typeStats = customerRepository.countByCustomerType();
        Map<String, Long> typeMap = new HashMap<>();
        for (Object[] row : typeStats) {
            typeMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("byType", typeMap);
        
        // By KYC status
        List<Object[]> kycStats = customerRepository.countByKycStatus();
        Map<String, Long> kycMap = new HashMap<>();
        for (Object[] row : kycStats) {
            kycMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("byKycStatus", kycMap);
        
        // Today's registrations
        List<Customer> todayCustomers = customerRepository.findTodayRegistered();
        stats.put("todayRegistrations", todayCustomers.size());
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Customer.CustomerType, Long> getCustomerCountByType() {
        List<Object[]> results = customerRepository.countByCustomerType();
        Map<Customer.CustomerType, Long> countMap = new HashMap<>();
        
        for (Object[] row : results) {
            Customer.CustomerType type = (Customer.CustomerType) row[0];
            Long count = (Long) row[1];
            countMap.put(type, count);
        }
        
        return countMap;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Customer.KycStatus, Long> getCustomerCountByKycStatus() {
        List<Object[]> results = customerRepository.countByKycStatus();
        Map<Customer.KycStatus, Long> countMap = new HashMap<>();
        
        for (Object[] row : results) {
            Customer.KycStatus status = (Customer.KycStatus) row[0];
            Long count = (Long) row[1];
            countMap.put(status, count);
        }
        
        return countMap;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMonthlyCustomerStats(int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        List<Object[]> results = customerRepository.getMonthlyCustomerStats(startDate);
        
        return results.stream()
                .map(row -> {
                    Map<String, Object> monthStats = new HashMap<>();
                    monthStats.put("month", row[0]);
                    monthStats.put("count", row[1]);
                    return monthStats;
                })
                .collect(Collectors.toList());
    }

    // Helper methods
    private void updateCustomerFields(Customer existingCustomer, CustomerDto customerDto) {
        existingCustomer.setCustomerType(customerDto.getCustomerType());
        existingCustomer.setFirstName(customerDto.getFirstName());
        existingCustomer.setLastName(customerDto.getLastName());
        existingCustomer.setRegisterNumber(customerDto.getRegisterNumber());
        existingCustomer.setBirthDate(customerDto.getBirthDate());
        existingCustomer.setGender(customerDto.getGender());
        existingCustomer.setPhone(customerDto.getPhone());
        existingCustomer.setEmail(customerDto.getEmail());
        existingCustomer.setAddress(customerDto.getAddress());
        existingCustomer.setCity(customerDto.getCity());
        existingCustomer.setProvince(customerDto.getProvince());
        existingCustomer.setPostalCode(customerDto.getPostalCode());
        existingCustomer.setEmployerName(customerDto.getEmployerName());
        existingCustomer.setJobTitle(customerDto.getJobTitle());
        existingCustomer.setWorkExperienceYears(customerDto.getWorkExperienceYears());
        existingCustomer.setMonthlyIncome(customerDto.getMonthlyIncome());
        existingCustomer.setCompanyName(customerDto.getCompanyName());
        existingCustomer.setBusinessRegistrationNumber(customerDto.getBusinessRegistrationNumber());
        existingCustomer.setTaxNumber(customerDto.getTaxNumber());
        existingCustomer.setBusinessType(customerDto.getBusinessType());
        existingCustomer.setAnnualRevenue(customerDto.getAnnualRevenue());
    }

    // Placeholder implementations for interface completeness
    @Override
    public Page<CustomerDto> getCustomersWithLoanApplications(Pageable pageable) {
        return customerRepository.findCustomersWithLoanApplications(pageable)
                .map(CustomerDto::fromEntity);
    }

    @Override
    public Page<CustomerDto> getCustomersWithActiveLoans(Pageable pageable) {
        return customerRepository.findCustomersWithActiveLoans(pageable)
                .map(CustomerDto::fromEntity);
    }

    @Override
    public Page<CustomerDto> getCustomersWithoutLoanApplications(Pageable pageable) {
        return customerRepository.findCustomersWithoutLoanApplications(pageable)
                .map(CustomerDto::fromEntity);
    }

    @Override
    public Map<String, Object> getCustomerLoanHistory(UUID customerId) {
        // Implementation would depend on LoanApplication relationships
        return new HashMap<>();
    }

    @Override
    public Map<String, Long> getCustomerCountByCity() {
        List<Object[]> results = customerRepository.getCustomerCountByCity();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]));
    }

    @Override
    public Map<String, Long> getCustomerCountByProvince() {
        List<Object[]> results = customerRepository.getCustomerCountByProvince();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]));
    }

    @Override
    public Map<String, Object> getTodayCustomerStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Customer> todayCustomers = customerRepository.findTodayRegistered();
        stats.put("todayRegistrations", todayCustomers.size());
        return stats;
    }

    @Override
    public Page<CustomerDto> getTopCustomersByLoanAmount(Pageable pageable) {
        // This would need JOIN with LoanApplication
        return Page.empty();
    }

    @Override
    public List<CustomerDto> getRecentCustomers() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return customerRepository.findRecentCustomers(thirtyDaysAgo)
                .stream()
                .map(CustomerDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CustomerDto> getInactiveCustomers(Pageable pageable) {
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        return customerRepository.findInactiveCustomers(ninetyDaysAgo, pageable)
                .map(CustomerDto::fromEntity);
    }

    @Override
    public int updateKycStatusForCustomers(List<UUID> customerIds, Customer.KycStatus newStatus) {
        return customerRepository.updateKycStatusForCustomers(customerIds, newStatus);
    }

    // Additional placeholder methods for interface completeness
    @Override
    public List<CustomerDto> createCustomersBulk(List<CustomerDto> customers) {
        // Bulk creation implementation
        return customers.stream()
                .map(this::createCustomer)
                .collect(Collectors.toList());
    }

    @Override
    public byte[] exportCustomersToExcel(List<UUID> customerIds) {
        // Excel export implementation
        return new byte[0];
    }

    @Override
    public CustomerDto updateCustomerProfile(UUID id, CustomerDto profileDto) {
        return updateCustomer(id, profileDto);
    }

    @Override
    public CustomerDto updatePrivacySettings(UUID id, Map<String, Boolean> privacySettings) {
        // Privacy settings implementation
        return getCustomerById(id);
    }

    @Override
    public List<Map<String, Object>> getCustomerAuditHistory(UUID customerId) {
        // Audit history implementation
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getCustomerActivityLog(UUID customerId, int days) {
        // Activity log implementation
        return new ArrayList<>();
    }

    @Override
    public boolean canCustomerApplyForLoan(UUID customerId) {
        CustomerDto customer = getCustomerById(customerId);
        return customer.getIsKycCompleted();
    }

    @Override
    public BigDecimal calculateLoanLimit(UUID customerId) {
        CustomerDto customer = getCustomerById(customerId);
        if (customer.getMonthlyIncome() != null) {
            return customer.getMonthlyIncome().multiply(BigDecimal.valueOf(10));
        }
        return BigDecimal.ZERO;
    }

    @Override
    public String determineRiskCategory(UUID customerId) {
        // Risk category determination logic
        return "MEDIUM";
    }

    @Override
    public boolean sendNotificationToCustomer(UUID customerId, String subject, String message) {
        // Notification implementation
        return true;
    }

    @Override
    public boolean sendKycReminder(UUID customerId) {
        // KYC reminder implementation
        return true;
    }

    @Override
    public int cleanupInactiveCustomers(int inactiveDays) {
        // Cleanup implementation
        return 0;
    }

    @Override
    public List<CustomerDto> getCustomersWithIncompleteInfo() {
        // Get customers with incomplete information
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> validateDataIntegrity() {
        // Data integrity validation
        return new HashMap<>();
    }
}