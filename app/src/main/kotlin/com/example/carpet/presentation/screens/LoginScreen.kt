package com.example.carpet.presentation.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carpet.R
import com.example.carpet.presentation.components.SignInInputRow
import com.example.carpet.presentation.models.LoginUiState
import com.example.carpet.presentation.theme.VetBookTheme
import com.example.carpet.presentation.viewmodels.LoginViewModel
import kotlin.math.roundToInt

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val peekHeight = 100.dp
    
    var offsetY by remember { mutableStateOf(screenHeight - peekHeight) }
    val animatedOffsetY by animateDpAsState(
        targetValue = offsetY,
        animationSpec = tween(durationMillis = 300),
        label = "loginSwipe"
    )

    val swipeProgress = (screenHeight - animatedOffsetY - peekHeight) / (screenHeight - peekHeight)
    val textAlpha = (1f - swipeProgress * 4f).coerceIn(0f, 1f)
    val contentAlpha = ((swipeProgress - 0.2f) * 1.25f).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))
            
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFFFF3E0)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.pawns),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = buildAnnotatedString {
                    append("Welcome to ")
                    withStyle(style = SpanStyle(color = Color(0xFFFF9800))) {
                        append("VetBook")
                    }
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            
            Text(
                text = "The best place for your best friends",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, animatedOffsetY.toPx().roundToInt()) }
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        val newValue = offsetY + delta.dp
                        if (newValue >= 0.dp && newValue <= screenHeight - peekHeight) {
                            offsetY = newValue
                        }
                    },
                    onDragStopped = {
                        offsetY = if (offsetY < screenHeight / 2) 0.dp else screenHeight - peekHeight
                    }
                ),
            color = Color(0xFFFFD700),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.Black.copy(alpha = 0.2f))
                )
                
                Text(
                    text = "Swipe up to Login",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .alpha(textAlpha)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(contentAlpha),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Chào mừng bạn đến với VetBook",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text(
                        text = "Giải pháp toàn diện cho mọi nhu cầu của thú cưng - từ đặt lịch spa, khám bệnh đến mua sắm phụ kiện.",
                        fontSize = 14.sp,
                        color = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    SignInInputRow(
                        label = "Username",
                        value = uiState.username,
                        placeholder = "Example@email.com",
                        onValueChange = onUsernameChange
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SignInInputRow(
                        label = "Password",
                        value = uiState.password,
                        placeholder = "At least 8 characters",
                        isPassword = true,
                        onValueChange = onPasswordChange
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onForgotPasswordClick) {
                            Text(
                                text = "Forgot Password?",
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onLoginClick,
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A))
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text(text = "Đăng nhập", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Don't have an account? ", color = Color.Black.copy(alpha = 0.7f))
                        Text(
                            text = "Sign Up",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onSignUpClick() }
                        )
                    }
                }
            }
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
            onSignUpClick = {},
            onForgotPasswordClick = {}
        )
    }
}
