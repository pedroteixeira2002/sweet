package com.cmu.sweet.data.local.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.cmu.sweet.data.local.entities.Establishment
import com.cmu.sweet.data.local.entities.User

data class UserWithEstablishments(
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "addedBy"
    )
    val establishments: List<Establishment>
)
