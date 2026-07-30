package com.tarsem.BookMyStay.Controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.tarsem.BookMyStay.Enums.VerificationStatus;
import com.tarsem.BookMyStay.Repositroy.HotelElasticRepository;
import com.tarsem.BookMyStay.Repositroy.HotelRepository;
import com.tarsem.BookMyStay.Repositroy.UserRepository;
import com.tarsem.BookMyStay.Service.Interfaces.AdminService;
import com.tarsem.BookMyStay.Utils.AppUtils;
import com.tarsem.BookMyStay.document.HotelDocument;
import com.tarsem.BookMyStay.dto.owner.OwnerVerificationResponseDTO;
import com.tarsem.BookMyStay.dto.owner.RejectionRequestDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private HotelElasticRepository elasticRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminService adminService;

    @PatchMapping("/users/{userId}/roles")
    public ResponseEntity<String> changeRole(@PathVariable Long userId){
            return ResponseEntity.ok(adminService.approveOwner(userId));
    }

    @GetMapping("/owner-verifications/pending")
    public ResponseEntity<List<OwnerVerificationResponseDTO>> findPendingApplications(){
        return ResponseEntity.ok(adminService.getPendingApplication(VerificationStatus.PENDING));
    }

    @PutMapping("/owner-verifications/{verificationId}/approve")
    public ResponseEntity<String> approveApplication(
            @PathVariable Long verificationId) {

        adminService.approveApplication(verificationId);
        return ResponseEntity.ok("Owner application approved successfully.");
    }

    @PutMapping("/owner-verifications/{verificationId}/reject")
    public ResponseEntity<String> rejectApplication(
            @PathVariable Long verificationId,
            @Valid @RequestBody RejectionRequestDTO request) {

        adminService.rejectApplication(verificationId, request);

        return ResponseEntity.ok("Owner application rejected successfully.");
    }

    @GetMapping("/reindex")
    public String reindex() {

        elasticRepository.deleteAll();

        List<HotelDocument> docs = hotelRepository.findAll()
                .stream()
                .map(AppUtils::mapToDocument)
                .toList();

        elasticRepository.saveAll(docs);

        return "Reindex completed: " + docs.size();
    }

    @GetMapping("/es-test")
    public String test() throws Exception {
        return elasticsearchClient.info().clusterName();
    }
}
