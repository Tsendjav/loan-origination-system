package com.company.los.service.impl;

import com.company.los.dto.CreateUserRequestDto;
import com.company.los.dto.UserDto;
import com.company.los.entity.Role;
import com.company.los.entity.User;
import com.company.los.repository.RoleRepository;
import com.company.los.repository.UserRepository;
import com.company.los.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Хэрэглэгчийн Service Implementation
 * User Service Implementation with Spring Security UserDetailsService
 * ⭐ ЗАСВАРЛАСАН - setPassword алдаа засварлагдсан ⭐
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Spring Security UserDetailsService implementation
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user by username: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Хэрэглэгч олдсонгүй: " + username));
        
        return user;
    }

    // CRUD операциуд
    @Override
    public UserDto createUser(CreateUserRequestDto createRequest) {
        logger.info("Creating new user with username: {}", createRequest.getUsername());
        
        if (createRequest.getUsername() == null || createRequest.getUsername().isEmpty() ||
            createRequest.getEmail() == null || createRequest.getEmail().isEmpty() ||
            createRequest.getPassword() == null || createRequest.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Хэрэглэгчийн мэдээлэл дутуу эсвэл буруу байна");
        }
        
        if (existsByUsername(createRequest.getUsername())) {
            throw new IllegalArgumentException("Хэрэглэгчийн нэр аль хэдийн байна: " + createRequest.getUsername());
        }
        
        if (existsByEmail(createRequest.getEmail())) {
            throw new IllegalArgumentException("И-мэйл аль хэдийн байна: " + createRequest.getEmail());
        }
        
        User user = new User();
        user.setUsername(createRequest.getUsername());
        user.setEmail(createRequest.getEmail());
        user.setFirstName(createRequest.getFirstName());
        user.setLastName(createRequest.getLastName());
        user.setPhone(createRequest.getPhone());
        user.setEmployeeId(createRequest.getEmployeeId());
        user.setPosition(createRequest.getPosition());
        user.setDepartment(createRequest.getDepartment());
        user.setStatus(createRequest.getStatus() != null ? createRequest.getStatus() : User.UserStatus.ACTIVE);
        user.setEnabled(createRequest.getActivateImmediately() != null ? createRequest.getActivateImmediately() : true);
        user.setLanguage(createRequest.getLanguage());
        user.setTimezone(createRequest.getTimezone());

        if (createRequest.getPassword() != null) {
            // ⭐ ЗАСВАРЛАСАН: setPassword -> setPasswordHash ⭐
            user.setPasswordHash(passwordEncoder.encode(createRequest.getPassword()));
        } else {
            String tempPassword = generateTemporaryPassword();
            // ⭐ ЗАСВАРЛАСАН: setPassword -> setPasswordHash ⭐
            user.setPasswordHash(passwordEncoder.encode(tempPassword));
            user.setCredentialsNonExpired(false);
        }
        
        user.setPasswordChangedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        logger.info("User created successfully with ID: {}", savedUser.getId());
        
        return UserDto.fromEntity(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        logger.debug("Getting user by ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        
        return UserDto.fromEntity(user);
    }

    @Override
    public UserDto updateUser(UUID id, UserDto userDto) {
        logger.info("Updating user with ID: {}", id);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        
        if (!validateUserData(userDto)) {
            throw new IllegalArgumentException("Хэрэглэгчийн мэдээлэл дутуу эсвэл буруу байна");
        }
        
        if (!existingUser.getUsername().equals(userDto.getUsername()) &&
            userRepository.existsByUsername(userDto.getUsername())) {
            throw new IllegalArgumentException("Хэрэглэгчийн нэр аль хэдийн байна: " + userDto.getUsername());
        }
        
        if (!existingUser.getEmail().equals(userDto.getEmail()) &&
            userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("И-мэйл аль хэдийн байна: " + userDto.getEmail());
        }
        
        updateUserFields(existingUser, userDto);
        
        User savedUser = userRepository.save(existingUser);
        logger.info("User updated successfully with ID: {}", savedUser.getId());
        
        return UserDto.fromEntity(savedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        logger.info("Deleting user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        
        if (!canDeleteUser(id.toString())) {
            throw new IllegalArgumentException("Хэрэглэгчийг устгах боломжгүй");
        }
        
        user.markAsDeleted();
        userRepository.save(user);
        
        logger.info("User deleted successfully with ID: {}", id);
    }

    @Override
    public UserDto restoreUser(UUID id) {
        logger.info("Restoring user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        
        user.restore();
        User savedUser = userRepository.save(user);
        
        logger.info("User restored successfully with ID: {}", id);
        return UserDto.fromEntity(savedUser);
    }

    // Authentication operations
    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        logger.debug("Getting user by username: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + username));
        
        return UserDto.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        logger.debug("Getting user by email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("И-мэйл олдсонгүй: " + email));
        
        return UserDto.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByEmployeeId(String employeeId) {
        logger.debug("Getting user by employee ID: {}", employeeId);
        
        User user = userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + employeeId));
        
        return UserDto.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByPhone(String phone) {
        logger.debug("Getting user by phone: {}", phone);
        
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + phone));
        
        return UserDto.fromEntity(user);
    }

    // Хайлт операциуд
    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable, String search, String department, String status, String role) {
        logger.debug("Getting all users with pageable: {}, search: {}, department: {}, status: {}, role: {}", pageable, search, department, status, role);
        
        Page<User> users;
        if (search != null && !search.isEmpty()) {
            users = userRepository.findBySearchTerm(search, pageable);
        } else if (department != null && !department.isEmpty()) {
            users = userRepository.findByDepartment(department, pageable);
        } else if (status != null && !status.isEmpty()) {
            try {
                User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
                users = userRepository.findByStatus(userStatus, pageable);
            } catch (IllegalArgumentException e) {
                users = Page.empty(pageable);
            }
        } else if (role != null && !role.isEmpty()) {
            users = userRepository.findByRoleName(role, pageable);
        }
        else {
            users = userRepository.findAll(pageable);
        }
        return users.map(UserDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getUsersByStatus(User.UserStatus status, Pageable pageable) {
        logger.debug("Getting users by status: {}", status);
        
        Page<User> users = userRepository.findByStatus(status, pageable);
        return users.map(UserDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getUsersByDepartment(String department, Pageable pageable) {
        logger.debug("Getting users by department: {}", department);
        
        Page<User> users = userRepository.findByDepartment(department, pageable);
        return users.map(UserDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getUsersByPosition(String position, Pageable pageable) {
        logger.debug("Getting users by position: {}", position);
        
        Page<User> users = userRepository.findByPosition(position, pageable);
        return users.map(UserDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> searchUsersByName(String searchTerm, Pageable pageable) {
        logger.debug("Searching users by name: {}", searchTerm);
        
        Page<User> users = userRepository.findByName(searchTerm, pageable);
        return users.map(UserDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> searchUsers(String searchTerm, Pageable pageable) {
        logger.debug("Searching users with term: {}", searchTerm);
        
        Page<User> users = userRepository.findBySearchTerm(searchTerm, pageable);
        return users.map(UserDto::fromEntity);
    }

    // Дэвшилтэт хайлт
    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> searchUsersWithFilters(User.UserStatus status, String department, String position,
                                               Boolean enabled, Boolean twoFactorEnabled, Boolean hasRoles,
                                               LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return userRepository.findByAdvancedFilters(status, department, position, null, enabled, 
                twoFactorEnabled, hasRoles, null, startDate, endDate, pageable)
                .map(UserDto::fromEntity);
    }

    // Role management - ЗАСВАРЛАСАН UUID ашиглах
    @Override
    public UserDto assignRoleToUser(UUID userId, UUID roleId) {
        logger.info("Assigning role {} to user {}", roleId, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + userId));
        
        // UUID шууд ашиглах
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Дүр олдсонгүй: " + roleId));
        
        user.addRole(role);
        User savedUser = userRepository.save(user);
        
        logger.info("Role assigned successfully to user: {}", userId);
        return UserDto.fromEntity(savedUser);
    }

    @Override
    public UserDto removeRoleFromUser(UUID userId, UUID roleId) {
        logger.info("Removing role {} from user {}", roleId, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + userId));
        
        // UUID шууд ашиглах
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Дүр олдсонгүй: " + roleId));
        
        user.removeRole(role);
        User savedUser = userRepository.save(user);
        
        logger.info("Role removed successfully from user: {}", userId);
        return UserDto.fromEntity(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserRoles(UUID userId) {
        logger.debug("Getting roles for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + userId));
        
        return user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getUsersByRole(String roleId, Pageable pageable) {
        logger.debug("Getting users by role: {}", roleId);
        
        try {
            UUID roleUuid = UUID.fromString(roleId);
            Role role = roleRepository.findById(roleUuid)
                    .orElseThrow(() -> new IllegalArgumentException("Дүр олдсонгүй: " + roleId));
            
            Page<User> users = userRepository.findByRole(role, pageable);
            return users.map(UserDto::fromEntity);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid role ID format: {}", roleId);
            return Page.empty(pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getUsersByRoleName(String roleName, Pageable pageable) {
        logger.debug("Getting users by role name: {}", roleName);
        
        Page<User> users = userRepository.findByRoleName(roleName, pageable);
        return users.map(UserDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersWithMultipleRoles() {
        logger.debug("Getting users with multiple roles");
        
        List<User> users = userRepository.findUsersWithMultipleRoles();
        return users.stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersWithoutRoles() {
        logger.debug("Getting users without roles");
        
        List<User> users = userRepository.findUsersWithoutRoles();
        return users.stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    // Account management
    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getActiveUsers(Pageable pageable) {
        logger.debug("Getting active users");
        
        Page<User> users = userRepository.findActiveUsers(pageable);
        return users.map(UserDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getLockedUsers(Pageable pageable) {
        logger.debug("Getting locked users");
        
        Page<User> users = userRepository.findLockedUsers(pageable);
        return users.map(UserDto::fromEntity);
    }

    @Override
    public UserDto enableUser(UUID id) {
        logger.info("Enabling user: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        
        user.enable();
        User savedUser = userRepository.save(user);
        
        logger.info("User enabled successfully: {}", id);
        return UserDto.fromEntity(savedUser);
    }

    @Override
    public UserDto disableUser(UUID id) {
        logger.info("Disabling user: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        
        user.disable();
        User savedUser = userRepository.save(user);
        
        logger.info("User disabled successfully: {}", id);
        return UserDto.fromEntity(savedUser);
    }

    @Override
    public UserDto lockUser(UUID id, LocalDateTime until, String reason) {
        logger.info("Locking user: {} until: {}", id, until);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        
        user.lockAccount(until);
        User savedUser = userRepository.save(user);
        
        logger.info("User locked successfully: {}", id);
        return UserDto.fromEntity(savedUser);
    }

    @Override
    public UserDto unlockUser(UUID id) {
        logger.info("Unlocking user: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        
        user.unlockAccount();
        User savedUser = userRepository.save(user);
        
        logger.info("User unlocked successfully: {}", id);
        return UserDto.fromEntity(savedUser);
    }

    @Override
    public UserDto suspendUser(UUID id, String reason) {
        logger.info("Suspending user: {} with reason: {}", id, reason);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        
        user.suspend();
        User savedUser = userRepository.save(user);
        
        logger.info("User suspended successfully: {}", id);
        return UserDto.fromEntity(savedUser);
    }

    @Override
    public UserDto toggleUserStatus(UUID id) {
        logger.info("Toggling status for user: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        if (user.getStatus() == User.UserStatus.ACTIVE) {
            user.setStatus(User.UserStatus.INACTIVE);
        } else {
            user.setStatus(User.UserStatus.ACTIVE);
        }
        userRepository.save(user);
        return UserDto.fromEntity(user);
    }

    @Override
    public List<UserDto> getOnlineUsers() {
        logger.debug("Fetching online users");
        // Placeholder: Implement actual online user tracking logic
        return Collections.emptyList();
    }

    // Password management
    @Override
    public UserDto changePassword(UUID id, String currentPassword, String newPassword) {
        logger.info("Changing password for user: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        
        // ⭐ ЗАСВАРЛАСАН: getPassword() -> getPasswordHash() ⭐
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Одоогийн нууц үг буруу байна");
        }
        
        if (!isPasswordStrong(newPassword)) {
            throw new IllegalArgumentException("Шинэ нууц үг хангалттай хүчтэй биш байна");
        }
        
        user.changePassword(passwordEncoder.encode(newPassword));
        User savedUser = userRepository.save(user);
        
        logger.info("Password changed successfully for user: {}", id);
        return UserDto.fromEntity(savedUser);
    }

    @Override
    public Map<String, Object> resetUserPassword(UUID id, String newPassword) {
        logger.info("Resetting password for user: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        
        if (!isPasswordStrong(newPassword)) {
            throw new IllegalArgumentException("Шинэ нууц үг хангалттай хүчтэй биш байна");
        }
        
        user.changePassword(passwordEncoder.encode(newPassword));
        user.setCredentialsNonExpired(false);
        User savedUser = userRepository.save(user);
        
        logger.info("Password reset successfully for user: {}", id);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Password reset successfully");
        result.put("userId", savedUser.getId());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersWithExpiredPasswords() {
        logger.debug("Getting users with expired passwords");
        
        List<User> users = userRepository.findUsersWithExpiredPasswords();
        return users.stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto markPasswordExpired(UUID id) {
        logger.info("Marking password as expired for user: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        
        user.setCredentialsNonExpired(false);
        User savedUser = userRepository.save(user);
        
        logger.info("Password marked as expired for user: {}", id);
        return UserDto.fromEntity(savedUser);
    }

    @Override
    public String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }

    // Login management
    @Override
    public void recordSuccessfulLogin(String username) {
        logger.debug("Recording successful login for: {}", username);
        
        userRepository.findByUsername(username).ifPresent(user -> {
            user.recordSuccessfulLogin();
            userRepository.save(user);
        });
    }

    @Override
    public void recordFailedLoginAttempt(String username) {
        logger.debug("Recording failed login attempt for: {}", username);
        
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= 5) {
                user.setIsLocked(true);
                user.setLockedUntil(LocalDateTime.now().plusHours(1));
            }
            userRepository.save(user);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getUsersByLastLogin(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return userRepository.findByLastLoginBetween(startDate, endDate, pageable)
                .map(UserDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getInactiveUsers(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return userRepository.findInactiveUsers(cutoffDate)
                .stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersWithFailedAttempts(int threshold) {
        return userRepository.findUsersWithFailedAttempts(threshold)
                .stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    // Two-Factor Authentication
    @Override
    public UserDto enableTwoFactor(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        
        user.setTwoFactorEnabled(true);
        return UserDto.fromEntity(userRepository.save(user));
    }

    @Override
    public UserDto disableTwoFactor(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        
        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        return UserDto.fromEntity(userRepository.save(user));
    }

    @Override
    public String generateTwoFactorSecret(UUID id) {
        return "JBSWY3DPEHPK3PXP"; // Example secret - should use proper TOTP library
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getUsersWithTwoFactorEnabled(Pageable pageable) {
        return userRepository.findUsersWithTwoFactorEnabled(pageable)
                .map(UserDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getUsersWithoutTwoFactor(Pageable pageable) {
        return userRepository.findUsersWithoutTwoFactor(pageable)
                .map(UserDto::fromEntity);
    }

    // Profile management
    @Override
    public UserDto updateUserProfile(UUID id, UserDto profileDto) { 
        return updateUser(id, profileDto);
    }

    @Override
    public UserDto updateUserPreferences(UUID id, String language, String timezone) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        user.setLanguage(language);
        user.setTimezone(timezone);
        return UserDto.fromEntity(userRepository.save(user));
    }

    @Override
    public UserDto uploadProfilePicture(UUID id, byte[] imageData, String contentType) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        user.setProfilePictureUrl("/api/profile-pictures/" + id);
        return UserDto.fromEntity(userRepository.save(user));
    }

    @Override
    public UserDto updateProfilePicture(UUID id, String profilePictureUrl) {
        logger.info("Updating profile picture for user: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        user.setProfilePictureUrl(profilePictureUrl);
        return UserDto.fromEntity(userRepository.save(user));
    }

    // Validation методууд - ШИНЭ НЭМЭГДСЭН
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean validateUserData(UserDto userDto) {
        if (userDto == null) {
            return false;
        }
        
        // Username шалгах
        if (userDto.getUsername() == null || userDto.getUsername().trim().isEmpty()) {
            return false;
        }
        
        // Email шалгах
        if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()) {
            return false;
        }
        
        // Name шалгах
        if (userDto.getFirstName() == null || userDto.getFirstName().trim().isEmpty()) {
            return false;
        }
        
        if (userDto.getLastName() == null || userDto.getLastName().trim().isEmpty()) {
            return false;
        }
        
        // Email format шалгах
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!userDto.getEmail().matches(emailRegex)) {
            return false;
        }
        
        return true;
    }

    @Override
    public boolean canDeleteUser(String id) {
        try {
            UUID userId = UUID.fromString(id);
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return false;
            }
            
            // Супер админыг устгах боломжгүй
            if (user.hasAnyRole("ROLE_SYSTEM_ADMIN")) {
                return false;
            }
            
            // Аль хэдийн устгагдсан бол дахин устгах боломжгүй
            if (Boolean.TRUE.equals(user.getIsDeleted())) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Error checking if user can be deleted: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isWhitespace(c)) {
                hasSpecial = true;
            }
        }
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    // Permissions - placeholder implementations
    @Override
    public Set<String> getUserPermissions(UUID id) { 
        return new HashSet<>(); 
    }

    @Override
    public boolean hasPermission(UUID id, String permissionName) { 
        return false; 
    }

    @Override
    public boolean hasResourcePermission(UUID id, String resource, String action) { 
        return false; 
    }

    @Override
    public List<UserDto> getUsersWithPermission(String permissionName) { 
        return new ArrayList<>(); 
    }

    // Statistics and Dashboard methods
    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalUsers = userRepository.count();
        stats.put("totalUsers", totalUsers);
        
        List<Object[]> statusStats = userRepository.countByStatus();
        Map<String, Long> statusMap = new HashMap<>();
        for (Object[] row : statusStats) {
            statusMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("byStatus", statusMap);
        
        List<User> todayUsers = userRepository.findTodayRegistered();
        stats.put("todayRegistrations", todayUsers.size());
        
        return stats;
    }

    @Override
    public Map<User.UserStatus, Long> getUserCountByStatus() {
        List<Object[]> results = userRepository.countByStatus();
        Map<User.UserStatus, Long> countMap = new HashMap<>();
        
        for (Object[] row : results) {
            User.UserStatus status = (User.UserStatus) row[0];
            Long count = (Long) row[1];
            countMap.put(status, count);
        }
        
        return countMap;
    }

    @Override
    public Map<String, Long> getUserCountByDepartment() {
        List<Object[]> results = userRepository.countByDepartment();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]));
    }

    @Override
    public Map<String, Long> getUserCountByRole() {
        List<Object[]> results = userRepository.countByRole();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]));
    }

    @Override
    public List<Map<String, Object>> getMonthlyUserStats(int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        List<Object[]> results = userRepository.getMonthlyUserStats(startDate);
        
        return results.stream()
                .map(row -> {
                    Map<String, Object> monthStats = new HashMap<>();
                    monthStats.put("month", row[0]);
                    monthStats.put("count", row[1]);
                    return monthStats;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getLoginStats(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = userRepository.getLoginStats(startDate, endDate);
        
        return results.stream()
                .map(row -> {
                    Map<String, Object> loginStats = new HashMap<>();
                    loginStats.put("date", row[0]);
                    loginStats.put("count", row[1]);
                    return loginStats;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getTodayUserStats() {
        // ЗАСВАРЛАСАН: Object[] -> Map<String, Object> хөрвүүлэлт
        Object[] results = userRepository.getTodayUserStats();
        Map<String, Object> statsMap = new HashMap<>();
        
        if (results != null && results.length > 0) {
            // Assuming the array contains: [totalRegistered, activeUsers, newUsers, etc.]
            statsMap.put("totalRegistered", results.length > 0 ? results[0] : 0);
            statsMap.put("activeUsers", results.length > 1 ? results[1] : 0);
            statsMap.put("newUsers", results.length > 2 ? results[2] : 0);
        }
        
        return statsMap;
    }

    @Override
    public Map<String, Object> getUserActivityStats() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);
        Object[] results = userRepository.getUserActivityStats(oneWeekAgo, oneMonthAgo);
        
        Map<String, Object> stats = new HashMap<>();
        if (results != null && results.length >= 3) {
            stats.put("weeklyActive", results[0]);
            stats.put("monthlyActive", results[1]);
            stats.put("totalUsers", results[2]);
        }
        
        return stats;
    }

    @Override
    public Page<UserDto> getMostActiveUsers(Pageable pageable) {
        return userRepository.findMostActiveUsers(pageable)
                .map(UserDto::fromEntity);
    }

    @Override
    public Page<UserDto> getLeastActiveUsers(Pageable pageable) {
        return userRepository.findLeastActiveUsers(pageable)
                .map(UserDto::fromEntity);
    }

    // Bulk operations
    @Override
    public Map<String, Object> bulkUpdateUserStatus(List<UUID> userIds, Boolean isActive) {
        logger.info("Bulk updating user status for user IDs: {}", userIds);
        
        User.UserStatus newStatus = isActive ? User.UserStatus.ACTIVE : User.UserStatus.INACTIVE;
        int updatedCount = userRepository.updateStatusForUsers(userIds, newStatus, isActive, "system");
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Bulk user status updated successfully");
        result.put("updatedCount", updatedCount);
        return result;
    }

    @Override
    public int resetFailedAttemptsForUsers(List<UUID> userIds) {
        return userRepository.resetFailedAttempts(userIds);
    }

    @Override
    public int markPasswordExpiredForUsers(List<UUID> userIds) {
        return userRepository.markPasswordExpired(userIds);
    }

    @Override
    public List<UserDto> createUsersBulk(List<CreateUserRequestDto> users) {
        return users.stream().map(this::createUser).collect(Collectors.toList());
    }

    @Override
    public byte[] exportUsersToExcel(List<UUID> userIds) { 
        return new byte[0]; 
    }

    @Override
    public List<UserDto> getAdminUsers() {
        return userRepository.findAdminUsers().stream()
                .map(UserDto::fromEntity).collect(Collectors.toList());
    }

    @Override
    public Page<UserDto> getSystemUsers(Pageable pageable) {
        return userRepository.findSystemUsers(pageable).map(UserDto::fromEntity);
    }

    @Override
    public Page<UserDto> getRegularUsers(Pageable pageable) {
        return userRepository.findRegularUsers(pageable).map(UserDto::fromEntity);
    }

    @Override
    public int cleanupInactiveUsers(int inactiveDays) {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusDays(inactiveDays);
        List<User> inactiveUsers = userRepository.findInactiveForCleanup(sixMonthsAgo);
        return inactiveUsers.size();
    }

    @Override
    public List<UserDto> getUsersNeverLoggedIn(int days) {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(days);
        return userRepository.findNeverLoggedIn(oneMonthAgo).stream()
                .map(UserDto::fromEntity).collect(Collectors.toList());
    }

    // Notification and audit placeholders
    @Override
    public boolean sendNotificationToUser(UUID userId, String subject, String message) { 
        return true; 
    }

    @Override
    public boolean sendPasswordResetNotification(UUID userId) { 
        return true; 
    }

    @Override
    public boolean sendAccountLockedNotification(UUID userId) { 
        return true; 
    }

    @Override
    public List<Map<String, Object>> getUserAuditHistory(UUID id) { 
        return new ArrayList<>(); 
    }

    @Override
    public Page<Map<String, Object>> getUserActivity(UUID id, Pageable pageable) {
        logger.debug("Getting user activity for user: {}", id);
        return Page.empty(pageable);
    }

    @Override
    public List<Map<String, Object>> getUserLoginHistory(UUID id, int days) { 
        return new ArrayList<>(); 
    }

    @Override
    public Map<String, Object> validateDataIntegrity() { 
        return new HashMap<>(); 
    }

    @Override
    public List<Map<String, Object>> validateRolePermissionConsistency() { 
        return new ArrayList<>(); 
    }

    // Helper methods
    private void updateUserFields(User existingUser, UserDto userDto) {
        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setFirstName(userDto.getFirstName());
        existingUser.setLastName(userDto.getLastName());
        existingUser.setPhone(userDto.getPhone());
        existingUser.setEmployeeId(userDto.getEmployeeId());
        existingUser.setPosition(userDto.getPosition());
        existingUser.setDepartment(userDto.getDepartment());
        existingUser.setStatus(userDto.getStatus());
        existingUser.setEnabled(userDto.getEnabled());
        existingUser.setLanguage(userDto.getLanguage());
        existingUser.setTimezone(userDto.getTimezone());
    }
}