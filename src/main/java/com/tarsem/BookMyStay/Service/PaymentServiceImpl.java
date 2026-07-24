package com.tarsem.BookMyStay.Service;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.tarsem.BookMyStay.Entity.BookingEntity;
import com.tarsem.BookMyStay.Entity.PaymentEntity;
import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Enums.PaymentGateway;
import com.tarsem.BookMyStay.Enums.PaymentStatus;
import com.tarsem.BookMyStay.Exceptions.PaymentException;
import com.tarsem.BookMyStay.Exceptions.ResourceNotFoundException;
import com.tarsem.BookMyStay.Repositroy.BookingRepository;
import com.tarsem.BookMyStay.Repositroy.PaymentRepository;
import com.tarsem.BookMyStay.Service.Interfaces.BookingService;
import com.tarsem.BookMyStay.Service.Interfaces.PaymentService;
import com.tarsem.BookMyStay.Utils.RazorPaySignatureUtil;
import com.tarsem.BookMyStay.Utils.RazorpayWebhookUtil;
import com.tarsem.BookMyStay.dto.payment.CreateOrderRequest;
import com.tarsem.BookMyStay.dto.payment.CreateOrderResponse;
import com.tarsem.BookMyStay.dto.payment.VerifyPaymentRequest;
import com.tarsem.BookMyStay.dto.payment.VerifyPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.key-id}")
    private String key;

    private final BookingService bookingService;
    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    private final BookingRepository bookingRepository;
    private final RazorPaySignatureUtil paySignatureUtil;
    private final RazorpayWebhookUtil razorpayWebhookUtil;

    @Override
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest createOrderRequest) throws RazorpayException {
        BookingEntity booking=bookingRepository.findById(createOrderRequest.getBookingId()).orElseThrow(
                ()-> new ResourceNotFoundException("Booking with this id:"+createOrderRequest.getBookingId()+"not found")
        );

        PaymentEntity payment=booking.getPayment();
        if(payment==null){
            payment=new PaymentEntity();
            payment.setBooking(booking);
            payment.setPaymentStatus(PaymentStatus.PENDING);
        }
        if(payment.getPaymentStatus() == PaymentStatus.SUCCESS){
            throw new PaymentException("Payment has already been completed.");
        }

        if (booking.getStatus() != BookingStatus.PAYMENT_PENDING) {
            throw new PaymentException("Booking is not eligible for payment.");
        }

        JSONObject options=new JSONObject();

        options.put("amount",booking.getTotalPrice().multiply(BigDecimal.valueOf(100)).longValueExact());
        options.put("currency","INR");
        options.put("receipt","booking_"+booking.getId());

        Order order= razorpayClient.orders.create(options);

        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setGateway(PaymentGateway.RAZORPAY);
        payment.setGatewayOrderId(order.get("id").toString());
        payment.setCurrency("INR");
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setGatewayPaymentId(null);
        payment.setSignature(null);
        booking.setPayment(payment);
        paymentRepository.save(payment);
        return CreateOrderResponse.builder()
                .orderId(order.get("id").toString())
                .amount(payment.getAmount())
                .currency("INR")
                .keyId(key)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .build();
    }

    @Override
    @Transactional
    public VerifyPaymentResponse verifyPayment(VerifyPaymentRequest request) throws RazorpayException {

        PaymentEntity payment=paymentRepository.findByGatewayOrderId(request.getOrderId()).orElseThrow(

                ()->new ResourceNotFoundException("Payment Not Found")
        );

        BookingEntity booking=payment.getBooking();

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            return VerifyPaymentResponse.builder()
                    .bookingId(booking.getId())
                    .razorpayPaymentId(payment.getGatewayPaymentId())
                    .message("Payment has already been processed or has been expired")
                    .paymentStatus(payment.getPaymentStatus())
                    .bookingStatus(booking.getStatus())
                    .build();
        }

        if (!payment.getGatewayOrderId().equals(request.getOrderId())) {
            return failureResponse(payment,booking,"Order ID mismatch");
        }

        if (!validateRazorpayPayment(request, payment.getAmount())) {
            return failureResponse(payment,booking,"Payment amount mismatch");
        }

        if (!paySignatureUtil.verify(request.getOrderId(), request.getPaymentId(), request.getSignature())) {
            return failureResponse(payment,booking,"Invalid payment signature");
        }

        Payment currPayment=razorpayClient.payments.fetch(request.getPaymentId());

        String paymentStatus=currPayment.get("status");
        if("captured".equals(paymentStatus)){
            confirmBooking(payment,booking,request.getPaymentId());
            payment.setSignature(request.getSignature());
        }
        else{
            return failureResponse(payment,booking,"Payment Failed");
        }

        return VerifyPaymentResponse.builder()
                .bookingId(booking.getId())
                .razorpayPaymentId(payment.getGatewayPaymentId())
                .message("Payment Verified Successful")
                .paymentStatus(payment.getPaymentStatus())
                .bookingStatus(booking.getStatus())
                .build();
    }

    private void confirmBooking(PaymentEntity payment, BookingEntity booking,String paymentId) {
        log.info("Confirming Booking for booking id:{}",booking.getId());
        bookingService.confirmInventory(booking.getId());
        booking.setStatus(BookingStatus.BOOKED);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setGatewayPaymentId(paymentId);

    }

    private void failedBooking(PaymentEntity payment, BookingEntity booking) {
        log.info("Booking failed for booking id:{}",booking.getId());
        bookingService.releaseInventory(booking.getId());
        booking.setStatus(BookingStatus.CANCELLED);
        payment.setPaymentStatus(PaymentStatus.FAILED);
    }

    private VerifyPaymentResponse failureResponse(
            PaymentEntity payment,
            BookingEntity booking,
            String message
    ) {
        if (payment.getPaymentStatus() == PaymentStatus.PENDING) {
            failedBooking(payment, booking);
        }

        return VerifyPaymentResponse.builder()
                .bookingId(booking.getId())
                .razorpayPaymentId(payment.getGatewayPaymentId())
                .message(message)
                .paymentStatus(payment.getPaymentStatus())
                .bookingStatus(booking.getStatus())
                .build();
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String signature) throws Exception {

        if(!razorpayWebhookUtil.verifyWebhookSignature(payload,signature)){
            throw new PaymentException("Invalid Signature");
        }

        JSONObject object=new JSONObject(payload);

        String event=object.getString("event");

        switch (event) {
            case "payment.captured":
                handlePaymentCaptured(object);
                break;

            case "payment.failed":
                handleFailedBooking(object);
                break;

            default:
                log.info("Ignoring webhook event: {}", event);
        }

    }

    private void handlePaymentCaptured(JSONObject object) {
        JSONObject paymentJson=object
                .getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String paymentId = paymentJson.getString("id");
        String orderId = paymentJson.getString("order_id");

        PaymentEntity payment = paymentRepository
                .findByGatewayOrderId(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found"));

        BookingEntity booking = payment.getBooking();
        if (booking.getStatus() != BookingStatus.PAYMENT_PENDING ||
                payment.getPaymentStatus() != PaymentStatus.PENDING) {

            log.warn("Ignoring captured payment for booking {}", booking.getId());

            // Future:
            // Trigger refund

            return;
        }

        confirmBooking(payment,booking,paymentId);

    }

    private void handleFailedBooking(JSONObject object) {
        JSONObject paymentJson = object
                .getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String orderId = paymentJson.getString("order_id");

        PaymentEntity payment = paymentRepository
                .findByGatewayOrderId(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found"));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            return;
        }

        BookingEntity booking = payment.getBooking();

        failedBooking(payment, booking);
    }

    private boolean validateRazorpayPayment(
            VerifyPaymentRequest request,
            BigDecimal amount) throws RazorpayException {

        Payment payment = razorpayClient.payments.fetch(request.getPaymentId());

        String rStatus = payment.get("status").toString();
        String rOrderId = payment.get("order_id").toString();
        long rAmount = ((Number) payment.get("amount")).longValue();

        long expectedAmount = amount.multiply(BigDecimal.valueOf(100))
                .longValueExact();

        return "captured".equals(rStatus)
                && rOrderId.equals(request.getOrderId())
                && rAmount == expectedAmount;
    }



}
