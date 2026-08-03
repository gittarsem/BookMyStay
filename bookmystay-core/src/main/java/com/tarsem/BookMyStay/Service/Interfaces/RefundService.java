package com.tarsem.BookMyStay.Service.Interfaces;

import com.razorpay.RazorpayException;
import com.tarsem.BookMyStay.dto.payment.RefundResponseDTO;

import java.nio.file.AccessDeniedException;

public interface RefundService {

    RefundResponseDTO refund(Long bookingId) throws AccessDeniedException, RazorpayException;

}
