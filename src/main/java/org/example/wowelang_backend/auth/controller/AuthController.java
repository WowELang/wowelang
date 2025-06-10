package org.example.wowelang_backend.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.auth.dto.LoginRequestDto;
import org.example.wowelang_backend.auth.dto.JwtDto;
import org.example.wowelang_backend.auth.service.AuthService;
import org.example.wowelang_backend.common.apiPayLoad.ApiResponse;
import org.example.wowelang_backend.security.jwt.JwtTokenProvider;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    // @return ApiResponse에 래핑된 JWT 토큰
    @PostMapping("/login")
    public ApiResponse<JwtDto> login(@RequestBody LoginRequestDto requestDto) {

        JwtDto jwtDto = authService.login(requestDto);

        return ApiResponse.onSuccess(jwtDto);
    }

    @PostMapping("/refresh")
    public ApiResponse<JwtDto> reIssueAccessToken(
        @RequestHeader("RefreshToken") String refreshToken,
        @RequestHeader("Authorization") String expiredAccessTokenHeader) {

        JwtDto jwtDto = authService.reIssueAccessToken(refreshToken, expiredAccessTokenHeader);

        return ApiResponse.onSuccess(jwtDto);
    }


}