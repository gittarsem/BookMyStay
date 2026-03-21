package com.tarsem.BookMyStay.Utils;

import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Entity.UserPrincipal;
import com.tarsem.BookMyStay.Exceptions.UnAuthorisedException;
import org.springframework.security.core.context.SecurityContextHolder;

public class AppUtils
{
    public static UserEntity giveMeCurrentUser(){
        UserPrincipal userPrincipal=(UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userPrincipal.getUser();
    }
    public static boolean verifyHotelOwner(HotelEntity hotel){
        UserEntity user=giveMeCurrentUser();
        return user.equals(hotel.getOwner());
    }
}
