package com.cmu.sweet.data.local.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.cmu.sweet.data.local.entities.Establishment
import com.cmu.sweet.data.local.entities.Review

data class EstablishmentWithReviews(
    @Embedded val establishment: Establishment,
    @Relation(
        parentColumn = "id",
        entityColumn = "establishmentId"
    )
    val reviews: List<Review>
)
