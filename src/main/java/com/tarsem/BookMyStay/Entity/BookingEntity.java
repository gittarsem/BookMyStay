package com.tarsem.BookMyStay.Entity;

import com.tarsem.BookMyStay.Enums.BookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bookings")
public class BookingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id",nullable = false)
    private HotelEntity hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id",nullable = false)
    private RoomEntity room;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private LocalDate check_in;

    @Column(nullable = false)
    private LocalDate check_out;

    @Column(name="total_price", nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,name = "status")
    private BookingStatus status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "booking_guest",joinColumns = @JoinColumn(name ="booking_id"),
            inverseJoinColumns = @JoinColumn(name = "guest_id"))
    private Set<GuestEntity> guests;

    @Column(unique = true)
    private String paymentId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime created_at;



}
