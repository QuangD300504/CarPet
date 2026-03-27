package com.example.vetbook.presentation.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.presentation.theme.*

/** Message types matching the app's visual language. */
enum class SnackbarType {
    Success,
    Error,
    Info,
}

private data class TypeStyle(
    val icon: ImageVector,
    val backgroundColor: Color,
    val accentColor: Color,
    val contentColor: Color,
)

private val TypeConfig = mapOf(
    SnackbarType.Success to TypeStyle(
        icon = Icons.Filled.CheckCircle,
        backgroundColor = Color(0xFFECFDF5),
        accentColor = Success,
        contentColor = Color(0xFF065F46),
    ),
    SnackbarType.Error to TypeStyle(
        icon = Icons.Filled.Error,
        backgroundColor = Color(0xFFFEE2E2),
        accentColor = Error,
        contentColor = Color(0xFF7F1D1D),
    ),
    SnackbarType.Info to TypeStyle(
        icon = Icons.Filled.Info,
        backgroundColor = HealthSurface,
        accentColor = HealthPrimary,
        contentColor = HealthPrimary,
    ),
)

/**
 * Shows a single VetBook-styled snackbar.
 *
 * Usage:
 * ```kotlin
 * val snackbarHostState = remember { SnackbarHostState() }
 * val scope = rememberCoroutineScope()
 *
 * scope.launch {
 *     VetBookSnackbar.show(
 *         snackbarHostState = snackbarHostState,
 *         message = "Added to cart!",
 *         type = SnackbarType.Success
 *     )
 * }
 * ```
 */
object VetBookSnackbar {
    suspend fun show(
        snackbarHostState: SnackbarHostState,
        message: String,
        type: SnackbarType = SnackbarType.Info,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) {
        // Dismiss any currently visible snackbar immediately so the new one
        // replaces it without waiting for the previous duration to expire.
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(
            message = "$type│$message",
            duration = duration,
        )
    }
}

/** Custom SnackbarHost that renders VetBook-styled snackbars. */
@Composable
fun VetBookSnackbarHost(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = snackbarHostState) { data ->
        val (type, message) = data.visuals.message.split("│", limit = 2).let { parts ->
            if (parts.size == 2) {
                SnackbarType.valueOf(parts[0]) to parts[1]
            } else {
                SnackbarType.Info to parts.getOrElse(0) { "" }
            }
        }
        val style = TypeConfig[type] ?: TypeConfig[SnackbarType.Info]!!

        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(style.backgroundColor)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = style.accentColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    color = style.contentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}