package com.tarsem.BookMyStay.Utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RazorPaySignatureUtil {

    @Value("${razorpay.key-secret}")
    private String key;

    public boolean verify(
            String orderId,
            String paymentId,
            String razorPaySignature
    ){
        return true;
    }
}
