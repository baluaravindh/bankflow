package com.balu.bankflow.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromMail;

    // METHOD: sendWelcomeEmail(String toEmail, String fullName)
    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromMail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to Bank Flow!");

            String htmlBody = """
                    <html>
                    <body style="font-family: Arial, sans-serif;">
                        <h2 style="color: #e74c3c;">
                            Welcome to Bank Flow, %s!
                        </h2>
                        <p>Your account has been 
                           created successfully.</p>
                        <p>Start exploring our services!</p>
                        <br/>
                        <p style="color: #888;">
                            Team BankFlow
                        </p>
                    </body>
                    </html>
                    """.formatted(fullName);
            helper.setText(htmlBody, true);
            javaMailSender.send(message);

            log.info("Welcome email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send welcome email"
                            + " to: {}, error: {}",
                    toEmail, e.getMessage());
        }
    }

// METHOD: sendTransactionAlertEmail(String toEmail,
//         String fullName, String type,
//         BigDecimal amount, BigDecimal balance)

    @Async
    public void sendTransactionAlertEmail(String toEmail,
                                          String fullName, String type,
                                          BigDecimal amount, BigDecimal balance) {
        try {

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromMail);
            helper.setTo(toEmail);
            helper.setSubject("Transaction Alert Email!");

            String htmlBody = """
                    <html>
                    <body style="font-family: Arial, sans-serif;">
                        <h2 style="color: #27ae60;">
                            Transaction Alert!
                        </h2>
                        <p>Hi %s,</p>
                        <p>Transaction Type: <strong>%s</strong></p>
                        <p>Amount: <strong>₹%s</strong></p>
                        <p>Available Balance: <strong>₹%s</strong></p>
                        <p>Please contact branch if not done by you!</p>
                        <br/>
                        <p style="color: #888;">
                            Team BankFlow.
                        </p>
                    </body>
                    </html>
                    """.formatted(fullName, type, amount, balance);

            helper.setText(htmlBody, true);
            javaMailSender.send(message);

            log.info("Transaction alert mail sent to{}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send application email to{}, error: {}", toEmail, e.getMessage());
        }
    }

    // METHOD: sendLowBalanceAlertEmail(String toEmail,
    // String fullName, BigDecimal balance)
    @Async
    public void sendLowBalanceAlertEmail(String toEmail, String fullName, BigDecimal balance) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromMail);
            helper.setTo(toEmail);
            helper.setSubject("Low Balance Alert!");

            String htmlBody = """
                    <html>
                    <body style="font-family: Arial, sans-serif;">
                        <h2 style="color: #e74c3c;">
                            Low Balance Alert!
                        </h2>
                        <p>Hi %s,</p>
                        <p>Your account balance is <strong>₹%s</strong>.</p>
                        <p>Please maintain minimum required balance 
                           to avoid charges!</p>
                        <br/>
                        <p style="color: #888;">
                            Team BankFlow.
                        </p>
                    </body>
                    </html>
                    """.formatted(fullName, balance);

            helper.setText(htmlBody, true);
            javaMailSender.send(message);
            log.info("Low balance alert mail sent to{}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send application email to{}, error: {}", toEmail, e.getMessage());
        }
    }
}
