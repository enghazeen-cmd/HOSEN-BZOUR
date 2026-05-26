package com.example.data.database

import androidx.room.*
import com.example.data.models.Customer
import com.example.data.models.Reading
import com.example.data.models.Zone
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {

    // Zones
    @Query("SELECT * FROM zones ORDER BY id ASC")
    fun getAllZones(): Flow<List<Zone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZones(zones: List<Zone>)

    @Query("SELECT * FROM zones WHERE id = :zoneId")
    suspend fun getZoneById(zoneId: String): Zone?

    // Customers
    @Query("SELECT * FROM customers ORDER BY nameAr ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :customerId")
    suspend fun getCustomerById(customerId: String): Customer?

    @Query("SELECT * FROM customers WHERE id = :customerId")
    fun getCustomerFlowById(customerId: String): Flow<Customer?>

    @Query("SELECT * FROM customers WHERE zoneId = :zoneId ORDER BY nameAr ASC")
    fun getCustomersByZone(zoneId: String): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE nameAr LIKE '%' || :query || '%' OR nameEn LIKE '%' || :query || '%' OR meterNumber LIKE '%' || :query || '%'")
    fun searchCustomers(query: String): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<Customer>)

    @Update
    suspend fun updateCustomer(customer: Customer)

    // Readings
    @Query("SELECT * FROM readings ORDER BY timestamp DESC")
    fun getAllReadings(): Flow<List<Reading>>

    @Query("SELECT * FROM readings WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getReadingsForCustomer(customerId: String): Flow<List<Reading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: Reading)

    @Query("DELETE FROM readings WHERE id = :readingId")
    suspend fun deleteReading(readingId: String)

    // Quick Stats Calculations
    @Query("SELECT COUNT(*) FROM readings")
    fun getReadingCountFlow(): Flow<Int>

    @Query("SELECT SUM(consumption) FROM readings")
    fun getTotalConsumptionFlow(): Flow<Double?>
}
