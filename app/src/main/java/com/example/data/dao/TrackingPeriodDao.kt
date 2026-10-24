package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TrackingPeriod
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackingPeriodDao {
    @Query("SELECT * FROM tracking_periods WHERE isActive = 1 ORDER BY id DESC LIMIT 1")
    fun getActivePeriodFlow(): Flow<TrackingPeriod?>

    @Query("SELECT * FROM tracking_periods WHERE isActive = 1 ORDER BY id DESC LIMIT 1")
    suspend fun getActivePeriod(): TrackingPeriod?

    @Query("SELECT * FROM tracking_periods ORDER BY id DESC")
    fun getAllPeriodsFlow(): Flow<List<TrackingPeriod>>

    @Query("SELECT * FROM tracking_periods ORDER BY id DESC")
    suspend fun getAllPeriods(): List<TrackingPeriod>

    @Query("SELECT * FROM tracking_periods WHERE id = :id LIMIT 1")
    suspend fun getPeriodById(id: Long): TrackingPeriod?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeriod(period: TrackingPeriod): Long

    @Update
    suspend fun updatePeriod(period: TrackingPeriod)
}
