package com.tarsem.emailservice.provider;


import com.tarsem.emailservice.exception.EmailSendingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class SMTPEmailProvider {

    @Value(
            "${book-my-stay-mail}"
    )
    private String fromMail;
    private final JavaMailSender javaMailSender;


    public void sendEmail(String emailId, String subject, String body) {

        SimpleMailMessage message=new SimpleMailMessage();

        message.setTo(emailId);
        message.setFrom(fromMail);
        message.setSubject(subject);
        message.setText(body);
        message.setSentDate(new Date());
        message.setReplyTo(fromMail);
        log.info("Sending email to {}", emailId);
        try {
            javaMailSender.send(message);
        } catch (MailException ex) {
            log.error("Failed to send email to {}", emailId, ex);
            throw new EmailSendingException("Failed to send email", ex);
        }
        log.info("Email sent successfully to {}", emailId);

    }
}
