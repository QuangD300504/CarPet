package com.example.carpet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OtpContent(
    title: String = "Xác minh",
    email: String = "example@gmail.com",
    onVerify: () -> Unit,
    onBack: () -> Unit,
    backgroundColor: Color = Color(0xFFFFD700)
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
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
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Text(
            text = "Vui lòng nhập mã xác minh gồm 6 chữ số đã được gửi đến email $email. Lưu ý: Không chia sẻ mã này với bất kỳ ai để bảo mật tài khoản",
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(6) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onVerify,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Xác thực", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Didn't receive any code? Resend Again", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = "Request new code in 00:30s", fontSize = 12.sp, color = Color.Black.copy(alpha = 0.5f))
        }
    }
}
