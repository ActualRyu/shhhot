package com.ryucodes.shhhot

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
                                // Create censored bitmap and save it
                                val censoredBitmap = ImageUtils.createCensoredBitmap(
                                    bitmap,
                                    viewModel.detectedTextLines,
                                    viewModel.currentCensorMode
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
                            // Display the image
                            AsyncImage(
                                model = bitmap,
                                contentDescription = "Selected Image",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Overlay for detected text
                            DetectedTextOverlay(
                                detectedLines = viewModel.detectedTextLines,
                                onWordClick = { lineIndex, wordIndex ->
                                    viewModel.toggleWordCensoring(lineIndex, wordIndex)
                                }
                            )
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

@Composable
fun DetectedTextOverlay(
    detectedLines: List<DetectedTextLine>,
    onWordClick: (Int, Int) -> Unit
) {
    // For each line of text
    detectedLines.forEachIndexed { lineIndex, line ->
        // For each word in the line
        line.words.forEachIndexed { wordIndex, word ->
            // Calculate position relative to the image size
            val left = word.boundingBox.left.toFloat()
            val top = word.boundingBox.top.toFloat()
            val width = word.boundingBox.width().toFloat()
            val height = word.boundingBox.height().toFloat()
            
            // Create a clickable overlay for this word
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = left
                        translationY = top
                    }
                    .size(width = width.dp, height = height.dp)
                    .border(
                        width = 1.dp,
                        color = if (word.isCensored) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(2.dp)
                    )
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (word.isCensored)
                            MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                    .clickable {
                        onWordClick(lineIndex, wordIndex)
                    }
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
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                onClick = { onModeSelected(CensorMode.BLOCK) },
                selected = selectedMode == CensorMode.BLOCK
            ) {
                Text("Block")
            }
            
            // Hide mode
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                onClick = { onModeSelected(CensorMode.HIDE) },
                selected = selectedMode == CensorMode.HIDE
            ) {
                Text("Hide")
            }
            
            // Blur mode
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                onClick = { onModeSelected(CensorMode.BLUR) },
                selected = selectedMode == CensorMode.BLUR
            ) {
                Text("Blur")
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