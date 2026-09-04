package com.tarsem.BookMyStay.Controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.tarsem.BookMyStay.Enums.Role;
import com.tarsem.BookMyStay.Enums.VerificationStatus;
import com.tarsem.BookMyStay.Repositroy.HotelElasticRepository;
import com.tarsem.BookMyStay.Repositroy.HotelRepository;
import com.tarsem.BookMyStay.Service.Interfaces.AdminService;
import com.tarsem.BookMyStay.Utils.AppUtils;
import com.tarsem.BookMyStay.document.HotelDocument;
import com.tarsem.BookMyStay.dto.User.UserDTO;
import com.tarsem.BookMyStay.dto.admin.AdminActivityDTO;
import com.tarsem.BookMyStay.dto.admin.AdminDashboardDTO;
import com.tarsem.BookMyStay.dto.admin.AdminHotelDTO;
import com.tarsem.BookMyStay.dto.admin.AdminReportDTO;
import com.tarsem.BookMyStay.dto.admin.AdminReviewDTO;
import com.tarsem.BookMyStay.dto.owner.OwnerVerificationResponseDTO;
import com.tarsem.BookMyStay.dto.owner.RejectionRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final HotelRepository hotelRepository;

    private final ElasticsearchClient elasticsearchClient;

    private final HotelElasticRepository elasticRepository;

    private final AdminService adminService;


    // =========================================================
    // DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> getDashboard() {

        return ResponseEntity.ok(
                adminService.getDashboard()
        );
    }


    // =========================================================
    // USERS
    // =========================================================

    @GetMapping("/users")
    public ResponseEntity<Page<UserDTO>> getUsers(

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            Role role,

            Pageable pageable
    ) {

        return ResponseEntity.ok(
                adminService.getUsers(
                        search,
                        role,
                        pageable
                )
        );
    }


    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDTO> getUser(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                adminService.getUser(userId)
        );
    }


    @PatchMapping("/users/{userId}/roles")
    public ResponseEntity<String> changeRole(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                adminService.approveOwner(userId)
        );
    }


    @DeleteMapping("/users/{userId}/owner-role")
    public ResponseEntity<String> removeOwnerRole(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                adminService.removeOwnerRole(userId)
        );
    }


    // =========================================================
    // HOTELS
    // =========================================================

    @GetMapping("/hotels")
    public ResponseEntity<Page<AdminHotelDTO>> getHotels(

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            Boolean active,

            Pageable pageable
    ) {

        return ResponseEntity.ok(
                adminService.getHotels(
                        search,
                        active,
                        pageable
                )
        );
    }


    @GetMapping("/hotels/{hotelId}")
    public ResponseEntity<AdminHotelDTO> getHotel(
            @PathVariable Long hotelId
    ) {

        return ResponseEntity.ok(
                adminService.getHotel(hotelId)
        );
    }


    @PatchMapping("/hotels/{hotelId}/activate")
    public ResponseEntity<String> activateHotel(
            @PathVariable Long hotelId
    ) {

        return ResponseEntity.ok(
                adminService.activateHotel(
                        hotelId
                )
        );
    }


    @PatchMapping("/hotels/{hotelId}/suspend")
    public ResponseEntity<String> suspendHotel(
            @PathVariable Long hotelId
    ) {

        return ResponseEntity.ok(
                adminService.suspendHotel(
                        hotelId
                )
        );
    }


    @DeleteMapping("/hotels/{hotelId}")
    public ResponseEntity<String> deleteHotel(
            @PathVariable Long hotelId
    ) {

        return ResponseEntity.ok(
                adminService.deleteHotel(
                        hotelId
                )
        );
    }


    // =========================================================
    // OWNER VERIFICATIONS
    // =========================================================

    @GetMapping("/owner-verifications/pending")
    public ResponseEntity<List<OwnerVerificationResponseDTO>>
    findPendingApplications() {

        List<OwnerVerificationResponseDTO> applications =
                adminService.getPendingApplication(
                        VerificationStatus.PENDING
                );

        /*
         * Never return null.
         * If there are no pending applications,
         * return an empty JSON array [].
         */
        return ResponseEntity.ok(
                applications == null
                        ? List.of()
                        : applications
        );
    }


    @PutMapping(
            "/owner-verifications/{verificationId}/approve"
    )
    public ResponseEntity<String> approveApplication(
            @PathVariable Long verificationId
    ) {

        adminService.approveApplication(
                verificationId
        );

        return ResponseEntity.ok(
                "Owner application approved successfully."
        );
    }


    @PutMapping(
            "/owner-verifications/{verificationId}/reject"
    )
    public ResponseEntity<String> rejectApplication(
            @PathVariable Long verificationId,

            @Valid
            @RequestBody
            RejectionRequestDTO request
    ) {

        adminService.rejectApplication(
                verificationId,
                request
        );

        return ResponseEntity.ok(
                "Owner application rejected successfully."
        );
    }


    // =========================================================
    // REVIEWS
    // =========================================================

    @GetMapping("/reviews")
    public ResponseEntity<Page<AdminReviewDTO>> getReviews(

            @RequestParam(required = false)
            Integer rating,

            Pageable pageable
    ) {

        return ResponseEntity.ok(
                adminService.getReviews(
                        rating,
                        pageable
                )
        );
    }


    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<AdminReviewDTO> getReview(
            @PathVariable Long reviewId
    ) {

        return ResponseEntity.ok(
                adminService.getReview(
                        reviewId
                )
        );
    }


    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId
    ) {

        adminService.deleteReview(
                reviewId
        );

        return ResponseEntity.noContent()
                .build();
    }


    // =========================================================
    // REPORTS
    // =========================================================

    @GetMapping("/reports")
    public ResponseEntity<Page<AdminReportDTO>> getReports(

            @RequestParam(required = false)
            String status,

            Pageable pageable
    ) {

        return ResponseEntity.ok(
                adminService.getReports(
                        status,
                        pageable
                )
        );
    }


    @GetMapping("/reports/{reportId}")
    public ResponseEntity<AdminReportDTO> getReport(
            @PathVariable Long reportId
    ) {

        return ResponseEntity.ok(
                adminService.getReport(
                        reportId
                )
        );
    }


    @PatchMapping("/reports/{reportId}/resolve")
    public ResponseEntity<Void> resolveReport(
            @PathVariable Long reportId
    ) {

        adminService.resolveReport(
                reportId
        );

        return ResponseEntity.noContent()
                .build();
    }


    @PatchMapping("/reports/{reportId}/dismiss")
    public ResponseEntity<Void> dismissReport(
            @PathVariable Long reportId
    ) {

        adminService.dismissReport(
                reportId
        );

        return ResponseEntity.noContent()
                .build();
    }


    // =========================================================
    // ACTIVITY
    // =========================================================

    @GetMapping("/activity")
    public ResponseEntity<Page<AdminActivityDTO>> getActivity(
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                adminService.getActivity(
                        pageable
                )
        );
    }


    // =========================================================
    // ELASTICSEARCH
    // =========================================================

    @GetMapping("/reindex")
    public ResponseEntity<String> reindex() {

        elasticRepository.deleteAll();

        List<HotelDocument> documents =
                hotelRepository.findAll()
                        .stream()
                        .map(AppUtils::mapToDocument)
                        .toList();

        elasticRepository.saveAll(
                documents
        );

        return ResponseEntity.ok(
                "Reindex completed: " +
                        documents.size()
        );
    }


    @GetMapping("/es-test")
    public ResponseEntity<String> testElasticsearch()
            throws Exception {

        return ResponseEntity.ok(
                elasticsearchClient
                        .info()
                        .clusterName()
        );
    }
}