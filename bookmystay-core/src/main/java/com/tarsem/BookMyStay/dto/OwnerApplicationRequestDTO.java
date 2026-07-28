package com.tarsem.BookMyStay.dto;

import com.tarsem.BookMyStay.Enums.GovernmentIdType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OwnerApplicationRequestDTO {

    @NotNull(message = "Government ID type is required")
    private GovernmentIdType governmentIdType;

    @NotBlank(message = "Government ID number is required")
    private String governmentIdNumber;

    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Invalid phone number"
    )
    private String phoneNumber;

    @NotBlank(message = "Business address is required")
    private String businessAddress;
}