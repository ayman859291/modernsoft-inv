package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ClosePartEntity
import com.example.data.PartEntity
import com.example.data.SettingsEntity
import com.example.data.StartPartEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryApp(viewModel: InventoryViewModel = viewModel()) {
    val activeScreen by viewModel.activeScreen.collectAsState()
    val settings by viewModel.settingsState.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val partsCount by viewModel.searchedParts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showBarcodeDialog by remember { mutableStateOf(false) }
    var exportDialogTitle by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf(false) }

    // Display Arabic/English header with Frosted Glass layout
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ModernSoftPrimaryCyan,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "MS",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Column {
                            Text(
                                "ModernSoft",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ModernSoftPrimaryCyan,
                                    lineHeight = 16.sp
                                )
                            )
                            Text(
                                "INVENTORY MANAGEMENT",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ModernSoftSecondaryCyan.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Glass Connection status pill matching HTML requirements: bg-white/60 border border-white
                    val pillColor = if (settings.isConnected) SuccessSoftGreen else ErrorOrangeRed
                    val pillText = if (settings.isConnected) "Oracle: ${settings.oracleHost}" else "غير متصل"

                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = Color.White.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, Color.White),
                        shadowElevation = 1.dp,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(pillColor, RoundedCornerShape(50.dp))
                            )
                            Text(
                                pillText,
                                fontSize = 11.sp,
                                color = TextDark.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = ModernSoftPrimaryCyan
                )
            )
        },
        bottomBar = {
            // White and Cyan interactive bottom menu bar with Frosted overlay
            NavigationBar(
                containerColor = Color.White.copy(alpha = 0.75f),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                NavigationBarItem(
                    selected = activeScreen == ActiveScreen.OracleSettings,
                    onClick = { viewModel.setScreen(ActiveScreen.OracleSettings) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "قاعدة البيانات") },
                    label = { Text("الاتصال والمزامنة", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ModernSoftPrimaryCyan,
                        selectedTextColor = ModernSoftPrimaryCyan,
                        indicatorColor = GlassWhite80,
                        unselectedIconColor = TextLightGrey,
                        unselectedTextColor = TextLightGrey
                    )
                )

                NavigationBarItem(
                    selected = activeScreen == ActiveScreen.OpeningBalance,
                    onClick = { viewModel.setScreen(ActiveScreen.OpeningBalance) },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "الرصيد الافتتاحي") },
                    label = { Text("أرصدة افتتاحية", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ModernSoftPrimaryCyan,
                        selectedTextColor = ModernSoftPrimaryCyan,
                        indicatorColor = GlassWhite80,
                        unselectedIconColor = TextLightGrey,
                        unselectedTextColor = TextLightGrey
                    )
                )

                NavigationBarItem(
                    selected = activeScreen == ActiveScreen.YearEndClosing,
                    onClick = { viewModel.setScreen(ActiveScreen.YearEndClosing) },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "جرد نهاية السنة") },
                    label = { Text("جرد نهاية السنة", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ModernSoftPrimaryCyan,
                        selectedTextColor = ModernSoftPrimaryCyan,
                        indicatorColor = GlassWhite80,
                        unselectedIconColor = TextLightGrey,
                        unselectedTextColor = TextLightGrey
                    )
                )

                NavigationBarItem(
                    selected = activeScreen == ActiveScreen.SqlReference,
                    onClick = { viewModel.setScreen(ActiveScreen.SqlReference) },
                    icon = { Icon(Icons.Default.Code, contentDescription = "استعلامات SQL") },
                    label = { Text("جداول أوراكل", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ModernSoftPrimaryCyan,
                        selectedTextColor = ModernSoftPrimaryCyan,
                        indicatorColor = GlassWhite80,
                        unselectedIconColor = TextLightGrey,
                        unselectedTextColor = TextLightGrey
                    )
                )
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE0F2FE), // Sky-100 Light Base
                            Color(0xFFF0F9FF)  // Sky-50 Light Tint
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            when (activeScreen) {
                ActiveScreen.OracleSettings -> OracleSettingsScreen(
                    viewModel = viewModel,
                    isSyncing = isSyncing,
                    syncMessage = syncMessage
                )
                ActiveScreen.OpeningBalance -> OpeningBalanceScreen(
                    viewModel = viewModel,
                    onOpenScanner = { showBarcodeDialog = true },
                    onShowExport = {
                        viewModel.exportOpeningBalanceSql()
                        exportDialogTitle = "تصدير الأرصدة الافتتاحية (START_PARTS)"
                        showExportDialog = true
                    }
                )
                ActiveScreen.YearEndClosing -> YearEndClosingScreen(
                    viewModel = viewModel,
                    onOpenScanner = { showBarcodeDialog = true },
                    onShowExport = {
                        viewModel.exportYearEndSql()
                        exportDialogTitle = "تصدير جرد نهاية السنة (CLOSE_PARTS)"
                        showExportDialog = true
                    }
                )
                ActiveScreen.SqlReference -> SqlReferenceScreen(
                    viewModel = viewModel
                )
            }

            // Real-time Barcode Camera Simulation Dialog
            if (showBarcodeDialog) {
                BarcodeScannerSimulationDialog(
                    viewModel = viewModel,
                    onDismiss = { showBarcodeDialog = false }
                )
            }

            // Copyable Export Script Bottom Dialog
            if (showExportDialog) {
                ExportScriptDialog(
                    title = exportDialogTitle,
                    viewModel = viewModel,
                    onDismiss = { showExportDialog = false }
                )
            }
        }
    }
}

