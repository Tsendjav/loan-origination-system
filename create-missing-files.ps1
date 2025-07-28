# ================================================================
# 📁 LOS Дутуу файлууд үүсгэх PowerShell Script
# create-missing-files.ps1
# ================================================================

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "🔧 LOS Дутуу файлууд үүсгэж байна..." -ForegroundColor Yellow
Write-Host "════════════════════════════════════" -ForegroundColor Yellow

$createdFiles = @()

# 1. BaseRepository.java үүсгэх
$baseRepoPath = "backend/src/main/java/com/company/los/repository/BaseRepository.java"
$baseRepoContent = @'
package com.company.los.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

/**
 * Base Repository интерфейс - бүх repository-д хамтын функцууд
 * UUID тип ашиглах
 */
@NoRepositoryBean
public interface BaseRepository<T> extends JpaRepository<T, UUID> {
    
    /**
     * ID-аар идэвхтэй entity олох
     */
    Optional<T> findByIdAndDeletedFalse(UUID id);
    
    /**
     * Бүх идэвхтэй entity-үүдийг олох
     */
    List<T> findAllByDeletedFalse();
    
    /**
     * Нэрээр хайх (хэрэв entity-д name field байгаа бол)
     */
    default List<T> findByNameContainingIgnoreCase(String name) {
        // Хэрэв шаардлагатай бол override хийнэ
        return List.of();
    }
    
    /**
     * Soft delete хийх
     */
    default void softDelete(UUID id) {
        findById(id).ifPresent(entity -> {
            // Хэрэв entity-д setDeleted method байгаа бол
            // ((BaseEntity) entity).setDeleted(true);
            save(entity);
        });
    }
}
'@

# 2. AuthServiceImpl.java үүсгэх
$authServiceImplPath = "backend/src/main/java/com/company/los/service/impl/AuthServiceImpl.java"
$authServiceImplContent = @'
package com.company.los.service.impl;

import com.company.los.dto.AuthResponseDto;
import com.company.los.dto.LoginRequestDto;
import com.company.los.entity.User;
import com.company.los.repository.UserRepository;
import com.company.los.security.JwtUtil;
import com.company.los.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Authentication Service Implementation
 * JWT token ашиглан хэрэглэгчийн баталгаажуулалт
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponseDto login(LoginRequestDto loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        
        try {
            // Authentication хийх
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // User мэдээлэл авах
            User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // JWT token үүсгэх
            String token = jwtUtil.generateToken(user.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

            // Last login шинэчлэх
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            log.info("Login successful for user: {}", loginRequest.getUsername());

            return AuthResponseDto.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();

        } catch (AuthenticationException ex) {
            log.error("Login failed for user: {}", loginRequest.getUsername(), ex);
            throw new RuntimeException("Invalid username or password");
        }
    }

    @Override
    public AuthResponseDto refreshToken(String refreshToken) {
        log.info("Refresh token request");
        
        if (!jwtUtil.isRefreshTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        String newToken = jwtUtil.generateToken(username);
        String newRefreshToken = jwtUtil.generateRefreshToken(username);

        return AuthResponseDto.builder()
            .token(newToken)
            .refreshToken(newRefreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtUtil.getExpirationTime())
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .roles(user.getRoles())
            .build();
    }

    @Override
    public void logout(String token) {
        log.info("Logout request");
        
        // Token-ийг blacklist-д нэмэх эсвэл invalid болгох
        // Redis ашиглаж болно
        String username = jwtUtil.getUsernameFromToken(token);
        log.info("User logged out: {}", username);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.isTokenValid(token);
    }

    @Override
    public Optional<User> getCurrentUser(String token) {
        if (!jwtUtil.isTokenValid(token)) {
            return Optional.empty();
        }
        
        String username = jwtUtil.getUsernameFromToken(token);
        return userRepository.findByUsername(username);
    }

    @Override
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("Password changed for user: {}", user.getUsername());
    }
}
'@

# 3. JwtUtil.java үүсгэх
$jwtUtilPath = "backend/src/main/java/com/company/los/security/JwtUtil.java"
$jwtUtilContent = @'
package com.company.los.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Token Utility класс
 * Token үүсгэх, баталгаажуулах, мэдээлэл гаргах
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${app.jwt.secret:mySecretKey}")
    private String secret;

    @Value("${app.jwt.expiration:86400000}") // 24 цаг (milliseconds)
    private Long jwtExpiration;

    @Value("${app.jwt.refresh-expiration:604800000}") // 7 хоног
    private Long refreshExpiration;

    /**
     * Access token үүсгэх
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        return createToken(claims, username, jwtExpiration);
    }

    /**
     * Refresh token үүсгэх
     */
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, username, refreshExpiration);
    }

    /**
     * Token үүсгэх
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Token-оос username гаргах
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Token-оос expiration date гаргах
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Token-оос тодорхой claim гаргах
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Token-оос бүх claims гаргах
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT parsing error: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token");
        }
    }

    /**
     * Token хугацаа дууссан эсэхийг шалгах
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Access token батлах
     */
    public Boolean isTokenValid(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return false;
            }
            
            Claims claims = getAllClaimsFromToken(token);
            String tokenType = (String) claims.get("type");
            
            return "access".equals(tokenType) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Refresh token батлах
     */
    public Boolean isRefreshTokenValid(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return false;
            }
            
            Claims claims = getAllClaimsFromToken(token);
            String tokenType = (String) claims.get("type");
            
            return "refresh".equals(tokenType) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Refresh token validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Token expiration time (seconds) 
     */
    public Long getExpirationTime() {
        return jwtExpiration / 1000; // milliseconds -> seconds
    }

    /**
     * Signing key үүсгэх
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Token-оос Bearer prefix хасах
     */
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }
}
'@

