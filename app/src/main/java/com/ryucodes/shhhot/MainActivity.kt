package com.ryucodes.shhhot

import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
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
import kotlinx.coroutines.launch

// App screens enum
enum class AppScreen {
    HOME,
    ABOUT,
    EDITOR
}

class MainActivity : ComponentActivity() {
    // Current screen state
    private var currentScreen by mutableStateOf(AppScreen.HOME)
    
    // View model for the editor
    private val editorViewModel by lazy { EditorViewModel() }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Set up image picker launcher
        val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                // Set the selected image in the view model
                editorViewModel.setSelectedImage(it)
                
                // Load the bitmap for processing
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                editorViewModel.processImage(bitmap)
                
                // Navigate to editor
                currentScreen = AppScreen.EDITOR
            }
        }
        
        // Handle back button press
        onBackPressedDispatcher.addCallback(this) {
            when (currentScreen) {
                AppScreen.ABOUT, AppScreen.EDITOR -> {
                    currentScreen = AppScreen.HOME // Go back to home screen
                }
                AppScreen.HOME -> {
                    // If already on home screen, allow normal back behavior (exit app)
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        
        setContent {
            ShhhotApp(
                currentScreen = currentScreen,
                onNavigateToAbout = { currentScreen = AppScreen.ABOUT },
                onNavigateToHome = { currentScreen = AppScreen.HOME },
                onPickImage = { imagePickerLauncher.launch("image/*") },
                editorViewModel = editorViewModel
            )
        }
    }
}

@Composable
fun ShhhotApp(
    currentScreen: AppScreen,
    onNavigateToAbout: () -> Unit,
    onNavigateToHome: () -> Unit,
    onPickImage: () -> Unit,
    editorViewModel: EditorViewModel
) {
    ShhhotTheme {
        // Wrap all screens in a Box with the theme background color to prevent flashing
        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
        ) {
            // Home screen
            AnimatedVisibility(
                visible = currentScreen == AppScreen.HOME,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                HomeScreen(
                    onNavigateToAbout = onNavigateToAbout,
                    onPickImage = onPickImage
                )
            }
            
            // About screen
            AnimatedVisibility(
                visible = currentScreen == AppScreen.ABOUT,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                AboutScreen(onNavigateBack = onNavigateToHome)
            }
            
            // Editor screen
            AnimatedVisibility(
                visible = currentScreen == AppScreen.EDITOR,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                EditorScreen(
                    viewModel = editorViewModel,
                    onNavigateBack = onNavigateToHome,
                    onExportComplete = onNavigateToHome
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ShhhotTheme {
        HomeScreen(onNavigateToAbout = {}, onPickImage = {})
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    ShhhotTheme {
        AboutScreen(onNavigateBack = {})
    }
}