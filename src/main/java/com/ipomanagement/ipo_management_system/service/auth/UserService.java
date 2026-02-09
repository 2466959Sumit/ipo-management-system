package com.ipomanagement.ipo_management_system.service.auth;

import com.ipomanagement.ipo_management_system.domain.entity.User;
import com.ipomanagement.ipo_management_system.domain.enums.Role;
import com.ipomanagement.ipo_management_system.repository.UserRepository;
import com.ipomanagement.ipo_management_system.service.audit.AuditService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }

    @Transactional
    public User registerInvestor(String username, String email, String rawPassword) {
        if (userRepository.existsByUsername(username)) throw new IllegalArgumentException("Username already exists");
        if (userRepository.existsByEmail(email)) throw new IllegalArgumentException("Email already exists");

        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setRole(Role.INVESTOR);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setEnabled(true);

        User saved = userRepository.save(u);

        auditService.log(saved.getUserId(), "INVESTOR_REGISTERED", "USER", saved.getUserId(),
                "Investor self-registered");
        return saved;
    }

    @Transactional
    public User createUserByAdmin(String username, String email, String rawPassword, Role role, Long adminUserId) {
        if (userRepository.existsByUsername(username)) throw new IllegalArgumentException("Username already exists");
        if (userRepository.existsByEmail(email)) throw new IllegalArgumentException("Email already exists");

        if (role != Role.ADMIN && role != Role.ISSUER) {
            throw new IllegalArgumentException("Admin can only create ADMIN or ISSUER users");
        }

        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setRole(role);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setEnabled(true);

        User saved = userRepository.save(u);

        auditService.log(adminUserId, "ADMIN_CREATED_USER", "USER", saved.getUserId(),
                "Created user role=" + role.name() + ", username=" + username);

        return saved;
    }

    // Keep this for DataInitializer usage (no admin user id available there)
    @Transactional
    public User createUserByAdmin(String username, String email, String rawPassword, Role role) {
        if (userRepository.existsByUsername(username)) throw new IllegalArgumentException("Username already exists");
        if (userRepository.existsByEmail(email)) throw new IllegalArgumentException("Email already exists");

        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setRole(role);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setEnabled(true);

        return userRepository.save(u);
    }
}