package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Repositroy.UserRepository;
import com.tarsem.BookMyStay.dto.profile.ChangePasswordRequestDTO;
import com.tarsem.BookMyStay.dto.profile.DeleteAccountRequestDTO;
import com.tarsem.BookMyStay.dto.profile.ProfileDTO;
import com.tarsem.BookMyStay.dto.profile.UpdateProfileRequestDTO;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

import static com.tarsem.BookMyStay.Utils.AppUtils.giveMeCurrentUser;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl
        implements ProfileService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    // =========================================================
    // GET PROFILE
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ProfileDTO getProfile() {

        UserEntity user =
                giveMeCurrentUser();

        return mapToProfileDTO(user);
    }


    // =========================================================
    // UPDATE PROFILE
    // =========================================================

    @Override
    @Transactional
    public ProfileDTO updateProfile(
            UpdateProfileRequestDTO request
    ) {

        UserEntity user =
                giveMeCurrentUser();

        user.setName(
                request.getName().trim()
        );

        userRepository.save(user);

        return mapToProfileDTO(user);
    }


    // =========================================================
    // CHANGE PASSWORD
    // =========================================================

    @Override
    @Transactional
    public void changePassword(
            ChangePasswordRequestDTO request
    ) {

        UserEntity user =
                giveMeCurrentUser();

        /*
         * Verify current password.
         */

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "Current password is incorrect."
            );
        }


        /*
         * Verify new password confirmation.
         */

        if (!request.getNewPassword().equals(
                request.getConfirmPassword()
        )) {

            throw new IllegalArgumentException(
                    "New passwords do not match."
            );
        }


        /*
         * Don't allow the user to reuse
         * the current password.
         */

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "New password must be different from the current password."
            );
        }


        /*
         * Encode and save.
         */

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);
    }


    // =========================================================
    // DELETE ACCOUNT
    // =========================================================

    @Override
    @Transactional
    public void deleteAccount(
            DeleteAccountRequestDTO request
    ) {

        UserEntity user =
                giveMeCurrentUser();


        /*
         * Explicit confirmation.
         */

        if (!"DELETE".equals(
                request.getConfirmation()
        )) {

            throw new IllegalArgumentException(
                    "Account deletion confirmation is invalid."
            );
        }


        /*
         * Verify current password.
         */

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "Current password is incorrect."
            );
        }


        /*
         * Owner accounts need special handling.
         *
         * Do NOT blindly delete the user because
         * an owner may have hotels, rooms,
         * inventory, etc.
         */

        /*
         * For now, prevent deletion of an owner
         * account until the owner-property cleanup/
         * account closure policy is implemented.
         */

        if (user.getRoles().stream()
                .anyMatch(role ->
                        role.name().equals("ROLE_OWNER")
                )) {

            throw new IllegalStateException(
                    "Owner accounts with owner access cannot be deleted yet."
            );
        }


        userRepository.delete(user);
    }


    // =========================================================
    // MAPPER
    // =========================================================

    private ProfileDTO mapToProfileDTO(
            UserEntity user
    ) {

        return ProfileDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roles(
                        user.getRoles()
                                .stream()
                                .map(Enum::name)
                                .collect(Collectors.toList())
                )
                .created_at(user.getCreated_at())
                .build();
    }
}