package com.tarsem.BookMyStay.Controller;


import com.tarsem.BookMyStay.Service.Interfaces.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature)
            throws Exception {

        paymentService.handleWebhook(payload, signature);

        return ResponseEntity.ok().build();
    }
}

