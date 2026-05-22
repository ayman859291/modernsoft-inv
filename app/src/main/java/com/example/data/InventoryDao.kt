package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    // --- App Settings ---
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<SettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingsEntity)


    // --- PARTS Cached ---
    @Query("SELECT * FROM parts_cache ORDER BY part_no ASC")
    fun getAllPartsFlow(): Flow<List<PartEntity>>

    @Query("SELECT * FROM parts_cache WHERE part_no = :partNo LIMIT 1")
    suspend fun getPartByNo(partNo: String): PartEntity?

    @Query("SELECT * FROM parts_cache WHERE barcode = :barcode LIMIT 1")
    suspend fun getPartByBarcode(barcode: String): PartEntity?

    @Query("SELECT * FROM parts_cache WHERE part_name LIKE :keyword OR part_no LIKE :keyword OR barcode LIKE :keyword")
    fun searchPartsFlow(keyword: String): Flow<List<PartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParts(parts: List<PartEntity>)

    @Query("DELETE FROM parts_cache")
    suspend fun clearPartsCache()

    @Query("SELECT COUNT(*) FROM parts_cache")
    suspend fun getPartsCount(): Int


    // --- Local START_PARTS (Opening Balances) ---
    @Query("SELECT * FROM start_parts_local ORDER BY id DESC")
    fun getStartPartsFlow(): Flow<List<StartPartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStartPart(startPart: StartPartEntity)

    @Query("DELETE FROM start_parts_local WHERE id = :id")
    suspend fun deleteStartPartById(id: Int)

    @Query("DELETE FROM start_parts_local")
    suspend fun clearStartParts()

    @Update
    suspend fun updateStartPart(startPart: StartPartEntity)


    // --- Local CLOSE_PARTS (Year-End Closing) ---
    @Query("SELECT * FROM close_parts_local ORDER BY id DESC")
    fun getClosePartsFlow(): Flow<List<ClosePartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClosePart(closePart: ClosePartEntity)

    @Query("DELETE FROM close_parts_local WHERE id = :id")
    suspend fun deleteClosePartById(id: Int)

    @Query("DELETE FROM close_parts_local")
    suspend fun clearCloseParts()

    @Update
    suspend fun updateClosePart(closePart: ClosePartEntity)
}
