package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Service.Interfaces.OwnerVerificationService;
import com.tarsem.BookMyStay.dto.owner.OwnerApplicationRequestDTO;
import com.tarsem.BookMyStay.dto.owner.OwnerVerificationRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;

@RestController
@RequiredArgsConstructor
public class OwnerVerificationController {

    private final OwnerVerificationService verificationService;
    @PostMapping("/owner/apply")
    public ResponseEntity<String> requestOwnerVerification(@RequestBody OwnerApplicationRequestDTO requestDTO) throws AccessDeniedException {
        return ResponseEntity.ok(verificationService.verificationOwner(requestDTO));
    }

    @PostMapping("/verification/resubmit")
    @ResponseStatus(HttpStatus.OK)
    public void resubmitVerification(
            @Valid
            @RequestBody OwnerVerificationRequestDTO request
    ) {
        verificationService.resubmitVerification(request);
    }
}
