package com.tarsem.BookMyStay.Strategy;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class RefundPolicy {

    public double calculateRefundPercentage(LocalDate checkInDate) {

        long days =
                ChronoUnit.DAYS.between(LocalDate.now(), checkInDate);

        if (days >= 7)
            return 100;

        if (days >= 3)
            return 75;

        if (days >= 1)
            return 50;

        return 0;
    }
}
