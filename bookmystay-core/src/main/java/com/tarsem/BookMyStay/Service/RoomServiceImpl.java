package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.Entity.RoomTypePricingEntity;
import com.tarsem.BookMyStay.Exceptions.RoomNotFoundException;
import com.tarsem.BookMyStay.Repositroy.HotelElasticRepository;
import com.tarsem.BookMyStay.Repositroy.HotelRepository;
import com.tarsem.BookMyStay.Repositroy.RoomRepository;
import com.tarsem.BookMyStay.Repositroy.RoomTypePricingRepository;
import com.tarsem.BookMyStay.Service.Interfaces.InventoryService;
import com.tarsem.BookMyStay.Service.Interfaces.RoomService;
import com.tarsem.BookMyStay.dto.hotel.RoomDTO;
import com.tarsem.BookMyStay.dto.hotel.RoomTypeDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.tarsem.BookMyStay.Utils.AppUtils.getMinDailyPriceRoom;
import static com.tarsem.BookMyStay.Utils.AppUtils.mapToDocument;

@Service
@Slf4j
@AllArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepo;
    private final InventoryService inventoryService;
    private final ModelMapper modelMapper;
    private final HotelElasticRepository elasticRepository;
    private final AuthorizationService authorizationService;
    private final RoomTypePricingRepository roomTypePricingRepository;

    @Override
    @Transactional
    public RoomDTO addNewRoom(RoomDTO roomDTO, Long hotelId) {

        log.info("Creating new physical room in hotel: {}", hotelId);

        HotelEntity hotel =
                authorizationService.getOwnedHotel(hotelId);

        RoomTypePricingEntity pricing =
                roomTypePricingRepository
                        .findByHotelIdAndRoomType(
                                hotelId,
                                roomDTO.getRoomType()
                        )
                        .orElseThrow(() ->
                                new RoomNotFoundException(
                                        "Pricing configuration does not exist for room type: "
                                                + roomDTO.getRoomType()
                                )
                        );

        RoomEntity room =
                modelMapper.map(
                        roomDTO,
                        RoomEntity.class
                );

        room.setHotel(hotel);
        room.setRoomType(pricing.getRoomType());

        RoomEntity savedRoom =
                roomRepo.save(room);

        if (Boolean.TRUE.equals(hotel.getActive())) {
            inventoryService.initializeRoom(savedRoom);
        }

        hotel.setMinPrice(
                getMinDailyPriceRoom(hotel)
        );

        hotelRepository.save(hotel);

        elasticRepository.save(
                mapToDocument(hotel)
        );

        return modelMapper.map(
                savedRoom,
                RoomDTO.class
        );
    }

    @Override
    public List<RoomDTO> giveAllRoomsInHotel(Long hotelId) {

        log.info(
                "Getting all physical rooms in hotel: {}",
                hotelId
        );

        HotelEntity hotel =
                authorizationService.getOwnedHotel(hotelId);

        return hotel.getRooms()
                .stream()
                .map(room ->
                        modelMapper.map(
                                room,
                                RoomDTO.class
                        )
                )
                .toList();
    }

    @Override
    public RoomDTO getRoomById(
            Long hotelId,
            Long roomId
    ) {

        log.info(
                "Getting physical room: {}",
                roomId
        );

        authorizationService.getOwnedHotel(hotelId);

        RoomEntity room =
                roomRepo
                        .findByIdAndHotelId(
                                roomId,
                                hotelId
                        )
                        .orElseThrow(() ->
                                new RoomNotFoundException(
                                        "Room does not exist"
                                )
                        );

        return modelMapper.map(
                room,
                RoomDTO.class
        );
    }

    @Override
    @Transactional
    public RoomDTO updateRoomById(
            Long hotelId,
            Long roomId,
            RoomDTO roomDTO
    ) {

        log.info(
                "Updating physical room: {}",
                roomId
        );

        HotelEntity hotel =
                authorizationService.getOwnedHotel(hotelId);

        RoomEntity room =
                roomRepo
                        .findByIdAndHotelId(
                                roomId,
                                hotelId
                        )
                        .orElseThrow(() ->
                                new RoomNotFoundException(
                                        "Room does not exist"
                                )
                        );

        if (roomDTO.getRoomType() != null &&
                roomDTO.getRoomType() != room.getRoomType()) {

            roomTypePricingRepository
                    .findByHotelIdAndRoomType(
                            hotelId,
                            roomDTO.getRoomType()
                    )
                    .orElseThrow(() ->
                            new RoomNotFoundException(
                                    "Pricing configuration does not exist for room type: "
                                            + roomDTO.getRoomType()
                            )
                    );
        }

        modelMapper.map(
                roomDTO,
                room
        );

        room.setId(roomId);
        room.setHotel(hotel);

        RoomEntity updatedRoom =
                roomRepo.save(room);

        hotel.setMinPrice(
                getMinDailyPriceRoom(hotel)
        );

        hotelRepository.save(hotel);

        elasticRepository.save(
                mapToDocument(hotel)
        );

        return modelMapper.map(
                updatedRoom,
                RoomDTO.class
        );
    }

    @Override
    @Transactional
    public String deleteRoomById(
            Long hotelId,
            Long roomId
    ) {

        log.info(
                "Deleting physical room: {}",
                roomId
        );

        HotelEntity hotel =
                authorizationService.getOwnedHotel(hotelId);

        RoomEntity room =
                roomRepo
                        .findByIdAndHotelId(
                                roomId,
                                hotelId
                        )
                        .orElseThrow(() ->
                                new RoomNotFoundException(
                                        "Room does not exist"
                                )
                        );

        inventoryService.deleteAllInventories(room);

        roomRepo.delete(room);

        hotel.setMinPrice(
                getMinDailyPriceRoom(hotel)
        );

        hotelRepository.save(hotel);

        elasticRepository.save(
                mapToDocument(hotel)
        );

        return "Deleted Room with id: " + roomId;
    }

    @Override
    public List<RoomTypeDTO> getRoomTypes(Long hotelId) {

        log.info(
                "Getting room types for hotel: {}",
                hotelId
        );

        HotelEntity hotel =
                authorizationService.getOwnedHotel(hotelId);

        return hotel.getRoomTypePricingEntities()
                .stream()
                .map(pricing -> {

                    RoomTypeDTO dto =
                            new RoomTypeDTO();

                    dto.setRoomType(
                            pricing.getRoomType()
                    );

                    dto.setHourlyPrice(
                            pricing.getHourlyPrice()
                    );

                    dto.setDailyPrice(
                            pricing.getDailyPrice()
                    );

                    int totalRooms =
                            (int) hotel.getRooms()
                                    .stream()
                                    .filter(room ->
                                            room.getRoomType()
                                                    == pricing.getRoomType()
                                    )
                                    .count();

                    dto.setTotalRooms(totalRooms);

                    hotel.getRooms()
                            .stream()
                            .filter(room ->
                                    room.getRoomType()
                                            == pricing.getRoomType()
                            )
                            .findFirst()
                            .ifPresent(room ->
                                    dto.setCapacity(
                                            room.getCapacity()
                                    )
                            );

                    return dto;
                })
                .toList();
    }
}