package com.tarsem.BookMyStay.Exceptions;

public class RoomNotAvailableException extends RuntimeException {
    public RoomNotAvailableException(String roomIsNotAvailableAnymore) {
        super(roomIsNotAvailableAnymore);
    }
}
