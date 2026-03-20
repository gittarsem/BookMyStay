package com.tarsem.BookMyStay.Utils;

import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Entity.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

public class AppUtils
{
    public static UserEntity giveMeCurrentUser(){
        UserPrincipal userPrincipal=(UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userPrincipal.getUser();
    }
}
