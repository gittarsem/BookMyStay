package com.tarsem.BookMyStay.Exceptions;

public class GuestAlreadyAddedException extends RuntimeException{
    public GuestAlreadyAddedException(String guestHasAlreadyBeenAdded) {
        super(guestHasAlreadyBeenAdded);
    }
}
