package com.example.vetbook.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

/**
 * Legacy Sign-In Input Row component (using BasicTextField).
 * 
 * Note: Consider migrating to CustomTextField for better consistency.
 * This component has been updated to fix:
 * 1. Dark text color forced for light background
 * 2. IME Actions support (Next/Done)
 */
@Composable
fun SignInInputRow(
    label: String,
    value: String,
    placeholder: String,
    isPassword: Boolean = false,
    imeAction: ImeAction = ImeAction.Default,
    onValueChange: (String) -> Unit,
    onImeAction: (() -> Unit)? = null
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
                Text(text = placeholder, color = Color(0xFF9CA3AF), fontSize = 14.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontSize = 14.sp, 
                    color = Color.Black // FIXED: Force black text for light background
                ),
                cursorBrush = SolidColor(Color.Black), // FIXED: Black cursor
                singleLine = true,
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text,
                    imeAction = imeAction
                ),
                keyboardActions = KeyboardActions(
                    onNext = { onImeAction?.invoke() },
                    onDone = { onImeAction?.invoke() },
                    onGo = { onImeAction?.invoke() }
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
