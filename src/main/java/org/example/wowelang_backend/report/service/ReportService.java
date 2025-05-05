package org.example.wowelang_backend.report.service;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.common.apiPayLoad.status.ErrorStatus;
import org.example.wowelang_backend.report.domain.Report;
import org.example.wowelang_backend.report.domain.ReportCategory;
import org.example.wowelang_backend.report.dto.ReportCreateDTO;
import org.example.wowelang_backend.report.repository.ReportCategoryRepository;
import org.example.wowelang_backend.report.repository.ReportRepository;
import org.example.wowelang_backend.user.domain.User;
import org.example.wowelang_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportCategoryRepository reportCategoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createReport(ReportCreateDTO reportCreateDTO) {

        User reportingUser = userRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        User reportedUser = userRepository.findById(reportCreateDTO.getReportedUserId())
                .orElseThrow(() -> new IllegalArgumentException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        ReportCategory category = reportCategoryRepository.findByReportCategoryName(reportCreateDTO.getReportCategoryName())
                .orElseThrow(() -> new IllegalArgumentException(ErrorStatus.REPORT_CATEGORY_NOT_FOUND.getMessage()));

        Report report = Report.createReport(reportCreateDTO, reportingUser, reportedUser, category);

        reportRepository.save(report);
        return report.getId();
    }
}
