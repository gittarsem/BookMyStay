package com.tarsem.BookMyStay.Utils;

import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Entity.UserPrincipal;
import com.tarsem.BookMyStay.Exceptions.UnAuthorisedException;
import com.tarsem.BookMyStay.document.HotelDocument;
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

    public static HotelDocument mapToDocument(HotelEntity hotel){
        HotelDocument document=new HotelDocument();
        document.setId(hotel.getId().toString());
        document.setName(hotel.getName());
        document.setCity(hotel.getCity());
        document.setPrice(hotel.getRooms().stream()
                .map(it->it.getPrice().doubleValue())
                .min(Double::compareTo)
                .orElse(0.0)
        );
        document.setRatings(hotel.getRatings());
        return document;
    }
}
