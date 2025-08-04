package com.company.los.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails; 

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JWT Token Utility класс
 * Token үүсгэх, баталгаажуулах, мэдээлэл гаргах
 * ⭐ ЗАСВАРЛАСАН - extractUsername method нэмэгдсэн ⭐
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

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
     * Access token үүсгэх (UserDetails ашиглан)
     */
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        // Дүрүүдийг claims-д нэмэх
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toList()));
        return createToken(claims, userDetails.getUsername(), jwtExpiration);
    }

    /**
     * Refresh token үүсгэх (UserDetails ашиглан)
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
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
     * ⭐ ЗАСВАРЛАСАН: Token-оос username гаргах (AuthServiceImpl-аас дуудагдаж байгаа) ⭐
     */
    public String extractUsername(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Token-оос username гаргах (хуучин нэр)
     */
    public String getUsernameFromToken(String token) {
        return extractUsername(token);
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
     * Access token-ийн хүчинтэй хугацаа (секундээр)
     */
    public Long getAccessTokenExpiration() {
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