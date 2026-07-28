package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.OwnerVerificationEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Enums.VerificationStatus;
import com.tarsem.BookMyStay.dto.OwnerVerificationResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OwnerVerificationRepository extends JpaRepository<OwnerVerificationEntity,Long> {
    boolean findByGovernmentIdNumber(String idNumber);

    Optional<UserEntity> findByUser(UserEntity user);

    List<OwnerVerificationEntity> findByVerificationStatus(VerificationStatus verificationStatus);
}
