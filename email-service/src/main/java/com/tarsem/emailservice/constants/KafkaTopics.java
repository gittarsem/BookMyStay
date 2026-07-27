package com.tarsem.emailservice.constants;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String BOOKING_CONFIRMED = "booking-confirmed";
    public static final String BOOKING_CANCELLED = "booking-cancelled";
    public static final String BOOKING_EXPIRED = "booking-expired";
    public static final String BOOKING_CONFIRMED_DLT = "booking-confirmed.DLT";
    public static final String BOOKING_CANCELLED_DLT= "booking-cancelled.DLT";
    public static final String BOOKING_EXPIRED_DLT="booking-expired.DLT";
}