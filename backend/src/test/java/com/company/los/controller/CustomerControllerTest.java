package com.company.los.controller;

import com.company.los.dto.CustomerRequestDto;
import com.company.los.dto.CustomerResponseDto;
import com.company.los.dto.CustomerDto;
import com.company.los.entity.Customer;
import com.company.los.enums.CustomerStatus;
import com.company.los.enums.CustomerType;
import com.company.los.enums.KYCStatus;
import com.company.los.service.CustomerService;
import com.company.los.config.TestSecurityConfig; // ⭐ TestSecurityConfig импортлосон ⭐
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ⭐ ЭЦСИЙН ЗАСВАРЛАСАН CustomerController тест - SECURITY АЛДАА БҮРЭН ЗАСВАРЛАСАН ⭐
 *
 * ЗАСВАРУУД:
 * ✅ @Import(TestSecurityConfig.class) нэмсэн
 * ✅ @WithMockUser аннотацийн 'roles'-ийг 'authorities' болгож, контроллер дээрх @PreAuthorize-тай тааруулсан.
 * ✅ @ActiveProfiles("test") идэвхжүүлсэн
 * ✅ Security context бүрэн тохируулсан
 * ✅ 403 алдаанууд БҮРЭН засварласан
 * ✅ Content-Type-д charset=UTF-8 нэмсэн
 * ✅ updateKycStatus_ShouldUpdateKycStatus тестэд гарсан алдааг зассан.
 *
 * @author LOS Development Team
 * @version 12.0 - FINAL SECURITY COMPLETE FIX
 * @since 2025-08-03
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class) // ⭐ SECURITY CONFIG IMPORT ⭐
@Transactional
@DisplayName("Customer Controller Tests - SECURITY COMPLETE FIXED v12.0")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerDto testCustomerDto;
    private CustomerRequestDto testCustomerRequest;
    private CustomerResponseDto testCustomerResponse;

    @BeforeEach
    void setUp() {
        reset(customerService);
        testCustomerDto = createTestCustomerDto();
        testCustomerRequest = createTestCustomerRequest();
        testCustomerResponse = createTestCustomerResponse();
    }

    @Test
    @DisplayName("⭐ Context loads test - MockMvc available ⭐")
    @WithMockUser(authorities = "customer:view") // ⭐ AUTHORITIES-ээр тохируулсан ⭐
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
        assertThat(objectMapper).isNotNull();
        assertThat(customerService).isNotNull();
    }

    @Test
    @DisplayName("GET /customers - Should return paginated customer list")
    @WithMockUser(authorities = "customer:view") // ⭐ AUTHORITIES-ээр тохируулсан ⭐
    void getAllCustomers_ShouldReturnPaginatedList() throws Exception {
        // Given
        List<CustomerDto> customers = Arrays.asList(testCustomerDto, createAnotherTestCustomerDto());
        Page<CustomerDto> customerPage = new PageImpl<>(customers, PageRequest.of(0, 20), 2);

        when(customerService.getAllCustomers(any(Pageable.class)))
            .thenReturn(customerPage);

        // When & Then
        mockMvc.perform(get("/api/v1/customers")
                .param("page", "0")
                .param("size", "20")
                .param("sort", "lastName")
                .param("direction", "ASC")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                    status().isOk(),
                    content().contentType("application/json;charset=UTF-8"),
                    jsonPath("$.success").value(true),
                    jsonPath("$.data.content").isArray(),
                    jsonPath("$.data.content", hasSize(2)),
                    jsonPath("$.data.totalElements").value(2),
                    jsonPath("$.data.totalPages").value(1),
                    jsonPath("$.data.content[0].firstName").value("Батбаяр"),
                    jsonPath("$.data.content[0].lastName").value("Болд"),
                    jsonPath("$.data.content[0].email").value("batbayar@test.com")
                );

        verify(customerService, times(1)).getAllCustomers(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /customers/{id} - Should return customer by ID")
    @WithMockUser(authorities = "customer:view") // ⭐ AUTHORITIES-ээр тохируулсан ⭐
    void getCustomerById_ShouldReturnCustomer() throws Exception {
        // Given
        UUID customerId = testCustomerDto.getId();
        when(customerService.getCustomerById(customerId)).thenReturn(testCustomerDto);

        // When & Then
        mockMvc.perform(get("/api/v1/customers/{id}", customerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                    status().isOk(),
                    content().contentType("application/json;charset=UTF-8"),
                    jsonPath("$.success").value(true),
                    jsonPath("$.data.id").value(customerId.toString()),
                    jsonPath("$.data.firstName").value("Батбаяр"),
                    jsonPath("$.data.lastName").value("Болд"),
                    jsonPath("$.data.email").value("batbayar@test.com")
                );

        verify(customerService, times(1)).getCustomerById(customerId);
    }

    @Test
    @DisplayName("GET /customers/{id} - Should return 404 for non-existent customer")
    @WithMockUser(authorities = "customer:view") // ⭐ AUTHORITIES-ээр тохируулсан ⭐
    void getCustomerById_ShouldReturn404ForNonExistentCustomer() throws Exception {
        // Given
        UUID customerId = UUID.randomUUID();
        when(customerService.getCustomerById(customerId))
            .thenThrow(new IllegalArgumentException("Customer not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/customers/{id}", customerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                    status().isNotFound(),
                    content().contentType("application/json;charset=UTF-8"),
                    jsonPath("$.success").value(false),
                    jsonPath("$.error").value("Харилцагч олдсонгүй") // ⭐ Зөв мессежийг шалгаж байна ⭐
                );

        verify(customerService, times(1)).getCustomerById(customerId);
    }

    @Test
    @DisplayName("POST /customers - Should create new customer")
    @WithMockUser(authorities = "customer:create") // ⭐ AUTHORITIES-ээр тохируулсан ⭐
    void createCustomer_ShouldCreateNewCustomer() throws Exception {
        // Given
        when(customerService.createCustomer(any(CustomerDto.class))).thenReturn(testCustomerDto);

        // When & Then
        ResultActions result = mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCustomerRequest)))
                .andDo(print());

        result.andExpectAll(
            status().isCreated(),
            content().contentType("application/json;charset=UTF-8"),
            jsonPath("$.success").value(true),
            jsonPath("$.data.firstName").value("Батбаяр"),
            jsonPath("$.data.lastName").value("Болд"),
            jsonPath("$.data.email").value("batbayar@test.com")
        );

        verify(customerService, times(1)).createCustomer(any(CustomerDto.class));
    }

    @Test
    @DisplayName("POST /customers - Should return 400 for invalid data")
    @WithMockUser(authorities = "customer:create") // ⭐ AUTHORITIES-ээр тохируулсан ⭐
    void createCustomer_ShouldReturn400ForInvalidData() throws Exception {
        // Given - Customer with missing required fields
        CustomerRequestDto invalidRequest = new CustomerRequestDto();
        invalidRequest.setFirstName("");
        invalidRequest.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpectAll(
                    status().isBadRequest(),
                    content().contentType("application/json;charset=UTF-8"),
                    jsonPath("$.success").value(false),
                    jsonPath("$.error").exists()
                );

        verify(customerService, never()).createCustomer(any(CustomerDto.class));
    }

    @Test
    @DisplayName("PUT /customers/{id} - Should update existing customer")
    @WithMockUser(authorities = "customer:update") // ⭐ AUTHORITIES-ээр тохируулсан ⭐
    void updateCustomer_ShouldUpdateExistingCustomer() throws Exception {
        // Given
        UUID customerId = testCustomerDto.getId();
        CustomerDto updatedCustomer = createTestCustomerDto();
        updatedCustomer.setFirstName("Updated Name");

        when(customerService.updateCustomer(eq(customerId), any(CustomerDto.class)))
            .thenReturn(updatedCustomer);

        CustomerRequestDto updateRequest = createTestCustomerRequest();
        updateRequest.setFirstName("Updated Name");

        // When & Then
        mockMvc.perform(put("/api/v1/customers/{id}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpectAll(
                    status().isOk(),
                    content().contentType("application/json;charset=UTF-8"),
                    jsonPath("$.success").value(true),
                    jsonPath("$.data.firstName").value("Updated Name")
                );

        verify(customerService, times(1)).updateCustomer(eq(customerId), any(CustomerDto.class));
    }

    @Test
    @DisplayName("DELETE /customers/{id} - Should delete customer")
    @WithMockUser(authorities = "customer:delete") // ⭐ AUTHORITIES-ээр тохируулсан ⭐
    void deleteCustomer_ShouldDeleteCustomer() throws Exception {
        // Given
        UUID customerId = testCustomerDto.getId();
        doNothing().when(customerService).deleteCustomer(customerId);

        // When & Then
        mockMvc.perform(delete("/api/v1/customers/{id}", customerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                    status().isNoContent()
                );

        verify(customerService, times(1)).deleteCustomer(customerId);
    }

    @Test
    @DisplayName("GET /customers/search - Should search customers")
    @WithMockUser(authorities = "customer:view") // ⭐ AUTHORITIES-ээр тохируулсан ⭐
    void searchCustomers_ShouldReturnFilteredResults() throws Exception {
        // Given
        List<CustomerDto> customers = Arrays.asList(testCustomerDto);
        Page<CustomerDto> customerPage = new PageImpl<>(customers, PageRequest.of(0, 20), 1);

        when(customerService.searchCustomers(anyString(), any(Pageable.class)))
            .thenReturn(customerPage);

        // When & Then
        mockMvc.perform(get("/api/v1/customers/search")
                .param("query", "Батбаяр")
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                    status().isOk(),
                    content().contentType("application/json;charset=UTF-8"),
                    jsonPath("$.success").value(true),
                    jsonPath("$.data.content").isArray(),
                    jsonPath("$.data.content", hasSize(1)),
                    jsonPath("$.data.content[0].firstName").value("Батбаяр")
                );

        verify(customerService, times(1)).searchCustomers(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("POST /customers/validate - Should validate customer data")
    @WithMockUser(authorities = "customer:view") // ⭐ AUTHORITIES-ээр тохируулсан ⭐
    void validateCustomer_ShouldReturnValidationResult() throws Exception {
        // Given
        String email = "test@test.com";
        when(customerService.isEmailUnique(email)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/customers/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"))
                .andDo(print())
                .andExpectAll(
                    status().isOk(),
                    content().contentType("application/json;charset=UTF-8"),
                    jsonPath("$.success").value(true)
                );

        verify(customerService, times(1)).isEmailUnique(email);
    }

    @Test
    @DisplayName("PUT /customers/{id}/status - Should update customer status")
    @WithMockUser(authorities = "customer:update") // ⭐ AUTHORITIES-ээр тохируулсан ⭐
    void updateCustomerStatus_ShouldUpdateStatus() throws Exception {
        // Given
        UUID customerId = testCustomerDto.getId();
        CustomerStatus newStatus = CustomerStatus.SUSPENDED;
        CustomerDto updatedCustomer = createTestCustomerDto();
        updatedCustomer.setStatus(newStatus);

        when(customerService.updateCustomerStatus(customerId, newStatus))
            .thenReturn(updatedCustomer);

        // When & Then
        mockMvc.perform(put("/api/v1/customers/{id}/status", customerId)
                .param("status", newStatus.name())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                    status().isOk(),
                    content().contentType("application/json;charset=UTF-8"),
                    jsonPath("$.success").value(true),
                    jsonPath("$.data.status").value(newStatus.name())
                );

        verify(customerService, times(1)).updateCustomerStatus(customerId, newStatus);
    }

    @Test
    @DisplayName("PUT /customers/{id}/kyc-status - Should update KYC status")
    @WithMockUser(authorities = "customer:kyc") // ⭐ AUTHORITIES-ээр тохируулсан ⭐
    void updateKycStatus_ShouldUpdateKycStatus() throws Exception {
        // Given
        UUID customerId = testCustomerDto.getId();
        KYCStatus newKycStatus = KYCStatus.COMPLETED;
        CustomerDto updatedCustomer = createTestCustomerDto();
        updatedCustomer.setKycStatus(Customer.KycStatus.COMPLETED);

        when(customerService.updateKYCStatus(customerId, newKycStatus))
            .thenReturn(updatedCustomer);

        // When & Then
        mockMvc.perform(put("/api/v1/customers/{id}/kyc-status", customerId)
                .param("kycStatus", newKycStatus.name())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                    status().isOk(), // ⭐ 200 OK хүлээж байна ⭐
                    content().contentType("application/json;charset=UTF-8"),
                    jsonPath("$.success").value(true),
                    jsonPath("$.data.kycStatus").value("COMPLETED")
                );

        verify(customerService, times(1)).updateKYCStatus(customerId, newKycStatus);
    }

    @Test
    @DisplayName("GET /customers/statistics - Should return customer statistics")
    @WithMockUser(authorities = "customer:view") // ⭐ AUTHORITIES-ээр тохируулсан ⭐
    void getCustomerStatistics_ShouldReturnStatistics() throws Exception {
        // Given
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCustomers", 100L);
        stats.put("activeCustomers", 80L);

        when(customerService.getCustomerStatistics()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/v1/customers/statistics")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                    status().isOk(),
                    content().contentType("application/json;charset=UTF-8"),
                    jsonPath("$.success").value(true),
                    jsonPath("$.data.totalCustomers").value(100),
                    jsonPath("$.data.activeCustomers").value(80)
                );

        verify(customerService, times(1)).getCustomerStatistics();
    }

    @Test
    @DisplayName("GET /customers/health - Should return health status")
    @WithMockUser(authorities = "customer:view") // ⭐ AUTHORITIES-ээр тохируулсан ⭐
    void healthCheck_ShouldReturnHealthStatus() throws Exception {
        // Given
        when(customerService.getTotalCustomerCount()).thenReturn(150L);

        // When & Then
        mockMvc.perform(get("/api/v1/customers/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                    status().isOk(),
                    content().contentType("application/json;charset=UTF-8"),
                    jsonPath("$.success").value(true),
                    jsonPath("$.data.status").value("UP"),
                    jsonPath("$.data.service").value("CustomerController"),
                    jsonPath("$.data.totalCustomers").value(150)
                );

        verify(customerService, times(1)).getTotalCustomerCount();
    }

    // Helper methods for creating test data
    private CustomerDto createTestCustomerDto() {
        CustomerDto dto = new CustomerDto();
        dto.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        dto.setFirstName("Батбаяр");
        dto.setLastName("Болд");
        dto.setEmail("batbayar@test.com");
        dto.setPhone("99119911");
        dto.setBirthDate(LocalDate.of(1990, 1, 15));
        dto.setRegisterNumber("УБ90011500");
        dto.setCustomerType(Customer.CustomerType.INDIVIDUAL);
        dto.setKycStatus(Customer.KycStatus.COMPLETED);
        dto.setStatus(CustomerStatus.ACTIVE);
        dto.setIsActive(true);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }

    private CustomerDto createAnotherTestCustomerDto() {
        CustomerDto dto = new CustomerDto();
        dto.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"));
        dto.setFirstName("Сарангэрэл");
        dto.setLastName("Батбаяр");
        dto.setEmail("sarangerel@test.com");
        dto.setPhone("88228822");
        dto.setBirthDate(LocalDate.of(1992, 3, 20));
        dto.setRegisterNumber("УБ92032000");
        dto.setCustomerType(Customer.CustomerType.INDIVIDUAL);
        dto.setKycStatus(Customer.KycStatus.IN_PROGRESS);
        dto.setStatus(CustomerStatus.ACTIVE);
        dto.setIsActive(true);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }

    private CustomerRequestDto createTestCustomerRequest() {
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("Батбаяр");
        request.setLastName("Болд");
        request.setEmail("batbayar@test.com");
        request.setPhone("99119911");
        request.setDateOfBirth(LocalDate.of(1990, 1, 15));
        request.setSocialSecurityNumber("УБ90011500");
        request.setCustomerType(CustomerType.INDIVIDUAL);
        request.setPreferredLanguage("mn");
        return request;
    }

    private CustomerResponseDto createTestCustomerResponse() {
        CustomerResponseDto response = new CustomerResponseDto();
        response.setId(testCustomerDto != null ? testCustomerDto.getId() : UUID.randomUUID());
        response.setFirstName("Батбаяр");
        response.setLastName("Болд");
        response.setEmail("batbayar@test.com");
        response.setPhone("99119911");
        response.setStatus(CustomerStatus.ACTIVE);
        response.setKycStatus(KYCStatus.COMPLETED);
        response.setRegistrationDate(LocalDateTime.now());
        response.setLastUpdated(LocalDateTime.now());
        return response;
    }
}