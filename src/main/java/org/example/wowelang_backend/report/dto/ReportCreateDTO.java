package org.example.wowelang_backend.report.dto;

import lombok.Getter;
import org.example.wowelang_backend.report.domain.ReportCategoryName;

@Getter
public class ReportCreateDTO {

    private Long reportedUserId;

    private String reportReason;

    ReportCategoryName reportCategoryName;
}
