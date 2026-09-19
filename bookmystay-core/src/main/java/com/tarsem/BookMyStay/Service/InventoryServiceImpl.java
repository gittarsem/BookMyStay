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
    private final ElasticsearchAvailabilityService elasticsearchAvailabilityService;

    private static final int DAYS_AHEAD = 30;

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

        if (startDate == null || endDate == null) {
            return List.of();
        }

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

                                        boolean closed =
                                                rows.stream()
                                                        .anyMatch(
                                                                inventory ->
                                                                        Boolean.TRUE.equals(
                                                                                inventory.getClosed()
                                                                        )
                                                        );

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

    @Override
    @Transactional
    public void deleteAllInventories(RoomEntity room) {
        inventoryRepository.deleteByRoom(room);
    }

    @Override
    @Transactional(readOnly = true)
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

        String normalizedSortField =
                normalizeSortField(sortField);

        String normalizedSortOrder =
                normalizeSortOrder(sortOrder);

        String normalizedKeyword =
                normalizeNullable(keyword);

        String normalizedCity =
                normalizeNullable(city);

        if (normalizedKeyword == null) {
            normalizedKeyword = normalizedCity;
            normalizedCity = null;
        }

        LocalTime effectiveCheckInTime =
                resolveCheckInTime(checkInTime);

        LocalTime effectiveCheckOutTime =
                resolveCheckOutTime(checkOutTime);

        Boolean elasticsearchAvailable =
                elasticsearchAvailabilityService.getStatus();

        if (!Boolean.FALSE.equals(elasticsearchAvailable)) {

            try {

                HotelSearchResponseDTO result =
                        searchUsingElasticsearch(
                                normalizedKeyword,
                                normalizedCity,
                                minPrice,
                                maxPrice,
                                ratings,
                                checkInDate,
                                effectiveCheckInTime,
                                checkOutDate,
                                effectiveCheckOutTime,
                                normalizedSortField,
                                normalizedSortOrder,
                                page,
                                size
                        );

                elasticsearchAvailabilityService.markAvailable();

                return result;

            } catch (Exception exception) {

                elasticsearchAvailabilityService.markUnavailable();

                log.warn(
                        "Elasticsearch search failed. Falling back to PostgreSQL.",
                        exception
                );
            }
        }

        return searchUsingPostgres(
                normalizedKeyword,
                normalizedCity,
                minPrice,
                maxPrice,
                ratings,
                checkInDate,
                effectiveCheckInTime,
                checkOutDate,
                effectiveCheckOutTime,
                normalizedSortField,
                normalizedSortOrder,
                page,
                size
        );
    }

    private HotelSearchResponseDTO searchUsingElasticsearch(
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

        BoolQuery.Builder builder =
                new BoolQuery.Builder();

        if (keyword != null && !keyword.isBlank()) {

            String[] tokens =
                    keyword.trim()
                            .toLowerCase()
                            .split("\\s+");

            for (String token : tokens) {

                if (token.isBlank()) {
                    continue;
                }

                builder.must(
                        b -> b.bool(
                                q -> q
                                        .should(
                                                s -> s.wildcard(
                                                        w -> w
                                                                .field("name")
                                                                .value("*" + token + "*")
                                                                .caseInsensitive(true)
                                                )
                                        )
                                        .should(
                                                s -> s.wildcard(
                                                        w -> w
                                                                .field("city")
                                                                .value("*" + token + "*")
                                                                .caseInsensitive(true)
                                                )
                                        )
                                        .minimumShouldMatch("1")
                        )
                );
            }
        }

        builder.filter(
                f -> f.term(
                        t -> t
                                .field("active")
                                .value(true)
                )
        );

        if (city != null && !city.isBlank()) {

            String normalizedCity =
                    city.trim().toLowerCase();

            builder.filter(
                    b -> b.wildcard(
                            w -> w
                                    .field("city")
                                    .value("*" + normalizedCity + "*")
                                    .caseInsensitive(true)
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
                                )
                                .sort(
                                        so -> so.field(
                                                f -> f
                                                        .field("id")
                                                        .order(SortOrder.Asc)
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

        hotels =
                filterHotelsByAvailability(
                        hotels,
                        checkInDate,
                        checkInTime,
                        checkOutDate,
                        checkOutTime
                );

        return createPaginatedResponse(
                hotels,
                page,
                size
        );
    }

    private HotelSearchResponseDTO searchUsingPostgres(
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
    ) {

        List<HotelEntity> hotelEntities =
                hotelRepository.searchHotels(
                        normalizeNullable(keyword),
                        normalizeNullable(city),
                        minPrice,
                        maxPrice,
                        ratings,
                        sortField,
                        sortOrder
                );

        List<HotelDocument> hotels =
                hotelEntities
                        .stream()
                        .map(this::convertToHotelDocument)
                        .toList();

        hotels =
                filterHotelsByAvailability(
                        hotels,
                        checkInDate,
                        checkInTime,
                        checkOutDate,
                        checkOutTime
                );

        return createPaginatedResponse(
                hotels,
                page,
                size
        );
    }

    private List<HotelDocument> filterHotelsByAvailability(
            List<HotelDocument> hotels,
            LocalDate checkInDate,
            LocalTime checkInTime,
            LocalDate checkOutDate,
            LocalTime checkOutTime
    ) {

        if (checkInDate == null || checkOutDate == null) {
            return hotels;
        }

        LocalTime effectiveCheckInTime =
                resolveCheckInTime(checkInTime);

        LocalTime effectiveCheckOutTime =
                resolveCheckOutTime(checkOutTime);

        LocalDateTime checkIn =
                LocalDateTime.of(
                        checkInDate,
                        effectiveCheckInTime
                );

        LocalDateTime checkOut =
                LocalDateTime.of(
                        checkOutDate,
                        effectiveCheckOutTime
                );

        Collection<String> activeStatuses =
                List.of(
                        BookingStatus.PAYMENT_PENDING.name(),
                        BookingStatus.BOOKED.name()
                );

        List<Long> availableHotelIds =
                roomRepo.findAvailableHotelIds(
                        checkInDate,
                        checkOutDate,
                        checkIn,
                        checkOut,
                        activeStatuses
                );

        Set<String> availableHotels =
                availableHotelIds
                        .stream()
                        .map(String::valueOf)
                        .collect(Collectors.toSet());

        return hotels
                .stream()
                .filter(
                        hotel ->
                                availableHotels.contains(
                                        hotel.getId()
                                )
                )
                .toList();
    }

    private LocalTime resolveCheckInTime(
            LocalTime checkInTime
    ) {

        if (checkInTime != null) {
            return checkInTime;
        }

        return LocalTime.of(14, 0);
    }

    private LocalTime resolveCheckOutTime(
            LocalTime checkOutTime
    ) {

        if (checkOutTime != null) {
            return checkOutTime;
        }

        return LocalTime.of(11, 0);
    }

    private HotelDocument convertToHotelDocument(
            HotelEntity hotel
    ) {

        String thumbnail = null;

        if (hotel.getImages() != null &&
                !hotel.getImages().isEmpty()) {

            thumbnail =
                    hotel.getImages().get(0);
        }

        return new HotelDocument(
                String.valueOf(hotel.getId()),
                hotel.getName(),
                hotel.getCity(),
                hotel.getMinPrice(),
                hotel.getAverageRating(),
                hotel.getTotalReviews(),
                hotel.getActive(),
                thumbnail
        );
    }

    private String normalizeSortField(
            String sortField
    ) {

        if (sortField == null ||
                sortField.isBlank()) {

            return "price";
        }

        return switch (sortField.toLowerCase()) {
            case "price" -> "price";
            case "name" -> "name";
            case "ratings" -> "ratings";
            default -> "price";
        };
    }

    private String normalizeSortOrder(
            String sortOrder
    ) {

        if ("desc".equalsIgnoreCase(sortOrder)) {
            return "desc";
        }

        return "asc";
    }

    private String normalizeNullable(
            String value
    ) {

        if (value == null ||
                value.isBlank()) {

            return null;
        }

        return value.trim();
    }

    private void validateSearchDates(
            LocalDate checkInDate,
            LocalTime checkInTime,
            LocalDate checkOutDate,
            LocalTime checkOutTime
    ) {

        boolean anyDateProvided =
                checkInDate != null ||
                        checkOutDate != null;

        boolean anyTimeProvided =
                checkInTime != null ||
                        checkOutTime != null;

        if (!anyDateProvided && anyTimeProvided) {
            throw new IllegalArgumentException(
                    "Check-in and check-out dates are required when times are provided."
            );
        }

        if (!anyDateProvided) {
            return;
        }

        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException(
                    "Both check-in date and check-out date must be provided."
            );
        }

        LocalTime effectiveCheckInTime =
                resolveCheckInTime(checkInTime);

        LocalTime effectiveCheckOutTime =
                resolveCheckOutTime(checkOutTime);

        LocalDateTime checkIn =
                LocalDateTime.of(
                        checkInDate,
                        effectiveCheckInTime
                );

        LocalDateTime checkOut =
                LocalDateTime.of(
                        checkOutDate,
                        effectiveCheckOutTime
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