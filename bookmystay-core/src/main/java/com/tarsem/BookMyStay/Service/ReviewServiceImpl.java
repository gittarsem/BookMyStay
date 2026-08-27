package com.tarsem.BookMyStay.Service;

import com.tarsem.BookMyStay.Entity.BookingEntity;
import com.tarsem.BookMyStay.Entity.HotelEntity;
import com.tarsem.BookMyStay.Entity.ReviewEntity;
import com.tarsem.BookMyStay.Entity.UserEntity;
import com.tarsem.BookMyStay.Enums.BookingStatus;
import com.tarsem.BookMyStay.Exceptions.*;
import com.tarsem.BookMyStay.Repositroy.BookingRepository;
import com.tarsem.BookMyStay.Repositroy.HotelElasticRepository;
import com.tarsem.BookMyStay.Repositroy.HotelRepository;
import com.tarsem.BookMyStay.Repositroy.ReviewRepository;
import com.tarsem.BookMyStay.Service.Interfaces.ReviewService;
import com.tarsem.BookMyStay.dto.review.CreateReviewRequest;
import com.tarsem.BookMyStay.dto.review.ReviewResponse;
import com.tarsem.BookMyStay.dto.review.ReviewStatistics;
import com.tarsem.BookMyStay.dto.review.UpdateReviewRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.tarsem.BookMyStay.Utils.AppUtils.giveMeCurrentUser;
import static com.tarsem.BookMyStay.Utils.AppUtils.mapToDocument;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final ModelMapper modelMapper;
    private final HotelRepository hotelRepository;
    private final HotelElasticRepository elasticRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest reviewRequest) {
        BookingEntity booking=bookingRepository.findById(reviewRequest.getBookingId()).orElseThrow(
                ()->new BookingNotFoundException("Booking does not exist:"+reviewRequest.getBookingId())
        );
        UserEntity user=giveMeCurrentUser();
        if(!booking.getUser().getId().equals(user.getId())){
            throw new UnAuthorisedException("User is not allowed for review");
        }

        if(!BookingStatus.BOOKED.equals(booking.getStatus())){
            throw new BusinessRuleViolationException("Booking is not completed yet");
        }

        if(booking.getCheckOutDate().isAfter(LocalDate.now())){
            throw new BusinessRuleViolationException("User is not stayed yet");
        }

        if (booking.getHotel().getOwner().getId().equals(user.getId())) {
            throw new BusinessRuleViolationException("Owners cannot review their own hotels");
        }

        if (booking.getReview() != null) {
            throw new BusinessRuleViolationException("Review already exists for this booking");
        }

        ReviewEntity review=new ReviewEntity();
        review.setBooking(booking);
        review.setRating(reviewRequest.getRatings());
        review.setComment(reviewRequest.getComment());
        review.setHotel(booking.getHotel());
        review.setGuest(user);
        reviewRepository.save(review);
        updateHotelRating(booking.getHotel().getId());

        ReviewResponse response = modelMapper.map(review, ReviewResponse.class);
        response.setGuestName(review.getGuest().getName());

        return response;
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId,
                                       UpdateReviewRequest request) {

        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ReviewNotFoundException("Review not found : " + reviewId));

        UserEntity currentUser = giveMeCurrentUser();

        if (!review.getGuest().getId().equals(currentUser.getId())) {
            throw new UnAuthorisedException(
                    "You are not allowed to update this review");
        }

        review.setRating(request.getRatings());
        review.setComment(request.getComment());

        updateHotelRating(review.getHotel().getId());

        ReviewResponse response =
                modelMapper.map(review, ReviewResponse.class);

        response.setGuestName(review.getGuest().getName());

        return response;
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {

        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ReviewNotFoundException(
                                "Review not found : " + reviewId));

        UserEntity currentUser = giveMeCurrentUser();

        if (!review.getGuest().getId().equals(currentUser.getId())) {
            throw new UnAuthorisedException(
                    "You are not allowed to delete this review");
        }

        Long hotelId = review.getHotel().getId();

        reviewRepository.delete(review);

        updateHotelRating(hotelId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getHotelReviews(Long hotelId, Pageable pageable) {

        if (!hotelRepository.existsById(hotelId)) {
            throw new HotelNotFoundException("Hotel not found : " + hotelId);
        }

        Page<ReviewEntity> reviews =
                reviewRepository.findByHotelId(hotelId, pageable);

        return reviews.map(review -> {
            ReviewResponse response =
                    modelMapper.map(review, ReviewResponse.class);

            response.setGuestName(review.getGuest().getName());

            return response;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReview(Long reviewId) {

        ReviewEntity review =
                reviewRepository.findById(reviewId)
                        .orElseThrow(() ->
                                new ReviewNotFoundException(
                                        "Review not found : " + reviewId
                                )
                        );

        UserEntity currentUser =
                giveMeCurrentUser();

        if (!review.getGuest().getId()
                .equals(currentUser.getId())) {

            throw new UnAuthorisedException(
                    "You are not allowed to view this review"
            );
        }

        ReviewResponse response =
                modelMapper.map(
                        review,
                        ReviewResponse.class
                );

        response.setGuestName(
                review.getGuest().getName()
        );

        return response;
    }


    private void updateHotelRating(Long hotelId) {

        ReviewStatistics reviewStatistics =
                reviewRepository.calculateHotelStatistics(hotelId);

        HotelEntity hotel =
                hotelRepository.findById(hotelId)
                        .orElseThrow(() ->
                                new HotelNotFoundException(
                                        "Hotel not found with this id: " + hotelId
                                )
                        );

        hotel.setAverageRating(
                reviewStatistics.getAverageRating() == null
                        ? 0.0
                        : reviewStatistics.getAverageRating()
        );

        hotel.setTotalReviews(
                Math.toIntExact(reviewStatistics.getTotalReviews())
        );

        // Persist updated rating/review count
        hotelRepository.save(hotel);

        // Update Elasticsearch so hotel search gets the latest values
        elasticRepository.save(mapToDocument(hotel));
    }

}
