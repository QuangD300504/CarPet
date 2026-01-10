package com.example.vetbook.presentation.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.presentation.components.EmailVerificationContent
import com.example.vetbook.presentation.viewmodels.SignUpViewModel
import kotlinx.coroutines.delay

enum class ForgotPasswordStep {
    EnterEmail, Verify, Success
}

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableStateOf(ForgotPasswordStep.EnterEmail) }
    var email by remember { mutableStateOf("") }
    val timerSeconds by viewModel.timerSeconds.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFD700)
    ) {
        Crossfade(targetState = currentStep, label = "forgotPasswordFlow") { step ->
            when (step) {
                ForgotPasswordStep.EnterEmail -> EnterEmailContent(
                    email = email,
                    onEmailChange = { email = it },
                    onResetClick = { 
                        if (email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            viewModel.sendPasswordResetEmail(email)
                            currentStep = ForgotPasswordStep.Verify 
                        }
                    },
                    onBack = onBack
                )
                ForgotPasswordStep.Verify -> EmailVerificationContent(
                    title = "Kiểm tra Email",
                    email = email,
                    timerSeconds = timerSeconds,
                    onContinue = { 
                        // Since password reset happens on the Firebase web page, 
                        // we just move to the success screen in the app.
                        currentStep = ForgotPasswordStep.Success
                    },
                    onResendEmail = { viewModel.sendPasswordResetEmail(email) },
                    onBack = { currentStep = ForgotPasswordStep.EnterEmail }
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
    val isEmailValid = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

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
            text = "Vui lòng nhập email của bạn để nhận liên kết đặt lại mật khẩu",
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Email của bạn",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = { Text("Nhập email", fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
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
            enabled = isEmailValid,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF4444),
                disabledContainerColor = Color(0xFFEF4444).copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Đặt lại mật khẩu", color = Color.White, fontWeight = FontWeight.Bold)
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
            text = "Yêu cầu đã được gửi",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Nếu email tồn tại, bạn sẽ nhận được hướng dẫn đặt lại mật khẩu. Vui lòng đăng nhập lại sau khi đã thay đổi mật khẩu!",
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
            Text(text = "Trở về đăng nhập", color = Color.White, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.weight(0.7f))
    }
}
