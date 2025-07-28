package com.company.los.service;

import com.company.los.entity.User;
import com.company.los.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Нэмэлт User Details Service
 * UserServiceImpl-тэй давхцахгүйн тулд UserDetailsService implement хийхгүй
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService {

    private final UserRepository userRepository;

    /**
     * Email-ээр хэрэглэгч хайх
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("User not found by email: {}", email);
                return new UsernameNotFoundException("User not found by email: " + email);
            });

        log.debug("User loaded by email successfully: {}", email);
        return user;
    }

    /**
     * Username эсвэл email-ээр хэрэглэгч хайх
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsernameOrEmail(String identifier) throws UsernameNotFoundException {
        log.debug("Loading user by username or email: {}", identifier);
        
        User user = userRepository.findByUsernameOrEmail(identifier)
            .orElseThrow(() -> {
                log.error("User not found by username or email: {}", identifier);
                return new UsernameNotFoundException("User not found: " + identifier);
            });

        log.debug("User loaded by username or email successfully: {}", identifier);
        return user;
    }

    /**
     * Employee ID-аар хэрэглэгч хайх
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByEmployeeId(String employeeId) throws UsernameNotFoundException {
        log.debug("Loading user by employee ID: {}", employeeId);
        
        User user = userRepository.findByEmployeeId(employeeId)
            .orElseThrow(() -> {
                log.error("User not found by employee ID: {}", employeeId);
                return new UsernameNotFoundException("User not found by employee ID: " + employeeId);
            });

        log.debug("User loaded by employee ID successfully: {}", employeeId);
        return user;
    }
}