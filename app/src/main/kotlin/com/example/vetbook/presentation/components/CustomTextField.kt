package com.example.vetbook.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.TextPrimary
import com.example.vetbook.presentation.theme.TextSecondary

/**
 * Reusable TextField component that fixes:
 * 1. Dark Mode text visibility (forces dark text on light background)
 * 2. Keyboard IME actions (Next/Done buttons with proper focus management)
 *
 * @param value Current text value
 * @param onValueChange Callback when text changes
 * @param modifier Modifier for the TextField
 * @param placeholder Placeholder text to display when empty
 * @param label Optional label text above the field
 * @param singleLine Whether the field is single line (default: true)
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param isError Whether the field has an error state
 * @param visualTransformation Visual transformation (e.g., PasswordVisualTransformation)
 * @param keyboardType Type of keyboard to show (e.g., Email, Password, Number)
 * @param imeAction IME action for the keyboard button (Next, Done, Search, etc.)
 * @param onImeAction Callback when IME action is triggered
 * @param trailingIcon Optional trailing icon composable
 * @param leadingIcon Optional leading icon composable
 * @param maxLines Maximum number of lines (default: 1 for singleLine, Int.MAX_VALUE otherwise)
 */
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { 
            Text(
                text = placeholder, 
                fontSize = 14.sp,
                color = TextSecondary.copy(alpha = 0.7f)
            ) 
        },
        label = label?.let { { Text(text = it, color = TextPrimary) } },
        singleLine = singleLine,
        enabled = enabled,
        readOnly = readOnly,
        isError = isError,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction?.invoke() },
            onDone = { onImeAction?.invoke() },
            onGo = { onImeAction?.invoke() },
            onSearch = { onImeAction?.invoke() },
            onSend = { onImeAction?.invoke() }
        ),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            // Tinted light background
            focusedContainerColor = HealthPrimary.copy(alpha = 0.05f),
            unfocusedContainerColor = HealthPrimary.copy(alpha = 0.03f),
            disabledContainerColor = HealthPrimary.copy(alpha = 0.02f),
            
            // Health theme text colors
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            disabledTextColor = TextPrimary.copy(alpha = 0.6f),
            
            // Cursor and selection
            cursorColor = HealthPrimary,
            selectionColors = androidx.compose.foundation.text.selection.TextSelectionColors(
                handleColor = HealthPrimary,
                backgroundColor = HealthPrimary.copy(alpha = 0.2f)
            ),
            
            // Border colors
            focusedBorderColor = HealthPrimary.copy(alpha = 0.3f),
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color(0xFFEF4444)
        )
    )
}
