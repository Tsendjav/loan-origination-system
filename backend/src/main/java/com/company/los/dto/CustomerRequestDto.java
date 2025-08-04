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
 * ⭐ ЗАСВАРЛАСАН - GETTER METHODS НЭМЭГДСЭН ⭐
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

    // ⭐ MANUAL GETTER METHODS - Lombok @Data-аас гадна нэмэлт ⭐
    // Эдгээр методууд нь CustomerController-т ашиглагдаж байгаа

    /**
     * Эхний нэр авах
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Овог авах
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * И-мэйл авах
     */
    public String getEmail() {
        return email;
    }

    /**
     * Утасны дугаар авах
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Төрсөн огноо авах
     */
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Регистрийн дугаар авах
     */
    public String getSocialSecurityNumber() {
        return socialSecurityNumber;
    }

    /**
     * Хэрэглэгчийн төрөл авах
     */
    public CustomerType getCustomerType() {
        return customerType;
    }

    /**
     * Сонгосон хэл авах
     */
    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    // ⭐ MANUAL SETTER METHODS - Lombok @Data-аас гадна нэмэлт ⭐

    /**
     * Эхний нэр тохируулах
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Овог тохируулах
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * И-мэйл тохируулах
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Утасны дугаар тохируулах
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Төрсөн огноо тохируулах
     */
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Регистрийн дугаар тохируулах
     */
    public void setSocialSecurityNumber(String socialSecurityNumber) {
        this.socialSecurityNumber = socialSecurityNumber;
    }

    /**
     * Хэрэглэгчийн төрөл тохируулах
     */
    public void setCustomerType(CustomerType customerType) {
        this.customerType = customerType;
    }

    /**
     * Сонгосон хэл тохируулах
     */
    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    // ⭐ VALIDATION HELPER METHODS ⭐

    /**
     * Бүх шаардлагатай талбарууд бөглөгдсөн эсэхийг шалгах
     */
    public boolean hasRequiredFields() {
        return firstName != null && !firstName.trim().isEmpty() &&
               lastName != null && !lastName.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               phone != null && !phone.trim().isEmpty() &&
               dateOfBirth != null &&
               socialSecurityNumber != null && !socialSecurityNumber.trim().isEmpty() &&
               customerType != null;
    }

    /**
     * И-мэйлийн формат зөв эсэхийг шалгах
     */
    public boolean isValidEmail() {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    /**
     * Утасны дугаарын формат зөв эсэхийг шалгах
     */
    public boolean isValidPhone() {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return phone.matches("^[0-9]{8}$");
    }

    /**
     * Регистрийн дугаарын формат зөв эсэхийг шалгах
     */
    public boolean isValidSocialSecurityNumber() {
        if (socialSecurityNumber == null || socialSecurityNumber.trim().isEmpty()) {
            return false;
        }
        // Регистрийн дугаар 10 тэмдэгт эсвэл түүнээс бага байх ёстой
        return socialSecurityNumber.trim().length() <= 10;
    }

    /**
     * Насыг тооцоолох (жилээр)
     */
    public int calculateAge() {
        if (dateOfBirth == null) {
            return 0;
        }
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    /**
     * Хэрэглэгчийн бүрэн нэрийг авах
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return "";
        }
        
        StringBuilder fullName = new StringBuilder();
        if (lastName != null && !lastName.trim().isEmpty()) {
            fullName.append(lastName.trim());
        }
        if (firstName != null && !firstName.trim().isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(firstName.trim());
        }
        
        return fullName.toString();
    }

    /**
     * DTO-г String болгож хөрвүүлэх
     */
    @Override
    public String toString() {
        return "CustomerRequestDto{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", socialSecurityNumber='" + socialSecurityNumber + '\'' +
                ", customerType=" + customerType +
                ", preferredLanguage='" + preferredLanguage + '\'' +
                '}';
    }
}