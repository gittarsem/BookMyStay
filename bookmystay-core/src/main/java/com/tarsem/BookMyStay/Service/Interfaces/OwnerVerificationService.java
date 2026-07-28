package com.tarsem.BookMyStay.Service.Interfaces;

import com.tarsem.BookMyStay.dto.OwnerApplicationRequestDTO;

import java.nio.file.AccessDeniedException;

public interface OwnerVerificationService {
    String verificationOwner(OwnerApplicationRequestDTO requestDTO) throws AccessDeniedException;
}
