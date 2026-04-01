package com.tarsem.BookMyStay.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;


@Data
@Embeddable
public class HotelContactInfo {
    private String address;
    private String phoneNumber;
    private String email;
    @Column(name = "location")
    private String city;
}
