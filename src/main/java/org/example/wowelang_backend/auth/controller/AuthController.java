package org.example.wowelang_backend.auth.controller;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.auth.dto.LoginRequestDto;
import org.example.wowelang_backend.auth.service.AuthService;
import org.example.wowelang_backend.common.apiPayLoad.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /*
      POST /auth/login
      @param requestDto { loginId, password }
      @return ApiResponse에 래핑된 JWT 토큰
     */
    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody LoginRequestDto requestDto) {
        String token = authService.login(requestDto);
        return ApiResponse.onSuccess(token);
    }
}