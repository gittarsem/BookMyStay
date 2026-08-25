package com.tarsem.BookMyStay.Security;

import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Entity.UserPrincipal;
import com.tarsem.BookMyStay.Enums.Role;
import com.tarsem.BookMyStay.Exceptions.ResourceNotFoundException;
import com.tarsem.BookMyStay.Repositroy.UserRepository;
import com.tarsem.BookMyStay.dto.User.LoginResultDTO;
import com.tarsem.BookMyStay.dto.login.LoginRequestDTO;
import com.tarsem.BookMyStay.dto.login.SignUpRequestDTO;
import com.tarsem.BookMyStay.dto.User.UserDTO;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.tarsem.BookMyStay.Utils.AppUtils.giveMeCurrentUser;

@Service
@AllArgsConstructor
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    private final ModelMapper modelMapper;
    private PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    public UserDTO signup(SignUpRequestDTO signUpRequestDTO) {
        UserEntity user= userRepository.findByEmail(signUpRequestDTO.getEmail()).orElse(null);
        if(user!=null){
            throw new RuntimeException("User with same email Id exist");
        }
        UserEntity newUser=modelMapper.map(signUpRequestDTO,UserEntity.class);
        newUser.setRoles(Set.of(Role.ROLE_GUEST));
        newUser.setPassword(passwordEncoder.encode(signUpRequestDTO.getPassword()));
        userRepository.save(newUser);

        return modelMapper.map(newUser,UserDTO.class);
    }

    public LoginResultDTO login(LoginRequestDTO loginRequestDTO){
        Authentication authentication=authenticationManager.authenticate(
               new UsernamePasswordAuthenticationToken( loginRequestDTO.getEmail(),
                       loginRequestDTO.getPassword())
        );
        UserPrincipal userPrincipal= (UserPrincipal) authentication.getPrincipal();
        UserDTO user=modelMapper.map(userPrincipal.getUser(),UserDTO.class);
        return new LoginResultDTO(
                jwtService.generateAccessToken(user.getEmail(),user.getRoles()),
                jwtService.generateRefreshToken(user.getEmail()),
                user
        );
    }

    public String refreshToken(String refreshToken) {
        String email=jwtService.getUserEmailFromToken(refreshToken);
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with id: "+email));
        return jwtService.generateAccessToken(user.getEmail(),user.getRoles());
    }

    public UserDTO getCurrentUser() {
        UserEntity user = giveMeCurrentUser();

        return modelMapper.map(user,UserDTO.class);
    }
}
