package com.recruitment.ai.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET =
            "4F8A9B72D35E1C847A91F6D28C5B9E73F84A9D21E6C4B7A15D8F2C93E7A4B6F18";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        jwtService = new JwtService(properties);
    }

    @Test
    void acceptsOnlyAccessTokens() {
        assertThat(jwtService.validateToken(token("access"))).isTrue();
        assertThat(jwtService.validateToken(token("refresh"))).isFalse();
        assertThat(jwtService.validateToken("not-a-jwt")).isFalse();
    }

    private String token(String tokenType) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        return Jwts.builder()
                .subject("test@example.com")
                .claim("token_type", tokenType)
                .claim("userId", UUID.randomUUID().toString())
                .claim("email", "test@example.com")
                .claim("roles", List.of("ROLE_CANDIDATE"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)
                .compact();
    }

}
