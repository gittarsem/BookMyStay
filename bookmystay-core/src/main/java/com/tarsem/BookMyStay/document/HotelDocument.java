package com.tarsem.BookMyStay.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tarsem.BookMyStay.Entity.HotelContactInfo;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;

@Document(indexName = "hotels",createIndex = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelDocument {

    @Id
    private String id;

    @Field(type= FieldType.Text)
    private String name;

    @Field(type = FieldType.Keyword,normalizer = "lowercase")
    private String city;

    @Field(type=FieldType.Double)
    private Double price;

    @Field(type=FieldType.Double)
    private Double ratings;

    @Field(type = FieldType.Boolean)
    private Boolean active;
}
