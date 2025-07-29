package com.example.moneymanager.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}") // NEW: Get base URL from config
    private String baseUrl;

    // NEW: Dedicated activation email method
    public void sendActivationEmail(String to, String token) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Recipient email cannot be null or empty");
        }

        String activationLink = baseUrl + "/api/activate?token=" + token; // Construct proper link
        String subject = "Activate Your MoneyTracker Account";
        String body = "Please click the following link to activate your account:\n\n"
                + activationLink + "\n\n"
                + "If you didn't request this, please ignore this email.";

        sendEmail(to, subject, body);
    }

    public void sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Recipient email cannot be null or empty");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(getFromAddress());
            message.setTo(to);
            message.setSubject(subject != null ? subject : "");
            message.setText(body != null ? body : "");

            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (MailException e) {
            log.error("Failed to send email to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String getFromAddress() {
        return fromEmail != null ? fromEmail : "no-reply@moneytracker.com";
    }

    public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String filename){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            helper.addAttachment(filename, new ByteArrayResource(attachment));
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e.getMessage());
        }


    }
}
