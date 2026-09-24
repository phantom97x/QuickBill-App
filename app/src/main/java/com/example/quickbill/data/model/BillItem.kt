package com.example.quickbill.data.model

data class BillItem(
    val itemNo: Int,
    val name: String,
    val qty: Double,
    val unit: String,
    val price: Double,
    val lineTotal: Double
)
