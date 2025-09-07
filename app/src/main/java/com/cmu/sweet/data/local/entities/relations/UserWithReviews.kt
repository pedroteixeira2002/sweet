package com.cmu.sweet.data.local.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.cmu.sweet.data.local.entities.Review
import com.cmu.sweet.data.local.entities.User

data class UserWithReviews(
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val reviews: List<Review>
)
