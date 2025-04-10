package com.ryucodes.shhhot

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToAbout: () -> Unit,
    onPickImage: () -> Unit
) {
    Scaffold(
        // Remove default content padding as we're handling it with edge-to-edge UI
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // App title at top
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .padding(top = 60.dp) // Increased from 40dp to 60dp to move it down
                    .align(Alignment.TopCenter)
                    .alpha(0.8f) // Subtle transparency for elegance
            )
            
            // Main content - centered button
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedButton(
                    onClick = onPickImage,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(12.dp), // Slightly rounded corners, Apple-style
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp, // Flat design like Apple
                        pressedElevation = 0.dp
                    ),
                    modifier = Modifier
                        .width(250.dp)
                        .height(56.dp)
                ) {
                    Text(
                        text = stringResource(R.string.button_text),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                
                // Add some spacer below the button
                Spacer(modifier = Modifier.height(60.dp))
            }
            
            // Info button at the bottom with a more elegant, subtle design
            IconButton(
                onClick = onNavigateToAbout,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info, // Outlined style is more Apple-like
                    contentDescription = stringResource(R.string.about_title),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), // Subtle coloring
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}