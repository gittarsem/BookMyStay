package com.tarsem.BookMyStay.Security;

import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Entity.UserPrincipal;
import com.tarsem.BookMyStay.Enums.Role;
import com.tarsem.BookMyStay.Exceptions.ResourceNotFoundException;
import com.tarsem.BookMyStay.Repositroy.UserRepo;
import com.tarsem.BookMyStay.dto.LoginRequestDTO;
import com.tarsem.BookMyStay.dto.SignUpRequestDTO;
import com.tarsem.BookMyStay.dto.UserDTO;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@AllArgsConstructor
public class AuthService {
    @Autowired
    private UserRepo userRepo;

    private final ModelMapper modelMapper;
    private PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    public UserDTO signup(SignUpRequestDTO signUpRequestDTO) {
        UserEntity user=userRepo.findByEmail(signUpRequestDTO.getEmail()).orElse(null);
        if(user!=null){
            throw new RuntimeException("User with same email Id exist");
        }
        UserEntity newUser=modelMapper.map(signUpRequestDTO,UserEntity.class);
        newUser.setRole(Set.of(Role.GUEST));
        newUser.setPassword(passwordEncoder.encode(signUpRequestDTO.getPassword()));
        userRepo.save(newUser);

        return modelMapper.map(newUser,UserDTO.class);
    }

    public String[] login(LoginRequestDTO loginRequestDTO){
        Authentication authentication=authenticationManager.authenticate(
               new UsernamePasswordAuthenticationToken( loginRequestDTO.getEmail(),
                       loginRequestDTO.getPassword())
        );
        UserPrincipal userPrincipal= (UserPrincipal) authentication.getPrincipal();
        UserEntity user=userPrincipal.getUser();
        String[] token =new String[2];
        token[0]= jwtService.generateAccessToken(user.getEmail());
        token[1]= jwtService.generateRefreshToken(user.getEmail());
        return token;
    }

    public String refreshToken(String refreshToken) {
        String email=jwtService.getUserEmailFromToken(refreshToken);
        UserEntity user = userRepo.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with id: "+email));
        return jwtService.generateAccessToken(user.getEmail());
    }
}
