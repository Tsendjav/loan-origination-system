package com.company.los.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * JWT Request Filter
 *
 * HTTP хүсэлт бүрт Authorization: Bearer &lt;token&gt; header-ийг шалгаж,
 * token хүчинтэй бол SecurityContext-д authentication тавина.
 *
 * ⭐ ЗАСВАР: өмнөх хувилбар нь зөвхөн placeholder байсан (token-оос username
 * уншиж чаддаггүй, `if (false)` салбартай) тул JWT-тэй бүх protected endpoint
 * 401 буцаадаг байсан. Одоо JwtUtil ашиглан бодитоор баталгаажуулна.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtRequestFilter(@Lazy JwtUtil jwtUtil, @Lazy UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain chain) throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwtToken);
            } catch (Exception e) {
                log.warn("JWT Token дээрээс username авч чадсангүй: {}", e.getMessage());
            }
        } else {
            log.debug("JWT Token Bearer string-ээр эхлэхгүй байна");
        }

        if (username != null
                && SecurityContextHolder.getContext().getAuthentication() == null
                && jwtUtil.isTokenValid(jwtToken)) {

            Collection<? extends GrantedAuthority> authorities = resolveAuthorities(username, jwtToken);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("✅ JWT баталгаажлаа: {} ({} эрх)", username, authorities.size());
        }

        chain.doFilter(request, response);
    }

    /**
     * Хэрэглэгчийн эрхийг тодорхойлох.
     * Эхлээд DB-ээс UserDetails ачаалж үзнэ (role + permission).
     * Олдохгүй бол token доторх "roles" claim-ийг ашиглана (жишээ нь dev test users).
     */
    private Collection<? extends GrantedAuthority> resolveAuthorities(String username, String token) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (userDetails != null && userDetails.getAuthorities() != null
                    && !userDetails.getAuthorities().isEmpty()) {
                return userDetails.getAuthorities();
            }
        } catch (Exception e) {
            log.debug("UserDetails ачаалж чадсангүй, token claim ашиглана: {} - {}", username, e.getMessage());
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : jwtUtil.getRoles(token)) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }

    /**
     * Public endpoints-д JWT шалгалт хийхгүй
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        return path.startsWith("/h2-console") ||
               path.startsWith("/los/h2-console") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/los/swagger-ui") ||
               path.startsWith("/api-docs") ||
               path.startsWith("/los/api-docs") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/los/v3/api-docs") ||
               path.startsWith("/actuator") ||
               path.startsWith("/los/actuator") ||
               path.startsWith("/login") ||
               path.startsWith("/logout") ||
               path.startsWith("/css") ||
               path.startsWith("/js") ||
               path.startsWith("/images") ||
               path.equals("/favicon.ico") ||
               path.equals("/error");
    }
}
