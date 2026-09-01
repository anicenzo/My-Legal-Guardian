package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.example.util.LocalErrorLogger
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object OcrTextExtractor {

    private const val TAG = "OcrTextExtractor"
    private const val MAX_RENDER_DIMENSION = 2048f // Clamped to prevent OOM on budget devices
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private suspend fun processImageAsync(inputImage: InputImage): String =
        suspendCancellableCoroutine { continuation ->
            recognizer.process(inputImage)
                .addOnSuccessListener { result ->
                    continuation.resume(result.text)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }

    /**
     * Runs OCR on a list of image Uris (e.g. returned by GmsDocumentScanner).
     */
    suspend fun extractTextFromImages(context: Context, imageUris: List<Uri>): String = withContext(Dispatchers.Default) {
        val stringBuilder = StringBuilder()
        for ((index, uri) in imageUris.withIndex()) {
            try {
                val inputImage = InputImage.fromFilePath(context, uri)
                val text = processImageAsync(inputImage)
                stringBuilder.append("--- PAGE ${index + 1} ---\n")
                stringBuilder.append(text)
                stringBuilder.append("\n\n")
            } catch (e: Exception) {
                LocalErrorLogger.record(context, TAG, "OCR failed on image page $index: ${e.message}", e)
            }
        }
        return@withContext stringBuilder.toString()
    }

    /**
     * Extracts text from a PDF file using PdfRenderer to render pages into bitmaps,
     * then processes each page bitmap via ML Kit Text Recognition with memory-safe dimensions.
     */
    suspend fun extractTextFromPdf(context: Context, pdfUri: Uri): String = withContext(Dispatchers.Default) {
        val stringBuilder = StringBuilder()
        var parcelFileDescriptor: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null

        try {
            parcelFileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
            if (parcelFileDescriptor != null) {
                pdfRenderer = PdfRenderer(parcelFileDescriptor)
                val pageCount = pdfRenderer.pageCount

                for (i in 0 until pageCount) {
                    val page = pdfRenderer.openPage(i)

                    // Safe dimension calculation clamped to MAX_RENDER_DIMENSION to prevent OOM
                    val maxSide = maxOf(page.width, page.height).toFloat()
                    val scale = if (maxSide > 0) minOf(2f, MAX_RENDER_DIMENSION / maxSide) else 1f
                    val width = (page.width * scale).toInt().coerceAtLeast(1)
                    val height = (page.height * scale).toInt().coerceAtLeast(1)

                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    try {
                        val inputImage = InputImage.fromBitmap(bitmap, 0)
                        val text = processImageAsync(inputImage)
                        stringBuilder.append("--- PAGE ${i + 1} ---\n")
                        stringBuilder.append(text)
                        stringBuilder.append("\n\n")
                    } finally {
                        bitmap.recycle() // Promptly recycle to free native graphic memory
                    }
                }
            }
        } catch (e: Exception) {
            LocalErrorLogger.record(context, TAG, "OCR failed on PDF: ${e.message}", e)
            throw e
        } finally {
            try {
                pdfRenderer?.close()
                parcelFileDescriptor?.close()
            } catch (e: Exception) {
                LocalErrorLogger.record(context, TAG, "Error closing PDF renderer: ${e.message}", e)
            }
        }

        return@withContext stringBuilder.toString()
    }
}
