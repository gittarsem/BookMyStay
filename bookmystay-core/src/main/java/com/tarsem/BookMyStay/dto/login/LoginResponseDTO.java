package com.tarsem.BookMyStay.dto.login;

import com.tarsem.BookMyStay.dto.User.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {

    private String accessToken;

    private UserDTO user;

}
