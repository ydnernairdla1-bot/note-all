package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.data.AppearanceSettings
import com.example.data.ImageStorageManager
import com.example.ui.theme.BackgroundMode

@Composable
fun AppBackgroundContainer(
    settings: AppearanceSettings,
    imageStorageManager: ImageStorageManager,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (settings.backgroundMode) {
            BackgroundMode.DEFAULT -> {
                // Uses MaterialTheme.colorScheme.background
            }
            BackgroundMode.GRADIENT -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(colors = settings.gradientPreset.colors)
                        )
                )
            }
            BackgroundMode.SOLID -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(settings.solidPreset.color)
                )
            }
            BackgroundMode.IMAGE -> {
                val imageFile = remember(settings.customImageName) {
                    settings.customImageName?.let { imageStorageManager.getImageFile(it) }
                }
                if (imageFile != null && imageFile.exists()) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = "Latar belakang aplikasi kustom",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.background.copy(
                                    alpha = settings.imageDimAlpha
                                )
                            )
                    )
                }
            }
        }

        content()
    }
}
