package com.alyona.rustore.ui.theme.models

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationItem(
    val id: Int,
    val name: String,
    val category: String,
    val shortDescription: String,
    val fullDescription: String,
    val developer: String,
    val ageRating: String,
    val apkUrl: String,
    val iconUrl: String,
    val screenshots: List<String>,
    val rating: Double
)