package com.tarsem.BookMyStay.Security;



import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Entity.UserPrincipal;
import com.tarsem.BookMyStay.Repositroy.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user =userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + email));

        if(user==null){
            System.out.println("Login attempt for: " + email);
            throw new UsernameNotFoundException("User 404");
        }

        return new UserPrincipal(user);
    }
}
