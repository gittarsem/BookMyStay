package com.tarsem.BookMyStay.Utils;

import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RazorpayWebhookUtil {

    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;

    public boolean verifyWebhookSignature(String payload, String signature) {

        String generatedSignature = HmacUtils.hmacSha256Hex(
                webhookSecret,
                payload
        );

        return generatedSignature.equals(signature);
    }
}
