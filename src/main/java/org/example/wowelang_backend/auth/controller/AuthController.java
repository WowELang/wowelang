package org.example.wowelang_backend.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
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

    // @return ApiResponse에 래핑된  JWT 토큰
    @Operation(summary = "로그인 API", description = "로그인 시 리프레시 토큰과 액세스 토큰 반환", tags = "인증")
    @PostMapping("/login")
    public ApiResponse<JwtDto> login(@RequestBody LoginRequestDto requestDto) {

        JwtDto jwtDto = authService.login(requestDto);

        return ApiResponse.onSuccess(jwtDto);
    }

    @PostMapping("/refresh")
    @Operation(summary = "AccessToken, RefreshToken 재발급 API", description = "만료된 AccessToken과 RefreshToken 투입 -> 새로운 AccessToken과 새로운 RefreshToken 생성해 전달 (RTR방식) \n"
    + "리프레시 토큰이 만료되면 로그아웃\n" + "Authorization에는 [Bearer accesstoken] 으로 들어가야함", tags = "인증")
    public ApiResponse<JwtDto> reIssueAccessToken(
        @RequestHeader("RefreshToken") String refreshToken,
        @RequestHeader("Authorization") String expiredAccessTokenHeader) {

        JwtDto jwtDto = authService.reIssueAccessToken(refreshToken, expiredAccessTokenHeader);

        return ApiResponse.onSuccess(jwtDto);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 API", description = "RefreshToken을 유효시간을 만료시켜 로그아웃 진행", tags = "인증")
    public ApiResponse<Void> logout(
        @RequestHeader ("RefreshToken") String refreshToken) {

        authService.logout(refreshToken);

        return ApiResponse.onSuccess(null);
    }

}