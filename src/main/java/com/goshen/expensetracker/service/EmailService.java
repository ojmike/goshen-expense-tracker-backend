package com.goshen.expensetracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Goshen Expense Tracker - Password Reset");
            message.setText("To reset your password, click the link below:\n\n"
                    + baseUrl + "/reset-password?token=" + token
                    + "\n\nThis link expires in 1 hour."
                    + "\n\nIf you did not request this, ignore this email.");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", toEmail, e);
        }
    }
}
