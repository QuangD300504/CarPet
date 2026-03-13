package com.example.vetbook.presentation.screens.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    var isOpened by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val offsetAnim by animateFloatAsState(
        targetValue = if (isOpened) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "diagonalShutterAnimation"
    )

    // Trigger animation automatically
    LaunchedEffect(Unit) {
        delay(500)
        isOpened = true
    }

    // Navigate when animation finishes
    LaunchedEffect(offsetAnim) {
        if (offsetAnim >= 1f) {
            onAnimationFinished()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(HealthSurface)) {
        if (offsetAnim < 1f) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top-Left Half - Slides Left
                DiagonalHalfPanel(
                    modifier = Modifier.fillMaxSize(),
                    isTopLeftHalf = true,
                    progress = offsetAnim,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight
                )

                // Bottom-Right Half - Slides Right
                DiagonalHalfPanel(
                    modifier = Modifier.fillMaxSize(),
                    isTopLeftHalf = false,
                    progress = offsetAnim,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight
                )
            }
        }
    }
}

@Composable
fun DiagonalHalfPanel(
    modifier: Modifier,
    isTopLeftHalf: Boolean,
    progress: Float,
    screenWidth: Dp,
    screenHeight: Dp
) {
    val diagonalShape = remember(isTopLeftHalf) {
        object : Shape {
            override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
                val path = Path().apply {
                    if (isTopLeftHalf) {
                        moveTo(0f,0f)
                        lineTo(size.width, 0f)
                        lineTo(0f, size.height)
                        close()
                    } else {
                        moveTo(size.width, size.height)
                        lineTo(0f, size.height)
                        lineTo(size.width, 0f)
                        close()
                    }
                }
                return Outline.Generic(path)
            }
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = if (isTopLeftHalf) {
                    -progress * screenWidth.toPx()
                } else {
                    progress * screenWidth.toPx()
                }
            }
            .clip(diagonalShape)
            .background(HealthPrimary)
            .border(1.dp, Color.Black.copy(alpha = 0.1f), diagonalShape)
    )
}
