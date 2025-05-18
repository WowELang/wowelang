package org.example.wowelang_backend.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.common.apiPayLoad.ApiResponse;
import org.example.wowelang_backend.security.custom.CustomUserDetails;
import org.example.wowelang_backend.user.dto.InterestDto;
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
    public ApiResponse<List<InterestDto>> getAll() {
        List<InterestDto> data = interestService.getAllInterest();
        return ApiResponse.onSuccess(data);
    }

    // 최초 관심사 설정
    @PostMapping("/me")
    public ApiResponse<List<Long>> initMyInterests(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestBody List<Long> interestIds
    ) {
        interestService.initInterest(me.getId(), interestIds);
        // 생성된 리소스(최초 관심사 리스트)를 result 로 담아서 201 반환
        return ApiResponse.created(interestIds);
    }

    // 관심사 수정
    @PutMapping("/me")
    public ApiResponse<List<Long>> updateMyInterests(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestBody List<Long> newInterestIds
    ) {
        interestService.updateInterests(me.getId(), newInterestIds);
        return ApiResponse.onSuccess(newInterestIds);
    }
}