package com.example.vetbook.presentation.screens.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.presentation.components.CustomTextField
import com.example.vetbook.presentation.viewmodels.ContinueLoginViewModel
import kotlinx.coroutines.delay

/**
 * First intro screen - Auto-transitions after 1 second (or tap to skip)
 * This screen initializes the auth check in the background
 */
@Composable
fun ContinueLoginScreen(
    onNext: () -> Unit,
    viewModel: ContinueLoginViewModel = hiltViewModel()
) {
    val alpha = remember { Animatable(0f) }
    
    // Start fade-in animation
    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400)
        )
    }
    
    // Preload user data in background (viewModel.init already calls load())
    // This ensures data is ready by the time we reach the password screen
    
    // Auto-transition after 1 second (user can click to skip)
    LaunchedEffect(Unit) {
        delay(1000)
        onNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFD700))
            .alpha(alpha.value)
            .clickable { onNext() } // Allow tap to skip
    )
}

/**
 * Second intro screen - Auto-transitions after 0.8 seconds (or tap to skip)
 * Shows an animated circle scaling up
 */
@Composable
fun ContinueLoginStartScreen(
    onNext: () -> Unit
) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }
    
    // Animations
    LaunchedEffect(Unit) {
        // Fade in
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 300)
        )
        // Scale up circle
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )
    }
    
    // Auto-transition after 0.8 seconds
    LaunchedEffect(Unit) {
        delay(800)
        onNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .alpha(alpha.value),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale.value)
                .background(Color(0xFFBDBDBD), CircleShape)
                .clickable { onNext() } // Allow tap to skip
        )
    }
}

@Composable
fun ContinueLoginPasswordScreen(
    viewModel: ContinueLoginViewModel = hiltViewModel(),
    onForgotPasswordClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onLoginClick: (password: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFD700))
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Vui lòng đăng nhập\nthông tin để tiếp tục",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(18.dp))

        val nameText = when {
            uiState.isLoading -> "..."
            uiState.fullName.isNotBlank() -> uiState.fullName
            uiState.email.isNotBlank() -> uiState.email
            else -> ""
        }

        Text(
            text = if (nameText.isBlank()) "Xin chào" else "Xin chào $nameText",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(18.dp))

        CustomTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "At least 8 characters",
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            onImeAction = {
                focusManager.clearFocus()
                onLoginClick(password)
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color.Black
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Forgot Password?",
                modifier = Modifier.align(Alignment.CenterEnd).clickable { onForgotPasswordClick() },
                color = Color(0xFF2563EB),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onLoginClick(password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
        ) {
            Text(
                text = "Đăng nhập",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Black)
            }
        }

        // No Face ID button per requirement
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = uiState.error ?: "",
            color = Color.Red,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth()
        )

        // When password login succeeds, caller should call onLoginSuccess
        // via onLoginClick callback.
    }
}
