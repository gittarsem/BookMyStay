package com.tarsem.BookMyStay.Service.Interfaces;

import com.tarsem.BookMyStay.dto.owner.OwnerApplicationRequestDTO;
import com.tarsem.BookMyStay.dto.owner.OwnerVerificationRequestDTO;
import jakarta.validation.Valid;

import java.nio.file.AccessDeniedException;

public interface OwnerVerificationService {
    String verificationOwner(OwnerApplicationRequestDTO requestDTO) throws AccessDeniedException;

    void resubmitVerification(@Valid OwnerVerificationRequestDTO request);
}
