package com.example.quickbill.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bills",
    indices = [
        Index(value = ["shopPhone", "billNo"], unique = true),
        Index(value = ["shopPhone", "dateKey"]),
        Index(value = ["shopPhone", "monthKey"])
    ]
)
data class Bill(
    @PrimaryKey val billId: String,
    val shopPhone: String,
    val billNo: Long,
    val customerName: String,
    val customerPhone: String,
    val items: List<BillItem>,
    val total: Double,
    val createdAt: Long,
    val dateKey: String,   // YYYY-MM-DD
    val monthKey: String   // YYYY-MM
)
