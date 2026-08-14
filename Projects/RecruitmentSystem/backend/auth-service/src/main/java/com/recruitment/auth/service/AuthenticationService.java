package com.recruitment.auth.service;

import com.recruitment.auth.dto.request.LoginRequest;
import com.recruitment.auth.dto.request.RegisterRequest;
import com.recruitment.auth.dto.request.RegistrationRole;
import com.recruitment.auth.dto.response.AuthResponse;
import com.recruitment.auth.entity.RefreshToken;
import com.recruitment.auth.entity.Role;
import com.recruitment.auth.entity.User;
import com.recruitment.auth.exception.BusinessException;
import com.recruitment.auth.exception.ErrorCode;
import com.recruitment.auth.repository.RefreshTokenRepository;
import com.recruitment.auth.repository.RoleRepository;
import com.recruitment.auth.repository.UserRepository;
import com.recruitment.auth.security.CustomUserDetails;
import com.recruitment.auth.security.JwtProperties;
import com.recruitment.auth.security.JwtService;
import com.recruitment.auth.security.RefreshTokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import com.recruitment.auth.dto.response.UserProfileResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenHasher refreshTokenHasher;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        RegistrationRole registrationRole = request.getRole() == null
                ? RegistrationRole.CANDIDATE
                : request.getRole();
        Role role = roleRepository.findByName(registrationRole.name())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .enabled(true)
                .verified(false)
                .roles(new HashSet<>())
                .build();

        user.getRoles().add(role);

        userRepository.save(user);

        return generateAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        user.setLastLoginAt(LocalDateTime.now());

        userRepository.save(user);

        refreshTokenRepository.deleteAllByUser(user);

        return generateAuthResponse(user);
    }
    public AuthResponse refreshToken(String refreshToken) {

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(refreshTokenHasher.hash(refreshToken))
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND)
                );

        if (Boolean.TRUE.equals(storedToken.getRevoked())) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_REVOKED);
        }

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        storedToken.setRevoked(true);

        refreshTokenRepository.save(storedToken);

        return generateAuthResponse(storedToken.getUser());

    }

    public void logout(String refreshToken) {

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(refreshTokenHasher.hash(refreshToken))
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND)
                );

        storedToken.setRevoked(true);

        refreshTokenRepository.save(storedToken);

    }

    public UserProfileResponse getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .enabled(user.getEnabled())
                .verified(user.getVerified())
                .roles(
                        user.getRoles()
                                .stream()
                                .map(Role::getName)
                                .toList()
                )
                .build();

    }
    private AuthResponse generateAuthResponse(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(refreshTokenHasher.hash(refreshToken))
                .expiresAt(
                        LocalDateTime.now().plusSeconds(
                                jwtProperties.getRefreshTokenExpiration() / 1000
                        )
                )
                .revoked(false)
                .build();

        refreshTokenRepository.save(token);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpiration())
                .build();
    }
}
