package com.tarsem.BookMyStay.Scheduler;

import com.tarsem.BookMyStay.Entity.BookingEntity;
import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Repositroy.BookingRepository;
import com.tarsem.BookMyStay.Service.Interfaces.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingExpiryScheduler {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    @Scheduled(cron = "0 */3 * * * *")
    @Transactional
    public void removeExpireBooking(){
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
        List<BookingEntity> bookingEntityList=bookingRepository.findExpiredBooking(BookingStatus.PAYMENT_PENDING,cutoff);

        for (BookingEntity booking : bookingEntityList) {
            try {
                if (booking.getStatus() != BookingStatus.PAYMENT_PENDING) {
                    continue;
                }
                bookingService.expireBooking(booking);
            } catch (Exception ex) {
                log.error("Failed to expire booking {}", booking.getId(), ex);
            }
        }

    }

}
