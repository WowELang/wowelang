package org.example.wowelang_backend.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.auth.dto.LoginRequestDto;
import org.example.wowelang_backend.common.apiPayLoad.status.ErrorStatus;
import org.example.wowelang_backend.security.jwt.JwtTokenProvider;
import org.example.wowelang_backend.user.domain.User;
import org.example.wowelang_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /*로그인 처리
    @param requestDto loginId, password 담긴 DTO
    @return 발급된 JWT 토큰*/
    public String login(LoginRequestDto requestDto) {
        // 1) 아이디로 사용자 조회
        User user = userRepository.findByLoginId(requestDto.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException(
                        ErrorStatus.USER_NOT_FOUND.getMessage()
                ));

        // 2) 비밀번호 검증 (추후 PasswordEncoder 적용 예정)
        if (!user.getPassword().equals(requestDto.getPassword())) {
            throw new IllegalArgumentException(
                    ErrorStatus.INVALID_PASSWORD.getMessage()
            );
        }

        // 3) 역할 문자열 생성 (ROLE_NATIVE, ROLE_FOREIGN, ROLE_ADMIN)
        String role = "ROLE_" + user.getUsertype().name(); //Spring Security 권한 표준 규약

        // 4) JWT 토큰 생성
        return jwtTokenProvider.createToken(
                String.valueOf(user.getId()),
                Collections.singletonList(role)
        );
    }
}
