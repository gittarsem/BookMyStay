package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Service.Interfaces.OwnerVerificationService;
import com.tarsem.BookMyStay.dto.OwnerApplicationRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
}
