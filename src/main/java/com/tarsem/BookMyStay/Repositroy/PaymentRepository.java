package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.BookingEntity;
import com.tarsem.BookMyStay.Entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity,Long> {
    Optional<PaymentEntity> findByGatewayOrderId(String GatewayOrderId);
    Optional<PaymentEntity> findByGatewayPaymentId(String gatewayPaymentId);
    Optional<PaymentEntity> findByBooking(BookingEntity booking);

}
