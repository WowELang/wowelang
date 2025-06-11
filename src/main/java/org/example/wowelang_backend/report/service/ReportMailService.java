package org.example.wowelang_backend.report.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.report.domain.Report;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportMailService {

    private final JavaMailSender mailSender;

    public void sendReportNotification(Report report) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo("wowelang0216@gmail.com");
            helper.setSubject("[신고 접수] 새로운 신고가 접수되었습니다.");
            helper.setText(buildHtmlContent(report), true); // true → HTML 모드

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("신고 메일 전송 실패", e);
        }
    }

    private String buildHtmlContent(Report report) {
        return String.format("""
                <div style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2 style="color: #d9534f;">🚨 신고 접수 알림</h2>
                    <p><strong>신고자:</strong> %s</p>
                    <p><strong>피신고자:</strong> %s</p>
                    <p><strong>신고 사유:</strong> %s</p>
                    <p><strong>상세 내용:</strong></p>
                    <div style="background: #f8f9fa; padding: 10px; border-radius: 4px; border: 1px solid #ddd;">
                        %s
                    </div>
                    <p><strong>신고 발원지:</strong> %s</p>
                    <hr>
                    <p style="font-size: 12px; color: #888;">WowELang 관리자 시스템에서 자동 발송된 메일입니다.</p>
                </div>
                """,
            report.getReportingUser().getName(),
            report.getReportedUser().getName(),
            report.getReportCategory().getReportCategoryName(),
            escapeHtml(report.getReportReason()),
            report.getReportTarget()
        );
    }

    // HTML 이스케이프 유틸 (간단 버전)
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
}