package com.example.carpet.presentation.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carpet.presentation.components.OtpContent
import kotlinx.coroutines.delay

enum class ForgotPasswordStep {
    EnterEmail, VerifyOTP, ResetPassword, Loading, Success
}

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit
) {
    var currentStep by remember { mutableStateOf(ForgotPasswordStep.EnterEmail) }
    var email by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFD700)
    ) {
        Crossfade(targetState = currentStep, label = "forgotPasswordFlow") { step ->
            when (step) {
                ForgotPasswordStep.EnterEmail -> EnterEmailContent(
                    email = email,
                    onEmailChange = { email = it },
                    onResetClick = { currentStep = ForgotPasswordStep.VerifyOTP },
                    onBack = onBack
                )
                ForgotPasswordStep.VerifyOTP -> OtpContent(
                    title = "Xác minh",
                    email = email,
                    onVerify = { currentStep = ForgotPasswordStep.ResetPassword },
                    onBack = { currentStep = ForgotPasswordStep.EnterEmail }
                )
                ForgotPasswordStep.ResetPassword -> ResetPasswordContent(
                    onComplete = { currentStep = ForgotPasswordStep.Loading },
                    onBack = { currentStep = ForgotPasswordStep.VerifyOTP }
                )
                ForgotPasswordStep.Loading -> LoadingContent(
                    onFinished = { currentStep = ForgotPasswordStep.Success }
                )
                ForgotPasswordStep.Success -> SuccessContent(
                    onBackToLogin = onBack
                )
            }
        }
    }
}

@Composable
fun EnterEmailContent(
    email: String,
    onEmailChange: (String) -> Unit,
    onResetClick: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back", tint = Color.Black)
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Quên mật khẩu",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Text(
            text = "Please enter your email to reset the password",
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Your Email",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = { Text("Enter your email", fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onResetClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Đặt lại mật khẩu", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ResetPasswordContent(onComplete: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back", tint = Color.Black)
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Đặt lại mật khẩu mới",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Text(
            text = "Create a new password. Ensure it differs from previous ones for security",
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        PasswordField(label = "Password", placeholder = "Enter your new password")
        Spacer(modifier = Modifier.height(16.dp))
        PasswordField(label = "Confirm Password", placeholder = "Re-enter password")

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Hoàn tất", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PasswordField(label: String, placeholder: String) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text(placeholder, fontSize = 14.sp) },
            trailingIcon = { Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun LoadingContent(onFinished: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(100.dp),
            color = Color(0xFF311B92),
            strokeWidth = 8.dp,
            trackColor = Color(0xFFEF4444)
        )
        
        LaunchedEffect(Unit) {
            delay(2000)
            onFinished()
        }
    }
}

@Composable
fun SuccessContent(onBackToLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.3f))
        
        Text(
            text = "Đặt lại mật khẩu thành công",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Mật khẩu của bạn đã được thay đổi, nhấn trở về để đăng nhập!",
            fontSize = 16.sp,
            color = Color.Black.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Trở về", color = Color.White, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.weight(0.7f))
    }
}
