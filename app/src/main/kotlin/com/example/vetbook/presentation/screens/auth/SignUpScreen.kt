package com.example.vetbook.presentation.screens.auth

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vetbook.presentation.components.CustomTextField
import com.example.vetbook.presentation.components.EmailVerificationContent
import com.example.vetbook.presentation.models.SignUpUiState
import com.example.vetbook.presentation.theme.VetBookTheme
import com.example.vetbook.presentation.viewmodels.SignUpViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.filled.Pets
import androidx.compose.ui.text.style.TextAlign
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.TextSecondary

enum class SignUpStep {
    Details, Verify, Welcome, Introduction
}

@Composable
fun SignUpScreen(
    onLoginClick: () -> Unit = {},
    onSignUpComplete: () -> Unit = {},
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    var currentStep by remember { mutableStateOf(SignUpStep.Details) }

    LaunchedEffect(uiState) {
        if (uiState is SignUpUiState.Success && currentStep == SignUpStep.Details) {
            currentStep = SignUpStep.Verify
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = HealthSurface
    ) {
        Crossfade(targetState = currentStep, label = "signUpFlow") { step ->
            when (step) {
                SignUpStep.Details -> SignUpDetailsContent(
                    fullName = formState.fullName,
                    email = formState.email,
                    phoneNumber = formState.phoneNumber,
                    password = formState.password,
                    isTermsAccepted = formState.isTermsAccepted,
                    isLoading = uiState is SignUpUiState.Loading,
                    errorMessage = formState.errorMessage,
                    onFullNameChange = viewModel::onFullNameChange,
                    onEmailChange = viewModel::onEmailChange,
                    onPhoneChange = viewModel::onPhoneChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onTermsChecked = viewModel::onTermsChecked,
                    onNext = { viewModel.onSignUpClick() },
                    onLoginClick = onLoginClick
                )
                SignUpStep.Verify -> EmailVerificationContent(
                    title = "Xác minh",
                    email = formState.email,
                    timerSeconds = timerSeconds,
                    errorMessage = formState.errorMessage,
                    successMessage = formState.successMessage,
                    onContinue = { 
                        viewModel.checkVerification {
                            currentStep = SignUpStep.Welcome
                        }
                    },
                    onResendEmail = { viewModel.resendVerificationEmail() },
                    onBack = { currentStep = SignUpStep.Details }
                )
                SignUpStep.Welcome -> WelcomeContent(
                    onFinished = { currentStep = SignUpStep.Introduction }
                )
                SignUpStep.Introduction -> IntroductionContent(
                    onFinished = onSignUpComplete
                )
            }
        }
    }
}

@Composable
fun SignUpDetailsContent(
    fullName: String,
    email: String,
    phoneNumber: String,
    password: String,
    isTermsAccepted: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTermsChecked: (Boolean) -> Unit,
    onNext: () -> Unit,
    onLoginClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero header
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

        // Signup Form Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-30.dp)),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tham gia cộng đồng VetBook",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Để lại thông tin để chúng mình có thể hỗ trợ bạn và thú cưng tốt nhất.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Tên đầy đủ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(
                        value = fullName,
                        onValueChange = onFullNameChange,
                        placeholder = "VD: Nguyễn Văn A",
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = HealthPrimary.copy(alpha = 0.6f)) },
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Địa chỉ Email", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        placeholder = "VD: alex@petcare.com",
                        leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, tint = HealthPrimary.copy(alpha = 0.6f)) },
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Số điện thoại", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(
                        value = phoneNumber,
                        onValueChange = onPhoneChange,
                        placeholder = "VD: 0123 456 789",
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = HealthPrimary.copy(alpha = 0.6f)) },
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Mật khẩu", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        placeholder = "Ít nhất 8 ký tự",
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = HealthPrimary.copy(alpha = 0.6f)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        onImeAction = {
                            focusManager.clearFocus()
                            if (isTermsAccepted) onNext()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = isTermsAccepted, onCheckedChange = onTermsChecked, colors = CheckboxDefaults.colors(checkedColor = HealthPrimary))
                    Text(text = "Tôi đồng ý với Điều khoản và Chính sách bảo mật", fontSize = 12.sp, color = TextSecondary)
                }

                if (errorMessage != null) {
                    Text(text = errorMessage, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onNext,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HealthPrimary),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(text = "Đăng ký ngay", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text(text = "Đã có tài khoản? ", color = TextSecondary)
                    Text(text = "Đăng nhập", color = HealthPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onLoginClick() })
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun WelcomeContent(onFinished: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthSurface)
    ) {
        // Hero Header
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
            }
        }

        // Welcome Card
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
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Chào mừng bạn!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(HealthPrimary.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = HealthPrimary.copy(alpha = 0.3f),
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Tài khoản của bạn đã sẵn sàng. Hãy để chúng mình cùng bạn chăm sóc và mang lại những điều tuyệt vời nhất cho những người bạn bốn chân nhé! 🐾",
                    fontSize = 15.sp,
                    color = TextSecondary,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier.padding(bottom = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = HealthPrimary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
        
        LaunchedEffect(Unit) {
            delay(2500)
            onFinished()
        }
    }
}

data class IntroPage(val title: String, val description: String)

@Composable
fun IntroductionContent(onFinished: () -> Unit) {
    val pages = listOf(
        IntroPage(
            "Chăm sóc bé yêu toàn diện",
            "Từ lịch tiêm phòng, theo dõi sức khỏe đến chế độ dinh dưỡng – tất cả đều được gói gọn trong tầm tay bạn."
        ),
        IntroPage(
            "Đặt lịch dịch vụ trong tích tắc",
            "Tìm kiếm và đặt chỗ tại các spa, cửa hàng làm đẹp uy tín nhất gần bạn. Để “Boss” luôn xinh đẹp và thơm tho!"
        ),
        IntroPage(
            "Thế giới dành riêng cho thú cưng",
            "Mua sắm phụ kiện cực chất và kết nối với cộng đồng những người yêu động vật giống như bạn."
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthSurface)
    ) {
        // Hero Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(HealthPrimary, HealthPrimary.copy(alpha = 0.8f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Khám phá VetBook",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Introduction Card
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-20.dp)),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { pageIndex ->
                    val page = pages[pageIndex]
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(HealthPrimary.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = null,
                                tint = HealthPrimary.copy(alpha = 0.2f),
                                modifier = Modifier.size(80.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = page.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = page.description,
                            fontSize = 15.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Page Indicator
                Row(
                    Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pages.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) HealthPrimary else HealthPrimary.copy(alpha = 0.2f)
                        val widthBuffer = if (pagerState.currentPage == iteration) 24.dp else 8.dp
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .height(8.dp)
                                .width(widthBuffer)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (pagerState.currentPage == pages.size - 1) {
                            onFinished()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pagerState.currentPage == pages.size - 1) HealthPrimary else HealthPrimary.copy(alpha = 0.1f),
                        contentColor = if (pagerState.currentPage == pages.size - 1) Color.White else HealthPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = if (pagerState.currentPage == pages.size - 1) 4.dp else 0.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.size - 1) "Bắt đầu ngay" else "Tiếp tục khám phá",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    VetBookTheme {
        SignUpScreen()
    }
}
