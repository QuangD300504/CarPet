package com.example.vetbook.presentation.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.presentation.components.CustomTextField
import com.example.vetbook.presentation.models.LoginUiState
import com.example.vetbook.presentation.theme.VetBookTheme
import com.example.vetbook.presentation.viewmodels.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
        }
    }

    LoginContent(
        uiState = uiState,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::onLoginClick,
        onGoogleLoginClick = { viewModel.onGoogleSignInClick(context) },
        onSignUpClick = onSignUpClick,
        onForgotPasswordClick = onForgotPasswordClick
    )
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFD700))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.LightGray.copy(alpha = 0.5f))
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Chào mừng bạn đến với VetBook",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Giải pháp toàn diện cho mọi nhu cầu của thú cưng – từ đặt lịch spa, khám bệnh đến mua sắm phụ kiện. Tất cả đều sẵn sàng chỉ trong vài bước chạm",
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Email", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            CustomTextField(
                value = uiState.username,
                onValueChange = onUsernameChange,
                placeholder = "Example@email.com",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Password", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            CustomTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                placeholder = "At least 8 characters",
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                onImeAction = { 
                    focusManager.clearFocus()
                    onLoginClick()
                },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = Color.Black)
                    }
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Forgot Password?",
                color = Color(0xFF2563EB),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp).clickable { onForgotPasswordClick() }
            )
        }

        uiState.error?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLoginClick,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text(text = "Đăng nhập", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Black.copy(alpha = 0.2f))
            Text(
                text = "Or sign in with",
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 12.sp,
                color = Color.Black.copy(alpha = 0.5f)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Black.copy(alpha = 0.2f))
        }

        Spacer(modifier = Modifier.height(32.dp))

        SocialLoginButton(
            text = "Google",
            iconRes = null,
            modifier = Modifier.fillMaxWidth(),
            onClick = onGoogleLoginClick
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Don't you have an account? ", color = Color.Black.copy(alpha = 0.7f), fontSize = 14.sp)
            Text(
                text = "Sign up",
                color = Color(0xFF2563EB),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onSignUpClick() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SocialLoginButton(
    text: String,
    iconRes: Int?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (iconRes != null) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Box(modifier = Modifier.size(20.dp).background(Color.LightGray, CircleShape))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    VetBookTheme {
        LoginContent(
            uiState = LoginUiState(),
            onUsernameChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onGoogleLoginClick = {},
            onSignUpClick = {},
            onForgotPasswordClick = {}
        )
    }
}
