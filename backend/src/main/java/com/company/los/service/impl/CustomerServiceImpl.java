package com.company.los.service.impl;

import com.company.los.dto.CustomerDto;
import com.company.los.entity.Customer;
import com.company.los.enums.CustomerStatus;
import com.company.los.enums.KYCStatus;
import com.company.los.repository.CustomerRepository;
import com.company.los.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
        
        customerRepository.delete(customer);
        
        logger.info("Customer deleted successfully with ID: {}", id);
    }

    @Override
    public CustomerDto restoreCustomer(UUID id) {
        logger.info("Restoring customer with ID: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Харилцагч олдсонгүй: " + id));
        
        customer.setIsActive(true);
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
            return customers.map(CustomerDto::fromEntity);
        } catch (Exception e) {
            // Fallback to basic search
            List<Customer> allCustomers = customerRepository.findAll();
            List<Customer> filteredCustomers = allCustomers.stream()
                    .filter(customer -> 
                        (customer.getFirstName() != null && customer.getFirstName().toLowerCase().contains(searchTerm.toLowerCase())) ||
                        (customer.getLastName() != null && customer.getLastName().toLowerCase().contains(searchTerm.toLowerCase())) ||
                        (customer.getRegisterNumber() != null && customer.getRegisterNumber().contains(searchTerm)) ||
                        (customer.getPhone() != null && customer.getPhone().contains(searchTerm)) ||
                        (customer.getEmail() != null && customer.getEmail().toLowerCase().contains(searchTerm.toLowerCase()))
                    )
                    .collect(Collectors.toList());
            
            return createPageFromList(filteredCustomers, pageable).map(CustomerDto::fromEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDto> quickSearchCustomers(String quickSearch) {
        logger.debug("Quick searching customers with term: {}", quickSearch);
        
        List<Customer> allCustomers = customerRepository.findAll();
        List<Customer> customers = allCustomers.stream()
                .filter(customer -> 
                    (customer.getFirstName() != null && customer.getFirstName().toLowerCase().contains(quickSearch.toLowerCase())) ||
                    (customer.getLastName() != null && customer.getLastName().toLowerCase().contains(quickSearch.toLowerCase())) ||
                    (customer.getRegisterNumber() != null && customer.getRegisterNumber().contains(quickSearch))
                )
                .limit(10) // Limit for quick search
                .collect(Collectors.toList());
                
        return customers.stream()
                .map(CustomerDto::createSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerDto> getCustomersByType(Customer.CustomerType customerType, Pageable pageable) {
        logger.debug("Getting customers by type: {}", customerType);
        
        List<Customer> allCustomers = customerRepository.findAll();
        List<Customer> filteredCustomers = allCustomers.stream()
                .filter(customer -> customer.getCustomerType() == customerType)
                .collect(Collectors.toList());
        
        return createPageFromList(filteredCustomers, pageable).map(CustomerDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerDto> getCustomersByKycStatus(Customer.KycStatus kycStatus, Pageable pageable) {
        logger.debug("Getting customers by KYC status: {}", kycStatus);
        
        List<Customer> allCustomers = customerRepository.findAll();
        List<Customer> filteredCustomers = allCustomers.stream()
                .filter(customer -> customer.getKycStatus() == kycStatus)
                .collect(Collectors.toList());
        
        return createPageFromList(filteredCustomers, pageable).map(CustomerDto::fromEntity);
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
        
        List<Customer> allCustomers = customerRepository.findAll();
        List<Customer> incompleteKycCustomers = allCustomers.stream()
                .filter(customer -> customer.getKycStatus() != Customer.KycStatus.COMPLETED)
                .collect(Collectors.toList());
        
        return createPageFromList(incompleteKycCustomers, pageable).map(CustomerDto::fromEntity);
    }

    @Override
    public CustomerDto updateKYCStatus(UUID customerId, KYCStatus newStatus) {
        logger.info("Updating KYC status for customer: {} to: {}", customerId, newStatus);
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Харилцагч олдсонгүй: " + customerId));
        
        // Convert external enum to internal enum
        Customer.KycStatus internalStatus = convertToInternalKycStatus(newStatus);
        customer.setKycStatus(internalStatus);
        
        if (internalStatus == Customer.KycStatus.COMPLETED) {
            customer.setKycCompletedAt(LocalDateTime.now());
        }
        
        Customer savedCustomer = customerRepository.save(customer);
        logger.info("KYC status updated for customer: {}", customerId);
        
        return CustomerDto.fromEntity(savedCustomer);
    }

    /**
     * External KYCStatus enum-ийг Internal Customer.KycStatus enum-д хөрвүүлэх
     */
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
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !customerRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailUnique(String email) {
        return !customerRepository.existsByEmail(email);
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

    // Status management
    @Override
    public CustomerDto updateCustomerStatus(UUID customerId, CustomerStatus newStatus) {
        logger.info("Updating customer status for customer: {} to: {}", customerId, newStatus);
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Харилцагч олдсонгүй: " + customerId));
        
        customer.setStatus(newStatus);
        customer.setLastUpdated(LocalDateTime.now());
        
        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Customer status updated for customer: {}", customerId);
        
        return CustomerDto.fromEntity(savedCustomer);
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
        Map<Customer.CustomerType, Long> typeMap = getCustomerCountByType();
        stats.put("byType", typeMap);
        
        // By KYC status
        Map<Customer.KycStatus, Long> kycMap = getCustomerCountByKycStatus();
        stats.put("byKycStatus", kycMap);
        
        // Today's registrations
        try {
            Object[] todayStats = customerRepository.getTodayCustomerStats();
            if (todayStats != null && todayStats.length > 0) {
                stats.put("todayRegistrations", todayStats[0]); // newToday
            } else {
                stats.put("todayRegistrations", 0L);
            }
        } catch (Exception e) {
            stats.put("todayRegistrations", 0L);
        }
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Customer.CustomerType, Long> getCustomerCountByType() {
        List<Customer> allCustomers = customerRepository.findAll();
        Map<Customer.CustomerType, Long> countMap = new HashMap<>();
        
        for (Customer.CustomerType type : Customer.CustomerType.values()) {
            long count = allCustomers.stream()
                    .mapToLong(customer -> customer.getCustomerType() == type ? 1 : 0)
                    .sum();
            countMap.put(type, count);
        }
        
        return countMap;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Customer.KycStatus, Long> getCustomerCountByKycStatus() {
        List<Customer> allCustomers = customerRepository.findAll();
        Map<Customer.KycStatus, Long> countMap = new HashMap<>();
        
        for (Customer.KycStatus status : Customer.KycStatus.values()) {
            long count = allCustomers.stream()
                    .mapToLong(customer -> customer.getKycStatus() == status ? 1 : 0)
                    .sum();
            countMap.put(status, count);
        }
        
        return countMap;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMonthlyCustomerStats(int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        
        try {
            return customerRepository.getMonthlyCustomerStats(startDate).stream()
                    .map(row -> {
                        Map<String, Object> stat = new HashMap<>();
                        stat.put("month", row[0]);
                        stat.put("count", row[1]);
                        return stat;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching monthly customer stats from repository, falling back to manual calculation: {}", e.getMessage());
            List<Customer> allCustomers = customerRepository.findAll();
            Map<String, Long> monthlyStats = new HashMap<>();
            
            for (Customer customer : allCustomers) {
                if (customer.getCreatedAt() != null && customer.getCreatedAt().isAfter(startDate)) {
                    String monthKey = customer.getCreatedAt().getYear() + "-" + 
                                     String.format("%02d", customer.getCreatedAt().getMonthValue());
                    monthlyStats.put(monthKey, monthlyStats.getOrDefault(monthKey, 0L) + 1);
                }
            }
            
            return monthlyStats.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> monthStat = new HashMap<>();
                        monthStat.put("month", entry.getKey());
                        monthStat.put("count", entry.getValue());
                        return monthStat;
                    })
                    .collect(Collectors.toList());
        }
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

    // Helper method to create Page from List
    private <T> org.springframework.data.domain.Page<T> createPageFromList(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        List<T> pageContent = list.subList(start, end);
        
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, list.size());
    }

    // Placeholder implementations for interface completeness
    @Override
    public Page<CustomerDto> getCustomersWithLoanApplications(Pageable pageable) {
        try {
            Page<Customer> customers = customerRepository.findCustomersWithLoanApplications(pageable);
            return customers.map(CustomerDto::fromEntity);
        } catch (Exception e) {
            logger.error("Error fetching customers with loan applications: {}", e.getMessage());
            return Page.empty();
        }
    }

    @Override
    public Page<CustomerDto> getCustomersWithActiveLoans(Pageable pageable) {
        try {
            Page<Customer> customers = customerRepository.findCustomersWithActiveLoanApplications(pageable);
            return customers.map(CustomerDto::fromEntity);
        } catch (Exception e) {
            logger.error("Error fetching customers with active loans: {}", e.getMessage());
            return Page.empty();
        }
    }

    @Override
    public Page<CustomerDto> getCustomersWithoutLoanApplications(Pageable pageable) {
        try {
            Page<Customer> customers = customerRepository.findCustomersWithoutLoanApplications(pageable);
            return customers.map(CustomerDto::fromEntity);
        } catch (Exception e) {
            logger.error("Error fetching customers without loan applications: {}", e.getMessage());
            return Page.empty();
        }
    }

    @Override
    public Map<String, Object> getCustomerLoanHistory(UUID customerId) {
        logger.warn("getCustomerLoanHistory not implemented yet for customer: {}", customerId);
        return new HashMap<>();
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
            logger.error("Error fetching customer count by city from repository: {}", e.getMessage());
            List<Customer> allCustomers = customerRepository.findAll();
            return allCustomers.stream()
                    .filter(customer -> customer.getCity() != null)
                    .collect(Collectors.groupingBy(
                            Customer::getCity,
                            Collectors.counting()));
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
            logger.error("Error fetching customer count by province from repository: {}", e.getMessage());
            List<Customer> allCustomers = customerRepository.findAll();
            return allCustomers.stream()
                    .filter(customer -> customer.getProvince() != null)
                    .collect(Collectors.groupingBy(
                            Customer::getProvince,
                            Collectors.counting()));
        }
    }

    @Override
    public Map<String, Object> getTodayCustomerStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            Object[] todayStats = customerRepository.getTodayCustomerStats();
            if (todayStats != null && todayStats.length > 0) {
                stats.put("newToday", todayStats[0]);
                stats.put("highIncome", todayStats[1]);
                stats.put("withLoans", todayStats[2]);
                stats.put("withoutDocuments", todayStats[3]);
            } else {
                stats.put("newToday", 0L);
                stats.put("highIncome", 0L);
                stats.put("withLoans", 0L);
                stats.put("withoutDocuments", 0L);
            }
        } catch (Exception e) {
            logger.error("Error fetching today customer stats: {}", e.getMessage());
            stats.put("newToday", 0L);
            stats.put("highIncome", 0L);
            stats.put("withLoans", 0L);
            stats.put("withoutDocuments", 0L);
        }
        return stats;
    }

    @Override
    public Page<CustomerDto> getTopCustomersByLoanAmount(Pageable pageable) {
        logger.warn("getTopCustomersByLoanAmount not implemented yet");
        return Page.empty();
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
            logger.error("Error fetching recent customers: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Page<CustomerDto> getInactiveCustomers(Pageable pageable) {
        try {
            LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
            List<Customer> inactiveCustomers = customerRepository.findOldInactiveCustomers(ninetyDaysAgo);
            return createPageFromList(inactiveCustomers, pageable).map(CustomerDto::fromEntity);
        } catch (Exception e) {
            logger.error("Error fetching inactive customers: {}", e.getMessage());
            return Page.empty();
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

    // Additional placeholder methods for interface completeness
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
        logger.warn("determineRiskCategory not fully implemented yet for customer: {}", customerId);
        return "MEDIUM";
    }

    @Override
    public boolean sendNotificationToCustomer(UUID customerId, String subject, String message) {
        logger.warn("sendNotificationToCustomer not implemented yet for customer: {}", customerId);
        return true;
    }

    @Override
    public boolean sendKycReminder(UUID customerId) {
        logger.warn("sendKycReminder not implemented yet for customer: {}", customerId);
        return true;
    }

    @Override
    public int cleanupInactiveCustomers(int inactiveDays) {
        logger.warn("cleanupInactiveCustomers not implemented yet for {} days", inactiveDays);
        return 0;
    }

    @Override
    public List<CustomerDto> getCustomersWithIncompleteInfo() {
        try {
            List<Customer> incompleteCustomers = customerRepository.findCustomersWithIncompleteData();
            return incompleteCustomers.stream()
                    .map(CustomerDto::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching customers with incomplete info: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> validateDataIntegrity() {
        logger.warn("validateDataIntegrity not implemented yet");
        return new HashMap<>();
    }
}