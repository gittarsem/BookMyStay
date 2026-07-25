package com.tarsem.BookMyStay.dto;

import com.tarsem.BookMyStay.Enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private Set<Role> role;
    private LocalDateTime created_at;

}
