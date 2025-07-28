package com.company.los.service;

import com.company.los.dto.CustomerDto;
import com.company.los.entity.Customer;
import com.company.los.enums.CustomerStatus;
import com.company.los.enums.CustomerType;
import com.company.los.enums.KYCStatus;
import com.company.los.repository.CustomerRepository;
import com.company.los.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerService
 * 
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
    private CustomerDto testCustomerDto;
    private UUID testCustomerId;

    @BeforeEach
    void setUp() {
        testCustomerId = UUID.randomUUID();
        testCustomer = createTestCustomer();
        testCustomerDto = createTestCustomerDto();
    }

    @Test
    @DisplayName("Should get all customers with pagination")
    void getAllCustomers_ShouldReturnPaginatedList() throws Exception {
        // Given
        List<Customer> customers = Arrays.asList(testCustomer, createAnotherTestCustomer());
        Page<Customer> customerPage = new PageImpl<>(customers, PageRequest.of(0, 20), 2);
        
        when(customerRepository.findAll(any(Pageable.class))).thenReturn(customerPage);

        // When
        Page<CustomerDto> result = customerService.getAllCustomers(PageRequest.of(0, 20));

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(customerRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should get customer by ID")
    void getCustomerById_ShouldReturnCustomer() throws Exception {
        // Given
        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));

        // When
        CustomerDto result = customerService.getCustomerById(testCustomerId);

        // Then
        assertNotNull(result);
        assertEquals(testCustomerId, result.getId());
        assertEquals("Батбаяр", result.getFirstName());
        assertEquals("Болд", result.getLastName());
        verify(customerRepository, times(1)).findById(testCustomerId);
    }

    @Test
    @DisplayName("Should throw exception when customer not found")
    void getCustomerById_ShouldThrowExceptionWhenNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(customerRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> customerService.getCustomerById(nonExistentId));
        verify(customerRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should create new customer")
    void createCustomer_ShouldCreateNewCustomer() throws Exception {
        // Given
        Customer newCustomer = createTestCustomer();
        newCustomer.setId(null); // New customer has no ID
        
        Customer savedCustomer = createTestCustomer();
        savedCustomer.setId(testCustomerId);
        
        when(customerRepository.existsByEmail(testCustomerDto.getEmail())).thenReturn(false);
        when(customerRepository.existsByPhone(testCustomerDto.getPhone())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        // When
        CustomerDto result = customerService.createCustomer(testCustomerDto);

        // Then
        assertNotNull(result);
        assertEquals(testCustomerId, result.getId());
        assertEquals("Батбаяр", result.getFirstName());
        assertEquals("Болд", result.getLastName());
        assertEquals("batbayar@test.com", result.getEmail());
        assertTrue(result.getIsActive());
        
        verify(customerRepository, times(1)).existsByEmail(testCustomerDto.getEmail());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void createCustomer_ShouldThrowExceptionWhenEmailExists() throws Exception {
        // Given
        when(customerRepository.existsByEmail(testCustomerDto.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> customerService.createCustomer(testCustomerDto));
        verify(customerRepository, times(1)).existsByEmail(testCustomerDto.getEmail());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should update existing customer")
    void updateCustomer_ShouldUpdateExistingCustomer() throws Exception {
        // Given
        Customer existingCustomer = createTestCustomer();
        existingCustomer.setId(testCustomerId);
        
        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.existsByEmailAndIdNot(testCustomerDto.getEmail(), testCustomerId)).thenReturn(false);
        when(customerRepository.existsByPhoneAndIdNot(testCustomerDto.getPhone(), testCustomerId)).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(existingCustomer);

        // When
        CustomerDto result = customerService.updateCustomer(testCustomerId, testCustomerDto);

        // Then
        assertNotNull(result);
        assertEquals(testCustomerId, result.getId());
        verify(customerRepository, times(1)).findById(testCustomerId);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent customer")
    void updateCustomer_ShouldThrowExceptionWhenNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(customerRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> customerService.updateCustomer(nonExistentId, testCustomerDto));
        verify(customerRepository, times(1)).findById(nonExistentId);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should delete customer")
    void deleteCustomer_ShouldDeleteCustomer() throws Exception {
        // Given
        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));

        // When
        customerService.deleteCustomer(testCustomerId);

        // Then
        verify(customerRepository, times(1)).findById(testCustomerId);
        verify(customerRepository, times(1)).delete(testCustomer);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent customer")
    void deleteCustomer_ShouldThrowExceptionWhenNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(customerRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> customerService.deleteCustomer(nonExistentId));
        verify(customerRepository, times(1)).findById(nonExistentId);
        verify(customerRepository, never()).delete(any(Customer.class));
    }

    @Test
    @DisplayName("Should search customers")
    void searchCustomers_ShouldReturnFilteredResults() throws Exception {
        // Given
        List<Customer> customers = Arrays.asList(testCustomer);
        Page<Customer> customerPage = new PageImpl<>(customers, PageRequest.of(0, 20), 1);
        
        when(customerRepository.findAll(any(Pageable.class))).thenReturn(customerPage);

        // When
        Page<CustomerDto> result = customerService.searchCustomers("Батбаяр", PageRequest.of(0, 20));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Батбаяр", result.getContent().get(0).getFirstName());
        verify(customerRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should check if email is unique - available")
    void isEmailUnique_ShouldReturnTrueWhenEmailAvailable() throws Exception {
        // Given
        String email = "test@test.com";
        when(customerRepository.existsByEmail(email)).thenReturn(false);

        // When
        boolean result = customerService.isEmailUnique(email);

        // Then
        assertTrue(result);
        verify(customerRepository, times(1)).existsByEmail(email);
    }

    @Test
    @DisplayName("Should check if email is unique - not available")
    void isEmailUnique_ShouldReturnFalseWhenEmailTaken() throws Exception {
        // Given
        String email = "test@test.com";
        when(customerRepository.existsByEmail(email)).thenReturn(true);

        // When
        boolean result = customerService.isEmailUnique(email);

        // Then
        assertFalse(result);
        verify(customerRepository, times(1)).existsByEmail(email);
    }

    @Test
    @DisplayName("Should find customer by email")
    void findByEmail_ShouldReturnCustomer() throws Exception {
        // Given
        String email = "batbayar@test.com";
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(testCustomer));

        // When
        Optional<CustomerDto> result = customerService.findByEmail(email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        verify(customerRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should return empty when customer not found by email")
    void findByEmail_ShouldReturnEmptyWhenNotFound() throws Exception {
        // Given
        String email = "nonexistent@test.com";
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<CustomerDto> result = customerService.findByEmail(email);

        // Then
        assertFalse(result.isPresent());
        verify(customerRepository, times(1)).findByEmail(email);
    }

    // Helper methods for creating test data
    private Customer createTestCustomer() {
        Customer customer = new Customer();
        customer.setId(testCustomerId);
        customer.setFirstName("Батбаяр");
        customer.setLastName("Болд");
        customer.setEmail("batbayar@test.com");
        customer.setPhone("99119911");
        customer.setDateOfBirth(LocalDate.of(1990, 1, 15));
        customer.setRegisterNumber("УБ90011500");
        customer.setCustomerType(Customer.CustomerType.INDIVIDUAL);
        customer.setKycStatus(Customer.KycStatus.COMPLETED);
        customer.setIsActive(true);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        return customer;
    }

    private Customer createAnotherTestCustomer() {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setFirstName("Сарангэрэл");
        customer.setLastName("Батбаяр");
        customer.setEmail("sarangerel@test.com");
        customer.setPhone("88228822");
        customer.setDateOfBirth(LocalDate.of(1992, 3, 20));
        customer.setRegisterNumber("УБ92032000");
        customer.setCustomerType(Customer.CustomerType.INDIVIDUAL);
        customer.setKycStatus(Customer.KycStatus.IN_PROGRESS);
        customer.setIsActive(true);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        return customer;
    }

    private CustomerDto createTestCustomerDto() {
        CustomerDto dto = new CustomerDto();
        dto.setId(testCustomerId);
        dto.setFirstName("Батбаяр");
        dto.setLastName("Болд");
        dto.setEmail("batbayar@test.com");
        dto.setPhone("99119911");
        dto.setBirthDate(LocalDate.of(1990, 1, 15));
        dto.setRegisterNumber("УБ90011500");
        dto.setCustomerType(Customer.CustomerType.INDIVIDUAL);
        dto.setKycStatus(Customer.KycStatus.COMPLETED);
        dto.setIsActive(true);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }
}