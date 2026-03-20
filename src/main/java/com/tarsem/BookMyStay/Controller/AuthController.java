package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Security.AuthService;
import com.tarsem.BookMyStay.dto.LoginRequestDTO;
import com.tarsem.BookMyStay.dto.LoginResponseDTO;
import com.tarsem.BookMyStay.dto.SignUpRequestDTO;
import com.tarsem.BookMyStay.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@Tag(name = "User Authentication", description = "Authentication Operation related to User")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "Sign up for new User", description = "Create a new User Account")
    public ResponseEntity<UserDTO> signUp(@RequestBody SignUpRequestDTO signUpRequestDTO){
        return new ResponseEntity<>(authService.signup(signUpRequestDTO), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns an JWT access token.")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO, HttpServletResponse response){
        String[] token=authService.login(loginRequestDTO);

        Cookie cookie=new Cookie("refreshToken", token[1]);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return ResponseEntity.ok(new LoginResponseDTO(token[0]));

    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generates a new access token using a refresh token.")
    public ResponseEntity<LoginResponseDTO> refresh(
            @CookieValue(name = "refreshToken") String refreshToken) {

        String accessToken = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(new LoginResponseDTO(accessToken));
    }
}
