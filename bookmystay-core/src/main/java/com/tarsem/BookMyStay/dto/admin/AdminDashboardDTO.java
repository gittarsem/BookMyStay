package com.tarsem.BookMyStay.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardDTO {

    private long totalUsers;

    private long totalOwners;

    private long totalGuests;

    private long totalHotels;

    private long activeHotels;

    private long suspendedHotels;

    private long pendingOwnerVerifications;

    private long totalReviews;

    private long totalReports;

    private long pendingReports;
}