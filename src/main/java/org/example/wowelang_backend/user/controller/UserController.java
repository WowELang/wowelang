package org.example.wowelang_backend.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.common.apiPayLoad.ApiResponse;
import org.example.wowelang_backend.security.custom.CustomUserDetails;
import org.example.wowelang_backend.user.dto.*;
import org.example.wowelang_backend.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "유저")
public class UserController {

    private final UserService userService;

    // 1단계: 기본 정보 입력 후 사용자 생성
    @PostMapping
    @Operation(description = "기본 정보 입력 후 사용자 임시로 생성", summary = "유저 임시 생성 api")
    public ApiResponse<Long> signUp(@RequestBody UserSignupReqDto dto) {
        Long userId = userService.createTempUser(dto);
        return ApiResponse.onSuccess(userId);
    }

    // 2단계: 인증 메일 발송
    @PostMapping("/{userId}/email-verification")
    @Operation(description = "유저에게 인증 메일(코드)을 발송", summary = "유저 인증 메일 발송")
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
    @Operation(description = "발급받은 코드를 유저가 입력 후 회원가입 완료", summary = "인증 코드 검증")
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
    @Operation(description = "이메일 인증을 초기화", summary = "메일 인증 초기화")
    public ApiResponse<String> clearEmail(@PathVariable Long userId,
                                          @RequestBody Map<String, String> body) {
        String email = body.get("email");
        String result = userService.clearCertification(email);
        return ApiResponse.onSuccess(result);
    }

    //아이디 중복확인
    @PostMapping("/check-login-id")
    @Operation(description = "아이디 중복확인", summary = "아이디 중복확인")
    public ApiResponse<?> checkLoginId(@RequestBody LoginIdCheckReqDto dto) {
        userService.checkLoginId(dto.getLoginId()); // 중복이면 예외 발생
        return ApiResponse.onSuccess(null);
    }

    // 최초 닉네임 설정
    @PostMapping("/me/nickname")
    @Operation(description = "최초 로그인 시 닉네임 설정", summary = "닉네임 설정")
    public ApiResponse<Void> initNickname(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestBody NicknameReqDto dto
    ) {
        String nickname = dto.getNickname();
        userService.setNickname(me.getId(), nickname);
        return ApiResponse.created(null);
    }

    // 최초 캐릭터 설정 (색깔, 표정)
    @PostMapping("/me/character")
    @Operation(description = "최초 로그인 시 아바타 설정", summary = "아바타 설정")
    public ApiResponse<Void> initCharacter(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestBody CharacterReqDto dto
    ) {
        userService.setCharacter(me.getId(), dto.getColorId(), dto.getMaskId());
        return ApiResponse.created(null);
    }

    // 내 프로필 조회
    @GetMapping("/me/profile")
    @Operation(description = "내 정보 조회", summary = "내 정보 조회")
    public UserProfileDto getProfile(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        return userService.getMyProfile(me.getId());
    }

    // 닉네임 수정
    @PutMapping("/me/nickname")
    @Operation(description = "닉네임 수정", summary = "닉네임 수정")
    public ApiResponse<String> updateNickname(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestBody NicknameReqDto dto
    ) {
        String saved = userService.updateNickname(me.getId(), dto.getNickname());
        return ApiResponse.onSuccess(saved);
    }

    // 캐릭터 수정
    @PutMapping("/me/character")
    @Operation(description = "아바타 수정", summary = "아바타 수정")
    public ApiResponse<CharacterInfoDto> updateCharacter(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestBody CharacterReqDto dto
    ) {
        CharacterInfoDto responseDto = userService.updateCharacter(
                me.getId(),
                dto.getColorId(),
                dto.getMaskId()
        );
        return ApiResponse.onSuccess(responseDto);
    }
}
