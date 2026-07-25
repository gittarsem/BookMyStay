package com.tarsem.BookMyStay.Config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorPayConfig {

    @Value("${razorpay.key-id}")
    private String key;

    @Value("${razorpay.key-secret}")
    private String secret;

    @PostConstruct
    public void init() {
        System.out.println("Key ID = " + key);
        System.out.println("Secret length = " + secret.length());
    }

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(key,secret);
    }
}
