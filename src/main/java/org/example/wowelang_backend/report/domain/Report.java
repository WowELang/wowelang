package org.example.wowelang_backend.report.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.wowelang_backend.common.BaseEntity;
import org.example.wowelang_backend.common.apiPayLoad.status.ErrorStatus;
import org.example.wowelang_backend.report.dto.ReportCreateDTO;
import org.example.wowelang_backend.user.domain.User;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @Column(name = "report_reason")
    private String reportReason;

    @Column(name = "report_target")
    @Enumerated(EnumType.STRING)
    private ReportTarget reportTarget;

    @OneToOne
    @JoinColumn(name = "reporting_user_id")
    private User reportingUser;

    @OneToOne
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    @ManyToOne
    @JoinColumn(name = "report_category_id")
    private ReportCategory reportCategory;

    public static Report createReport(ReportCreateDTO reportCreateDTO, User reportingUser, User reportedUser, ReportCategory category) {
        if(reportingUser.getId().equals(reportedUser.getId())) {
            throw new IllegalArgumentException(ErrorStatus.SELF_REPORT_NOT_ALLOWED.getMessage());
        }

        return Report.builder()
                .reportReason(reportCreateDTO.getReportReason())
                .reportTarget(reportCreateDTO.getReportTarget())
                .reportingUser(reportingUser)
                .reportedUser(reportedUser)
                .reportCategory(category)
                .build();
    }
}
