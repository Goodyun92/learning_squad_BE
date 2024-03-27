package com.capstone.learning_squad_be.config;

import com.capstone.learning_squad_be.security.CustomUserDetailsService;
import com.capstone.learning_squad_be.security.filter.JwtExceptionFilter;
import com.capstone.learning_squad_be.security.filter.JwtTokenFilter;
import com.capstone.learning_squad_be.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private final JwtExceptionFilter jwtExceptionFilter;

    @Value("${jwt.token.secret}")
    private String secretKey;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        return httpSecurity
                .httpBasic().disable()
                .csrf().disable()
                .cors().and()
                .authorizeRequests()
                .antMatchers("/api/users/join","/api/users/login","/api/users/refresh").permitAll() //join, login, refresh 항상 허용
                .antMatchers("/api/**").authenticated() //인증 필요
                .antMatchers("/**").hasAuthority("ROLE_ADMIN") // 'ADMIN' 역할을 가진 사용자에게 모든 경로에 대한 접근 허용
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //jwt사용
                .and()
                .addFilterBefore(new JwtTokenFilter(userService, secretKey), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, JwtTokenFilter.class) // JwtExceptionFilter 추가
                .build();
    }
}
