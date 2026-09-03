package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Service.Interfaces.OwnerSettingsService;
import com.tarsem.BookMyStay.dto.owner.OwnerSettingsDTO;
import com.tarsem.BookMyStay.dto.owner.UpdateBusinessNameRequestDTO;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/owner/settings")
@RequiredArgsConstructor
public class OwnerSettingsController {

    private final OwnerSettingsService ownerSettingsService;


    // =========================================================
    // GET SETTINGS
    // =========================================================

    @GetMapping
    public ResponseEntity<OwnerSettingsDTO> getSettings() {

        return ResponseEntity.ok(
                ownerSettingsService.getSettings()
        );
    }


    // =========================================================
    // UPDATE BUSINESS NAME
    // =========================================================

    @PatchMapping("/business-name")
    public ResponseEntity<OwnerSettingsDTO> updateBusinessName(
            @Valid
            @RequestBody
            UpdateBusinessNameRequestDTO request
    ) {

        return ResponseEntity.ok(
                ownerSettingsService.updateBusinessName(
                        request
                )
        );
    }


    // =========================================================
    // SWITCH OWNER → GUEST
    // =========================================================

    @PatchMapping("/switch-to-guest")
    public ResponseEntity<String> switchToGuest() {

        return ResponseEntity.ok(
                ownerSettingsService.switchToGuest()
        );
    }
}