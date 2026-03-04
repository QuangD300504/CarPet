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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.presentation.components.CustomTextField
import com.example.vetbook.presentation.models.LoginUiState
import com.example.vetbook.presentation.theme.Brand
import com.example.vetbook.presentation.theme.BrandSurface
import com.example.vetbook.presentation.theme.VetBookTheme
import com.example.vetbook.presentation.viewmodels.LoginViewModel

private val DarkCardColor = Color(0xFF1E293B)
private val AuthBackground = Color(0xFFF7F8FA)

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
            .background(AuthBackground)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Hero header with Brand gradient ──────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Brand, Brand.copy(alpha = 0.75f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // App logo placeholder — white circle with paw
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐾", fontSize = 36.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "CarPet",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Your pet's best companion",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        // ── White card for form ───────────────────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-20).dp),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = Color.White,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome back",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Vui lòng đăng nhập thông tin để tiếp tục",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Email field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Email",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CustomTextField(
                        value = uiState.username,
                        onValueChange = onUsernameChange,
                        placeholder = "example@email.com",
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Password field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Password",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
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
                                Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
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
                        color = Brand,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .clickable { onForgotPasswordClick() }
                    )
                }

                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Login button
                Button(
                    onClick = onLoginClick,
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCardColor)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                    } else {
                        Text(text = "Đăng nhập", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                    Text(
                        text = "Or sign in with",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                SocialLoginButton(
                    text = "Continue with Google",
                    iconRes = null,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onGoogleLoginClick
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Don't have an account? ",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Sign up",
                        color = Brand,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onSignUpClick() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SocialLoginButton(
    text: String,
    iconRes: Int?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (iconRes != null) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                // Google "G" placeholder
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFF4285F4), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("G", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, color = Color(0xFF1E293B), fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
