package org.example.wowelang_backend.security.jwt;

import lombok.RequiredArgsConstructor;

import org.example.wowelang_backend.common.apiPayLoad.status.ErrorStatus;
import org.example.wowelang_backend.user.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // 1) Authorization 헤더에서 "Bearer {token}" 추출
        String accessHeader = checkBearer(request.getHeader("Authorization"));

        if (accessHeader != null && jwtTokenProvider.validateAccessToken(accessHeader)) {
            setAuth(jwtTokenProvider, accessHeader);
        } else {
            String refreshToken = extractRefresh(request);
            if(refreshToken != null) {
                reIssue(accessHeader, refreshToken, response);
            }
        }
        // 4) 다음 필터 실행
        filterChain.doFilter(request, response);
    }

    private void reIssue(String oldAccess, String refreshToken, HttpServletResponse response) {
        RefreshToken refreshTokenObj = refreshTokenRepository.findByRefreshToken(refreshToken)
            .filter(refresh -> !refresh.isExpired())
            .orElseThrow(() -> new ResponseStatusException(ErrorStatus.TOKEN_INVALID.getHttpStatus(), ErrorStatus.TOKEN_INVALID.getMessage()));

        User user = refreshTokenObj.getUser();

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getLoginId(),
                                                                    List.of("ROLE_" + user.getUsertype()));

        response.setHeader("accessToken", newAccessToken);

        setAuth(jwtTokenProvider, newAccessToken);
    }

    private void setAuth(JwtTokenProvider jwtTokenProvider, String token) {
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String checkBearer(String header) {
        return (header != null && header.startsWith("Bearer "))
            ? header.substring(7) : null;
    }

    private String extractRefresh(HttpServletRequest request) {
        return request.getHeader("refreshToken");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 토큰 없이도 접근 가능한 엔드포인트
        // 1) POST /user               (회원가입)
        if ("/user".equals(path) && "POST".equals(method)) {
            return true;
        }
        // 2) POST /user/check-login-id (아이디 중복 확인)
        if ("/user/check-login-id".equals(path) && "POST".equals(method)) {
            return true;
        }
        // 3) POST /user/email-verification (메일발송/초기화)
        if (path.matches("^/user/email-verification$") && "POST".equals(method)) {
            return true;
        }
        // 4) PATCH /user/{id}/complete (코드검증+가입완료)
        if (path.matches("^/user/complete$") && "PATCH".equals(method)) {
            return true;
        }
        // 5) POST /auth/login         (로그인)
        if ("/auth/login".equals(path) && "POST".equals(method)) {
            return true;
        }
        // 6) DELETE /user/email-verification (메일발송/초기화)
        if (path.matches("^/user/email-verification$") && "DELETE".equals(method)) {
            return true;
        }

        // 그 외 모든 요청은 JWT 검사 대상
        return false;
    }
}
