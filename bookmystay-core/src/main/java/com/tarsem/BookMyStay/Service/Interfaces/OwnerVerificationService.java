package com.tarsem.BookMyStay.Service.Interfaces;

import com.tarsem.BookMyStay.dto.owner.OwnerApplicationRequestDTO;
import com.tarsem.BookMyStay.dto.owner.OwnerVerificationRequestDTO;
import com.tarsem.BookMyStay.dto.owner.OwnerVerificationResponseDTO;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

public interface OwnerVerificationService {
    
    OwnerVerificationResponseDTO getMyVerification();

    String verificationOwner(OwnerApplicationRequestDTO requestDTO, MultipartFile governmentIdFront, MultipartFile governmentIdBack) throws IOException;

    void resubmitVerification(@Valid OwnerVerificationRequestDTO request, MultipartFile governmentIdFront, MultipartFile governmentIdBack) throws IOException;
}
