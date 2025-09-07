package com.cmu.sweet.data.remote.dto

/**
 * Firestore DTO for User.
 * Used exclusively for Firestore read/write operations.
 */
data class UserDto(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val bio: String? = null,
    val createdAt: Long? = null
)
