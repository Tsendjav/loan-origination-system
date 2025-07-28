package com.company.los.dto;

import com.company.los.enums.LoanPurpose;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * LoanApplicationRequestDto нь зээлийн өргөдөл үүсгэх эсвэл шинэчлэхэд хэрэглэгдэх өгөгдлийг агуулна.
 */
@Data
public class LoanApplicationRequestDto {
    @NotNull(message = "Хэрэглэгчийн ID заавал байх ёстой.")
    private Long customerId;

    @NotNull(message = "Зээлийн бүтээгдэхүүний ID заавал байх ёстой.")
    private Long loanProductId;

    @NotNull(message = "Хүссэн дүн заавал байх ёстой.")
    @DecimalMin(value = "0.01", message = "Хүссэн дүн 0-ээс их байх ёстой.")
    private BigDecimal requestedAmount;

    @NotNull(message = "Зээлийн хугацаа заавал байх ёстой.")
    @Min(value = 1, message = "Зээлийн хугацаа 1-ээс их байх ёстой.")
    private Integer loanTerm; // Сар

    @NotNull(message = "Зээлийн зориулалт заавал байх ёстой.")
    private LoanPurpose purpose;

    private String notes;
}
