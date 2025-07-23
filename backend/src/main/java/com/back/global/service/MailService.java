package com.back.global.service;

// import jakarta.mail.MessagingException;
// import jakarta.mail.internet.InternetAddress;
// import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

// import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.mail.host")
public class MailService {

    // private final JavaMailSender mailSender;

    @Value("${mail.from.address:test@example.com}")
    private String fromAddress;

    @Value("${mail.from.name:Test Sender}")
    private String fromName;

    public void sendSummaryMail(String to, String subject, String body) {
        log.warn("MailService is disabled due to missing jakarta.mail dependency");
        log.info("Would send email to: {}, subject: {}", to, subject);

        // 임시로 콘솔에만 출력
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📧 DISABLED MAIL SERVICE");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("From: " + fromName + " <" + fromAddress + ">");
        System.out.println("-".repeat(60));
        System.out.println(body);
        System.out.println("=".repeat(60) + "\n");
    }
}
