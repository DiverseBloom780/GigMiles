package com.gigmiles.app.navigation

data class NavigationDestination(
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class NavigationInstruction(
    val text: String,
    val distanceMeters: Double,
    val latitude: Double,
    val longitude: Double
)
