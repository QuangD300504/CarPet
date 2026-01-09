package com.example.carpet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SignInInputRow(
    label: String,
    value: String,
    placeholder: String,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.width(115.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(22.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFEEEEEE), Color(0xFFF5F5F5))
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(text = placeholder, color = Color.LightGray, fontSize = 14.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(fontSize = 14.sp, color = Color.Gray),
                singleLine = true,
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
