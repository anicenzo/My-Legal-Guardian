package com.example.engine

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.text.TextPaint
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class PdfExportEngine(private val context: Context) {

    fun generateAuditReport(
        documentTitle: String,
        isHighRisk: Boolean,
        riskScore: Int,
        redFlags: List<MatchedRedFlag>,
        missingClauses: List<String>
    ): Uri? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = TextPaint().apply {
            textSize = 20f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = Color.rgb(11, 19, 43) // Navy
        }

        val headerPaint = TextPaint().apply {
            textSize = 13f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = Color.rgb(28, 37, 65)
        }

        val subheaderPaint = TextPaint().apply {
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = Color.rgb(28, 37, 65)
        }

        val bodyPaint = TextPaint().apply {
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            color = Color.rgb(40, 40, 40)
        }

        val quotePaint = TextPaint().apply {
            textSize = 9f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC)
            color = Color.rgb(90, 102, 119)
        }

        val marginX = 40f
        val contentWidth = pageWidth - 80f
        var currentY = 50f

        fun checkPageBreak(neededHeight: Float) {
            if (currentY + neededHeight > pageHeight - 50f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = 50f
            }
        }

        // App & Document Header
        canvas.drawText("My Legal Guardian — Confidential Audit Report", marginX, currentY, headerPaint)
        currentY += 24f
        canvas.drawText("Document: $documentTitle", marginX, currentY, titlePaint)
        currentY += 28f

        // Risk Badge / Summary Bar
        val riskLabel = when {
            riskScore > 60 || isHighRisk -> "HIGH RISK DETECTED ($riskScore/100)"
            riskScore > 30 -> "MODERATE RISK ($riskScore/100)"
            else -> "LOW RISK / SAFE ($riskScore/100)"
        }
        val riskColor = when {
            riskScore > 60 || isHighRisk -> Color.rgb(217, 4, 41)
            riskScore > 30 -> Color.rgb(233, 163, 25)
            else -> Color.rgb(42, 157, 143)
        }

        val statusPaint = TextPaint(headerPaint).apply {
            color = riskColor
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        canvas.drawText("Audit Summary: $riskLabel", marginX, currentY, statusPaint)
        currentY += 20f

        // Divider
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        canvas.drawLine(marginX, currentY, marginX + contentWidth, currentY, linePaint)
        currentY += 20f

        // Predatory Clauses Section
        checkPageBreak(30f)
        canvas.drawText("1. Predatory & High-Risk Clauses (${redFlags.size} Found)", marginX, currentY, headerPaint)
        currentY += 20f

        if (redFlags.isEmpty()) {
            canvas.drawText("✓ No predatory or unfair clauses identified.", marginX + 10f, currentY, bodyPaint)
            currentY += 20f
        } else {
            for ((index, flag) in redFlags.withIndex()) {
                checkPageBreak(50f)
                val flagSevColor = if (flag.severity == 3) Color.rgb(217, 4, 41) else Color.rgb(233, 163, 25)
                val flagPaint = TextPaint(subheaderPaint).apply { color = flagSevColor }

                canvas.drawText("${index + 1}. [Severity ${flag.severity}] ${flag.displayName}", marginX + 10f, currentY, flagPaint)
                currentY += 16f

                // Explanation
                canvas.drawText("Explanation: ${flag.explanation}", marginX + 16f, currentY, bodyPaint)
                currentY += 14f

                if (flag.matchedSnippet.isNotBlank()) {
                    val snippet = if (flag.matchedSnippet.length > 120) flag.matchedSnippet.take(117) + "..." else flag.matchedSnippet
                    canvas.drawText("Quote: \"$snippet\"", marginX + 16f, currentY, quotePaint)
                    currentY += 16f
                }
                currentY += 6f
            }
        }

        // Missing Safeguards Section
        currentY += 10f
        checkPageBreak(30f)
        canvas.drawText("2. Statutory & Essential Safeguards Analysis", marginX, currentY, headerPaint)
        currentY += 20f

        if (missingClauses.isEmpty()) {
            canvas.drawText("✓ All standard tenant / contractor protection clauses are present.", marginX + 10f, currentY, bodyPaint)
            currentY += 20f
        } else {
            canvas.drawText("The following recommended protective clauses were missing from the draft:", marginX + 10f, currentY, bodyPaint)
            currentY += 16f
            for (clause in missingClauses) {
                checkPageBreak(20f)
                canvas.drawText("• Missing: $clause", marginX + 20f, currentY, bodyPaint)
                currentY += 16f
            }
        }

        // Footer Note
        currentY += 20f
        checkPageBreak(30f)
        canvas.drawLine(marginX, currentY, marginX + contentWidth, currentY, linePaint)
        currentY += 16f
        canvas.drawText("Generated 100% offline & securely by My Legal Guardian (Anixium Studios).", marginX, currentY, quotePaint)

        pdfDocument.finishPage(page)

        return try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "AuditReport_${System.currentTimeMillis()}.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}
