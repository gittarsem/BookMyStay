package com.tarsem.BookMyStay.Entity;

import com.tarsem.BookMyStay.Enums.RoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "room_type_pricing",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"hotelId","roomType"}
                )
        }

)
public class RoomTypePricingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private HotelEntity hotel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType roomType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyPrice;
}
