package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Service.Interfaces.OwnerVerificationService;
import com.tarsem.BookMyStay.dto.owner.OwnerApplicationRequestDTO;
import com.tarsem.BookMyStay.dto.owner.OwnerVerificationRequestDTO;
import com.tarsem.BookMyStay.dto.owner.OwnerVerificationResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/apply")
public class OwnerVerificationController {

    private final OwnerVerificationService verificationService;

    @PostMapping(
            value = "/owner",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<String> requestOwnerVerification(
            @ModelAttribute OwnerApplicationRequestDTO requestDTO,
            @RequestPart("governmentIdFront") MultipartFile governmentIdFront,
            @RequestPart("governmentIdBack") MultipartFile governmentIdBack
    ) throws IOException {

        return ResponseEntity.ok(
                verificationService.verificationOwner(
                        requestDTO,
                        governmentIdFront,
                        governmentIdBack
                )
        );
    }

    @PostMapping(
            value = "/verification/resubmit",
            consumes = "multipart/form-data"
    )
    @ResponseStatus(HttpStatus.OK)
    public void resubmitVerification(
            @Valid
            @ModelAttribute OwnerVerificationRequestDTO request,
            @RequestPart("governmentIdFront") MultipartFile governmentIdFront,
            @RequestPart("governmentIdBack") MultipartFile governmentIdBack
    ) throws IOException {

        verificationService.resubmitVerification(
                request,
                governmentIdFront,
                governmentIdBack
        );
    }

    @GetMapping("/verification")
    public ResponseEntity<OwnerVerificationResponseDTO> getMyVerification() {
        return ResponseEntity.ok(
                verificationService.getMyVerification()
        );
    }
}
