package com.tarsem.BookMyStay.Utils;

import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Entity.UserPrincipal;
import com.tarsem.BookMyStay.document.HotelDocument;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;

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
        document.setPrice(getMinPriceRoom(hotel));

        document.setRatings(
                hotel.getAverageRating()!= null ? hotel.getAverageRating(): 0.0
        );

        document.setActive(hotel.getActive());
        return document;
    }

    public static Double getMinPriceRoom(HotelEntity hotel){
        List<RoomEntity> rooms=hotel.getRooms();
        if (rooms == null || rooms.isEmpty()) return 0.0;
        return rooms.stream()
                .map(it->it.getPrice().doubleValue())
                .min(Double::compareTo)
                .orElse(0.0);


    }
}
