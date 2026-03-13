package com.example.vetbook.presentation.screens.auth

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.Link
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.TextSecondary
import com.example.vetbook.presentation.viewmodels.ContinueLoginViewModel
import com.example.vetbook.presentation.viewmodels.LoginViewModel
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
            .background(HealthSurface)
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
            .background(HealthSurface)
            .alpha(alpha.value),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(scale.value)
                .background(HealthPrimary.copy(alpha = 0.1f), CircleShape)
                .clickable { onNext() }, // Allow tap to skip
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                tint = HealthPrimary,
                modifier = Modifier.size(60.dp)
            )
        }
    }
}

@Composable
fun ContinueLoginPasswordScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    continueLoginViewModel: ContinueLoginViewModel = hiltViewModel(),
    onForgotPasswordClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onUseAnotherAccount: () -> Unit,
    onLoginClick: (String) -> Unit
) {
    val loginUiState by loginViewModel.uiState.collectAsState()
    val continueUiState by continueLoginViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Navigate when login is successful
    LaunchedEffect(loginUiState.isSuccess) {
        if (loginUiState.isSuccess) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthSurface)
    ) {
        // Hero Header (Shared across all Auth screens)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(HealthPrimary, HealthPrimary.copy(alpha = 0.8f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = HealthPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "VetBook",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Professional Pet Care Solutions",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Form Section (Card)
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-30.dp)),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 32.dp)
            ) {
                Text(
                    text = "Chào mừng trở lại",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                val nameText = when {
                    continueUiState.isLoading -> "..."
                    continueUiState.fullName.isNotBlank() -> continueUiState.fullName
                    continueUiState.email.isNotBlank() -> continueUiState.email
                    else -> ""
                }

                Text(
                    text = if (nameText.isBlank()) "Vui lòng xác thực tài khoản" else "Xin chào, $nameText",
                    fontSize = 16.sp,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Password Field with Label
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Mật khẩu",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Ít nhất 8 ký tự",
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
                                    contentDescription = null,
                                    tint = HealthPrimary.copy(alpha = 0.6f)
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Quên mật khẩu?",
                        modifier = Modifier.align(Alignment.CenterEnd).clickable { onForgotPasswordClick() },
                        color = Link,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                Button(
                    onClick = { onLoginClick(password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HealthPrimary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "Đăng nhập",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (loginUiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = HealthPrimary)
                    }
                }

                Text(
                    text = loginUiState.error ?: "",
                    color = Color.Red,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f))

                // Use another account button
                OutlinedButton(
                    onClick = onUseAnotherAccount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, HealthMuted.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = HealthMuted
                    )
                ) {
                    Text(
                        text = "Sử dụng tài khoản khác",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
