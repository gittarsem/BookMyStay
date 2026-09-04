package com.tarsem.BookMyStay.Service.Interfaces;

import com.tarsem.BookMyStay.Enums.Role;
import com.tarsem.BookMyStay.Enums.VerificationStatus;
import com.tarsem.BookMyStay.dto.User.UserDTO;
import com.tarsem.BookMyStay.dto.admin.AdminDashboardDTO;
import com.tarsem.BookMyStay.dto.admin.AdminHotelDTO;
import com.tarsem.BookMyStay.dto.admin.AdminReviewDTO;
import com.tarsem.BookMyStay.dto.admin.AdminActivityDTO;
import com.tarsem.BookMyStay.dto.admin.AdminReportDTO;
import com.tarsem.BookMyStay.dto.owner.OwnerVerificationResponseDTO;
import com.tarsem.BookMyStay.dto.owner.RejectionRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {

    // =========================================================
    // DASHBOARD
    // =========================================================

    AdminDashboardDTO getDashboard();


    // =========================================================
    // USERS
    // =========================================================

    Page<UserDTO> getUsers(
            String search,
            Role role,
            Pageable pageable
    );

    UserDTO getUser(Long userId);

    String approveOwner(Long userId);

    String removeOwnerRole(Long userId);


    // =========================================================
    // HOTELS
    // =========================================================

    Page<AdminHotelDTO> getHotels(
            String search,
            Boolean active,
            Pageable pageable
    );

    AdminHotelDTO getHotel(Long hotelId);

    String activateHotel(Long hotelId);

    String suspendHotel(Long hotelId);

    String deleteHotel(Long hotelId);


    // =========================================================
    // OWNER VERIFICATION
    // =========================================================

    List<OwnerVerificationResponseDTO> getPendingApplication(
            VerificationStatus verificationStatus
    );

    void approveApplication(Long verificationId);

    void rejectApplication(
            Long verificationId,
            RejectionRequestDTO request
    );


    // =========================================================
    // REVIEWS
    // =========================================================

    Page<AdminReviewDTO> getReviews(
            Integer rating,
            Pageable pageable
    );

    AdminReviewDTO getReview(Long reviewId);

    void deleteReview(Long reviewId);


    // =========================================================
    // REPORTS
    // =========================================================

    Page<AdminReportDTO> getReports(
            String status,
            Pageable pageable
    );

    AdminReportDTO getReport(Long reportId);

    void resolveReport(Long reportId);

    void dismissReport(Long reportId);


    // =========================================================
    // ACTIVITY
    // =========================================================

    Page<AdminActivityDTO> getActivity(
            Pageable pageable
    );
}