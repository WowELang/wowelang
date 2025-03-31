package org.example.wowelang_backend.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.common.apiPayLoad.GlobalResponseDTO;
import org.example.wowelang_backend.user.dto.ClearEmailDto;
import org.example.wowelang_backend.user.dto.LoginIdCheckReqDto;
import org.example.wowelang_backend.user.dto.UserSignupReqDto;
import org.example.wowelang_backend.user.dto.VerificationDto;
import org.example.wowelang_backend.user.service.UserService;
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
    public ResponseEntity<Long> signUp(@RequestBody UserSignupReqDto dto) {
        Long userId = userService.createTempUser(dto);
        return ResponseEntity.ok(userId);
    }

    // 2단계: 인증 메일 발송(재학생 튜터인 경우)
    @PostMapping("/{userId}/email-verification")
    public ResponseEntity<String> sendEmail(@PathVariable Long userId) {
        userService.sendVerificationEmail(userId);
        return ResponseEntity.ok("인증 메일이 발송되었습니다.");
    }

    // 2-1단계: 인증 코드 검증 및 대학 이메일 인증 업데이트
    @PatchMapping("/{userId}/email-verification")
    public ResponseEntity<String> verifyEmail(@RequestBody VerificationDto dto) {
        return userService.verifyUnivEmail(dto);
    }

    // 3단계: 최종 회원가입 완료
    @PatchMapping("/{userId}/complete-registration")
    public ResponseEntity<Long> completeSignUp(@PathVariable Long userId) {
        Long finalUserId = userService.completeSignUp(userId);
        return ResponseEntity.ok(finalUserId);
    }

    // 이메일 인증 초기화
    @DeleteMapping("/{userId}/email-verification")
    public ResponseEntity<String> clearEmail(@RequestBody Map<String,String> body) {
        String email = body.get("email");
        userService.clearCertification(email);
        return ResponseEntity.ok("인증 상태가 초기화되었습니다.");
    }

    //아이디 중복확인
    @PostMapping("/check-login-id")
    public ResponseEntity<GlobalResponseDTO> checkLoginId(@RequestBody LoginIdCheckReqDto dto) {

        GlobalResponseDTO response = userService.checkLoginId(dto.getLoginId());
        return ResponseEntity
                .status(response.getHttpStatus())
                .body(response);
    }
}
