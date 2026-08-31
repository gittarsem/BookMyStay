package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.OwnerVerificationEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Enums.Role;
import com.tarsem.BookMyStay.Enums.VerificationStatus;
import com.tarsem.BookMyStay.Exceptions.BusinessRuleViolationException;
import com.tarsem.BookMyStay.Exceptions.ResourceNotFoundException;
import com.tarsem.BookMyStay.Repositroy.OwnerVerificationRepository;
import com.tarsem.BookMyStay.Repositroy.UserRepository;
import com.tarsem.BookMyStay.Service.Interfaces.CloudinaryService;
import com.tarsem.BookMyStay.Service.Interfaces.OwnerVerificationService;
import com.tarsem.BookMyStay.dto.owner.OwnerApplicationRequestDTO;
import com.tarsem.BookMyStay.dto.owner.OwnerVerificationRequestDTO;
import com.tarsem.BookMyStay.dto.owner.OwnerVerificationResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

import static com.tarsem.BookMyStay.Utils.AppUtils.giveMeCurrentUser;

@Service
@RequiredArgsConstructor
public class OwnerVerificationServiceImpl implements OwnerVerificationService {

    private final UserRepository userRepository;
    private final OwnerVerificationRepository verificationRepository;
    private final CloudinaryService cloudinaryService;



    @Override
    public String verificationOwner(OwnerApplicationRequestDTO requestDTO, MultipartFile governmentIdFront, MultipartFile governmentIdBack) throws IOException {

        UserEntity user = giveMeCurrentUser();

        if (user.getRoles().contains(Role.ROLE_OWNER)) {
            throw new AccessDeniedException("User is already an owner.");
        }

        if (verificationRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("Owner verification request is already submitted.");
        }

        String frontendUrl=cloudinaryService.uploadImage(governmentIdFront);
        String backendUrl=cloudinaryService.uploadImage(governmentIdBack);

        OwnerVerificationEntity verification = OwnerVerificationEntity.builder()
                .user(user)
                .governmentIdType(requestDTO.getGovernmentIdType())
                .governmentIdNumber(requestDTO.getGovernmentIdNumber())
                .businessName(requestDTO.getBusinessName())
                .phoneNumber(requestDTO.getPhoneNumber())
                .businessAddress(requestDTO.getBusinessAddress())
                .verificationStatus(VerificationStatus.PENDING)
                .govtIdFront(frontendUrl)
                .govtIdBack(backendUrl)
                .build();

        verificationRepository.save(verification);

        return "Owner verification request submitted successfully.";
    }

    @Transactional
    @Override
    public void resubmitVerification(OwnerVerificationRequestDTO request, MultipartFile governmentIdFront, MultipartFile governmentIdBack) throws IOException {


        UserEntity user = giveMeCurrentUser();

        String frontendUrl=cloudinaryService.uploadImage(governmentIdFront);
        String backendUrl=cloudinaryService.uploadImage(governmentIdBack);
        OwnerVerificationEntity verification =
                verificationRepository.findByUser(user)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Verification request not found."
                                ));

        switch (verification.getVerificationStatus()) {

            case APPROVED ->
                    throw new BusinessRuleViolationException(
                            "Owner is already verified."
                    );

            case PENDING ->
                    throw new BusinessRuleViolationException(
                            "Verification request is already pending."
                    );

            case REJECTED -> {

                verification.setBusinessName(request.getBusinessName());
                verification.setBusinessAddress(request.getBusinessAddress());
                verification.setPhoneNumber(request.getPhoneNumber());

                verification.setGovernmentIdType(
                        request.getGovernmentIdType()
                );

                verification.setGovernmentIdNumber(
                        request.getGovernmentIdNumber()
                );


                verification.setVerificationStatus(
                        VerificationStatus.PENDING
                );

                verification.setSubmittedAt(LocalDateTime.now());
                verification.setGovtIdFront(frontendUrl);
                verification.setGovtIdBack(backendUrl);
                verification.setReviewedAt(LocalDateTime.now());
                verification.setReviewedBy(null);
                verification.setRejectionReason(verification.getRejectionReason());

                verificationRepository.save(verification);
            }
        }
    }

    @Override
    public OwnerVerificationResponseDTO getMyVerification() {

        UserEntity currentUser = giveMeCurrentUser();

        OwnerVerificationEntity verification = verificationRepository
                .findByUser(currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Owner verification application not found")
                );

        return OwnerVerificationResponseDTO.builder()
                .id(verification.getId())
                .applicantName(currentUser.getName())
                .applicantEmail(currentUser.getEmail())
                .governmentIdType(verification.getGovernmentIdType())
                .governmentIdNumber(verification.getGovernmentIdNumber())
                .businessName(verification.getBusinessName())
                .phoneNumber(verification.getPhoneNumber())
                .businessAddress(verification.getBusinessAddress())
                .verificationStatus(verification.getVerificationStatus())
                .rejectionReason(verification.getRejectionReason())
                .submittedAt(verification.getSubmittedAt())
                .build();
    }


}
