package com.tarsem.BookMyStay.Service;

import com.razorpay.RazorpayException;
import com.tarsem.BookMyStay.Entity.*;
import com.tarsem.BookMyStay.Enums.BookingMode;
import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Enums.PaymentStatus;
import com.tarsem.BookMyStay.Exceptions.*;
import com.tarsem.BookMyStay.Repositroy.*;
import com.tarsem.BookMyStay.Service.Interfaces.BookingService;
import com.tarsem.BookMyStay.Service.Interfaces.RefundService;
import com.tarsem.BookMyStay.Strategy.PricingService;
import com.tarsem.BookMyStay.Strategy.RefundPolicy;
import com.tarsem.BookMyStay.dto.booking.*;
import com.tarsem.BookMyStay.dto.hotel.GuestDTO;
import com.tarsem.BookMyStay.dto.owner.OwnerBookingDTO;
import com.tarsem.BookMyStay.dto.owner.OwnerBookingDetailsDTO;
import com.tarsem.BookMyStay.dto.payment.RefundResponseDTO;
import com.tarsem.BookMyStay.producer.KafkaProducerService;
import com.tarsem.bookmystay.events.booking.BookingCancelledEvent;
import com.tarsem.bookmystay.events.booking.BookingExpiredEvent;
import com.tarsem.bookmystay.events.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.tarsem.BookMyStay.Utils.AppUtils.giveMeCurrentUser;

