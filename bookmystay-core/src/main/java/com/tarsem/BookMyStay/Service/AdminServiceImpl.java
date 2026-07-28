package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.OwnerVerificationEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Enums.Role;
import com.tarsem.BookMyStay.Enums.VerificationStatus;
import com.tarsem.BookMyStay.Exceptions.ResourceNotFoundException;
import com.tarsem.BookMyStay.Repositroy.OwnerVerificationRepository;
import com.tarsem.BookMyStay.Repositroy.UserRepository;
import com.tarsem.BookMyStay.Service.Interfaces.AdminService;
import com.tarsem.BookMyStay.dto.OwnerVerificationResponseDTO;
import com.tarsem.BookMyStay.dto.RejectionRequestDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.tarsem.BookMyStay.Utils.AppUtils.giveMeCurrentUser;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final OwnerVerificationRepository verificationRepository;
    private final ModelMapper modelMapper;
    @Override
    public String approveOwner(Long userId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRoles().contains(Role.ROLE_OWNER)) {
            throw new IllegalStateException("User is already an owner.");
        }

        user.getRoles().add(Role.ROLE_OWNER);

        userRepository.save(user);

        return "APPROVED SUCCESSFULLY";
    }

    @Override
    public List<OwnerVerificationResponseDTO> getPendingApplication(VerificationStatus verificationStatus) {
        List<OwnerVerificationEntity> applications =
                verificationRepository.findByVerificationStatus(
                        VerificationStatus.PENDING
                );

        if (applications.isEmpty()) {
            throw new ResourceNotFoundException("No pending verification requests found.");
        }

        return applications.stream()
                .map(application -> OwnerVerificationResponseDTO.builder()
                        .id(application.getId())
                        .applicantName(application.getUser().getName())
                        .applicantEmail(application.getUser().getEmail())
                        .governmentIdType(application.getGovernmentIdType())
                        .businessName(application.getBusinessName())
                        .phoneNumber(application.getPhoneNumber())
                        .businessAddress(application.getBusinessAddress())
                        .verificationStatus(application.getVerificationStatus())
                        .submittedAt(application.getSubmittedAt())
                        .build())
                .toList();
    }

    @Override
    public void approveApplication(Long verificationId) {
        OwnerVerificationEntity verificationEntity=verificationRepository.findById(verificationId).orElseThrow(
                ()-> new ResourceNotFoundException("Verification request not found.")
        );

        if(verificationEntity.getVerificationStatus()!=VerificationStatus.PENDING){
            throw new IllegalStateException(
                    "Only pending applications can be approved.");
        }

        approveOwner(verificationEntity.getUser().getId());
        verificationEntity.setVerificationStatus(VerificationStatus.APPROVED);
        verificationEntity.setReviewedBy(giveMeCurrentUser());
        verificationEntity.setReviewedAt(LocalDateTime.now());
        verificationRepository.save(verificationEntity);
    }

    @Override
    public void rejectApplication(Long verificationId, RejectionRequestDTO request) {
        OwnerVerificationEntity verificationEntity=verificationRepository.findById(verificationId).orElseThrow(
                ()-> new ResourceNotFoundException("Verification request not found.")
        );

        if(verificationEntity.getVerificationStatus()!=VerificationStatus.PENDING){
            throw new IllegalStateException(
                    "Only pending applications can be approved.");
        }
        approveOwner(verificationEntity.getUser().getId());
        verificationEntity.setVerificationStatus(VerificationStatus.REJECTED);
        verificationEntity.setReviewedBy(giveMeCurrentUser());
        verificationEntity.setReviewedAt(LocalDateTime.now());
        verificationEntity.setRejectionReason(request.getReason());
        verificationRepository.save(verificationEntity);
    }

}
