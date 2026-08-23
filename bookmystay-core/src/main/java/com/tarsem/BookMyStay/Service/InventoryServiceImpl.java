package com.tarsem.BookMyStay.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Exceptions.RoomNotFoundException;
import com.tarsem.BookMyStay.document.HotelDocument;
import com.tarsem.BookMyStay.dto.hotel.HotelSearchResponseDTO;
import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.Repositroy.HotelMinPriceRepository;
import com.tarsem.BookMyStay.Repositroy.InventoryRepository;
import com.tarsem.BookMyStay.Repositroy.RoomRepository;
import com.tarsem.BookMyStay.Service.Interfaces.InventoryService;
import com.tarsem.BookMyStay.dto.inventory.InventoryDTO;
import com.tarsem.BookMyStay.dto.inventory.InventoryUpdateRequest;
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
import java.util.stream.Collectors;

import static com.tarsem.BookMyStay.Utils.AppUtils.verifyHotelOwner;

@Service
@Slf4j
@AllArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final RoomRepository roomRepo;
    private final ModelMapper modelMapper;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private static final int DAYS_AHEAD=30;
    private final ElasticsearchClient elasticsearch;

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

        inventoryRepository.initializeRoomInventory(
                room.getId(),
                room.getHotel().getId(),
                room.getHotel().getCity(),
                1,
                room.getPrice(),
                startDate,
                requiredEndDate
        );
    }

    @Transactional
    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduledInventoryJob() {

        List<RoomEntity> rooms = roomRepo.findAll();

        log.info("Rooms found: {}", rooms.size());

        for (RoomEntity room : rooms) {
            if (room.getHotel().getActive()) {
                initializeRoom(room);
            }

        }
    }

    @Override
    public void deleteAllInventories(RoomEntity room){
        log.info("Deleting the inventories of room with id: {}", room.getId());
        inventoryRepository.deleteByRoom(room);
    }

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
                                /*
                                 * Fetch all Elasticsearch candidates first.
                                 *
                                 * Availability filtering happens in PostgreSQL
                                 * before pagination.
                                 */
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

        /*
         * If no date/time was supplied,
         * return normal Elasticsearch search.
         */
        if (checkInDate == null) {

            return createPaginatedResponse(
                    hotels,
                    page,
                    size
            );
        }

        /*
         * Build requested time range.
         */
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

        /*
         * Hourly booking:
         *
         * 29 Aug 14:00 → 18:00
         *
         * Inventory required:
         * 29 Aug only
         *
         * Daily booking:
         *
         * 29 Aug → 31 Aug
         *
         * Inventory required:
         * 29 Aug + 30 Aug
         */
        LocalDate inventoryEndDate =
                checkInDate.equals(checkOutDate)
                        ? checkInDate.plusDays(1)
                        : checkOutDate;

        long requiredInventoryDays =
                java.time.temporal.ChronoUnit.DAYS.between(
                        checkInDate,
                        inventoryEndDate
                );

        Collection<String> activeStatuses =
                List.of(
                        BookingStatus.PAYMENT_PENDING.name(),
                        BookingStatus.BOOKED.name()
                );

        /*
         * Ask PostgreSQL which hotels have
         * at least one available room.
         */
        List<Long> availableHotelIds =
                roomRepo.findAvailableHotelIds(
                        checkInDate,
                        inventoryEndDate,
                        requiredInventoryDays,
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

    private HotelSearchResponseDTO createPaginatedResponse(
            List<HotelDocument> hotels,
            int page,
            int size
    ) {

        int start =
                page * size;

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


    @Override
    public List<InventoryDTO> getAllInventoryByRoom(Long roomId) {
        log.info("Getting All inventory by room for room with id: {}", roomId);
        RoomEntity room=roomRepo.findById(roomId).orElseThrow(
                ()-> new RoomNotFoundException("Room with id "+roomId+" does not exist")
        );

        if(!verifyHotelOwner(room.getHotel())) throw new AccessDeniedException("You are not the owner of room with id: "+roomId);
        return inventoryRepository.findByRoomOrderByDate(room)
                .stream()
                .map(
                        (element)->modelMapper.map(element,InventoryDTO.class)
                )
                .toList();

    }

    @Override
    public String updateInventory(Long roomId, InventoryUpdateRequest inventoryUpdateRequest) {
        log.info("Updating All inventory by room for room with id: {} between date range: {} - {}", roomId,
                inventoryUpdateRequest.getStartDate(),inventoryUpdateRequest.getEndDate());
        RoomEntity room=roomRepo.findById(roomId).orElseThrow(
                ()-> new RoomNotFoundException("Room with id "+roomId+" does not exist")
        );

        if(!verifyHotelOwner(room.getHotel())) throw new AccessDeniedException("You are not the owner of room with id: "+roomId);

        inventoryRepository.updateInventory(roomId,inventoryUpdateRequest.getStartDate(),
                inventoryUpdateRequest.getEndDate(),inventoryUpdateRequest.getSurgeFactor(),
                inventoryUpdateRequest.getClosed());
        return "Updated Room with id: " + roomId;
    }




}
