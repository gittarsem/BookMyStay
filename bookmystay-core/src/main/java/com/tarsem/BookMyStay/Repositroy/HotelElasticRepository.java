package com.tarsem.BookMyStay.Repositroy;

import com.tarsem.BookMyStay.document.HotelDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelElasticRepository
        extends ElasticsearchRepository<HotelDocument, String> {
}
