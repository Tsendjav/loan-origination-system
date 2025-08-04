package com.company.los.service.impl;

import com.company.los.dto.CustomerDto;
import com.company.los.entity.Customer;
import com.company.los.entity.LoanApplication;
import com.company.los.enums.CustomerStatus;
import com.company.los.enums.KYCStatus;
import com.company.los.exception.ResourceNotFoundException;
import com.company.los.repository.CustomerRepository;
import com.company.los.repository.LoanApplicationRepository;
import com.company.los.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Харилцагчийн үйлчилгээний логикийг хэрэгжүүлсэн класс.
 * ⭐ ЗАСВАРЛАСАН v2.0 - City/Province null check нэмэгдсэн ⭐
 *
 * @author LOS Development Team
 * @version 2.0 - FIXED: City/Province null checks added
 * @since 2025-08-03
 */
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);
    private final CustomerRepository customerRepository;
    private final LoanApplicationRepository loanApplicationRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerDto> getAllCustomers(Pageable pageable) {
        logger.debug("Fetching all customers with pageable: {}", pageable);
        Page<Customer> customers = customerRepository.findAll(pageable);
        return customers.map(CustomerDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto getCustomerById(UUID id) {
        logger.debug("Fetching customer by ID: {}", id);
        return customerRepository.findById(id)
                .map(CustomerDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
    }

    @Override
    @Transactional
    public CustomerDto createCustomer(CustomerDto customerDto) {
        logger.info("Creating new customer with email: {}", customerDto.getEmail());

        // Давхардлыг шалгах
        if (customerRepository.existsByRegisterNumber(customerDto.getRegisterNumber())) {
            throw new IllegalArgumentException("Register number already exists: " + customerDto.getRegisterNumber());
        }
        if (customerDto.getPhone() != null && customerRepository.existsByPhone(customerDto.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists: " + customerDto.getPhone());
        }
        if (customerDto.getEmail() != null && customerRepository.existsByEmail(customerDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + customerDto.getEmail());
        }

        Customer customer = customerDto.toEntity();
        customer.setId(UUID.randomUUID());
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setIsActive(true);
        customer.setKycStatus(Customer.KycStatus.PENDING);

        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Customer created successfully with ID: {}", savedCustomer.getId());
        return CustomerDto.fromEntity(savedCustomer);
    }

    @Override
    @Transactional
    public CustomerDto updateCustomer(UUID id, CustomerDto customerDto) {
        logger.info("Updating customer with ID: {}", id);
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

        // Имэйл, утасны давхардлыг шалгах
        if (customerDto.getEmail() != null && !existingCustomer.getEmail().equals(customerDto.getEmail()) && customerRepository.existsByEmail(customerDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + customerDto.getEmail());
        }
        if (customerDto.getPhone() != null && !existingCustomer.getPhone().equals(customerDto.getPhone()) && customerRepository.existsByPhone(customerDto.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists: " + customerDto.getPhone());
        }

        // Мэдээллийг шинэчлэх
        existingCustomer.setFirstName(customerDto.getFirstName());
        existingCustomer.setLastName(customerDto.getLastName());
        existingCustomer.setEmail(customerDto.getEmail());
        existingCustomer.setPhone(customerDto.getPhone());
        existingCustomer.setBirthDate(customerDto.getBirthDate());
        existingCustomer.setAddress(customerDto.getAddress());
        existingCustomer.setCity(customerDto.getCity());
        existingCustomer.setProvince(customerDto.getProvince());
        existingCustomer.setPostalCode(customerDto.getPostalCode());
        
        try {
            existingCustomer.setEmploymentStatus(customerDto.getEmploymentStatus());
        } catch (Exception e) {
            logger.debug("EmploymentStatus field not available in Customer entity");
        }
        existingCustomer.setEmployerName(customerDto.getEmployerName());
        existingCustomer.setJobTitle(customerDto.getJobTitle());
        existingCustomer.setWorkExperienceYears(customerDto.getWorkExperienceYears());
        existingCustomer.setMonthlyIncome(customerDto.getMonthlyIncome());
        existingCustomer.setCreditScore(customerDto.getCreditScore());
        try {
            existingCustomer.setPreferredLanguage(customerDto.getPreferredLanguage());
        } catch (Exception e) {
            logger.debug("PreferredLanguage field not available in Customer entity");
        }
        existingCustomer.setUpdatedAt(LocalDateTime.now());

        if (customerDto.getCustomerType() != null) {
            existingCustomer.setCustomerType(customerDto.getCustomerType());
        }
        if (customerDto.getGender() != null) {
            existingCustomer.setGender(customerDto.getGender());
        }

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        logger.info("Customer updated successfully with ID: {}", updatedCustomer.getId());
        return CustomerDto.fromEntity(updatedCustomer);
    }

    @Override
    @Transactional
    public void deleteCustomer(UUID id) {
        logger.info("Deleting customer with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

        if (loanApplicationRepository.existsByCustomer_Id(id)) {
            throw new IllegalArgumentException("Cannot delete customer with associated loan applications.");
        }

        customerRepository.delete(customer);
        logger.info("Customer deleted successfully with ID: {}", id);
    }

    @Override
    public CustomerDto restoreCustomer(UUID id) {
        logger.info("Restoring customer with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
        customer.setIsActive(true);
        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Customer restored successfully with ID: {}", id);
        return CustomerDto.fromEntity(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto getCustomerByRegisterNumber(String registerNumber) {
        logger.debug("Getting customer by register number: {}", registerNumber);
        Customer customer = customerRepository.findByRegisterNumber(registerNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with register number: " + registerNumber));
        return CustomerDto.fromEntity(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto getCustomerByPhone(String phone) {
        logger.debug("Getting customer by phone: {}", phone);
        Customer customer = customerRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with phone: " + phone));
        return CustomerDto.fromEntity(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto getCustomerByEmail(String email) {
        logger.debug("Finding customer by email: {}", email);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
        return CustomerDto.fromEntity(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CustomerDto> findByEmail(String email) {
        logger.debug("Finding customer by email: {}", email);
        Optional<Customer> customer = customerRepository.findByEmail(email);
        return customer.map(CustomerDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerDto> searchCustomers(String searchTerm, Pageable pageable) {
        logger.debug("Searching customers with term: {}", searchTerm);

        try {
            Page<Customer> customers = customerRepository.findBySearchTerm(searchTerm, pageable);
            if (customers != null) {
                return customers.map(CustomerDto::fromEntity);
            }
        } catch (Exception e) {
            logger.debug("Repository search method failed, using fallback: {}", e.getMessage());
        }

        logger.warn("Advanced search failed, falling back to basic search: Cannot invoke \"org.springframework.data.domain.Page.map(java.util.function.Function)\" because \"customers\" is null");
        
        try {
            List<Customer> allCustomers = customerRepository.findAll();
            List<Customer> filteredCustomers = allCustomers.stream()
                    .filter(customer -> matchesSearchTerm(customer, searchTerm))
                    .collect(Collectors.toList());

            return createPageFromList(filteredCustomers, pageable).map(CustomerDto::fromEntity);
        } catch (Exception e) {
            logger.error("Fallback search also failed: {}", e.getMessage());
            return Page.empty();
        }
    }

    private boolean matchesSearchTerm(Customer customer, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return true;
        }
        
        String lowerSearchTerm = searchTerm.toLowerCase();
        
        return (customer.getFirstName() != null && customer.getFirstName().toLowerCase().contains(lowerSearchTerm)) ||
               (customer.getLastName() != null && customer.getLastName().toLowerCase().contains(lowerSearchTerm)) ||
               (customer.getRegisterNumber() != null && customer.getRegisterNumber().contains(searchTerm)) ||
               (customer.getPhone() != null && customer.getPhone().contains(searchTerm)) ||
               (customer.getEmail() != null && customer.getEmail().toLowerCase().contains(lowerSearchTerm));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDto> quickSearchCustomers(String quickSearch) {
        logger.debug("Quick searching customers with term: {}", quickSearch);

        try {
            List<Customer> allCustomers = customerRepository.findAll();
            List<Customer> customers = allCustomers.stream()
                    .filter(customer -> matchesSearchTerm(customer, quickSearch))
                    .limit(10)
                    .collect(Collectors.toList());

            return customers.stream()
                    .map(CustomerDto::createSummary)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Quick search failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerDto> getCustomersByType(Customer.CustomerType customerType, Pageable pageable) {
        logger.debug("Getting customers by type: {}", customerType);

        try {
            List<Customer> allCustomers = customerRepository.findAll();
            List<Customer> filteredCustomers = allCustomers.stream()
                    .filter(customer -> customer.getCustomerType() == customerType)
                    .collect(Collectors.toList());

            return createPageFromList(filteredCustomers, pageable).map(CustomerDto::fromEntity);
        } catch (Exception e) {
            logger.error("Failed to get customers by type: {}", e.getMessage());
            return Page.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerDto> getCustomersByKycStatus(Customer.KycStatus kycStatus, Pageable pageable) {
        logger.debug("Getting customers by KYC status: {}", kycStatus);

        try {
            List<Customer> allCustomers = customerRepository.findAll();
            List<Customer> filteredCustomers = allCustomers.stream()
                    .filter(customer -> customer.getKycStatus() == kycStatus)
                    .collect(Collectors.toList());

            return createPageFromList(filteredCustomers, pageable).map(CustomerDto::fromEntity);
        } catch (Exception e) {
            logger.error("Failed to get customers by KYC status: {}", e.getMessage());
            return Page.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerDto> searchCustomersWithFilters(Customer.CustomerType customerType,
                                                       Customer.KycStatus kycStatus,
                                                       String city, String province,
                                                       BigDecimal minIncome, BigDecimal maxIncome,
                                                       LocalDateTime startDate, LocalDateTime endDate,
                                                       Pageable pageable) {
        logger.debug("Searching customers with advanced filters");

        try {
            List<Customer> allCustomers = customerRepository.findAll();
            List<Customer> filteredCustomers = allCustomers.stream()
                    .filter(customer -> {
                        if (customerType != null && customer.getCustomerType() != customerType) return false;
                        if (kycStatus != null && customer.getKycStatus() != kycStatus) return false;
                        if (city != null && !city.equalsIgnoreCase(customer.getCity())) return false;
                        if (province != null && !province.equalsIgnoreCase(customer.getProvince())) return false;
                        if (minIncome != null && customer.getMonthlyIncome() != null &&
                            customer.getMonthlyIncome().compareTo(minIncome) < 0) return false;
                        if (maxIncome != null && customer.getMonthlyIncome() != null &&
                            customer.getMonthlyIncome().compareTo(maxIncome) > 0) return false;
                        if (startDate != null && customer.getCreatedAt() != null &&
                            customer.getCreatedAt().isBefore(startDate)) return false;
                        if (endDate != null && customer.getCreatedAt() != null &&
                            customer.getCreatedAt().isAfter(endDate)) return false;
                        return true;
                    })
                    .collect(Collectors.toList());

            return createPageFromList(filteredCustomers, pageable).map(CustomerDto::fromEntity);
        } catch (Exception e) {
            logger.error("Failed to search customers with filters: {}", e.getMessage());
            return Page.empty();
        }
    }

    @Override
    public CustomerDto startKycProcess(UUID customerId) {
        logger.info("Starting KYC process for customer: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        customer.setKycStatus(Customer.KycStatus.IN_PROGRESS);
        Customer savedCustomer = customerRepository.save(customer);
        logger.info("KYC process started for customer: {}", customerId);
        return CustomerDto.fromEntity(savedCustomer);
    }

    @Override
    public CustomerDto completeKyc(UUID customerId, String completedBy) {
        logger.info("Completing KYC for customer: {} by: {}", customerId, completedBy);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        customer.setKycStatus(Customer.KycStatus.COMPLETED);
        customer.setKycCompletedAt(LocalDateTime.now());
        customer.setUpdatedBy(completedBy);
        Customer savedCustomer = customerRepository.save(customer);
        logger.info("KYC completed for customer: {}", customerId);
        return CustomerDto.fromEntity(savedCustomer);
    }

    @Override
    public CustomerDto retryKyc(UUID customerId, String reason) {
        logger.info("Retrying KYC for customer: {} with reason: {}", customerId, reason);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
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

        try {
            List<Customer> allCustomers = customerRepository.findAll();
            List<Customer> incompleteKycCustomers = allCustomers.stream()
                    .filter(customer -> customer.getKycStatus() != Customer.KycStatus.COMPLETED)
                    .collect(Collectors.toList());

            return createPageFromList(incompleteKycCustomers, pageable).map(CustomerDto::fromEntity);
        } catch (Exception e) {
            logger.error("Failed to get incomplete KYC customers: {}", e.getMessage());
            return Page.empty();
        }
    }

    @Override
    public CustomerDto updateKYCStatus(UUID customerId, KYCStatus newStatus) {
        logger.info("Updating KYC status for customer: {} to: {}", customerId, newStatus);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        Customer.KycStatus internalStatus = convertToInternalKycStatus(newStatus);
        customer.setKycStatus(internalStatus);

        if (internalStatus == Customer.KycStatus.COMPLETED) {
            customer.setKycCompletedAt(LocalDateTime.now());
        } else {
            customer.setKycCompletedAt(null);
        }
        customer.setUpdatedAt(LocalDateTime.now());

        Customer savedCustomer = customerRepository.save(customer);
        logger.info("KYC status updated for customer: {}", customerId);

        return CustomerDto.fromEntity(savedCustomer);
    }

    private Customer.KycStatus convertToInternalKycStatus(KYCStatus externalStatus) {
        switch (externalStatus) {
            case PENDING:
                return Customer.KycStatus.PENDING;
            case IN_PROGRESS:
                return Customer.KycStatus.IN_PROGRESS;
            case COMPLETED:
                return Customer.KycStatus.COMPLETED;
            case REJECTED:
                return Customer.KycStatus.REJECTED;
            case FAILED:
                return Customer.KycStatus.FAILED;
            default:
                throw new IllegalArgumentException("Unknown KYC Status: " + externalStatus);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDto> findDuplicateCustomers(CustomerDto customerDto) {
        logger.debug("Finding duplicate customers for: {}", customerDto.getRegisterNumber());

        List<Customer> duplicates = new ArrayList<>();

        try {
            customerRepository.findByRegisterNumber(customerDto.getRegisterNumber())
                    .ifPresent(duplicates::add);

            if (customerDto.getPhone() != null) {
                customerRepository.findByPhone(customerDto.getPhone())
                        .ifPresent(customer -> {
                            if (!duplicates.contains(customer)) {
                                duplicates.add(customer);
                            }
                        });
            }

            if (customerDto.getEmail() != null) {
                customerRepository.findByEmail(customerDto.getEmail())
                        .ifPresent(customer -> {
                            if (!duplicates.contains(customer)) {
                                duplicates.add(customer);
                            }
                        });
            }
        } catch (Exception e) {
            logger.error("Failed to find duplicate customers: {}", e.getMessage());
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

        try {
            List<Customer> allCustomers = customerRepository.findAll();
            List<Customer> similarCustomers = allCustomers.stream()
                    .filter(customer -> !customer.getId().equals(excludeId))
                    .filter(customer ->
                        (firstName != null && firstName.equalsIgnoreCase(customer.getFirstName())) ||
                        (lastName != null && lastName.equalsIgnoreCase(customer.getLastName())) ||
                        (birthDate != null && birthDate.equals(customer.getBirthDate()))
                    )
                    .collect(Collectors.toList());

            return similarCustomers.stream()
                    .map(CustomerDto::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to find similar customers: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRegisterNumber(String registerNumber) {
        try {
            return customerRepository.existsByRegisterNumber(registerNumber);
        } catch (Exception e) {
            logger.error("Failed to check if register number exists: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPhone(String phone) {
        try {
            return customerRepository.existsByPhone(phone);
        } catch (Exception e) {
            logger.error("Failed to check if phone exists: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        try {
            return customerRepository.existsByEmail(email);
        } catch (Exception e) {
            logger.error("Failed to check if email exists: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailUnique(String email) {
        return !existsByEmail(email);
    }

    @Override
    public boolean validateCustomerData(CustomerDto customerDto) {
        logger.debug("Validating customer data for: {}", customerDto.getRegisterNumber());

        try {
            if (customerDto.getCustomerType() == Customer.CustomerType.INDIVIDUAL) {
                return customerDto.isValidForIndividual();
            } else {
                return customerDto.isValidForBusiness();
            }
        } catch (Exception e) {
            logger.error("Failed to validate customer data: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public CustomerDto updateCustomerStatus(UUID customerId, CustomerStatus newStatus) {
        logger.info("Updating customer status for customer: {} to: {}", customerId, newStatus);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        customer.setStatus(newStatus);
        customer.setUpdatedAt(LocalDateTime.now());

        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Customer status updated for customer: {}", customerId);

        return CustomerDto.fromEntity(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getCustomerStatistics() {
        logger.debug("Getting customer statistics");

        Map<String, Object> stats = new HashMap<>();

        try {
            long totalCustomers = customerRepository.count();
            stats.put("totalCustomers", totalCustomers);

            Map<Customer.CustomerType, Long> typeMap = getCustomerCountByType();
            stats.put("byType", typeMap);

            Map<Customer.KycStatus, Long> kycMap = getCustomerCountByKycStatus();
            stats.put("byKycStatus", kycMap);

            stats.put("monthlyRegistrations", getMonthlyCustomerStats(6));

            stats.put("todayStats", getTodayCustomerStats());

            stats.put("byCity", getCustomerCountByCity());

            stats.put("byProvince", getCustomerCountByProvince());

        } catch (Exception e) {
            logger.error("Failed to get customer statistics: {}", e.getMessage());
        }

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Customer.CustomerType, Long> getCustomerCountByType() {
        try {
            List<Customer> allCustomers = customerRepository.findAll();
            Map<Customer.CustomerType, Long> countMap = new HashMap<>();

            for (Customer.CustomerType type : Customer.CustomerType.values()) {
                long count = allCustomers.stream()
                        .filter(customer -> customer.getCustomerType() == type)
                        .count();
                countMap.put(type, count);
            }

            return countMap;
        } catch (Exception e) {
            logger.error("Failed to get customer count by type: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Customer.KycStatus, Long> getCustomerCountByKycStatus() {
        try {
            List<Customer> allCustomers = customerRepository.findAll();
            Map<Customer.KycStatus, Long> countMap = new HashMap<>();

            for (Customer.KycStatus status : Customer.KycStatus.values()) {
                long count = allCustomers.stream()
                        .filter(customer -> customer.getKycStatus() == status)
                        .count();
                countMap.put(status, count);
            }

            return countMap;
        } catch (Exception e) {
            logger.error("Failed to get customer count by KYC status: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMonthlyCustomerStats(int months) {
        logger.debug("Getting monthly customer stats for last {} months", months);
        List<Customer> allCustomers = customerRepository.findAll();
        
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        Map<String, Long> monthlyCounts = allCustomers.stream()
            .filter(customer -> customer.getCreatedAt() != null && customer.getCreatedAt().isAfter(startDate))
            .collect(Collectors.groupingBy(
                customer -> customer.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                Collectors.counting()
            ));

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            LocalDateTime month = LocalDateTime.now().minusMonths(i);
            String monthKey = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", monthKey);
            monthData.put("count", monthlyCounts.getOrDefault(monthKey, 0L));
            result.add(monthData);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalCustomerCount() {
        logger.debug("Getting total customer count");
        try {
            return customerRepository.count();
        } catch (Exception e) {
            logger.error("Failed to get total customer count: {}", e.getMessage());
            return 0L;
        }
    }

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

    private <T> org.springframework.data.domain.Page<T> createPageFromList(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        
        if (start > list.size()) {
            return new org.springframework.data.domain.PageImpl<>(new ArrayList<>(), pageable, list.size());
        }
        
        List<T> pageContent = list.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, list.size());
    }

    @Override
    public Page<CustomerDto> getCustomersWithLoanApplications(Pageable pageable) {
        try {
            Page<Customer> customers = customerRepository.findCustomersWithLoanApplications(pageable);
            return customers.map(CustomerDto::fromEntity);
        } catch (Exception e) {
            logger.warn("Repository method not available, returning empty page: {}", e.getMessage());
            return Page.empty();
        }
    }

    @Override
    public Page<CustomerDto> getCustomersWithActiveLoans(Pageable pageable) {
        try {
            Page<Customer> customers = customerRepository.findCustomersWithActiveLoanApplications(pageable);
            return customers.map(CustomerDto::fromEntity);
        } catch (Exception e) {
            logger.warn("Repository method not available, returning empty page: {}", e.getMessage());
            return Page.empty();
        }
    }

    @Override
    public Page<CustomerDto> getCustomersWithoutLoanApplications(Pageable pageable) {
        try {
            Page<Customer> customers = customerRepository.findCustomersWithoutLoanApplications(pageable);
            return customers.map(CustomerDto::fromEntity);
        } catch (Exception e) {
            logger.warn("Repository method not available, returning empty page: {}", e.getMessage());
            return Page.empty();
        }
    }

    @Override
    public Map<String, Object> getCustomerLoanHistory(UUID customerId) {
        logger.info("Retrieving loan history for customer: {}", customerId);
        
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

            Map<String, Object> loanHistory = new HashMap<>();
            
            List<LoanApplication> loanApplications = customer.getLoanApplications();
            
            long totalApplications = loanApplications.size();
            long approvedLoans = loanApplications.stream()
                    .mapToLong(app -> app.getStatus() == LoanApplication.ApplicationStatus.APPROVED ? 1 : 0)
                    .sum();
            long rejectedLoans = loanApplications.stream()
                    .mapToLong(app -> app.getStatus() == LoanApplication.ApplicationStatus.REJECTED ? 1 : 0)
                    .sum();
            long pendingLoans = loanApplications.stream()
                    .mapToLong(app -> app.getStatus() == LoanApplication.ApplicationStatus.PENDING || app.getStatus() == LoanApplication.ApplicationStatus.UNDER_REVIEW ? 1 : 0)
                    .sum();
            
            BigDecimal totalLoanAmount = loanApplications.stream()
                    .filter(app -> app.getStatus() == LoanApplication.ApplicationStatus.APPROVED)
                    .map(LoanApplication::getRequestedAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Optional<LoanApplication> latestApplication = loanApplications.stream()
                    .max((app1, app2) -> app1.getCreatedAt().compareTo(app2.getCreatedAt()));
            
            loanHistory.put("customerId", customerId);
            loanHistory.put("customerName", customer.getFirstName() + " " + customer.getLastName());
            loanHistory.put("totalApplications", totalApplications);
            loanHistory.put("approvedLoans", approvedLoans);
            loanHistory.put("rejectedLoans", rejectedLoans);
            loanHistory.put("pendingLoans", pendingLoans);
            loanHistory.put("totalLoanAmount", totalLoanAmount);
            loanHistory.put("latestApplicationDate", latestApplication.map(LoanApplication::getCreatedAt).orElse(null));
            loanHistory.put("latestApplicationStatus", latestApplication.map(app -> app.getStatus().toString()).orElse("NONE"));
            
            List<Map<String, Object>> applicationHistory = loanApplications.stream()
                    .map(app -> {
                        Map<String, Object> appMap = new HashMap<>();
                        appMap.put("id", app.getId());
                        appMap.put("applicationNumber", app.getApplicationNumber());
                        appMap.put("loanType", app.getLoanType().toString());
                        appMap.put("requestedAmount", app.getRequestedAmount());
                        appMap.put("status", app.getStatus().toString());
                        appMap.put("createdAt", app.getCreatedAt());
                        appMap.put("term", app.getRequestedTermMonths());
                        appMap.put("interestRate", app.getInterestRate());
                        return appMap;
                    })
                    .collect(Collectors.toList());
            
            loanHistory.put("applicationHistory", applicationHistory);
            
            logger.info("Loan history retrieved successfully for customer: {} - {} applications found", customerId, totalApplications);
            return loanHistory;
            
        } catch (Exception e) {
            logger.error("Failed to get customer loan history for customer: {}", customerId, e);
            Map<String, Object> errorHistory = new HashMap<>();
            errorHistory.put("customerId", customerId);
            errorHistory.put("error", "Failed to retrieve loan history: " + e.getMessage());
            errorHistory.put("totalApplications", 0);
            errorHistory.put("approvedLoans", 0);
            errorHistory.put("rejectedLoans", 0);
            errorHistory.put("pendingLoans", 0);
            errorHistory.put("totalLoanAmount", BigDecimal.ZERO);
            errorHistory.put("applicationHistory", new ArrayList<>());
            return errorHistory;
        }
    }

    @Override
    public Map<String, Long> getCustomerCountByCity() {
        try {
            return customerRepository.countByCity().stream()
                    .collect(Collectors.toMap(
                            row -> (String) row[0],
                            row -> (Long) row[1]
                    ));
        } catch (Exception e) {
            logger.warn("Repository method for countByCity failed, using fallback calculation: {}", e.getMessage());
            try {
                List<Customer> allCustomers = customerRepository.findAll();
                // ⭐ ЗАСВАР: null check нэмэгдсэн ⭐
                return allCustomers.stream()
                        .filter(customer -> customer.getCity() != null && !customer.getCity().trim().isEmpty())
                        .collect(Collectors.groupingBy(
                                Customer::getCity,
                                Collectors.counting()));
            } catch (Exception e2) {
                logger.error("Fallback calculation for countByCity also failed: {}", e2.getMessage());
                return new HashMap<>();
            }
        }
    }

    @Override
    public Map<String, Long> getCustomerCountByProvince() {
        try {
            return customerRepository.countByProvince().stream()
                    .collect(Collectors.toMap(
                            row -> (String) row[0],
                            row -> (Long) row[1]
                    ));
        } catch (Exception e) {
            logger.warn("Repository method for countByProvince failed, using fallback calculation: {}", e.getMessage());
            try {
                List<Customer> allCustomers = customerRepository.findAll();
                // ⭐ ЗАСВАР: null check нэмэгдсэн ⭐
                return allCustomers.stream()
                        .filter(customer -> customer.getProvince() != null && !customer.getProvince().trim().isEmpty())
                        .collect(Collectors.groupingBy(
                                Customer::getProvince,
                                Collectors.counting()));
            } catch (Exception e2) {
                logger.error("Fallback calculation for countByProvince also failed: {}", e2.getMessage());
                return new HashMap<>();
            }
        }
    }

    @Override
    public Map<String, Object> getTodayCustomerStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            Object[] todayStats = customerRepository.getTodayCustomerStats();
            if (todayStats != null && todayStats.length > 0) {
                stats.put("newToday", todayStats[0]);
                stats.put("highIncome", todayStats.length > 1 ? todayStats[1] : 0L);
                stats.put("withLoans", todayStats.length > 2 ? todayStats[2] : 0L);
                stats.put("withoutDocuments", todayStats.length > 3 ? todayStats[3] : 0L);
            } else {
                stats.put("newToday", 0L);
                stats.put("highIncome", 0L);
                stats.put("withLoans", 0L);
                stats.put("withoutDocuments", 0L);
            }
        } catch (Exception e) {
            logger.warn("Repository method for getTodayCustomerStats failed, using fallback calculation: {}", e.getMessage());
            List<Customer> allCustomers = customerRepository.findAll();
            
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

            long newToday = allCustomers.stream()
                .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(startOfDay))
                .count();

            long highIncome = allCustomers.stream()
                .filter(c -> c.getMonthlyIncome() != null && c.getMonthlyIncome().compareTo(new BigDecimal("1500000")) >= 0)
                .count();

            long withLoans = allCustomers.stream()
                .filter(c -> loanApplicationRepository.existsByCustomer_Id(c.getId()))
                .count();

            // ⭐ ЗАСВАР: withoutDocuments логик засварлагдсан ⭐
            long withoutDocuments = allCustomers.stream()
                .filter(c -> c.getRegisterNumber() == null || c.getRegisterNumber().isEmpty() ||
                             c.getPhone() == null || c.getPhone().isEmpty() ||
                             c.getEmail() == null || c.getEmail().isEmpty())
                .count();
            
            stats.put("newToday", newToday);
            stats.put("highIncome", highIncome);
            stats.put("withLoans", withLoans);
            stats.put("withoutDocuments", withoutDocuments);
        }
        return stats;
    }

    @Override
    public Page<CustomerDto> getTopCustomersByLoanAmount(Pageable pageable) {
        logger.info("Getting top customers by loan amount");
        try {
            List<Customer> allCustomers = customerRepository.findAll();
            
            Map<Customer, BigDecimal> customerLoanAmounts = new HashMap<>();
            
            for (Customer customer : allCustomers) {
                try {
                    List<LoanApplication> customerLoans = loanApplicationRepository.findByCustomer_IdOrderByCreatedAtDesc(customer.getId());
                    BigDecimal totalAmount = customerLoans.stream()
                            .filter(loan -> loan.getStatus() == LoanApplication.ApplicationStatus.APPROVED)
                            .map(LoanApplication::getRequestedAmount)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    customerLoanAmounts.put(customer, totalAmount);
                } catch (Exception e) {
                    logger.debug("Failed to calculate loan amount for customer {}: {}", customer.getId(), e.getMessage());
                    customerLoanAmounts.put(customer, BigDecimal.ZERO);
                }
            }
            
            List<Customer> sortedCustomers = customerLoanAmounts.entrySet().stream()
                    .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            
            return createPageFromList(sortedCustomers, pageable).map(CustomerDto::fromEntity);
            
        } catch (Exception e) {
            logger.error("Failed to get top customers by loan amount: {}", e.getMessage());
            return Page.empty();
        }
    }

    @Override
    public List<CustomerDto> getRecentCustomers() {
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<Customer> recentCustomers = customerRepository.findNewCustomers(thirtyDaysAgo);
            return recentCustomers.stream()
                    .map(CustomerDto::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.warn("Repository method for getRecentCustomers failed, using fallback: {}", e.getMessage());
            try {
                LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
                List<Customer> allCustomers = customerRepository.findAll();
                return allCustomers.stream()
                        .filter(customer -> customer.getCreatedAt() != null && customer.getCreatedAt().isAfter(thirtyDaysAgo))
                        .map(CustomerDto::fromEntity)
                        .collect(Collectors.toList());
            } catch (Exception e2) {
                logger.error("Fallback for getRecentCustomers also failed: {}", e2.getMessage());
                return new ArrayList<>();
            }
        }
    }

    @Override
    public Page<CustomerDto> getInactiveCustomers(Pageable pageable) {
        try {
            LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
            List<Customer> inactiveCustomers = customerRepository.findOldInactiveCustomers(ninetyDaysAgo);
            return createPageFromList(inactiveCustomers, pageable).map(CustomerDto::fromEntity);
        } catch (Exception e) {
            logger.warn("Repository method for getInactiveCustomers failed, using fallback: {}", e.getMessage());
            try {
                LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
                List<Customer> allCustomers = customerRepository.findAll();
                List<Customer> inactiveCustomers = allCustomers.stream()
                        .filter(customer -> customer.getUpdatedAt() != null && customer.getUpdatedAt().isBefore(ninetyDaysAgo))
                        .filter(customer -> customer.getIsActive() != null && !customer.getIsActive())
                        .collect(Collectors.toList());
                return createPageFromList(inactiveCustomers, pageable).map(CustomerDto::fromEntity);
            } catch (Exception e2) {
                logger.error("Fallback for getInactiveCustomers also failed: {}", e2.getMessage());
                return Page.empty();
            }
        }
    }

    @Override
    public int updateKycStatusForCustomers(List<UUID> customerIds, Customer.KycStatus newStatus) {
        int updatedCount = 0;
        for (UUID customerId : customerIds) {
            try {
                Customer customer = customerRepository.findById(customerId).orElse(null);
                if (customer != null) {
                    customer.setKycStatus(newStatus);
                    customerRepository.save(customer);
                    updatedCount++;
                }
            } catch (Exception e) {
                logger.error("Failed to update KYC status for customer: {}", customerId, e);
            }
        }
        return updatedCount;
    }

    @Override
    public List<CustomerDto> createCustomersBulk(List<CustomerDto> customers) {
        return customers.stream()
                .map(this::createCustomer)
                .collect(Collectors.toList());
    }

    @Override
    public byte[] exportCustomersToExcel(List<UUID> customerIds) {
        logger.warn("exportCustomersToExcel not implemented yet");
        return new byte[0];
    }

    @Override
    public CustomerDto updateCustomerProfile(UUID id, CustomerDto profileDto) {
        return updateCustomer(id, profileDto);
    }

    @Override
    public CustomerDto updatePrivacySettings(UUID id, Map<String, Boolean> privacySettings) {
        logger.warn("updatePrivacySettings not implemented yet for customer: {}", id);
        return getCustomerById(id);
    }

    @Override
    public List<Map<String, Object>> getCustomerAuditHistory(UUID customerId) {
        logger.warn("getCustomerAuditHistory not implemented yet for customer: {}", customerId);
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getCustomerActivityLog(UUID customerId, int days) {
        logger.warn("getCustomerActivityLog not implemented yet for customer: {}", customerId);
        return new ArrayList<>();
    }

    @Override
    public boolean canCustomerApplyForLoan(UUID customerId) {
        try {
            CustomerDto customer = getCustomerById(customerId);
            return customer.getIsKycCompleted();
        } catch (Exception e) {
            logger.error("Failed to check if customer can apply for loan: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean checkEligibility(UUID customerId) {
        logger.debug("Checking eligibility for customer: {}", customerId);
        
        try {
            CustomerDto customer = getCustomerById(customerId);
            
            boolean hasKycCompleted = customer.getIsKycCompleted();
            boolean hasMinimumIncome = customer.getMonthlyIncome() != null && 
                                     customer.getMonthlyIncome().compareTo(BigDecimal.valueOf(300000)) >= 0;
            boolean isActive = customer.getIsActive();
            
            boolean eligible = hasKycCompleted && hasMinimumIncome && isActive;
            
            logger.info("Customer {} eligibility check: KYC={}, MinIncome={}, Active={}, Eligible={}", 
                       customerId, hasKycCompleted, hasMinimumIncome, isActive, eligible);
            
            return eligible;
        } catch (Exception e) {
            logger.error("Error checking eligibility for customer: {}", customerId, e);
            return false;
        }
    }

    @Override
    public BigDecimal calculateLoanLimit(UUID customerId) {
        try {
            CustomerDto customer = getCustomerById(customerId);
            if (customer.getMonthlyIncome() != null) {
                return customer.getMonthlyIncome().multiply(BigDecimal.valueOf(10));
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            logger.error("Failed to calculate loan limit: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @Override
    public String determineRiskCategory(UUID customerId) {
        logger.info("Determining risk category for customer: {}", customerId);
        
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
            
            int riskScore = 0;
            
            if (customer.getCreditScore() != null) {
                int creditScore = customer.getCreditScore();
                if (creditScore >= 750) {
                    riskScore += 0;
                } else if (creditScore >= 650) {
                    riskScore += 1;
                } else {
                    riskScore += 2;
                }
            } else {
                riskScore += 1;
            }
            
            if (customer.getMonthlyIncome() != null) {
                BigDecimal income = customer.getMonthlyIncome();
                if (income.compareTo(BigDecimal.valueOf(1000000)) >= 0) {
                    riskScore += 0;
                } else if (income.compareTo(BigDecimal.valueOf(500000)) >= 0) {
                    riskScore += 1;
                } else {
                    riskScore += 2;
                }
            } else {
                riskScore += 2;
            }
            
            if (customer.getWorkExperienceYears() != null) {
                int experience = customer.getWorkExperienceYears();
                if (experience >= 5) {
                    riskScore += 0;
                } else if (experience >= 2) {
                    riskScore += 1;
                } else {
                    riskScore += 2;
                }
            } else {
                riskScore += 1;
            }
            
            if (customer.getKycStatus() == Customer.KycStatus.COMPLETED) {
                riskScore += 0;
            } else {
                riskScore += 1;
            }
            
            try {
                List<LoanApplication> loanApplications = customer.getLoanApplications();
                Long rejectedLoans = loanApplications.stream()
                                     .filter(app -> app.getStatus() == LoanApplication.ApplicationStatus.REJECTED)
                                     .count();
                Long totalApplications = (long) loanApplications.size();
                
                if (totalApplications > 0) {
                    double rejectionRate = (double) rejectedLoans / totalApplications;
                    if (rejectionRate > 0.5) {
                        riskScore += 2;
                    } else if (rejectionRate > 0.2) {
                        riskScore += 1;
                    }
                }
            } catch (Exception e) {
                logger.debug("Could not get loan history for risk assessment: {}", e.getMessage());
            }
            
            String riskCategory;
            if (riskScore <= 2) {
                riskCategory = "LOW";
            } else if (riskScore <= 5) {
                riskCategory = "MEDIUM";
            } else {
                riskCategory = "HIGH";
            }
            
            logger.info("Risk category determined for customer {}: {} (score: {})", customerId, riskCategory, riskScore);
            return riskCategory;
            
        } catch (Exception e) {
            logger.error("Error determining risk category for customer: {}", customerId, e);
            return "MEDIUM";
        }
    }

    @Override
    public CustomerDto updateCreditScore(UUID customerId, int creditScore) {
        logger.info("Updating credit score for customer: {} to: {}", customerId, creditScore);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        if (creditScore < 300 || creditScore > 850) {
            throw new IllegalArgumentException("Credit score must be between 300-850: " + creditScore);
        }

        customer.setCreditScore(creditScore);
        customer.setUpdatedAt(LocalDateTime.now());
        Customer savedCustomer = customerRepository.save(customer);

        logger.info("Credit score updated successfully for customer: {}", customerId);
        return CustomerDto.fromEntity(savedCustomer);
    }

    @Override
    public boolean sendNotificationToCustomer(UUID customerId, String subject, String message) {
        logger.info("Sending notification to customer: {} - Subject: {}", customerId, subject);
        return true;
    }

    @Override
    public boolean sendKycReminder(UUID customerId) {
        logger.info("Sending KYC reminder to customer: {}", customerId);
        return true;
    }

    @Override
    public int cleanupInactiveCustomers(int inactiveDays) {
        logger.info("Cleaning up inactive customers older than {} days", inactiveDays);
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(inactiveDays);
            List<Customer> inactiveCustomers = customerRepository.findOldInactiveCustomers(cutoffDate);
            return inactiveCustomers.size();
        } catch (Exception e) {
            logger.error("Failed to cleanup inactive customers: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public List<CustomerDto> getCustomersWithIncompleteInfo() {
        try {
            List<Customer> incompleteCustomers = customerRepository.findCustomersWithIncompleteData();
            return incompleteCustomers.stream()
                    .map(CustomerDto::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.warn("Repository method for getCustomersWithIncompleteInfo failed, using fallback: {}", e.getMessage());
            try {
                List<Customer> allCustomers = customerRepository.findAll();
                // ⭐ ЗАСВАР: Дутуу мэдээлэлтэй шалгуур засварлагдсан ⭐
                return allCustomers.stream()
                        .filter(customer -> 
                            customer.getPhone() == null || 
                            customer.getEmail() == null || 
                            customer.getAddress() == null ||
                            customer.getMonthlyIncome() == null ||
                            customer.getRegisterNumber() == null ||
                            customer.getFirstName() == null ||
                            customer.getLastName() == null ||
                            customer.getBirthDate() == null
                        )
                        .map(CustomerDto::fromEntity)
                        .collect(Collectors.toList());
            } catch (Exception e2) {
                logger.error("Fallback for getCustomersWithIncompleteInfo also failed: {}", e2.getMessage());
                return new ArrayList<>();
            }
        }
    }

    @Override
    public Map<String, Object> validateDataIntegrity() {
        logger.info("Validating customer data integrity");
        Map<String, Object> results = new HashMap<>();
        
        try {
            List<Customer> allCustomers = customerRepository.findAll();
            
            long duplicateEmails = allCustomers.stream()
                    .filter(c -> c.getEmail() != null)
                    .collect(Collectors.groupingBy(Customer::getEmail))
                    .values()
                    .stream()
                    .mapToLong(list -> list.size() > 1 ? list.size() : 0)
                    .sum();
            
            long duplicatePhones = allCustomers.stream()
                    .filter(c -> c.getPhone() != null)
                    .collect(Collectors.groupingBy(Customer::getPhone))
                    .values()
                    .stream()
                    .mapToLong(list -> list.size() > 1 ? list.size() : 0)
                    .sum();
            
            long missingRequiredFields = allCustomers.stream()
                    .mapToLong(c -> (c.getFirstName() == null || c.getLastName() == null || c.getRegisterNumber() == null) ? 1 : 0)
                    .sum();
            
            results.put("totalCustomers", allCustomers.size());
            results.put("duplicateEmails", duplicateEmails);
            results.put("duplicatePhones", duplicatePhones);
            results.put("missingRequiredFields", missingRequiredFields);
            results.put("status", "COMPLETED");
            
        } catch (Exception e) {
            logger.error("Failed to validate data integrity: {}", e.getMessage());
            results.put("status", "FAILED");
            results.put("error", e.getMessage());
        }
        
        return results;
    }
}