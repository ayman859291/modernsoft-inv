package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parts_cache")
data class PartEntity(
    @PrimaryKey val part_no: String,
    val part_name: String?,
    val barcode: String?,
    val group_no: String?,
    val store_no: String?,
    val price: Double = 0.0,
    val cost: Double = 0.0,
    val unit_no: Int = 1,
    val active: Int = 1,
    val qtyrefill: Double = 1.0,
    val e_name: String? = null
)
