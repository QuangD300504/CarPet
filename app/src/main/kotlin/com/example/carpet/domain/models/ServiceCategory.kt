package com.example.carpet.domain.models

/**
 * Representing a major service category on the main Service Screen.
 * Based on the Pitchdeck's 3-in-1 solution.
 */
data class ServiceCategory(
    val id: String,
    val title: String,
    val shortDescription: String,
    val iconRes: Int
)