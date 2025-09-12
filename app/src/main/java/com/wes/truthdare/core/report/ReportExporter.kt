package com.wes.truthdare.core.report

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material3.MaterialTheme
import com.wes.truthdare.ui.theme.DarkColorScheme
import com.wes.truthdare.ui.theme.LightColorScheme
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Theme for report export
 */
enum class Theme {
    LIGHT, DARK
}

/**
 * Interface for exporting reports to different formats
 */
interface ReportExporter {
    /**
     * Export a report to PDF
     * @param report The report to export
     * @param theme The theme to use for the PDF
     * @param watermark Whether to include a watermark
     * @return The exported PDF file
     */
    suspend fun toPdf(report: Report, theme: Theme, watermark: Boolean): File
    
    /**
     * Export a report to CSV
     * @param report The report to export
     * @return The exported CSV file
     */
    suspend fun toCsv(report: Report): File
    
    /**
     * Export a report to JSON
     * @param report The report to export
     * @return The exported JSON file
     */
    suspend fun toJson(report: Report): File
}

/**
 * Default implementation of ReportExporter
 */
class DefaultReportExporter @Inject constructor(
    private val context: Context
) : ReportExporter {
    
    override suspend fun toPdf(report: Report, theme: Theme, watermark: Boolean): File {
        // Create PDF document
        val document = PdfDocument()
        
        // Set up colors based on theme
        val backgroundColor = when (theme) {
            Theme.LIGHT -> Color.WHITE
            Theme.DARK -> Color.rgb(18, 18, 18) // Dark background
        }
        
        val textColor = when (theme) {
            Theme.LIGHT -> Color.BLACK
            Theme.DARK -> Color.WHITE
        }
        
        val accentColor = when (theme) {
            Theme.LIGHT -> LightColorScheme.primary.toArgb()
            Theme.DARK -> DarkColorScheme.primary.toArgb()
        }
        
        // Create page
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        
        // Set up paints
        val titlePaint = Paint().apply {
            color = textColor
            textSize = 24f
            isFakeBoldText = true
        }
        
        val subtitlePaint = Paint().apply {
            color = textColor
            textSize = 18f
            isFakeBoldText = true
        }
        
        val bodyPaint = Paint().apply {
            color = textColor
            textSize = 12f
        }
        
        val highlightPaint = Paint().apply {
            color = accentColor
            textSize = 14f
            isFakeBoldText = true
        }
        
        // Fill background
        canvas.drawColor(backgroundColor)
        
        // Draw title
        canvas.drawText("Truth or Dare Deluxe - Session Report", 50f, 50f, titlePaint)
        
        // Draw summary
        canvas.drawText("Game Summary", 50f, 100f, subtitlePaint)
        canvas.drawText("Mode: ${report.summary.gameMode}", 50f, 120f, bodyPaint)
        canvas.drawText("Date: ${formatInstant(report.summary.startTime)}", 50f, 140f, bodyPaint)
        canvas.drawText("Duration: ${formatDuration(report.summary.duration)}", 50f, 160f, bodyPaint)
        canvas.drawText("Players: ${report.summary.playerCount}", 50f, 180f, bodyPaint)
        canvas.drawText("Rounds: ${report.summary.roundCount}", 50f, 200f, bodyPaint)
        canvas.drawText("Truths: ${report.summary.truthCount}", 50f, 220f, bodyPaint)
        canvas.drawText("Dares: ${report.summary.dareCount}", 50f, 240f, bodyPaint)
        
        if (report.summary.drinkMode) {
            canvas.drawText("Total Shots: ${report.summary.totalShots}", 50f, 260f, bodyPaint)
        }
        
        // Draw player stats
        canvas.drawText("Player Statistics", 50f, 300f, subtitlePaint)
        var yPos = 320f
        
        report.perPlayer.forEach { player ->
            canvas.drawText("${player.playerName}:", 50f, yPos, highlightPaint)
            yPos += 20f
            canvas.drawText("Truths: ${player.truthsAnswered}, Dares: ${player.daresCompleted}", 70f, yPos, bodyPaint)
            yPos += 20f
            canvas.drawText("Completion Rate: ${(player.completionRate * 100).toInt()}%", 70f, yPos, bodyPaint)
            yPos += 20f
            
            if (report.summary.drinkMode) {
                canvas.drawText("Shots Taken: ${player.shotsTaken}", 70f, yPos, bodyPaint)
                yPos += 20f
            }
            
            yPos += 10f // Add some space between players
        }
        
        // Draw highlights
        if (report.highlights.isNotEmpty()) {
            canvas.drawText("Highlights", 50f, yPos, subtitlePaint)
            yPos += 20f
            
            report.highlights.forEach { highlight ->
                canvas.drawText(highlight.title, 50f, yPos, highlightPaint)
                yPos += 20f
                canvas.drawText(highlight.description, 70f, yPos, bodyPaint)
                yPos += 30f
            }
        }
        
        // Add watermark if needed
        if (watermark) {
            val watermarkPaint = Paint().apply {
                color = Color.argb(50, 128, 128, 128) // Semi-transparent gray
                textSize = 30f
                isFakeBoldText = true
            }
            
            canvas.save()
            canvas.rotate(45f, pageInfo.pageWidth / 2f, pageInfo.pageHeight / 2f)
            canvas.drawText(
                "Truth or Dare Deluxe",
                pageInfo.pageWidth / 2f - 150f,
                pageInfo.pageHeight / 2f,
                watermarkPaint
            )
            canvas.restore()
        }
        
        // Finish page
        document.finishPage(page)
        
        // Create output file
        val fileName = "TruthOrDare_Report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        
        // Write to file
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        
        // Close document
        document.close()
        
        return file
    }
    
    override suspend fun toCsv(report: Report): File {
        val fileName = "TruthOrDare_Report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.csv"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        
        file.bufferedWriter().use { writer ->
            // Write header
            writer.write("Truth or Dare Deluxe - Session Report\n\n")
            
            // Write summary
            writer.write("SUMMARY\n")
            writer.write("Mode,${report.summary.gameMode}\n")
            writer.write("Date,${formatInstant(report.summary.startTime)}\n")
            writer.write("Duration,${formatDuration(report.summary.duration)}\n")
            writer.write("Players,${report.summary.playerCount}\n")
            writer.write("Rounds,${report.summary.roundCount}\n")
            writer.write("Truths,${report.summary.truthCount}\n")
            writer.write("Dares,${report.summary.dareCount}\n")
            
            if (report.summary.drinkMode) {
                writer.write("Total Shots,${report.summary.totalShots}\n")
            }
            
            writer.write("\n")
            
            // Write player stats
            writer.write("PLAYER STATISTICS\n")
            writer.write("Player,Truths,Dares,Completion Rate,Shots\n")
            
            report.perPlayer.forEach { player ->
                writer.write("${player.playerName},${player.truthsAnswered},${player.daresCompleted},${(player.completionRate * 100).toInt()}%,${player.shotsTaken}\n")
            }
            
            writer.write("\n")
            
            // Write rounds
            writer.write("ROUNDS\n")
            writer.write("Round,Player,Type,Question,Answer,Completed,Penalties\n")
            
            report.rounds.forEach { round ->
                val answer = round.answer?.replace(",", ";")?.replace("\n", " ") ?: ""
                val question = round.question.replace(",", ";").replace("\n", " ")
                
                writer.write("${round.roundNumber},${round.playerName},${round.cardType},&quot;${question}&quot;,&quot;${answer}&quot;,${round.completed},${round.penalties}\n")
            }
            
            writer.write("\n")
            
            // Write highlights
            if (report.highlights.isNotEmpty()) {
                writer.write("HIGHLIGHTS\n")
                writer.write("Title,Description\n")
                
                report.highlights.forEach { highlight ->
                    val description = highlight.description.replace(",", ";").replace("\n", " ")
                    writer.write("${highlight.title},&quot;${description}&quot;\n")
                }
            }
        }
        
        return file
    }
    
    override suspend fun toJson(report: Report): File {
        val fileName = "TruthOrDare_Report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.json"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        
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
        
        // Add player stats
        val players = JSONArray()
        report.perPlayer.forEach { player ->
            val playerJson = JSONObject().apply {
                put("playerId", player.playerId)
                put("playerName", player.playerName)
                put("truthsAnswered", player.truthsAnswered)
                put("daresCompleted", player.daresCompleted)
                put("skipped", player.skipped)
                put("shotsTaken", player.shotsTaken)
                put("completionRate", player.completionRate)
                
                val categories = JSONArray()
                player.mostCommonCategories.forEach { categories.put(it) }
                put("mostCommonCategories", categories)
            }
            players.put(playerJson)
        }
        json.put("players", players)
        
        // Add rounds
        val rounds = JSONArray()
        report.rounds.forEach { round ->
            val roundJson = JSONObject().apply {
                put("roundNumber", round.roundNumber)
                put("playerName", round.playerName)
                put("cardType", round.cardType)
                put("question", round.question)
                put("answer", round.answer ?: JSONObject.NULL)
                put("completed", round.completed)
                put("penalties", round.penalties)
                put("timestamp", round.timestamp.toString())
            }
            rounds.put(roundJson)
        }
        json.put("rounds", rounds)
        
        // Add highlights
        val highlights = JSONArray()
        report.highlights.forEach { highlight ->
            val highlightJson = JSONObject().apply {
                put("type", highlight.type.toString())
                put("title", highlight.title)
                put("description", highlight.description)
                put("playerName", highlight.playerName ?: JSONObject.NULL)
                put("roundNumber", highlight.roundNumber ?: JSONObject.NULL)
            }
            highlights.put(highlightJson)
        }
        json.put("highlights", highlights)
        
        // Write to file
        file.writeText(json.toString(2))
        
        return file
    }
    
    /**
     * Format an Instant to a readable date/time string
     */
    private fun formatInstant(instant: Instant): String {
        val formatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }
    
    /**
     * Format a Duration to a readable string
     */
    private fun formatDuration(duration: java.time.Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        
        return if (hours > 0) {
            "$hours hours, $minutes minutes"
        } else {
            "$minutes minutes"
        }
    }
}