package com.recruitment.application.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "token_type";

    private static final String ACCESS_TOKEN_TYPE = "access";

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public List<String> extractRoles(String token) {
        Object value = extractAllClaims(token).get("roles");
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(Object::toString)
                    .toList();
        }
        return List.of();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return ACCESS_TOKEN_TYPE.equals(
                    claims.get(TOKEN_TYPE_CLAIM, String.class)
            );
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean isExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

}
