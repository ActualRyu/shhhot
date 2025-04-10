package com.ryucodes.shhhot

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onExportComplete: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // If export successful, show success screen and navigate back after delay
    if (viewModel.isExportSuccessful) {
        ExportSuccessScreen(onNavigateBack = onExportComplete)
        return
    }
    
    // Variables for image transformation (zoom, pan)
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    // Create a transformable state for handling gestures
    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 3f)
        offset += offsetChange
    }
    
    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.editor_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                actions = {
                    // Export button
                    IconButton(
                        onClick = {
                            viewModel.processedBitmap?.let { bitmap ->
                                // Create censored bitmap with the current mode
                                val selectedMode = viewModel.currentCensorMode
                                val censoredBitmap = ImageUtils.createCensoredBitmap(
                                    bitmap,
                                    viewModel.detectedTextLines,
                                    selectedMode // Use the selected mode directly
                                )
                                
                                // Launch coroutine to save bitmap
                                coroutineScope.launch {
                                    val uri = ImageUtils.saveBitmapToGallery(context, censoredBitmap)
                                    if (uri != null) {
                                        viewModel.markExportSuccessful()
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Export",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main content with image and detections
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.isProcessing) {
                    // Show loading indicator when processing
                    CircularProgressIndicator()
                } else {
                    // Show image with text detections
                    viewModel.processedBitmap?.let { bitmap ->
                        // We'll use a container Box for the entire image area
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Image with transformation capabilities
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        translationX = offset.x
                                        translationY = offset.y
                                    }
                                    .transformable(state = transformableState)
                            ) {
                                // Display the image with the text detections
                                AsyncImage(
                                model = viewModel.processedBitmap,
                                contentDescription = "Selected Image",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                                )
                                
                                // Let's use custom drawing instead
                                // Get colors outside the Canvas lambda since it's not a Composable context
                                val errorColor = MaterialTheme.colorScheme.error
                                val primaryColor = MaterialTheme.colorScheme.primary
                                
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(viewModel.detectedTextLines) {
                                            detectTapGestures { tapOffset ->
                                                // Calculate scaling factors again
                                                val canvasBitmap = viewModel.processedBitmap ?: return@detectTapGestures
                                                val canvasSizeX = size.width 
                                                val canvasSizeY = size.height
                                                
                                                val canvasScaleX = canvasSizeX / canvasBitmap.width.toFloat()
                                                val canvasScaleY = canvasSizeY / canvasBitmap.height.toFloat()
                                                val canvasScaleFactor = minOf(canvasScaleX, canvasScaleY)
                                                
                                                val canvasOffsetX = (canvasSizeX - canvasBitmap.width * canvasScaleFactor) / 2f
                                                val canvasOffsetY = (canvasSizeY - canvasBitmap.height * canvasScaleFactor) / 2f
                                                
                                                // Check if tap is inside any word box
                                                viewModel.detectedTextLines.forEachIndexed { lineIndex, line ->
                                                    line.words.forEachIndexed { wordIndex, word ->
                                                        // Convert bounding box to canvas coordinates
                                                        val boxLeft = word.boundingBox.left * canvasScaleFactor + canvasOffsetX
                                                        val boxTop = word.boundingBox.top * canvasScaleFactor + canvasOffsetY
                                                        val boxRight = word.boundingBox.right * canvasScaleFactor + canvasOffsetX
                                                        val boxBottom = word.boundingBox.bottom * canvasScaleFactor + canvasOffsetY
                                                        
                                                        // Check if tap is inside this word's box
                                                        if (tapOffset.x in boxLeft..boxRight && 
                                                           tapOffset.y in boxTop..boxBottom) {
                                                            // Toggle censoring for this word
                                                            viewModel.toggleWordCensoring(lineIndex, wordIndex)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                ) {
                                    // Calculate scaling factors
                                val bitmap = viewModel.processedBitmap ?: return@Canvas
                                
                                // Calculate the scaling factor based on the canvas size and bitmap size
                                val scaleX = size.width / bitmap.width.toFloat()
                                    val scaleY = size.height / bitmap.height.toFloat()
                                val scaleFactor = minOf(scaleX, scaleY)
                                
                                // Calculate starting point to center the image
                                val offsetX = (size.width - bitmap.width * scaleFactor) / 2f
                                val offsetY = (size.height - bitmap.height * scaleFactor) / 2f
                                
                                // Draw clickable areas for each word
                                for ((lineIndex, line) in viewModel.detectedTextLines.withIndex()) {
                                    for ((wordIndex, word) in line.words.withIndex()) {
                                        // Convert bounding box to canvas coordinates
                                        val left = word.boundingBox.left * scaleFactor + offsetX
                                        val top = word.boundingBox.top * scaleFactor + offsetY
                                        val right = word.boundingBox.right * scaleFactor + offsetX
                                        val bottom = word.boundingBox.bottom * scaleFactor + offsetY
                                        
                                        // Draw rectangle for this word
                                        drawRect(
                                            color = if (word.isCensored) 
                                                errorColor.copy(alpha = 0.3f)
                                            else 
                                                primaryColor.copy(alpha = 0.1f),
                                            topLeft = Offset(left, top),
                                            size = androidx.compose.ui.geometry.Size(
                                                width = right - left,
                                                height = bottom - top
                                            )
                                        )
                                        
                                        // Draw border
                                        drawRect(
                                            color = if (word.isCensored) 
                                                errorColor
                                            else 
                                                primaryColor.copy(alpha = 0.5f),
                                            topLeft = Offset(left, top),
                                            size = androidx.compose.ui.geometry.Size(
                                                width = right - left,
                                                height = bottom - top
                                            ),
                                            style = Stroke(width = 1f)
                                        )
                                    }
                                }
                            }
                            }
                        }
                    }
                }
            }
            
            // Bottom mode selector
            CensorModeSelector(
                selectedMode = viewModel.currentCensorMode,
                onModeSelected = { viewModel.setCensorMode(it) }
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CensorModeSelector(
    selectedMode: CensorMode,
    onModeSelected: (CensorMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Censoring Mode",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Block mode
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                onClick = { onModeSelected(CensorMode.BLOCK) },
                selected = selectedMode == CensorMode.BLOCK
            ) {
                Text("Block")
            }
            
            // Hide mode
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                onClick = { onModeSelected(CensorMode.HIDE) },
                selected = selectedMode == CensorMode.HIDE
            ) {
                Text("Hide")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ExportSuccessScreen(onNavigateBack: () -> Unit) {
    // Auto-navigate back after a delay
    LaunchedEffect(Unit) {
        delay(2000) // 2 seconds
        onNavigateBack()
    }
    
    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("") }, // Empty title
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Success icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Success",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Export Successful",
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}