package com.example.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InventoryRepository(private val dao: InventoryDao) {

    // Settings
    val settingsFlow: Flow<SettingsEntity?> = dao.getSettingsFlow()

    suspend fun getSettings(): SettingsEntity? = dao.getSettings()

    suspend fun saveSettings(settings: SettingsEntity) {
        dao.insertSettings(settings)
    }

    // Cached Parts
    val cachedParts: Flow<List<PartEntity>> = dao.getAllPartsFlow()

    fun searchParts(query: String): Flow<List<PartEntity>> {
        return if (query.isBlank()) {
            dao.getAllPartsFlow()
        } else {
            dao.searchPartsFlow("%$query%")
        }
    }

    suspend fun getPartByNo(partNo: String): PartEntity? {
        return dao.getPartByNo(partNo)
    }

    suspend fun getPartByBarcode(barcode: String): PartEntity? {
        return dao.getPartByBarcode(barcode)
    }

    suspend fun getPartsCount(): Int = dao.getPartsCount()

    // START_PARTS (Opening Balances)
    val startParts: Flow<List<StartPartEntity>> = dao.getStartPartsFlow()

    suspend fun insertStartPart(startPart: StartPartEntity) {
        dao.insertStartPart(startPart)
    }

    suspend fun deleteStartPartById(id: Int) {
        dao.deleteStartPartById(id)
    }

    suspend fun clearStartParts() {
        dao.clearStartParts()
    }

    suspend fun updateStartPart(startPart: StartPartEntity) {
        dao.updateStartPart(startPart)
    }


    // CLOSE_PARTS (Year-End Closing)
    val closeParts: Flow<List<ClosePartEntity>> = dao.getClosePartsFlow()

    suspend fun insertClosePart(closePart: ClosePartEntity) {
        dao.insertClosePart(closePart)
    }

    suspend fun deleteClosePartById(id: Int) {
        dao.deleteClosePartById(id)
    }

    suspend fun clearCloseParts() {
        dao.clearCloseParts()
    }

    suspend fun updateClosePart(closePart: ClosePartEntity) {
        dao.updateClosePart(closePart)
    }


    // --- Oracle Connection & Data Pull Simulation ---
    suspend fun simulateOracleSync(settings: SettingsEntity): Result<Int> {
        return try {
            // Simulate networking delay for Oracle handshake/Query execution
            delay(1800)

            // Seed initial realistic medical/retail entries
            val seedParts = listOf(
                PartEntity("P000001", "بندول إكسترا 50 قرص - Panadol Extra", "6281101230012", "1001", "01", 24.50, 18.0, 1, 1),
                PartEntity("P000002", "أوميز 20 مجم 14 كبسولة حارق للمعدة - Omez 20mg", "6281101230029", "1001", "01", 43.00, 32.5, 1, 1),
                PartEntity("P000003", "أوجمنتين مضاد حيوي 1 جم 14 قرص - Augmentin", "6281101230036", "1001", "01", 97.00, 75.0, 1, 1),
                PartEntity("P000004", "فلاجيل مطهر معوي 500 مجم - Flagyl 500mg", "6281101230043", "1001", "01", 15.00, 11.2, 1, 1),
                PartEntity("P000005", "بروفين مسكن ألام 400 مجم 24 قرص - Brufen", "6281101230050", "1002", "01", 19.00, 14.0, 1, 1),
                PartEntity("P000006", "شراب فيفادول خافض حرارة للأطفال 100 مل - Fevadol", "6281101230067", "1002", "01", 10.50, 8.0, 1, 1),
                PartEntity("P000007", "سولبادين فوار مسكن قوي 20 قرص - Solpadeine", "6281101230074", "1002", "01", 18.00, 13.5, 1, 1),
                PartEntity("P000008", "ميبو مرهم حروق شهير 40 جرام - Mebo Ointment", "6281101230081", "1003", "01", 78.00, 60.0, 1, 1),
                PartEntity("P000009", "قطرة معقمة ومرطبة للعين ريفريش - Refresh Tears", "6281101230098", "1003", "01", 33.00, 25.0, 1, 1),
                PartEntity("P000010", "حليب أطفال نيدو مجفف غني بالدسم 900 جرام", "6281101230104", "1004", "02", 110.00, 92.0, 1, 1),
                PartEntity("P000011", "شراب كافينول مهدئ للسعال 120 مل", "6281101230111", "1002", "01", 16.50, 12.0, 1, 1),
                PartEntity("P000012", "بنادول نايت للمساعدة على النوم 20 قرص - Panadol Night", "6281101230128", "1001", "01", 22.00, 16.8, 1, 1),
                PartEntity("P000013", "شراب جافيسكون لعلاج الحموضة 150 مل - Gaviscon", "6281101230135", "1001", "01", 31.50, 24.0, 1, 1),
                PartEntity("P000014", "كريم فوسيدين مضاد حيوي جلدي 30 جرام - Fucidin", "6281101230142", "1003", "01", 28.00, 21.0, 1, 1),
                PartEntity("P000015", "علبة شاش معقم لاصق للجروح 100 حبة", "6281101230159", "1004", "02", 15.00, 12.0, 1, 1)
            )

            // Overwrite database cached parts with the newly refreshed Oracle set
            dao.clearPartsCache()
            dao.insertParts(seedParts)

            // Save settings connected status
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateStr = sdf.format(Date())
            val updatedSettings = settings.copy(
                isConnected = true,
                lastSyncTime = dateStr
            )
            dao.insertSettings(updatedSettings)

            Result.success(seedParts.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper functions to generate Export SQL
    fun generateStartPartsSql(records: List<StartPartEntity>): String {
        if (records.isEmpty()) return "-- لا يوجد أرصدة افتتاحية للتصدير"
        val sb = java.lang.StringBuilder()
        sb.append("-- جدول وسيط أوراكل للأرصدة الافتتاحية المضافة من الجوال\n")
        sb.append("-- عدد السجلات: ${records.size}\n")
        sb.append("-- تاريخ التوليد: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n\n")

        for (rec in records) {
            sb.append("INSERT INTO START_PARTS (")
            sb.append("part_no, branch_no, comp_no, balance, depot_no, price, cost, batch_no, exp_date, month_no, year_no, st_date, user_no, coins_no, unit_no, refill, equivalent, inv_no, trans, ok_send")
            sb.append(") VALUES (\n")
            sb.append("  '${rec.part_no.padEnd(7).take(7)}', ")
            sb.append("'${rec.branch_no.take(2)}', ")
            sb.append("'${rec.comp_no.take(4)}', ")
            sb.append("${rec.balance}, ")
            sb.append("'${rec.depot_no.take(5)}', ")
            sb.append("${rec.price}, ")
            sb.append("${rec.cost}, ")
            sb.append("'${rec.batch_no.replace("'", "''")}', ")
            if (rec.exp_date.isNotBlank()) {
                sb.append("TO_DATE('${rec.exp_date}', 'YYYY-MM-DD'), ")
            } else {
                sb.append("NULL, ")
            }
            sb.append("${rec.month_no}, ")
            sb.append("${rec.year_no}, ")
            sb.append("TO_DATE('${rec.st_date.take(10)}', 'YYYY-MM-DD'), ")
            sb.append("${rec.user_no}, ")
            sb.append("'${rec.coins_no.take(2)}', ")
            sb.append("${rec.unit_no}, ")
            sb.append("${rec.refill}, ")
            sb.append("${rec.equivalent}, ")
            sb.append("${rec.inv_no}, ")
            sb.append("'Y', 'N'") // trans='Y', ok_send='N' standard configuration for draft Oracle validation
            sb.append("\n);\n")
        }
        sb.append("\nCOMMIT;\n")
        return sb.toString()
    }

    fun generateClosePartsSql(records: List<ClosePartEntity>): String {
        if (records.isEmpty()) return "-- لا يوجد بيانات جرد نهاية السنة للتصدير"
        val sb = java.lang.StringBuilder()
        sb.append("-- جدول وسيط أوراكل لجرد نهاية السنة المضاف من الجوال\n")
        sb.append("-- عدد السجلات: ${records.size}\n")
        sb.append("-- تاريخ التوليد: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n\n")

        for (rec in records) {
            sb.append("INSERT INTO CLOSE_PARTS (")
            sb.append("ref_no, type_op, branch_no, depot_no, part_no, comp_no, batch_no, exp_date, comp_qty, act_qty, deferent, price, cost, inv_date, ok_send, user_no, equivalent, journal_no, reference_no, unit_no, refill, page_no, min_price, max_price, seq, journaltrans, trans")
            sb.append(") VALUES (\n")
            sb.append("  ${rec.ref_no}, ")
            sb.append("'YEAR_END_INV', ")
            sb.append("'${rec.branch_no.take(2)}', ")
            sb.append("'${rec.depot_no.take(5)}', ")
            sb.append("'${rec.part_no.padEnd(7).take(7)}', ")
            sb.append("'${rec.comp_no.take(4)}', ")
            sb.append("'${rec.batch_no.replace("'", "''")}', ")
            if (rec.exp_date.isNotBlank()) {
                sb.append("TO_DATE('${rec.exp_date}', 'YYYY-MM-DD'), ")
            } else {
                sb.append("NULL, ")
            }
            sb.append("${rec.comp_qty}, ")
            sb.append("${rec.act_qty}, ")
            sb.append("${rec.deferent}, ")
            sb.append("${rec.price}, ")
            sb.append("${rec.cost}, ")
            sb.append("TO_DATE('${rec.inv_date.take(10)}', 'YYYY-MM-DD'), ")
            sb.append("'N', ") // ok_send defaults N
            sb.append("${rec.user_no}, ")
            sb.append("${rec.equivalent}, ")
            sb.append("${rec.journal_no}, ")
            sb.append("'${rec.reference_no}', ")
            sb.append("${rec.unit_no}, ")
            sb.append("${rec.refill}, ")
            sb.append("${rec.page_no}, ")
            sb.append("${rec.min_price}, ")
            sb.append("${rec.max_price}, ")
            sb.append("${rec.seq}, ")
            sb.append("'${rec.journaltrans}', ")
            sb.append("'${rec.trans}'")
            sb.append("\n);\n")
        }
        sb.append("\nCOMMIT;\n")
        return sb.toString()
    }

    // Export query helper scripts
    fun getDdlsToCreateIntermediates(): String {
        return """
-- ========================================================
-- 1. جدول أوراكل الوسيط للأرصدة الافتتاحية (MOBILE_START_PARTS)
-- تماثل الهيكل مع START_PARTS لتصدير آمن للمراجعة والترحيل
-- ========================================================
CREATE TABLE MOBILE_START_PARTS (
  id         NUMBER(10) GENERATED BY DEFAULT ON NULL AS IDENTITY,
  part_no    CHAR(7) NOT NULL,
  branch_no  CHAR(2) NOT NULL,
  comp_no    CHAR(4) NOT NULL,
  balance    NUMBER(12,3) DEFAULT 0,
  depot_no   CHAR(5) NOT NULL,
  price      NUMBER(12,2) DEFAULT 0,
  cost       NUMBER(14,7) DEFAULT 0,
  batch_no   VARCHAR2(15),
  exp_date   DATE,
  month_no   NUMBER(2) DEFAULT 1,
  year_no    NUMBER(4) DEFAULT 2026,
  st_date    DATE DEFAULT SYSDATE,
  user_no    NUMBER(5),
  coins_no   CHAR(2) DEFAULT '01',
  unit_no    NUMBER(3),
  refill     NUMBER(10,2) DEFAULT 1,
  equivalent NUMBER(17,7) DEFAULT 0,
  inv_no     NUMBER(9) DEFAULT 0,
  trans      CHAR(1) DEFAULT 'N',
  ok_send    CHAR(1) DEFAULT 'N',
  CONSTRAINT pk_mobile_start PRIMARY KEY (id)
);

-- ========================================================
-- 2. جدول أوراكل الوسيط لجرد نهاية السنة (MOBILE_CLOSE_PARTS)
-- لمراجعة فوارق الجرد الفعلي والترحيل للنظام الأساسي
-- ========================================================
CREATE TABLE MOBILE_CLOSE_PARTS (
  id           NUMBER(10) GENERATED BY DEFAULT ON NULL AS IDENTITY,
  ref_no       NUMBER(10) DEFAULT 0,
  type_op      VARCHAR2(20) DEFAULT 'YEAR_END_INV',
  branch_no    CHAR(2) NOT NULL,
  depot_no     CHAR(5) NOT NULL,
  part_no      CHAR(7) NOT NULL,
  comp_no      CHAR(4) NOT NULL,
  batch_no     VARCHAR2(15),
  exp_date     DATE,
  comp_qty     NUMBER(12,3) DEFAULT 0,
  act_qty      NUMBER(12,3) DEFAULT 0,
  deferent     NUMBER(17,6) DEFAULT 0,
  price        NUMBER(12,2) DEFAULT 0,
  cost         NUMBER(14,7) DEFAULT 0,
  inv_date     DATE DEFAULT SYSDATE,
  ok_send      CHAR(1) DEFAULT 'N',
  user_no      NUMBER(5),
  equivalent   NUMBER(17,7) DEFAULT 0,
  journal_no   NUMBER(15),
  reference_no CHAR(13),
  unit_no      NUMBER(3),
  refill       NUMBER(10,2) DEFAULT 1,
  page_no      NUMBER(4) DEFAULT 1,
  min_price    NUMBER(12,2) DEFAULT 0,
  max_price    NUMBER(12,2) DEFAULT 0,
  seq          NUMBER(6) DEFAULT 1,
  journaltrans VARCHAR2(30),
  trans        CHAR(1) DEFAULT 'N',
  CONSTRAINT pk_mobile_close PRIMARY KEY (id)
);

-- ========================================================
-- مثال على استعلام الترحيل الفني النهائي من الوسيط إلى الأساسي
-- ========================================================
/*
-- 1. ترحيل الرصيد الإفتتاحي
INSERT INTO START_PARTS 
SELECT part_no, branch_no, comp_no, balance, depot_no, price, cost, batch_no, exp_date, month_no, year_no, st_date, user_no, coins_no, unit_no, refill, equivalent, inv_no, trans, 'Y' 
FROM MOBILE_START_PARTS 
WHERE ok_send = 'N';

-- 2. ترحيل جرد نهاية السنة
INSERT INTO CLOSE_PARTS 
SELECT ref_no, type_op, branch_no, depot_no, part_no, comp_no, batch_no, exp_date, comp_qty, act_qty, deferent, price, cost, inv_date, 'Y', user_no, equivalent, journal_no, reference_no, unit_no, refill, page_no, min_price, max_price, seq, journaltrans, trans 
FROM MOBILE_CLOSE_PARTS 
WHERE ok_send = 'N';
*/
        """.trimIndent()
    }
}
