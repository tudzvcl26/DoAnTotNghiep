package com.recruitment.auth.security;

import com.recruitment.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "token_type";

    private static final String ACCESS_TOKEN_TYPE = "access";

    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    private SecretKey getSigningKey() {

        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());

        return Keys.hmacShaKeyFor(keyBytes);

    }

    public String generateAccessToken(UserDetails userDetails) {

        return generateAccessToken(Map.of(), userDetails);

    }

    public String generateAccessToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {

        Map<String, Object> claims = new HashMap<>(extraClaims);

        claims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE);

        return buildToken(
                claims,
                userDetails,
                jwtProperties.getAccessTokenExpiration()
        );

    }

    public String generateRefreshToken(UserDetails userDetails) {

        return buildToken(
                Map.of(
                        "jti", UUID.randomUUID().toString(),
                        TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE
                ),
                userDetails,
                jwtProperties.getRefreshTokenExpiration()
        );

    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            Long expiration
    ) {

        Date now = new Date();

        Date expiry = new Date(now.getTime() + expiration);

        Map<String, Object> claims = new HashMap<>(extraClaims);

        claims.put(
                "roles",
                extractRoles(userDetails.getAuthorities())
        );

        if (userDetails instanceof CustomUserDetails customUserDetails) {

            User user = customUserDetails.getUser();

            claims.put(
                    "userId",
                    user.getId().toString()
            );

            claims.put(
                    "email",
                    user.getEmail()
            );

        }

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();

    }

    private List<String> extractRoles(
            Collection<? extends GrantedAuthority> authorities
    ) {

        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

    }

    public String extractUsername(String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );

    }

    public Date extractExpiration(String token) {

        return extractClaim(
                token,
                Claims::getExpiration
        );

    }

    public List<String> extractRoles(String token) {

        Claims claims = extractAllClaims(token);

        Object value = claims.get("roles");

        if (value instanceof List<?>) {

            return ((List<?>) value)
                    .stream()
                    .map(Object::toString)
                    .toList();

        }

        return List.of();

    }

    public String extractEmail(String token) {

        return extractClaim(
                token,
                claims -> claims.get("email", String.class)
        );

    }

    public String extractUserId(String token) {

        return extractClaim(
                token,
                claims -> claims.get("userId", String.class)
        );

    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {

        Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);

    }

    public Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    public boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new Date());

    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);

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

}
