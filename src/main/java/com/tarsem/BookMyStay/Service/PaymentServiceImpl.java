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
import com.tarsem.BookMyStay.Service.Interfaces.PaymentService;
import com.tarsem.BookMyStay.Utils.RazorPaySignatureUtil;
import com.tarsem.BookMyStay.Utils.RazorpayWebhookUtil;
import com.tarsem.BookMyStay.dto.payment.CreateOrderRequest;
import com.tarsem.BookMyStay.dto.payment.CreateOrderResponse;
import com.tarsem.BookMyStay.dto.payment.VerifyPaymentRequest;
import com.tarsem.BookMyStay.dto.payment.VerifyPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.key-id}")
    private String key;

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
        }
        if(payment.getPaymentStatus() == PaymentStatus.SUCCESS){
            throw new PaymentException("Payment has already been completed.");
        }

        if (booking.getStatus() != BookingStatus.PAYMENTS_PENDING) {
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
        payment.setPaymentStatus(PaymentStatus.CREATED);
        payment.setGatewayPaymentId(null);
        payment.setSignature(null);

        booking.setPayment(payment);
        paymentRepository.save(payment);


        return CreateOrderResponse.builder()
                .orderId(order.get("id").toString())
                .amount(payment.getAmount())
                .currency("INR")
                .keyId(key)
                .build();
    }

    @Override
    @Transactional
    public VerifyPaymentResponse verifyPayment(VerifyPaymentRequest request) throws RazorpayException {

        PaymentEntity payment=paymentRepository.findByGatewayOrderId(request.getOrderId()).orElseThrow(
                ()->new ResourceNotFoundException("Payment Not Found")
        );

        if (!payment.getGatewayOrderId().equals(request.getOrderId())) {
            throw new PaymentException("Order ID mismatch.");
        }

        if(payment.getPaymentStatus()==PaymentStatus.SUCCESS){
            throw new PaymentException("Payment already verified");
        }

        if(!paySignatureUtil.verify(request.getOrderId(), request.getPaymentId(), request.getSignature())){
            throw new PaymentException("Invalid payment signature");
        }

        if(!validateRazorpayPayment(request,payment.getAmount())){
            throw new PaymentException("Payment Failed");
        }
        payment.setGatewayPaymentId(request.getPaymentId());
        payment.setSignature(request.getSignature());
        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        BookingEntity booking=payment.getBooking();
        booking.setStatus(BookingStatus.CONFIRMED);

        paymentRepository.save(payment);

        return VerifyPaymentResponse.builder()
                .bookingId(booking.getId())
                .razorpayPaymentId(payment.getGatewayPaymentId())
                .message("Payment Verified Successful")
                .paymentStatus(payment.getPaymentStatus())
                .bookingStatus(booking.getStatus())
                .build();
    }

    @Override
    public void handleWebhook(String payload, String signature) throws Exception {

        if(!razorpayWebhookUtil.verifyWebhookSignature(payload,signature)){
            throw new PaymentException("Invalid Signature");
        }

        JSONObject object=new JSONObject(payload);

        String event=object.getString("event");

        if("payment.captured".equals(event)){
            handlePaymentCaptured(object);
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

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return;
        }

        payment.setGatewayPaymentId(paymentId);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        BookingEntity booking = payment.getBooking();
        booking.setStatus(BookingStatus.CONFIRMED);

        paymentRepository.save(payment);


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
