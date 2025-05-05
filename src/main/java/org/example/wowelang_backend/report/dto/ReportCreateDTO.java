package org.example.wowelang_backend.report.dto;

import lombok.Getter;
import org.example.wowelang_backend.report.domain.ReportCategoryName;
import org.example.wowelang_backend.report.domain.ReportTarget;

@Getter
public class ReportCreateDTO {

    private Long reportedUserId;

    private String reportReason;

    private ReportTarget reportTarget;

    ReportCategoryName reportCategoryName;
}
