package com.company.los.dto;

import com.company.los.enums.CustomerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * CustomerRequestDto нь хэрэглэгчийн мэдээлэл үүсгэх эсвэл шинэчлэхэд хэрэглэгдэх өгөгдлийг агуулна.
 */
@Data
public class CustomerRequestDto {
    @NotBlank(message = "Эхний нэр заавал байх ёстой.")
    @Size(max = 100, message = "Эхний нэр хамгийн ихдээ 100 тэмдэгт байх ёстой.")
    private String firstName;

    @NotBlank(message = "Эцэг/Эхийн нэр заавал байх ёстой.")
    @Size(max = 100, message = "Эцэг/Эхийн нэр хамгийн ихдээ 100 тэмдэгт байх ёстой.")
    private String lastName;

    @NotBlank(message = "Имэйл хаяг заавал байх ёстой.")
    @Email(message = "Зөв имэйл хаяг оруулна уу.")
    @Size(max = 255, message = "Имэйл хаяг хамгийн ихдээ 255 тэмдэгт байх ёстой.")
    private String email;

    @NotBlank(message = "Утасны дугаар заавал байх ёстой.")
    @Pattern(regexp = "^[0-9]{8}$", message = "Утасны дугаар 8 оронтой тоо байх ёстой.")
    private String phone;

    @NotNull(message = "Төрсөн огноо заавал байх ёстой.")
    @Past(message = "Төрсөн огноо өнгөрсөн огноо байх ёстой.")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Регистрийн дугаар заавал байх ёстой.")
    @Size(max = 10, message = "Регистрийн дугаар хамгийн ихдээ 10 тэмдэгт байх ёстой.")
    private String socialSecurityNumber; // Регистрийн дугаар

    @NotNull(message = "Хэрэглэгчийн төрөл заавал байх ёстой.")
    private CustomerType customerType;

    private String preferredLanguage;
}
