package com.tarsem.BookMyStay.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;

import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.InventoryEntity;
import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.Entity.RoomTypePricingEntity;

import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Enums.RoomType;

import com.tarsem.BookMyStay.Exceptions.RoomNotFoundException;

import com.tarsem.BookMyStay.Repositroy.HotelMinPriceRepository;
import com.tarsem.BookMyStay.Repositroy.HotelRepository;
import com.tarsem.BookMyStay.Repositroy.InventoryRepository;
import com.tarsem.BookMyStay.Repositroy.RoomRepository;
import com.tarsem.BookMyStay.Repositroy.RoomTypePricingRepository;

import com.tarsem.BookMyStay.Service.Interfaces.InventoryService;

import com.tarsem.BookMyStay.document.HotelDocument;

import com.tarsem.BookMyStay.dto.hotel.HotelSearchResponseDTO;
import com.tarsem.BookMyStay.dto.inventory.HotelInventoryDTO;
import com.tarsem.BookMyStay.dto.inventory.InventoryDTO;
import com.tarsem.BookMyStay.dto.inventory.InventoryUpdateRequest;
import com.tarsem.BookMyStay.dto.inventory.RoomTypeInventoryDTO;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.tarsem.BookMyStay.Utils.AppUtils.verifyHotelOwner;