@Service
@AllArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private HotelRepository hotelRepository;
    private RoomRepository roomRepository;
    private InventoryRepository inventoryRepository;
    private RoomTypePricingRepository roomTypePricingRepository;
    private PricingService pricingService;
    private ModelMapper modelMapper;
    private GuestRepository guestRepository;
    private KafkaProducerService kafkaProducerService;
    private AuthorizationService authorizationService;
    private RefundService refundService;
    private final PriceQuoteService priceQuoteService;
    private final RefundPolicy refundPolicy;



    @Override
    @Transactional
    public BookingDTO initializeBooking(
            BookingRequestDTO request
    ) {

        if (request.getQuoteId() == null
                || request.getQuoteId().isBlank()) {

            throw new BusinessRuleViolationException(
                    "Price quote is required."
            );
        }

        PriceQuoteDTO quote =
                priceQuoteService.claimQuote(
                        request.getQuoteId()
                );

        try {

            validateQuote(
                    quote,
                    request
            );

            HotelEntity hotel =
                    hotelRepository.findById(
                            request.getHotelId()
                    ).orElseThrow(() ->
                            new HotelNotFoundException(
                                    "Hotel does not exist with this ID: "
                                            + request.getHotelId()
                            )
                    );

            RoomEntity room =
                    roomRepository.findByIdForUpdateAndLock(
                            quote.getRoomId()
                    ).orElseThrow(() ->
                            new RoomNotAvailableException(
                                    "Room no longer exists."
                            )
                    );

            Collection<String> activeStatuses =
                    List.of(
                            BookingStatus.PAYMENT_PENDING.name(),
                            BookingStatus.BOOKED.name()
                    );

            /*
             * Only hourly bookings need
             * booking-level time overlap.
             */
            if (request.getBookingMode()
                    == BookingMode.HOURLY) {

                if (!roomRepository.isRoomAvailable(
                        room.getId(),
                        request.getCheckInDate(),
                        request.getCheckInTime(),
                        request.getCheckOutDate(),
                        request.getCheckOutTime(),
                        activeStatuses
                )) {

                    throw new RoomNotAvailableException(
                            "Room is no longer available for the selected time."
                    );
                }
            }

            LocalDate inventoryEndDate;

            long requiredDays;

            if (request.getBookingMode() == BookingMode.HOURLY) {

                inventoryEndDate =
                        request.getCheckInDate().plusDays(1);

                requiredDays = 1;

            } else {

                inventoryEndDate =
                        request.getCheckOutDate();

                requiredDays =
                        ChronoUnit.DAYS.between(
                                request.getCheckInDate(),
                                request.getCheckOutDate()
                        );
            }

            List<InventoryEntity> inventory =
                    inventoryRepository.findAndLockAvailableInventory(
                            room.getId(),
                            request.getCheckInDate(),
                            inventoryEndDate
                    );


            if (inventory.size() != requiredDays) {

                throw new RoomNotAvailableException(
                        "Room inventory is no longer available."
                );
            }

            int updatedRows =
                    inventoryRepository.initBooking(
                            room.getId(),
                            request.getCheckInDate(),
                            inventoryEndDate
                    );

            if (updatedRows != requiredDays) {

                throw new RoomNotAvailableException(
                        "Room inventory is no longer available."
                );
            }

            BookingEntity booking =
                    BookingEntity.builder()
                            .status(
                                    BookingStatus.PAYMENT_PENDING
                            )
                            .hotel(hotel)
                            .room(room)
                            .roomsCount(1)
                            .checkInDate(
                                    request.getCheckInDate()
                            )
                            .checkOutDate(
                                    request.getCheckOutDate()
                            )
                            .checkInTime(
                                    request.getCheckInTime()
                            )
                            .checkOutTime(
                                    request.getCheckOutTime()
                            )
                            .bookingMode(
                                    request.getBookingMode()
                            )
                            .adultCount(
                                    request.getAdultCount()
                            )
                            .childCount(
                                    request.getChildCount()
                            )
                            .user(
                                    giveMeCurrentUser()
                            )
                            .totalPrice(
                                    quote.getFinalPrice()
                            )
                            .build();

            bookingRepository.save(booking);

            priceQuoteService.completeQuote(
                    request.getQuoteId()
            );

            return modelMapper.map(
                    booking,
                    BookingDTO.class
            );

        } catch (Exception ex) {

            priceQuoteService.releaseQuote(
                    request.getQuoteId()
            );

            throw ex;
        }
    }


    private void validateQuote(
            PriceQuoteDTO quote,
            BookingRequestDTO request
    ) {

        if (quote == null) {
            throw new BusinessRuleViolationException(
                    "Price quote is invalid or expired."
            );
        }

        if (request == null) {
            throw new BusinessRuleViolationException(
                    "Booking request is required."
            );
        }

        boolean mismatch =
                !Objects.equals(
                        quote.getHotelId(),
                        request.getHotelId()
                )
                        || request.getRoomType() == null
                        || !Objects.equals(
                        quote.getRoomType(),
                        request.getRoomType().name()
                )
                        || request.getBookingMode() == null
                        || !Objects.equals(
                        quote.getBookingMode(),
                        request.getBookingMode().name()
                )
                        || !Objects.equals(
                        quote.getCheckInDate(),
                        request.getCheckInDate()
                )
                        || !Objects.equals(
                        quote.getCheckOutDate(),
                        request.getCheckOutDate()
                )
                        || !Objects.equals(
                        quote.getCheckInTime(),
                        request.getCheckInTime()
                )
                        || !Objects.equals(
                        quote.getCheckOutTime(),
                        request.getCheckOutTime()
                );

        if (mismatch) {
            throw new BusinessRuleViolationException(
                    "Price quote does not match the booking request."
            );
        }
    }

    @Override
    @Transactional
    public @Nullable BookingDTO addGuests(Long bookingId, List<GuestDTO> guests) {
        log.info("Adding guests for booking with id: {}", bookingId);
        BookingEntity booking=bookingRepository.findById(bookingId).orElseThrow(
                ()-> new BookingNotFoundException("Booking not found with this id: "+bookingId)
        );
        UserEntity user=giveMeCurrentUser();
        if(!user.getId().equals(booking.getUser().getId())){
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }

        if(booking.getStatus()!=BookingStatus.BOOKED){
            throw new PaymentException("Payment has not be completed yet");
        }


        int guestsCount=booking.getAdultCount()+booking.getChildCount();

        int existingGuests = booking.getGuests().size();

        if (existingGuests + guests.size() > guestsCount) {
            throw new BusinessRuleViolationException(
                    "Expected maximum " + guestsCount +
                            " guests but received " +
                            (existingGuests + guests.size())
            );
        }
        for(GuestDTO guest:guests){
            GuestEntity guest1=modelMapper.map(guest,GuestEntity.class);
            guest1.setUser(user);
            guest1.setBooking(booking);
            booking.getGuests().add(guest1);
        }
        booking.setUpdatedAt(LocalDateTime.now());
        booking=bookingRepository.save(booking);
        return modelMapper.map(booking,BookingDTO.class);

    }

    private LocalDate getInventoryEndDate(BookingEntity booking) {
        return booking.getBookingMode() == BookingMode.HOURLY
                ? booking.getCheckInDate().plusDays(1)
                : booking.getCheckOutDate();
    }

    @Override
    @Transactional
    public void releaseInventory(Long bookingId) {
        log.info("Releasing inventory for booking : {}", bookingId);

        BookingEntity booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new BookingNotFoundException("Booking does not exist with this ID: " + bookingId)
        );
        LocalDate inventoryEndDate =
                getInventoryEndDate(booking);

        inventoryRepository.releaseReservation(
                booking.getRoom().getId(),
                booking.getCheckInDate(),
                inventoryEndDate
        );
    }

    @Override
    @Transactional
    public void confirmInventory(Long bookingId){
        log.info("Confirming inventory for booking : {}",bookingId);

        BookingEntity booking=bookingRepository.findById(bookingId).orElseThrow(
                ()-> new BookingNotFoundException("Booking does not exist with this ID: "+bookingId)
        );
        LocalDate inventoryEndDate =
                getInventoryEndDate(booking);

        inventoryRepository.confirmReservation(
                booking.getRoom().getId(),
                booking.getCheckInDate(),
                inventoryEndDate
        );
    }

    @Transactional
    public BookingCancelDTO cancelBooking(Long bookingId)
            throws RuntimeException, AccessDeniedException, RazorpayException {

        log.info("Cancel request for booking : {}", bookingId);

        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException(
                                "Booking does not exist for id :" + bookingId
                        )
                );

        if (!booking.getUser().getId().equals(giveMeCurrentUser().getId())) {
            throw new UnAuthorisedException(
                    "You are not authorised to cancel this booking"
            );
        }

        if (booking.getStatus() != BookingStatus.BOOKED) {
            throw new BusinessRuleViolationException(
                    "Only booked reservations can be cancelled"
            );
        }

        // Calculate/process refund.
        // ₹0 refund must NOT throw.
        RefundResponseDTO refundResponseDTO =
                refundService.refund(bookingId);

        // Release inventory
        LocalDate inventoryEndDate = getInventoryEndDate(booking);

        inventoryRepository.cancelBooking(
                booking.getRoom().getId(),
                booking.getCheckInDate(),
                inventoryEndDate
        );

        // Cancel booking regardless of refund amount
        booking.setStatus(BookingStatus.CANCELLED);

        kafkaProducerService.publishCancelledBooking(
                buildBookingCancelledEvent(
                        booking,
                        refundResponseDTO.getRefundAmount()
                )
        );

        return BookingCancelDTO.builder()
                .bookingId(booking.getId())
                .bookingStatus(booking.getStatus())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .refundStatus(refundResponseDTO.getRefundStatus())
                .refundAmount(
                        BigDecimal.valueOf(
                                refundResponseDTO.getRefundAmount()
                        )
                )
                .message("Booking cancelled successfully.")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void expireBooking(BookingEntity booking) {
        log.info("Expiring booking: {}", booking.getId());
        releaseInventory(booking.getId());
        booking.setStatus(BookingStatus.EXPIRED);
        if (booking.getPayment() != null) {
            booking.getPayment().setPaymentStatus(PaymentStatus.EXPIRED);
        }
        kafkaProducerService.publishExpiredBooking(buildBookingExpiredEvent(booking));
    }

    @Override
    public List<BookingHistoryDTO> getMyBookings() {
        List<BookingEntity> bookingHistory=bookingRepository.findAllByUserOrderByCreatedAtDesc(giveMeCurrentUser());

        return bookingHistory.stream()
                .map(this::mapToBookingHistoryDTO)
                .toList();
    }

    @Override
    public BookingDetailsDTO getBookingDetails(Long bookingId) {
        BookingEntity booking=bookingRepository.findById(bookingId).orElseThrow(
                ()->new BookingNotFoundException("Booking does not exist with id:"+bookingId));
        PaymentEntity payment = booking.getPayment();

        PaymentStatus paymentStatus =
                payment != null
                        ? payment.getPaymentStatus()
                        : PaymentStatus.PENDING;

        BigDecimal amount =
                payment != null
                        ? payment.getAmount()
                        : booking.getTotalPrice();

        if(!booking.getUser().getId().equals(giveMeCurrentUser().getId())){
            throw new UnAuthorisedException("User is not allowed to Access this booking");
        }
        BookingDetailsDTO details=new BookingDetailsDTO();
        details.setBookingId(bookingId);
        details.setHotelName(booking.getHotel().getName());
        details.setCity(booking.getHotel().getCity());
        details.setRoomType(booking.getRoom().getRoomType());
        details.setCheckInDate(booking.getCheckInDate());
        details.setCheckOutDate(booking.getCheckOutDate());
        details.setBookingMode(booking.getBookingMode());
        details.setCheckInTime(booking.getCheckInTime());
        details.setCheckOutTime(booking.getCheckOutTime());
        details.setAdultCount(booking.getAdultCount());
        details.setChildCount(booking.getChildCount());
        details.setPaymentStatus(paymentStatus);
        details.setBookingStatus(booking.getStatus());
        details.setAmount(amount);


        Set<GuestDTO> guests = booking.getGuests()
                .stream()
                .map(guest -> modelMapper.map(guest, GuestDTO.class))
                .collect(Collectors.toSet());
        details.setGuests(guests);
        return details;

    }

    @Override
    public List<OwnerBookingDTO> getHotelBookings(
            Long hotelId,
            BookingStatus bookingStatus) {

        HotelEntity hotel = authorizationService.getOwnedHotel(hotelId);

        List<BookingEntity> bookings;

        if (bookingStatus == null) {
            bookings = bookingRepository
                    .findAllByRoom_HotelOrderByCreatedAtDesc(hotel);
        } else {
            bookings = bookingRepository
                    .findAllByRoom_HotelAndStatusOrderByCreatedAtDesc(
                            hotel,
                            bookingStatus
                    );
        }

        return bookings.stream()
                .map(this::mapToOwnerBookingDTO)
                .toList();
    }

    @Override
    public OwnerBookingDetailsDTO getHotelBooking(
            Long hotelId,
            Long bookingId) {

        HotelEntity hotel = authorizationService.getOwnedHotel(hotelId);

        BookingEntity booking = bookingRepository
                .findByIdAndRoom_Hotel(bookingId, hotel)
                .orElseThrow(() ->
                        new BookingNotFoundException(
                                "Booking not found with id : " + bookingId));

        return mapToOwnerBookingDetailsDTO(booking);
    }

    @Override
    @Transactional
    public GuestDTO updateGuestDetails(Long bookingId, Long guestId,GuestDTO request) {
        BookingEntity booking=bookingRepository.findById(bookingId).orElseThrow(
                ()->new BookingNotFoundException("Booking not found with id:"+bookingId)
        );
        GuestEntity guest=guestRepository.findById(guestId).orElseThrow(
                ()-> new ResourceNotFoundException("Guest not found with id:"+request.getId())
        );
        UserEntity user=giveMeCurrentUser();

        if(!user.getId().equals(booking.getUser().getId())){
            throw new UnAuthorisedException("User is not allowed to Access this booking");
        }

        if (!guest.getBooking().getId().equals(bookingId)) {
            throw new UnAuthorisedException(
                    "Guest does not belong to this booking"
            );
        }

        guest.setName(request.getName().trim());
        guest.setAge(request.getAge());
        guest.setUser(user);
        guest.setGender(request.getGender());

        guestRepository.save(guest);
        booking.setUpdatedAt(LocalDateTime.now());
        return modelMapper.map(guest,GuestDTO.class);

    }

    @Override
    @Transactional
    public String deleteGuestDetails(Long bookingId, Long guestId) {
        BookingEntity booking=bookingRepository.findById(bookingId).orElseThrow(
                ()->new BookingNotFoundException("Booking not found with id:"+bookingId)
        );
        GuestEntity guest=guestRepository.findById(guestId).orElseThrow(
                ()-> new ResourceNotFoundException("Guest not found with id:"+guestId)
        );
        UserEntity user=giveMeCurrentUser();

        if(!user.getId().equals(booking.getUser().getId())){
            throw new UnAuthorisedException("User is not allowed to Access this booking");
        }

        if (!guest.getBooking().getId().equals(bookingId)) {
            throw new UnAuthorisedException(
                    "Guest does not belong to this booking"
            );
        }

        booking.getGuests().remove(guest);
        booking.setUpdatedAt(LocalDateTime.now());

        return guest.getName()+"is deleted";
    }

    @Override
    @Transactional(readOnly = true)
    public CancellationPreviewDTO getCancellationPreview(Long bookingId) {

        log.info("Cancellation preview request for booking : {}", bookingId);

        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException(
                                "Booking does not exist for id : " + bookingId
                        )
                );

        UserEntity currentUser = giveMeCurrentUser();

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new UnAuthorisedException(
                    "You are not authorised to view this booking"
            );
        }

        if (booking.getStatus() != BookingStatus.BOOKED) {
            throw new BusinessRuleViolationException(
                    "Only booked reservations can be cancelled"
            );
        }

        PaymentEntity payment = booking.getPayment();

        if (payment == null) {
            throw new PaymentException("Payment not found for this booking");
        }

        BigDecimal amountPaid = payment.getAmount();

        double refundPercentage =
                refundPolicy.calculateRefundPercentage(
                        booking.getCheckInDate()
                );

        BigDecimal percentage =
                BigDecimal.valueOf(refundPercentage)
                        .divide(BigDecimal.valueOf(100));

        BigDecimal refundAmount =
                amountPaid.multiply(percentage);

        BigDecimal nonRefundableCharges =
                payment.getGatewayFee()
                        .add(payment.getGatewayTax());

        refundAmount = refundAmount
                .subtract(nonRefundableCharges)
                .max(BigDecimal.ZERO);

        BigDecimal cancellationFee =
                amountPaid.subtract(refundAmount);

        return CancellationPreviewDTO.builder()
                .bookingId(booking.getId())
                .amountPaid(amountPaid)
                .refundPercentage(
                        BigDecimal.valueOf(refundPercentage)
                )
                .refundAmount(refundAmount)
                .cancellationFee(cancellationFee)
                .build();
    }


    private BookingHistoryDTO mapToBookingHistoryDTO(
            BookingEntity booking
    ) {

        BookingHistoryDTO dto = new BookingHistoryDTO();

        /*
         * Hotel image
         */
        if (booking.getHotel().getImages() != null
                && !booking.getHotel().getImages().isEmpty()) {

            dto.setHotelImage(
                    booking.getHotel().getImages().getFirst()
            );
        }

        /*
         * Basic booking information
         */
        dto.setBookingId(
                booking.getId()
        );

        dto.setHotelId(
                booking.getRoom().getHotel().getId()
        );

        dto.setHotelName(
                booking.getRoom().getHotel().getName()
        );

        dto.setCity(
                booking.getRoom().getHotel().getCity()
        );

        dto.setRoomType(
                booking.getRoom().getRoomType()
        );


        /*
         * Booking mode
         *
         * DAILY  -> no time should be displayed
         * HOURLY -> time should be displayed
         */
        dto.setBookingMode(
                booking.getBookingMode()
        );


        /*
         * Booking dates
         */
        dto.setCheckInDate(
                booking.getCheckInDate()
        );

        dto.setCheckOutDate(
                booking.getCheckOutDate()
        );


        /*
         * Booking times
         *
         * These will normally be:
         *
         * DAILY:
         *   null
         *   null
         *
         * HOURLY:
         *   10:00
         *   14:00
         */
        dto.setCheckInTime(
                booking.getCheckInTime()
        );

        dto.setCheckOutTime(
                booking.getCheckOutTime()
        );


        /*
         * Guests
         */
        dto.setAdultCount(
                booking.getAdultCount()
        );

        dto.setChildCount(
                booking.getChildCount()
        );


        /*
         * Booking status
         */
        dto.setBookingStatus(
                booking.getStatus()
        );


        /*
         * Review
         */
        dto.setReviewId(
                booking.getReview() != null
                        ? booking.getReview().getId()
                        : null
        );



        PaymentEntity payment =
                booking.getPayment();


        /*
         * =========================================================
         * CASE 1:
         *
         * PAYMENT ENTITY EXISTS
         * =========================================================
         */

        if (payment != null) {

            PaymentStatus paymentStatus =
                    payment.getPaymentStatus();

            dto.setPaymentStatus(
                    paymentStatus
            );


            /*
             * Pending payment:
             *
             * The amount that the customer needs to pay
             * is the booking total price.
             *
             * Fallback to payment amount only if total price
             * is not available.
             */

            if (paymentStatus ==
                    PaymentStatus.PENDING) {

                if (booking.getTotalPrice() != null) {

                    dto.setAmount(
                            booking.getTotalPrice()
                    );

                } else {

                    dto.setAmount(
                            payment.getAmount()
                    );
                }

            } else {

                /*
                 * SUCCESS / FAILED / REFUNDED etc.
                 */

                dto.setAmount(
                        payment.getAmount()
                );
            }

        }
        else if (
                booking.getStatus() != null
                        && "PAYMENT_PENDING".equals(
                        booking.getStatus().name()
                )
        ) {

            /*
             * There is no PaymentEntity yet,
             * but the booking requires payment.
             */

            dto.setPaymentStatus(
                    PaymentStatus.PENDING
            );


            /*
             * Use the booking's actual calculated
             * total amount.
             */

            dto.setAmount(
                    booking.getTotalPrice()
            );
        }



        return dto;
    }

    private BookingCancelledEvent buildBookingCancelledEvent(BookingEntity booking, Double refundAmount) {

        return BookingCancelledEvent.builder()

                .userId(booking.getUser().getId())
                .customerName(booking.getUser().getName())
                .customerEmail(booking.getUser().getEmail())
                .hotelName(booking.getHotel().getName())
                .roomType(booking.getRoom().getRoomType().toString())
                .bookingId(booking.getId())
                .refundAmount(BigDecimal.valueOf(refundAmount))
                .eventType(EventType.BOOKING_CANCELLED)
                .build();
    }

    public BookingExpiredEvent buildBookingExpiredEvent(BookingEntity booking){
        return BookingExpiredEvent.builder()
                .userId(booking.getUser().getId())
                .customerName(booking.getUser().getName())
                .customerEmail(booking.getUser().getEmail())
                .hotelName(booking.getHotel().getName())
                .bookingId(booking.getId())
                .amountPaid(booking.getTotalPrice())
                .eventType(EventType.BOOKING_EXPIRED)
                .build();
    }
    private OwnerBookingDTO mapToOwnerBookingDTO(BookingEntity booking) {

        OwnerBookingDTO dto = new OwnerBookingDTO();

        dto.setBookingId(booking.getId());

        dto.setGuestName(booking.getUser().getName());

        dto.setRoomType(booking.getRoom().getRoomType());

        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());

        dto.setBookingStatus(booking.getStatus());

        dto.setPaymentStatus(
                booking.getPayment() != null
                        ? booking.getPayment().getPaymentStatus()
                        : PaymentStatus.PENDING
        );

        dto.setAmount(
                booking.getPayment()!=null
                ? booking.getPayment().getAmount()
                : booking.getTotalPrice()
        );

        return dto;
    }

    private OwnerBookingDetailsDTO mapToOwnerBookingDetailsDTO(
            BookingEntity booking) {

        OwnerBookingDetailsDTO dto = new OwnerBookingDetailsDTO();

        dto.setBookingId(booking.getId());

        dto.setGuestName(
                booking.getUser().getName());

        dto.setEmail(
                booking.getUser().getEmail());

        dto.setPhone(
                booking.getHotel().getHotelContactInfo().getPhoneNumber());

        dto.setHotelName(
                booking.getHotel().getName());

        dto.setCity(
                booking.getHotel().getCity());

        dto.setRoomType(
                booking.getRoom().getRoomType());

        dto.setAdultCount(
                booking.getAdultCount());

        dto.setChildCount(
                booking.getChildCount());

        dto.setCheckInDate(
                booking.getCheckInDate());

        dto.setCheckOutDate(
                booking.getCheckOutDate());

        dto.setBookingStatus(
                booking.getStatus());

        dto.setPaymentStatus(
                booking.getPayment().getPaymentStatus());

        dto.setAmount(
                booking.getPayment().getAmount());

        dto.setGuests(
                booking.getGuests()
                        .stream()
                        .map(guest ->
                                modelMapper.map(guest, GuestDTO.class))
                        .collect(Collectors.toSet())
        );

        return dto;
    }



}
