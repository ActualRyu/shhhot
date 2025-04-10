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
        censorMode: CensorMode
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
                    // Expand the bounding box slightly for better coverage
                    val expandedBox = Rect(
                        word.boundingBox.left - 2,
                        word.boundingBox.top - 2,
                        word.boundingBox.right + 2,
                        word.boundingBox.bottom + 2
                    )
                    
                    when (censorMode) {
                        CensorMode.BLOCK -> {
                            // Get most common text color by sampling multiple points
                            val textColor = getDominantTextColor(original, word.boundingBox)
                            
                            val paint = Paint().apply {
                                color = textColor
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
                        
                        CensorMode.BLUR -> {
                            // White overlay with semi-transparency
                            val paint = Paint().apply {
                                color = Color.WHITE
                                style = Paint.Style.FILL
                                alpha = 180
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
        // Sample from just above the text
        val x = box.left + box.width() / 2
        val y = (box.top - 5).coerceAtLeast(0)
        
        return bitmap.getPixel(x, y)
    }
    
    // Get dominant text color by sampling multiple points in the text region
    private fun getDominantTextColor(bitmap: Bitmap, box: Rect): Int {
        // Define sampling points inside the text area
        val samplePoints = listOf(
            Pair(0.5f, 0.5f),  // Center
            Pair(0.25f, 0.5f), // Left-center
            Pair(0.75f, 0.5f), // Right-center
            Pair(0.5f, 0.25f), // Top-center
            Pair(0.5f, 0.75f)  // Bottom-center
        )
        
        // Sample colors from each point
        val colors = samplePoints.map { (xFrac, yFrac) ->
            val x = (box.left + box.width() * xFrac).toInt().coerceIn(0, bitmap.width - 1)
            val y = (box.top + box.height() * yFrac).toInt().coerceIn(0, bitmap.height - 1)
            bitmap.getPixel(x, y)
        }
        
        // Use the most frequent color (should be the text color)
        return colors.groupBy { it }.maxByOrNull { it.value.size }?.key ?: Color.BLACK
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