package com.cmu.sweet.data.mappers

import com.cmu.sweet.data.local.entities.Establishment
import com.cmu.sweet.data.remote.dto.EstablishmentDto
import java.util.Date

fun Establishment.toDto(): EstablishmentDto = EstablishmentDto(
    id = id,
    name = name,
    address = address,
    type = type,
    description = description,
    latitude = latitude,
    longitude = longitude,
    addedBy = addedBy,
    createdAt = createdAt?.let { com.google.firebase.Timestamp(Date(it)) }
)

fun EstablishmentDto.toLocal(): Establishment = Establishment(
    id = id,
    name = name,
    address = address,
    type = type,
    description = description,
    latitude = latitude,
    longitude = longitude,
    addedBy = addedBy,
    createdAt = createdAt?.toDate()?.time
)
