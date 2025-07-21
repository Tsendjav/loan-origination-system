package com.los.service.impl;

import com.los.dto.UserDto;
import com.los.entity.Role;
import com.los.entity.User;
import com.los.repository.RoleRepository;
import com.los.repository.UserRepository;
import com.los.service.UserService;
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
        
        return user; // User entity implements UserDetails
    }

    // CRUD операциуд
    @Override
    public UserDto createUser(UserDto userDto) {
        logger.info("Creating new user with username: {}", userDto.getUsername());
        
        // Validation
        if (!validateUserData(userDto)) {
            throw new IllegalArgumentException("Хэрэглэгчийн мэдээлэл дутуу эсвэл буруу байна");
        }
        
        // Check for duplicates
        if (existsByUsername(userDto.getUsername())) {
            throw new IllegalArgumentException("Хэрэглэгчийн нэр аль хэдийн байна: " + userDto.getUsername());
        }
        
        if (existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("И-мэйл аль хэдийн байна: " + userDto.getEmail());
        }
        
        // Create user entity
        User user = userDto.toEntity();
        
        // Encode password
        if (userDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        } else {
            // Generate temporary password if not provided
            String tempPassword = generateTemporaryPassword();
            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setCredentialsNonExpired(false); // Force password change on first login
        }
        
        user.setPasswordChangedAt(LocalDateTime.now());
        
        // Save user
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
        
        // Validation
        if (!validateUserData(userDto)) {
            throw new IllegalArgumentException("Хэрэглэгчийн мэдээлэл дутуу эсвэл буруу байна");
        }
        
        // Check duplicates (excluding current user)
        if (!existingUser.getUsername().equals(userDto.getUsername()) &&
            existsByUsername(userDto.getUsername())) {
            throw new IllegalArgumentException("Хэрэглэгчийн нэр аль хэдийн байна: " + userDto.getUsername());
        }
        
        if (!existingUser.getEmail().equals(userDto.getEmail()) &&
            existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("И-мэйл аль хэдийн байна: " + userDto.getEmail());
        }
        
        // Update fields
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
        
        // Check if can be deleted
        if (!canDeleteUser(id)) {
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
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + email));
        
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
    public Page<UserDto> getAllUsers(Pageable pageable) {
        logger.debug("Getting all users with pageable: {}", pageable);
        
        Page<User> users = userRepository.findAll(pageable);
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

    // Role management
    @Override
    public UserDto assignRoleToUser(UUID userId, UUID roleId) {
        logger.info("Assigning role {} to user {}", roleId, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + userId));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Дүр олдсонгүй: " + roleId));
        
        if (!role.canAssignTo(user)) {
            throw new IllegalArgumentException("Энэ дүрийг хэрэглэгчид өгөх боломжгүй");
        }
        
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
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Дүр олдсонгүй: " + roleId));
        
        user.removeRole(role);
        User savedUser = userRepository.save(user);
        
        logger.info("Role removed successfully from user: {}", userId);
        return UserDto.fromEntity(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getUserRoles(UUID userId) {
        logger.debug("Getting roles for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + userId));
        
        return new ArrayList<>(user.getRoles());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getUsersByRole(UUID roleId, Pageable pageable) {
        logger.debug("Getting users by role: {}", roleId);
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Дүр олдсонгүй: " + roleId));
        
        Page<User> users = userRepository.findByRole(role, pageable);
        return users.map(UserDto::fromEntity);
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

    // Password management
    @Override
    public UserDto changePassword(UUID id, String currentPassword, String newPassword) {
        logger.info("Changing password for user: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Одоогийн нууц үг буруу байна");
        }
        
        // Validate new password strength
        if (!isPasswordStrong(newPassword)) {
            throw new IllegalArgumentException("Шинэ нууц үг хангалттай хүчтэй биш байна");
        }
        
        // Change password
        user.changePassword(passwordEncoder.encode(newPassword));
        User savedUser = userRepository.save(user);
        
        logger.info("Password changed successfully for user: {}", id);
        return UserDto.fromEntity(savedUser);
    }

    @Override
    public UserDto resetPassword(UUID id, String newPassword) {
        logger.info("Resetting password for user: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        
        // Validate new password strength
        if (!isPasswordStrong(newPassword)) {
            throw new IllegalArgumentException("Шинэ нууц үг хангалттай хүчтэй биш байна");
        }
        
        // Reset password
        user.changePassword(passwordEncoder.encode(newPassword));
        user.setCredentialsNonExpired(false); // Force password change on next login
        User savedUser = userRepository.save(user);
        
        logger.info("Password reset successfully for user: {}", id);
        return UserDto.fromEntity(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersWithExpiredPasswords() {
        logger.debug("Getting users with expired passwords");
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        List<User> users = userRepository.findUsersWithExpiredPasswords(cutoffDate);
        
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
        // Generate a secure temporary password
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
            user.recordFailedLoginAttempt();
            userRepository.save(user);
        });
    }

    // Validation
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean validateUserData(UserDto userDto) {
        return userDto.isValidForRegistration();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        
        // Don't allow deleting admin users
        if (user.hasAnyRole("ROLE_SYSTEM_ADMIN", "ROLE_BUSINESS_ADMIN")) {
            return false;
        }
        
        // Don't allow deleting active users
        return user.getStatus() != User.UserStatus.ACTIVE;
    }

    @Override
    public boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    // Permissions
    @Override
    @Transactional(readOnly = true)
    public Set<String> getUserPermissions(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Хэрэглэгч олдсонгүй: " + id));
        
        return user.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(UUID id, String permissionName) {
        return getUserPermissions(id).contains(permissionName);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasResourcePermission(UUID id, String resource, String action) {
        return userRepository.userHasResourcePermission(id, resource, 
                com.los.entity.Permission.Action.valueOf(action));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersWithPermission(String permissionName) {
        List<User> users = userRepository.findUsersWithPermission(permissionName);
        return users.stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
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

    // Placeholder implementations for interface completeness
    @Override
    public Page<UserDto> searchUsersWithFilters(User.UserStatus status, String department, String position,
                                               Boolean enabled, Boolean twoFactorEnabled, Boolean hasRoles,
                                               LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return userRepository.findByAdvancedFilters(status, department, position, enabled, 
                twoFactorEnabled, hasRoles, startDate, endDate, pageable)
                .map(UserDto::fromEntity);
    }

    @Override
    public Page<UserDto> getUsersByLastLogin(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return userRepository.findByLastLoginBetween(startDate, endDate, pageable)
                .map(UserDto::fromEntity);
    }

    @Override
    public List<UserDto> getInactiveUsers(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return userRepository.findInactiveUsers(cutoffDate)
                .stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getUsersWithFailedAttempts(int threshold) {
        return userRepository.findUsersWithFailedAttempts(threshold)
                .stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    // Two-Factor Authentication placeholders
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
        // Generate TOTP secret
        return "JBSWY3DPEHPK3PXP"; // Example secret
    }

    @Override
    public Page<UserDto> getUsersWithTwoFactorEnabled(Pageable pageable) {
        return userRepository.findUsersWithTwoFactorEnabled(pageable)
                .map(UserDto::fromEntity);
    }

    @Override
    public Page<UserDto> getUsersWithoutTwoFactor(Pageable pageable) {
        return userRepository.findUsersWithoutTwoFactor(pageable)
                .map(UserDto::fromEntity);
    }

    // Statistics
    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalUsers = userRepository.count();
        stats.put("totalUsers", totalUsers);
        
        // By status
        List<Object[]> statusStats = userRepository.countByStatus();
        Map<String, Long> statusMap = new HashMap<>();
        for (Object[] row : statusStats) {
            statusMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("byStatus", statusMap);
        
        // Today's registrations
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

    // Dashboard and additional methods - placeholder implementations
    @Override
    public Map<String, Object> getTodayUserStats() {
        Object[] results = userRepository.getTodayUserStats();
        Map<String, Object> stats = new HashMap<>();
        
        if (results != null && results.length >= 4) {
            stats.put("todayRegistered", results[0]);
            stats.put("activeUsers", results[1]);
            stats.put("lockedUsers", results[2]);
            stats.put("todayLoggedIn", results[3]);
        }
        
        return stats;
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

    // Remaining placeholder implementations
    @Override
    public UserDto updateUserProfile(UUID id, UserDto profileDto) { return updateUser(id, profileDto); }

    @Override
    public UserDto updateUserPreferences(UUID id, String language, String timezone) {
        User user = userRepository.findById(id).orElseThrow();
        user.setLanguage(language);
        user.setTimezone(timezone);
        return UserDto.fromEntity(userRepository.save(user));
    }

    @Override
    public UserDto uploadProfilePicture(UUID id, byte[] imageData, String contentType) {
        User user = userRepository.findById(id).orElseThrow();
        // Implementation would store image and return URL
        user.setProfilePictureUrl("/api/profile-pictures/" + id);
        return UserDto.fromEntity(userRepository.save(user));
    }

    @Override
    public int updateStatusForUsers(List<UUID> userIds, User.UserStatus newStatus, Boolean enabled) {
        return userRepository.updateStatusForUsers(userIds, newStatus, enabled);
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
    public List<UserDto> createUsersBulk(List<UserDto> users) {
        return users.stream().map(this::createUser).collect(Collectors.toList());
    }

    @Override
    public byte[] exportUsersToExcel(List<UUID> userIds) { return new byte[0]; }

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
    public boolean sendNotificationToUser(UUID userId, String subject, String message) { return true; }

    @Override
    public boolean sendPasswordResetNotification(UUID userId) { return true; }

    @Override
    public boolean sendAccountLockedNotification(UUID userId) { return true; }

    @Override
    public List<Map<String, Object>> getUserAuditHistory(UUID id) { return new ArrayList<>(); }

    @Override
    public List<Map<String, Object>> getUserActivityLog(UUID id, int days) { return new ArrayList<>(); }

    @Override
    public List<Map<String, Object>> getUserLoginHistory(UUID id, int days) { return new ArrayList<>(); }

    @Override
    public Map<String, Object> validateDataIntegrity() { return new HashMap<>(); }

    @Override
    public List<Map<String, Object>> validateRolePermissionConsistency() { return new ArrayList<>(); }
}