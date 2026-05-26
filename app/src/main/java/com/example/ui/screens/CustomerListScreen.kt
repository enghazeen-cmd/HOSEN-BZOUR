package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.Customer
import com.example.ui.theme.StringsAr
import com.example.ui.viewmodel.WaterViewModel

@Composable
fun CustomerListScreen(
    viewModel: WaterViewModel,
    onNavigateToScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.languageState.collectAsState()
    val customers by viewModel.filteredCustomers.collectAsState()
    val zones by viewModel.zones.collectAsState()
    val selectedZoneFilter by viewModel.selectedZoneFilter.collectAsState()

    val isRtl = lang == "ar"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        
        // --- 1. Top Search Header Area ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isRtl) "خطط وجداول السير اليومية للجباة" else "Assigned Field Routes Routes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                // Search Box
                var searchQueryText by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = searchQueryText,
                    onValueChange = {
                        searchQueryText = it
                        viewModel.updateSearchQuery(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_list_search_input"),
                    placeholder = { Text(StringsAr.get("search_hint", lang), color = Color.White.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) },
                    trailingIcon = {
                        if (searchQueryText.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQueryText = ""
                                viewModel.updateSearchQuery("")
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = Color.White)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
            }
        }

        // --- 2. Zones selection Horizontal Chips row ---
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = selectedZoneFilter == null,
                    onClick = { viewModel.filterByZone(null) },
                    label = { Text(if (isRtl) "كافة المناطق" else "All Sectors") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
            items(zones) { zone ->
                FilterChip(
                    selected = selectedZoneFilter == zone.id,
                    onClick = { viewModel.filterByZone(if (selectedZoneFilter == zone.id) null else zone.id) },
                    label = { Text(text = "Zone ${zone.id}") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // --- 3. Customers Grid List ---
        if (customers.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isRtl) "عذراً، لم نجد أي مشترك يطابق المدخلات." else "No water meters match search criteria.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(customers) { customer ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectCustomer(customer) }
                            .shadow(1.dp, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isRtl) customer.nameAr else customer.nameEn,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Area ${customer.zoneId} | Meter: ${customer.meterNumber}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                StatusBadge(status = customer.status, lang = lang)
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, size = 14.dp, tint = MaterialTheme.colorScheme.secondary)
                                    Text(text = customer.address, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                // Interactive Quick Scan launch Redirection Button
                                Button(
                                    onClick = {
                                        viewModel.selectCustomer(customer)
                                        onNavigateToScan()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("action_scan_customer_${customer.id}"),
                                    shape = RoundedCornerShape(24.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, size = 14.dp, tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = if (isRtl) "قراءة" else "OCR Scan", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
