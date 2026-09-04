package com.tarsem.BookMyStay.Utils;

import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.RoomTypePricingEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Entity.UserPrincipal;
import com.tarsem.BookMyStay.document.HotelDocument;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;

public class AppUtils {

    public static UserEntity giveMeCurrentUser() {
        UserPrincipal userPrincipal =
                (UserPrincipal) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        return userPrincipal.getUser();
    }

    public static boolean verifyHotelOwner(HotelEntity hotel) {
        UserEntity user = giveMeCurrentUser();

        return user != null
                && hotel != null
                && hotel.getOwner() != null
                && user.getId().equals(hotel.getOwner().getId());
    }

    public static HotelDocument mapToDocument(HotelEntity hotel) {
        HotelDocument document = new HotelDocument();

        document.setId(hotel.getId().toString());
        document.setName(hotel.getName());
        document.setCity(hotel.getCity());

        document.setPrice(
                getMinDailyPriceRoom(hotel)
        );

        document.setThumbnail(
                hotel.getImages() != null && !hotel.getImages().isEmpty()
                        ? hotel.getImages().getFirst()
                        : null
        );

        document.setRatings(
                hotel.getAverageRating() != null
                        ? hotel.getAverageRating()
                        : 0.0
        );

        document.setReviewCount(
                hotel.getTotalReviews() != null
                        ? hotel.getTotalReviews()
                        : 0
        );

        document.setActive(hotel.getActive());

        return document;
    }

    public static Double getMinDailyPriceRoom(HotelEntity hotel) {

        List<RoomTypePricingEntity> pricingList =
                hotel.getRoomTypePricingEntities();

        if (pricingList == null || pricingList.isEmpty()) {
            return 0.0;
        }

        return pricingList.stream()
                .map(RoomTypePricingEntity::getDailyPrice)
                .filter(dailyPrice -> dailyPrice != null)
                .min(BigDecimal::compareTo)
                .map(BigDecimal::doubleValue)
                .orElse(0.0);
    }
}