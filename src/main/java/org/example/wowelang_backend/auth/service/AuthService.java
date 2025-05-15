package org.example.wowelang_backend.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.auth.dto.LoginRequestDto;
import org.example.wowelang_backend.common.apiPayLoad.status.ErrorStatus;
import org.example.wowelang_backend.security.custom.CustomUserDetails;
import org.example.wowelang_backend.security.jwt.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    // 1) 로그인 로직: 아이디 → 인증 → User 엔티티
    public String login(LoginRequestDto dto) {
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getLoginId(), dto.getPassword())
            );
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(
                    ErrorStatus.INVALID_USER.getHttpStatus(),
                    ErrorStatus.INVALID_USER.getMessage()
            );
        }

        CustomUserDetails cd = (CustomUserDetails) auth.getPrincipal();
        Long userId    = cd.getId();
        String loginId = cd.getUsername();
        List<String> roles = cd.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return jwtTokenProvider.createToken(
                String.valueOf(userId),
                loginId,
                roles
        );
    }
}
