package com.company.los.controller;

import com.company.los.dto.UserDto;
import com.company.los.dto.CreateUserRequestDto;
import com.company.los.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Хэрэглэгчийн удирдлагын REST API Controller
 * User Management REST API Controller
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Хэрэглэгчийн удирдлагын API")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    /**
     * Бүх хэрэглэгчийн жагсаалт (pagination-тай)
     * Get all users with pagination
     */
    @GetMapping
    @Operation(summary = "Хэрэглэгчдийн жагсаалт", description = "Бүх хэрэглэгчийн жагсаалтыг pagination-тай авах")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай"),
        @ApiResponse(responseCode = "403", description = "Эрх хүрэхгүй")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) @Parameter(description = "Хайх утга") String search,
            @RequestParam(required = false) @Parameter(description = "Хэлтэс") String department,
            @RequestParam(required = false) @Parameter(description = "Статус") String status,
            @RequestParam(required = false) @Parameter(description = "Дүр") String role) {
        
        log.debug("Getting all users with pagination: {}, search: {}, department: {}, status: {}, role: {}", 
                pageable, search, department, status, role);
        
        Page<UserDto> users = userService.getAllUsers(pageable, search, department, status, role);
        return ResponseEntity.ok(users);
    }

    /**
     * Хэрэглэгчийн дэлгэрэнгүй мэдээлэл ID-гаар
     * Get user by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Хэрэглэгчийн мэдээлэл", description = "ID-гаар хэрэглэгчийн дэлгэрэнгүй мэдээлэл авах")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай"),
        @ApiResponse(responseCode = "404", description = "Хэрэглэгч олдсонгүй"),
        @ApiResponse(responseCode = "403", description = "Эрх хүрэхгүй")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or authentication.name == @userService.getUserById(#id).username")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable @Parameter(description = "Хэрэглэгчийн ID") UUID id) {
        
        log.debug("Getting user by ID: {}", id);
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Username-аар хэрэглэгч авах
     * Get user by username
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "Username-аар хэрэглэгч", description = "Username-аар хэрэглэгчийн мэдээлэл авах")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай"),
        @ApiResponse(responseCode = "404", description = "Хэрэглэгч олдсонгүй"),
        @ApiResponse(responseCode = "403", description = "Эрх хүрэхгүй")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or authentication.name == #username")
    public ResponseEntity<UserDto> getUserByUsername(
            @PathVariable @Parameter(description = "Username") String username) {
        
        log.debug("Getting user by username: {}", username);
        UserDto user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    /**
     * Шинэ хэрэглэгч үүсгэх
     * Create new user
     */
    @PostMapping
    @Operation(summary = "Шинэ хэрэглэгч", description = "Шинэ хэрэглэгч үүсгэх")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Амжилттай үүсгэгдсэн"),
        @ApiResponse(responseCode = "400", description = "Буруу мэдээлэл"),
        @ApiResponse(responseCode = "409", description = "Username эсвэл email давхцсан"),
        @ApiResponse(responseCode = "403", description = "Эрх хүрэхгүй")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequestDto createRequest) {
        log.debug("Creating new user: {}", createRequest.getUsername());
        
        UserDto createdUser = userService.createUser(createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Хэрэглэгчийн мэдээлэл шинэчлэх
     * Update user
     */
    @PutMapping("/{id}")
    @Operation(summary = "Хэрэглэгч шинэчлэх", description = "Хэрэглэгчийн мэдээлэл шинэчлэх")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай шинэчлэгдсэн"),
        @ApiResponse(responseCode = "404", description = "Хэрэглэгч олдсонгүй"),
        @ApiResponse(responseCode = "400", description = "Буруу мэдээлэл"),
        @ApiResponse(responseCode = "403", description = "Эрх хүрэхгүй")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or (hasRole('MANAGER') and @userService.getUserById(#id).department == authentication.details.department)")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable @Parameter(description = "Хэрэглэгчийн ID") UUID id,
            @Valid @RequestBody UserDto userDto) {
        
        log.debug("Updating user: {}", id);
        UserDto updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Хэрэглэгч устгах (логикоор)
     * Delete user (soft delete)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Хэрэглэгч устгах", description = "Хэрэглэгчийг логикоор устгах")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Амжилттай устгагдсан"),
        @ApiResponse(responseCode = "404", description = "Хэрэглэгч олдсонгүй"),
        @ApiResponse(responseCode = "403", description = "Эрх хүрэхгүй")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable @Parameter(description = "Хэрэглэгчийн ID") UUID id) {
        
        log.debug("Deleting user: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Хэрэглэгчийн идэвхжүүлэх/идэвхгүй болгох
     * Activate/Deactivate user
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Статус өөрчлөх", description = "Хэрэглэгчийн идэвх статус өөрчлөх")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай өөрчлөгдсөн"),
        @ApiResponse(responseCode = "404", description = "Хэрэглэгч олдсонгүй"),
        @ApiResponse(responseCode = "403", description = "Эрх хүрэхгүй")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<UserDto> toggleUserStatus(
            @PathVariable @Parameter(description = "Хэрэглэгчийн ID") UUID id) {
        
        log.debug("Toggling status for user: {}", id);
        UserDto user = userService.toggleUserStatus(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Хэрэглэгчийн нууц үг сэргээх
     * Reset user password
     */
    @PutMapping("/{id}/reset-password")
    @Operation(summary = "Нууц үг сэргээх", description = "Хэрэглэгчийн нууц үг сэргээх")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай сэргээгдсэн"),
        @ApiResponse(responseCode = "404", description = "Хэрэглэгч олдсонгүй"),
        @ApiResponse(responseCode = "403", description = "Эрх хүрэхгүй")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> resetUserPassword(
            @PathVariable @Parameter(description = "Хэрэглэгчийн ID") UUID id,
            @RequestParam(required = false) @Parameter(description = "Шинэ нууц үг") String newPassword) {
        
        log.debug("Resetting password for user: {}", id);
        Map<String, Object> result = userService.resetUserPassword(id, newPassword);
        return ResponseEntity.ok(result);
    }

    /**
     * Хэрэглэгчийн цоож тайлах
     * Unlock user
     */
    @PutMapping("/{id}/unlock")
    @Operation(summary = "Хэрэглэгч цоож тайлах", description = "Цоожтой хэрэглэгчийг тайлах")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай тайлагдсан"),
        @ApiResponse(responseCode = "404", description = "Хэрэглэгч олдсонгүй"),
        @ApiResponse(responseCode = "403", description = "Эрх хүрэхгүй")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<UserDto> unlockUser(
            @PathVariable @Parameter(description = "Хэрэглэгчийн ID") UUID id) {
        
        log.debug("Unlocking user: {}", id);
        UserDto user = userService.unlockUser(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Хэрэглэгчийн дүрүүд авах
     * Get user roles
     */
    @GetMapping("/{id}/roles")
    @Operation(summary = "Хэрэглэгчийн дүрүүд", description = "Хэрэглэгчийн бүх дүрүүдийн жагсаалт")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай"),
        @ApiResponse(responseCode = "404", description = "Хэрэглэгч олдсонгүй")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or authentication.name == @userService.getUserById(#id).username")
    public ResponseEntity<List<String>> getUserRoles(
            @PathVariable @Parameter(description = "Хэрэглэгчийн ID") UUID id) {
        
        log.debug("Getting roles for user: {}", id);
        List<String> roles = userService.getUserRoles(id);
        return ResponseEntity.ok(roles);
    }

    /**
     * Хэрэглэгчид дүр олгох
     * Assign role to user
     */
    @PostMapping("/{id}/roles/{roleId}")
    @Operation(summary = "Дүр олгох", description = "Хэрэглэгчид дүр олгох")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай олгогдсон"),
        @ApiResponse(responseCode = "404", description = "Хэрэглэгч эсвэл дүр олдсонгүй"),
        @ApiResponse(responseCode = "409", description = "Дүр аль хэдийн олгогдсон"),
        @ApiResponse(responseCode = "403", description = "Эрх хүрэхгүй")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDto> assignRoleToUser(
            @PathVariable @Parameter(description = "Хэрэглэгчийн ID") UUID id,
            @PathVariable @Parameter(description = "Дүрийн ID") UUID roleId) {
        
        log.debug("Assigning role {} to user: {}", roleId, id);
        UserDto user = userService.assignRoleToUser(id, roleId);
        return ResponseEntity.ok(user);
    }

    /**
     * Хэрэглэгчээс дүр хасах
     * Remove role from user
     */
    @DeleteMapping("/{id}/roles/{roleId}")
    @Operation(summary = "Дүр хасах", description = "Хэрэглэгчээс дүр хасах")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай хасагдсан"),
        @ApiResponse(responseCode = "404", description = "Хэрэглэгч эсвэл дүр олдсонгүй"),
        @ApiResponse(responseCode = "403", description = "Эрх хүрэхгүй")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDto> removeRoleFromUser(
            @PathVariable @Parameter(description = "Хэрэглэгчийн ID") UUID id,
            @PathVariable @Parameter(description = "Дүрийн ID") UUID roleId) {
        
        log.debug("Removing role {} from user: {}", roleId, id);
        UserDto user = userService.removeRoleFromUser(id, roleId);
        return ResponseEntity.ok(user);
    }

    /**
     * Хэлтэс тутмын хэрэглэгчид
     * Get users by department
     */
    @GetMapping("/department/{department}")
    @Operation(summary = "Хэлтэсийн хэрэглэгчид", description = "Тодорхой хэлтэсийн хэрэглэгчдийн жагсаалт")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Page<UserDto>> getUsersByDepartment(
            @PathVariable @Parameter(description = "Хэлтэс") String department,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Getting users by department: {}", department);
        Page<UserDto> users = userService.getUsersByDepartment(department, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Онлайн хэрэглэгчдийн жагсаалт
     * Get online users
     */
    @GetMapping("/online")
    @Operation(summary = "Онлайн хэрэглэгчид", description = "Одоо онлайн байгаа хэрэглэгчдийн жагсаалт")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<UserDto>> getOnlineUsers() {
        log.debug("Getting online users");
        List<UserDto> onlineUsers = userService.getOnlineUsers();
        return ResponseEntity.ok(onlineUsers);
    }

    /**
     * Хэрэглэгчийн статистик
     * Get user statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Хэрэглэгчийн статистик", description = "Хэрэглэгчийн ерөнхий статистик")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        log.debug("Getting user statistics");
        Map<String, Object> stats = userService.getUserStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Хэрэглэгчийн профайл зураг солих
     * Update user profile picture
     */
    @PostMapping("/{id}/profile-picture")
    @Operation(summary = "Профайл зураг солих", description = "Хэрэглэгчийн профайл зураг шинэчлэх")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай шинэчлэгдсэн"),
        @ApiResponse(responseCode = "404", description = "Хэрэглэгч олдсонгүй"),
        @ApiResponse(responseCode = "403", description = "Эрх хүрэхгүй")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or authentication.name == @userService.getUserById(#id).username")
    public ResponseEntity<UserDto> updateProfilePicture(
            @PathVariable @Parameter(description = "Хэрэглэгчийн ID") UUID id,
            @RequestParam @Parameter(description = "Профайл зургийн URL") String profilePictureUrl) {
        
        log.debug("Updating profile picture for user: {}", id);
        UserDto user = userService.updateProfilePicture(id, profilePictureUrl);
        return ResponseEntity.ok(user);
    }

    /**
     * Хэрэглэгчийн үйл ажиллагааны түүх
     * Get user activity history
     */
    @GetMapping("/{id}/activity")
    @Operation(summary = "Үйл ажиллагааны түүх", description = "Хэрэглэгчийн үйл ажиллагааны түүх")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай"),
        @ApiResponse(responseCode = "404", description = "Хэрэглэгч олдсонгүй")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or authentication.name == @userService.getUserById(#id).username")
    public ResponseEntity<Page<Map<String, Object>>> getUserActivity(
            @PathVariable @Parameter(description = "Хэрэглэгчийн ID") UUID id,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Getting activity history for user: {}", id);
        Page<Map<String, Object>> activities = userService.getUserActivity(id, pageable);
        return ResponseEntity.ok(activities);
    }

    /**
     * Bulk хэрэглэгчийн статус өөрчлөх
     * Bulk update user status
     */
    @PutMapping("/bulk-status")
    @Operation(summary = "Bulk статус өөрчлөх", description = "Олон хэрэглэгчийн статус нэгэн зэрэг өөрчлөх")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай"),
        @ApiResponse(responseCode = "403", description = "Эрх хүрэхгүй")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> bulkUpdateUserStatus(
            @RequestBody @Parameter(description = "Bulk статус өөрчлөх мэдээлэл") Map<String, Object> bulkData) {
        
        log.debug("Bulk updating user status");
        
        @SuppressWarnings("unchecked")
        List<UUID> userIds = (List<UUID>) bulkData.get("userIds");
        Boolean isActive = (Boolean) bulkData.get("isActive");
        
        Map<String, Object> result = userService.bulkUpdateUserStatus(userIds, isActive);
        return ResponseEntity.ok(result);
    }

    /**
     * Хэрэглэгч сэргээх
     * Restore user
     */
    @PutMapping("/{id}/restore")
    @Operation(summary = "Хэрэглэгч сэргээх", description = "Устгагдсан хэрэглэгчийг сэргээх")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай сэргээгдсэн"),
        @ApiResponse(responseCode = "404", description = "Хэрэглэгч олдсонгүй"),
        @ApiResponse(responseCode = "403", description = "Эрх хүрэхгүй")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDto> restoreUser(
            @PathVariable @Parameter(description = "Хэрэглэгчийн ID") UUID id) {
        
        log.debug("Restoring user: {}", id);
        UserDto user = userService.restoreUser(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Хэрэглэгчийг идэвхжүүлэх
     * Enable user
     */
    @PutMapping("/{id}/enable")
    @Operation(summary = "Хэрэглэгч идэвхжүүлэх", description = "Хэрэглэгчийг идэвхжүүлэх")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай идэвхжүүлэгдсэн"),
        @ApiResponse(responseCode = "404", description = "Хэрэглэгч олдсонгүй"),
        @ApiResponse(responseCode = "403", description = "Эрх хүрэхгүй")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<UserDto> enableUser(
            @PathVariable @Parameter(description = "Хэрэглэгчийн ID") UUID id) {
        
        log.debug("Enabling user: {}", id);
        UserDto user = userService.enableUser(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Хэрэглэгчийг идэвхгүй болгох
     * Disable user
     */
    @PutMapping("/{id}/disable")
    @Operation(summary = "Хэрэглэгч идэвхгүй болгох", description = "Хэрэглэгчийг идэвхгүй болгох")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай идэвхгүй болгогдсон"),
        @ApiResponse(responseCode = "404", description = "Хэрэглэгч олдсонгүй"),
        @ApiResponse(responseCode = "403", description = "Эрх хүрэхгүй")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<UserDto> disableUser(
            @PathVariable @Parameter(description = "Хэрэглэгчийн ID") UUID id) {
        
        log.debug("Disabling user: {}", id);
        UserDto user = userService.disableUser(id);
        return ResponseEntity.ok(user);
    }
}