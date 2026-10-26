package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_ip_history")
data class LocationIpHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 5.0f,
    val mockIpAddress: String,
    val ipCountry: String,
    val ipCity: String,
    val carrierName: String = "NTT Communications",
    val asn: String = "AS2914",
    val timestamp: Long = System.currentTimeMillis(),
    val isActive: Boolean = false,
    val notes: String = ""
)
