package com.example.engine

import android.app.Activity
import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ScannerEngine(private val context: Context) {

    fun getScannerClient(): GmsDocumentScanner {
        val options = GmsDocumentScannerOptions.Builder()
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .setGalleryImportAllowed(true)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG, GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
            .build()
        return GmsDocumentScanning.getClient(options)
    }

    suspend fun extractTextFromUris(uris: List<Uri>): String = withContext(Dispatchers.IO) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val stringBuilder = StringBuilder()

        for (uri in uris) {
            try {
                val image = InputImage.fromFilePath(context, uri)
                val text = suspendCancellableCoroutine<String> { continuation ->
                    recognizer.process(image)
                        .addOnSuccessListener { result ->
                            continuation.resume(result.text)
                        }
                        .addOnFailureListener { e ->
                            continuation.resumeWithException(e)
                        }
                }
                stringBuilder.append(text).append("\n\n")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        stringBuilder.toString().trim()
    }
}
