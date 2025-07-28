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