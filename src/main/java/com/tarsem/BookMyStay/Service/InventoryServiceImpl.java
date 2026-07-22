package com.tarsem.BookMyStay.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.tarsem.BookMyStay.document.HotelDocument;
import com.tarsem.BookMyStay.dto.HotelSearchResponseDTO;
import com.tarsem.BookMyStay.Entity.RoomEntity;
import com.tarsem.BookMyStay.Exceptions.ResourceNotFoundException;
import com.tarsem.BookMyStay.Repositroy.HotelMinPriceRepository;
import com.tarsem.BookMyStay.Repositroy.InventoryRepository;
import com.tarsem.BookMyStay.Repositroy.RoomRepository;
import com.tarsem.BookMyStay.Service.Interfaces.InventoryService;
import com.tarsem.BookMyStay.dto.InventoryDTO;
import com.tarsem.BookMyStay.dto.InventoryUpdateRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    @Cacheable(value = "hotel_search",
            key = "#keyword + '-' + #city + '-' + #minPrice + '-' + #maxPrice + '-' + #ratings + '-' + #sortField + '-' + #sortOrder + '-' + #page + '-' + #size"
    )
    public HotelSearchResponseDTO searchHotels(String keyword,
                                               String city,
                                               Double minPrice,
                                               Double maxPrice,
                                               Double ratings,
                                               String sortField,
                                               String sortOrder,
                                               int page,
                                               int size) throws IOException {
        BoolQuery.Builder builder=new BoolQuery.Builder();

        if(keyword!=null && !keyword.isEmpty()){
            builder.must(b->b
                    .match(mm->mm
                            .field("name")
                            .query(keyword)
                    )
            );

        }

        builder.filter(f -> f
                .term(t -> t
                        .field("active")
                        .value(true)
                )
        );

        if(city!=null && !city.isEmpty()){
            builder.filter(b->b
                    .term(m->m
                            .value(city.toLowerCase())
                            .field("city")
                    )

            );
        }

        if(minPrice!=null && maxPrice!=null){
            builder.filter(f->f
                    .range(r->r

                            .number(n->{

                                n.field("price");
                                if(minPrice!=null) n.gte(minPrice);
                                if(maxPrice!=null) n.lte(maxPrice);

                                return n;
                            }


                            )
                    )
            );
        }

        if(ratings!=null){
            builder.filter(f->f
                    .range(r->r
                            .number(n->{
                                n.field("ratings");
                                return n.gte(ratings);
                            })
                    )
            );
        }

        BoolQuery boolQuery =builder.build();
        SearchResponse<HotelDocument> response =elasticsearch.search(s->s
                .index("hotels")
                .query(q->q.bool(boolQuery))
                .from(page*size)
                .size(size)
                .sort(so->so
                        .field(f->f
                                .field(sortField.equals("name")?"name.keyword":sortField)
                                .order(sortOrder.equalsIgnoreCase("asc")? SortOrder.Asc:SortOrder.Desc)
                        )

                ),
                HotelDocument.class
        );

        List<HotelDocument> hotelDocumentList=response.hits().hits().stream()
                .map(it->it.source())
                .toList();
        long total=response.hits().total().value();

        return new HotelSearchResponseDTO(hotelDocumentList,total,page,size);
    }


    @Override
    public List<InventoryDTO> getAllInventoryByRoom(Long roomId) {
        log.info("Getting All inventory by room for room with id: {}", roomId);
        RoomEntity room=roomRepo.findById(roomId).orElseThrow(
                ()-> new ResourceNotFoundException("Room with id "+roomId+" does not exist")
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
                ()-> new ResourceNotFoundException("Room with id "+roomId+" does not exist")
        );

        if(!verifyHotelOwner(room.getHotel())) throw new AccessDeniedException("You are not the owner of room with id: "+roomId);

        inventoryRepository.updateInventory(roomId,inventoryUpdateRequest.getStartDate(),
                inventoryUpdateRequest.getEndDate(),inventoryUpdateRequest.getSurgeFactor(),
                inventoryUpdateRequest.getClosed());
        return "Updated Room with id: " + roomId;
    }
}
