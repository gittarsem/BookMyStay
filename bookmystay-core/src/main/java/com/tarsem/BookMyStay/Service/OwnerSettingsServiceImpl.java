package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.OwnerVerificationEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Enums.Role;
import com.tarsem.BookMyStay.Repositroy.OwnerVerificationRepository;
import com.tarsem.BookMyStay.Repositroy.UserRepository;

import com.tarsem.BookMyStay.Service.Interfaces.OwnerSettingsService;
import com.tarsem.BookMyStay.dto.owner.OwnerSettingsDTO;
import com.tarsem.BookMyStay.dto.owner.UpdateBusinessNameRequestDTO;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.tarsem.BookMyStay.Utils.AppUtils.giveMeCurrentUser;

@Service
@RequiredArgsConstructor
public class OwnerSettingsServiceImpl
        implements OwnerSettingsService {

    private final UserRepository userRepository;

    private final OwnerVerificationRepository ownerVerificationRepository;


    // =========================================================
    // GET SETTINGS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public OwnerSettingsDTO getSettings() {

        UserEntity user = giveMeCurrentUser();

        OwnerVerificationEntity verification =
                ownerVerificationRepository
                        .findByUser(user)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Owner verification details not found."
                                )
                        );

        return OwnerSettingsDTO.builder()
                .businessName(
                        verification.getBusinessName()
                )
                .build();
    }


    // =========================================================
    // UPDATE BUSINESS NAME
    // =========================================================

    @Override
    @Transactional
    public OwnerSettingsDTO updateBusinessName(
            UpdateBusinessNameRequestDTO request
    ) {

        UserEntity user = giveMeCurrentUser();

        if (!user.getRoles().contains(Role.ROLE_OWNER)) {
            throw new IllegalStateException(
                    "Only owners can update business settings."
            );
        }

        OwnerVerificationEntity verification =
                ownerVerificationRepository
                        .findByUser(user)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Owner verification details not found."
                                )
                        );

        verification.setBusinessName(
                request.getBusinessName().trim()
        );

        ownerVerificationRepository.save(
                verification
        );

        return OwnerSettingsDTO.builder()
                .businessName(
                        verification.getBusinessName()
                )
                .build();
    }


    // =========================================================
    // SWITCH OWNER → GUEST
    // =========================================================

    @Override
    @Transactional
    public String switchToGuest() {

        UserEntity user = giveMeCurrentUser();

        if (!user.getRoles().contains(Role.ROLE_OWNER)) {
            throw new IllegalStateException(
                    "User is not an owner."
            );
        }

        /*
         * Remove owner role.
         */
        user.getRoles()
                .remove(Role.ROLE_OWNER);

        /*
         * Keep guest role.
         */
        if (!user.getRoles().contains(Role.ROLE_GUEST)) {
            user.getRoles()
                    .add(Role.ROLE_GUEST);
        }

        userRepository.save(user);

        return "Account switched to guest successfully.";
    }
}