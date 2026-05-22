package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "close_parts_local")
data class ClosePartEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ref_no: Int = 0,
    val type_op: String = "YEAR_END_INV",
    val branch_no: String,
    val depot_no: String,
    val part_no: String,
    val part_name: String, // Cached for display convenience
    val barcode: String?,
    val comp_no: String,
    val batch_no: String,
    val exp_date: String,
    val comp_qty: Double = 0.0, // Computer quantity
    val act_qty: Double = 0.0,  // Actual quantity counted
    val deferent: Double = 0.0, // deferent = act_qty - comp_qty
    val price: Double = 0.0,
    val cost: Double = 0.0,
    val inv_date: String,
    val ok_send: String = "N", // N mean not exported, Y mean exported
    val user_no: Int = 1,
    val equivalent: Double = 0.0,
    val journal_no: Long = 0,
    val reference_no: String = "",
    val unit_no: Int = 1,
    val refill: Double = 1.0,
    val page_no: Int = 1,
    val min_price: Double = 0.0,
    val max_price: Double = 0.0,
    val seq: Int = 1,
    val journaltrans: String = "",
    val trans: String = "N"
)
