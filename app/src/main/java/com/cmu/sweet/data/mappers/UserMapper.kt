package com.cmu.sweet.data.mappers

import com.cmu.sweet.data.local.entities.User
import com.cmu.sweet.data.remote.dto.UserDto

fun User.toDto(): UserDto = UserDto(
    id = id,
    name = name,
    email = email,
    bio = bio,
    createdAt = createdAt
)

fun UserDto.toLocal(): User = User(
    id = id,
    name = name,
    email = email,
    bio = bio,
    createdAt = createdAt
)
