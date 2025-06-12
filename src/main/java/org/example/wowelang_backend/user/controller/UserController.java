package org.example.wowelang_backend.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.example.wowelang_backend.common.annotation.CurrentUser;
import org.example.wowelang_backend.common.apiPayLoad.ApiResponse;
import org.example.wowelang_backend.security.custom.CustomUserDetails;
import org.example.wowelang_backend.user.domain.User;
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

    // 인증 메일 발송
    @PostMapping("/email-verification")
    @Operation(description = "유저에게 인증 메일(코드)을 발송", summary = "유저 인증 메일 발송")
    public ApiResponse<String> sendEmail(
            @RequestBody EmailReqDto dto
    ) {
        boolean sent = userService.sendVerificationEmail(dto.getEmail());
        if (sent) {
            return ApiResponse.onSuccess("인증 메일이 발송되었습니다.");
        }
        // 이 라인에 도달할 일은 없지만, 안정성을 위해 남겨둡니다.
        return ApiResponse.onSuccess("알 수 없는 상태");
    }

    // 3단계: 인증 코드 검증 및 가입 완료
    @PatchMapping("/complete")
    @Operation(description = "발급받은 코드를 유저가 입력 후 회원가입 완료", summary = "인증 코드 검증")
    public ApiResponse<Long> verifyAndComplete(
            @RequestBody VerifyReqDto dto
    ) {
        Long finalUserId = userService.verifyAndCompleteSignUp(
                dto.getLoginId(),
                dto.getEmail(),
                dto.getPassword(),
                dto.getName(),
                dto.getBirthday(),
                dto.getMajor(),
                dto.getGender(),
                dto.getUsertype(),
                dto.getCountry(),
                dto.getCode()
        );
        return ApiResponse.onSuccess(finalUserId);
    }
    // 이메일 인증 초기화
    @DeleteMapping("/email-verification")
    @Operation(description = "이메일 인증을 초기화", summary = "메일 인증 초기화")
    public ApiResponse<String> clearEmail(
            @RequestBody EmailReqDto dto
    ) {
        String email = dto.getEmail();
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
        @Parameter(hidden = true) @CurrentUser User user,
            @RequestBody NicknameReqDto dto
    ) {
        String nickname = dto.getNickname();
        userService.setNickname(user, nickname);
        return ApiResponse.created(null);
    }

    // 최초 캐릭터 설정 (색깔, 표정)
    @PostMapping("/me/character")
    @Operation(description = "최초 로그인 시 아바타 설정", summary = "아바타 설정")
    public ApiResponse<Void> initCharacter(
        @Parameter(hidden = true) @CurrentUser User user,
            @RequestBody CharacterReqDto dto
    ) {
        userService.setCharacter(user, dto.getColorId(), dto.getMaskId());
        return ApiResponse.created(null);
    }

    // 내 프로필 조회
    @GetMapping("/me/profile")
    @Operation(description = "내 정보 조회", summary = "내 정보 조회")
    public UserProfileDto getProfile(
        @Parameter(hidden = true) @CurrentUser User user
    ) {
        return userService.getMyProfile(user);
    }

    // 닉네임 수정
    @PutMapping("/me/nickname")
    @Operation(description = "닉네임 수정", summary = "닉네임 수정")
    public ApiResponse<String> updateNickname(
        @Parameter(hidden = true) @CurrentUser User user,
            @RequestBody NicknameReqDto dto
    ) {
        String saved = userService.updateNickname(user, dto.getNickname());
        return ApiResponse.onSuccess(saved);
    }

    // 캐릭터 수정
    @PutMapping("/me/character")
    @Operation(description = "아바타 수정", summary = "아바타 수정")
    public ApiResponse<CharacterInfoDto> updateCharacter(
        @Parameter(hidden = true) @CurrentUser User user,
            @RequestBody CharacterReqDto dto
    ) {
        CharacterInfoDto responseDto = userService.updateCharacter(
                user,
                dto.getColorId(),
                dto.getMaskId()
        );
        return ApiResponse.onSuccess(responseDto);
    }

    //비밀번호 변경
    @PutMapping("/me/password")
    @Operation(description = "비밀번호 변경", summary = "비밀번호 변경")
    public ApiResponse<Void> changePassword(
        @Parameter(hidden = true) @CurrentUser User user,
            @RequestBody ChangePasswordReqDto req
    ) {
        userService.changePassword(
                user,
                req.getCurrentPassword(),
                req.getNewPassword()
        );
        return ApiResponse.onSuccess(null);
    }

    //회원탈퇴
    @DeleteMapping("/me")
    @Operation(description = "회원탈퇴", summary = "탈퇴")
    public ApiResponse<Object> deleteAccount(
        @Parameter(hidden = true) @CurrentUser User user
    ) {
        userService.deleteAccount(user);
        return ApiResponse.onSuccess(null);
    }

    // 친구 요청 수락
    @GetMapping("/me/{userId}/profile")
    public ApiResponse<FriendProfileDto> getFriendProfile(@PathVariable Long userId) {
        FriendProfileDto dto = userService.getFriendProfile(userId);
        return ApiResponse.onSuccess(dto);
    }

}
