package com.cmu.sweet.data.mappers

import com.cmu.sweet.data.local.entities.Review
import com.cmu.sweet.data.remote.dto.ReviewDto

fun Review.toDto(): ReviewDto = ReviewDto(
    id = id,
    establishmentId = establishmentId,
    userId = userId,
    rating = rating,
    priceRating = priceRating,
    comment = comment,
    timestamp = timestamp
)

fun ReviewDto.toLocal(): Review = Review(
    id = id,
    establishmentId = establishmentId,
    userId = userId,
    rating = rating,
    priceRating = priceRating,
    comment = comment,
    timestamp = timestamp
)
