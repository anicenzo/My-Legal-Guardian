package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object OcrTextExtractor {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Runs OCR on a list of image Uris (e.g. returned by GmsDocumentScanner).
     */
    suspend fun extractTextFromImages(context: Context, imageUris: List<Uri>): String = withContext(Dispatchers.Default) {
        val stringBuilder = StringBuilder()
        for ((index, uri) in imageUris.withIndex()) {
            try {
                val inputImage = InputImage.fromFilePath(context, uri)
                val result = Tasks.await(recognizer.process(inputImage))
                stringBuilder.append("--- PAGE ${index + 1} ---\n")

                stringBuilder.append(result.text)
                stringBuilder.append("\n\n")
            } catch (e: Exception) {
                Log.e("OcrTextExtractor", "OCR failed on image page $index: ${e.message}")
            }
        }
        return@withContext stringBuilder.toString()
    }

    /**
     * Extracts text from a PDF file using PdfRenderer to render pages into bitmaps,
     * then processes each page bitmap via ML Kit Text Recognition.
     */
    suspend fun extractTextFromPdf(context: Context, pdfUri: Uri): String = withContext(Dispatchers.Default) {
        val stringBuilder = StringBuilder()
        var parcelFileDescriptor: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null

        try {
            // Open the PDF using content resolver
            parcelFileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
            if (parcelFileDescriptor != null) {
                pdfRenderer = PdfRenderer(parcelFileDescriptor)
                val pageCount = pdfRenderer.pageCount
                
                // Cap pages for free tier (up to 5 pages), handled here as a default constraint
                // Let's implement this check. If they are pro we parse everything,
                // if they are free we only parse up to 5 pages as specified in Section 8!
                // Wait, we can pass isPro to this method or let the ViewModel handle the limit.
                // Let's parse everything in the extractor, and let the caller restrict the string or handle page bounds!
                // That keeps the extractor clean and generic.
                
                for (i in 0 until pageCount) {
                    val page = pdfRenderer.openPage(i)
                    
                    // Create a high-res bitmap of the page
                    val scale = 2f // Render at 2x resolution for high accuracy text recognition
                    val width = (page.width * scale).toInt()
                    val height = (page.height * scale).toInt()
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    
                    // Render PDF page into bitmap
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    // OCR on the generated page bitmap
                    val inputImage = InputImage.fromBitmap(bitmap, 0)
                    val result = Tasks.await(recognizer.process(inputImage))
                    
                    stringBuilder.append("--- PAGE ${i + 1} ---\n")
                    stringBuilder.append(result.text)
                    stringBuilder.append("\n\n")
                    
                    // Recycle bitmap to free memory on budget devices
                    bitmap.recycle()
                }
            }
        } catch (e: Exception) {
            Log.e("OcrTextExtractor", "OCR failed on PDF: ${e.message}")
            throw e
        } finally {
            try {
                pdfRenderer?.close()
                parcelFileDescriptor?.close()
            } catch (e: Exception) {
                Log.e("OcrTextExtractor", "Error closing renderer: ${e.message}")
            }
        }

        return@withContext stringBuilder.toString()
    }
}
