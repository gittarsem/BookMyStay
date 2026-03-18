package com.tarsem.BookMyStay.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "inventory",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"room_id","date"})
        }
)
public class InventoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "room_id",nullable = false)
    private RoomEntity room;

    private Date date;

    @Column(nullable = false,columnDefinition = "INTEGER DEFAULT 0")
    private Integer bookCount;

    @Column(nullable = false,columnDefinition = "INTEGER DEFAULT 0")
    private Integer reservedCount;

    @Column(nullable = false,columnDefinition = "INTEGER DEFAULT 0")
    private Integer totalCount;

    @Column(nullable = false,precision = 5,scale = 2)
    private BigDecimal surgeFactor;

    @Column(nullable = false,precision = 10,scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private Boolean closed;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime created_at;

}
