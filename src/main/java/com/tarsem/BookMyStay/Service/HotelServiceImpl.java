package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Exceptions.ResourceNotFoundException;
import com.tarsem.BookMyStay.Exceptions.UnAuthorisedException;
import com.tarsem.BookMyStay.Repositroy.HotelElasticRepository;
import com.tarsem.BookMyStay.Repositroy.HotelRepository;
import com.tarsem.BookMyStay.Service.Interfaces.HotelService;
import com.tarsem.BookMyStay.Service.Interfaces.InventoryService;
import com.tarsem.BookMyStay.document.HotelDocument;
import com.tarsem.BookMyStay.dto.HotelInfoDTO;
import com.tarsem.BookMyStay.dto.HotelRequestDTO;
import com.tarsem.BookMyStay.dto.HotelResponseDTO;
import com.tarsem.BookMyStay.dto.RoomDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.tarsem.BookMyStay.Utils.AppUtils.giveMeCurrentUser;
import static com.tarsem.BookMyStay.Utils.AppUtils.mapToDocument;

@Service
@Slf4j
@AllArgsConstructor
public class HotelServiceImpl implements HotelService {
    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private InventoryService inventoryService;

    private final ModelMapper modelMapper;

    @Autowired
    private HotelElasticRepository elasticRepository;

    @Override
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
    public HotelResponseDTO getHotel(Long hotelId) {
        log.info("Getting the hotel with ID: {}",hotelId);
        HotelEntity hotel= hotelRepository.findById(hotelId).orElseThrow(
                ()->new ResourceNotFoundException("Hotel with this id not exists")
        );
        UserEntity user=giveMeCurrentUser();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }

        return modelMapper.map(hotel,HotelResponseDTO.class);
    }

    @Override
    @Transactional
    public HotelResponseDTO updateHotelById(HotelRequestDTO hotelRequestDTO, Long id) {
        log.info("Updating the hotel with ID: {}", id);

        UserEntity user=giveMeCurrentUser();
        HotelEntity hotel= hotelRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Hotel does not exist")
        );
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+id);
        }
        modelMapper.map(hotelRequestDTO,hotel);
        hotel.setId(id);
        hotelRepository.save(hotel);
        elasticRepository.save(mapToDocument(hotel));
        return modelMapper.map(hotel,HotelResponseDTO.class);

    }

    @Override
    @Transactional
    public @Nullable String deleteHotelById(Long hotelId) {
        log.info("Deleting the hotel with ID: {}", hotelId);

        UserEntity user=giveMeCurrentUser();
        HotelEntity hotel= hotelRepository.findById(hotelId).orElseThrow(
                        ()-> new ResourceNotFoundException("Hotel does not exist")
                );
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }
        hotelRepository.deleteById(hotelId);
        elasticRepository.deleteById(hotelId.toString());
        return "Deleted Successfully";
    }

    @Override
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
    public String activateHotelById(Long hotelId) {
        log.info("Activating hotel with ID: {}", hotelId);
        UserEntity user=giveMeCurrentUser();
        HotelEntity hotel= hotelRepository.findById(hotelId).orElseThrow(
                ()->new ResourceNotFoundException("Hotel does not exist")
        );
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }
        for(RoomEntity room: hotel.getRooms()){
            inventoryService.initializeRoom(room);
        }
        hotel.setActive(true);
        return "Hotel is now Active";
    }

    @Override
    @Cacheable(value = "hotels", key = "#hotelId")
    public HotelInfoDTO findHotelById(Long hotelId) {
        HotelEntity hotel= hotelRepository.findById(hotelId).orElseThrow(
                ()-> new ResourceNotFoundException("Hotel with this id does not exist")
        );
        List<RoomDTO> roomsList=hotel.getRooms()
                .stream()
                .map(
                        (el)->modelMapper.map(el,RoomDTO.class)

                )
                .toList();
        return new HotelInfoDTO(modelMapper.map(hotel,HotelResponseDTO.class),roomsList);
    }
}
