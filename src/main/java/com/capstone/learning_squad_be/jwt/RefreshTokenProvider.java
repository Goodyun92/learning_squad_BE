package com.capstone.learning_squad_be.jwt;

import com.capstone.learning_squad_be.security.CustomUserDetailsService;

public class RefreshTokenProvider extends JwtTokenProvider{
    public RefreshTokenProvider(CustomUserDetailsService customUserDetailsService,String secretKey, long tokenValidTime) {
        super(customUserDetailsService,secretKey,tokenValidTime);
    }
}
