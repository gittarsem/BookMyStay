package com.tarsem.BookMyStay.Exceptions;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(String s) {
        super(s);
    }
}
