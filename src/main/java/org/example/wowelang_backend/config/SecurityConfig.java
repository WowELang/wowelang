package org.example.wowelang_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 토큰 없이도 API 호출 가능하도록 비활성화 (REST API 특성상)
                .csrf(csrf -> csrf.disable())
                // 세션을 사용하지 않고 JWT로 인증 관리
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 요청별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 회원가입과 로그인은 인증 없이 허용
                        .requestMatchers(HttpMethod.POST, "/user").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        // 이 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
        // (추후 JWT 필터 추가 등)
        ;
        return http.build();
    }
}
