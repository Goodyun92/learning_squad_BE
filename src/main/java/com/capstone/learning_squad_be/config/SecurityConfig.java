package com.capstone.learning_squad_be.config;

import com.capstone.learning_squad_be.jwt.JwtTokenProvider;
import com.capstone.learning_squad_be.security.JwtAccessDeniedHandler;
import com.capstone.learning_squad_be.security.JwtAuthenticationEntryPoint;
import com.capstone.learning_squad_be.security.filter.JwtExceptionFilter;
import com.capstone.learning_squad_be.security.filter.JwtTokenFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter{

    private final JwtExceptionFilter jwtExceptionFilter;
    private final JwtTokenProvider jwtTokenProvider;


    public SecurityConfig(
            @Qualifier("access")JwtTokenProvider jwtTokenProvider,
            JwtExceptionFilter jwtExceptionFilter
    ){
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtExceptionFilter = jwtExceptionFilter;
    }


    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .cors().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/users/join", "/api/users/login", "/api/users/refresh","/","/actuator/health").permitAll()
                .antMatchers("/api/**").authenticated()
                .antMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                .and()
                .addFilterBefore(new JwtTokenFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, JwtTokenFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()) // 인증 예외 처리 핸들러
                .accessDeniedHandler(new JwtAccessDeniedHandler()); // 인가 예외 처리 핸들러
    }

}
