package com.example.vetbook.presentation.components.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.vetbook.R
import com.example.vetbook.presentation.theme.HealthPrimary

@Composable
fun VetBookImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    contentScale: ContentScale = ContentScale.Crop,
    crossfade: Boolean = true,
    initials: String? = null,
    fallbackIcon: ImageVector? = null,
    fallbackIconSize: Dp = 24.dp,
    containerColor: Color = HealthPrimary.copy(alpha = 0.1f)
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (model != null && (model !is String || model.isNotEmpty())) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(model)
                        .crossfade(crossfade)
                        .build(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale,
                    error = painterResource(R.drawable.pawns) // Temporary error fallback
                )
            } else {
                FallbackContent(
                    initials = initials,
                    fallbackIcon = fallbackIcon,
                    fallbackIconSize = fallbackIconSize
                )
            }
        }
    }
}

@Composable
private fun FallbackContent(
    initials: String? = null,
    fallbackIcon: ImageVector? = null,
    fallbackIconSize: Dp = 24.dp
) {
    when {
        !initials.isNullOrEmpty() -> {
            Text(
                text = initials.take(2).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = HealthPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        fallbackIcon != null -> {
            Icon(
                imageVector = fallbackIcon,
                contentDescription = null,
                tint = HealthPrimary.copy(alpha = 0.6f),
                modifier = Modifier.size(fallbackIconSize)
            )
        }
        else -> {
            Image(
                painter = painterResource(R.drawable.pawns),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                alpha = 0.3f
            )
        }
    }
}
