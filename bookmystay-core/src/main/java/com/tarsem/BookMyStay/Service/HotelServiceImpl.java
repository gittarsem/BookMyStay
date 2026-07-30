package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Exceptions.HotelNotFoundException;
import com.tarsem.BookMyStay.Exceptions.UnAuthorisedException;
import com.tarsem.BookMyStay.Repositroy.HotelElasticRepository;
import com.tarsem.BookMyStay.Repositroy.HotelRepository;
import com.tarsem.BookMyStay.Service.Interfaces.HotelService;
import com.tarsem.BookMyStay.Service.Interfaces.InventoryService;
import com.tarsem.BookMyStay.dto.hotel.HotelInfoDTO;
import com.tarsem.BookMyStay.dto.hotel.HotelRequestDTO;
import com.tarsem.BookMyStay.dto.hotel.HotelResponseDTO;
import com.tarsem.BookMyStay.dto.hotel.RoomDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.tarsem.BookMyStay.Utils.AppUtils.*;

@Service
@Slf4j
@AllArgsConstructor
public class HotelServiceImpl implements HotelService {

     private final HotelRepository hotelRepository;
     private final InventoryService inventoryService;
     private final ModelMapper modelMapper;
     private final HotelElasticRepository elasticRepository;
    private final AuthorizationService authorizationService;
    @Override
    @Caching(evict = {
            @CacheEvict(value = "hotel_search", allEntries = true),
            @CacheEvict(value = "user_hotels", allEntries = true)
    })
    public HotelResponseDTO createHotel(HotelRequestDTO hotelRequestDTO){
        HotelEntity hotel=modelMapper.map(hotelRequestDTO,HotelEntity.class);
        UserEntity user=giveMeCurrentUser();
        hotel.setOwner(user);
        hotel.setActive(false);
        hotelRepository.save(hotel);
        elasticRepository.save(mapToDocument(hotel));
        HotelResponseDTO newHotel=modelMapper.map(hotel,HotelResponseDTO.class);
        log.info("Saved hotel with id {}", newHotel.getId());
        return newHotel;
    }

    @Override
    @Cacheable(value = "hotels", key = "#hotelId")
    public HotelResponseDTO getHotel(Long hotelId) throws UnAuthorisedException {
        log.info("Getting the hotel with ID: {}",hotelId);
        HotelEntity hotel= authorizationService.getOwnedHotel(hotelId);
        return modelMapper.map(hotel,HotelResponseDTO.class);
    }



    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "hotels", key = "#id"),
            @CacheEvict(value = "hotel_search", allEntries = true)
    })
    public HotelResponseDTO updateHotelById(HotelRequestDTO hotelRequestDTO, Long hotelId) {
        log.info("Updating the hotel with ID: {}", hotelId);
        HotelEntity hotel= authorizationService.getOwnedHotel(hotelId);
        modelMapper.map(hotelRequestDTO,hotel);
        hotel.setId(hotelId);
        hotelRepository.save(hotel);
        elasticRepository.save(mapToDocument(hotel));
        return modelMapper.map(hotel,HotelResponseDTO.class);

    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "hotels", key = "#hotelId"),
            @CacheEvict(value = "hotel_search", allEntries = true),
            @CacheEvict(value = "user_hotels", allEntries = true)
    })
    public @Nullable String deleteHotelById(Long hotelId) {
        log.info("Deleting the hotel with ID: {}", hotelId);
        HotelEntity hotel= authorizationService.getOwnedHotel(hotelId);
        hotelRepository.deleteById(hotelId);
        elasticRepository.deleteById(hotelId.toString());
        return "Deleted Successfully";
    }

    @Override
    @Cacheable(
            value = "user_hotels",
            key = "T(com.tarsem.BookMyStay.Utils.AppUtils).giveMeCurrentUser().id"
    )
    public List<HotelResponseDTO> getAllHotel() {
        UserEntity user=giveMeCurrentUser();
        List<HotelEntity> hotels= hotelRepository.findByOwner(user);
        log.info("Getting all hotels for the admin user with ID: {}", user.getId());
        return hotels
                .stream()
                .map(
                        (it)->modelMapper.map(it,HotelResponseDTO.class)
                )
                .toList();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "hotel_search",key="#hotelId"),
            @CacheEvict(value = "user_hotels", allEntries = true)
    })
    public String activateHotelById(Long hotelId) {
        log.info("Activating hotel with ID: {}", hotelId);
        HotelEntity hotel= authorizationService.getOwnedHotel(hotelId);
        for(RoomEntity room: hotel.getRooms()){
            inventoryService.initializeRoom(room);
        }
        if(hotel.getActive()) return "Hotel is already active";
        hotel.setActive(true);
        //hotel.setMinPrice(getMinPriceRoom(hotel));
        hotelRepository.save(hotel);
        elasticRepository.save(mapToDocument(hotel));
        return "Hotel is now Active";
    }

    @Override
    @Cacheable(value = "hotels", key = "#hotelId")
    public HotelInfoDTO findHotelById(Long hotelId) {
        HotelEntity hotel= hotelRepository.findById(hotelId).orElseThrow(
                ()-> new HotelNotFoundException("Hotel with this id does not exist")
        );
        List<RoomDTO> roomsList=hotel.getRooms()
                .stream()
                .map(
                        (el)->modelMapper.map(el,RoomDTO.class)

                )
                .toList();
        return new HotelInfoDTO(modelMapper.map(hotel,HotelResponseDTO.class),roomsList);
    }

    @Override
    public List<HotelResponseDTO> getMyHotels() {
        List<HotelEntity> hotels=hotelRepository.findAllByOwner(giveMeCurrentUser());
        return hotels.stream().map(
                (hotel)->modelMapper.map(hotel,HotelResponseDTO.class)
        ).toList();
    }
}
