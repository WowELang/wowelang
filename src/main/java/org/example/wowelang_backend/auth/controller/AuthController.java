package org.example.wowelang_backend.auth.controller;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.auth.dto.LoginRequestDto;
import org.example.wowelang_backend.auth.dto.LoginResponseDto;
import org.example.wowelang_backend.auth.service.AuthService;
import org.example.wowelang_backend.common.apiPayLoad.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // @return ApiResponse에 래핑된 JWT 토큰
    @PostMapping("/login")
    public ApiResponse<LoginResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        LoginResponseDto loginResponseDto = authService.login(requestDto);
        return ApiResponse.onSuccess(loginResponseDto);
    }


}