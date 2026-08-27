package com.tarsem.BookMyStay.Service.Interfaces;

import com.tarsem.BookMyStay.dto.review.CreateReviewRequest;
import com.tarsem.BookMyStay.dto.review.ReviewResponse;
import com.tarsem.BookMyStay.dto.review.UpdateReviewRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface ReviewService {
    ReviewResponse createReview(CreateReviewRequest reviewRequest);

    ReviewResponse updateReview(Long reviewId, @Valid UpdateReviewRequest request);

    void deleteReview(Long reviewId);

    Page<ReviewResponse> getHotelReviews(Long hotelId, Pageable pageable);

    ReviewResponse getReview(Long reviewId);
}
