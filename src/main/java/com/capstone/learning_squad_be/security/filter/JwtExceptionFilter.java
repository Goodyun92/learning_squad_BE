package com.capstone.learning_squad_be.security.filter;

import com.capstone.learning_squad_be.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtExceptionFilter(@Qualifier("access")JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("exception filter is running");

        String path = request.getRequestURI();

        // 회원가입, 로그인, 토큰 갱신 요청, 루트 경로 및 actuator/health에 대해서는 토큰 필터를 적용하지 않음
        if (path.startsWith("/api/users/join") || path.startsWith("/api/users/login") || path.startsWith("/api/users/refresh") || path.equals("/") || path.startsWith("/actuator/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 가져오기
        String token = jwtTokenProvider.resolveToken(request);

        // 토큰이 null이면 예외 처리
        if (token == null) {
            log.info("JWT token is missing.");
            setErrorResponse(request, response, new JwtException("JWT token is missing"));
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } catch (JwtException ex) {
            setErrorResponse(request, response, ex);
        }
    }

    public void setErrorResponse(HttpServletRequest req, HttpServletResponse res, Throwable ex) throws IOException {

        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", ex.getMessage());   // ex.getMessage() 는 jwtException을 발생시키면서 입력한 메세지
        body.put("path", req.getServletPath());
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(res.getOutputStream(), body);
    }



}
