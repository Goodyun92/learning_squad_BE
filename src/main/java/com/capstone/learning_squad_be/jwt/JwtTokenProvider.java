package com.capstone.learning_squad_be.jwt;

import com.capstone.learning_squad_be.domain.enums.ErrorCode;
import com.capstone.learning_squad_be.domain.enums.Role;
import com.capstone.learning_squad_be.security.CustomUserDetailsService;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;

@Slf4j
public class JwtTokenProvider implements AuthenticationProvider {

    private String secretKey;

    private long tokenValidTime;

    private final CustomUserDetailsService customUserDetailsService;

    public JwtTokenProvider(CustomUserDetailsService customUserDetailsService,String secretKey, long tokenValidTime) {
        this.customUserDetailsService = customUserDetailsService;
        String formattedSecretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        this.secretKey = formattedSecretKey;
        this.tokenValidTime = tokenValidTime;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = (String) authentication.getCredentials();
        String username = getUsername(token);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    // JWT 토큰 생성
    public String createToken(String userName, Role role) {

        Claims claims = Jwts.claims().setSubject(userName); // JWT payload 에 저장되는 정보단위
        claims.put("role", role.name());//권한 저장

        return  Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tokenValidTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact()
                ;
    }

    // JWT 토큰에서 인증 정보 조회
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(this.getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }

    // 토큰에서 회원 정보 추출
    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    // Request의 Header에서 token 값을 가져옴
    public String resolveToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7); // "Bearer "를 제외한 문자열 반환
        }
        return null; // 토큰이 없는 경우 null 반환
    }

    // 토큰의 유효성 + 만료일자 확인
    public boolean validateToken(String jwtToken) {

        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken);
            log.info("token valid");
            return !claims.getBody().getExpiration().before(new Date());
        } catch (SecurityException e) {
            log.info("Invalid JWT signature.");
            throw new JwtException("잘못된 JWT 시그니처");
        } catch (MalformedJwtException e) {
            log.info("Invalid JWT token.");
            throw new JwtException("유효하지 않은 JWT 토큰");
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token.");
            throw new JwtException("토큰 기한 만료");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token.");
        } catch (IllegalArgumentException e) {
            log.info("JWT token compact of handler are invalid.");
            throw new JwtException("JWT token compact of handler are invalid.");
        } catch (SignatureException e) {
            log.error("JWT signature does not match locally computed signature");
            throw new JwtException("JWT 서명이 일치하지 않습니다");
        }
        //메시지 설정해서 JwtException 던져 주기

        return false;

    }
}
