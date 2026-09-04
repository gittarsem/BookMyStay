package com.tarsem.BookMyStay.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminActivityDTO {

    private Long id;

    private Long adminId;

    private String adminName;

    private String action;

    private String targetType;

    private Long targetId;

    private String description;

    private LocalDateTime createdAt;
}