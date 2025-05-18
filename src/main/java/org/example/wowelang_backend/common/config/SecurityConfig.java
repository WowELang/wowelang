package org.example.wowelang_backend.common.config;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.security.jwt.JwtAuthenticationFilter;
import org.example.wowelang_backend.security.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;


@Configuration
@RequiredArgsConstructor
@EnableWebSecurity(debug = true)
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 토큰 없이도 API 호출 가능하도록 비활성화 (REST API 특성상)
                .csrf(csrf -> csrf.disable())
                //백엔드 도메인(8080) - 프론트 도메인(3000) 도메인 연결 허용 -서로 다른 도메인은 원래 통신이 안됨
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // 세션을 사용하지 않고 JWT로 인증 관리
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 요청별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // Pre-flight(OPTIONS) 요청 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 회원가입과 로그인은 인증 없이 허용
                        .requestMatchers(HttpMethod.POST, "/user").permitAll()
                        .requestMatchers(HttpMethod.POST, "/user/*/email-verification").permitAll()
                        .requestMatchers(HttpMethod.POST, "/user/check-login-id").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/user/*/complete").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/user/*/email-verification").permitAll()
                        .requestMatchers("/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/env", "/hc").permitAll()
                        // 이 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter(spring 기본 로그인 처리 필터) 앞에 등록
                //JWT 방식으로 들어오는 요청은 “폼 로그인”이 아니라 “헤더에 달린 토큰” 기반으로 인증을 해야 하므로,
                //폼 로그인 필터가 동작하기 전에 JWT 필터가 먼저 돌면서 인증 여부를 결정하도록 순서를 조정
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig
    ) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
