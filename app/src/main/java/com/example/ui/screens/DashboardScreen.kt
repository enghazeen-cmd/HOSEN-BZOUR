package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Customer
import com.example.data.models.Zone
import com.example.ui.theme.StringsAr
import com.example.ui.viewmodel.WaterViewModel

@Composable
fun DashboardScreen(
    viewModel: WaterViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.languageState.collectAsState()
    val customers by viewModel.filteredCustomers.collectAsState()
    val zones by viewModel.zones.collectAsState()
    val totalM3 by viewModel.totalConsumption.collectAsState()
    val readingsCount by viewModel.readingCount.collectAsState()
    
    val selectedZoneFilter by viewModel.selectedZoneFilter.collectAsState()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsState()
    
    var showInvoiceFeedback by remember { mutableStateOf(false) }
    var invoiceTargetName by remember { mutableStateOf("") }

    val isRtl = lang == "ar"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // --- 1. KPI Cards Row ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isRtl) "مؤشرات الأداء العامة لشبكة المياه" else "Municipal Utility KPIs",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                // 2x2 grid for compact responsive mobile layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // KPI: Completed Readings
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(2.dp, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = StringsAr.get("total_active_meters", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$readingsCount / ${customers.size}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    // KPI: Total Consumption general
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(2.dp, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = Color(0xFF00716B),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = StringsAr.get("total_consumption", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format("%.1f %s", totalM3, StringsAr.get("cubic_meters", lang)),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // KPI: Suspicious Leakages
                    val leakages = customers.filter { cust -> cust.status == "active" && cust.id in listOf("C-102", "C-104") }.size
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(2.dp, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFC53030),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = StringsAr.get("leakage_alerts", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$leakages",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC53030)
                                )
                            )
                        }
                    }

                    // KPI: Suspended Meters count
                    val suspendedCount = customers.filter { it.status == "suspended" }.size
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(2.dp, RoundedCornerShape(24.dp)),
                        colors = CardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Icon(
                                imageVector = Icons.Default.ReportOff,
                                contentDescription = null,
                                tint = Color(0xFFD69E2E),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = StringsAr.get("suspended", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$suspendedCount",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD69E2E)
                                )
                            )
                        }
                    }
                }
            }
        }

        // --- 2. Custom Canvas Zone Bars Chart (Water Consumption Comparer) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = StringsAr.get("zone_title", lang),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isRtl) "مقارنة كمية الاستهلاك بآلاف الأمتار المكعبة بين مناطق الزبابدة الفيدرالية السبع"
                               else "Average consumption patterns across Zababdeh areas 1-7",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Draw the custom statistics bar chart
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val secondaryColor = MaterialTheme.colorScheme.secondary
                    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            
                            // Draw baseline axis
                            drawLine(
                                color = labelColor.copy(alpha = 0.3f),
                                start = Offset(0f, canvasHeight - 30.dp.toPx()),
                                end = Offset(canvasWidth, canvasHeight - 30.dp.toPx()),
                                strokeWidth = 2f
                            )

                            val totalBars = 7
                            val barSpacing = canvasWidth / (totalBars + 1)
                            val maxConsumptionVal = 80.0 // Normalizing limit

                            for (i in 0 until totalBars) {
                                val xPos = barSpacing * (i + 1)
                                
                                // Simulated high fidelity heights for Areas 1 to 7
                                val consumptionVal = when (i) {
                                    0 -> 24.5 // Old Town
                                    1 -> 68.2 // AAUUP Heights
                                    2 -> 16.1 // Olive Ridge
                                    3 -> 59.4 // Southern Farms
                                    4 -> 32.8 // Commercial
                                    5 -> 21.0 // East Bypass
                                    6 -> 74.3 // Industrial
                                    else -> 30.0
                                }

                                val barHeight = (consumptionVal / maxConsumptionVal) * (canvasHeight - 60.dp.toPx())
                                val barWidth = 20.dp.toPx()

                                // Draw bar with nice corporate rounded gradient style or solid
                                drawRect(
                                    color = if (i == 1 || i == 6) secondaryColor else primaryColor, // Highlight AAUUP and Industrial in teal
                                    topLeft = Offset(xPos - barWidth / 2, (canvasHeight - 30.dp.toPx()) - barHeight.toFloat()),
                                    size = Size(barWidth, barHeight.toFloat())
                                )

                                // Draw Label indicators at bottom
                                drawContext.canvas.nativeCanvas.apply {
                                    val textPaint = android.graphics.Paint().apply {
                                        color = labelColor.toArgb()
                                        textSize = 10.sp.toPx()
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        isAntiAlias = true
                                    }
                                    drawText(
                                        "Z-${i + 1}",
                                        xPos,
                                        canvasHeight - 10.dp.toPx(),
                                        textPaint
                                    )
                                    
                                    // Draw consumption value above bar
                                    val textPaintVal = android.graphics.Paint().apply {
                                        color = primaryColor.toArgb()
                                        textSize = 9.sp.toPx()
                                        isFakeBoldText = true
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        isAntiAlias = true
                                    }
                                    drawText(
                                        "${consumptionVal.toInt()}",
                                        xPos,
                                        (canvasHeight - 34.dp.toPx() - barHeight.toFloat()).coerceAtLeast(12.dp.toPx()),
                                        textPaintVal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3. Filter Swipers ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isRtl) "تصفية وعرض المشتركين" else "Accounts Directories Filters",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                // Search Bar
                var textQuery by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = textQuery,
                    onValueChange = {
                        textQuery = it
                        viewModel.updateSearchQuery(it)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("dashboard_search_input"),
                    placeholder = { Text(StringsAr.get("search_hint", lang)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (textQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                textQuery = ""
                                viewModel.updateSearchQuery("")
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                // Quick horizontal Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val activeLabel = StringsAr.get("active", lang)
                    val suspendedLabel = StringsAr.get("suspended", lang)

                    FilterChip(
                        selected = selectedStatusFilter == null,
                        onClick = { viewModel.filterByStatus(null) },
                        label = { Text(if (isRtl) "الكل" else "All") }
                    )
                    FilterChip(
                        selected = selectedStatusFilter == "active",
                        onClick = { viewModel.filterByStatus("active") },
                        label = { Text(activeLabel) },
                        leadingIcon = {
                            if (selectedStatusFilter == "active") {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                    FilterChip(
                        selected = selectedStatusFilter == "suspended",
                        onClick = { viewModel.filterByStatus("suspended") },
                        label = { Text(suspendedLabel) },
                        leadingIcon = {
                            if (selectedStatusFilter == "suspended") {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                }
            }
        }

        // --- 4. Customers accounts directory ---
        if (customers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isRtl) "لا يوجد مشتركين يطابقون خيارات البحث الحالية." else "No customers found matching current filters.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(customers) { customer ->
                CustomerRecordItem(
                    customer = customer,
                    lang = lang,
                    isRtl = isRtl,
                    onBillClicked = {
                        invoiceTargetName = if (isRtl) customer.nameAr else customer.nameEn
                        showInvoiceFeedback = true
                    },
                    onProfileClicked = {
                        viewModel.selectCustomer(customer)
                    }
                )
            }
        }
    }

    // Interactive Dialog Invoice simulation
    if (showInvoiceFeedback) {
        AlertDialog(
            onDismissRequest = { showInvoiceFeedback = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(text = StringsAr.get("billing_total", lang))
                }
            },
            text = {
                Text(
                    text = if (isRtl) "تم احتساب استهلاك وحساب الفاتورة إلكترونياً للمشترك ($invoiceTargetName) وجاري العمل على إرسالها لرسائل الهاتف وسيرفر الجباية بنجاح."
                           else "Invoice calculated successfully for ($invoiceTargetName). Details have been compiled and sent to local servers."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showInvoiceFeedback = false },
                    modifier = Modifier.testTag("invoice_confirm_btn")
                ) {
                    Text("موافق | OK")
                }
            }
        )
    }
}

@Composable
fun CustomerRecordItem(
    customer: Customer,
    lang: String,
    isRtl: Boolean,
    onBillClicked: () -> Unit,
    onProfileClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProfileClicked() }
            .shadow(1.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Name
                Text(
                    text = if (isRtl) customer.nameAr else customer.nameEn,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                // Account details
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FilterList, contentDescription = null, size = 14.dp, tint = MaterialTheme.colorScheme.secondary)
                    Text(
                        text = " Area ${customer.zoneId} | ${customer.meterNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, size = 14.dp, tint = MaterialTheme.colorScheme.secondary)
                    Text(
                        text = " ${customer.address}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status Badge
                StatusBadge(status = customer.status, lang = lang)

                // Quick Invoice Action button
                Button(
                    onClick = { onBillClicked() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("bill_account_${customer.id}"),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = if (isRtl) "فاتورة" else "Invoice",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String, lang: String) {
    val (label, containerColor, textColor) = when (status) {
        "active" -> Triple(
            StringsAr.get("active", lang),
            Color(0xFFE6FFFA),
            Color(0xFF006A65)
        )
        "suspended" -> Triple(
            StringsAr.get("suspended", lang),
            Color(0xFFFFF9E6),
            Color(0xFFD69E2E)
        )
        else -> Triple(
            StringsAr.get("normal", lang),
            Color(0xFFEDF2F7),
            Color(0xFF4A5568)
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(textColor)
            )
            Text(
                text = label,
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

// Simple override of standard Icon to allow size overrides elegantly and safely
@Composable
fun Icon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    size: androidx.compose.ui.unit.Dp,
    tint: Color
) {
    androidx.compose.material3.Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint,
        modifier = androidx.compose.ui.Modifier.size(size)
    )
}
