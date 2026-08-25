package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Entity.UserPrincipal;
import com.tarsem.BookMyStay.Security.AuthService;
import com.tarsem.BookMyStay.dto.User.LoginResultDTO;
import com.tarsem.BookMyStay.dto.User.TokenResponseDTO;
import com.tarsem.BookMyStay.dto.login.LoginRequestDTO;
import com.tarsem.BookMyStay.dto.login.LoginResponseDTO;
import com.tarsem.BookMyStay.dto.login.SignUpRequestDTO;
import com.tarsem.BookMyStay.dto.User.UserDTO;
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

        LoginResultDTO result=authService.login(loginRequestDTO);
        Cookie cookie=new Cookie("refreshToken", result.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);
        return ResponseEntity.ok(
                new LoginResponseDTO(
                        result.getAccessToken(),
                        result.getUser()
                )
        );

    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generates a new access token using a refresh token.")
    public ResponseEntity<TokenResponseDTO> refresh(
            @CookieValue(name = "refreshToken") String refreshToken) {

        String accessToken = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(new TokenResponseDTO(accessToken));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout User")
    public ResponseEntity<Void> logout(HttpServletResponse response) {

        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    public ResponseEntity<UserDTO> me() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }
}
