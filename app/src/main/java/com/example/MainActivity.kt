package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StringsAr
import com.example.ui.viewmodel.WaterViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: WaterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: WaterViewModel) {
    val context = LocalContext.current
    
    val lang by viewModel.languageState.collectAsState()
    val role by viewModel.roleState.collectAsState()
    val isRtl = lang == "ar"

    val isInitializing by viewModel.isInitializing.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val offlineCount by viewModel.offlineReadingsCount.collectAsState()

    var activeTab by remember { mutableStateOf("dashboard") }
    
    // Watch role: if swiped, we automatically route logically
    LaunchedEffect(role) {
        if (role == "manager") {
            activeTab = "dashboard"
        } else {
            activeTab = "customers"
        }
    }

    if (isInitializing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "بلدية الزبابدة | جاري تهيئة وتحميل قاعدة البيانات الفنية...",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                // Customized Bilingual Executive Header Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                    tonalElevation = 6.dp,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Column(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = StringsAr.get("municipality", lang),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Text(
                                    text = StringsAr.get("sub_title", lang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            // Utility Row: Translation button + Cloud Sync Indicators
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Sync indicator showing offline status queue
                                if (offlineCount > 0) {
                                    IconButton(
                                        onClick = {
                                            viewModel.triggerCloudSynchronization { count ->
                                                Toast.makeText(
                                                    context,
                                                    if (lang == "ar") "تم مزامنة $count قراءات مع الخادم السحابي لبلدية الزبابدة بنجاح!"
                                                    else "Successfully synchronized $count offline readings to Zababdeh cloud servers!",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        },
                                        modifier = Modifier.testTag("trigger_sync_action_btn")
                                    ) {
                                        BadgedBox(
                                            badge = {
                                                Badge { Text("$offlineCount") }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudSync,
                                                contentDescription = StringsAr.get("sync_status", lang),
                                                tint = Color(0xFF6FF7EE)
                                            )
                                        }
                                    }
                                }

                                // Interactive translation swap button
                                Button(
                                    onClick = { viewModel.switchLanguage(if (lang == "ar") "en" else "ar") },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("toggle_language_btn"),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Text(
                                        text = if (lang == "ar") "English" else "العربية",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        // Executive Role Switcher Tabs (Reader vs Supervisor Admin)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(24.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val readerLabel = StringsAr.get("reader_role", lang)
                            val managerLabel = StringsAr.get("manager_role", lang)

                            // Role 1 option
                            Button(
                                onClick = { viewModel.switchRole("reader") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (role == "reader") MaterialTheme.colorScheme.secondary else Color.Transparent,
                                    contentColor = if (role == "reader") Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .testTag("switch_to_reader_role_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Engineering,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = readerLabel, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                            }

                            // Role 2 option
                            Button(
                                onClick = { viewModel.switchRole("manager") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (role == "manager") MaterialTheme.colorScheme.secondary else Color.Transparent,
                                    contentColor = if (role == "manager") Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .testTag("switch_to_manager_role_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = managerLabel, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                            }
                        }
                    }
                }
            },
            bottomBar = {
                // Modern responsive Navigation Bar conforming to active roles
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val dashboardLabel = StringsAr.get("nav_dashboard", lang)
                    val customersLabel = StringsAr.get("nav_customers", lang)
                    val scanLabel = StringsAr.get("nav_scan", lang)
                    val mapLabel = StringsAr.get("nav_gis", lang)

                    // Navigation Tab: Dashboard
                    NavigationBarItem(
                        selected = activeTab == "dashboard",
                        onClick = {
                            viewModel.selectCustomer(null)
                            activeTab = "dashboard"
                        },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = dashboardLabel) },
                        label = { Text(dashboardLabel, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )

                    // Navigation Tab: Customer directory route
                    NavigationBarItem(
                        selected = activeTab == "customers",
                        onClick = {
                            viewModel.selectCustomer(null)
                            activeTab = "customers"
                        },
                        icon = { Icon(Icons.Default.PeopleAlt, contentDescription = customersLabel) },
                        label = { Text(customersLabel, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("nav_tab_customers")
                    )

                    // Navigation Tab: AI Vision Scanner (Field reader specific)
                    NavigationBarItem(
                        selected = activeTab == "scan",
                        onClick = {
                            viewModel.selectCustomer(null)
                            activeTab = "scan"
                        },
                        icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = scanLabel) },
                        label = { Text(scanLabel, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("nav_tab_scan")
                    )

                    // Navigation Tab: GIS Telemetry Map tracker
                    NavigationBarItem(
                        selected = activeTab == "gis",
                        onClick = {
                            viewModel.selectCustomer(null)
                            activeTab = "gis"
                        },
                        icon = { Icon(Icons.Default.Map, contentDescription = mapLabel) },
                        label = { Text(mapLabel, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("nav_tab_gis")
                    )
                }
            }
        ) { innerPadding ->
            // Master animated screen switcher container swapping layouts based on selection rules
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = selectedCustomer to activeTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
                    },
                    label = "main_screens_navigator"
                ) { (customer, tab) ->
                    // Priority override: If custom client is selected, displays their details history timeline
                    if (customer != null) {
                        CustomerDetailScreen(
                            customer = customer,
                            viewModel = viewModel
                        )
                    } else {
                        when (tab) {
                            "dashboard" -> {
                                DashboardScreen(viewModel = viewModel)
                            }
                            "customers" -> {
                                CustomerListScreen(
                                    viewModel = viewModel,
                                    onNavigateToScan = {
                                        activeTab = "scan"
                                    }
                                )
                            }
                            "scan" -> {
                                ScanScreen(viewModel = viewModel)
                            }
                            "gis" -> {
                                MapScreen(viewModel = viewModel)
                            }
                            else -> {
                                DashboardScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Infix helper for transition spec builder
private infix fun Any.since(b: Int) = this

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
