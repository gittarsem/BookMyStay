package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.OwnerVerificationEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Enums.Role;
import com.tarsem.BookMyStay.Enums.VerificationStatus;
import com.tarsem.BookMyStay.Repositroy.OwnerVerificationRepository;
import com.tarsem.BookMyStay.Repositroy.UserRepository;
import com.tarsem.BookMyStay.Service.Interfaces.OwnerVerificationService;
import com.tarsem.BookMyStay.dto.OwnerApplicationRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;

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
}
