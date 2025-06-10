package org.example.wowelang_backend.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.auth.dto.LoginRequestDto;
import org.example.wowelang_backend.auth.dto.JwtDto;
import org.example.wowelang_backend.auth.dto.UserPrincipalDTO;
import org.example.wowelang_backend.common.apiPayLoad.status.ErrorStatus;
import org.example.wowelang_backend.security.custom.CustomUserDetails;
import org.example.wowelang_backend.security.jwt.JwtTokenProvider;
import org.example.wowelang_backend.security.jwt.RefreshToken;
import org.example.wowelang_backend.security.jwt.RefreshTokenRepository;
import org.example.wowelang_backend.user.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Security;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTtl;

    // 1) 로그인 로직: 아이디 → 인증 → User 엔티티
    public JwtDto login(LoginRequestDto loginRequestDto) {
        Authentication auth;
        auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequestDto.getLoginId(), loginRequestDto.getPassword())
        );

        // 유저 Id, 로그인 Id, 역할을 담은 유저 디테일 -> 토큰 생성 시 필요
        CustomUserDetails cd = (CustomUserDetails) auth.getPrincipal();
        Long userId    = cd.getId();
        String loginId = cd.getUsername();
        List<String> roles = cd.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // 액세스 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(userId, loginId, roles);

        // 유저 Principal생성
        UserPrincipalDTO userPrincipalDTO = UserPrincipalDTO.builder()
            .id(userId)
            .loginId(loginId)
            .roles(roles)
            .build();

        // 리프레시 토큰 생성
        String refreshToken = jwtTokenProvider.createRefreshToken(userPrincipalDTO ,refreshTtl);

        return new JwtDto(accessToken, refreshToken);
    }

    // 액세스 토큰 만료 시 액세스 토큰, 리프레시 토큰 모두 재발행 (RTR 방식)
    @Transactional
    public JwtDto reIssueAccessToken(String refreshToken, String expiredAccessTokenHeader) {

        // 헤더에서 액세스 토큰 추출
        String expiredAccessToken = jwtTokenProvider.extractAccessToken(expiredAccessTokenHeader);

        // 액세스 토큰 밸리데이션 체크
        jwtTokenProvider.validateAccessToken(expiredAccessToken);

        // 유저의 리프레시 토큰과 db의 리프레시 토큰 대조
        RefreshToken refreshTokenObj = refreshTokenRepository.findByRefreshToken(refreshToken)
            .orElseThrow(this::unauthorized);

        // 2-1) 만료 or 이미 삭제된 토큰 ⇒ 로그아웃
        if (refreshTokenObj.isExpired() || refreshTokenObj.isRevoked()) {
            refreshTokenObj.revokeRefreshToken();          // 논리 삭제
            throw unauthorized();
        }

        // 액세스 토큰의 유저아이디와 리프레시 토큰이 참조하는 유저아이디가 같은 지 확인
        String accessTokenUserId = jwtTokenProvider.getUserId(expiredAccessToken);
        if(!accessTokenUserId.equals(String.valueOf(refreshTokenObj.getUser().getId()))) {
            throw new ResponseStatusException(
                ErrorStatus.TOKEN_INVALID.getHttpStatus(),
                ErrorStatus.TOKEN_INVALID.getMessage()
            );
        }

        // 유저 엔티티 생성 후 Principal 생성
        User user = refreshTokenObj.getUser();
        List<String> roles = List.of("ROLE_" + user.getUsertype());

        UserPrincipalDTO userPrincipalDTO = UserPrincipalDTO.builder()
            .id(user.getId())
            .loginId(user.getLoginId())
            .roles(roles)
            .build();

        // 리프레시 토큰 삭제(논리)
        refreshTokenObj.revokeRefreshToken();

        // 새로운 리프레시 토큰 생성 (RTR 방식)
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userPrincipalDTO ,refreshTtl);

        // 새로운 액세스 토큰 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getLoginId(), roles);

        return new JwtDto(newAccessToken, newRefreshToken);
    }

    // 공통 401 반환
    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(
            ErrorStatus.TOKEN_INVALID.getHttpStatus(),
            ErrorStatus.TOKEN_INVALID.getMessage());
    }

    @Transactional
    public void logout(String refreshToken) {

        // DB에서 리프레시 토큰 찾아와서 논리삭제
        refreshTokenRepository.findByRefreshToken(refreshToken).ifPresent(RefreshToken::revokeRefreshToken);

        // TODO : 블랙리스트 방식은 고려사항 (급한건 아님)

        // SecurityContext 비우기
        SecurityContextHolder.clearContext();
    }
}
