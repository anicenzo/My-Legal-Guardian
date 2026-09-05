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
        missingClauses: List<String>,
        contractType: ContractType = ContractType.GENERAL_AGREEMENT
    ): Uri? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = TextPaint().apply {
            textSize = 18f
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

        fun drawWrappedText(text: String, x: Float, maxWidth: Float, paint: TextPaint, lineSpacing: Float = 14f) {
            val words = text.split(Regex("\\s+"))
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) > maxWidth) {
                    checkPageBreak(lineSpacing + 2f)
                    canvas.drawText(currentLine, x, currentY, paint)
                    currentY += lineSpacing
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) {
                checkPageBreak(lineSpacing + 2f)
                canvas.drawText(currentLine, x, currentY, paint)
                currentY += lineSpacing
            }
        }

        // App & Document Header
        canvas.drawText("Legal AI — Confidential Audit Report", marginX, currentY, headerPaint)
        currentY += 24f
        canvas.drawText("Document: $documentTitle", marginX, currentY, titlePaint)
        currentY += 22f
        canvas.drawText("Classification: ${contractType.displayName}", marginX, currentY, subheaderPaint)
        currentY += 24f

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
        checkPageBreak(35f)
        canvas.drawText("1. Predatory & High-Risk Clauses (${redFlags.size} Found)", marginX, currentY, headerPaint)
        currentY += 20f

        if (redFlags.isEmpty()) {
            canvas.drawText("✓ No predatory or unfair clauses identified.", marginX + 10f, currentY, bodyPaint)
            currentY += 20f
        } else {
            for ((index, flag) in redFlags.withIndex()) {
                checkPageBreak(60f)
                val flagSevColor = if (flag.severity == 3) Color.rgb(217, 4, 41) else Color.rgb(233, 163, 25)
                val flagPaint = TextPaint(subheaderPaint).apply { color = flagSevColor }

                canvas.drawText("${index + 1}. [Severity ${flag.severity}] ${flag.displayName}", marginX + 10f, currentY, flagPaint)
                currentY += 16f

                // Explanation with wrapping
                canvas.drawText("Explanation: ", marginX + 16f, currentY, bodyPaint)
                val explOffset = bodyPaint.measureText("Explanation: ")
                drawWrappedText(flag.explanation, marginX + 16f + explOffset, contentWidth - 26f - explOffset, bodyPaint, 13f)

                if (flag.matchedSnippet.isNotBlank()) {
                    checkPageBreak(20f)
                    canvas.drawText("Quote: ", marginX + 16f, currentY, quotePaint)
                    val quoteOffset = quotePaint.measureText("Quote: ")
                    val quoteText = "\"${flag.matchedSnippet.trim()}\""
                    drawWrappedText(quoteText, marginX + 16f + quoteOffset, contentWidth - 26f - quoteOffset, quotePaint, 12f)
                }
                currentY += 8f
            }
        }

        // Missing Safeguards Section
        currentY += 10f
        checkPageBreak(35f)
        canvas.drawText("2. Contextual Safeguards Analysis (${contractType.displayName})", marginX, currentY, headerPaint)
        currentY += 20f

        if (missingClauses.isEmpty()) {
            canvas.drawText("✓ All standard ${contractType.displayName.lowercase()} protection clauses are present.", marginX + 10f, currentY, bodyPaint)
            currentY += 20f
        } else {
            canvas.drawText("The following recommended protective clauses were missing from the draft:", marginX + 10f, currentY, bodyPaint)
            currentY += 16f
            for (clause in missingClauses) {
                checkPageBreak(22f)
                canvas.drawText("• Missing: $clause", marginX + 20f, currentY, bodyPaint)
                currentY += 16f
            }
        }

        // Footer Note
        currentY += 20f
        checkPageBreak(35f)
        canvas.drawLine(marginX, currentY, marginX + contentWidth, currentY, linePaint)
        currentY += 16f
        canvas.drawText("Generated 100% offline & securely by Legal AI — Contract Scanner (Anixium Studios).", marginX, currentY, quotePaint)

        pdfDocument.finishPage(page)

        return try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "AuditReport_${System.currentTimeMillis()}.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            if (com.example.BuildConfig.DEBUG) {
                android.util.Log.e("PdfExportEngine", "Failed to export PDF: ${e.message}", e)
            }
            com.example.util.LocalErrorLogger.record(context, "PdfExportEngine", "Failed to export PDF: ${e.message}", e)
            pdfDocument.close()
            null
        }
    }
}
