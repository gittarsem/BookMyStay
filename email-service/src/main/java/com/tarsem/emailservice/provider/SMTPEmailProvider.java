package com.tarsem.emailservice.provider;

import com.tarsem.emailservice.exception.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SMTPEmailProvider {

    @Value("${book-my-stay-mail}")
    private String fromMail;

    private final JavaMailSender javaMailSender;

    public void sendEmail(
            String emailId,
            String subject,
            String body
    ) {

        log.info("Sending HTML email to {}", emailId);

        try {

            MimeMessage message =
                    javaMailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setTo(emailId);
            helper.setFrom(fromMail);
            helper.setSubject(subject);

            // true = HTML email
            helper.setText(body, true);

            helper.setReplyTo(fromMail);

            javaMailSender.send(message);

            log.info(
                    "HTML email sent successfully to {}",
                    emailId
            );

        } catch (MessagingException | MailException e) {

            log.error(
                    "Failed to send email to {}",
                    emailId,
                    e
            );

            throw new EmailSendingException(
                    "Failed to send email"
            );
        }
    }
}