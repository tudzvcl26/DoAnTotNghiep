package com.recruitment.auth.service;

import com.recruitment.auth.dto.response.AdminUserResponse;
import com.recruitment.auth.entity.Role;
import com.recruitment.auth.entity.User;
import com.recruitment.auth.exception.BusinessException;
import com.recruitment.auth.exception.ErrorCode;
import com.recruitment.auth.repository.RefreshTokenRepository;
import com.recruitment.auth.repository.RoleRepository;
import com.recruitment.auth.repository.UserRepository;
import com.recruitment.auth.security.CustomUserDetails;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserService {
    private static final Set<String> MANAGED_ROLES = Set.of("ADMIN", "EMPLOYER", "CANDIDATE");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getUsers(String keyword, String role, Boolean enabled, Pageable pageable) {
        String normalizedRole = normalizeRole(role);
        Specification<User> specification = (root, query, cb) -> {
            query.distinct(true);
            var predicate = cb.conjunction();
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                predicate = cb.and(predicate, cb.or(cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("fullName")), pattern)));
            }
            if (enabled != null) predicate = cb.and(predicate, cb.equal(root.get("enabled"), enabled));
            if (normalizedRole != null) {
                Join<User, Role> roles = root.join("roles", JoinType.INNER);
                predicate = cb.and(predicate, cb.equal(roles.get("name"), normalizedRole));
            }
            return predicate;
        };
        return userRepository.findAll(specification, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUser(UUID id) {
        return toResponse(requireUser(id));
    }

    public AdminUserResponse updateRoles(UUID id, Set<String> requestedRoles) {
        User user = requireUser(id);
        Set<String> normalized = requestedRoles.stream().map(this::normalizeRequiredRole).collect(java.util.stream.Collectors.toSet());
        if (isCurrentUser(user) && !normalized.contains("ADMIN")) {
            throw new BusinessException(ErrorCode.ADMIN_SELF_LOCKOUT);
        }
        Set<Role> roles = new HashSet<>();
        normalized.forEach(name -> roles.add(roleRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND))));
        user.setRoles(roles);
        refreshTokenRepository.deleteAllByUser(user);
        return toResponse(userRepository.save(user));
    }

    public AdminUserResponse updateEnabled(UUID id, boolean enabled) {
        User user = requireUser(id);
        if (isCurrentUser(user) && !enabled) throw new BusinessException(ErrorCode.ADMIN_SELF_LOCKOUT);
        user.setEnabled(enabled);
        refreshTokenRepository.deleteAllByUser(user);
        return toResponse(userRepository.save(user));
    }

    private User requireUser(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) return null;
        return normalizeRequiredRole(role);
    }

    private String normalizeRequiredRole(String role) {
        String normalized = role == null ? "" : role.replaceFirst("^ROLE_", "").toUpperCase(Locale.ROOT);
        if (!MANAGED_ROLES.contains(normalized)) throw new BusinessException(ErrorCode.INVALID_ROLE);
        return normalized;
    }

    private boolean isCurrentUser(User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details
                && details.getUser().getId().equals(user.getId());
    }

    private AdminUserResponse toResponse(User user) {
        return AdminUserResponse.builder().id(user.getId()).email(user.getEmail()).fullName(user.getFullName())
                .phone(user.getPhone()).enabled(user.getEnabled()).verified(user.getVerified())
                .roles(user.getRoles().stream().map(Role::getName).sorted().toList())
                .lastLoginAt(user.getLastLoginAt()).createdAt(user.getCreatedAt()).updatedAt(user.getUpdatedAt()).build();
    }
}
