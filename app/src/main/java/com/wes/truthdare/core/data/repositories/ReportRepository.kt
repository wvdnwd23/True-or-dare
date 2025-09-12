package com.wes.truthdare.core.data.repositories

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.wes.truthdare.core.data.GameMode
import com.wes.truthdare.core.data.models.SessionLog
import com.wes.truthdare.core.report.Report
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Entity for storing reports in the database
 */
@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val title: String,
    val gameMode: GameMode,
    val drinkMode: Boolean,
    val playerCount: Int,
    val roundCount: Int,
    val createdAt: Instant,
    val reportJson: String // Serialized Report object
)

/**
 * Type converters for Room
 */
class ReportTypeConverters {
    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilli()
    }
    
    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }
    
    @TypeConverter
    fun fromGameMode(value: GameMode?): String? {
        return value?.name
    }
    
    @TypeConverter
    fun toGameMode(value: String?): GameMode? {
        return value?.let { GameMode.valueOf(it) }
    }
}

/**
 * DAO for reports
 */
@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<ReportEntity>>
    
    @Query("SELECT * FROM reports WHERE id = :id")
    suspend fun getReportById(id: String): ReportEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)
    
    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteReport(id: String)
    
    @Query("DELETE FROM reports")
    suspend fun deleteAllReports()
    
    @Query("SELECT COUNT(*) FROM reports")
    suspend fun getReportCount(): Int
    
    @Query("SELECT * FROM reports ORDER BY createdAt ASC LIMIT 1")
    suspend fun getOldestReport(): ReportEntity?
}

/**
 * Database for reports
 */
@Database(entities = [ReportEntity::class], version = 1, exportSchema = false)
@TypeConverters(ReportTypeConverters::class)
abstract class ReportDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    
    companion object {
        @Volatile
        private var INSTANCE: ReportDatabase? = null
        
        fun getDatabase(context: Context): ReportDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReportDatabase::class.java,
                    "report_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Repository for managing reports
 */
@Singleton
class ReportRepository @Inject constructor(
    private val reportDao: ReportDao
) {
    companion object {
        private const val MAX_REPORTS = 5 // Maximum number of reports to store
    }
    
    /**
     * Get all reports
     */
    fun getAllReports(): Flow<List<ReportEntity>> {
        return reportDao.getAllReports()
    }
    
    /**
     * Get a report by ID
     */
    suspend fun getReportById(id: String): ReportEntity? {
        return reportDao.getReportById(id)
    }
    
    /**
     * Save a report
     * If there are already MAX_REPORTS reports, the oldest one will be deleted
     */
    suspend fun saveReport(report: Report, sessionLog: SessionLog): String {
        // Generate a unique ID for the report
        val reportId = java.util.UUID.randomUUID().toString()
        
        // Create a title for the report
        val title = "Game Report - ${formatInstant(report.summary.startTime)}"
        
        // Convert report to JSON
        val reportJson = reportToJson(report)
        
        // Create report entity
        val reportEntity = ReportEntity(
            id = reportId,
            sessionId = sessionLog.sessionId,
            title = title,
            gameMode = sessionLog.mode,
            drinkMode = sessionLog.drinkMode,
            playerCount = sessionLog.players.size,
            roundCount = sessionLog.rounds.size,
            createdAt = Instant.now(),
            reportJson = reportJson
        )
        
        // Check if we need to delete an old report
        val reportCount = reportDao.getReportCount()
        if (reportCount >= MAX_REPORTS) {
            // Delete the oldest report
            val oldestReport = reportDao.getOldestReport()
            oldestReport?.let {
                reportDao.deleteReport(it.id)
            }
        }
        
        // Save the new report
        reportDao.insertReport(reportEntity)
        
        return reportId
    }
    
    /**
     * Delete a report
     */
    suspend fun deleteReport(id: String) {
        reportDao.deleteReport(id)
    }
    
    /**
     * Delete all reports
     */
    suspend fun deleteAllReports() {
        reportDao.deleteAllReports()
    }
    
    /**
     * Convert a Report object to JSON
     */
    private fun reportToJson(report: Report): String {
        val json = JSONObject()
        
        // Add summary
        val summary = JSONObject().apply {
            put("sessionId", report.summary.sessionId)
            put("startTime", report.summary.startTime.toString())
            put("endTime", report.summary.endTime.toString())
            put("duration", report.summary.duration.toString())
            put("gameMode", report.summary.gameMode.toString())
            put("drinkMode", report.summary.drinkMode)
            put("playerCount", report.summary.playerCount)
            put("roundCount", report.summary.roundCount)
            put("truthCount", report.summary.truthCount)
            put("dareCount", report.summary.dareCount)
            put("skipCount", report.summary.skipCount)
            put("totalShots", report.summary.totalShots)
        }
        json.put("summary", summary)
        
        // We'll skip adding all the details to save space
        // In a real implementation, you might want to include more data
        
        return json.toString()
    }
    
    /**
     * Format an Instant to a readable date/time string
     */
    private fun formatInstant(instant: Instant): String {
        val date = java.util.Date.from(instant)
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return formatter.format(date)
    }
}