// 1. Connection settings and simulated parts search
@Composable
fun OracleSettingsScreen(
    viewModel: InventoryViewModel,
    isSyncing: Boolean,
    syncMessage: String
) {
    val settings by viewModel.settingsState.collectAsState()
    val parts by viewModel.searchedParts.collectAsState()
    val query by viewModel.searchQuery.collectAsState()

    // Host parameters input states
    var ipHost by remember(settings) { mutableStateOf(settings.oracleHost) }
    var port by remember(settings) { mutableStateOf(settings.oraclePort) }
    var sid by remember(settings) { mutableStateOf(settings.oracleSidOrServiceName) }
    var username by remember(settings) { mutableStateOf(settings.oracleUsername) }
    var password by remember(settings) { mutableStateOf(settings.oraclePassword) }

    // Shared global fields
    var branchNo by remember(settings) { mutableStateOf(settings.branchNo) }
    var depotNo by remember(settings) { mutableStateOf(settings.depotNo) }
    var compNo by remember(settings) { mutableStateOf(settings.compNo) }
    var userNo by remember(settings) { mutableStateOf(settings.userNo.toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header glass card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Cloud,
                        contentDescription = "اتصالات",
                        tint = ModernSoftPrimaryCyan,
                        modifier = Modifier.size(48.dp)
                    )
                    Column {
                        Text(
                            "ربط أوراكل والمزامنة",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            "قم بتهيئة الاتصال بجهاز خادم أوراكل المحلي ضمن الشبكة لسحب بيانات الأصناف النشطة.",
                            fontSize = 12.sp,
                            color = TextLightGrey,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Connection detail form card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "بيانات الاتصال بقاعدة البيانات أوراكل",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ModernSoftPrimaryCyan
                    )

                    OutlinedTextField(
                        value = ipHost,
                        onValueChange = { ipHost = it },
                        label = { Text("عنوان خادم الـ IP (مثال 192.168.1.50)", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Laptop, contentDescription = null, tint = ModernSoftPrimaryCyan) },
                        modifier = Modifier.fillMaxWidth().testTag("host_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                            focusedContainerColor = Color.White.copy(alpha = 0.95f),
                            unfocusedBorderColor = Color(0xFFCFFAFE),
                            focusedBorderColor = ModernSoftPrimaryCyan,
                            unfocusedLabelColor = TextLightGrey,
                            focusedLabelColor = ModernSoftPrimaryCyan,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = port,
                            onValueChange = { port = it },
                            label = { Text("المنفذ Port", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                focusedContainerColor = Color.White.copy(alpha = 0.95f),
                                unfocusedBorderColor = Color(0xFFCFFAFE),
                                focusedBorderColor = ModernSoftPrimaryCyan,
                                unfocusedLabelColor = TextLightGrey,
                                focusedLabelColor = ModernSoftPrimaryCyan,
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark
                            )
                        )
                        OutlinedTextField(
                            value = sid,
                            onValueChange = { sid = it },
                            label = { Text("الاسم SID / Service", fontSize = 11.sp) },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                focusedContainerColor = Color.White.copy(alpha = 0.95f),
                                unfocusedBorderColor = Color(0xFFCFFAFE),
                                focusedBorderColor = ModernSoftPrimaryCyan,
                                unfocusedLabelColor = TextLightGrey,
                                focusedLabelColor = ModernSoftPrimaryCyan,
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("اسم المستخدم", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                focusedContainerColor = Color.White.copy(alpha = 0.95f),
                                unfocusedBorderColor = Color(0xFFCFFAFE),
                                focusedBorderColor = ModernSoftPrimaryCyan,
                                unfocusedLabelColor = TextLightGrey,
                                focusedLabelColor = ModernSoftPrimaryCyan,
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark
                            )
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("كلمة المرور أوراكل", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                focusedContainerColor = Color.White.copy(alpha = 0.95f),
                                unfocusedBorderColor = Color(0xFFCFFAFE),
                                focusedBorderColor = ModernSoftPrimaryCyan,
                                unfocusedLabelColor = TextLightGrey,
                                focusedLabelColor = ModernSoftPrimaryCyan,
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark
                            )
                        )
                    }
                }
            }
        }

        // Shared default variables for inventory (البيانات المشتركة لكل الأصناف المجرودة)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = LightCyanGlow.copy(alpha = 0.4f),
                    contentColor = TextDark
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = ModernSoftPrimaryCyan)
                        Text(
                            "تعميم البيانات المشتركة (المستودع والموقع والشركة)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ModernSoftPrimaryCyan
                        )
                    }
                    Text(
                        "سيقوم التطبيق بتعميم هذه القيم المشتركة لجميع الأصناف المظروفة تلقائياً أثناء الجرد الميداني.",
                        fontSize = 11.sp,
                        color = TextLightGrey
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = branchNo,
                            onValueChange = { branchNo = it },
                            label = { Text("رقم الفرع", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                focusedContainerColor = Color.White.copy(alpha = 0.95f),
                                unfocusedBorderColor = Color(0xFFCFFAFE),
                                focusedBorderColor = ModernSoftPrimaryCyan,
                                unfocusedLabelColor = TextLightGrey,
                                focusedLabelColor = ModernSoftPrimaryCyan,
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark
                            )
                        )
                        OutlinedTextField(
                            value = depotNo,
                            onValueChange = { depotNo = it },
                            label = { Text("رقم المستودع", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                focusedContainerColor = Color.White.copy(alpha = 0.95f),
                                unfocusedBorderColor = Color(0xFFCFFAFE),
                                focusedBorderColor = ModernSoftPrimaryCyan,
                                unfocusedLabelColor = TextLightGrey,
                                focusedLabelColor = ModernSoftPrimaryCyan,
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark
                            )
                        )
                        OutlinedTextField(
                            value = compNo,
                            onValueChange = { compNo = it },
                            label = { Text("رقم الشركة", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                focusedContainerColor = Color.White.copy(alpha = 0.95f),
                                unfocusedBorderColor = Color(0xFFCFFAFE),
                                focusedBorderColor = ModernSoftPrimaryCyan,
                                unfocusedLabelColor = TextLightGrey,
                                focusedLabelColor = ModernSoftPrimaryCyan,
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = userNo,
                            onValueChange = { userNo = it },
                            label = { Text("رقم المستخدم الجاري المعين", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                focusedContainerColor = Color.White.copy(alpha = 0.95f),
                                unfocusedBorderColor = Color(0xFFCFFAFE),
                                focusedBorderColor = ModernSoftPrimaryCyan,
                                unfocusedLabelColor = TextLightGrey,
                                focusedLabelColor = ModernSoftPrimaryCyan,
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Buttons to save and sync
                    Button(
                        onClick = {
                            viewModel.updateSettings(
                                host = ipHost,
                                port = port,
                                sid = sid,
                                username = username,
                                pass = password,
                                branch = branchNo,
                                depot = depotNo,
                                comp = compNo,
                                user = userNo.toIntOrNull() ?: 101
                            )
                            viewModel.connectAndSyncOracle()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("sync_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ModernSoftSecondaryCyan),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("جاري الاتصال والتحقق...", fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("فحص الاتصال وسحب بيانات الأصناف", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Live connection result feedback toast simulation
        if (syncMessage.isNotEmpty()) {
            item {
                Surface(
                    color = LightCyanGlow.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ModernSoftPrimaryCyan.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        syncMessage,
                        color = TextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(14.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // List of cached imported items offline matching schema
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "كتالوج الأصناف المجرودة المسحوبة من أوراكل (${parts.size} صنف)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("بحث باسم الصنف، الباركود أو الكود المرجعي...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextLightGrey) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                        focusedContainerColor = Color.White.copy(alpha = 0.95f),
                        unfocusedBorderColor = Color(0xFFCFFAFE),
                        focusedBorderColor = ModernSoftPrimaryCyan,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        if (parts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CloudQueue, contentDescription = null, tint = TextLightGrey, modifier = Modifier.size(48.dp))
                        Text(
                            "لا يوجد أصناف في الكاش المحلي بعد.\nقم بالنقر على 'فحص الاتصال وسحب الأصناف' لمحاكاة ربط ومجامعة قاعدة بيانات أوراكل.",
                            fontSize = 12.sp,
                            color = TextLightGrey,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            items(parts) { part ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.65f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White),
                    modifier = Modifier.fillMaxWidth().clickable {
                        // Quick switch to Opening Balance counting screen when item clicked
                        viewModel.selectPartForEntry(part)
                        viewModel.setScreen(ActiveScreen.OpeningBalance)
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                part.part_name ?: "بدون اسم",
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1.5f)
                            )
                            Surface(
                                color = LightCyanGlow.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color.White)
                            ) {
                                Text(
                                    part.part_no,
                                    fontSize = 11.sp,
                                    color = ModernSoftPrimaryCyan,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = null, tint = TextLightGrey, modifier = Modifier.size(14.dp))
                                Text(
                                    "باركود: ${part.barcode ?: "بدون"}",
                                    fontSize = 12.sp,
                                    color = TextLightGrey
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    "السعر: ${part.price} ر.س",
                                    fontSize = 12.sp,
                                    color = ModernSoftPrimaryCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "التكلفة: ${part.cost} ر.س",
                                    fontSize = 12.sp,
                                    color = TextLightGrey
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2. Opening Balance Stocktaking Screen (الرصيد الافتتاحي - START_PARTS)
@Composable
fun OpeningBalanceScreen(
    viewModel: InventoryViewModel,
    onOpenScanner: () -> Unit,
    onShowExport: () -> Unit
) {
    val selectedPart by viewModel.selectedPart.collectAsState()
    val startDrafts by viewModel.allStartParts.collectAsState()
    val parts by viewModel.searchedParts.collectAsState()
    val query by viewModel.searchQuery.collectAsState()

    var showDropdown by remember { mutableStateOf(false) }
    val settings by viewModel.settingsState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 2-Column Branch and Warehouse status row (styled like glass components in HTML)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "الفرع الحالي",
                            color = ModernSoftPrimaryCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "فرع: ${settings.branchNo}",
                            color = TextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "المستودع",
                            color = ModernSoftPrimaryCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "مستودع: ${settings.depotNo}",
                            color = TextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Search & Barcode Scan Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "البحث عن صنف جرد الرصيد الإفتتاحي",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ModernSoftPrimaryCyan
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = {
                                viewModel.updateSearchQuery(it)
                                showDropdown = it.isNotEmpty()
                            },
                            placeholder = { Text("بحث عن الصنف أو مسح الباركود...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f).testTag("opening_search_input"),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextLightGrey) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                focusedContainerColor = Color.White.copy(alpha = 0.95f),
                                unfocusedBorderColor = Color(0xFFCFFAFE),
                                focusedBorderColor = ModernSoftPrimaryCyan,
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )

                        // Quick simulator barcode scan button
                        Button(
                            onClick = onOpenScanner,
                            colors = ButtonDefaults.buttonColors(containerColor = ModernSoftSecondaryCyan),
                            modifier = Modifier.height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "تصوير الباركود", tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مسح الباركود", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // Simulated Dropdown lookup suggestions result
                    if (showDropdown && parts.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, Color(0xFFCFFAFE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                parts.take(5).forEach { part ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectPartForEntry(part)
                                                showDropdown = false
                                                viewModel.updateSearchQuery("")
                                            }
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            part.part_name ?: "",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                            color = TextDark
                                        )
                                        Text(
                                            part.part_no,
                                            fontSize = 12.sp,
                                            color = ModernSoftPrimaryCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Entry details Form Card (When an item has been selected) - Matches capsule with rounded-[32px]
        selectedPart?.let { part ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.7f),
                        contentColor = TextDark
                    ),
                    shape = RoundedCornerShape(32.dp),
                    border = BorderStroke(1.5.dp, Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(ModernSoftSecondaryCyan, RoundedCornerShape(50.dp))
                                )
                                Text(
                                    "تعبئة بيانات الصنف الجاري جرده",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ModernSoftPrimaryCyan
                                )
                            }
                            IconButton(onClick = { viewModel.selectPartForEntry(part) }) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = ErrorOrangeRed)
                            }
                        }

                        // Product short summary spec card (Styled with glowing background)
                        Surface(
                            color = LightCyanGlow.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color(0xFFCFFAFE))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    part.part_name ?: "بدون اسم",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("معرف الصنف: ${part.part_no}", fontSize = 11.sp, color = TextLightGrey)
                                    Text("باركود: ${part.barcode ?: "لا يوجد"}", fontSize = 11.sp, color = TextLightGrey)
                                    Text("الوحدة: ${part.unit_no}", fontSize = 11.sp, color = TextLightGrey)
                                }
                            }
                        }

                        // Form input numerical boxes
                        val qty by viewModel.inputQty.collectAsState()
                        val price by viewModel.inputPrice.collectAsState()
                        val cost by viewModel.inputCost.collectAsState()
                        val batch by viewModel.inputBatchNo.collectAsState()
                        val expDate by viewModel.inputExpDate.collectAsState()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = qty,
                                onValueChange = { viewModel.inputQty.value = it },
                                label = { Text("كمية الجرد الفعلية (*)", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("opening_qty_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                    focusedContainerColor = Color.White.copy(alpha = 0.95f),
                                    unfocusedBorderColor = Color(0xFFCFFAFE),
                                    focusedBorderColor = ModernSoftPrimaryCyan,
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark
                                )
                            )

                            OutlinedTextField(
                                value = price,
                                onValueChange = { viewModel.inputPrice.value = it },
                                label = { Text("سعر البيع الافتراضي", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                    focusedContainerColor = Color.White.copy(alpha = 0.95f),
                                    unfocusedBorderColor = Color(0xFFCFFAFE),
                                    focusedBorderColor = ModernSoftPrimaryCyan,
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = cost,
                                onValueChange = { viewModel.inputCost.value = it },
                                label = { Text("التكلفة Cost", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                    focusedContainerColor = Color.White.copy(alpha = 0.95f),
                                    unfocusedBorderColor = Color(0xFFCFFAFE),
                                    focusedBorderColor = ModernSoftPrimaryCyan,
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark
                                )
                            )

                            OutlinedTextField(
                                value = batch,
                                onValueChange = { viewModel.inputBatchNo.value = it },
                                label = { Text("رقم التشغيلة Batch No", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                    focusedContainerColor = Color.White.copy(alpha = 0.95f),
                                    unfocusedBorderColor = Color(0xFFCFFAFE),
                                    focusedBorderColor = ModernSoftPrimaryCyan,
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark
                                )
                            )
                        }

                        OutlinedTextField(
                            value = expDate,
                            onValueChange = { viewModel.inputExpDate.value = it },
                            label = { Text("تاريخ نهاية الصلاحية (السنة-الشهر-اليوم YYYY-MM-DD)", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                focusedContainerColor = Color.White.copy(alpha = 0.95f),
                                unfocusedBorderColor = Color(0xFFCFFAFE),
                                focusedBorderColor = ModernSoftPrimaryCyan,
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Submit count item
                        Button(
                            onClick = { viewModel.addOpeningInventoryEntry() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("add_opening_draft_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ModernSoftSecondaryCyan),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تأكيد وحفظ بمسودة الجرد", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Current Inventory Draft Ledger list
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "مسودة جرد الرصيد الإفتتاحي الحالي (${startDrafts.size} صنف مضاف)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                if (startDrafts.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.clearStartEntries() },
                        colors = ButtonDefaults.textButtonColors(contentColor = ErrorOrangeRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("مسح المسودة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (startDrafts.isEmpty()) {
            item {
                Surface(
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = TextLightGrey)
                            Text(
                                "المسودة المحمولة فارغة تماماً!\nقم بالبحث عن صنف أعلاه أو مسح باركود وإضافة الكمية والبيانات للبدء.",
                                fontSize = 12.sp,
                                color = TextLightGrey,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Summary total Card
            item {
                val totalQty = startDrafts.sumOf { it.balance }
                val totalPriceVal = startDrafts.sumOf { it.balance * it.price }

                Card(
                    colors = CardDefaults.cardColors(containerColor = ModernSoftPrimaryCyan),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("إجمالي كمية الجرد", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                            Text("$totalQty وحدة مجرودة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("إجمالي القيمة المحسوبة", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                            Text("$totalPriceVal ر.س", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }

            items(startDrafts) { entry ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.65f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    entry.part_name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("معرف: ${entry.part_no}", fontSize = 11.sp, color = TextLightGrey)
                                    Text("المستودع: ${entry.depot_no}", fontSize = 11.sp, color = ModernSoftPrimaryCyan, fontWeight = FontWeight.Bold)
                                    Text("الفرع: ${entry.branch_no}", fontSize = 11.sp, color = TextLightGrey)
                                }
                            }

                            IconButton(onClick = { viewModel.deleteStartEntry(entry) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ErrorOrangeRed)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9).copy(alpha = 0.5f))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Column {
                                    Text("الكمية الفعلية", fontSize = 10.sp, color = TextLightGrey)
                                    Text("${entry.balance}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                }
                                Column {
                                    Text("السعر", fontSize = 10.sp, color = TextLightGrey)
                                    Text("${entry.price}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                }
                                Column {
                                    Text("تاريخ الانتهاء", fontSize = 10.sp, color = TextLightGrey)
                                    Text(entry.exp_date, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                }
                            }

                            Surface(
                                color = LightCyanGlow.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color.White)
                            ) {
                                Text(
                                    "إجمالي: ${entry.balance * entry.price} ر.س",
                                    fontSize = 12.sp,
                                    color = ModernSoftPrimaryCyan,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Export to intermediate storage button
            item {
                Button(
                    onClick = onShowExport,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessSoftGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .padding(top = 8.dp)
                        .testTag("export_opening_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.SendAndArchive, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تهيئة وتصدير الملفات لأوراكل (SQL)", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// 3. Year-End Closing Stocktake Screen (جرد نهاية السنة - CLOSE_PARTS)
@Composable
fun YearEndClosingScreen(
    viewModel: InventoryViewModel,
    onOpenScanner: () -> Unit,
    onShowExport: () -> Unit
) {
    val selectedPart by viewModel.selectedPart.collectAsState()
    val closeDrafts by viewModel.allCloseParts.collectAsState()
    val parts by viewModel.searchedParts.collectAsState()
    val query by viewModel.searchQuery.collectAsState()

    var showDropdown by remember { mutableStateOf(false) }
    val settings by viewModel.settingsState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming Card in Frosted Glass variant
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = "جرد نهائي",
                        tint = ModernSoftPrimaryCyan,
                        modifier = Modifier.size(36.dp)
                    )
                    Column {
                        Text(
                            "جرد نهاية السنة المالية (CLOSE_PARTS)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            "مقارنة فوارق كمية الجرد الدفترية لنظام أوراكل بالكميات المجرودة فعلياً وتصدير الفروقات.",
                            fontSize = 11.sp,
                            color = TextLightGrey,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // 2-Column Branch and Warehouse status row (styled like glass components in HTML)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "الفرع الحالي",
                            color = ModernSoftPrimaryCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "فرع: ${settings.branchNo}",
                            color = TextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "المستودع",
                            color = ModernSoftPrimaryCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "مستودع: ${settings.depotNo}",
                            color = TextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Searching/Barcode segment
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "البحث السريع واستدعاء الصنف المالي للجرد",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ModernSoftPrimaryCyan
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = {
                                viewModel.updateSearchQuery(it)
                                showDropdown = it.isNotEmpty()
                            },
                            placeholder = { Text("أدخل الكود المالي أو امسح الباركود...", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("close_search_input"),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextLightGrey) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                focusedContainerColor = Color.White.copy(alpha = 0.95f),
                                unfocusedBorderColor = Color(0xFFCFFAFE),
                                focusedBorderColor = ModernSoftPrimaryCyan,
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Button(
                            onClick = onOpenScanner,
                            colors = ButtonDefaults.buttonColors(containerColor = ModernSoftSecondaryCyan),
                            modifier = Modifier.height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "تصوير الباركود", tint = Color.White)
                        }
                    }

                    if (showDropdown && parts.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, Color(0xFFCFFAFE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                parts.take(5).forEach { part ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectPartForEntry(part)
                                                showDropdown = false
                                                viewModel.updateSearchQuery("")
                                            }
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            part.part_name ?: "",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                            color = TextDark
                                        )
                                        Text(
                                            part.part_no,
                                            fontSize = 11.sp,
                                            color = ModernSoftPrimaryCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active selection input (CLOSE_PARTS schema includes comp_qty, act_qty, and deferent)
        selectedPart?.let { part ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.7f),
                        contentColor = TextDark
                    ),
                    shape = RoundedCornerShape(32.dp),
                    border = BorderStroke(1.5.dp, Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(ModernSoftSecondaryCyan, RoundedCornerShape(50.dp))
                                )
                                Text(
                                    "تحديث البيانات المالية للمطابقة والجرد",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ModernSoftPrimaryCyan
                                )
                            }
                            IconButton(onClick = { viewModel.selectPartForEntry(part) }) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = ErrorOrangeRed)
                            }
                        }

                        Surface(
                            color = LightCyanGlow.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color(0xFFCFFAFE))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    part.part_name ?: "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("معرف أوراكل: ${part.part_no}", fontSize = 11.sp, color = TextLightGrey)
                                    Text("سعر افتراضي: ${part.price} ر.س", fontSize = 11.sp, color = ModernSoftPrimaryCyan, fontWeight = FontWeight.Bold)
                                    Text("تكلفة أساسية: ${part.cost}", fontSize = 11.sp, color = TextLightGrey)
                                }
                            }
                        }

                        val qtyActual by viewModel.inputQty.collectAsState()
                        val qtyComputer by viewModel.inputCompQty.collectAsState()
                        val batchNo by viewModel.inputBatchNo.collectAsState()
                        val expDate by viewModel.inputExpDate.collectAsState()

                        // Calculated difference/deficit preview card in Frosted Glass styling
                        val actDbl = qtyActual.toDoubleOrNull() ?: 1.0
                        val compDbl = qtyComputer.toDoubleOrNull() ?: 0.0
                        val def = actDbl - compDbl
                        val defText = if (def == 0.0) "مستقر ومطابق (0)" else if (def > 0.0) "زيادة بمقدار (+$def)" else "عجز بمقدار ($def)"
                        val defColor = if (def == 0.0) SuccessSoftGreen else if (def > 0.0) ModernSoftSecondaryCyan else ErrorOrangeRed

                        Surface(
                            color = defColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, defColor.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("محصلة مطابقة الفوارق المالية:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text(defText, color = defColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = qtyComputer,
                                onValueChange = { viewModel.inputCompQty.value = it },
                                label = { Text("الكمية الدفترية (Oracle)", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1.2f),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                    focusedContainerColor = Color.White.copy(alpha = 0.95f),
                                    unfocusedBorderColor = Color(0xFFCFFAFE),
                                    focusedBorderColor = ModernSoftPrimaryCyan,
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark
                                )
                            )

                            OutlinedTextField(
                                value = qtyActual,
                                onValueChange = { viewModel.inputQty.value = it },
                                label = { Text("الكمية الفعلية المكتشفة", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1.2f).testTag("close_qty_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                    focusedContainerColor = Color.White.copy(alpha = 0.95f),
                                    unfocusedBorderColor = Color(0xFFCFFAFE),
                                    focusedBorderColor = ModernSoftPrimaryCyan,
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = batchNo,
                                onValueChange = { viewModel.inputBatchNo.value = it },
                                label = { Text("رقم التشغيلة Batch No", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                    focusedContainerColor = Color.White.copy(alpha = 0.95f),
                                    unfocusedBorderColor = Color(0xFFCFFAFE),
                                    focusedBorderColor = ModernSoftPrimaryCyan,
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark
                                )
                            )

                            OutlinedTextField(
                                value = expDate,
                                onValueChange = { viewModel.inputExpDate.value = it },
                                label = { Text("تاريخ الصلاحية YYYY-MM-DD", fontSize = 10.sp) },
                                modifier = Modifier.weight(1.2f),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                    focusedContainerColor = Color.White.copy(alpha = 0.95f),
                                    unfocusedBorderColor = Color(0xFFCFFAFE),
                                    focusedBorderColor = ModernSoftPrimaryCyan,
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Button(
                            onClick = { viewModel.addCloseInventoryEntry() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("add_close_draft_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ModernSoftPrimaryCyan),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.LibraryAdd, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("اعتماد فارق الجرد وحفظ المسودة", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Ledger list block
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "قيود فوارق جرد نهاية السنة الحالي (${closeDrafts.size} بند مجرود)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                if (closeDrafts.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.clearCloseEntries() },
                        colors = ButtonDefaults.textButtonColors(contentColor = ErrorOrangeRed)
                    ) {
                        Text("مسح قيود الجرد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (closeDrafts.isEmpty()) {
            item {
                Surface(
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Compare, contentDescription = null, tint = TextLightGrey)
                            Text(
                                "لا يوجد سجلات جرد مالي نهاية السنة بعد.\nقم بالبحث مسبقاً وتعبئة كمية المحاسبة الفعلية للمقارنة ومطابقة الفوارق.",
                                fontSize = 11.sp,
                                color = TextLightGrey,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Stats ledger card in Frosted Glass style
            item {
                val totalComp = closeDrafts.sumOf { it.comp_qty }
                val totalAct = closeDrafts.sumOf { it.act_qty }
                val totalDef = totalAct - totalComp

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("الدفترية الكلية", fontSize = 11.sp, color = TextLightGrey)
                            Text("$totalComp", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("الفعلية الكلية", fontSize = 11.sp, color = TextLightGrey)
                            Text("$totalAct", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ModernSoftPrimaryCyan)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("تراكمي الفوارق", fontSize = 11.sp, color = TextLightGrey)
                            val defAccumColor = if (totalDef >= 0.0) SuccessSoftGreen else ErrorOrangeRed
                            Text(
                                if (totalDef > 0) "+$totalDef" else "$totalDef",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = defAccumColor
                            )
                        }
                    }
                }
            }

            // Entries cards with nice glass background
            items(closeDrafts) { entry ->
                val lineDef = entry.deferent
                val statusBorderClr = if (lineDef == 0.0) SuccessSoftGreen else if (lineDef > 0.0) ModernSoftSecondaryCyan else ErrorOrangeRed

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.65f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, statusBorderClr.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    entry.part_name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("معرف: ${entry.part_no}", fontSize = 11.sp, color = TextLightGrey)
                                    Text("المناولة: المستودع ${entry.depot_no}", fontSize = 11.sp, color = TextLightGrey)
                                    Text("الفرع: ${entry.branch_no}", fontSize = 11.sp, color = TextLightGrey)
                                }
                            }

                            IconButton(onClick = { viewModel.deleteCloseEntry(entry) }) {
                                Icon(Icons.Default.RemoveCircle, contentDescription = "حذف", tint = ErrorOrangeRed)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9).copy(alpha = 0.5f))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column {
                                    Text("الدفترية", fontSize = 10.sp, color = TextLightGrey)
                                    Text("${entry.comp_qty}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                }
                                Column {
                                    Text("الجرد الفعلي", fontSize = 10.sp, color = TextLightGrey)
                                    Text("${entry.act_qty}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ModernSoftPrimaryCyan)
                                }
                                Column {
                                    Text("مجموع الفارق", fontSize = 10.sp, color = TextLightGrey)
                                    Text(
                                        if (entry.deferent > 0) "+${entry.deferent}" else "${entry.deferent}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusBorderClr
                                    )
                                }
                            }

                            Surface(
                                color = statusBorderClr.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, statusBorderClr.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    if (entry.deferent == 0.0) "مطابق" else if (entry.deferent > 0.0) "فائض" else "عجز",
                                    color = statusBorderClr,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Export close button
            item {
                Button(
                    onClick = onShowExport,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessSoftGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .padding(top = 8.dp)
                        .testTag("export_close_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.DriveFolderUpload, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تهيئة وتصدير قيود جرد نهاية السنة للأوراكل", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// 4. SQL and Schema Help Reference Screen (جداول أوراكل واستعلامات وسيطة)
@Composable
fun SqlReferenceScreen(
    viewModel: InventoryViewModel
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val createSql = remember { viewModel.getIntermediateDDL() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.60f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "هيكل الجداول وسيناريو التكامل والربط مع أوراكل",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ModernSoftPrimaryCyan
                    )
                    Text(
                        "ينسق هذا التطبيق الجرد دون اتصال عبر جداول وسيطة تسمى MOBILE_START_PARTS و MOBILE_CLOSE_PARTS مما يتيح لفريق الـ DBA التحقق والترحيل للنظام الرسمي بسهولة.",
                        fontSize = 11.sp,
                        color = TextLightGrey,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.60f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "استعلام إنشاء الجداول الوسيطة بمطابقة للأوراكل DDL",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ModernSoftPrimaryCyan
                        )

                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(createSql))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ModernSoftPrimaryCyan),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("نسخ الكود الكلي", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFCFFAFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(14.dp)) {
                            Text(
                                createSql,
                                fontSize = 10.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = TextDark,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// 5. Simulated Barcode Scanner Camera view Dialog
@Composable
fun BarcodeScannerSimulationDialog(
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    val partsResult by viewModel.searchedParts.collectAsState()
    var searchCode by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val lineOffsetY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLineFloat"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color.White.copy(alpha = 0.92f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            border = BorderStroke(1.5.dp, Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "محاكي قارئ الباركود الضوئي الذكي",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ModernSoftPrimaryCyan
                )

                Text(
                    "اضغط على باركود منتج من القائمة السريعة لمحاكاة مسحه ضوئياً بالكاميرا وتعبئة حقول الجرد.",
                    fontSize = 11.sp,
                    color = TextLightGrey,
                    textAlign = TextAlign.Center
                )

                // Mock dynamic viewfinder viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(2.dp, ModernSoftSecondaryCyan, RoundedCornerShape(16.dp))
                        .drawBehind {
                            // Draw neon scanline
                            val y = size.height * lineOffsetY
                            drawLine(
                                color = InfoCyan,
                                start = Offset(10f, y),
                                end = Offset(size.width - 10f, y),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                ) {
                    Box(
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = ModernSoftSecondaryCyan.copy(alpha = 0.6f),
                                modifier = Modifier.size(54.dp)
                            )
                            Text(
                                "جاري تتبع الباركود تلقائياً...",
                                fontSize = 10.sp,
                                color = ModernSoftSecondaryCyan.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = searchCode,
                    onValueChange = { searchCode = it },
                    label = { Text("أو اكتب الباركود يدوياً للمطابقة", fontSize = 11.sp) },
                    trailingIcon = {
                        IconButton(onClick = {
                            if (searchCode.isNotBlank()) {
                                // Simulate searching and setting part if found
                                val target = partsResult.find { it.barcode == searchCode || it.part_no == searchCode }
                                if (target != null) {
                                    viewModel.selectPartForEntry(target)
                                    onDismiss()
                                }
                            }
                        }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = ModernSoftPrimaryCyan)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("manual_barcode_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                        focusedContainerColor = Color.White.copy(alpha = 0.95f),
                        unfocusedBorderColor = Color(0xFFCFFAFE),
                        focusedBorderColor = ModernSoftPrimaryCyan,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark
                    )
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9).copy(alpha = 0.4f))

                Text("أكواد الباركود الجاهزة في الكاش للاختبار السريع:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLightGrey)

                // List of sample parts to perform click scanning in single click
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(partsResult) { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.8f),
                            border = BorderStroke(0.5.dp, Color(0xFFCFFAFE)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectPartForEntry(item)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.part_name ?: "", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = TextDark)
                                    Text("باركود: ${item.barcode}", fontSize = 10.sp, color = TextLightGrey)
                                }
                                Surface(
                                    color = ModernSoftPrimaryCyan,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        "مسح ضوئي",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 6. View & copy generated Oracle staging SQL Insert scripts Dialog
@Composable
fun ExportScriptDialog(
    title: String,
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    val sqlText by viewModel.exportedSql.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color.White.copy(alpha = 0.92f),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            border = BorderStroke(1.5.dp, Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ModernSoftPrimaryCyan
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = ErrorOrangeRed)
                    }
                }

                Text(
                    "تم توليد استعلامات الـ SQL بنجاح. يمكنك نسخ هذه الأكواد وإرسالها أو تشغيلها مباشرة في قاعدة بيانات أوراكل لتعبئة الجداول المرجعية.",
                    fontSize = 11.sp,
                    color = TextLightGrey,
                    lineHeight = 15.sp
                )

                Surface(
                    color = Color.White.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFCFFAFE)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn(modifier = Modifier.padding(10.dp)) {
                        item {
                            Text(
                                sqlText,
                                fontSize = 10.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = TextDark
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(sqlText))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("copy_exported_sql_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessSoftGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("نسخ استعلامات SQL للحافظة", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
