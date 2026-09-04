package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.*;
import com.tarsem.BookMyStay.Enums.ReportStatus;
import com.tarsem.BookMyStay.Enums.Role;
import com.tarsem.BookMyStay.Enums.VerificationStatus;
import com.tarsem.BookMyStay.Exceptions.BusinessRuleViolationException;
import com.tarsem.BookMyStay.Exceptions.HotelNotFoundException;
import com.tarsem.BookMyStay.Exceptions.ResourceNotFoundException;
import com.tarsem.BookMyStay.Exceptions.ReviewNotFoundException;
import com.tarsem.BookMyStay.Repositroy.AdminActivityRepository;
import com.tarsem.BookMyStay.Repositroy.HotelElasticRepository;
import com.tarsem.BookMyStay.Repositroy.HotelRepository;
import com.tarsem.BookMyStay.Repositroy.OwnerVerificationRepository;
import com.tarsem.BookMyStay.Repositroy.ReportRepository;
import com.tarsem.BookMyStay.Repositroy.ReviewRepository;
import com.tarsem.BookMyStay.Repositroy.UserRepository;
import com.tarsem.BookMyStay.Service.Interfaces.AdminService;
import com.tarsem.BookMyStay.dto.User.UserDTO;
import com.tarsem.BookMyStay.dto.admin.AdminActivityDTO;
import com.tarsem.BookMyStay.dto.admin.AdminDashboardDTO;
import com.tarsem.BookMyStay.dto.admin.AdminHotelDTO;
import com.tarsem.BookMyStay.dto.admin.AdminReportDTO;
import com.tarsem.BookMyStay.dto.admin.AdminReviewDTO;
import com.tarsem.BookMyStay.dto.owner.OwnerVerificationResponseDTO;
import com.tarsem.BookMyStay.dto.owner.RejectionRequestDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.tarsem.BookMyStay.Utils.AppUtils.giveMeCurrentUser;
import static com.tarsem.BookMyStay.Utils.AppUtils.mapToDocument;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final OwnerVerificationRepository verificationRepository;
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;
    private final AdminActivityRepository activityRepository;
    private final HotelElasticRepository elasticRepository;
    private final ModelMapper modelMapper;


    // =========================================================
    // PAGINATION
    // =========================================================

    /**
     * Creates a safe pageable for admin listing endpoints.
     *
     * IMPORTANT:
     * We intentionally remove sorting here.
     *
     * The current entities contain Java properties such as
     * "created_at", but Spring Data interprets "_" as a
     * nested-property separator when resolving Sort properties.
     *
     * Therefore:
     *
     * created_at -> created.at
     *
     * which causes:
     *
     * No property 'created' found for type ...
     *
     * Admin listing endpoints do not need server-side sorting
     * yet, so we use an UNSORTED pageable.
     */
    private Pageable createSafePageable(Pageable pageable) {

        int page = 0;
        int size = 20;

        if (pageable != null) {

            page = Math.max(
                    pageable.getPageNumber(),
                    0
            );

            size = pageable.getPageSize();
        }

        /*
         * Prevent invalid or excessively large page sizes.
         */
        size = Math.min(
                Math.max(size, 1),
                100
        );

        /*
         * CRITICAL:
         * Do NOT copy pageable.getSort().
         *
         * PageRequest.of(page, size) creates an UNSORTED pageable.
         */
        return PageRequest.of(
                page,
                size
        );
    }


    // =========================================================
    // DASHBOARD
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboard() {

        long totalUsers =
                userRepository.count();

        long totalOwners =
                userRepository.countByRolesContaining(
                        Role.ROLE_OWNER
                );

        long totalGuests =
                userRepository.countByRolesContaining(
                        Role.ROLE_GUEST
                );

        long totalHotels =
                hotelRepository.count();

        long activeHotels =
                hotelRepository.countByActiveTrue();

        long suspendedHotels =
                hotelRepository.countByActiveFalse();

        long pendingVerifications =
                verificationRepository
                        .countByVerificationStatus(
                                VerificationStatus.PENDING
                        );

        long totalReviews =
                reviewRepository.count();

        long totalReports =
                reportRepository.count();

        long pendingReports =
                reportRepository.countByStatus(
                        ReportStatus.PENDING
                );

        return AdminDashboardDTO.builder()
                .totalUsers(totalUsers)
                .totalOwners(totalOwners)
                .totalGuests(totalGuests)
                .totalHotels(totalHotels)
                .activeHotels(activeHotels)
                .suspendedHotels(suspendedHotels)
                .pendingOwnerVerifications(
                        pendingVerifications
                )
                .totalReviews(totalReviews)
                .totalReports(totalReports)
                .pendingReports(pendingReports)
                .build();
    }


    // =========================================================
    // USERS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getUsers(
            String search,
            Role role,
            Pageable pageable
    ) {

        Pageable safePageable =
                createSafePageable(pageable);

        Specification<UserEntity> specification =
                Specification.unrestricted();

        /*
         * Search by name or email.
         */
        if (search != null && !search.isBlank()) {

            String value =
                    "%" +
                            search.trim().toLowerCase() +
                            "%";

            specification =
                    specification.and(
                            (root, query, cb) ->
                                    cb.or(
                                            cb.like(
                                                    cb.lower(
                                                            root.get("name")
                                                    ),
                                                    value
                                            ),
                                            cb.like(
                                                    cb.lower(
                                                            root.get("email")
                                                    ),
                                                    value
                                            )
                                    )
                    );
        }

        /*
         * Filter by role.
         */
        if (role != null) {

            specification =
                    specification.and(
                            (root, query, cb) ->
                                    cb.isMember(
                                            role,
                                            root.get("roles")
                                    )
                    );
        }

        Page<UserEntity> users =
                userRepository.findAll(
                        specification,
                        safePageable
                );

        /*
         * Empty database result is completely valid.
         * Spring Data returns an empty Page, which .map()
         * also handles correctly.
         */
        if (users == null || users.isEmpty()) {

            return Page.empty(
                    safePageable
            );
        }

        return users.map(
                user ->
                        modelMapper.map(
                                user,
                                UserDTO.class
                        )
        );
    }


    @Override
    @Transactional(readOnly = true)
    public UserDTO getUser(
            Long userId
    ) {

        UserEntity user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found with id: " +
                                                userId
                                )
                        );

        return modelMapper.map(
                user,
                UserDTO.class
        );
    }


    @Override
    @Transactional
    public String approveOwner(
            Long userId
    ) {

        UserEntity user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found with id: " +
                                                userId
                                )
                        );

        if (
                user.getRoles()
                        .contains(Role.ROLE_OWNER)
        ) {

            throw new BusinessRuleViolationException(
                    "User is already an owner."
            );
        }

        user.getRoles()
                .add(Role.ROLE_OWNER);

        userRepository.save(user);

        logActivity(
                "ROLE_GRANTED",
                "USER",
                userId,
                "Owner role granted to user"
        );

        return "OWNER ROLE GRANTED SUCCESSFULLY";
    }


    @Override
    @Transactional
    public String removeOwnerRole(
            Long userId
    ) {

        UserEntity currentUser =
                giveMeCurrentUser();

        if (
                currentUser != null &&
                        currentUser.getId()
                                .equals(userId)
        ) {

            throw new BusinessRuleViolationException(
                    "Administrator cannot remove their own role."
            );
        }

        UserEntity user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found with id: " +
                                                userId
                                )
                        );

        if (
                !user.getRoles()
                        .contains(Role.ROLE_OWNER)
        ) {

            throw new BusinessRuleViolationException(
                    "User is not an owner."
            );
        }

        user.getRoles()
                .remove(Role.ROLE_OWNER);

        userRepository.save(user);

        logActivity(
                "ROLE_REMOVED",
                "USER",
                userId,
                "Owner role removed from user"
        );

        return "OWNER ROLE REMOVED SUCCESSFULLY";
    }


    // =========================================================
    // HOTELS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<AdminHotelDTO> getHotels(
            String search,
            Boolean active,
            Pageable pageable
    ) {

        Pageable safePageable =
                createSafePageable(pageable);

        Specification<HotelEntity> specification =
                Specification.unrestricted();

        /*
         * Search by hotel name or city.
         */
        if (
                search != null &&
                        !search.isBlank()
        ) {

            String value =
                    "%" +
                            search.trim()
                                    .toLowerCase() +
                            "%";

            specification =
                    specification.and(
                            (root, query, cb) ->
                                    cb.or(
                                            cb.like(
                                                    cb.lower(
                                                            root.get("name")
                                                    ),
                                                    value
                                            ),
                                            cb.like(
                                                    cb.lower(
                                                            root.get("city")
                                                    ),
                                                    value
                                            )
                                    )
                    );
        }

        /*
         * Filter by active/suspended state.
         */
        if (active != null) {

            specification =
                    specification.and(
                            (root, query, cb) ->
                                    cb.equal(
                                            root.get("active"),
                                            active
                                    )
                    );
        }

        Page<HotelEntity> hotels =
                hotelRepository.findAll(
                        specification,
                        safePageable
                );

        if (
                hotels == null ||
                        hotels.isEmpty()
        ) {

            return Page.empty(
                    safePageable
            );
        }

        return hotels.map(
                this::mapHotel
        );
    }


    @Override
    @Transactional(readOnly = true)
    public AdminHotelDTO getHotel(
            Long hotelId
    ) {

        HotelEntity hotel =
                hotelRepository
                        .findById(hotelId)
                        .orElseThrow(() ->
                                new HotelNotFoundException(
                                        "Hotel not found with id: " +
                                                hotelId
                                )
                        );

        return mapHotel(hotel);
    }


    @Override
    @Transactional
    public String activateHotel(
            Long hotelId
    ) {

        HotelEntity hotel =
                hotelRepository
                        .findById(hotelId)
                        .orElseThrow(() ->
                                new HotelNotFoundException(
                                        "Hotel not found with id: " +
                                                hotelId
                                )
                        );

        if (
                Boolean.TRUE.equals(
                        hotel.getActive()
                )
        ) {

            return "Hotel is already active";
        }

        hotel.setActive(true);

        hotelRepository.save(hotel);

        /*
         * Elasticsearch is secondary to the database.
         * If Elasticsearch fails, the DB update should
         * remain successful.
         */
        try {

            elasticRepository.save(
                    mapToDocument(hotel)
            );

        } catch (Exception ignored) {
            // Database operation already succeeded.
        }

        logActivity(
                "HOTEL_ACTIVATED",
                "HOTEL",
                hotelId,
                "Hotel activated by administrator"
        );

        return "Hotel activated successfully";
    }


    @Override
    @Transactional
    public String suspendHotel(
            Long hotelId
    ) {

        HotelEntity hotel =
                hotelRepository
                        .findById(hotelId)
                        .orElseThrow(() ->
                                new HotelNotFoundException(
                                        "Hotel not found with id: " +
                                                hotelId
                                )
                        );

        if (
                Boolean.FALSE.equals(
                        hotel.getActive()
                )
        ) {

            return "Hotel is already suspended";
        }

        hotel.setActive(false);

        hotelRepository.save(hotel);

        /*
         * ES failure must not make the successful DB
         * operation return 500.
         */
        try {

            elasticRepository.deleteById(
                    hotelId.toString()
            );

        } catch (Exception ignored) {
            // Database operation already succeeded.
        }

        logActivity(
                "HOTEL_SUSPENDED",
                "HOTEL",
                hotelId,
                "Hotel suspended by administrator"
        );

        return "Hotel suspended successfully";
    }


    @Override
    @Transactional
    public String deleteHotel(
            Long hotelId
    ) {

        HotelEntity hotel =
                hotelRepository
                        .findById(hotelId)
                        .orElseThrow(() ->
                                new HotelNotFoundException(
                                        "Hotel not found with id: " +
                                                hotelId
                                )
                        );

        hotelRepository.delete(hotel);

        /*
         * Elasticsearch cleanup should not turn a
         * successful database deletion into an error.
         */
        try {

            elasticRepository.deleteById(
                    hotelId.toString()
            );

        } catch (Exception ignored) {
            // Database deletion already succeeded.
        }

        logActivity(
                "HOTEL_DELETED",
                "HOTEL",
                hotelId,
                "Hotel deleted by administrator"
        );

        return "Hotel deleted successfully";
    }


    private AdminHotelDTO mapHotel(
            HotelEntity hotel
    ) {

        if (hotel == null) {
            return null;
        }

        return AdminHotelDTO.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .city(hotel.getCity())
                .active(
                        Boolean.TRUE.equals(
                                hotel.getActive()
                        )
                )
                .averageRating(
                        hotel.getAverageRating()
                )
                .totalReviews(
                        hotel.getTotalReviews()
                )
                .build();
    }


    // =========================================================
    // OWNER VERIFICATIONS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<OwnerVerificationResponseDTO>
    getPendingApplication(
            VerificationStatus verificationStatus
    ) {

        List<OwnerVerificationEntity> applications =
                verificationRepository
                        .findByVerificationStatus(
                                verificationStatus
                        );

        if (
                applications == null ||
                        applications.isEmpty()
        ) {

            return List.of();
        }

        return applications
                .stream()
                .map(application ->
                        OwnerVerificationResponseDTO
                                .builder()
                                .id(
                                        application.getId()
                                )
                                .applicantName(
                                        application.getUser() != null
                                                ? application.getUser().getName()
                                                : null
                                )
                                .applicantEmail(
                                        application.getUser() != null
                                                ? application.getUser().getEmail()
                                                : null
                                )
                                .governmentIdType(
                                        application
                                                .getGovernmentIdType()
                                )
                                .governmentIdNumber(
                                        application
                                                .getGovernmentIdNumber()
                                )
                                .govtIdBack(application.getGovtIdBack())
                                .govtIdFront(application.getGovtIdFront())
                                .businessName(
                                        application
                                                .getBusinessName()
                                )
                                .phoneNumber(
                                        application
                                                .getPhoneNumber()
                                )
                                .businessAddress(
                                        application
                                                .getBusinessAddress()
                                )
                                .verificationStatus(
                                        application
                                                .getVerificationStatus()
                                )
                                .rejectionReason(
                                        application
                                                .getRejectionReason()
                                )
                                .submittedAt(
                                        application
                                                .getSubmittedAt()
                                )
                                .build()
                )
                .toList();
    }


    @Override
    @Transactional
    public void approveApplication(
            Long verificationId
    ) {

        OwnerVerificationEntity verification =
                verificationRepository
                        .findById(verificationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Verification request not found."
                                )
                        );

        if (
                verification
                        .getVerificationStatus()
                        != VerificationStatus.PENDING
        ) {

            throw new BusinessRuleViolationException(
                    "Only pending applications can be approved."
            );
        }

        UserEntity applicant =
                verification.getUser();

        if (
                applicant.getRoles()
                        .contains(Role.ROLE_OWNER)
        ) {

            throw new BusinessRuleViolationException(
                    "User is already an owner."
            );
        }

        applicant.getRoles()
                .add(Role.ROLE_OWNER);

        userRepository.save(applicant);

        verification.setVerificationStatus(
                VerificationStatus.APPROVED
        );

        verification.setReviewedBy(
                giveMeCurrentUser()
        );

        verification.setReviewedAt(
                LocalDateTime.now()
        );

        verificationRepository.save(
                verification
        );

        logActivity(
                "OWNER_APPLICATION_APPROVED",
                "OWNER_VERIFICATION",
                verificationId,
                "Owner application approved"
        );
    }


    @Override
    @Transactional
    public void rejectApplication(
            Long verificationId,
            RejectionRequestDTO request
    ) {

        OwnerVerificationEntity verification =
                verificationRepository
                        .findById(verificationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Verification request not found."
                                )
                        );

        if (
                verification
                        .getVerificationStatus()
                        != VerificationStatus.PENDING
        ) {

            throw new BusinessRuleViolationException(
                    "Only pending applications can be rejected."
            );
        }

        verification.setVerificationStatus(
                VerificationStatus.REJECTED
        );

        verification.setReviewedBy(
                giveMeCurrentUser()
        );

        verification.setReviewedAt(
                LocalDateTime.now()
        );

        verification.setRejectionReason(
                request.getReason()
        );

        verificationRepository.save(
                verification
        );

        logActivity(
                "OWNER_APPLICATION_REJECTED",
                "OWNER_VERIFICATION",
                verificationId,
                "Owner application rejected"
        );
    }


    // =========================================================
    // REVIEWS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<AdminReviewDTO> getReviews(
            Integer rating,
            Pageable pageable
    ) {

        Pageable safePageable =
                createSafePageable(pageable);

        Page<ReviewEntity> reviews;

        /*
         * Invalid rating should simply return no results,
         * rather than throwing an exception.
         */
        if (
                rating != null &&
                        (rating < 1 || rating > 5)
        ) {

            return Page.empty(
                    safePageable
            );
        }

        if (rating == null) {

            reviews =
                    reviewRepository.findAll(
                            safePageable
                    );

        } else {

            reviews =
                    reviewRepository.findByRating(
                            rating,
                            safePageable
                    );
        }

        if (
                reviews == null ||
                        reviews.isEmpty()
        ) {

            return Page.empty(
                    safePageable
            );
        }

        return reviews.map(
                review ->
                        AdminReviewDTO.builder()
                                .reviewId(
                                        review.getId()
                                )
                                .guestId(
                                        review.getGuest()
                                                .getId()
                                )
                                .guestName(
                                        review.getGuest()
                                                .getName()
                                )
                                .hotelId(
                                        review.getHotel()
                                                .getId()
                                )
                                .hotelName(
                                        review.getHotel()
                                                .getName()
                                )
                                .rating(
                                        review.getRating()
                                )
                                .comment(
                                        review.getComment()
                                )
                                .createdAt(
                                        review.getCreatedAt()
                                )
                                .build()
        );
    }


    @Override
    @Transactional(readOnly = true)
    public AdminReviewDTO getReview(
            Long reviewId
    ) {

        ReviewEntity review =
                reviewRepository
                        .findById(reviewId)
                        .orElseThrow(() ->
                                new ReviewNotFoundException(
                                        "Review not found : " +
                                                reviewId
                                )
                        );

        return AdminReviewDTO.builder()
                .reviewId(review.getId())
                .guestId(
                        review.getGuest().getId()
                )
                .guestName(
                        review.getGuest().getName()
                )
                .hotelId(
                        review.getHotel().getId()
                )
                .hotelName(
                        review.getHotel().getName()
                )
                .rating(
                        review.getRating()
                )
                .comment(
                        review.getComment()
                )
                .createdAt(
                        review.getCreatedAt()
                )
                .build();
    }


    @Override
    @Transactional
    public void deleteReview(
            Long reviewId
    ) {

        ReviewEntity review =
                reviewRepository
                        .findById(reviewId)
                        .orElseThrow(() ->
                                new ReviewNotFoundException(
                                        "Review not found : " +
                                                reviewId
                                )
                        );

        Long hotelId =
                review.getHotel()
                        .getId();

        reviewRepository.delete(review);

        updateHotelRating(hotelId);

        logActivity(
                "REVIEW_DELETED",
                "REVIEW",
                reviewId,
                "Review deleted by administrator"
        );
    }


    private void updateHotelRating(
            Long hotelId
    ) {

        var statistics =
                reviewRepository
                        .calculateHotelStatistics(
                                hotelId
                        );

        HotelEntity hotel =
                hotelRepository
                        .findById(hotelId)
                        .orElseThrow(() ->
                                new HotelNotFoundException(
                                        "Hotel not found with id: " +
                                                hotelId
                                )
                        );

        hotel.setAverageRating(
                statistics.getAverageRating() == null
                        ? 0.0
                        : statistics.getAverageRating()
        );

        hotel.setTotalReviews(
                Math.toIntExact(
                        statistics.getTotalReviews()
                )
        );

        hotelRepository.save(hotel);

        /*
         * Keep Elasticsearch secondary.
         */
        try {

            elasticRepository.save(
                    mapToDocument(hotel)
            );

        } catch (Exception ignored) {
            // DB update already succeeded.
        }
    }


    // =========================================================
    // REPORTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<AdminReportDTO> getReports(
            String status,
            Pageable pageable
    ) {

        Pageable safePageable =
                createSafePageable(pageable);

        Page<ReportEntity> reports;

        if (
                status == null ||
                        status.isBlank()
        ) {

            reports =
                    reportRepository.findAll(
                            safePageable
                    );

        } else {

            ReportStatus reportStatus;

            try {

                reportStatus =
                        ReportStatus.valueOf(
                                status.trim()
                                        .toUpperCase()
                        );

            } catch (IllegalArgumentException exception) {

                /*
                 * Invalid filter = no results.
                 * Do not throw 500.
                 */
                return Page.empty(
                        safePageable
                );
            }

            reports =
                    reportRepository.findByStatus(
                            reportStatus,
                            safePageable
                    );
        }

        if (
                reports == null ||
                        reports.isEmpty()
        ) {

            return Page.empty(
                    safePageable
            );
        }

        return reports.map(
                this::mapReport
        );
    }


    @Override
    @Transactional(readOnly = true)
    public AdminReportDTO getReport(
            Long reportId
    ) {

        ReportEntity report =
                reportRepository
                        .findById(reportId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Report not found with id: " +
                                                reportId
                                )
                        );

        return mapReport(report);
    }


    @Override
    @Transactional
    public void resolveReport(
            Long reportId
    ) {

        ReportEntity report =
                reportRepository
                        .findById(reportId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Report not found with id: " +
                                                reportId
                                )
                        );

        if (
                report.getStatus()
                        != ReportStatus.PENDING
        ) {

            throw new BusinessRuleViolationException(
                    "Only pending reports can be resolved."
            );
        }

        UserEntity admin =
                giveMeCurrentUser();

        report.setStatus(
                ReportStatus.RESOLVED
        );

        report.setResolvedBy(admin);

        report.setResolvedAt(
                LocalDateTime.now()
        );

        reportRepository.save(report);

        logActivity(
                "REPORT_RESOLVED",
                "REPORT",
                reportId,
                "Report resolved by administrator"
        );
    }


    @Override
    @Transactional
    public void dismissReport(
            Long reportId
    ) {

        ReportEntity report =
                reportRepository
                        .findById(reportId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Report not found with id: " +
                                                reportId
                                )
                        );

        if (
                report.getStatus()
                        != ReportStatus.PENDING
        ) {

            throw new BusinessRuleViolationException(
                    "Only pending reports can be dismissed."
            );
        }

        UserEntity admin =
                giveMeCurrentUser();

        report.setStatus(
                ReportStatus.DISMISSED
        );

        report.setResolvedBy(admin);

        report.setResolvedAt(
                LocalDateTime.now()
        );

        reportRepository.save(report);

        logActivity(
                "REPORT_DISMISSED",
                "REPORT",
                reportId,
                "Report dismissed by administrator"
        );
    }


    private AdminReportDTO mapReport(
            ReportEntity report
    ) {

        UserEntity reporter =
                report.getReporter();

        UserEntity resolver =
                report.getResolvedBy();

        return AdminReportDTO.builder()
                .id(report.getId())
                .reporterId(
                        reporter == null
                                ? null
                                : reporter.getId()
                )
                .reporterName(
                        reporter == null
                                ? null
                                : reporter.getName()
                )
                .targetType(
                        report.getTargetType()
                )
                .targetId(
                        report.getTargetId()
                )
                .reason(
                        report.getReason()
                )
                .status(
                        report.getStatus()
                )
                .createdAt(
                        report.getCreatedAt()
                )
                .resolvedAt(
                        report.getResolvedAt()
                )
                .resolvedById(
                        resolver == null
                                ? null
                                : resolver.getId()
                )
                .resolvedByName(
                        resolver == null
                                ? null
                                : resolver.getName()
                )
                .build();
    }


    // =========================================================
    // ACTIVITY
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<AdminActivityDTO> getActivity(
            Pageable pageable
    ) {

        Pageable safePageable =
                createSafePageable(pageable);

        Page<AdminActivityDTO> result =
                activityRepository
                        .findAll(safePageable)
                        .map(activity ->
                                AdminActivityDTO.builder()
                                        .id(
                                                activity.getId()
                                        )
                                        .adminId(
                                                activity
                                                        .getAdmin()
                                                        .getId()
                                        )
                                        .adminName(
                                                activity
                                                        .getAdmin()
                                                        .getName()
                                        )
                                        .action(
                                                activity.getAction()
                                        )
                                        .targetType(
                                                activity
                                                        .getTargetType()
                                        )
                                        .targetId(
                                                activity
                                                        .getTargetId()
                                        )
                                        .description(
                                                activity
                                                        .getDescription()
                                        )
                                        .createdAt(
                                                activity
                                                        .getCreatedAt()
                                        )
                                        .build()
                        );

        if (
                result == null ||
                        result.isEmpty()
        ) {

            return Page.empty(
                    safePageable
            );
        }

        return result;
    }


    // =========================================================
    // ACTIVITY LOGGING
    // =========================================================

    private void logActivity(
            String action,
            String targetType,
            Long targetId,
            String description
    ) {

        /*
         * Activity logging is secondary.
         *
         * If activity logging fails, the primary admin
         * operation should NOT become a 500 response.
         */
        try {

            UserEntity admin =
                    giveMeCurrentUser();

            if (admin == null) {
                return;
            }

            AdminActivityEntity activity =
                    AdminActivityEntity.builder()
                            .admin(admin)
                            .action(action)
                            .targetType(targetType)
                            .targetId(targetId)
                            .description(description)
                            .build();

            activityRepository.save(activity);

        } catch (Exception ignored) {
            // Never break the primary admin operation.
        }
    }
}