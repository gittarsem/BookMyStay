package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.Entity.OwnerVerificationEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OwnerVerificationRepository extends JpaRepository<OwnerVerificationEntity,Long> {

    Optional<OwnerVerificationEntity> findByUser(UserEntity user);

    List<OwnerVerificationEntity> findByVerificationStatus(VerificationStatus verificationStatus);

    long countByVerificationStatus(
            VerificationStatus status
    );
}
