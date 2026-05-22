package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ActiveScreen {
    OracleSettings,
    OpeningBalance,
    YearEndClosing,
    SqlReference
}

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val dao = database.inventoryDao()
    private val repository = InventoryRepository(dao)

    // Screen State
    private val _activeScreen = MutableStateFlow(ActiveScreen.OracleSettings)
    val activeScreen: StateFlow<ActiveScreen> = _activeScreen.asStateFlow()

    // Configuration/Settings State
    val settingsState: StateFlow<SettingsEntity> = repository.settingsFlow
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsEntity()
        )

    // Cached Parts flow
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchedParts: StateFlow<List<PartEntity>> = _searchQuery
        .debounce(200)
        .flatMapLatest { query ->
            repository.searchParts(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allStartParts: StateFlow<List<StartPartEntity>> = repository.startParts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCloseParts: StateFlow<List<ClosePartEntity>> = repository.closeParts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Connection Sync loading state
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow("")
    val syncMessage: StateFlow<String> = _syncMessage.asStateFlow()

    // Active Inventory Selection Draft Form (Opening Balance or Close)
    private val _selectedPart = MutableStateFlow<PartEntity?>(null)
    val selectedPart: StateFlow<PartEntity?> = _selectedPart.asStateFlow()

    // Form inputs variables
    val inputQty = MutableStateFlow("1")
    val inputPrice = MutableStateFlow("0.0")
    val inputCost = MutableStateFlow("0.0")
    val inputBatchNo = MutableStateFlow("BATCH-01")
    val inputExpDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val inputCompQty = MutableStateFlow("5") // Defaults for Close Parts

    // Export generation strings
    private val _exportedSql = MutableStateFlow("")
    val exportedSql: StateFlow<String> = _exportedSql.asStateFlow()

    init {
        // Initialize default settings record if DB is empty
        viewModelScope.launch {
            val current = repository.getSettings()
            if (current == null) {
                repository.saveSettings(SettingsEntity())
            }
        }
    }

    fun setScreen(screen: ActiveScreen) {
        _activeScreen.value = screen
        _selectedPart.value = null // Reset selection on screen switch
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Connect and Sync simulated files
    fun connectAndSyncOracle() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "جاري محاولة الاتصال بخادم أوراكل..."
            
            val currentSettings = repository.getSettings() ?: SettingsEntity()
            val result = repository.simulateOracleSync(currentSettings)
            
            if (result.isSuccess) {
                _syncMessage.value = "تم الاتصال بسيرفر أوراكل بنجاح وسحب عدد ${result.getOrNull()} منتجًا!"
            } else {
                _syncMessage.value = "فشل الاتصال: يرجى التحقق من صحة عنوان الآي بي والمنفذ في الشبكة."
            }
            _isSyncing.value = false
        }
    }

    fun selectPartForEntry(part: PartEntity) {
        _selectedPart.value = part
        inputPrice.value = part.price.toString()
        inputCost.value = part.cost.toString()
        // Generate random realistic computer inventory quantity for year-end calculations
        val fakeCompQty = ((part.price * 3) % 20 + 3).toInt()
        inputCompQty.value = fakeCompQty.toString()
    }

    fun updateSettings(
        host: String,
        port: String,
        sid: String,
        username: String,
        pass: String,
        branch: String,
        depot: String,
        comp: String,
        user: Int
    ) {
        viewModelScope.launch {
            val current = repository.getSettings() ?: SettingsEntity()
            val fresh = current.copy(
                oracleHost = host,
                oraclePort = port,
                oracleSidOrServiceName = sid,
                oracleUsername = username,
                oraclePassword = pass,
                branchNo = branch,
                depotNo = depot,
                compNo = comp,
                userNo = user
            )
            repository.saveSettings(fresh)
        }
    }

    // Add to Local Draft- Opening Balance
    fun addOpeningInventoryEntry() {
        val part = _selectedPart.value ?: return
        val qty = inputQty.value.toDoubleOrNull() ?: 1.0
        val price = inputPrice.value.toDoubleOrNull() ?: part.price
        val cost = inputCost.value.toDoubleOrNull() ?: part.cost
        val batch = inputBatchNo.value.ifBlank { "BATCH-DEFAULT" }
        val exp = inputExpDate.value

        viewModelScope.launch {
            val settings = repository.getSettings() ?: SettingsEntity()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val entry = StartPartEntity(
                part_no = part.part_no,
                part_name = part.part_name ?: "غير معروف",
                comp_no = settings.compNo,
                branch_no = settings.branchNo,
                depot_no = settings.depotNo,
                balance = qty,
                price = price,
                cost = cost,
                batch_no = batch,
                exp_date = exp,
                st_date = sdf.format(Date()),
                user_no = settings.userNo,
                year_no = settings.yearNo,
                month_no = settings.monthNo,
                unit_no = part.unit_no
            )
            repository.insertStartPart(entry)
            _selectedPart.value = null // reset selection
            inputQty.value = "1"
        }
    }

    // Add to Local Draft - Year-End Closing
    fun addCloseInventoryEntry() {
        val part = _selectedPart.value ?: return
        val qtyActual = inputQty.value.toDoubleOrNull() ?: 1.0
        val qtyComputer = inputCompQty.value.toDoubleOrNull() ?: 0.0
        val price = inputPrice.value.toDoubleOrNull() ?: part.price
        val cost = inputCost.value.toDoubleOrNull() ?: part.cost
        val batch = inputBatchNo.value.ifBlank { "BATCH-DEFAULT" }
        val exp = inputExpDate.value

        viewModelScope.launch {
            val settings = repository.getSettings() ?: SettingsEntity()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val entry = ClosePartEntity(
                branch_no = settings.branchNo,
                depot_no = settings.depotNo,
                part_no = part.part_no,
                part_name = part.part_name ?: "غير معروف",
                barcode = part.barcode,
                comp_no = settings.compNo,
                batch_no = batch,
                exp_date = exp,
                comp_qty = qtyComputer,
                act_qty = qtyActual,
                deferent = qtyActual - qtyComputer,
                price = price,
                cost = cost,
                inv_date = sdf.format(Date()),
                user_no = settings.userNo,
                unit_no = part.unit_no,
                refill = part.qtyrefill
            )
            repository.insertClosePart(entry)
            _selectedPart.value = null // reset selection
            inputQty.value = "1"
        }
    }

    fun deleteStartEntry(entry: StartPartEntity) {
        viewModelScope.launch {
            repository.deleteStartPartById(entry.id)
        }
    }

    fun deleteCloseEntry(entry: ClosePartEntity) {
        viewModelScope.launch {
            repository.deleteClosePartById(entry.id)
        }
    }

    fun clearStartEntries() {
        viewModelScope.launch {
            repository.clearStartParts()
            _exportedSql.value = ""
        }
    }

    fun clearCloseEntries() {
        viewModelScope.launch {
            repository.clearCloseParts()
            _exportedSql.value = ""
        }
    }

    // Generate export DDL and dynamic INSERT scripts
    fun exportOpeningBalanceSql() {
        viewModelScope.launch {
            val currentRecords = allStartParts.value
            val sql = repository.generateStartPartsSql(currentRecords)
            _exportedSql.value = sql

            // Simulating push/export flag update
            currentRecords.forEach {
                repository.updateStartPart(it.copy(ok_send = "Y"))
            }
        }
    }

    fun exportYearEndSql() {
        viewModelScope.launch {
            val currentRecords = allCloseParts.value
            val sql = repository.generateClosePartsSql(currentRecords)
            _exportedSql.value = sql

            // Simulating push/export flag update
            currentRecords.forEach {
                repository.updateClosePart(it.copy(ok_send = "Y"))
            }
        }
    }

    fun getIntermediateDDL(): String {
        return repository.getDdlsToCreateIntermediates()
    }
}
