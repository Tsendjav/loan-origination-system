package com.company.los.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.company.los.entity.User;
import jakarta.validation.constraints.*;

import java.util.UUID;

/**
 * Хэрэглэгч үүсгэх хүсэлтийн DTO
 * Create User Request Data Transfer Object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateUserRequestDto {

    @NotBlank(message = "Хэрэглэгчийн нэр заавал бөглөх ёстой")
    @Size(min = 3, max = 100, message = "Хэрэглэгчийн нэр 3-100 тэмдэгт байх ёстой")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Хэрэглэгчийн нэрэнд зөвхөн үсэг, тоо, цэг, доор зураас ашиглана уу")
    private String username;

    @NotBlank(message = "Нууц үг заавал бөглөх ёстой")
    @Size(min = 8, max = 100, message = "Нууц үг 8-100 тэмдэгт байх ёстой")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", 
             message = "Нууц үг дор хаяж 1 жижиг үсэг, 1 том үсэг, 1 тоо, 1 тусгай тэмдэгт агуулах ёстой")
    private String password;

    @NotBlank(message = "Нууц үг давтах заавал бөглөх ёстой")
    private String confirmPassword;

    @NotBlank(message = "И-мэйл заавал бөглөх ёстой")
    @Email(message = "И-мэйлийн формат буруу")
    @Size(max = 255, message = "И-мэйл 255 тэмдэгтээс ихгүй байх ёстой")
    private String email;

    @NotBlank(message = "Нэр заавал бөглөх ёстой")
    @Size(max = 100, message = "Нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String firstName;

    @NotBlank(message = "Овог заавал бөглөх ёстой")
    @Size(max = 100, message = "Овог 100 тэмдэгтээс ихгүй байх ёстой")
    private String lastName;

    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Утасны дугаарын формат буруу")
    @Size(max = 20, message = "Утасны дугаар 20 тэмдэгтээс ихгүй байх ёстой")
    private String phone;

    @Size(max = 50, message = "Ажилтны дугаар 50 тэмдэгтээс ихгүй байх ёстой")
    private String employeeId;

    @Size(max = 100, message = "Албан тушаал 100 тэмдэгтээс ихгүй байх ёстой")
    private String position;

    @Size(max = 100, message = "Хэлтэс 100 тэмдэгтээс ихгүй байх ёстой")
    private String department;

    // Manager ID - UUID format to match User entity
    private UUID managerId;

    // User status - optional, defaults to PENDING_ACTIVATION
    private User.UserStatus status;

    // Profile fields
    @Size(max = 10, message = "Хэл 10 тэмдэгтээс ихгүй байх ёстой")
    private String language;

    @Size(max = 50, message = "Цагийн бүс 50 тэмдэгтээс ихгүй байх ёстой")
    private String timezone;

    @Size(max = 500, message = "Профайл зургийн URL 500 тэмдэгтээс ихгүй байх ёстой")
    private String profilePictureUrl;

    // Email verification settings
    private Boolean sendEmailVerification;
    private Boolean requireEmailVerification;

    // Password settings
    private Boolean requirePasswordChange;
    private Integer passwordExpiryDays;

    // Account activation
    private Boolean activateImmediately;
    private Boolean sendWelcomeEmail;

    // Role assignment - role IDs to assign
    private java.util.Set<UUID> roleIds;

    // Additional notes for the new user
    @Size(max = 500, message = "Тэмдэглэл 500 тэмдэгтээс ихгүй байх ёстой")
    private String notes;

    // Constructors
    public CreateUserRequestDto() {
        // Set default values
        this.language = "mn";
        this.timezone = "Asia/Ulaanbaatar";
        this.status = User.UserStatus.PENDING_ACTIVATION;
        this.sendEmailVerification = true;
        this.requireEmailVerification = true;
        this.requirePasswordChange = false;
        this.passwordExpiryDays = 90;
        this.activateImmediately = false;
        this.sendWelcomeEmail = true;
    }

    public CreateUserRequestDto(String username, String email, String password, String firstName, String lastName) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Validation methods
    public boolean isValidRequest() {
        return username != null && !username.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               password != null && !password.trim().isEmpty() &&
               firstName != null && !firstName.trim().isEmpty() &&
               lastName != null && !lastName.trim().isEmpty() &&
               isValidEmail(email) &&
               isValidUsername(username) &&
               passwordsMatch();
    }

    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }

    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    public boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        String usernameRegex = "^[a-zA-Z0-9._-]{3,100}$";
        return username.matches(usernameRegex);
    }

    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        // Check for at least one lowercase, uppercase, digit, and special character
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }

    public boolean hasWorkInfo() {
        return (employeeId != null && !employeeId.trim().isEmpty()) ||
               (position != null && !position.trim().isEmpty()) ||
               (department != null && !department.trim().isEmpty());
    }

    public boolean hasManagerAssigned() {
        return managerId != null;
    }

    public boolean hasRolesAssigned() {
        return roleIds != null && !roleIds.isEmpty();
    }

    // Business logic methods
    public String getFullName() {
        return lastName + " " + firstName;
    }

    public String getDisplayName() {
        String fullName = getFullName().trim();
        return !fullName.isEmpty() ? fullName : username;
    }

    public String getFormattedPhone() {
        if (phone == null || phone.trim().isEmpty()) {
            return "";
        }
        String cleanPhone = phone.replaceAll("[^0-9+]", "");
        if (cleanPhone.startsWith("+976")) {
            return cleanPhone.substring(0, 4) + " " + cleanPhone.substring(4, 8) + " " + cleanPhone.substring(8);
        } else if (cleanPhone.length() == 8) {
            return cleanPhone.substring(0, 4) + " " + cleanPhone.substring(4);
        }
        return cleanPhone;
    }

    // Convert to UserDto for service layer
    public UserDto toUserDto() {
        UserDto userDto = new UserDto();
        userDto.setUsername(this.username);
        userDto.setPassword(this.password);
        userDto.setEmail(this.email);
        userDto.setFirstName(this.firstName);
        userDto.setLastName(this.lastName);
        userDto.setPhone(this.phone);
        userDto.setEmployeeId(this.employeeId);
        userDto.setPosition(this.position);
        userDto.setDepartment(this.department);
        userDto.setManagerId(this.managerId);
        userDto.setStatus(this.status != null ? this.status : User.UserStatus.PENDING_ACTIVATION);
        userDto.setLanguage(this.language != null ? this.language : "mn");
        userDto.setTimezone(this.timezone != null ? this.timezone : "Asia/Ulaanbaatar");
        userDto.setProfilePictureUrl(this.profilePictureUrl);
        
        // Set account properties based on request settings
        userDto.setIsEmailVerified(!Boolean.TRUE.equals(this.requireEmailVerification));
        userDto.setIsLocked(false);
        userDto.setFailedLoginAttempts(0);
        userDto.setTwoFactorEnabled(false);
        userDto.setIsActive(Boolean.TRUE.equals(this.activateImmediately));
        userDto.setIsDeleted(false);
        
        return userDto;
    }

    // Validation error messages
    public String getValidationSummary() {
        StringBuilder sb = new StringBuilder();
        
        if (username == null || username.trim().isEmpty()) {
            sb.append("• Хэрэглэгчийн нэр хоосон\n");
        } else if (!isValidUsername(username)) {
            sb.append("• Хэрэглэгчийн нэрийн формат буруу\n");
        }
        
        if (email == null || email.trim().isEmpty()) {
            sb.append("• И-мэйл хоосон\n");
        } else if (!isValidEmail(email)) {
            sb.append("• И-мэйлийн формат буруу\n");
        }
        
        if (password == null || password.trim().isEmpty()) {
            sb.append("• Нууц үг хоосон\n");
        } else if (!isValidPassword(password)) {
            sb.append("• Нууц үгийн формат буруу\n");
        }
        
        if (!passwordsMatch()) {
            sb.append("• Нууц үг тохирохгүй байна\n");
        }
        
        if (firstName == null || firstName.trim().isEmpty()) {
            sb.append("• Нэр хоосон\n");
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            sb.append("• Овог хоосон\n");
        }
        
        return sb.toString();
    }

    public boolean hasValidationErrors() {
        return !isValidRequest();
    }

    // Security risk assessment
    public String getSecurityRiskAssessment() {
        StringBuilder risks = new StringBuilder();
        
        if (Boolean.TRUE.equals(activateImmediately)) {
            risks.append("• Шууд идэвхжүүлэх хүсэлт\n");
        }
        
        if (!Boolean.TRUE.equals(requireEmailVerification)) {
            risks.append("• И-мэйл баталгаажуулахгүй байх\n");
        }
        
        if (!Boolean.TRUE.equals(requirePasswordChange)) {
            risks.append("• Эхний нэвтрэхэд нууц үг солихгүй\n");
        }
        
        if (hasRolesAssigned()) {
            risks.append("• Анхнаасаа дүр олгох\n");
        }
        
        if (passwordExpiryDays != null && passwordExpiryDays > 180) {
            risks.append("• Урт хугацааны нууц үг\n");
        }
        
        return risks.toString();
    }

    // Helper methods for user creation workflow
    public boolean shouldSendEmailVerification() {
        return Boolean.TRUE.equals(sendEmailVerification);
    }

    public boolean shouldSendWelcomeEmail() {
        return Boolean.TRUE.equals(sendWelcomeEmail);
    }

    public boolean shouldActivateImmediately() {
        return Boolean.TRUE.equals(activateImmediately);
    }

    public boolean shouldRequirePasswordChange() {
        return Boolean.TRUE.equals(requirePasswordChange);
    }

    public int getPasswordExpiryDaysOrDefault() {
        return passwordExpiryDays != null ? passwordExpiryDays : 90;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public UUID getManagerId() { return managerId; }
    public void setManagerId(UUID managerId) { this.managerId = managerId; }

    public User.UserStatus getStatus() { return status; }
    public void setStatus(User.UserStatus status) { this.status = status; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public Boolean getSendEmailVerification() { return sendEmailVerification; }
    public void setSendEmailVerification(Boolean sendEmailVerification) { this.sendEmailVerification = sendEmailVerification; }

    public Boolean getRequireEmailVerification() { return requireEmailVerification; }
    public void setRequireEmailVerification(Boolean requireEmailVerification) { this.requireEmailVerification = requireEmailVerification; }

    public Boolean getRequirePasswordChange() { return requirePasswordChange; }
    public void setRequirePasswordChange(Boolean requirePasswordChange) { this.requirePasswordChange = requirePasswordChange; }

    public Integer getPasswordExpiryDays() { return passwordExpiryDays; }
    public void setPasswordExpiryDays(Integer passwordExpiryDays) { this.passwordExpiryDays = passwordExpiryDays; }

    public Boolean getActivateImmediately() { return activateImmediately; }
    public void setActivateImmediately(Boolean activateImmediately) { this.activateImmediately = activateImmediately; }

    public Boolean getSendWelcomeEmail() { return sendWelcomeEmail; }
    public void setSendWelcomeEmail(Boolean sendWelcomeEmail) { this.sendWelcomeEmail = sendWelcomeEmail; }

    public java.util.Set<UUID> getRoleIds() { return roleIds; }
    public void setRoleIds(java.util.Set<UUID> roleIds) { this.roleIds = roleIds; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "CreateUserRequestDto{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", position='" + position + '\'' +
                ", department='" + department + '\'' +
                ", managerId='" + managerId + '\'' +
                ", status=" + status +
                ", language='" + language + '\'' +
                ", timezone='" + timezone + '\'' +
                ", activateImmediately=" + activateImmediately +
                ", roleIds=" + roleIds +
                '}';
    }
}