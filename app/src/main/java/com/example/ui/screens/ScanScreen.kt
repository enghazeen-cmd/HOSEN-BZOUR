package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Customer
import com.example.ui.theme.StringsAr
import com.example.ui.viewmodel.WaterViewModel

@Composable
fun ScanScreen(
    viewModel: WaterViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.languageState.collectAsState()
    val customers by viewModel.filteredCustomers.collectAsState()
    
    val ocrLoading by viewModel.ocrLoading.collectAsState()
    val lastOcrResult by viewModel.lastOcrResult.collectAsState()

    var selectedCustomerForScan by remember { mutableStateOf<Customer?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var typedReadingValue by remember { mutableStateOf("") }
    var simulatedValueText by remember { mutableStateOf("") }
    
    var showAnomalousLeakageAlert by remember { mutableStateOf(false) }
    var saveSuccessFeedback by remember { mutableStateOf(false) }
    var computedConsumption by remember { mutableStateOf(0.0) }

    val isRtl = lang == "ar"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // --- 1. Top Guideline Label ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = StringsAr.get("scanner_header", lang),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = StringsAr.get("viewfinder_instruction", lang),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }

        // --- 2. Interactive Customer Account Selector ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = StringsAr.get("choose_customer_prompt", lang),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )

                        Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { dropdownExpanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("select_customer_dropdown_btn")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedCustomerForScan?.let { if (isRtl) it.nameAr else it.nameEn }
                                        ?: (if (isRtl) "اختر الحساب والموقع..." else "Select account to read..."),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .heightIn(max = 280.dp)
                        ) {
                            customers.forEach { customer ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = if (isRtl) customer.nameAr else customer.nameEn,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Area ${customer.zoneId} | ${customer.meterNumber}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedCustomerForScan = customer
                                        dropdownExpanded = false
                                        typedReadingValue = ""
                                        simulatedValueText = ""
                                    },
                                    modifier = Modifier.testTag("dropdown_item_${customer.id}")
                                )
                            }
                        }
                    }

                    // Selected Customer Quick Metadata card info
                    selectedCustomerForScan?.let { cust ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${StringsAr.get("previous_label", lang)} ${cust.lastReadingValue} ${StringsAr.get("cubic_meters", lang)}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${StringsAr.get("address", lang)} ${cust.address} | Serial: ${cust.meterNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 3. Viewfinder Camera Overlay (TACTILE SIMULATOR) ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
            ) {
                // Diagonal background viewfinder laser effects
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color.Transparent, Color(0x30006A65), Color.Transparent)
                            )
                        )
                )

                // Brackets Corners lines representing scanning camera view
                Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    // Draw mock target corner borders
                    Box(modifier = Modifier.align(Alignment.TopStart).size(24.dp).border(2.dp, Color(0xFF6FF7EE), RoundedCornerShape(topStart = 4.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 0.dp)))
                    Box(modifier = Modifier.align(Alignment.TopEnd).size(24.dp).border(2.dp, Color(0xFF6FF7EE), RoundedCornerShape(topStart = 0.dp, topEnd = 4.dp, bottomStart = 0.dp, bottomEnd = 0.dp)))
                    Box(modifier = Modifier.align(Alignment.BottomStart).size(24.dp).border(2.dp, Color(0xFF6FF7EE), RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 4.dp, bottomEnd = 0.dp)))
                    Box(modifier = Modifier.align(Alignment.BottomEnd).size(24.dp).border(2.dp, Color(0xFF6FF7EE), RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 4.dp)))

                    // Central target viewfinder containing custom mechanical dials graphics
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(200.dp)
                            .height(56.dp)
                            .background(Color(0xFF2D3748), RoundedCornerShape(24.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Dial values
                            val displayDigits = selectedCustomerForScan?.let {
                                val simulatedCurrent = it.lastReadingValue + (15 + (1..35).random())
                                String.format("%06d", simulatedCurrent.toInt())
                            } ?: "000284"

                            // Simulated mechanical roller barrels
                            displayDigits.forEachIndexed { idx, digit ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(if (idx < 4) Color.Black else Color(0xFFC53030), RoundedCornerShape(2.dp))
                                        .border(0.5.dp, Color.Gray, RoundedCornerShape(2.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = digit.toString(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // AI scan live horizontal scanning line bar
                val infiniteTransitionLine = rememberInfiniteTransition(label = "scanning")
                val scanLineY by infiniteTransitionLine.animateFloat(
                    initialValue = 0f,
                    targetValue = 220f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = EaseInOutQuad),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scanLine"
                )

                Box(
                    modifier = Modifier
                        .offset(y = scanLineY.dp)
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color(0xFF6FF7EE).copy(alpha = 0.8f))
                        .shadow(4.dp, spotColor = Color(0xFF00716B))
                )
            }
        }

        // --- 4. Main Action Trigger Buttons ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Secondary trigger: Manual overwrite input
                Button(
                    onClick = {
                        if (selectedCustomerForScan != null) {
                            simulatedValueText = ""
                            typedReadingValue = selectedCustomerForScan!!.lastReadingValue.toString()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("trigger_manual_input_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(24.dp),
                    enabled = selectedCustomerForScan != null
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, size = 18.dp, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(StringsAr.get("manual_override", lang))
                }

                // Primary trigger: Simulate Photo Capture + AI OCR validation
                Button(
                    onClick = {
                        val currentCust = selectedCustomerForScan
                        if (currentCust != null) {
                            // Generate programmatic mechanical bitmap
                            val digitsVal = currentCust.lastReadingValue + (15 + (1..35).random())
                            
                            val width = 300
                            val height = 100
                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(bitmap)
                            val paintBg = AndroidPaint().apply { color = AndroidColor.DKGRAY }
                            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintBg)
                            
                            val paintText = AndroidPaint().apply {
                                color = AndroidColor.WHITE
                                textSize = 42f
                                isFakeBoldText = true
                            }
                            canvas.drawText("WATER_METER: ${digitsVal.toInt()}", 20f, 60f, paintText)

                            // Let's fire the Gemini API OCR!
                            viewModel.analyzeMeterReading(bitmap, currentCust) { digits, status ->
                                simulatedValueText = String.format("%.1f", digits)
                                computedConsumption = (digits - currentCust.lastReadingValue).coerceAtLeast(0.0)
                                
                                // Leakage safety trigger: if consumption is >2.5x times prior intervals baseline (e.g. >100 m³)
                                if (computedConsumption > 100.0) {
                                    showAnomalousLeakageAlert = true
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                        .testTag("trigger_ai_ocr_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(24.dp),
                    enabled = selectedCustomerForScan != null && !ocrLoading
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, size = 18.dp, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(StringsAr.get("take_reading", lang))
                }
            }
        }

        // --- 5. AI Loading Indicator ---
        if (ocrLoading) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = StringsAr.get("analyze_loading", lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // --- 6. OCR Validation Results & Save Section ---
        val activeValueInput = simulatedValueText.ifEmpty { typedReadingValue }
        if (activeValueInput.isNotEmpty() && !ocrLoading) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = StringsAr.get("current_result", lang),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            // Mock ML Certainty Badge
                            lastOcrResult?.let { res ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color(0xFFE6FFFA))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = String.format("AI Conf: %.0f%%", res.confidence * 100),
                                        color = Color(0xFF006A65),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Extracted digits display or Text input modifier
                        OutlinedTextField(
                            value = activeValueInput,
                            onValueChange = {
                                if (simulatedValueText.isNotEmpty()) {
                                    simulatedValueText = it
                                } else {
                                    typedReadingValue = it
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ocr_override_input"),
                            label = { Text(StringsAr.get("detected_digits", lang)) },
                            suffix = { Text(StringsAr.get("cubic_meters", lang)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(24.dp)
                        )

                        // Relative Consumption info
                        selectedCustomerForScan?.let { cust ->
                            val currentNum = activeValueInput.toDoubleOrNull() ?: 0.0
                            val consumptionDiff = (currentNum - cust.lastReadingValue).coerceAtLeast(0.0)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isRtl) "كمية الاستهلاك المسجلة:" else "Derived Consumption:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = String.format("%.1f %s", consumptionDiff, StringsAr.get("cubic_meters", lang)),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (consumptionDiff > 100) Color(0xFFC53030) else Color(0xFF006A65)
                                    )
                                )
                            }
                        }

                        // Commit Button
                        Button(
                            onClick = {
                                val currentCust = selectedCustomerForScan
                                val finalDouble = activeValueInput.toDoubleOrNull()
                                if (currentCust != null && finalDouble != null) {
                                    val isAnomalous = (finalDouble - currentCust.lastReadingValue) > 100.0
                                    val finalStatus = if (isAnomalous) "leakage_suspected" else "normal"
                                    
                                    viewModel.commitWaterReading(
                                        customerId = currentCust.id,
                                        currentValue = finalDouble,
                                        previousValue = currentCust.lastReadingValue,
                                        status = finalStatus,
                                        confidence = lastOcrResult?.confidence ?: 1.0,
                                        offlineMode = false
                                    )
                                    
                                    saveSuccessFeedback = true
                                    simulatedValueText = ""
                                    typedReadingValue = ""
                                    selectedCustomerForScan = null
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("commit_reading_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006A65)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(StringsAr.get("save_reading_btn", lang))
                        }
                    }
                }
            }
        }
    }

    // Anomalous leakage alert dialog (Rethink/Validate dialogue)
    if (showAnomalousLeakageAlert) {
        AlertDialog(
            onDismissRequest = { showAnomalousLeakageAlert = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFC53030), modifier = Modifier.padding(end = 8.dp))
                    Text(text = StringsAr.get("alert_title", lang), color = Color(0xFFC53030))
                }
            },
            text = {
                Text(text = StringsAr.get("leakage_warn", lang))
            },
            confirmButton = {
                Button(
                    onClick = { showAnomalousLeakageAlert = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC53030)),
                    modifier = Modifier.testTag("leakage_alert_dismiss_btn")
                ) {
                    Text(StringsAr.get("alert_dismiss", lang))
                }
            }
        )
    }

    // Success dialog
    if (saveSuccessFeedback) {
        AlertDialog(
            onDismissRequest = { saveSuccessFeedback = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF006A65), modifier = Modifier.padding(end = 8.dp))
                    Text(text = StringsAr.get("confirm_msg", lang), color = Color(0xFF006A65))
                }
            },
            text = {
                Text(text = if (isRtl) "تم توريد القراءة وإجراء الاحتساب الرقمي وسحب صورة العداد بنجاح." else "The water reading is registered. Consumption history was calculated and archived.")
            },
            confirmButton = {
                TextButton(
                    onClick = { saveSuccessFeedback = false },
                    modifier = Modifier.testTag("success_alert_confirm_btn")
                ) {
                    Text("تم | Done")
                }
            }
        )
    }
}
