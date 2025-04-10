package com.ryucodes.shhhot

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
// Remove unused imports
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageUtils {
    
    // Create a censored bitmap based on the original and detected words
    fun createCensoredBitmap(
        original: Bitmap,
        detectedLines: List<DetectedTextLine>,
        defaultCensorMode: CensorMode // This is just the default, we'll use per-word mode
    ): Bitmap {
        // Create a mutable copy of the bitmap
        val result = original.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        
        // For each detected line
        for (line in detectedLines) {
            // Process each word in the line
            for (word in line.words) {
                // Only censor if marked as censored
                if (word.isCensored) {
                    // Expand bounding box by 3 pixels on all sides to prevent text seeping through
                    val expandedBox = Rect(
                        word.boundingBox.left - 3,
                        word.boundingBox.top - 3,
                        word.boundingBox.right + 3,
                        word.boundingBox.bottom + 3
                    )
                    
                    // Use the word's own censoring mode
                    when (word.censorMode) {
                        CensorMode.BLOCK -> {
                            // Use fixed colors based on text - black for now
                            // This ensures consistency across all blocks
                            val paint = Paint().apply {
                                color = Color.BLACK
                                style = Paint.Style.FILL
                            }
                            canvas.drawRect(expandedBox, paint)
                        }
                        
                        CensorMode.HIDE -> {
                            // Sample background color from above the text
                            val bgColor = sampleBackgroundColor(original, word.boundingBox)
                            
                            val paint = Paint().apply {
                                color = bgColor
                                style = Paint.Style.FILL
                            }
                            canvas.drawRect(expandedBox, paint)
                        }
                    }
                }
            }
        }
        
        return result
    }
    
    // Sample background color from near the bounding box
    private fun sampleBackgroundColor(bitmap: Bitmap, box: Rect): Int {
        // Sample from multiple points around the text
        val samplePoints = listOf(
            Pair(box.left + box.width() / 2, (box.top - 5).coerceAtLeast(0)), // Above
            Pair(box.left - 5, box.top + box.height() / 2), // Left
            Pair(box.right + 5, box.top + box.height() / 2), // Right
            Pair(box.left + box.width() / 2, box.bottom + 5) // Below
        )
        
        // Sample colors and get the most common one
        val colors = samplePoints.map { (x, y) ->
            val safeX = x.coerceIn(0, bitmap.width - 1)
            val safeY = y.coerceIn(0, bitmap.height - 1)
            bitmap.getPixel(safeX, safeY)
        }
        
        // Return most frequent background color
        return colors.groupBy { it }.maxByOrNull { it.value.size }?.key ?: Color.WHITE
    }
    
    // Save bitmap to gallery
    suspend fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Uri? {
        return withContext(Dispatchers.IO) {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val filename = "Shhhot_$timestamp.jpg"
            var fos: OutputStream? = null
            var uri: Uri? = null
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // For Android 10 and above
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    
                    uri = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )
                    
                    uri?.let {
                        fos = context.contentResolver.openOutputStream(it)
                    }
                } else {
                    // For versions below Android 10
                    val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val image = File(imagesDir, filename)
                    fos = FileOutputStream(image)
                    uri = Uri.fromFile(image)
                }
                
                fos?.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
                }
                
                uri
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}