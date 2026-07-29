package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Exceptions.ResourceNotFoundException;
import com.tarsem.BookMyStay.Exceptions.UnAuthorisedException;
import com.tarsem.BookMyStay.Repositroy.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.tarsem.BookMyStay.Utils.AppUtils.giveMeCurrentUser;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final HotelRepository hotelRepository;

    public HotelEntity getOwnedHotel(Long hotelId) {

        HotelEntity hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Hotel not found."));

        UserEntity currentUser = giveMeCurrentUser();

        if (!hotel.getOwner().getId().equals(currentUser.getId())) {
            throw new UnAuthorisedException(
                    "You are not authorized to manage this hotel.");
        }

        return hotel;
    }

}