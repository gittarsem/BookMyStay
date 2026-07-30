package com.tarsem.BookMyStay.dto.hotel;

import com.tarsem.BookMyStay.Enums.Gender;
import lombok.Data;

@Data
public class GuestDTO {
    private Long id;
    private String name;
    private Gender gender;
    private Integer age;
}
