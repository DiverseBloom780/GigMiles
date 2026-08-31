package com.gigmiles.app.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "drive_records")
data class DriveRecord(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val app: String,
    val startedAt: Long,
    val endedAt: Long,
    val miles: Double,
    val basePay: Double = 0.0,
    val customerTips: Double = 0.0,
    val boost: Double = 0.0,
    val incentives: Double = 0.0,
    val appPay: Double = 0.0,
    val isHistorical: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "expenses")
data class ExpenseRecord(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val category: String,
    val description: String = "",
    val amount: Double,
    val businessUsePercent: Double = 100.0,
    val receiptPath: String? = null
)

@Dao
interface GigMilesDao {
    @Insert suspend fun insertDrive(record: DriveRecord): Long
    @Insert suspend fun insertExpense(record: ExpenseRecord): Long
    @Query("SELECT * FROM drive_records ORDER BY startedAt DESC")
    fun observeDrives(): Flow<List<DriveRecord>>
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun observeExpenses(): Flow<List<ExpenseRecord>>
    @Query("SELECT * FROM drive_records WHERE app = :app ORDER BY startedAt DESC")
    fun observeDrivesForApp(app: String): Flow<List<DriveRecord>>
}

@Database(entities = [DriveRecord::class, ExpenseRecord::class], version = 1, exportSchema = false)
abstract class GigMilesDatabase : RoomDatabase() {
    abstract fun gigMilesDao(): GigMilesDao
}
