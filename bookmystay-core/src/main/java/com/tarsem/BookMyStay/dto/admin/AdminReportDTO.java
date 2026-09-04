package com.tarsem.BookMyStay.dto.admin;

import com.tarsem.BookMyStay.Enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminReportDTO {

    private Long id;

    private Long reporterId;

    private String reporterName;

    private String targetType;

    private Long targetId;

    private String reason;

    private ReportStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    private Long resolvedById;

    private String resolvedByName;
}