package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Entity.UserPrincipal;
import com.tarsem.BookMyStay.Exceptions.ResourceNotFoundException;
import com.tarsem.BookMyStay.Exceptions.UnAuthorisedException;
import com.tarsem.BookMyStay.Repositroy.HotelRepo;
import com.tarsem.BookMyStay.Service.Interfaces.HotelService;
import com.tarsem.BookMyStay.dto.HotelRequestDTO;
import com.tarsem.BookMyStay.dto.HotelResponseDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.tarsem.BookMyStay.Utils.AppUtils.giveMeCurrentUser;

@Service
@Slf4j
@AllArgsConstructor
public class HotelServiceImpl implements HotelService {
    @Autowired
    private HotelRepo hotelRepo;

    private final ModelMapper modelMapper;



    @Override
    public HotelResponseDTO createHotel(HotelRequestDTO hotelRequestDTO){
        HotelEntity hotel=modelMapper.map(hotelRequestDTO,HotelEntity.class);
        UserEntity user=giveMeCurrentUser();
        hotel.setOwner(user);
        hotel.setActive(false);
        hotelRepo.save(hotel);
        HotelResponseDTO newHotel=modelMapper.map(hotel,HotelResponseDTO.class);
        log.info("Saved hotel with id {}", newHotel.getId());
        return newHotel;
    }

    @Override
    public HotelResponseDTO getHotel(Long hotelId) {
        log.info("Getting the hotel with ID: {}",hotelId);
        HotelEntity hotel=hotelRepo.findById(hotelId).orElseThrow(
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
        HotelEntity hotel=hotelRepo.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Hotel does not exist")
        );
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+id);
        }
        modelMapper.map(hotelRequestDTO,hotel);
        hotel.setId(id);
        hotelRepo.save(hotel);
        return modelMapper.map(hotel,HotelResponseDTO.class);

    }

    @Override
    @Transactional
    public @Nullable String deleteHotelById(Long hotelId) {
        log.info("Deleting the hotel with ID: {}", hotelId);

        UserEntity user=giveMeCurrentUser();
        HotelEntity hotel=hotelRepo.findById(hotelId).orElseThrow(
                        ()-> new ResourceNotFoundException("Hotel does not exist")
                );
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }
        hotelRepo.deleteById(hotelId);
        return "Deleted Successfully";
    }

    @Override
    public List<HotelResponseDTO> getAllHotel() {
        UserEntity user=giveMeCurrentUser();
        List<HotelEntity> hotels=hotelRepo.findByOwner(user);
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
        HotelEntity hotel=hotelRepo.findById(hotelId).orElseThrow(
                ()->new ResourceNotFoundException("Hotel does not exist")
        );
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }
        hotel.setActive(true);
        return "Hotel is now Active";
    }
}
