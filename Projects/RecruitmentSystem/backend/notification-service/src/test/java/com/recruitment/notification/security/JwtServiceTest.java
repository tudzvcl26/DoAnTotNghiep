package com.recruitment.notification.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void acceptsAccessToken() {
        assertTrue(jwtService.validateToken(createToken("access")));
    }

    @Test
    void rejectsRefreshToken() {
        assertFalse(jwtService.validateToken(createToken("refresh")));
    }

    @Test
    void rejectsLegacyTokenWithoutPurpose() {
        assertFalse(jwtService.validateToken(createToken(null)));
    }

    private String createToken(String tokenType) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        var builder = Jwts.builder()
                .subject("security-test@example.test")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000));

        if (tokenType != null) {
            builder.claim("token_type", tokenType);
        }

        return builder.signWith(key).compact();
    }

}
