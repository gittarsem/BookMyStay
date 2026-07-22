package com.tarsem.BookMyStay.Controller;

import com.razorpay.RazorpayException;
import com.tarsem.BookMyStay.Service.Interfaces.PaymentService;
import com.tarsem.BookMyStay.dto.payment.CreateOrderRequest;
import com.tarsem.BookMyStay.dto.payment.CreateOrderResponse;
import com.tarsem.BookMyStay.dto.payment.VerifyPaymentRequest;
import com.tarsem.BookMyStay.dto.payment.VerifyPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestBody CreateOrderRequest request)
            throws RazorpayException, IllegalAccessException {

        return ResponseEntity.ok(
                paymentService.createOrder(request)
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyPaymentResponse> verifyPayment(
            @RequestBody VerifyPaymentRequest request)
            throws RazorpayException {
        System.out.println("==== VERIFY CONTROLLER HIT ====");
        return ResponseEntity.ok(
                paymentService.verifyPayment(request)
        );
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature)
            throws Exception {

        paymentService.handleWebhook(payload, signature);

        return ResponseEntity.ok().build();
    }
}