# 4. SecurityConfig.java үүсгэх
$securityConfigPath = "backend/src/main/java/com/company/los/security/SecurityConfig.java"
$securityConfigContent = @'
package com.company.los.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security тохиргоо
 * JWT authentication болон authorization
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;

    /**
     * Password encoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication provider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Security filter chain тохиргоо
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/los/api/v1/auth/**",
                    "/los/api/v1/health/**",
                    "/los/actuator/**",
                    "/los/swagger-ui/**",
                    "/los/swagger-ui.html",
                    "/los/v3/api-docs/**",
                    "/los/h2-console/**"
                ).permitAll()
                
                // Admin endpoints
                .requestMatchers("/los/api/v1/admin/**").hasRole("ADMIN")
                
                // Manager endpoints  
                .requestMatchers("/los/api/v1/manager/**").hasAnyRole("ADMIN", "MANAGER")
                
                // Loan officer endpoints
                .requestMatchers(
                    "/los/api/v1/loan-applications/**",
                    "/los/api/v1/documents/**"
                ).hasAnyRole("ADMIN", "MANAGER", "LOAN_OFFICER")
                
                // Customer service endpoints
                .requestMatchers("/los/api/v1/customers/**").hasAnyRole("ADMIN", "MANAGER", "LOAN_OFFICER", "CUSTOMER_SERVICE")
                
                // Бусад бүх хүсэлт authentication шаардах
                .anyRequest().authenticated()
            );

        // JWT filter нэмэх
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        // H2 console-ын тулд frame options disable хийх
        http.headers(headers -> headers.frameOptions().sameOrigin());

        return http.build();
    }

    /**
     * CORS тохиргоо
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Frontend URL-үүд
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:3001", 
            "http://localhost:4200",
            "https://*.company.com"
        ));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
'@

# 5. AuthResponseDto.java үүсгэх
$authResponseDtoPath = "backend/src/main/java/com/company/los/dto/AuthResponseDto.java"
$authResponseDtoContent = @'
package com.company.los.dto;

import com.company.los.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Authentication Response DTO
 * Login хариуд буцаах мэдээлэл
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {

    /**
     * JWT access token
     */
    private String token;

    /**
     * Refresh token
     */
    private String refreshToken;

    /**
     * Token төрөл (Bearer)
     */
    private String tokenType;

    /**
     * Token-ийн хүчинтэй хугацаа (секунд)
     */
    private Long expiresIn;

    /**
     * Хэрэглэгчийн ID (UUID)
     */
    private UUID userId;

    /**
     * Хэрэглэгчийн нэр
     */
    private String username;

    /**
     * Хэрэглэгчийн имэйл
     */
    private String email;

    /**
     * Хэрэглэгчийн дүрүүд
     */
    private Set<Role> roles;

    /**
     * Нэвтрэх амжилттай эсэх
     */
    @Builder.Default
    private boolean success = true;

    /**
     * Хариуны мессеж
     */
    private String message;

    /**
     * Хэрэглэгчийн овог нэр
     */
    private String fullName;

    /**
     * Хэрэглэгчийн зураг
     */
    private String profileImage;

    /**
     * Анхны нэвтрэлт эсэх
     */
    @Builder.Default
    private boolean firstLogin = false;

    /**
     * Нууц үг солих шаардлагатай эсэх
     */
    @Builder.Default
    private boolean passwordChangeRequired = false;

    /**
     * Амжилттай хариу үүсгэх
     */
    public static AuthResponseDto success(String token, String refreshToken, Long expiresIn, 
                                        UUID userId, String username, String email, Set<Role> roles) {
        return AuthResponseDto.builder()
                .success(true)
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .userId(userId)
                .username(username)
                .email(email)
                .roles(roles)
                .message("Login successful")
                .build();
    }

    /**
     * Амжилтгүй хариу үүсгэх
     */
    public static AuthResponseDto failure(String message) {
        return AuthResponseDto.builder()
                .success(false)
                .message(message)
                .build();
    }
}
'@

