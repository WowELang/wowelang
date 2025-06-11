package org.example.wowelang_backend.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

import org.example.wowelang_backend.common.annotation.CurrentUser;
import org.example.wowelang_backend.common.apiPayLoad.ApiResponse;
import org.example.wowelang_backend.security.custom.CustomUserDetails;
import org.example.wowelang_backend.user.domain.User;
import org.example.wowelang_backend.user.dto.InterestDto;
import org.example.wowelang_backend.user.dto.InterestReqDto;
import org.example.wowelang_backend.user.service.InterestService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interest")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

    // 전체 관심사 목록 조회
    @GetMapping
    @Operation(description = "최초 로그인 시 관심사 설정", summary = "관심사 초기 설정")
    public ApiResponse<List<InterestDto>> getAll() {
        List<InterestDto> data = interestService.getAllInterest();
        return ApiResponse.onSuccess(data);
    }

    // 최초 관심사 설정
    @PostMapping("/me")
    @Operation(description = "관심사 수정", summary = "관심사 수정")
    public ApiResponse<List<Long>> initMyInterests(
        @Parameter(hidden = true) @CurrentUser User user,
            @RequestBody InterestReqDto dto
            ) {
        List<Long> ids = dto.getInterestIds();
        interestService.initInterest(user, ids);
        // 생성된 리소스(최초 관심사 리스트)를 result로 담아서 201 반환
        return ApiResponse.created(ids);
    }

    // 관심사 수정
    @PutMapping("/me")
    public ApiResponse<List<Long>> updateMyInterests(
        @Parameter(hidden = true) @CurrentUser User user,
            @RequestBody InterestReqDto dto
    ) {
        List<Long> newIds = dto.getInterestIds();
        interestService.updateInterests(user, newIds);
        return ApiResponse.onSuccess(newIds);
    }
}