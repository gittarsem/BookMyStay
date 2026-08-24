package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.*;
import com.tarsem.BookMyStay.Enums.HotelAmenity;
import com.tarsem.BookMyStay.Enums.RoomType;
import com.tarsem.BookMyStay.Exceptions.HotelNotFoundException;
import com.tarsem.BookMyStay.Exceptions.UnAuthorisedException;
import com.tarsem.BookMyStay.Repositroy.HotelElasticRepository;
import com.tarsem.BookMyStay.Repositroy.HotelRepository;
import com.tarsem.BookMyStay.Repositroy.RoomTypePricingRepository;
import com.tarsem.BookMyStay.Service.Interfaces.CloudinaryService;
import com.tarsem.BookMyStay.Service.Interfaces.HotelService;
import com.tarsem.BookMyStay.Service.Interfaces.InventoryService;
import com.tarsem.BookMyStay.dto.hotel.HotelPricingDTO;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.tarsem.BookMyStay.Utils.AppUtils.*;

@Service
@Slf4j
@AllArgsConstructor
public class HotelServiceImpl implements HotelService {

     private final HotelRepository hotelRepository;
     private final RoomTypePricingRepository roomTypePricingRepository;
     private final InventoryService inventoryService;
     private final ModelMapper modelMapper;
     private final HotelElasticRepository elasticRepository;
     private final AuthorizationService authorizationService;
     private final CloudinaryService cloudinaryService;


    @Override
    @Caching(evict = {
            @CacheEvict(value = "hotel_search", allEntries = true),
            @CacheEvict(value = "user_hotels", allEntries = true)
    })
    public HotelResponseDTO createHotel(HotelRequestDTO hotelRequestDTO, List<MultipartFile> img) throws IOException {
        List<String> imageUrls = new ArrayList<>();

        if (img != null && !img.isEmpty()) {
            imageUrls = cloudinaryService.uploadImages(img);
        }
        System.out.println(hotelRequestDTO);
        HotelEntity hotel=modelMapper.map(hotelRequestDTO,HotelEntity.class);
        UserEntity user=giveMeCurrentUser();
        hotel.setOwner(user);
        hotel.setCity(hotelRequestDTO.getCity());
        hotel.setActive(false);
        hotel.setImages(imageUrls);
        hotel=hotelRepository.save(hotel);
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
    @Transactional(readOnly = true)
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
        List<String> images = new ArrayList<>(hotel.getImages());
        List<HotelAmenity> amenities = new ArrayList<>(hotel.getAmenities());
        return HotelInfoDTO.builder()
                .hotels(modelMapper.map(hotel, HotelResponseDTO.class))
                .rooms(roomsList)
                .description(hotel.getDescription())
                .images(images)
                .amenities(amenities)
                .rating(hotel.getAverageRating())
                .reviewCount(hotel.getTotalReviews())
                .minPrice(hotel.getMinPrice())
                .build();
    }

    @Override
    public List<HotelResponseDTO> getMyHotels() {
        List<HotelEntity> hotels=hotelRepository.findAllByOwner(giveMeCurrentUser());
        return hotels.stream().map(
                (hotel)->modelMapper.map(hotel,HotelResponseDTO.class)
        ).toList();
    }

    @Override
    @Transactional
    public List<HotelPricingDTO> putHotelPricing(Long hotelId, RoomType roomType, HotelPricingDTO hotelPricingDTO) {

        HotelEntity hotel=hotelRepository.findById(hotelId).orElseThrow(
                ()->new HotelNotFoundException("Hotel not found with id:"+hotelId)
        );

        RoomTypePricingEntity roomTypePricingEntity=roomTypePricingRepository.
                findByHotelIdAndRoomType(hotelId,roomType)
                .orElseGet(
                        ()->{
                            RoomTypePricingEntity entity=new RoomTypePricingEntity();
                            entity.setHotel(hotel);
                            entity.setRoomType(roomType);
                            return entity;
                        }
                );


        roomTypePricingEntity.setHourlyPrice(hotelPricingDTO.getHourlyPrice());
        roomTypePricingEntity.setDailyPrice(hotelPricingDTO.getDailyPrice());

        roomTypePricingRepository.save(roomTypePricingEntity);
        List<HotelPricingDTO> list=hotel.getRoomTypePricingEntities().stream().map(
                roomTypePricingEntity1 ->
                    modelMapper.map(roomTypePricingEntity1,HotelPricingDTO.class)

        ).toList();
        return list;
    }
}
