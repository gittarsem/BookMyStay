package com.tarsem.BookMyStay.Service.Interfaces;


import com.tarsem.BookMyStay.Entity.OwnerVerificationEntity;
import com.tarsem.BookMyStay.Enums.VerificationStatus;
import com.tarsem.BookMyStay.dto.OwnerVerificationResponseDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface AdminService {
    String approveOwner(Long userId);

    List<OwnerVerificationResponseDTO> getPendingApplication(VerificationStatus verificationStatus);

    void approveApplication(Long verificationId);


    void rejectApplication(Long verificationId, com.tarsem.BookMyStay.dto.RejectionRequestDTO request);
}
