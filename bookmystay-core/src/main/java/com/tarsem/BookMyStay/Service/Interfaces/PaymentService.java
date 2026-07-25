package com.tarsem.BookMyStay.Service.Interfaces;

import com.razorpay.RazorpayException;
import com.tarsem.BookMyStay.dto.payment.CreateOrderRequest;
import com.tarsem.BookMyStay.dto.payment.CreateOrderResponse;
import com.tarsem.BookMyStay.dto.payment.VerifyPaymentRequest;
import com.tarsem.BookMyStay.dto.payment.VerifyPaymentResponse;

public interface PaymentService {
    CreateOrderResponse createOrder(CreateOrderRequest createOrderRequest) throws RuntimeException, IllegalAccessException, RazorpayException;
    VerifyPaymentResponse verifyPayment(VerifyPaymentRequest verifyPaymentRequest) throws RazorpayException;
    void handleWebhook(String payload, String signature) throws Exception;
}
