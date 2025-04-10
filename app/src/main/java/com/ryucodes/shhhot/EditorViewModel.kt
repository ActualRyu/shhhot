package com.ryucodes.shhhot

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Type to represent censoring mode
enum class CensorMode {
    BLOCK, // Blocks text with solid color
    HIDE,  // Hides text by matching background color
    BLUR   // Blurs the text
}

// Represents a detected word and its bounding box
data class DetectedWord(
    val text: String,
    val boundingBox: Rect,
    var isCensored: Boolean = false
)

// Represents a detected text line
data class DetectedTextLine(
    val words: List<DetectedWord>,
    val boundingBox: Rect
)

class EditorViewModel : ViewModel() {
    // Selected image
    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set
    
    var processedBitmap by mutableStateOf<Bitmap?>(null)
        private set
    
    // Text detection results
    var detectedTextLines by mutableStateOf<List<DetectedTextLine>>(emptyList())
        private set
    
    // Censoring mode
    var currentCensorMode by mutableStateOf(CensorMode.BLOCK)
        private set
    
    // Processing state
    var isProcessing by mutableStateOf(false)
        private set
    
    // Export success state
    private var _isExportSuccessful = mutableStateOf(false)
    val isExportSuccessful: Boolean get() = _isExportSuccessful.value
    
    // Text recognizer instance
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    // Set the selected image URI
    fun setSelectedImage(uri: Uri) {
        selectedImageUri = uri
        // Reset state when new image is selected
        detectedTextLines = emptyList()
        resetExportState()
    }
    
    // Process the image for text detection
    fun processImage(bitmap: Bitmap) {
        isProcessing = true
        processedBitmap = bitmap
        
        viewModelScope.launch(Dispatchers.IO) {
            val image = InputImage.fromBitmap(bitmap, 0)
            
            textRecognizer.process(image)
                .addOnSuccessListener { text ->
                    // Process the detected text
                    detectedTextLines = processTextRecognitionResult(text)
                    isProcessing = false
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    isProcessing = false
                }
        }
    }
    
    // Parse ML Kit Text result into our data model
    private fun processTextRecognitionResult(text: Text): List<DetectedTextLine> {
        val lines = mutableListOf<DetectedTextLine>()
        
        for (block in text.textBlocks) {
            for (line in block.lines) {
                val lineBox = line.boundingBox ?: continue
                val words = mutableListOf<DetectedWord>()
                
                for (element in line.elements) {
                    val wordBox = element.boundingBox ?: continue
                    words.add(
                        DetectedWord(
                            text = element.text,
                            boundingBox = wordBox
                        )
                    )
                }
                
                lines.add(
                    DetectedTextLine(
                        words = words,
                        boundingBox = lineBox
                    )
                )
            }
        }
        
        return lines
    }
    
    // Toggle censoring for a specific word
    fun toggleWordCensoring(lineIndex: Int, wordIndex: Int) {
        val updatedLines = detectedTextLines.toMutableList()
        val line = updatedLines[lineIndex]
        val words = line.words.toMutableList()
        val word = words[wordIndex]
        
        words[wordIndex] = word.copy(isCensored = !word.isCensored)
        updatedLines[lineIndex] = line.copy(words = words)
        
        detectedTextLines = updatedLines
    }
    
    // Change the current censoring mode
    fun setCensorMode(mode: CensorMode) {
        // Update the censoring mode and make sure it's applied
        if (currentCensorMode != mode) {
            currentCensorMode = mode
            // Force a small state update to ensure recomposition
            val updatedLines = detectedTextLines.toMutableList()
            if (updatedLines.isNotEmpty()) {
                detectedTextLines = updatedLines
            }
        }
    }
    
    // Mark export as successful
    fun markExportSuccessful() {
        _isExportSuccessful.value = true
    }
    
    // Mark export as not successful (reset)
    fun resetExportState() {
        _isExportSuccessful.value = false
    }
    
    // Reset for new session
    fun reset() {
        selectedImageUri = null
        processedBitmap = null
        detectedTextLines = emptyList()
        resetExportState()
    }
}