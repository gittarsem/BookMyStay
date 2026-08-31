package com.tarsem.BookMyStay.dto.owner;

import com.tarsem.BookMyStay.Enums.GovernmentIdType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerVerificationRequestDTO {

    private String businessName;
    private String businessAddress;
    private String phoneNumber;
    private GovernmentIdType governmentIdType;
    private String governmentIdNumber;
}
