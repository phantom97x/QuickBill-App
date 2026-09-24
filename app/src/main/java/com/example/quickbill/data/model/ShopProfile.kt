package com.example.quickbill.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shops")
data class ShopProfile(
    @PrimaryKey val phone: String, // Normalized 10-digit phone
    val shopName: String,
    val passwordHash: String,
    val ownerName: String = "",
    val address: String = "",
    val gstin: String = "",
    val currencySymbol: String = "₹",
    val createdAt: Long = System.currentTimeMillis()
)
