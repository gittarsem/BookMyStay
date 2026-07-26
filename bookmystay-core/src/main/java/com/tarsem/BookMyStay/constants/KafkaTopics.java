package com.tarsem.BookMyStay.constants;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String BOOKING_CONFIRMED = "booking-confirmed";
    public static final String BOOKING_CANCELLED = "booking-cancelled";
    public static final String BOOKING_EXPIRED = "booking-expired";

}