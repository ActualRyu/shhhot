package com.ryucodes.shhhot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.activity.addCallback
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.ryucodes.shhhot.ui.theme.ShhhotTheme

class MainActivity : ComponentActivity() {
    private var showAbout by mutableStateOf(false)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Handle back button press
        onBackPressedDispatcher.addCallback(this) {
            if (showAbout) {
                showAbout = false // Go back to home screen instead of exiting
            } else {
                // If already on home screen, allow normal back behavior (exit app)
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
        
        setContent {
            ShhhotApp(
                showAbout = showAbout,
                onNavigateToAbout = { showAbout = true },
                onNavigateBack = { showAbout = false }
            )
        }
    }
}

@Composable
fun ShhhotApp(
    showAbout: Boolean,
    onNavigateToAbout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    ShhhotTheme {
        // Wrap both screens in a Box with the theme background color to prevent flashing
        // Apply safe drawing padding to handle edge-to-edge properly
        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.safeDrawing.asPaddingValues())
        ) {
            // HomeScreen with fade animation
            AnimatedVisibility(
                visible = !showAbout,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                HomeScreen(onNavigateToAbout = onNavigateToAbout)
            }
            
            // AboutScreen with fade animation
            AnimatedVisibility(
                visible = showAbout,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                AboutScreen(onNavigateBack = onNavigateBack)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ShhhotTheme {
        HomeScreen(onNavigateToAbout = {})
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    ShhhotTheme {
        AboutScreen(onNavigateBack = {})
    }
}