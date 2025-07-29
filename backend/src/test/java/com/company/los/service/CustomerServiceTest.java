package com.company.los.service;

import com.company.los.dto.CustomerDto; 
import com.company.los.entity.Customer;
import com.company.los.entity.Customer.CustomerType; // Customer-ийн дотоод CustomerType-г ашиглах
import com.company.los.entity.Customer.KycStatus; // Customer-ийн дотоод KycStatus-г ашиглах
import com.company.los.enums.CustomerStatus; // Энэ enum нь Customer.java-д зааснаар гадаад хэвээр байна
import com.company.los.exception.CustomerNotFoundException;
import com.company.los.exception.DuplicateEmailException;
import com.company.los.repository.CustomerRepository;
import com.company.los.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension; // MockitoExtension import нэмэх
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID; // UUID-г импортлох
import java.util.Map; // Map-г импортлох

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerService
 * @author LOS Development Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Service Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer testCustomer;
    private Customer anotherTestCustomer;

    // Fixed UUIDs for consistent testing
    private final UUID TEST_CUSTOMER_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
    private final UUID ANOTHER_TEST_CUSTOMER_ID = UUID.fromString("b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22");
    private final UUID NON_EXISTENT_CUSTOMER_ID = UUID.fromString("c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33");

    @BeforeEach
    void setUp() {
        testCustomer = createTestCustomer(TEST_CUSTOMER_ID);
        anotherTestCustomer = createAnotherTestCustomer(ANOTHER_TEST_CUSTOMER_ID);
    }

    @Test
    @DisplayName("Should get all customers with pagination")
    void getAllCustomers_ShouldReturnPaginatedResults() {
        // Given
        List<Customer> customers = Arrays.asList(testCustomer, anotherTestCustomer);
        
        // Mock the findAll method of customerRepository
        when(customerRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(customers)); // Repository нь Customer-г буцаана

        // When
        Pageable pageable = PageRequest.of(0, 20, Sort.by("lastName").ascending());
        // getAllCustomers нь Page<CustomerDto> буцаах ёстой
        Page<CustomerDto> result = customerService.getAllCustomers(pageable); 

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        // CustomerDto-г ашиглан шалгах
        assertTrue(result.getContent().stream().anyMatch(dto -> dto.getId().equals(testCustomer.getId())));
        assertTrue(result.getContent().stream().anyMatch(dto -> dto.getId().equals(anotherTestCustomer.getId())));

        // Verify that findAll was called with any Pageable
        verify(customerRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should get customer by ID successfully")
    void getCustomerById_ShouldReturnCustomer_WhenCustomerExists() {
        // Given
        UUID customerId = TEST_CUSTOMER_ID; // Use UUID
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));

        // When
        // getCustomerById нь CustomerDto буцаах ёстой
        CustomerDto result = customerService.getCustomerById(customerId);

        // Then
        assertNotNull(result);
        assertEquals(testCustomer.getId(), result.getId());
        assertEquals(testCustomer.getFirstName(), result.getFirstName());
        assertEquals(testCustomer.getLastName(), result.getLastName());
        assertEquals(testCustomer.getEmail(), result.getEmail());

        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    @DisplayName("Should throw exception when customer not found")
    void getCustomerById_ShouldThrowException_WhenCustomerNotFound() {
        // Given
        UUID customerId = NON_EXISTENT_CUSTOMER_ID; // Use UUID
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> customerService.getCustomerById(customerId)
        );

        assertTrue(exception.getMessage().contains("Харилцагч олдсонгүй"));
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    @DisplayName("Should create customer successfully")
    void createCustomer_ShouldCreateCustomer_WhenValidData() {
        // Given
        Customer newCustomerEntity = createTestCustomer(null); // New customer entity
        CustomerDto newCustomerDto = CustomerDto.fromEntity(newCustomerEntity); // Convert to DTO for service input
        
        when(customerRepository.existsByEmail(newCustomerDto.getEmail())).thenReturn(false);
        when(customerRepository.existsByPhone(newCustomerDto.getPhone())).thenReturn(false);
        when(customerRepository.existsByRegisterNumber(newCustomerDto.getRegisterNumber()))
            .thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(newCustomerEntity); // Repository нь Entity-г хадгална

        // When
        CustomerDto result = customerService.createCustomer(newCustomerDto);

        // Then
        assertNotNull(result);
        assertEquals(testCustomer.getId(), result.getId()); // testCustomer-ийн ID-тай таарах ёстой
        assertEquals(CustomerStatus.PENDING_VERIFICATION, result.getStatus()); // DTO-д CustomerStatus талбар нэмэгдсэн
        assertEquals(KycStatus.PENDING, result.getKycStatus()); // Customer.KycStatus-г ашиглах
        assertNotNull(result.getCreatedAt()); // DTO-ийн createdAt-г шалгах

        verify(customerRepository, times(1)).existsByEmail(newCustomerDto.getEmail());
        verify(customerRepository, times(1)).existsByPhone(newCustomerDto.getPhone());
        verify(customerRepository, times(1)).existsByRegisterNumber(newCustomerDto.getRegisterNumber());
        verify(customerRepository, times(1)).save(any(Customer.class)); // Service нь DTO-г Entity болгож хадгална
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void createCustomer_ShouldThrowException_WhenEmailExists() {
        // Given
        Customer newCustomerEntity = createTestCustomer(null);
        CustomerDto newCustomerDto = CustomerDto.fromEntity(newCustomerEntity);
        
        when(customerRepository.existsByEmail(newCustomerDto.getEmail())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> customerService.createCustomer(newCustomerDto) // DTO-г дамжуулах
        );

        assertTrue(exception.getMessage().contains("И-мэйл аль хэдийн байна"));
        verify(customerRepository, times(1)).existsByEmail(newCustomerDto.getEmail());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should update customer successfully")
    void updateCustomer_ShouldUpdateCustomer_WhenValidData() {
        // Given
        UUID customerId = TEST_CUSTOMER_ID; // Use UUID
        Customer existingCustomer = createTestCustomer(customerId);
        CustomerDto updateDataDto = CustomerDto.fromEntity(createTestCustomer(customerId)); // Update data-г DTO болгох
        updateDataDto.setFirstName("Updated Name");
        updateDataDto.setPhone("77777777");
        updateDataDto.setEmail("updated@test.com"); // Ensure email is different or handled

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(existingCustomer); // Repository нь Entity-г хадгална

        // When
        CustomerDto result = customerService.updateCustomer(customerId, updateDataDto);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getFirstName()); // DTO-ийн утгыг шалгах
        assertEquals("77777777", result.getPhone()); // DTO-ийн утгыг шалгах
        assertNotNull(result.getUpdatedAt()); // DTO-ийн updatedAt-г шалгах

        verify(customerRepository, times(1)).findById(customerId);
        verify(customerRepository, times(1)).save(existingCustomer);
    }

    @Test
    @DisplayName("Should delete customer successfully")
    void deleteCustomer_ShouldDeleteCustomer_WhenCustomerExists() {
        // Given
        UUID customerId = TEST_CUSTOMER_ID; // Use UUID
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        doNothing().when(customerRepository).delete(testCustomer);

        // When
        customerService.deleteCustomer(customerId);

        // Then
        verify(customerRepository, times(1)).findById(customerId);
        verify(customerRepository, times(1)).delete(testCustomer);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent customer")
    void deleteCustomer_ShouldThrowException_WhenCustomerNotFound() {
        // Given
        UUID customerId = NON_EXISTENT_CUSTOMER_ID; // Use UUID
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> customerService.deleteCustomer(customerId)
        );

        assertTrue(exception.getMessage().contains("Харилцагч олдсонгүй"));
        verify(customerRepository, times(1)).findById(customerId);
        verify(customerRepository, never()).delete(any(Customer.class));
    }

    @Test
    @DisplayName("Should search customers by query")
    void searchCustomers_ShouldReturnFilteredResults() {
        // Given
        String searchQuery = "Батбаяр";
        List<Customer> customers = Arrays.asList(testCustomer);
        
        when(customerRepository.findBySearchTerm(eq(searchQuery), any(Pageable.class)))
            .thenReturn(new PageImpl<>(customers)); // Repository нь Customer-г буцаана

        // When
        Pageable pageable = PageRequest.of(0, 20);
        Page<CustomerDto> result = customerService.searchCustomers(searchQuery, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testCustomer.getId(), result.getContent().get(0).getId()); // DTO-ийн ID-г шалгах

        verify(customerRepository, times(1)).findBySearchTerm(eq(searchQuery), any(Pageable.class));
    }

    @Test
    @DisplayName("Should check if email is available")
    void isEmailAvailable_ShouldReturnTrue_WhenEmailNotExists() {
        // Given
        String email = "newuser@test.com";
        when(customerRepository.existsByEmail(email)).thenReturn(false);

        // When
        boolean result = customerService.isEmailAvailable(email);

        // Then
        assertTrue(result);
        verify(customerRepository, times(1)).existsByEmail(email);
    }

    @Test
    @DisplayName("Should check if email is not available")
    void isEmailAvailable_ShouldReturnFalse_WhenEmailExists() {
        // Given
        String email = "existing@test.com";
        when(customerRepository.existsByEmail(email)).thenReturn(true);

        // When
        boolean result = customerService.isEmailAvailable(email);

        // Then
        assertFalse(result);
        verify(customerRepository, times(1)).existsByEmail(email);
    }

    @Test
    @DisplayName("Should get customer by email")
    void getCustomerByEmail_ShouldReturnCustomer_WhenEmailExists() {
        // Given
        String email = "batbayar@test.com";
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(testCustomer));

        // When - service method returns CustomerDto, not Optional
        CustomerDto result = customerService.getCustomerByEmail(email);

        // Then
        assertNotNull(result);
        assertEquals(testCustomer.getId(), result.getId());
        verify(customerRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should return Optional when using findByEmail")
    void findByEmail_ShouldReturnOptional_WhenEmailExists() {
        // Given
        String email = "batbayar@test.com";
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(testCustomer));

        // When - findByEmail method returns Optional<CustomerDto>
        Optional<CustomerDto> result = customerService.findByEmail(email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testCustomer.getId(), result.get().getId());
        verify(customerRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should return empty when customer not found by email using findByEmail")
    void findByEmail_ShouldReturnEmpty_WhenEmailNotExists() {
        // Given
        String email = "nonexistent@test.com";
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When - findByEmail method returns Optional<CustomerDto>
        Optional<CustomerDto> result = customerService.findByEmail(email);

        // Then
        assertFalse(result.isPresent());
        verify(customerRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should update customer status")
    void updateCustomerStatus_ShouldUpdateStatus() {
        // Given
        UUID customerId = TEST_CUSTOMER_ID; // Use UUID
        CustomerStatus newStatus = CustomerStatus.SUSPENDED;
        
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        CustomerDto result = customerService.updateCustomerStatus(customerId, newStatus);

        // Then
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus()); // DTO-ийн статусыг шалгах (CustomerDto-д getStatus() нэмэгдсэн)
        assertNotNull(result.getUpdatedAt()); // DTO-ийн updatedAt-г шалгах

        verify(customerRepository, times(1)).findById(customerId);
        verify(customerRepository, times(1)).save(testCustomer);
    }

    @Test
    @DisplayName("Should update KYC status")
    void updateKYCStatus_ShouldUpdateKYCStatus() {
        // Given
        UUID customerId = TEST_CUSTOMER_ID; // Use UUID
        KycStatus newKYCStatus = KycStatus.COMPLETED; // Use Customer.KycStatus
        
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When - call service method directly without additional mocking
        CustomerDto result = customerService.updateKYCStatus(customerId, 
            com.company.los.enums.KYCStatus.COMPLETED); // Use external enum parameter

        // Then
        assertNotNull(result);
        // KycStatus comparison using enum values
        assertEquals(newKYCStatus, result.getKycStatus()); 
        assertNotNull(result.getKycCompletedAt()); // DTO-ийн kycCompletedAt-г шалгах

        verify(customerRepository, times(1)).findById(customerId);
        verify(customerRepository, times(1)).save(testCustomer);
    }

    @Test
    @DisplayName("Should get customer statistics")
    void getCustomerStatistics_ShouldReturnStatistics() {
        // Given
        when(customerRepository.count()).thenReturn(100L);
        when(customerRepository.findAll()).thenReturn(Arrays.asList(testCustomer, anotherTestCustomer));

        // Mock the Object[] returned by getTodayCustomerStats
        Object[] todayStatsArray = new Object[]{10L, 5000000L, 5L, 2L}; // Example values
        when(customerRepository.getTodayCustomerStats()).thenReturn(todayStatsArray);

        // When
        Map<String, Object> stats = customerService.getCustomerStatistics();

        // Then
        assertNotNull(stats);
        // Map-ийн утгуудыг шууд хандаж шалгах
        assertEquals(100L, stats.get("totalCustomers")); 
        assertNotNull(stats.get("byType"));
        assertNotNull(stats.get("byKycStatus"));

        verify(customerRepository, times(1)).count();
        verify(customerRepository, times(1)).getTodayCustomerStats();
    }

    @Test
    @DisplayName("Should validate customer data")
    void validateCustomerData_ShouldReturnValidationErrors() {
        // Given
        Customer invalidCustomerEntity = new Customer();
        invalidCustomerEntity.setFirstName(""); // Empty name
        invalidCustomerEntity.setEmail("invalid-email"); // Invalid email
        invalidCustomerEntity.setRegisterNumber(""); // Empty register number
        invalidCustomerEntity.setPhone(""); // Empty phone
        invalidCustomerEntity.setCustomerType(CustomerType.INDIVIDUAL); // Set a type
        
        CustomerDto invalidCustomerDto = CustomerDto.fromEntity(invalidCustomerEntity);

        // When - call real service method
        boolean isValid = customerService.validateCustomerData(invalidCustomerDto);

        // Then
        assertFalse(isValid); // Алдаатай тул false байх ёстой
    }

    @Test
    @DisplayName("Should return no validation errors for valid customer")
    void validateCustomerData_ShouldReturnNoErrors_WhenCustomerValid() {
        // Given
        Customer validCustomerEntity = createTestCustomer(TEST_CUSTOMER_ID);
        CustomerDto validCustomerDto = CustomerDto.fromEntity(validCustomerEntity);

        // When - call real service method
        boolean isValid = customerService.validateCustomerData(validCustomerDto);

        // Then
        assertTrue(isValid); // Зөв тул true байх ёстой
    }

    // Helper methods for creating test data
    // Updated to accept UUID for ID
    private Customer createTestCustomer(UUID id) {
        Customer customer = new Customer();
        customer.setId(id); // Set UUID
        customer.setFirstName("Батбаяр");
        customer.setLastName("Болд");
        customer.setEmail("batbayar@test.com");
        customer.setPhone("99119911");
        customer.setBirthDate(LocalDate.of(1990, 1, 15));
        customer.setRegisterNumber("УБ90011500"); // Changed from setSocialSecurityNumber
        customer.setCustomerType(CustomerType.INDIVIDUAL); // Use Customer.CustomerType
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setKycStatus(KycStatus.COMPLETED); // Use Customer.KycStatus
        customer.setIsActive(true);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        return customer;
    }

    // Updated to accept UUID for ID
    private Customer createAnotherTestCustomer(UUID id) {
        Customer customer = new Customer();
        customer.setId(id); // Set UUID
        customer.setFirstName("Сарангэрэл");
        customer.setLastName("Батбаяр");
        customer.setEmail("sarangerel@test.com");
        customer.setPhone("88228822");
        customer.setBirthDate(LocalDate.of(1992, 3, 20));
        customer.setRegisterNumber("УБ92032000"); // Changed from setSocialSecurityNumber
        customer.setCustomerType(CustomerType.INDIVIDUAL); // Use Customer.CustomerType
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setKycStatus(KycStatus.IN_PROGRESS); // Use Customer.KycStatus
        customer.setIsActive(true);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        return customer;
    }
}