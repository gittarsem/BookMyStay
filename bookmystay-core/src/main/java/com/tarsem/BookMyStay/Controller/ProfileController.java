package com.tarsem.BookMyStay.Controller;


import com.tarsem.BookMyStay.Service.ProfileService;
import com.tarsem.BookMyStay.dto.profile.ChangePasswordRequestDTO;
import com.tarsem.BookMyStay.dto.profile.DeleteAccountRequestDTO;
import com.tarsem.BookMyStay.dto.profile.ProfileDTO;
import com.tarsem.BookMyStay.dto.profile.UpdateProfileRequestDTO;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private UpdateProfileRequestDTO request;

    // =========================================================
    // GET PROFILE
    // =========================================================

    @GetMapping
    public ResponseEntity<ProfileDTO> getProfile() {

        return ResponseEntity.ok(
                profileService.getProfile()
        );
    }


    // =========================================================
    // UPDATE PROFILE
    // =========================================================

    @PatchMapping
    public ResponseEntity<ProfileDTO> updateProfile(
            @Valid
            @RequestBody
            UpdateProfileRequestDTO request
    ) {
        this.request = request;

        return ResponseEntity.ok(
                profileService.updateProfile(request)
        );
    }


    // =========================================================
    // CHANGE PASSWORD
    // =========================================================

    @PatchMapping("/password")
    public ResponseEntity<String> changePassword(
            @Valid
            @RequestBody
            ChangePasswordRequestDTO request
    ) {

        profileService.changePassword(request);

        return ResponseEntity.ok(
                "Password changed successfully."
        );
    }


    // =========================================================
    // DELETE ACCOUNT
    // =========================================================

    @DeleteMapping
    public ResponseEntity<String> deleteAccount(
            @Valid
            @RequestBody
            DeleteAccountRequestDTO request
    ) {

        profileService.deleteAccount(request);

        return ResponseEntity.ok(
                "Account deleted successfully."
        );
    }
}