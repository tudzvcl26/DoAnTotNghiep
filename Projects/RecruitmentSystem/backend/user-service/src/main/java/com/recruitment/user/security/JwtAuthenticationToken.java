package com.recruitment.user.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final CurrentUser principal;

    private final String token;

    public JwtAuthenticationToken(
            CurrentUser principal,
            String token
    ) {

        super(null);

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