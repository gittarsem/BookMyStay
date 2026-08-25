package com.tarsem.BookMyStay.dto.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResultDTO {

    private String accessToken;
    private String refreshToken;
    private UserDTO user;
}
