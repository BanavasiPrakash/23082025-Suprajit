package com.ipmonitoring.ipmonitoringapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = "http://localhost:8080/reset-password.html?token=" + token;

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(toEmail);
        mail.setFrom(fromEmail);
        mail.setSubject("Password Reset Request");
        mail.setText(
                "To reset your password, click the link below:\n" + resetUrl + "\nThis link is valid for 30 minutes.");

        mailSender.send(mail);
    }
}
