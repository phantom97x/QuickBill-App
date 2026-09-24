package com.example.quickbill.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meta_counters")
data class MetaCounter(
    @PrimaryKey val id: String, // e.g. "billSeq_${shopPhone}"
    val counterValue: Long
)
