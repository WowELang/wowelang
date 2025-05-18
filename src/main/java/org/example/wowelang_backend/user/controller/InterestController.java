package org.example.wowelang_backend.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.security.custom.CustomUserDetails;
import org.example.wowelang_backend.user.dto.InterestDto;
import org.example.wowelang_backend.user.service.InterestService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interest")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

    //전체 관심사 목록 조회
    @GetMapping
    public List<InterestDto> getAll() {
        return interestService.getAllInterest();
    }

    //최초 관심사 설정
    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public void initMyInterests(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestBody List<Long> interestIds
    ) {
        interestService.initInterest(me.getId(), interestIds);
    }

    //관심사 수정
    @PutMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMyInterests(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestBody List<Long> newInterestIds
    ) {
        interestService.updateInterests(me.getId(), newInterestIds);
    }
}
