package com.cmu.sweet.data.local.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.cmu.sweet.data.local.entities.Establishment
import com.cmu.sweet.data.local.entities.Review
import com.cmu.sweet.data.local.entities.User

data class ReviewWithUserAndEstablishment(
    @Embedded val review: Review,
    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val user: User?,
    @Relation(
        parentColumn = "establishmentId",
        entityColumn = "id"
    )
    val establishment: Establishment
)
