package org.example.wowelang_backend.report.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

import org.example.wowelang_backend.common.annotation.CurrentUser;
import org.example.wowelang_backend.common.apiPayLoad.ApiResponse;
import org.example.wowelang_backend.report.dto.ReportCreateDTO;
import org.example.wowelang_backend.report.service.ReportService;
import org.example.wowelang_backend.user.domain.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("")
    public ApiResponse<Long> createReport(@Parameter(hidden = true) @CurrentUser User user,  @RequestBody ReportCreateDTO reportCreateDTO) {

        return ApiResponse.created(reportService.createReport(reportCreateDTO, user));
    }
}
