package com.tarsem.BookMyStay.Entity;

import com.tarsem.BookMyStay.Enums.GovernmentIdType;
import com.tarsem.BookMyStay.Enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "owner_verifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerVerificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GovernmentIdType governmentIdType;

    @Column(nullable = false)
    private String governmentIdNumber;

    @Column(nullable = false)
    private String govtIdFront;

    @Column(nullable = false)
    private String govtIdBack;

    @Column(nullable = false)
    private String businessName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String businessAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime submittedAt;

    @Column(length = 500)
    private String rejectionReason;

    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private UserEntity reviewedBy;
}