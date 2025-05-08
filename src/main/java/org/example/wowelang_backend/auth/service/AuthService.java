package org.example.wowelang_backend.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.auth.dto.LoginRequestDto;
import org.example.wowelang_backend.common.apiPayLoad.status.ErrorStatus;
import org.example.wowelang_backend.security.jwt.JwtTokenProvider;
import org.example.wowelang_backend.user.domain.User;
import org.example.wowelang_backend.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    public String login(LoginRequestDto requestDto) {
        // 1) 실제 인증 전, User 엔티티 조회해서 userId 확보
        User user = userRepository.findByLoginId(requestDto.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException(
                        ErrorStatus.USER_NOT_FOUND.getMessage()
                ));

        // 2) AuthenticationManager 에게 로그인 시도 위임 (비밀번호 검증)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDto.getLoginId(),
                        requestDto.getPassword()
                )
        );

        // 3) 인증 성공하면 userId 를 subject 로, roles 를 claim 으로 토큰 생성
        String subject = String.valueOf(user.getId());
        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return jwtTokenProvider.createToken(subject, roles);
    }
}
