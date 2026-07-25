package com.tarsem.emailservice.exception;

import org.springframework.mail.MailException;

public class EmailSendingException extends RuntimeException{

    public EmailSendingException(String message, MailException ex){
        super(message);
    }
}
