package com.tarsem.BookMyStay.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileDTO {

    private Long id;

    private String name;

    private String email;

    private List<String> roles;

    private LocalDateTime created_at;
}
