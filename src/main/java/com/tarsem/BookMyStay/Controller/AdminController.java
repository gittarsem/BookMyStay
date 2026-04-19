package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Repositroy.HotelElasticRepository;
import com.tarsem.BookMyStay.Repositroy.HotelRepository;
import com.tarsem.BookMyStay.Utils.AppUtils;
import com.tarsem.BookMyStay.document.HotelDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private HotelElasticRepository elasticRepository;

    @GetMapping("/reindex")
    public String reindex() {

        elasticRepository.deleteAll();

        List<HotelDocument> docs = hotelRepository.findAll()
                .stream()
                .map(AppUtils::mapToDocument)
                .toList();

        elasticRepository.saveAll(docs);

        return "Reindex completed: " + docs.size();
    }
}
