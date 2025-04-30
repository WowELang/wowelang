package org.example.wowelang_backend.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.common.apiPayLoad.ApiResponse;
import org.example.wowelang_backend.common.apiPayLoad.GlobalResponseDTO;
import org.example.wowelang_backend.user.dto.ClearEmailDto;
import org.example.wowelang_backend.user.dto.LoginIdCheckReqDto;
import org.example.wowelang_backend.user.dto.UserSignupReqDto;
import org.example.wowelang_backend.user.dto.VerificationDto;
import org.example.wowelang_backend.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        // boolean 값에 따라 컨트롤러가 메시지를 설정 가능.
        if (sent) {
            // userType에 따라 메시지 달라질 수 있음 (예: 유학생은 따로 처리)
            return ApiResponse.onSuccess("인증 메일이 발송되었습니다.");
        }
        // 실패는 서비스에서 이미 예외로 처리되므로 여기는 도달하지 않음.
        return ApiResponse.onSuccess("알 수 없는 상태");
    }

    // 3단계: 인증 코드 검증 및 가입 완료
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
