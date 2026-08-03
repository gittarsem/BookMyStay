package com.tarsem.BookMyStay.Controller;

import com.tarsem.BookMyStay.Service.Interfaces.ReviewService;
import com.tarsem.BookMyStay.dto.review.CreateReviewRequest;
import com.tarsem.BookMyStay.dto.review.ReviewResponse;
import com.tarsem.BookMyStay.dto.review.UpdateReviewRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/guest/review")
    public ResponseEntity<ReviewResponse> createReview(@RequestBody CreateReviewRequest reviewRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(reviewRequest));
    }

    @PutMapping("/guest/review/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        return ResponseEntity.ok(
                reviewService.updateReview(reviewId, request)
        );
    }

    @DeleteMapping("/guest/review/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {

        reviewService.deleteReview(reviewId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/hotels/{hotelId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getHotelReviews(
            @PathVariable Long hotelId,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {

        return ResponseEntity.ok(
                reviewService.getHotelReviews(hotelId, pageable)
        );
    }


}
