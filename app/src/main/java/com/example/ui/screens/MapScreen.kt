package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Customer
import com.example.ui.theme.StringsAr
import com.example.ui.viewmodel.WaterViewModel

@Composable
fun MapScreen(
    viewModel: WaterViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.languageState.collectAsState()
    val customers by viewModel.filteredCustomers.collectAsState()
    
    var selectedMapCustomer by remember { mutableStateOf<Customer?>(null) }
    var filterLeakageOnly by remember { mutableStateOf(false) }
    var showReaderTrack by remember { mutableStateOf(true) }

    val isRtl = lang == "ar"

    // Animation for pulsing alerts (leakages)
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE2EAF4)) // Ambient slate blue coordinate map base
    ) {
        
        // --- 1. Interactive Canvas Map ---
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(customers, filterLeakageOnly) {
                    detectTapGestures { offset ->
                        // Hit test customers pins
                        // Convert customer coordinate offsets to canvas points
                        val widthScale = size.width
                        val heightScale = size.height

                        var clickedCust: Customer? = null
                        for (cust in customers) {
                            if (filterLeakageOnly && cust.id !in listOf("C-102", "C-104")) continue

                            // Translate relative GPS coordinates (Zababdeh bounding box)
                            val mapCoords = translateGpsToCanvas(
                                lat = cust.gpsLat,
                                lng = cust.gpsLng,
                                width = widthScale.toFloat(),
                                height = heightScale.toFloat()
                            )

                            // Hit test radius 20dp
                            val distance = Math.hypot(
                                (offset.x - mapCoords.x).toDouble(),
                                (offset.y - mapCoords.y).toDouble()
                            )
                            if (distance < 25.dp.toPx()) {
                                clickedCust = cust
                                break
                            }
                        }
                        selectedMapCustomer = clickedCust
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // A. Draw Zone Boundaries (Polygons overlays)
            // Representing the 7 sectors programmatically
            val pathZone1 = Path().apply {
                moveTo(canvasWidth * 0.1f, canvasHeight * 0.2f)
                lineTo(canvasWidth * 0.45f, canvasHeight * 0.15f)
                lineTo(canvasWidth * 0.5f, canvasHeight * 0.45f)
                lineTo(canvasWidth * 0.15f, canvasHeight * 0.5f)
                close()
            }
            drawPath(
                path = pathZone1,
                color = Color(0x20002045),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
            drawPath(
                path = pathZone1,
                color = Color(0x40002045),
                style = Stroke(width = 2.dp.toPx())
            )

            val pathZone2 = Path().apply {
                moveTo(canvasWidth * 0.5f, canvasHeight * 0.15f)
                lineTo(canvasWidth * 0.9f, canvasHeight * 0.25f)
                lineTo(canvasWidth * 0.82f, canvasHeight * 0.6f)
                lineTo(canvasWidth * 0.48f, canvasHeight * 0.45f)
                close()
            }
            drawPath(
                path = pathZone2,
                color = Color(0x18006A65), // Teal Sector
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
            drawPath(
                path = pathZone2,
                color = Color(0x30006A65),
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw other zones simplified backgrounds
            drawRoundRect(
                color = Color(0x15D69E2E), // Agricultural South
                topLeft = Offset(canvasWidth * 0.05f, canvasHeight * 0.55f),
                size = Size(canvasWidth * 0.42f, canvasHeight * 0.35f),
                cornerRadius = CornerRadius(16f, 16f)
            )

            // B. Draw Main Reference Arterial Streets of Zababdeh
            // University Road Route
            drawLine(
                color = Color.White,
                start = Offset(canvasWidth * 0.15f, canvasHeight * 0.35f),
                end = Offset(canvasWidth * 0.85f, canvasHeight * 0.45f),
                strokeWidth = 6.dp.toPx()
            )
            drawLine(
                color = Color(0xFFADC7F7),
                start = Offset(canvasWidth * 0.15f, canvasHeight * 0.35f),
                end = Offset(canvasWidth * 0.85f, canvasHeight * 0.45f),
                strokeWidth = 2.dp.toPx()
            )

            // Jenin Bypass Road
            drawLine(
                color = Color.White,
                start = Offset(canvasWidth * 0.45f, canvasHeight * 0.05f),
                end = Offset(canvasWidth * 0.52f, canvasHeight * 0.95f),
                strokeWidth = 5.dp.toPx()
            )

            // C. Draw Customer Water Meter Pins
            customers.forEach { cust ->
                val isLeaky = cust.id in listOf("C-102", "C-104")
                if (filterLeakageOnly && !isLeaky) return@forEach

                val pinCenter = translateGpsToCanvas(
                    lat = cust.gpsLat,
                    lng = cust.gpsLng,
                    width = canvasWidth,
                    height = canvasHeight
                )

                if (isLeaky) {
                    // Pulsing red halo for active hazard suspects
                    drawCircle(
                        color = Color(0xFFC53030).copy(alpha = 0.35f - (pulseScale / 25f).coerceAtMost(0.35f)),
                        radius = pulseScale.dp.toPx(),
                        center = pinCenter
                    )
                }

                // Inner Pin
                drawCircle(
                    color = if (isLeaky) Color(0xFFC53030) else Color(0xFF002045),
                    radius = 8.dp.toPx(),
                    center = pinCenter
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = pinCenter
                )
            }

            // D. Draw Reader Live Location Tracker
            if (showReaderTrack) {
                val readerCoords = Offset(canvasWidth * 0.48f, canvasHeight * 0.38f)
                // Pulse halo
                drawCircle(
                    color = Color(0xFF006A65).copy(alpha = 0.25f),
                    radius = 16.dp.toPx(),
                    center = readerCoords
                )
                // Core position indicator
                drawCircle(
                    color = Color(0xFF006A65),
                    radius = 6.dp.toPx(),
                    center = readerCoords
                )
            }
        }

        // --- 2. Floating Map Legend Overlay ---
        Card(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .shadow(4.dp, RoundedCornerShape(24.dp))
                .widthIn(max = 240.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isRtl) "خارطة شبكة المياه الجغرافية" else "GIS Schema Legend",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Legend row 1
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF002045)))
                    Text(text = if (isRtl) "عداد سليم ونشط" else "Normal active Meter", style = MaterialTheme.typography.labelSmall)
                }

                // Legend row 2
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFC53030)))
                    Text(text = if (isRtl) "تسريب مياه نشط" else "Active Leakage Spot", style = MaterialTheme.typography.labelSmall)
                }

                // Legend row 3
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF006A65)))
                    Text(text = if (isRtl) "live موظف الميدان (الجابي)" else "Field reader (Live)", style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Toggle controls buttons inside Legend representation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (isRtl) "إنذارات التسريب" else "Leaks Alert", style = MaterialTheme.typography.labelSmall)
                    Switch(
                        checked = filterLeakageOnly,
                        onCheckedChange = { filterLeakageOnly = it },
                        modifier = Modifier
                            .scale(0.7f)
                            .testTag("toggle_leaks_switch")
                    )
                }
            }
        }

        // --- 3. Pin Interaction Detail Overlay Pane (Slides up from bottom) ---
        AnimatedVisibility(
            visible = selectedMapCustomer != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val customer = selectedMapCustomer
            if (customer != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(24.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = if (isRtl) customer.nameAr else customer.nameEn,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${StringsAr.get("meter_serial", lang)} ${customer.meterNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { selectedMapCustomer = null },
                                modifier = Modifier.testTag("close_map_pin_overlay")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // GIS details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = StringsAr.get("address", lang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(text = customer.address, style = MaterialTheme.typography.bodySmall)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = StringsAr.get("status_label", lang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                StatusBadge(status = customer.status, lang = lang)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Custom Water status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = if (customer.id in listOf("C-102", "C-104")) Icons.Default.Warning else Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = if (customer.id in listOf("C-102", "C-104")) Color(0xFFC53030) else Color(0xFF006A65)
                                )
                                Text(
                                    text = if (customer.id in listOf("C-102", "C-104")) StringsAr.get("leakage_warn", lang).take(19) + "..."
                                           else StringsAr.get("normal", lang),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (customer.id in listOf("C-102", "C-104")) Color(0xFFC53030) else Color(0xFF006A65)
                                    )
                                )
                            }

                            // View profile / Select action
                            Button(
                                onClick = {
                                    viewModel.selectCustomer(customer)
                                    selectedMapCustomer = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("map_view_profile_btn")
                            ) {
                                Text(
                                    text = if (isRtl) "عرض السجل والبيانات" else "Open Record",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Translates latitude and longitude variables into proportional canvas viewport coordinate points.
 * Relative bounding box coordinates for Zababdeh Town, West Bank inside standard maps.
 */
private fun translateGpsToCanvas(
    lat: Double,
    lng: Double,
    width: Float,
    height: Float
): Offset {
    // Latitude range: 32.3650 to 32.3880
    // Longitude range: 35.3100 to 35.3300
    val minLat = 32.3650
    val maxLat = 32.3880
    val minLng = 35.3100
    val maxLng = 35.3300

    // Standard linear projections
    val pctX = ((lng - minLng) / (maxLng - minLng)).coerceIn(0.1, 0.9)
    val pctY = 1.0 - ((lat - minLat) / (maxLat - minLat)).coerceIn(0.1, 0.9) // Invert Y as canvas top-left is 0,0

    return Offset(
        x = (pctX * width).toFloat(),
        y = (pctY * height).toFloat()
    )
}
