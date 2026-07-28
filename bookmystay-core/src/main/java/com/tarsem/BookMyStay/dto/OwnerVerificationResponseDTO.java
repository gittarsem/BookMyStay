package com.tarsem.BookMyStay.dto;

import com.tarsem.BookMyStay.Enums.GovernmentIdType;
import com.tarsem.BookMyStay.Enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerVerificationResponseDTO {

    private Long id;

    private String applicantName;

    private String applicantEmail;

    private GovernmentIdType governmentIdType;

    private String governmentIdNumber;

    private String businessName;

    private String phoneNumber;

    private String businessAddress;

    private VerificationStatus verificationStatus;

    private LocalDateTime submittedAt;
}