package com.ontop.wms.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendSignatureLink(String to, String signerName, String link) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("wms@ontop.com");
            message.setTo(to);
            message.setSubject("Yêu cầu ký duyệt chứng từ kho - OnTop WMS");
            message.setText("Kính gửi " + signerName + ",\n\n" +
                    "Bạn có một yêu cầu ký duyệt chứng từ trên hệ thống OnTop WMS.\n" +
                    "Vui lòng nhấn vào đường dẫn sau để xác nhận: " + link + "\n\n" +
                    "Trân trọng,\nĐội ngũ OnTop WMS");
            
            mailSender.send(message);
            log.info("Email sent to {} with link {}", to, link);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("wms@ontop.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            log.info("Alert email sent to {} with subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send alert email to {}: {}", to, e.getMessage());
        }
    }
}
