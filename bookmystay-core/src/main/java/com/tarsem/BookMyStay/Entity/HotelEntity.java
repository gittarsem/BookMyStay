package com.tarsem.BookMyStay.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "hotels")
public class HotelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Embedded
    private HotelContactInfo hotelContactInfo;

    @Column(name = "location")
    private String city;

    @Column(name="min_price")
    private Double minPrice;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime created_at;

    @UpdateTimestamp
    private LocalDateTime deleted_at;

    @Column(nullable = false)
    private Boolean active;

    @OneToMany(mappedBy = "hotel",fetch = FetchType.LAZY)
    @JsonIgnore
    private List<RoomEntity> rooms;

    @ManyToOne(optional = false)
    private UserEntity owner;

    @Column(nullable = false)
    private Double averageRating = 0.0;

    @Column(nullable = false)
    private Integer totalReviews = 0;

    @OneToMany(
            mappedBy = "hotel",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ReviewEntity> reviews = new ArrayList<>();
}
