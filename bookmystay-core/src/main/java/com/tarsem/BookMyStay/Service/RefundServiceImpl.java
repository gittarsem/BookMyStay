package com.tarsem.BookMyStay.Service;

import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import com.tarsem.BookMyStay.Entity.BookingEntity;
import com.tarsem.BookMyStay.Entity.PaymentEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Enums.PaymentStatus;
import com.tarsem.BookMyStay.Enums.RefundStatus;
import com.tarsem.BookMyStay.Exceptions.BookingNotFoundException;
import com.tarsem.BookMyStay.Exceptions.BusinessRuleViolationException;
import com.tarsem.BookMyStay.Exceptions.PaymentException;
import com.tarsem.BookMyStay.Repositroy.BookingRepository;
import com.tarsem.BookMyStay.Repositroy.PaymentRepository;
import com.tarsem.BookMyStay.Service.Interfaces.RefundService;
import com.tarsem.BookMyStay.Strategy.RefundPolicy;
import com.tarsem.BookMyStay.dto.payment.RefundResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;

import static com.tarsem.BookMyStay.Utils.AppUtils.giveMeCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundServiceImpl implements RefundService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final RefundPolicy refundPolicy;
    private final RazorpayClient razorpayClient;

    @Override
    public RefundResponseDTO refund(Long bookingId) throws AccessDeniedException, RazorpayException {
        BookingEntity booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new BookingNotFoundException("Booking does not exist with id:" + bookingId)
        );
        UserEntity user = giveMeCurrentUser();
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied as User is different");
        }

        PaymentEntity payment=paymentRepository.findById(booking.getPayment().getId()).orElseThrow(
                ()->new PaymentException("Payment Not found for this")
        );

        if( !PaymentStatus.SUCCESS.equals(payment.getPaymentStatus()) || !BookingStatus.BOOKED.equals(booking.getStatus())){
            throw new BusinessRuleViolationException("Payment is not yet completed or Booking is not confirmed yet");
        }

        double refundPercentage=refundPolicy.calculateRefundPercentage(booking.getCheckInDate());

        BigDecimal percentage =
                BigDecimal.valueOf(refundPercentage)
                        .divide(BigDecimal.valueOf(100));

        BigDecimal refundAmount =
                payment.getAmount().multiply(percentage);

        BigDecimal nonRefundableCharges =
                payment.getGatewayFee()
                        .add(payment.getGatewayTax());

        refundAmount = refundAmount.subtract(nonRefundableCharges);

        refundAmount = refundAmount.max(BigDecimal.ZERO);

        if (refundAmount.compareTo(BigDecimal.ZERO) == 0) {

            payment.setRefundedAmount(BigDecimal.ZERO);
            payment.setRefundStatus(RefundStatus.COMPLETED);

            paymentRepository.save(payment);

            return RefundResponseDTO.builder()
                    .refundId(null)
                    .refundAmount(0.0)
                    .refundStatus(RefundStatus.COMPLETED)
                    .message("Booking cancelled with no refund.")
                    .build();
        }

        try {
            JSONObject options=new JSONObject();

            options.put("amount",
                    refundAmount
                            .multiply(BigDecimal.valueOf(100))
                            .intValue()
            );
            log.info("Payment ID: {}", payment.getGatewayPaymentId());
            log.info("Refund Amount (₹): {}", refundAmount);
            log.info("Refund Amount (Paise): {}", options.get("amount"));
            Payment razorpayPayment = razorpayClient.payments.fetch(payment.getGatewayPaymentId());

            log.info("Fetched Payment: {}", razorpayPayment);
            log.info("Status: {}", (Object) razorpayPayment.get("status"));
            log.info("Amount: {}", (Object) razorpayPayment.get("amount"));
            log.info("Captured: {}", (Object) razorpayPayment.get("captured"));
            Refund refund = razorpayClient.payments.refund(
                    payment.getGatewayPaymentId(),
                    options
            );
            payment.setGatewayRefundId(refund.get("id"));

        }
        catch (Exception ex) {

//            payment.setRefundStatus(RefundStatus.FAILED);
//
//            paymentRepository.save(payment);
            log.error("Refund failed", ex);
            throw new PaymentException(ex.getMessage());
        }


        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        payment.setRefundStatus(RefundStatus.COMPLETED);
        payment.setRefundedAmount(refundAmount);
        paymentRepository.save(payment);


        return RefundResponseDTO.builder()
                .refundId(payment.getGatewayRefundId())
                .refundAmount(refundAmount.doubleValue())
                .refundStatus(payment.getRefundStatus())
                .message("Refund processed successfully.")
                .build();

    }
}
