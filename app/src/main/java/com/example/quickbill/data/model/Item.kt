package com.example.quickbill.data.model

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "items",
    primaryKeys = ["shopPhone", "itemNo"],
    indices = [
        Index(value = ["shopPhone", "nameLower"], unique = true)
    ]
)
data class Item(
    val itemNo: Int, // Item number (1, 2, 3...)
    val shopPhone: String,
    val name: String,
    val nameLower: String,
    val price: Double,
    val quantity: Double,
    val lowStockLimit: Double = 10.0,
    val unit: String = "pcs",
    val category: String = "General",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = quantity <= lowStockLimit
}
