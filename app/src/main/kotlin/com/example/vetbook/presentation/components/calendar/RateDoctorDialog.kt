package com.example.vetbook.presentation.components.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetbook.presentation.theme.HealthMuted
import com.example.vetbook.presentation.theme.HealthPrimary
import com.example.vetbook.presentation.theme.HealthSurface

@Composable
fun RateDoctorDialog(
    doctorName: String,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String?) -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Text(
                text = "Đánh giá bác sĩ",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = doctorName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HealthPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Bạn cảm thấy thế nào?",
                    style = MaterialTheme.typography.bodySmall,
                    color = HealthMuted
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(5) { index ->
                        IconButton(
                            onClick = { rating = index + 1 },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "Star ${index + 1}",
                                tint = if (index < rating) Color(0xFFFFC107) else HealthMuted,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Nhận xét (không bắt buộc)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HealthPrimary,
                        unfocusedBorderColor = HealthMuted.copy(alpha = 0.3f),
                        cursorColor = HealthPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rating > 0) {
                        isSubmitting = true
                        onSubmit(rating, comment.ifBlank { null })
                    }
                },
                enabled = rating > 0 && !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = HealthPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Gửi đánh giá", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Hủy", color = HealthMuted)
            }
        },
        containerColor = HealthSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
