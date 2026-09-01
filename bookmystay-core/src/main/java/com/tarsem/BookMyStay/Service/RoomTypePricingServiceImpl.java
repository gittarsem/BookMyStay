package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.RoomTypePricingEntity;
import com.tarsem.BookMyStay.Exceptions.RoomNotFoundException;
import com.tarsem.BookMyStay.Repositroy.RoomTypePricingRepository;
import com.tarsem.BookMyStay.Service.Interfaces.RoomTypePricingService;
import com.tarsem.BookMyStay.dto.hotel.RoomTypeDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class RoomTypePricingServiceImpl
        implements RoomTypePricingService {

    private final RoomTypePricingRepository pricingRepository;
    private final AuthorizationService authorizationService;
    private final ModelMapper modelMapper;

    @Override
    public List<RoomTypeDTO> getAllPricing(Long hotelId) {

        log.info(
                "Getting room type pricing for hotel: {}",
                hotelId
        );

        HotelEntity hotel =
                authorizationService.getOwnedHotel(hotelId);

        return hotel.getRoomTypePricingEntities()
                .stream()
                .map(pricing ->
                        modelMapper.map(
                                pricing,
                                RoomTypeDTO.class
                        )
                )
                .toList();
    }

    @Override
    public RoomTypeDTO getPricing(
            Long hotelId,
            Long pricingId
    ) {

        log.info(
                "Getting room type pricing: {}",
                pricingId
        );

        authorizationService.getOwnedHotel(hotelId);

        RoomTypePricingEntity pricing =
                pricingRepository
                        .findByIdAndHotelId(
                                pricingId,
                                hotelId
                        )
                        .orElseThrow(() ->
                                new RoomNotFoundException(
                                        "Room type pricing does not exist"
                                )
                        );

        return modelMapper.map(
                pricing,
                RoomTypeDTO.class
        );
    }

    @Override
    @Transactional
    public RoomTypeDTO createPricing(
            Long hotelId,
            RoomTypeDTO roomTypeDTO
    ) {

        log.info(
                "Creating pricing for room type {} in hotel {}",
                roomTypeDTO.getRoomType(),
                hotelId
        );

        HotelEntity hotel =
                authorizationService.getOwnedHotel(hotelId);

        if (pricingRepository
                .findByHotelIdAndRoomType(
                        hotelId,
                        roomTypeDTO.getRoomType()
                )
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Pricing already exists for room type: "
                            + roomTypeDTO.getRoomType()
            );
        }

        RoomTypePricingEntity pricing =
                new RoomTypePricingEntity();

        pricing.setHotel(hotel);
        pricing.setRoomType(
                roomTypeDTO.getRoomType()
        );
        pricing.setHourlyPrice(
                roomTypeDTO.getHourlyPrice()
        );
        pricing.setDailyPrice(
                roomTypeDTO.getDailyPrice()
        );

        RoomTypePricingEntity saved =
                pricingRepository.save(pricing);

        return modelMapper.map(
                saved,
                RoomTypeDTO.class
        );
    }

    @Override
    @Transactional
    public RoomTypeDTO updatePricing(
            Long hotelId,
            Long pricingId,
            RoomTypeDTO roomTypeDTO
    ) {

        log.info(
                "Updating room type pricing: {}",
                pricingId
        );

        authorizationService.getOwnedHotel(hotelId);

        RoomTypePricingEntity pricing =
                pricingRepository
                        .findByIdAndHotelId(
                                pricingId,
                                hotelId
                        )
                        .orElseThrow(() ->
                                new RoomNotFoundException(
                                        "Room type pricing does not exist"
                                )
                        );

        if (roomTypeDTO.getHourlyPrice() != null) {
            pricing.setHourlyPrice(
                    roomTypeDTO.getHourlyPrice()
            );
        }

        if (roomTypeDTO.getDailyPrice() != null) {
            pricing.setDailyPrice(
                    roomTypeDTO.getDailyPrice()
            );
        }

        RoomTypePricingEntity updated =
                pricingRepository.save(pricing);

        return modelMapper.map(
                updated,
                RoomTypeDTO.class
        );
    }

    @Override
    @Transactional
    public String deletePricing(
            Long hotelId,
            Long pricingId
    ) {

        log.info(
                "Deleting room type pricing: {}",
                pricingId
        );

        authorizationService.getOwnedHotel(hotelId);

        RoomTypePricingEntity pricing =
                pricingRepository
                        .findByIdAndHotelId(
                                pricingId,
                                hotelId
                        )
                        .orElseThrow(() ->
                                new RoomNotFoundException(
                                        "Room type pricing does not exist"
                                )
                        );

        pricingRepository.delete(pricing);

        return "Deleted room type pricing with id: "
                + pricingId;
    }
}