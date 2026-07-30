package com.tarsem.BookMyStay.Service.Interfaces;


import com.tarsem.BookMyStay.Enums.VerificationStatus;
import com.tarsem.BookMyStay.dto.owner.OwnerVerificationResponseDTO;
import com.tarsem.BookMyStay.dto.owner.RejectionRequestDTO;

import java.util.List;

public interface AdminService {
    String approveOwner(Long userId);

    List<OwnerVerificationResponseDTO> getPendingApplication(VerificationStatus verificationStatus);

    void approveApplication(Long verificationId);


    void rejectApplication(Long verificationId, RejectionRequestDTO request);
}
