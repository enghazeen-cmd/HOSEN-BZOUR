package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zones")
data class Zone(
    @PrimaryKey val id: String, // 1-7
    val name: String,
    val totalCustomers: Int,
    val averageConsumption: Double,
    val leakageAlertCount: Int
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey val id: String,
    val nameAr: String,
    val nameEn: String,
    val meterNumber: String,
    val phone: String,
    val address: String,
    val zoneId: String, // 1-7
    val gpsLat: Double,
    val gpsLng: Double,
    val status: String, // "active" | "inactive" | "suspended"
    val lastReadingValue: Double,
    val installationDate: Long,
    val qrCodeUrl: String
)

@Entity(tableName = "readings")
data class Reading(
    @PrimaryKey val id: String,
    val customerId: String,
    val readerId: String,
    val value: Double,
    val previousValue: Double,
    val consumption: Double,
    val imageUrl: String,
    val gpsLat: Double,
    val gpsLng: Double,
    val timestamp: Long,
    val status: String, // "normal" | "abnormal" | "leakage_suspected"
    val ocrConfidence: Double
)
