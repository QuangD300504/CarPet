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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.vetbook.presentation.theme.Brand
import com.example.vetbook.presentation.theme.VetBookTheme
import com.example.vetbook.presentation.viewmodels.SignUpViewModel
import kotlinx.coroutines.delay

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
        color = Color.White
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
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "Chào mừng bạn đến với VetBook",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Giải pháp toàn diện cho mọi nhu cầu của thú cưng – từ đặt lịch spa, khám bệnh đến mua sắm phụ kiện. Tất cả đều sẵn sàng chỉ trong vài bước chạm",
            fontSize = 13.sp,
            color = Color.Black.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        CustomTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            placeholder = "Tên đầy đủ",
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        CustomTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = "Địa chỉ Email",
            leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, tint = Color.Gray) },
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        CustomTextField(
            value = phoneNumber,
            onValueChange = onPhoneChange,
            placeholder = "Số điện thoại",
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray) },
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        CustomTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "Mật khẩu",
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            onImeAction = {
                focusManager.clearFocus()
                if (isTermsAccepted) onNext()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isTermsAccepted,
                onCheckedChange = onTermsChecked,
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEF4444))
            )
            Text(
                text = "Bằng việc đánh dấu vào ô bên dưới, bạn xác nhận đã đồng ý với Điều khoản và Điều kiện của chúng tôi.",
                fontSize = 11.sp,
                color = Color.Black.copy(alpha = 0.6f)
            )
        }

        if (errorMessage != null) {
            Text(text = errorMessage, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(text = "Tiếp tục", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Text(text = "Already a member? ", color = Color.Black.copy(alpha = 0.7f))
            Text(
                text = "Log In",
                color = Brand,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onLoginClick() }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun WelcomeContent(onFinished: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        
        Text(
            text = "Welcome",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.LightGray.copy(alpha = 0.4f))
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Tài khoản của bạn đã sẵn sàng. Hãy để chúng mình cùng bạn chăm sóc và mang lại những điều tuyệt vời nhất cho những người bạn bốn chân nhé! 🐾",
            fontSize = 15.sp,
            color = Color.Black.copy(alpha = 0.8f),
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier.size(48.dp).padding(bottom = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.Black, strokeWidth = 3.dp)
        }
        
        LaunchedEffect(Unit) {
            delay(2000)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            val page = pages[pageIndex]
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f))
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = page.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = page.description,
                    fontSize = 16.sp,
                    color = Color.Black.copy(alpha = 0.6f),
                    lineHeight = 24.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Page Indicator
        Row(
            Modifier
                .height(50.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) Brand else Brand.copy(alpha = 0.3f)
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(12.dp)
                )
            }
        }

        if (pagerState.currentPage == pages.size - 1) {
            Button(
                onClick = onFinished,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Bắt đầu ngay", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        } else {
            Spacer(modifier = Modifier.height(56.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Legacy SignUpTextField removed - now using CustomTextField globally

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    VetBookTheme {
        SignUpScreen()
    }
}
