package com.back.global.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Profile("test-mail")
@ConditionalOnMissingBean(name = "mailService")
@Slf4j
public class MockMailService {
    
    private static final String EMAIL_LOG_DIR = "mock-emails";
    
    public void sendSummaryMail(String to, String subject, String body) {
        log.info("=== MOCK EMAIL SENT ===");
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("Body:\n{}", body);
        log.info("======================");
        
        // 콘솔 출력
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📧 MOCK EMAIL - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("-".repeat(60));
        System.out.println(body);
        System.out.println("=".repeat(60) + "\n");
        
        // 파일로도 저장
        saveEmailToFile(to, subject, body);
    }
    
    private void saveEmailToFile(String to, String subject, String body) {
        try {
            Path logDir = Paths.get(EMAIL_LOG_DIR);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String fileName = String.format("email_%s_%s.txt", timestamp, to.replaceAll("[^a-zA-Z0-9]", "_"));
            Path emailFile = logDir.resolve(fileName);
            
            String emailContent = String.format(
                "Timestamp: %s%n" +
                "To: %s%n" +
                "Subject: %s%n" +
                "----------------------------------------%n" +
                "%s%n" +
                "----------------------------------------%n",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                to, subject, body
            );
            
            Files.write(emailFile, emailContent.getBytes(), 
                       StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            
            log.info("Email saved to file: {}", emailFile.toAbsolutePath());
            
        } catch (IOException e) {
            log.warn("Failed to save email to file: {}", e.getMessage());
        }
    }
}
