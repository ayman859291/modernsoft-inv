package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "start_parts_local")
data class StartPartEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val part_no: String,
    val part_name: String, // Cached for display convenience
    val branch_no: String,
    val comp_no: String,
    val balance: Double, // This equals actual physical counted quantity
    val depot_no: String,
    val price: Double,
    val cost: Double,
    val batch_no: String,
    val exp_date: String, // String representation format (e.g. YYYY-MM-DD or DD/MM/YYYY)
    val month_no: Int = 1,
    val year_no: Int = 2026,
    val st_date: String, // Count entry timestamp / system date
    val user_no: Int = 1,
    val coins_no: String = "01",
    val unit_no: Int = 1,
    val refill: Double = 1.0,
    val equivalent: Double = 0.0,
    val inv_no: Int = 0,
    val trans: String = "N",
    val ok_send: String = "N" // N mean not exported, Y mean exported
)
