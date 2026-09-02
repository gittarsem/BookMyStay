package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Service.Interfaces.OwnerDashboardService;
import com.tarsem.BookMyStay.dto.owner.OwnerDashboardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/owner/dashboard")
public class OwnerDashboardController {

    private final OwnerDashboardService dashboardService;

    @GetMapping
    public ResponseEntity<OwnerDashboardDTO> getDashboard() {

        return ResponseEntity.ok(
                dashboardService.getDashboard()
        );
    }
}