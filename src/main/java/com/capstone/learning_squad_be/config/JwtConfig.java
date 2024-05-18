package com.capstone.learning_squad_be.config;

import com.capstone.learning_squad_be.jwt.JwtTokenProvider;
import com.capstone.learning_squad_be.jwt.RefreshTokenProvider;
import com.capstone.learning_squad_be.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Value("${jwt.token.secret}")
    private String accessKey;

    @Value("${jwt.token.refresh}")
    private String refreshKey;

    private long accessExpireTimeMs = 1000 * 60 * 60 * 24 * 30L;    //한달
//    private long accessExpireTimeMs = 1000 * 60L;    //1분

    private long refreshExpireTimeMs = 1000 * 60 * 60 * 24 * 30L;   //한달
//    private long refreshExpireTimeMs = 1000 * 60 * 2L;   //2분

    @Bean
    @Qualifier("access")
    public JwtTokenProvider accessJwtTokenProvider() {
        JwtTokenProvider accessJwtTokenProvider = new JwtTokenProvider(customUserDetailsService,accessKey,accessExpireTimeMs);
        return accessJwtTokenProvider;
    }

    @Bean
    @Qualifier("refresh")
    public RefreshTokenProvider refreshJwtTokenProvider() {
        RefreshTokenProvider refreshJwtTokenProvider = new RefreshTokenProvider(customUserDetailsService,refreshKey,refreshExpireTimeMs);
        return refreshJwtTokenProvider;
    }
}