# Функц: Файл үүсгэх
function Create-JavaFile {
    param($FilePath, $Content)
    
    try {
        # Directory үүсгэх
        $dir = Split-Path $FilePath -Parent
        if (!(Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
        }
        
        # Файл үүсгэх
        Set-Content -Path $FilePath -Value $Content -Encoding UTF8
        Write-Host "   ✅ Үүсгэсэн: $FilePath" -ForegroundColor Green
        $global:createdFiles += $FilePath
        return $true
    } catch {
        Write-Host "   ❌ Алдаа: $FilePath - $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Файлуудыг үүсгэх
Write-Host "📁 Java файлууд үүсгэж байна..." -ForegroundColor Cyan

$files = @(
    @{ Path = $baseRepoPath; Content = $baseRepoContent; Name = "BaseRepository.java" },
    @{ Path = $authServiceImplPath; Content = $authServiceImplContent; Name = "AuthServiceImpl.java" },
    @{ Path = $jwtUtilPath; Content = $jwtUtilContent; Name = "JwtUtil.java" },
    @{ Path = $securityConfigPath; Content = $securityConfigContent; Name = "SecurityConfig.java" },
    @{ Path = $authResponseDtoPath; Content = $authResponseDtoContent; Name = "AuthResponseDto.java" }
)

foreach ($file in $files) {
    if (!(Test-Path $file.Path)) {
        Create-JavaFile -FilePath $file.Path -Content $file.Content
    } else {
        Write-Host "   ⚠️  Файл аль хэдийн байна: $($file.Path)" -ForegroundColor Yellow
    }
}

# Нэмэлт support файлууд үүсгэх
Write-Host "📁 Нэмэлт support файлууд үүсгэж байна..." -ForegroundColor Cyan

# LoginRequestDto.java
$loginRequestDtoPath = "backend/src/main/java/com/company/los/dto/LoginRequestDto.java"
$loginRequestDtoContent = @'
package com.company.los.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Login хүсэлтийн DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    /**
     * Хэрэглэгчийн нэр эсвэл имэйл
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /**
     * Нууц үг
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    /**
     * Намайг санах (optional)
     */
    private boolean rememberMe = false;
}
'@

# AuthService.java interface
$authServicePath = "backend/src/main/java/com/company/los/service/AuthService.java"
$authServiceContent = @'
package com.company.los.service;

import com.company.los.dto.AuthResponseDto;
import com.company.los.dto.LoginRequestDto;
import com.company.los.entity.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Authentication Service Interface
 */
public interface AuthService {

    /**
     * Хэрэглэгч нэвтрэх
     */
    AuthResponseDto login(LoginRequestDto loginRequest);

    /**
     * Token сэргээх
     */
    AuthResponseDto refreshToken(String refreshToken);

    /**
     * Хэрэглэгч гарах
     */
    void logout(String token);

    /**
     * Token батлах
     */
    boolean validateToken(String token);

    /**
     * Одоогийн хэрэглэгчийн мэдээлэл авах
     */
    Optional<User> getCurrentUser(String token);

    /**
     * Нууц үг солих
     */
    void changePassword(UUID userId, String oldPassword, String newPassword);
}
'@

$supportFiles = @(
    @{ Path = $loginRequestDtoPath; Content = $loginRequestDtoContent; Name = "LoginRequestDto.java" },
    @{ Path = $authServicePath; Content = $authServiceContent; Name = "AuthService.java" }
)

foreach ($file in $supportFiles) {
    if (!(Test-Path $file.Path)) {
        Create-JavaFile -FilePath $file.Path -Content $file.Content
    } else {
        Write-Host "   ⚠️  Файл аль хэдийн байна: $($file.Path)" -ForegroundColor Yellow
    }
}

# Дүгнэлт
Write-Host ""
Write-Host "🎉 ФАЙЛ ҮҮСГЭЛТ ДУУССАН!" -ForegroundColor Green
Write-Host "═══════════════════════" -ForegroundColor Green

if ($createdFiles.Count -gt 0) {
    Write-Host "✅ Үүсгэгдсэн файлууд ($($createdFiles.Count)):" -ForegroundColor Green
    foreach ($file in $createdFiles) {
        Write-Host "   📄 $file" -ForegroundColor White
    }
    
    Write-Host ""
    Write-Host "🔧 Дараагийн алхмууд:" -ForegroundColor Yellow
    Write-Host "   1. Backend эхлүүлэх: cd backend && .\mvnw.cmd spring-boot:run" -ForegroundColor White
    Write-Host "   2. Progress шалгах: .\progress-tracker.ps1" -ForegroundColor White
    Write-Host "   3. API тест хийх: .\progress-tracker.ps1 -TestMode" -ForegroundColor White
    
    Write-Host ""
    Write-Host "⚠️  Анхаарах зүйлүүд:" -ForegroundColor Yellow
    Write-Host "   • Maven dependencies (JWT, Spring Security) шалгах" -ForegroundColor Gray
    Write-Host "   • Entity классуудад UUID fields нэмэх" -ForegroundColor Gray
    Write-Host "   • UserRepository-д findByUsername method нэмэх" -ForegroundColor Gray
    Write-Host "   • Application.yml-д JWT тохиргоо нэмэх" -ForegroundColor Gray
    
} else {
    Write-Host "ℹ️  Бүх файл аль хэдийн байна." -ForegroundColor Blue
}

Write-Host ""
Write-Host "🔄 Прогресс шалгах: .\progress-tracker.ps1" -ForegroundColor Cyan