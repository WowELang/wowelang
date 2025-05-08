package org.example.wowelang_backend.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.common.apiPayLoad.ApiResponse;
import org.example.wowelang_backend.user.dto.LoginIdCheckReqDto;
import org.example.wowelang_backend.user.dto.UserSignupReqDto;
import org.example.wowelang_backend.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 1단계: 기본 정보 입력 후 사용자 생성
    @PostMapping
    public ApiResponse<Long> signUp(@RequestBody UserSignupReqDto dto) {
        Long userId = userService.createTempUser(dto);
        return ApiResponse.onSuccess(userId);
    }

    // 2단계: 인증 메일 발송
    @PostMapping("/{userId}/email-verification")
    public ApiResponse<String> sendEmail(@PathVariable Long userId) {
        boolean sent = userService.sendVerificationEmail(userId);
        // 여기는 sent == true 인 경우만 오므로, 바로 성공 메시지 반환
        return ApiResponse.onSuccess("인증 메일이 발송되었습니다.");
    }


    // 3단계: 인증 코드 검증 및 회원가입 완료
    @PatchMapping("/{userId}/complete")
    public ApiResponse<Long> verifyAndComplete(
            @PathVariable Long userId,
            @RequestBody Map<String, Integer> body
    ) {
        int code = body.get("code");
        Long finalUserId = userService.verifyAndCompleteSignUp(userId, code);
        return ApiResponse.onSuccess(finalUserId);
    }

    // 이메일 인증 초기화
    @DeleteMapping("/{userId}/email-verification")
    public ApiResponse<String> clearEmail(@PathVariable Long userId,
                                          @RequestBody Map<String, String> body) {
        String email = body.get("email");
        String result = userService.clearCertification(email);
        return ApiResponse.onSuccess(result);
    }

    //아이디 중복확인
    @PostMapping("/check-login-id")
    public ApiResponse<?> checkLoginId(@RequestBody LoginIdCheckReqDto dto) {
        userService.checkLoginId(dto.getLoginId()); // 중복이면 예외 발생
        return ApiResponse.onSuccess(null);
    }
}
