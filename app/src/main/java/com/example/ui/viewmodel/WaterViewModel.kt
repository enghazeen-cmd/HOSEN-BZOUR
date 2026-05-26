package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.WaterDatabase
import com.example.data.models.Customer
import com.example.data.models.Reading
import com.example.data.models.Zone
import com.example.data.repository.WaterRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class WaterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WaterRepository
    
    // UI Language: English ("en") or Arabic ("ar")
    private val _languageState = MutableStateFlow("ar")
    val languageState: StateFlow<String> = _languageState.asStateFlow()

    // Current Role: "reader" (Meter Reader Employee) or "manager" (Municipality Manager / Admin)
    private val _roleState = MutableStateFlow("reader")
    val roleState: StateFlow<String> = _roleState.asStateFlow()

    // Active Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected Zone Filtering for Dashboard/Lists
    private val _selectedZoneFilter = MutableStateFlow<String?>(null)
    val selectedZoneFilter: StateFlow<String?> = _selectedZoneFilter.asStateFlow()

    // Selected Customer status Filter
    private val _selectedStatusFilter = MutableStateFlow<String?>(null)
    val selectedStatusFilter: StateFlow<String?> = _selectedStatusFilter.asStateFlow()

    // Seeding/Initialization Status
    private val _isInitializing = MutableStateFlow(true)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

    // Direct Database Streams
    val zones: StateFlow<List<Zone>>
    val allReadings: StateFlow<List<Reading>>
    val readingCount: StateFlow<Int>
    val totalConsumption: StateFlow<Double>

    // Dynamic Filtered Customer List
    private val _filteredCustomers = MutableStateFlow<List<Customer>>(emptyList())
    val filteredCustomers: StateFlow<List<Customer>> = _filteredCustomers.asStateFlow()

    // Current Selected Customer for Profile Detail screen
    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    // Active Scan State
    private val _ocrLoading = MutableStateFlow(false)
    val ocrLoading: StateFlow<Boolean> = _ocrLoading.asStateFlow()

    private val _lastOcrResult = MutableStateFlow<WaterRepository.OcrResult?>(null)
    val lastOcrResult: StateFlow<WaterRepository.OcrResult?> = _lastOcrResult.asStateFlow()

    // Pending Sync Counters (Offline Capability Showcase)
    private val _offlineReadingsCount = MutableStateFlow(0)
    val offlineReadingsCount: StateFlow<Int> = _offlineReadingsCount.asStateFlow()

    init {
        val database = WaterDatabase.getDatabase(application)
        repository = WaterRepository(database.waterDao())

        // Collect db streams and expose as view state
        zones = repository.allZones.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allReadings = repository.allReadings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        readingCount = repository.readingCount.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

        totalConsumption = repository.totalConsumption
            .map { it ?: 0.0 }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0.0
            )

        // Run seed prepopulation and initialize app
        viewModelScope.launch {
            _isInitializing.value = true
            repository.seedDatabaseIfEmpty()
            _isInitializing.value = false
        }

        // Connect and combine reactive search and filtering in a separate coroutine
        viewModelScope.launch {
            combine(
                repository.allCustomers,
                _searchQuery,
                _selectedZoneFilter,
                _selectedStatusFilter
            ) { customerList, query, zone, status ->
                var list = customerList
                if (query.isNotEmpty()) {
                    list = list.filter {
                        it.nameAr.contains(query, ignoreCase = true) ||
                        it.nameEn.contains(query, ignoreCase = true) ||
                        it.meterNumber.contains(query, ignoreCase = true) ||
                        it.id.contains(query, ignoreCase = true)
                    }
                }
                if (zone != null) {
                    list = list.filter { it.zoneId == zone }
                }
                if (status != null) {
                    list = list.filter { it.status == status }
                }
                list
            }.collect {
                _filteredCustomers.value = it
                if (_selectedCustomer.value != null) {
                    // Update detail pointer references if refreshed
                    _selectedCustomer.value = it.find { cust -> cust.id == _selectedCustomer.value?.id }
                }
            }
        }
    }

    // Toggle Application Role
    fun switchRole(role: String) {
        _roleState.value = role
    }

    // Toggle Application Locales
    fun switchLanguage(lang: String) {
        _languageState.value = lang
    }

    // Search and Filters
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun filterByZone(zoneId: String?) {
        _selectedZoneFilter.value = zoneId
    }

    fun filterByStatus(status: String?) {
        _selectedStatusFilter.value = status
    }

    // Main Selection Target
    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    /**
     * AI-Powered Meter Reading Entry. Uses base64 representation if available or simulates.
     */
    fun analyzeMeterReading(bitmap: Bitmap, customer: Customer, onComplete: (Double, String) -> Unit) {
        viewModelScope.launch {
            _ocrLoading.value = true
            _lastOcrResult.value = null
            
            val result = repository.performOcrOnMeterImage(bitmap)
            
            _lastOcrResult.value = result
            _ocrLoading.value = false
            onComplete(result.digits, result.status)
        }
    }

    /**
     * Commits a new water meter reading to the Local SQLite system and updates statistics.
     */
    fun commitWaterReading(
        customerId: String,
        currentValue: Double,
        previousValue: Double,
        status: String,
        confidence: Double,
        offlineMode: Boolean
    ) {
        viewModelScope.launch {
            val reading = Reading(
                id = UUID.randomUUID().toString(),
                customerId = customerId,
                readerId = "R-501", // Active simulated mobile employee
                value = currentValue,
                previousValue = previousValue,
                consumption = (currentValue - previousValue).coerceAtLeast(0.0),
                imageUrl = "meter_scan_${System.currentTimeMillis()}.jpg",
                gpsLat = 32.37890 + (Math.random() - 0.5) * 0.015, // simulated reader alignment
                gpsLng = 35.31910 + (Math.random() - 0.5) * 0.015,
                timestamp = System.currentTimeMillis(),
                status = status,
                ocrConfidence = confidence
            )
            repository.saveReading(reading)

            if (offlineMode) {
                _offlineReadingsCount.value += 1
            }
        }
    }

    /**
     * Showcases active synchronisation to remote Cloud Servers.
     */
    fun triggerCloudSynchronization(onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val syncedCount = _offlineReadingsCount.value
            _offlineReadingsCount.value = 0
            onComplete(syncedCount)
        }
    }

    // Drop reading records for testing
    fun deleteReading(readingId: String) {
        viewModelScope.launch {
            repository.deleteReading(readingId)
        }
    }
}
