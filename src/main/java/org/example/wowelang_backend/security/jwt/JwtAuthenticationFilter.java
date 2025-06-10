package org.example.wowelang_backend.security.jwt;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;

import org.example.wowelang_backend.common.apiPayLoad.status.ErrorStatus;
import org.example.wowelang_backend.user.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    // 필터에서는 액세스 토큰의 서명만 검증함 (DB 체크 X)
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String accessTokenWithBearer = request.getHeader("Authorization");

        String accessToken = jwtTokenProvider.extractAccessToken(accessTokenWithBearer);

        if (jwtTokenProvider.validateAccessToken(accessToken)) {
            setAuth(jwtTokenProvider, accessToken);
        }

        // 4) 다음 필터 실행
        filterChain.doFilter(request, response);
    }

    // 시큐리티 컨텍스트에 유저정보를 넣어줌
    private void setAuth(JwtTokenProvider jwtTokenProvider, String token) {
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 필터를 생략할 URL 패턴 정의
        Map<String, Set<String>> whitelist = Map.of(
            "OPTIONS", Set.of("/**"), // CORS Preflight 요청 허용
            "POST", Set.of(
                "/user",                       // 회원가입
                "/user/email-verification",    // 이메일 인증 요청
                "/user/check-login-id",        // 아이디 중복 체크
                "/auth/login"                  // 로그인
            ),
            "PATCH", Set.of(
                "/user/complete"               // 가입 완료
            ),
            "DELETE", Set.of(
                "/user/email-verification"     // 이메일 인증 초기화
            )
        );

        // Swagger 및 기타 인증 제외 경로
        Set<String> excludedPaths = Set.of(
            "/", "/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/env", "/hc", "/error"
        );

        // exclude 경로 매칭
        for (String excluded : excludedPaths) {
            if (PATH_MATCHER.match(excluded, path)) return true;
        }

        // HTTP 메서드별 화이트리스트 매칭
        return whitelist.getOrDefault(method, Set.of())
            .stream()
            .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
}
