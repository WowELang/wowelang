package org.example.wowelang_backend.report.service;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.report.domain.Report;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportMailService {

    private final JavaMailSender mailSender;

    public void sendReportNotification(Report report) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("wowelang0216@gmail.com"); // 받을 이메일
        message.setSubject("[신고 접수] 새로운 신고가 접수되었습니다.");
        message.setText(buildContent(report));

        mailSender.send(message);
    }

    private String buildContent(Report report) {
        return String.format(
                """
                신고자: %s
                피신고자: %s
                신고 사유: %s
                상세 내용: %s
                신고 발원지: %s
                """,
                report.getReportingUser().getName(),
                report.getReportedUser().getName(),
                report.getReportCategory().getReportCategoryName(),
                report.getReportReason(),
                report.getReportTarget()
        );
    }
}
