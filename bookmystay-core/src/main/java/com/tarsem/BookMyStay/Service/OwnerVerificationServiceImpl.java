package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.OwnerVerificationEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Enums.Role;
import com.tarsem.BookMyStay.Enums.VerificationStatus;
import com.tarsem.BookMyStay.Exceptions.BusinessRuleViolationException;
import com.tarsem.BookMyStay.Exceptions.ResourceNotFoundException;
import com.tarsem.BookMyStay.Repositroy.OwnerVerificationRepository;
import com.tarsem.BookMyStay.Repositroy.UserRepository;
import com.tarsem.BookMyStay.Service.Interfaces.OwnerVerificationService;
import com.tarsem.BookMyStay.dto.owner.OwnerApplicationRequestDTO;
import com.tarsem.BookMyStay.dto.owner.OwnerVerificationRequestDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

import static com.tarsem.BookMyStay.Utils.AppUtils.giveMeCurrentUser;

@Service
@RequiredArgsConstructor
public class OwnerVerificationServiceImpl implements OwnerVerificationService {

    private final UserRepository userRepository;
    private final OwnerVerificationRepository verificationRepository;

    @Override
    public String verificationOwner(OwnerApplicationRequestDTO requestDTO) throws AccessDeniedException {

        UserEntity user = giveMeCurrentUser();

        if (user.getRoles().contains(Role.ROLE_OWNER)) {
            throw new AccessDeniedException("User is already an owner.");
        }

        if (verificationRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("Owner verification request is already submitted.");
        }

        OwnerVerificationEntity verification = OwnerVerificationEntity.builder()
                .user(user)
                .governmentIdType(requestDTO.getGovernmentIdType())
                .governmentIdNumber(requestDTO.getGovernmentIdNumber())
                .businessName(requestDTO.getBusinessName())
                .phoneNumber(requestDTO.getPhoneNumber())
                .businessAddress(requestDTO.getBusinessAddress())
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        verificationRepository.save(verification);

        return "Owner verification request submitted successfully.";
    }

    @Transactional
    @Override
    public void resubmitVerification(OwnerVerificationRequestDTO request) {

        UserEntity user = giveMeCurrentUser();

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

                verification.setReviewedAt(null);
                verification.setReviewedBy(null);
                verification.setRejectionReason(null);

                verificationRepository.save(verification);
            }
        }
    }
}