@Service
@Slf4j
@AllArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final RoomRepository roomRepo;
    private final RoomTypePricingRepository roomTypePricingRepository;
    private final ModelMapper modelMapper;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final ElasticsearchClient elasticsearch;
    private final HotelRepository hotelRepository;

    private static final int DAYS_AHEAD = 30;

    // ============================================================
    // INVENTORY INITIALIZATION
    // ============================================================

    @Override
    @Transactional
    public void initializeRoom(RoomEntity room) {

        LocalDate today = LocalDate.now();
        LocalDate requiredEndDate = today.plusDays(DAYS_AHEAD);

        LocalDate lastInventoryDate =
                inventoryRepository.findLastInventoryDate(room.getId());

        LocalDate startDate;

        if (lastInventoryDate == null) {
            startDate = today;
        } else {
            startDate = lastInventoryDate.plusDays(1);
        }

        if (startDate.isAfter(requiredEndDate)) {
            return;
        }

        RoomTypePricingEntity pricing =
                roomTypePricingRepository
                        .findByHotelIdAndRoomType(
                                room.getHotel().getId(),
                                room.getRoomType()
                        )
                        .orElseThrow(() ->
                                new RoomNotFoundException(
                                        "Pricing configuration does not exist for room type: "
                                                + room.getRoomType()
                                )
                        );

        inventoryRepository.initializeRoomInventory(
                room.getId(),
                room.getHotel().getId(),
                room.getHotel().getCity(),
                1,
                pricing.getDailyPrice(),
                startDate,
                requiredEndDate
        );

        log.info(
                "Inventory initialized for room {} from {} to {}",
                room.getId(),
                startDate,
                requiredEndDate
        );
    }

    @Transactional
    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduledInventoryJob() {

        List<RoomEntity> rooms = roomRepo.findAll();

        log.info("Inventory job started. Rooms found: {}", rooms.size());

        for (RoomEntity room : rooms) {

            if (Boolean.TRUE.equals(room.getHotel().getActive())) {

                try {
                    initializeRoom(room);
                } catch (Exception exception) {

                    /*
                     * One room should not prevent inventory
                     * initialization for all other rooms.
                     */
                    log.error(
                            "Failed to initialize inventory for room {}",
                            room.getId(),
                            exception
                    );
                }
            }
        }

        log.info("Inventory job completed");
    }

    // ============================================================
    // HOTEL INVENTORY
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<HotelInventoryDTO> getHotelInventory(
            Long hotelId,
            LocalDate startDate,
            LocalDate endDate
    ) {

        log.info(
                "Getting hotel inventory for hotel {} between {} and {}",
                hotelId,
                startDate,
                endDate
        );

        HotelEntity hotel =
                hotelRepository.findById(hotelId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Hotel with id " +
                                                hotelId +
                                                " does not exist"
                                )
                        );

        if (!verifyHotelOwner(hotel)) {
            throw new AccessDeniedException(
                    "You are not the owner of hotel with id: " + hotelId
            );
        }

        /*
         * Missing dates should return an empty result.
         */
        if (startDate == null || endDate == null) {
            return List.of();
        }

        /*
         * Invalid date range should return an empty result.
         */
        if (startDate.isAfter(endDate)) {
            return List.of();
        }

        List<InventoryEntity> inventories =
                inventoryRepository
                        .findByHotelIdAndDateBetweenOrderByDate(
                                hotelId,
                                startDate,
                                endDate
                        );

        if (inventories.isEmpty()) {
            return List.of();
        }

        /*
         * Group inventory:
         *
         * Date
         *   └── Room Type
         *          ├── total rooms
         *          ├── booked rooms
         *          ├── reserved rooms
         *          ├── available rooms
         *          ├── closed
         *          ├── price
         *          └── surge factor
         */
        return inventories
                .stream()
                .collect(
                        Collectors.groupingBy(
                                InventoryEntity::getDate,
                                TreeMap::new,
                                Collectors.groupingBy(
                                        inventory ->
                                                inventory
                                                        .getRoom()
                                                        .getRoomType()
                                )
                        )
                )
                .entrySet()
                .stream()
                .map(dateEntry -> {

                    LocalDate date = dateEntry.getKey();

                    List<RoomTypeInventoryDTO> roomTypes =
                            dateEntry.getValue()
                                    .entrySet()
                                    .stream()
                                    .map(roomTypeEntry -> {

                                        RoomType roomType =
                                                roomTypeEntry.getKey();

                                        List<InventoryEntity> rows =
                                                roomTypeEntry.getValue();

                                        int totalRooms =
                                                rows.stream()
                                                        .mapToInt(
                                                                inventory ->
                                                                        inventory.getTotalCount() == null
                                                                                ? 0
                                                                                : inventory.getTotalCount()
                                                        )
                                                        .sum();

                                        int bookedRooms =
                                                rows.stream()
                                                        .mapToInt(
                                                                inventory ->
                                                                        inventory.getBookCount() == null
                                                                                ? 0
                                                                                : inventory.getBookCount()
                                                        )
                                                        .sum();

                                        int reservedRooms =
                                                rows.stream()
                                                        .mapToInt(
                                                                inventory ->
                                                                        inventory.getReservedCount() == null
                                                                                ? 0
                                                                                : inventory.getReservedCount()
                                                        )
                                                        .sum();

                                        int availableRooms =
                                                Math.max(
                                                        0,
                                                        totalRooms
                                                                - bookedRooms
                                                                - reservedRooms
                                                );

                                        /*
                                         * If any physical room of this
                                         * room type is closed for the date,
                                         * mark the room type as closed.
                                         */
                                        boolean closed =
                                                rows.stream()
                                                        .anyMatch(
                                                                inventory ->
                                                                        Boolean.TRUE.equals(
                                                                                inventory.getClosed()
                                                                        )
                                                        );

                                        /*
                                         * Pricing is configured at the
                                         * room-type level, so the first
                                         * inventory row is sufficient for
                                         * displaying the price/surge.
                                         */
                                        InventoryEntity first =
                                                rows.get(0);

                                        return new RoomTypeInventoryDTO(
                                                roomType,
                                                totalRooms,
                                                bookedRooms,
                                                reservedRooms,
                                                availableRooms,
                                                closed,
                                                first.getPrice(),
                                                first.getSurgeFactor()
                                        );
                                    })
                                    .toList();

                    return new HotelInventoryDTO(
                            date,
                            roomTypes
                    );
                })
                .toList();
    }

    // ============================================================
    // DELETE INVENTORY
    // ============================================================

    @Override
    @Transactional
    public void deleteAllInventories(RoomEntity room) {

        log.info(
                "Deleting inventories of room {}",
                room.getId()
        );

        inventoryRepository.deleteByRoom(room);
    }

    // ============================================================
    // HOTEL SEARCH
    // ============================================================

    @Override
    @Cacheable(
            value = "hotel_search",
            key = "#keyword + '-' + #city + '-' + #minPrice + '-' + #maxPrice + '-' + #ratings + '-' + #checkInDate + '-' + #checkInTime + '-' + #checkOutDate + '-' + #checkOutTime + '-' + #sortField + '-' + #sortOrder + '-' + #page + '-' + #size"
    )
    public HotelSearchResponseDTO searchHotels(
            String keyword,
            String city,
            Double minPrice,
            Double maxPrice,
            Double ratings,
            LocalDate checkInDate,
            LocalTime checkInTime,
            LocalDate checkOutDate,
            LocalTime checkOutTime,
            String sortField,
            String sortOrder,
            int page,
            int size
    ) throws IOException {

        validateSearchDates(
                checkInDate,
                checkInTime,
                checkOutDate,
                checkOutTime
        );

        BoolQuery.Builder builder =
                new BoolQuery.Builder();

        if (keyword != null && !keyword.isEmpty()) {

            builder.must(
                    b -> b.match(
                            mm -> mm
                                    .field("name")
                                    .query(keyword)
                    )
            );
        }

        builder.filter(
                f -> f.term(
                        t -> t
                                .field("active")
                                .value(true)
                )
        );

        if (city != null && !city.isEmpty()) {

            builder.filter(
                    b -> b.term(
                            m -> m
                                    .value(city.toLowerCase())
                                    .field("city")
                    )
            );
        }

        if (minPrice != null || maxPrice != null) {

            builder.filter(
                    f -> f.range(
                            r -> r.number(
                                    n -> {

                                        n.field("price");

                                        if (minPrice != null) {
                                            n.gte(minPrice);
                                        }

                                        if (maxPrice != null) {
                                            n.lte(maxPrice);
                                        }

                                        return n;
                                    }
                            )
                    )
            );
        }

        if (ratings != null) {

            builder.filter(
                    f -> f.range(
                            r -> r.number(
                                    n -> n
                                            .field("ratings")
                                            .gte(ratings)
                            )
                    )
            );
        }

        SearchResponse<HotelDocument> response =
                elasticsearch.search(
                        s -> s
                                .index("hotels")
                                .query(
                                        q -> q.bool(
                                                builder.build()
                                        )
                                )
                                .from(0)
                                .size(10000)
                                .sort(
                                        so -> so.field(
                                                f -> f
                                                        .field(
                                                                sortField.equals("name")
                                                                        ? "name.keyword"
                                                                        : sortField
                                                        )
                                                        .order(
                                                                sortOrder.equalsIgnoreCase("asc")
                                                                        ? SortOrder.Asc
                                                                        : SortOrder.Desc
                                                        )
                                        )
                                ),
                        HotelDocument.class
                );

        List<HotelDocument> hotels =
                response.hits()
                        .hits()
                        .stream()
                        .map(hit -> hit.source())
                        .filter(java.util.Objects::nonNull)
                        .toList();

        if (checkInDate == null) {

            return createPaginatedResponse(
                    hotels,
                    page,
                    size
            );
        }

        LocalDateTime checkIn =
                LocalDateTime.of(
                        checkInDate,
                        checkInTime
                );

        LocalDateTime checkOut =
                LocalDateTime.of(
                        checkOutDate,
                        checkOutTime
                );

        LocalDate inventoryEndDate =
                checkInDate.equals(checkOutDate)
                        ? checkInDate.plusDays(1)
                        : checkOutDate;

        Collection<String> activeStatuses =
                List.of(
                        BookingStatus.PAYMENT_PENDING.name(),
                        BookingStatus.BOOKED.name()
                );

        List<Long> availableHotelIds =
                roomRepo.findAvailableHotelIds(
                        checkIn,
                        checkOut,
                        activeStatuses
                );

        Set<String> availableHotels =
                availableHotelIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.toSet());

        hotels = hotels.stream()
                .filter(
                        hotel ->
                                availableHotels.contains(
                                        hotel.getId()
                                )
                )
                .toList();

        return createPaginatedResponse(
                hotels,
                page,
                size
        );
    }

    // ============================================================
    // SEARCH DATE VALIDATION
    // ============================================================

    private void validateSearchDates(
            LocalDate checkInDate,
            LocalTime checkInTime,
            LocalDate checkOutDate,
            LocalTime checkOutTime
    ) {

        boolean anyProvided =
                checkInDate != null
                        || checkInTime != null
                        || checkOutDate != null
                        || checkOutTime != null;

        boolean allProvided =
                checkInDate != null
                        && checkInTime != null
                        && checkOutDate != null
                        && checkOutTime != null;

        if (anyProvided && !allProvided) {

            throw new IllegalArgumentException(
                    "Check-in date, check-in time, check-out date " +
                            "and check-out time must all be provided."
            );
        }

        if (!allProvided) {
            return;
        }

        LocalDateTime checkIn =
                LocalDateTime.of(
                        checkInDate,
                        checkInTime
                );

        LocalDateTime checkOut =
                LocalDateTime.of(
                        checkOutDate,
                        checkOutTime
                );

        if (!checkIn.isBefore(checkOut)) {

            throw new IllegalArgumentException(
                    "Check-out must be after check-in."
            );
        }
    }

    // ============================================================
    // PAGINATION
    // ============================================================

    private HotelSearchResponseDTO createPaginatedResponse(
            List<HotelDocument> hotels,
            int page,
            int size
    ) {

        int start = page * size;

        if (start >= hotels.size()) {

            return new HotelSearchResponseDTO(
                    List.of(),
                    hotels.size(),
                    page,
                    size
            );
        }

        int end =
                Math.min(
                        start + size,
                        hotels.size()
                );

        List<HotelDocument> pageContent =
                hotels.subList(
                        start,
                        end
                );

        return new HotelSearchResponseDTO(
                pageContent,
                hotels.size(),
                page,
                size
        );
    }

    // ============================================================
    // ROOM INVENTORY
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> getAllInventoryByRoom(Long roomId) {

        log.info(
                "Getting inventory for room {}",
                roomId
        );

        RoomEntity room =
                roomRepo.findById(roomId)
                        .orElseThrow(() ->
                                new RoomNotFoundException(
                                        "Room with id " +
                                                roomId +
                                                " does not exist"
                                )
                        );

        if (!verifyHotelOwner(room.getHotel())) {

            throw new AccessDeniedException(
                    "You are not the owner of room with id: " +
                            roomId
            );
        }

        return inventoryRepository
                .findByRoomOrderByDate(room)
                .stream()
                .map(element ->
                        modelMapper.map(
                                element,
                                InventoryDTO.class
                        )
                )
                .toList();
    }

    // ============================================================
    // UPDATE INVENTORY
    // ============================================================

    @Override
    @Transactional
    public String updateInventory(
            Long roomId,
            InventoryUpdateRequest inventoryUpdateRequest
    ) {

        log.info(
                "Updating inventory for room {} between {} and {}",
                roomId,
                inventoryUpdateRequest.getStartDate(),
                inventoryUpdateRequest.getEndDate()
        );

        RoomEntity room =
                roomRepo.findById(roomId)
                        .orElseThrow(() ->
                                new RoomNotFoundException(
                                        "Room with id " +
                                                roomId +
                                                " does not exist"
                                )
                        );

        if (!verifyHotelOwner(room.getHotel())) {

            throw new AccessDeniedException(
                    "You are not the owner of room with id: " +
                            roomId
            );
        }

        inventoryRepository.updateInventory(
                roomId,
                inventoryUpdateRequest.getStartDate(),
                inventoryUpdateRequest.getEndDate(),
                inventoryUpdateRequest.getSurgeFactor(),
                inventoryUpdateRequest.getClosed()
        );

        return "Updated Room with id: " + roomId;
    }
}