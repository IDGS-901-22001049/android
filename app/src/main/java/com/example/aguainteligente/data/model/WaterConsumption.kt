package com.example.aguainteligente.data.model

data class WaterConsumption(
    val id: String? = null,
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val liters: Float = 0f
)