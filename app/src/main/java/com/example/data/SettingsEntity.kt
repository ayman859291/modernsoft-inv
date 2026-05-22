package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    // Database connectivity info
    val oracleHost: String = "192.168.1.100",
    val oraclePort: String = "1521",
    val oracleSidOrServiceName: String = "xe",
    val oracleUsername: String = "SYSTEM",
    val oraclePassword: String = "modernsoft",
    // Connection state
    val isConnected: Boolean = false,
    val lastSyncTime: String = "لم يتم المزامنة بعد",
    // Shared common fields mapping automatically to each part
    val branchNo: String = "01",
    val depotNo: String = "05",
    val compNo: String = "0001",
    val userNo: Int = 101,
    val coinsNo: String = "01",
    val yearNo: Int = 2026,
    val monthNo: Int = 5
)
