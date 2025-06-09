package org.example.wowelang_backend.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.auth.dto.LoginRequestDto;
import org.example.wowelang_backend.auth.dto.LoginResponseDto;
import org.example.wowelang_backend.auth.dto.UserPrincipalDTO;
import org.example.wowelang_backend.security.custom.CustomUserDetails;
import org.example.wowelang_backend.security.jwt.JwtTokenProvider;
import org.example.wowelang_backend.security.jwt.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

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
    public LoginResponseDto login(LoginRequestDto dto) {
        Authentication auth;
        auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.getLoginId(), dto.getPassword())
        );

        CustomUserDetails cd = (CustomUserDetails) auth.getPrincipal();
        Long userId    = cd.getId();
        String loginId = cd.getUsername();
        List<String> roles = cd.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String accessToken = jwtTokenProvider.createAccessToken(userId, loginId, roles);

        UserPrincipalDTO userPrincipalDTO = UserPrincipalDTO.builder()
            .id(userId)
            .loginId(loginId)
            .roles(roles)
            .build();

        String refreshToken = jwtTokenProvider.createRefreshToken(userPrincipalDTO ,refreshTtl);

        return new LoginResponseDto(accessToken, refreshToken);
    }
}
