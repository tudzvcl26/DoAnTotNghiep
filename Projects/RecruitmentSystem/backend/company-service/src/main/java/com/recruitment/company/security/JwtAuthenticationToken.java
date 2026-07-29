package com.recruitment.company.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final CurrentUser principal;

    private final String token;

    public JwtAuthenticationToken(
            CurrentUser principal,
            String token,
            Collection<? extends GrantedAuthority> authorities
    ) {

        super(authorities);

        this.principal = principal;
        this.token = token;

        setAuthenticated(true);

    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public CurrentUser getPrincipal() {
        return principal;
    }

}
